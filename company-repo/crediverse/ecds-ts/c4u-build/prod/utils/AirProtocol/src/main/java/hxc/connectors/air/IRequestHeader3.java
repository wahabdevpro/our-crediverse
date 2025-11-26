package hxc.connectors.air;

import hxc.utils.protocol.ucip.MessageCapabilityFlag;

public interface IRequestHeader3 extends IRequestHeader2
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// //////////////////////////////////

	public abstract String getTransactionCode();

	public abstract void setTransactionCode(String transactionCode);

	public abstract String getTransactionType();

	public abstract void setTransactionType(String transactionType);

	public abstract MessageCapabilityFlag getMessageCapabilityFlag();

	public abstract void setMessageCapabilityFlag(MessageCapabilityFlag messageCapabilityFlag);
}
