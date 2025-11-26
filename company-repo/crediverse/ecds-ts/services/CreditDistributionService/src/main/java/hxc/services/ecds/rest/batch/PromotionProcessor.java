package hxc.services.ecds.rest.batch;

import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Bundle;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.Promotions;

public class PromotionProcessor extends Processor<Promotion>
{
	final static Logger logger = LoggerFactory.getLogger(PromotionProcessor.class);
	
	public static final String[] HEADINGS = new String[] { //
			"id", //
			"name", //
			"status", //
			"start_time", //
			"end_time", //
			"transfer_rule", //
			"area_name", //
			"area_type", //
			"service_class", //
			"bundle", //
			"target_amount", //
			"target_period", //
			"reward_percentage", //
			"reward_amount", //
			"retriggers", //
	};

	public PromotionProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
	{
		super(context, mayInsert, mayUpdate, mayDelete);
	}

	@Override
	protected String getProperty(String heading, boolean lastColumn)
	{
		switch (heading)
		{
			case "id":
				return "id";

			case "name":
				return "name";

			case "status":
				return "state";

			case "start_time":
				return "startTime";

			case "end_time":
				return "endTime";

			case "transfer_rule":
				return "transferRuleID";

			case "area_name":
				return "areaName";

			case "area_type":
				return "areaType";

			case "service_class":
				return "serviceClassID";

			case "bundle":
				return "bundleID";

			case "target_amount":
				return "targetAmount";

			case "target_period":
				return "targetPeriod";

			case "reward_percentage":
				return "rewardPercentage";

			case "reward_amount":
				return "rewardAmount";

			case "retriggers":
				return "retriggerable";

			default:
				return null;
		}

	}

	@Override
	protected String getAuditType()
	{
		return null;
	}

	@Override
	protected Promotion instantiate(EntityManager em, State state, Promotion from)
	{
		Promotion result = new Promotion();
		if (from != null)
			result.amend(from);
		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, Promotion promotion, String[] rowValues, List<Object> other)
	{
		String areaName = null, areaType = null;
		for (int index = 0; index < rowValues.length && index < headings.length; index++)
		{
			String property = propertyMap.get(index);
			if (property == null)
				continue;
			String value = rowValues[index];
			String heading = headings[index];
			switch (property)
			{
				case "id":
					promotion.setId(state.parseInt(heading, value, 0));
					break;

				case "name":
					promotion.setName(value);
					break;

				case "state":
					promotion.setState(value);
					break;

				case "startTime":
					promotion.setStartTime(state.parseDate("startTime", value));
					break;

				case "endTime":
					promotion.setEndTime(state.parseDate("endTime", value));
					break;

				case "transferRuleID":
					if (value == null || value.isEmpty())
					{
						promotion.setTransferRule(null);
						promotion.setTransferRuleID(null);
					}
					else
					{
						TransferRule rule = TransferRule.findByName(em, state.getCompanyID(), value);
						if (rule == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "transferRule", null, "%s is not a valid Transfer Rule", value);
						else
						{
							promotion.setTransferRule(rule);
							promotion.setTransferRuleID(rule.getId());
						}
					}
					break;

				case "areaName":
					areaName = ((value != null) && value.isEmpty()) ? null : value;
					break;

				case "areaType":
					areaType = ((value != null) && value.isEmpty()) ? null : value;
					break;

				case "serviceClassID":
					if (value == null || value.isEmpty())
					{
						promotion.setServiceClassID(null);
						promotion.setServiceClass(null);
					}
					else
					{
						ServiceClass sc = ServiceClass.findByName(em, state.getCompanyID(), value);
						if (sc == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "serviceClassID", null, "%s is not a valid Service Class", value);
						else
						{
							promotion.setServiceClass(sc);
							promotion.setServiceClassID(sc.getId());
						}
					}
					break;

				case "bundleID":
					if (value == null || value.isEmpty())
					{
						promotion.setBundleID(null);
						promotion.setBundle(null);
					}
					else
					{
						Bundle bundle = Bundle.findByName(em, state.getCompanyID(), value);
						if (bundle == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "bundleID", null, "%s is not a valid Bundle", value);
						else
						{
							promotion.setBundle(bundle);
							promotion.setBundleID(bundle.getId());
						}
					}
					break;

				case "targetAmount":
					promotion.setTargetAmount(state.parseBigDecimal("targetAmount", value));
					break;

				case "targetPeriod":
					int targetPeriod = 0;
					switch (value == null ? "" : value.toLowerCase())
					{
						case "perday":
							targetPeriod = Promotion.PER_DAY;
							break;

						case "perweek":
							targetPeriod = Promotion.PER_WEEK;
							break;

						case "permonth":
							targetPeriod = Promotion.PER_MONTH;
							break;

						case "percalendarday":
							targetPeriod = Promotion.PER_CALENDAR_DAY;
							break;

						case "percalendarweek":
							targetPeriod = Promotion.PER_CALENDAR_WEEK;
							break;

						case "percalendarmonth":
							targetPeriod = Promotion.PER_CALENDAR_MONTH;
							break;
					}
					promotion.setTargetPeriod(targetPeriod);
					break;

				case "rewardPercentage":
					promotion.setRewardPercentage(state.parsePercentage("rewardPercentage", value));
					break;

				case "rewardAmount":
					promotion.setRewardAmount(state.parseBigDecimal("rewardAmount", value));
					break;

				case "retriggerable":
					promotion.setRetriggerable(state.parseBoolean("retriggers", value));
					break;
			}
		}

		if (areaName == null && areaType != null)
		{
			state.addIssue(BatchIssue.INVALID_VALUE, "area_type", null, "area type without area name");
			promotion.setArea(null);
			promotion.setAreaID(null);
		}
		else if (areaName != null && areaType == null)
		{
			state.addIssue(BatchIssue.INVALID_VALUE, "area_name", null, "area name without area type");
			promotion.setArea(null);
			promotion.setAreaID(null);
		}
		else if (areaName == null && areaType == null)
		{
			promotion.setArea(null);
			promotion.setAreaID(null);
		}
		else
		{
			Area area = Area.findByNameAndType(em, state.getCompanyID(), areaName, areaType);
			if (area == null)
				state.addIssue(BatchIssue.INVALID_VALUE, "area_name", null, "area " + areaName + ":" + areaType + " not found");
			else
			{
				promotion.setArea(area);
				promotion.setAreaID(area.getId());
			}
		}
	}

	@Override
	protected Promotion loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Integer columnIndex = columnIndexForProperty("id");
		if (columnIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIndex], 0);
			Promotion promotion = Promotion.findByID(em, id, state.getCompanyID());
			if (promotion != null)
				return promotion;
		}

		// Load by Name
		columnIndex = columnIndexForProperty("name");
		if (columnIndex != null)
		{
			Promotion promotion = Promotion.findByName(em, state.getCompanyID(), rowValues[columnIndex]);
			if (promotion != null)
				return promotion;
		}

		return null;
	}

	@Override
	protected Stage addNew(EntityManager em, State state, Promotion newInstance, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_INSERT, ET_PROMOTION) //

				.setName(newInstance.getName()) //
				.setState(newInstance.getState()) //
				.setD1(newInstance.getStartTime()) //
				.setD2(newInstance.getEndTime()) //
				.setI1(newInstance.getTransferRuleID()) //
				.setI2(newInstance.getAreaID()) //
				.setI3(newInstance.getServiceClassID()) //
				.setI4(newInstance.getBundleID()) //
				.setBd1(newInstance.getTargetAmount()) //
				.setI5(newInstance.getTargetPeriod()) //
				.setBd2(newInstance.getRewardPercentage()) //
				.setBd3(newInstance.getRewardAmount()) //
				.setB1(newInstance.isRetriggerable()) //
		;

		return stage;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, Promotion existing, Promotion updated, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_UPDATE, ET_PROMOTION) //
				.setName(updated.getName()) //
				.setState(updated.getState()) //
				.setD1(updated.getStartTime()) //
				.setD2(updated.getEndTime()) //
				.setI1(updated.getTransferRuleID()) //
				.setI2(updated.getAreaID()) //
				.setI3(updated.getServiceClassID()) //
				.setI4(updated.getBundleID()) //
				.setBd1(updated.getTargetAmount()) //
				.setI5(updated.getTargetPeriod()) //
				.setBd2(updated.getRewardPercentage()) //
				.setBd3(updated.getRewardAmount()) //
				.setB1(updated.isRetriggerable()) //

				.setEntityID(existing.getId()) //
				.setEntityVersion(existing.getVersion()) //
		;

		return stage;
	}

	@Override
	protected Stage deleteExisting(EntityManager em, State state, Promotion existing, List<Object> other)
	{
		if (Transaction.referencesPromotion(em, existing.getId()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Promotion is in use");
			return null;
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_DELETE, ET_PROMOTION) //

				.setEntityID(existing.getId()) //
		;

		return stage;
	}

	@Override
	protected void verifyExisting(EntityManager em, State state, Promotion existing, Promotion instance, List<Object> other)
	{
		super.verify(state, "name", existing.getName(), instance.getName());
		super.verify(state, "state", existing.getState(), instance.getState());
		super.verify(state, "startTime", existing.getStartTime(), instance.getStartTime());
		super.verify(state, "endTime", existing.getEndTime(), instance.getEndTime());
		super.verify(state, "transferRule", existing.getTransferRuleID(), instance.getTransferRuleID());
		super.verify(state, "area", existing.getAreaID(), instance.getAreaID());
		super.verify(state, "serviceClass", existing.getServiceClassID(), instance.getServiceClassID());
		super.verify(state, "bundle", existing.getBundleID(), instance.getBundleID());
		super.verify(state, "targetAmount", existing.getTargetAmount(), instance.getTargetAmount());
		super.verify(state, "targetPeriod", existing.getTargetPeriod(), instance.getTargetPeriod());
		super.verify(state, "rewardPercentage", existing.getRewardPercentage(), instance.getRewardPercentage());
		super.verify(state, "rewardAmount", existing.getRewardAmount(), instance.getRewardAmount());
		super.verify(state, "retriggerable", existing.isRetriggerable(), instance.isRetriggerable());
	}

	@Override
	public boolean complete(EntityManager em, State state)
	{
		// Exit if there are issues
		if (state.hasIssues())
			return false;

		// Check for duplicates
		if (!checkForDuplicates(em, state, ET_PROMOTION, "name", "name"))
			return false;

		Promotions promotions = new Promotions(context);
		int batchID = state.getBatch().getId();
		int companyID = state.getCompanyID();
		Session session = state.getSession();

		// Insert
		try
		{
			List<Stage> inserts = Stage.findRecords(em, companyID, batchID, ET_PROMOTION, Stage.ACTION_INSERT, 0, null);

			for (Stage stage : inserts)
			{
				Integer parentID = stage.getI1();
				hxc.ecds.protocol.rest.Promotion promotion = new hxc.ecds.protocol.rest.Promotion() //
						.setCompanyID(companyID) //
						.setName(stage.getName()) //
						.setState(stage.getState()) //
						.setStartTime(stage.getD1()) //
						.setEndTime(stage.getD2()) //
						.setTransferRuleID(stage.getI1()) //
						.setAreaID(stage.getI2()) //
						.setServiceClassID(stage.getI3()) //
						.setBundleID(stage.getI4()) //
						.setTargetAmount(stage.getBd1()) //
						.setTargetPeriod(stage.getI5()) //
						.setRewardPercentage(stage.getBd2()) //
						.setRewardAmount(stage.getBd3()) //
						.setRetriggerable(stage.getB1()) //
				;
				promotions.updatePromotion(promotion, em, session);
			}

			state.getBatch().setInsertCount(inserts.size());

		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_ADD, tr);
			state.addIssue(BatchIssue.CANNOT_ADD, null, null, tr.getMessage());
			return false;
		}

		// Update
		try
		{
			List<Stage> updates = Stage.findRecords(em, companyID, batchID, ET_PROMOTION, Stage.ACTION_UPDATE, 0, null);

			for (Stage stage : updates)
			{
				Integer parentID = stage.getI1();
				hxc.ecds.protocol.rest.Promotion promotion = new hxc.ecds.protocol.rest.Promotion() //
						.setId(stage.getEntityID()) //
						.setCompanyID(companyID) //
						.setName(stage.getName()) //
						.setState(stage.getState()) //
						.setStartTime(stage.getD1()) //
						.setEndTime(stage.getD2()) //
						.setTransferRuleID(stage.getI1()) //
						.setAreaID(stage.getI2()) //
						.setServiceClassID(stage.getI3()) //
						.setBundleID(stage.getI4()) //
						.setTargetAmount(stage.getBd1()) //
						.setTargetPeriod(stage.getI5()) //
						.setRewardPercentage(stage.getBd2()) //
						.setRewardAmount(stage.getBd3()) //
						.setRetriggerable(stage.getB1()) //

				;
				promotions.updatePromotion(promotion, em, session);
			}

			state.getBatch().setUpdateCount(updates.size());
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_UPDATE, tr);
			state.addIssue(BatchIssue.CANNOT_UPDATE, null, null, tr.getMessage());
			return false;
		}

		// Delete
		try
		{
			List<Stage> deletes = Stage.findRecords(em, companyID, batchID, ET_PROMOTION, Stage.ACTION_DELETE, 0, null);

			for (Stage stage : deletes)
			{
				promotions.deletePromotion(stage.getEntityID(), em, session);
			}

			state.getBatch().setDeleteCount(deletes.size());
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_DELETE, tr);
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, tr.getMessage());
			return false;
		}

		return true;
	}

}
