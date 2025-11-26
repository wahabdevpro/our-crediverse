package hxc.utils.protocol.vsip;

import java.util.Date;

import hxc.utils.xmlrpc.XmlRpcAsString;

// The message GetVoucherDetails is used in order to obtain detailed information
// on an individual voucher.

public class GetVoucherDetailsResponse implements IVsipResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String activationCode;
	private String agent;
	private String batchId;
	private String currency;
	private Date expiryDate;
	private String extensionText1;
	private String extensionText2;
	private String extensionText3;
	private String operatorId;
	private int responseCode;
	private String subscriberId;
	private Date timestamp;
	private String state;
	@XmlRpcAsString
	private long value;
	private String voucherGroup;
	private String serialNumber;
	private Boolean voucherExpired;
	private String supplierId;

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
	// Optional (PC:01915)

	public String getActivationCode()
	{
		return activationCode;
	}

	public void setActivationCode(String activationCode)
	{
		this.activationCode = activationCode;
	}

	// The agent parameter is used to indicate the name of the dealer who has
	// received the card from the service provider.
	//
	// Optional

	public String getAgent()
	{
		return agent;
	}

	public void setAgent(String agent)
	{
		this.agent = agent;
	}

	// The batchId parameter indicates what batch a voucher belongs to. The
	// batchId is assigned when vouchers are generated.
	//
	// Mandatory

	public String getBatchId()
	{
		return batchId;
	}

	public void setBatchId(String batchId)
	{
		this.batchId = batchId;
	}

	// The currency parameter is used to indicate the currency of the voucher value.
	// The currency is expressed as a three letter string according to the ISO 4217
	// standard, see Codes for the representation of currencies and funds, Reference
	// [6]. Examples are "EUR" for Euro and "SEK" for Swedish Kronor.
	//
	// Mandatory

	public String getCurrency()
	{
		return currency;
	}

	public void setCurrency(String currency)
	{
		this.currency = currency;
	}

	// The expiryDate parameter is used to identify the last date when the voucher
	// will be usable in the system. Only the date information will be considered by
	// this parameter. The time and timezone should be set to all zeroes, and will
	// be ignored.
	// TZ is the deviation in hours from UTC. This field is optional. This date format
	// does not strictly follow the XML-RPC specification on date format. It does
	// however follow the ISO 8601 specification. Parsers for this protocol must be
	// prepared to parse dates containing timezone.
	//
	// Mandatory

	public Date getExpiryDate()
	{
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}

	// The extensionText1, extensionText2 and extensionText3 parameters
	// are used to store any additional information connected to a voucher. The
	// definition of the extension texts are site specific and have no special meaning
	// within VS.
	// Note: Space is an allowed character.
	//
	// Optional

	public String getExtensionText1()
	{
		return extensionText1;
	}

	public void setExtensionText1(String extensionText1)
	{
		this.extensionText1 = extensionText1;
	}

	// The extensionText1, extensionText2 and extensionText3 parameters
	// are used to store any additional information connected to a voucher. The
	// definition of the extension texts are site specific and have no special meaning
	// within VS.
	// Note: Space is an allowed character.
	//
	// Optional

	public String getExtensionText2()
	{
		return extensionText2;
	}

	public void setExtensionText2(String extensionText2)
	{
		this.extensionText2 = extensionText2;
	}

	// The extensionText1, extensionText2 and extensionText3 parameters
	// are used to store any additional information connected to a voucher. The
	// definition of the extension texts are site specific and have no special meaning
	// within VS.
	// Note: Space is an allowed character.
	//
	// Optional

	public String getExtensionText3()
	{
		return extensionText3;
	}

	public void setExtensionText3(String extensionText3)
	{
		this.extensionText3 = extensionText3;
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

	// The responseCode parameter is sent back after a message has been
	// processed and indicates success or failure of the message.
	//
	// Mandatory

	@Override
	public int getResponseCode()
	{
		return responseCode;
	}

	@Override
	public void setResponseCode(int responseCode)
	{
		this.responseCode = responseCode;
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

	// The timestamp parameter is detailing the time a voucher state change was
	// done.
	//
	// Optional

	public Date getTimestamp()
	{
		return timestamp;
	}

	public void setTimestamp(Date timestamp)
	{
		this.timestamp = timestamp;
	}

	// The state parameter is used to represent the state of a voucher, as it currently
	// is.
	//
	// Mandatory

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	// The value parameter is used to specify the actual value of the voucher
	// in currency units. The value is formatted as a numeric string. No decimal
	// separator is included. The amount is expressed in the lowest denomination of
	// the specified currency. For example a USD 100 value is represented as 10000.
	//
	// Mandatory

	public long getValue()
	{
		return value;
	}

	public void setValue(long value)
	{
		this.value = value;
	}

	// The voucherGroup parameter is used to define a set of properties that are
	// associated with a voucher. Each voucher is assigned to a voucher group and
	// many vouchers can be assigned the same voucher group.
	//
	// Mandatory

	public String getVoucherGroup()
	{
		return voucherGroup;
	}

	public void setVoucherGroup(String voucherGroup)
	{
		this.voucherGroup = voucherGroup;
	}

	// The serialNumber parameter is used to state the unique voucher serial
	// number that is used to identify the voucher. Leading zeros are allowed. The
	// element size defined below defines the limit at protocol level, and may be
	// further restricted at application level by the server side.
	//
	// The serial number is included in the response when the request includes the activation code.
	//
	// Optional

	public String getSerialNumber()
	{
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber)
	{
		this.serialNumber = serialNumber;
	}

	// The voucherExpired parameter is used to indicate if the voucher has passed
	// the expiration date. When this parameter is set the voucher can no longer be
	// used. If the voucher is not expired this parameter will not be included.
	//
	// Optional

	public Boolean getVoucherExpired()
	{
		return voucherExpired;
	}

	public void setVoucherExpired(Boolean voucherExpired)
	{
		this.voucherExpired = voucherExpired;
	}

	// The supplierId parameter is used to indicate the supplier (print shop) for
	// which voucher batch files with separate encryption keys per supplier will be
	// generated.
	//
	// Optional (PC:09502)

	public String getSupplierId()
	{
		return supplierId;
	}

	public void setSupplierId(String supplierId)
	{
		this.supplierId = supplierId;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public boolean validate(IValidationContext context)
	{
		if (responseCode != Protocol.RESPONSECODE_SUCCESS)
			return Protocol.validateResponseCode(context, true, responseCode);

		return Protocol.validateActivationCode(context, false, activationCode) && //
				Protocol.validateAgent(context, false, agent) && //
				Protocol.validateBatchId(context, true, batchId) && //
				Protocol.validateCurrency(context, true, currency) && //
				Protocol.validateExpiryDate(context, true, expiryDate) && //
				Protocol.validateExtensionText1(context, false, extensionText1) && //
				Protocol.validateExtensionText2(context, false, extensionText2) && //
				Protocol.validateExtensionText3(context, false, extensionText3) && //
				Protocol.validateOperatorId(context, false, operatorId) && //
				Protocol.validateResponseCode(context, true, responseCode) && //
				Protocol.validateSubscriberId(context, false, subscriberId) && //
				Protocol.validateTimestamp(context, false, timestamp) && //
				Protocol.validateState(context, true, state) && //
				Protocol.validateValue(context, true, value) && //
				Protocol.validateVoucherGroup(context, true, voucherGroup) && //
				Protocol.validateSerialNumber(context, false, serialNumber) && //
				Protocol.validateVoucherExpired(context, false, voucherExpired) && //
				Protocol.validateSupplierId(context, false, supplierId);

	}

}
