package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
ADHOC: CSV: GET ~/reports/account_balance_summary/adhoc/csv?first=...&max=...&sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
ADHOC: JSON: GET ~/reports/account_balance_summary/adhoc/json?first=...&max=...&sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
*/
/*
CREATE: PUT ~/reports/account_balance_summary?sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&name=...
UPDATE: PUT ~/reports/account_balance_summary/{id}?sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&name=...
LIST: GET ~/reports/account_balance_summary
LOAD: GET ~/reports/account_balance_summary/{id}
DOWNLOAD REPORT DATA: JSON: GET ~/reports/account_balance_summary/{id}/json?first=...&max=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
DOWNLOAD REPORT DATA: CSV: GET ~/reports/account_balance_summary/{id}/csv?first=...&max=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...

*/

public class AccountBalanceSummaryReport
{
	public static enum ResultField implements Report.IResultField
	{
		MSISDN("msisdn",String.class),
		AGENT_NAME("agentName",String.class),
		BALANCE("balance",BigDecimal.class),
		BONUS_BALANCE("bonusBalance",BigDecimal.class),
		HOLD_BALANCE("holdBalance",BigDecimal.class),
		TIER_NAME("tierName",String.class),
		GROUP_NAME("groupName",String.class);

		private String identifier;
		private Class<?> type;

		public String getIdentifier()
		{
			return this.identifier;
		}

		public Class<?> getType()
		{
			return this.type;
		}

		private ResultField(String identifier, Class<?> type)
		{
			this.identifier = identifier;
			this.type = type;
		}

		public static final Map<String,ResultField> identifierMap = new HashMap<String,ResultField>();

		public static Map<String,ResultField> getIdentifierMap()
		{
			return Collections.unmodifiableMap(identifierMap);
		}

		public static ResultField fromIdentifier(String identifier) throws NullPointerException, IllegalArgumentException
		{
			if (identifier == null) throw new NullPointerException("identifier may not be null");
			ResultField result = identifierMap.get(identifier);
			if (result == null) throw new IllegalArgumentException(String.format("No ResultField associated with identifier '%s' (valid identifiers = %s)", identifier, identifierMap.keySet()));
			return result;
		}

		static
		{
			for (ResultField resultField : values())
			{
				identifierMap.put(resultField.getIdentifier(),resultField);
			}
		}
	}

	public static enum FilterField implements Report.IFilterField
	{
		TIER_TYPE("tierType", String.class, Report.FilterOperator.getStringOperators()),
		TIER_NAME("tierName", String.class, Report.FilterOperator.getStringOperators()),
		GROUP_NAME("groupName", String.class, Report.FilterOperator.getStringOperators()),
		INCLUDE_ZERO_BALANCE("includeZeroBalance", String.class, Report.FilterOperator.getEnumOperators()),
		INCLUDE_DELETED("includeDeleted", String.class, Report.FilterOperator.getEnumOperators()),
		ACTIVITY_SCALE("activityScale", String.class, Report.FilterOperator.getStringOperators()),
		ACTIVITY_VALUE("activityValue", Integer.class, Report.FilterOperator.getNumericOperators());

		private String identifier;
		private Class<?> type;
		private Set<Report.FilterOperator> allowedOperators;

		@Override
		public String getIdentifier()
		{
			return this.identifier;
		}

		@Override
		public Class<?> getType()
		{
			return this.type;
		}

		@Override
		public Set<Report.FilterOperator> getAllowedOperators()
		{
			return this.allowedOperators;
		}


		private FilterField(String identifier, Class<?> type, Set<Report.FilterOperator> allowedOperators)
		{
			this.identifier = identifier;
			this.type = type;
			this.allowedOperators = allowedOperators;
		}

		public static final Map<String,FilterField> identifierMap = new HashMap<String,FilterField>();

		public static Map<String,FilterField> getIdentifierMap()
		{
			return Collections.unmodifiableMap(identifierMap);
		}

		public static FilterField fromIdentifier(String identifier) throws NullPointerException, IllegalArgumentException
		{
			if (identifier == null) throw new NullPointerException("identifier may not be null");
			FilterField result = identifierMap.get(identifier);
			if (result == null) throw new IllegalArgumentException(String.format("No ResultField associated with identifier '%s' (valid identifiers = %s)", identifier, identifierMap.keySet()));
			return result;
		}

		static
		{
			for (FilterField filterField : values())
			{
				identifierMap.put(filterField.getIdentifier(),filterField);
			}
		}
	}

	public static class FilterItem<ValueType> extends Report.FilterItem<FilterField,ValueType>
	{
		public FilterItem()
		{
			super();
		}

		public FilterItem(FilterField field, Report.FilterOperator operator, ValueType value)
		{
			super(field, operator, value);
		}
	}

	public static class Filter extends Report.Filter<FilterItem<? extends Object>>
	{
		public Filter()
		{
			super();
		}

		public Filter(List<? extends FilterItem<? extends Object>> items)
		{
			super(items);
		}
	}

	public static class SortItem extends Report.SortItem<ResultField>
	{
		public SortItem()
		{
			super();
		}

		public SortItem(ResultField field, Report.SortOperator operator)
		{
			super(field, operator);
		}
	}

	public static class Sort extends Report.Sort<SortItem>
	{
		public Sort()
		{
			super();
		}

		public Sort(List<? extends SortItem> items)
		{
			super(items);
		}
	}

	protected AccountBalanceSummaryReport()
	{
	}
}
