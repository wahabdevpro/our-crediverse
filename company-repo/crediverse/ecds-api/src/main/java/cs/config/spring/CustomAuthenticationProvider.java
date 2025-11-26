package cs.config.spring;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import cs.dto.data.BaseResponse;
import cs.dto.security.DataWrapper;
import cs.dto.security.LoginSessionData;
import cs.service.AccountService;
import cs.service.AgentService;
//import cs.dto.security.LoginSessionData;
import cs.service.ContextService;
import cs.service.GuiAuthenticationService;
import cs.service.UserService;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.AgentUser;
import hxc.ecds.protocol.rest.AuthenticationRequest;


@Component
public class CustomAuthenticationProvider implements AuthenticationProvider 
{
	/*@Autowired
	private LoginSessionData sessionData;*/
	
	@Autowired
	private GuiAuthenticationService authService;
	
	@Autowired //ask @Configuration-marked class for this
	private AgentService agentService;
	
	@Autowired //ask @Configuration-marked class for this
	private UserService userService;
	
	@Autowired
	private ContextService contextService;
	
	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private ObjectMapper mapper;
	
	private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    @Override
    public Authentication authenticate(Authentication auth) throws AuthenticationException {
        final String username = auth.getName();
        final String password = auth.getCredentials().toString();
        logger.info("Request for authentication received from user {}", username);
        Authentication result = null;
        DataWrapper oauthResponse = new DataWrapper();
		try
		{
			BaseResponse resp = authService.authenticate(contextService.getCompanyID(), false, null);
			String sessionID = resp.getSessionId();
			//contextService.getSessionContextData().setSessionID(sessionID);
			sessionData.setServerSessionID(sessionID);
			sessionData.setUsername(username);
			oauthResponse.setData(mapper.writeValueAsString(resp));
			logger.info("Session received for authentication request; user {}; sessionID", username, sessionID);
			if(resp.getState().equals("REQUIRE_UTF8_USERNAME")){
				//BaseRequest request = new BaseRequest();
				AuthenticationRequest request = new AuthenticationRequest();
				request.setSessionID(sessionID);
				request.setUsername(username);
				request.setPassword(password);
				resp = authService.process(request);
			}
			if(resp.getState().equals("AUTHENTICATED"))
			{
				List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
			    grantedAuths.add(new SimpleGrantedAuthority("ROLE_USER"));
			    
				UserDetailsImpl userDetails = new UserDetailsImpl();
				userDetails.setUsername(username);
				userDetails.setPassword(password);
				userDetails.setSessionId(sessionID);
				userDetails.setAuthorities(grantedAuths);
				
				if(sessionData.getAgentId() != null)
				{
					int agentId = sessionData.getAgentId();
					userDetails.setAgentID(agentId);
					Agent agent = agentService.getAgent(agentId);
					if(agent != null)
					{
						userDetails.setAgentMsisdn(agent.getMobileNumber());
					}
				}
				if(sessionData.getAgentUserID() != null)
				{
					int agentUserId = sessionData.getAgentUserID();
					userDetails.setAgentUserID(agentUserId);
					AgentUser agentUser = userService.getAgentUser(agentUserId);
					if(agentUser != null)
					{
						userDetails.setAgentUserMsisdn(agentUser.getMobileNumber());
					}
				}
				if(sessionData.getWebUserId() != null)
					userDetails.setWebUserID(sessionData.getWebUserId());
				if(sessionData.getAgentMsisdn() != null && !sessionData.getAgentMsisdn().isEmpty())
					userDetails.setAgentMsisdn(sessionData.getAgentMsisdn());
				if(sessionData.getAgentUserMsisdn() != null && !sessionData.getAgentUserMsisdn().isEmpty())
					userDetails.setAgentUserMsisdn(sessionData.getAgentUserMsisdn());

				sessionData.setServerSessionID(sessionID);
				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, password, grantedAuths);
				token.setDetails(userDetails);
				result = token;
				
				logger.info("Authenticated user {} agentID {} agentUserID {} sessionID", username, sessionData.getAgentId(), sessionData.getAgentUserID(), sessionID);
	        } else {
	        	logger.info("Authentication failed; user {}; sessionID", username, sessionID);
	        }
		}
		catch (Exception e)
		{
			/*sessionData.setCurrentState(AuthenticationState.INVALID);
			oauthResponse.setError(e.getLocalizedMessage());
			oauthResponse.setData(Common.CONST_FATAL_ERROR);*/
			logger.error("", e);
		}
		if(result == null)
			throw new BadCredentialsException("External system authentication failed");
		
		return result;
    }
    
    @Override
    public boolean supports(Class<?> auth) {
        return auth.equals(UsernamePasswordAuthenticationToken.class);
    }
}