package hxc.services.airsim.protocol;

import java.util.Date;

public interface ILifecycle
{
	public abstract String getMsisdn();

	public abstract String getServiceID();

	public abstract String getVariantID();

	public abstract String getState();

	public abstract Date getDateTime0();

	public abstract Date getDateTime1();

	public abstract Date getDateTime2();

	public abstract Date getDateTime3();

	public abstract Date getDateTime4();

	public abstract String getMode();

	public abstract boolean isBeingProcessed();

	public abstract boolean isCancelled();

	public abstract String[] getAdditionalInformation();

}
