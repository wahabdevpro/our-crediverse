package hxc.utils.protocol.vsip;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

// The message ChangeVoucherState message is used to schedule a task to
// change the state of vouchers.
// Caution!
// When using alphanumeric serial numbers (PC), range based operations are not
// recommended since it is likely to affect more vouchers than intended.

@XmlAccessorType(XmlAccessType.FIELD)
public class ChangeVoucherStateRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String batchId;
	private String activationCode;
	private String serialNumber;
	private String serialNumberFirst;
	private String serialNumberLast;
	@XmlElement(required = true)
	private String newState;
	private String oldState;
	private Integer reportFormat;
	private ChangeVoucherStateSchedulation schedulation;
	private String networkOperatorId;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The batchId parameter indicates what batch a voucher belongs to. The
	// batchId is assigned when vouchers are generated.
	//
	// One of the elements batchId, activationCode, serialNumber or a combination of
	// serialNumberFirst and serialNumberLast must be present. In case two or more of the above
	// elements are present (except for the combination of serialNumberFirst and serialNumberLast),
	// an error will be returned.
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

	// The activationCode parameter is the unique secret code which is used to
	// refill the account. The activation code may have leading zeros. The element
	// size defined below defines the limit at protocol level, and may be further
	// restricted at application level by the server side.
	//
	// One of the elements batchId, activationCode, serialNumber or a combination of
	// serialNumberFirst and serialNumberLast must be present. In case two or more of the above
	// elements are present (except for the combination of serialNumberFirst and serialNumberLast),
	// an error will be returned.
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

	// The serialNumber parameter is used to state the unique voucher serial
	// number that is used to identify the voucher. Leading zeros are allowed. The
	// element size defined below defines the limit at protocol level, and may be
	// further restricted at application level by the server side.
	//
	// One of the elements batchId, activationCode, serialNumber or a combination of
	// serialNumberFirst and serialNumberLast must be present. In case two or more of the above
	// elements are present (except for the combination of serialNumberFirst and serialNumberLast),
	// an error will be returned.
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

	// The serialNumberFirst parameter is used to state the first voucher in the
	// serial number range to be checked. Leading zeros are allowed. The element
	// size defined bellow defines the limit at protocol level, and may be further
	// restricted at application level by the server side.
	//
	// One of the elements batchId, activationCode, serialNumber or a combination of
	// serialNumberFirst and serialNumberLast must be present. In case two or more of the above
	// elements are present (except for the combination of serialNumberFirst and serialNumberLast),
	// an error will be returned.
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
	// One of the elements batchId, activationCode, serialNumber or a combination of
	// serialNumberFirst and serialNumberLast must be present. In case two or more of the above
	// elements are present (except for the combination of serialNumberFirst and serialNumberLast),
	// an error will be returned.
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

	// The newState parameter is used to represent the state of the voucher as it is,
	// or will be, after a specific event. Such an event could be a request to change
	// a voucher to the new state, or it could be a historical record that represents a
	// state change in the past.
	//
	// The following state changes are valid: see Table 34
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

	// The oldState parameter is used to represent the state of the voucher as it is,
	// or was, prior to a specific event. Such an event could be a request to change a
	// vouchers state from the specified state, or it could be a historical record that
	// represents a state change in the past.
	//
	// The following state changes are valid: see Table 34
	// If oldState is omitted, the state change will be applied to all vouchers, within the specified
	// range, where the operation is supported.
	//
	// Optional

	public String getOldState()
	{
		return oldState;
	}

	public void setOldState(String oldState)
	{
		this.oldState = oldState;
	}

	// The reportFormat parameter is used to determine what output format the
	// result report file will have.
	//
	// Optional

	public Integer getReportFormat()
	{
		return reportFormat;
	}

	public void setReportFormat(Integer reportFormat)
	{
		this.reportFormat = reportFormat;
	}

	// The scheduled record is a <struct> of its own, see Table 33.
	//
	// Optional

	public ChangeVoucherStateSchedulation getSchedulation()
	{
		return schedulation;
	}

	public void setSchedulation(ChangeVoucherStateSchedulation schedulation)
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
		boolean hasBatchId = batchId != null && batchId.length() > 0;
		boolean hasActivationCode = activationCode != null && activationCode.length() > 0;
		boolean hasSerialNumber = serialNumber != null && serialNumber.length() > 0;
		boolean hasSerialNumberFirst = serialNumberFirst != null && serialNumberFirst.length() > 0;
		boolean hasSerialNumberLast = serialNumberLast != null && serialNumberLast.length() > 0;

		return Protocol.validateBatchId(context, false, batchId) && //
				Protocol.validateActivationCode(context, false, activationCode) && //
				Protocol.validateSerialNumber(context, false, serialNumber) && //
				Protocol.validateSerialNumberFirst(context, false, serialNumberFirst) && //
				Protocol.validateSerialNumberLast(context, false, serialNumberLast) && //
				Protocol.validateNewState(context, true, newState) && //
				Protocol.validateOldState(context, false, oldState) && //
				Protocol.validateReportFormat(context, false, reportFormat) && //
				(schedulation == null || schedulation.validate(context)) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId) && //
				(hasBatchId ^ hasActivationCode) ^ (hasSerialNumber ^ (hasSerialNumberFirst && hasSerialNumberLast));
	}

}
