package hxc.utils.calendar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("serial")
public class DateRange implements Serializable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Enumerations
	//
	// /////////////////////////////////
	public enum Periods
	{

		Today, Past24Hours, Yesterday, DayBeforeYesterday,

		ThisWeek, PastWeek, LastWeek, WeekBeforeLast,

		ThisMonth, PastMonth, LastMonth, MonthBeforeLast,

		ThisQuarter, PastQuarter, LastQuarter, The1stQuarter, The2ndQuarter, The3rdQuarter, The4thQuarter,

		ThisYear, PastYear, LastYear, YearBeforeLast,

		ThisFinancialYear, LastFinancialYear, FinancialYearBeforeLast,

		Custom,
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Periods period;
	private String name;
	private Date startDate;
	private Date endDateExclusive;
	private static int weekStartsOnDay = DateTime.SUNDAY;
	private static int financialYearStartsOnMonth = 1;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Periods getPeriod()
	{
		return period;
	}

	public void setPeriod(Periods period)
	{
		this.period = period;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public Date getEndDateInclusive()
	{
		return new Date(endDateExclusive.getTime() - 1);
	}

	public Date getEndDateExclusive()
	{
		return endDateExclusive;
	}

	public void setEndDateExclusive(Date endDateExclusive)
	{
		this.endDateExclusive = endDateExclusive;
	}

	public static int getWeekStartsOnDay()
	{
		return weekStartsOnDay;
	}

	public static void setWeekStartsOnDay(int weekStartsOnDay)
	{
		DateRange.weekStartsOnDay = weekStartsOnDay;
	}

	public static int getFinancialYearStartsOnMonth()
	{
		return financialYearStartsOnMonth;
	}

	public static void setFinancialYearStartsOnMonth(int financialYearStartsOnMonth)
	{
		DateRange.financialYearStartsOnMonth = financialYearStartsOnMonth;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public DateRange()
	{
	}

	public DateRange(Periods period, String name, DateTime startDate, DateTime endDateExclusive)
	{
		this.period = period;
		this.name = name;
		this.startDate = startDate;
		this.endDateExclusive = endDateExclusive;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public static DateRange[] GetAllRanges()
	{
		return GetAllRanges(DateTime.getNow(), true);
	}

	public static DateRange[] GetAllRanges(DateTime now, boolean allowCustom)
	{
		List<DateRange> result = new ArrayList<DateRange>();
		DateTime dateNow = now.getDatePart();

		if (allowCustom)
		{
			// Custom
			result.add(new DateRange(Periods.Custom, "Please Specify", dateNow, dateNow.addDays(1)));
		}

		// Today
		result.add(new DateRange(Periods.Today, "Today", dateNow, dateNow.addDays(1)));

		// Past 24 Hours
		result.add(new DateRange(Periods.Past24Hours, "Past 24 hours", now.addHours(-24), now));

		// Yesterday
		result.add(new DateRange(Periods.Yesterday, "Yesterday", dateNow.addDays(-1), dateNow));

		// Day Before Yesterday
		result.add(new DateRange(Periods.DayBeforeYesterday, "Day Before Yesterday", dateNow.addDays(-2), dateNow.addDays(-1)));

		// This Week
		int daysAgoTheWeekStarted = now.getDayOfWeek() - weekStartsOnDay;
		if (daysAgoTheWeekStarted < 1)
			daysAgoTheWeekStarted += 7;
		DateTime weekStartedOn = dateNow.addDays(-daysAgoTheWeekStarted);
		result.add(new DateRange(Periods.ThisWeek, "This Week", weekStartedOn, dateNow.addDays(1)));

		// Past Week
		result.add(new DateRange(Periods.PastWeek, "Past Week", now.addDays(-7), now));

		// Last Week
		result.add(new DateRange(Periods.LastWeek, "Last Week", weekStartedOn.addDays(-7), weekStartedOn));

		// Week Before Last
		result.add(new DateRange(Periods.WeekBeforeLast, "Week Before Last", weekStartedOn.addDays(-14), weekStartedOn.addDays(-7)));

		// This Month
		DateTime thisMonthStartedOn = new DateTime(dateNow.getYear(), dateNow.getMonth(), 1);
		result.add(new DateRange(Periods.ThisMonth, "This Month", thisMonthStartedOn, dateNow.addDays(1)));

		// Past 30 Days
		int year = dateNow.getYear();
		int month = dateNow.getMonth() - 1;
		if (month < 1)
		{
			month = 12;
			year--;
		}
		int day = dateNow.getDay();
		while (day > DateTime.getMonthLength(year, month))
			day--;
		DateTime pastMonthStartedOn = (new DateTime(year, month, day)).add(now.getTimePart());
		result.add(new DateRange(Periods.PastMonth, "Past Month", pastMonthStartedOn, now));

		// Last Month
		year = dateNow.getYear();
		month = dateNow.getMonth() - 1;
		if (month < 1)
		{
			month = 12;
			year--;
		}
		DateTime lastMonthStartedOn = new DateTime(year, month, 1);
		result.add(new DateRange(Periods.LastMonth, "Last Month", lastMonthStartedOn, thisMonthStartedOn));

		// Month Before Last
		month--;
		if (month < 1)
		{
			month = 12;
			year--;
		}
		DateTime monthBeforeLastStartedOn = new DateTime(year, month, 1);
		result.add(new DateRange(Periods.MonthBeforeLast, "Month Before Last", monthBeforeLastStartedOn, lastMonthStartedOn));

		// This Quarter
		year = dateNow.getYear();
		month = ((dateNow.getMonth() - 1) / 3) * 3 + 1;
		DateTime thisQuarterStartedOn = new DateTime(year, month, 1);
		result.add(new DateRange(Periods.ThisQuarter, "This Quarter", thisQuarterStartedOn, dateNow.addDays(1)));

		// Past Quarter
		year = dateNow.getYear();
		month = dateNow.getMonth() - 3;
		if (month < 1)
		{
			year--;
			month += 12;
		}
		day = dateNow.getDay();
		while (day > DateTime.getMonthLength(year, month))
			day--;
		DateTime pastQuarterStartedOn = (new DateTime(year, month, day)).add(now.getTimePart());
		result.add(new DateRange(Periods.PastQuarter, "Past Quarter", pastQuarterStartedOn, now));

		// Last Quarter
		year = thisQuarterStartedOn.getYear();
		month = thisQuarterStartedOn.getMonth() - 3;
		if (month < 1)
		{
			year--;
			month += 12;
		}
		day = dateNow.getDay();
		while (day > DateTime.getMonthLength(year, month))
			day--;
		DateTime lastQuarterStartedOn = new DateTime(year, month, day);
		result.add(new DateRange(Periods.LastQuarter, "Last Quarter", lastQuarterStartedOn, thisQuarterStartedOn));

		// 1st Quarter
		addQuarter(result, Periods.The1stQuarter, "1st", dateNow);

		// 2nd Quarter
		addQuarter(result, Periods.The2ndQuarter, "2nd", dateNow);

		// 3rd Quarter
		addQuarter(result, Periods.The3rdQuarter, "3rd", dateNow);

		// 4th Quarter
		addQuarter(result, Periods.The4thQuarter, "4th", dateNow);

		// This Year
		DateTime thisYearStartedOn = new DateTime(dateNow.getYear(), 1, 1);
		result.add(new DateRange(Periods.ThisYear, "This Year", thisYearStartedOn, dateNow.addDays(1)));

		// Past Year
		year = dateNow.getYear() - 1;
		month = dateNow.getMonth();
		day = dateNow.getDay();
		while (day > DateTime.getMonthLength(year, month))
			day--;
		DateTime pastYearStartedOn = (new DateTime(year, month, day)).add(now.getTimePart());
		result.add(new DateRange(Periods.PastYear, "Past Year", pastYearStartedOn, now));

		// Last Year
		DateTime lastYearStartedOn = new DateTime(dateNow.getYear() - 1, 1, 1);
		result.add(new DateRange(Periods.LastYear, "Last Year", lastYearStartedOn, thisYearStartedOn));

		// Year Before Last
		DateTime yearBeforeLastStartedOn = new DateTime(dateNow.getYear() - 2, 1, 1);
		result.add(new DateRange(Periods.YearBeforeLast, "Year Before Last", yearBeforeLastStartedOn, lastYearStartedOn));

		if (DateRange.financialYearStartsOnMonth != 0)
		{
			// This Financial Year
			year = dateNow.getYear();
			month = DateRange.financialYearStartsOnMonth;
			if (month > dateNow.getMonth())
				year--;
			DateTime thisFinancialYearStartedOn = new DateTime(year, month, 1);
			result.add(new DateRange(Periods.ThisFinancialYear, "This Financial Year", thisFinancialYearStartedOn, dateNow.addDays(1)));

			// Last Financial Year
			year--;
			DateTime lastFinancialYearStartedOn = new DateTime(year, month, 1);
			result.add(new DateRange(Periods.LastFinancialYear, "Last Financial Year", lastFinancialYearStartedOn, thisFinancialYearStartedOn));

			// Financial Year Before Last
			year--;
			DateTime financialYearBeforeLastStartedOn = new DateTime(year, month, 1);
			result.add(new DateRange(Periods.FinancialYearBeforeLast, "Financial Year Before Last", financialYearBeforeLastStartedOn, lastFinancialYearStartedOn));
		}

		return result.toArray(new DateRange[result.size()]);
	}

	public static DateRange GetRange(Periods period)
	{
		DateRange[] allRanges = GetAllRanges();
		for (DateRange dr : allRanges)
		{
			if (dr.getPeriod() == period || (period == Periods.Custom && dr.getPeriod() == Periods.ThisMonth))
			{
				return dr;
			}
		}
		return null;
	}

	@Override
	public String toString()
	{
		return name;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////
	private static void addQuarter(List<DateRange> result, Periods period, String quarterName, DateTime dateNow)
	{
		DateTime quarterStartsOn = new DateTime(dateNow.getYear(), (period.ordinal() - Periods.The1stQuarter.ordinal()) * 3 + 1, 1);
		if (quarterStartsOn.after(dateNow))
			return;
		int year = quarterStartsOn.getYear();
		int month = quarterStartsOn.getMonth() + 3;
		if (month > 12)
		{
			month = 1;
			year++;
		}
		DateTime quarterEndsOn = new DateTime(year, month, 1);
		quarterName += String.format(" Quarter %d", year);
		result.add(new DateRange(period, quarterName, quarterStartsOn, quarterEndsOn));
	}

}
