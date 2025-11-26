package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~/transactions/change_pin
public class ChangePinRequest extends TransactionRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int PIN_MIN_LENGTH = 3;
	public static final int PIN_MAX_LENGTH = 10;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String newPin;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getNewPin()
	{
		return newPin;
	}

	public ChangePinRequest setNewPin(String newPin)
	{
		this.newPin = newPin;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// TransactionRequest
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		return new Validator(super.validate()) //
				.notEmpty("newPin", newPin, PIN_MIN_LENGTH, PIN_MAX_LENGTH) //
				.toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public ChangePinResponse createResponse()
	{
		return new ChangePinResponse(this);
	}

}
