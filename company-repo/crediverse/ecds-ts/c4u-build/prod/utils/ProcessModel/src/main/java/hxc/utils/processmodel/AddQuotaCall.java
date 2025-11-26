package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.AddQuotaRequest;
import com.concurrent.hxc.AddQuotaResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ServiceQuota;

import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public class AddQuotaCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(AddQuotaCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantID;
	private IValueT<Number> member;
	private IValueT<String> quotaID;
	private IValueT<String> serviceType;
	private IValueT<String> destination;
	private IValueT<String> daysOfWeek;
	private IValueT<String> timesOfDay;
	private IValueT<Integer> quantity;
	private IValueT<Boolean> rateOnly;

	private static final String FIELD_UNITS = "Units";
	private static final String FIELD_NAME = "Name";
	private static final String FIELD_CHARGE = "Charge";

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
	public AddQuotaCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<String> variantID, IValueT<Number> member, //
			IValueT<String> quotaID, IValueT<String> serviceType, IValueT<String> destination, IValueT<String> daysOfWeek, IValueT<String> timesOfDay, IValueT<Integer> quantity, //
			IValueT<Boolean> rateOnly)
	{
		super(afterAction, errorAction);

		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.member = member;
		this.quotaID = quotaID;
		this.serviceType = serviceType;
		this.destination = destination;
		this.daysOfWeek = daysOfWeek;
		this.timesOfDay = timesOfDay;
		this.quantity = quantity;
		this.rateOnly = rateOnly;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private IValueT<String> units = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			return state.get(AddQuotaCall.this, FIELD_UNITS);
		}
	};

	private IValueT<String> name = new IValueT<String>()
	{
		@Override
		public String getValue(IProcessState state)
		{
			return state.get(AddQuotaCall.this, FIELD_NAME);
		}
	};

	private IValueT<Long> charge = new IValueT<Long>()
	{
		@Override
		public Long getValue(IProcessState state)
		{
			return state.get(AddQuotaCall.this, FIELD_CHARGE);
		}
	};

	public IValueT<String> getVariantID()
	{
		return variantID;
	}

	public IValueT<Number> getMember()
	{
		return member;
	}

	public IValueT<String> getServiceType()
	{
		return serviceType;
	}

	public IValueT<String> getDestination()
	{
		return destination;
	}

	public IValueT<String> getTimesOfDay()
	{
		return timesOfDay;
	}

	public IValueT<Integer> getAmount()
	{
		return quantity;
	}

	public IValueT<String> getUnits()
	{
		return units;
	}

	public IValueT<String> getName()
	{
		return name;
	}

	public IValueT<Long> getCharge()
	{
		return charge;
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
			AddQuotaRequest request = state.getRequest(AddQuotaRequest.class);
			request.setMode(rateOnly.getValue(state) ? RequestModes.testOnly : RequestModes.normal);
			request.setServiceID(serviceID.getValue(state));
			request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setMemberNumber(member.getValue(state));
			ServiceQuota quota = new ServiceQuota();
			request.setQuota(quota);

			if (quotaID != null)
				quota.setQuotaID(quotaID.getValue(state));

			if (serviceType != null)
				quota.setService(serviceType.getValue(state));

			if (destination != null)
				quota.setDestination(destination.getValue(state));

			if (daysOfWeek != null)
				quota.setDaysOfWeek(daysOfWeek.getValue(state));

			if (timesOfDay != null)
				quota.setTimeOfDay(timesOfDay.getValue(state));

			if (quantity != null)
				quota.setQuantity((long) (Integer) quantity.getValue(state));

			AddQuotaResponse response = state.getVasService().addQuota(state, request);
			state.setLastReturnCode(response.getReturnCode());
			if (!response.wasSuccess())
				return errorAction;
			state.set(this, FIELD_UNITS, response.getQuota().getUnits());
			state.set(this, FIELD_NAME, response.getQuota().getName());
			state.set(this, FIELD_CHARGE, response.getChargeLevied());

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
