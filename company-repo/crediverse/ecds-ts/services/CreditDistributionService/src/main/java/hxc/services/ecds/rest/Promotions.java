package hxc.services.ecds.rest;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
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
import hxc.services.ecds.model.Bundle;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.rest.batch.PromotionProcessor;
import hxc.services.ecds.rewards.RewardProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/promotions")
public class Promotions
{
	final static Logger logger = LoggerFactory.getLogger(Promotions.class);
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
	public Promotions()
	{
	}

	public Promotions(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Promotion
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Promotion getPromotion(@PathParam("id") int promotionID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			Promotion promotion = Promotion.findByID(em, promotionID, session.getCompanyID());
			if (promotion == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Promotion %d not found", promotionID);
			return promotion;
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
	// Get Promotions
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.Promotion[] getPromotions( //
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
				List<Promotion> promotions = Promotion.findAll(em, params, session.getCompanyID());
				return promotions.toArray(new Promotion[promotions.size()]);
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
	public Long getPromotionsCount( //
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
				return Promotion.findCount(em, params, session.getCompanyID());
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
	public String getPromotionCsv( //
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
			List<Promotion> promotions;
			// TODO Remove when queries are fixed
			try (QueryToken token = context.getQueryToken())
			{
				promotions = Promotion.findAll(em, params, session.getCompanyID());
			}
			final BigDecimal hundred = new BigDecimal(100);

			CsvExportProcessor<Promotion> processor = new CsvExportProcessor<Promotion>(PromotionProcessor.HEADINGS, first)
			{
				@Override
				protected void write(Promotion record)
				{
					put("id", record.getId());
					put("name", record.getName());
					put("status", record.getState());
					put("start_time", record.getStartTime());
					put("end_time", record.getEndTime());
					if (record.getTransferRule() != null)
						put("transfer_rule", record.getTransferRule().getName());
					if (record.getArea() != null) {
						put("area_name", record.getArea().getName());
						put("area_type", record.getArea().getType());
					}	
					if (record.getServiceClass() != null)
						put("service_class", record.getServiceClass().getName());
					if (record.getBundle() != null)
						put("bundle", record.getBundle().getName());
					put("target_amount", record.getTargetAmount());

					switch (record.getTargetPeriod())
					{
						case Promotion.PER_DAY:
							put("target_period", "perDay");
							break;

						case Promotion.PER_WEEK:
							put("target_period", "perWeek");
							break;

						case Promotion.PER_MONTH:
							put("target_period", "perMonth");
							break;

						case Promotion.PER_CALENDAR_DAY:
							put("target_period", "perCalendarDay");
							break;

						case Promotion.PER_CALENDAR_WEEK:
							put("target_period", "perCalendarWeek");
							break;

						case Promotion.PER_CALENDAR_MONTH:
							put("target_period", "perCalendarMonth");
							break;
					}
					put("reward_percentage", record.getRewardPercentage().multiply(hundred));
					put("reward_amount", record.getRewardAmount());
					put("retriggers", record.isRetriggerable());
				}
			};
			return processor.add(promotions);
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
	public void updatePromotion(hxc.ecds.protocol.rest.Promotion promotion, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			updatePromotion(promotion, em, session);

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

	public void updatePromotion(hxc.ecds.protocol.rest.Promotion promotion, EntityManager em, Session session) throws RuleCheckException
	{
		// Determine if this is a new Instance
		boolean isNew = promotion.getId() <= 0;
		if (isNew)
		{
			createPromotion(em, promotion, session);
			return;
		}

		// Test Permission
		session.check(em, Promotion.MAY_UPDATE, "Not allowed to Update Promotion %d", promotion.getId());

		// Get the Existing Instance
		Promotion existing = Promotion.findByID(em, promotion.getId(), session.getCompanyID());
		if (existing == null || promotion.getCompanyID() != session.getCompanyID())
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Promotion %d not found", promotion.getId());
		Promotion updated = existing;
		existing = new Promotion(existing);
		updated.amend(promotion);

		testCommon(em, session, updated);

		// Persist to Database
		AuditEntryContext auditContext = new AuditEntryContext("PROMOTION_UPDATED", updated.getId());
		updated.persist(em, existing, session, auditContext);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	// /////////////////////////////////

	private void createPromotion(EntityManager em, hxc.ecds.protocol.rest.Promotion promotion, Session session) throws RuleCheckException
	{
		// Test Permission
		session.check(em, Promotion.MAY_ADD, "Not allowed to Create Promotion");

		// Persist it
		Promotion newPromotion = new Promotion();
		newPromotion.amend(promotion);
		testCommon(em, session, newPromotion);
		AuditEntryContext auditContext = new AuditEntryContext("PROMOTION_CREATE", newPromotion.getName());
		newPromotion.persist(em, null, session, auditContext);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deletePromotion(@PathParam("id") int promotionID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			deletePromotion(promotionID, em, session);
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

	public void deletePromotion(int promotionID, EntityManager em, Session session) throws RuleCheckException
	{
		// Test Permission
		session.check(em, Promotion.MAY_DELETE, "Not allowed to Delete Promotion %d", promotionID);

		// Get the Existing Instance
		Promotion existing = Promotion.findByID(em, promotionID, session.getCompanyID());
		if (existing == null)
			throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Promotion %d not found", promotionID);
			
		// Test if in use
		if (Transaction.referencesPromotion(em, promotionID))
			throw new RuleCheckException(StatusCode.RESOURCE_IN_USE, null, "Promotion %d is in use", promotionID);
		
		// Remove from Database
		AuditEntryContext auditContext = new AuditEntryContext("PROMOTION_REMOVE", existing.getId());
		existing.remove(em, session, auditContext);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private void testCommon(EntityManager em, Session session, Promotion promotion) throws RuleCheckException
	{
		int companyID = session.getCompanyID();

		// Test Bundle
		Integer bundleID = promotion.getBundleID();
		if (bundleID == null)
		{
			promotion.setBundle(null);
		}
		else
		{
			Bundle bundle = Bundle.findByID(em, bundleID, companyID);
			if (bundle == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Bundle %d not found", bundleID);
			else
				promotion.setBundle(bundle);
		}

		// Test Area
		Integer areaID = promotion.getAreaID();
		if (areaID == null)
		{
			promotion.setArea(null);
		}
		else
		{
			Area area = Area.findByID(em, areaID, companyID);
			if (area == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Area %d not found", areaID);
			else
				promotion.setArea(area);
		}

		// Test Service Class
		Integer serviceClassID = promotion.getServiceClassID();
		if (serviceClassID == null)
		{
			promotion.setServiceClass(null);
		}
		else
		{
			ServiceClass sc = ServiceClass.findByID(em, serviceClassID, companyID);
			if (sc == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "ServiceClass %d not found", serviceClassID);
			else
				promotion.setServiceClass(sc);
		}

		// Test TransferRule
		Integer transferRuleID = promotion.getTransferRuleID();
		if (transferRuleID == null)
		{
			promotion.setTransferRule(null);
		}
		else
		{
			TransferRule tr = TransferRule.findByID(em, transferRuleID, companyID);
			if (tr == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Transfer Rule %d not found", transferRuleID);
			else
				promotion.setTransferRule(tr);
		}

		// Test for Ambiguity
		RestParams params = new RestParams(session.getSessionID());
		List<Promotion> all = Promotion.findAll(em, params, companyID);
		for (Promotion promo : all)
		{
			if (promotion.overlaps(promo))
				throw new RuleCheckException(StatusCode.AMBIGUOUS, null, "Overlaps with Promotion %d", promo.getId());
		}
	}

	@POST
	@Path("/award_now")
	@Produces(MediaType.APPLICATION_JSON)
	public void awardNow(@HeaderParam(RestParams.SID) String sessionID)
	{
		try
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			Boolean testMode = session.get(Session.TEST_MODE);
			if (testMode == null || !testMode)
				return;
			RewardProcessor rewardProcessor = new RewardProcessor(context, session.getCompanyInfo());
			rewardProcessor.tryEvaluateRewards();
			rewardProcessor.sendDeferredSms();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////

}
