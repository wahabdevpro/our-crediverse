package cs.utility;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import cs.dto.error.GuiValidationException;
import cs.dto.error.GuiViolation.ViolationType;
import hxc.ecds.protocol.rest.Violation;

public class TimeUtility
{
	public static void validateHourMinuteTime(String HHmm, String property) throws GuiValidationException
	{
		String[] split = HHmm.split(":");
		List<Violation> violations = new ArrayList<>();

		if (split.length != 2 )
		{
			violations.add(new Violation(ViolationType.invalidTimeHMFormat.toString(), property, null, "Invalid time format (Must be in HH:MM format)"));
		}
		else
		{
			try
			{
				int hours = Integer.valueOf(split[0]);
				int minutes = Integer.valueOf(split[1]);

				if (hours < 0 || hours > 23 )
				{
					violations.add(new Violation(ViolationType.invalidTimeHour.toString(), property, null, "Invalid hour, must be between 00 and 23"));
				}
				if (minutes < 0 || minutes > 59 )
				{
					violations.add(new Violation(ViolationType.invalidTimeMinute.toString(), property, null, "Invalid minute, must be between 00 and 59"));
				}
			}
			catch(NumberFormatException exception)
			{
				violations.add(new Violation(ViolationType.invalidTimeHMFormat.toString(), property, null, "Invalid time format (Must be in HH:MM format)"));
			}
		}

		if (violations.size() > 0)
		{
			throw new GuiValidationException(violations);
		}
	}

	public static void validateHourMinuteSecondTime(String HHmmss,String property) throws GuiValidationException
	{
		String[] split = HHmmss.split(":");
		List<Violation> violations = new ArrayList<>();

		if (split.length != 3 )
		{
			violations.add(new Violation(ViolationType.invalidTimeFormat.toString(), property, null, "Invalid time format (Must be in HH:MM:SS format)"));
		}
		else
		{
			try
			{
				int hours = Integer.valueOf(split[0]);
				int minutes = Integer.valueOf(split[1]);
				int seconds = Integer.valueOf(split[2]);

				if (hours < 0 || hours > 23 )
				{
					violations.add(new Violation(ViolationType.invalidTimeHour.toString(), property, null, "Invalid hour, must be between 00 and 23"));
				}
				if (minutes < 0 || minutes > 59 )
				{
					violations.add(new Violation(ViolationType.invalidTimeMinute.toString(), property, null, "Invalid minute, must be between 00 and 59"));
				}
				if (seconds < 0 || seconds > 59 )
				{
					violations.add(new Violation(ViolationType.invalidTimeSecond.toString(), property, null, "Invalid second, must be between 00 and 59"));
				}
			}
			catch(NumberFormatException exception)
			{
				violations.add(new Violation(ViolationType.invalidTimeHMFormat.toString(), property, null, "Invalid time format (Must be in HH:MM:SS format)"));
			}
		}

		if (violations.size() > 0)
		{
			throw new GuiValidationException(violations);
		}
	}

	public static Date convertHourMinuteSecondStringToDate(String HHmmss, String property) throws GuiValidationException
	{
		String format = "HH:mm:ss";
		return TimeUtility.convertStringToDate(format, HHmmss, property);
	}

	public static Date convertStringToDate(
			String sdfFormatting,		// Contains string of characters accepted by SDF, examples are "HH:mm:ss" or "HH:mm"
			String dateString,			// Contains a literal time but in "String.class" format, examples are "15:27:00" or "10:20" (MUST MATCH SDF FORMAT ABOVE!!)
			String property) throws GuiValidationException		// property is a string that is sent to the Exception handler, defined by the developer
	{
		SimpleDateFormat sdf = new SimpleDateFormat(sdfFormatting);
		Date result = null;

		try
		{
			result = sdf.parse(dateString);
		}
		catch(Exception e)
		{
			throw new GuiValidationException(Arrays.asList(new Violation[] {new Violation(ViolationType.invalidTimeFormat.toString(), property, null, "Invalid Time Format")}));
		}

		return result;
	}

	public static LocalTime convertStringToLocalTime(
			String sdfFormatting,		// Contains string of characters accepted by SDF, examples are "HH:mm:ss" or "HH:mm"
			String dateString,			// Contains a literal time but in "String.class" format, examples are "15:27:00" or "10:20" (MUST MATCH SDF FORMAT ABOVE!!)
			String property) throws GuiValidationException		// property is a string that is sent to the Exception handler, defined by the developer
	{
		SimpleDateFormat sdf = new SimpleDateFormat(sdfFormatting);
		Date result = null;

		try
		{
			result = sdf.parse(dateString);
		}
		catch(Exception e)
		{
			throw new GuiValidationException(Arrays.asList(new Violation[] {new Violation(ViolationType.invalidTimeFormat.toString(), property, null, "Invalid Time Format")}));
		}

		return result.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime();
	}
	
	public static Date validateAndConvertTime(String value, String property, boolean required) throws Exception
	{
		if (value != null && value.length() > 0)
		{
			TimeUtility.validateHourMinuteSecondTime(value, property);
			return TimeUtility.convertHourMinuteSecondStringToDate(value, property);
		}
		else if (required)
		{
			throw new GuiValidationException( Arrays.asList(new Violation[] {new Violation(ViolationType.cannotBeEmpty.toString(), property, null, "Time Format Required")}) );
		}

		return null;
	}

	public static String convertDateToHourMinuteSecondString(Date date)
	{
		return TimeUtility.convertDateToString("HH:mm:ss", date);
	}
	
	public static String convertDateToString(String format, Date date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

}
