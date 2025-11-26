package hxc.services.ecds.rest;

import java.util.ArrayList;
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

import hxc.ecds.protocol.rest.ExResult;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Cell;
import hxc.services.ecds.model.CellGroup;
import hxc.services.ecds.model.QualifyingTransaction;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.batch.CellProcessor;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
//TODOimport hxc.services.ecds.rest.batch.CellProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/cells")
public class Cells
{
	final static Logger logger = LoggerFactory.getLogger(Cells.class);
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
	public Cells()
	{

	}

	public Cells(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Cell
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Cell getCell(@PathParam("id") int cellID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			Cell cell = Cell.findByID(em, cellID, session.getCompanyID());
			if (cell == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Cell %d not found", cellID);
			return cell;
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
	// Get Cells
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public ExResult<hxc.ecds.protocol.rest.Cell> getCells( //
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
			List<Cell> cells;
			RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
			Session session = context.getSession(params.getSessionID());
			cells = Cell.findAll(em, params, session.getCompanyID());
			Cell[] cellArray = cells.toArray(new Cell[cells.size()]);
			Long foundRowsL = Cell.findAllCount(em, params, session.getCompanyID());
			Integer foundRows = foundRowsL != null ? foundRowsL.intValue() : null;
			return new ExResult<hxc.ecds.protocol.rest.Cell>(foundRows, cellArray);
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
	public Long getCellsCount( //
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
				return Cell.findCount(em, params, session.getCompanyID());
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
	public String getCellCsv( //
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
			List<Cell> cells = Cell.findAll(em, params, session.getCompanyID());

			CsvExportProcessor<Cell> processor = new CsvExportProcessor<Cell>(CellProcessor.HEADINGS, first)
			{
				@Override
				protected void write(Cell record)
				{
					put("id", record.getId());
					put("mcc", record.getMobileCountryCode());
					put("mnc", record.getMobileNetworkCode());
					put("lac", record.getLocalAreaCode());
					put("cid", record.getCellID());
					put("latitude", record.getLatitude());
					put("longitude", record.getLongitude());

					List<Area> areas = record.getAreas();
					List<String> areaNames = new ArrayList(areas.size());
					List<String> areaTypes = new ArrayList(areas.size());
					for ( Area area : areas )
					{
						areaNames.add(area.getName());
						areaTypes.add(area.getType());
					}
					put("area_name", String.join(",", areaNames));
					put("area_type", String.join(",", areaTypes));

					List<CellGroup> cellGroups = record.getCellGroups();
					List<String> cellGroupNames = new ArrayList(cellGroups.size());
					for ( CellGroup cellGroup : cellGroups )
					{
						cellGroupNames.add(cellGroup.getName());
					}
					put("cellGroup", String.join(",", cellGroupNames));
				}
			};
			return processor.add(cells);
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
	public void updateCell(hxc.ecds.protocol.rest.Cell cell, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			updateCell(em, cell, session);

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

	public Cell updateCell(EntityManager em, hxc.ecds.protocol.rest.Cell cell, Session session) throws RuleCheckException
	{
		// Determine if this is a new Instance
		boolean isNew = cell.getId() <= 0;
		if (isNew)
		{
			return createCell(em, cell, session);
		}

		// Test Permission
		session.check(em, Cell.MAY_UPDATE, "Not allowed to Update Cell %d", cell.getId());

		// Get the Existing Instance
		Cell existing = Cell.findByID(em, cell.getId(), session.getCompanyID());
		if (existing == null || cell.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Cell %d not found", cell.getId());
		Cell updated = existing;
		existing = new Cell(em, existing);
		updated.amend(em, cell);

		// Persist to Database
		AuditEntryContext auditContext = new AuditEntryContext("CELL_UPDATE", updated.getId());
		updated.persist(em, existing, session, auditContext);

		return updated;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	// /////////////////////////////////

	private Cell createCell(EntityManager em, hxc.ecds.protocol.rest.Cell cell, Session session) throws RuleCheckException
	{
		// Test Permission
		session.check(em, Cell.MAY_ADD, "Not allowed to Create Cell");

		// Persist it
		Cell newCell = new Cell();
		newCell.amend(em, cell);
		AuditEntryContext auditContext = new AuditEntryContext("CELL_CREATE", newCell.getCellGlobalIdentity());
		newCell.persist(em, null, session, auditContext);

		return newCell;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteCell(@PathParam("id") int cellID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			deleteCell(em, cellID, session);
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

	public void deleteCell(EntityManager em, int cellID, Session session) throws RuleCheckException
	{
		// Test Permission
		session.check(em, Cell.MAY_DELETE, "Not allowed to Delete Cell %d", cellID);

		// Get the Existing Instance
		Cell existing = Cell.findByID(em, cellID, session.getCompanyID());
		if (existing == null)
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Cell %d not found", cellID);
		
		// Test if in use
		if (Transaction.referencesCell(em, cellID) || QualifyingTransaction.referencesCell(em, cellID))
			throw new RuleCheckException(StatusCode.RESOURCE_IN_USE, null, "Cell %d is in use", cellID);

		// Remove from Database
		AuditEntryContext auditContext = new AuditEntryContext("CELL_REMOVE", existing.getCellGlobalIdentity(), existing.getId());
		existing.remove(em, session, auditContext);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////

}
