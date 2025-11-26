package hxc.utils.processmodel;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.Number;
import com.concurrent.hxc.SendSMSRequest;
import com.concurrent.hxc.SendSMSResponse;

import hxc.servicebus.ReturnCodes;
import hxc.services.notification.ITexts;
import hxc.services.notification.Texts;

public class SendSmsCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(SendSmsCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriberNumber;
	private IValueT<String> sourceAddress;
	private IValueT<Date> sheduleTime;
	private IValueT<Date> expiryTime;
	private ITexts text;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public IValueT<Number> getSubscriberNumber()
	{
		return subscriberNumber;
	}

	public void setSubscriberNumber(IValueT<Number> subscriberNumber)
	{
		this.subscriberNumber = subscriberNumber;
	}

	public IValueT<String> getSourceAddress()
	{
		return sourceAddress;
	}

	public void setSourceAddress(IValueT<String> sourceAddress)
	{
		this.sourceAddress = sourceAddress;
	}

	public IValueT<Date> getSheduleTime()
	{
		return sheduleTime;
	}

	public void setSheduleTime(IValueT<Date> sheduleTime)
	{
		this.sheduleTime = sheduleTime;
	}

	public IValueT<Date> getExpiryTime()
	{
		return expiryTime;
	}

	public void setExpiryTime(IValueT<Date> expiryTime)
	{
		this.expiryTime = expiryTime;
	}

	public ITexts getText()
	{
		return text;
	}

	public void setText(ITexts text)
	{
		this.text = text;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	public SendSmsCall(Action afterAction, Action errorAction, IValueT<Number> subscriberNumber, IValueT<String> sourceAddress, IValueT<Date> sheduleTime, IValueT<Date> expiryTime, String text)
	{
		super(afterAction, errorAction);

		this.subscriberNumber = subscriberNumber;
		this.sourceAddress = sourceAddress;
		this.sheduleTime = sheduleTime;
		this.expiryTime = expiryTime;
		this.text = new Texts(text);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public Action execute(IProcessState state, String command)
	{
		try
		{
			SendSMSRequest request = state.getRequest(SendSMSRequest.class);

			request.setSubscriberNumber(subscriberNumber.getValue(state));
			request.setSourceAddress(sourceAddress.getValue(state));

			String message = text.getSafeText(state.getLanguageID());

			if (message == null || message.length() == 0)
			{
				return nextAction;
			}

			message = state.getNotifications().get(state.getLanguageCode(), message, state.getLocale(), state.getProperties());

			request.setMessage(message);

			SendSMSResponse response = state.getVasInterface().sendSMS(request);
			state.setLastReturnCode(response.getReturnCode());
			if (!response.wasSuccess())
				return errorAction;

			return nextAction;
		}
		catch (Throwable e)
		{
			logger.error("execution error", e);
			state.setLastReturnCode(ReturnCodes.technicalProblem);
			return errorAction;
		}

	}

}
