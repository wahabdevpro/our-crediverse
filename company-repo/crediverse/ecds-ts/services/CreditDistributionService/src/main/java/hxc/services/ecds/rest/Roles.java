package hxc.services.ecds.rest;

import java.util.List;

import javax.persistence.EntityManager;
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
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Permission;
import hxc.services.ecds.model.Role;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/roles")
public class Roles
{
	final static Logger logger = LoggerFactory.getLogger(Roles.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Role
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Role getRole(@PathParam("id") int roleID, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			Role role = Role.findByID(em, roleID, session.getCompanyID());
			if (role == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Role %d not found", roleID);
			return role;
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
	// Get Roles
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Role[] getRole( //
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
				List<Role> roles = Role.findAll(em, params, session.getCompanyID());
				return roles.toArray(new Role[roles.size()]);
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
	public Long getRoleCount( //
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
				return Role.findCount(em, params, session.getCompanyID());
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
	public void updateRole(hxc.ecds.protocol.rest.Role role, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Determine if this is a new Instance
			boolean isNew = role.getId() <= 0;
			if (isNew)
			{
				createRole(em, role, session, params);
				return;
			}

			// Test Permission
			session.check(em, Role.MAY_UPDATE, "Not allowed to Update Role %d", role.getId());

			// Get the Existing Instance
			Role updated = Role.findByID(em, role.getId(), session.getCompanyID());
			if (updated == null || role.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Role %d not found", role.getId());
			Role existing = updated.copy(em);
			
			/*
			 *  Create new role for verification only.
			 */
			Role checkRole = updated.copy(em);
			
			checkRole.amend(em, role);
			

			// Do some more Testing
			testCommon(em, session, checkRole);
			
			// Only amend role after testing to avoid privilege escalation vulnerability
			updated.amend(em, role);

			// Persist to Database
			AuditEntryContext auditContext = new AuditEntryContext("ROLE_UPDATE", updated.getName(), updated.getId());
			updated.persist(em, existing, session, auditContext);

			// Flush the Cache
			CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
			companyInfo.flushPermissionCache(updated.getId());

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

	private void createRole(EntityManagerEx em, hxc.ecds.protocol.rest.Role role, Session session, RestParams params) throws RuleCheckException
	{
		// Test Permission
		session.check(em, Role.MAY_ADD, "Not allowed to Create Role");

		// Cannot create new Permanent
		RuleCheck.isFalse("permanent", role.isPermanent(), "May not Create new Permanent Role");

		// Persist it
		Role newRole = new Role();
		newRole.amend(em, role);

		// Do some more Testing
		testCommon(em, session, newRole);
		AuditEntryContext auditContext = new AuditEntryContext("ROLE_CREATE", newRole.getName());
		newRole.persist(em, null, session, auditContext);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteRole(@PathParam("id") int roleID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			session.check(em, Role.MAY_DELETE, "Not allowed to delete Role %d", roleID);

			// Get the Existing Instance
			Role existing = Role.findByID(em, roleID, session.getCompanyID());
			if (existing == null || existing.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Role %d not found", roleID);

			// Delete from Database
			AuditEntryContext auditContext = new AuditEntryContext("ROLE_REMOVE", existing.getName(), existing.getId());
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
	private boolean contains(List<? extends hxc.ecds.protocol.rest.Permission> permissions, hxc.ecds.protocol.rest.Permission permission)
	{
		for (hxc.ecds.protocol.rest.Permission perm : permissions)
		{
			if (perm.getId() == permission.getId())
				return true;
		}
		return false;
	}

	private void testCommon(EntityManager em, Session session, Role role) throws RuleCheckException
	{
		// Test if the caller has all the permissions in this role himself
		List<Permission> permissions = role.getPermissions();
		if (permissions != null)
		{
			for (Permission permission : permissions)
			{
				if (!session.hasPermission(em, permission))
				{
					// May have the Escalation Permission
					if (Role.TYPE_AGENT.equals(role.getType()) && permission.isAgentAllowed() && session.hasPermission(em, Permission.MAY_ESCALATE_AGENT))
						continue;
					else
						throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Cannot grant %s Permission", permission);
				}
			}
		}
	}
}
