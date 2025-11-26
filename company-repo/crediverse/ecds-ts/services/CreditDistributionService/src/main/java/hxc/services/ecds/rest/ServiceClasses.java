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
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.QualifyingTransaction;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.rest.batch.ServiceClassProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/service_classes")
public class ServiceClasses
{
	final static Logger logger = LoggerFactory.getLogger(ServiceClasses.class);
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
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.ServiceClass getServiceClass(@PathParam("id") int serviceClassID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			ServiceClass serviceClass = ServiceClass.findByID(em, serviceClassID, session.getCompanyID());
			if (serviceClass == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "ServiceClass %d not found", serviceClassID);
			return serviceClass;
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
	// Get ServiceClasses
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.ServiceClass[] getServiceClass( //
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
				List<ServiceClass> serviceClasses = ServiceClass.findAll(em, params, session.getCompanyID());
				return serviceClasses.toArray(new ServiceClass[serviceClasses.size()]);
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
	public Long getServiceClassesCount( //
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
				return ServiceClass.findCount(em, params, session.getCompanyID());
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
	public String getServiceClassCsv( //
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
			List<ServiceClass> serviceClasses;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				serviceClasses = ServiceClass.findAll(em, params, session.getCompanyID());
			}

			CsvExportProcessor<ServiceClass> processor = new CsvExportProcessor<ServiceClass>(ServiceClassProcessor.HEADINGS, first)
			{
				@Override
				protected void write(ServiceClass record)
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
	public void updateServiceClass(hxc.ecds.protocol.rest.ServiceClass serviceClass, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Determine if this is a new Instance
			boolean isNew = serviceClass.getId() <= 0;
			if (isNew)
			{
				createServiceClass(em, serviceClass, session, params);
				return;
			}

			// Test Permission
			session.check(em, ServiceClass.MAY_UPDATE, "Not allowed to Update ServiceClass %d", serviceClass.getId());

			// Get the Existing Instance
			ServiceClass existing = ServiceClass.findByID(em, serviceClass.getId(), session.getCompanyID());
			if (existing == null || serviceClass.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "ServiceClass %d not found", serviceClass.getId());
			ServiceClass updated = existing;
			existing = new ServiceClass(existing);
			updated.amend(serviceClass);

			// Persist to Database
			AuditEntryContext auditContext = new AuditEntryContext("SERVICE_CLASS_UPDATE", updated.getName(), updated.getId());
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

	private void createServiceClass(EntityManagerEx em, hxc.ecds.protocol.rest.ServiceClass serviceClass, Session session, RestParams params) throws RuleCheckException
	{
		// Test Permission
		session.check(em, ServiceClass.MAY_ADD, "Not allowed to Create ServiceClass");

		// Persist it
		ServiceClass newServiceClass = new ServiceClass();
		newServiceClass.amend(serviceClass);
		AuditEntryContext auditContext = new AuditEntryContext("SERVICE_CLASS_CREATE", newServiceClass.getName());
		newServiceClass.persist(em, null, session, auditContext);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteServiceClass(@PathParam("id") int serviceClassID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Test Permission
			session.check(em, ServiceClass.MAY_DELETE, "Not allowed to Delete ServiceClass %d", serviceClassID);

			// Get the Existing Instance
			ServiceClass existing = ServiceClass.findByID(em, serviceClassID, session.getCompanyID());
			if (existing == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "ServiceClass %d not found", serviceClassID);

			// Test if in use
			if (Transaction.referencesServiceClass(em, serviceClassID) || QualifyingTransaction.referencesServiceClass(em, serviceClassID) || Agent.referencesServiceClass(em, serviceClassID) //
					|| TransferRule.referencesServiceClass(em, serviceClassID) || Promotion.referencesServiceClass(em, serviceClassID))
				throw new RuleCheckException(StatusCode.RESOURCE_IN_USE, null, "Service Class %d is in use", serviceClassID);

			// Remove from Database
			AuditEntryContext auditContext = new AuditEntryContext("SERVICE_CLASS_REMOVE", existing.getName(), existing.getId());
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
