package hxc.services.ecds.reports;

import static hxc.ecds.protocol.rest.Transaction.Type.NON_AIRTIME_DEBIT;
import static hxc.ecds.protocol.rest.Transaction.Type.SELF_TOPUP;
import static hxc.ecds.protocol.rest.Transaction.Type.SELL;
import static hxc.services.ecds.rest.TransactionHelper.isEmptyString;
import static java.math.BigDecimal.ZERO;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportParameters;
import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportResult;
import hxc.ecds.protocol.rest.util.DateHelper;
import hxc.ecds.protocol.rest.util.TimeInterval;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.olapmodel.OlapAgentAccount;
import hxc.services.ecds.olapmodel.OlapGroup;
import hxc.services.ecds.olapmodel.OlapTransaction;

public class DailyGroupSalesReport
	extends hxc.ecds.protocol.rest.reports.DailyGroupSalesReport
{
	public static class ResultEntry extends hxc.ecds.protocol.rest.reports.DailyGroupSalesReportResultEntry
	{
		public ResultEntry(String groupName, long agentTotalCount, long agentTransactedCount, long transactionCount,
			BigDecimal agentAverageAmount, BigDecimal transactionAverageAmount, BigDecimal totalAmount)
		{
			super();
			this.setGroupName(groupName);
			
			this.setAgentTotalCount(Long.valueOf(agentTotalCount).intValue());
			this.setAgentTransactedCount(Long.valueOf(agentTransactedCount).intValue());
			this.setTransactionCount(Long.valueOf(transactionCount).intValue());

			this.setAgentAverageAmount(agentAverageAmount);
			this.setTransactionAverageAmount(transactionAverageAmount);
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
			return filterField.getType().getDeclaredMethod("valueOf", String.class).invoke(null, value);
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

		public Processor(EntityManager oltpEm, EntityManager em, int companyID, Sort sort, Filter filter)
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
			this.filter = filter;
			this.sort = sort;
		}

		public Processor(EntityManager oltpEm, EntityManager em, int companyID, String sortString, String filterString) throws Exception
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
			this.setParameters(sortString, filterString);
		}

		public Processor(EntityManager oltpEm, EntityManager em, int companyID, DailyGroupSalesReportParameters parameters) throws Exception
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
			this.setParameters(parameters);
		}
		
		public Processor(EntityManager oltpEm, EntityManager em, int companyID, DailyGroupSalesReportParameters parameters, Date relativeTimeRangeReference) throws Exception
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
			this.relativeTimeRangeReference = relativeTimeRangeReference;
			this.setParameters(parameters, relativeTimeRangeReference);
		}

		public void setParameters(DailyGroupSalesReportParameters parameters, Date relativeTimeRangeReference) throws Exception
		{
			this.relativeTimeRangeReference = relativeTimeRangeReference;
			this.setParameters(parameters);
		}

		public void setParameters(DailyGroupSalesReportParameters parameters) throws Exception
		{
			this.filter = parameters.getFilter();
			this.sort = parameters.getSort();
			
			this.relativeTimeRange = Report.RelativeTimeRange.PREVIOUS_HOUR;
			TimeInterval timeInterval = this.relativeTimeRange.resolve(this.relativeTimeRangeReference != null ? relativeTimeRangeReference : new Date());
			timeInterval.setStartDate(DateHelper.startOf(timeInterval.getStartDate(), Calendar.DATE).getTime());
			this.timeInterval = timeInterval;
		}

		public void setParameters(String sortString, String filterString) throws Exception
		{
			if (filterString != null)
			{
				this.filter = DailyGroupSalesReport.parseFilterString("+" + filterString);
			}
			if (sortString != null)
			{
				this.sort = DailyGroupSalesReport.parseSortString(sortString);
			}

			this.relativeTimeRange = Report.RelativeTimeRange.PREVIOUS_HOUR;
			TimeInterval timeInterval = this.relativeTimeRange.resolve(this.relativeTimeRangeReference != null ? relativeTimeRangeReference : new Date());
			timeInterval.setStartDate(DateHelper.startOf(timeInterval.getStartDate(), Calendar.DATE).getTime());
			this.timeInterval = timeInterval;
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
			}
			throw new IllegalArgumentException(String.format("operator %s not handled", operator));
		}

		private void process(Report.IFilterItem<? extends Report.IFilterField,? extends Object> filterItem) throws Exception
		{
			FilterField field = (FilterField)filterItem.getField();
			switch(field)
			{
				case TIER_NAME:
					this.wherePredicates.add(this.process(root.get("a_TierName"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case GROUP_NAME:
					this.wherePredicates.add(this.process(root.get("a_GroupName"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case TRANSACTION_TYPE:
					CriteriaBuilder cb = criteriaBuilder;
					if ("AIRTIME".equals(filterItem.getValue())) {
						this.wherePredicates.add(cb.
								or(cb.equal(root.get("type"), SELF_TOPUP.getCode()),
								   cb.equal(root.get("type"), SELL.getCode())
								));
					} else if ("NON_AIRTIME".equals(filterItem.getValue())) {
						this.wherePredicates.add(cb.equal(root.get("type"), NON_AIRTIME_DEBIT.getCode()));
					} else if ("ALL_TRANSACTIONS".equals(filterItem.getValue())) {
						this.wherePredicates.add(cb.
								or(cb.equal(root.get("type"), SELF_TOPUP.getCode()),
								   cb.equal(root.get("type"), SELL.getCode()),
								   cb.equal(root.get("type"), NON_AIRTIME_DEBIT.getCode())
								));
					}
					break;
				default:
					throw new IllegalArgumentException(String.format("FilterField %s not handled", filterItem.getField()));
			}
		}

		private Order process(Expression<? extends Object> expression, Report.SortOperator operator) throws IllegalArgumentException
		{
			switch(operator)
			{
				case ASCENDING:
					return criteriaBuilder.asc(expression);
				case DESCENDING:
					return criteriaBuilder.desc(expression);
			}
			throw new IllegalArgumentException(String.format("operator %s not handled", operator));
		}

		/*
		private void process(Report.ISortItem<? extends Report.IResultField> sortItem) throws IllegalArgumentException
		{
			ResultField field = (ResultField)sortItem.getField();
			switch(field)
			{
				case GROUP_NAME:
					this.orderList.add(this.process(root.get("a_GroupName"), sortItem.getOperator()));
					break;
				case TOTAL_AMOUNT:
					{
						Expression<? extends Number> expression = root.get("amount");
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
			//wherePredicates.add(criteriaBuilder.equal(criteriaBuilder.function("useindex_gsr",Boolean.class),criteriaBuilder.literal(true)));
			//wherePredicates.add(criteriaBuilder.equal(criteriaBuilder.literal(true),criteriaBuilder.literal(true)));

			Set<String> transactionCodeSet = new HashSet<String>();
			transactionCodeSet.add(TransactionType.SELL.getCode());
			transactionCodeSet.add(TransactionType.SELF_TOPUP.getCode());
			transactionCodeSet.add(TransactionType.SELL_BUNDLE.getCode());
			transactionCodeSet.add(TransactionType.NON_AIRTIME_DEBIT.getCode());
			transactionCodeSet.add(TransactionType.NON_AIRTIME_REFUND.getCode());

			wherePredicates.add(root.get("type").in(transactionCodeSet));
			wherePredicates.add(criteriaBuilder.equal(root.get("success"),true));

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
			if (wherePredicates.size() > 0) criteriaQuery.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));
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
				root.get("a_GroupName")
			);
			criteriaQuery.select(criteriaBuilder.construct(ResultEntry.class,
				root.get("a_GroupName"),
				criteriaBuilder.countDistinct(root.get("a_AgentID")), // FIXME
				criteriaBuilder.countDistinct(root.get("a_AgentID")),
				criteriaBuilder.count(root.get("id")),
				criteriaBuilder.quot(criteriaBuilder.sum(root.<Number>get("amount")), criteriaBuilder.countDistinct(root.get("a_AgentID"))),
				criteriaBuilder.quot(criteriaBuilder.sum(root.<Number>get("amount")), criteriaBuilder.count(root.get("id"))),
				criteriaBuilder.sum(root.<Number>get("amount"))
			));
			return criteriaQuery;
		}

		protected List<DailyGroupSalesReport.ResultEntry> postprocessResult( List<DailyGroupSalesReport.ResultEntry> entries, int first, int max )
		{
			CriteriaQuery<OlapGroup> groupCriteriaQuery = this.criteriaBuilder.createQuery(OlapGroup.class);
			Root<OlapGroup> groupRoot = groupCriteriaQuery.from(OlapGroup.class);

			String filterByGroupName = null;
			List<Predicate> groupWherePredicates = new ArrayList<Predicate>();
			if (filter != null)
			{
				for (Report.IFilterItem<? extends FilterField,? extends Object> filterItem : filter.getItems())
				{
					FilterField field = (FilterField)filterItem.getField();
					switch(field)
					{
					case TIER_NAME:
						Tier tier = Tier.findByName(oltpEm, companyID, (String)filterItem.getValue());
						if (tier != null)
						{
							List<Group> groups = Group.findByTierID(oltpEm, companyID, tier.getId());
							if (groups.size() > 0)
							{
								Set<String> groupNameSet = new HashSet<String>();
								for (Group group : groups )
								{
									groupNameSet.add(group.getName());
								}
								groupWherePredicates.add(groupRoot.get("name").in(groupNameSet));
							}	
							else
								groupWherePredicates.add(criteriaBuilder.equal(criteriaBuilder.literal(false), criteriaBuilder.literal(true)));
						}
						break;
					case GROUP_NAME:
						groupWherePredicates.add(this.process(groupRoot.get("name"), filterItem.getOperator(), (String)filterItem.getValue()));
						break;
					}
				}
			}
			if (groupWherePredicates.size() > 0) groupCriteriaQuery.where(groupWherePredicates.toArray(new Predicate[groupWherePredicates.size()]));
			
			groupCriteriaQuery.select(groupRoot);
			TypedQuery<OlapGroup> groupTypedQuery = this.em.createQuery(groupCriteriaQuery);
			List<OlapGroup> groups = groupTypedQuery.getResultList();

			CriteriaQuery<GroupCountResultEntry> agentCriteriaQuery = this.criteriaBuilder.createQuery(GroupCountResultEntry.class);
			Root<OlapAgentAccount> agentRoot = agentCriteriaQuery.from(OlapAgentAccount.class);
			agentCriteriaQuery.groupBy(
				agentRoot.get("groupName")
			);
			agentCriteriaQuery.select(criteriaBuilder.construct(GroupCountResultEntry.class,
				agentRoot.get("groupName"),
				criteriaBuilder.count(root.get("id"))
			));
			TypedQuery<GroupCountResultEntry> agentTypedQuery = this.em.createQuery(agentCriteriaQuery);
			List<GroupCountResultEntry> totals = agentTypedQuery.getResultList();

			// Add entry for agents without group
			if (entries.stream().noneMatch(e -> isEmptyString(e.getGroupName()))) {
				entries.add(new ResultEntry(null, 0,0,0, ZERO, ZERO, ZERO));
			}

			for (OlapGroup group : groups)
			{
				boolean found = false;
				for (ResultEntry entry : entries)
				{
					if (Objects.equals(entry.getGroupName(), group.getName()))
					{
						found = true;
						break;
					}
				}	
					
				if ( !found )
				{
					ResultEntry gentry = new ResultEntry(group.getName(), 0, 0, 0, ZERO, ZERO, ZERO);
					entries.add( gentry );
				}
			}

			for (ResultEntry entry : entries) {
				if (isEmptyString(entry.getGroupName())) {
					int numberOfAgentsWithoutGroup =
							totals.stream().filter(t -> isEmptyString(t.getGroupName())).mapToInt(GroupCountResultEntry::getAgentTotalCount).sum();
					entry.setAgentTotalCount(numberOfAgentsWithoutGroup);
				} else {
					totals.stream().filter(t -> Objects.equals(entry.getGroupName(), t.getGroupName()))
							.findFirst().ifPresent(t -> entry.setAgentTotalCount(t.getAgentTotalCount()));
				}
			}
	
			Collections.sort(entries);
			if ( first > 0 || max > 0 )
			{
				DailyGroupSalesReport.ResultEntry[] entriesArray = new DailyGroupSalesReport.ResultEntry[entries.size()];
				entries.toArray(entriesArray);
				entries = Arrays.asList(Arrays.copyOfRange(entriesArray, first > 0 ? first : 0, max > 0 ? Math.min(entriesArray.length, first + max) : entriesArray.length));
			}	

			return entries;
		}

		public DailyGroupSalesReportResult result(int first, int max) throws Exception
		{
			DailyGroupSalesReportResult result = new DailyGroupSalesReportResult();
			if (first == 0 && max == 0)
			{
				result.setEntries(new ArrayList<DailyGroupSalesReport.ResultEntry>());
			}
			else
			{
				CriteriaQuery<DailyGroupSalesReport.ResultEntry> criteriaQuery = this.criteriaQuery();
				TypedQuery<DailyGroupSalesReport.ResultEntry> typedQuery = this.em.createQuery(criteriaQuery);
				//if (first > 0) typedQuery.setFirstResult(first);
				//if (max > 0) typedQuery.setMaxResults(max);
				//typedQuery.setHint("eclipselink.sql.hint", "SQL_CALC_FOUND_ROWS");
				List<DailyGroupSalesReport.ResultEntry> entries = typedQuery.getResultList();
				entries = postprocessResult(entries, first, max);
				result.setEntries(entries);
				/*
				{
					Query query = em.createNativeQuery("select found_rows();");
					BigInteger count = (BigInteger)query.getSingleResult();
					result.setFound(count == null ? null : count.intValue());
				}
				*/
				//result.setFound( first + ( entries.size() >= max ? entries.size() + 1 : entries.size() ) );
				result.setFound( ( max > 0 ) ? ( first + ( entries.size() >= max ? entries.size() + 1 : entries.size() ) ) : ( first + entries.size() ) );
			}
			return result;
		}

		public List<DailyGroupSalesReport.ResultEntry> entries(int first, int max) throws Exception
		{
			List<DailyGroupSalesReport.ResultEntry> entries = null;
			if (first == 0 && max == 0)
			{
				entries = new ArrayList<DailyGroupSalesReport.ResultEntry> ();
			}
			else
			{
				CriteriaQuery<DailyGroupSalesReport.ResultEntry> criteriaQuery = this.criteriaQuery();
				TypedQuery<DailyGroupSalesReport.ResultEntry> typedQuery = this.em.createQuery(criteriaQuery);
				//if (first > 0) typedQuery.setFirstResult(first);
				//if (max > 0) typedQuery.setMaxResults(max);
				entries = typedQuery.getResultList();
				entries = postprocessResult(entries, first, max);
			}
			return entries;
		}
	}

	public static class CsvExportProcessor extends hxc.services.ecds.rest.batch.CsvExportProcessor<DailyGroupSalesReport.ResultEntry>
	{
		private static final String[] HEADINGS = new String[] {
			"GROUPE",
			
			"NOMBRE_D'AGENT",
			"NOMBRE_D'AGENT_EFFECTUE_TRANSACTIONS",
			"NOMBRE_TOTAL_DE_TRANSACTIONS",

			"MOYENNE_PAR_CABINE",
			"MOYENNE_PAR_TRANSACTION",
			"MONTANT_TOTAL",
		};

		public CsvExportProcessor(int first)
		{
			super(HEADINGS, first, false);
		}

		@Override
		protected void write(DailyGroupSalesReport.ResultEntry record)
		{
			put("GROUPE", record.getGroupName());
			
			put("NOMBRE_D'AGENT", record.getAgentTotalCount());
			put("NOMBRE_D'AGENT_EFFECTUE_TRANSACTIONS", record.getAgentTransactedCount());
			put("NOMBRE_TOTAL_DE_TRANSACTIONS", record.getTransactionCount());

			put("MOYENNE_PAR_CABINE", record.getAgentAverageAmount());
			put("MOYENNE_PAR_TRANSACTION", record.getTransactionAverageAmount());
			put("MONTANT_TOTAL", record.getTotalAmount());
		}
	}
}
