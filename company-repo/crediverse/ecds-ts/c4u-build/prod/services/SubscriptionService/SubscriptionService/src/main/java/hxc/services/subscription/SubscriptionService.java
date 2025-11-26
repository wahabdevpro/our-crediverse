package hxc.services.subscription;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SubscribeResponse;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.VasServiceInfo;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.air.AirException;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.air.proxy.Subscriber;
import hxc.connectors.vas.VasService;
import hxc.servicebus.ILocale;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ReturnCodes;
import hxc.services.IService;
import hxc.services.notification.INotifications;
import hxc.services.notification.Phrase;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.transactions.CsvCdr;
import hxc.services.transactions.ITransaction;
import hxc.services.transactions.ITransactionService;
import hxc.services.transactions.Transaction;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.notification.Notifications;

public class SubscriptionService extends VasService implements IService
{
	final static Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Internal Properties
	//
	// /////////////////////////////////
	protected IServiceBus esb;
	protected ITransactionService transactions;
	protected IAirConnector air;
	protected ILocale locale;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IService Implementation
	//
	// /////////////////////////////////

	@Override
	public void initialise(IServiceBus esb)
	{
		this.esb = esb;

	}

	@Override
	public boolean start(String[] args)
	{
		// Must have Transaction Service
		transactions = esb.getFirstService(ITransactionService.class);
		if (transactions == null)
			return false;

		// Must have Air
		air = esb.getFirstConnector(IAirConnector.class);
		if (air == null)
			return false;

		// Get Locale
		this.locale = esb.getLocale();

		// Log Information
		logger.info("Subscription Service Started");

		return true;
	}

	@Override
	public void stop()
	{
		// Log Information
		logger.info("Subscription Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (SubscriptionConfig) config;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable Parameters
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewSubscriptionParameters", description = "View Subscription Parameters", category = "Subscription", supplier = true),
			@Perm(name = "ChangeSubscriptionParameters", implies = "ViewSubscriptionParameters", description = "Change Subscription Parameters", category = "Subscription", supplier = true),
			@Perm(name = "ViewSubscriptionNotifications", description = "View Subscription Notifications", category = "Subscription", supplier = true),
			@Perm(name = "ChangeSubscriptionNotifications", implies = "ViewSubscriptionNotifications", description = "Change Subscription Notifications", category = "Subscription", supplier = true) })
	public class SubscriptionConfig extends ConfigurationBase
	{
		protected Phrase serviceName = Phrase.en("Subscription");

		protected Variant[] variants = new Variant[] { new Variant("Au", 76, Phrase.en("C4U Gold").fre("C4U Gold")), //
				new Variant("Ag", 75, Phrase.en("C4U Silver").fre("C4U Silver")), //
		};

		public Phrase getServiceName()
		{
			check(esb, "ViewSubscriptionParameters");
			return serviceName;
		}

		public void setServiceName(Phrase serviceName)
		{
			check(esb, "ChangeSubscriptionParameters");
			this.serviceName = serviceName;
		}

		public Variant[] getVariants()
		{
			check(esb, "ViewSubscriptionParameters");
			return variants;
		}

		public void setVariants(Variant[] variants)
		{
			check(esb, "ChangeSubscriptionParameters");
			this.variants = variants;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "VAS Services";
		}

		@Override
		public INotifications getNotifications()
		{
			return notifications;
		}

		@Override
		public long getSerialVersionUID()
		{
			return -2858014763078107169L;
		}

		@Override
		public String getName(String languageCode)
		{
			return serviceName.getSafe(languageCode, "Subscription");
		}

		@Override
		public void validate() throws ValidationException
		{
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeSubscriptionNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewSubscriptionNotifications");
		}

	}

	SubscriptionConfig config = new SubscriptionConfig();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Notifications
	//
	// /////////////////////////////////
	private Notifications notifications = new Notifications(Properties.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	class Properties
	{
		protected String charge = "0";

		public String getCharge()
		{
			return charge;
		}

		public void setCharge(String charge)
		{
			this.charge = charge;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// VasService Implementation
	//
	// /////////////////////////////////

	@Override
	public String getServiceID()
	{
		return "SUBS";
	}

	@Override
	public String getServiceName(String languageCode)
	{
		return config.getName(languageCode);
	}

	@Override
	public VasServiceInfo[] getServiceInfo(IServiceContext context, Number subscriberNumber, String variantID, Integer languageID, boolean activeOnly, boolean suggested)
	{
		List<VasServiceInfo> results = new ArrayList<VasServiceInfo>();
		int currentServiceClass = -1;

		if (subscriberNumber != null)
		{
			Subscriber subscriber = getSubscriber(context, subscriberNumber, null);
			currentServiceClass = subscriber.getServiceClass();
		}

		for (Variant variant : config.variants)
		{
			if (activeOnly && variant.getServiceClassID() != currentServiceClass)
				continue;

			VasServiceInfo result = new VasServiceInfo();
			result.setServiceName(getServiceName(esb.getLocale().getLanguage(languageID)));
			result.setServiceID(getServiceID());
			result.setVariantID(variant.getVariantID());
			String languageCode = esb.getLocale().getLanguage(languageID);
			result.setVariantName(variant.getNames().getSafe(languageCode, variant.getVariantID()));
			if (subscriberNumber != null)
				result.setState(variant.getServiceClassID() == currentServiceClass ? SubscriptionState.active : SubscriptionState.notActive);
			else
				result.setState(SubscriptionState.unknown);
			results.add(result);
		}

		return results.toArray(new VasServiceInfo[results.size()]);
	}

	@Override
	public SubscribeResponse subscribe(IServiceContext context, SubscribeRequest request)
	{
		// Prepare an Response
		SubscribeResponse response = super.subscribe(context, request);

		// Create a CDR
		CsvCdr cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Transaction Reversal Scope
		try (Transaction<?> transaction = transactions.create(cdr, null))
		{
			// Validate Request
			String problem = SubscribeRequest.validate(request);
			if (problem != null)
				return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

			// Update CDR
			cdr.setServiceID(request.getServiceID());
			cdr.setVariantID(request.getVariantID());
			cdr.setA_MSISDN(request.getSubscriberNumber().toMSISDN());
			cdr.setProcessID("subscribe");

			// Get Subscriber Proxy
			Subscriber subscriber = getSubscriber(context, request.getSubscriberNumber(), transaction);
			try
			{
				// Set Properties
                // FIXME -- MAJOR SIDE EFFECTS ..... this getProperties(context) has serious side effects ... should be using setProperties explicitly!!!!!
				getProperties(context);

				// Get the Variant
				Variant variant = Variant.getVariant(config.variants, request.getVariantID());
				if (variant == null)
					return response.exitWith(cdr, ReturnCodes.invalidVariant, "Invalid Variant");

				// Perform the Installation
				transaction.track(this, "UpdateAccount");
				subscriber.install(variant.getServiceClassID());

				// Complete the Transaction
				transaction.complete();

			}
			catch (AirException e)
			{
				if (e.getResponseCode() == 142)
					return response.exitWith(cdr, ReturnCodes.alreadySubscribed, e);
				else
					return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
			}

			// Complete
			transaction.complete();
			return response.exitWith(cdr, ReturnCodes.success, "Success");
		}
		catch (Throwable e)
		{
			logger.error(e.getMessage(), e);
			return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private Subscriber getSubscriber(IServiceContext context, Number subscriberNumber, ITransaction transaction)
	{
		Subscriber result;
		if (context.getSubscriberProxy() != null && context.getSubscriberProxy() instanceof Subscriber //
				&& context.getSubscriberProxy().isSameNumber(subscriberNumber.toMSISDN()))
		{
			result = (Subscriber) context.getSubscriberProxy();
			result.setTransaction(transaction);
		}
		else
		{
			result = new Subscriber(subscriberNumber.toMSISDN(), air, transaction);
			context.setSubscriberProxy(result);
		}

		return result;
	}

	private Properties getProperties(IServiceContext context)
	{
		Object properties = context.getProperties();
		if (properties == null || !(properties instanceof Properties))
		{
			properties = new Properties();
			context.setProperties(properties);
		}

		return (Properties) properties;
	}

}
