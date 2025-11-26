package hxc.utils.protocol.vsip;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

// The message LoadVoucherCheck is used to check if the vouchers in a serial
// number range are loaded into the database. The number of voucher found
// within the range are returned. If any voucher is missing, this is indicated
// in the response code, which is 10 (Voucher Does not exist), in that case.
// The requested serial number range is pointed out by serialNumberFirst and
// serialNumberLast.
// Note: When using alphanumeric serial numbers (PC) the numberOfVouchers
// parameter will not always be correct. The use of message
// LoadVoucherCheck is not recommended for alphanumeric serial
// numbers (PC).

@XmlAccessorType(XmlAccessType.FIELD)
public class LoadVoucherCheckRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@XmlElement(required = true)
	private String serialNumberFirst;
	@XmlElement(required = true)
	private String serialNumberLast;
	private String networkOperatorId;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The serialNumberFirst parameter is used to state the first voucher in the
	// serial number range to be checked. Leading zeros are allowed. The element
	// size defined bellow defines the limit at protocol level, and may be further
	// restricted at application level by the server side.
	//
	// serialNumberFirst and serialNumberLast must contain serial numbers of equal length.
	//
	// Mandatory

	public String getSerialNumberFirst()
	{
		return serialNumberFirst;
	}

	public void setSerialNumberFirst(String serialNumberFirst)
	{
		this.serialNumberFirst = serialNumberFirst;
	}

	// The serialNumberLast parameter states the last voucher in the serial
	// number range to be checked. Leading zeros are allowed. The element size
	// defined below defines the limit at protocol level, and may be further restricted at
	// application level by the server side.
	//
	// serialNumberFirst and serialNumberLast must contain serial numbers of equal length.
	//
	// Mandatory

	public String getSerialNumberLast()
	{
		return serialNumberLast;
	}

	public void setSerialNumberLast(String serialNumberLast)
	{
		this.serialNumberLast = serialNumberLast;
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
		return Protocol.validateSerialNumberFirst(context, true, serialNumberFirst) && //
				Protocol.validateSerialNumberLast(context, true, serialNumberLast) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId);
	}

}
