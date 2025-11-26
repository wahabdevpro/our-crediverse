package hxc.services.ecds.util;

import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public abstract class PredicateExtender<T>
{
	public abstract String getName();

	public List<Predicate> extend(CriteriaBuilder cb, Root<T> root, CriteriaQuery<?> query, List<Predicate> predicates)
	{
		return predicates;
	}
	
	public String getExtendCacheToken()
	{
		return "";
	}

	public void addParameters(TypedQuery<?> query)
	{

	}

	public List<Predicate> addSearches(List<Predicate> searches, Root<T> root, CriteriaBuilder cb, String parameterName)
	{
		return searches;
	}

	protected static <T> Path<?> col(Root<T> root, String column)
	{
		Path<Object> result = null;
		String[] parts = column.split("\\.");
		for (String part : parts)
		{
			result = result == null ? root.get(part) : result.get(part);
		}

		return result;
	}

	public boolean preProcessFilter(String column, String operator, String value)
	{
		return true;
	}

}
