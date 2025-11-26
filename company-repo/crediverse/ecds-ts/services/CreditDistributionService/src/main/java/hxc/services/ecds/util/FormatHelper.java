package hxc.services.ecds.util;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.ecds.rest.ICreditDistribution;

public class FormatHelper {
	final static Logger logger = LoggerFactory.getLogger(EmailUtils.class);
	public static final DateFormat TIME_ZONE_FORMAT = new SimpleDateFormat("z");
	public static final TimeZone UTC = TimeZone.getTimeZone("UTC");
	public static final Map<Locale, TimeZone> localeTimezoneMap;
	
	static {
		localeTimezoneMap = new HashMap<>();
		localeTimezoneMap.put(Locale.forLanguageTag("en-CI"), TimeZone.getTimeZone("Africa/Abidjan"));
	}

	public static String formatTime(ICreditDistribution context, Object origin, Locale locale, Date date)
	{
		if (date == null)
		{
			logger.warn("formatDate called with null date ...");
			return "";
		}

		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		return df.format(date);
	}

	public static String formatTimeWithTimezone(Locale locale, Date date) {
		if (date == null) {
			logger.warn("formatTimeWithTimezone called with null date ...");
			return "";
		}

		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT, locale);
		TimeZone timeZone = UTC;
		if (localeTimezoneMap.containsKey(locale)) {
			timeZone = localeTimezoneMap.get(locale);
		}
		df.setTimeZone(timeZone);
		TIME_ZONE_FORMAT.setTimeZone(timeZone);
		return df.format(date) + " " + TIME_ZONE_FORMAT.format(date);
	}
	
	public static String formatDate(ICreditDistribution context, Object origin, Locale locale, Date date)
	{
		if (date == null)
		{
			logger.warn("formatDate called with null date ...");
			return "";
		}

		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
		return df.format(date);
	}
	
	public String formatBigDecimal(ICreditDistribution context, Object origin, Locale locale, BigDecimal amount)
	{
		if (amount == null || locale == null)
		{
			logger.warn("format called with null amount or locale ...");
			return "";
		}

		NumberFormat numberFormat = context.getCurrencyFormat(locale);
		return numberFormat.format(amount);
	}
	
	public static String longDateDaysAgo( int dayIncrement )
	{
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, dayIncrement);
		
		String year		= calendar.get(Calendar.YEAR) + "";
		int mon			= 1 + calendar.get(Calendar.MONTH);
		String month	= (mon < 10)?("0" + String.valueOf(mon)):String.valueOf(mon);
		String day		= (calendar.get(Calendar.DATE) < 10)?("0" + calendar.get(Calendar.DATE)):(calendar.get(Calendar.DATE) + "");
		
		return year + "-" + month + "-" + day;
	}
}
