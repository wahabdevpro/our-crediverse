package hxc.connectors.smpp;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import hxc.connectors.Channels;
import hxc.connectors.IInteraction;
import hxc.connectors.sms.ISmsConnector;
import hxc.services.notification.INotificationText;

public class SmsRequest implements IInteraction
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String sourceNumber;
	private String destinationNumber;
	private String message;
	private String originTransactionId;
	private Date originTimeStamp;
	private static AtomicInteger originSessionId = new AtomicInteger(100);
	private ISmsConnector connector;
	private String originInterface;

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
	public SmsRequest(String sourceNumber, String destinationNumber, String message, String originTransactionId, Date originTimeStamp, ISmsConnector connector)
	{
		this.sourceNumber = sourceNumber;
		this.destinationNumber = destinationNumber;
		this.message = message;
		this.originTransactionId = originTransactionId;
		this.connector = connector;
		this.originTimeStamp = originTimeStamp;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IInteraction implementation
	//
	// /////////////////////////////////

	@Override
	public String getMSISDN()
	{
		return sourceNumber;
	}

	@Override
	public String getShortCode()
	{
		return destinationNumber;
	}

	@Override
	public boolean reply(INotificationText notificationText)
	{
		String fromMSISDN = destinationNumber;
		String toMSISDN = sourceNumber;
		return connector.send(fromMSISDN, toMSISDN, notificationText, true);
	}

	@Override
	public Channels getChannel()
	{
		return Channels.SMS;
	}

	@Override
	public String getInboundTransactionID()
	{
		return originTransactionId;
	}

	@Override
	public String getInboundSessionID()
	{
		return Integer.toString(originSessionId.getAndIncrement());
	}

	@Override
	public String getIMSI()
	{
		return null;
	}
	
	@Override
	public void setRequest(boolean request)
	{
		// Not Required
	}
	
	@Override
	public Date getOriginTimeStamp()
	{
		return originTimeStamp;
	}

	@Override
	public String getOriginInterface() {
		return originInterface;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Property Methods
	//
	// /////////////////////////////////
	public void setSourceNumber(String sourceNumber)
	{
		this.sourceNumber = sourceNumber;
	}

	public String getSourceNumber()
	{
		return sourceNumber;
	}

	public void setDestinationNumber(String destinationNumber)
	{
		this.destinationNumber = destinationNumber;
	}

	public String getDestinationNumber()
	{
		return destinationNumber;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	@Override
	public String getMessage()
	{
		return message;
	}

	public void setOriginInterface(String originInterface) {
		this.originInterface = originInterface;
	}
}
