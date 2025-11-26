package hxc.services.ecds.reports;

import static hxc.ecds.protocol.rest.Transaction.Type.NON_AIRTIME_DEBIT;
import static hxc.ecds.protocol.rest.Transaction.Type.NON_AIRTIME_REFUND;
import static hxc.ecds.protocol.rest.Transaction.Type.REVERSE;
import static hxc.ecds.protocol.rest.Transaction.Type.REVERSE_PARTIALLY;
import static hxc.ecds.protocol.rest.Transaction.Type.SELF_TOPUP;
import static hxc.ecds.protocol.rest.Transaction.Type.SELL;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.fasterxml.jackson.databind.ObjectMapper;

import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportParameters;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportResult;
import hxc.ecds.protocol.rest.util.TimeInterval;
import hxc.services.ecds.olapmodel.OlapTransaction;

public class MonthlySalesPerformanceReport
	extends hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReport
{
	public static class ResultEntry extends hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportResultEntry
	{
		public ResultEntry(String month, String groupName, String ownerMsisdn, String msisdn, BigDecimal totalAmount)
		{
			super();
			this.setMonth(month);
			this.setGroupName(groupName);
			this.setOwnerMsisdn(ownerMsisdn);
			this.setMsisdn(msisdn);
			this.setTotalAmount(totalAmount);
		}
	}

	public static class GroupCountResultEntry 
	{
		private String groupName;
		private Integer agentTotalCount;

		public GroupCountResultEntry()
		{
		}

		public GroupCountResultEntry(String groupName, long agentTotalCount)
		{
			super();
			this.setGroupName(groupName);
			this.setAgentTotalCount(Long.valueOf(agentTotalCount).intValue());
		}
	
		public String getGroupName()
		{
			return this.groupName;
		}
	
		public GroupCountResultEntry setGroupName(String groupName)
		{
			this.groupName = groupName;
			return this;
		}
		
		public Integer getAgentTotalCount()
		{
			return this.agentTotalCount;
		}
	
		public GroupCountResultEntry setAgentTotalCount(Integer agentTotalCount)
		{
			this.agentTotalCount = agentTotalCount;
			return this;
		}
	}

	public static class FilterFieldFactory implements Report.IFilterFieldFactory
	{
		private static FilterFieldFactory INSTANCE = new FilterFieldFactory();

		@Override
		public Class<FilterField> getFilterFieldClass()
		{
			return FilterField.class;
		}

		@Override
		public FilterField fromIdentifier(String identifier) throws Exception
		{
			return FilterField.fromIdentifier(identifier);
		}

		public static FilterFieldFactory getInstance()
		{
			return INSTANCE;
		}
	}

	public static class ResultFieldFactory implements Report.IResultFieldFactory
	{
		private static ResultFieldFactory INSTANCE = new ResultFieldFactory();

		@Override
		public Class<ResultField> getResultFieldClass()
		{
			return ResultField.class;
		}

		@Override
		public ResultField fromIdentifier(String identifier) throws Exception
		{
			return ResultField.fromIdentifier(identifier);
		}

		public static ResultFieldFactory getInstance()
		{
			return INSTANCE;
		}
	}

	public static class FilterFieldValueFactory implements Report.IFilterFieldValueFactory
	{
		private static FilterFieldValueFactory INSTANCE = new FilterFieldValueFactory();

		@Override
		public Class<FilterField> getFilterFieldClass()
		{
			return FilterField.class;
		}

		@Override
		public Object fromString(Report.IFilterField iFilterField, String value)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IllegalArgumentException
		{
			if (!(iFilterField instanceof FilterField))
			{
				throw new IllegalArgumentException(String.format("iFilterField must be of type %s but is %s",
					FilterField.class.getCanonicalName(), iFilterField.getClass().getCanonicalName()));
			}
			FilterField filterField = (FilterField) iFilterField;
			if (filterField.getType().isAssignableFrom(String.class))
			{
				return value;
			}
			else if (filterField.getType().isAssignableFrom(BigDecimal.class))
			{
				return new BigDecimal(value);
			}
			else if (filterField.getType().isAssignableFrom(String[].class))
			{
				ObjectMapper mapper = new ObjectMapper();
				try {
					return mapper.readValue(value, String[].class);
				} catch (IOException e) {
					throw new IllegalArgumentException(e);
				}
			}
			switch (filterField)
			{
				case TRANSACTION_TYPES:
					if (TransactionType.hasCode(value)) return TransactionType.fromCode(value);
					return TransactionType.valueOf(value);
				default:
					return filterField.getType().getDeclaredMethod("valueOf", String.class).invoke(null, value);
			}
			//return filterField.getType().getDeclaredMethod("valueOf", String.class).invoke(null, value);
		}

		public static FilterFieldValueFactory getInstance()
		{
			return INSTANCE;
		}
	}

	public static Filter parseFilterString(String filterString) throws Exception
	{
		List<ReportHelper.IntermediateFilterItem> intermediateFilterItems = ReportHelper.parseFilterStringIntermediate(filterString);
		FilterFieldFactory fieldFactory = FilterFieldFactory.getInstance();
		FilterFieldValueFactory fieldValueFactory = FilterFieldValueFactory.getInstance();

		List<FilterItem<? extends Object>> items = new ArrayList<FilterItem<? extends Object>>();
		Filter result = new Filter(items);
		for (ReportHelper.IntermediateFilterItem item : intermediateFilterItems)
		{
			FilterField field = fieldFactory.fromIdentifier(item.getFieldIdentifier());
			Report.FilterOperator operator = item.getOperator();
			Object value = fieldValueFactory.fromString(field, item.getValueString());
			if (!field.getAllowedOperators().contains(operator))
			{
				throw new IllegalArgumentException(String.format("Operator %s is not valid for field %s", operator, field));
			}
			items.add(new FilterItem<Object>(field, operator, value));
		}
		return result;
	}

	public static Sort parseSortString(String filterString) throws Exception
	{
		List<ReportHelper.IntermediateSortItem> intermediateSortItems = ReportHelper.parseSortStringIntermediate(filterString);
		ResultFieldFactory fieldFactory = ResultFieldFactory.getInstance();

		List<SortItem> items = new ArrayList<SortItem>();
		//List<? extends Report.ISortItem<? extends Report.IResultField>> items = result.getItems();
		for (ReportHelper.IntermediateSortItem item : intermediateSortItems)
		{
			ResultField field = fieldFactory.fromIdentifier(item.getFieldIdentifier());
			Report.SortOperator operator = item.getOperator();
			//Report.ISortItem<? extends Report.IResultField> sortItem = new SortItem(field, operator);
			//items.add(sortItem);
			items.add(new SortItem(field, operator));
		}
		Sort result = new Sort(items);
		return result;
	}

	public static class Processor
	{
		private EntityManager oltpEm;
		private EntityManager em;
		private int companyID;
		private Filter filter;
		private Sort sort;

		private TimeInterval timeInterval;
		private Date relativeTimeRangeReference;
		private Report.RelativeTimeRange relativeTimeRange;
		private CriteriaBuilder criteriaBuilder;
		private CriteriaQuery<ResultEntry> criteriaQuery;
		private Root<OlapTransaction> root;

		private List<Predicate> wherePredicates;
		private List<Predicate> havingPredicates;
		private List<Order> orderList;
		private Expression<Number> rootGet;

		public EntityManager getEm()
		{
			return this.em;
		}

		public TimeInterval getTimeInterval()
		{
			return this.timeInterval;
		}

		public Report.RelativeTimeRange getRelativeTimeRange()
		{
			return relativeTimeRange;
		}

		public Filter getFilter()
		{
			return this.filter;
		}

		public Sort getSort()
		{
			return this.sort;
		}

		public String describe(String extra)
		{
			return String.format("%s@%s(filter = %s, sort = %s%s%s)",
				this.getClass().getName(), Integer.toHexString(this.hashCode()),
				filter, sort, 
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

		public Processor(EntityManager oltpEm, EntityManager em, int companyID, Sort sort, Filter filter, TimeInterval timeInterval)
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
			this.filter = filter;
			this.sort = sort;
			this.timeInterval = timeInterval;
		}

		public Processor(EntityManager em, String sortString, String filterString, String timeIntervalStart, String timeIntervalEnd, String relativeTimeRangeCode, String relativeTimeRangeReferenceString) throws Exception
		{
			this.em = em;
			this.setParameters(sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
		}

		public Processor(EntityManager em, MonthlySalesPerformanceReportParameters parameters, Date relativeTimeRangeReference) throws Exception
		{
			this.em = em;
			this.setParameters(parameters, relativeTimeRangeReference);
		}
		
		public Processor(EntityManager em, MonthlySalesPerformanceReportParameters parameters, String relativeTimeRangeReferenceString) throws Exception
		{
			this.em = em;
			this.setParameters(parameters, relativeTimeRangeReferenceString);
		}
		
		public void setParameters(MonthlySalesPerformanceReportParameters parameters, String relativeTimeRangeReferenceString) throws Exception
		{
			Date relativeTimeRangeReference = null;
			if ( relativeTimeRangeReferenceString != null )
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
				relativeTimeRangeReference = sdf.parse(relativeTimeRangeReferenceString);
			}
			this.setParameters(parameters, relativeTimeRangeReference);
		}

		public void setParameters(MonthlySalesPerformanceReportParameters parameters, Date relativeTimeRangeReference) throws Exception
		{
			this.filter = parameters.getFilter();
			this.sort = parameters.getSort();
			if ( parameters.getRelativeTimeRange() != null )
			{
				this.relativeTimeRange = parameters.getRelativeTimeRange();
				this.timeInterval = this.relativeTimeRange.resolve(relativeTimeRangeReference != null ? relativeTimeRangeReference : new Date());
			}
			else {
				this.timeInterval = parameters.getTimeInterval();
			}
		}

		public void setParameters(String sortString, String filterString, String timeIntervalStart, String timeIntervalEnd, String relativeTimeRangeCode, String relativeTimeRangeReferenceString) throws Exception
		{
			if (filterString != null)
			{
				this.filter = MonthlySalesPerformanceReport.parseFilterString("+" + filterString);
			}
			if (sortString != null)
			{
				this.sort = MonthlySalesPerformanceReport.parseSortString(sortString);
			}
			this.timeInterval = new TimeInterval();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
			if (relativeTimeRangeCode != null)
			{
				this.relativeTimeRange = Report.RelativeTimeRange.valueOf(relativeTimeRangeCode);
				this.timeInterval = this.relativeTimeRange.resolve(relativeTimeRangeReferenceString != null ? sdf.parse(relativeTimeRangeReferenceString) : new Date());
			}
			else
			{
				if (timeIntervalStart != null) this.timeInterval.setStartDate(sdf.parse(timeIntervalStart));
				if (timeIntervalEnd != null) this.timeInterval.setEndDate(sdf.parse(timeIntervalEnd));
			}
		}

		@SuppressWarnings({"unchecked"})
		private Predicate process(Expression<? extends Object> expression, Report.FilterOperator operator, Object value) throws IllegalArgumentException
		{
			switch(operator)
			{
				case LIKE:
					return criteriaBuilder.like((Expression<String>)expression, (String)value);
				case EQUAL:
					return criteriaBuilder.equal(expression, value);
				case NOT_EQUAL:
					return criteriaBuilder.notEqual(expression, value);
				case GREATER_THAN:
					return criteriaBuilder.greaterThan((Expression<? extends Comparable<? super Comparable<? extends Object>>>)expression, (Comparable<? super Comparable<? extends Object>>)value);
				case LESS_THAN:
					return criteriaBuilder.lessThan((Expression<? extends Comparable<? super Comparable<? extends Object>>>)expression, (Comparable<? super Comparable<? extends Object>>)value);
				case GREATER_THAN_OR_EQUAL:
					return criteriaBuilder.greaterThanOrEqualTo((Expression<? extends Comparable<? super Comparable<? extends Object>>>)expression, (Comparable<? super Comparable<? extends Object>>)value);
				case LESS_THAN_OR_EQUAL:
					return criteriaBuilder.lessThanOrEqualTo((Expression<? extends Comparable<? super Comparable<? extends Object>>>)expression, (Comparable<? super Comparable<? extends Object>>)value);
				case IN:
				{
					if(value instanceof Object[])
					{
						Object[] values = (Object[])value;
						return expression.in(Arrays.asList(Arrays.copyOf(values, values.length, String[].class)));
					} else if(value instanceof Collection<?>)
					{
						Collection<?> values = (Collection<?>)value;
						return expression.in(values);
					} else 
						throw new IllegalArgumentException(String.format("IN report operator not used upon a java.util.Collection. Value toString: [%s], class name: [%s] ", value.toString(), value.getClass().getName()));
				}
			}
			throw new IllegalArgumentException(String.format("operator %s not handled", operator));
		}

		private Expression<Number> nonAirtimeNetSalesPredicate(Root<OlapTransaction> olapTrn, CriteriaBuilder cb) {
			return cb.<Number>selectCase().
					when(cb.
								 and(cb.equal(olapTrn.get("success"), true),
									 cb.equal(olapTrn.get("type"), NON_AIRTIME_DEBIT.getCode())
								 ),
						 olapTrn.get("amount")
					).
					when(cb.
								 and(cb.equal(olapTrn.get("success"), true),
									 cb.equal(olapTrn.get("type"), NON_AIRTIME_REFUND.getCode())
								 ),
						 cb.neg(olapTrn.get("amount"))
					).otherwise(0);
		}

		private Expression<Number> netSalesPredicate(Root<OlapTransaction> olapTrn, CriteriaBuilder cb) {
			return cb.<Number>selectCase().
					when(cb.
								 and(cb.equal(olapTrn.get("success"), true),
									 cb.or(cb.equal(olapTrn.get("type"), SELF_TOPUP.getCode()),
										   cb.equal(olapTrn.get("type"), SELL.getCode()),
										   cb.equal(olapTrn.get("type"), REVERSE_PARTIALLY.getCode()),
										   cb.equal(olapTrn.get("type"), REVERSE.getCode()),
										   cb.equal(olapTrn.get("type"), NON_AIRTIME_DEBIT.getCode())
									 )
								 ),
						 olapTrn.get("amount")
					).
					when(cb.
								 and(cb.equal(olapTrn.get("success"), true),
									 cb.equal(olapTrn.get("type"), NON_AIRTIME_REFUND.getCode())
								 ),
						 cb.neg(olapTrn.get("amount"))
					).otherwise(0);
		}

		private void process(Report.IFilterItem<? extends Report.IFilterField,? extends Object> filterItem) throws Exception
		{
			FilterField field = (FilterField)filterItem.getField();
			switch(field)
			{
				case TRANSACTION_TYPES:
				{
					if (filterItem.getValue() instanceof TransactionType
							&& filterItem.getValue() == TransactionType.NON_AIRTIME_NET_SALES) {
						rootGet = nonAirtimeNetSalesPredicate(root, criteriaBuilder);
					} else if (filterItem.getValue() instanceof TransactionType
							&& filterItem.getValue() == TransactionType.NET_SALES) {
						rootGet = netSalesPredicate(root, criteriaBuilder);
					} else {
						Object value = filterItem.getValue();
						Set<String> effectiveValue = null;
						if (value instanceof TransactionType) {
							effectiveValue = ((TransactionType) filterItem.getValue()).getCode();
						} else if (value instanceof String) {
							TransactionType transactionType = (TransactionType) FilterFieldValueFactory.getInstance().fromString(field, (String) value);
							effectiveValue = transactionType.getCode();
						} else {
							throw new IllegalArgumentException(String.format("TRANSACTION_TYPE value %s not acceptable", value));
						}
						this.wherePredicates.add(this.process(root.get("type"), filterItem.getOperator(), effectiveValue));
					}
				}
				break;
				case TRANSACTION_STATUS:
					this.wherePredicates.add(this.process(root.get("success"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case TIERS:
					this.wherePredicates.add(this.process(root.get("a_TierName"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case GROUPS:
					this.wherePredicates.add(this.process(root.get("a_GroupName"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case AGENTS:
					this.wherePredicates.add(this.process(root.get("a_MSISDN"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case OWNER_AGENTS:
					this.wherePredicates.add(this.process(root.get("a_OwnerMobileNumber"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case PERIOD:
					break;
				default:
					throw new IllegalArgumentException(String.format("FilterField %s not handled", filterItem.getField()));
			}
		}

		/*private Order process(Expression<? extends Object> expression, Report.SortOperator operator) throws IllegalArgumentException
		{
			switch(operator)
			{
				case ASCENDING:
					return criteriaBuilder.asc(expression);
				case DESCENDING:
					return criteriaBuilder.desc(expression);
			}
			throw new IllegalArgumentException(String.format("operator %s not handled", operator));
		}*/

		public CriteriaQuery<ResultEntry> criteriaQuery() throws Exception
		{
			criteriaBuilder = this.em.getCriteriaBuilder();
			criteriaQuery = this.criteriaBuilder.createQuery(ResultEntry.class);
			root = this.criteriaQuery.from(OlapTransaction.class);
			rootGet = root.get("amount");	// Changed later in this.process() for some cases
			wherePredicates = new ArrayList<Predicate>();
			wherePredicates.add(criteriaBuilder.equal(root.get("followUp"),"None"));

			if (filter != null)
			{
				for (Report.IFilterItem<? extends FilterField,? extends Object> filterItem : filter.getItems())
				{
					this.process(filterItem);
				}
			}

            if (this.timeInterval.getStartDate() != null)
            {
                wherePredicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.<Date>get("endDate"),
                    criteriaBuilder.function("date",Date.class,criteriaBuilder.literal(timeInterval.getStartDate()))
                ));
                wherePredicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.<Date>get("endTime"),
                    criteriaBuilder.function("time",Date.class,criteriaBuilder.literal(timeInterval.getStartDate()))
                ));
            }
            if (this.timeInterval.getEndDate() != null)
            {
                wherePredicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.<Date>get("endDate"),
                    criteriaBuilder.function("date",Date.class,criteriaBuilder.literal(timeInterval.getEndDate()))
                ));
                wherePredicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.<Date>get("endTime"),
                    criteriaBuilder.function("time",Date.class,criteriaBuilder.literal(timeInterval.getEndDate()))
                ));
            }
			if (wherePredicates.size() > 0) 
				criteriaQuery.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));

			criteriaQuery.groupBy(
					root.get("a_GroupName"),
					root.get("a_OwnerMobileNumber"),
					root.get("a_MSISDN"),
					criteriaBuilder.function("date_format", String.class, root.get("endDate"), criteriaBuilder.literal("%Y-%m"))
			);
			criteriaQuery.select(criteriaBuilder.construct(ResultEntry.class,
				criteriaBuilder.function("date_format", String.class, root.get("endDate"), criteriaBuilder.literal("%Y-%m")),
				root.get("a_GroupName"),
				root.get("a_OwnerMobileNumber"),
				root.get("a_MSISDN"),
				criteriaBuilder.sum(rootGet)
			));
			return criteriaQuery;
		}

		protected List<MonthlySalesPerformanceReport.ResultEntry> postprocessResult( List<MonthlySalesPerformanceReport.ResultEntry> entries, int first, int max )
		{
			return entries;
		}

		public MonthlySalesPerformanceReportResult result(int first, int max) throws Exception
		{
			MonthlySalesPerformanceReportResult result = new MonthlySalesPerformanceReportResult();
			if (first == 0 && max == 0)
			{
				result.setEntries(new ArrayList<MonthlySalesPerformanceReport.ResultEntry>());
			}
			else
			{
				CriteriaQuery<MonthlySalesPerformanceReport.ResultEntry> criteriaQuery = this.criteriaQuery();
				TypedQuery<MonthlySalesPerformanceReport.ResultEntry> typedQuery = this.em.createQuery(criteriaQuery);
				List<MonthlySalesPerformanceReport.ResultEntry> entries = typedQuery.getResultList();
				entries = postprocessResult(entries, first, max);
				result.setEntries(entries);
				result.setFound( ( max > 0 ) ? ( first + ( entries.size() >= max ? entries.size() + 1 : entries.size() ) ) : ( first + entries.size() ) );
			}
			return result;
		}

		public List<MonthlySalesPerformanceReport.ResultEntry> entries(int first, int max) throws Exception
		{
			List<MonthlySalesPerformanceReport.ResultEntry> entries = null;
			if (first == 0 && max == 0)
			{
				entries = new ArrayList<MonthlySalesPerformanceReport.ResultEntry> ();
			}
			else
			{
				CriteriaQuery<MonthlySalesPerformanceReport.ResultEntry> criteriaQuery = this.criteriaQuery();
				TypedQuery<MonthlySalesPerformanceReport.ResultEntry> typedQuery = this.em.createQuery(criteriaQuery);
				entries = postprocessResult(typedQuery.getResultList(), first, max);
			}
			return entries;
		}
	}

	public static class CsvExportProcessor extends hxc.services.ecds.rest.batch.CsvExportProcessor<MonthlySalesPerformanceReport.ResultEntry>
	{
		private static final String[] HEADINGS = new String[] {
			"MOIS",
			"GROUPE_CABINE",
			"MASTER",
			"E.CABINES",
			"TOTAL VENTES"
		};

		public CsvExportProcessor(int first)
		{
			super(HEADINGS, first, false);
		}

		@Override
		protected void write(MonthlySalesPerformanceReport.ResultEntry record)
		{
			put("MOIS", record.getMonth());
			put("GROUPE_CABINE", record.getGroupName());
			put("MASTER", record.getOwnerMsisdn());
			put("E.CABINES", record.getMsisdn());
			put("TOTAL VENTES", record.getTotalAmount());
		}
	}
}
