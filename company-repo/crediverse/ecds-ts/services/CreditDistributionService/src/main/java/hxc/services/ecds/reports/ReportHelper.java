package hxc.services.ecds.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportHelper
{
	private static final String filterPatternString = "\\+([A-Z,a-z,0-9,\\_,\\.]+)(" + Report.FilterOperator.getSymbolRegexString() + ")\\'([^\\']*)\\'";
	private static final Pattern filterPattern = Pattern.compile(filterPatternString);

	/*
	public static enum SortOperator
	{
		ASCENDING("+"),
		DESCENDING("-");

		private String symbol;

		public String getSymbol()
		{
			return this.symbol;
		}

		private SortOperator(String symbol)
		{
			symbol = symbol;
		}

		private static final Map<String,SortOperator> symbolMap = new HashMap<>();
		public static final Map<String,SortOperator> getSymbolMap()
		{
			return symbolMap;
		}

		static
		{
			for (SortOperator sortOperator : values())
			{
				symbolMap.put(sortOperator.getSymbol(), sortOperator);
			}
		}
	}
	*/

	public static String getFilterPatternString()
	{
		return filterPatternString;
	}

	public static Pattern getFilterPattern()
	{
		return filterPattern;
	}

	public static class IntermediateSortItem
	{
		String fieldIdentifier;
		Report.SortOperator operator;

		public String getFieldIdentifier()
		{
			return this.fieldIdentifier;
		}

		public Report.SortOperator getOperator()
		{
			return this.operator;
		}

		public IntermediateSortItem(String fieldIdentifier, Report.SortOperator operator)
		{
			this.fieldIdentifier = fieldIdentifier;
			this.operator = operator;
		}

		public String toString()
		{
			return this.toString("");
		}

		public String toString(String extra)
		{
			return String.format("%s@%s(fieldIdentifier = '%s', operator = '%s'%s%s)",
				this.getClass().getName(), Integer.toHexString(this.hashCode()),
				this.getFieldIdentifier(), this.getOperator(),
				(extra.isEmpty() ? "" : ", "), extra);
		}
	}

	public static List<IntermediateSortItem> parseSortStringIntermediate(String sortString) throws Exception
	{
		List<IntermediateSortItem> result = new ArrayList<IntermediateSortItem>();
		StringTokenizer st = new StringTokenizer(sortString, "+-", true);
		Map<String,Report.SortOperator> symbolMap = Report.SortOperator.getSymbolMap();
		String fieldIdentifier = null;
		Report.SortOperator operator = null;
		while (st.hasMoreTokens())
		{
			String token = st.nextToken();
			if (symbolMap.containsKey(token))
			{
				operator = symbolMap.get(token);
				result.add(new IntermediateSortItem(fieldIdentifier, operator));
			}
			else
			{
				fieldIdentifier = token;
			}
		}
		return result;
	}

	public static class IntermediateFilterItem
	{
		String fieldIdentifier;
		Report.FilterOperator operator;
		String valueString;

		public String getFieldIdentifier()
		{
			return this.fieldIdentifier;
		}

		public Report.FilterOperator getOperator()
		{
			return this.operator;
		}

		public String getValueString()
		{
			return this.valueString;
		}

		IntermediateFilterItem(String fieldIdentifier, Report.FilterOperator operator, String valueString)
		{
			this.fieldIdentifier = fieldIdentifier;
			this.operator = operator;
			this.valueString = valueString;
		}

		public String toString()
		{
			return this.toString("");
		}

		public String toString(String extra)
		{
			return String.format("%s@%s(fieldIdentifier = '%s', operator = '%s', valueString = '%s'%s%s)",
				this.getClass().getName(), Integer.toHexString(this.hashCode()),
				this.getFieldIdentifier(), this.getOperator(), this.getValueString(),
				(extra.isEmpty() ? "" : ", "), extra);
		}
	}

	public static List<IntermediateFilterItem> parseFilterStringIntermediate(String filterString) throws Exception
	{
		List<IntermediateFilterItem> result = new ArrayList<IntermediateFilterItem>();
		Matcher filterMatcher = filterPattern.matcher(filterString);
		while (filterMatcher.find())
		{
			String fieldIdentifier = filterMatcher.group(1);
			String operatorRepresentation = filterMatcher.group(2);
			String valueString = filterMatcher.group(3);
			Report.FilterOperator operator = Report.FilterOperator.fromRepresentation(operatorRepresentation);
			result.add(new IntermediateFilterItem(fieldIdentifier, operator, valueString));
		}
		return result;
	}
}
