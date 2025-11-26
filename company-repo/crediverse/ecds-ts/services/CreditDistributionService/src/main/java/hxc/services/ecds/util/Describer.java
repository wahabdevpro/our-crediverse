package hxc.services.ecds.util;

import java.util.Date;

import hxc.ecds.protocol.rest.reports.Report;

public class Describer
{
	public static String describe(Date date)
	{
		return describe(date, "");
	}

	public static String describe(Date date, String extra)
	{
		return String.format("%s@%s(time = '%s', toString = '%s'%s%s)",
			date.getClass().getName(), Integer.toHexString(date.hashCode()),
			date.getTime(), date.toString(),
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public static String describe(Report.IFilterItem<? extends Report.IFilterField, ? extends Object> object)
	{
		return describe(object, "");
	}

	public static String describe(Report.IFilterItem<? extends Report.IFilterField, ? extends Object> object, String extra)
	{
		return String.format("%s@%s(field = '%s', operator = '%s', value = '%s'%s%s)",
			object.getClass().getName(), Integer.toHexString(object.hashCode()),
			object.getField(), object.getOperator(), object.getValue(),
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public static String describe(Report.IFilter<? extends Report.IFilterField> object)
	{
		return describe(object, "");
	}

	@SuppressWarnings({"unchecked"})
	public static String describe(Report.IFilter<? extends Report.IFilterField> object, String extra)
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("%s@%s(", object.getClass().getName(), Integer.toHexString(object.hashCode())));
		stringBuilder.append("items = [ ");
		boolean first = true;
		for (Report.IFilterItem<? extends Report.IFilterField, ? extends Object> item: object.getItems())
		{
			if (first) first = false;
			else stringBuilder.append(", ");

			stringBuilder.append(describe(item));
		}
		stringBuilder.append(" ]");
		if (!extra.isEmpty())
		{
			stringBuilder.append(", ");
			stringBuilder.append(extra);
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}

	public static String describe(Report.ISortItem<? extends Report.IResultField> object)
	{
		return describe(object, "");
	}

	public static String describe(Report.ISortItem<? extends Report.IResultField> object, String extra)
	{
		return String.format("%s@%s(field = '%s', operator = '%s'%s%s)",
			object.getClass().getName(), Integer.toHexString(object.hashCode()),
			object.getField(), object.getOperator(),
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public static String describe(Report.ISort<? extends Report.IResultField> object)
	{
		return describe(object, "");
	}

	@SuppressWarnings({"unchecked"})
	public static String describe(Report.ISort<? extends Report.IResultField> object, String extra)
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(String.format("%s@%s(", object.getClass().getName(), Integer.toHexString(object.hashCode())));
		stringBuilder.append("items = [ ");
		boolean first = true;
		for (Report.ISortItem<? extends Report.IResultField> item: object.getItems())
		{
			if (first) first = false;
			else stringBuilder.append(", ");

			stringBuilder.append(describe(item));
		}
		stringBuilder.append(" ]");
		if (!extra.isEmpty())
		{
			stringBuilder.append(", ");
			stringBuilder.append(extra);
		}
		stringBuilder.append(")");
		return stringBuilder.toString();
	}
}
