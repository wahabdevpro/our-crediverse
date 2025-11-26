package hxc.ecds.protocol.rest;

import java.util.List;

@Deprecated
/*
 * Functionality on hold MSISDN-RECYCLING
  */
// REST End-Point: ~/transactions/adjust
public class MsisdnSubmitRecycleRequest extends TransactionRequest implements ICoSignable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int REASON_MAX_LENGTH = 100;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private List<Integer> agentIdsToRecycle;
	private String coSignatorySessionID;
	private String coSignatoryTransactionID;
	private String coSignatoryOTP;
	//private int agentID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////


	public List<Integer> getAgentIdsToRecycle() {
		return agentIdsToRecycle;
	}

	public void setAgentIdsToRecycle(List<Integer> agentIdsToRecycle) {
		this.agentIdsToRecycle = agentIdsToRecycle;
	}

	@Override
	public String getCoSignatorySessionID()
	{
		return coSignatorySessionID;
	}

	@Override
	public MsisdnSubmitRecycleRequest setCoSignatorySessionID(String coSignatorySessionID)
	{
		this.coSignatorySessionID = coSignatorySessionID;
		return this;
	}

	@Override
	public String getCoSignatoryTransactionID()
	{
		return this.coSignatoryTransactionID;
	}

	@Override
	public MsisdnSubmitRecycleRequest setCoSignatoryTransactionID(String coSignatoryTransactionID)
	{
		this.coSignatoryTransactionID = coSignatoryTransactionID;
		return this;
	}

	@Override
	public String getCoSignatoryOTP()
	{
		return this.coSignatoryOTP;
	}

	@Override
	public MsisdnSubmitRecycleRequest setCoSignatoryOTP(String coSignatoryOTP)
	{
		this.coSignatoryOTP = coSignatoryOTP;
		return this;
	}



	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	@SuppressWarnings("unchecked")
	@Override
	public MsisdnSubmitRecycleResponse createResponse()
	{
		return new MsisdnSubmitRecycleResponse(this);
	}

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator(super.validate());
		validator = CoSignableUtils.validate(validator, this, true);
		return validator.toList();
	}

}
