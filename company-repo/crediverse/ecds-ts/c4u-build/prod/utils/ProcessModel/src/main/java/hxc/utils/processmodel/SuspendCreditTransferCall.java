package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.Number;
import com.concurrent.hxc.SuspendCreditTransferRequest;
import com.concurrent.hxc.SuspendCreditTransferResponse;

import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public class SuspendCreditTransferCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(SuspendCreditTransferCall.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private IValueT<Number> subscriber;
	private IValueT<String> serviceID;
	private IValueT<String> variantID;
	private IValueT<Number> member;
	private IValueT<String> transferMode;
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
	public SuspendCreditTransferCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<String> variantID, IValueT<Number> member,
			IValueT<String> transferMode, IValueT<Boolean> rateOnly)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.member = member;
		this.transferMode = transferMode;
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
			SuspendCreditTransferRequest request = state.getRequest(SuspendCreditTransferRequest.class);

			request.setServiceID(serviceID.getValue(state));
			if (variantID != null)
				request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setMemberNumber(member.getValue(state));
			request.setTransferMode(transferMode.getValue(state));
			request.setMode(rateOnly.getValue(state) ? RequestModes.testOnly : RequestModes.normal);
			SuspendCreditTransferResponse response = state.getVasService().suspendCreditTransfer(state, request);
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
