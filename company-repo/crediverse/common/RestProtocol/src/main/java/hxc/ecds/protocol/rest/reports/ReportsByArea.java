package hxc.ecds.protocol.rest.reports;

import java.math.BigDecimal;
import java.util.*;

import hxc.ecds.protocol.rest.Transaction;

public class ReportsByArea {
    
    int id;
    String name;
    String description;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public static enum ResultField implements Report.IResultField
	{
		AREA_NAME("areaName",String.class),
		TRANSACTION_COUNT("transactionCount",Integer.class),
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

    public static enum TransactionType implements Transaction.IType
	{
		SELL(Transaction.Type.SELL);

		private Transaction.Type type;

		public Transaction.Type getType()
		{
			return this.type;
		}

		@Override
		public String getCode()
		{
			return this.type.getCode();
		}

		private TransactionType(Transaction.Type type)
		{
			this.type = type;
		}

		private static final Set<String> codeSet = new HashSet<String>();
		private static final Map<String,TransactionType> codeMap = new HashMap<String,TransactionType>();

		public static Set<String> getCodeSet()
		{
			return Collections.unmodifiableSet(codeSet);
		}

		public static Map<String,TransactionType> getCodeMap()
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
				codeMap.put(transactionType.getCode(),transactionType);
			}
		}
	}
		public static enum FilterField implements Report.IFilterField
	{
		TIER_NAME("tierName", String.class, Report.FilterOperator.getStringOperators()),
		GROUP_NAME("groupName", String.class, Report.FilterOperator.getStringOperators()),
		TRANSACTION_TYPE("transactionType", String.class, Report.FilterOperator.getStringOperators());

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

	protected ReportsByArea()
	{
	}


}
