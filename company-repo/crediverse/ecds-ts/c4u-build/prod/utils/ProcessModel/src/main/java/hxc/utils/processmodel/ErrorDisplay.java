package hxc.utils.processmodel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.concurrent.hxc.GetReturnCodeTextRequest;
import com.concurrent.hxc.GetReturnCodeTextResponse;

import hxc.connectors.Channels;
import hxc.servicebus.HostInfo;
import hxc.servicebus.RequestModes;
import hxc.services.notification.ITexts;
import hxc.services.notification.Texts;
import hxc.utils.processmodel.ui.UIProperties;

public class ErrorDisplay extends Action
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<String> serviceID;
	private ITexts suffixText;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@UIProperties(category = "Texts", editable = true)
	public ITexts getSuffixText()
	{
		return suffixText;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	public ErrorDisplay(Action afterAction, IValueT<String> serviceID)
	{
		this(afterAction, serviceID, null, null);
	}

	public ErrorDisplay(Action afterAction, IValueT<String> serviceID, String suffixText, Action nextAction)
	{
		super(afterAction);
		this.serviceID = serviceID;
		this.nextAction = nextAction;
		this.suffixText = new Texts(suffixText);
		this.nextAction = nextAction;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public Action execute(IProcessState state, String command)
	{

		String output = state.getOutput();
		if (output != null && output.length() > 0)
		{
			if (nextAction == null)
				state.setCompleted(true);
			state.setOutput(null);
			return nextAction;
		}
		else
		{
			GetReturnCodeTextRequest request = new GetReturnCodeTextRequest();
			request.setCallerID("USSD");

			request.setCallerID(state.getSubscriberNumber().getAddressDigits());
			request.setChannel(Channels.USSD);
			request.setHostName(HostInfo.getNameOrElseHxC());
			request.setTransactionID("1");
			request.setSessionID("1");
			request.setVersion(GetReturnCodeTextRequest.CURRENT_VERSION);
			request.setMode(RequestModes.normal);
			request.setLanguageID(state.getLanguageID());
			request.setServiceID(serviceID.getValue(state));
			request.setReturnCode(state.getLastReturnCode());
			GetReturnCodeTextResponse response = state.getVasInterface().getReturnCodeText(request);
			String text = response.getReturnCodeText();
			text = state.getNotifications().get(state.getLanguageCode(), text, state.getLocale(), state.getProperties());

			StringBuilder sb = new StringBuilder();
			sb.append(text);
			text = suffixText.getSafeText(state.getLanguageID());
			if (text != null && text.length() > 0)
			{
				sb.append("\n");

				for (Pattern pattern : Menu.patterns)
				{
					Matcher matcher = pattern.matcher(text);
					if (matcher.find())
					{
						String match = matcher.group(1);
						int startIndex = matcher.start(1);
						int endIndex = matcher.end(1);

						String index = match.substring(1);
						if (index.length() == 0)
							index = "1";
						text = text.substring(0, startIndex) + index + text.substring(endIndex);
						break;
					}
				}
				sb.append(text);

			}

			state.setOutput(sb.toString());
			return null;
		}

	}

}
