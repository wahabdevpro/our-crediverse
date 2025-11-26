package hxc.services.ecds.rest;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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

import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.rest.batch.TierProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/tiers")
public class Tiers
{
	final static Logger logger = LoggerFactory.getLogger(Tiers.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Tier
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Tier getTier(@PathParam("id") int tierID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			Tier tier = Tier.findByID(em, tierID, session.getCompanyID());
			if (tier == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Tier %d not found", tierID);
			return tier;
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
	public hxc.ecds.protocol.rest.Tier getRootTier(@HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			Tier tier = Tier.findRoot(em, session.getCompanyID());
			if (tier == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Root Tier not found");
			return tier;
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
	@Path("/subscriber")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Tier getSubscriberTier(@HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			Tier tier = Tier.findSubscriber(em, session.getCompanyID());
			if (tier == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Subscriber Tier not found");
			return tier;
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
	// Get Tiers
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Tier[] getTiers( //
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
				List<Tier> tiers = Tier.findAll(em, params, session.getCompanyID());
				return tiers.toArray(new Tier[tiers.size()]);
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
	@Path("*")
	@Produces(MediaType.APPLICATION_JSON)
	public Long getTierCount( //
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
				return Tier.findCount(em, params, session.getCompanyID());
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
	public String getTierCsv( //
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
			List<Tier> serviceClasses;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				serviceClasses = Tier.findAll(em, params, session.getCompanyID());
			}

			CsvExportProcessor<Tier> processor = new CsvExportProcessor<Tier>(TierProcessor.HEADINGS, first)
			{
				@Override
				protected void write(Tier record)
				{
					put("id", record.getId());
					put("name", record.getName());
					put("status", record.getState());
					put("description", record.getDescription());
					put("type", record.getType());
					put("max_amount", record.getMaxTransactionAmount());
					put("max_daily_count", record.getMaxDailyCount());
					put("max_daily_amount", record.getMaxDailyAmount());
					put("max_monthly_count", record.getMaxMonthlyCount());
					put("max_monthly_amount", record.getMaxMonthlyAmount());
					put("allow_intratier_transfer", record.isAllowIntraTierTransfer());
					put("default_bonus_pct", record.getBuyerDefaultTradeBonusPercentage());
				}
			};
			return processor.add(serviceClasses);
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
	public void updateTier(hxc.ecds.protocol.rest.Tier tier, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Determine if this is a new Instance
			boolean isNew = tier.getId() <= 0;
			if (isNew)
			{
				createTier(em, tier, session, params);
				return;
			}

			// Test Permission
			session.check(em, Tier.MAY_UPDATE, "Not allowed to Update Tier %d", tier.getId());

			// Get the Existing Instance
			Tier existing = Tier.findByID(em, tier.getId(), session.getCompanyID());
			if (existing == null || tier.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Tier %d not found", tier.getId());
			Tier updated = existing;
			existing = new Tier(existing);
			updated.amend(tier);

			// Persist to Database
			AuditEntryContext auditContext = new AuditEntryContext("TIER_UPDATE", updated.getName(), updated.getId());
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
	// Create
	//
	// /////////////////////////////////

	private void createTier(EntityManagerEx em, hxc.ecds.protocol.rest.Tier tier, Session session, RestParams params) throws RuleCheckException
	{
		// Test Permission
		session.check(em, Tier.MAY_ADD, "Not allowed to Create Tier");

		// Cannot create new Permanent
		RuleCheck.isFalse("permanent", tier.isPermanent(), "May not Create new Permanent Tier");

		// Persist it
		Tier newTier = new Tier();
		newTier.amend(tier);
		AuditEntryContext auditContext = new AuditEntryContext("TIER_CREATE", newTier.getName());
		newTier.persist(em, null, session, auditContext);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteTier(@PathParam("id") int tierID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Test Permission
			session.check(em, Tier.MAY_DELETE, "Not allowed to Delete Tier %d", tierID);

			// Get the Existing Instance
			Tier existing = Tier.findByID(em, tierID, session.getCompanyID());
			if (existing == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Tier %d not found", tierID);

			// Test if in use
			if (Transaction.referencesTier(em, tierID) || Agent.referencesTier(em, tierID) //
					|| Group.referencesTier(em, tierID) || TransferRule.referencesTier(em, tierID))
				throw new RuleCheckException(StatusCode.RESOURCE_IN_USE, null, "Tier %d is in use", tierID);

			// Remove from Database
			AuditEntryContext auditContext = new AuditEntryContext("TIER_REMOVE", existing.getName(), existing.getId());
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
	// Helpers
	//
	// /////////////////////////////////

}
