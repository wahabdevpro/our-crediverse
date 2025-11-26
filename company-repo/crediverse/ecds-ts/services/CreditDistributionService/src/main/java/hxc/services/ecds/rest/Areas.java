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
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.batch.AreaProcessor;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/areas")
public class Areas
{
	final static Logger logger = LoggerFactory.getLogger(Areas.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Areas()
	{

	}

	public Areas(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Area
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Area getArea(@PathParam("id") int areaID, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			Area area = Area.findByID(em, areaID, session.getCompanyID());
			if (area == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Area %d not found", areaID);
			return area;
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
	// Get Areas
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Area[] getAreas( //
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
				List<Area> areas = Area.findAll(em, params, session.getCompanyID());
				return areas.toArray(new Area[areas.size()]);
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
	public Long getAreaCount( //
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
				return Area.findCount(em, params, session.getCompanyID());
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
	public void updateArea(hxc.ecds.protocol.rest.Area area, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams paramsx = new RestParams(sessionID);
			Session session = context.getSession(paramsx.getSessionID());

			updateArea(area, em, session);

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

	public void updateArea(hxc.ecds.protocol.rest.Area area, EntityManager em, Session session) throws RuleCheckException
	{
		// Determine if this is a new Instance
		boolean isNew = area.getId() <= 0;
		if (isNew)
		{
			createArea(em, area, session);
			return;
		}

		// Test Permission
		session.check(em, Area.MAY_UPDATE, "Not allowed to Update Area %d", area.getId());

		// Get the Existing Instance
		Area updated = Area.findByID(em, area.getId(), session.getCompanyID());
		Integer previousParentAreaID = updated.getParentAreaID();
		if (updated == null || area.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Area %d not found", area.getId());
		Area existing = updated.copy();
		updated.amend(area);
		Integer newParentAreaID = updated.getParentAreaID();

		// Perform Common Testing
		testCommon(em, updated, session);

		try (RequiresTransaction ts = new RequiresTransaction(em))
		{
			// Remove from Previous Parents
			if (previousParentAreaID != null && !previousParentAreaID.equals(newParentAreaID))
			{
				Area previousParent = Area.findByID(em, previousParentAreaID, session.getCompanyID());
				removeFromParents(em, updated, previousParent, ts);
			}

			// Persist to Database
			AuditEntryContext auditEntryContext = new AuditEntryContext("AREA_UPDATE", updated.getName(), updated.getId());
			updated.persist(em, existing, session, auditEntryContext);

			// Add to new Parents
			if (newParentAreaID != null && !newParentAreaID.equals(previousParentAreaID))
			{
				addToParents(em, updated, updated.getParentArea(), ts);
			}

			ts.commit();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	// /////////////////////////////////

	private void createArea(EntityManager em, hxc.ecds.protocol.rest.Area area, Session session) throws RuleCheckException
	{
		// Test Permission
		session.check(em, Area.MAY_ADD, "Not allowed to Create Area");

		// Persist it
		Area newArea = new Area();
		newArea.amend(area);

		// Perform Common Testing
		testCommon(em, newArea, session);

		try (RequiresTransaction ts = new RequiresTransaction(em))
		{
			// Persist
			AuditEntryContext auditEntryContext = new AuditEntryContext("AREA_CREATED", newArea.getName());
			newArea.persist(em, null, session, auditEntryContext);

			// Cascade Containment
			addToParents(em, newArea, newArea.getParentArea(), ts);

			ts.commit();
		}
	}

	private void testCommon(EntityManager em, Area area, Session session) throws RuleCheckException
	{
		// Load Parent
		if (area.getParentAreaID() != null)
		{
			area.setParentArea(Area.findByID(em, area.getParentAreaID(), session.getCompanyID()));
			RuleCheck.notNull("parentAreaID", area.getParentArea());
		}
		else
		{
			area.setParentArea(null);
		}

		// Test if not own (indirect) owner
		Area parent = area.getParentArea();
		while (parent != null)
		{
			if (parent.getId() == area.getId())
				throw new RuleCheckException(StatusCode.RECURSIVE, "parentID", "Cyclical Area Containment %d", area.getId());
			parent = parent.getParentArea();
		}
	}

	private void addToParents(EntityManager em, Area area, Area parent, RequiresTransaction ts) throws RuleCheckException
	{
		if (area == null || parent == null)
			return;
		addToParents(em, area, parent.getParentArea(), ts);
		if (parent.getId() == area.getId() || parent.getSubAreas().contains(area))
			throw new RuleCheckException(StatusCode.RECURSIVE, "parentID", "Cyclical Area Containment %d", area.getId());
		parent.getSubAreas().add(area);
		em.persist(parent);
	}

	private void removeFromParents(EntityManager em, Area area, Area parent, RequiresTransaction ts) throws RuleCheckException
	{
		if (area == null || parent == null)
			return;
		removeFromParents(em, area, parent.getParentArea(), ts);
		if (parent.getId() == area.getId() || !parent.getSubAreas().contains(area))
			throw new RuleCheckException(StatusCode.RECURSIVE, "parentID", "Cyclical Area Containment %d", area.getId());
		parent.getSubAreas().remove(area);
		em.persist(parent);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteArea(@PathParam("id") int areaID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			deleteArea(em, areaID, session);

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

	public void deleteArea(EntityManager em, int areaID, Session session) throws RuleCheckException
	{
		session.check(em, Area.MAY_DELETE, "Not allowed to delete Area %d", areaID);

		// Test if in use
		if (Transaction.referencesArea(em, areaID) || Area.referencesArea(em, areaID) || Promotion.referencesArea(em, areaID))
			throw new RuleCheckException(StatusCode.RESOURCE_IN_USE, null, "Area %d is in use", areaID);

		// Get the Existing Instance
		Area existing = Area.findByID(em, areaID, session.getCompanyID());
		if (existing == null || existing.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Area %d not found", areaID);

		try (RequiresTransaction ts = new RequiresTransaction(em))
		{
			// Remove from Parents
			removeFromParents(em, existing, existing.getParentArea(), ts);

			// Delete from Database
			AuditEntryContext auditContext = new AuditEntryContext("AREA_REMOVE", existing.getName(), existing.getId());
			existing.remove(em, session, auditContext);

			ts.commit();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// CSV
	//
	// /////////////////////////////////
	@GET
	@Path("/csv")
	@Produces("text/csv")
	public String getAreaCsv( //
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
			List<Area> areas;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				areas = Area.findAll(em, params, session.getCompanyID());
			}

			CsvExportProcessor<Area> processor = new CsvExportProcessor<Area>(AreaProcessor.HEADINGS, first)
			{
				@Override
				protected void write(Area record)
				{
					put("id", record.getId());
					put("name", record.getName());
					put("type", record.getType());
					put("parent_name", record.getParentAreaID() != null ? record.getParentArea().getName() : null);
					put("parent_type", record.getParentAreaID() != null ? record.getParentArea().getType() : null);
				}
			};
			return processor.add(areas);

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
