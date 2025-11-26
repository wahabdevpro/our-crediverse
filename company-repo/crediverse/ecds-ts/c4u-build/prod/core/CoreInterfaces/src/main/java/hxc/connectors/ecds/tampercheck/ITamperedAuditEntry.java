package hxc.connectors.ecds.tampercheck;

import java.util.Date;

public interface ITamperedAuditEntry { //Change to ITamperedAuditEntry

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract Boolean isTampered();
	public abstract Long getId();
	public abstract Integer getCompanyId();	
	public abstract String getSequenceNo();
	public abstract Date getTimestamp();
	public abstract Integer getUserId();
	public abstract String getIpAddress();
	public abstract String getMacAddress();
	public abstract String getMachineName();
	public abstract String getDomainName();
	public abstract String getDataType();
	public abstract String getAction();
	public abstract String getOldValue();
	public abstract String getNewValue();
	public abstract Long getSignature();

}
