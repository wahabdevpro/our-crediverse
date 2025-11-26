package hxc.ecds.protocol.rest;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import hxc.ecds.protocol.rest.config.Phrase;

public class Validator
{
	public static final Pattern ONLY_DIGITS_PATTERN = Pattern.compile("\\d+");
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private List<Violation> violations = new ArrayList<Violation>();
	private static final BigDecimal moneyUlp = new BigDecimal("0.0001");
	private static final BigDecimal moneyMax = new BigDecimal("9999999999999.9999"); 
	private static final BigDecimal moneyMin = moneyMax.negate(); 
	private static final Pattern pathPattern = Pattern.compile("^[a-zA-Z0-9-_./]*$");
	private static final Pattern filenamePattern = Pattern.compile("^[a-zA-Z0-9-_.]*$");
	private static final Pattern emailPattern = Pattern.compile("^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$");

	public Validator()
	{
	}

	public Validator(List<Violation> violations)
	{
		this.violations = violations;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public Validator matchesRegex(String property, String value, String patternString, String violationDescriptionSuffix)
	{
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(value);

		boolean matchFound = matcher.find();
		if (!matchFound)
			return append(Violation.INVALID_VALUE, property, "", "%s", violationDescriptionSuffix);

		return this;
	}

	public Validator notEmpty(String property, String value, int maxLength)
	{
		if (value == null || value.isEmpty())
			return append(Violation.CANNOT_BE_EMPTY, property, maxLength, "%s cannot be empty", property);

		if (value.length() > maxLength)
			return append(Violation.TOO_LONG, property, maxLength, "%s cannot be longer than %d characters", property, maxLength);

		return this;
	}
	
	public Validator notEmpty(String property, Phrase phrase)
	{
		if (Phrase.nullOrEmpty(phrase))
			return append(Violation.CANNOT_BE_EMPTY, property, null, "%s cannot be empty", property);
		
		return this;
	}

	public Validator notEmpty(String property, byte[] value, long maxLength)
	{
		if (value == null || value.length == 0)
			return append(Violation.CANNOT_BE_EMPTY, property, maxLength, "%s cannot be empty", property);

		if (value.length > maxLength)
			return append(Violation.TOO_LONG, property, maxLength, "%s cannot be longer than %d characters", property, maxLength);

		return this;
	}

	public Validator exactLength(String property, String value, int length) {
		notEmpty(property, value, length, length);
		return this;
	}

	public Validator onlyDigits(String property, String value) {
		if (!ONLY_DIGITS_PATTERN.matcher(value).matches()) {
			append(Violation.INVALID_VALUE, property, null, "%s must contain only digits", property);
		}
		return this;
	}

	public Validator notEmpty(String property, String value, int minLength, int maxLength)
	{
		if (value == null || value.isEmpty())
			return append(Violation.CANNOT_BE_EMPTY, property, null, "%s cannot be empty", property);

		if (value.length() > maxLength)
			return append(Violation.TOO_LONG, property, maxLength, "%s cannot be longer than %d characters", property, maxLength);

		if (value.length() < minLength)
			return append(Violation.TOO_SHORT, property, minLength, "%s cannot be shorter than %d characters", property, minLength);

		return this;
	}
	
	public Validator notAnyEmpty(String property, Phrase value)
	{
		if (value == null || Phrase.someNullOrEmpty(value))
			return append(Violation.CANNOT_BE_EMPTY, property, null, "%s cannot be empty", property);
		
		return this;
	}

	public Validator notNull(String property, String value, int maxLength)
	{
		if (value == null)
			return append(Violation.CANNOT_BE_EMPTY, property, null, "%s cannot be empty", property);

		if (value.length() > maxLength)
			return append(Violation.TOO_LONG, property, maxLength, "%s cannot be longer than %d characters", property, maxLength);

		return this;
	}

	public Validator notLonger(String property, String value, int maxLength)
	{
		if (value == null)
			return this;

		if (value.length() > maxLength)
			return append(Violation.TOO_LONG, property, maxLength, "%s cannot be longer than %d characters", property, maxLength);

		return this;
	}

	public Validator notLonger(String property, byte[] value, int maxLength)
	{
		if (value == null)
			return this;

		if (value.length > maxLength)
			return append(Violation.TOO_LONG, property, maxLength, "%s cannot have more than %d elements", property, maxLength);

		return this;
	}

	public Validator oneOf(String property, String value, String... possibleValues)
	{
		if (value == null || value.isEmpty() || possibleValues == null)
			return append(Violation.CANNOT_BE_EMPTY, property, false, "%s cannot be empty", property);

		for (String possibleValue : possibleValues)
		{
			if (value.equals(possibleValue))
				return this;
		}

		return append(Violation.INVALID_VALUE, property, null, "%s cannot be set to %s", property, value);
	}
	
	public Validator oneOf(String property, int value, int... possibleValues)
	{
		if (possibleValues == null)
			return append(Violation.INVALID_VALUE, property, null, "%s cannot be set to %s", property, value);

		for (int possibleValue : possibleValues)
		{
			if (value == possibleValue)
				return this;
		}

		return append(Violation.INVALID_VALUE, property, null, "%s cannot be set to %s", property, value);
	}

	public Validator notNull(String property, Object value)
	{
		if (value == null)
			return append(Violation.CANNOT_BE_EMPTY, property, null, "%s cannot be null", property);

		return this;
	}

	public Validator isMoney(String property, BigDecimal amount)
	{
		if (amount == null || amount.signum() == 0)
			return this;

		if (amount.stripTrailingZeros().ulp().compareTo(moneyUlp) < 0 )
			return append(Violation.INVALID_VALUE, property, null, "%s has too many decimal digits", property);
		
		if (amount.compareTo(moneyMax) > 0)
			return append(Violation.TOO_LARGE, property, null, "%s is too large", property);
		
		if (amount.compareTo(moneyMin) < 0)
			return append(Violation.TOO_SMALL, property, null, "%s is too small", property);

		return this;
	}

	public Validator contains(String property, String text, String substring)
	{
		if (text == null || substring == null)
			return this;

		if (!text.toLowerCase().contains(substring.toLowerCase()))
			return append(Violation.INVALID_VALUE, property, null, "Must contain %s", substring);

		return this;
	}

	public Validator maxDigitsAfterDecimalPoint(String property, BigDecimal value, int digits) {
		if (value.stripTrailingZeros().scale() > digits) {
			append(Violation.INVALID_VALUE, property, digits, "%s has more than %d digits after decimal point", property, digits);
		}
		return this;
	}

	public Validator isNull(String property, Object value)
	{
		if (value != null)
			return append(Violation.CANNOT_HAVE_VALUE, property, null, "%s cannot have a value", property);

		return this;
	}

	public Validator noChange(String property, Object newValue, Object oldValue)
	{
		return noChange(property, newValue, oldValue, true);
	}

	public Validator noChange(String property, Object newValue, Object oldValue, boolean when)
	{
		if (!when)
			return this;

		boolean changed = newValue == null ? oldValue != null : !newValue.equals(oldValue);

		if (changed)
			return append(Violation.CANT_BE_CHANGED, property, null, "May not change %s", property);

		return this;
	}

	public Validator notEquals(String propertyLeft, String propertyRight, Object left, Object right)
	{
		boolean same = left == null ? right == null : left.equals(right);

		if (same)
			return append(Violation.SAME, propertyLeft, right, "%s and %s values cannot be the same", propertyLeft, propertyRight);

		return this;
	}

	public Validator equals(String property, Object left, Object right)
	{
		boolean same = left == null ? right == null : left.equals(right);

		if (!same)
			return append(Violation.NOT_SAME, property, right, "%s values not the same", property);

		return this;
	}

	public Validator notLess(String property, BigDecimal value, BigDecimal minimum)
	{
		if (value == null || minimum == null)
			return this;

		if (value.compareTo(minimum) < 0)
			return append(Violation.TOO_SMALL, property, minimum, "%s may not be less than %s", property, minimum.toString());

		return this;
	}
	
	public Validator notMore(String property, BigDecimal value, BigDecimal minimum)
	{
		if (value == null || minimum == null)
			return this;

		if (value.compareTo(minimum) > 0)
			return append(Violation.TOO_LARGE, property, minimum, "%s may not be more than %s", property, minimum.toString());

		return this;
	}

	public Validator notLess(String property, Date value, Date minimum)
	{
		if (value == null || minimum == null)
			return this;

		if (value.before(minimum))
			return append(Violation.TOO_SMALL, property, minimum, "%s may not be earlier than %s", property, minimum.toString());

		return this;
	}

	public Validator notLess(String property, Integer value, Integer minimum)
	{
		if (value == null || minimum == null)
			return this;

		if (value < minimum)
			return append(Violation.TOO_SMALL, property, minimum, "%s may not be less than %d", property, minimum);

		return this;
	}

	public Validator notMore(String property, Integer value, Integer maximum)
	{
		if (value == null || maximum == null)
			return this;

		if (value > maximum)
			return append(Violation.TOO_LARGE, property, maximum, "%s may not be more than %d", property, maximum);

		return this;
	}

	public Validator notLess(String property, Long value, Long minimum)
	{
		if (value == null || minimum == null)
			return this;

		if (value < minimum)
			return append(Violation.TOO_SMALL, property, minimum, "%s may not be less than %d", property, minimum);

		return this;
	}

	public Validator notLess(String property, Double value, Double minimum)
	{
		if (value == null || minimum == null)
			return this;

		if (value < minimum)
			return append(Violation.TOO_SMALL, property, minimum, "%s may not be less than %f", property, minimum);

		return this;
	}

	public Validator notMore(String property, Double value, Double maximum)
	{
		if (value == null || maximum == null)
			return this;

		if (value > maximum)
			return append(Violation.TOO_LARGE, property, maximum, "%s may not be more than %f", property, maximum);

		return this;
	}

	public Validator isFalse(String property, boolean condition, String message, Object... args)
	{
		if (condition)
			return append(Violation.INVALID_VALUE, property, false, message, args);

		return this;
	}

	public Validator isTrue(String property, boolean condition, String message, Object... args)
	{
		if (!condition)
			return append(Violation.INVALID_VALUE, property, true, message, args);

		return this;
	}

	public Validator validate(IValidatable instance)
	{
		if (instance == null)
			return append(Violation.CANNOT_BE_EMPTY, "instance", null, "Instance cannot be null");

		List<Violation> violations = instance.validate();
		this.violations.addAll(violations);

		return this;
	}

	public Validator validExpandableText(final String property, Phrase phrase, Phrase[] fields)
	{
		// FIXME: potentially unsafe to do this ... phrase should not be null
		if (phrase == null)
			return this;
		Map<String, String> map = phrase.getTexts();

		// FIXME: potentially unsafe to do this ... phrase should not be null
		if (fields == null)
			fields = new Phrase[0];

		for (String language : map.keySet())
		{
			String text = map.get(language);
			if (text == null || text.isEmpty())
				continue;
			String[] parts = text.split("(?=\\{)|(?<=\\})");
			for (String part : parts)
			{
				if (part.startsWith("{"))
				{
					boolean found = false;
					for (Phrase field : fields)
					{
						if (field.contains(part))
						{
							found = true;
							break;
						}
					}
					if (!found) {
						append(Violation.INVALID_VALUE, property, part, "Invalid value: %s", part);
					}
				}

			}
		}

		return this;
	}

	private interface ExtraCriterion<ValueType>
	{
		public void validate(ValueType value);
	}

	public Validator validExpandableTextWithExtraCriteria(final String property, Phrase phrase, Phrase[] fields, ExtraCriterion... extraCriteria)
	{
		// FIXME: potentially unsafe to do this ... phrase should nor be null
		if (phrase == null)
			return this;

		// FIXME: potentially unsafe to do this ... phrase should nor be null
		if (fields == null)
			fields = new Phrase[0];

		Map<String, String> map = phrase.getTexts();

		for (Map.Entry<String, String> entry : map.entrySet())
		{
			String text = entry.getValue();
			String[] parts = text.split("(?=\\{)|(?<=\\})");
			for (String part : parts)
			{
				if (part.startsWith("{"))
				{
					boolean found = false;
					for (Phrase field : fields)
					{
						if (field.contains(part))
						{
							found = true;
							break;
						}
					}
					if (!found)
						append(Violation.INVALID_VALUE, property, part, "Invalid value: %s", part);
				}
				else if (extraCriteria.length > 0)
				{
					for (ExtraCriterion extraCriterion : extraCriteria)
					{
						extraCriterion.validate(part);
					}
				}
			}
		}
		return this;
	}

	public Validator validExpandablePath(final String property, Phrase phrase, Phrase[] fields)
	{
		return this.validExpandableTextWithExtraCriteria(property, phrase, fields, new ExtraCriterion<String>()
		{
			@Override
			public void validate(String string)
			{
				if (pathPattern.matcher(string).matches() == false)
				{
					append(Violation.INVALID_VALUE, property, string, "%s may only contain ASCII alphanumeric characters (A-Z, a-z, 0-9) and '.', '_', '-', '/' (offending part: '%s')", property,
							string);
				}
			}
		});
	}

	public Validator validExpandableFilename(final String property, Phrase phrase, Phrase[] fields)
	{
		return this.validExpandableTextWithExtraCriteria(property, phrase, fields, new ExtraCriterion<String>()
		{
			@Override
			public void validate(String string)
			{
				if (filenamePattern.matcher(string).matches() == false)
				{
					append(Violation.INVALID_VALUE, property, string, "%s may only contain ASCII alphanumeric characters (A-Z, a-z, 0-9) and '.', '_', '-' (offending part: '%s')", property, string);
				}
			}
		});
	}

	public Validator validUssdCommand(String property, Phrase command, Phrase[] commandFields)
	{
		return validExpandableText(property, command, commandFields);
	}

	public Validator validSmsCommand(String property, Phrase command, Phrase[] commandFields)
	{
		return validExpandableText(property, command, commandFields);
	}

	public Validator numeric(String property, String value, Integer minLength, Integer maxLength)
	{
		if (value == null || value.isEmpty())
			return this;

		if (maxLength != null && value.length() > maxLength)
			return append(Violation.TOO_LONG, property, maxLength, "%s may not be more longer than %d", property, maxLength);

		if (minLength != null && value.length() < minLength)
			return append(Violation.TOO_SHORT, property, minLength, "%s may not be more shorter than %d", property, minLength);

		for (char c : value.toCharArray())
		{
			if (!Character.isDigit(c))
			{
				return append(Violation.INVALID_VALUE, property, null, "%s must be numeric", property);
			}
		}

		return this;
	}
	
	public Validator validEmailAddress(String property, String emailAddress)
	{
		Matcher matcher = emailPattern.matcher(emailAddress);
		if(!matcher.matches())
		{
			return append(Violation.INVALID_VALUE, property, emailAddress, "%s supplied for field %s is not a valid email address.", emailAddress, property);
		}
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	public Validator append(String code, String property, Object criterium, String message, Object... args)
	{
		violations.add(new Violation(code, property, criterium, message == null ? "" : String.format(message, args)));
		return this;
	}

	public List<Violation> toList()
	{
		return violations;
	}

}
