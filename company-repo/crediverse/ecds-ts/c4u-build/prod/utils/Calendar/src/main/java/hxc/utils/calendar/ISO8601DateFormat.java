package hxc.utils.calendar;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ISO8601DateFormat extends DateFormat
{

	private static final long serialVersionUID = 674851455996030315L;
	private final int dateFormatWithoutTimezoneLength;

	private SimpleDateFormat simpleDateFormatterWithTimezone, simpleDateFormatterWithoutTimezone;

	public ISO8601DateFormat()
	{
		this.dateFormatWithoutTimezoneLength = "yyyyMMddTHH:mm:ss".length();
		this.simpleDateFormatterWithTimezone = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ssZ");
		this.simpleDateFormatterWithoutTimezone = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss");
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition fieldPosition)
	{
		return simpleDateFormatterWithTimezone.format(date, toAppendTo, fieldPosition);
	}

	@Override
	public Date parse(String source, ParsePosition pos)
	{
		if (source.length() - pos.getIndex() == dateFormatWithoutTimezoneLength)
			return simpleDateFormatterWithoutTimezone.parse(source, pos);
		return simpleDateFormatterWithTimezone.parse(source, pos);
	}

}
