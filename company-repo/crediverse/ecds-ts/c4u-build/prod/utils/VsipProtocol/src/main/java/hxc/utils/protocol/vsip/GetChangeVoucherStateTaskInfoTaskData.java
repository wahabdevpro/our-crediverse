package hxc.utils.protocol.vsip;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class GetChangeVoucherStateTaskInfoTaskData
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String batchId;
	private String serialNumber;
	private String serialNumberFirst;
	private String serialNumberLast;
	private String activationCode;
	@XmlElement(required = true)
	private String newState;
	private String oldState;
	private Integer reportFormat;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The batchId parameter indicates what batch a voucher belongs to. The
	// batchId is assigned when vouchers are generated.
	//
	// Optional

	public String getBatchId()
	{
		return batchId;
	}

	public void setBatchId(String batchId)
	{
		this.batchId = batchId;
	}

	// The serialNumber parameter is used to state the unique voucher serial
	// number that is used to identify the voucher. Leading zeros are allowed. The
	// element size defined below defines the limit at protocol level, and may be
	// further restricted at application level by the server side.
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

	// The serialNumberFirst parameter is used to state the first voucher in the
	// serial number range to be checked. Leading zeros are allowed. The element
	// size defined bellow defines the limit at protocol level, and may be further
	// restricted at application level by the server side.
	//
	// Optional

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
	// Optional

	public String getSerialNumberLast()
	{
		return serialNumberLast;
	}

	public void setSerialNumberLast(String serialNumberLast)
	{
		this.serialNumberLast = serialNumberLast;
	}

	// The activationCode parameter is the unique secret code which is used to
	// refill the account. The activation code may have leading zeros. The element
	// size defined below defines the limit at protocol level, and may be further
	// restricted at application level by the server side.
	//
	// Optional

	public String getActivationCode()
	{
		return activationCode;
	}

	public void setActivationCode(String activationCode)
	{
		this.activationCode = activationCode;
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

	// The oldState parameter is used to represent the state of the voucher as it is,
	// or was, prior to a specific event. Such an event could be a request to change a
	// vouchers state from the specified state, or it could be a historical record that
	// represents a state change in the past.
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////

	public boolean validate(IValidationContext context)
	{
		return Protocol.validateBatchId(context, false, batchId) && //
				Protocol.validateSerialNumber(context, false, serialNumber) && //
				Protocol.validateSerialNumberFirst(context, false, serialNumberFirst) && //
				Protocol.validateSerialNumberLast(context, false, serialNumberLast) && //
				Protocol.validateActivationCode(context, false, activationCode) && //
				Protocol.validateNewState(context, true, newState) && //
				Protocol.validateOldState(context, false, oldState) && //
				Protocol.validateReportFormat(context, false, reportFormat);
	}

}
