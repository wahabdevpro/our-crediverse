package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.GetQuotasRequest;
import com.concurrent.hxc.GetQuotasResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ServiceQuota;

import hxc.servicebus.ReturnCodes;

public class GetQuotaCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(GetQuotaCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantID;
	private IValueT<Number> member;
	private IValueT<ServiceQuota> quota;

	private static final String NAME_SERVICE = "ServiceNames";
	private static final String NAME_TOD = "TimesOfDayNames";
	private static final String NAME_DOW = "DaysOfWeekNames";
	private static final String NAME_DESTINATION = "DestinationNames";
	private static final String NAME_UNITS = "UnitsName";

	private IValueT<String> serviceName = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			return state.get(GetQuotaCall.this, NAME_SERVICE);
		}
	};

	private IValueT<String> timesOfDayName = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			return state.get(GetQuotaCall.this, NAME_TOD);
		}
	};

	private IValueT<String> daysOfWeekName = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			return state.get(GetQuotaCall.this, NAME_DOW);
		}
	};

	private IValueT<String> destinationName = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			return state.get(GetQuotaCall.this, NAME_DESTINATION);
		}
	};

	private IValueT<String> units = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			return state.get(GetQuotaCall.this, NAME_UNITS);
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public IValueT<String> getServiceName()
	{
		return serviceName;
	}

	public IValueT<String> getTimesOfDayName()
	{
		return timesOfDayName;
	}

	public IValueT<String> getDaysOfWeekName()
	{
		return daysOfWeekName;
	}

	public IValueT<String> getDestinationName()
	{
		return destinationName;
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
	public GetQuotaCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, //
			IValueT<String> variantID, IValueT<Number> member, //
			IValueT<ServiceQuota> quota)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.member = member;
		this.quota = quota;
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

			request.setServiceID(serviceID.getValue(state));
			request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setMemberNumber(member.getValue(state));
			request.setQuotaID(quota.getValue(state).getQuotaID());

			GetQuotasResponse response = state.getVasService().getQuotas(state, request);
			state.setLastReturnCode(response.getReturnCode());
			if (!response.wasSuccess())
				return errorAction;

			ServiceQuota[] quotas = response.getServiceQuotas();
			ServiceQuota quota = quotas.length > 0 ? quotas[0] : null;

			state.set(this, NAME_SERVICE, quota == null ? null : quota.getService());
			state.set(this, NAME_DESTINATION, quota == null ? null : quota.getDestination());
			state.set(this, NAME_TOD, quota == null ? null : quota.getTimeOfDay());
			state.set(this, NAME_DOW, quota == null ? null : quota.getDaysOfWeek());
			state.set(this, NAME_UNITS, quota == null ? null : quota.getUnits());

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
