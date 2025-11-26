package hxc.utils.calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class DateTimeTest
{
	@Test
	public void testDateTime()
	{
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'000000");
		long MILLIS_PER_DAY = 24L * 60 * 60 * 1000;
		long TIME_ZONE_MILLIS = 2L * 60 * 60 * 1000;

		// Test Now & Today
		while (true)
		{
			Date time1 = new Date();
			DateTime now = DateTime.getNow();
			DateTime today = DateTime.getToday();
			Date time2 = new Date();

			if (time1.getTime() == time2.getTime())
			{
				assertEquals(dateTimeFormat.format(time1), now.toString());
				assertEquals(dateFormat.format(time1), today.toString());
				assertEquals((today.toLong() + TIME_ZONE_MILLIS) % MILLIS_PER_DAY, 0L);
				break;
			}
		}

		// Test Date/Time Parts
		{
			DateTime date = new DateTime(2014, 12, 25, 10, 11, 12, 113);
			DateTime timePart = date.getTimePart();
			assertEquals(0L, timePart.toLong() / MILLIS_PER_DAY);
			assertEquals("19700101T101112", timePart.toString());
			DateTime datePart = date.getDatePart();
			assertEquals(0L, (datePart.toLong() + TIME_ZONE_MILLIS) % MILLIS_PER_DAY);
			assertEquals("20141225T000000", datePart.toString());
		}

		// Offset
		{
			DateTime leapDay = new DateTime(2012, 2, 29, 10, 11, 12, 0);
			DateTime twoYearsBack = leapDay.addYears(-2);
			assertEquals("20100228T101112", twoYearsBack.toString());
		}

	}

	@Test
	public void testDateRange()
	{
		DateTime now = new DateTime(2014, 10, 23, 9, 54, 30);
		DateRange.setFinancialYearStartsOnMonth(3);
		DateRange[] ranges = DateRange.GetAllRanges(now, false);
		assertEquals(26, ranges.length);

		for (DateRange range : ranges)
		{
			switch (range.getPeriod())
			{
				case Today:
					assertEquals("Today", range.getName());
					assertEquals(new DateTime(2014, 10, 23, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 24, 0, 0, 0), range.getEndDateExclusive());
					break;

				case Past24Hours:
					assertEquals("Past 24 hours", range.getName());
					assertEquals(new DateTime(2014, 10, 22, 9, 54, 30), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 23, 9, 54, 30), range.getEndDateExclusive());
					break;

				case Yesterday:
					assertEquals("Yesterday", range.getName());
					assertEquals(new DateTime(2014, 10, 22, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 23, 0, 0, 0), range.getEndDateExclusive());
					break;

				case DayBeforeYesterday:
					assertEquals("Day Before Yesterday", range.getName());
					assertEquals(new DateTime(2014, 10, 21, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 22, 0, 0, 0), range.getEndDateExclusive());
					break;

				case ThisWeek:
					assertEquals("This Week", range.getName());
					assertEquals(new DateTime(2014, 10, 19, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 24, 0, 0, 0), range.getEndDateExclusive());
					break;

				case PastWeek:
					assertEquals("Past Week", range.getName());
					assertEquals(new DateTime(2014, 10, 16, 9, 54, 30), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 23, 9, 54, 30), range.getEndDateExclusive());
					break;

				case LastWeek:
					assertEquals("Last Week", range.getName());
					assertEquals(new DateTime(2014, 10, 12, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 19, 0, 0, 0), range.getEndDateExclusive());
					break;

				case WeekBeforeLast:
					assertEquals("Week Before Last", range.getName());
					assertEquals(new DateTime(2014, 10, 5, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 12, 0, 0, 0), range.getEndDateExclusive());
					break;

				case ThisMonth:
					assertEquals("This Month", range.getName());
					assertEquals(new DateTime(2014, 10, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 24, 0, 0, 0), range.getEndDateExclusive());
					break;

				case PastMonth:
					assertEquals("Past Month", range.getName());
					assertEquals(new DateTime(2014, 9, 23, 9, 54, 30), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 23, 9, 54, 30), range.getEndDateExclusive());
					break;

				case LastMonth:
					assertEquals("Last Month", range.getName());
					assertEquals(new DateTime(2014, 9, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				case MonthBeforeLast:
					assertEquals("Month Before Last", range.getName());
					assertEquals(new DateTime(2014, 8, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 9, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				case ThisQuarter:
					assertEquals("This Quarter", range.getName());
					assertEquals(new DateTime(2014, 10, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 24, 0, 0, 0), range.getEndDateExclusive());
					break;

				case PastQuarter:
					assertEquals("Past Quarter", range.getName());
					assertEquals(new DateTime(2014, 7, 23, 9, 54, 30), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 23, 9, 54, 30), range.getEndDateExclusive());
					break;

				case LastQuarter:
					assertEquals("Last Quarter", range.getName());
					assertEquals(new DateTime(2014, 7, 23, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				case The1stQuarter:
					assertEquals("1st Quarter 2014", range.getName());
					assertEquals(new DateTime(2014, 1, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 4, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				case The2ndQuarter:
					assertEquals("2nd Quarter 2014", range.getName());
					assertEquals(new DateTime(2014, 4, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 7, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				case The3rdQuarter:
					assertEquals("3rd Quarter 2014", range.getName());
					assertEquals(new DateTime(2014, 7, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				case The4thQuarter:
					assertEquals("4th Quarter 2015", range.getName());
					assertEquals(new DateTime(2014, 10, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2015, 1, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				case ThisYear:
					assertEquals("This Year", range.getName());
					assertEquals(new DateTime(2014, 1, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 24, 0, 0, 0), range.getEndDateExclusive());
					break;

				case PastYear:
					assertEquals("Past Year", range.getName());
					assertEquals(new DateTime(2013, 10, 23, 9, 54, 30), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 23, 9, 54, 30), range.getEndDateExclusive());
					break;

				case LastYear:
					assertEquals("Last Year", range.getName());
					assertEquals(new DateTime(2013, 1, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 1, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				case YearBeforeLast:
					assertEquals("Year Before Last", range.getName());
					assertEquals(new DateTime(2012, 1, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2013, 1, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				case ThisFinancialYear:
					assertEquals("This Financial Year", range.getName());
					assertEquals(new DateTime(2014, 3, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 10, 24, 0, 0, 0), range.getEndDateExclusive());
					break;

				case LastFinancialYear:
					assertEquals("Last Financial Year", range.getName());
					assertEquals(new DateTime(2013, 3, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2014, 3, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				case FinancialYearBeforeLast:
					assertEquals("Financial Year Before Last", range.getName());
					assertEquals(new DateTime(2012, 3, 1, 0, 0, 0), range.getStartDate());
					assertEquals(new DateTime(2013, 3, 1, 0, 0, 0), range.getEndDateExclusive());
					break;

				default:
					assertTrue("Unknown Period : " + range.getPeriod(), false);
			}

		}

	}
}
