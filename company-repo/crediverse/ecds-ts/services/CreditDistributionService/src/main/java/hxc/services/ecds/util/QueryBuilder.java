package hxc.services.ecds.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Parameter;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.AuditEntry;
import hxc.services.ecds.model.IMasterData;
import hxc.services.ecds.rest.RestParams;

public class QueryBuilder
{
	final static Logger logger = LoggerFactory.getLogger(QueryBuilder.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static Pattern pattern = Pattern.compile("\\+([A-Z,a-z,0-9,\\_,\\.]+)(\\:|\\=|\\#|\\~|\\>|\\<|\\≥|\\≤|\\!=|\\>=|\\<=)\\'([^\\']*)\\'");
	private static ConcurrentMap<String, CriteriaQuery<?>> queryCache = new ConcurrentHashMap<String, CriteriaQuery<?>>();

	private static final String COMPANY_ID = "companyID";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public static <T> Map<String,List<String>> getFilterMap(Class<T> cls, String filter, int companyID)
	{
		HashMap<String,List<String>> filterMap = new HashMap<String,List<String>>(); 
		String tenantPredicate = String.format("+%s='%d'", COMPANY_ID, companyID);
		StringBuilder sb = new StringBuilder();		
		
		if (filter != null && !filter.isEmpty())
			filter = tenantPredicate + "+" + filter;
		else
			filter = tenantPredicate;
		
		Matcher matcher = pattern.matcher(filter);
		while (matcher.find())
		{
			String column = matcher.group(1);
			String operator = matcher.group(2);
			String value = matcher.group(3);
			List<String> attributes = new ArrayList<String>();
			attributes.add(operator);
			attributes.add(value);
			filterMap.put(column,  attributes);			
		}
		return filterMap;
	}
	
	public static <T> List<T> getQueryResultList(EntityManager em, Class<T> cls, RestParams params, int companyID, String... searchColumns)
	{
		return getQueryResultList(em, cls, params, companyID, null, searchColumns);
	}
	
	public static <T> List<T> getQueryResultList(EntityManager em, Class<T> cls, RestParams params, int companyID, PredicateExtender<T> px, String... searchColumns)
	{
		if (params.getFirst() == 0 && params.getMax() == 0) return new ArrayList<T>();

		TypedQuery<T> query = QueryBuilder.getQuery(em, cls, params, companyID, px, searchColumns);
		return query.getResultList();
	}
	
	private static <T> TypedQuery<T> getQuery(EntityManager em, Class<T> cls, RestParams params, int companyID, String... searchColumns)
	{
		return getQuery(em, cls, params, companyID, null, searchColumns);
	}

	private static <T> TypedQuery<T> getQuery(EntityManager em, Class<T> cls, RestParams params, int companyID, PredicateExtender<T> px, String... searchColumns)
	{
		// Parse Filter Clause
		StringBuilder sb = new StringBuilder();
		sb.append(cls.getName());
		sb.append(':');

		if (px != null)
		{
			sb.append('$');
			sb.append(px.getName());
		}

		List<String> filterColumns = new ArrayList<String>();
		List<String> filterOperators = new ArrayList<String>();
		List<Object> filterValues = new ArrayList<Object>();

		// Add Tenant Predicate
		String tenantPredicate = "";
		if (!"hxc.services.ecds.model.Permission".equals(cls.getName())
				&& !"hxc.services.ecds.model.Account".equals(cls.getName()))
		{
			tenantPredicate = String.format("+%s='%d'", COMPANY_ID, companyID);
		}
		String filter = params.getFilter();
		if (filter != null && !filter.isEmpty())
			filter = tenantPredicate + "+" + filter;
		else
			filter = tenantPredicate;

		// Build Filter Predicates
		Matcher matcher = pattern.matcher(filter);
		while (matcher.find())
		{
			String column = matcher.group(1);
			String operator = matcher.group(2);
			String value = matcher.group(3);
			sb.append(column);
			sb.append(operator); 
			if (px != null && !px.preProcessFilter(column,operator,value))
			{
				sb.append(value);
				continue;
			}
			filterColumns.add(column);
			filterOperators.add(operator);
			filterValues.add(value);
		}

		// Add Sort
		String sort = params.getSort();
		if (sort != null && !sort.isEmpty())
		{
			sb.append('^');
			sb.append(sort);
		}

		// Add Search
		String search = params.getSearch();
		boolean hasSearch = search != null && !search.isEmpty() && searchColumns != null && searchColumns.length > 0;
		if (hasSearch)
		{
			filterValues.add(search);
			for (String searchColumn : searchColumns)
			{
				sb.append('?');
				sb.append(searchColumn);
			}
		}
		
		if (px != null)
		{
			sb.append(px.getExtendCacheToken());
		}
	
		/* 
		// replaced by px.getExtendCacheToken() above
		sb.append("ic1122="); // random number added to ensure uniqueness.
		if (params.isIncludeQuery())
		{
			sb.append('1');
		}
		else
		{
			sb.append('0');
		}
		*/

		// Attempt to get from Cache
		String key = sb.toString();
		@SuppressWarnings("unchecked")
		CriteriaQuery<T> criteria = (CriteriaQuery<T>) queryCache.get(key);

		// Build Criteria
		if (criteria == null)
		{
			// Build Query
			CriteriaBuilder cb = em.getCriteriaBuilder();
			criteria = cb.createQuery(cls);
			Root<T> root = criteria.from(cls);
			criteria.select(root);

			// Add Where Clause
			List<Predicate> predicates = new ArrayList<Predicate>();
			addWhereClause(filterColumns, filterOperators, cb, root, predicates);

			// Add Searching
			if (hasSearch)
			{
				addSearching(filterValues, cb, root, predicates, px, searchColumns);
			}

			if (px != null)
			{
				predicates = px.extend(cb, root, criteria, predicates);
			}

			criteria.where(predicates.toArray(new Predicate[predicates.size()]));

			// Add Sorting
			if (sort != null && !sort.isEmpty())
			{
				StringTokenizer st = new StringTokenizer(sort, "+-", true);
				List<Order> orderList = new ArrayList<Order>();
				String name = null;
				while (st.hasMoreTokens())
				{
					String token = st.nextToken();
					if (token.equals("+"))
						orderList.add(cb.asc(QueryBuilder.sortExpression(root, name)));
					else if (token.equals("-"))
						orderList.add(cb.desc(QueryBuilder.sortExpression(root, name)));
					else
					{
						name = token;
					}
				}
				criteria.orderBy(orderList);
			}

			// Cache the result
			queryCache.putIfAbsent(key, criteria);
		}

		// Add First/Max Results
		TypedQuery<T> query = em.createQuery(criteria);
		if (params.getFirst() > 0)
			query.setFirstResult(params.getFirst());
		query.setMaxResults(params.getMax() >= 0 ? params.getMax() : RestParams.DEFAULT_MAX_RESULTS);

		// Add Parameters
		addParameters(filterValues, query);
		if (px != null)
			px.addParameters(query);
		return query;
	}

	public static <T> TypedQuery<Long> getCountQuery(EntityManager em, Class<T> cls, RestParams params, int companyID, String... searchColumns)
	{
		return getCountQuery(em, cls, params, companyID, null, searchColumns);
	}

	public static <T> TypedQuery<Long> getCountQuery(EntityManager em, Class<T> cls, RestParams params, int companyID, PredicateExtender<T> px, String... searchColumns)
	{
		// Parse Filter Clause
		StringBuilder sb = new StringBuilder();
		sb.append(cls.getName());
		sb.append("*:");

		if (px != null)
		{
			sb.append('$');
			sb.append(px.getName());
		}

		List<String> filterColumns = new ArrayList<String>();
		List<String> filterOperators = new ArrayList<String>();
		List<Object> filterValues = new ArrayList<Object>();

		// Add Tenant Predicate
		// Add Tenant Predicate
		String tenantPredicate = "";
		if (!"hxc.services.ecds.model.Permission".equals(cls.getName())
				&& !"hxc.services.ecds.model.Account".equals(cls.getName()))
		{
			tenantPredicate = String.format("+%s='%d'", COMPANY_ID, companyID);
		}
		String filter = params.getFilter();
		if (filter != null && !filter.isEmpty())
			filter = tenantPredicate + "+" + filter;
		else
			filter = tenantPredicate;

		// Build Filter Predicates
		Matcher matcher = pattern.matcher(filter);
		while (matcher.find())
		{
			String column = matcher.group(1);
			String operator = matcher.group(2);
			String value = matcher.group(3);
			sb.append(column);
			sb.append(operator);
			if (px != null && !px.preProcessFilter(column,operator,value))
			{
				sb.append(value);
				continue;
			}
			filterColumns.add(column);
			filterOperators.add(operator);
			filterValues.add(matcher.group(3));
			
			
		}

		// Add Search
		String search = params.getSearch();
		boolean hasSearch = search != null && !search.isEmpty() && searchColumns != null && searchColumns.length > 0;
		if (hasSearch)
		{
			filterValues.add(search);
			for (String searchColumn : searchColumns)
			{
				sb.append('?');
				sb.append(searchColumn);
			}
		}
			
		if (px != null)
		{
			sb.append(px.getExtendCacheToken());
		}
		
		// Attempt to get from Cache
		String key = sb.toString();
		@SuppressWarnings("unchecked")
		CriteriaQuery<Long> criteria = (CriteriaQuery<Long>) queryCache.get(key);

		// Build Criteria
		if (criteria == null)
		{
			// Build Query
			CriteriaBuilder cb = em.getCriteriaBuilder();
			criteria = cb.createQuery(Long.class);
			Root<T> root = criteria.from(cls);
			criteria.select(cb.count(root));

			// Add Where Clause
			List<Predicate> predicates = new ArrayList<Predicate>();
			addWhereClause(filterColumns, filterOperators, cb, root, predicates);

			// Add Searching
			if (hasSearch)
			{
				addSearching(filterValues, cb, root, predicates, px, searchColumns);
			}

			if (px != null)
			{
				predicates = px.extend(cb, root, criteria, predicates);
			}

			criteria.where(predicates.toArray(new Predicate[predicates.size()]));

			// Cache the result
			queryCache.putIfAbsent(key, criteria);
		}

		// Add Parameters
		TypedQuery<Long> query = em.createQuery(criteria);
		addParameters(filterValues, query);
		if (px != null)
			px.addParameters(query);

		return query;

	}

	public static <T> void persist(EntityManager em, IMasterData<T> oldValue, IMasterData<T> newValue, Session session, String objectType, AuditEntryContext auditEntryContext ) throws RuleCheckException
	{
		try (RequiresTransaction transaction = new RequiresTransaction(em))
		{
			newValue.setLastTime(new Date());
			if(session.isUserIDValid())
			{
				newValue.setLastUserID(session.getUserID());
			}

			em.persist(newValue);
			AuditEntry.log(em, oldValue, newValue, session, objectType, auditEntryContext);
			transaction.commit();

		}
		catch (PersistenceException ex)
		{
			throw new RuleCheckException(ex, StatusCode.FAILED_TO_SAVE, null, ex.getMessage());
		}

	}
	
	/**
	 * @deprecated
	 * After execution of this method oldValue becomes unmanaged.
	 * Use mergeAndReturnManagedEntity() instead.
	 */
	@Deprecated
	public static <T> void merge(EntityManager em, IMasterData<T> oldValue, IMasterData<T> newValue, Session session, String objectType, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		logger.warn("Calling QueryBuilder.merge");
		try (RequiresTransaction transaction = new RequiresTransaction(em))
		{
			newValue.setLastTime(new Date());
			if(session.isUserIDValid()) //The userID in the session is not valid pre-authentication for incrementing attempts
				newValue.setLastUserID(session.getUserID());			
			em.merge(newValue);
			AuditEntry.log(em, oldValue, newValue, session, objectType, auditEntryContext);
			transaction.commit();

		}
		catch (PersistenceException ex)
		{
			throw new RuleCheckException(ex, StatusCode.FAILED_TO_SAVE, null, ex.getMessage());
		}

	}

	public static <T> IMasterData<T> mergeAndReturnManagedEntity(EntityManager em, IMasterData<T> oldValue, IMasterData<T> newValue, Session session,
																 String objectType, AuditEntryContext auditEntryContext) throws RuleCheckException {
		try (RequiresTransaction transaction = new RequiresTransaction(em)) {
			newValue.setLastTime(new Date());
			if (session.isUserIDValid()) { //The userID in the session is not valid pre-authentication for incrementing attempts
				newValue.setLastUserID(session.getUserID());
			}
			IMasterData<T> managedEntity = em.merge(newValue);
			AuditEntry.log(em, oldValue, newValue, session, objectType, auditEntryContext);
			transaction.commit();
			return managedEntity;
		} catch (PersistenceException ex) {
			throw new RuleCheckException(ex, StatusCode.FAILED_TO_SAVE, null, ex.getMessage());
		}
	}

	public static <T> void remove(EntityManager em, IMasterData<T> object, Session session, String objectType, AuditEntryContext auditEntryContext) throws RuleCheckException
	{
		try (RequiresTransaction transaction = new RequiresTransaction(em))
		{
			em.remove(object);
			AuditEntry.log(em, object, null, session, objectType, auditEntryContext);
			transaction.commit();
		}
		catch (PersistenceException ex)
		{
			throw new RuleCheckException(ex, StatusCode.FAILED_TO_DELETE, null, ex.getMessage());
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////

	private static <ReturnType, T> Path<ReturnType> sortExpression(Root<T> root, String column)
	{
		Path<ReturnType> result = null;
		String[] parts = column.split("\\.");
		for (String part : parts)
		{
			result = result == null ? root.<ReturnType>get(part) : result.<ReturnType>get(part);
		}

		return result;
	}

	private static <T extends Comparable<? super T>> void addCriterium(Class<T> cls, Path<T> column, String filterOperator, List<Predicate> predicates, CriteriaBuilder cb, String parameterName)
	{
		switch (filterOperator)
		{
			case "=":
				predicates.add(cb.equal(column, cb.parameter(cls, parameterName)));
				break;

			case "#":
			case "!=":
				predicates.add(cb.notEqual(column, cb.parameter(cls, parameterName)));
				break;

			case ">":
				predicates.add(cb.greaterThan(column, cb.parameter(cls, parameterName)));
				break;

			case "≥":
			case ">=":
				predicates.add(cb.greaterThanOrEqualTo(column, cb.parameter(cls, parameterName)));
				break;

			case "<":
				predicates.add(cb.lessThan(column, cb.parameter(cls, parameterName)));
				break;

			case "≤":
			case "<=":
				predicates.add(cb.lessThanOrEqualTo(column, cb.parameter(cls, parameterName)));
				break;
			/*case "~": TODO: DONO
				predicates.add(cb.in(expression))*/
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> void addWhereClause(List<String> filterColumns, List<String> filterOperators, CriteriaBuilder cb, Root<T> root, List<Predicate> predicates)
	{
		for (int index = 0; index < filterColumns.size(); index++)
		{
			String filterColumn = filterColumns.get(index);
			String filterOperator = filterOperators.get(index);
			String parameterName = String.format("Parm%d", index);
			if (":".equals(filterOperator))
			{
				Path<String> col = QueryBuilder.<String, T>sortExpression(root, filterColumn);
				predicates.add(cb.like(col, cb.parameter(String.class, parameterName)));
			}
			else
			{
				try
				{
					Path<?> column = QueryBuilder.sortExpression(root, filterColumn);
					Class<?> type = column.getJavaType();
					switch (type.getSimpleName())
					{
						case "int":
						case "Integer":
							Path<Integer> col1 = (Path<Integer>) column;
							addCriterium(Integer.class, col1, filterOperator, predicates, cb, parameterName);
							break;

						case "long":
						case "Long":
							Path<Long> col1a = (Path<Long>) column;
							addCriterium(Long.class, col1a, filterOperator, predicates, cb, parameterName);
							break;

						case "boolean":
							Path<Boolean> col2 = (Path<Boolean>) column;
							addCriterium(Boolean.class, col2, filterOperator, predicates, cb, parameterName);
							break;

						case "Date":
							Path<Date> col3 = (Path<Date>) column;
							addCriterium(Date.class, col3, filterOperator, predicates, cb, parameterName);
							break;

						case "String":
							Path<String> col4 = (Path<String>) column;
							addCriterium(String.class, col4, filterOperator, predicates, cb, parameterName);
							break;

						case "BigDecimal":
							Path<BigDecimal> col5 = (Path<BigDecimal>) column;
							addCriterium(BigDecimal.class, col5, filterOperator, predicates, cb, parameterName);
							break;
					}

				}
				catch (Exception ex)
				{
					logger.error("addWhereClause: caught exception", ex);
					if (!filterColumn.equals(COMPANY_ID) || !"=".equals(filterOperator))
						throw ex;
					ParameterExpression<Integer> param = cb.parameter(Integer.class, parameterName);
					predicates.add(cb.equal(param, param));
				}
			}

		}
	}

	private static <T> void addSearching(List<Object> filterValues, CriteriaBuilder cb, Root<T> root, List<Predicate> predicates, PredicateExtender<T> px, String... searchColumns)
	{
		List<Predicate> restrictions = new ArrayList<Predicate>();
		String parameterName = String.format("Parm%d", filterValues.size() - 1);
		for (String searchColumn : searchColumns)
		{
			Path<String> col = QueryBuilder.<String, T>sortExpression(root, searchColumn);
			restrictions.add(cb.like(col, cb.parameter(String.class, parameterName)));
		}
		if (px != null)
		{
			restrictions = px.addSearches(restrictions, root, cb, parameterName);
		}
		
		
		predicates.add(cb.or(restrictions.toArray(new Predicate[restrictions.size()])));
	}

	private static <T> void addParameters(List<Object> filterValues, TypedQuery<T> query)
	{
		int index = 0;
		for (Object value : filterValues)
		{
			String name = String.format("Parm%d", index++);
			Parameter<?> parameter = query.getParameter(name);
			Class<?> type = parameter.getParameterType();

			if (value != null && !value.getClass().equals(type))
			{
				switch (type.getSimpleName())
				{
					case "Integer":
						value = Integer.valueOf(value.toString());
						break;

					case "Long":
						value = Long.valueOf(value.toString());
						break;

					case "Boolean":
						value = "1".equals(value) || Boolean.valueOf(value.toString());
						break;

					case "BigDecimal":
						value = new BigDecimal(value.toString());
						break;

					case "Date":
						try
						{
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
							value = sdf.parse(value.toString());
						}
						catch (ParseException e)
						{
							// Will fail Later
							logger.error("", e);
							value = "??";
						}
						break;

					default:
						// TODO
						value = value.toString();
						break;
				}
			}

			query.setParameter(name, value);
		}
	}

	public static Integer getFoundRows(EntityManager em)
	{
		// MySql Specific!
		Query query = em.createNativeQuery("select found_rows();");
		BigInteger count = (BigInteger)query.getSingleResult();
		return count == null ? null : count.intValue();
	}


}
