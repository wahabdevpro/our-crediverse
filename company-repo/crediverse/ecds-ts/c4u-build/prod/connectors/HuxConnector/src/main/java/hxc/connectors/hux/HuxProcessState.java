package hxc.connectors.hux;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.IHxC;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.RequestHeader;
import com.concurrent.hxc.ServiceContext;

import hxc.connectors.Channels;
import hxc.connectors.IInteraction;
import hxc.connectors.vas.VasService;
import hxc.servicebus.HostInfo;
import hxc.servicebus.ILocale;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.services.notification.INotificationText;
import hxc.utils.notification.Notifications;
import hxc.utils.processmodel.Action;
import hxc.utils.processmodel.IProcessState;

public class HuxProcessState extends ServiceContext implements IProcessState, IInteraction
{
	final static Logger logger = LoggerFactory.getLogger(HuxProcessState.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ILocale locale;
	private Number subscriberNumber;
	private Action currentAction;
	private boolean completed = false;
	private String transactionID;
	private Date transactionTime;
	private Date lastInteractionTime = new Date();
	private int sessionID;
	private String input;
	private String output;
	private HuxConnection connection;
	private String serviceCode;
	private String requestString;
	private String[] requestFields;
	private VasService vasService;
	private IHxC vasInterface;
	private ReturnCodes lastReturnCode;
	private Map<Action, Map<String, Object>> variables = new HashMap<Action, Map<String, Object>>();
	private Notifications notifications;
	private int maxOutputLength = 80;
	private String sessionKey;
	private String IMSI;
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public ILocale getLocale()
	{
		return locale;
	}

	public void setLocale(ILocale locale)
	{
		this.locale = locale;
	}

	public String getTransactionID()
	{
		return transactionID;
	}

	public void setTransactionID(String transactionID)
	{
		this.transactionID = transactionID;
	}

	@Override
	public Date getOriginTimeStamp()
	{
		return transactionTime;
	}

	public void setTransactionTime(Date transactionTime)
	{
		this.transactionTime = transactionTime;
	}

	public Date getLastInteractionTime()
	{
		return lastInteractionTime;
	}

	public void setLastInteractionTime(Date lastInteractionTime)
	{
		this.lastInteractionTime = lastInteractionTime;
	}

	public int getSessionID()
	{
		return sessionID;
	}

	public void setSessionID(int sessionID)
	{
		this.sessionID = sessionID;
	}

	public boolean isCompleted()
	{
		return completed;
	}

	public void setSubscriberNumber(Number subscriberNumber)
	{
		this.subscriberNumber = subscriberNumber;
	}

	@Override
	public String getInput()
	{
		return input;
	}

	@Override
	public void setInput(String input)
	{
		this.input = input;
	}

	@Override
	public String getOutput()
	{
		return output;
	}

	@Override
	public void setOutput(String output)
	{
		this.output = output;
	}

	public HuxConnection getConnection()
	{
		return connection;
	}

	public void setConnection(HuxConnection connection)
	{
		this.connection = connection;
	}

	@Override
	public int getLanguageID()
	{
		if (subscriber == null)
			return 1;
		else
			return subscriber.getLanguageID();
	}

	@Override
	public String getLanguageCode()
	{
		return locale.getLanguage(getLanguageID());
	}

	public String getServiceCode()
	{
		return serviceCode;
	}

	public void setServiceCode(String serviceCode)
	{
		this.serviceCode = serviceCode;
	}

	public String getRequestString()
	{
		return requestString;
	}

	public void setRequestString(String requestString)
	{
		this.requestString = requestString;
	}
	public String getOriginInterface()
	{
		return "";
	}

	public String[] getRequestFields()
	{
		if (requestFields == null && requestString != null)
		{
			requestFields = requestString.split("[*#]", 0);
			if (requestFields != null && requestFields.length > 1)
				requestFields[0] = serviceCode;
		}

		return requestFields;
	}

	@Override
	public ReturnCodes getLastReturnCode()
	{
		return lastReturnCode;
	}

	@Override
	public void setLastReturnCode(ReturnCodes lastReturnCode)
	{
		this.lastReturnCode = lastReturnCode;
	}

	@Override
	public Notifications getNotifications()
	{
		return notifications;
	}

	public void setNotifications(Notifications notifications)
	{
		this.notifications = notifications;
	}

	@Override
	public int getMaxOutputLength()
	{
		return maxOutputLength;
	}

	public void setMaxOutputLength(int maxOutputLength)
	{
		this.maxOutputLength = maxOutputLength;
	}

	public String getSessionKey()
	{
		return sessionKey;
	}

	public void setSessionKey(String sessionKey)
	{
		this.sessionKey = sessionKey;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public HuxProcessState(ILocale locale)
	{
		this.locale = locale;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IProcessState Implementation
	//
	// /////////////////////////////////
	@Override
	public Number getSubscriberNumber()
	{
		return subscriberNumber;
	}

	@Override
	public void setCompleted(boolean completed)
	{
		this.completed = completed;
	}

	@Override
	public Action getCurrentAction()
	{
		return currentAction;
	}

	@Override
	public void setCurrentAction(Action action)
	{
		this.currentAction = action;
	}

	@Override
	public VasService getVasService()
	{
		return this.vasService;
	}

	@Override
	public void setVasService(hxc.connectors.vas.VasService vasService)
	{
		this.vasService = vasService;
	}

	@Override
	public IHxC getVasInterface()
	{
		return vasInterface;
	}

	@Override
	public void setVasInterface(IHxC vasInterface)
	{
		this.vasInterface = vasInterface;
	}

	@Override
	public <T extends RequestHeader> T getRequest(Class<T> cls)
	{
		try
		{
			T request = (T) cls.newInstance();
			request.setCallerID(getSubscriberNumber().getAddressDigits());
			request.setChannel(Channels.USSD);
			request.setHostName(HostInfo.getNameOrElseHxC());
			request.setTransactionID(this.transactionID);
			request.setSessionID(Integer.toString(this.sessionID));
			request.setVersion("1");
			request.setMode(RequestModes.normal);
			request.setLanguageID(getLanguageID());

			return request;
		}
		catch (InstantiationException | IllegalAccessException e)
		{
			logger.error("Failed to get request", e);
			return null;
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T get(Action action, String variableName)
	{
		Map<String, Object> actionVariables = variables.get(action);
		if (actionVariables == null)
			return null;
		return (T) actionVariables.get(variableName);
	}

	@Override
	public <T> void set(Action action, String variableName, T value)
	{
		Map<String, Object> actionVariables = variables.get(action);
		if (actionVariables == null)
		{
			actionVariables = new HashMap<String, Object>();
			variables.put(action, actionVariables);
		}
		actionVariables.put(variableName, value);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void execute()
	{
		while (!completed)
		{
			Action nextAction = currentAction.execute(this, input);
			if (nextAction == null)
			{
				break;
			}

			currentAction = nextAction;
			input = null;
		}

		// Send Menu
		connection.display(this);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IServiceContext Implementation
	//
	// /////////////////////////////////

	@Override
	public VasService getVasService(String serviceID)
	{
		return vasInterface.getVasService(serviceID);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IInteraction Implementation
	//
	// /////////////////////////////////

	@Override
	public String getMSISDN()
	{
		return subscriberNumber.toMSISDN();
	}

	@Override
	public String getMessage()
	{
		return getInput();
	}

	@Override
	public boolean reply(INotificationText notificationText)
	{
		completed = true;
		logger.trace("USSD State set to completed. notificationText [{}]", notificationText);
		output = notificationText.getText();
		connection.display(this);
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
		return transactionID;
	}

	@Override
	public String getInboundSessionID()
	{
		return Integer.toString(sessionID);
	}

	@Override
	public String getShortCode()
	{
		return getServiceCode();
	}

	@Override
	public String getIMSI()
	{
		return IMSI;
	}

	public void setIMSI(String imsi)
	{
		this.IMSI = imsi;
	}

	@Override
	public void setRequest(boolean request)
	{
		// Not Required
	}



	
}
