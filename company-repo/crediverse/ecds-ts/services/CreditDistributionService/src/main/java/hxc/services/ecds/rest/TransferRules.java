package hxc.services.ecds.rest;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors; 
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.TransferRuleIssue;
import hxc.ecds.protocol.rest.UpdateTransferRulesRequest;
import hxc.ecds.protocol.rest.UpdateTransferRulesResponse;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.TransfersConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.ICompanyData;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.QualifyingTransaction;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.rest.batch.TransferRuleProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import javax.ws.rs.core.Response.Status;

@Path("/transfer_rules")
public class TransferRules
{
	final static Logger logger = LoggerFactory.getLogger(TransferRules.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static int MAX_TRANSFER_RULES = 100;
	private static final BigDecimal PERCENTAGE_ULP = BigDecimal.ONE.movePointLeft(ICompanyData.FINE_MONEY_SCALE);

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
	public TransferRules()
	{

	}

	public TransferRules(ICreditDistribution context)
	{
		this.context = context;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get TransferRule
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.TransferRule getTransferRule(@PathParam("id") int transferRuleID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			TransferRule transferRule = TransferRule.findByID(em, transferRuleID, session.getCompanyID());
			if (transferRule == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Transfer Rule %d not found", transferRuleID);
			return transferRule;
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
	// Get Transfer Rules
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.TransferRule[] getTransferRules( //
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
				List<TransferRule> transferRules = TransferRule.findAll(em, params, session.getCompanyID());
				return transferRules.toArray(new TransferRule[transferRules.size()]);
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
	public Long getTransferRuleCount( //
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
				return TransferRule.findCount(em, params, session.getCompanyID());
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
	public String getTransferRuleCsv( //
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
			List<TransferRule> serviceClasses;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				serviceClasses = TransferRule.findAll(em, params, session.getCompanyID());
			}
			final BigDecimal hundred = new BigDecimal(100);

			CsvExportProcessor<TransferRule> processor = new CsvExportProcessor<TransferRule>(TransferRuleProcessor.HEADINGS, first)
			{
				@Override
				protected void write(TransferRule record)
				{
					put("id", record.getId());
					put("name", record.getName());
					put("status", record.getState());
					if (record.getSourceTier() != null)
						put("source", record.getSourceTier().getName());
					if (record.getTargetTier() != null)
						put("target", record.getTargetTier().getName());
					put("strict_area", record.isStrictArea());
					put("strict_supplier", record.isStrictSupplier());
					put("min_amount", record.getMinimumAmount());
					put("max_amount", record.getMaximumAmount());
					put("trade_bonus", record.getBuyerTradeBonusPercentage().multiply(hundred));
					if( record.getTargetBonusPercentage() != null )
						put("target_bonus_percent", record.getTargetBonusPercentage().multiply(hundred));
					if( record.getTargetBonusProfile() != null )	
						put("target_bonus_profile", record.getTargetBonusProfile());
					putTime("start_tod", record.getStartTimeOfDay());
					putTime("end_tod", record.getEndTimeOfDay());
					put("dow", record.getDaysOfWeek());
					if (record.getArea() != null) {
						put("area_name", record.getArea().getName());
						put("area_type", record.getArea().getType());
					}	
					if (record.getGroup() != null)
						put("group", record.getGroup().getName());
					if (record.getServiceClass() != null)
						put("service_class", record.getServiceClass().getName());
					if (record.getTargetGroup() != null)
						put("tgt_group", record.getTargetGroup().getName());
					if (record.getTargetServiceClass() != null)
						put("tgt_service_class", record.getTargetServiceClass().getName());
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
	public Response updateTransferRule(hxc.ecds.protocol.rest.TransferRule transferRule, @HeaderParam(RestParams.SID) String sessionID)
	{
		UpdateTransferRulesResponse response;
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);

			// Delegate request
			UpdateTransferRulesRequest request = new UpdateTransferRulesRequest();
			request.setSessionID(params.getSessionID());
			request.setTransferRulesToUpsert(new hxc.ecds.protocol.rest.TransferRule[] { transferRule });
			response = updateTransferRules(em, request);
			TransferRuleIssue[] issues = response.getProblematicRules();
			if (issues != null && issues.length > 0) {
				RuleCheckException ex = toRuleCheckException(issues[0]);
				logger.warn(ex.getMessage(), ex);
				response.setReturnCode(issues[0].getReturnCode());
				return Response.status(Status.NOT_ACCEPTABLE).entity(response).build();
			}
			else {
				return Response.status(Status.OK).build();
			}

		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteTransferRule(@PathParam("id") int transferRuleID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);

			// Delegate request
			UpdateTransferRulesRequest request = new UpdateTransferRulesRequest();
			request.setSessionID(params.getSessionID());
			request.setTransferRulesToRemove(new int[] { transferRuleID });
			UpdateTransferRulesResponse response = updateTransferRules(em, request);
			TransferRuleIssue[] issues = response.getProblematicRules();
			if (issues != null && issues.length > 0)
				throw toRuleCheckException(issues[0]);

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
	// Max Cumulative Bonus Percentage
	//
	// /////////////////////////////////
	@GET
	@Path("max_cumulative_bonus")
	@Produces(MediaType.APPLICATION_JSON)
	public BigDecimal getMaxCumulativeBonusPercentage(@HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(sessionID);
			Tier rootTier = Tier.findRoot(em, session.getCompanyID());
			String filter = String.format("sourceTierID='%d'", rootTier.getId());
			RestParams params = new RestParams(sessionID, 0, 1, "targetTier.downStreamPercentage-", null, filter);
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				List<TransferRule> rules = TransferRule.findAll(em, params, session.getCompanyID());
				if (rules == null || rules.size() == 0)
					return null;

				TransferRule rule = rules.get(0);
				BigDecimal buyerTradeBonusPercentage = rule.getBuyerTradeBonusPercentage();
				BigDecimal b = rule.getTargetTier().getDownStreamPercentage();

				if (buyerTradeBonusPercentage == null || b == null)
					return null;

				return buyerTradeBonusPercentage.multiply(b).add(buyerTradeBonusPercentage).add(b);
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
	// Batch Insert/Update/Delete
	//
	// /////////////////////////////////
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.UpdateTransferRulesResponse updateTransferRules(hxc.ecds.protocol.rest.UpdateTransferRulesRequest request)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			return updateTransferRules(em, request);
		}
	}

	public hxc.ecds.protocol.rest.UpdateTransferRulesResponse updateTransferRules(EntityManager em, hxc.ecds.protocol.rest.UpdateTransferRulesRequest request)
	{
		// Create a Response
		UpdateTransferRulesResponse response = request.createResponse();
		response.setReturnCode(ResponseHeader.RETURN_CODE_SUCCESS);
		State state = new State();
		state.request = request;
		state.response = response;

		try
		{
			// Check Session
			Session session = context.getSession(request.getSessionID());

			// Read all Existing Rules
			RestParams params = new RestParams(session.getSessionID());
			state.addRules(TransferRule.findAll(em, params, session.getCompanyID()));

			// Rules to delete
			int[] ruleIDsToRemove = request.getTransferRulesToRemove();
			if (ruleIDsToRemove != null && ruleIDsToRemove.length > 0)
			{
				session.check(em, TransferRule.MAY_DELETE, "Not allowed to Delete Transfer Rule %d", ruleIDsToRemove[0]);
				for (int ruleID : ruleIDsToRemove)
				{
					TransferRule rule = state.getRule(ruleID);
					if (rule == null)
					{
						state.addIssue(ruleID, null, StatusCode.NOT_FOUND, null, null, String.format("RuleID %d not found", ruleID));
						continue;
					}
					if (Transaction.referencesTransferRule(em, rule.getId()) || QualifyingTransaction.referencesTransferRule(em, rule.getId()) || Promotion.referencesTransferRule(em, rule.getId()))
					{
						state.addIssue(ruleID, null, StatusCode.RESOURCE_IN_USE, null, null, String.format("RuleID %d is in use", ruleID));
						continue;
					}
					state.rulesToRemove.add(rule);
					state.removeRule(rule);
				}
			}

			// Rules to add/update
			boolean addPermissionChecked = false;
			boolean updatePermissionChecked = false;
			hxc.ecds.protocol.rest.TransferRule[] upserts = request.getTransferRulesToUpsert();
			if (upserts == null)
				upserts = new hxc.ecds.protocol.rest.TransferRule[0];
			for (hxc.ecds.protocol.rest.TransferRule newRule : upserts)
			{
				int ruleID = newRule.getId();
				if (ruleID == 0)
				{
					if (!addPermissionChecked)
					{
						session.check(em, TransferRule.MAY_ADD, "Not allowed to Add Transfer Rule %s", newRule.getName());
						addPermissionChecked = true;
					}
					TransferRule rule = new TransferRule();
					amend(em, rule, newRule);
					state.addRule(rule);
					state.rulesToAddOrUpdate.add(rule);
				}
				else
				{
					if (!updatePermissionChecked)
					{
						session.check(em, TransferRule.MAY_UPDATE, "Not allowed to Update Transfer Rule %d", ruleID);
						updatePermissionChecked = true;
					}
					TransferRule rule = state.getRule(ruleID);
					if (rule == null)
					{
						state.addIssue(ruleID, null, StatusCode.NOT_FOUND, null, null, String.format("RuleID %d not found", ruleID));
						continue;
					}
					amend(em, rule, newRule);
					state.rulesToAddOrUpdate.add(rule);
				}
			}

			// Validate Individual Rules
			detectRuleViolations(state);

			// Exit if any errors so far
			if (!response.wasSuccessful())
				return response;

			// Detect Recursion (loops)
			// (and Ambiguous Rules)
			Tier rootTier = Tier.findRoot(em, session.getCompanyID());
			detectRecursion(state, rootTier.getId(), new HashSet<Integer>(), new HashSet<Integer>());

			// Exit if any errors so far
			if (!response.wasSuccessful())
				return response;

			Tier subscriberTier = Tier.findSubscriber(em, session.getCompanyID());
			TransfersConfig transfersConfig = context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, TransfersConfig.class);

			boolean checkTradeBonus = !transfersConfig.getDisregardTradeBonusCalculation();
			detectBonusInconsistency(state, null, subscriberTier, BigDecimal.ZERO, new HashSet<Tier>(), checkTradeBonus);

			if (!response.wasSuccessful()){
				return response;
			}


			try (RequiresTransaction ts = new RequiresTransaction(em))
			{
				// Delete Rules
				for (TransferRule rule : state.rulesToRemove)
				{
					AuditEntryContext auditContext = new AuditEntryContext("TRANSFER_RULE_REMOVE", rule.getName(), rule.getId());
					rule.remove(em, session, auditContext);
				}

				// Upsert Rules
				for (TransferRule rule : state.rulesToAddOrUpdate)
				{
					AuditEntryContext auditContext = new AuditEntryContext("TRANSFER_RULE_UPSERT", rule.getName(), rule.getId());
					rule.persist(em, null, session, auditContext);
				}

				// Update Tiers
				for (Tier tier : state.tiersToUpdate)
				{
					AuditEntryContext auditContext = new AuditEntryContext("TRANSFER_RULE_TIER_UPDATE", tier.getName(), tier.getId());
					tier.persist(em, null, session, auditContext);
				}

				ts.commit();
			}

		}
		catch (RuleCheckException ex)
		{
			logger.warn(ex.getMessage(), ex);
			state.addIssue(0, "", ex.getError(), Status.NOT_ACCEPTABLE, ex.getProperty(), null, ex.getMessage());
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			state.addIssue(0, "", TransactionsConfig.ERR_TECHNICAL_PROBLEM, Status.INTERNAL_SERVER_ERROR, null, null, ex.getMessage());
		}

		return response;
	}

	// Amend an existing rule with new details
	private void amend(EntityManager em, TransferRule transferRule, hxc.ecds.protocol.rest.TransferRule newRule) throws RuleCheckException
	{
		transferRule.amend(newRule);

		// Load Tier Objects
		transferRule.setSourceTier(Tier.findByID(em, transferRule.getSourceTierID(), transferRule.getCompanyID()));
		RuleCheck.notNull("sourceTierID", transferRule.getSourceTier());
		transferRule.setTargetTier(Tier.findByID(em, transferRule.getTargetTierID(), transferRule.getCompanyID()));
		RuleCheck.notNull("targetTierID", transferRule.getTargetTier());

		// Load Group
		if (transferRule.getGroupID() != null)
		{
			transferRule.setGroup(Group.findByID(em, transferRule.getGroupID(), transferRule.getCompanyID()));
			RuleCheck.notNull("groupID", transferRule.getGroup());
		}
		else
		{
			transferRule.setGroup(null);
		}

		// Load Service Class
		if (transferRule.getServiceClassID() != null)
		{
			transferRule.setServiceClass(ServiceClass.findByID(em, transferRule.getServiceClassID(), transferRule.getCompanyID()));
			RuleCheck.notNull("serviceClassID", transferRule.getServiceClass());
		}
		else
		{
			transferRule.setServiceClass(null);
		}

		// Load Target Group
		if (transferRule.getTargetGroupID() != null)
		{
			transferRule.setTargetGroup(Group.findByID(em, transferRule.getTargetGroupID(), transferRule.getCompanyID()));
			RuleCheck.notNull("targetGroupID", transferRule.getTargetGroup());
		}
		else
		{
			transferRule.setTargetGroup(null);
		}

		// Load Target Service Class
		if (transferRule.getTargetServiceClassID() != null)
		{
			transferRule.setTargetServiceClass(ServiceClass.findByID(em, transferRule.getTargetServiceClassID(), transferRule.getCompanyID()));
			RuleCheck.notNull("targetServiceClassID", transferRule.getTargetServiceClass());
		}
		else
		{
			transferRule.setTargetServiceClass(null);
		}

		// Load Area
		if (transferRule.getAreaID() != null)
		{
			transferRule.setArea(Area.findByID(em, transferRule.getAreaID(), transferRule.getCompanyID()));
			RuleCheck.notNull("areaID", transferRule.getArea());
		}
		else
		{
			transferRule.setArea(null);
		}

	}

	// Validate Individual Rules
	private void detectRuleViolations(State state)
	{
		logger.trace("detectRuleViolations: entry ...");
		for (TransferRule rule : state.getRules())
		{
			logger.trace("detectRuleViolations: rule (id = {}, name = {})", rule.getId(), rule.getName());
			List<Violation> violations = rule.validate();

			if (violations != null && !violations.isEmpty())
			{
				for (Violation violation : violations)
				{
					state.addIssue(rule.getId(), rule.getName(), //
							violation.getReturnCode(), Status.NOT_ACCEPTABLE, violation.getProperty(), violation.getCriterium(), violation.getAdditionalInformation());
				}
				continue;
			}

			try
			{
				rule.validate(null);
			}
			catch (RuleCheckException e)
			{
				logger.error(e.getMessage(), e);
				state.addIssue(rule.getId(), rule.getName(), e.getError(), Status.NOT_ACCEPTABLE, e.getProperty(), null, e.getMessage());
			}
		}
	}

	// Detect Recursion (loops)
	private void detectRecursion(State state, int fromTierID, Set<Integer> marked, Set<Integer> onStack)
	{
		marked.add(fromTierID);
		onStack.add(fromTierID);

		List<TransferRule> rules = state.getRulesFrom(fromTierID);
		Map<Integer, TransferRule> newlyVisited = new HashMap<Integer, TransferRule>();
		for (TransferRule rule : rules)
		{
			int toTierID = rule.getTargetTierID();
			if (!newlyVisited.containsKey(toTierID))
				newlyVisited.put(toTierID, rule);
		}

		for (Integer toTierID : newlyVisited.keySet())
		{
			if (!marked.contains(toTierID))
			{
				detectRecursion(state, toTierID, marked, onStack);
			}
			else if (onStack.contains(toTierID))
			{
				TransferRule rule = newlyVisited.get(toTierID);
				state.addIssue(rule.getId(), rule.getName(), StatusCode.RECURSIVE, "targetTierID", null, "Recursive Transfer Rule");
				return;
			}

			// Detect Ambiguous Rules
			detectAmbiguity(state, fromTierID, toTierID);
		}

		onStack.remove(fromTierID);

	}

	// Detect Ambiguous Rules
	private void detectAmbiguity(State state, int fromTierID, Integer toTierID)
	{
		List<TransferRule> rules = state.getRulesBetween(fromTierID, toTierID);
		for (int sourceRuleID = 0; sourceRuleID < rules.size() - 1; sourceRuleID++)
		{
			TransferRule sourceRule = rules.get(sourceRuleID);
			for (int targetRuleID = sourceRuleID + 1; targetRuleID < rules.size(); targetRuleID++)
			{
				TransferRule targetRule = rules.get(targetRuleID);
				if (sourceRule.overlapsWith(targetRule)) {
					String message = "Ambiguous Transfer Rule" + " sourceRule(id:'"+sourceRule.getId()+"', name:'"+sourceRule.getName()+"')"
						+ ", targetRule(id:'"+targetRule.getId()+"', name:'"+targetRule.getName()
						+"'). These rules have the same or overlapping values for all of the following fields: " + "Min Amounts, Max Amount, Day of week, "
						+ "Start Time of Day, End Time of Day, Group, Service Class, Target Group, Target Service Class";
					state.addIssue(targetRule.getId(), targetRule.getName(), StatusCode.AMBIGUOUS, null, null, message);
				}
			}
		}
	}

	// Detect Bonus Inconsistencies
	private void detectBonusInconsistency(State state, TransferRule rule, Tier tier, BigDecimal downStreamPercentage,
										  HashSet<Tier> tiers, boolean checkTradeBonus) {
		// downStreamPercentage = null for ROOT
		boolean isRoot = Tier.TIER_ROOT_NAME.equalsIgnoreCase(tier.getName());
		if (isRoot)
			downStreamPercentage = null;

		// Add Tier to set if it has not been reached before
		BigDecimal tierDownStreamPercentage = tier.getDownStreamPercentage();
		
		BigDecimal roundedTdStreamPercentage = null;
		if ( tierDownStreamPercentage != null ) {
			roundedTdStreamPercentage = tierDownStreamPercentage.setScale(8,RoundingMode.UP);
		}
		
		boolean diffirent = !same(roundedTdStreamPercentage, downStreamPercentage);
		if (!tiers.contains(tier))
		{
			tiers.add(tier);
			if (diffirent)
			{
				tier.setDownStreamPercentage(downStreamPercentage);
				state.tiersToUpdate.add(tier);
			}
		}

		// Else complain if the percentages differ
		else if (diffirent)
		{
			// If this is a Store, don't Complain - Use Maximum Downstream Percentage
			if (Tier.TYPE_STORE.equals(rule.getSourceTier().getType()))
			{
				// Update Maximum Downstream Percentage
				if (tierDownStreamPercentage.compareTo(downStreamPercentage) < 0)
				{
					tierDownStreamPercentage = downStreamPercentage;
					tier.setDownStreamPercentage(downStreamPercentage);
					if (!state.tiersToUpdate.contains(tier))
						state.tiersToUpdate.add(tier);
				}

			}
			else {
				if (checkTradeBonus) {
					state.addIssue(rule == null ? 0 : rule.getId(), rule == null ? "" : rule.getName(), //
							StatusCode.AMBIGUOUS, "tradeBonusPercentage", null, "Bonus structure ambiguous");
					return;
				}
			}
		}

		// Repeat for all rules going to this tier
		List<TransferRule> upstreamRules = state.getRulesTo(tier.getId());
		for (TransferRule upstreamRule : upstreamRules)
		{
			if (!TransferRule.STATE_ACTIVE.equals(upstreamRule.getState()))
				continue;
			
			BigDecimal bonus = upstreamRule.getBuyerTradeBonusPercentage();
			BigDecimal cumulativeBonus = bonus.add(BigDecimal.ONE).multiply(downStreamPercentage).add(bonus);
			BigDecimal cmBonus = cumulativeBonus.setScale(8,RoundingMode.UP);
			BigDecimal ulp = cmBonus.stripTrailingZeros().ulp();
			
			 if (checkTradeBonus) {
			 	if (cmBonus.signum() != 0 && ulp.compareTo(PERCENTAGE_ULP) < 0) {
			 		state.addIssue(upstreamRule.getId(), upstreamRule.getName(), StatusCode.INVALID_VALUE, //
			 				"tradeBonusPercentage", PERCENTAGE_ULP, "Too many decimal digits");
			 	}
			 }
			detectBonusInconsistency(state, upstreamRule, upstreamRule.getSourceTier(), cmBonus, tiers, checkTradeBonus);
		}
	}



	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////

	private boolean same(BigDecimal left, BigDecimal right)
	{
		if (left == null)
			return right == null;
		else if (right == null)
			return false;
		else
			return left.compareTo(right) == 0;
	}

	private RuleCheckException toRuleCheckException(TransferRuleIssue issue)
	{
		return new RuleCheckException(new StatusCode(issue.getReturnCode(), issue.getHttpStatus()), issue.getProperty(), issue.getAdditionalInformation());
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Classes
	//
	// /////////////////////////////////
	private class State
	{
		private UpdateTransferRulesResponse response;
		private UpdateTransferRulesRequest request;
		private List<TransferRule> rules = new ArrayList<TransferRule>();
		private List<TransferRule> rulesToRemove = new ArrayList<TransferRule>();
		private List<TransferRule> rulesToAddOrUpdate = new ArrayList<TransferRule>();
		private List<Tier> tiersToUpdate = new ArrayList<Tier>();

		public void addIssue(Integer id, String name, String returnCode, Status httpStatus, String property, Object criterium, String additionalInformation)
		{
			logger.trace("addIssue(id = {}, name = {}, returnCode = {}, httpStatus = {}, property = {}, criterium = {}, additionalInformation = {})", id, name, returnCode,
					httpStatus, property, criterium, additionalInformation);
			TransferRuleIssue[] issues = response.getProblematicRules();
			if (issues == null)
				issues = new TransferRuleIssue[1];
			else
				issues = Arrays.copyOf(issues, issues.length + 1);
			issues[issues.length - 1] = new TransferRuleIssue(id, name, returnCode, httpStatus, property, criterium, additionalInformation);
			response.setProblematicRules(issues);

			if (response.wasSuccessful())
			{
				response.setReturnCode(returnCode);
			}
		}

		public void addIssue(Integer id, String name, StatusCode statusCode, String property, Object criterium, String additionalInformation)
		{
			addIssue(id, name, statusCode.getName(), statusCode.getStatus(), property, criterium, additionalInformation);
		}

		public void addRules(List<TransferRule> rules)
		{
			this.rules.addAll(rules);
		}

		public TransferRule getRule(int ruleID)
		{
			for (TransferRule rule : this.rules)
			{
				if (ruleID == rule.getId())
					return rule;
			}

			return null;
		}

		public List<TransferRule> getRules()
		{
			return rules;
		}

		public List<TransferRule> getRulesFrom(int fromTierID)
		{
			List<TransferRule> result = new ArrayList<TransferRule>();
			for (TransferRule rule : rules)
			{
				if (rule.getSourceTierID() == fromTierID)
					result.add(rule);
			}

			return result;
		}

		public List<TransferRule> getRulesTo(int toTierID)
		{
			List<TransferRule> result = new ArrayList<TransferRule>();
			for (TransferRule rule : rules)
			{
				if (rule.getTargetTierID() == toTierID)
					result.add(rule);
			}

			return result;
		}

		public List<TransferRule> getRulesBetween(int fromTierID, Integer toTierID)
		{
			List<TransferRule> result = new ArrayList<TransferRule>();
			for (TransferRule rule : rules)
			{
				if (rule.getSourceTierID() == fromTierID && rule.getTargetTierID() == toTierID)
					result.add(rule);
			}

			return result;
		}

		public void addRule(TransferRule rule)
		{
			this.rules.add(rule);

		}

		public void removeRule(TransferRule rule)
		{
			this.rules.remove(rule);
		}

	}

}
