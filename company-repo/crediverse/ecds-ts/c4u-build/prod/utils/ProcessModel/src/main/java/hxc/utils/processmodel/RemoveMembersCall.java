package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.Number;
import com.concurrent.hxc.RemoveMembersRequest;
import com.concurrent.hxc.RemoveMembersResponse;

import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public class RemoveMembersCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(RemoveMembersCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantID;
	private IValueT<Boolean> rateOnly;

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
	public RemoveMembersCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<String> variantID, IValueT<Boolean> rateOnly)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
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
			RemoveMembersRequest request = state.getRequest(RemoveMembersRequest.class);

			request.setServiceID(serviceID.getValue(state));
			request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setMode(rateOnly.getValue(state) ? RequestModes.testOnly : RequestModes.normal);

			RemoveMembersResponse response = state.getVasService().removeMembers(state, request);
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
