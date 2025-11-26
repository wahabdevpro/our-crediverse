package hxc.utils.protocol.vsip;

// The GenerateVoucherDistributionReport message is used to create a voucher
// distribution report either for a batch or for all vouchers in the database.
// For information about the report file, see PMS Voucher Distribution Report
// File, Reference [4].

public class GenerateVoucherDistributionReportRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String batchId;
	private String networkOperatorId;
	private GenerateVoucherDistributionReportSchedulation schedulation;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The batchId parameter indicates what batch a voucher belongs to. The
	// batchId is assigned when vouchers are generated.
	//
	// If batchId is omitted all vouchers in the database will be included in the report.
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

	public GenerateVoucherDistributionReportSchedulation getSchedulation()
	{
		return schedulation;
	}

	public void setSchedulation(GenerateVoucherDistributionReportSchedulation schedulation)
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
		return Protocol.validateBatchId(context, false, batchId) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId) && //
				(schedulation == null || schedulation.validate(context));
	}

}
