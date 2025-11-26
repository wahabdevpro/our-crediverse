package hxc.connectors.ecds.tampercheck;

import java.math.BigDecimal;
import java.util.Date;

public interface ITamperedBatch { //Change to ITamperedBatch

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract Boolean isTampered();
	public abstract Integer getId();
	public abstract Integer getCompanyID();
	public abstract String getFilename();
	public abstract Integer getInsertCount();
	public abstract Integer getUpdateCount();
	public abstract Integer getDeleteCount();
	public abstract Integer getFailureCount();
	public abstract BigDecimal getTotalValue();
	public abstract String getType();
	public abstract Long getFileSize();
	public abstract Date getTimestamp();
	public abstract Integer getWebUserID();
	public abstract String getIpAddress();
	public abstract String getMacAddress();
	public abstract String getMachineName();
	public abstract Long getSignature();
	public abstract String getDomainName();
	/*
		companyID
		filename
		insertCount
		updateCount
		deleteCount
		failureCount
		totalValue
		type
		fileSize
		timestamp
		webUserID
		ipAddress
		macAddress
		machineName
		domainName
	*/
}
