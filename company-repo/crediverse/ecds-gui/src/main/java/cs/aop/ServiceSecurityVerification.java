package cs.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cs.dto.security.LoginSessionData;
import cs.security.permissions.EcdsPermissionException;
import cs.security.permissions.RequiredPermission;
import cs.security.permissions.RequiredPermissions;

@Aspect
@Component
public class ServiceSecurityVerification
{
	private static final Logger logger = LoggerFactory.getLogger(ServiceSecurityVerification.class);

	@Autowired
	private LoginSessionData sessionData;

	@Before("cs.aop.CommonPointcuts.validateRequiredPermission(requiredPermission)")
	public void validateRequiredPermission(RequiredPermission requiredPermission) throws EcdsPermissionException
	{
		logger.debug("Checking permission for %s:%s", requiredPermission.group(), requiredPermission.name());

		if (!sessionData.getCurrentUser().hasPermission(requiredPermission.group(), requiredPermission.name()))
		{
			throw new EcdsPermissionException("Permission %s:%s Denied", requiredPermission.group(), requiredPermission.name());
		}
	}

//	@RequiredPermission(group="", permission="")
	//@Before("cs.aop.CommonPointcuts.publicMethodInsideOfService()")
	public void logBeforeAllMethods(JoinPoint joinPoint) throws Exception
	{
		RequiredPermissions.MatchType matchType;
		logger.info("****LoggingAspect.logBeforeAllMethods() : " + joinPoint.getSignature().getName());
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		Method method = signature.getMethod();

		RequiredPermissions permissionList = method.getAnnotation(RequiredPermissions.class);
		if (permissionList != null)
		{
			matchType = permissionList.match();
			for (RequiredPermission permission : permissionList.permissions())
			{
				boolean result = hasPermission(permission);
				switch(matchType)
				{
				case ANY:
					if (result)
					{
						return;
					}
					break;
				case All:
					if (result)
					{
						throw new Exception("Permission Denied");
					}
					break;
				default:
					break;
				}
			}
		}
	}

	private boolean hasPermission(RequiredPermission permission) throws Exception
	{
		boolean result = true;

		return result;
	}
}
