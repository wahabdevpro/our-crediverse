package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.AddMemberRequest;
import com.concurrent.hxc.AddMemberResponse;
import com.concurrent.hxc.Number;

import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public class AddMemberCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(AddMemberCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantID;
	private IValueT<Number> member;
	private IValueT<Boolean> rateOnly;

	private static final String NAMES_HAS_CHARGE = "ADDM_HAS_CHARGE";

	private IValueT<Boolean> hasCharge = new IValueT<Boolean>()
	{
		@Override
		public Boolean getValue(IProcessState state)
		{
			return state.get(AddMemberCall.this, NAMES_HAS_CHARGE);
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public IValueT<Boolean> getHasCharge()
	{
		return hasCharge;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public AddMemberCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<String> variantID, IValueT<Number> member, IValueT<Boolean> rateOnly)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.member = member;
		this.rateOnly = rateOnly;
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
			AddMemberRequest request = state.getRequest(AddMemberRequest.class);

			request.setServiceID(serviceID.getValue(state));
			if (variantID != null)
				request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setMemberNumber(member.getValue(state));
			request.setMode(rateOnly.getValue(state) ? RequestModes.testOnly : RequestModes.normal);

			AddMemberResponse response = state.getVasService().addMember(state, request);
			state.setLastReturnCode(response.getReturnCode());
			if (!response.wasSuccess())
				return errorAction;

			state.set(this, NAMES_HAS_CHARGE, response.getChargeLevied() != 0);

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
