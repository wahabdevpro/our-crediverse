package cs.service;

// import java.math.BigDecimal;
// import java.text.DecimalFormat;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;

import com.fasterxml.jackson.databind.ObjectMapper;

import cs.config.CsServletConfiguration;
import cs.config.RestServerConfiguration;
// import cs.constants.ApplicationConstants;
import cs.constants.I18NMessages;
import cs.dto.User;
import cs.dto.data.BaseRequest;
import cs.dto.data.BaseResponse;
import cs.dto.error.GuiGeneralException;
import cs.dto.error.GuiValidationException;
import cs.dto.error.GuiViolation.ViolationType;
import cs.dto.security.AuthenticationData;
import cs.dto.security.AuthenticationData.AuthenticationState;
import cs.dto.security.AuthenticationData.CaptureField;
import cs.dto.security.LoginSessionData;
import cs.security.EcdsAuthenticationToken;
import cs.template.CsRestTemplate;
import cs.utility.Common;
// import hxc.ecds.protocol.rest.AdjustmentRequest;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.AgentUser;
import hxc.ecds.protocol.rest.AuthenticationRequest;
import hxc.ecds.protocol.rest.AuthenticationRequest.UserType;
import hxc.ecds.protocol.rest.AuthenticationResponse;
import hxc.ecds.protocol.rest.Permission;
// import hxc.ecds.protocol.rest.ReplenishRequest;
import hxc.ecds.protocol.rest.Role;
import hxc.ecds.protocol.rest.Session;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.WebUser;

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
	private RSAEncryptionService rsaService;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private NetworkIdentifierService networkIdentifierService;

	@Autowired
	private WebUserService webUserService;

	@Autowired
	private ContextService contextService;

	@Autowired
	private AgentService agentService;

	@Autowired
	private AgentUserService agentUserService;

	@Autowired
	private RoleService roleService;


	private String restServerAuth;
	private boolean configured = false;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerAuth = restServerConfig.getRestServer() + restServerConfig.getAuthurl();
			this.authenticationRequests = new ConcurrentHashMap<String, AuthenticationData>();
			configured = true;
		}
	}

	// private boolean isDevProfile()
	// {
	// 	return environment.acceptsProfiles(Common.CONST_DEVELOPMENT_PROFILE);
	// }

	private boolean isPortalProfile()
	{
		return environment.acceptsProfiles(Common.CONST_PORTAL_PROFILE);
	}

	private boolean isMobileProfile()
	{
		return environment.acceptsProfiles(Common.CONST_MOBILE_PROFILE);
	}

	private AuthenticationRequest prePopulateAuthenticationRequest()
	{
		AuthenticationRequest auth = new AuthenticationRequest();
		if (isPortalProfile())
		{
			auth.setUserType(UserType.AGENT);
			auth.setChannel(AuthenticationRequest.CHANNEL_WUI);
		}
		else if (isMobileProfile())
		{
			auth.setUserType(UserType.AGENT);
			auth.setChannel(AuthenticationRequest.CHANNEL_SMART_DEVICE);
		}
		else
		{
			auth.setUserType(UserType.WEBUSER);
			auth.setChannel(AuthenticationRequest.CHANNEL_WUI);
		}
		return auth;
	}

	public BaseResponse authenticate(int companyID, boolean coauth, String forTransactionId) throws Exception
	{
		AuthenticationData authData = null;
		BaseResponse resp = new BaseResponse();
		resp.setCid(String.valueOf(companyID));
		try
		{
			if (coauth)
			{
				UUID uuid = UUID.randomUUID();
				resp.setUuid(uuid.toString());
				authData = new AuthenticationData();
				authData.setUuid(uuid.toString());
				authData.setCoauth(coauth);
				authData.setForTransactionId(forTransactionId);
				authenticationRequests.put(uuid.toString(), authData);
			}
			else
			{
				String mySessionId = sessionData.getMySessionId();
				authData = authenticationRequests.get(mySessionId);
				if (authData == null)
				{
					authData = new AuthenticationData();
					this.authenticationRequests.put(mySessionId, authData);
				}
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
			networkIdentifierService.lookupAddress(sessionData);
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
	// FIXME --- These functions are commented out as they are called from commented code in `process` function
	// private String formatCurrency(BigDecimal amount, String format)
	// {
	// 	DecimalFormat df = new DecimalFormat(format);
	// 	return df.format(amount);
	// }

	// private void setPinNotificationMessage(Object transaction, AuthenticationRequest auth)
	// {
	// 	try
	// 	{
	// 		StringBuilder otpMessage = new StringBuilder();
	// 		String userLanguage = contextService.getContextData().getLanguageID();
	// 		if (userLanguage.equals(ApplicationConstants.CONST_LANGUAGE_ENGLISH))
	// 		{
	// 			if (transaction instanceof AdjustmentRequest)
	// 			{
	// 				AdjustmentRequest adj = (AdjustmentRequest) transaction;
	// 				otpMessage.append("Adjustment for amount ");
	// 				otpMessage.append(adj.getAmount().toString());
	// 				otpMessage.append(" Fcfa");
	// 			}
	// 			else if (transaction instanceof ReplenishRequest)
	// 			{
	// 				ReplenishRequest repl = (ReplenishRequest) transaction;
	// 				otpMessage.append("Replenish for amount ");
	// 				otpMessage.append(formatCurrency(repl.getAmount(), "#,###.00"));
	// 				otpMessage.append(" Fcfa");
	// 			}
	// 			otpMessage.append(", Confirmation Pin {OTP}");
	// 		}
	// 		else
	// 		{
	// 			if (transaction instanceof AdjustmentRequest)
	// 			{
	// 				AdjustmentRequest adj = (AdjustmentRequest) transaction;
	// 				otpMessage.append("Ajustement pour montant ");
	// 				otpMessage.append(adj.getAmount().toString());
	// 				otpMessage.append(" Fcfa");
	// 			}
	// 			else if (transaction instanceof ReplenishRequest)
	// 			{
	// 				ReplenishRequest repl = (ReplenishRequest) transaction;
	// 				otpMessage.append("Réapprovisionner pour montant ");
	// 				otpMessage.append(formatCurrency(repl.getAmount(), "#,###.00"));
	// 				otpMessage.append(" Fcfa");
	// 			}
	// 		}
	// 		auth.setCustomPinChangeMessage(otpMessage.toString());
	// 	}
	// 	catch(Exception ex)
	// 	{
	// 		logger.error("", ex);
	// 	}
	// }

	// http://localhost:14400/ecds/authentication/authenticate
	public BaseResponse process(BaseRequest request) throws Exception
	{
		AuthenticationRequest auth = prePopulateAuthenticationRequest();

		BaseResponse resp = new BaseResponse();
		AuthenticationData authData = null;

		resp.setCid(request.getCid());
		String uuid = request.getUuid();
		resp.setUuid(uuid);
		authData = authenticationRequests.get((uuid != null)?uuid:sessionData.getMySessionId());

		if (authData.isCoauth() && (authData.getCaptureField() == CaptureField.USERNAME))
		{
			String userName = new String(request.getDataBytes());
			if (sessionData.getUsername().equals(userName))
			{
				throw new GuiValidationException(Arrays.asList(new Violation[] {new Violation(ViolationType.CO_AUTHORIZE.toString(), null, null, "CO_AUTHORIZE")}));
			}
		}


		if (authData.getCurrentState() == AuthenticationState.REQUIRES_RSA)
		{
			request.setDataBytes(rsaService.encrypt(authData.getData1(), request.getDataBytes()));
			if (authData.getForTransactionId() != null)
			{
				/* Object transaction = transactionService.getTransactionById(authData.getForTransactionId());
				if (transaction instanceof AdjustmentRequest || transaction instanceof ReplenishRequest)
					setPinNotificationMessage(transaction, auth); */
			}
		}

		auth.setCompanyID(Integer.parseInt(request.getCid()));
		auth.setSessionID(authData.getServerSessionID());
		auth.setData(request.getDataBytes());
		try
		{
			networkIdentifierService.lookupAddress(sessionData);
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
			else
			{
				User user = new User();
				user.setUsername(authData.getUsername());
				Authentication authentication = new EcdsAuthenticationToken(user, authData.getServerSessionID(), user.getAuthorities());
				SecurityContextHolder.getContext().setAuthentication(authentication);


				// Set session language for User
				Session session = contextService.getSessionContextData();
				sessionData.setLocale(new Locale(session.getLanguageID(), session.getSessionID()));
				sessionData.updateSession(session);

				// Find User Details
				if (sessionData.getAgentUserID() != null)
				{
					AgentUser agentUser = agentUserService.getAgentUser(sessionData.getAgentUserID());
					user.setFullName( this.formulateFullName(agentUser.getTitle(), agentUser.getFirstName(), agentUser.getSurname(), agentUser.getMobileNumber(), user.getUsername()) );
					Agent agent = agentService.getAgent(sessionData.getAgentId());
					if (agent != null)
					{
						user.setUserMsisdn(agent.getMobileNumber());
						updateSessionPermissions(user, agentUser.getRoleID(), agent.getRoleID());
					}
					else
					{
						killSession(authData.getServerSessionID(), false);
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
				else if (sessionData.getWebUserId() != null)
				{
					// User is Web User
					WebUser webUser = webUserService.getWebUser(session.getWebUserID());
					if (webUser != null)
					{
						//user.s
						for (Role role : webUser.getRoles())
						{
							for (Permission perm : role.getPermissions())
							{
								user.addPermission(perm);
							}
						}
						user.setRoles(webUser.getRoles());
						user.setId(webUser.getId());
					}
					user.setUserMsisdn(webUser.getMobileNumber());
					user.setFullName( this.formulateFullName(webUser.getTitle(), webUser.getFirstName(), webUser.getSurname(), webUser.getMobileNumber(), user.getUsername()) );
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

	private void killSession(String sessionId, boolean server)
	{
		if (!server)
		{
			String serverSessionId = null;
			try {
				serverSessionId = sessionData.getServerSessionID();
			}
			catch (Exception ex) {
				// Ignore, this just means no session existed
			}
			if (serverSessionId != null && sessionId != null && serverSessionId.equals(sessionId))
			{

				IllegalStateException ex = new IllegalStateException("If you really want to kill the server session, pass in the correct flag.");
				throw ex;
			}
		}
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

		killSession(sessionData.getServerSessionID(), true);
		sessionData.invalidate();
		SecurityContextHolder.getContext().setAuthentication(null);
	}

	public void setLocale(Locale locale)
	{
		sessionData.setLocale(locale);
	}

	public void logout(String sessionId)
	{
		killSession(sessionId, false);
	}
}
