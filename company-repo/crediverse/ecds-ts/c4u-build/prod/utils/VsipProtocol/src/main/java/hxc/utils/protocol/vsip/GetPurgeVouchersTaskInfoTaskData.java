package hxc.utils.protocol.vsip;

import java.util.Date;

public class GetPurgeVouchersTaskInfoTaskData
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Date expiryDate;
	private Integer offset;
	private String state;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	// The expiryDate parameter is used to identify the last date when the voucher
	// will be usable in the system. Only the date information will be considered by
	// this parameter. The time and timezone should be set to all zeroes, and will
	// be ignored.
	// TZ is the deviation in hours from UTC. This field is optional. This date format
	// does not strictly follow the XML-RPC specification on date format. It does
	// however follow the ISO 8601 specification. Parsers for this protocol must be
	// prepared to parse dates containing timezone.
	//
	// Optional

	public Date getExpiryDate()
	{
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate)
	{
		this.expiryDate = expiryDate;
	}

	// The offset parameter is used to indicate a date in the past, by specifying the
	// offset, in days, from the current date. If this parameter is used in a scheduled
	// request the offset will based on the time of execution rather than the current
	// date.
	//
	// Optional

	public Integer getOffset()
	{
		return offset;
	}

	public void setOffset(Integer offset)
	{
		this.offset = offset;
	}

	// The state parameter is used to represent the state of a voucher, as it currently
	// is.
	//
	// Mandatory

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////

	public boolean validate(IValidationContext context)
	{
		return Protocol.validateExpiryDate(context, false, expiryDate) && //
				Protocol.validateOffset(context, false, offset) && //
				Protocol.validateState(context, true, state);
	}

}
