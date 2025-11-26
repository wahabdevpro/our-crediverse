package hxc.utils.protocol.vsip;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import hxc.utils.xmlrpc.XmlRpcAsString;

// The message GenerateVoucher is used to schedule a generate voucher task.
// The GenerateVoucher message will be added to the VS Task Manager for
// immediate or scheduled execution.

@XmlAccessorType(XmlAccessType.FIELD)
public class GenerateVoucherRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@XmlElement(required = true)
	private int numberOfVouchers;
	@XmlElement(required = true)
	private int activationCodeLength;
	@XmlElement(required = true)
	private String currency;
	@XmlElement(required = true)
	private String serialNumber;
	@XmlElement(required = true)
	@XmlRpcAsString
	private long value;
	@XmlElement(required = true)
	private String voucherGroup;
	@XmlElement(required = true)
	private Date expiryDate;
	private String agent;
	private String extensionText1;
	private String extensionText2;
	private String extensionText3;
	private GenerateVoucherSchedulation schedulation;
	private String networkOperatorId;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The numberOfVouchers parameter is used to define the number of vouchers
	// in a batch or a serial number range.
	//
	// A maximum of 1000000 vouchers may be generated at a time.
	//
	// Mandatory

	public int getNumberOfVouchers()
	{
		return numberOfVouchers;
	}

	public void setNumberOfVouchers(int numberOfVouchers)
	{
		this.numberOfVouchers = numberOfVouchers;
	}

	// This flag can be used on reservation to automatically commit the voucher or
	// to request automatic rollback of the voucher in case of no commit within a
	// specified time.
	//
	// Mandatory

	public int getActivationCodeLength()
	{
		return activationCodeLength;
	}

	public void setActivationCodeLength(int activationCodeLength)
	{
		this.activationCodeLength = activationCodeLength;
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

	// The expiryDate parameter is used to identify the last date when the voucher
	// will be usable in the system. Only the date information will be considered by
	// this parameter. The time and timezone should be set to all zeroes, and will
	// be ignored.
	// TZ is the deviation in hours from UTC. This field is optional. This date format
	// does not strictly follow the XML-RPC specification on date format. It does
	// however follow the ISO 8601 specification. Parsers for this protocol must be
	// prepared to parse dates containing timezone.
	//
	// Not allowed when PC:10505 is active. The date will instead be generated from pre-configured
	// values.
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

	// The schedulation record is a <struct> of its own.
	//
	// Optional

	public GenerateVoucherSchedulation getSchedulation()
	{
		return schedulation;
	}

	public void setSchedulation(GenerateVoucherSchedulation schedulation)
	{
		this.schedulation = schedulation;
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
		return Protocol.validateNumberOfVouchers(context, true, numberOfVouchers) && //
				Protocol.validateActivationCodeLength(context, true, activationCodeLength) && //
				Protocol.validateCurrency(context, true, currency) && //
				Protocol.validateSerialNumber(context, true, serialNumber) && //
				Protocol.validateValue(context, true, value) && //
				Protocol.validateVoucherGroup(context, true, voucherGroup) && //
				Protocol.validateExpiryDate(context, true, expiryDate) && //
				Protocol.validateAgent(context, false, agent) && //
				Protocol.validateExtensionText1(context, false, extensionText1) && //
				Protocol.validateExtensionText2(context, false, extensionText2) && //
				Protocol.validateExtensionText3(context, false, extensionText3) && //
				(schedulation == null || schedulation.validate(context)) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId);
	}

}
