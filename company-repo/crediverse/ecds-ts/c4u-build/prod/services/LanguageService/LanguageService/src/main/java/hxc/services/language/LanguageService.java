package hxc.services.language;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.IServiceContext;
import com.concurrent.hxc.MigrateRequest;
import com.concurrent.hxc.MigrateResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.SubscriptionState;
import com.concurrent.hxc.VasServiceInfo;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.air.AirException;
import hxc.connectors.air.IAirConnector;
import hxc.connectors.air.proxy.AccountUpdate;
import hxc.connectors.air.proxy.Subscriber;
import hxc.connectors.vas.VasService;
import hxc.servicebus.ILocale;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.services.IService;
import hxc.services.notification.INotifications;
import hxc.services.notification.IPhrase;
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

public class LanguageService extends VasService implements IService
{
	final static Logger logger = LoggerFactory.getLogger(LanguageService.class);
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
		logger.info("Language Change Service Started");

		return true;
	}

	@Override
	public void stop()
	{
		// Log Information
		logger.info("Language Change Service Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (LanguageChangeConfig) config;
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
	@Perms(perms = {
			@Perm(name = "ViewLanguageChangeParameters", description = "View Language Change Parameters", category = "LanguageChange", supplier = true),
			@Perm(name = "ChangeLanguageChangeParameters", implies = "ViewLanguageChangeParameters", description = "Change Language Change Parameters", category = "LanguageChange", supplier = true),
			@Perm(name = "ViewLanguageChangeNotifications", description = "View Language Change Notifications", category = "LanguageChange", supplier = true),
			@Perm(name = "ChangeLanguageChangeNotifications", implies = "ViewLanguageChangeNotifications", description = "Change Language Change Notifications", category = "LanguageChange", supplier = true) })
	public class LanguageChangeConfig extends ConfigurationBase
	{
		private int migrationCharge = 0;
		protected int revenueDedicatedAccountID = 9999;

		protected Phrase serviceName = Phrase.en("Language Change").fre("Changement de langue");

		public int getMigrationCharge()
		{
			check(esb, "ViewLanguageChangeParameters");
			return migrationCharge;
		}

		public void setMigrationCharge(int migrationCharge)
		{
			check(esb, "ChangeLanguageChangeParameters");
			this.migrationCharge = migrationCharge;
		}

		public int getRevenueDedicatedAccountID()
		{
			check(esb, "ViewLanguageChangeParameters");
			return revenueDedicatedAccountID;
		}

		public void setRevenueDedicatedAccountID(int revenueDedicatedAccountID)
		{
			check(esb, "ChangeLanguageChangeParameters");
			this.revenueDedicatedAccountID = revenueDedicatedAccountID;
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
			return -6108431840841914523L;
		}

		@Override
		public String getName(String languageCode)
		{
			return serviceName.getSafe(languageCode, "Language Change");
		}

		@Override
		public void validate() throws ValidationException
		{
		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
			check(esb, "ChangeLanguageChangeNotifications");
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{
			check(esb, "ViewLanguageChangeNotifications");
		}

	}

	LanguageChangeConfig config = new LanguageChangeConfig();

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
		return "LangCh";
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

		int currentLanguageIndex = -1;
		if (subscriberNumber != null)
		{
			Subscriber subscriber = getSubscriber(context, subscriberNumber, null);
			currentLanguageIndex = subscriber.getLanguageID();
		}

		for (int languageIndex = 1; languageIndex <= IPhrase.MAX_LANGUAGES; languageIndex++)
		{
			String language = esb.getLocale().getLanguage(languageIndex);
			if (language == null || language.length() == 0)
				continue;

			if (activeOnly && languageIndex != currentLanguageIndex)
				continue;

			VasServiceInfo serviceInfo = new VasServiceInfo();
			serviceInfo.setServiceName(getServiceName(esb.getLocale().getLanguage(languageID)));
			serviceInfo.setServiceID(getServiceID());
			serviceInfo.setVariantID(language);
			serviceInfo.setVariantName(esb.getLocale().getLanguageName(languageIndex));
			if (currentLanguageIndex < 0)
				serviceInfo.setState(SubscriptionState.unknown);
			else
				serviceInfo.setState(languageIndex == currentLanguageIndex ? SubscriptionState.active : SubscriptionState.notActive);
			results.add(serviceInfo);
		}

		return results.toArray(new VasServiceInfo[results.size()]);
	}

	@Override
	public MigrateResponse migrate(IServiceContext context, MigrateRequest request)
	{
		// Prepare an Response
		MigrateResponse response = super.migrate(context, request);

		// Create a CDR
		CsvCdr cdr = new CsvCdr(request, esb.getNextTransactionNumber(Subscriber.TRANSACTION_ID_LENGTH));

		// Transaction Reversal Scope
		try (Transaction<?> transaction = transactions.create(cdr, null))
		{
			// Validate Request
			String problem = MigrateRequest.validate(request);
			if (problem != null)
				return response.exitWith(cdr, ReturnCodes.malformedRequest, problem);

			// Must be to the Same Service
			if (!request.getNewServiceID().equalsIgnoreCase(getServiceID()))
				return response.exitWith(cdr, ReturnCodes.malformedRequest, "NoServiceChange");

			// Get the Old/New Language IDs
			int oldLanguageID = getLanguageID(request.getVariantID());
			int newLanguageID = getLanguageID(request.getNewVariantID());

			// Must be between 1 and MAX_LANUGAGES
			if (oldLanguageID < 1 || oldLanguageID > IPhrase.MAX_LANGUAGES || newLanguageID < 1 || newLanguageID > IPhrase.MAX_LANGUAGES)
				return response.exitWith(cdr, ReturnCodes.malformedRequest, "BadLanguageID");

			// Update CDR
			cdr.setServiceID(request.getServiceID());
			cdr.setVariantID(request.getNewVariantID());
			cdr.setA_MSISDN(request.getSubscriberNumber().toMSISDN());
			cdr.setProcessID("migrate");

			// Get Subscriber Proxy
			Subscriber subscriber = getSubscriber(context, request.getSubscriberNumber(), transaction);
			try
			{
				// Set Properties
				Properties properties = getProperties(context);

				// Check if already new Language
				transaction.track(this, "CheckAlreadySubscribed");
				if (newLanguageID == subscriber.getLanguageID())
					return response.exitWith(cdr, ReturnCodes.alreadySubscribed, "MigrateToSameLanguage");

				// Deduct Subscription Fee
				long migrationCharge = config.getMigrationCharge();

				if (request.getMode() == RequestModes.testOnly)
				{
					cdr.setChargeLevied((int) migrationCharge);
					response.setChargeLevied(migrationCharge);
					properties.setCharge(locale.formatCurrency(migrationCharge));
					transaction.complete();
					return response.exitWith(cdr, ReturnCodes.successfulTest, "Success");
				}

				if (migrationCharge != 0)
				{

					// Check if subscriber has sufficient funds
					transaction.track(this, "CheckSufficientFunds");
					if (subscriber.getAccountValue1() < migrationCharge)
						return response.exitWith(cdr, ReturnCodes.insufficientBalance, "InsufficientBalance");

					// Debit subscriber
					transaction.track(this, "DebitMainAccount");
					AccountUpdate updateRevenueDA = new AccountUpdate(config.revenueDedicatedAccountID, Subscriber.DATYPE_MONEY, migrationCharge, null, null, null, null, null);
					subscriber.updateAccounts(-migrationCharge, updateRevenueDA);
					response.setChargeLevied(migrationCharge);
					cdr.setChargeLevied((int) migrationCharge);
				}

				// Perform the Migration
				transaction.track(this, "UpdateAccount");
				subscriber.updateAccountDetails(Integer.valueOf(newLanguageID), null, null, null, null, null, null);

				// Complete the Transaction
				transaction.complete();

			}
			catch (AirException e)
			{
				return response.exitWith(cdr, ReturnCodes.technicalProblem, e);
			}

			// Complete
			transaction.complete();
			return response.exitWith(cdr, ReturnCodes.success, "Success");
		}
		catch (Throwable ex)
		{
			logger.error("migrate error", ex);
			return response.exitWith(cdr, ReturnCodes.technicalProblem, ex);
		}

	}

	private int getLanguageID(String variantID)
	{
		if (variantID == null || variantID.length() == 0)
			return 0;

		for (int languageIndex = 1; languageIndex <= IPhrase.MAX_LANGUAGES; languageIndex++)
		{
			if (variantID.equals(Integer.toString(languageIndex)))
				return languageIndex;

			if (variantID.equalsIgnoreCase(esb.getLocale().getLanguage(languageIndex)))
				return languageIndex;

			if (variantID.equalsIgnoreCase(esb.getLocale().getLanguageName(languageIndex)))
				return languageIndex;

		}

		return 0;
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
