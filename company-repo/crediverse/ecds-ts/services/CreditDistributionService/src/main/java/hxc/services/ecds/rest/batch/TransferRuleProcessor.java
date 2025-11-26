package hxc.services.ecds.rest.batch;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.EntityManager;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.ecds.protocol.rest.BatchUploadRequest;
import hxc.ecds.protocol.rest.UpdateTransferRulesRequest;
import hxc.ecds.protocol.rest.UpdateTransferRulesResponse;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Batch;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.QualifyingTransaction;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.TransferRules;

public class TransferRuleProcessor extends Processor<TransferRule>
{
	public static final String[] HEADINGS = new String[] { //
			"strict_area", //
			"strict_supplier", //
			"min_amount", //
			"max_amount", //
			//"buyer_trade_bonus", //
			"trade_bonus", //
			"target_bonus_percent", //
			"target_bonus_profile", //
			"start_tod", //
			"end_tod", //
			"id", //
			"dow", //
			"name", //
			"status", //
			"source", //
			"target", //
			"area_name", //
			"area_type", //
			"group", //
			"service_class", //
			"tgt_group", //
			"tgt_service_class", //
	};

	public TransferRuleProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
	{
		super(context, mayInsert, mayUpdate, mayDelete);
	}

	@Override
	protected String getProperty(String heading, boolean lastColumn)
	{
		switch (heading)
		{
			case "strict_area":
				return "strictArea";

			case "strict_supplier":
				return "strictSupplier";

			case "min_amount":
				return "minimumAmount";

			case "max_amount":
				return "maximumAmount";

			case "trade_bonus":
				return "buyerTradeBonusPercentage";

			case "target_bonus_percent":
				return "targetBonusPercentage";

			case "target_bonus_profile":
				return "targetBonusProfile";

			case "start_tod":
				return "startTimeOfDay";

			case "end_tod":
				return "endTimeOfDay";

			case "id":
				return "id";

			case "dow":
				return "daysOfWeek";

			case "name":
				return "name";

			case "status":
				return "state";

			case "source":
				return "sourceTierID";

			case "target":
				return "targetTierID";

			case "area_name":
				return "areaName";

			case "area_type":
				return "areaType";

			case "group":
				return "groupID";

			case "service_class":
				return "serviceClassID";

			case "tgt_group":
				return "targetGroupID";

			case "tgt_service_class":
				return "targetServiceClassID";

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
	protected TransferRule instantiate(EntityManager em, State state, TransferRule from)
	{
		TransferRule result = new TransferRule();
		if (from != null)
			result.amend(from);
		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, TransferRule transferRule, String[] rowValues, List<Object> other)
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
				case "strictArea":
					transferRule.setStrictArea(state.parseBoolean(heading, value));
					break;

				case "strictSupplier":
					transferRule.setStrictSupplier(state.parseBoolean(heading, value));
					break;

				case "minimumAmount":
					transferRule.setMinimumAmount(state.parseBigDecimal(heading, value));
					break;

				case "maximumAmount":
					transferRule.setMaximumAmount(state.parseBigDecimal(heading, value));
					break;

				case "buyerTradeBonusPercentage":
					{
						final BigDecimal hundred = new BigDecimal(100);
						BigDecimal percentage = state.parseBigDecimal(heading, value);
						transferRule.setBuyerTradeBonusPercentage(percentage == null ? null : percentage.divide(hundred));
					}	
					break;

				case "targetBonusPercentage":
					{
						final BigDecimal hundred = new BigDecimal(100);
						BigDecimal percentage = state.parseBigDecimal(heading, value);
						transferRule.setTargetBonusPercentage(percentage == null ? null : percentage.divide(hundred));
					}	
					break;

				case "targetBonusProfile":
					transferRule.setTargetBonusProfile(value);
					break;

				case "startTimeOfDay":
					transferRule.setStartTimeOfDay(state.parseDate(heading, value));
					break;

				case "endTimeOfDay":
					transferRule.setEndTimeOfDay(state.parseDate(heading, value));
					break;

				case "id":
					transferRule.setId(state.parseInt(heading, value, 0));
					break;

				case "daysOfWeek":
					transferRule.setDaysOfWeek(state.parseInteger(heading, value));
					break;

				case "name":
					transferRule.setName(value);
					break;

				case "state":
					transferRule.setState(value);
					break;

				case "sourceTierID":
					if (value == null || value.isEmpty())
					{
						transferRule.setSourceTier(null);
						transferRule.setSourceTierID(0);
					}
					else
					{
						Tier sourceTier = Tier.findByName(em, state.getCompanyID(), value);
						if (sourceTier == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "sourceTierID", null, "Invalid Source Tier");
						else
						{
							transferRule.setSourceTier(sourceTier);
							transferRule.setSourceTierID(sourceTier.getId());
						}
					}
					break;

				case "targetTierID":
					if (value == null || value.isEmpty())
					{
						transferRule.setTargetTier(null);
						transferRule.setTargetTierID(0);
					}
					else
					{
						Tier targetTier = Tier.findByName(em, state.getCompanyID(), value);
						if (targetTier == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "targetTierID", null, "Invalid Target Tier");
						else
						{
							transferRule.setTargetTier(targetTier);
							transferRule.setTargetTierID(targetTier.getId());
						}
					}
					break;

				case "areaName":
					areaName = ((value != null) && value.isEmpty()) ? null : value;
					break;

				case "areaType":
					areaType = ((value != null) && value.isEmpty()) ? null : value;
					break;

				case "groupID":
					if (value == null || value.isEmpty())
					{
						transferRule.setGroup(null);
						transferRule.setGroupID(null);
					}
					else
					{
						Group group = Group.findByName(em, state.getCompanyID(), value);
						if (group == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "groupID", null, "Invalid Group");
						else
						{
							transferRule.setGroup(group);
							transferRule.setGroupID(group.getId());
						}
					}
					break;

				case "targetGroupID":
					if (value == null || value.isEmpty())
					{
						transferRule.setTargetGroup(null);
						transferRule.setTargetGroupID(null);
					}
					else
					{
						Group group = Group.findByName(em, state.getCompanyID(), value);
						if (group == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "targetGroupID", null, "Invalid Target Group");
						else
						{
							transferRule.setTargetGroup(group);
							transferRule.setTargetGroupID(group.getId());
						}
					}
					break;

				case "serviceClassID":
					if (value == null || value.isEmpty())
					{
						transferRule.setServiceClass(null);
						transferRule.setServiceClassID(null);
					}
					else
					{
						ServiceClass serviceClass = ServiceClass.findByName(em, state.getCompanyID(), value);
						if (serviceClass == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "serviceClassID", null, "Invalid Service Class");
						else
						{
							transferRule.setServiceClass(serviceClass);
							transferRule.setServiceClassID(serviceClass.getId());
						}
					}
					break;

				case "targetServiceClassID":
					if (value == null || value.isEmpty())
					{
						transferRule.setTargetServiceClass(null);
						transferRule.setTargetServiceClassID(null);
					}
					else
					{
						ServiceClass serviceClass = ServiceClass.findByName(em, state.getCompanyID(), value);
						if (serviceClass == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "targetServiceClassID", null, "Invalid Target Service Class");
						else
						{
							transferRule.setTargetServiceClass(serviceClass);
							transferRule.setTargetServiceClassID(serviceClass.getId());
						}
					}
					break;

			}
		}

		if (areaName == null && areaType != null)
		{
			state.addIssue(BatchIssue.INVALID_VALUE, "area_type", null, "area type without area name");
			transferRule.setArea(null);
			transferRule.setAreaID(null);
		}
		else if (areaName != null && areaType == null)
		{
			state.addIssue(BatchIssue.INVALID_VALUE, "area_name", null, "area name without area type");
			transferRule.setArea(null);
			transferRule.setAreaID(null);
		}
		else if (areaName == null && areaType == null)
		{
			transferRule.setArea(null);
			transferRule.setAreaID(null);
		}
		else
		{
			Area area = Area.findByNameAndType(em, state.getCompanyID(), areaName, areaType);
			if (area == null)
				state.addIssue(BatchIssue.INVALID_VALUE, "area_name", null, "area " + areaName + ":" + areaType + " not found");
			else
			{
				transferRule.setArea(area);
				transferRule.setAreaID(area.getId());
			}
		}
	}

	@Override
	protected TransferRule loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Integer columnIndex = columnIndexForProperty("id");
		if (columnIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIndex], 0);
			TransferRule transferRule = TransferRule.findByID(em, id, state.getCompanyID());
			if (transferRule != null)
				return transferRule;
		}

		// Load by Name
		columnIndex = columnIndexForProperty("name");
		if (columnIndex != null)
		{
			TransferRule transferRule = TransferRule.findByName(em, state.getCompanyID(), rowValues[columnIndex]);
			if (transferRule != null)
				return transferRule;
		}

		return null;
	}

	@Override
	protected Stage addNew(EntityManager em, State state, TransferRule newInstance, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_INSERT, ET_RULE) //

				.setB1(newInstance.isStrictArea()) //
				.setB2(newInstance.isStrictSupplier()) //
				.setBd1(newInstance.getMinimumAmount()) //
				.setBd2(newInstance.getMaximumAmount()) //
				.setBd3(newInstance.getBuyerTradeBonusPercentage()) //
				.setBd4(newInstance.getTargetBonusPercentage()) //
				.setCode(newInstance.getTargetBonusProfile()) //
				.setD1(newInstance.getStartTimeOfDay()) //
				.setD2(newInstance.getEndTimeOfDay()) //
				.setCompanyID(newInstance.getCompanyID()) //
				.setI1(newInstance.getDaysOfWeek()) //
				.setLastTime(newInstance.getLastTime()) //
				.setLastUserID(newInstance.getLastUserID()) //
				.setName(newInstance.getName()) //
				.setState(newInstance.getState()) //
				.setTierID1(newInstance.getSourceTierID()) //
				.setTierID2(newInstance.getTargetTierID()) //
				.setAreaID(newInstance.getAreaID()) //
				.setGroupID(newInstance.getGroupID()) //
				.setServiceClassID(newInstance.getServiceClassID()) //
				.setI2(newInstance.getTargetGroupID()) //
				.setI3(newInstance.getTargetServiceClassID()) //
		;

		return stage;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, TransferRule existing, TransferRule updated, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_UPDATE, ET_RULE) //

				.setB1(updated.isStrictArea()) //
				.setB2(updated.isStrictSupplier()) //
				.setBd1(updated.getMinimumAmount()) //
				.setBd2(updated.getMaximumAmount()) //
				.setBd3(updated.getBuyerTradeBonusPercentage()) //
				.setBd4(updated.getTargetBonusPercentage()) //
				.setCode(updated.getTargetBonusProfile()) //
				.setD1(updated.getStartTimeOfDay()) //
				.setD2(updated.getEndTimeOfDay()) //
				.setCompanyID(updated.getCompanyID()) //
				.setI1(updated.getDaysOfWeek()) //
				.setLastTime(updated.getLastTime()) //
				.setLastUserID(updated.getLastUserID()) //
				.setName(updated.getName()) //
				.setState(updated.getState()) //
				.setTierID1(updated.getSourceTierID()) //
				.setTierID2(updated.getTargetTierID()) //
				.setAreaID(updated.getAreaID()) //
				.setGroupID(updated.getGroupID()) //
				.setServiceClassID(updated.getServiceClassID()) //
				.setI2(updated.getTargetGroupID()) //
				.setI3(updated.getTargetServiceClassID()) //
				.setEntityID(existing.getId()) //
				.setEntityVersion(existing.getVersion()) //

		;

		return stage;
	}

	@Override
	protected Stage deleteExisting(EntityManager em, State state, TransferRule existing, List<Object> other)
	{
		if (Transaction.referencesTransferRule(em, existing.getId()) || QualifyingTransaction.referencesTransferRule(em, existing.getId()) || Promotion.referencesTransferRule(em, existing.getId()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Transfer Rule in use");
			return null;
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_DELETE, ET_RULE) //

				.setEntityID(existing.getId()) //
		;

		return stage;
	}

	@Override
	protected void verifyExisting(EntityManager em, State state, TransferRule existing, TransferRule instance, List<Object> other)
	{
		verify(state, "strictArea", existing.isStrictArea(), instance.isStrictArea());
		verify(state, "strictSupplier", existing.isStrictSupplier(), instance.isStrictSupplier());
		verify(state, "minimumAmount", existing.getMinimumAmount(), instance.getMinimumAmount());
		verify(state, "maximumAmount", existing.getMaximumAmount(), instance.getMaximumAmount());
		verify(state, "buyerTradeBonusPercentage", existing.getBuyerTradeBonusPercentage(), instance.getBuyerTradeBonusPercentage());
		verify(state, "targetBonusPercentage", existing.getTargetBonusPercentage(), instance.getTargetBonusPercentage());
		verify(state, "targetBonusProfile", existing.getTargetBonusProfile(), instance.getTargetBonusProfile());
		verify(state, "startTimeOfDay", existing.getStartTimeOfDay(), instance.getStartTimeOfDay());
		verify(state, "endTimeOfDay", existing.getEndTimeOfDay(), instance.getEndTimeOfDay());
		verify(state, "daysOfWeek", existing.getDaysOfWeek(), instance.getDaysOfWeek());
		verify(state, "name", existing.getName(), instance.getName());
		verify(state, "state", existing.getState(), instance.getState());
		verify(state, "sourceTierID", existing.getSourceTierID(), instance.getSourceTierID());
		verify(state, "targetTierID", existing.getTargetTierID(), instance.getTargetTierID());
		verify(state, "areaID", existing.getAreaID(), instance.getAreaID());
		verify(state, "groupID", existing.getGroupID(), instance.getGroupID());
		verify(state, "serviceClassID", existing.getServiceClassID(), instance.getServiceClassID());
		verify(state, "targetGroupID", existing.getTargetGroupID(), instance.getTargetGroupID());
		verify(state, "targetServiceClassID", existing.getTargetServiceClassID(), instance.getTargetServiceClassID());
	}

	@Override
	public boolean complete(EntityManager em, State state)
	{
		// Exit if there are issues
		if (state.hasIssues())
			return false;

		// Create an Update Transfer Rules Request
		UpdateTransferRulesRequest updateRequest = new UpdateTransferRulesRequest();
		BatchUploadRequest batchRequest = state.getRequest();
		updateRequest.setSessionID(batchRequest.getSessionID());
		updateRequest.setInboundTransactionID(batchRequest.getInboundTransactionID());
		updateRequest.setInboundSessionID(batchRequest.getInboundSessionID());
		updateRequest.setVersion(batchRequest.getVersion());
		updateRequest.setMode(batchRequest.getMode());

		// Get Rules to be Deleted
		List<Stage> records = Stage.findRecords(em, state.getCompanyID(), state.getBatch().getId(), ET_RULE, Stage.ACTION_DELETE, null, null);
		int deleteCount = records.size();
		int[] transferRulesToRemove = new int[deleteCount];
		int index = 0;
		for (Stage record : records)
		{
			transferRulesToRemove[index++] = record.getEntityID();
		}
		updateRequest.setTransferRulesToRemove(transferRulesToRemove);

		// Get Rules to be Updated/Added
		records = Stage.findRecords(em, state.getCompanyID(), state.getBatch().getId(), ET_RULE, Stage.ACTION_UPDATE, null, null);
		int updateCount = records.size();
		List<Stage> inserts = Stage.findRecords(em, state.getCompanyID(), state.getBatch().getId(), ET_RULE, Stage.ACTION_INSERT, null, null);
		int insertCount = inserts.size();
		records.addAll(inserts);
		TransferRule[] transferRulesToUpsert = new TransferRule[records.size()];
		index = 0;
		for (Stage record : records)
		{
			transferRulesToUpsert[index++] = reCast(record);
		}
		updateRequest.setTransferRulesToUpsert(transferRulesToUpsert);

		// Execute Update Request
		TransferRules transferRules = new TransferRules(context);
		UpdateTransferRulesResponse response = transferRules.updateTransferRules(em, updateRequest);

		// Unpack Errors
		boolean successful = response.wasSuccessful();
		if (!successful)
		{
			state.addIssue(response.getReturnCode(), null, null, response.getAdditionalInformation());
		}

		// Set Stats
		else
		{
			Batch batch = state.getBatch();
			batch.setDeleteCount(deleteCount);
			batch.setUpdateCount(updateCount);
			batch.setInsertCount(insertCount);
		}

		return successful;
	}

	private TransferRule reCast(Stage record)
	{
		TransferRule rule = new TransferRule() //
				.setStrictArea(record.getB1()) //
				.setStrictSupplier(record.getB2()) //
				.setMinimumAmount(record.getBd1()) //
				.setMaximumAmount(record.getBd2()) //
				.setBuyerTradeBonusPercentage(record.getBd3()) //
				.setTargetBonusPercentage(record.getBd4()) //
				.setTargetBonusProfile(record.getCode()) //
				.setStartTimeOfDay(record.getD1()) //
				.setEndTimeOfDay(record.getD2()) //
				.setCompanyID(record.getCompanyID()) //
				.setDaysOfWeek(record.getI1()) //
				.setLastTime(record.getLastTime()) //
				.setLastUserID(record.getLastUserID()) //
				.setName(record.getName()) //
				.setState(record.getState()) //
				.setSourceTierID(record.getTierID1()) //
				.setTargetTierID(record.getTierID2()) //
				.setAreaID(record.getAreaID()) //
				.setGroupID(record.getGroupID()) //
				.setServiceClassID(record.getServiceClassID()) //
				.setTargetGroupID(record.getI2()) //
				.setTargetServiceClassID(record.getI3()); //

		if (record.getAction() == Stage.ACTION_UPDATE)
		{
			rule.setId(record.getEntityID());
			rule.setVersion(record.getEntityVersion());
		}

		return rule;
	}

}
