package hxc.services.ecds.reports;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
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
import javax.persistence.criteria.Subquery;

import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportParameters;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportResult;
import hxc.ecds.protocol.rest.util.DateHelper;
import hxc.ecds.protocol.rest.util.TimeInterval;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.olapmodel.OlapAgentAccount;
import hxc.services.ecds.olapmodel.OlapTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AccountBalanceSummaryReport
	extends hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReport
{
	final static Logger logger = LoggerFactory.getLogger(AccountBalanceSummaryReport.class);
	public static class ResultEntry extends hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportResultEntry
	{
		public ResultEntry(String msisdn, String name, BigDecimal balance, BigDecimal bonusBalance, BigDecimal holdBalance, String tierName, String groupName )
		{
			super();
			this.setMsisdn(msisdn);
			this.setName(name);
			this.setBalance(balance);
			this.setBonusBalance(bonusBalance);
			this.setHoldBalance(holdBalance);
			this.setTierName(tierName);
			this.setGroupName(groupName);
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
		private Integer agentID;
		private Filter filter;
		private Sort sort;

		private TimeInterval timeInterval;
		private Report.RelativeTimeRange relativeTimeRange;
		private CriteriaBuilder criteriaBuilder;
		private CriteriaQuery<ResultEntry> criteriaQuery;
		private Root<OlapAgentAccount> root;

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

		public Processor(EntityManager oltpEm, EntityManager em, int companyID, Integer agentID, Sort sort, Filter filter)
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
			this.agentID = agentID;
			this.filter = filter;
			this.sort = sort;
		}

		public Processor(EntityManager oltpEm, EntityManager em, int companyID, Integer agentID, String sortString, String filterString) throws Exception
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
			this.agentID = agentID;
			this.setParameters(sortString, filterString);
		}

		public Processor(EntityManager oltpEm, EntityManager em, int companyID, Integer agentID, AccountBalanceSummaryReportParameters parameters) throws Exception
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
			this.agentID = agentID;
			this.setParameters(parameters);
		}
		
		public Processor(EntityManager oltpEm, EntityManager em, int companyID, Integer agentID, AccountBalanceSummaryReportParameters parameters, Date relativeTimeRangeReference) throws Exception
		{
			this.oltpEm = oltpEm;
			this.em = em;
			this.companyID = companyID;
			this.agentID = agentID;
			this.setParameters(parameters, relativeTimeRangeReference);
		}

		public void setParameters(AccountBalanceSummaryReportParameters parameters, Date relativeTimeRangeReference) throws Exception
		{
			this.relativeTimeRange = Report.RelativeTimeRange.PREVIOUS_HOUR;
			TimeInterval timeInterval = this.relativeTimeRange.resolve(relativeTimeRangeReference != null ? relativeTimeRangeReference : new Date());
			timeInterval.setStartDate(DateHelper.startOf(timeInterval.getStartDate(), Calendar.DATE).getTime());
			this.timeInterval = timeInterval;
			this.setParameters(parameters);
		}

		public void setParameters(AccountBalanceSummaryReportParameters parameters) throws Exception
		{
			this.filter = parameters.getFilter();
			this.sort = parameters.getSort();
		}

		public void setParameters(String sortString, String filterString) throws Exception
		{
			if (filterString != null)
			{
				this.filter = AccountBalanceSummaryReport.parseFilterString("+" + filterString);
			}
			if (sortString != null)
			{
				this.sort = AccountBalanceSummaryReport.parseSortString(sortString);
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
			}
			throw new IllegalArgumentException(String.format("operator %s not handled", operator));
		}
		
		//private Predicate agentActivity(CriteriaBuilder cb, Root<Transaction> root, CriteriaQuery<?> query, String agentIdName, ParameterExpression<Integer> me)
		private Predicate agentActivity(Root<OlapAgentAccount> root, Integer offsetDays)
		{
			Calendar now = Calendar.getInstance();
			Date endDate = now.getTime();
			//now.add(Calendar.DATE, -10);
			offsetDays = -offsetDays;
			now.add(Calendar.DATE, offsetDays);
			Date startDate = now.getTime();
			
			Subquery<OlapTransaction> sq = criteriaQuery.subquery(OlapTransaction.class);
			Root<OlapTransaction> transaction = sq.from(OlapTransaction.class);
			
			//Expression<Date> endedDate = transaction.<Date>get("ended_date");
			Expression<Date> endedDate = transaction.<Date>get("endDate");
			sq.select(transaction)
				.where(
						criteriaBuilder.or(
								criteriaBuilder.equal(transaction.get("a_AgentID"), root.get("id")), 
								criteriaBuilder.equal(transaction.get("b_AgentID"), root.get("id"))
								),
						criteriaBuilder.and(
								criteriaBuilder.between(endedDate, startDate, endDate)
								)
						);
			
			/*Subquery<OlapTransaction> sq = query.subquery(OlapTransaction.class);
			
			Path<Integer> agentId = root.get(agentIdName);
			Subquery<Agent> sq = query.subquery(Agent.class);
			Root<Agent> agent = sq.from(Agent.class);
			sq.select(agent) //
					.where(cb.equal(agent.get("id"), agentId), cb.equal(agent.get("ownerAgentID"), me));
			return cb.exists(sq);*/
			return criteriaBuilder.exists(sq);
		}

		private void process(Report.IFilterItem<? extends Report.IFilterField,? extends Object> filterItem) throws Exception
		{
			FilterField field = (FilterField)filterItem.getField();
			switch(field)
			{
				case TIER_TYPE:
					List<Tier> tiers = Tier.findByType(oltpEm, companyID, (String)filterItem.getValue());
					Set<String> tierSet = new HashSet<String>();
					for( Tier tier : tiers )
					{
						tierSet.add(tier.getName());
					}
					this.wherePredicates.add(root.get("tierName").in(tierSet));
					break;
				case TIER_NAME:
					this.wherePredicates.add(this.process(root.get("tierName"), filterItem.getOperator(), filterItem.getValue()));
					break;
				case GROUP_NAME:
					this.wherePredicates.add(this.process(root.get("groupName"), filterItem.getOperator(), filterItem.getValue()));
					break;	
				case ACTIVITY_SCALE:
				case ACTIVITY_VALUE:
					try {
						Integer offsetDays = Integer.valueOf(String.valueOf(filterItem.getValue()));
						this.wherePredicates.add(agentActivity(root, offsetDays));
					}
					catch(NumberFormatException nex) {
						// Don't filter for invalid numbers.
					}
					catch(Exception ex) {
						logger.error("", ex);
					}
					break;
				case INCLUDE_ZERO_BALANCE:
					if ( !filterItem.getValue().equals("1") )	
					{
						Predicate balancePredicate = criteriaBuilder.or(
							criteriaBuilder.notEqual(root.get("balance"), 0),
							criteriaBuilder.notEqual(root.get("bonusBalance"), 0),
							criteriaBuilder.notEqual(root.get("holdBalance"), 0)
						);
						this.wherePredicates.add(balancePredicate);
					}	
					break;
				case INCLUDE_DELETED:
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

		private void process(Report.ISortItem<? extends Report.IResultField> sortItem) throws IllegalArgumentException
		{
			ResultField field = (ResultField)sortItem.getField();
			switch(field)
			{
				case GROUP_NAME:
					this.orderList.add(this.process(root.get("a_GroupName"), sortItem.getOperator()));
					break;
				default:
					throw new IllegalArgumentException(String.format("ResultField %s not handled", sortItem.getField()));
			}
		}

		public CriteriaQuery<ResultEntry> criteriaQuery() throws Exception
		{
			criteriaBuilder = this.em.getCriteriaBuilder();
			criteriaQuery = this.criteriaBuilder.createQuery(ResultEntry.class);
			root = this.criteriaQuery.from(OlapAgentAccount.class);
			wherePredicates = new ArrayList<Predicate>();
				
			wherePredicates.add(criteriaBuilder.equal(root.get("companyID"), this.companyID));
			if (this.agentID != null && this.agentID != 0)
				wherePredicates.add(criteriaBuilder.equal(root.get("ownerID"), this.agentID));

			if (filter != null)
			{
				for (Report.IFilterItem<? extends FilterField,? extends Object> filterItem : filter.getItems())
				{
					this.process(filterItem);
				}
			}

			if (wherePredicates.size() > 0) criteriaQuery.where(wherePredicates.toArray(new Predicate[wherePredicates.size()]));
			if (sort != null)
			{
				orderList = new ArrayList<Order>();
				for (Report.ISortItem<? extends Report.IResultField> sortItem : sort.getItems())
				{
					this.process(sortItem);
				}
				criteriaQuery.orderBy(orderList);
			}
			criteriaQuery.select(criteriaBuilder.construct(ResultEntry.class,
				root.get("msisdn"),
				root.get("name"),
				root.get("balance"),
				root.get("bonusBalance"),
				root.get("holdBalance"),
				root.get("tierName"),
				root.get("groupName")
			));
			return criteriaQuery;
		}

		public AccountBalanceSummaryReportResult result(int first, int max) throws Exception
		{
			AccountBalanceSummaryReportResult result = new AccountBalanceSummaryReportResult();
			if (first == 0 && max == 0)
			{
				result.setEntries(new ArrayList<AccountBalanceSummaryReport.ResultEntry>());
			}
			else
			{
				CriteriaQuery<AccountBalanceSummaryReport.ResultEntry> criteriaQuery = this.criteriaQuery();
				TypedQuery<AccountBalanceSummaryReport.ResultEntry> typedQuery = this.em.createQuery(criteriaQuery);
				if (first > 0) typedQuery.setFirstResult(first);
				if (max > 0) typedQuery.setMaxResults(max);
				//typedQuery.setHint("eclipselink.sql.hint", "SQL_CALC_FOUND_ROWS");
				List<AccountBalanceSummaryReport.ResultEntry> entries = typedQuery.getResultList();
				
				result.setEntries(entries);
				/*
				{
					Query query = em.createNativeQuery("select found_rows();");
					BigInteger count = (BigInteger)query.getSingleResult();
					result.setFound(count == null ? null : count.intValue());
				}
				*/
				result.setFound( ( max > 0 ) ? ( first + ( entries.size() >= max ? entries.size() + 1 : entries.size() ) ) : ( first + entries.size() ) );
			}
			return result;
		}

		public List<AccountBalanceSummaryReport.ResultEntry> entries(int first, int max) throws Exception
		{
			List<AccountBalanceSummaryReport.ResultEntry> entries = null;
			if (first == 0 && max == 0)
			{
				entries = new ArrayList<AccountBalanceSummaryReport.ResultEntry> ();
			}
			else
			{
				CriteriaQuery<AccountBalanceSummaryReport.ResultEntry> criteriaQuery = this.criteriaQuery();
				TypedQuery<AccountBalanceSummaryReport.ResultEntry> typedQuery = this.em.createQuery(criteriaQuery);
				if (first > 0) typedQuery.setFirstResult(first);
				if (max > 0) typedQuery.setMaxResults(max);
				entries = typedQuery.getResultList();
			}
			return entries;
		}
	}

	public static class CsvExportProcessor extends hxc.services.ecds.rest.batch.CsvExportProcessor<AccountBalanceSummaryReport.ResultEntry>
	{
		private static final String[] HEADINGS = new String[] {
			"Date",
			"Numéro",
			"Noms",

			"Montant",
			"Approvisionnement Bonus",
			"Hold Montant",

			"Tier",
			"Groupe"
		};

		String timestamp;

		public CsvExportProcessor(int first)
		{
			super(HEADINGS, first, false);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:MM:SS");
			this.timestamp = sdf.format(new Date());
		}

		@Override
		protected void write(AccountBalanceSummaryReport.ResultEntry record)
		{
			put(HEADINGS[0], timestamp);
			
			put(HEADINGS[1], record.getMsisdn());
			put(HEADINGS[2], record.getName());

			put(HEADINGS[3], record.getBalance());
			put(HEADINGS[4], record.getBonusBalance());
			put(HEADINGS[5], record.getHoldBalance() == null ? new BigDecimal(0) : record.getHoldBalance());
			put(HEADINGS[6], record.getTierName() == null ? "" : record.getTierName());
			put(HEADINGS[7], record.getGroupName() == null ? "" : record.getGroupName());
		}
	}
}
