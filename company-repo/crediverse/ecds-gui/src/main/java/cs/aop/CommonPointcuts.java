package cs.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import cs.security.permissions.RequiredPermission;

@Aspect
@Component
public class CommonPointcuts
{
	@Pointcut("within(@org.springframework.stereotype.Controller *) && " + "@annotation(requestMapping) && " + "execution(* *(..))")
	public void controller(RequestMapping requestMapping)
	{
	}

	// @Pointcut("execution(* cs.spring.controller.*.*(..))")
	@Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
	public void allSpringControllerMethods()
	{
	}

	@Pointcut("within(@org.springframework.stereotype.Service *)")
	public void beanAnnotatedWithService() {}

	@Pointcut("execution(public * *(..))")
	public void publicMethod() {}

	@Pointcut("publicMethod() && beanAnnotatedWithService()")
	public void publicMethodInsideOfService() {}

	@Pointcut("execution(public * cs..*(..))")
	public void allCsPublicMethods() {}

	@Pointcut("@annotation(requiredPermission)")
	public void validateRequiredPermission(RequiredPermission requiredPermission ) {}


	/*
	 * @Pointcut("execution(public * cs.plugin.*.controller.*.*(..))") public void webController() { }
	 *
	 * @Pointcut("execution(public * cs.plugin..*(..))") public void pluginBean() { }
	 *
	 * @Pointcut( "@annotation(org.springframework.web.bind.annotation.RequestMapping") public void webControllerMethods() {
	 *
	 * }
	 */
}
