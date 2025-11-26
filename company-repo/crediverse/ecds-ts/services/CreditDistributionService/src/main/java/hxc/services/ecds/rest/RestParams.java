package hxc.services.ecds.rest;

public class RestParams
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String SID = "CS_SID";
	public static final String FIRST = "first";
	public static final String MAX = "max";
	public static final String SORT = "sort";
	public static final String SEARCH = "search";
	public static final String FILTER = "filter";
	public static final String WITHCOUNT = "withcount";
	public static final String INCLUDEQUERY = "includequery";
	public static final String INCLUDE_EXTRA_DATA = "includeExtraData";
	public static final int DEFAULT_MAX_RESULTS = 10000;
	public static final String FETCH_SUBSCRIBER_STATE = "fetchSubscriberState";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private String sessionID;
	private int first = 0;
	private int max = -1;
	private String sort;
	private String search;
	private String filter;
	private boolean includeQuery;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getSessionID()
	{
		return sessionID;
	}

	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	public int getFirst()
	{
		return first;
	}

	public void setFirst(int first)
	{
		this.first = first;
	}

	public int getMax()
	{
		return max;
	}

	public void setMax(int max)
	{
		this.max = max;
	}

	public String getSort()
	{
		return sort;
	}

	public void setSort(String sort)
	{
		this.sort = sort;
	}

	public String getSearch()
	{
		return search;
	}

	public void setSearch(String search)
	{
		this.search = search;
	}

	public String getFilter()
	{
		return filter;
	}

	public void setFilter(String filter)
	{
		this.filter = filter;
	}
	
	public boolean isIncludeQuery() {
		return includeQuery;
	}

	public void setIncludeQuery(boolean includeQuery) {
		this.includeQuery = includeQuery;
	}

	public String describe(String extra)
	{
		return String.format("%s@%s(sessionID = %s, first = %s, max = %s, sort = %s, search = %s, filter = %s, includeQuery = %s%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			sessionID, first, max, sort, search, filter, includeQuery,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	// /////////////////////////////////
	public RestParams(String sessionID, int first, int max, String sort, String search, String filter)
	{
		this.sessionID = sessionID;
		this.first = first;
		this.max = max;
		this.sort = sort;
		this.search = search;
		this.filter = filter;
		this.includeQuery = true;
	}
	
	public RestParams(String sessionID)
	{
		this.sessionID = sessionID;
	}
}
