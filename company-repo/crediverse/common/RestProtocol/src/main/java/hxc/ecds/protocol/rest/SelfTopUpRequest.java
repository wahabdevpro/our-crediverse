package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.List;

// REST End-Point: ~/transactions/self_topup
public class SelfTopUpRequest extends TransactionRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private BigDecimal amount;
	private Double latitude;
	private Double longitude;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public BigDecimal getAmount()
	{
		return amount;
	}

	public SelfTopUpRequest setAmount(BigDecimal amount)
	{
		this.amount = amount;
		return this;
	}


	public Double getLatitude() 
	{
		return this.latitude;
	}

	public SelfTopUpRequest setLatitude(Double latitude)
	{
		this.latitude = latitude;
		return this;
	}

	public Double getLongitude() 
	{
		return this.longitude;
	}

	public SelfTopUpRequest setLongitude(Double longitude)
	{
		this.longitude = longitude;
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
				.notNull("amount", amount) //
				.notLess("amount", amount, BigDecimal.ZERO) //
				.isMoney("amount", amount) //
				.toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public SelfTopUpResponse createResponse()
	{
		return new SelfTopUpResponse(this);
	}

}
