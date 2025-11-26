package hxc.connectors.lifecycle;

import java.util.Date;

public interface IMemberQuota
{

	public abstract String getMsisdn();

	public abstract void setMsisdn(String msisdn);

	public abstract String getServiceID();

	public abstract void setServiceID(String serviceID);

	public abstract String getVariantID();

	public abstract void setVariantID(String variantID);

	public abstract String getMemberMsisdn();

	public abstract void setMemberMsisdn(String memberMsisdn);

	public abstract String getQuotaID();

	public abstract void setQuotaID(String quotaID);

	public abstract Long getQuantity();

	public abstract void setQuantity(Long quantity);

	public abstract Date getDateTime1();

	public abstract void setDateTime1(Date dateTime1);

	public abstract int getState();

	public abstract void setState(int state);

}