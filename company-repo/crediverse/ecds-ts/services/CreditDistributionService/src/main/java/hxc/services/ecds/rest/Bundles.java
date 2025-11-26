package hxc.services.ecds.rest;

import java.math.BigDecimal;
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

import hxc.connectors.bundles.IBundleInfo;
import hxc.connectors.bundles.IBundleProvider;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Bundle;
import hxc.services.ecds.model.QualifyingTransaction;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/bundles")
public class Bundles
{
	final static Logger logger = LoggerFactory.getLogger(Bundles.class);
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

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public Bundles()
	{
	}

	public Bundles(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Bundle
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Bundle getBundle(@PathParam("id") int bundleID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			Bundle bundle = Bundle.findByID(em, bundleID, session.getCompanyID());
			if (bundle == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Bundle %d not found", bundleID);
			return bundle;
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
	// Get Bundles
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Bundle[] getBundles( //
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
				List<Bundle> bundles = Bundle.findAll(em, params, session.getCompanyID());
				for (Bundle bundle : bundles)
				{
					int size = bundle.getLanguages().size();
				}
				return bundles.toArray(new Bundle[bundles.size()]);
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
	public Long getBundlesCount( //
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
				return Bundle.findCount(em, params, session.getCompanyID());
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

	@Path("/info")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.BundleInfo[] getBundleInfo( //
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
			RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
			Session session = context.getSession(params.getSessionID());
			IBundleInfo[] bundleInfo = context.getBundleProvider().getBundleInfo(session.getCompanyID());
			if (bundleInfo == null)
				bundleInfo = new IBundleInfo[0];
			hxc.ecds.protocol.rest.BundleInfo[] results = new hxc.ecds.protocol.rest.BundleInfo[bundleInfo.length];
			int index = 0;
			for (IBundleInfo info : bundleInfo)
			{
				results[index++] = new hxc.ecds.protocol.rest.BundleInfo() //
						.setDescription(info.getDescription()) //
						.setName(info.getName()) //
						.setTag(info.getTag()) //
						.setType(info.getType());
			}

			return results;

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

	@Path("/eligible/{msisdn}/{tag}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public IBundleProvider.StatusCode getBundleInfo( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@PathParam("msisdn") String msisdn, //
			@PathParam("tag") String tag, //
			@PathParam("subscriber_imsi") String subscriberIMSI //
	)
	{
		try
		{
			Session session = context.getSession(sessionID);
			IBundleProvider.StatusCode status = context.getBundleProvider().isEligible(msisdn, tag, subscriberIMSI);
			return status;
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

	@Path("/provision/{msisdn}/{tag}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public IBundleProvider.StatusCode provision( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@PathParam("msisdn") String msisdn, //
			@PathParam("tag") String tag, //
			@PathParam("subscriber_imsi") String subscriberIMSI //
	)
	{
		try
		{
			Session session = context.getSession(sessionID);
			IBundleProvider.StatusCode status = context.getBundleProvider().provision(msisdn, tag, "0000000000", "1234567890", subscriberIMSI, BigDecimal.ZERO, null, null, null, null);
			return status;
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
	public void updateBundle(hxc.ecds.protocol.rest.Bundle bundle, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			updateBundle(bundle, em, session);

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

	public void updateBundle(hxc.ecds.protocol.rest.Bundle bundle, EntityManager em, Session session) throws RuleCheckException
	{
		// Determine if this is a new Instance
		boolean isNew = bundle.getId() <= 0;
		if (isNew)
		{
			createBundle(em, bundle, session);
			return;
		}

		// Test Permission
		session.check(em, Bundle.MAY_UPDATE, "Not allowed to Update Bundle %d", bundle.getId());

		// Get the Existing Instance
		Bundle existing = Bundle.findByID(em, bundle.getId(), session.getCompanyID());
		if (existing == null || bundle.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Bundle %d not found", bundle.getId());
		Bundle updated = existing;
		existing = existing.copy(em);
		updated.amend(em, bundle);

		testCommon(em, session, updated);

		// Persist to Database
		try (RequiresTransaction ts = new RequiresTransaction(em))
		{
			// Add new Languages
			List<hxc.services.ecds.model.BundleLanguage> languages = updated.getLanguages();
			for (hxc.services.ecds.model.BundleLanguage language : languages)
			{
				language.setBundleID(updated.getId());
				em.persist(language);
			}

			// Remove Old Languages
			List<hxc.services.ecds.model.BundleLanguage> previous = existing.getLanguages();
			for (hxc.services.ecds.model.BundleLanguage prev : previous)
			{
				if (!languages.contains(prev))
					em.remove(prev);
			}

			// Persist
			AuditEntryContext auditContext = new AuditEntryContext("BUNDLE_UPDATE", bundle.getId());
			updated.persist(em, existing, session, auditContext);
			ts.commit();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	// /////////////////////////////////

	private void createBundle(EntityManager em, hxc.ecds.protocol.rest.Bundle bundle, Session session) throws RuleCheckException
	{
		// Test Permission
		session.check(em, Bundle.MAY_ADD, "Not allowed to Create Bundle");

		// Persist it
		try (RequiresTransaction ts = new RequiresTransaction(em))
		{
			Bundle newBundle = new Bundle();
			newBundle.amend(em, bundle);
			testCommon(em, session, newBundle);
			AuditEntryContext auditContext = new AuditEntryContext("BUNDLE_CREATE", newBundle.getName());
			newBundle.persist(em, null, session, auditContext);

			List<hxc.services.ecds.model.BundleLanguage> languages = newBundle.getLanguages();
			if (languages != null)
			{
				for (hxc.services.ecds.model.BundleLanguage language : languages)
				{
					language.setBundleID(newBundle.getId());
					em.persist(language);
				}
			}
			ts.commit();
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteBundle(@PathParam("id") int bundleID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			deleteBundle(bundleID, em, session);
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

	public void deleteBundle(int bundleID, EntityManager em, Session session) throws RuleCheckException
	{
		// Test Permission
		session.check(em, Bundle.MAY_DELETE, "Not allowed to Delete Bundle %d", bundleID);

		// Get the Existing Instance
		Bundle existing = Bundle.findByID(em, bundleID, session.getCompanyID());
		if (existing == null)
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Bundle %d not found", bundleID);

		// Test if in use
		if (Transaction.referencesBundle(em, bundleID) || QualifyingTransaction.referencesBundle(em, bundleID))
			throw new RuleCheckException(StatusCode.RESOURCE_IN_USE, null, "Bundle %d is in use", bundleID);

		// Remove from Database
		AuditEntryContext auditContext = new AuditEntryContext("BUNDLE_REMOVE", existing.getName(), existing.getId());
		existing.remove(em, session, auditContext);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private void testCommon(EntityManager em, Session session, Bundle bundle) throws RuleCheckException
	{
		int companyID = session.getCompanyID();

		// Test if Tag is valid
		IBundleProvider provider = context.getBundleProvider();
		IBundleInfo[] bundleInfo = provider.getBundleInfo(companyID);
		if (bundleInfo == null)
			bundleInfo = new IBundleInfo[0];
		String tag = bundle.getTag();
		if (tag == null)
			tag = "?";
		for (IBundleInfo info : bundleInfo)
		{
			if (tag.equalsIgnoreCase(info.getTag()))
				return;
		}

		throw new RuleCheckException(StatusCode.INVALID_VALUE, "tag", "Tag %s not found", tag);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////

}
