package hxc.utils.protocol.vsip;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

// The message LoadVoucherBatchFile is used to schedule the loading of a
// batch file. This message will be added to the task manager for immediate or
// scheduled execution.

@XmlAccessorType(XmlAccessType.FIELD)
public class LoadVoucherBatchFileRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	@XmlElement(required = true)
	private String filename;
	@XmlElement(required = true)
	private String batchId;
	private String newState;
	private LoadVoucherBatchFileSchedulation schedulation;
	private String networkOperatorId;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The filename parameter is the filename generated as output from the specific
	// operation. Note that the filename is not a complete filename. The full path of
	// the file is not included and the suffix of the file may be excluded (for report
	// files for example).
	//
	// Mandatory

	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
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

	// The newState parameter is used to represent the state of the voucher as it is,
	// or will be, after a specific event. Such an event could be a request to change
	// a voucher to the new state, or it could be a historical record that represents a
	// state change in the past.
	//
	// Valid states are available and unavailable.
	// If newState is omitted, the default value configured in VS will be used.
	//
	// Optional

	public String getNewState()
	{
		return newState;
	}

	public void setNewState(String newState)
	{
		this.newState = newState;
	}

	// The schedulation record is a <struct> of its own.
	//
	// Optional

	public LoadVoucherBatchFileSchedulation getSchedulation()
	{
		return schedulation;
	}

	public void setSchedulation(LoadVoucherBatchFileSchedulation schedulation)
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
		return Protocol.validateFilename(context, true, filename) && //
				Protocol.validateBatchId(context, true, batchId) && //
				Protocol.validateNewState(context, false, newState) && //
				(schedulation == null || schedulation.validate(context)) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId);
	}

}
