package hxc.services.ecds.rest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.Channels;
import hxc.connectors.IInteraction;
import hxc.ecds.protocol.rest.UssdCommand;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.UssdConfig;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.ussd.IMenuProcessor;
import hxc.services.ecds.rest.ussd.MenuConstructor;
import hxc.services.ecds.rest.ussd.MenuProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.notification.INotificationText;
import hxc.utils.protocol.hux.HandleUSSDRequest;
import hxc.utils.protocol.hux.HandleUSSDRequestMembers;
import hxc.utils.xmlrpc.XmlRpcSerializer;

@Path("/ussd")
public class Ussd implements IChannelTarget
{
	final static Logger logger = LoggerFactory.getLogger(Ussd.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String responseXml = "<?xml version=\"1.0\"?>" + "<methodResponse><params><param><value><struct><member><name>TransactionId</name><value><string>%s</string>"
			+ "</value></member><member><name>TransactionTime</name><value><dateTime.iso8601>%s</dateTime.iso8601>"
			+ "</value></member><member><name>USSDResponseString</name><value><string>%s</string></value>"
			+ "</member><member><name>encodingSelection</name><value><array><data><value><struct><member><name>alphabet</name><value>"
			+ "<string>%s</string></value></member><member><name>language</name><value><string>%s</string></value></member></struct></value>"
			+ "</data></array></value></member><member><name>action</name><value><string>%s</string></value></member><member>"
			+ "<name>ResponseCode</name><value><i4>%d</i4></value></member></struct></value></param></params></methodResponse>";

	// 0: TransactionId
	// 1: TransactionTime (20160919T09:02:40)
	// 2: USSDResponseString
	// 3: alphabet latn
	// 4: language
	// 5: action (end)
	// 6: ResponseCode

	private static final String ACTION_REQUEST = "request";
	private static final String ACTION_END = "end";
	
	public static final int COMMAND_REGISTER_PIN = 1;
	public static final int COMMAND_SELL = 2;
	public static final int COMMAND_SELL_BUNDLE = 3;
	public static final int COMMAND_TRANSFER = 4;
	public static final int COMMAND_BALANCE_ENQUIRY = 5;
	public static final int COMMAND_SELF_TOPUP = 6;
	public static final int COMMAND_TRANSACTION_STATUS_ENQUIRY = 7;
	public static final int COMMAND_SALES_QUERY = 8;
	public static final int COMMAND_LAST_TRANSACTION_ENQUIRY = 9;
	public static final int COMMAND_DEPOSITS_QUERY = 10;
	public static final int COMMAND_CHANGE_PIN = 11;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Ussd Command support
	//
	// /////////////////////////////////
	private static Map<Integer, Constructor<?>> ussdProcessors = null;

	static
	{
		ussdProcessors = new HashMap<Integer, Constructor<?>>();
		addUssdProcessor(COMMAND_REGISTER_PIN, RegisterPin.class);
		addUssdProcessor(COMMAND_SELL, Sell.class);
		addUssdProcessor(COMMAND_SELL_BUNDLE, SellBundle.class);
		addUssdProcessor(COMMAND_TRANSFER, Transfer.class);
		addUssdProcessor(COMMAND_BALANCE_ENQUIRY, BalanceEnquiry.class);
		addUssdProcessor(COMMAND_SELF_TOPUP, SelfTopUp.class);
		addUssdProcessor(COMMAND_TRANSACTION_STATUS_ENQUIRY, TransactionStatusEnquiry.class);
		addUssdProcessor(COMMAND_SALES_QUERY, SalesQuery.class);
		addUssdProcessor(COMMAND_LAST_TRANSACTION_ENQUIRY, LastTransactionEnquiry.class);
		addUssdProcessor(COMMAND_DEPOSITS_QUERY, DepositsQuery.class);
		addUssdProcessor(COMMAND_CHANGE_PIN, ChangePin.class);
	}


	private static void addUssdProcessor(int id, Class<?> cls)
	{
		Constructor<?> constructor;
		try
		{
			constructor = cls.getConstructor(ICreditDistribution.class);
			ussdProcessors.put(id, constructor);
		}
		catch (NoSuchMethodException | SecurityException e)
		{
			logger.error("", e);
			return; // No logging available yet
		}
	}

	private IMenuProcessor getUssdProcessor(int id)
	{
		Constructor<?> constructor = ussdProcessors.get(id);
		if (constructor == null)
			return null;
		IMenuProcessor processor;
		try
		{
			processor = (IMenuProcessor) constructor.newInstance(context);
			return processor;
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
		{
			logger.error("Cannot instantiate processor", e);
			return null;
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// HandleUSSDRequest
	//
	// /////////////////////////////////
	@POST
	@Consumes(MediaType.TEXT_XML)
	@Produces(MediaType.TEXT_XML)
	public String handleUSSDRequest(String input)
	{
		final String[] results = new String[] { context.getInvalidUssdCommandNotification(), "eng", ACTION_END };
		String transactionId = "";

		XmlRpcSerializer serializer = new XmlRpcSerializer();
		try (InputStream stream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)))
		{
			HandleUSSDRequest request = serializer.deSerialize(stream, HandleUSSDRequest.class);
			final HandleUSSDRequestMembers members = request.members;
			if (members != null)
			{
				logger.trace("HuX Req From: {} *{}{} Seq: {} Session: {}  requestOriginInterface: {}", //
						members.MSISDN, //
						members.USSDServiceCode, //
						members.USSDRequestString, //
						members.Sequence == null ? "" : members.Sequence, //
						members.SessionId == null ? "" : members.SessionId, //
						members.requestOriginInterface == null ? "" : members.requestOriginInterface);

				transactionId = members.TransactionId;
				
				IInteraction interaction = new IInteraction()
				{

					@Override
					public String getMSISDN()
					{
						return members.MSISDN;
					}

					@Override
					public String getShortCode()
					{
						return members.USSDServiceCode;
					}

					@Override
					public String getMessage()
					{
						return members.USSDRequestString;
					}

					@Override
					public boolean reply(INotificationText notificationText)
					{
						results[0] = notificationText.getText();
						results[1] = notificationText.getLanguageCode();
						return true;
					}

					@Override
					public Channels getChannel()
					{
						return Channels.USSD;
					}

					@Override
					public String getInboundTransactionID()
					{
						return members.TransactionId;
					}

					@Override
					public String getInboundSessionID()
					{
						return members.SessionId != null ? members.SessionId.toString() : null;
					}

					@Override
					public String getIMSI()
					{
						return members.IMSI;
					}

					@Override
					public void setRequest(boolean request)
					{
						results[2] = request ? ACTION_REQUEST : ACTION_END;
					}

					@Override
					public Date getOriginTimeStamp()
					{
						return members.TransactionTime;
					}
					public String getOriginInterface()
					{
						return members.requestOriginInterface;
					}
					
				};

				if (members.response != null && members.response)
					processResponse(interaction);
				else
					context.processUssd(interaction);

			}
		}
		catch (Throwable e)
		{
			logger.error("Cannot read input stream", e);
			results[0] = context.getInvalidUssdCommandNotification();
		}

		return getResponse(transactionId, new Date(), results[0], "latn", results[1], results[2], 0);
	}

	private String getResponse(String transactionId, Date transactionTime, String ussdResponseString, String alphabet, //
			String language, String action, int responseCode)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
		String time = sdf.format(transactionTime);
		if(ussdResponseString.isEmpty())
		{
			ussdResponseString = "Au revoir/Good bye.";
		}
		return String.format(responseXml, transactionId, time, ussdResponseString, alphabet, language, action, responseCode);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Commands
	//
	// /////////////////////////////////
	@GET
	@Path("/commands")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.UssdCommand[] getCommands(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return getCommands(em, session);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/commands", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	private UssdCommand[] getCommands(EntityManager em, Session session)
	{
		List<UssdCommand> commands = new ArrayList<UssdCommand>();
		for (Integer id : ussdProcessors.keySet())
		{
			IMenuProcessor processor = getUssdProcessor(id);
			if (processor == null)
				continue;
			UssdCommand command = new UssdCommand() //
					.setId(id) //
					.setName(processor.menuName()) //
					.setCommandFields(processor.menuCommandFields(em, session.getCompanyID())) //
					.setInformationFields(processor.menuInformationFields(em, session.getCompanyID()));
			commands.add(command);
		}
		return commands.toArray(new UssdCommand[commands.size()]);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@GET
	@Path("/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.UssdConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			UssdConfig ussdConfig = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.UssdConfig.class);
			if (ussdConfig.getMenus() == null || ussdConfig.getMenus().size() == 0)
				constructDefaultMenu(em, session, ussdConfig);

			return ussdConfig;
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/config", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	@Path("/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.UssdConfig ussdConfig, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Transaction.MAY_CONFIG_USSD);
			int companyID = session.getCompanyID();
			context.findCompanyInfoByID(companyID).setConfiguration(em, ussdConfig, session);
			defineChannelFilters(ussdConfig, companyID);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/config", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IChannelTarget
	//
	// /////////////////////////////////
	@Override
	public void defineChannelFilters(EntityManager em, ICreditDistribution context, CompanyInfo company)
	{
		this.context = context;
		UssdConfig configuration = company.getConfiguration(em, UssdConfig.class);
		defineChannelFilters(configuration, company.getCompany().getId());
	}

	private void defineChannelFilters(UssdConfig configuration, int companyID)
	{
		context.defineChannelFilter(this, companyID, Phrase.en(configuration.getUssdMenuCommand()), new Phrase[0], 1);
	}

	@Override
	public boolean processChannelRequest(int companyID, IInteraction interaction, Map<String, String> values, int tag)
	{
		// Get an Agent Session for this MSISDN
		try (EntityManagerEx em = context.getEntityManager())
		{
			// Get an Agent Session
			Session session = context.getSessions().getAgentSession(em, context, companyID, interaction);
			if (session == null)
				return false;

			// Get USSD Configuration
			CompanyInfo companyInfo = context.findCompanyInfoByID(companyID);
			UssdConfig ussdConfig = companyInfo.getConfiguration(em, UssdConfig.class);
			if (ussdConfig.getMenus() == null || ussdConfig.getMenus().size() == 0)
				constructDefaultMenu(em, session, ussdConfig);

			return new MenuProcessor(context)
			{
				@Override
				protected IMenuProcessor getUssdProcessor(int id)
				{
					return Ussd.this.getUssdProcessor(id);
				}
			}.initiate(em, interaction, session, ussdConfig);
		}
		catch (Throwable ex)
		{
			logger.error("Failed to get entity manager", ex);
		}

		return false;
	}

	// Process an Ussd response
	private void processResponse(IInteraction interaction)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			new MenuProcessor(context)
			{
				@Override
				protected IMenuProcessor getUssdProcessor(int id)
				{
					return Ussd.this.getUssdProcessor(id);
				}
			}.processResponse(em, interaction);
		}
		catch (Throwable ex)
		{
			logger.error("Failed to get entity manager", ex);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private void constructDefaultMenu(EntityManager em, Session session, UssdConfig ussdConfig)
	{
		new MenuConstructor()
		{
			@Override
			protected IMenuProcessor getUssdProcessor(int id)
			{
				return Ussd.this.getUssdProcessor(id);
			}
		}.createDefaultMenu(em, session, ussdConfig, getCommands(em, session));
	}

}
