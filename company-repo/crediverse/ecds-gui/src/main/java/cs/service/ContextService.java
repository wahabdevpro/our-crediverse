package cs.service;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;

import cs.config.ApplicationDetailsConfiguration;
import cs.config.BuildInfo;
import cs.config.RestServerConfiguration;
import cs.constants.ApplicationConstants;
import cs.dto.ContextCache;
import cs.dto.User;
import cs.dto.data.ContextData;
import cs.dto.data.ContextData.UserType;
import cs.dto.error.GuiGeneralException;
import cs.dto.security.LoginSessionData;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.Session;

@Service
public class ContextService
{
	private Map<String, Map<String, ContextCache>> contextCacheMap;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private ApplicationDetailsConfiguration appConfig;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private CorrelationIdService correlationIdService;

	@PostConstruct
	public void configure()
	{
		contextCacheMap = new HashMap<String, Map<String, ContextCache>>();
	}

	public void clearSession(String sessionId)
	{
		if (contextCacheMap.containsKey(sessionId))
		{
			contextCacheMap.remove(sessionId);
		}
	}


	public String getLogoFileName()
	{
		String logoFilename = ApplicationConstants.CONST_DEFAULT_LOGO_FILENAME;
		try
		{
			File logoDir = new File("/var/opt/cs/ecdsui/logo");
			File currentLogoFile = null;
			for (File currentFile : logoDir.listFiles())
			{
				if (currentFile.isFile() && currentFile.canRead())
				{
					if (currentLogoFile == null || currentLogoFile.lastModified() < currentFile.lastModified())
					{
						currentLogoFile = currentFile;
					}
				}
			}
			if (currentLogoFile != null) logoFilename = restServerConfig.getLogodir() + "/" + currentLogoFile.getName() + "?t=" + String.valueOf(currentLogoFile.lastModified());
		}
		catch(Exception ex)
		{
			logoFilename = ApplicationConstants.CONST_DEFAULT_LOGO_FILENAME+"?t=1";
		}
		return logoFilename;
	}

	private DecimalFormatSymbols getSeperatorSymbols(Locale lcl)
	{
		return ((DecimalFormat) DecimalFormat.getInstance(lcl)).getDecimalFormatSymbols();
	}

	public void cachePut(ContextCache item)
	{
		String key = item.getKey();
		if (key != null && key.length() > 3)
		{
			String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
			if (!contextCacheMap.containsKey(sessionId))
			{
				contextCacheMap.put(sessionId, new HashMap<String, ContextCache>());
			}

			Map<String, ContextCache>sessionCache = contextCacheMap.get(sessionId);
			item.setSessionId(sessionId);
			sessionCache.put(key, item);
		}
	}

	public ContextCache cacheGet(String key)
	{
		ContextCache result = null;
		String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
		if (contextCacheMap.containsKey(sessionId))
		{
			Map<String, ContextCache>sessionCache = contextCacheMap.get(sessionId);
			if (sessionCache.containsKey(key))
			{
				result = sessionCache.get(key);
			}
		}
		return result;
	}

	public ContextData getContextData() throws Exception
	{
		ContextData ctxt = new ContextData();
		ctxt.setAppName(appConfig.getName());
		ctxt.setAppVersion(BuildInfo.DOCKER_TAG);
		ctxt.setGithubTag(BuildInfo.GITHUB_TAG);
		ctxt.setBranchName(BuildInfo.BRANCH_NAME);
		ctxt.setBuildNumber(BuildInfo.BUILD_NUMBER);
		ctxt.setBuildDateTime(BuildInfo.BUILD_DATE_TIME);
		ctxt.setCommitRef(BuildInfo.BUILD_COMMIT_REF);
		ctxt.setKeepalive(appConfig.getKeepalive() * 1000);
		ctxt.setLogoFilename(getLogoFileName());

		ctxt.setCompanyPrefix(sessionData.getCompanyPrefix());

		ctxt.addSeperatorSet("en", getSeperatorSymbols(Locale.ENGLISH));
		ctxt.addSeperatorSet("fr", getSeperatorSymbols(Locale.FRENCH));

		Locale lcl = sessionData.getLocale();
		if (lcl != null)
		{
			ctxt.setLanguageID(lcl.getLanguage());
			ctxt.setCountryID(lcl.getCountry());
		}

		SecurityContext context = SecurityContextHolder.getContext();
		Authentication auth = context.getAuthentication();
		if (auth != null)
		{
			Object principal = auth.getPrincipal();
			if (principal instanceof String)
			{
				ctxt.setUser(null);
			}
			else if (principal instanceof User)
			{
				ctxt.setUser((User)principal);
				// GET /ecds/sessions/{SessionID}
				updateUserType(ctxt);
			}
			else
			{
				// log error
			}
		}
		else
		{
			ctxt.setUser(null);
		}

		if (ctxt.getLanguageID() == null)
		{

		}
		ctxt.setCompanyID(appConfig.getCompanyid());
		ctxt.setJsDebug(appConfig.isJsdebug());
		sessionData.setCompanyID(appConfig.getCompanyid());
		getSessionContextData();

		return ctxt;
	}

	private void updateUserType(ContextData ctxt)
	{
		if (sessionData.getWebUserId() != null)
		{
			ctxt.setUserType( UserType.WebUser );
		}
		else if (sessionData.getAgentUserID() != null)
		{
			ctxt.setUserType( UserType.UserAgent );
		}
		else
		{
			ctxt.setUserType( UserType.Agent );
		}
	}

	private Session getSessionContextData(boolean verify) throws Exception
	{
		Session session = null;
		String sessionId = sessionData.getServerSessionID();
		if (sessionId != null && sessionId.length() > 0)
		{
			String sessionServiceUrl = String.format("%s%s/%s", restServerConfig.getRestServer(), restServerConfig.getSessionurl(), sessionId);
			session = restTemplate.execute(sessionServiceUrl, HttpMethod.GET, Session.class);
			//sessionData.updateSession(session);
		}
		else
		{
			if (verify)
			{
				GuiGeneralException ex = new GuiGeneralException("Invalid transaction server session id");
				ex.setErrorCode(HttpStatus.UNAUTHORIZED);
				ex.setAdditional("Invalid transaction server session id");
				ex.setServerCode(String.valueOf(HttpStatus.UNAUTHORIZED));
				ex.setCorrelationId(correlationIdService.getUniqueId());
				ex.fillInStackTrace();
				throw ex;
			}
		}
		return session;
	}

	public Session getSessionContextData() throws Exception
	{
		return getSessionContextData(true);
	}

	public int getCompanyID()
	{
		return appConfig.getCompanyid();
	}
}
