package hxc.services.airsim.protocol;

import java.util.Date;

public interface ITemporalTrigger
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract String getServiceID();

	public abstract String getVariantID();

	public abstract String getMsisdnA();

	public abstract String getMsisdnB();

	public abstract String getKeyValue();

	public abstract Date getNextDateTime();

	public abstract boolean isBeingProcessed();

	public abstract int getState();

}