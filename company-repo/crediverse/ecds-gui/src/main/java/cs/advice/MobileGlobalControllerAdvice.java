package cs.advice;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import cs.constants.ApplicationConstants;
import cs.utility.Common;

@ControllerAdvice
@Profile(Common.CONST_MOBILE_PROFILE)
public class MobileGlobalControllerAdvice extends AdminGlobalControllerAdvice
{

	@Override
	@ModelAttribute(ApplicationConstants.CONST_PROJECT_LOGIN_VERSION)
	public String getProjectLoginVersion(HttpServletRequest request)
	{
		StringBuilder runningVersion = new StringBuilder();
		if (Common.isDevelopment())
		{
			runningVersion.append(ApplicationConstants.CONST_MOBILE_DEV_LOGIN_JS_LOCATION);
		}

		return runningVersion.toString();
	}

}
