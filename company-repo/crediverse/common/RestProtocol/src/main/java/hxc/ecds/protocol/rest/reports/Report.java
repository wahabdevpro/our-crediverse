package hxc.ecds.protocol.rest.reports;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import hxc.ecds.protocol.rest.util.DateHelper;
import hxc.ecds.protocol.rest.util.TimeInterval;

public class Report
{

	public enum Originator
	{
		USER,
		MINIMUM_REQUIRED_DATA;
	}

	public static enum Type //Max length 50: er_report.type varchar(50)
	{
        RETAILER_PERFORMANCE("retailer_performance"),
        WHOLESALER_PERFORMANCE("wholesaler_performance"),
        						
        SALES_SUMMARY("sales_summary"),
        DAILY_GROUP_SALES("daily_group_sales"),
        DAILY_PERFORMANCE_BY_AREA("daily_performance_by_area"),
        ACCOUNT_BALANCE_SUMMARY("account_balance_summary"),
		MONTHLY_SALES_PERFORMANCE("monthly_sales_performance");
		
		private String pathSegment;

		public String getPathSegment()
		{
			return this.pathSegment;
		}

		private Type(String pathSegment)
		{
			this.pathSegment = pathSegment;
		}	

		private static final Map<String,Type> pathSegmentMap = new HashMap<>();
		public static final Map<String,Type> getPathSegmentMap()
		{
			return pathSegmentMap;
		}

		public static Type fromPathSegment(String pathSegment)
		{
			if (pathSegment == null) throw new NullPointerException("pathSegment may not be null");
			Type result = pathSegmentMap.get(pathSegment);
			if ( result == null ) throw new IllegalArgumentException(String.format("No Type associated with path segment '%s'", pathSegment));
			return result;
		}

		public static boolean hasPathSegment(String pathSegment)
		{
			if (pathSegment == null) throw new NullPointerException("pathSegment may not be null");
			return pathSegmentMap.containsKey(pathSegment);
		}

		static
		{
			for (Type type : values())
			{
				pathSegmentMap.put(type.getPathSegment(), type);
			}
		}
	}

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
			this.symbol = symbol;
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
	};

	public static enum FilterOperator
	{
		LIKE(":", "like", null, null),
		EQUAL("=", "eq", null, null),
		NOT_EQUAL("!=", "ne", new HashSet<>(Arrays.asList("#")), null),
		GREATER_THAN(">", "gt", null, null),
		LESS_THAN("<", "lt", null, null),
		GREATER_THAN_OR_EQUAL(">=", "ge", new HashSet<>(Arrays.asList("≥")), null),
		LESS_THAN_OR_EQUAL("<=", "le", new HashSet<>(Arrays.asList("≤")), null),
		IN("~", "in", null, null);

		////////////////////////////////////////////////////////////////////////////

		private String canonicalSymbol;
		private String canonicalAbbreviation;
		private Set<String> extraSymbols;
		private Set<String> extraAbbreviations;
		private Set<String> symbols;
		private Set<String> abbreviations;
		private Set<String> representations;

		public String getCanonicalSymbol()
		{
			return this.canonicalSymbol;
		}

		public String getCanonicalAbbreviation()
		{
			return this.canonicalAbbreviation;
		}

		public Set<String> getExtraSymbols()
		{
			return this.extraSymbols;
		}

		public Set<String> getExtraAbbreviations()
		{
			return this.extraAbbreviations;
		}

		public Set<String> getSymbols()
		{
			return this.symbols;
		}

		public Set<String> getAbbreviations()
		{
			return this.abbreviations;
		}

		public Set<String> getRepresentations()
		{
			return this.representations;
		}

		////////////////////////////////////////////////////////////////////////////

		private FilterOperator(String canonicalSymbol, String canonicalAbbreviation, Set<String> extraSymbols, Set<String> extraAbbreviations)
		{
			this.canonicalSymbol = canonicalSymbol;
			this.canonicalAbbreviation = canonicalAbbreviation;
			this.extraSymbols = (extraSymbols == null ? new HashSet<String>() : extraSymbols);
			this.extraAbbreviations = (extraAbbreviations == null ? new HashSet<String>() : extraAbbreviations);

			// Calculated
			this.symbols = new HashSet<String>();
			this.symbols.add(this.canonicalSymbol);
			this.symbols.addAll(this.extraSymbols);
			this.abbreviations = new HashSet<String>();
			this.abbreviations.add(this.canonicalAbbreviation);
			this.abbreviations.addAll(this.extraAbbreviations);
			this.representations = new HashSet<String>();
			this.representations.addAll(this.symbols);
			this.representations.addAll(this.abbreviations);
		}

		////////////////////////////////////////////////////////////////////////////

		private static final Map<String,FilterOperator> canonicalSymbolMap = new HashMap<>();
		private static final Map<String,FilterOperator> canonicalAbbreviationMap = new HashMap<>();
		private static final Map<String,FilterOperator> extraSymbolMap = new HashMap<>();
		private static final Map<String,FilterOperator> extraAbbreviationMap = new HashMap<>();
		private static final Map<String,FilterOperator> symbolMap = new HashMap<>();
		private static final Map<String,FilterOperator> abbreviationMap = new HashMap<>();
		private static final Map<String,FilterOperator> representationMap = new HashMap<>();

		private static final String canonicalSymbolRegexString;
		private static final String canonicalAbbreviationRegexString;
		private static final String extraSymbolRegexString;
		private static final String extraAbbreviationRegexString;
		private static final String symbolRegexString;
		private static final String abbreviationRegexString;
		private static final String representationRegexString;

		////////////////////////////////////////////////////////////////////////////

		public static Map<String,FilterOperator> getCanonicalSymbolMap()
		{
			return Collections.unmodifiableMap(canonicalSymbolMap);
		}

		public static Map<String,FilterOperator> getCanonicalAbbreviationMap()
		{
			return Collections.unmodifiableMap(canonicalAbbreviationMap);
		}

		public static Map<String,FilterOperator> getExtraSymbolMap()
		{
			return Collections.unmodifiableMap(extraSymbolMap);
		}

		public static Map<String,FilterOperator> getExtraAbbreviationMap()
		{
			return Collections.unmodifiableMap(extraAbbreviationMap);
		}

		public static Map<String,FilterOperator> getSymbolMap()
		{
			return Collections.unmodifiableMap(symbolMap);
		}

		public static Map<String,FilterOperator> getAbbreviationMap()
		{
			return Collections.unmodifiableMap(abbreviationMap);
		}

		public static Map<String,FilterOperator> getRepresentationMap()
		{
			return Collections.unmodifiableMap(representationMap);
		}

		////////////////////////////////////////////////////////////////////////////

		public static FilterOperator fromCanonicalSymbol(String canonicalSymbol)
			throws IllegalArgumentException
		{
			if (canonicalSymbol == null) throw new NullPointerException("canonicalSymbol may not be null");
			FilterOperator result = canonicalSymbolMap.get(canonicalSymbol);
			if (result == null) throw new IllegalArgumentException(String.format("No FilterOperator associated with canonicalSymbol '%s'", canonicalSymbol));
			return result;
		}

		public static FilterOperator fromCanonicalAbbreviation(String canonicalAbbreviation)
			throws IllegalArgumentException
		{
			if (canonicalAbbreviation == null) throw new NullPointerException("canonicalAbbreviation may not be null");
			FilterOperator result = canonicalAbbreviationMap.get(canonicalAbbreviation);
			if (result == null) throw new IllegalArgumentException(String.format("No FilterOperator associated with canonicalAbbreviation '%s'", canonicalAbbreviation));
			return result;
		}

		public static FilterOperator fromExtraSymbol(String extraSymbol)
			throws IllegalArgumentException
		{
			if (extraSymbol == null) throw new NullPointerException("extraSymbol may not be null");
			FilterOperator result = extraSymbolMap.get(extraSymbol);
			if (result == null) throw new IllegalArgumentException(String.format("No FilterOperator associated with extraSymbol '%s'", extraSymbol));
			return result;
		}

		public static FilterOperator fromExtraAbbreviation(String extraAbbreviation)
			throws IllegalArgumentException
		{
			if (extraAbbreviation == null) throw new NullPointerException("extraAbbreviation may not be null");
			FilterOperator result = extraAbbreviationMap.get(extraAbbreviation);
			if (result == null) throw new IllegalArgumentException(String.format("No FilterOperator associated with extraAbbreviation '%s'", extraAbbreviation));
			return result;
		}

		public static FilterOperator fromSymbol(String symbol)
			throws IllegalArgumentException
		{
			if (symbol == null) throw new NullPointerException("symbol may not be null");
			FilterOperator result = symbolMap.get(symbol);
			if (result == null) throw new IllegalArgumentException(String.format("No FilterOperator associated with symbol '%s'", symbol));
			return result;
		}

		public static FilterOperator fromAbbreviation(String abbreviation)
			throws IllegalArgumentException
		{
			if (abbreviation == null) throw new NullPointerException("abbreviation may not be null");
			FilterOperator result = abbreviationMap.get(abbreviation);
			if (result == null) throw new IllegalArgumentException(String.format("No FilterOperator associated with abbreviation '%s'", abbreviation));
			return result;
		}

		public static FilterOperator fromRepresentation(String representation)
			throws IllegalArgumentException
		{
			if (representation == null) throw new NullPointerException("representation may not be null");
			FilterOperator result = representationMap.get(representation);
			if (result == null) throw new IllegalArgumentException(String.format("No FilterOperator associated with representation '%s'", representation));
			return result;
		}

		////////////////////////////////////////////////////////////////////////////

		public static String getCanonicalSymbolRegexString()
		{
			return canonicalSymbolRegexString;
		}

		public static String getCanonicalAbbreviationRegexString()
		{
			return canonicalAbbreviationRegexString;
		}

		public static String getExtraSymbolRegexString()
		{
			return extraSymbolRegexString;
		}

		public static String getExtraAbbreviationRegexString()
		{
			return extraAbbreviationRegexString;
		}

		public static String getSymbolRegexString()
		{
			return symbolRegexString;
		}

		public static String getAbbreviationRegexString()
		{
			return abbreviationRegexString;
		}

		public static String getRepresentationRegexString()
		{
			return representationRegexString;
		}

		////////////////////////////////////////////////////////////////////////////

		private static final Set<FilterOperator> enumOperators = Collections.unmodifiableSet(new HashSet<FilterOperator>(Arrays.asList(
			FilterOperator.EQUAL,
			FilterOperator.NOT_EQUAL,
			FilterOperator.IN
		)));

		private static final Set<FilterOperator> stringOperators = Collections.unmodifiableSet(new HashSet<FilterOperator>(Arrays.asList(
			FilterOperator.EQUAL,
			FilterOperator.NOT_EQUAL,
			FilterOperator.LIKE
		)));

		private static final Set<FilterOperator> numericOperators = Collections.unmodifiableSet(new HashSet<FilterOperator>(Arrays.asList(
			FilterOperator.EQUAL,
			FilterOperator.NOT_EQUAL,
			FilterOperator.GREATER_THAN,
			FilterOperator.LESS_THAN,
			FilterOperator.GREATER_THAN_OR_EQUAL,
			FilterOperator.LESS_THAN_OR_EQUAL
		)));
		
		private static final Set<FilterOperator> arrayOperators = Collections.unmodifiableSet(new HashSet<FilterOperator>(Arrays.asList(
				FilterOperator.IN
			)));

		public static Set<FilterOperator> getEnumOperators()	
		{
			return enumOperators;
		}

		public static Set<FilterOperator> getStringOperators()	
		{
			return stringOperators;
		}

		public static Set<FilterOperator> getNumericOperators()	
		{
			return numericOperators;
		}
		
		public static Set<FilterOperator> getArrayOperators()	
		{
			return arrayOperators;
		}

		////////////////////////////////////////////////////////////////////////////

		private static String createRegexString(Map<String,FilterOperator> map)
		{
			StringBuilder result = new StringBuilder();
			boolean first = true;
			for (String key : map.keySet())
			{
				if (first) first = false;
				else result.append("|");
				result.append(Pattern.quote(key));
			}
			return result.toString();
		}

		static
		{
			for (FilterOperator filterOperator : values())
			{
				canonicalSymbolMap.put(filterOperator.getCanonicalSymbol(), filterOperator);
				canonicalAbbreviationMap.put(filterOperator.getCanonicalAbbreviation(), filterOperator);
				for (String symbol : filterOperator.getExtraSymbols())
				{
					extraSymbolMap.put(symbol, filterOperator);
				}
				for (String abbreviation : filterOperator.getExtraAbbreviations())
				{
					extraAbbreviationMap.put(abbreviation, filterOperator);
				}
			}
			symbolMap.putAll(canonicalSymbolMap);
			symbolMap.putAll(extraSymbolMap);
			abbreviationMap.putAll(canonicalAbbreviationMap);
			abbreviationMap.putAll(extraAbbreviationMap);
			representationMap.putAll(symbolMap);
			representationMap.putAll(abbreviationMap);

			canonicalSymbolRegexString = createRegexString(canonicalSymbolMap);
			canonicalAbbreviationRegexString = createRegexString(canonicalAbbreviationMap);
			extraSymbolRegexString = createRegexString(extraSymbolMap);
			extraAbbreviationRegexString = createRegexString(extraAbbreviationMap);
			symbolRegexString = createRegexString(symbolMap);
			abbreviationRegexString = createRegexString(abbreviationMap);
			representationRegexString = createRegexString(representationMap);
		}
	};


	public static enum RelativeTimeRange
	{
		PREVIOUS_DAY( true ),
		CURRENT_DAY( false ),
		PREVIOUS_WEEK( true ),
		CURRENT_WEEK( false ),
		PREVIOUS_30DAYS( true ),
		PREVIOUS_MONTH( true ),
		CURRENT_MONTH( false ),
		PREVIOUS_YEAR( true ),
		CURRENT_YEAR( false ),
		PREVIOUS_HOUR( true ),
		CURRENT_HOUR( false ),
		PREVIOUS_HOURS_IN_DAY( true ),
		MONTHS_AGO_2(true),
		MONTHS_AGO_3(true);

		boolean previous;

		public boolean getPrevious()
		{
			return this.previous;
		}

		public boolean isPrevious()
		{
			return this.previous;
		}

		private RelativeTimeRange(boolean previous)
		{
			this.previous = previous;
		}

		public TimeInterval resolve(Date reference)
		{
			return resolve(this, reference);
		}

		public TimeInterval resolve()
		{
			return resolve(this);
		}

		public TimeInterval resolve(Calendar reference)
		{
			return resolve(this, reference);
		}

		public static TimeInterval resolve(RelativeTimeRange realtiveTimeRange)
		{
			Calendar calendarReference = Calendar.getInstance();
			return resolve(realtiveTimeRange, calendarReference);
		}

		public static TimeInterval resolve(RelativeTimeRange realtiveTimeRange, Date reference)
		{
			Calendar calendarReference = Calendar.getInstance();
			calendarReference.setTime(reference);
			return resolve(realtiveTimeRange, calendarReference);
		}

		public static TimeInterval resolve(RelativeTimeRange realtiveTimeRange, Calendar reference)
		{
			switch(realtiveTimeRange)
			{
				// TODO --- MONTHS AGO 3 and MONTHS AGO 2 do not have conversions below ... 
				case MONTHS_AGO_3:
				{
					throw new IllegalArgumentException(String.format("Invalid realtiveTimeRange %s", realtiveTimeRange));
				}
				case MONTHS_AGO_2:
				{
					throw new IllegalArgumentException(String.format("Invalid realtiveTimeRange %s", realtiveTimeRange));
				}
				case CURRENT_DAY:
				{
					return new TimeInterval(DateHelper.startOf(reference,Calendar.DAY_OF_MONTH), DateHelper.endOf(reference,Calendar.DAY_OF_MONTH));
				}
				case PREVIOUS_DAY:
				{
					Calendar calendar = (Calendar)reference.clone();
					calendar.add(Calendar.DAY_OF_MONTH, -1);
					return new TimeInterval(DateHelper.startOf(calendar,Calendar.DAY_OF_MONTH), DateHelper.endOf(calendar,Calendar.DAY_OF_MONTH));
				}
				case CURRENT_WEEK:
				{
					return new TimeInterval(DateHelper.startOf(reference,Calendar.WEEK_OF_YEAR), DateHelper.endOf(reference,Calendar.WEEK_OF_YEAR));
				}
				case PREVIOUS_WEEK:
				{
					Calendar calendar = (Calendar)reference.clone();
					calendar.add(Calendar.DAY_OF_MONTH, -7);
					return new TimeInterval(DateHelper.startOf(calendar,Calendar.WEEK_OF_YEAR), DateHelper.endOf(calendar,Calendar.WEEK_OF_YEAR));
				}
				case PREVIOUS_30DAYS:
				{
					Calendar end = (Calendar)reference.clone();
					end.add(Calendar.DAY_OF_MONTH, -1);

					Calendar start = (Calendar)end.clone();
					start.add(Calendar.DAY_OF_MONTH, -29);
					return new TimeInterval(start, end);
				}
				case CURRENT_MONTH:
				{
					return new TimeInterval(DateHelper.startOf(reference,Calendar.MONTH), DateHelper.endOf(reference,Calendar.MONTH));
				}
				case PREVIOUS_MONTH:
				{
					Calendar calendar = (Calendar)reference.clone();
					calendar.add(Calendar.MONTH, -1);
					return new TimeInterval(DateHelper.startOf(calendar,Calendar.MONTH), DateHelper.endOf(calendar,Calendar.MONTH));
				}
				case CURRENT_YEAR:
				{
					return new TimeInterval(DateHelper.startOf(reference,Calendar.YEAR), DateHelper.endOf(reference,Calendar.YEAR));
				}
				case PREVIOUS_YEAR:
				{
					Calendar calendar = (Calendar)reference.clone();
					calendar.add(Calendar.YEAR, -1);
					return new TimeInterval(DateHelper.startOf(calendar,Calendar.YEAR), DateHelper.endOf(calendar,Calendar.YEAR));
				}
				case CURRENT_HOUR:
				{
					return new TimeInterval(DateHelper.startOf(reference,Calendar.HOUR_OF_DAY), DateHelper.endOf(reference,Calendar.HOUR_OF_DAY));
				}
				case PREVIOUS_HOUR:
				{
					Calendar calendar = (Calendar)reference.clone();
					calendar.add(Calendar.HOUR_OF_DAY, -1);
					return new TimeInterval(DateHelper.startOf(calendar,Calendar.HOUR_OF_DAY), DateHelper.endOf(calendar,Calendar.HOUR_OF_DAY));
				}
				case PREVIOUS_HOURS_IN_DAY:
				{
					Calendar calendar = (Calendar)reference.clone();
					calendar.add(Calendar.HOUR_OF_DAY, -1);
					return new TimeInterval(DateHelper.startOf(calendar,Calendar.DAY_OF_MONTH), DateHelper.endOf(calendar,Calendar.HOUR_OF_DAY));
				}
			}
			throw new IllegalArgumentException(String.format("Invalid realtiveTimeRange %s", realtiveTimeRange));
		}
	}

	/*
	public static interface IFilterFieldFactory
	{
		public Class<? extends IFilterField> getFilterFieldClass();
		public IFilterField fromIdentifier(String identifier) throws NullPointerException, IllegalArgumentException;
	}
	*/

	public static interface IResultField
	{
		public String getIdentifier();
		public Class<?> getType();
	}

	public static interface IFilterField
	{
		public String getIdentifier();
		public Class<?> getType();
		public Set<? extends FilterOperator> getAllowedOperators();
	}

	/*
	public static interface IFilterFieldValueFactory<ValueType extends Object>
	{
		public Class<ValueType> getFilterFieldValueClass();
		public ValueType fromString(String value) throws Exception;
	}

	public static class ValueOfFilterFieldValueFactory<ValueType extends Object>
	{
		public Class<ValueType> getFilterFieldValueClass(){ return ValueType.class }
		public ValueType fromString(String value) throws Exception
		{
			
		}
	}
	*/

	public static interface ISortItem<FieldType extends IResultField>
	{
		public FieldType getField();
		public ISortItem<? extends IResultField> setField(FieldType field);

		public SortOperator getOperator();
		public ISortItem<? extends IResultField> setOperator(SortOperator operator);
	}

	public static class SortItem<FieldType extends IResultField>
		implements ISortItem<FieldType>
	{
		private FieldType field;
		private SortOperator operator;

		@Override
		public FieldType getField()
		{
			return this.field;
		}

		@Override
		public SortItem<? extends IResultField> setField(FieldType field)
		{
			this.field = field;
			return this;
		}

		@Override
		public SortOperator getOperator()
		{
			return this.operator;
		}

		@Override
		public SortItem<? extends IResultField> setOperator(SortOperator operator)
		{
			this.operator = operator;
			return this;
		}

		public SortItem()
		{
		}

		public SortItem(FieldType field, SortOperator operator)
		{
			this.field = field;
			this.operator = operator;
		}

		public String describe(String extra)
		{
			return String.format("%s@%s(field = %s, operator = %s%s%s)",
				this.getClass().getName(), Integer.toHexString(this.hashCode()),
				field, operator,
				(extra.isEmpty() ? "" : ", "), extra);
		}

		public String describe()
		{
			return this.describe("");
		}

		public String toString()
		{
			return this.describe();
		}
	}

	public static interface ISort<ItemType extends ISortItem>
	{
		public List<? extends ItemType> getItems();
	}

	public static class Sort<ItemType extends ISortItem>
		implements ISort<ItemType>
	{
		private List<? extends ItemType> items;

		@Override
		public List<? extends ItemType> getItems()
		{
			return this.items;
		}

		public Sort()
		{
		}

		public Sort(List<? extends ItemType> items)
		{
			this.items = items;
		}

		public String describe(String extra)
		{
			return String.format("%s@%s(items = %s%s%s)",
				this.getClass().getName(), Integer.toHexString(this.hashCode()),
				items,
				(extra.isEmpty() ? "" : ", "), extra);
		}

		public String describe()
		{
			return this.describe("");
		}

		public String toString()
		{
			return this.describe();
		}
	}

	public static interface IFilterItem<FieldType extends IFilterField, ValueType extends Object>
	{
		public FieldType getField();
		public IFilterItem<FieldType, ValueType> setField(FieldType field);

		public FilterOperator getOperator();
		public IFilterItem<FieldType, ValueType> setOperator(FilterOperator operator);

		public ValueType getValue();
		public IFilterItem<FieldType, ValueType> setValue(ValueType value);
	}

	public static class FilterItem<FieldType extends IFilterField, ValueType extends Object>
		implements IFilterItem<FieldType, ValueType>
	{
		private FieldType field;
		private FilterOperator operator;
		private ValueType value;

		@Override
		public FieldType getField()
		{
			return this.field;
		}

		@Override
		public FilterItem<FieldType, ValueType> setField(FieldType field)
		{
			this.field = field;
			return this;
		}

		@Override
		public FilterOperator getOperator()
		{
			return this.operator;
		}

		@Override
		public FilterItem<FieldType, ValueType> setOperator(FilterOperator operator)
		{
			this.operator = operator;
			return this;
		}

		@Override
		public ValueType getValue()
		{
			return this.value;
		}

		@Override
		public FilterItem<FieldType, ValueType> setValue(ValueType value)
		{
			this.value = value;
			return this;
		}

		public FilterItem()
		{
		}

		public FilterItem(FieldType field, FilterOperator operator, ValueType value)
		{
			this.field = field;
			this.operator = operator;
			this.value = value;
		}

		public String describe(String extra)
		{
			return String.format("%s@%s(field = %s, operator = %s, value = %s%s%s)",
				this.getClass().getName(), Integer.toHexString(this.hashCode()),
				field, operator, value,
				(extra.isEmpty() ? "" : ", "), extra);
		}

		public String describe()
		{
			return this.describe("");
		}

		public String toString()
		{
			return this.describe();
		}
	};

	public static interface IFilter<ItemType extends FilterItem>
	{
		public List<? extends ItemType> getItems();
	}

	public static class Filter<ItemType extends FilterItem>
		implements IFilter<ItemType>
	{
		private List<? extends ItemType> items;

		@Override
		public List<? extends ItemType> getItems()
		{
			return this.items;
		}

		public Filter()
		{
		}

		public Filter(List<? extends ItemType> items)
		{
			this.items = items;
		}

		public String describe(String extra)
		{
			return String.format("%s@%s(items = %s%s%s)",
				this.getClass().getName(), Integer.toHexString(this.hashCode()),
				items,
				(extra.isEmpty() ? "" : ", "), extra);
		}

		public String describe()
		{
			return this.describe("");
		}

		public String toString()
		{
			return this.describe();
		}
	}

	protected Report()
	{
	}
}
