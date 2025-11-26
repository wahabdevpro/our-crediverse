package hxc.userinterfaces.gui.translations;

import hxc.userinterfaces.gui.controller.CustomerCare;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

public class CreditSharingTranslations
{

	//@formatter:off
	public enum MessageContext
	{
		defaultMessage, 
		subscribeSuccess, 
		unsubscribeSuccess, 
		consumerRemoveSuccess, 
		consumerAddSuccess, 
		quotaAddSuccess, 
		quotaAddSuccessTest, 
		quotaRemoveSuccess, 
		webServicesUnavailable, 
		quotaQuantityUpdateSuccess,

		confirmSubscribeMessageNoCost, 
		confirmSubscribeMessageWithCost, 
		confirmUnsubscribeMessageNoCost, 
		confirmUnsubscribeMessageWithCost, 
		confirmAddConsumerMessageNoCost, 
		confirmAddConsumerMessageWithCost, 
		confirmRemoveConsumerMessageNoCost, 
		confirmRemoveConsumerMessageWithCost, 
		confirmRemoveQuotaMessageNoCost, 
		confirmRemoveQuotaMessageWithCost, 
		confirmUpdateQuotaMessageNoCost, 
		confirmUpdateQuotaMessageWithCost, 
		confirmRemoveOwnerNoCost, 
		confirmRemoveOwnerWithCost,
		
		noBalanceAvailable,
		balanceAvailableHeading,
		balanceRetrievedMessage
	}
	//@formatter:on

	public final int FRENCH = 1;
	public final int ENGLISH = 2;
	Properties props = null;

	public CreditSharingTranslations(int languageId)
	{
		this.props = readProperties(languageId);
	}

	private Properties readProperties(int languageId)
	{
		Properties props = new Properties();
		try
		{
			URL url = CustomerCare.class.getProtectionDomain().getCodeSource().getLocation();
			// URL loadFrom = new URL(url, ".." + File.separatorChar + "creditsharing_" + ((languageId == FRENCH) ? "fr" : "en") + ".properties");
			URL loadFrom = new URL(url, ".." + File.separatorChar + "creditsharing.properties");

			File propFile = null;
			try
			{
				propFile = new File(loadFrom.toURI());
			}
			catch (Exception ex)
			{
				propFile = new File(loadFrom.getPath());
			}

			props.load(new FileInputStream(propFile));
		}
		catch (IOException e)
		{
		}
		return props;
	}

	private String underScoredToCamelCase(String code)
	{
		String result = code.toLowerCase();
		int pos = 0;
		try
		{
			while ((pos = result.indexOf('_')) >= 0)
			{
				result = result.substring(0, pos) + result.substring(pos + 1, pos + 2).toUpperCase() + result.substring(pos + 2);
			}
		}
		catch (Exception e)
		{
		}
		return result;
	}

	private String getTranslatableMessage(MessageContext context)
	{
		if (props.containsKey(context.toString()))
		{
			return props.getProperty(context.toString());
		}
		else if (props.containsKey("defaultMessage"))
		{
			return props.getProperty("defaultMessage").replaceAll("\\{0\\}", context.toString());
		}
		else
		{
			return String.format(context.toString());
		}
	}

	// private String getTranslatableMessage(ReturnCodes returnCode)
	// {
	// String camelReturnCode = underScoredToCamelCase(returnCode.toString());
	// if (props.containsKey(camelReturnCode.toString()))
	// {
	// return props.getProperty(camelReturnCode.toString());
	// }
	// else if (props.containsKey("defaultMessage"))
	// {
	// return props.getProperty("defaultMessage").replaceAll("\\{0\\}", returnCode.toString());
	// }
	// else
	// {
	// return camelReturnCode;
	// }
	// }

	public String translate(MessageContext context, String... parms)
	{
		String message = getTranslatableMessage(context);
		for (int i = 0; i < parms.length; i++)
		{
			message = message.replaceAll("\\{" + i + "\\}", parms[i]);
		}
		return message;
	}

	// public String translate(ReturnCodes returnCode, int languageId)
	// {
	// return getTranslatableMessage(returnCode);
	// }

	public String translate(String message)
	{
		if (props.containsKey(message))
		{
			return props.getProperty(message);
		}
		else
		{
			return String.format(message); // Translate as is
		}
	}

	public String translate(String message, String... parms)
	{
		String msg = translate(message);
		for (int i = 0; i < parms.length; i++)
		{
			msg = msg.replaceAll("\\{" + i + "\\}", parms[i]);
		}
		return msg;
	}

}
