package hxc.connectors.datawarehouse;

import java.util.Date;

import hxc.connectors.Channels;
import hxc.servicebus.ReturnCodes;

public interface ICdrHistory
{

	public abstract String getA_MSISDN();

	public abstract String getB_MSISDN();

	public abstract Date getStartTime();

	public abstract Channels getChannel();

	public abstract String getServiceID();

	public abstract String getVariantID();

	public abstract String getProcessID();

	public abstract int getChargeLevied();

	public abstract ReturnCodes getReturnCode();

	public abstract boolean isRolledBack();

	public abstract boolean isFollowUp();

	public abstract String getParam1();

	public abstract String getParam2();

}