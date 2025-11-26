package hxc.utils.protocol.vsip;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class GetVoucherHistoryTransactionRecords
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String operatorId;
	private String subscriberId;
	@XmlElement(required = true)
	private String newState;
	@XmlElement(required = true)
	private Date timestamp;
	private String transactionId;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

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
	// Optional

	public String getSubscriberId()
	{
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId)
	{
		this.subscriberId = subscriberId;
	}

	// The newState parameter is used to represent the state of the voucher as it is,
	// or will be, after a specific event. Such an event could be a request to change
	// a voucher to the new state, or it could be a historical record that represents a
	// state change in the past.
	//
	// Mandatory

	public String getNewState()
	{
		return newState;
	}

	public void setNewState(String newState)
	{
		this.newState = newState;
	}

	// The timestamp parameter is detailing the time a voucher state change was
	// done.
	//
	// Mandatory

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

	// The transactionId parameter should be unique among transactions, and it
	// must be common among different requests within the same transaction.
	//
	// Optional

	public String getTransactionId()
	{
		return transactionId;
	}

	public void setTransactionId(String transactionId)
	{
		this.transactionId = transactionId;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public boolean validate(IValidationContext context)
	{
		return Protocol.validateOperatorId(context, false, operatorId) && //
				Protocol.validateSubscriberId(context, false, subscriberId) && //
				Protocol.validateNewState(context, true, newState) && //
				Protocol.validateTimestamp(context, true, timestamp) && //
				Protocol.validateTransactionId(context, false, transactionId);
	}

}
