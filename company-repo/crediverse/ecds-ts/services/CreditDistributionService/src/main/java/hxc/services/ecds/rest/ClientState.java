package hxc.services.ecds.rest;

import static hxc.services.ecds.Session.CHANNEL_3PP;

import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.IAgentUser;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/client_state")
public class ClientState
{
	final static Logger logger = LoggerFactory.getLogger(ClientState.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get ServiceClass
	//
	// /////////////////////////////////
	@GET
	@Path("/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.ClientState getClientState(@PathParam("key") String key, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			hxc.services.ecds.model.ClientState clientState = new hxc.services.ecds.model.ClientState();
			clientState.setKey(key);
			complete(clientState, session);
			clientState = hxc.services.ecds.model.ClientState.findByKey(em, clientState);
			if (clientState == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Client State for %d not found", key);
			return clientState;
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
	public void updateClientState(hxc.ecds.protocol.rest.ClientState clientState, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Get Existing Instance
			hxc.services.ecds.model.ClientState cs = new  hxc.services.ecds.model.ClientState();
			cs.amend(clientState);
			complete(cs, session);
			hxc.services.ecds.model.ClientState existing = hxc.services.ecds.model.ClientState.findByKey(em, cs);
			
			// Create if new
			if (existing == null)
			{
				AuditEntryContext auditContext = new AuditEntryContext("CLIENTSTATE_CREATE");
				cs.persist(em, null, session, auditContext);
				return;
			}
			
			// Update Existing
			hxc.services.ecds.model.ClientState updated = existing;
			existing = new hxc.services.ecds.model.ClientState(existing);
			updated.amend(clientState);
			complete(updated, session);
			AuditEntryContext auditContext = new AuditEntryContext("CLIENTSTATE_UPDATE");
			updated.persist(em, existing, session, auditContext);

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
	// Helpers
	//
	// /////////////////////////////////
	private void complete(hxc.services.ecds.model.ClientState clientState, Session session)
	{
		clientState.setLastUpdated(new Date());
		clientState.setCompanyID(session.getCompanyID());
		IAgentUser agentUser = session.getAgentUser();
		if (agentUser == null)
		{
			if (CHANNEL_3PP.equals(session.getChannel())) {
				clientState.setUserType(Transaction.REQUESTER_TYPE_SERVICE_USER);
			} else {
				clientState.setUserType(Transaction.REQUESTER_TYPE_WEB_USER);
			}
			clientState.setUserID(session.getWebUserID());
		}
		else if (agentUser instanceof Agent)
		{
			clientState.setUserType( Transaction.REQUESTER_TYPE_AGENT);
			clientState.setUserID(session.getAgentID());
		}
		else
		{
			clientState.setUserType( Transaction.REQUESTER_TYPE_AGENT_USER);
			clientState.setUserID(agentUser.getId());
		}
		
	}

}
