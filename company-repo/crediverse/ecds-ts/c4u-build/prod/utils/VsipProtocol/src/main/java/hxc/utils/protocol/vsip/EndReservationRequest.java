package hxc.utils.protocol.vsip;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

// This message is sent after a refill has been carried out. A successful refill
// will result in the transaction committing, and the voucher state is updated to
// "used". An unsuccessful refill will cause the refill transaction to be rolled back.
// The voucher status will then be reset to "available".

@XmlAccessorType(XmlAccessType.FIELD)
public class EndReservationRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@XmlElement(required = true)
	private String activationCode;
	@XmlElement(required = true)
	private String action;
	@XmlElement(required = true)
	private String subscriberId;
	@XmlElement(required = true)
	private String transactionId;
	private String networkOperatorId;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The activationCode parameter is the unique secret code which is used to
	// refill the account. The activation code may have leading zeros. The element
	// size defined below defines the limit at protocol level, and may be further
	// restricted at application level by the server side.
	//
	// Mandatory

	public String getActivationCode()
	{
		return activationCode;
	}

	public void setActivationCode(String activationCode)
	{
		this.activationCode = activationCode;
	}

	// This flag is used on completion of the refill transaction. It indicates to the
	// Voucher Server if the transaction should be committed or rolled back.
	//
	// Mandatory

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	// The subscriberId parameter is used to identify a subscriber in the system.
	// This field will hold the phone number of the subscriber in the same format
	// as held in the account database. The number is usually in national format.
	// Leading zeroes are allowed.
	//
	// Mandatory

	public String getSubscriberId()
	{
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId)
	{
		this.subscriberId = subscriberId;
	}

	// The transactionId parameter should be unique among transactions, and it
	// must be common among different requests within the same transaction.
	//
	// Mandatory

	public String getTransactionId()
	{
		return transactionId;
	}

	public void setTransactionId(String transactionId)
	{
		this.transactionId = transactionId;
	}

	// The networkOperatorId parameter is used to reference a Mobile Virtual
	// Network Operator. The VS system is capable of administering and managing
	// multiple operators simultaneously. Each Mobile Virtual Network Operator has
	// its own database schema, in which this operator's own vouchers are stored.
	// The parameter is bound to the Mobile Virtual Network Operator functionality,
	// which must be explicitly configured. If not activated, the parameter is not
	// mandatory, in which case all requests are targeted to the default database
	// schema of the VS system.
	//
	// This element is mandatory if Mobile Virtual Network Operator functionality is activated;
	// otherwise, the element is optional.
	//
	// Optional

	public String getNetworkOperatorId()
	{
		return networkOperatorId;
	}

	public void setNetworkOperatorId(String networkOperatorId)
	{
		this.networkOperatorId = networkOperatorId;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public boolean validate(IValidationContext context)
	{
		return Protocol.validateActivationCode(context, true, activationCode) && //
				Protocol.validateAction(context, true, action) && //
				Protocol.validateSubscriberId(context, true, subscriberId) && //
				Protocol.validateTransactionId(context, true, transactionId) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId);
	}

}
