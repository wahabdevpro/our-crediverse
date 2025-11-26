package hxc.utils.protocol.vsip;

import hxc.utils.xmlrpc.XmlRpcAsString;

// This message is used to reserve a voucher. The message represents the start
// of a refill transaction.
// The additionalAction parameter can be used to request different reservation
// mechanisms, and it controls the need of issuing specific commit or rollback
// messages.

public class ReserveVoucherResponse implements IVsipResponse
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String agent;
	private String currency;
	private String extensionText1;
	private String extensionText2;
	private String extensionText3;
	private int responseCode;
	private String serialNumber;
	private String voucherGroup;
	@XmlRpcAsString
	private long value;
	private String supplierId;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

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

	// The serialNumber parameter is used to state the unique voucher serial
	// number that is used to identify the voucher. Leading zeros are allowed. The
	// element size defined below defines the limit at protocol level, and may be
	// further restricted at application level by the server side.
	//
	// Mandatory

	public String getSerialNumber()
	{
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber)
	{
		this.serialNumber = serialNumber;
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

		return Protocol.validateAgent(context, false, agent) && //
				Protocol.validateCurrency(context, true, currency) && //
				Protocol.validateExtensionText1(context, false, extensionText1) && //
				Protocol.validateExtensionText2(context, false, extensionText2) && //
				Protocol.validateExtensionText3(context, false, extensionText3) && //
				Protocol.validateResponseCode(context, true, responseCode) && //
				Protocol.validateSerialNumber(context, true, serialNumber) && //
				Protocol.validateVoucherGroup(context, true, voucherGroup) && //
				Protocol.validateValue(context, true, value) && //
				Protocol.validateSupplierId(context, false, supplierId);
	}

}
