package hxc.configuration;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hxc.servicebus.ILocale;
import hxc.services.notification.IPhrase;
import hxc.services.notification.ITexts;
import hxc.services.notification.Phrase;

public class ValidationException extends Exception
{

	private static final Pattern numeric = Pattern.compile("\\d*");
	private String field;

	public ValidationException(String format, Object... args)
	{
		super(String.format(format, args));
	}

	public ValidationException(Throwable cause, String format, Object... args)
	{
		super(String.format(format, args), cause);
	}

	/**
	 *
	 */
	private static final long serialVersionUID = -5210664119429264088L;

	public static void inRange(int min, int value, int max) throws ValidationException
	{
		if (value < min || value > max)
			throw new ValidationException("Must be larger or equal to %d and less or equal to %d", min, max);
	}

	public static void inRange(int min, int value, int max, String fieldName) throws ValidationException
	{
		if (value < min || value > max)
			throw createFieldValidationException(fieldName, String.format("", "Field '%s' must be larger or equal to %d and less or equal to %d", splitCamelCase(fieldName), min, max));
	}

	public static void inRange(int min, int value, int max, String fieldName, String message) throws ValidationException
	{
		if (value < min || value > max)
			throw createFieldValidationException(fieldName, message);
	}

	public static void inRange(long min, long value, long max, String message) throws ValidationException
	{
		if (value < min || value > max)
			throw new ValidationException(message);
	}

	public static void inRange(long min, long value, long max, String fieldName, String message) throws ValidationException
	{
		if (value < min || value > max)
			throw createFieldValidationException(fieldName, message);
	}

	public static <T extends Number> void min(T min, T value) throws ValidationException
	{
		if (value.floatValue() < min.floatValue())
			throw new ValidationException("Must be larger than or equal to  %d", min);
	}

	public static <T extends Number> void min(T min, T value, String fieldName) throws ValidationException
	{
		if (value.floatValue() < min.floatValue())
			throw createFieldValidationException(fieldName, String.format("Field '%s' must be larger than or equal to  %d", splitCamelCase(fieldName), min));
	}

	public static void port(int value) throws ValidationException
	{
		if (value < 1 || value > 65535)
			throw new ValidationException("URI Port must be between 1 and 65635");
	}

	public static void port(int value, String fieldName) throws ValidationException
	{
		if (value < 1 || value > 65535)
			throw createFieldValidationException(fieldName, String.format("Field '%s' must be between 1 and 65635", splitCamelCase(fieldName)));
	}

	public static void validate(String value, String regexExpression) throws ValidationException
	{
		if (!value.matches(regexExpression))
			throw new ValidationException("Invalid format.");
	}

	public static void validate(String value, String regexExpression, String fieldName) throws ValidationException
	{
		if (!value.matches(regexExpression))
			throw createFieldValidationException(fieldName, String.format("Format for field '%s' invalid", splitCamelCase(fieldName)));
	}

	public static void validateFormat(String formatString) throws ValidationException
	{
		FormatValidator.validateFormat(formatString);
	}

	public static void validateFormat(String formatString, String fieldName) throws ValidationException
	{
		try
		{
			FormatValidator.validateFormat(formatString);
		}
		catch (Exception e)
		{
			throw createFieldValidationException(fieldName, e.getMessage());
		}
	}

	public static void validateURL(String url) throws ValidationException
	{
		if (url == null || url.length() == 0)
		{
			return;
		}

		int port = 0;

		try
		{
			new URI(url);

			int poscl = url.lastIndexOf(":");
			int posfs = url.indexOf('/', poscl);
			String strPort = url.substring(poscl + 1, posfs);
			port = Integer.parseInt(strPort);

		}
		catch (Exception e)
		{
			throw new ValidationException("URI Not valid");
		}

		ValidationException.port(port);
	}

	public static void validateURL(String url, String fieldName) throws ValidationException
	{
		try
		{
			validateURL(url);
		}
		catch (ValidationException ve)
		{
			String message = String.format("%s for field '%s'", ve.getMessage(), splitCamelCase(fieldName));
			throw createFieldValidationException(fieldName, message);
		}
	}

	public static void validateTimeFormat(String timeFormat) throws ValidationException
	{
		try
		{
			new SimpleDateFormat(timeFormat);
		}
		catch (Exception e)
		{
			throw new ValidationException("Invalid Time Format");
		}
	}

	public static void validateTimeFormat(String timeFormat, String fieldName) throws ValidationException
	{
		try
		{
			new SimpleDateFormat(timeFormat);
		}
		catch (Exception e)
		{
			throw createFieldValidationException(fieldName, "Invalid Time Format");
		}
	}

	static class FormatValidator
	{
		private static final String formatSpecifier = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";

		public static void validateFormat(String formatString) throws ValidationException
		{

			Pattern fsPattern = Pattern.compile(formatSpecifier);
			Matcher m = fsPattern.matcher(formatString);
			for (int i = 0, len = formatString.length(); i < len;)
			{
				if (m.find(i))
				{
					// Anything between the start of the string and the beginning
					// of the format specifier is either fixed text or contains
					// an invalid format string.
					if (m.start() != i)
					{
						// Make sure we didn't miss any invalid format specifiers
						checkText(formatString, i, m.start());
					}
					i = m.end();
				}
				else
				{
					// No more valid format specifiers. Check for possible invalid
					// format specifiers.
					checkText(formatString, i, len);
					break;
				}
			}
		}

		private static void checkText(String s, int start, int end) throws ValidationException
		{
			for (int i = start; i < end; i++)
			{
				// Any '%' found in the region starts an invalid format specifier.
				if (s.charAt(i) == '%')
				{
					char c = (i == end - 1) ? '%' : s.charAt(i + 1);

					throw new ValidationException("Error in format near " + String.valueOf(c));
				}
			}
		}
	}

	public static void notEmpty(String text, String message) throws ValidationException
	{
		if (text == null || text.length() == 0)
			throw new ValidationException(message);
	}

	public static void notEmpty(String text, String fieldName, String message) throws ValidationException
	{
		if (text == null || text.length() == 0)
			throw createFieldValidationException(fieldName, message);
	}

	public static void lengthInRange(String text, long min, long max, String fieldName) throws ValidationException
	{
		if (text.length() < min || text.length() > max)
			throw createFieldValidationException(fieldName, String.format("length of %s must be in range [%s,%s]", fieldName, min, max));
	}

	public static void lengthInRange(String text, long min, long max, String fieldName, String message) throws ValidationException
	{
		if (text.length() < min || text.length() > max)
			throw createFieldValidationException(fieldName, message);
	}

	public static void isNumeric(String text, String message) throws ValidationException
	{
		if (!numeric.matcher(text).find())
			throw new ValidationException(message);
	}

	public static void isNumeric(String text, String fieldName, String message) throws ValidationException
	{
		if (!numeric.matcher(text).find())
			throw createFieldValidationException(fieldName, message);
	}

	private static boolean doesContain(int element, int[] elements)
	{
		for (int e : elements)
		{
			if (e == element)
				return true;
		}
		return false;
	}

	public static void doesContain(int element, int[] elements, String message) throws ValidationException
	{
		if (!doesContain(element, elements))
			throw new ValidationException(message);
	}

	public static void doesContain(int element, int[] elements, String fieldName, String message) throws ValidationException
	{
		if (!doesContain(element, elements))
			throw createFieldValidationException(fieldName, message);
	}

	private static boolean doesContain(String element, String[] elements)
	{
		for (String e : elements)
		{
			if (e.equalsIgnoreCase(element))
				return true;
		}
		return false;
	}

	public static void doesContain(String element, String[] elements, String message) throws ValidationException
	{
		if (elements == null || !doesContain(element, elements))
			throw new ValidationException(message);
	}

	public static void doesContain(String element, String[] elements, String fieldName, String message) throws ValidationException
	{
		if (elements == null || !doesContain(element, elements))
			throw createFieldValidationException(fieldName, message);
	}

	public static void validate(ITexts name, ILocale locale, String message) throws ValidationException
	{
		if (locale == null)
			return;
		for (int languageID = 1; languageID <= IPhrase.MAX_LANGUAGES; languageID++)
		{
			String language = locale.getLanguage(languageID);
			if (language == null || language.length() == 0)
				continue;
			ValidationException.notEmpty(name.getText(languageID), message);
		}
	}

	public static void validate(ITexts name, ILocale locale, String fieldName, String message) throws ValidationException
	{
		try
		{
			validate(name, locale, message);
		}
		catch (ValidationException ve)
		{
			throw createFieldValidationException(fieldName, message);
		}
	}

	public static <T> void isOneOff(T element, String message, T... elements) throws ValidationException
	{
		for (T e : elements)
		{
			if (e.equals(element))
				return;
		}

		throw new ValidationException(message);
	}

	public static <T> void isOneOff(T element, String fieldName, String message, T... elements) throws ValidationException
	{
		for (T e : elements)
		{
			if (e.equals(element))
				return;
		}

		throw ValidationException.createFieldValidationException(fieldName, message);
	}

	public static void notNull(Object object, String message) throws ValidationException
	{
		if (object == null)
			throw new ValidationException(message);
	}

	public static void notNull(Object object, String fieldName, String message) throws ValidationException
	{
		if (object == null)
			throw createFieldValidationException(fieldName, message);
	}

	public static ValidationException createFieldValidationException(String field, String message, Throwable cause)
	{
		ValidationException ve = new ValidationException(cause, message);
		ve.field = field;
		return ve;
	}

	public static ValidationException createFieldValidationException(String field, String message)
	{
		ValidationException ve = new ValidationException(message);
		ve.field = field;
		return ve;
	}

	public String getField()
	{
		return field;
	}

	static String splitCamelCase(String s)
	{
		String result = (s == null) ? "" : s.replaceAll(String.format("%s|%s|%s", "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])", "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
		if (result.length() > 0)
			result = result.substring(0, 1).toUpperCase() + result.substring(1);
		return result;
	}

	public static void validate(Phrase name, ILocale locale, String fieldName, String message)
	{

	}
}
