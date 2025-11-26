package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.List;

// REST End-Point: ~/transactions/sell
public class SellRequest extends TransactionRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String targetMSISDN;
	private BigDecimal amount;
	private Double latitude;
	private Double longitude;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getTargetMSISDN()
	{
		return targetMSISDN;
	}

	public SellRequest setTargetMSISDN(String targetMSISDN)
	{
		this.targetMSISDN = targetMSISDN;
		return this;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public SellRequest setAmount(BigDecimal amount)
	{
		this.amount = amount;
		return this;
	}

	public Double getLatitude() 
	{
		return this.latitude;
	}

	public SellRequest setLatitude(Double latitude)
	{
		this.latitude = latitude;
		return this;
	}

	public Double getLongitude() 
	{
		return this.longitude;
	}

	public SellRequest setLongitude(Double longitude)
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
				.notEmpty("targetMSISDN", targetMSISDN, MSISDN_MAX_LENGTH) //
				.notNull("amount", amount) //
				.notLess("amount", amount, BigDecimal.ZERO) //
				.isMoney("amount", amount) //
				.toList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public SellResponse createResponse()
	{
		return new SellResponse(this);
	}

}
