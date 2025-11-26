package cs.utility;

import org.slf4j.Logger;

public class AopUtil
{
	public static void printArgs(Logger logger, Object[] args)
	{
		if (logger.isDebugEnabled() && args != null && args.length > 0)
		{
			logger.debug("#### START OF ARGS ####");
			StringBuilder result = new StringBuilder();
			for (Object arg : args)
			{
				if (arg != null)
				{
					result.append(" [[");
					result.append(arg.toString());
					result.append("]]");
				}
			}
			logger.debug(result.toString());
			logger.debug("#### END OF ARGS ####");
		}
	}
}
