package hxc.connectors;

import java.util.Date;

import hxc.services.notification.INotificationText;

public interface IInteraction
{
	public String getMSISDN();

	public String getShortCode();

	public String getMessage();

	public boolean reply(INotificationText notificationText);

	public Channels getChannel();

	public String getInboundTransactionID();

	public String getInboundSessionID();
	
	public String getIMSI();
	
	public void setRequest(boolean request);
	
	public Date getOriginTimeStamp();

	public String getOriginInterface();
}
