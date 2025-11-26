package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hxc.ecds.protocol.rest.Transaction;

/*
ADHOC: CSV: GET ~/reports/retailer_performance/adhoc/csv?first=...&max=...&sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
ADHOC: JSON: GET ~/reports/retailer_performance/adhoc/json?first=...&max=...&sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
*/
/*
CREATE: PUT ~/reports/retailer_performance?sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&name=...
UPDATE: PUT ~/reports/retailer_performance/{id}?sort=...&filter=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&name=...
LIST: GET ~/reports/retailer_performance
LOAD: GET ~/reports/retailer_performance/{id}
DOWNLOAD REPORT DATA: JSON: GET ~/reports/retailer_performance/{id}/json?first=...&max=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...
DOWNLOAD REPORT DATA: CSV: GET ~/reports/retailer_performance/{id}/csv?first=...&max=...&timeInterval.start=...&timeInterval.end=...&relativeTimeRange.code=...&relativeTimeRange.reference=...

*/

public class RetailerPerformanceReport
{
	public static enum TransactionType// implements Transaction.IType
	{
		SELL(new Transaction.Type[] {Transaction.Type.SELL}),
		SELF_TOPUP(new Transaction.Type[] {Transaction.Type.SELF_TOPUP}),
		SELL_BUNDLE(new Transaction.Type[] {Transaction.Type.SELL_BUNDLE}),
		REVERSE(new Transaction.Type[] {Transaction.Type.REVERSE, Transaction.Type.REVERSE_PARTIALLY}),
		TRANSFER(new Transaction.Type[] {Transaction.Type.TRANSFER}),
		ADJUDICATE(new Transaction.Type[] {Transaction.Type.ADJUDICATE}),
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

	public static enum ResultField implements Report.IResultField
	{
		DATE("date",Date.class),

		TRANSACTION_TYPE("transactionType",Transaction.Type.class),
		TRANSACTION_STATUS("transactionStatus",Boolean.class),
		FOLLOW_UP("followUp",String.class),

		A_AGENT_ID("a_AgentID",Integer.class),
		A_ACCOUNT_NUMBER("a_AccountNumber",String.class),
		A_MOBILE_NUMBER("a_MobileNumber",String.class),
		A_IMEI("a_IMEI",String.class),
		A_IMSI("a_IMSI",String.class),
		A_NAME("a_Name",String.class),

		A_TIER_NAME("a_TierName",String.class),
		A_GROUP_NAME("a_GroupName",String.class),
		A_SERVICE_CLASS_NAME("a_ServiceClassName",String.class),

		A_OWNER_ID("a_OwnerID",Integer.class),
		A_OWNER_MOBILE_NUMBER("a_OwnerMobileNumber",String.class),
		A_OWNER_IMSI("a_OwnerImsi",String.class),
		A_OWNER_NAME("a_OwnerName",String.class),

		TOTAL_AMOUNT("totalAmount",BigDecimal.class),
		TOTAL_BONUS("totalBonus",BigDecimal.class),
		TRANSACTION_COUNT("transactionCount",Integer.class);

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
		TRANSACTION_TYPE("transactionType", TransactionType.class, Report.FilterOperator.getEnumOperators()),
		TRANSACTION_STATUS("transactionStatus", Boolean.class, Report.FilterOperator.getEnumOperators()),
		FOLLOW_UP("followUp", String.class, Report.FilterOperator.getEnumOperators()),

		A_AGENT_ID("a_AgentID", String.class, Report.FilterOperator.getStringOperators()),
		A_MOBILE_NUMBER("a_MobileNumber", String.class, Report.FilterOperator.getStringOperators()),

		A_TIER_NAME("a_TierName", String.class, Report.FilterOperator.getStringOperators()),
		A_GROUP_NAME("a_GroupName", String.class, Report.FilterOperator.getStringOperators()),
		A_SERVICE_CLASS_NAME("a_ServiceClassName", String.class, Report.FilterOperator.getStringOperators()),

		A_OWNER_ID("a_OwnerID", String.class, Report.FilterOperator.getStringOperators()),
		A_OWNER_MOBILE_NUMBER("a_OwnerMobileNumber", String.class, Report.FilterOperator.getStringOperators()),
		
		A_IMEI("a_IMEI", String.class, Report.FilterOperator.getStringOperators()),

		TOTAL_AMOUNT("totalAmount", BigDecimal.class, Report.FilterOperator.getNumericOperators());

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

	protected RetailerPerformanceReport()
	{
	}
}
