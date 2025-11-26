package hxc.services.ecds.rest;

import hxc.connectors.hlr.IHlrInformation;
import hxc.ecds.protocol.rest.IAuthenticatable;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.WebUsersConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.config.RestServerConfiguration;
import hxc.services.ecds.model.*;
import hxc.services.ecds.util.*;
import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static hxc.services.ecds.rest.Agents.*;

@Path("/agent_users")
public class AgentUsers
{
	final static Logger logger = LoggerFactory.getLogger(AgentUsers.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configurable
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get AgentUser
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.AgentUser getAgentUser(@PathParam("id") int agentID, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			AgentUser agent = AgentUser.findByID(em, agentID, session.getCompanyID());
			if (agent == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "AgentUser User %d not found", agentID);
			updateImsiLockoutFlag(em, session, agent);
			return agent;
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/root")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.AgentUser getRootAgentUser(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			AgentUser agentUser = AgentUser.findRoot(em, session.getCompanyID());
			if (agentUser == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Root Agent User not found");
			updateImsiLockoutFlag(em, session, agentUser);
			return agentUser;
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get AgentUsers
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.AgentUser[] getAgentUsers( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			Agent me = session.getAgent();
			List<AgentUser> agentUsers;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				agentUsers = me == null ? AgentUser.findAll(em, params, session.getCompanyID()) : AgentUser.findMine(em, params, session.getCompanyID(), me.getId());
			}
			AgentUser[] agentUserArray = agentUsers.toArray(new AgentUser[agentUsers.size()]);
			updateImsiLockoutFlag(em, session, agentUserArray);
			return agentUserArray;

		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET 
	@Path("/app_properties")
	@Produces(MediaType.APPLICATION_JSON)
	public Boolean getProperties() {
		return RestServerConfiguration.getInstance().isEnabledMobileMoney();
	}

	@GET
	@Path("/agentusers/{agentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.AgentUser[] getAgentUsersOnly( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@PathParam("agentID") int agentID,
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			List<AgentUser> agentUsers;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				agentUsers = AgentUser.findAgentUsers(em, params, session.getCompanyID(), agentID);
			}
			AgentUser[] agentUserArray = agentUsers.toArray(new AgentUser[agentUsers.size()]);
			updateImsiLockoutFlag(em, session, agentUserArray);
			return agentUserArray;
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("/apiusers/{agentID}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.AgentUser[] getApiUsersOnly( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@PathParam("agentID") int agentID,
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			List<AgentUser> agentUsers;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				agentUsers = AgentUser.findApiUsers(em, params, session.getCompanyID(), agentID);
			}
			AgentUser[] agentUserArray = agentUsers.toArray(new AgentUser[agentUsers.size()]);
			updateImsiLockoutFlag(em, session, agentUserArray);
			return agentUserArray;

		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@GET
	@Path("*")
	@Produces(MediaType.APPLICATION_JSON)
	public Long getAgentUserCount( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID, 0, -1, null, search, filter);
			Session session = context.getSession(params.getSessionID());
			Agent me = session.getAgent();
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				return me == null ? AgentUser.findCount(em, params, session.getCompanyID()) : AgentUser.findMyCount(em, params, session.getCompanyID(), me.getId());
			}
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	// /////////////////////////////////
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateAgentUser(hxc.ecds.protocol.rest.AgentUser agentUser, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Normalize MSISDN
			agentUser.setMobileNumber(context.toMSISDN(agentUser.getMobileNumber()));

			// Get AgentUsers Config
			CompanyInfo companyInfo = context.findCompanyInfoByID(session.getCompanyID());
			AgentsConfig config = companyInfo.getConfiguration(em, AgentsConfig.class);

			// Determine if this is a new Instance
			String pin = null;
			boolean isNew = agentUser.getId() <= 0;
			if (isNew)
			{
				createAgentUser(em, agentUser, session, params, config);
				return;
			}

			// Test Permission
			Agent callingAgent = session.getAgent();
			if (callingAgent != null && callingAgent.getId() != agentUser.getAgentID())
				session.check(em, AgentUser.MAY_UPDATE, "Not allowed to Update Agent User %d", agentUser.getId());

			// Get the Existing Instance
			AgentUser existing = AgentUser.findByID(em, agentUser.getId(), session.getCompanyID());
			if (existing == null || agentUser.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "AgentUser %d not found", agentUser.getId());
			AgentUser updated = existing;
			existing = new AgentUser(existing);
			updated.amend(agentUser);
			updateImsiLockoutFlag(em, session, updated);

			// Perform Common Testing
			testCommon(em, updated, session);

			//PIN means no domainAccountName or Username is set.
			if(updated.getAuthenticationMethod().equals(IAuthenticatable.AUTHENTICATE_PIN_2FACTOR))
			{
				updated.setDomainAccountName(null);
			}
			
			// Create Random/Default PIN
			if (!existing.isTemporaryPin() && updated.isTemporaryPin())
			{
				if(updated.getAuthenticationMethod().equals(IAuthenticatable.AUTHENTICATE_PASSWORD_2FACTOR))
				{
					logger.trace("Creating temporary password ...");
					session.check(em, AgentUser.MAY_RESET_PIN, "Not allowed to Reset PASSWORD for Agent User %d", agentUser.getId());
					pin = AuthenticationHelper.createRandomPassword(updated);
				} else {
					logger.trace("Creating temporary pin ...");
					session.check(em, AgentUser.MAY_RESET_PIN, "Not allowed to Reset PIN for Agent User %d", agentUser.getId());
					String defaultPin = config.getDefaultPin();
					if (defaultPin == null || defaultPin.isEmpty())
						pin = AuthenticationHelper.createRandomPin(updated);
					else
						pin = AuthenticationHelper.createDefaultPin(updated, defaultPin);
				}
				updated.setConsecutiveAuthFailures(0);
			}

			// Reset IMSI Lockout
			if (updated.isImsiLockedOut() && !agentUser.isImsiLockedOut())
			{
				session.check(em, AgentUser.MAY_RESET_IMSI_LOCK, "Not allowed to Reset IMSI Lock for Agent User %d", agentUser.getId());
				updated.setLastImsiChange(null);
			}

			// Persist to Database
			AuditEntryContext auditEntryContext = new AuditEntryContext("AGENTUSER_UPDATE", updated.getId());
			updated.persist(em, existing, session, auditEntryContext);

			// Send SMS
			if (pin != null)
			{
				if(updated.getAuthenticationMethod().equals(IAuthenticatable.AUTHENTICATE_PASSWORD_2FACTOR))
				{
					AgentsConfig agentsConfig = companyInfo.getConfiguration(em, AgentsConfig.class);
					sendEmailNotification(em, session, updated, agentsConfig.getPasswordResetEmailSubject(), agentsConfig.getPasswordResetEmailBody(), pin);
				} else {
					sendPinNotification(context, config, updated.getMobileNumber(), updated.getLanguage(), updated.isTemporaryPin(), pin);
				}
			}

			// Send State Change SMS
			if (!updated.getState().equals(existing.getState()))
			{
				sendStateChangedNotification(em, session, updated);
			}

		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (WebApplicationException ex)
		{
			//WebApplicationException thrown from createAgentUser, throw as it is (don't translate again).
			throw ex;
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

	}

	private void testCommon(EntityManagerEx em, AgentUser agentUser, Session session) throws RuleCheckException
	{
		int companyID = session.getCompanyID();

		// Load Agent
		agentUser.setAgent(Agent.findByID(em, agentUser.getAgentID(), companyID));
		RuleCheck.notNull("agentID", agentUser.getAgent());

		// Agent may not be root
		Agent root = Agent.findRoot(em, companyID);
		if (agentUser.getAgentID() == root.getId())
			throw new RuleCheckException(StatusCode.INVALID_VALUE, "agentID", "Cannot add to Root. Agent User %s", agentUser.getMobileNumber());

		// Load Role
		agentUser.setRole(Role.findByID(em, agentUser.getRoleID(), companyID));
		RuleCheck.notNull("agentID", agentUser.getRole());

		// No Access Channel Escalation
		int extraChannels = agentUser.getAllowedChannels() & ~agentUser.getAgent().getAllowedChannels();
		if (extraChannels != 0)
			throw new RuleCheckException(StatusCode.INVALID_CHANNEL, "allowedChannels", "Allowed Channel Escallation Agent User %s", agentUser.getMobileNumber());

		// TODO NO Permission Escalation

		// Test for Duplicate Mobile Number
		AgentUser duplicateUser = AgentUser.findByMSISDN(em, agentUser.getMobileNumber(), companyID);
		Agent duplicateAgent = Agent.findByMSISDN(em, agentUser.getMobileNumber(), companyID);
		if (duplicateUser != null && duplicateUser.getId() != agentUser.getId()) {
			throw new RuleCheckException(StatusCode.DUPLICATE_VALUE, "mobileNumber", "Duplicate MSISDN: Agent User %d", duplicateUser.getId());
		}
		
		if (duplicateAgent != null) {
			throw new RuleCheckException(StatusCode.DUPLICATE_VALUE, "mobileNumber", "Duplicate MSISDN: Agent User %d", duplicateAgent.getId());
		}

		// Test for Duplicate Account Number
		String accountNumber = agentUser.getAccountNumber();
		if (accountNumber != null && !accountNumber.isEmpty() && !AgentUser.AUTO_NUMBER.equals(accountNumber))
		{
			duplicateUser = AgentUser.findByAccountNumber(em, accountNumber, companyID);
			duplicateAgent = Agent.findByAccountNumber(em, accountNumber, companyID);
			if (duplicateUser != null && duplicateUser.getId() != agentUser.getId() || duplicateAgent != null)
				throw new RuleCheckException(StatusCode.DUPLICATE_VALUE, "accountNumber", "Duplicate AccountNumber: %s", accountNumber);
		}
		//Test for Duplicate Domain Account Name
		String domainAccountName = agentUser.getDomainAccountName();
		if(domainAccountName != null && !domainAccountName.isEmpty())
		{
			duplicateUser = AgentUser.findByDomainAccountName(em, companyID, domainAccountName);
			if(duplicateUser != null && duplicateUser.getId() != agentUser.getId())
				throw new RuleCheckException(StatusCode.DUPLICATE_VALUE, "domainAccountName", "Duplicate Domain Account Name: AgentUser %d", duplicateUser.getId());
			Agent agent = Agent.findByDomainAccountName(em, companyID, domainAccountName);
			if(agent != null)
				throw new RuleCheckException(StatusCode.DUPLICATE_VALUE, "domainAccountName", "Duplicate Domain Account Name / ECDS Username conflicting with Agent: Agent %d", agent.getId());
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	// /////////////////////////////////
	private void createAgentUser(EntityManagerEx em, hxc.ecds.protocol.rest.AgentUser agentUser, Session session, RestParams params, AgentsConfig config) throws RuleCheckException
	{
		try
		{
			// Test Permission
			session.check(em, AgentUser.MAY_ADD, "Not allowed to Create AgentUser");
	
			// Persist it
			AgentUser newAgentUser = new AgentUser();
			newAgentUser.amend(agentUser);
	
			// Add Default Account Number
			newAgentUser.autoNumber(em, session.getCompanyID());
	
			// Perform Common Testing
			testCommon(em, newAgentUser, session);
	
			// Get IMSI
			IHlrInformation info = context.getHlrInformation(agentUser.getMobileNumber(), false, false, true);
			if (info != null)
				newAgentUser.setImsi(info.getIMSI());
	
			// Create Random PIN or Password
			String pin;
			String defaultPin = config.getDefaultPin();
			if(newAgentUser.getAuthenticationMethod().equals(IAuthenticatable.AUTHENTICATE_PASSWORD_2FACTOR))
			{
				pin = AuthenticationHelper.createRandomPassword(newAgentUser);
			} else {
				if (defaultPin == null || defaultPin.isEmpty())
					pin = AuthenticationHelper.createRandomPin(newAgentUser);
				else
					pin = AuthenticationHelper.createDefaultPin(newAgentUser, defaultPin);
			}
			AuditEntryContext auditEntryContext = new AuditEntryContext("AGENTUSER_CREATE", newAgentUser.getMobileNumber());
			newAgentUser.persist(em, null, session, auditEntryContext);
	
			// Send Pin Notification
			if(newAgentUser.getAuthenticationMethod().equals(IAuthenticatable.AUTHENTICATE_PASSWORD_2FACTOR))
			{
				CompanyInfo companyInfo = context.findCompanyInfoByID(session.getCompanyID());
				AgentsConfig agentsConfig = companyInfo.getConfiguration(em, AgentsConfig.class);
				sendEmailNotification(em, session, newAgentUser, agentsConfig.getPasswordResetEmailSubject(), agentsConfig.getPasswordResetEmailBody(), pin);
			} else {
				sendPinNotification(context, config, newAgentUser.getMobileNumber(), newAgentUser.getLanguage(), newAgentUser.isTemporaryPin(), pin);
			}
		} catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteAgentUser(@PathParam("id") int agentUserID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Test Permission
			session.check(em, AgentUser.MAY_DELETE, "Not allowed to Delete Agent User %d", agentUserID);

			// Get the Existing Instance
			AgentUser existing = AgentUser.findByID(em, agentUserID, session.getCompanyID());
			if (existing == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent User %d not found", agentUserID);

			// Test if in use
			if (AuditEntry.referencesAgentUser(em, agentUserID) || Transaction.referencesAgentUser(em, existing.getMobileNumber()))
				throw new RuleCheckException(StatusCode.RESOURCE_IN_USE, null, "Agent User %d is in use", agentUserID);

			// Remove from Database
			AuditEntryContext auditContext = new AuditEntryContext("AGENTUSER_REMOVE", existing.getId());
			existing.remove(em, session, auditContext);
		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Passwords
	//
	// /////////////////////////////////
	@POST
	@Path("/change_password")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.ChangePasswordResponse changePassword(hxc.ecds.protocol.rest.ChangePasswordRequest request, @HeaderParam(RestParams.SID) String sessionID)
	{	
		hxc.ecds.protocol.rest.ChangePasswordResponse response = new hxc.ecds.protocol.rest.ChangePasswordResponse();
		response.setReturnCode(hxc.ecds.protocol.rest.ChangePasswordResponse.RETURN_CODE_SUCCESS);
		RestParams params = new RestParams(sessionID);
		byte[] oldPassword = null; //TODO: Old password for rollback incase email can't send or something...
		AgentUser agentUser = null;
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			Integer sessionAgentUserID = session.getAgentUserID();
			if(sessionAgentUserID == null )
				throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Cannot change password. Session does not belong to an AgentUser. Only the AgentUser can change its password.");
			if(sessionAgentUserID != null  && !sessionAgentUserID.equals(request.getEntityID()))
				throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Cannot change another AgentUser's password. userID %d changing userID %s password", sessionAgentUserID, request.getEntityID());
			
			// Get the Agent
			Integer agentUserID = request.getEntityID();
			agentUser = AgentUser.findByID(em, agentUserID, session.getCompanyID());
			if (agentUser == null || agentUser.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", agentUserID);
			if(!agentUser.testIfSamePin(request.getCurrentPassword()))
				throw new RuleCheckException(StatusCode.INVALID_PIN, null, "Autentication credentials are incorrect");
			String newPassword = request.getNewPassword();
			oldPassword = agentUser.getKey1().clone();			//Store old password for rollback.
			if (newPassword == null || newPassword.isEmpty())
				throw new RuleCheckException(StatusCode.INVALID_PIN, null, "Agent %d password is empty", agentUserID);
			AgentUsers.validateNewPassword(em, context.findCompanyInfoByID(em, session.getCompanyID()), agentUser, newPassword);
			byte[] encryptedPassword = AuthenticationHelper.encryptPin(newPassword);
			agentUser.updatePin(em, encryptedPassword, session);
	    	{//Send Email...
		    	CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
		    	WebUsersConfig webUsersConfig = companyInfo.getConfiguration(em, WebUsersConfig.class);
		    	sendEmailNotification(em, session, agentUser, webUsersConfig.getPasswordChangeEmailSubject(), webUsersConfig.getPasswordChangeEmailBody(), newPassword);
	    	}
		} //TODO: Gracefully handle password rollback.
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			response.setReturnCode(ex.getError());
			//TODO: Improve
			//revertPasswordChange(oldPassword, agentUser, params.getSessionID());
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			//TODO: Improve
			//revertPasswordChange(oldPassword, agentUser, params.getSessionID());
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);			
		}

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	
	//TODO: Improve
	public void revertPasswordChange(byte[] oldPassword, AgentUser agentUser, String sessionID)
	{
		if(agentUser != null)
		{
			try (EntityManagerEx em = context.getEntityManager())
			{
				logger.info("Reverting password change due to an exception or error which occurred after updating the password. agentUserId[{}]", agentUser.getId());
				Session session = context.getSession(sessionID);
				//Historical passwords do get lost with this approach, will end up with key1 == key3; key4 is lost
				agentUser.updatePin(em, oldPassword, session);
			} catch (Throwable ex)
			{
				logger.error("revertPasswordChange: An exception occurred inside a function intended to handle an exception. Exception: [{}]", ex);
				throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
			}
		} else {
			logger.error("revertPasswordChange: the supplied agentUser is null");
			throw new WebApplicationException("revertPasswordChange: the supplied agentUser is null", Status.INTERNAL_SERVER_ERROR);
		}
	}

	public static byte[] validateNewPin(EntityManager em, CompanyInfo company, AgentUser agentUser, String newPin) throws RuleCheckException
	{
		// Test if Valid
		if (newPin == null || newPin.isEmpty() || agentUser == null)
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PIN, "pin", "Empty PIN");

		// Test Min Max Length
		AgentsConfig config = company.getConfiguration(em, AgentsConfig.class);
		if (newPin.length() < config.getMinPinLength())
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PIN, "pin", "PIN too short");
		else if (newPin.length() > config.getMaxPinLength())
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PIN, "pin", "PIN too long");

		// Test if numeric
		for (char digit : newPin.toCharArray())
		{
			if (!Character.isDigit(digit))
				throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PIN, "pin", "PIN not numeric");
		}

		// Encrypt
		byte[] key = AuthenticationHelper.encryptPin(newPin);

		// Test History
		if (AuthenticationHelper.testIfSameKey(key, agentUser.getKey1()) //
				|| AuthenticationHelper.testIfSameKey(key, agentUser.getKey2()) //
				|| AuthenticationHelper.testIfSameKey(key, agentUser.getKey3()) //
				|| AuthenticationHelper.testIfSameKey(key, agentUser.getKey4()))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PIN, "pin", "Repeated PIN");

		return key;

	}
	
	public static byte[] validateNewPassword(EntityManager em, CompanyInfo company, AgentUser agentUser, String newPassword) throws RuleCheckException
	{
		// Test if Valid
		if (newPassword == null || newPassword.isEmpty() || agentUser == null)
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Empty Password");
		
		// Test Min Max Length
		AgentsConfig config = company.getConfiguration(em, AgentsConfig.class);
		if (newPassword.length() < config.getMinPasswordLength())
			throw new RuleCheckException(TransactionsConfig.ERR_TOO_SHORT, "password", "Password too short");
		else if (newPassword.length() > config.getMaxPasswordLength())
			throw new RuleCheckException(TransactionsConfig.ERR_TOO_LONG, "password", "Password too long");
		
		if (!newPassword.matches(".*[a-zA-Z]+.*"))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Password must contain atleast one alphabetic character");
		
		if (!newPassword.matches(".*[0-9]+.*"))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Password must contain atleast one numeric character");
		
		if (!newPassword.matches(".*[ !\"#$%&'()*+,-./:;<=>?@\\[\\\\\\]^_`{|}~]+.*"))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Password must contain atleast one special character");
		
		// Encrypt
		byte[] key = AuthenticationHelper.encryptPin(newPassword);

		// Test History
		if (AuthenticationHelper.testIfSameKey(key, agentUser.getKey1()) //
				|| AuthenticationHelper.testIfSameKey(key, agentUser.getKey2()) //
				|| AuthenticationHelper.testIfSameKey(key, agentUser.getKey3()) //
				|| AuthenticationHelper.testIfSameKey(key, agentUser.getKey4()))
			throw new RuleCheckException(TransactionsConfig.ERR_HISTORIC_PASSWORD, "pin", "Repeated PIN");

		return key;
	}

	public static void validateAgentUserImsi(ICreditDistribution context, EntityManager em, TransactionsConfig transactionsConfig, Session session, AgentUser agentUser) throws RuleCheckException
	{
		// Test for Imsi lockout
		Date lastImsiChange = agentUser.getLastImsiChange();
		boolean updateAgentUser = false;
		boolean imsiChanged = false;
		if (lastImsiChange != null)
		{
			Date now = new Date();
			long hours = (now.getTime() - lastImsiChange.getTime()) / MILLISECONDS_IN_ONE_HOUR;
			if (hours < transactionsConfig.getImsiChangeLockoutHours()) {
				throwImsiLockoutException(lastImsiChange, transactionsConfig, IMSI_BLOCKED_MESSAGE_TEMPLATE);
			}
		}

		// Get the AgentUser's old and new IMSI
		String oldImsi = agentUser.getImsi();
		String imsi = session.get("IMSI");
		if (imsi == null || imsi.isEmpty())
		{
			IHlrInformation info = context.getHlrInformation(agentUser.getMobileNumber(), false, false, true);
			if (info != null)
				imsi = info.getIMSI();
			if (imsi == null || imsi.isEmpty())
				imsi = oldImsi;
		}

		// Test for Imsi change
		if (imsi != null)
		{
			if (oldImsi == null || oldImsi.isEmpty())
			{
				updateAgentUser = true;
			}
			else if (!imsi.equals(oldImsi))
			{
				updateAgentUser = true;
				if (transactionsConfig.getImsiChangeLockoutHours() > 0)
				{
					lastImsiChange = new Date();
					imsiChanged = true;
				}
			}
		}

		// Update AgentUser
		if (updateAgentUser)
		{
			AgentUser updated = agentUser;
			agentUser = new AgentUser(agentUser);
			updated.setLastImsiChange(lastImsiChange);
			updated.setImsi(imsi);
			AuditEntryContext auditEntryContext = new AuditEntryContext("AGENTUSER_UPDATED_IMSI", updated.getId());
			updated.persist(em, agentUser, session, auditEntryContext);
			agentUser = updated;
		}

		if (imsiChanged) {
			throwImsiLockoutException(lastImsiChange, transactionsConfig, IMSI_CHANGED_MESSAGE_TEMPLATE);
		}
	}

	public static void sendPinNotification(ICreditDistribution context, AgentsConfig config, String mobileNumber, String language, boolean temporary, String pin)
	{
		logger.trace("Sending pin notification to {}", mobileNumber);
		String text = temporary ? //
				config.getNewPinNotification().safe(language, "") : config.getDefaultPinNotification().safe(language, "");
		if (text != null)
			text = text.replace(AgentsConfig.TEMPORARY_PIN, pin).replace(AgentsConfig.DEFAULT_PIN, pin);
		else
			logger.error("pin notification for temporary = {} is empty ... define it in the UI", temporary);
		context.sendSMS(mobileNumber, language, text);
	}
	
	private void sendEmailNotification(EntityManagerEx em, Session session, AgentUser agent, Phrase emailSubject, Phrase emailBody, String password) throws Exception
	{
		EmailUtils emailer = new EmailUtils(context);
		CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
		AgentsConfig agentsConfig = companyInfo.getConfiguration(em, AgentsConfig.class);
		InternetAddress fromEmailAddress = new InternetAddress(agentsConfig.getFromEmailAddress());
		StringExpander<AgentUser> expander = new StringExpander<AgentUser>()
		{
			@Override
			protected String expandField(String englishName, Locale locale, AgentUser agent)
			{					
				switch (englishName)
				{
					case AgentsConfig.DATE:
						return FormatHelper.formatDate(context, this, locale, new Date());
					case AgentsConfig.TIME:
						return FormatHelper.formatTime(context, this, locale, new Date());
					case AgentsConfig.RECEPIENT_TITLE:
						return agent.getTitle();
					case AgentsConfig.RECEPIENT_FIRST_NAME:
						return agent.getFirstName();
					case AgentsConfig.RECEPIENT_SURNAME:
						return agent.getSurname();
					case AgentsConfig.RECEPIENT_INITIALS:
						return agent.getInitials();
					case AgentsConfig.PASSWORD:
						return password;
					default:
						return "";
				}
			}

		};
		Locale locale = new Locale(agent.getLanguage(), companyInfo.getCompany().getCountry());
		String subject = expander.expandNotification(emailSubject, locale, agentsConfig.listPasswordEmailFields(), agent);
		String body = expander.expandNotification(emailBody, locale, agentsConfig.listPasswordEmailFields(), agent);    		
		hxc.ecds.protocol.rest.config.GeneralConfig generalConfig =
				context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.GeneralConfig.class);
		emailer.sendEmail(fromEmailAddress, agent.getEmail(), subject, body, null, generalConfig.getSmtpRetries(), true);
	}

	private void sendStateChangedNotification(EntityManager em, Session session, AgentUser agentUser)
	{
		CompanyInfo company = context.findCompanyInfoByID(session.getCompanyID());
		AgentsConfig config = company.getConfiguration(em, AgentsConfig.class);

		Phrase notification = null;
		switch (agentUser.getState())
		{
			case AgentUser.STATE_ACTIVE:
				notification = config.getReActivatedNotification();
				break;

			case AgentUser.STATE_DEACTIVATED:
				notification = config.getDeActivatedNotification();
				break;

			default:
				return;
		}

		String text = notification.safe(agentUser.getLanguage(), "");
		context.sendSMS(agentUser.getMobileNumber(), agentUser.getLanguage(), text);

	}

	private void updateImsiLockoutFlag(EntityManager em, Session session, AgentUser... agentUsers)
	{
		if (agentUsers == null)
			return;
		TransactionsConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, TransactionsConfig.class);
		Date cutOff = new Date();
		cutOff = new Date(cutOff.getTime() - config.getImsiChangeLockoutHours() * MILLISECONDS_IN_ONE_HOUR);
		for (AgentUser agentUser : agentUsers)
		{
			Date lastImsiChange = agentUser.getLastImsiChange();
			agentUser.setImsiLockedOut(lastImsiChange != null && lastImsiChange.after(cutOff));
		}

	}

	public static void updateAgentUserImei(ICreditDistribution context, EntityManager em, TransactionsConfig transactionsConfig, Session session, AgentUser agentUser) throws RuleCheckException
	{
		Date now = new Date();
		Date lastImeiUpdate = agentUser.getLastImeiUpdate();
		long refreshIntervalMinutes = transactionsConfig.getImeiRefreshInterval() * 60000L;
		if (lastImeiUpdate == null || (now.getTime() - lastImeiUpdate.getTime()) > refreshIntervalMinutes)
		{
			String currentImei = agentUser.getImei();
			String imei = context.getImei(agentUser.getMobileNumber());
			if (imei != null && !imei.isEmpty())
			{
				AgentUser updated = agentUser;
				agentUser = new AgentUser(agentUser);
				updated.setLastImeiUpdate(now);
				updated.setImei(imei);
				AuditEntryContext auditEntryContext = new AuditEntryContext("AGENTUSER_UPDATED_IMEI", updated.getId());
				if (imei.equals(currentImei)) {
					auditEntryContext.setSkipAuditLog(true);
				}
				updated.persist(em, agentUser, session, auditEntryContext);
				agentUser = updated;
			}
		}
	}
}
