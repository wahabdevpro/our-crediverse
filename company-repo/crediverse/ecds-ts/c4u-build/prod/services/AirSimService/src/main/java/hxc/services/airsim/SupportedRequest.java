package hxc.services.airsim;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import hxc.connectors.air.Air;
import hxc.connectors.air.IRequestHeader;
import hxc.connectors.air.IResponseHeader;
import hxc.services.airsim.model.SubscriberEx;
import hxc.services.airsim.protocol.CallHistory;
import hxc.services.airsim.protocol.Subscriber;
import hxc.services.airsim.protocol.SubscriberState;
import hxc.utils.calendar.DateTime;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.FieldInfo;
import hxc.utils.reflection.ReflectionHelper;

public abstract class SupportedRequest<Treq, Tresp>
{
	private Class<Treq> requestType;

	private static final DateTime dateInfinite = new DateTime(9999, 12, 31);

	protected ISimulationData simulationData;

	protected SupportedRequest(Class<Treq> requestType)
	{
		this.requestType = requestType;
	}

	final public Tresp execute(Object request, ISimulationData simulationData) throws Exception
	{
		this.simulationData = simulationData;
		String simpleName = request.getClass().getSimpleName();
		InjectedResponse injectedResponse = simulationData.getInjectedResponses().get(simpleName);
		Tresp response = null;
		try
		{
			response = execute((Treq)request, injectedResponse);
		}
		catch (Exception e1)
		{
			throw (e1);
		}

		CallHistory call = new CallHistory(simulationData.getGson(), simpleName, request, response);
		List<CallHistory> history = simulationData.getHistory();
		history.add(call);
		while (history.size() > 100)
			history.remove(0);

		return response;

	}

	protected abstract Tresp execute(Treq request, InjectedResponse injectedResponse) throws Exception;

	public Class<?> getRequestType()
	{
		return requestType;
	}

	protected boolean validate(IRequestHeader request, IResponseHeader response, InjectedResponse injectedResponse)
	{
		// Response cannot be Null
		if (response == null)
			return false;

		// Assume the worst
		response.setResponseCode(100);

		// Test for injected response
		if (injectedResponse != null)
		{
			int count = injectedResponse.getCount();
			injectedResponse.setCount(count + 1);
			if (count >= injectedResponse.getSkipCount() && count < injectedResponse.getSkipCount() + injectedResponse.getFailCount())
			{
				if (injectedResponse.getDelay_ms() > 0)
				{
					try
					{
						Thread.sleep(injectedResponse.getDelay_ms());
					}
					catch (InterruptedException e)
					{
					}
				}
				response.setResponseCode(injectedResponse.getResponseCode());
				if (response.getResponseCode() <= 0 || response.getResponseCode() > 2)
					return false;
			}
			else
				response.setResponseCode(0);
		}
		else
			response.setResponseCode(0);

		// Request cannot be null
		if (request == null)
			return false;

		if (!validate(request))
		{
			response.setResponseCode(999);
			return false;
		}

		// Copy Fields
		// response.setAvailableServerCapabilities(request.getNegotiatedCapabilities());
		// response.setNegotiatedCapabilities(request.getNegotiatedCapabilities());
		response.setOriginTransactionID(request.getOriginTransactionID());

		return true;
	}

	private boolean validate(IRequestHeader request)
	{
		ClassInfo classInfo = ReflectionHelper.getClassInfo(request.getClass());

		for (FieldInfo field : classInfo.getFields().values())
		{
			Air air = field.getAnnotation(Air.class);
			if (air == null)
				continue;

			Object value;
			try
			{
				value = field.get(request);
			}
			catch (IllegalArgumentException | IllegalAccessException e)
			{
				return false;
			}

			if (air.Mandatory())
			{
				if (value == null || "".equals(value))
				{
					return false;
				}
			}

		}

		return true;
	}

	protected Date addDays(Date date, Integer days)
	{
		if (days == null || date == null)
			return date;

		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

	public Date getDate(Date currentDate, Date newDate, Integer days)
	{
		if (newDate != null && !newDate.before(dateInfinite))
		{
			return null;
		}
		else if (currentDate == null)
		{
			return days != null ? DateTime.getToday().addDays(days) : newDate;
		}
		else
		{
			return days != null ? new DateTime(currentDate).addDays(days) : (newDate == null ? currentDate : newDate);
		}
	}

	public Long getValue(Long currentValue, Long newValue, Long deltaValue)
	{
		if (currentValue == null)
		{
			return deltaValue != null ? deltaValue : newValue;
		}
		else
		{
			return deltaValue != null ? Long.valueOf(currentValue + deltaValue) : (newValue == null ? currentValue : newValue);
		}
	}

	public Tresp exitWith(Tresp response, IResponseHeader member, int responseCode)
	{
		member.setResponseCode(responseCode);
		return response;
	}

	protected Date pseudoNull(Date date)
	{
		if (date != null && !date.before(dateInfinite))
			return null;
		else
			return date;
	}

	public SubscriberEx getSubscriber(IRequestHeader request)
	{
		switch (request.getSubscriberNumberNAI())
		{
			case 0:
			case 1:
				return simulationData.getInternationalSubscriber(request.getSubscriberNumber());
			case 2:
				return simulationData.getNationalSubscriber(request.getSubscriberNumber());

			default:
				return null;
		}
	}

	public Subscriber addSubscriber(String msisdn, int languageID, int serviceClass, long accountValue, SubscriberState state)
	{
		return simulationData.addSubscriber(msisdn, languageID, serviceClass, accountValue, state);
	}

	public String getNaiNumber(String number, Integer nai)
	{
		return simulationData.getNaiNumber(number, nai);
	}

	public boolean isSet(Boolean flag)
	{
		return flag != null && flag;
	}
}
