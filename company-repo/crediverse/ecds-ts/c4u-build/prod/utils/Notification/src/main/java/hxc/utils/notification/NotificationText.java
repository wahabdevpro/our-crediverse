package hxc.utils.notification;

import hxc.services.notification.INotificationText;

public class NotificationText implements INotificationText
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private String text;
	private String languageCode;

	@Override
	public String getText()
	{
		return text;
	}

	@Override
	public String getLanguageCode()
	{
		return languageCode;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public NotificationText(String text, String languageCode)
	{
		this.languageCode = languageCode;
		this.text = text;
	}

}
