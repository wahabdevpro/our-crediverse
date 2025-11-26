package hxc.services.ecds.rest;

import java.util.List;

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

import hxc.ecds.protocol.rest.AgentAccount;
import hxc.ecds.protocol.rest.AgentAccountEx;
import hxc.ecds.protocol.rest.ExResult;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Role;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/accounts")
public class Accounts
{
	final static Logger logger = LoggerFactory.getLogger(Accounts.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Audit Entry
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Account getAccount(@PathParam("id") int agentID, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			Account account = Account.findByAgentID(em, agentID, false);
			if (account == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Account %d not found", agentID);

			return account;
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
	// Get Accounts
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Account[] getAccounts( //
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
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				List<Account> accounts = Account.findAll(em, params, session.getCompanyID());
				return accounts.toArray(new Account[accounts.size()]);
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
	// Get Agent Accounts
	//
	// /////////////////////////////////
	@GET
	@Path("/agent")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.AgentAccount[] getAgentAccounts( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		logger.info("getAgentAccounts: entry: sessionID = {}, first = {}, max = {}, sort = {}, search = {}, filter = {}", sessionID, first, max, sort, search, filter);
		RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			Agent me = session.getAgent();
			List<Account> accounts;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				accounts = me == null ? Account.findAll(em, params, session.getCompanyID()) : Account.findMine(em, params, session.getCompanyID(), me.getId());
			}
			AgentAccount[] agentAccounts = new AgentAccount[accounts.size()];
			int index = 0;
			for (Account account : accounts)
			{
				// 121 Lazy Load Work-Around
				AgentAccount agentAccount;
				try (EntityManagerEx em2 = context.getEntityManager())
				{
					agentAccount = new AgentAccount(account);
					agentAccount.setAgent(Agent.findByID(em2, account.getAgentID(), session.getCompanyID()));
					agentAccounts[index++] = agentAccount;
				}
			}

			logger.trace("getAgentAccounts: end: agentAccounts.length = {}", agentAccounts.length);
			return agentAccounts;

		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(Status.INTERNAL_SERVER_ERROR.toString(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
		finally
		{
			logger.trace("getAgentAccounts: finally ...");
			int debug = 0;
		}
	}

	@GET
	@Path("/agent_ex")
	@Produces(MediaType.APPLICATION_JSON)
	public ExResult<hxc.ecds.protocol.rest.AgentAccountEx> getAgentAccountsEx( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter, //
			@QueryParam(RestParams.WITHCOUNT) Integer withcount)
	{
		logger.info("AgentAccountEx: entry: sessionID = {}, first = {}, max = {}, sort = {}, search = {}, filter = {}", sessionID, first, max, sort, search, filter);
		RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			Agent me = session.getAgent();

			// Integer foundRows = QueryBuilder.getFoundRows(em);
			boolean performCount = ((withcount == null) || (withcount != null && withcount == 1));
			List<Account> accounts = null;
			Long foundRows = null;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				if (me == null)
				{
					accounts = Account.findAll(em, params, session.getCompanyID());
					if (performCount)
						foundRows = Account.findCount(em, params, session.getCompanyID());
				}
				else
				{
					accounts = Account.findMine(em, params, session.getCompanyID(), me.getId());
					if (performCount)
						foundRows = Account.findMyCount(em, params, session.getCompanyID(), me.getId());
				}
			}

			AgentAccountEx[] agentAccounts = new AgentAccountEx[accounts.size()];
			int index = 0;
			for (Account account : accounts)
			{
				Agent agent;

				// 121 LazyLoading work-around
				try (EntityManagerEx em2 = context.getEntityManager())
				{
					agent = Agent.findByID(em2, account.getAgentID(), session.getCompanyID());

					AgentAccountEx ex = new AgentAccountEx(agent, account);

					Tier tier = agent.getTier();
					if (tier != null)
					{
						ex.setTierName(tier.getName());
						ex.setTierType(tier.getType());
					}

					Group group = agent.getGroup();
					if (group != null)
					{
						ex.setGroupName(group.getName());
					}

					ServiceClass sc = agent.getServiceClass();
					if (sc != null)
					{
						ex.setServiceClassName(sc.getName());
					}

					Agent supplier = agent.getSupplier();
					if (supplier != null)
					{
						ex.setSupplierFirstName(supplier.getFirstName());
						ex.setSupplierSurname(supplier.getSurname());
					}

					Agent owner = agent.getOwner();
					if (owner != null)
					{
						ex.setOwnerFirstName(owner.getFirstName());
						ex.setOwnerSurname(owner.getSurname());
					}
					
					Role role = agent.getRole();
					if(role != null)
					{
						ex.setRoleName(role.getName());
					}

					agentAccounts[index++] = ex;
				}
			}

			logger.trace("AgentAccountEx: end: agentAccounts.length = {}", agentAccounts.length);
			return new ExResult<hxc.ecds.protocol.rest.AgentAccountEx>(foundRows, agentAccounts);

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
	@Path("/agent/*")
	@Produces(MediaType.APPLICATION_JSON)
	public Long getAgentAccountCount( //
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
				return me == null ? Account.findCount(em, params, session.getCompanyID()) : Account.findMyCount(em, params, session.getCompanyID(), me.getId());
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

}
