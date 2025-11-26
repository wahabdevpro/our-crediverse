package hxc.services.airsim;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jws.WebService;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import hxc.connectors.snmp.IAlarm;
import hxc.servicebus.IServiceBus;
import hxc.services.airsim.engine.AddPeriodicAccountManagementData;
import hxc.services.airsim.engine.DeleteAccumulators;
import hxc.services.airsim.engine.DeleteOffer;
import hxc.services.airsim.engine.DeletePeriodicAccountManagementData;
import hxc.services.airsim.engine.DeleteUsageThresholds;
import hxc.services.airsim.engine.GetAccountDetails;
import hxc.services.airsim.engine.GetAccumulators;
import hxc.services.airsim.engine.GetBalanceAndDate;
import hxc.services.airsim.engine.GetFaFList;
import hxc.services.airsim.engine.GetOffers;
import hxc.services.airsim.engine.GetSubscriberInformation;
import hxc.services.airsim.engine.GetUsageThresholdsAndCounters;
import hxc.services.airsim.engine.InstallSubscriber;
import hxc.services.airsim.engine.Refill;
import hxc.services.airsim.engine.RunPeriodicAccountManagement;
import hxc.services.airsim.engine.UpdateAccountDetails;
import hxc.services.airsim.engine.UpdateAccumulators;
import hxc.services.airsim.engine.UpdateBalanceAndDate;
import hxc.services.airsim.engine.UpdateCommunityList;
import hxc.services.airsim.engine.UpdateFaFList;
import hxc.services.airsim.engine.UpdateOffer;
import hxc.services.airsim.engine.UpdatePeriodicAccountManagementData;
import hxc.services.airsim.engine.UpdateServiceClass;
import hxc.services.airsim.engine.UpdateSubDedicatedAccounts;
import hxc.services.airsim.engine.UpdateSubscriberSegmentation;
import hxc.services.airsim.engine.UpdateUsageThresholdsAndCounters;
import hxc.services.airsim.model.OfferEx;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.Accumulator;
import hxc.services.airsim.protocol.AirCalls;
import hxc.services.airsim.protocol.Alarm;
import hxc.services.airsim.protocol.CallHistory;
import hxc.services.airsim.protocol.Cdr;
import hxc.services.airsim.protocol.DedicatedAccount;
import hxc.services.airsim.protocol.FafEntry;
import hxc.services.airsim.protocol.Filter;
import hxc.services.airsim.protocol.GetUsageResponse;
import hxc.services.airsim.protocol.IAirSim;
import hxc.services.airsim.protocol.ICdr;
import hxc.services.airsim.protocol.IFilter;
import hxc.services.airsim.protocol.ILifecycle;
import hxc.services.airsim.protocol.ISmsHistory;
import hxc.services.airsim.protocol.ISystemUnderTest;
import hxc.services.airsim.protocol.ITemporalTrigger;
import hxc.services.airsim.protocol.Lifecycle;
import hxc.services.airsim.protocol.Offer;
import hxc.services.airsim.protocol.ServiceOffering;
import hxc.services.airsim.protocol.SmsHistory;
import hxc.services.airsim.protocol.StartUsageRequest;
import hxc.services.airsim.protocol.SubDedicatedAccountInformation;
import hxc.services.airsim.protocol.Subscriber;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.services.airsim.protocol.TemporalTrigger;
import hxc.services.airsim.protocol.TnpThreshold;
import hxc.services.airsim.protocol.UsageCounter;
import hxc.services.airsim.protocol.UsageThreshold;
import hxc.services.airsim.protocol.UsageTimer;
import hxc.services.airsim.protocol.UssdResponse;
import hxc.services.numberplan.INumberPlan;
import hxc.utils.calendar.DateTime;
import hxc.utils.protocol.sdp.DedicatedAccountsFileV3;
import hxc.utils.protocol.sdp.DedicatedAccountsFileV3_3;
import hxc.utils.protocol.sdp.SubscriberFileV3;
import hxc.utils.protocol.sdp.SubscriberFileV3_3;
import hxc.utils.protocol.ucip.FafInformation;
import hxc.utils.protocol.ucip.PamInformationList;
import hxc.utils.protocol.ucip.ServiceOfferings;
import hxc.utils.xmlrpc.XmlRpcException;
import hxc.utils.xmlrpc.XmlRpcSerializer;

@WebService(endpointInterface = "hxc.services.airsim.protocol.IAirSim")
public class AirSim implements IAirSim, ISystemUnderTest, ISimulationData
{
	final static Logger logger = LoggerFactory.getLogger(AirSim.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private ISystemUnderTest sutx;
	private Map<String, SubscriberEx> subscribers = new HashMap<String, SubscriberEx>();
	private int portNo;
	private String url;
	private INumberPlan numberPlan;
	private String currency;
	private Map<String, InjectedResponse> injectedResponses = new HashMap<String, InjectedResponse>();
	private Gson gson = new Gson();
	private List<CallHistory> history = new ArrayList<CallHistory>();
	private AtomicInteger msisdnCounter = new AtomicInteger(0);
	private Map<Long, TnpThreshold> tnpThresholds = new HashMap<Long, TnpThreshold>();
	private static int sequenceNo = 0;
	private String stateFilename;
	private ScheduledThreadPoolExecutor scheduledThreadPool;
	private SupportedRequests requests;
	private HttpServer server;
	private String lastRefillProfileID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public AirSim(IServiceBus esb, int portNo, String url, INumberPlan numberPlan, String currency, String stateFilename)
	{
		this.esb = esb;
		this.portNo = portNo;
		this.url = url;
		this.currency = currency;
		this.numberPlan = numberPlan;
		this.stateFilename = stateFilename;
	}

	public AirSim(IServiceBus esb, int portNo, String url, INumberPlan numberPlan, String currency)
	{
		this(esb, portNo, url, numberPlan, currency, null);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAirSim Ping
	//
	// /////////////////////////////////
	@Override
	public int ping(int seq)
	{
		return seq + 1;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAirSim Start/Stop
	//
	// /////////////////////////////////

	@Override
	public synchronized boolean start()
	{
		stop();

		// Reset Error Responses
		injectedResponses = new HashMap<String, InjectedResponse>();

		// List of Supported Requests
		requests = new SupportedRequests();
		requests.add(new GetSubscriberInformation()); // HmX, not UCIP/ACIP !
		requests.add(new GetBalanceAndDate());
		requests.add(new GetOffers());
		requests.add(new UpdateBalanceAndDate());
		requests.add(new UpdateOffer());
		requests.add(new DeleteOffer());
		requests.add(new UpdateAccountDetails());
		requests.add(new GetAccountDetails());
		requests.add(new UpdateUsageThresholdsAndCounters());
		requests.add(new DeleteUsageThresholds());
		requests.add(new GetUsageThresholdsAndCounters());
		requests.add(new GetAccumulators());
		requests.add(new UpdateAccumulators());
		requests.add(new DeleteAccumulators());
		requests.add(new UpdateServiceClass());
		requests.add(new GetFaFList());
		requests.add(new UpdateSubscriberSegmentation());
		requests.add(new Refill());
		requests.add(new UpdateFaFList());
		requests.add(new AddPeriodicAccountManagementData());
		requests.add(new DeletePeriodicAccountManagementData());
		requests.add(new UpdatePeriodicAccountManagementData());
		requests.add(new RunPeriodicAccountManagement());
		requests.add(new UpdateSubDedicatedAccounts());
		requests.add(new UpdateCommunityList());
		requests.add(new InstallSubscriber());
		final Class<?>[] types = requests.getRequestTypes();

		// Mock Air Server
		server = HttpServer.createSimpleServer(url, portNo);
		ServerConfiguration serverConfig = server.getServerConfiguration();
		serverConfig.setName("AirSim");
		serverConfig.addHttpHandler(new HttpHandler()
		{
			@Override
			public void service(Request request, Response response) throws Exception
			{
				XmlRpcSerializer sz = new XmlRpcSerializer();
				Object req = null;
				Object resp = null;
				try (InputStream stream = request.getInputStream())
				{
					req = sz.deSerializeAny(stream, types);
				}
				catch (Throwable tr)
				{
					resp = new XmlRpcException(tr.getMessage(), 1001);
				}

				try
				{
					if (resp == null)
						resp = requests.execute(req, AirSim.this);
					if (resp == null)
						resp = new XmlRpcException("Failed", 1001);
				}
				catch (Throwable tr)
				{
					resp = new XmlRpcException(tr.getMessage(), 1001);
				}

				String responseString = sz.serialize(resp);
				response.setContentType("text/xml");
				response.setContentLength(responseString.length());
				try (Writer writer = response.getWriter())
				{
					writer.write(responseString);
				}

			}
		});

		// server = new XmlRpcServer(requests.getRequestTypes())
		// {
		// @Override
		// protected void uponXmlRpcRequest(XmlRpcRequest rpcRequest)
		// {
		// try
		// {
		// // long transactionStartTime = System.currentTimeMillis();
		// requests.execute(rpcRequest, AirSim.this);
		// // logger.trace( "{} completed within: {} ms", rpc.getMethodCall().getClass().getSimpleName(), System.currentTimeMillis() - transactionStartTime);
		// }
		// catch (Exception e)
		// {
		// 
		// logger.error( e.toString());
		// }
		// }
		//
		// };

		logger.debug("Starting AirSim server on {} : {}", url, portNo);
		try
		{
			server.start();
		}
		catch (IOException e)
		{
			logger.info("Airsim failed to start", e);
			return false;
		}

		msisdnCounter = new AtomicInteger(0);

		// Create new Thread Pool Scheduler
		scheduledThreadPool = new ScheduledThreadPoolExecutor(100, new ThreadFactory()
		{
			private int num = 0;

			@Override
			public Thread newThread(Runnable r)
			{
				return new Thread(r, "ScheduledAirSimThreadPool-" + num++);
			}

		});

		// Restore State
		try
		{
			restoreState();
		}
		catch (Exception e)
		{
			logger.info("Failed to restore airsim state", e);
		}

		return true;
	}

	@Override
	public synchronized void stop()
	{
		if (scheduledThreadPool != null)
			scheduledThreadPool.shutdownNow();

		if (server != null)
		{
			logger.debug("Stopping AirSim server.");
			server.shutdownNow();
			server = null;
		}
	}

	@Override
	public void reset()
	{
		logger.debug("Resetting AirSim data.");
		stopUsageTimers(null);
		subscribers.clear();
		injectedResponses.clear();
		history.clear();
		msisdnCounter = new AtomicInteger(0);
		tnpThresholds.clear();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAirSim Subscribers
	//
	// /////////////////////////////////

	@Override
	public Subscriber addSubscriber(String msisdn, int languageID, int serviceClass, long accountValue, SubscriberState state)
	{
		if (msisdn == null)
		{
			logger.error("Invalid msisdn provided.");
			return null;
		}

		// logger.debug( "Adding subscriber {}.", msisdn);
		String nationalNumber = numberPlan.getNationalFormat(msisdn);
		String internationalNumber = numberPlan.getInternationalFormat(msisdn);

		SubscriberEx subscriber = new SubscriberEx(currency, nationalNumber, internationalNumber, languageID, serviceClass, accountValue, state, tnpThresholds);
		subscribers.put(subscriber.getInternationalNumber(), subscriber);

		// logger.trace( "Subscriber {} added.", msisdn);
		return subscriber;
	}

	@Override
	public void addSubscribers(String msisdn, int count, int languageID, int serviceClass, long accountValue, SubscriberState state)
	{
		if (msisdn == null)
		{
			logger.error("Invalid msisdn provided.");
			return;
		}

		Long number = null;
		try
		{
			number = Long.parseLong(msisdn);
		}
		catch (NumberFormatException e)
		{
			logger.debug("Msisdn is not a valid number: {}", e.getMessage());
			return;
		}

		String format = String.format("%%0%dd", msisdn.length());
		logger.debug("Adding subscribers starting from {} until {}.", msisdn, String.format(format, number + count));

		for (int index = 0; index < count; index++)
		{
			addSubscriber(String.format(format, number++), languageID, serviceClass, accountValue, state);
		}

		logger.trace("{} Subscribers have been added.", count);
	}

	@Override
	public Subscriber addSubscriber2(String msisdn, int languageID, int serviceClass, long accountValue, Date activationDate, Date supervisionExpiryDate, Date serviceFeeExpiryDate, Date creditClearanceDate, Date serviceRemovalDate)
	{
		if (msisdn == null)
		{
			logger.error("Invalid msisdn provided.");
			return null;
		}

		// logger.debug( "Adding subscriber {}.", msisdn);
		String nationalNumber = numberPlan.getNationalFormat(msisdn);
		String internationalNumber = numberPlan.getInternationalFormat(msisdn);

		SubscriberEx subscriber = new SubscriberEx(currency, nationalNumber, internationalNumber, languageID, serviceClass, accountValue, SubscriberState.active, tnpThresholds);

		subscriber.setActivationDate(activationDate);
		subscriber.setSupervisionExpiryDate(supervisionExpiryDate);
		subscriber.setServiceFeeExpiryDate(serviceFeeExpiryDate);
		subscriber.setCreditClearanceDate(creditClearanceDate);
		subscriber.setServiceRemovalDate(serviceRemovalDate);

		subscribers.put(subscriber.getInternationalNumber(), subscriber);

		// logger.trace( "Subscriber {} added.", msisdn);
		return subscriber;
	}

	@Override
	public Subscriber cloneSubscriber(String msisdn, String newMsisdn)
	{
		if (msisdn == null)
		{
			logger.error("Invalid msisdn provided.");
			return null;
		}

		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		SubscriberEx clone = getSubscriberEx(newMsisdn);
		if (clone != null)
			return null;

		String nationalNumber = numberPlan.getNationalFormat(newMsisdn);
		String internationalNumber = numberPlan.getInternationalFormat(newMsisdn);

		clone = new SubscriberEx(nationalNumber, internationalNumber, subscriber, tnpThresholds);
		subscribers.put(clone.getInternationalNumber(), clone);

		return clone;
	}

	@Override
	public boolean cloneSubscribers(String msisdn, String newMsisdn, int count)
	{
		String format = String.format("%%0%dd", newMsisdn.length());

		long msisdnB = Long.parseLong(newMsisdn);
		for (int index = 0; index < count; index++)
		{
			String numberB = String.format(format, msisdnB++);
			Subscriber clone = cloneSubscriber(msisdn, numberB);
			if (clone == null)
				return false;
		}

		return true;
	}

	@Override
	public Subscriber getSubscriber(String msisdn)
	{
		logger.debug("Getting subscriber {}.", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
		{
			logger.debug("Subscriber ({}) does not exist.", msisdn);
		}
		return subscriber;
	}

	@Override
	public boolean updateSubscriber(Subscriber subscriber)
	{
		if (subscriber == null || subscriber.getInternationalNumber() == null)
		{
			logger.error("Invaild subscriber, please provide a valid international number.");
			return false;
		}

		logger.debug("Updating subscriber {}.", subscriber.getInternationalNumber());
		SubscriberEx subscriberEx = subscribers.get(subscriber.getInternationalNumber());
		if (subscriberEx == null)
			return false;

		try
		{
			subscriberEx.update(subscriber);
		}
		catch (Exception e)
		{
			logger.warn("Unable to update subscriber", e.getMessage());
		}

		logger.trace("Updated {}.", subscriber.getInternationalNumber());
		return true;
	}

	@Override
	public boolean deleteSubscriber(String msisdn)
	{
		if (msisdn == null)
		{
			logger.error("Invalid msisdn.");
			return false;
		}

		logger.debug("Deleting subscriber {}.", msisdn);
		String internationalNumber = numberPlan.getInternationalFormat(msisdn);
		if (!subscribers.containsKey(internationalNumber))
			return false;

		subscribers.remove(internationalNumber);

		logger.trace("{} has been removed.", msisdn);
		return true;
	}

	@Override
	public boolean adjustBalance(String msisdn, long deltaAmount)
	{
		logger.debug("Adjusting balance of {} by {} ({}).", msisdn, deltaAmount, currency);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		if (subscriber.getAccountValue1() != null)
		{
			try
			{
				subscriber.setAccountValue1(subscriber.getAccountValue1() - deltaAmount);
			}
			catch (Exception e)
			{
				logger.warn("Failed to adjust subscriber balance: ", e);
			}
			logger.trace("Adjusted balance for {} to {} ({}).", msisdn, subscriber.getAccountValue1(), currency);
		}

		return true;
	}

	@Override
	public boolean setBalance(String msisdn, long newAmount)
	{
		logger.debug("Setting balance for {} to {} ({}).", msisdn, newAmount, currency);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		try
		{
			subscriber.setAccountValue1(newAmount);
		}
		catch (Exception e)
		{
			logger.warn("Failed to set account balance", e.getMessage());
		}

		logger.trace("Account value for {} was set to {} ({}).", msisdn, newAmount, currency);
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAirSim Offers
	//
	// /////////////////////////////////

	@Override
	public Offer[] getOffers(String msisdn)
	{
		logger.debug("Getting offers for {}.", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		Collection<OfferEx> offers = subscriber.getOffers().values();
		Offer[] result = new Offer[offers.size()];
		int index = 0;
		for (OfferEx offer : offers)
		{
			result[index++] = offer;
		}

		logger.trace("Found {} offers from {}.", result.length, msisdn);
		return result;
	}

	@Override
	public Offer getOffer(String msisdn, int offerID)
	{
		logger.debug("Getting offer ({}) for {}.", offerID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		return subscriber.getOffers().get(offerID);
	}

	@Override
	public boolean hasOffer(String msisdn, int offerID)
	{
		logger.debug("Checking if {} has offer ({}).", msisdn, offerID);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		boolean hasOffer = subscriber.getOffers().containsKey(offerID);
		logger.trace("{} does {} have offer ({}).", msisdn, hasOffer ? "" : " not", offerID);
		return false;
	}

	@Override
	public boolean updateOffer(String msisdn, Offer offer)
	{
		if (offer == null)
		{
			logger.error("Invalid offer.");
			return false;
		}

		logger.debug("Updating offer ({}) for {}.", offer.getOfferID(), msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		OfferEx ofr = subscriber.getOffers().get(offer.getOfferID());
		if (ofr == null)
		{
			logger.trace("Offer ({}) not found, adding offer to {}.", offer.getOfferID(), msisdn);
			ofr = new OfferEx();
			ofr.setOfferID(offer.getOfferID());
			subscriber.getOffers().put(ofr.getOfferID(), ofr);
		}

		ofr.setStartDate(offer.getStartDate());
		ofr.setExpiryDate(offer.getExpiryDate());
		ofr.setStartDateTime(offer.getStartDateTime());
		ofr.setExpiryDateTime(offer.getExpiryDateTime());
		ofr.setPamServiceID(offer.getPamServiceID());
		ofr.setOfferType(offer.getOfferType());
		ofr.setOfferState(offer.getOfferState());
		ofr.setOfferProviderID(offer.getOfferProviderID());
		ofr.setProductID(offer.getProductID());

		logger.trace("Offer ({}) updated for {}.", ofr.getOfferID(), msisdn);
		return true;
	}

	@Override
	public boolean deleteOffer(String msisdn, int offerID)
	{
		logger.debug("Deleting offer ({}) for {}.", offerID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		if (!subscriber.getOffers().containsKey(offerID))
			return false;

		subscriber.getOffers().remove(offerID);

		logger.trace("Removed offer ({}) from {}.", offerID, msisdn);
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IAirSim Dedicated Accounts
	//
	// /////////////////////////////////
	@Override
	public DedicatedAccount[] getDedicatedAccounts(String msisdn)
	{
		logger.debug("Getting dedicated accounts for {}.", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		DedicatedAccount[] result = new DedicatedAccount[subscriber.getDedicatedAccounts().size()];
		int index = 0;
		for (DedicatedAccount da : subscriber.getDedicatedAccounts().values())
		{
			result[index++] = da;
		}

		logger.trace("Found {} dedicated accounts for {}.", result.length, msisdn);
		return result;
	}

	@Override
	public DedicatedAccount getDedicatedAccount(String msisdn, int dedicatedAccountID)
	{
		logger.debug("Getting dedicated account ({}) for {}.", dedicatedAccountID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		DedicatedAccount da = subscriber.getDedicatedAccounts().get(dedicatedAccountID);

		if (da != null)
			return new DedicatedAccount(da);
		else
			return da;
	}

	@Override
	public boolean hasDedicatedAccount(String msisdn, int dedicatedAccountID)
	{
		logger.debug("Checking if {} has dedicated account ({}).", msisdn, dedicatedAccountID);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		boolean hasDedicatedAccount = subscriber.getDedicatedAccounts().containsKey(dedicatedAccountID);

		logger.trace("Dedicated account ({}) does{} exist for {}.", dedicatedAccountID, hasDedicatedAccount ? "" : " not", msisdn);
		return hasDedicatedAccount;
	}

	@Override
	public boolean updateDedicatedAccount(String msisdn, DedicatedAccount dedicatedAccount)
	{
		if (dedicatedAccount == null)
		{
			logger.error("Invalid dedicated account.");
			return false;
		}

		logger.debug("Updating dedicated account ({}) for {}.", dedicatedAccount.getDedicatedAccountID(), msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		DedicatedAccount da = subscriber.getDedicatedAccounts().get(dedicatedAccount.getDedicatedAccountID());
		if (da == null)
		{
			logger.trace("Dedicated account ({}) not found, adding dedicated account to {}.", dedicatedAccount.getDedicatedAccountID(), msisdn);
			da = new DedicatedAccount();
			da.setDedicatedAccountID(dedicatedAccount.getDedicatedAccountID());
			subscriber.getDedicatedAccounts().put(da.getDedicatedAccountID(), da);
			da.setDedicatedAccountUnitType(dedicatedAccount.getDedicatedAccountUnitType());
		}

		Long oldValue = da.getDedicatedAccountValue1();

		// Copy Fields
		da.setDedicatedAccountValue1(dedicatedAccount.getDedicatedAccountValue1());
		da.setDedicatedAccountValue2(dedicatedAccount.getDedicatedAccountValue2());
		da.setExpiryDate(dedicatedAccount.getExpiryDate());
		da.setStartDate(dedicatedAccount.getStartDate());
		da.setPamServiceID(dedicatedAccount.getPamServiceID());
		da.setOfferID(dedicatedAccount.getOfferID());
		da.setProductID(dedicatedAccount.getProductID());
		da.setDedicatedAccountRealMoneyFlag(dedicatedAccount.getDedicatedAccountRealMoneyFlag());
		da.setClosestExpiryDate(dedicatedAccount.getClosestExpiryDate());
		da.setClosestExpiryValue1(dedicatedAccount.getClosestExpiryValue1());
		da.setClosestExpiryValue2(dedicatedAccount.getClosestExpiryValue2());
		da.setClosestAccessibleDate(dedicatedAccount.getClosestAccessibleDate());
		da.setClosestAccessibleValue1(dedicatedAccount.getClosestAccessibleValue1());
		da.setClosestAccessibleValue2(dedicatedAccount.getClosestAccessibleValue2());
		da.setDedicatedAccountActiveValue1(dedicatedAccount.getDedicatedAccountActiveValue1());
		da.setDedicatedAccountActiveValue2(dedicatedAccount.getDedicatedAccountActiveValue2());
		da.setDedicatedAccountUnitType(dedicatedAccount.getDedicatedAccountUnitType());
		da.setCompositeDedicatedAccountFlag(dedicatedAccount.getCompositeDedicatedAccountFlag());
		da.setSubDedicatedAccountInformation(dedicatedAccount.getSubDedicatedAccountInformation());

		try
		{
			subscriber.triggerValueChange(da.getDedicatedAccountID(), oldValue, da.getDedicatedAccountValue1());
		}
		catch (Exception e)
		{
			logger.warn("Attempt to trigger value change failed", e.getMessage());
		}

		logger.trace("Updated dedicated account ({}) for {}", dedicatedAccount.getDedicatedAccountID(), msisdn);
		return true;
	}

	@Override
	public boolean deleteDedicatedAccount(String msisdn, int dedicatedAccountID)
	{
		logger.debug("Deleting dedicated account ({}) for {}.", dedicatedAccountID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		if (!subscriber.getDedicatedAccounts().containsKey(dedicatedAccountID))
		{
			logger.debug("Dedicated account ({}) does not exist for {}.", dedicatedAccountID, msisdn);
			return false;
		}

		subscriber.getDedicatedAccounts().remove(dedicatedAccountID);

		logger.trace("Removed dedicated account ({}) from {}.", dedicatedAccountID, msisdn);
		return true;
	}

	@Override
	public boolean updateSubDedicatedAccounts(String msisdn, int dedicatedAccountID, SubDedicatedAccountInformation[] subDedicatedAccounts)
	{
		if (subDedicatedAccounts == null || subDedicatedAccounts.length == 0)
		{
			logger.debug("Invalid sub dedicated account.");
			return false;
		}

		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		DedicatedAccount da = subscriber.getDedicatedAccounts().get(dedicatedAccountID);
		if (da == null)
		{
			logger.error("Dedicated account ({}) does not exist for {}.", dedicatedAccountID, msisdn);
			return false;
		}

		logger.debug("Updating sub dedicated accounts for da ({}) for {}.", dedicatedAccountID, msisdn);

		da.setSubDedicatedAccountInformation(subDedicatedAccounts);

		logger.trace("Updated sub dedicated accounts for da ({}) for {}.", dedicatedAccountID, msisdn);
		return true;
	}

	@Override
	public boolean createDedicatedAccount(String msisdn, int dedicatedAccountID, int unitType, Long value, Date startDate, Date expiryDate)
	{
		logger.debug("Creating dedicated account ({}) for {}.", dedicatedAccountID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		if (subscriber.getDedicatedAccounts().containsKey(dedicatedAccountID))
		{
			logger.debug("Dedicated account ({}) already exists for {}.", dedicatedAccountID, msisdn);
			return false;
		}

		DedicatedAccount da = new DedicatedAccount();
		da.setDedicatedAccountValue1(value);
		da.setDedicatedAccountValue2(value);
		da.setDedicatedAccountID(dedicatedAccountID);
		da.setDedicatedAccountUnitType(unitType);
		da.setStartDate(startDate);
		da.setExpiryDate(expiryDate);
		subscriber.getDedicatedAccounts().put(dedicatedAccountID, da);

		logger.trace("Created dedicated account ({}) from {}.", dedicatedAccountID, msisdn);
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Usage Counters
	//
	// /////////////////////////////////

	@Override
	public UsageCounter[] getUsageCounters(String msisdn)
	{
		logger.debug("Getting usage counters for {}.", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		Collection<UsageCounter> ucs = subscriber.getUsageCounters().values();

		logger.trace("Found {} usage counters for {}.", ucs.size(), msisdn);
		return ucs.toArray(new UsageCounter[ucs.size()]);
	}

	@Override
	public UsageCounter getUsageCounter(String msisdn, int usageCounterID)
	{
		logger.debug("Getting usage counter ({}) for {}.", usageCounterID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		UsageCounter usageCounter = subscriber.getUsageCounters().get(usageCounterID);

		logger.trace("Usage counter ({}) does{} exist for {}.", usageCounterID, usageCounter != null ? "" : " not", msisdn);
		return usageCounter;
	}

	@Override
	public boolean hasUsageCounter(String msisdn, int usageCounterID)
	{
		logger.debug("Checking if {} has usage counter ({}).", msisdn, usageCounterID);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		boolean hasUsageCounter = subscriber.getUsageCounters().containsKey(usageCounterID);

		logger.trace("Usage counter ({}) does{} exist for {}.", usageCounterID, hasUsageCounter ? "" : " not", msisdn);
		return hasUsageCounter;
	}

	@Override
	public boolean updateUsageCounter(String msisdn, UsageCounter usageCounter)
	{
		if (usageCounter == null)
		{
			logger.error("Invalid usage counter.");
			return false;
		}

		logger.debug("Updating usage counter ({}) for {}.", usageCounter.getUsageCounterID(), msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		UsageCounter uc = subscriber.getUsageCounters().get(usageCounter.getUsageCounterID());
		if (uc == null)
		{
			uc = new UsageCounter();
			uc.setUsageCounterID(usageCounter.getUsageCounterID());
			subscriber.getUsageCounters().put(usageCounter.getUsageCounterID(), uc);
		}

		uc.setUsageCounterValue(usageCounter.getUsageCounterValue());
		uc.setUsageCounterMonetaryValue1(usageCounter.getUsageCounterMonetaryValue1());
		uc.setUsageCounterMonetaryValue2(usageCounter.getUsageCounterMonetaryValue2());
		uc.setAssociatedPartyID(usageCounter.getAssociatedPartyID());
		uc.setProductID(usageCounter.getProductID());

		logger.trace("Updated usage counter ({}) for {}.", usageCounter.getUsageCounterID(), msisdn);
		return true;
	}

	@Override
	public boolean deleteUsageCounter(String msisdn, int usageCounterID)
	{
		logger.debug("Deleting usage counter ({}) for {}.", usageCounterID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		Map<Integer, UsageCounter> ucs = subscriber.getUsageCounters();
		if (!ucs.containsKey(usageCounterID))
		{
			logger.trace("Usage counter ({}) does not exist for {}.", usageCounterID, msisdn);
			return false;
		}

		ucs.remove(usageCounterID);

		logger.trace("Deleted usage counter ({}) for {}.", usageCounterID, msisdn);
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Usage Thresholds
	//
	// /////////////////////////////////

	@Override
	public UsageThreshold[] getUsageThresholds(String msisdn)
	{
		logger.debug("Getting usage thresholds for {}.", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		Collection<UsageThreshold> uts = subscriber.getUsageThresholds().values();

		logger.trace("Found {} usage thresholds for {}.", uts.size(), msisdn);
		return uts.toArray(new UsageThreshold[uts.size()]);
	}

	@Override
	public UsageThreshold getUsageThreshold(String msisdn, int usageThresholdID)
	{
		logger.debug("Getting usage threshold ({}) for {}.", usageThresholdID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		UsageThreshold usageThreshold = subscriber.getUsageThresholds().get(usageThresholdID);

		logger.trace("Usage threshold ({}) does {} exist for {}.", usageThresholdID, usageThreshold != null ? "" : " not", msisdn);
		return usageThreshold;
	}

	@Override
	public boolean hasUsageThreshold(String msisdn, int usageThresholdID)
	{
		logger.debug("Checking usage threshold ({}) exists for {}.", usageThresholdID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		boolean hasUsageThreshold = subscriber.getUsageThresholds().containsKey(usageThresholdID);

		logger.trace("Usage threshold ({}) does {} exist for {}.", usageThresholdID, hasUsageThreshold ? "" : " not", msisdn);
		return hasUsageThreshold;
	}

	@Override
	public boolean updateUsageThreshold(String msisdn, UsageThreshold usageThreshold)
	{
		if (usageThreshold == null)
		{
			logger.error("Invalid usage threshold.");
			return false;
		}

		logger.debug("Updating usage threshold ({}) for {}.", usageThreshold.getUsageThresholdID(), msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		UsageThreshold ut = subscriber.getUsageThresholds().get(usageThreshold.getUsageThresholdID());
		if (ut == null)
		{
			ut = new UsageThreshold();
			ut.setUsageThresholdID(usageThreshold.getUsageThresholdID());
			subscriber.getUsageThresholds().put(ut.getUsageThresholdID(), ut);
		}

		ut.setUsageThresholdValue(usageThreshold.getUsageThresholdValue());
		ut.setUsageThresholdMonetaryValue1(usageThreshold.getUsageThresholdMonetaryValue1());
		ut.setUsageThresholdMonetaryValue2(usageThreshold.getUsageThresholdMonetaryValue2());
		ut.setUsageThresholdSource(usageThreshold.getUsageThresholdSource());
		ut.setAssociatedPartyID(usageThreshold.getAssociatedPartyID());

		logger.trace("Usage threshold ({}) updated for {}.", usageThreshold.getUsageThresholdID(), msisdn);
		return true;

	}

	@Override
	public boolean deleteUsageThreshold(String msisdn, int usageThresholdID)
	{
		logger.debug("Deleting usage threshold ({}) for {}.", usageThresholdID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		Map<Integer, UsageThreshold> uts = subscriber.getUsageThresholds();
		if (!uts.containsKey(usageThresholdID))
		{
			logger.trace("Usage threshold ({}) doesn't exist for {}.", usageThresholdID, msisdn);
			return false;
		}

		uts.remove(usageThresholdID);

		logger.trace("Usage threshold ({}) deleted for {}.", usageThresholdID, msisdn);
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accumulators
	//
	// /////////////////////////////////

	@Override
	public Accumulator[] getAccumulators(String msisdn)
	{
		logger.debug("Getting accumulators for {}.", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		Collection<Accumulator> uas = subscriber.getAccumulators().values();

		logger.trace("Found {} accumulators for {}.", uas.size(), msisdn);
		return uas.toArray(new Accumulator[uas.size()]);
	}

	@Override
	public Accumulator getAccumulator(String msisdn, int accumulatorID)
	{
		logger.debug( "Getting accumulator ({}) for {}.", accumulatorID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		Accumulator accumulator = subscriber.getAccumulators().get(accumulatorID);

		logger.trace( "Accumulator ({}) does{} exist for {}.", accumulatorID, accumulator != null ? "" : " not", msisdn);
		return accumulator;
	}

	@Override
	public boolean hasAccumulator(String msisdn, int accumulatorID)
	{
		logger.debug( "Checking if {} has accumulator ({}).", msisdn, accumulatorID);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		boolean hasAccumulator = subscriber.getAccumulators().containsKey(accumulatorID);

		logger.trace( "Accumulator ({}) does{} exist for {}.", accumulatorID, hasAccumulator ? "" : " not", msisdn);
		return hasAccumulator;
	}

	@Override
	public boolean updateAccumulator(String msisdn, Accumulator accumulator)
	{
		if (accumulator == null)
		{
			logger.error( "Invalid accumulator.");
			return false;
		}

		logger.debug( "Updating accumulator ({}) for {}.", accumulator.getAccumulatorID(), msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		Accumulator ua = subscriber.getAccumulators().get(accumulator.getAccumulatorID());
		if (ua == null)
		{
			logger.debug( "Accumulator ({}) does not exist for {}. Creating accumulator.", accumulator.getAccumulatorID(), msisdn);
			ua = new Accumulator();
			ua.setAccumulatorID(accumulator.getAccumulatorID());
			subscriber.getAccumulators().put(ua.getAccumulatorID(), ua);
		}

		ua.setAccumulatorValue(accumulator.getAccumulatorValue());
		ua.setAccumulatorStartDate(accumulator.getAccumulatorStartDate());
		ua.setAccumulatorEndDate(accumulator.getAccumulatorEndDate());

		logger.trace( "Accumulator ({}) updated for {}.", accumulator.getAccumulatorID(), msisdn);
		return true;
	}

	@Override
	public boolean deleteAccumulator(String msisdn, int accumulatorID)
	{
		logger.debug( "Deleting accumulator ({}) for {}.", accumulatorID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		Map<Integer, Accumulator> uas = subscriber.getAccumulators();
		if (!uas.containsKey(accumulatorID))
		{
			logger.trace( "Accumulator ({}) does not exist for {}.", accumulatorID, msisdn);
			return false;
		}

		uas.remove(accumulatorID);

		logger.trace( "Accumulator ({}) deleted from {}.", accumulatorID, msisdn);
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Service Offerings
	//
	// /////////////////////////////////

	@Override
	public ServiceOffering[] getServiceOfferings(String msisdn)
	{
		logger.debug( "Getting service offerings for {}.", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		ServiceOffering[] result = new ServiceOffering[subscriber.getServiceOfferings().size()];
		int index = 0;
		for (ServiceOfferings serviceOffering : subscriber.getServiceOfferings().values())
		{
			result[index++] = new ServiceOffering(serviceOffering);
		}

		logger.trace( "Found {} service offerings for {}.", result.length, msisdn);
		return result;
	}

	@Override
	public ServiceOffering getServiceOffering(String msisdn, int serviceOfferingID)
	{
		logger.debug( "Getting service offering ({}) for {}.", serviceOfferingID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		ServiceOfferings serviceOffering = subscriber.getServiceOfferings().get(serviceOfferingID);
		if (serviceOffering == null)
		{
			logger.trace( "Service offering ({}) does not exist for {}.", serviceOffering, msisdn);
			return null;
		}

		logger.trace( "Service offering ({}) found for {}.", serviceOfferingID, msisdn);
		return new ServiceOffering(serviceOffering);
	}

	@Override
	public boolean hasServiceOffering(String msisdn, int serviceOfferingID)
	{
		logger.debug( "Checking if service offering ({}) exists for {}.", serviceOfferingID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		boolean hasServiceOffering = subscriber.getServiceOfferings().containsKey(serviceOfferingID);

		logger.trace( "Service offering ({}) does{} exist for {}.", serviceOfferingID, msisdn);
		return hasServiceOffering;
	}

	@Override
	public boolean updateServiceOffering(String msisdn, ServiceOffering serviceOffering)
	{
		if (serviceOffering == null)
		{
			logger.debug( "Invalid service offering.");
			return false;
		}

		logger.debug( "Updating service offering ({}) for {}.", serviceOffering.getServiceOfferingID(), msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		subscriber.getServiceOfferings().put(serviceOffering.getServiceOfferingID(), serviceOffering.toServiceOfferings());

		logger.trace( "Updated service offering ({}) for {}.", serviceOffering.getServiceOfferingID(), msisdn);
		return true;
	}

	@Override
	public boolean deleteServiceOffering(String msisdn, int serviceOfferingID)
	{
		logger.debug( "Deleting service offering ({}) for {}.", serviceOfferingID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		if (!subscriber.getServiceOfferings().containsKey(serviceOfferingID))
		{
			logger.trace( "Service offering ({}) does not exist for {}.", serviceOfferingID, msisdn);
			return false;
		}

		subscriber.getServiceOfferings().remove(serviceOfferingID);

		logger.trace( "Service offering ({}) deleted from {}.", serviceOfferingID, msisdn);
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// FaF
	//
	// /////////////////////////////////

	@Override
	public FafEntry[] getFafEntries(String msisdn)
	{
		logger.debug( "Getting faf entries for {}.", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return null;

		Collection<FafInformation> list = subscriber.getFafEntries().values();
		FafEntry[] result = new FafEntry[subscriber.getFafEntries().size()];
		int index = 0;
		for (FafInformation fafInfo : list)
		{
			result[index++] = new FafEntry(fafInfo);
		}

		logger.trace( "Found {} faf entries for {}.", result.length, msisdn);
		return result;
	}

	@Override
	public boolean deleteFafEntry(String msisdn, String fafNumber)
	{
		if (fafNumber == null)
		{
			logger.error( "Invalid faf number.");
			return false;
		}

		logger.debug( "Deleting faf entry ({}) for {}.", fafNumber, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		if (!subscriber.getFafEntries().containsKey(fafNumber))
		{
			logger.trace( "Faf entry ({}) does not exist for {}.", fafNumber, msisdn);
			return false;
		}

		subscriber.getFafEntries().remove(fafNumber);

		logger.trace( "Deleted faf entry ({}) for {}.", fafNumber, msisdn);
		return true;
	}

	@Override
	public boolean deleteFafEntries(String msisdn)
	{
		logger.debug( "Deleting faf entries for {}.", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		if (subscriber.getFafEntries().size() == 0)
		{
			logger.trace( "No faf entries found for {}.", msisdn);
			return false;
		}

		subscriber.getFafEntries().clear();

		logger.trace( "Faf entries deleted from {}.", msisdn);
		return true;
	}

	@Override
	public boolean updateFafEntry(String msisdn, FafEntry fafEntry)
	{
		if (fafEntry == null)
		{
			logger.error( "Invalid faf entry.");
			return false;
		}

		logger.debug( "Updating faf entry ({}) for {}.", fafEntry.getFafNumber(), msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		FafInformation fafInfo = subscriber.getFafEntries().get(fafEntry.getFafNumber());
		if (fafInfo == null)
		{
			logger.trace( "Faf entry ({}) doesn't exist for {}.", fafEntry.getFafNumber(), msisdn);
			return false;
		}

		fafEntry.clone(fafInfo);

		logger.trace( "Updated faf entry ({}) for {}.", fafEntry.getFafNumber(), msisdn);
		return true;
	}

	@Override
	public boolean addFafEntry(String msisdn, FafEntry fafEntry)
	{
		if (fafEntry == null)
		{
			logger.error( "Invalid faf entry.");
			return false;
		}

		logger.debug( "Adding faf entry ({}) to {}.", fafEntry.getFafNumber(), msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		FafInformation fafInfo = new FafInformation();
		fafInfo.fafNumber = fafEntry.getFafNumber();
		fafEntry.clone(fafInfo);
		subscriber.getFafEntries().put(fafInfo.fafNumber, fafInfo);

		logger.trace( "Added faf entry ({}) to {}.", fafEntry.getFafNumber(), msisdn);
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// PAM
	//
	// /////////////////////////////////

	@Override
	public boolean updatePAM(String msisdn, int pamServiceID, int pamClassID, int scheduleID, //
			String currentPamPeriod, Date deferredToDate, Date lastEvaluationDate, Integer pamServicePriority)
	{
		logger.debug( "Updating PAM ({}) for {}.", pamServiceID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		Map<Integer, PamInformationList> pamList = subscriber.getPamEntries();
		PamInformationList pam = pamList.get(pamServiceID);

		if (pam == null)
		{
			pam = new PamInformationList();
			pam.pamServiceID = pamServiceID;
			pam.pamClassID = pamClassID;
			pam.scheduleID = scheduleID;
			pamList.put(pamServiceID, pam);
		}

		pam.currentPamPeriod = currentPamPeriod;
		pam.deferredToDate = deferredToDate;
		pam.lastEvaluationDate = lastEvaluationDate;
		pam.pamServicePriority = pamServicePriority;

		logger.debug( "Updated PAM ({}) for {}.", pamServiceID, msisdn);

		return true;
	}

	@Override
	public boolean deletePAM(String msisdn, int pamServiceID)
	{
		logger.debug( "Deleting PAM ({}) for {}.", pamServiceID, msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		Map<Integer, PamInformationList> pamList = subscriber.getPamEntries();
		PamInformationList pam = pamList.get(pamServiceID);

		if (pam != null)
		{
			pamList.remove(pamServiceID);
			logger.debug( "Deleted PAM ({}) for {}.", pamServiceID,  msisdn);
			return true;
		}
		else
		{
			logger.debug( "Failed to delete PAM ({}) for {}.", pamServiceID,  msisdn);
			return false;
		}
	}


	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Refill
	//
	// /////////////////////////////////

	@Override
	public String getLastRefillProfileID()
	{
		String result = lastRefillProfileID;
		lastRefillProfileID = null;
		return result;
	}

	@Override
	public void setLastRefillProfileID(String lastRefillProfileID)
	{
		this.lastRefillProfileID = lastRefillProfileID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Call History
	//
	// /////////////////////////////////

	@Override
	public CallHistory[] getCallHistory()
	{
		logger.debug( "Getting call history [{}].", history.size());
		return history.toArray(new CallHistory[history.size()]);
	}

	@Override
	public void clearCallHistory()
	{
		logger.debug( "Clearing call history.");
		history.clear();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Persistance
	//
	// /////////////////////////////////

	@Override
	public boolean restoreState()
	{
		// Fail if Filename is empty
		if (stateFilename == null || stateFilename.length() == 0)
		{
			logger.debug( "Restore terminated, file name NULL or zero size");
			return false; // Causing UNIT test failures ... everywhere (moving assertion to validation on saving)
		}

		// Fail if the File doesn't exist
		File file = new File(stateFilename);
		if (!file.exists())
		{
			logger.warn( "Restore State Failed, file {} not found", stateFilename);
			return false;
		}
		try (BufferedReader br = new BufferedReader(new FileReader(stateFilename)))
		{
			Type subsMapType = new TypeToken<Map<String, SubscriberEx>>()
			{
			}.getType();
			Map<String, SubscriberEx> subs = gson.fromJson(br, subsMapType);
			if (subs == null)
			{
				logger.error( "Restore State Failed, Subscriber JSON could not decoded using file {}", stateFilename);
				throw new IllegalStateException(String.format("file %s could not be decoded", stateFilename));
			}
			else
				subscribers = subs;
		}
		catch (IOException e)
		{
			logger.warn("Unable to restore state", e);
			throw new IllegalStateException(String.format("Restore State failed: %s", e.getMessage()));
		}

		// Restart Usage Timers
		logger.info( "Restoring {} AirSim accounts from file {}", subscribers.size(), stateFilename);
		for (SubscriberEx subscriber : subscribers.values())
		{
			subscriber.scheduleUsage(scheduledThreadPool);
		}

		return true;
	}

	@Override
	public boolean saveState()
	{
		// Fail if Filename is empty
		if (stateFilename == null || stateFilename.length() == 0)
		{
			logger.error( "Safe State Failed, file name NULL or zero size");
			throw new AssertionError("File Name too small");
		}

		// Create Folder if folder doesn't exist
		File file = new File(stateFilename);
		String folderName = file.getParent();
		if (folderName != null)
		{
			File folder = new File(folderName);
			if (!folder.exists() && !folder.mkdirs())
			{
				logger.error( "Safe State Failed, folder {} could not be found/created", folderName);
				throw new AssertionError(String.format("Folder %s could not be created", stateFilename));
			}

		}

		logger.info( "Saving {} AirSim accounts to file {}", subscribers.size(), stateFilename);
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(stateFilename)))
		{
			gson.toJson(subscribers, bw);
		}
		catch (IOException e)
		{
			logger.warn("Unable to save state", e);
			throw new IllegalStateException(String.format("Save State failed: %s", e.getMessage()));
		}

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// File Creation
	//
	// /////////////////////////////////
	@Override
	public boolean addTnpThreshold(TnpThreshold threshold)
	{
		if (threshold == null)
		{
			logger.error( "Invalid threshold.");
			return false;
		}

		logger.debug( "Adding tnp threshold ({}).", threshold.getThresholdID());
		if (!threshold.getVersion().equals("2.0") && !threshold.getVersion().equals("3.0"))
		{
			logger.error( "Only Tnp threshold versions 2.0 and 3.0 are available.");
			return false;
		}

		long key = threshold.getThresholdID() * 10000 + threshold.getServiceClass();
		if (!threshold.isUpwards())
			key = -key;
		tnpThresholds.put(key, threshold);

		logger.trace( "Added tnp threshold ({}).", threshold.getThresholdID());
		return true;
	}

	@Override
	public void produceDedicatedAccountFile(String directory, String prefix, String version)
	{
		int versionNo = -1;

		try
		{
			versionNo = (int) (Double.parseDouble(version) * 1000 + 0.000001);
		}
		catch (NumberFormatException ex)
		{
			logger.error( "Version invalid.");
			versionNo = -1;
		}

		// Test Version
		Class<?> cls;
		switch (versionNo)
		{
			case 3000:
				cls = DedicatedAccountsFileV3.class;
				break;

			case 3300:
				cls = DedicatedAccountsFileV3_3.class;
				break;

			default:
				logger.error( "Unsupported Subscriber File Version");
				return;
		}

		// Cache Format
		Map<Integer, FileField> fields = FileField.getFieldFormats(cls);

		// Create folder if it doesn't exist
		File theDir = new File(directory);
		if (!theDir.exists())
		{
			logger.trace( "Directory doesn't exist, making it.");
			theDir.mkdir();
		}

		// Create filename
		DateTime now = new DateTime();
		String timePart = now.toString("yyyyMMddHHmmss");
		String filename = String.format("%s%05d_dedicatedaccount.v%s.csv", timePart, sequenceNo++, versionNo / 1000);
		String tempFilename = filename + ".tmp";
		logger.debug( "Creating temp file: {}", tempFilename);

		// Open the File
		File theFile = new File(theDir, tempFilename);

		try (FileWriter fw = new FileWriter(theFile, false))
		{

			for (SubscriberEx subscriber : subscribers.values())
			{
				for (DedicatedAccount dedicatedAccount : subscriber.getDedicatedAccounts().values())
				{
					boolean isMoney = dedicatedAccount.getDedicatedAccountUnitType() == 1;
					DedicatedAccountsFileV3 da = (DedicatedAccountsFileV3) cls.newInstance();
					da.accountID = subscriber.getNationalNumber();
					da.dedicatedAccountID = dedicatedAccount.getDedicatedAccountID();
					da.dedicatedAccountBalance = isMoney ? (double) (long) dedicatedAccount.getDedicatedAccountValue1() : null;
					da.expiryDate = dedicatedAccount.getExpiryDate();
					da.accountinEuroflag = false;
					da.offerID = dedicatedAccount.getOfferID();
					da.startDate = dedicatedAccount.getStartDate();
					da.dedicatedAccountUnitType = dedicatedAccount.getDedicatedAccountUnitType();
					da.dedicatedAccountCategory = dedicatedAccount.getCompositeDedicatedAccountFlag() != null && dedicatedAccount.getCompositeDedicatedAccountFlag() ? 1 : 0;
					da.moneyUnitSubtype = dedicatedAccount.getDedicatedAccountRealMoneyFlag() != null ? dedicatedAccount.getDedicatedAccountRealMoneyFlag() : false;
					da.dedicatedAccountUnitBalance = isMoney ? null : dedicatedAccount.getDedicatedAccountValue1();

					if (versionNo >= 3300)
					{
						DedicatedAccountsFileV3_3 da33 = (DedicatedAccountsFileV3_3) da;
						da33.pamServiceId = dedicatedAccount.getPamServiceID();
						da33.productID = dedicatedAccount.getProductID();
					}

					logger.trace( "Writing dedicated account information for subscriber: {}", subscriber.getNationalNumber());
					fw.write(FileField.getCsv(fields, da));

				}

			}

		}
		catch (IOException | InstantiationException | IllegalAccessException e)
		{
			logger.error("Unable to write file", e);
		}

		// Rename the file
		theFile.renameTo(new File(theDir, filename));
		logger.debug( "Produced dedicated account file: {}", filename);
	}

	@Override
	public void produceSubscriberFile(String directory, String prefix, String version)
	{

		int versionNo = -1;

		try
		{
			versionNo = (int) (Double.parseDouble(version) * 1000 + 0.000001);
		}
		catch (NumberFormatException ex)
		{
			versionNo = -1;
		}

		// Test Version
		Class<?> cls;
		switch (versionNo)
		{
			case 3000:
				cls = SubscriberFileV3.class;
				break;

			case 3300:
				cls = SubscriberFileV3_3.class;
				break;

			default:
				logger.error( "Unsupported Subscriber File Version");
				return;
		}

		// Cache Format
		Map<Integer, FileField> fields = FileField.getFieldFormats(cls);

		// Create folder if it doesn't exist
		File theDir = new File(directory);
		if (!theDir.exists())
		{
			theDir.mkdir();
		}

		// Create filename
		DateTime now = new DateTime();
		String timePart = now.toString("yyyyMMddHHmmss");
		String filename = String.format("%s%05d_subscriber.v%s.csv", timePart, sequenceNo++, versionNo / 1000);
		String tempFilename = filename + ".tmp";

		// Open the File
		File theFile = new File(theDir, tempFilename);

		try (FileWriter fw = new FileWriter(theFile, false))
		{

			for (SubscriberEx subscriber : subscribers.values())
			{
				SubscriberFileV3 subs = (SubscriberFileV3) cls.newInstance();

				subs.subscriberID = subscriber.getInternationalNumber();
				subs.accountID = subscriber.getInternationalNumber();
				subs.temporaryBlockFlag = coerce(subscriber.getTemporaryBlockedFlag());
				// subs.refillFailedCounter = subscriber.getRefillFailedCounter();
				subs.refillBarEndDateAndTime = subscriber.getRefillUnbarDateTime();
				// subs.firstIVRCallDoneFlag = subscriber.getFirstIVRCallDoneFlag();
				// subs.firstCallDoneFlag = subscriber.isFirstCallDoneFlag();
				subs.language = (byte) subscriber.getLanguageID();
				// subs.specialAnnouncementPlayedFlag = subscriber. getSpecialAnnouncementPlayedFlag();
				// subs.serviceFeePeriodWarningPlayedFlag = subscriber.getServiceFeePeriodWarningPlayedFlag();
				// subs.supervisionPeriodWarningPlayedFlag = subscriber.getSupervisionPeriodWarningPlayedFlag();
				// subs.lowLevelWarningPlayedFlag = subscriber.getLowLevelWarningPlayedFlag();
				// subs.originatingVoiceBlockStatus = subscriber.getOriginatingVoiceBlockStatus();
				// subs.terminatingVoiceBlockStatus = subscriber.getTerminatingVoiceBlockStatus();
				// subs.originatingSMSBlockStatus = subscriber.getOriginatingSMSBlockStatus();
				// subs.terminatingSMSBlockStatus = subscriber.getTerminatingSMSBlockStatus();
				// subs.gprsBlockStatus = subscriber.getGprsBlockStatus();
				subs.serviceClassID = subscriber.getServiceClass();
				subs.originalServiceClassID = subscriber.getServiceClassOriginal();
				// subs.temporaryServiceClassExpiryDate = getTemporaryServiceClassExpiryDate();
				subs.accountBalance = subscriber.getAccountValue1();
				subs.accountActivatedFlag = subscriber.getAccountActivatedFlag();
				subs.serviceFeeExpiryDate = subscriber.getServiceFeeExpiryDate();
				subs.supervisionPeriodExpiryDate = subscriber.getSupervisionExpiryDate();
				// subs.lastServiceFeeDeductionDate = subscriber.getLastServiceFeeDeductionDate();
				// subs.accountDisconnectionDate = subscriber.getAccountDisconnectionDate();
				// subs.serviceFeeExpiryFlag = subscriber.getServiceFeeExpiryFlag();
				// subs.serviceFeeExpiryWarningFlag = subscriber.getServiceFeeExpiryWarningFlag();
				subs.creditClearanceDate = subscriber.getCreditClearanceDate();
				// subs.supervisionExpiryFlag = subscriber.getSupervisionExpiryFlag();
				// subs.supervisionExpiryWarningFlag = subscriber.getSupervisionExpiryWarningFlag();
				subs.negativeBalanceBarringStartDate = subscriber.getNegativeBalanceBarringDate();
				// subs.negativeBalanceBarredFlag = subscriber.getNegativeBalanceBarredFlag();
				// subs.accountInEuroFlag = subscriber.getAccountInEuroFlag();
				// subs.activeServiceDisabledFlag = subscriber.getActiveServiceDisabledFlag();
				// subs.passiveServiceDisabledFlag = subscriber.getPassiveServiceDisabledFlag();
				// subs.convergedFlag = subscriber.getConvergedFlag();
				// subs.lifeCycleNotificationReport = subscriber.getLifeCycleNotificationReport();
				// subs.serviceOfferings = subscriber.getServiceOfferings();
				subs.accountGroupID = coerce(subscriber.getAccountGroupID());
				// subs.communityID1 = subscriber.getCommunityID1();
				// subs.communityID2 = subscriber.getCommunityID2();
				// subs.communityID3 = subscriber.getCommunityID3();
				subs.accountActivatedDate = subscriber.getActivationDate();

				if (versionNo >= 3300)
				{
					// ((SubscriberFileV3_3)subs).globalID = subscriber.getGlobalID(); // Global Id
					((SubscriberFileV3_3) subs).accountPrepaidEmptyLimit = coerce(subscriber.getAccountPrepaidEmptyLimit1()); // Account Prepaid Empty Limit (Not use, under development)
				}

				fw.write(FileField.getCsv(fields, subs));

			}

		}
		catch (IOException | InstantiationException | IllegalAccessException e)
		{
			logger.error("Unable to write file", e);
		}

		// Rename the file
		theFile.renameTo(new File(theDir, filename));

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Community IDs
	//
	// /////////////////////////////////
	@Override
	public boolean updateCommunityIDs(String msisdn, int[] communityIDs)
	{
		// Get Subscriber
		logger.debug( "Update Community IDs for {}", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		if (communityIDs == null)
			communityIDs = new int[0];
		if (communityIDs.length > 3)
			return false;

		List<Integer> list = new ArrayList<Integer>();
		for (int index = 0; index < communityIDs.length; index++)
		{
			list.add(communityIDs[index]);
		}
		subscriber.setCommunityIDs(list);

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// HLR/Map
	//
	// /////////////////////////////////
	@Override
	public boolean setHlrData(String msisdn, Integer stateId, Integer domain, Integer mnpStatusId, Integer mobileCountryCode, //
			Integer mobileNetworkCode, Integer locationAreaCode, Integer cellIdentity, String imsi, String imei)
	{
		// Get Subscriber
		logger.debug( "Set HLR/Map Data for {}", msisdn);
		SubscriberEx subscriber = getSubscriberEx(msisdn);
		if (subscriber == null)
			return false;

		if (stateId != null)
			subscriber.setStateId(stateId);
		if (domain != null)
			subscriber.setDomain(domain);
		if (mnpStatusId != null)
			subscriber.setMnpStatusId(mnpStatusId);
		if (mobileCountryCode != null)
			subscriber.setMobileCountryCode(mobileCountryCode);
		if (mobileNetworkCode != null)
			subscriber.setMobileNetworkCode(mobileNetworkCode);
		if (locationAreaCode != null)
			subscriber.setLocationAreaCode(locationAreaCode);
		if (cellIdentity != null)
			subscriber.setCellIdentity(cellIdentity);

		subscriber.setImsi(imsi);
		subscriber.setImei(imei);

		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Error Injections
	//
	// /////////////////////////////////

	@Override
	public void injectResponse(AirCalls airCall, int responseCode, int delay_ms)
	{
		logger.debug( "Injecting response [{}] with response code {}.", airCall.toString(), responseCode);
		injectSelectiveResponse(airCall, responseCode, delay_ms, 0, Integer.MAX_VALUE);
	}

	@Override
	public void injectSelectiveResponse(AirCalls airCall, int responseCode, int delay_ms, int skipCount, int failCount)
	{
		logger.debug( "Injecting selective response [{}] with response code {}. [{},{}]", airCall.toString(), responseCode, skipCount, failCount);
		InjectedResponse injectedResponse = new InjectedResponse(airCall, responseCode, delay_ms, skipCount, failCount);
		injectedResponses.put(airCall.toString() + "Request", injectedResponse);
	}

	public void resetInjectedResponse(AirCalls airCall)
	{
		logger.debug( "Resetting injected response [{}]", airCall.toString());
		injectedResponses.remove(airCall.toString() + "Request");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Usage
	//
	// /////////////////////////////////

	@Override
	public boolean startUsageTimers(StartUsageRequest request)
	{
		if (request == null || request.getUsageTimers() == null || request.getUsageTimers().length == 0)
			return false;

		for (UsageTimer timer : request.getUsageTimers())
		{
			SubscriberEx subscriber = getSubscriberEx(timer.getMsisdn());
			if (subscriber == null || !subscriber.startUsage(timer, scheduledThreadPool))
				return false;
		}

		return true;
	}

	@Override
	public boolean stopUsageTimers(String msisdn)
	{
		if (msisdn != null && msisdn.length() > 0)
		{
			SubscriberEx subscriber = getSubscriberEx(msisdn);
			if (subscriber == null)
				return false;
			return subscriber.stopUsage();
		}
		else
		{
			for (SubscriberEx subscriber : subscribers.values().toArray(new SubscriberEx[subscribers.size()]))
			{
				if (!subscriber.stopUsage())
					return false;
			}
		}

		return true;
	}

	@Override
	public GetUsageResponse getUsageTimers(String msisdn)
	{
		List<UsageTimer> usageTimers = new ArrayList<UsageTimer>();

		if (msisdn != null && msisdn.length() > 0)
		{
			SubscriberEx subscriber = getSubscriberEx(msisdn);
			if (subscriber != null)
				usageTimers.addAll(subscriber.getUsage());
		}
		else
		{
			for (SubscriberEx subscriber : subscribers.values().toArray(new SubscriberEx[subscribers.size()]))
			{
				usageTimers.addAll(subscriber.getUsage());
			}
		}

		GetUsageResponse response = new GetUsageResponse();
		response.setUsageTimers(usageTimers.toArray(new UsageTimer[usageTimers.size()]));
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISimulationData Implementation
	//
	// /////////////////////////////////

	@Override
	public Map<String, InjectedResponse> getInjectedResponses()
	{
		return injectedResponses;
	}

	@Override
	public Gson getGson()
	{
		return gson;
	}

	@Override
	public List<CallHistory> getHistory()
	{
		return history;
	}

	@Override
	public SubscriberEx getInternationalSubscriber(String subscriberNumber)
	{
		String internationalNumber = numberPlan.getInternationalFormat(subscriberNumber);
		if (!internationalNumber.equals(subscriberNumber))
			return null;
		return subscribers.get(internationalNumber);
	}

	@Override
	public SubscriberEx getNationalSubscriber(String subscriberNumber)
	{
		String nationalNumber = numberPlan.getNationalFormat(subscriberNumber);

		if (!nationalNumber.equals(subscriberNumber))
			return null;
		String internationalNumber = numberPlan.getInternationalFormat(subscriberNumber);
		return subscribers.get(internationalNumber);
	}

	@Override
	public String getNaiNumber(String number, Integer nai)
	{
		if (number == null || number.length() == 0)
			return number;

		switch (nai)
		{
			case 0:
			case 1:
				return numberPlan.getInternationalFormat(number);

			case 2:
				return numberPlan.getNationalFormat(number);

			default:
				return number;
		}

	}

	@Override
	public ScheduledThreadPoolExecutor getScheduledThreadPool()
	{
		return scheduledThreadPool;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// @SuppressWarnings("unused")
	// private void codeGen()
	// {
	// Class<Subscriber> cs = Subscriber.class;
	// Method[] methods = cs.getDeclaredMethods();
	// for (Method method : methods)
	// {
	// method.setAccessible(true);
	// String name = method.getName();
	// if (name.startsWith("get"))
	// {
	// System.out??.printf("subscriberEx.%s(subscriber.%s());\n", "s" + name.substring(1), name);
	// }
	//
	// }
	// }

	private boolean coerce(Boolean flag)
	{
		return flag == null ? false : flag;
	}

	private int coerce(Integer number)
	{
		return number == null ? 0 : number;
	}

	private long coerce(Long number)
	{
		return number == null ? 0 : number;
	}

	private ISystemUnderTest getSuT()
	{
		if (sutx != null)
			return sutx;

		logger.debug( "Retrieving system under test.");
		sutx = esb.getFirstConnector(ISystemUnderTest.class);
		logger.debug( "Found {}.", sutx != null ? sutx.getClass().getSimpleName() : "No SuT Connector");
		return sutx;
	}

	@Override
	public String getNextMSISDN(String seed)
	{
		Long number = Long.parseLong(seed) + (msisdnCounter.addAndGet(1));
		String format = String.format("%%0%dd", seed.length());
		return String.format(format, number);
	}

	@Override
	public boolean isCloseTo(Date datetime1, Date datetime2, int tollerance_s)
	{
		if (datetime1 == null || datetime2 == null)
			return false;

		long delta = (datetime1.getTime() - datetime2.getTime()) / 1000L;
		return delta >= -tollerance_s && delta <= tollerance_s;
	}

	@Override
	public Long parseTime(String time)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		try
		{
			return sdf.parse(time).getTime();
		}
		catch (ParseException e1)
		{
			return null;
		}
	}

	private SubscriberEx getSubscriberEx(String msisdn)
	{
		if (msisdn == null)
		{
			logger.error( "Invalid msisdn.");
			return null;
		}

		String internationalNumber = numberPlan.getInternationalFormat(msisdn);
		SubscriberEx subscriber = subscribers.get(internationalNumber);
		if (subscriber == null)
		{
			// logger.debug( "Subscriber ({}) not found.", msisdn);
		}

		return subscriber;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISystem Under Test
	//
	// /////////////////////////////////

	@Override
	public void setup()
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return;

		logger.debug( "{}: setup", sut.getClass().getSimpleName());
		sut.setup();
	}

	@Override
	public SmsHistory[] getSmsHistory()
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getSmsHistory", sut.getClass().getSimpleName());
		ISmsHistory[] history = sut.getSmsHistory();
		if (history == null)
		{
			logger.trace( "No history found.");
			return null;
		}

		SmsHistory[] result = new SmsHistory[history.length];
		for (int index = 0; index < history.length; index++)
		{
			result[index] = new SmsHistory(history[index]);
		}
		return result;
	}

	@Override
	public void clearSmsHistory()
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return;

		logger.debug( "{}: clearSmsHistory", sut.getClass().getSimpleName());
		sut.clearSmsHistory();
	}

	@Override
	public boolean restoreBackup(String backupFilename)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: restoreBackup(backupFilename: {})", sut.getClass().getSimpleName(), backupFilename);
		return sut.restoreBackup(backupFilename);
	}

	@Override
	public void injectMOSms(String from, String to, String text)
	{
		if (from == null)
		{
			logger.error( "Invalid from.");
			return;
		}

		if (to == null)
		{
			logger.error( "Invalid to.");
			return;
		}

		if (text == null)
		{
			logger.error( "Invalid text.");
			return;
		}

		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return;

		logger.debug( "{}: injectMOSms(from: {}, to: {}, text: {})", sut.getClass().getSimpleName(), from, to, text);
		sut.injectMOSms(from, to, text);
	}

	@Override
	public UssdResponse injectMOUssd(String from, String text, String imsi)
	{
		if (from == null)
		{
			logger.error( "Invalid from.");
			return new UssdResponse("Invalid from.", true);
		}

		if (text == null)
		{
			logger.error( "Invalid text.");
			return new UssdResponse("Invalid text.", true);
		}

		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: injectMOUssd(from: {}, text: {})", sut.getClass().getSimpleName(), from, text);
		return new UssdResponse(sut.injectMOUssd(from, text, imsi));
	}

	@Override
	public String getUssdMenuLine(int lineNumber)
	{
		if (lineNumber < 0)
		{
			logger.error( "Invalid line number.");
			return "Invalid line number.";
		}

		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getUssdMenuLine(lineNumber: {})", sut.getClass().getSimpleName(), lineNumber);
		return sut.getUssdMenuLine(lineNumber);
	}

	@Override
	public Cdr getLastCdr()
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getLastCdr", sut.getClass().getSimpleName());
		ICdr cdr = sut.getLastCdr();
		if (cdr == null)
		{
			logger.trace( "No cdr found.");
			return null;
		}

		return new Cdr(cdr);
	}

	@Override
	public Cdr[] getCdr(Filter filters[])
	{
		ICdr[] cdrs = getCdr((IFilter[]) filters);
		if (cdrs == null || cdrs.length == 0)
			return null;

		List<Cdr> cdrsList = new ArrayList<>();
		for (ICdr cdr : cdrs)
		{
			cdrsList.add(new Cdr(cdr));
		}

		return cdrsList.toArray(new Cdr[0]);
	}

	@Override
	public ICdr[] getCdr(IFilter[] filters)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getCdr(filters: {})", sut.getClass().getSimpleName(), filters != null ? "Filters[" + filters.length + "]" : "");
		ICdr cdrs[] = sut.getCdr(filters);
		return cdrs;
	}

	@Override
	public Cdr[] getCdrHistory()
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getCdrHistory", sut.getClass().getSimpleName());
		ICdr[] cdrs = sut.getCdrHistory();
		if (cdrs == null || cdrs.length == 0)
		{
			logger.trace( "No cdrs found.");
			return null;
		}

		Cdr result[] = new Cdr[cdrs.length];
		for (int i = 0; i < cdrs.length; i++)
		{
			if (cdrs[i] == null)
				continue;

			result[i] = new Cdr(cdrs[i]);
		}
		return result;
	}

	@Override
	public void clearCdrHistory()
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return;

		logger.debug( "{}: clearCdrHistory", sut.getClass().getSimpleName());
		sut.clearCdrHistory();
	}

	@Override
	public Lifecycle getLifecycle(String msisdn, String serviceID, String variantID)
	{
		if (msisdn == null)
		{
			logger.error( "Invalid msisdn.");
			return null;
		}

		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getLifecycle(msisdn: {}, serviceID: {}, variantID: {})", sut.getClass().getSimpleName(), msisdn, serviceID, variantID);
		ILifecycle lifecycle = sut.getLifecycle(msisdn, serviceID, variantID);

		if (lifecycle == null)
		{
			logger.trace( "No lifecycle found.");
			return null;
		}

		return new Lifecycle(lifecycle);
	}

	@Override
	public Lifecycle[] getLifecycles(String msisdn)
	{
		if (msisdn == null)
		{
			logger.error( "Invalid msisdn");
			return null;
		}

		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getLifecycles(msisdn: {})", sut.getClass().getSimpleName(), msisdn);
		List<Lifecycle> lifecycles = new ArrayList<>();
		ILifecycle iLifecycles[] = sut.getLifecycles(msisdn);
		if (iLifecycles == null)
		{
			logger.trace( "No lifecycles found.");
			return null;
		}

		for (ILifecycle lifecycle : iLifecycles)
		{
			if (lifecycle != null)
				lifecycles.add(new Lifecycle(lifecycle));
		}

		logger.trace( "Found {} lifecycle records.", lifecycles.size());
		return lifecycles.toArray(new Lifecycle[0]);
	}

	@Override
	public boolean updateLifecycle(Lifecycle lifecycle)
	{
		return updateLifecycle((ILifecycle) lifecycle);
	}

	@Override
	public boolean updateLifecycle(ILifecycle lifecycle)
	{
		if (lifecycle == null)
		{
			logger.error( "Invalid lifecycle.");
			return false;
		}

		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: updateLifecycle(lifecycle: {})", sut.getClass().getSimpleName(), "Lifecycle");
		return sut.updateLifecycle(lifecycle);
	}

	@Override
	public boolean deleteLifecycle(String msisdn, String serviceID, String variantID)
	{
		if (msisdn == null)
		{
			logger.error( "Invalid msisdn.");
			return false;
		}

		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: deleteLifecycle(msisdn: {}, serviceID: {}, variantID: {})", sut.getClass().getSimpleName(), msisdn, serviceID, variantID);
		return sut.deleteLifecycle(msisdn, serviceID, variantID);
	}

	@Override
	public boolean deleteLifecycles(String msisdn)
	{
		if (msisdn == null)
		{
			logger.error( "Invalid msisdn.");
			return false;
		}

		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: deleteLifecycles(msisdn: {})", sut.getClass().getSimpleName(), msisdn);
		return sut.deleteLifecycles(msisdn);
	}

	@Override
	public boolean adjustLifecycle(String msisdn, String serviceID, String variantID, Boolean isBeingProcessed, Date timeStamp)
	{
		if (msisdn == null)
		{
			logger.error( "Invalid msisdn.");
			return false;
		}

		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: adjustLifecycle(msisdn: {})", sut.getClass().getSimpleName(), msisdn);
		return sut.adjustLifecycle(msisdn, serviceID, variantID, isBeingProcessed, timeStamp);
	}

	@Override
	public boolean hasMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: hasMemberLifecycle(msisdn: {}, serviceID: {}, variantID: {}, memberMsisdn: {})", sut.getClass().getSimpleName(), msisdn, serviceID, variantID, memberMsisdn);
		return sut.hasMemberLifecycle(msisdn, serviceID, variantID, memberMsisdn);
	}

	@Override
	public String[] getMembersLifecycle(String msisdn, String serviceID, String variantID)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getMembersLifecycle(msisdn: {}, serviceID: {}, variantID: {})", sut.getClass().getSimpleName(), msisdn, serviceID, variantID);
		return sut.getMembersLifecycle(msisdn, serviceID, variantID);
	}

	@Override
	public boolean addMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: addMemberLifecycle(msisdn: {}, serviceID: {}, variantID: {}, memberMsisdn: {})", sut.getClass().getSimpleName(), msisdn, serviceID, variantID, memberMsisdn);
		return sut.addMemberLifecycle(msisdn, serviceID, variantID, memberMsisdn);
	}

	@Override
	public boolean deleteMemberLifecycle(String msisdn, String serviceID, String variantID, String memberMsisdn)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: deleteMemberLifecycle(msisdn: {}, serviceID: {}, variantID: {}, memberMsisdn: {})", sut.getClass().getSimpleName(), msisdn, serviceID, variantID, memberMsisdn);
		return sut.deleteMemberLifecycle(msisdn, serviceID, variantID, memberMsisdn);
	}

	@Override
	public Alarm getLastAlarm()
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getLastAlarm", sut.getClass().getSimpleName());
		IAlarm alarm = sut.getLastAlarm();
		if (alarm == null)
		{
			logger.trace( "No alarm found.");
			return null;
		}

		return new Alarm(alarm);
	}

	@Override
	public Alarm[] getAlarmHistory()
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getAlarmHistory", sut.getClass().getSimpleName());
		List<Alarm> alarms = new ArrayList<>();

		for (IAlarm alarm : sut.getAlarmHistory())
		{
			if (alarm == null)
				continue;

			alarms.add(new Alarm(alarm));
		}

		logger.trace( "Found {} alarms.", alarms.size());
		return alarms.toArray(new Alarm[0]);
	}

	@Override
	public String nonQuery(String command)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: non-Query", sut.getClass().getSimpleName());
		return sut.nonQuery(command);
	}

	@Override
	public boolean restart(String optionalCommand)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: restart", sut.getClass().getSimpleName());
		return sut.restart(optionalCommand);
	}

	@Override
	public void tearDown()
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return;

		logger.debug( "{}: tearDown", sut.getClass().getSimpleName());
		sut.tearDown();
	}

	@Override
	public TemporalTrigger[] getTemporalTriggers(String serviceID, String variantID, String msisdn, String msisdnB)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return null;

		logger.debug( "{}: getTemporalTrigger(serviceID: {}, variantID: {}, msisdn: {}, msisdnB: {})", sut.getClass().getSimpleName(), serviceID, variantID, msisdn, msisdnB);
		List<TemporalTrigger> temporalTriggers = new ArrayList<>();
		ITemporalTrigger iTemporalTriggers[] = sut.getTemporalTriggers(serviceID, variantID, msisdn, msisdnB);
		if (iTemporalTriggers == null)
			return null;

		for (ITemporalTrigger temporalTrigger : iTemporalTriggers)
		{
			if (temporalTrigger != null)
				temporalTriggers.add(new TemporalTrigger(temporalTrigger));
		}

		logger.trace( "Found {} temporal triggers.", temporalTriggers.size());
		return temporalTriggers.toArray(new TemporalTrigger[0]);
	}

	@Override
	public boolean updateTemporalTrigger(TemporalTrigger temporalTrigger)
	{
		if (temporalTrigger == null)
		{
			logger.error( "Invalid temporarl trigger.");
			return false;
		}

		return updateTemporalTrigger((ITemporalTrigger) temporalTrigger);
	}

	@Override
	public boolean updateTemporalTrigger(ITemporalTrigger temporalTrigger)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: updateTemporalTrigger(temporalTrigger: {})", sut.getClass().getSimpleName(), "TemporalTrigger");
		return sut.updateTemporalTrigger(temporalTrigger);
	}

	@Override
	public boolean deleteTemporalTrigger(TemporalTrigger temporalTrigger)
	{
		if (temporalTrigger == null)
		{
			logger.error( "Invalid temporarl trigger.");
			return false;
		}

		return deleteTemporalTrigger((ITemporalTrigger) temporalTrigger);
	}

	@Override
	public boolean deleteTemporalTrigger(ITemporalTrigger temporalTrigger)
	{
		ISystemUnderTest sut = getSuT();
		if (sut == null)
			return false;

		logger.debug( "{}: deleteTemporalTrigger(temporalTrigger: {})", sut.getClass().getSimpleName(), "TemporalTrigger");
		return sut.deleteTemporalTrigger(temporalTrigger);
	}

	@Override
	public void addSubscribers2(String msisdn, int count, int languageID, int serviceClass, long accountValue, Date activationDate, Date supervisionExpiryDate, Date serviceFeeExpiryDate, Date creditClearanceDate, Date serviceRemovalDate)
	{
		if (msisdn == null)
		{
			logger.error( "Invalid msisdn provided.");
			return;
		}

		Long number = null;
		try
		{
			number = Long.parseLong(msisdn);
		}
		catch (NumberFormatException e)
		{
			logger.debug( "Msisdn is not a valid number: {}", e.getMessage());
			return;
		}

		String format = String.format("%%0%dd", msisdn.length());
		logger.debug( "Adding subscribers starting from {} until {}.", msisdn, String.format(format, number + count));

		for (int index = 0; index < count; index++)
		{
			addSubscriber2(String.format(format, number++), languageID, serviceClass, accountValue, activationDate, supervisionExpiryDate, serviceFeeExpiryDate, creditClearanceDate,
					serviceRemovalDate);
		}

		logger.trace( "{} Subscribers have been added.", count);

	}

}
