package hxc.connectors.datawarehouse.reporting;

import java.math.BigDecimal;

public class ActivityReportData
{
	private String serviceID;
	private String variantID;
	private String processID;
	private String channel;
	private Long succeeded;
	private Long failed;
	private BigDecimal chargeLevied;

	public String getServiceID()
	{
		return serviceID;
	}

	public void setServiceID(String serviceID)
	{
		this.serviceID = serviceID;
	}

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

	public String getChannel()
	{
		return channel;
	}

	public void setChannel(String channel)
	{
		this.channel = channel;
	}

	public Long getSucceeded()
	{
		return succeeded;
	}

	public void setSucceeded(Long succeeded)
	{
		this.succeeded = succeeded;
	}

	public Long getFailed()
	{
		return failed;
	}

	public void setFailed(Long failed)
	{
		this.failed = failed;
	}

	public BigDecimal getChargeLevied()
	{
		return chargeLevied;
	}

	public void setChargeLevied(BigDecimal chargeLevied)
	{
		this.chargeLevied = chargeLevied;
	}

	public ActivityReportData(String serviceID, String variantID, String processID, String channel, Long succeeded, Long failed, BigDecimal chargeLevied)
	{
		this.serviceID = serviceID;
		this.variantID = variantID;
		this.processID = processID;
		this.channel = channel;
		this.succeeded = succeeded;
		this.failed = failed;
		this.chargeLevied = chargeLevied;
	}

	public ActivityReportData()
	{
	}

}
