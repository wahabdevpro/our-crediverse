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
import hxc.services.ecds.rest.batch.GroupProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/groups")
public class Groups
{
	final static Logger logger = LoggerFactory.getLogger(Groups.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Group
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Group getGroup(@PathParam("id") int groupID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			Group group = Group.findByID(em, groupID, session.getCompanyID());
			if (group == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Group %d not found", groupID);
			return group;
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
	// Get Groups
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Group[] getGroups( //
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
				List<Group> groups = Group.findAll(em, params, session.getCompanyID());
				return groups.toArray(new Group[groups.size()]);
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
	public Long getGroupCount( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID, 0, -1, null, search, filter);
			Session session = context.getSession(params.getSessionID());
			// TODO Remove when queries are fixed
			try (QueryToken token = context.getQueryToken())
			{
				return Group.findCount(em, params, session.getCompanyID());
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
	public String getGroupCsv( //
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
			List<Group> serviceClasses;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				serviceClasses = Group.findAll(em, params, session.getCompanyID());
			}

			CsvExportProcessor<Group> processor = new CsvExportProcessor<Group>(GroupProcessor.HEADINGS, first)
			{
				@Override
				protected void write(Group record)
				{
					put("id", record.getId());
					put("name", record.getName());
					put("status", record.getState());
					put("description", record.getDescription());
					put("max_amount", record.getMaxTransactionAmount());
					put("max_daily_count", record.getMaxDailyCount());
					put("max_daily_amount", record.getMaxDailyAmount());
					put("max_monthly_count", record.getMaxMonthlyCount());
					put("max_monthly_amount", record.getMaxMonthlyAmount());
					if (record.getTier() != null)
						put("tier", record.getTier().getName());
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
	public void updateGroup(hxc.ecds.protocol.rest.Group group, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Determine if this is a new Instance
			boolean isNew = group.getId() <= 0;
			if (isNew)
			{
				createGroup(em, group, session, params);
				return;
			}

			// Test Permission
			session.check(em, Group.MAY_UPDATE, "Not allowed to Update Group %d", group.getId());

			// Get the Existing Instance
			Group existing = Group.findByID(em, group.getId(), session.getCompanyID());
			if (existing == null || group.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Group %d not found", group.getId());
			Group updated = existing;
			existing = new Group(existing);
			updated.amend(group);
			updated.setTier(Tier.findByID(em, updated.getTierID(), updated.getCompanyID()));
			RuleCheck.notNull("tierID", updated.getTier());

			// The TierID cannot change if it is in use
			if (updated.getTierID() != existing.getTierID())
			{
				if (Agent.referencesGroup(em, updated.getId()) || TransferRule.referencesGroup(em, updated.getId()))
					RuleCheck.noChange("tierID", updated.getTierID(), existing.getTierID());
			}

			// Persist to Database
			AuditEntryContext auditContext = new AuditEntryContext("GROUP_UPDATE", updated.getId());
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

	private void createGroup(EntityManagerEx em, hxc.ecds.protocol.rest.Group group, Session session, RestParams params) throws RuleCheckException
	{
		// Test Permission
		session.check(em, Group.MAY_ADD, "Not allowed to Create Group");

		// Persist it
		Group newGroup = new Group();
		newGroup.amend(group);
		newGroup.setTier(Tier.findByID(em, newGroup.getTierID(), newGroup.getCompanyID()));
		RuleCheck.notNull("tierID", newGroup.getTier());
		AuditEntryContext auditContext = new AuditEntryContext("GROUP_CREATE", newGroup.getName());
		newGroup.persist(em, null, session, auditContext);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteGroup(@PathParam("id") int groupID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Test Permission
			session.check(em, Group.MAY_DELETE, "Not allowed to Delete Group %d", groupID);

			// Get the Existing Instance
			Group existing = Group.findByID(em, groupID, session.getCompanyID());
			if (existing == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Group %d not found", groupID);

			// Test if in use
			if (Transaction.referencesGroup(em, groupID) || Agent.referencesGroup(em, groupID) //
					|| TransferRule.referencesGroup(em, groupID))
				throw new RuleCheckException(StatusCode.RESOURCE_IN_USE, null, "Group %d is in use", groupID);

			// Remove from Database
			AuditEntryContext auditContext = new AuditEntryContext("GROUP_REMOVE", existing.getId());
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
