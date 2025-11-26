package hxc.services.ecds.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import hxc.ecds.protocol.rest.IValidatable;
import hxc.ecds.protocol.rest.Violation;

public class RuleCheck
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public static void notEmpty(String property, String value) throws RuleCheckException
	{
		if (value == null || value.isEmpty())
			throw new RuleCheckException(StatusCode.CANNOT_BE_EMPTY, property, "%s cannot be empty", property);
	}
	
	public static void notEmpty(String property, String value, int maxLength) throws RuleCheckException
	{
		if (value == null || value.isEmpty())
			throw new RuleCheckException(StatusCode.CANNOT_BE_EMPTY, property, "%s cannot be empty", property);

		if (value.length() > maxLength)
			throw new RuleCheckException(StatusCode.TOO_LONG, property, "%s cannot be longer than %d characters", property, maxLength);
	}

	public static void notNull(String property, String value, int maxLength) throws RuleCheckException
	{
		if (value == null)
			throw new RuleCheckException(StatusCode.CANNOT_BE_EMPTY, property, "%s cannot be empty", property);

		if (value.length() > maxLength)
			throw new RuleCheckException(StatusCode.TOO_LONG, property, "%s cannot be longer than %d characters", property, maxLength);
	}

	public static void notLonger(String property, String value, int maxLength) throws RuleCheckException
	{
		if (value == null)
			return;

		if (value.length() > maxLength)
			throw new RuleCheckException(StatusCode.TOO_LONG, property, "%s cannot be longer than %d characters", property, maxLength);
	}

	public static void notLonger(String property, byte[] value, int maxLength) throws RuleCheckException
	{
		if (value == null)
			return;

		if (value.length > maxLength)
			throw new RuleCheckException(StatusCode.TOO_LONG, property, "%s cannot have more than %d elements", property, maxLength);
	}

	public static void oneOf(String property, String value, String... possibleValues) throws RuleCheckException
	{
		if (value == null || value.isEmpty() || possibleValues == null)
			throw new RuleCheckException(StatusCode.CANNOT_BE_EMPTY, property, "%s cannot be empty", property);

		for (String possibleValue : possibleValues)
		{
			if (value.equals(possibleValue))
				return;
		}

		throw new RuleCheckException(StatusCode.INVALID_VALUE, property, "%s cannot be set to %s", property, value);
	}

	public static void oneOf(String property, Integer value, int... possibleValues) throws RuleCheckException
	{
		if (value == null || possibleValues == null)
			throw new RuleCheckException(StatusCode.CANNOT_BE_EMPTY, property, "%s cannot be empty", property);

		for (int possibleValue : possibleValues)
		{
			if (value.equals(possibleValue))
				return;
		}

		throw new RuleCheckException(StatusCode.INVALID_VALUE, property, "%s cannot be set to %d", property, value);
	}

	public static void notNull(String property, Object value) throws RuleCheckException
	{
		if (value == null)
			throw new RuleCheckException(StatusCode.CANNOT_BE_EMPTY, property, "%s cannot be null", property);
	}

	public static void isNull(String property, Object value) throws RuleCheckException
	{
		if (value != null)
			throw new RuleCheckException(StatusCode.CANNOT_HAVE_VALUE, property, "%s cannot have a value", property);
	}

	public static void noChange(String property, Object newValue, Object oldValue) throws RuleCheckException
	{
		noChange(property, newValue, oldValue, true);
	}

	public static void noChange(String property, Object newValue, Object oldValue, boolean when) throws RuleCheckException
	{
		if (!when)
			return;

		boolean changed = newValue == null ? oldValue != null : !newValue.equals(oldValue);

		if (changed)
			throw new RuleCheckException(StatusCode.CANT_BE_CHANGED, property, "May not change %s (%s -> %s)", property, oldValue, newValue);
	}

	public static void equals(String property, Object left, Object right) throws RuleCheckException
	{
		boolean same = left == null ? right == null : left.equals(right);

		if (!same)
			throw new RuleCheckException(StatusCode.NOT_SAME, property, "%s values not the same", property);
	}

	public static void notLess(String property, BigDecimal value, BigDecimal minimum) throws RuleCheckException
	{
		if (value == null || minimum == null)
			return;

		if (value.compareTo(minimum) < 0)
			throw new RuleCheckException(StatusCode.TOO_SMALL, property, "%s may not be less than %s", property, minimum.toString());
	}
	
	public static void notLess(String property, Date value, Date minimum) throws RuleCheckException
	{
		if (value == null || minimum == null)
			return;

		if (value.compareTo(minimum) < 0)
			throw new RuleCheckException(StatusCode.TOO_SMALL, property, "%s may not be earlier than %s", property, minimum.toString());
	}

	public static void notMore(String property, BigDecimal value, BigDecimal maximum) throws RuleCheckException
	{
		if (value == null || maximum == null)
			return;

		if (value.compareTo(maximum) > 0)
			throw new RuleCheckException(StatusCode.TOO_LARGE, property, "%s may not be more than %s", property, maximum.toString());
	}

	public static void notLess(String property, Integer value, Integer minimum) throws RuleCheckException
	{
		if (value == null || minimum == null)
			return;

		if (value < minimum)
			throw new RuleCheckException(StatusCode.TOO_SMALL, property, "%s may not be less than %d", property, minimum);
	}
	
	public static void notMore(String property, Integer value, Integer maximum) throws RuleCheckException
	{
		if (value == null || maximum == null)
			return;

		if (value > maximum)
			throw new RuleCheckException(StatusCode.TOO_LARGE, property, "%s may not be more than %d", property, maximum);
	}

	public static void notLess(String property, Long value, Long minimum) throws RuleCheckException
	{
		if (value == null || minimum == null)
			return;

		if (value < minimum)
			throw new RuleCheckException(StatusCode.TOO_SMALL, property, "%s may not be less than %d", property, minimum);
	}

	public static void isFalse(String property, boolean condition, String message, Object... args) throws RuleCheckException
	{
		if (condition)
			throw new RuleCheckException(StatusCode.INVALID_VALUE, property, message, args);
	}

	public static void isTrue(String property, boolean condition, String message, Object... args) throws RuleCheckException
	{
		if (!condition)
			throw new RuleCheckException(StatusCode.INVALID_VALUE, property, message, args);
	}

	public static void validate(IValidatable entity) throws RuleCheckException
	{
		if (entity == null)
			return;

		List<Violation> violations = entity.validate();

		if (violations != null && violations.size() > 0)
		{
			Violation violation = violations.get(0);
			throw new RuleCheckException( //
					new StatusCode(violation.getReturnCode(), //
							Status.NOT_ACCEPTABLE), //
					violation.getProperty(), //
					violation.toString());
		}

	}

	public static void zeroOrNull(String property, BigDecimal amount, String message, Object... args) throws RuleCheckException
	{
		if (amount == null || amount.signum() == 0)
			return;

		throw new RuleCheckException(StatusCode.INVALID_VALUE, property, message, args);
	}

}
