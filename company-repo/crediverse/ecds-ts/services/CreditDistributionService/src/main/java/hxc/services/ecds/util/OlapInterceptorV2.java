package hxc.services.ecds.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.EmptyInterceptor;

public class OlapInterceptorV2 extends EmptyInterceptor
{
	private static final long serialVersionUID = 8578087278158340765L;

	@Override
	public String onPrepareStatement(String sql)
	{
		int loopCount = 0;
		String[] s = sql.split("\\*/");
		sql = (s.length > 1)? s[1].trim() : s[0].trim();

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
			//	if (dotIdx -1) ? tableId.substring(0,dotIdx) : tableId;
			String tableName = tableId.substring(0, dotIdx);
			int tableIdx = sql.indexOf(" " + tableName + " ");
			if (tableIdx < 0)
			{
				throwError("unknown table name in useindex(table, index)", sql);
			}

			Pattern pattern = Pattern.compile("( [^ ]+ )" + tableName + " ");
			Matcher m = pattern.matcher(sql);
			m.find();
			String entity = m.group(1);
			String[] sqlParts = sql.split(entity);

			String newSql = sqlParts[0];
			for (int x = 0; x < sqlParts.length - 1; x++)
			{
				// Look ahead to the name of the table
				int nxtSpace = sqlParts[x + 1].indexOf(" ");
				String localTable = sqlParts[x + 1].substring(0, nxtSpace);
				newSql += entity + localTable;
				newSql += " use index(" + indexHint.replaceAll("'","") + ")";
				for(int a = x + 1;a < sqlParts.length;a++)
				{
					//remove all useindex for this table
					sqlParts[a] = sqlParts[a].replaceAll("useindex\\(" + localTable + "[^=]+=1 [^ ]+ ", "");
				}
				newSql += sqlParts[x + 1].substring(nxtSpace);
			}
			sql = newSql;
			if(sql.indexOf("useindex") == -1) break;
			loopCount++;
		}

		return sql;
	}

	protected void throwError(String message, String sql)
	{
		throw new IllegalStateException(String.format("%s [%s]", message, sql));
	}
}
