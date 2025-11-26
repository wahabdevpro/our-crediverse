package hxc.services.ecds.util;

import org.hibernate.EmptyInterceptor;

public class MySqlInterceptor extends EmptyInterceptor
{
	private static final long serialVersionUID = 389645839735056826L;
	private static final String SELECT = "select ";
	private static final String LIMIT = "limit";
	private static final String SQL_CALC_FOUND_ROWS = SELECT + "SQL_CALC_FOUND_ROWS ";

	@Override
	public String onPrepareStatement(String sql)
	{
		if (sql != null && sql.startsWith(SELECT) && sql.contains(LIMIT))
		{
			sql = SQL_CALC_FOUND_ROWS + sql.substring(SELECT.length());
		}

		return sql;
	}

}
