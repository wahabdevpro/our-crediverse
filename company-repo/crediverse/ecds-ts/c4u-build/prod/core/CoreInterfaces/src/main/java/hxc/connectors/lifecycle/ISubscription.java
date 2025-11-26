package hxc.connectors.lifecycle;

import java.util.Date;

public interface ISubscription
{
	public abstract String getMsisdn();

	public abstract String getServiceID();

	public abstract String getVariantID();

	public abstract int getServiceClass();

	public abstract void setServiceClass(int serviceClass);

	public abstract int getState();

	public abstract void setState(int state);

	public abstract Date getNextDateTime();

	public abstract void setNextDateTime(Date nextDateTime);

	public abstract boolean isBeingProcessed();

	public abstract void setBeingProcessed(boolean beingProcessed);

	public abstract Date getDateTime1();

	public abstract void setDateTime1(Date dateTime1);

	public abstract Date getDateTime2();

	public abstract void setDateTime2(Date dateTime2);

	public abstract Date getDateTime3();

	public abstract void setDateTime3(Date dateTime3);

	public abstract Date getDateTime4();

	public abstract void setDateTime4(Date dateTime4);

}
