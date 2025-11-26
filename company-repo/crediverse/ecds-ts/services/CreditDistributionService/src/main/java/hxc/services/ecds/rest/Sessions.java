package hxc.services.ecds.rest;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.AgentUser;
import hxc.services.ecds.model.Company;
import hxc.services.ecds.model.Permission;
import hxc.services.ecds.model.WebUser;
import hxc.services.ecds.model.WorkItem;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/sessions")
public class Sessions
{
	final static Logger logger = LoggerFactory.getLogger(Sessions.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String TEST_SESSION_ID = "test";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Session
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Session getSession(@PathParam("id") String id, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		Session session = null;
		if (TEST_SESSION_ID.equalsIgnoreCase(id))
		{
			try (EntityManagerEx em = context.getEntityManager())
			{
				Company company = Company.findByName(em, Company.NAME_SUPPLIER);
				CompanyInfo companyInfo = context.findCompanyInfoByID(em, company.getId());
				session = context.getSessions().getNew(companyInfo);
				session.setExpiryTime(new Date(new Date().getTime() + 24 * 60 * 60 * 1000L));
				session.setState(Session.State.AUTHENTICATED);
				WebUser webUser = WebUser.findByAccountNo(em, company.getId(), WebUser.ACCOUNT_NO_ADMINISTRATOR);
				Objects.requireNonNull(webUser, "webUser may not be null");
				session.setWebUser(webUser);
				session.set(Session.TEST_MODE, true);
			}
			catch (Throwable ex)
			{
				logger.error("Error getting test session", ex);
				throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
			}

			return session;
		}
		else
		{
			try
			{
				session = context.getSession(id);
			}
			catch (RuleCheckException ex)
			{
				logger.info("rulecheck", ex);
				throw ex.toWebException();
			}
			catch (Throwable ex)
			{
				logger.error("error retrieving session", ex);
				throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
			}

		}

		return convert(session);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Session
	//
	// /////////////////////////////////
	@GET
	@Path("/expiry")
	@Produces(MediaType.APPLICATION_JSON)
	public Date getExpiry(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try
		{
			Session session = context.getSession(sessionID);
			return session.getExpiryTime();
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/expiry", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/mine")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Session getMine(@HeaderParam(RestParams.SID) String sessionID)
	{
		try
		{
			Session session = context.getSession(sessionID);
			return convert(session);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/mine", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/work_item/{uuid}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Session getWorkItem(@PathParam("uuid") UUID uuid, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session sessionx = context.getSession(sessionID);
			int companyID = sessionx.getCompanyID();

			// Get the original Work Item
			WorkItem workItem = WorkItem.findByUUID(em, uuid, companyID);
			if (workItem == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, "uuid", "WorkItem %s not found", uuid);
			RuleCheck.oneOf("state", workItem.getState(), WorkItem.STATE_NEW, WorkItem.STATE_IN_PROGRESS, WorkItem.STATE_ON_HOLD);

			// For caller?
			WebUser recipient = workItem.getCreatedForWebUser();
			Permission permission = workItem.getCreatedForPermission();
			if (recipient != null && recipient.getId() != sessionx.getUserID() //
					|| permission != null && !sessionx.hasPermission(em, permission))
			{
				throw new RuleCheckException(StatusCode.UNAUTHORIZED, "uuid", "Not your WorkItem");
			}

			// Agent Session
			Session originalSession = null;
			if (workItem.getCreatedByAgent() != null)
			{
				originalSession = context.getSessions() //
						.getAgentSession(em, context, sessionx.getCompanyID(), //
								workItem.getCreatedByAgent().getMobileNumber(), sessionx.getChannel(), null);
			}

			// Web User Session
			else if (workItem.getCreatedByWebUser() != null)
			{
				CompanyInfo companyInfo = context.findCompanyInfoByID(em, companyID);
				originalSession = context.getSessions().getNew(companyInfo);
				originalSession.setState(Session.State.AUTHENTICATED);
				originalSession.setWebUser(workItem.getCreatedByWebUser());
			}

			return convert(originalSession);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/work_item/{uuid}", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Web User Session
	//
	// /////////////////////////////////
	@GET
	@Path("/test/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Session getWebUserTestSession(
		@PathParam("id") int id, @HeaderParam(RestParams.SID) String sessionID,
		@QueryParam("coSignForSessionID") String coSignForSessionID,
		@QueryParam("coSignatoryTransactionID") String coSignatoryTransactionID)
	{
		RestParams params = new RestParams(sessionID);
		Session session = null;
		logger.info("Request for test session with user id = {}, session id = {}, coSignForSessionID = {}, coSignatoryTransactionID = {}",
			id, sessionID, coSignForSessionID, coSignatoryTransactionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Company company = Company.findByName(em, Company.NAME_SUPPLIER);
			CompanyInfo companyInfo = context.findCompanyInfoByID(em, company.getId());
			session = context.getSessions().getNew(companyInfo);
			session.setExpiryTime(new Date(new Date().getTime() + 24 * 60 * 60 * 1000L));
			session.setState(Session.State.AUTHENTICATED);
			WebUser webUser = WebUser.findByID(em, id, company.getId());
			session.setWebUser(webUser);
			session.setCoSignForSessionID(coSignForSessionID);
			session.setCoSignatoryTransactionID(coSignatoryTransactionID);
			if (session.getCoSignatoryTransactionID() != null || session.getCoSignatoryTransactionID() != null)
			{
				session.setCoSignOnly(true);
			}
			session.set(Session.TEST_MODE, true);
			logger.trace("session.channel = {}, session.type = {}, session.coSignForSessionID = {}, session.coSignatoryTransactionID = {}, session.coSignOnly = {}",
				session.getChannel(), session.getUserType(), session.getCoSignForSessionID(), session.getCoSignatoryTransactionID(), session.getCoSignOnly());

		}
		catch (Throwable ex)
		{
			logger.error("/test/{id}", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

		return session;

	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Agent User Session
	//
	// /////////////////////////////////
	@GET
	@Path("/test/{agentId}/{userId}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Session getWebUserTestSession(
		@PathParam("agentId") int agentID, @PathParam("userId") int userID, @HeaderParam(RestParams.SID) String sessionID,
		@QueryParam("coSignForSessionID") String coSignForSessionID,
		@QueryParam("coSignatoryTransactionID") String coSignatoryTransactionID)
	{
		RestParams params = new RestParams(sessionID);
		Session session = null;
		logger.info("Request for test session with agent id = {}, agent user id = {}, session id = {}, coSignForSessionID = {}, coSignatoryTransactionID = {}",
				agentID, userID, sessionID, coSignForSessionID, coSignatoryTransactionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Company company = Company.findByName(em, Company.NAME_SUPPLIER);
			CompanyInfo companyInfo = context.findCompanyInfoByID(em, company.getId());
			session = context.getSessions().getNew(companyInfo);
			session.setExpiryTime(new Date(new Date().getTime() + 24 * 60 * 60 * 1000L));
			session.setState(Session.State.AUTHENTICATED);
			Agent agent = Agent.findByID(em, agentID, company.getId());
			session.setAgentID(agentID);
			session.setAgent(agent);
			
			AgentUser agentUser = AgentUser.findByID(em, userID, company.getId());
			session.setAgentUserID(userID);
			session.setAgentUser(agentUser);
			session.setDomainAccountName(agentUser.getDomainAccountName());
			session.setCoSignForSessionID(coSignForSessionID);
			session.setCoSignatoryTransactionID(coSignatoryTransactionID);
			if (session.getCoSignatoryTransactionID() != null || session.getCoSignatoryTransactionID() != null)
			{
				session.setCoSignOnly(true);
			}
			session.set(Session.TEST_MODE, true);
			logger.trace("session.channel = {}, session.type = {}, session.coSignForSessionID = {}, session.coSignatoryTransactionID = {}, session.coSignOnly = {}",
					session.getChannel(), session.getUserType(), session.getCoSignForSessionID(), session.getCoSignatoryTransactionID(), session.getCoSignOnly());

		}
		catch (Throwable ex)
		{
			logger.error("/test/{agentId}/{userId}", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
		return session;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Agent Session
	//
	// /////////////////////////////////
	@GET
	@Path("/agent/{msisdn}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Session getAgentTestSession(@PathParam("msisdn") String msisdn, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		Session session = null;

		try (EntityManagerEx em = context.getEntityManager())
		{
			Company company = Company.findByName(em, Company.NAME_SUPPLIER);
			session = context.getSessions().getAgentSession(em, context, company.getId(), msisdn, Session.CHANNEL_WUI, null);
			Objects.requireNonNull(session, "session is null after retrieval");
			session.set(Session.TEST_MODE, true);
		}
		catch (Throwable ex)
		{
			logger.error("/agent/{msisdn}", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

		return session;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Sessions
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Session[] getSessions( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		try
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			Boolean testMode = session.get(Session.TEST_MODE);
			if (testMode == null || !testMode)
				throw new RuleCheckException(StatusCode.UNAUTHORIZED, null, "Only Allowed in TestMode");

			Session[] sessions = context.getSessions().toArray();
			hxc.ecds.protocol.rest.Session[] result = new hxc.ecds.protocol.rest.Session[sessions.length];
			int index = 0;
			for (Session sessin : sessions)
			{
				result[index++] = convert(sessin);
			}
			return result;
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete Session
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteSession(@PathParam("id") String id, @HeaderParam(RestParams.SID) String sessionID) throws RuleCheckException
	{
		Session session = context.getSession(id);
		if(session != null)
		{
			Integer agentID = session.getAgentID();
			synchronized (context.getCallbackItemsLock()) {
				if(agentID != null)
				{
					context.deregisterSessionFromCallback(sessionID, agentID);
				}
			}
		}
		context.getSessions().delete(id);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private hxc.ecds.protocol.rest.Session convert(Session session)
	{
		if (session == null)
			return null;

		hxc.ecds.protocol.rest.Session response = new hxc.ecds.protocol.rest.Session();
		response.setSessionID(session.getSessionID());
		response.setExpiryTime(session.getExpiryTime());
		response.setCompanyID(session.getCompanyID());
		response.setLanguageID(session.getLanguageID());
		response.setWebUserID(session.getWebUserID());
		response.setAgentID(session.getAgentID());
		response.setDomainAccountName(session.getDomainAccountName());
		response.setCountryID(session.getCountryID());
		response.setCompanyPrefix(session.getCompanyPrefix());
		response.setOwnerAgentID(session.getOwnerAgentID());
		response.setAgentUserID(session.getAgentUserID());
		return response;
	}

}
