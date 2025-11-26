package cs.advice;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import cs.constants.ApplicationConstants;
import cs.utility.Common;

@ControllerAdvice
@Profile(Common.CONST_PORTAL_PROFILE)
public class PortalGlobalControllerAdvice extends AdminGlobalControllerAdvice
{

	@Override
	@ModelAttribute(ApplicationConstants.CONST_PROJECT_LOGIN_VERSION)
	public String getProjectLoginVersion(HttpServletRequest request)
	{
		StringBuilder runningVersion = new StringBuilder();
		if (Common.isDevelopment())
		{
			runningVersion.append(ApplicationConstants.CONST_PORTAL_DEV_LOGIN_JS_LOCATION);
		}
		else
		{
			runningVersion.append(ApplicationConstants.CONST_PORTAL_LOGIN_JS_PREFIX);
			runningVersion.append(projectVersion);
		}
		request.setAttribute(ApplicationConstants.CONST_PROJECT_VERSION, runningVersion.toString());
		return runningVersion.toString();
	}

	@Override
	@ModelAttribute(ApplicationConstants.CONST_PROJECT_VERSION)
	public String getProjectVersion(HttpServletRequest request)
	{
		StringBuilder runningVersion = new StringBuilder();
		if (Common.isDevelopment())
		{
			runningVersion.append(ApplicationConstants.CONST_PORTAL_DEV_APP_JS_LOCATION);
		}
		else
		{
			runningVersion.append(ApplicationConstants.CONST_PORTAL_APP_JS_PREFIX);
			runningVersion.append(projectVersion);
		}
		request.setAttribute(ApplicationConstants.CONST_PROJECT_VERSION, runningVersion.toString());
		return runningVersion.toString();
	}
}
