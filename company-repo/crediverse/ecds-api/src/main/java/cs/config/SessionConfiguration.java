package cs.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.context.WebApplicationContext;

import cs.dto.security.AuthenticationData;
import cs.dto.security.LoginSessionData;
import cs.service.AccountService;

@Configuration
public class SessionConfiguration
{
	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
	
	@Bean
	@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = WebApplicationContext.SCOPE_REQUEST)
	public LoginSessionData getSessionData()
	{
		LoginSessionData sess = new LoginSessionData();
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();        
		Object details = authentication.getDetails();
		if ( details instanceof OAuth2AuthenticationDetails )
		{
		    OAuth2AuthenticationDetails oAuth2AuthenticationDetails = (OAuth2AuthenticationDetails)details;
		    AuthenticationData authData = new AuthenticationData();
		    sess.setSessionAuthentication(authData);
		    @SuppressWarnings("unchecked")
			Map<String, Object> decodedDetails = (Map<String, Object>)oAuth2AuthenticationDetails.getDecodedDetails();
		    String sessionId;
		    String username;
		    Integer agentUserID;
		    Integer agentID;
		    Integer webUserID;
		    String agentMsisdn;
		    String agentUserMsisdn;
		    
		    if(decodedDetails.containsKey("sid"))
		    {
		    	sessionId = decodedDetails.get("sid").toString();
		    	authData.setServerSessionID(sessionId);
		    	sess.setMySessionId(sessionId);
		    }
		    else
		    	logger.error("Missing attribute in JWT: sid");
		    
		    if(decodedDetails.containsKey("username"))
		    {
		    	username = decodedDetails.get("username").toString();
		    	authData.setUsername(username);
		    }
		    else
		    	logger.warn("Missing attribute in JWT: username");
		    
		    if(decodedDetails.containsKey("agentUserID"))
		    {
		    	agentUserID = (Integer) decodedDetails.get("agentUserID");
		    	sess.setAgentUserID(agentUserID);
		    }
		    else
		    	logger.warn("Missing attribute in JWT: agentUserID");
		    
		    if(decodedDetails.containsKey("agentID"))
		    {
		    	agentID = (Integer) decodedDetails.get("agentID");
		    	sess.setAgentId(agentID);
		    }
		    else
		    	logger.warn("Missing attribute in JWT: agentID");
		    
		    if(decodedDetails.containsKey("webUserID"))
		    {
		    	webUserID = (Integer) decodedDetails.get("webUserID");
		    	sess.setWebUserId(webUserID);
		    }
		    else
		    	logger.warn("Missing attribute in JWT: webUserID");
		    
		    if(decodedDetails.containsKey("agentMsisdn") && decodedDetails.get("agentMsisdn") != null)
		    {
		    	agentMsisdn = decodedDetails.get("agentMsisdn").toString();
		    	sess.setAgentMsisdn(agentMsisdn);
		    }
		    else
		    	logger.warn("Missing attribute in JWT: agentMsisdn");
		    
		    if(decodedDetails.containsKey("agentUserMsisdn") && decodedDetails.get("agentUserMsisdn") != null)
		    {
		    	agentUserMsisdn = decodedDetails.get("agentUserMsisdn").toString();
		    	sess.setAgentUserMsisdn(agentUserMsisdn);
		    }
		    else
		    	logger.warn("Missing attribute in JWT: agentUserMsisdn");
		}
		return sess;
	}
}
