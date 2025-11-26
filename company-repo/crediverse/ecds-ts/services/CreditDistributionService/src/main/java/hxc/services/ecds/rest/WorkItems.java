package hxc.services.ecds.rest;

import java.security.SecureRandom;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.GenerateWorkItemCoSignOTPRequest;
import hxc.ecds.protocol.rest.GenerateWorkItemCoSignOTPResponse;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.WorkflowConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Permission;
import hxc.services.ecds.model.WebUser;
import hxc.services.ecds.model.WorkItem;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import hxc.services.ecds.util.StringExpander;

@Path("/work_items")
public class WorkItems
{
	final static Logger logger = LoggerFactory.getLogger(WorkItems.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final SecureRandom RANDOM = new SecureRandom();
	private static final String OTP_SALT = "u=CQ,4^ke`omYq,y";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	// private static final Phrase[] notificationFields = { Phrase.en(WorkflowConfig.ACTOR), Phrase.en(WorkflowConfig.OWNER), Phrase.en(WorkflowConfig.RECIPIENT), //
	// Phrase.en(WorkflowConfig.ACTION), Phrase.en(WorkflowConfig.TYPE), Phrase.en(WorkflowConfig.DESCRIPTION) };

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public WorkItems()
	{
	}

	public WorkItems(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get WorkItem
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.WorkItem getWorkItem(@PathParam("id") int workItemID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			WorkItem workItem = WorkItem.findByID(em, workItemID, session.getCompanyID());
			if (workItem == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WorkItem %d not found", workItemID);
			return workItem;
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/{id}", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/uuid/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.WorkItem getWorkItem(@PathParam("uuid") UUID workItemUUID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			WorkItem workItem = WorkItem.findByUUID(em, workItemUUID, session.getCompanyID());
			if (workItem == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WorkItem %s not found", workItemUUID);
			return workItem;
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/uuid/{uuid}", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get WorkItems
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.WorkItem[] getWorkItem( //
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
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				List<WorkItem> workItems = WorkItem.findAll(em, params, session.getCompanyID());
				return workItems.toArray(new WorkItem[workItems.size()]);
			}
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("getWorkItem", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("*")
	@Produces(MediaType.APPLICATION_JSON)
	public Long getWorkItemsCount( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID, 0, -1, null, search, filter);
			Session session = context.getSession(params.getSessionID());
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				return WorkItem.findCount(em, params, session.getCompanyID());
			}
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("getWorkItemsCount", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/for_me")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.WorkItem[] getForMe( //
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
			List<WorkItem> workItems = WorkItem.findForMe(em, params, session);
			return workItems.toArray(new WorkItem[workItems.size()]);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/for_me", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/for_me/*")
	@Produces(MediaType.APPLICATION_JSON)
	public Long getForMeCount( //
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
			return WorkItem.findForMeCount(em, params, session);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/for_me/*", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/my_history")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.WorkItem[] getMyHistory( //
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
			List<WorkItem> workItems = WorkItem.findMyHistory(em, params, session);
			return workItems.toArray(new WorkItem[workItems.size()]);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/my_history", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/my_history/*")
	@Produces(MediaType.APPLICATION_JSON)
	public Long getMyHistoryCount( //
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
			return WorkItem.findMyHistoryCount(em, params, session);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/my_history/*", ex);
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
	public void updateWorkItem(hxc.ecds.protocol.rest.WorkItem workItem, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Determine if this is a new Instance
			boolean isNew = workItem.getId() <= 0;
			if (isNew)
			{
				createWorkItem(em, workItem, session, params);
				return;
			}

			// Test Permission
			session.check(em, WorkItem.MAY_UPDATE, "Not allowed to Update WorkItem %d", workItem.getId());

			// Get the Existing Instance
			WorkItem existing = WorkItem.findByID(em, workItem.getId(), session.getCompanyID());
			if (existing == null || workItem.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WorkItem %d not found", workItem.getId());
			WorkItem updated = existing;
			existing = new WorkItem(existing);
			updated.amend(workItem);

			testCommon(em, session, updated);

			// Persist to Database
			AuditEntryContext auditContext = new AuditEntryContext("WORK_ITEM_UPDATE", updated.getId());
			updated.persist(em, existing, session, auditContext);

			// Send SMS Notification
			sendSms(em, existing, updated, session);

		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("updateWorkItem", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	// /////////////////////////////////

	private void createWorkItem(EntityManagerEx em, hxc.ecds.protocol.rest.WorkItem workItem, Session session, RestParams params) throws RuleCheckException
	{
		// Test Permission
		session.check(em, WorkItem.MAY_ADD, "Not allowed to Create WorkItem");

		// Persist it
		WorkItem newWorkItem = new WorkItem();
		newWorkItem.amend(workItem);
		testCommon(em, session, newWorkItem);
		AuditEntryContext auditContext = new AuditEntryContext("WORK_ITEM_CREATE");
		newWorkItem.persist(em, null, session, auditContext);

		// Send SMS Notification
		sendSms(em, null, newWorkItem, session);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Generate OTP
	//
	// /////////////////////////////////

	@POST
	@Path("/{id}/generate_co_sign_otp")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public GenerateWorkItemCoSignOTPResponse generateCoSignOTP(@PathParam("id") int workItemID, @HeaderParam(RestParams.SID) String sessionID, GenerateWorkItemCoSignOTPRequest request)
	{
		GenerateWorkItemCoSignOTPResponse response = new GenerateWorkItemCoSignOTPResponse();
		response.setReturnCode(GenerateWorkItemCoSignOTPResponse.RETURN_CODE_OTHER_ERROR);
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Get the Existing Instance
			WorkItem workItem = WorkItem.findByID(em, workItemID, session.getCompanyID());
			if (workItem == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WorkItem %d not found", workItemID);

			response = generateCoSignOTP(em, workItem, session, request);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/{id}/generate_co_sign_otp", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
		return response;
	}

	public GenerateWorkItemCoSignOTPResponse generateCoSignOTP(EntityManager em, WorkItem workItem, Session session, GenerateWorkItemCoSignOTPRequest request) throws Exception
	{
		if (session.getUserType() != Session.UserType.WEBUSER || session.getWebUserID() == null)
			throw new RuleCheckException(TransactionsConfig.ERR_NOT_WEBUSER_SESSION, null, "Cannot generate CoSign OTP for non-webuser session");

		if (session.isCoSignOnly())
			throw new RuleCheckException(TransactionsConfig.ERR_CO_SIGN_ONLY_SESSION, null, "Cannot generate CoSign OTP for CoSignOnly session");

		int companyID = session.getCompanyID();
		WorkflowConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, WorkflowConfig.class);

		Statex state = new Statex();
		state.config = config;

		state.actorAgent = session.getAgent();
		state.actorWebUser = session.getWebUserID() == null ? null : WebUser.findByID(em, session.getWebUserID(), companyID);
		state.ownerAgent = workItem.getCreatedByAgent();
		state.ownerWebUser = workItem.getCreatedByWebUser();
		state.notificationFields = config.listActorOtpNotificationFields();
		state.workItem = workItem;
		state.OTPString = session.generateCoSignatoryOTP(request.getCoSignForSessionID(), String.valueOf(workItem.getId()), config.getActorOtpExpiry());
		state.OTPExpiryDate = session.getCoSignatoryOTPExpiryDate();

		sendSms(state.actorWebUser.getMobileNumber(), session.getLocale(state.actorWebUser.getLanguage()), config.getActorOtpNotification(), state);
		GenerateWorkItemCoSignOTPResponse response = new GenerateWorkItemCoSignOTPResponse().setCoSignatoryTransactionID(String.valueOf(workItem.getId()));
		response.setReturnCode(GenerateWorkItemCoSignOTPResponse.RETURN_CODE_SUCCESS);
		return response;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteWorkItem(@PathParam("id") int workItemID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Test Permission
			session.check(em, WorkItem.MAY_DELETE, "Not allowed to Delete WorkItem %d", workItemID);

			// Get the Existing Instance
			WorkItem existing = WorkItem.findByID(em, workItemID, session.getCompanyID());
			if (existing == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WorkItem %d not found", workItemID);

			// Remove from Database
			AuditEntryContext auditContext = new AuditEntryContext("WORK_ITEM_REMOVE", existing.getId());
			existing.remove(em, session, auditContext);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/{id}", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private void testCommon(EntityManagerEx em, Session session, WorkItem workItem) throws RuleCheckException
	{
		// Test By WebUser
		int companyID = session.getCompanyID();

		// Test By WebUser
		Integer byWebUserID = workItem.getCreatedByWebUserID();
		if (byWebUserID == null)
		{
			workItem.setCreatedByWebUser(null);
		}
		else
		{
			WebUser createdByWebUser = WebUser.findByID(em, byWebUserID, companyID);
			if (createdByWebUser == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", byWebUserID);
			else
				workItem.setCreatedByWebUser(createdByWebUser);
		}

		// Test By Agent
		Integer byAgentID = workItem.getCreatedByAgentID();
		if (byAgentID == null)
		{
			workItem.setCreatedByAgent(null);
		}
		else
		{
			Agent createdByAgent = Agent.findByID(em, byAgentID, companyID);
			if (createdByAgent == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Agent %d not found", byAgentID);
			else
				workItem.setCreatedByAgent(createdByAgent);
		}

		// Test For WebUser
		Integer forWebUserID = workItem.getCreatedForWebUserID();
		if (forWebUserID == null)
		{
			workItem.setCreatedForWebUser(null);
		}
		else
		{
			WebUser createdForWebUser = WebUser.findByID(em, forWebUserID, companyID);
			if (createdForWebUser == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", forWebUserID);
			else
				workItem.setCreatedForWebUser(createdForWebUser);
		}

		// Test Permission
		Integer permissionID = workItem.getCreatedForPermissionID();
		if (permissionID == null)
		{
			workItem.setCreatedForPermission(null);
		}
		else
		{
			Permission createdForPermission = Permission.findByID(em, permissionID);
			if (createdForPermission == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Permission %d not found", forWebUserID);
			else
				workItem.setCreatedForPermission(createdForPermission);
		}
	}

	// Send SMS to Recipients
	private void sendSms(EntityManager em, WorkItem previous, WorkItem workItem, Session session)
	{
		logger.trace("sendSms(em = {}, previous = {}, workItem = {}, session = {})", em, previous, workItem, session);
		// Exit if SMS not enabled
		if (workItem == null || !workItem.isSmsOnChange())
			return;

		// Exit if no change
		if (previous != null && previous.getState().equals(workItem.getState()))
			return;

		// Create State Object
		Statex state = new Statex();

		// Determine who is the Actor
		int companyID = session.getCompanyID();
		state.actorAgent = session.getAgent();
		state.actorWebUser = session.getWebUserID() == null ? null : WebUser.findByID(em, session.getWebUserID(), companyID);

		// Determine who is the Owner
		state.ownerAgent = workItem.getCreatedByAgent();
		state.ownerWebUser = workItem.getCreatedByWebUser();

		// Determine who is the recipient
		WebUser recipientWebUser = workItem.getCreatedForWebUser();
		state.recipientWebUsers = new ArrayList<WebUser>();
		if (recipientWebUser != null)
			state.recipientWebUsers.add(recipientWebUser);
		state.permission = workItem.getCreatedForPermission();
		if (state.permission != null)
		{
			List<WebUser> webUsersWithPermission = WebUser.findWithPermission(em, state.permission.getId(), companyID);
			for (WebUser webUser : webUsersWithPermission)
			{
				if (!state.recipientWebUsers.contains(webUser))
					state.recipientWebUsers.add(webUser);
			}
		}

		// Get the Workflow configuration
		WorkflowConfig config = context.findCompanyInfoByID(companyID).getConfiguration(em, WorkflowConfig.class);
		state.config = config;
		state.notificationFields = config.listNotificationFields();
		state.workItem = workItem;

		// Send to Actor
		if (state.actorAgent != null)
			sendSms(state.actorAgent.getMobileNumber(), session.getLocale(state.actorAgent.getLanguage()), config.getActorNotification(), state);
		if (state.actorWebUser != null)
			sendSms(state.actorWebUser.getMobileNumber(), session.getLocale(state.actorWebUser.getLanguage()), config.getActorNotification(), state);

		// Send to Owner
		if (state.ownerWebUser != null && !sameWebUser(state.actorWebUser, state.ownerWebUser))
		{
			sendSms(state.ownerWebUser.getMobileNumber(), session.getLocale(state.ownerWebUser.getLanguage()), config.getOwnerNotification(), state);
		}
		else if (state.ownerAgent != null && !sameAgent(state.actorAgent, state.ownerAgent))
		{
			sendSms(state.ownerAgent.getMobileNumber(), session.getLocale(state.ownerAgent.getLanguage()), config.getOwnerNotification(), state);

		}

		// Send to Recipient List
		for (WebUser recipient : state.recipientWebUsers)
		{
			if (Objects.equals(recipient.getState(), WebUser.STATE_PERMANENT) == false && Objects.equals(recipient.getState(), WebUser.STATE_ACTIVE) == false)
			{
				logger.debug("Not sending work item notification to inactive WebUser( companyID = {}, accountNumber = {}, domainAccountName = {} )", recipient.getCompanyID(),
						recipient.getAccountNumber(), recipient.getDomainAccountName());
				continue;
			}
			if (!sameWebUser(recipient, state.ownerWebUser) && !sameWebUser(recipient, state.actorWebUser))
			{
				logger.debug("Sending work item notification to active WebUser( companyID = {}, accountNumber = {}, domainAccountName = {} )", recipient.getCompanyID(),
						recipient.getAccountNumber(), recipient.getDomainAccountName());
				sendSms(recipient.getMobileNumber(), session.getLocale(recipient.getLanguage()), config.getRecipientNotification(), state);
			}
		}

	}

	private boolean sameWebUser(WebUser left, WebUser right)
	{
		if (left == null || right == null)
			return false;
		return left.getId() == right.getId();
	}

	private boolean sameAgent(Agent left, Agent right)
	{
		if (left == null || right == null)
			return false;
		return left.getId() == right.getId();
	}

	private void sendSms(String mobileNumber, Locale locale, Phrase notification, Statex state)
	{
		logger.trace("sendSms(mobileNumber = {}, locale = {}, notification = {}, state = {})", mobileNumber, locale, notification, state);
		StringExpander<Statex> expander = new StringExpander<Statex>()
		{

			@Override
			protected String expandField(String englishName, Locale locale, Statex state)
			{
				return WorkItems.this.expandField(englishName, locale, state);
			}

		};
		String text = expander.expandNotification(notification, locale, state.notificationFields, state);
		context.sendSMS(mobileNumber, locale.getISO3Language(), text);
	}

	public String expandField(String englishName, Locale locale, Statex state)
	{
		logger.trace("expandField:englishName = {}", englishName);
		switch (englishName)
		{
			case WorkflowConfig.ACTOR:
				if (state.actorAgent != null)
					return state.actorAgent.getMobileNumber();
				else if (state.actorWebUser != null)
					return state.actorWebUser.getDomainAccountName();
				break;

			case WorkflowConfig.OWNER:
				if (state.ownerAgent != null)
					return state.ownerAgent.getMobileNumber();
				else if (state.ownerWebUser != null)
					return state.ownerWebUser.getDomainAccountName();
				break;

			case WorkflowConfig.RECIPIENT:
				if (state.recipientWebUsers.size() == 1)
					return state.recipientWebUsers.get(0).getDomainAccountName();
				else
					return state.config.getForPermission().safe(locale.getLanguage(), "");

			case WorkflowConfig.ACTION:
				Phrase action = state.config.getActions().get(state.workItem.getState());
				if (action != null)
					return action.safe(locale.getLanguage(), state.workItem.getState());
				else
					return state.workItem.getState();

			case WorkflowConfig.TYPE:
				Phrase type = state.config.getTypes().get(state.workItem.getType());
				if (type != null)
					return type.safe(locale.getLanguage(), state.workItem.getType());
				else
					return state.workItem.getType();

			case WorkflowConfig.OTP:
				logger.trace("expandField:OTP - {}", englishName, state.OTPString);
				if (state.OTPString != null)
				{
					return state.OTPString;
				}
				break;

			case WorkflowConfig.OTP_EXPIRY_DATETIME:
				logger.trace("expandField:OTP_EXPIRY_DATETIME - {}", englishName, state.OTPExpiryDate);
				if (state.OTPExpiryDate != null)
				{
					DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
					return df.format(state.OTPExpiryDate);
				}
				break;

			case WorkflowConfig.DESCRIPTION:
				return state.workItem.getDescription();

		}
		return "  ";
	}

	private class Statex
	{
		public Permission permission;
		public ArrayList<WebUser> recipientWebUsers;
		public WebUser ownerWebUser;
		public Agent ownerAgent;
		public WebUser actorWebUser;
		public Agent actorAgent;
		public WorkItem workItem;
		public WorkflowConfig config;
		public String OTPString;
		public Date OTPExpiryDate;
		public Phrase[] notificationFields = { Phrase.en(WorkflowConfig.ACTOR), Phrase.en(WorkflowConfig.OWNER), Phrase.en(WorkflowConfig.RECIPIENT), //
				Phrase.en(WorkflowConfig.ACTION), Phrase.en(WorkflowConfig.TYPE), Phrase.en(WorkflowConfig.DESCRIPTION) };
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@GET
	@Path("/config")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.config.WorkflowConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.WorkflowConfig.class);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/config", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	@Path("/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.WorkflowConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, WorkItem.MAY_CONFIGURE);
			context.findCompanyInfoByID(session.getCompanyID()).setConfiguration(em, configuration, session);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/config", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}
}
