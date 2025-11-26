package hxc.services.sharedaccounts.reporting;

import java.math.BigDecimal;
import java.util.Date;

public class SharedAccountsDetailedRevenueReportData
{
	private Date SDATE;
	private BigDecimal VOICE_REV;
	private BigDecimal SMS_REV;
	private BigDecimal DATA_REV;
	private BigDecimal MMS_REV;
	private BigDecimal AIRTIME_REV;
	private BigDecimal GLOBAL_REV;

	public Date getSDATE()
	{
		return SDATE;
	}

	public void setSDATE(Date sDATE)
	{
		SDATE = sDATE;
	}

	public BigDecimal getVOICE_REV()
	{
		return VOICE_REV;
	}

	public void setVOICE_REV(BigDecimal vOICE_REV)
	{
		VOICE_REV = vOICE_REV;
	}

	public BigDecimal getSMS_REV()
	{
		return SMS_REV;
	}

	public void setSMS_REV(BigDecimal sMS_REV)
	{
		SMS_REV = sMS_REV;
	}

	public BigDecimal getDATA_REV()
	{
		return DATA_REV;
	}

	public void setDATA_REV(BigDecimal dATA_REV)
	{
		DATA_REV = dATA_REV;
	}

	public BigDecimal getMMS_REV()
	{
		return MMS_REV;
	}

	public void setMMS_REV(BigDecimal mMS_REV)
	{
		MMS_REV = mMS_REV;
	}

	public BigDecimal getGLOBAL_REV()
	{
		return GLOBAL_REV;
	}

	public void setGLOBAL_REV(BigDecimal gLOBAL_REV)
	{
		GLOBAL_REV = gLOBAL_REV;
	}

	
	public BigDecimal getAIRTIME_REV()
	{
		return AIRTIME_REV;
	}

	public void setAIRTIME_REV(BigDecimal aIRTIME_REV)
	{
		AIRTIME_REV = aIRTIME_REV;
	}

	public SharedAccountsDetailedRevenueReportData(Date sDATE, BigDecimal vOICE_REV, BigDecimal sMS_REV, BigDecimal dATA_REV, BigDecimal mMS_REV, BigDecimal aIRTIME_REV, BigDecimal gLOBAL_REV)
	{
		SDATE = sDATE;
		VOICE_REV = vOICE_REV;
		SMS_REV = sMS_REV;
		DATA_REV = dATA_REV;
		MMS_REV = mMS_REV;
		GLOBAL_REV = gLOBAL_REV;
		AIRTIME_REV = aIRTIME_REV;
	}

	public SharedAccountsDetailedRevenueReportData()
	{
	}

}
