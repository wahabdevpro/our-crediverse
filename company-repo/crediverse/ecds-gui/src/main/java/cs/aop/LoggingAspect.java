package cs.aop;

import cs.service.CorrelationIdService;
import cs.utility.AopUtil;
import cs.utility.Common;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect
{
	@Autowired
	private CorrelationIdService correlationIdService;
	private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

	public void afterThrowing(JoinPoint joinPoint, Throwable e) {
		logger.error("Correlation ID: " + correlationIdService.getUniqueId(), e);
	}

	//@Around("cs.aop.CommonPointcuts.allCsPublicMethods()")
	public Object around(ProceedingJoinPoint point) throws Throwable
	{
		long start = System.currentTimeMillis();
		Object result = null;
		result = point.proceed();
		/*
		 * Left as an example for now.
		 */
		// final MethodSignature signature = (MethodSignature)
		// point.getSignature();
		// final String[] parameterNames = signature.;
		Signature signature = point.getSignature();
		String method = signature.getDeclaringTypeName() + "." + signature.getName();
		if (!method.startsWith("get") && !method.startsWith("set"))
		{
			if (logger.isTraceEnabled()||logger.isInfoEnabled() || Common.isDevelopment())
			{
				logger.debug("####### " + method + " START #######");
				AopUtil.printArgs(logger, point.getArgs());
				if (result != null)
				{
					logger.info("Result " + result.toString());
				}
				logger.debug("Execution Time " + (System.currentTimeMillis() - start) + " ms");
				logger.debug("####### " + method + " END #######");
			}
			else
			{
				//if (Common.isDevelopment())
					logger.info("## " + method + " : " + (System.currentTimeMillis() - start) + " ms");
			}
		}
		return result;
	}
}
