package hxc.connectors.sms;

import hxc.connectors.smpp.SmppAddress;
import hxc.services.notification.INotificationText;

public interface ISmsConnector
{
	public void send(String fromMSISDN, String toMSISDN, INotificationText notificationText);

	public boolean send(String fromMSISDN, String toMSISDN, INotificationText notificationText, boolean synchronise);

	public ISmsResponse sendRequest(String fromMSISDN, String toMSISDN, INotificationText notificationText);

	public void send(SmppAddress fromAddress, SmppAddress toAddress, INotificationText notificationText);

	public boolean send(SmppAddress fromAddress, SmppAddress toAddress, INotificationText notificationText, boolean synchronise);

	public ISmsResponse sendRequest(SmppAddress fromAddress, SmppAddress toAddress, INotificationText notificationText);

	public ISmsHistory[] getSmsHistory();

	public void clearHistory();
}
