package hxc.utils.protocol.vsip;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

// This message is used to reserve a voucher. The message represents the start
// of a refill transaction.
// The additionalAction parameter can be used to request different reservation
// mechanisms, and it controls the need of issuing specific commit or rollback
// messages.

@XmlAccessorType(XmlAccessType.FIELD)
public class ReserveVoucherRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@XmlElement(required = true)
	private String activationCode;
	private String additionalAction;
	private String operatorId;
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

	// Optional

	public String getAdditionalAction()
	{
		return additionalAction;
	}

	public void setAdditionalAction(String additionalAction)
	{
		this.additionalAction = additionalAction;
	}

	// The operatorId parameter is used to define the name of the operator who
	// carried out the operation.
	// When used in a response message it represents the operator that did the
	// change on the voucher.
	//
	// Optional

	public String getOperatorId()
	{
		return operatorId;
	}

	public void setOperatorId(String operatorId)
	{
		this.operatorId = operatorId;
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
				Protocol.validateAdditionalAction(context, false, additionalAction) && //
				Protocol.validateOperatorId(context, false, operatorId) && //
				Protocol.validateSubscriberId(context, true, subscriberId) && //
				Protocol.validateTransactionId(context, true, transactionId) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId);
	}

}
