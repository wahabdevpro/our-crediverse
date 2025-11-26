package hxc.services.transactions;

import java.io.FileOutputStream;
import java.util.Date;

import hxc.connectors.Channels;
import hxc.connectors.datawarehouse.ICdrHistory;
import hxc.servicebus.RequestModes;
import hxc.servicebus.ReturnCodes;

public interface ICdr extends ICdrHistory
{

	public abstract String getHostName();

	public abstract void setHostName(String hostName);

	public abstract String getCallerID();

	public abstract void setCallerID(String callerID);

	@Override
	public abstract String getA_MSISDN();

	public abstract void setA_MSISDN(String a_MSISDN);

	@Override
	public abstract String getB_MSISDN();

	public abstract void setB_MSISDN(String b_MSISDN);

	@Override
	public abstract Date getStartTime();

	public abstract void setStartTime(Date startTime);

	public abstract String getInboundTransactionID();

	public abstract void setInboundTransactionID(String inboundTransactionID);

	public abstract String getInboundSessionID();

	public abstract void setInboundSessionID(String inboundSessionID);

	@Override
	public abstract Channels getChannel();

	public abstract void setChannel(Channels channel);

	public abstract RequestModes getRequestMode();

	public abstract void setRequestMode(RequestModes requestMode);

	public abstract String getTransactionID();

	public abstract void setTransactionID(String transactionID);

	@Override
	public abstract String getServiceID();

	public abstract void setServiceID(String serviceID);

	@Override
	public abstract String getVariantID();

	public abstract void setVariantID(String variantID);

	@Override
	public abstract String getProcessID();

	public abstract void setProcessID(String processID);

	public abstract String getLastActionID();

	public abstract void setLastActionID(String lastActionID);

	public abstract int getLastExternalResultCode();

	public abstract void setLastExternalResultCode(int lastExternalResultCode);

	@Override
	public abstract int getChargeLevied();

	public abstract void setChargeLevied(int chargeLevied);

	@Override
	public abstract ReturnCodes getReturnCode();

	public abstract void setReturnCode(ReturnCodes returnCode);

	@Override
	public abstract boolean isRolledBack();

	public abstract void setRolledBack(boolean rolledBack);

	@Override
	public abstract boolean isFollowUp();

	public abstract void setFollowUp(boolean followUp);

	@Override
	public abstract String getParam1();

	public abstract void setParam1(String param1);

	@Override
	public abstract String getParam2();

	public abstract void setParam2(String param2);

	public abstract String getAdditionalInformation();

	public abstract void setAdditionalInformation(String additionalInformation);

	public abstract void Write(FileOutputStream stream);

}