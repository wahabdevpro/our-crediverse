package hxc.services.sharedaccounts.reporting;

import java.math.BigDecimal;

public class SharedAccountsActivityReportData
{
	private String variantID;
	private String processID;
	private String serviceType;
	private String quotaName;
	private Long totalSubscribers;
	private BigDecimal quantity;
	private BigDecimal revenue;

	public String getVariantID()
	{
		return variantID;
	}

	public void setVariantID(String variantID)
	{
		this.variantID = variantID;
	}

	public String getProcessID()
	{
		return processID;
	}

	public void setProcessID(String processID)
	{
		this.processID = processID;
	}

	public String getServiceType()
	{
		return serviceType;
	}

	public void setServiceType(String serviceType)
	{
		this.serviceType = serviceType;
	}

	public String getQuotaName()
	{
		return quotaName;
	}

	public void setQuotaName(String quotaName)
	{
		this.quotaName = quotaName;
	}

	public Long getTotalSubscribers()
	{
		return totalSubscribers;
	}

	public void setTotalSubscribers(Long totalSubscribers)
	{
		this.totalSubscribers = totalSubscribers;
	}

	public BigDecimal getQuantity()
	{
		return quantity;
	}

	public void setQuantity(BigDecimal quantity)
	{
		this.quantity = quantity;
	}

	public BigDecimal getRevenue()
	{
		return revenue;
	}

	public void setRevenue(BigDecimal revenue)
	{
		this.revenue = revenue;
	}

	public SharedAccountsActivityReportData(String variantID, String processID, String quotaName, BigDecimal quantity, BigDecimal revenue)
	{
		this.variantID = variantID;
		this.processID = processID;
		this.quotaName = quotaName;
		this.quantity = quantity;
		this.revenue = revenue;
	}

	public SharedAccountsActivityReportData()
	{
	}

}
