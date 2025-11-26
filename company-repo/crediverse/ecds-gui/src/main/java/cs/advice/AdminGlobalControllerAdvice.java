package cs.advice;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cs.config.ApplicationDetailsConfiguration;
import cs.config.BuildInfo;
import cs.constants.ApplicationConstants;
import cs.dto.security.LoginSessionData;
import cs.service.ContextService;
import cs.service.CorrelationIdService;
import cs.utility.Common;

@ControllerAdvice
@Profile(Common.CONST_ADMIN_PROFILE)
public class AdminGlobalControllerAdvice
{
	private static final Logger logger = LoggerFactory.getLogger(AdminGlobalControllerAdvice.class);
	
	@Value("#{servletContext.contextPath}")
	private String servletContextPath;

	@Autowired
	protected LoginSessionData sessionData;

	@Autowired
	protected ContextService contextService;

	@Autowired
	protected CorrelationIdService correlationIdService;

	@Value("${cs.application.version:none}")
	protected String projectVersion;

	@Autowired
	private ApplicationDetailsConfiguration appConfig;

	@ModelAttribute(ApplicationConstants.CONST_PROJECT_LOGIN_VERSION)
	public String getProjectLoginVersion(HttpServletRequest request)
	{
		StringBuilder runningVersion = new StringBuilder();
		if (Common.isDevelopment())
		{
			runningVersion.append("js/login/config/admin");
		}
		else
		{
			runningVersion.append("/js/adlogin-");
			runningVersion.append(projectVersion);
		}
		request.setAttribute(ApplicationConstants.CONST_PROJECT_VERSION, runningVersion.toString());
		return runningVersion.toString();
	}

	@ModelAttribute(ApplicationConstants.CONST_PROJECT_VERSION)
	public String getProjectVersion(HttpServletRequest request)
	{
		StringBuilder runningVersion = new StringBuilder();
		if (Common.isDevelopment())
		{
			runningVersion.append("/js/app/config/init");
		}
		else
		{
			runningVersion.append("/js/main-");
			runningVersion.append(projectVersion);
		}
		request.setAttribute(ApplicationConstants.CONST_PROJECT_VERSION, runningVersion.toString());
		return runningVersion.toString();
	}

	@ModelAttribute(ApplicationConstants.CONST_CORRELATIONID)
	public String getCorrelationId(HttpServletRequest request)
	{
		String id = correlationIdService.getUniqueId();
		request.setAttribute(ApplicationConstants.CONST_CORRELATIONID, id);
		return id;
	}

	@ModelAttribute(ApplicationConstants.CONST_INCOMINGIP)
	public String getSourceIP()
	{
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		if (sessionData != null)
		{
			sessionData.setIncomingIp(curRequest.getLocalAddr());
		}
		return curRequest.getLocalAddr();
	}

	@ModelAttribute(ApplicationConstants.CONST_REQUESTIP)
	public String getRequestIP()
	{
		HttpServletRequest curRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
		if (sessionData != null)
		{
			sessionData.setIpAddress(curRequest.getRemoteAddr());
		}
		return curRequest.getRemoteAddr();
	}

	@ModelAttribute(ApplicationConstants.CONST_APP_VERSION)
	public String getAppVersion()
	{
		try
		{
			return BuildInfo.DOCKER_TAG;
//			return contextService.getContextData().getAppVersion();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return "";
	}

	@ModelAttribute(ApplicationConstants.CONST_CONTEXT_PATH)
	public String getContextPath()
	{
		StringBuilder path = new StringBuilder(servletContextPath);
		if (!servletContextPath.endsWith("/"))
		{
			path.append("/");
		}
		return path.toString();
	}

	@ModelAttribute(ApplicationConstants.CONST_BRANDING_BASE)
	public String getBrandingPath()
	{
		return sessionData.getBrandingBase();
	}

	@ModelAttribute(ApplicationConstants.CONST_USER_LANGUAGE)
	public String getUserLanguage()
	{
		Locale locale = sessionData.getLocale();
		return (locale != null)? sessionData.getLocale().getLanguage() : "fr";
	}
}
