package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hxc.ecds.protocol.rest.Transaction;

/*
ADHOC: CSV: GET ~/reports/monthly_group_sales/adhoc/csv?first=...&max=...&sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
ADHOC: JSON: GET ~/reports/monthly_group_sales/adhoc/json?first=...&max=...&sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
*/
/*
CREATE: PUT ~/reports/monthly_group_sales?sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&name=...
UPDATE: PUT ~/reports/monthly_group_sales/{id}?sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&name=...
LIST: GET ~/reports/monthly_group_sales
LOAD: GET ~/reports/monthly_group_sales/{id}
DOWNLOAD REPORT DATA: JSON: GET ~/reports/monthly_group_sales/{id}/json?first=...&max=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
DOWNLOAD REPORT DATA: CSV: GET ~/reports/monthly_group_sales/{id}/csv?first=...&max=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...

*/

public class MonthlySalesPerformanceReport
{
	public static enum TransactionType //implements Transaction.IType
	{
		SALES_ONLY(new Transaction.Type[] {Transaction.Type.SELL, Transaction.Type.SELF_TOPUP, Transaction.Type.SELL_BUNDLE, Transaction.Type.ADJUDICATE}),
		REVERSALS_ONLY(new Transaction.Type[] {Transaction.Type.REVERSE, Transaction.Type.REVERSE_PARTIALLY}),
		AIRTIME_NET_SALES(new Transaction.Type[] {Transaction.Type.SELL, Transaction.Type.SELF_TOPUP, Transaction.Type.REVERSE, Transaction.Type.REVERSE_PARTIALLY}),
		NON_AIRTIME_NET_SALES(new Transaction.Type[] {}),
		NET_SALES(new Transaction.Type[] {}),
		SELL(new Transaction.Type[] {Transaction.Type.SELL}),
		SELF_TOPUP(new Transaction.Type[] {Transaction.Type.SELF_TOPUP}),
		SELL_BUNDLE(new Transaction.Type[] {Transaction.Type.SELL_BUNDLE}),
		NON_AIRTIME_DEBIT(new Transaction.Type[] {Transaction.Type.NON_AIRTIME_DEBIT}),
		NON_AIRTIME_REFUND(new Transaction.Type[] {Transaction.Type.NON_AIRTIME_REFUND});

		private Set<Transaction.Type> types;

		public Set<Transaction.Type> getTypes()
		{
			return this.types;
		}

		//@Override
		public Set<String> getCode()
		{
			Set<String> codes = new HashSet<String>(); 
			for(Transaction.Type type : this.types)
			{
				codes.add(type.getCode());
			}
			return codes;
		}

		private TransactionType(Transaction.Type[] types)
		{
			this.types = new HashSet<Transaction.Type>(Arrays.asList(types));
		}

		private static final Set<Set<String>> codeSet = new HashSet<Set<String>>();
		private static final Map<Set<String>,TransactionType> codeMap = new HashMap<Set<String>,TransactionType>();

		public static Set<Set<String>> getCodeSet()
		{
			return Collections.unmodifiableSet(codeSet);
		}

		public static Map<Set<String>,TransactionType> getCodeMap()
		{
			return Collections.unmodifiableMap(codeMap);
		}

		public static TransactionType fromCode(String code)
		{
			if (code == null) throw new NullPointerException("code may not be null");
			TransactionType result = codeMap.get(code);
			if (result == null) throw new IllegalArgumentException(String.format("No TransactionType associated with code '%s'", code));
			return result;
		}

		public static boolean hasCode(String code)
		{
			if (code == null) throw new NullPointerException("code may not be null");
			return codeMap.containsKey(code);
		}

		static
		{
			for (TransactionType transactionType : values())
			{
				codeSet.add(transactionType.getCode());
				codeMap.put(transactionType.getCode(), transactionType);
			}
		}
	}

	//Redundant ENUM Remove
	public static enum ResultField implements Report.IResultField
	{
		GROUP_NAME("groupName",String.class),
		AGENT_TOTAL_COUNT("agentTotalCount",Integer.class),
		AGENT_TRANSACTED_COUNT("agentTransactedCount",Integer.class),
		TRANSACTION_COUNT("transactionCount",Integer.class),
		AGENT_AVERAGE_AMOUNT("agentAverageAmount",BigDecimal.class),
		TRANSACTION_AVERAGE_AMOUNT("transactionAverageAmount",BigDecimal.class),
		TOTAL_AMOUNT("totalAmount",BigDecimal.class);

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
		PERIOD("period", String.class, Report.FilterOperator.getEnumOperators()),
		TIERS("tiers", String[].class, Report.FilterOperator.getArrayOperators()),
		GROUPS("groups", String[].class, Report.FilterOperator.getArrayOperators()),
		AGENTS("agents", String[].class, Report.FilterOperator.getArrayOperators()),
		OWNER_AGENTS("ownerAgents", String[].class, Report.FilterOperator.getArrayOperators()),
		TRANSACTION_TYPES("transactionTypes", TransactionType[].class, Report.FilterOperator.getArrayOperators()),
		TRANSACTION_STATUS("transactionStatus", Boolean.class, Report.FilterOperator.getEnumOperators());

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

	protected MonthlySalesPerformanceReport()
	{
	}
}
