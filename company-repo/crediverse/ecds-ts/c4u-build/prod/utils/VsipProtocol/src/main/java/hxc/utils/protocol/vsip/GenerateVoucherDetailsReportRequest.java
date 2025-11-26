package hxc.utils.protocol.vsip;

// The GenerateVoucherDetailsReport message is used to schedule a report
// of all vouchers in a specified batch.
// For information of the report file, see Protocol Message Specification Voucher
// Details Report File, Reference [3].

public class GenerateVoucherDetailsReportRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String batchId;
	private String networkOperatorId;
	private GenerateVoucherDetailsReportSchedulation schedulation;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

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

	// The schedulation record is a <struct> of its own
	//
	// Optional

	public GenerateVoucherDetailsReportSchedulation getSchedulation()
	{
		return schedulation;
	}

	public void setSchedulation(GenerateVoucherDetailsReportSchedulation schedulation)
	{
		this.schedulation = schedulation;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public boolean validate(IValidationContext context)
	{
		return Protocol.validateBatchId(context, true, batchId) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId) && //
				(schedulation == null || schedulation.validate(context));
	}

}
