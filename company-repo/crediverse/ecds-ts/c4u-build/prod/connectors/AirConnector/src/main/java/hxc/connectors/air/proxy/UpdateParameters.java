package hxc.connectors.air.proxy;

import hxc.connectors.air.IRequestHeader3;
import hxc.utils.protocol.ucip.MessageCapabilityFlag;

public class UpdateParameters
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String transactionType = "";
	private String transactionCode = "";
	private boolean flagPromotionNotification = false;
	private boolean flagFirstIVRCallSet = false;
	private boolean flagAccountActivation = false;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public String getTransactionType()
	{
		return transactionType;
	}

	public void setTransactionType(String transactionType)
	{
		this.transactionType = transactionType;
	}

	public String getTransactionCode()
	{
		return transactionCode;
	}

	public void setTransactionCode(String transactionCode)
	{
		this.transactionCode = transactionCode;
	}

	public boolean isFlagPromotionNotification()
	{
		return flagPromotionNotification;
	}

	public void setFlagPromotionNotification(boolean flagPromotionNotification)
	{
		this.flagPromotionNotification = flagPromotionNotification;
	}

	public boolean isFlagFirstIVRCallSet()
	{
		return flagFirstIVRCallSet;
	}

	public void setFlagFirstIVRCallSet(boolean flagFirstIVRCallSet)
	{
		this.flagFirstIVRCallSet = flagFirstIVRCallSet;
	}

	public boolean isFlagAccountActivation()
	{
		return flagAccountActivation;
	}

	public void setFlagAccountActivation(boolean flagAccountActivation)
	{
		this.flagAccountActivation = flagAccountActivation;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public UpdateParameters(String transactionType, String transactionCode, boolean flagPromotionNotification, boolean flagFirstIVRCallSet, boolean flagAccountActivation)
	{
		this.transactionType = transactionType;
		this.transactionCode = transactionCode;
		this.flagPromotionNotification = flagPromotionNotification;
		this.flagFirstIVRCallSet = flagFirstIVRCallSet;
		this.flagAccountActivation = flagAccountActivation;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void update(IRequestHeader3 request)
	{
		if (transactionCode != null && transactionCode.length() > 0)
			request.setTransactionCode(transactionCode);

		if (transactionType != null && transactionType.length() > 0)
			request.setTransactionType(transactionType);

		if (flagPromotionNotification || flagFirstIVRCallSet || flagAccountActivation)
		{
			MessageCapabilityFlag messageCapabilityFlag = request.getMessageCapabilityFlag();
			if (messageCapabilityFlag == null)
			{
				messageCapabilityFlag = new MessageCapabilityFlag();
				request.setMessageCapabilityFlag(messageCapabilityFlag);
			}

			messageCapabilityFlag.promotionNotificationFlag = flagPromotionNotification;
			messageCapabilityFlag.firstIVRCallSetFlag = flagFirstIVRCallSet;
			messageCapabilityFlag.accountActivationFlag = flagAccountActivation;
		}

	}

}
