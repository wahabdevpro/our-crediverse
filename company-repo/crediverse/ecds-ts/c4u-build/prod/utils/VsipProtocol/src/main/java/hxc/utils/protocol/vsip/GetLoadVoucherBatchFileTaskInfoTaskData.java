package hxc.utils.protocol.vsip;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class GetLoadVoucherBatchFileTaskInfoTaskData
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
	private String initialVoucherState;

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
	// Optional

	public String getNewState()
	{
		return newState;
	}

	public void setNewState(String newState)
	{
		this.newState = newState;
	}

	// The initialVoucherState parameter is used to set which state the voucher
	// will be in when a voucher is loaded (Voucher Load). A voucher can be set
	// to the state available or unavailable.
	// When performing the GetLoadVoucherBatchFileTaskInfo operation the name
	// and which state the voucher is in will be presented. See Section 9.18.1 on
	// page 67.
	//
	// Mandatory

	public String getInitialVoucherState()
	{
		return initialVoucherState;
	}

	public void setInitialVoucherState(String initialVoucherState)
	{
		this.initialVoucherState = initialVoucherState;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	public boolean validate(IValidationContext context)
	{
		return Protocol.validateFilename(context, true, filename) && //
				Protocol.validateBatchId(context, true, batchId) && //
				Protocol.validateNewState(context, false, newState) && //
				Protocol.validateInitialVoucherState(context, true, initialVoucherState);
	}

}
