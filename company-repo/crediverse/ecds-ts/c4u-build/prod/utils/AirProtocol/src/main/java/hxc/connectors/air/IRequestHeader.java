package hxc.connectors.air;

import java.util.Date;

public interface IRequestHeader
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract String getOriginNodeType();

	public abstract void setOriginNodeType(String originNodeType);

	public abstract String getOriginHostName();

	public abstract void setOriginHostName(String originHostName);

	public abstract String getOriginTransactionID();

	public abstract void setOriginTransactionID(String originTransactionID);

	public abstract Date getOriginTimeStamp();

	public abstract void setOriginTimeStamp(Date originTimeStamp);

	public abstract Integer getSubscriberNumberNAI();

	public abstract void setSubscriberNumberNAI(Integer subscriberNumberNAI);

	public abstract String getSubscriberNumber();

	public abstract void setSubscriberNumber(String subscriberNumber);

	public abstract Integer[] getNegotiatedCapabilities();

	public abstract void setNegotiatedCapabilities(Integer[] negotiatedCapabilities);

}