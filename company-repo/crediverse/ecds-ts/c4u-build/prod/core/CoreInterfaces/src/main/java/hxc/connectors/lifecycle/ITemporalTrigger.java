package hxc.connectors.lifecycle;

import java.util.Date;

public interface ITemporalTrigger
{
	public abstract String getServiceID();

	public abstract void setServiceID(String serviceID);

	public abstract String getVariantID();

	public abstract void setVariantID(String variantID);

	public abstract String getMsisdnA();

	public abstract void setMsisdnA(String msisdnA);

	public abstract String getMsisdnB();

	public abstract void setMsisdnB(String msisdnB);

	public abstract String getKeyValue();

	public abstract void setKeyValue(String key);

	public abstract Date getNextDateTime();

	public abstract void setNextDateTime(Date nextDateTime);

	public abstract boolean isBeingProcessed();

	public abstract void setBeingProcessed(boolean beingProcessed);

	public abstract int getState();

	public abstract void setState(int state);

	public abstract Date getDateTime1();

	public abstract void setDateTime1(Date dateTime1);

	public abstract Date getDateTime2();

	public abstract void setDateTime2(Date dateTime2);

	public abstract Date getDateTime3();

	public abstract void setDateTime3(Date dateTime3);

	public abstract Date getDateTime4();

	public abstract void setDateTime4(Date dateTime4);

}