package hxc.services.ecds.rest.batch;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.AuditEntry;
import hxc.ecds.protocol.rest.BatchIssue;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rest.ICreditDistribution;

public class TierProcessor extends Processor<Tier>
{
	final static Logger logger = LoggerFactory.getLogger(TierProcessor.class);
	
	public static final String[] HEADINGS = new String[] { //
			"id", //
			"name", //
			"status", //
			"type", //
			"description", //
			"max_amount", //
			"max_daily_count", //
			"max_daily_amount", //
			"max_monthly_count", //
			"max_monthly_amount", //
			"allow_intratier_transfer", //
			"default_bonus_pct"
			// "service_classes"
	};

	public TierProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
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

			case "type":
				return "type";

			case "description":
				return "description";

			case "max_amount":
				return "maxTransactionAmount";

			case "max_daily_count":
				return "maxDailyCount";

			case "max_daily_amount":
				return "maxDailyAmount";

			case "max_monthly_count":
				return "maxMonthlyCount";

			case "max_monthly_amount":
				return "maxMonthlyAmount";

			case "service_classes":
				return "serviceClasses";
				
			case "allow_intratier_transfer":
				return "allowIntraTierTransfer";

			case "default_bonus_pct":
				return "defaultBonusPct";

			default:
				return null;
		}

	}
	
	@Override
	protected String getAuditType()
	{
		return AuditEntry.TYPE_TIER;
	}

	@Override
	protected Tier instantiate(EntityManager em, State state, Tier from)
	{
		Tier result = new Tier();
		if (from != null)
			result.amend(from);
		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, Tier tier, String[] rowValues, List<Object> other)
	{
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
					tier.setId(state.parseInt(heading, value, 0));
					break;

				case "name":
					tier.setName(value);
					break;

				case "state":
					tier.setState(value);
					break;

				case "type":
					tier.setType(value);
					break;

				case "description":
					tier.setDescription(value);
					break;

				case "maxTransactionAmount":
					tier.setMaxTransactionAmount(state.parseBigDecimal(heading, value));
					break;

				case "maxDailyCount":
					tier.setMaxDailyCount(state.parseInteger(heading, value));
					break;

				case "maxDailyAmount":
					tier.setMaxDailyAmount(state.parseBigDecimal(heading, value));
					break;

				case "maxMonthlyCount":
					tier.setMaxMonthlyCount(state.parseInteger(heading, value));
					break;

				case "maxMonthlyAmount":
					tier.setMaxMonthlyAmount(state.parseBigDecimal(heading, value));
					break;
					
				case "allowIntraTierTransfer":
					tier.setAllowIntraTierTransfer(state.parseBoolean(heading, value));
					break;

				case "defaultBonusPct":
					tier.setBuyerDefaultTradeBonusPercentage(state.parseBigDecimal(heading, value));
					break;

				case "serviceClasses":
					break;
			}
		}

	}

	@Override
	protected Tier loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Integer columnIndex = columnIndexForProperty("id");
		if (columnIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIndex], 0);
			Tier tier = Tier.findByID(em, id, state.getCompanyID());
			if (tier != null)
				return tier;
		}

		// Load by Name
		columnIndex = columnIndexForProperty("name");
		if (columnIndex != null)
		{
			Tier tier = Tier.findByName(em, state.getCompanyID(), rowValues[columnIndex]);
			if (tier != null)
				return tier;
		}

		return null;
	}

	@Override
	protected Stage addNew(EntityManager em, State state, Tier newInstance, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_INSERT, ET_TIER) //

				.setName(newInstance.getName()) //
				.setDescription(newInstance.getDescription()) //
				.setState(newInstance.getState()) //
				.setType(newInstance.getType()) //
				.setBd1(newInstance.getMaxDailyAmount()) //
				.setI1(newInstance.getMaxDailyCount()) //
				.setBd2(newInstance.getMaxMonthlyAmount()) //
				.setI2(newInstance.getMaxMonthlyCount()) //
				.setBd3(newInstance.getMaxTransactionAmount()) //
				.setB1(newInstance.isPermanent()) //
				.setB2(newInstance.isAllowIntraTierTransfer())
				.setBd4(newInstance.getBuyerDefaultTradeBonusPercentage())
		;

		return stage;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, Tier existing, Tier updated, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_UPDATE, ET_TIER) //

				.setName(updated.getName()) //
				.setDescription(updated.getDescription()) //
				.setState(updated.getState()) //
				.setType(updated.getType()) //
				.setBd1(updated.getMaxDailyAmount()) //
				.setI1(updated.getMaxDailyCount()) //
				.setBd2(updated.getMaxMonthlyAmount()) //
				.setI2(updated.getMaxMonthlyCount()) //
				.setBd3(updated.getMaxTransactionAmount()) //
				.setEntityID(existing.getId()) //
				.setEntityVersion(existing.getVersion()) //
				.setB1(updated.isPermanent()) //
				.setB2(updated.isAllowIntraTierTransfer())
				.setBd4(updated.getBuyerDefaultTradeBonusPercentage())
		;

		return stage;
	}

	@Override
	protected Stage deleteExisting(EntityManager em, State state, Tier existing, List<Object> other)
	{
		if (existing.isPermanent())
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Cannot delete Permanent");
			return null;
		}

		if (Transaction.referencesTier(em, existing.getId()) || TransferRule.referencesTier(em, existing.getId()) || Agent.referencesTier(em, existing.getId())
				|| Group.referencesTier(em, existing.getId()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Tier in use");
			return null;
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_DELETE, ET_TIER) //

				.setEntityID(existing.getId()) //
		;

		return stage;
	}

	@Override
	protected void verifyExisting(EntityManager em, State state, Tier existing, Tier instance, List<Object> other)
	{
		super.verify(state, "name", existing.getName(), instance.getName());
		super.verify(state, "description", existing.getDescription(), instance.getDescription());
		super.verify(state, "state", existing.getState(), instance.getState());
		super.verify(state, "type", existing.getType(), instance.getType());
		super.verify(state, "maxTransactionAmount", existing.getMaxTransactionAmount(), instance.getMaxTransactionAmount());
		super.verify(state, "maxDailyCount", existing.getMaxDailyCount(), instance.getMaxDailyCount());
		super.verify(state, "maxDailyAmount", existing.getMaxDailyAmount(), instance.getMaxDailyAmount());
		super.verify(state, "maxMonthlyCount", existing.getMaxMonthlyCount(), instance.getMaxMonthlyCount());
		super.verify(state, "maxMonthlyAmount", existing.getMaxMonthlyAmount(), instance.getMaxMonthlyAmount());
		super.verify(state, "permanent", existing.isPermanent(), existing.isPermanent());
		super.verify(state, "defaultBonusPct", existing.getBuyerDefaultTradeBonusPercentage(), instance.getBuyerDefaultTradeBonusPercentage());
	}

	@Override
	public boolean complete(EntityManager em, State state)
	{
		// Exit if there are issues
		if (state.hasIssues())
			return false;

		// Check for duplicates
		if (!checkForDuplicates(em, state, ET_TIER, "name", "name"))
			return false;

		// Insert
		try
		{
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_INSERT, ET_TIER);
			if (expected > 0)
			{
				String sqlString = "insert et_tier (company_id,description,lm_time,lm_userid,max_daily_amount,max_daily_count, " + //
						"max_monthly_amount,max_monthly_count,max_amount,name,state,type,permanent,version,allow_intratier_transfer,default_bonus_pct) " + //
						"select company_id,description,lm_time,lm_userid,bd1 as max_daily_amount,i1 as max_daily_count, " + //
						"bd2 as max_monthly_amount, i2 as max_monthly_count, bd3 as max_amount,name,state,type,b1,version,b2,bd4 as default_bonus_pct " + //
						"from eb_stage where batch_id = :batch_id and action = :action and table_id = :tableID order by line_no";
				Query insertQuery = em.createNativeQuery(sqlString);
				insertQuery.setParameter("batch_id", state.getBatch().getId());
				insertQuery.setParameter("action", Stage.ACTION_INSERT);
				insertQuery.setParameter("tableID", ET_TIER);
				int count = insertQuery.executeUpdate();
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_ADD, "name", expected, String.format("%d/%d records inserted", count, expected));
					return false;
				}
				state.getBatch().setInsertCount(count);
			}
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
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_UPDATE, ET_TIER);
			if (expected > 0)
			{
				String sqlString = "update  et_tier as c \n" //
						+ "join    eb_stage as s on c.id = s.entity_id and c.version = s.entity_version  \n" //
						+ "set     c.description = s.description \n" //
						+ ",       c.lm_time = s.lm_time \n" //
						+ ",       c.lm_userid = s.lm_userid \n" //
						+ ",       c.max_daily_amount = s.bd1 \n" //
						+ ",       c.max_daily_count = s.i1 \n" //
						+ ",       c.max_monthly_amount = s.bd2 \n" //
						+ ",       c.max_monthly_count = s.i2 \n" //
						+ ",       c.max_amount = s.bd3 \n" //
						+ ",       c.name = s.name \n" //
						+ ",       c.state = s.state \n" //
						+ ",       c.type = s.type \n" //
						+ ",       c.permanent = s.b1 \n" //
						+ ",       c.version = s.entity_version + 1 \n" //
						+ ",       c.allow_intratier_transfer = s.b2 \n" //
						+ ",       c.default_bonus_pct = s.bd4 \n" //
						+ "where   s.action = :action \n" //
						+ "and     s.table_id = :tableID \n" //
						+ "and     s.batch_id = :batchID";
				Query updateQuery = em.createNativeQuery(sqlString);
				updateQuery.setParameter("batchID", state.getBatch().getId());
				updateQuery.setParameter("action", Stage.ACTION_UPDATE);
				updateQuery.setParameter("tableID", ET_TIER);
				int count = updateQuery.executeUpdate();
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_UPDATE, "name", expected, String.format("%d/%d records updated", count, expected));

					return false;
				}
				state.getBatch().setUpdateCount(count);
			}
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
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_DELETE, ET_TIER);
			if (expected > 0)
			{
				int count = Stage.delete(em, "et_tier", state.getBatch().getId(), ET_TIER);
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_DELETE, "id", expected, String.format("%d/%d records deleted", count, expected));
					return false;
				}
				state.getBatch().setDeleteCount(count);
			}
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
