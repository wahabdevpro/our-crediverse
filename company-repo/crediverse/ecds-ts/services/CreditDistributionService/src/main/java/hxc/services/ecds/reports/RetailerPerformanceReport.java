package hxc.services.ecds.reports;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportParameters;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportResult;
import hxc.ecds.protocol.rest.util.TimeInterval;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.olapmodel.OlapTransaction;

public class RetailerPerformanceReport
	extends hxc.ecds.protocol.rest.reports.RetailerPerformanceReport
{
	final static Logger logger = LoggerFactory.getLogger(RetailerPerformanceReport.class);
	public static class ResultEntry extends hxc.ecds.protocol.rest.reports.RetailerPerformanceReportResultEntry
	{
		public ResultEntry(Date date, String transactionType, Boolean transactionStatus, String followUp, 
			Integer a_AgentID, String a_AccountNumber, String a_MobileNumber, String a_IMEI, String a_IMSI, String a_Name,
			String a_TierName, String a_GroupName, String a_ServiceClassName,
			String a_OwnerImsi, String a_OwnerMobileNumber, String a_OwnerName,
			BigDecimal totalAmount, BigDecimal totalBonus, long transactionCount)
		{
			super();
			this.setDate(date);
			this.setTransactionType(transactionType);
			this.setTransactionStatus(transactionStatus);
			this.setFollowUp(followUp);

			this.setA_AgentID(a_AgentID == null ? -1 : a_AgentID);
			this.setA_AccountNumber(a_AccountNumber);
			this.setA_MobileNumber(a_MobileNumber);
			this.setA_IMEI(a_IMEI);
			this.setA_IMSI(a_IMSI);
			this.setA_Name(a_Name);

			this.setA_TierName(a_TierName);
			this.setA_GroupName(a_GroupName);
			this.setA_ServiceClassName(a_ServiceClassName);
			this.setA_OwnerImsi(a_OwnerImsi);
			this.setA_OwnerMobileNumber(a_OwnerMobileNumber);
			this.setA_OwnerName(a_OwnerName);

			this.setTotalAmount(totalAmount);
			this.setTotalBonus(totalBonus);
			this.setTransactionCount(Long.valueOf(transactionCount).intValue());
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
			switch (filterField)
			{
				case TRANSACTION_TYPE:
					if (TransactionType.hasCode(value)) return TransactionType.fromCode(value);
					return TransactionType.valueOf(value);
				default:
					return filterField.getType().getDeclaredMethod("valueOf", String.class).invoke(null, value);
			}
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
		private EntityManager em;
		private Integer agentID;
		private Filter filter;
		private Sort sort;
		private TimeInterval timeInterval;
		private Report.RelativeTimeRange relativeTimeRange;

		private CriteriaBuilder criteriaBuilder;
		private CriteriaQuery<ResultEntry> criteriaQuery;
		private Root<OlapTransaction> root;

		private List<Predicate> wherePredicates;
		private List<Predicate> havingPredicates;
		//Not used
		//private List<Order> orderList;

		public EntityManager getEm()
		{
			return this.em;
		}

		public Filter getFilter()
		{
			return this.filter;
		}

		public Sort getSort()
		{
			return this.sort;
		}

		public TimeInterval getTimeInterval()
		{
			return this.timeInterval;
		}

		public Report.RelativeTimeRange getRelativeTimeRange()
		{
			return relativeTimeRange;
		}

		public String describe(String extra)
		{
			return String.format("%s@%s(filter = %s, sort = %s, timeInterval = %s%s%s)",
				this.getClass().getName(), Integer.toHexString(this.hashCode()),
				filter, sort, timeInterval,
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

		public Processor(EntityManager em, Integer agentID, Sort sort, Filter filter, TimeInterval timeInterval)
		{
			this.em = em;
			this.agentID = agentID;
			this.filter = filter;
			this.sort = sort;
			this.timeInterval = timeInterval;
		}

		public Processor(EntityManager em, Integer agentID, String sortString, String filterString, String timeIntervalStart, String timeIntervalEnd, String relativeTimeRangeCode, String relativeTimeRangeReferenceString) throws Exception
		{
			this.em = em;
			this.agentID = agentID;
			this.setParameters(sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
		}

		public Processor(EntityManager em, Integer agentID, RetailerPerformanceReportParameters parameters, Date relativeTimeRangeReference) throws Exception
		{
			this.em = em;
			this.agentID = agentID;
			this.setParameters(parameters, relativeTimeRangeReference);
		}

		public Processor(EntityManager em, Integer agentID, RetailerPerformanceReportParameters parameters, String relativeTimeRangeReferenceString) throws Exception
		{
			this.em = em;
			this.agentID = agentID;
			this.setParameters(parameters, relativeTimeRangeReferenceString);
		}

		public void setParameters(RetailerPerformanceReportParameters parameters, String relativeTimeRangeReferenceString) throws Exception
		{
			Date relativeTimeRangeReference = null;
			if ( relativeTimeRangeReferenceString != null )
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
				relativeTimeRangeReference = sdf.parse(relativeTimeRangeReferenceString);
			}
			this.setParameters(parameters, relativeTimeRangeReference);
		}

		public void setParameters(RetailerPerformanceReportParameters parameters, Date relativeTimeRangeReference) throws Exception
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
				this.filter = RetailerPerformanceReport.parseFilterString("+" + filterString);
			}
			if (sortString != null)
			{
				this.sort = RetailerPerformanceReport.parseSortString(sortString);
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

		private void process(Report.IFilterItem<? extends Report.IFilterField,? extends Object> filterItem) throws Exception
		{
			FilterField field = (FilterField)filterItem.getField();
			switch(field)
			{
				case TRANSACTION_TYPE:
					{
						Object value = filterItem.getValue();
						Set<String> effectiveValue = null;
						if ( value instanceof TransactionType )
						{
							effectiveValue = ((TransactionType)filterItem.getValue()).getCode();
						}
						else if ( value instanceof String )
						{
							TransactionType transactionType = (TransactionType)FilterFieldValueFactory.getInstance().fromString(field, (String)value);
							effectiveValue = transactionType.getCode();
						}
						else
						{
							throw new IllegalArgumentException(String.format("TRANSACTION_TYPE value %s not acceptable", value));
						}
						this.wherePredicates.add(this.process(root.get("type"), filterItem.getOperator(), effectiveValue));
					}
					break;
				case TRANSACTION_STATUS:
					this.wherePredicates.add(this.process(root.get("success"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case FOLLOW_UP:
					this.wherePredicates.add(this.process(root.get("followUp"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case A_AGENT_ID:
					this.wherePredicates.add(this.process(root.get("a_AgentID"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case A_MOBILE_NUMBER:
					this.wherePredicates.add(this.process(root.get("a_MSISDN"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case A_TIER_NAME:
					this.wherePredicates.add(this.process(root.get("a_TierName"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case A_GROUP_NAME:
					this.wherePredicates.add(this.process(root.get("a_GroupName"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case A_SERVICE_CLASS_NAME:
					this.wherePredicates.add(this.process(root.get("a_ServiceClassName"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case A_OWNER_ID:
					this.wherePredicates.add(this.process(root.get("a_OwnerID"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case A_OWNER_MOBILE_NUMBER:
					this.wherePredicates.add(this.process(root.get("a_OwnerMobileNumber"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case A_IMEI:
					this.wherePredicates.add(this.process(root.get("a_IMEI"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case TOTAL_AMOUNT:
					Expression<? extends Number> path = root.get("amount");
					this.havingPredicates.add(this.process(criteriaBuilder.sum(path), filterItem.getOperator(), filterItem.getValue()));
					break;
				default:
					throw new IllegalArgumentException(String.format("FilterField %s not handled", filterItem.getField()));
			}
		}

		//Not used:
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

		/*
		private void process(Report.ISortItem<? extends Report.IResultField> sortItem) throws IllegalArgumentException
		{
			ResultField field = (ResultField)sortItem.getField();
			switch(field)
			{
				case DATE:
					this.orderList.add(this.process(root.get("endDate"), sortItem.getOperator()));
					break;
				case TRANSACTION_TYPE:
					this.orderList.add(this.process(root.get("type"), sortItem.getOperator()));
					break;
				case TRANSACTION_STATUS:
					this.orderList.add(this.process(root.get("success"), sortItem.getOperator()));
					break;
				case FOLLOW_UP:
					this.orderList.add(this.process(root.get("followUp"), sortItem.getOperator()));
					break;
				case A_AGENT_ID:
					this.orderList.add(this.process(root.get("a_AgentID"), sortItem.getOperator()));
					break;
				case A_ACCOUNT_NUMBER:
					this.orderList.add(this.process(root.get("a_AgentAccountNumber"), sortItem.getOperator()));
					break;
				case A_MOBILE_NUMBER:
					this.orderList.add(this.process(root.get("a_MSISDN"), sortItem.getOperator()));
					break;
				case A_IMEI:
					this.orderList.add(this.process(root.get("a_IMEI"), sortItem.getOperator()));
					break;
				case A_IMSI:
					this.orderList.add(this.process(root.get("a_IMSI"), sortItem.getOperator()));
					break;
				case A_NAME:
					this.orderList.add(this.process(root.get("a_AgentName"), sortItem.getOperator()));
					break;
				case A_TIER_NAME:
					this.orderList.add(this.process(root.get("a_TierName"), sortItem.getOperator()));
					break;
				case A_GROUP_NAME:
					this.orderList.add(this.process(root.get("a_GroupName"), sortItem.getOperator()));
					break;
				case A_SERVICE_CLASS_NAME:
					this.orderList.add(this.process(root.get("a_ServiceClassName"), sortItem.getOperator()));
					break;
				case A_OWNER_MOBILE_NUMBER:
					this.orderList.add(this.process(root.get("a_OwnerMobileNumber"), sortItem.getOperator()));
					break;
				case A_OWNER_IMSI:
					this.orderList.add(this.process(root.get("a_OwnerIMSI"), sortItem.getOperator()));
					break;
				case A_OWNER_NAME:
					this.orderList.add(this.process(root.get("a_OwnerName"), sortItem.getOperator()));
					break;
				case TOTAL_AMOUNT:
					{
						Expression<? extends Number> expression = root.get("amount");
						this.orderList.add(this.process(criteriaBuilder.sum(expression), sortItem.getOperator()));
					}
					break;
				case TOTAL_BONUS:
					{
						Expression<? extends Number> expression = root.get("bonusAmount");
						this.orderList.add(this.process(criteriaBuilder.sum(expression), sortItem.getOperator()));
					}
					break;
				case TRANSACTION_COUNT:
					{
						Expression<? extends Number> expression = root.get("id");
						this.orderList.add(this.process(criteriaBuilder.count(expression), sortItem.getOperator()));
					}
					break;
				default:
					throw new IllegalArgumentException(String.format("ResultField %s not handled", sortItem.getField()));
			}
		}
		*/

		public CriteriaQuery<ResultEntry> criteriaQuery() throws Exception
		{
			criteriaBuilder = this.em.getCriteriaBuilder();
			criteriaQuery = this.criteriaBuilder.createQuery(ResultEntry.class);
			root = this.criteriaQuery.from(OlapTransaction.class);
			wherePredicates = new ArrayList<Predicate>();
			if (this.agentID != null && this.agentID != 0)
			{
				wherePredicates.add(criteriaBuilder.equal(criteriaBuilder.function("useindex_rproa",Boolean.class),criteriaBuilder.literal(true)));
				wherePredicates.add(criteriaBuilder.equal(root.get("a_OwnerID"), this.agentID));
			}	
			else
			{
				wherePredicates.add(criteriaBuilder.equal(criteriaBuilder.function("useindex_rpr",Boolean.class),criteriaBuilder.literal(true)));
			}	
			wherePredicates.add(criteriaBuilder.equal(criteriaBuilder.literal(true),criteriaBuilder.literal(true)));
			havingPredicates = new ArrayList<Predicate>();

			Set<String> transactionCodeSet = new HashSet<String>();
			transactionCodeSet.addAll(TransactionType.SELL.getCode());
			transactionCodeSet.addAll(TransactionType.SELF_TOPUP.getCode());
			transactionCodeSet.addAll(TransactionType.SELL_BUNDLE.getCode());
			transactionCodeSet.addAll(TransactionType.NON_AIRTIME_DEBIT.getCode());
			transactionCodeSet.addAll(TransactionType.NON_AIRTIME_REFUND.getCode());
			transactionCodeSet.addAll(TransactionType.REVERSE.getCode());
			transactionCodeSet.addAll(TransactionType.ADJUDICATE.getCode());

			Predicate typePredicate1 = root.get("type").in(transactionCodeSet);
			Predicate tierPredicate1 = criteriaBuilder.or(
				criteriaBuilder.equal(root.get("a_TierType"),Tier.Type.RETAILER.getCode()),
				criteriaBuilder.equal(root.get("a_TierType"),criteriaBuilder.nullLiteral(String.class)));
			Predicate tierAndTypePredicate1 = criteriaBuilder.and(typePredicate1, tierPredicate1);

			Predicate typePredicate2 = criteriaBuilder.equal(root.get("type"), TransactionType.TRANSFER.getCode());
			Predicate tierPredicate2 = criteriaBuilder.and(
				criteriaBuilder.equal(root.get("a_TierType"),Tier.Type.RETAILER.getCode()),
				criteriaBuilder.equal(root.get("b_TierType"),Tier.Type.RETAILER.getCode()));
			Predicate tierAndTypePredicate2 = criteriaBuilder.and(typePredicate2, tierPredicate2);

			wherePredicates.add(criteriaBuilder.or(tierAndTypePredicate1, tierAndTypePredicate2));
		
			if (filter != null)
			{
				for (Report.IFilterItem<? extends FilterField,? extends Object> filterItem : filter.getItems())
				{
					this.process(filterItem);
				}
			}
			if (timeInterval != null && timeInterval.getStartDate() != null)
			{
				wherePredicates.add(criteriaBuilder.greaterThanOrEqualTo(
					root.<Date>get("endDate"),
					criteriaBuilder.function("date",Date.class,criteriaBuilder.literal(timeInterval.getStartDate()))
				));
			}
			if (timeInterval != null && timeInterval.getEndDate() != null)
			{
				wherePredicates.add(criteriaBuilder.lessThanOrEqualTo(
					root.<Date>get("endDate"),
					criteriaBuilder.function("date",Date.class,criteriaBuilder.literal(timeInterval.getEndDate()))
				));
			}
			if (wherePredicates.size() > 0) criteriaQuery.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));
			if (havingPredicates.size() > 0) criteriaQuery.having(havingPredicates.toArray(new Predicate[havingPredicates.size()]));
			/*
			if (sort != null)
			{
				orderList = new ArrayList<Order>();
				for (Report.ISortItem<? extends Report.IResultField> sortItem : sort.getItems())
				{
					this.process(sortItem);
				}
				criteriaQuery.orderBy(orderList);
			}
			*/
			criteriaQuery.groupBy(
				root.get("endDate"),
				root.get("a_MSISDN"),
				root.get("type"),
				root.get("success")
			);
			criteriaQuery.select(criteriaBuilder.construct(ResultEntry.class,
				root.get("endDate"),
				root.get("type"),
				root.get("success"),
				root.get("followUp"),
				root.get("a_AgentID"),
				root.get("a_AgentAccountNumber"),
				root.get("a_MSISDN"),
				root.get("a_IMEI"),
				root.get("a_IMSI"),
				root.get("a_AgentName"),
				root.get("a_TierName"),
				root.get("a_GroupName"),
				root.get("a_ServiceClassName"),
				root.get("a_OwnerIMSI"),
				root.get("a_OwnerMobileNumber"),
				root.get("a_OwnerName"),
				criteriaBuilder.sum(root.<Number>get("amount")),
				criteriaBuilder.sum(root.<Number>get("buyerTradeBonusAmount")),
				criteriaBuilder.count(root.get("id"))
			));
			return criteriaQuery;
		}
		
		public RetailerPerformanceReportResult result(int first, int max) throws Exception
		{
			RetailerPerformanceReportResult result = new RetailerPerformanceReportResult();
			if (first == 0 && max == 0)
			{
				result.setEntries(new ArrayList<RetailerPerformanceReport.ResultEntry>());
			}
			else
			{
				CriteriaQuery<RetailerPerformanceReport.ResultEntry> criteriaQuery = this.criteriaQuery();
				TypedQuery<RetailerPerformanceReport.ResultEntry> typedQuery = this.em.createQuery(criteriaQuery);
				if (first > 0) typedQuery.setFirstResult(first);
				if (max > 0) typedQuery.setMaxResults(max);
				//typedQuery.setHint("eclipselink.sql.hint", "SQL_CALC_FOUND_ROWS");
				List<RetailerPerformanceReport.ResultEntry> entries = typedQuery.getResultList();
				result.setEntries(entries);
				/*
				{
					Query query = em.createNativeQuery("select found_rows();");
					BigInteger count = (BigInteger)query.getSingleResult();
					result.setFound(count == null ? null : count.intValue());
				}
				*/
				//result.setFound( ( max > 0 ) ? ( first + ( entries.size() >= max ? entries.size() + 1 : entries.size() ) ) : entries.size() );
				result.setFound( ( max > 0 ) ? ( first + ( entries.size() >= max ? entries.size() + 1 : entries.size() ) ) : ( first + entries.size() ) );
			}
			return result;
		}

		public List<RetailerPerformanceReport.ResultEntry> entries(int first, int max) throws Exception
		{
			List<RetailerPerformanceReport.ResultEntry> entries = null;
			if (first == 0 && max == 0)
			{
				entries = new ArrayList<RetailerPerformanceReport.ResultEntry>();
			}
			else
			{
				CriteriaQuery<RetailerPerformanceReport.ResultEntry> criteriaQuery = this.criteriaQuery();
				TypedQuery<RetailerPerformanceReport.ResultEntry> typedQuery = this.em.createQuery(criteriaQuery);
				if (first > 0) typedQuery.setFirstResult(first);
				if (max > 0) typedQuery.setMaxResults(max);
				entries = typedQuery.getResultList();
			}
			return entries;
		}
	}

	public static class CsvExportProcessor extends hxc.services.ecds.rest.batch.CsvExportProcessor<RetailerPerformanceReport.ResultEntry>
	{
		private static final String[] HEADINGS = new String[] {
			"date",

			"transactionType",
			"transactionStatus",
			"followUp",

			"a_AgentID",
			"a_AccountNumber",
			"a_MobileNumber",
			"a_IMEI",
			"a_IMSI",
			"a_Name",

			"a_TierName",
			"a_GroupName",
			"a_ServiceClassName",

			"a_OwnerImsi",
			"a_OwnerMobileNumber",
			"a_OwnerName",

			"totalAmount",
			"totalBonus",
			"transactionCount",
		};

		public CsvExportProcessor(int first)
		{
			super(HEADINGS, first, false);
		}

		@Override
		protected void write(RetailerPerformanceReport.ResultEntry record)
		{
			put("date", record.getDate());

			put("transactionType", record.getTransactionType());
			put("transactionStatus", ( record.getTransactionStatus() ? "SUCCESS" : "FAILED" ));
			put("followUp", record.getFollowUp());

			put("a_AgentID", record.getA_AgentID());
			put("a_AccountNumber", record.getA_AccountNumber());
			put("a_MobileNumber", record.getA_MobileNumber());
			put("a_IMEI", record.getA_IMEI());
			put("a_IMSI", record.getA_IMSI());
			put("a_Name", record.getA_Name());

			put("a_TierName", record.getA_TierName());
			put("a_GroupName", record.getA_GroupName());
			put("a_ServiceClassName", record.getA_ServiceClassName());

			put("a_OwnerImsi", record.getA_OwnerImsi());
			put("a_OwnerMobileNumber", record.getA_OwnerMobileNumber());
			put("a_OwnerName", record.getA_OwnerName());

			put("totalAmount", record.getTotalAmount());
			put("totalBonus", record.getTotalBonus());
			put("transactionCount", record.getTransactionCount());
		}
	}
}
