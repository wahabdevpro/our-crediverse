package hxc.utils.processmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.AddCreditTransferRequest;
import com.concurrent.hxc.AddCreditTransferResponse;
import com.concurrent.hxc.Number;

import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public class AddCreditTransferCall extends MethodCall
{
	final static Logger logger = LoggerFactory.getLogger(AddCreditTransferCall.class);
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
	private IValueT<Integer> amount;
	private IValueT<Long> transferLimit;
	private IValueT<String> pin;
	private IValueT<Boolean> rateOnly;

	private static final String ADDTRANS_PINREQ = "AddTransPinReq";

	private IValueT<Boolean> pinRequired = new IValueT<Boolean>()
	{
		@Override
		public Boolean getValue(IProcessState state)
		{
			return state.get(AddCreditTransferCall.this, ADDTRANS_PINREQ);
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public IValueT<Boolean> getPinRequired()
	{
		return pinRequired;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public AddCreditTransferCall(Action afterAction, Action errorAction, IValueT<Number> subscriber, IValueT<String> serviceID, IValueT<String> variantID, IValueT<Number> member, //
			IValueT<String> transferMode, IValueT<Integer> amount, IValueT<Long> transferLimit, //
			IValueT<String> pin, IValueT<Boolean> rateOnly)
	{
		super(afterAction, errorAction);

		this.subscriber = subscriber;
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.member = member;
		this.transferMode = transferMode;
		this.amount = amount;
		this.transferLimit = transferLimit;
		this.pin = pin;
		this.rateOnly = rateOnly;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public IValueT<String> getVariantID()
	{
		return variantID;
	}

	public IValueT<Number> getMember()
	{
		return member;
	}

	public IValueT<Integer> getAmount()
	{
		return amount;
	}

	public IValueT<String> getTransferMode()
	{
		return transferMode;
	}

	public IValueT<Long> getTransferLimit()
	{
		return transferLimit;
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
			AddCreditTransferRequest request = state.getRequest(AddCreditTransferRequest.class);
			request.setMode(rateOnly.getValue(state) ? RequestModes.testOnly : RequestModes.normal);
			request.setServiceID(serviceID.getValue(state));
			request.setSubscriberNumber(subscriber.getValue(state));
			request.setMemberNumber(member.getValue(state));

			request.setTransferMode(transferMode.getValue(state));
			request.setAmount(amount.getValue(state));
			request.setTransferLimit(transferLimit.getValue(state));
			request.setPin(pin != null ? pin.getValue(state) : "");

			AddCreditTransferResponse response = state.getVasService().addCreditTransfer(state, request);
			state.setLastReturnCode(response.getReturnCode());
			if (!response.wasSuccess())
				return errorAction;

			state.set(this, ADDTRANS_PINREQ, response.getRequiresPIN());

			return nextAction;
		}
		catch (Throwable e)
		{
			logger.error("Execution error", e);
			state.setLastReturnCode(ReturnCodes.technicalProblem);
			return errorAction;
		}
	}

}
