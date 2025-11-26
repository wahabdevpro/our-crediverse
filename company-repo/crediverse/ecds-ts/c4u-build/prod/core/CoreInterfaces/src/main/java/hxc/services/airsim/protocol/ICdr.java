package hxc.services.airsim.protocol;

import java.util.Date;

import hxc.connectors.Channels;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public interface ICdr
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract String getHostName();

	public abstract String getCallerID();

	public abstract String getA_MSISDN();

	public abstract String getB_MSISDN();

	public abstract Date getStartTime();

	public abstract String getInboundTransactionID();

	public abstract String getInboundSessionID();

	public abstract Channels getChannel();

	public abstract RequestModes getRequestMode();

	public abstract String getTransactionID();

	public abstract String getServiceID();

	public abstract String getVariantID();

	public abstract String getProcessID();

	public abstract String getLastActionID();

	public abstract int getLastExternalResultCode();

	public abstract int getChargeLevied();

	public abstract ReturnCodes getReturnCode();

	public abstract boolean isRolledBack();

	public abstract boolean isFollowUp();

	public abstract String getParam1();

	public abstract String getParam2();

	public abstract String[] getAdditionalInformation();

}