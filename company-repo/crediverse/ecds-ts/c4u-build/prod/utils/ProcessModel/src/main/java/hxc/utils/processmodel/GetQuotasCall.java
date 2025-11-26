package hxc.utils.processmodel;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.GetQuotasRequest;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ServiceQuota;

import hxc.servicebus.ReturnCodes;

public class GetQuotasCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(GetQuotasCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantID;
	private IValueT<Number> member;
	private IValueT<String> service;
	private IValueT<String> destination;
	private IValueT<String> timeOfDay;
	private IValueT<String> daysOfWeek;
	private IValueT<Boolean> activeOnly;
	private static final String NAMES_SERVICE = "ServiceNames";
	private static final String NAMES_TOD = "TimesOfDayNames";
	private static final String NAMES_DOW = "DaysOfWeekNames";
	private static final String NAMES_DESTINATION = "DestinationNames";
	private static final String NAMES_UNITS = "UnitsName";
	private static final String HAS_QUOTAS = "HasQuotas";
	private static final String QUOTA_LIST = "QuotaList";

	private IValueT<Boolean> has_Quotas = new IValueT<Boolean>()
	{
		@Override
		public Boolean getValue(IProcessState state)
		{
			return state.get(GetQuotasCall.this, HAS_QUOTAS);
		}
	};

	private IValueT<ServiceQuota[]> quotaList = new IValueT<ServiceQuota[]>()
	{
		@Override
		public ServiceQuota[] getValue(IProcessState state)
		{
			return state.get(GetQuotasCall.this, QUOTA_LIST);
		}
	};

	private IValueT<String[]> serviceNames = new IValueT<String[]>()
	{
		@Override
		public String[] getValue(IProcessState state)
		{
			return state.get(GetQuotasCall.this, NAMES_SERVICE);
		}
	};

	private IValueT<String[]> timesOfDayNames = new IValueT<String[]>()
	{
		@Override
		public String[] getValue(IProcessState state)
		{
			return state.get(GetQuotasCall.this, NAMES_TOD);
		}
	};

	private IValueT<String[]> daysOfWeekNames = new IValueT<String[]>()
	{
		@Override
		public String[] getValue(IProcessState state)
		{
			return state.get(GetQuotasCall.this, NAMES_DOW);
		}
	};

	private IValueT<String[]> destinationNames = new IValueT<String[]>()
	{
		@Override
		public String[] getValue(IProcessState state)
		{
			return state.get(GetQuotasCall.this, NAMES_DESTINATION);
		}
	};

	private IValueT<String> units = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			return state.get(GetQuotasCall.this, NAMES_UNITS);
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public IValueT<Boolean> hasQuotas()
	{
		return has_Quotas;
	}

	public IValueT<ServiceQuota[]> getQuotaList()
	{
		return quotaList;
	}

	public IValueT<String[]> getServiceNames()
	{
		return serviceNames;
	}

	public IValueT<String[]> getTimesOfDayNames()
	{
		return timesOfDayNames;
	}

	public IValueT<String[]> getDaysOfWeekNames()
	{
		return daysOfWeekNames;
	}

	public IValueT<String[]> getDestinationNames()
	{
		return destinationNames;
	}

	public IValue getUnits()
	{
		return units;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public GetQuotasCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, //
			IValueT<String> variantID, IValueT<Number> member, //
			IValueT<String> service, IValueT<String> destination, IValueT<String> daysOfWeek, IValueT<String> timeOfDay, //
			IValueT<Boolean> activeOnly)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.member = member;
		this.service = service;
		this.destination = destination;
		this.timeOfDay = timeOfDay;
		this.daysOfWeek = daysOfWeek;
		this.activeOnly = activeOnly;
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
			GetQuotasRequest request = state.getRequest(GetQuotasRequest.class);

			// ?? TODO
			Integer languageID = request.getLanguageID();

			request.setServiceID(serviceID.getValue(state));
			request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setMemberNumber(member.getValue(state));
			request.setActiveOnly(activeOnly.getValue(state));
			if (service != null)
				request.setService(service.getValue(state));
			if (destination != null)
				request.setDestination(destination.getValue(state));
			if (timeOfDay != null)
				request.setTimeOfDay(timeOfDay.getValue(state));
			if (daysOfWeek != null)
				request.setDaysOfWeek(daysOfWeek.getValue(state));

			GetQuotasResponse response = state.getVasService().getQuotas(state, request);
			state.setLastReturnCode(response.getReturnCode());
			if (!response.wasSuccess())
				return errorAction;

			ServiceQuota[] quotas = response.getServiceQuotas();

			state.set(this, HAS_QUOTAS, quotas != null && quotas.length > 0);
			state.set(this, QUOTA_LIST, quotas);

			Map<String, String> services = new LinkedHashMap<String, String>();
			Map<String, String> destinations = new LinkedHashMap<String, String>();
			Map<String, String> timesOfDay = new LinkedHashMap<String, String>();
			Map<String, String> daysOfWeek = new LinkedHashMap<String, String>();

			for (ServiceQuota quota : response.getServiceQuotas())
			{
				addName(quota.getService(), services);
				addName(quota.getDestination(), destinations);
				addName(quota.getTimeOfDay(), timesOfDay);
				addName(quota.getDaysOfWeek(), daysOfWeek);
			}

			state.set(this, NAMES_SERVICE, services.values().toArray(new String[0]));
			state.set(this, NAMES_DESTINATION, destinations.values().toArray(new String[0]));
			state.set(this, NAMES_TOD, timesOfDay.values().toArray(new String[0]));
			state.set(this, NAMES_DOW, daysOfWeek.values().toArray(new String[0]));
			state.set(this, NAMES_UNITS, quotas != null && quotas.length > 0 ? quotas[0].getUnits() : "");

			return nextAction;
		}
		catch (Throwable e)
		{
			logger.error("execution error", e);
			state.setLastReturnCode(ReturnCodes.technicalProblem);
			return errorAction;
		}
	}

	private void addName(String name, Map<String, String> map)
	{
		if (name == null || name.length() == 0)
			return;
		String upperName = name.toUpperCase();
		map.put(upperName, name);
	}

}
