package cs.utility;

import java.util.Map;

public class SearchFilter
{
	public enum SearchOperations
	{
		IdEquals,
		Equals,
		Like,
	}

	private Map<String, String> pageParameters = null;
	private StringBuilder filter;

	private SearchFilter(Map<String, String> pageParameters)
	{
		this.pageParameters = pageParameters;
		this.filter = new StringBuilder();
	}

	public static SearchFilter createBasFilter(Map<String, String> pageParameters)
	{
		return new SearchFilter(pageParameters);
	}

	private void addFilter(String name, String operator, String value)
	{
		if ( !value.equals("")  && !value.equals("null"))
		{
			if (filter.length() > 0)
				filter.append("+");

			filter.append( String.format("%s%s'%s'", name, operator, value) );
		}
	}

	private long convertLongValue(String value)
	{
		try
		{
			return Long.valueOf(value.trim());
		}
		catch(Exception e) {}

		return 0;
	}

	public SearchFilter filter(String parameter, SearchOperations operation, String dbField)
	{
		String value = pageParameters.get(parameter);

		if (value != null)
		{
			switch(operation)
			{
				case IdEquals:
					long lValue = convertLongValue(value);
					addFilter(dbField, "=", String.valueOf(lValue));
					break;
				case Like:
					addFilter(dbField, ":", value.trim().replaceFirst("[*]$", "%"));
					break;
				case Equals:
					if ( value.substring(0, 1).equals( "~" ) )
						addFilter(dbField, "!=", value.substring(1));
					else
						addFilter(dbField, "=", value);
			}
		}

		return this;
	}

	@Override
	public String toString()
	{
		return filter.toString();
	}

}
