package cs.utility;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.web.util.UriComponentsBuilder;

public class RestRequestUtil
{
	/*
	 * These constants are currently defined in hxc.services.ecds.rest
	 * so duplicated here as hxc.services.ecds.rest is not part of the
	 * shared protocol jar.  Have requested that it be moved to 
	 * hxc.ecds.protocol.rest and shared with us.
	 * 
	 * Will update these references once the constants are shared.
	 */
	public static final String SID = "CS_SID";
	public static final String FIRST = "first";
	public static final String MAX = "max";
	public static final String SORT = "sort";	// age+gender-
	public static final String SEARCH = "search";	
	public static final String FILTER = "filter";	
	public static final String WITHCOUNT = "withcount";
	public static final String VIRTUAL_PREFIX = "virtual_";
	
	// channel='W'+type:'%R%'+amount>='100'+amount<='100000'+startTime>='20160820T115400'+startTime<='20160920T115400'
	
	private static final int HARDLIMIT = 1000;
	
	static public void standardLimit(UriComponentsBuilder uri, int limit)
	{
		if (limit > 0) uri.replaceQueryParam(MAX, String.valueOf(limit));
	}
	
	static public void standardLimit(UriComponentsBuilder uri)
	{
		uri.replaceQueryParam(MAX, String.valueOf(HARDLIMIT));
	}
	
	static public void standardSearch(UriComponentsBuilder uri, String query, int limit) throws UnsupportedEncodingException
	{
		if (query != null && query.length() > 0)
		{
			StringBuilder searchParam = new StringBuilder("%");
			searchParam.append(query).append('%');
			uri.replaceQueryParam(SEARCH, URLEncoder.encode(searchParam.toString(),"UTF-8" ).replaceAll("\\+", "%20"));
		}
		if (limit > 0) uri.queryParam(MAX, String.valueOf(limit));
	}
	
	static public void standardSearch(UriComponentsBuilder uri, String query) throws UnsupportedEncodingException
	{
		if (query != null && query.length() > 0)
		{
			StringBuilder searchParam = new StringBuilder("%");
			searchParam.append(query).append('%');
			uri.replaceQueryParam(SEARCH, URLEncoder.encode(searchParam.toString(),"UTF-8" ).replaceAll("\\+", "%20"));
		}
	}
	
	static public void standardPaging(UriComponentsBuilder uri, Integer offset, Integer limit)
	{
		if (offset != null && offset >= 0) uri.replaceQueryParam(FIRST, String.valueOf(offset));
		if (limit != null && limit >= 0) uri.replaceQueryParam(MAX, String.valueOf(limit));
	}
	
	static public void standardPaging(UriComponentsBuilder uri, int offset, int limit)
	{
		if (offset >= 0) uri.replaceQueryParam(FIRST, String.valueOf(offset));
		if (limit >= 0) uri.replaceQueryParam(MAX, String.valueOf(limit));
	}
	
	static public void standardPaging(UriComponentsBuilder uri, long offset, int limit)
	{
		if (offset >= 0) uri.replaceQueryParam(FIRST, String.valueOf(offset));
		if (limit >= 0) uri.replaceQueryParam(MAX, String.valueOf(limit));
	}
	
	static public void standardFilter(UriComponentsBuilder uri, String filter) throws UnsupportedEncodingException
	{
		if (filter != null && filter.length() > 0)
		{
			String escaped = URLEncoder.encode(filter,"UTF-8").replaceAll("\\+", "%20");
			uri.replaceQueryParam(FILTER, escaped);
		}
	}
	
	static public void virtualFilter(UriComponentsBuilder uri, String name, String value) throws UnsupportedEncodingException
	{
		if (name != null && name.length() > 0 && value != null && value.length() > 0 && name.startsWith(VIRTUAL_PREFIX))
		{
			String escaped = URLEncoder.encode(value,"UTF-8").replaceAll("\\+", "%20");
			uri.replaceQueryParam(name, escaped);
		}
	}
	
	static public void standardSorting(UriComponentsBuilder uri, String sort) throws UnsupportedEncodingException
	{
		if (sort != null && sort.length() > 0) uri.replaceQueryParam(SORT, URLEncoder.encode(sort,"UTF-8" ).replaceAll("\\+", "%20"));
	}
	
	static public void withRecordCount(UriComponentsBuilder uri, boolean withCount)
	{
		uri.replaceQueryParam(WITHCOUNT, String.valueOf(withCount?1:0));
	}
}
