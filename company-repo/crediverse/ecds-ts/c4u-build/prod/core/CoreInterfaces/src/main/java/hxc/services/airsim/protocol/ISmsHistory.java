package hxc.services.airsim.protocol;

import java.util.Date;

public interface ISmsHistory
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract Date getTime();

	public abstract String getFromMSISDN();

	public abstract String getToMSISDN();

	public abstract String getText();

}