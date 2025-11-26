package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.GetBalancesRequest;
import com.concurrent.hxc.GetBalancesResponse;
import com.concurrent.hxc.Number;

import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public class GetBalancesCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(GetBalancesCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantID;
	private IValueT<Boolean> requestSMS;
	private IValueT<Boolean> rateOnly;

	private static final String NAMES_HAS_CHARGE = "BENQ_HAS_CHARGE";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private IValueT<Boolean> hasCharge = new IValueT<Boolean>()
	{
		@Override
		public Boolean getValue(IProcessState state)
		{
			return state.get(GetBalancesCall.this, NAMES_HAS_CHARGE);
		}
	};

	public IValueT<Boolean> getHasCharge()
	{
		return hasCharge;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public GetBalancesCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, //
			IValueT<String> serviceID, IValueT<String> variantID, IValueT<Boolean> requestSMS, IValueT<Boolean> rateOnly)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.requestSMS = requestSMS;
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
			GetBalancesRequest request = state.getRequest(GetBalancesRequest.class);

			request.setServiceID(serviceID.getValue(state));
			if (variantID != null)
				request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setRequestSMS(requestSMS.getValue(state));
			request.setMode(rateOnly.getValue(state) ? RequestModes.testOnly : RequestModes.normal);

			GetBalancesResponse response = state.getVasService().getBalances(state, request);
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
