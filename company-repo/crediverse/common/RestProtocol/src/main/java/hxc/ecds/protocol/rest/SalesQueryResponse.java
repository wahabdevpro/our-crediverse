package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.Date;

public class SalesQueryResponse extends TransactionResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Date date;
	private int transfersCount;
	private BigDecimal transfersAmount;
	private int salesCount;
	private BigDecimal salesAmount;
	private int selfTopUpsCount;
	private BigDecimal selfTopUpsAmount;

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

	public int getTransfersCount()
	{
		return transfersCount;
	}

	public SalesQueryResponse setTransfersCount(int transfersCount)
	{
		this.transfersCount = transfersCount;
		return this;
	}

	public BigDecimal getTransfersAmount()
	{
		return transfersAmount;
	}

	public SalesQueryResponse setTransfersAmount(BigDecimal transfersAmount)
	{
		this.transfersAmount = transfersAmount;
		return this;
	}

	public int getSalesCount()
	{
		return salesCount;
	}

	public SalesQueryResponse setSalesCount(int salesCount)
	{
		this.salesCount = salesCount;
		return this;
	}

	public BigDecimal getSalesAmount()
	{
		return salesAmount;
	}

	public SalesQueryResponse setSalesAmount(BigDecimal salesAmount)
	{
		this.salesAmount = salesAmount;
		return this;
	}

	public int getSelfTopUpsCount()
	{
		return selfTopUpsCount;
	}

	public SalesQueryResponse setSelfTopUpsCount(int selfTopUpsCount)
	{
		this.selfTopUpsCount = selfTopUpsCount;
		return this;
	}

	public BigDecimal getSelfTopUpsAmount()
	{
		return selfTopUpsAmount;
	}

	public SalesQueryResponse setSelfTopUpsAmount(BigDecimal selfTopUpsAmount)
	{
		this.selfTopUpsAmount = selfTopUpsAmount;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public SalesQueryResponse()
	{

	}

	public SalesQueryResponse(SalesQueryRequest request)
	{
		super(request);
	}

}
