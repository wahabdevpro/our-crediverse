package hxc.services.ecds.rest;

import hxc.connectors.hlr.IHlrInformation;
import hxc.ecds.protocol.rest.IAuthenticatable;
import hxc.ecds.protocol.rest.Permission;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.WebUsersConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.*;
import hxc.services.ecds.olapmodel.OlapAgentAccount;
import hxc.services.ecds.rest.batch.AgentProcessor;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.util.*;
import jakarta.mail.internet.InternetAddress;
import hxc.ecds.protocol.rest.ExistingAgentInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static hxc.ecds.protocol.rest.Agent.STATE_ACTIVE;
import static hxc.ecds.protocol.rest.Agent.STATE_DEACTIVATED;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.ERR_IMSI_LOCKOUT;
import static hxc.services.ecds.util.StatusCode.ALREADY_DEACTIVATED;
import static hxc.services.ecds.util.StatusCode.NOT_FOUND;
import static hxc.services.ecds.util.StringUtil.isNullOrBlank;

@Path("/agents")
public class Agents
{
	final static Logger logger = LoggerFactory.getLogger(Agents.class);
	static final long MILLISECONDS_IN_ONE_HOUR = 3600000;
	public static final SimpleDateFormat IMSI_BLOCKED_UNTIL_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final String IMSI_CHANGED_MESSAGE_TEMPLATE = "IMSI change detected. Your account is blocked from transacting until %s.";
	public static final String IMSI_BLOCKED_MESSAGE_TEMPLATE = "IMSI lock-out in effect. Your account is blocked from transacting until %s.";
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
	// Get Agent
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Agent getAgent(@PathParam("id") int agentID, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			Agent agent = Agent.findByID(em, agentID, session.getCompanyID());
			if (agent == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", agentID);
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
	public hxc.ecds.protocol.rest.Agent getRootAgent(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			Agent agent = Agent.findRoot(em, session.getCompanyID());
			if (agent == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Root Agent not found");
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Agents
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Agent[] getAgents( //
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
			List<Agent> agents;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				agents = me == null ? Agent.findAll(em, params, session.getCompanyID()) : Agent.findMine(em, params, session.getCompanyID(), me.getId());
			}
			Agent[] agentArray = agents.toArray(new Agent[agents.size()]);
			updateImsiLockoutFlag(em, session, agentArray);
			return agentArray;

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
	public Long getAgentCount( //
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
				return me == null ? Agent.findCount(em, params, session.getCompanyID()) : Agent.findMyCount(em, params, session.getCompanyID(), me.getId());
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

	@GET
	@Path("/csv")
	@Produces("text/csv")
	public String getAgentCsv( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
			)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
			Session session = context.getSession(params.getSessionID());

			Agent me = session.getAgent();
			List<Agent> agents;
			// TODO Remove when queries are fixed
			try (QueryToken token = context.getQueryToken())
			{
				agents = me == null ? Agent.findAll(em, params, session.getCompanyID()) : Agent.findMine(em, params, session.getCompanyID(), me.getId());
			}
			CsvExportProcessor<Agent> processor = new CsvExportProcessor<Agent>(AgentProcessor.HEADINGS, first)
			{
				@Override
				protected void write(Agent record)
				{
					put("id", record.getId());
					put("msisdn", record.getMobileNumber());
					put("first_name", record.getFirstName());
					put("surname", record.getSurname());
					put("status", record.getState());
					put("gender", record.getGender());
					put("initials", record.getInitials());
					put("title", record.getTitle());
					put("account_number", record.getAccountNumber());
					put("language", record.getLanguage());
					put("temp_pin", record.isTemporaryPin());
					put("dob", record.getDateOfBirth());
					put("channels", record.getAllowedChannels());
					put("alt_phone", record.getAltPhoneNumber());
					put("email", record.getEmail());
					put("domain_account", record.getDomainAccountName());
					put("imei", record.getImei());
					put("imsi", record.getImsi());
					put("activation_date", record.getActivationDate());
					put("deactivation_date", record.getDeactivationDate());
					put("expiration_date", record.getExpirationDate());
					put("max_amount", record.getMaxTransactionAmount());
					put("max_daily_count", record.getMaxDailyCount());
					put("max_daily_amount", record.getMaxDailyAmount());
					put("max_monthly_count", record.getMaxMonthlyCount());
					put("max_monthly_amount", record.getMaxMonthlyAmount());
					put("max_report_count", record.getReportCountLimit());
					put("max_report_daily_schedule_count", record.getReportDailyScheduleLimit());
					put("warning_threshold", record.getWarningThreshold());
					if (record.getTier() != null)
						put("tier", record.getTier().getName());
					if (record.getArea() != null) {
						put("area_name", record.getArea().getName());
						put("area_type", record.getArea().getType());
					}	
					if (record.getGroup() != null)
						put("group", record.getGroup().getName());
					if (record.getServiceClass() != null)
						put("service_class", record.getServiceClass().getName());
					if (record.getRole() != null)
						put("role", record.getRole().getName());
					if (record.getSupplier() != null)
						put("supplier", record.getSupplier().getMobileNumber());
					if (record.getOwner() != null)
						put("owner", record.getOwner().getMobileNumber());
					put("postal_city", record.getPostalAddressCity());
					put("postal_line1", record.getPostalAddressLine1());
					put("postal_line2", record.getPostalAddressLine2());
					put("postal_suburb", record.getPostalAddressSuburb());
					put("postal_zip", record.getPostalAddressZip());
					put("street_city", record.getStreetAddressCity());
					put("street_line1", record.getStreetAddressLine1());
					put("street_line2", record.getStreetAddressLine2());
					put("street_suburb", record.getStreetAddressSuburb());
					put("street_zip", record.getStreetAddressZip());
					put("ussd_confirmation", record.isConfirmUssd());
					put("send_daily_bundle_commission_report", record.isSendBundleCommissionReport());
					/*
					 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
					 */
					//put("msisdn_recycled", record.getMsisdnRecycled());
				}
			};
			return processor.add(agents);

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

	private void updateAgentProfilePartial(EntityManagerEx em, Session session, hxc.ecds.protocol.rest.Agent updatedAgent) throws RuleCheckException
	{
		// Get the Existing Instance
		Agent existing = Agent.findByID(em, updatedAgent.getId(), session.getCompanyID());
		if (existing == null || updatedAgent.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", updatedAgent.getId());

		Agent updated = existing;
		existing = new Agent(existing, false);

		/* F0393 Only the following fields are to be updated */
		updated.setTitle(updatedAgent.getTitle());
		updated.setFirstName(updatedAgent.getFirstName());
		updated.setSurname(updatedAgent.getSurname());
		updated.setLanguage(updatedAgent.getLanguage());
		updated.setEmail(updatedAgent.getEmail());

		// Persist to Database
		AuditEntryContext auditContext = new AuditEntryContext("AGENT_PROFILE_UPDATE", updated.getId());
		updated.persist(em, existing, session, auditContext);
	}

	private void updateAgentProfile(EntityManagerEx em, Session session, hxc.ecds.protocol.rest.Agent updatedAgent) throws RuleCheckException
	{
		// Get the Existing Instance
		Agent existing = Agent.findByID(em, updatedAgent.getId(), session.getCompanyID());
		if (existing == null || updatedAgent.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", updatedAgent.getId());

		Agent updated = existing;
		existing = new Agent(existing, false);

		/* F0393 Only the following fields are to be updated */
		updated.setTitle(updatedAgent.getTitle());
		updated.setFirstName(updatedAgent.getFirstName());
		updated.setSurname(updatedAgent.getSurname());
		updated.setLanguage(updatedAgent.getLanguage());
		updated.setEmail(updatedAgent.getEmail());
		updated.setAltPhoneNumber(updatedAgent.getAltPhoneNumber());
		updated.setInitials(updatedAgent.getInitials());
		updated.setWarningThreshold(updatedAgent.getWarningThreshold());
		updated.setSignature(updated.calcSecuritySignature());

		// Perform Common Testing
		testCommon(em, updated, session);

		// Persist to Database
		AuditEntryContext auditContext = new AuditEntryContext("AGENT_PROFILE_UPDATE", updated.getId());
		updated.persist(em, existing, session, auditContext);
	}

	private ExistingAgentInfo extractInfoAboutAgent(EntityManager em, String msisdn, int companyId) {
		Agent agent = Agent.findByMSISDN(em, msisdn, companyId);
		Account account = Account.findByAgentID(em, agent.getId(), false);
		List<Transaction> lastTransactions = Transaction.findLastForAgent(em, agent.getId(), 1, agent.getCompanyID());
		Date lastTransactionDate = lastTransactions.isEmpty() ? null : lastTransactions.get(0).getEndTime();

		ExistingAgentInfo agentInfo = new ExistingAgentInfo();
		agentInfo.setAgentId(agent.getId());
		agentInfo.setAgentState(agent.getState());
		agentInfo.setBalance(account.getBalance());
		agentInfo.setBonusBalance(account.getBonusBalance());
		agentInfo.setOnHoldBalance(account.getOnHoldBalance());
		agentInfo.setActivationDate(agent.getActivationDate());
		agentInfo.setLastTransactionDate(lastTransactionDate);
		return agentInfo;
	}
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update Own Profile
	//
	// /////////////////////////////////
	@PUT
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateAgentProfile(hxc.ecds.protocol.rest.Agent updatedAgent, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			Agent callingAgent = session.getAgent();
			if (callingAgent == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", updatedAgent.getId());

			// Test Permission
			session.check(em, Agent.MAY_UPDATE_OWN, "Not allowed to Update own profile %d", callingAgent.getId());

			// Only allow my own details to be updated here
			Integer agentId = Integer.valueOf(updatedAgent.getId());
			Integer sessionUserId = session.getAgentID();
			RuleCheck.equals("Id", agentId, sessionUserId);

			updateAgentProfile(em, session, updatedAgent);
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
	// Update Agent Profile Partially- not all the required fields on the GUI
	//
	// /////////////////////////////////
	@PUT
	@Path("/profile/partial")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateAgentProfilePartial(hxc.ecds.protocol.rest.Agent updatedAgent, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			Agent callingAgent = session.getAgent();
			if (callingAgent == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", updatedAgent.getId());

			// Test Permission
			session.check(em, Agent.MAY_UPDATE_OWN, "Not allowed to Update own profile %d", callingAgent.getId());

			// Only allow my own details to be updated here
			Integer agentId = Integer.valueOf(updatedAgent.getId());
			Integer sessionUserId = session.getAgentID();
			RuleCheck.equals("Id", agentId, sessionUserId);

			updateAgentProfilePartial(em, session, updatedAgent);
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
	// Update Sub Agent Profile, allowed by owner agent and owner agent users, but only if the
	// owner agent has the update permission
	//
	// /////////////////////////////////
	@PUT
	@Path("/subprofile")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateSubAgentProfile(hxc.ecds.protocol.rest.Agent updatedAgent, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			Agent callingAgent = null;
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			Integer agentUserId = session.getAgentUserID();
			if (agentUserId != null)
			{
				AgentUser agentUser = AgentUser.findByID(em, agentUserId, session.getCompanyID());
				if (agentUser == null)
					throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent User %d not found", agentUserId);

				callingAgent = Agent.findByID(em, agentUser.getAgentID(), session.getCompanyID());
				if (callingAgent == null)
					throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", agentUser.getAgentID());
			}
			else
			{
				callingAgent = session.getAgent();
				if (callingAgent == null)
					throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", session.getAgentID());
			}

			// Test Agent Permission
			Role agentRole = Role.findByID(em, callingAgent.getRoleID(), session.getCompanyID());
			if (agentRole != null)
			{
				boolean hasPerm = false;
				for (Permission agentPerm : agentRole.getPermissions())
				{
					if (agentPerm.getName().equals(Agent.MAY_UPDATE.getName()) && agentPerm.getGroup().equals(Agent.MAY_UPDATE.getGroup()))
					{
						hasPerm = true;
						break;
					}
				}
				if (!hasPerm)
				{
					throw new RuleCheckException(StatusCode.FORBIDDEN, null, "WebUser/Agent doesn't have the '%s' permission", Agent.MAY_UPDATE);
				}
			}
			else
			{
				throw new RuleCheckException(StatusCode.FORBIDDEN, null, "WebUser/Agent must have at least one role");
			}

			// Only allow owned agents to be updated here
			Integer ownerAgentId = Integer.valueOf(updatedAgent.getOwnerAgentID());
			Integer sessionUserId = session.getAgentID();
			RuleCheck.equals("Id", ownerAgentId, sessionUserId);

			updateAgentProfile(em, session, updatedAgent);
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

	@PUT
	@Path("/deactivate/by-msisdn/{msisdn}")
	public void deactivateAgent(@PathParam("msisdn") String msisdn, @HeaderParam(RestParams.SID) String sessionID) {
		try (EntityManagerEx em = this.context.getEntityManager()) {
			Session session = context.getSession(sessionID);
			Agent agent = Agent.findByMSISDN(em, msisdn, session.getCompanyID());
			if (agent == null) {
				throw new RuleCheckException(NOT_FOUND, null, "Agent with MSISDN " + msisdn + " not found");
			}

			if (agent.getState().equals(STATE_DEACTIVATED)) {
				throw new RuleCheckException(ALREADY_DEACTIVATED, null, "Agent with MSISDN " + msisdn + " is already deactivated");
			}

			agent.setState(STATE_DEACTIVATED);
			updateAgent(agent, sessionID); // TODO check whether permanent account are handled properly
		} catch (RuleCheckException ex) {
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/by-msisdn/{msisdn}")
	@Produces(MediaType.APPLICATION_JSON)
	public ExistingAgentInfo getRecyclingAgentInfo(@PathParam("msisdn") String msisdn, @HeaderParam(RestParams.SID) String sessionID) {
		try (EntityManagerEx em = context.getEntityManager()) {
			Session session = context.getSession(sessionID);
			return extractInfoAboutAgent(em, msisdn, session.getCompanyID());
		} catch (RuleCheckException ex) {
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/tdr-agent/agents-by-msisdn/{msisdn}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Agent> getAllAgentsByMsisdn(@PathParam("msisdn") String msisdn, @HeaderParam(RestParams.SID) String sessionID) {
		try (EntityManagerEx em = context.getEntityManager()) {
			Session session = context.getSession(sessionID);
			return Agent.findAllAgentsByMSISDN(em, msisdn, session.getCompanyID());
		} catch (RuleCheckException ex) {
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		} catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateAgent(hxc.ecds.protocol.rest.Agent agent, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = this.context.getEntityManager(); EntityManagerEx apEm = this.context.getApEntityManager();)
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Normalize MSISDN
			agent.setMobileNumber(context.toMSISDN(agent.getMobileNumber()));

			// Get Agents Config
			CompanyInfo companyInfo = context.findCompanyInfoByID(session.getCompanyID());
			AgentsConfig config = companyInfo.getConfiguration(em, AgentsConfig.class);

			// Determine if this is a new Instance
			String pin = null;
			boolean isNew = agent.getId() <= 0;
			if (isNew)
			{
				createAgent(em, agent, session, params, config);
				return;
			}

			// Test Permission
			Agent callingAgent = session.getAgent();
			if (callingAgent == null || agent.getOwnerAgentID() == null || callingAgent.getId() != agent.getOwnerAgentID())
				session.check(em, Agent.MAY_UPDATE, "Not allowed to Update Agent %d", agent.getId());

			// Get the Existing Instance
			Agent existing = Agent.findByID(em, agent.getId(), session.getCompanyID());
			if (existing == null || agent.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", agent.getId());
			Agent updated = existing;
			existing = new Agent(existing, false);

			updated.amend(agent, true);
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
					session.check(em, AgentUser.MAY_RESET_PIN, "Not allowed to Reset PASSWORD for Agent User %d", agent.getId());
					pin = AuthenticationHelper.createRandomPassword(updated);
				} else {
					logger.trace("Creating temporary pin ...");
					session.check(em, Agent.MAY_RESET_PIN, "Not allowed to Reset PIN for Agent %d", agent.getId());
					String defaultPin = config.getDefaultPin();
					if (defaultPin == null || defaultPin.isEmpty())
						pin = AuthenticationHelper.createRandomPin(updated);
					else
						pin = AuthenticationHelper.createDefaultPin(updated, defaultPin);
					updated.setConsecutiveAuthFailures(0);
				}
			}

			// Reset IMSI Lockout
			if (updated.isImsiLockedOut() && !agent.isImsiLockedOut())
			{
				session.check(em, Agent.MAY_RESET_IMSI_LOCK, "Not allowed to Reset IMSI Lock for Agent %d", agent.getId());
				updated.setLastImsiChange(null);
			}

			// Get IMEI
			TransactionsConfig transactionsConfig = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, TransactionsConfig.class);
			Date lastImeiUpdate = updated.getLastImeiUpdate();
			long refreshIntervalMinutes = transactionsConfig.getImeiRefreshInterval();
			boolean isRecent = updated.isImeiRecent(refreshIntervalMinutes);
			if (!isRecent)
			{
				String updatedImei = context.getImei(updated.getMobileNumber());
				if (updatedImei != null && !updatedImei.isEmpty())
				{
					logger.trace("Agent [{}] : IMEI = [{}]", updated.getMobileNumber(), updatedImei);
					updated.setImei(updatedImei);
					updated.setLastImeiUpdate(new Date());
				}
			}
			else
			{
				String msg = String.format("IMEI for [%s] is still recent [%2$tFT%2$tT] - not re-updating. ", updated.getMobileNumber(), updated.getLastImeiUpdate());
				logger.trace(msg);
			}

			// Persist to Database
			AuditEntryContext auditContext = new AuditEntryContext("AGENT_UPDATE", updated.getId());
			updated.persist(em, existing, session, auditContext);

			if ( ! updated.getState().equals( existing.getState() ) )
			{
				OlapAgentAccount.synchronizeState(em, apEm, existing.getId());
			}

			// Send SMS or Email
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
			//WebApplicationException thrown from createAgent, throw as it is (don't translate again).
			throw ex;
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

	}

	private void testCommon(EntityManagerEx em, Agent agent, Session session) throws RuleCheckException
	{
		int companyID = session.getCompanyID();

		// Load Tier
		agent.setTier(Tier.findByID(em, agent.getTierID(), companyID));
		RuleCheck.notNull("tierID", agent.getTier());

		// Load Role
		agent.setRole(Role.findByID(em, agent.getRoleID(), companyID));
		RuleCheck.notNull("roleID", agent.getRole());
		if (!Role.TYPE_AGENT.equals(agent.getRole().getType()))
			throw new RuleCheckException(StatusCode.INVALID_VALUE, "roleID", "Invalid Role Type %s", agent.getRole().getType());

		// Load Supplier
		if (agent.getSupplierAgentID() != null)
		{
			agent.setSupplier(Agent.findByID(em, agent.getSupplierAgentID(), companyID));
			RuleCheck.notNull("supplierAgentID", agent.getSupplier());
		}
		else
		{
			agent.setSupplier(null);
		}

		// Load Owner
		if (agent.getOwnerAgentID() != null)
		{
			agent.setOwner(Agent.findByID(em, agent.getOwnerAgentID(), companyID));
			RuleCheck.notNull("ownerAgentID", agent.getOwner());
		}
		else
		{
			agent.setOwner(null);
		}

		// Load Group
		if (agent.getGroupID() != null)
		{
			agent.setGroup(Group.findByID(em, agent.getGroupID(), companyID));
			RuleCheck.notNull("groupID", agent.getGroup());
		}
		else
		{
			agent.setGroup(null);
		}

		// Load Area
		if (agent.getAreaID() != null)
		{
			agent.setArea(Area.findByID(em, agent.getAreaID(), companyID));
			RuleCheck.notNull("areaID", agent.getArea());
		}
		else
		{
			agent.setArea(null);
		}

		// Load Service Class
		if (agent.getServiceClassID() != null)
		{
			agent.setServiceClass(ServiceClass.findByID(em, agent.getServiceClassID(), companyID));
			RuleCheck.notNull("serviceClassID", agent.getServiceClass());
		}
		else
		{
			agent.setServiceClass(null);
		}

		// Test for Duplicate Agents by various criteria
		Agent duplicate = null;

		// Test for Duplicate Account Number
		String accountNumber = agent.getAccountNumber();
		if (accountNumber != null && !accountNumber.isEmpty() && !Agent.AUTO_NUMBER.equals(accountNumber))
		{
			duplicate = Agent.findByAccountNumber(em, accountNumber, companyID);
			if (duplicate != null && duplicate.getId() != agent.getId())
				throw new RuleCheckException(StatusCode.DUPLICATE_VALUE, "accountNumber", "Duplicate AccountNumber: Agent %d", duplicate.getId());
		}
		//Test for Duplicate Domain Account Name
		String domainAccountName = agent.getDomainAccountName();
		if(domainAccountName != null && !domainAccountName.isEmpty())
		{
			duplicate = Agent.findByDomainAccountName(em, companyID, domainAccountName);
			if(duplicate != null && duplicate.getId() != agent.getId())
				throw new RuleCheckException(StatusCode.DUPLICATE_VALUE, "domainAccountName", "Duplicate Domain Account Name: Agent %d", duplicate.getId());
			AgentUser agentUser = AgentUser.findByDomainAccountName(em, companyID, domainAccountName);
			if(agentUser != null)
				throw new RuleCheckException(StatusCode.DUPLICATE_VALUE, "domainAccountName", "Duplicate Domain Account Name / ECDS Username conflicting with AgentUser: AgentUser %d", agentUser.getId());
		}

		// Test for (and allow) Duplicate MSISDN
		//  Duplicates only allowed when all duplicates are Deactivated
		//  Only 1 active OR suspended allowed with this msisdn
		duplicate = Agent.findByMSISDN(em, agent.getMobileNumber(), companyID);
		boolean duplicateExists = duplicate != null && duplicate.getId() != agent.getId();
		if (duplicateExists && !duplicate.getState().equals(STATE_DEACTIVATED) && !agent.getState().equals(STATE_DEACTIVATED)) {
			// duplicates not allowed if both will be active -- allowed if either is not active
			throw new RuleCheckException(StatusCode.DUPLICATE_VALUE, "mobileNumber", "Duplicate MSISDN (Only 1 can be Active): Agent %d", duplicate.getId());
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	// /////////////////////////////////
	private void createAgent(EntityManagerEx em, hxc.ecds.protocol.rest.Agent agent, Session session, RestParams params, AgentsConfig config) throws RuleCheckException
	{
		try
		{
			// Test Permission
			session.check(em, Agent.MAY_ADD, "Not allowed to Create Agent");

			// Persist it
			Agent newAgent = new Agent();
			newAgent.amend(agent, true);

			// Add Default Account Number
			newAgent.autoNumber(em, session.getCompanyID());

			// Perform Common Testing
			testCommon(em, newAgent, session);

			// Get IMSI
			IHlrInformation info = context.getHlrInformation(agent.getMobileNumber(), false, false, true);
			if (info != null)
				newAgent.setImsi(info.getIMSI());

			// Get IMEI
			String imei = context.getImei(agent.getMobileNumber());
			if (imei != null && !imei.isEmpty())
			{
				newAgent.setImei(imei);
				newAgent.setLastImeiUpdate(new Date());
			}

			// Create Random PIN or Password
			String pin;
			String defaultPin = config.getDefaultPin();
			if(newAgent.getAuthenticationMethod().equals(IAuthenticatable.AUTHENTICATE_PASSWORD_2FACTOR))
			{
				pin = AuthenticationHelper.createRandomPassword(newAgent);
			} else {
				if (defaultPin == null || defaultPin.isEmpty())
					pin = AuthenticationHelper.createRandomPin(newAgent);
				else
					pin = AuthenticationHelper.createDefaultPin(newAgent, defaultPin);
			}
			/*AuditContext auditContext = new AuditContext();
			  auditContext.setHeadline("Create Agent");
			  session.
			  auditContext.setReason(session,);*/
			AuditEntryContext auditEntryContext = new AuditEntryContext("AGENT_CREATED", agent.getMobileNumber());
			newAgent.persist(em, null, session, auditEntryContext);
			if(newAgent.getAuthenticationMethod().equals(IAuthenticatable.AUTHENTICATE_PASSWORD_2FACTOR))
			{
				// Send Pin Notification				
				CompanyInfo companyInfo = context.findCompanyInfoByID(session.getCompanyID());
				AgentsConfig agentsConfig = companyInfo.getConfiguration(em, AgentsConfig.class);
				sendEmailNotification(em, session, newAgent, agentsConfig.getPasswordResetEmailSubject(), agentsConfig.getPasswordResetEmailBody(), pin);
			} else {
				sendPinNotification(context, config, newAgent.getMobileNumber(), newAgent.getLanguage(), newAgent.isTemporaryPin(), pin);
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
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteAgent(@PathParam("id") int agentID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Test Permission
			session.check(em, Agent.MAY_DELETE, "Not allowed to Delete Agent %d", agentID);

			// Get the Existing Instance
			Agent existing = Agent.findByID(em, agentID, session.getCompanyID());
			if (existing == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", agentID);

			// Test if in use
			String agentInUseMessage = null;
			if (Agent.referencesAgent(em, agentID)) {
				agentInUseMessage = "Another agent references agent";
			} else if (Transaction.referencesAgent(em, agentID)) {
				agentInUseMessage = "A transaction references the agent";
			} else if (AuditEntry.referencesAgent(em, agentID)) {
				agentInUseMessage = "An audit entry references the agent";
			} else if (WorkItem.referencesAgent(em, agentID)) {
				agentInUseMessage = "A work item references the agent";
			}

			if (agentInUseMessage != null) {
				throw new RuleCheckException(StatusCode.RESOURCE_IN_USE, null, agentInUseMessage + ": %d", agentID);
			}	

			// Remove from Database
			AuditEntryContext auditContext = new AuditEntryContext("AGENT_REMOVE", existing.getId());
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
	// Configuration
	//
	// /////////////////////////////////
	@GET
	@Path("/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.AgentsConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.AgentsConfig.class);
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

	@PUT
	@Path("/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.AgentsConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, Agent.MAY_CONFIGURE);
			context.findCompanyInfoByID(session.getCompanyID()).setConfiguration(em, configuration, session);
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
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());		
			Integer sessionAgentID = session.getAgentID();
			if(sessionAgentID == null )
				throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Cannot change password. Session does not belong to an Agent. Only the agent can change the its password.");
			if(sessionAgentID != null  && !sessionAgentID.equals(request.getEntityID()))
				throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Cannot change another Agent's password. agentID %d changing agentID %s password", sessionAgentID, request.getEntityID());
			// Get the Agent
			Integer agentID = request.getEntityID();
			Agent agent = Agent.findByID(em, agentID, session.getCompanyID());
			if (agent == null || agent.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", agentID);
			if(!agent.testIfSamePin(request.getCurrentPassword()))
			{
				throw new RuleCheckException(StatusCode.INVALID_PIN, null, "Autentication credentials are incorrect");
			}
			String newPassword = request.getNewPassword();
			if (newPassword == null || newPassword.isEmpty())
				throw new RuleCheckException(StatusCode.INVALID_PIN, null, "Agent %d password is empty", agentID);
			Agents.validateNewPassword(em, context.findCompanyInfoByID(em, session.getCompanyID()), agent, newPassword);
			byte[] encryptedPassword = AuthenticationHelper.encryptPin(newPassword);
			agent.updatePin(em, encryptedPassword, session);
			{//Send Email...
				CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
				WebUsersConfig webUsersConfig = companyInfo.getConfiguration(em, WebUsersConfig.class);
				sendEmailNotification(em, session, agent, webUsersConfig.getPasswordChangeEmailSubject(), webUsersConfig.getPasswordChangeEmailBody(), newPassword);
			}

		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			response.setReturnCode(ex.getError());
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////

	public static byte[] validateNewPin(EntityManager em, CompanyInfo company, Agent agent, String newPin) throws RuleCheckException
	{
		// Test if Valid
		if (newPin == null || newPin.isEmpty() || agent == null)
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
		if (AuthenticationHelper.testIfSameKey(key, agent.getKey1()) //
				|| AuthenticationHelper.testIfSameKey(key, agent.getKey2()) //
				|| AuthenticationHelper.testIfSameKey(key, agent.getKey3()) //
				|| AuthenticationHelper.testIfSameKey(key, agent.getKey4()))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PIN, "pin", "Repeated PIN");

		return key;
	}

	public static byte[] validateNewPassword(EntityManager em, CompanyInfo company, Agent agent, String newPassword) throws RuleCheckException
	{
		// Test if Valid
		if (newPassword == null || newPassword.isEmpty() || agent == null)
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Empty Password");

		if (!newPassword.matches(".*[a-zA-Z]+.*"))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Password must contain atleast one alphabetic character");

		if (!newPassword.matches(".*[0-9]+.*"))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Password must contain atleast one numeric character");

		if (!newPassword.matches(".*[ !\"#$%&'()*+,-./:;<=>?@\\[\\\\\\]^_`{|}~]+.*"))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Password must contain atleast one special character");

		// Encrypt
		byte[] key = AuthenticationHelper.encryptPin(newPassword);

		// Test History
		if (AuthenticationHelper.testIfSameKey(key, agent.getKey1()) //
				|| AuthenticationHelper.testIfSameKey(key, agent.getKey2()) //
				|| AuthenticationHelper.testIfSameKey(key, agent.getKey3()) //
				|| AuthenticationHelper.testIfSameKey(key, agent.getKey4()))
			throw new RuleCheckException(TransactionsConfig.ERR_HISTORIC_PASSWORD, "pin", "Repeated PIN");

		return key;

	}

	public static void checkImsiLockout(TransactionsConfig transactionsConfig, Agent agent) throws RuleCheckException {
		Date lastImsiChange = agent.getLastImsiChange();
		if (lastImsiChange != null) {
			Date now = new Date();
			long hours = (now.getTime() - lastImsiChange.getTime()) / MILLISECONDS_IN_ONE_HOUR;
			if (hours < transactionsConfig.getImsiChangeLockoutHours()) {
				throwImsiLockoutException(lastImsiChange, transactionsConfig, IMSI_BLOCKED_MESSAGE_TEMPLATE);
			}
		}
	}

	public static void throwImsiLockoutException(Date lastImsiChange, TransactionsConfig transactionsConfig, String message) throws RuleCheckException {
		Date blockedUntilDate = new Date(lastImsiChange.getTime() + transactionsConfig.getImsiChangeLockoutHours() * MILLISECONDS_IN_ONE_HOUR);
		throw new RuleCheckException(ERR_IMSI_LOCKOUT, "lastImsiChange", message, IMSI_BLOCKED_UNTIL_FORMAT.format(blockedUntilDate));
	}

	private static String fetchImsi(ICreditDistribution context, Session session, Agent agent, String oldImsi) {
		String imsi = session.get("IMSI");
		if (isNullOrBlank(imsi)) {
			IHlrInformation info = context.getHlrInformation(agent.getMobileNumber(), false, false, true);
			if (info != null) {
				imsi = info.getIMSI();
				session.set("IMSI", imsi);
			}
			if (isNullOrBlank(imsi)) {
				imsi = oldImsi;
			}
		}
		return imsi;
	}

	public static void validateAgentImsi(ICreditDistribution context, EntityManager em, TransactionsConfig transactionsConfig, Session session,
			Agent agent) throws RuleCheckException {
		checkImsiLockout(transactionsConfig, agent);
		String oldImsi = agent.getImsi();
		String imsi = fetchImsi(context, session, agent, oldImsi);

		checkForImsiChangeAndUpdateAgent(em, transactionsConfig, session, agent, imsi);
	}

	public static Agent checkForImsiChangeAndUpdateAgent(EntityManager em, TransactionsConfig transactionsConfig, Session session,
			Agent agent, String imsi) throws RuleCheckException {
		String oldImsi = agent.getImsi();

		// Test for Imsi change
		Date lastImsiChange = agent.getLastImsiChange();
		boolean updateAgent = false;
		boolean imsiChanged = false;
		if (imsi != null) {
			if (isNullOrBlank(oldImsi)) {
				updateAgent = true;
			} else if (!imsi.equals(oldImsi)) {
				updateAgent = true;
				if (transactionsConfig.getImsiChangeLockoutHours() > 0) {
					lastImsiChange = new Date();
					imsiChanged = true;
				}
			}
		}

		Agent managedEntity = agent;
		if (updateAgent) {
			managedEntity = updateAgentImsi(em, session, agent, imsi, lastImsiChange);
		}

		if (imsiChanged) {
			throwImsiLockoutException(lastImsiChange, transactionsConfig, IMSI_CHANGED_MESSAGE_TEMPLATE);
		}
		return managedEntity;
	}

	private static Agent updateAgentImsi(EntityManager em, Session session, Agent agent, String imsi, Date lastImsiChange) throws RuleCheckException {
		Agent oldAgent = new Agent(agent);
		agent.setLastImsiChange(lastImsiChange);
		agent.setImsi(imsi);
		AuditEntryContext auditEntryContext = new AuditEntryContext("AGENT_UPDATED_IMSI", agent.getId());
		return agent.persistOrMergeAndReturnManagedEntity(em, oldAgent, session, auditEntryContext);
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

	private void sendEmailNotification(EntityManagerEx em, Session session, Agent agent, Phrase emailSubject, Phrase emailBody, String password) throws Exception
	{
		EmailUtils emailer = new EmailUtils(context);
		CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
		AgentsConfig agentsConfig = companyInfo.getConfiguration(em, AgentsConfig.class);
		InternetAddress fromEmailAddress = new InternetAddress(agentsConfig.getFromEmailAddress());
		StringExpander<Agent> expander = new StringExpander<Agent>()
		{
			@Override
			protected String expandField(String englishName, Locale locale, Agent agent)
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

	private void sendStateChangedNotification(EntityManager em, Session session, Agent agent)
	{
		CompanyInfo company = context.findCompanyInfoByID(session.getCompanyID());
		AgentsConfig config = company.getConfiguration(em, AgentsConfig.class);

		Phrase notification = null;
		switch (agent.getState())
		{
			case Agent.STATE_ACTIVE:
				notification = config.getReActivatedNotification();
				break;

				//case Agent.STATE_DEACTIVATED: // FIXME Agent Deactivation removed the Agent. prefix. Seems an odd choice here, check if it works with the old version.
			case STATE_DEACTIVATED:
				notification = config.getDeActivatedNotification();
				break;

			case Agent.STATE_SUSPENDED:
				notification = config.getSuspendedNotification();
				break;

			default:
				return;
		}

		String text = notification.safe(agent.getLanguage(), "");
		context.sendSMS(agent.getMobileNumber(), agent.getLanguage(), text);

	}

	private void updateImsiLockoutFlag(EntityManager em, Session session, Agent... agents)
	{
		if (agents == null)
			return;
		TransactionsConfig config = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, TransactionsConfig.class);
		Date cutOff = new Date();
		cutOff = new Date(cutOff.getTime() - config.getImsiChangeLockoutHours() * MILLISECONDS_IN_ONE_HOUR);
		for (Agent agent : agents)
		{
			Date lastImsiChange = agent.getLastImsiChange();
			agent.setImsiLockedOut(lastImsiChange != null && lastImsiChange.after(cutOff));
		}

	}

	public static void updateAgentImei(ICreditDistribution context, EntityManager em, TransactionsConfig transactionsConfig, Session session, Agent agent) throws RuleCheckException
	{
		Date now = new Date();
		boolean isImeiRecent = agent.isImeiRecent(transactionsConfig.getImeiRefreshInterval());
		if (!isImeiRecent)
		{
			String currentImei = agent.getImei();
			String updatedImei = context.getImei(agent.getMobileNumber());
			if (updatedImei != null && !updatedImei.isEmpty())
			{
				Agent updated = agent; // Agent.findByID(em, agent.getId(), agent.getCompanyID());
				agent = new Agent(agent, false);
				updated.setLastImeiUpdate(now);
				updated.setImei(updatedImei);
				// updated.setSignature(updated.calcSecuritySignature()); // how to get the signature in the audit log to match the final ea_agents data?
				AuditEntryContext auditEntryContext = new AuditEntryContext("AGENT_UPDATED_IMEI", updated.getId());
				if (updatedImei.equals(currentImei)) {
					auditEntryContext.setSkipAuditLog(true);
				}
				updated.persist(em, agent, session, auditEntryContext);
				agent = updated;
			}
		}
	}

}
