package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.MigrateRequest;
import com.concurrent.hxc.MigrateResponse;
import com.concurrent.hxc.Number;

import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public class MigrateCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(MigrateCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> fromServiceID;
	private IValueT<String> fromVariantID;
	private IValueT<String> toServiceID;
	private IValueT<String> toVariantID;
	private IValueT<Boolean> rateOnly;

	private static final String NAMES_HAS_CHARGE = "MIG_HAS_CHARGE";

	private IValueT<Boolean> hasCharge = new IValueT<Boolean>()
	{
		@Override
		public Boolean getValue(IProcessState state)
		{
			return state.get(MigrateCall.this, NAMES_HAS_CHARGE);
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
	public MigrateCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, //
			IValueT<String> fromServiceID, IValueT<String> fromVariantID, //
			IValueT<String> toServiceID, IValueT<String> toVariantID, IValueT<Boolean> rateOnly)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.fromServiceID = fromServiceID;
		this.fromVariantID = fromVariantID;
		this.toServiceID = toServiceID;
		this.toVariantID = toVariantID;
		this.rateOnly = rateOnly;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Method
	//
	// /////////////////////////////////

	@Override
	public Action execute(IProcessState state, String command)
	{
		try
		{
			MigrateRequest request = state.getRequest(MigrateRequest.class);

			request.setServiceID(fromServiceID.getValue(state));
			request.setVariantID(fromVariantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setNewServiceID(toServiceID.getValue(state));
			request.setNewVariantID(toVariantID.getValue(state));
			request.setMode(rateOnly.getValue(state) ? RequestModes.testOnly : RequestModes.normal);

			MigrateResponse response = state.getVasService(request.getServiceID()).migrate(state, request);
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
