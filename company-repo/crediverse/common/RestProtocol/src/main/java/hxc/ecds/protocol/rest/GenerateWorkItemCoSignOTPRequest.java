package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~/work_items/{id}/generate_co_sign_otp
public class GenerateWorkItemCoSignOTPRequest extends RequestHeader
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int CO_SIGN_FOR_SESSION_ID_MAX_LENGTH = CoSignableUtils.CO_SIGN_FOR_SESSION_ID_MAX_LENGTH;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected String coSignForSessionID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getCoSignForSessionID()
	{
		return this.coSignForSessionID;
	}

	public GenerateWorkItemCoSignOTPRequest setCoSignForSessionID( String coSignForSessionID )
	{
		this.coSignForSessionID = coSignForSessionID;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public GenerateWorkItemCoSignOTPResponse createResponse()
	{
		return new GenerateWorkItemCoSignOTPResponse(this);
	}

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
				.notEmpty("coSignForSessionID", this.getCoSignForSessionID(), CO_SIGN_FOR_SESSION_ID_MAX_LENGTH);
				;
		return validator.toList();
	}

}
