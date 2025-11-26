package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Date;

public class DepositsQueryResponse extends TransactionResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Date date;
	private int count;
	private BigDecimal amount;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}

	public BigDecimal getAmount()
	{
		return amount;
	}

	public void setAmount(BigDecimal amount)
	{
		this.amount = amount;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public DepositsQueryResponse()
	{

	}

	public DepositsQueryResponse(DepositsQueryRequest request)
	{
		super(request);
	}

}
