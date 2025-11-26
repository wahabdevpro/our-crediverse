package hxc.utils.protocol.vsip;

import java.util.Date;

// The GenerateVoucherUsageReport message is used to schedule a report of all
// vouchers that was used within a specified time frame.

public class GenerateVoucherUsageReportRequest implements IVsipRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Date fromTime;
	private Date toTime;
	private String networkOperatorId;
	private GenerateVoucherUsageReportSchedulation schedulation;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The fromTime parameter together with toTime parameter is used to create
	// a time frame, which can be used to define, for example a matching criteria.
	// The fromTime parameter is used to determine which vouchers was used in
	// a specific time frame.
	//
	// If fromTime is omitted all vouchers up to the toTime will be included in the report.
	//
	// Optional

	public Date getFromTime()
	{
		return fromTime;
	}

	public void setFromTime(Date fromTime)
	{
		this.fromTime = fromTime;
	}

	// The toTime parameter together with the fromTime parameter is used to
	// create an interval and within this time frame is the matching criteria. The
	// toTime parameter is used to determine which vouchers was used in a specific
	// time frame.
	//
	// If toTime is omitted all vouchers from the fromTime will be included in the report.
	//
	// Optional

	public Date getToTime()
	{
		return toTime;
	}

	public void setToTime(Date toTime)
	{
		this.toTime = toTime;
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

	public GenerateVoucherUsageReportSchedulation getSchedulation()
	{
		return schedulation;
	}

	public void setSchedulation(GenerateVoucherUsageReportSchedulation schedulation)
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
		return Protocol.validateFromTime(context, false, fromTime) && //
				Protocol.validateToTime(context, false, toTime) && //
				Protocol.validateNetworkOperatorId(context, networkOperatorId) && //
				(schedulation == null || schedulation.validate(context));
	}

}
