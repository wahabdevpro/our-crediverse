package cs.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.apache.http.conn.HttpHostConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.databind.ObjectMapper;

import cs.config.CsServletConfiguration;
import cs.config.RestServerConfiguration;
import cs.constants.I18NMessages;
import cs.dto.User;
import cs.dto.data.BaseResponse;
import cs.dto.error.GuiGeneralException;
import cs.dto.error.GuiValidationException;
import cs.dto.error.GuiViolation.ViolationType;
import cs.dto.security.AuthenticationData;
import cs.dto.security.AuthenticationData.CaptureField;
import cs.dto.security.LoginSessionData;
import cs.template.CsRestTemplate;
import cs.utility.Common;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.AgentUser;
import hxc.ecds.protocol.rest.AuthenticationRequest;
import hxc.ecds.protocol.rest.AuthenticationRequest.UserType;
import hxc.ecds.protocol.rest.AuthenticationResponse;
import hxc.ecds.protocol.rest.Permission;
import hxc.ecds.protocol.rest.Role;
import hxc.ecds.protocol.rest.Session;
import hxc.ecds.protocol.rest.Violation;

@Service
public class GuiAuthenticationService
{
	//public static final String CONST_DEFAULT_AUTHENTICATION = "defaultAuthentication";
	
	private static Logger logger = LoggerFactory.getLogger(GuiAuthenticationService.class);
	
	private Map<String, AuthenticationData>authenticationRequests = new ConcurrentHashMap<String, AuthenticationData>();
	
	@Autowired
	private MessageSource messageSource;

	@Autowired
	private Environment environment;

	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private CsServletConfiguration servletConfiguration;

	@Autowired
	private ObjectMapper mapper;
	
	/*@Autowired
	private TransactionService transactionService;*/
	
	/*@Autowired
	private NetworkIdentifierService networkIdentifierService;*/
	
	@Autowired
	private ContextService contextService; 

	@Autowired
	private AgentService agentService;
	
	/*@Autowired
	private AgentUserService agentUserService;*/
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private Environment enviroment;
	
	private String restServerAuth;
	private boolean configured = false;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerAuth = restServerConfig.getRestServer() + restServerConfig.getAuth2url();
			this.authenticationRequests = new ConcurrentHashMap<String, AuthenticationData>();
			configured = true;
		}
	}



	private boolean isExtApiProfile()
	{
		return environment.acceptsProfiles(Common.CONST_EXTAPI_PROFILE);
	}

	private AuthenticationRequest prePopulateAuthenticationRequest()
	{
		AuthenticationRequest auth = new AuthenticationRequest();
		if (isExtApiProfile())
		{
			auth.setUserType(UserType.AGENT);
			auth.setChannel(AuthenticationRequest.CHANNEL_3PP);
			logger.info("██████ EXTAPI. profiles: " + String.join(", ", environment.getActiveProfiles()));
		}
		return auth;
	}
	
	private void prePopulateAuthenticationRequest(AuthenticationRequest auth)
	{
		if (isExtApiProfile())
		{
			auth.setUserType(UserType.AGENT);
			auth.setChannel(AuthenticationRequest.CHANNEL_3PP);
			logger.info("██████ EXTAPI. profiles: " + String.join(", ", environment.getActiveProfiles()));
		} 		
	}
	
	public BaseResponse authenticate(int companyID, boolean coauth, String forTransactionId) throws Exception
	{
		AuthenticationData authData = null;
		BaseResponse resp = new BaseResponse();
		resp.setCid(String.valueOf(companyID));
		try
		{
			authData = new AuthenticationData();
			if (coauth)
			{
				UUID uuid = UUID.randomUUID();
				resp.setUuid(uuid.toString());
				//authData = new AuthenticationData();
				authData.setUuid(uuid.toString());
				authData.setCoauth(coauth);
				authData.setForTransactionId(forTransactionId);
				authenticationRequests.put(uuid.toString(), authData);
			}
			else
			{
				//authData = authenticationRequests.get(sessionData.getMySessionId());
				//if (authData == null)
				//{
				//	authData = new AuthenticationData();
				//	this.authenticationRequests.put(sessionData.getMySessionId(), authData);
				//}
				sessionData.setSessionAuthentication(authData);
			}
			
			AuthenticationRequest auth = prePopulateAuthenticationRequest();
			sessionData.setCompanyID(companyID);
			if (coauth)
			{
				auth.setCoSignForSessionID(sessionData.getServerSessionID());
				
				if (authData != null)
				{
					String serverSessionId = authData.getServerSessionID();
					if (serverSessionId != null && serverSessionId.length() > 0) auth.setCoSignForSessionID(serverSessionId);
				}
				if (forTransactionId != null && forTransactionId.length() > 0)
				{
					auth.setCoSignatoryTransactionID(forTransactionId);
					if (authData != null)
					{
						auth.setCoSignatoryTransactionID(authData.getForTransactionId());
					}
				}
			}
			
			AuthenticationResponse response = null;
			response = restTemplate.postForObject(restServerAuth, auth, AuthenticationResponse.class, authData);
			//if (isDevProfile())
			logger.info("request::"+mapper.writeValueAsString(auth));
			logger.info("requestAuth::"+mapper.writeValueAsString(authData));
			logger.info("Response::"+mapper.writeValueAsString(response));
			
			String responseCode = null;
			if (response != null)
			{
				responseCode = response.getReturnCode();
			}
			if (responseCode != null && responseCode.length() > 0)
			{
				switch (responseCode)
				{
					case AuthenticationResponse.CODE_FAIL_SESSION_INVALID:
						authData.setServerSessionID(null);
						return authenticate(companyID, false, null);
					case AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID:
					case AuthenticationResponse.CODE_FAIL_INVALID_COMPANY:
						resp.setState(AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID);
						authData.setServerSessionID(null);
						break;
					default:
						resp.setState(responseCode);
						resp.setSessionId(response.getSessionID());
						authData.setServerSessionID(response.getSessionID());
						break;
				}
			}
			else
			{
				resp.setState(AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID);				
				authData.setServerSessionID(null);
				resp.setError(messageSource.getMessage(I18NMessages.CONST_AUTH_INVALID_USER, null, sessionData.getLocale()));
			}
		}
		catch(HttpHostConnectException | ResourceAccessException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			resp.setState(AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID);
			authData.setServerSessionID(null);
			resp.setError(messageSource.getMessage(I18NMessages.CONST_TECHNICAL_ERROR, null, sessionData.getLocale()));
			logger.error(I18NMessages.CONST_TECHNICAL_ERROR, ex);
		}
		sessionData.setCompanyID(companyID);
		authData.setCurrentState(resp.getState());
		return resp;
	}
	
	public BaseResponse process(AuthenticationRequest auth) throws Exception
	{
		prePopulateAuthenticationRequest(auth);
		BaseResponse resp = new BaseResponse();
		//AuthenticationData authData = null;
		
		//authData = authenticationRequests.get(sessionData.getMySessionId());
		AuthenticationData authData = sessionData.getSessionAuthentication();
		if (authData.isCoauth() && (authData.getCaptureField() == CaptureField.USERNAME))
		{
			String userName = new String(auth.getUsername());
			if (sessionData.getUsername().equals(userName)) 
			{
				throw new GuiValidationException(Arrays.asList(new Violation[] {new Violation(ViolationType.CO_AUTHORIZE.toString(), null, null, "CO_AUTHORIZE")}));
			}
		}
		
		try
		{
			AuthenticationResponse response = restTemplate.postForObject(restServerAuth, auth, AuthenticationResponse.class, authData);
			//if (isDevProfile())
				logger.info("request::"+mapper.writeValueAsString(auth));
				logger.info("requestAuth::"+mapper.writeValueAsString(authData));
				logger.info("Response::"+mapper.writeValueAsString(response));
			String responseCode = response.getReturnCode();
			if (responseCode != null && responseCode.length() > 0)
			{
				switch (responseCode)
				{
					case AuthenticationResponse.CODE_FAIL_SESSION_INVALID:
//					case AuthenticationResponse.CODE_FAIL_INVALID:
					case AuthenticationResponse.CODE_FAIL_INVALID_COMPANY:
					case AuthenticationResponse.CODE_FAIL_OTHER_ERROR:
//						resp.setState(AuthenticationResponse.CODE_FAIL_INVALID);
						resp.setState(AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID);
						break;
					
					default:
						resp.setState(responseCode);
						break;
				}
			}
			else
			{
				resp.setState(AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID);
				resp.setError(messageSource.getMessage(I18NMessages.CONST_AUTH_INVALID_USER, null, sessionData.getLocale()));
				logger.info("Server response was ||"+mapper.writeValueAsString(response)+"||");
			}
			resp.setState(response.getReturnCode());
			resp.setData1(response.getKey1());
			resp.setData2(response.getKey2());
		}
		catch(GuiGeneralException ex)
		{
//			resp.setState(AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID);
			resp.setState(ex.getServerCode());
			resp.setError(ex.getAdditional());
			logger.error(I18NMessages.CONST_TECHNICAL_ERROR, ex);
		}
		catch (Throwable ex)
		{
			resp.setState(AuthenticationResponse.CODE_FAIL_CREDENTIALS_INVALID);
			resp.setError(messageSource.getMessage(I18NMessages.CONST_TECHNICAL_ERROR, null, sessionData.getLocale()));
			logger.error(I18NMessages.CONST_TECHNICAL_ERROR, ex);
		}
		authData.setCurrentState(resp.getState(), resp.getData1(), resp.getData2());
		if (AuthenticationData.AuthenticationState.AUTHENTICATED == authData.getCurrentState())
		{
			/* Commented for now as there is no co-auth use case in the API
			if (authData.isCoauth())
			{
				try
				{
					BaseResponse result = null;
					String parentUuid = request.getParentUuid();
					result = transactionService.processTransaction(parentUuid, authData.getServerSessionID());
					
					if (result != null)
					{
						BeanUtils.copyProperties(resp, result);
						return result;
					}
				}
				catch(Exception ex)
				{
					killSession(authData.getServerSessionID(), false);
					authData.invalidate();
					throw ex;
				}
				killSession(authData.getServerSessionID(), false);
				authData.invalidate();
			}
			else*/
			{
				User user = new User();
				user.setUsername(authData.getUsername());
				
				if (enviroment.getActiveProfiles() != null)
				{
					Authentication authentication = null;

					if (environment.getActiveProfiles().length > 0) {
						authentication = new UsernamePasswordAuthenticationToken(user, authData.getServerSessionID(), user.getAuthorities());
					}

					SecurityContextHolder.getContext().setAuthentication(authentication);
				}				
				//Authentication authentication = new EcdsAuthenticationToken(user, authData.getServerSessionID(), user.getAuthorities());
				
				// Set session language for User
				Session session = contextService.getSessionContextData();
				sessionData.setLocale(new Locale(session.getLanguageID(), session.getSessionID()));
				sessionData.updateSession(session);
				
				// Find User Details
				if (sessionData.getAgentUserID() != null)
				{
					//AgentUser agentUser = agentUserService.getAgentUser(sessionData.getAgentUserID());
					AgentUser agentUser = userService.getAgentUser(sessionData.getAgentUserID());
					
					user.setFullName( this.formulateFullName(agentUser.getTitle(), agentUser.getFirstName(), agentUser.getSurname(), agentUser.getMobileNumber(), user.getUsername()) );
					Agent agent = agentService.getAgent(sessionData.getAgentId());
					if (agent != null)
					{
						user.setUserMsisdn(agent.getMobileNumber());
						updateSessionPermissions(user, agentUser.getRoleID(), agent.getRoleID());
					}
					else
					{
						killSession(authData.getServerSessionID());
						authData.invalidate();
						throw new GuiValidationException(Arrays.asList(new Violation[] {new Violation(ViolationType.forbidden.toString(), null, null, "FORBIDDEN")}));
					}
						
				}
				else if (sessionData.getAgentId() != null) 
				{
					// User is Portal Agent
					Agent agent = agentService.getAgent(sessionData.getAgentId());
					user.setFullName( this.formulateFullName(agent.getTitle(), agent.getFirstName(), agent.getSurname(), agent.getMobileNumber(), user.getUsername()) );
					user.setUserMsisdn(agent.getMobileNumber());
					updateSessionPermissions(user, agent.getRoleID());
					
					// CanSearch Permission
					if ((agent.getOwnerAgentID() != null) && (agent.getId() == agent.getOwnerAgentID())) 
					{
						user.addCustomPermission("Agent", "Search");
					}
				}
				
				sessionData.setCurrentUser(user);
				
				//resp.setRedirectUrl(servletConfiguration.getServletContextPath() + "/home/" + sessionData.getUsername());
				/*
				 * Removed username from URL to keep URL names consistent between users.
				 */
				resp.setRedirectUrl(servletConfiguration.getServletContextPath() + "/");
			}
		}
		return resp;
	} 
	
	/*
	 * For agent Users, filter permissions based on main agent permissions.
	 * Note these are enforced by the TS, this is mearly adjusting users permissions for displaying / hiding buttons etc.
	 */
	private void updateSessionPermissions(User user, int roleId, int agentRoleId) throws Exception 
	{
		Map<String, Permission> agentPermissions= new HashMap<String, Permission>();
		if (agentRoleId > 0 && roleId > 0)
		{
			Role role = roleService.getRole( String.valueOf(roleId) );
			for (Permission perm : role.getPermissions())
			{
				if (perm.isAgentAllowed())
				{
					agentPermissions.put(perm.getGroup()+"_"+perm.getName(), perm);
				}
			}
			Role agentUserRole = roleService.getRole( String.valueOf(agentRoleId) );
			for (Permission perm : agentUserRole.getPermissions())
			{
				if (perm.isAgentAllowed())
				{
					if (agentPermissions.containsKey(perm.getGroup()+"_"+perm.getName()))
					{
						user.addPermission(perm);
					}
				}
			}
			List<Role>userRoles = new ArrayList<Role>();
			userRoles.add(role);
			userRoles.add(agentUserRole);
			user.setRoles(userRoles);
		}
	}
	
	private void updateSessionPermissions(User user, int roleId) throws Exception 
	{
		if (roleId > 0)
		{
			Role role = roleService.getRole( String.valueOf(roleId) );
			for (Permission perm : role.getPermissions())
			{
				if (perm.isAgentAllowed())
				{
					user.addPermission(perm);
				}
			}
		}
	}
	
	private String formulateFullName(String title, String firstName, String surname, String mobileNumber, String logInName)
	{
		if (firstName != null && surname != null)
			return String.format("%s %s", firstName, surname);
		else if (title != null && surname != null)
			return String.format("%s %s", title, surname);
		else if (mobileNumber != null)
			return mobileNumber;
		else if (logInName != null)
			return logInName;

		return null;
	}
	
	private void killSession(String sessionId)
	{
		StringBuilder sessionUrl = new StringBuilder(restServerConfig.getRestServer());
		sessionUrl.append(restServerConfig.getSessionurl());
		sessionUrl.append("/");
		sessionUrl.append(sessionId);
		try
		{
			restTemplate.delete(sessionUrl.toString());
		}
		catch (Throwable th)
		{
			// Ignore failure to delete session
		}
	}

	public void logout()
	{
		// http://localhost:14400/ecds/sessions/b4fcd8cea46c434e95077f70a3971d99

		/*
		 * Generate session URL to delete server side session.
		 */

		killSession(sessionData.getServerSessionID());
		sessionData.invalidate();
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	public void setLocale(Locale locale)
	{
		sessionData.setLocale(locale);
	}

	public void logout(String sessionId)
	{
		killSession(sessionId);
	}
}
