package hxc.services.ecds.util;

import org.hibernate.EmptyInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OlapInterceptorV3 extends EmptyInterceptor
{
	final static Logger logger = LoggerFactory.getLogger(OlapInterceptorV3.class);
	private static final long serialVersionUID = 8578087278158340765L;

	@Override
	public String onPrepareStatement(String sql)
	{
		//logger.trace("query = {}", sql);
		String tmp = sql;
		tmp = tmp.replace("where useindex_rpr()=? and", "use index (ap_transact_aggregate000) where 1 = ? and");
		tmp = tmp.replace("where useindex_wpr()=? and", "use index (ap_transact_aggregate000) where 1 = ? and");
		tmp = tmp.replace("where useindex_ssr()=? and", "use index (ap_transact_aggregate000) where 1 = ? and");
		tmp = tmp.replace("where useindex_rproa()=? and", "use index (ap_transact_aggregate001) where 1 = ? and");
		tmp = tmp.replace("where useindex_wproa()=? and", "use index (ap_transact_aggregate001) where 1 = ? and");
		tmp = tmp.replace("where useindex_wprob()=? and", "use index (ap_transact_aggregate002) where 1 = ? and");
		tmp = tmp.replace("where useindex_gsr()=? and", "use index (ap_transact_a_group_name) where 1 = ? and");
		//logger.trace("fixed_query = {}", tmp);
		//throwError("query = %s", sql);
		return tmp;
	}

	protected void throwError(String message, Object... args)
	{
		throw new IllegalStateException(String.format(message, args));
	}
}
