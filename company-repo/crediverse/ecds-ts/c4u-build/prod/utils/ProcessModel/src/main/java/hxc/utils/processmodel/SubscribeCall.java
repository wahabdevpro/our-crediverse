package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.Number;
import com.concurrent.hxc.SubscribeRequest;
import com.concurrent.hxc.SubscribeResponse;

import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;
import hxc.utils.processmodel.ui.UIProperties;

public class SubscribeCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(SubscribeCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantID;
	private IValueT<Boolean> rateOnly;

	private static final String NAMES_HAS_CHARGE = "SUBS_HAS_CHARGE";

	private IValueT<Boolean> hasCharge = new IValueT<Boolean>()
	{
		@Override
		public Boolean getValue(IProcessState state)
		{
			return state.get(SubscribeCall.this, NAMES_HAS_CHARGE);
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	@UIProperties(category = "Input", value = "subscriber")
	public IValueT<Number> getSubscriber()
	{
		return subscriber;
	}

	@UIProperties(category = "Input", value = "serviceId")
	public IValueT<String> getServiceID()
	{
		return serviceID;
	}

	@UIProperties(category = "Input", value = "variantId")
	public IValueT<String> getVariantID()
	{
		return variantID;
	}

	public IValueT<Boolean> getHasCharge()
	{
		return hasCharge;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public SubscribeCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<String> variantID, IValueT<Boolean> rateOnly)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
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
			SubscribeRequest request = state.getRequest(SubscribeRequest.class);

			request.setServiceID(serviceID.getValue(state));
			request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setMode(rateOnly.getValue(state) ? RequestModes.testOnly : RequestModes.normal);

			SubscribeResponse response = state.getVasService().subscribe(state, request);
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
