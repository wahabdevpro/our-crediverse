package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.ChangeQuotaRequest;
import com.concurrent.hxc.ChangeQuotaResponse;
import com.concurrent.hxc.Number;
import com.concurrent.hxc.ServiceQuota;

import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public class UpdateQuotaCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(UpdateQuotaCall.class);
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
	private IValueT<Integer> newQuantity;
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
	public UpdateQuotaCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<String> variantID, IValueT<Number> member,
			IValueT<ServiceQuota> quota, IValueT<Integer> newQuantity, IValueT<Boolean> rateOnly)
	{
		super(afterAction, errorAction);
		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.member = member;
		this.quota = quota;
		this.newQuantity = newQuantity;
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
			ChangeQuotaRequest request = state.getRequest(ChangeQuotaRequest.class);

			request.setServiceID(serviceID.getValue(state));
			request.setVariantID(variantID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setMemberNumber(member.getValue(state));
			request.setOldQuota(quota.getValue(state));
			ServiceQuota newQuota = new ServiceQuota(request.getOldQuota());
			newQuota.setQuantity((long) (int) newQuantity.getValue(state));
			request.setNewQuota(newQuota);
			request.setMode(rateOnly.getValue(state) ? RequestModes.testOnly : RequestModes.normal);

			ChangeQuotaResponse response = state.getVasService().changeQuota(state, request);
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
