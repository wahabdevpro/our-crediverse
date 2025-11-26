package hxc.services.ecds.util;

import org.hibernate.EmptyInterceptor;

public class OlapInterceptor extends EmptyInterceptor
{
	private static final long serialVersionUID = 8578087278158340765L;

	@Override
	public String onPrepareStatement(String sql)
	{
		while (true)
		{
			// check if function specified
			int idx = sql.indexOf("useindex(");
			if (idx < 0) { break; }

			// find end of function
			int endidx = sql.indexOf(")=1", idx);
			if (endidx < idx)
			{
				throwError("expected useindex(table, index) is true", sql);
			}

			// get both parameters
			String[] params = sql.substring(idx + 9, endidx).split(",");
			if (params.length != 2)
			{
				throwError("expected 2 parameters to useindex(table, index)", sql);
			}

			// trim parameters and verify
			String tableId = params[0].trim();
			String indexHint = params[1].trim();
			if (tableId.length() == 0 || indexHint.length() == 0)
			{
				throwError("invalid parameters to useindex(table, index)", sql);
			}

			// find actual table name minus id
			int dotIdx = tableId.indexOf('.');
			if (dotIdx < 0)
			{
				throwError("invalid table name in useindex(table, index)", sql);
			}

			// find table name within declaration
			String tableName = tableId.substring(0, dotIdx);
			int tableIdx = sql.indexOf(" " + tableName + " ");
			if (tableIdx < 0)
			{
				throwError("unknown table name in useindex(table, index)", sql);
			}

			// remove useindex function from predicate
			String predicate = sql.substring(endidx + 3);
			if (predicate.startsWith(" and "))
			{
				predicate = predicate.substring(5);
			}

			// inject use index after table declaration
			sql = sql.substring(0, tableIdx + 2 + tableName.length()) +
				  "use index (" + indexHint + ") " +
				  sql.substring(tableIdx + 2 + tableName.length(), idx) +
				  predicate;
		}

		return sql;
	}

	protected void throwError(String message, String sql)
	{
		throw new IllegalStateException(String.format("%s [%s]", message, sql));
	}
}
