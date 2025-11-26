package hxc.ecds.protocol.rest.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class TimeInterval
{
	Date startDate;
	Date endDate;

	public Date getStartDate()
	{
		return this.startDate;
	}

	public TimeInterval setStartDate(Date startDate)
	{
		//this.validate(startDate, this.endDate);
		this.startDate = startDate;
		return this;
	}

	public Date getEndDate()
	{
		return this.endDate;
	}

	public TimeInterval setEndDate(Date endDate)
	{
		//this.validate(this.startDate, endDate);
		this.endDate = endDate;
		return this;
	}

	public TimeInterval setDates(Date startDate, Date endDate)
	{
		//this.validate(startDate, endDate);
		this.startDate = startDate;
		this.endDate = endDate;
		return this;
	}

	public TimeInterval(Date startDate, Date endDate)
	{
		//this.validate(startDate, endDate);
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public TimeInterval(Calendar startCalendar, Calendar endCalendar)
	{
		Date startDate = startCalendar.getTime();
		Date endDate = endCalendar.getTime();
		//this.validate(startDate, endDate);
		this.startDate = startDate;
		this.endDate = endDate;
	}

	public TimeInterval()
	{
	}

	public TimeInterval(TimeInterval other)
	{
		if (other != null) this.setDates(other.startDate, other.endDate);
	}

	public String describe(String extra)
	{
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z");
		return String.format("%s@%s(startDate = '%s', endDate = '%s'%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			(startDate != null ? dateFormat.format(startDate) : null), (endDate != null ? dateFormat.format(endDate) : null),
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public boolean includes(Date date, boolean closed )
	{
		Objects.requireNonNull(date, "date may not be null ...");
		if ( this.endDate != null )
		{
			if ( closed && this.endDate.compareTo(date) < 0 ) return false; // endDate < date
			if ( !closed && this.endDate.compareTo(date) <= 0 ) return false;
		}
		// XXX TODO FIXME ...
		return true;
	}

	public String describe()
	{
		return this.describe("");
	}

	/*
	Unused ?

	private static void validate( Date startDate, Date endDate )
	{
		if ( startDate == null || endDate == null ) return; // If only one is set it is always valid ...
		if ( startDate.compareTo(endDate) > 0 ) throw new IllegalArgumentException(String.format("startDate(%s) may not be larger than endDate(%s)", startDate, endDate));
	}

	private void validate()
	{
		validate( this.startDate, this.endDate );
	}
	*/

	@Override
	public String toString()
	{
		return this.describe();
	}

    @Override
    public int hashCode()
    {
        return Objects.hash(startDate, endDate);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
            return false;
        if (this == other)
            return true;
        if (!(other instanceof TimeInterval))
            return false;
        TimeInterval otherTyped = (TimeInterval) other;
        return ( true
			&& Objects.equals(this.startDate, otherTyped.startDate)
			&& Objects.equals(this.endDate, otherTyped.endDate)
		);
    }
}
