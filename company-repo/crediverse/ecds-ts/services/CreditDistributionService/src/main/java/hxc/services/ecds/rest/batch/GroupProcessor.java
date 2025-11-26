package hxc.services.ecds.rest.batch;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.AuditEntry;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rest.AgentUsers;
import hxc.services.ecds.rest.ICreditDistribution;

public class GroupProcessor extends Processor<Group>
{
	final static Logger logger = LoggerFactory.getLogger(AgentUsers.class);
	
	public static final String[] HEADINGS = new String[] { //
			"id", //
			"name", //
			"status", //
			"tier", //
			"description", //
			"max_amount", //
			"max_daily_count", //
			"max_daily_amount", //
			"max_monthly_count", //
			"max_monthly_amount" //
	};

	public GroupProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
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

			case "tier":
				return "tier";

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

			default:
				return null;
		}

	}
	
	@Override
	protected String getAuditType()
	{
		return AuditEntry.TYPE_GROUP;
	}

	@Override
	protected Group instantiate(EntityManager em, State state, Group from)
	{
		Group result = new Group();
		if (from != null)
			result.amend(from);
		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, Group group, String[] rowValues, List<Object> other)
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
					group.setId(state.parseInt(heading, value, 0));
					break;

				case "name":
					group.setName(value);
					break;

				case "state":
					group.setState(value);
					break;

				case "tier":
					if (value == null || value.isEmpty())
						group.setTier(null);
					else
					{
						Tier tier = Tier.findByName(em, state.getCompanyID(), value);
						if (tier == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "tier", null, value + " is not a valid Tier");
						else
						{
							group.setTier(tier);
							group.setTierID(tier.getId());
						}
					}
					break;

				case "description":
					group.setDescription(value);
					break;

				case "maxTransactionAmount":
					group.setMaxTransactionAmount(state.parseBigDecimal(heading, value));
					break;

				case "maxDailyCount":
					group.setMaxDailyCount(state.parseInteger(heading, value));
					break;

				case "maxDailyAmount":
					group.setMaxDailyAmount(state.parseBigDecimal(heading, value));
					break;

				case "maxMonthlyCount":
					group.setMaxMonthlyCount(state.parseInteger(heading, value));
					break;

				case "maxMonthlyAmount":
					group.setMaxMonthlyAmount(state.parseBigDecimal(heading, value));
					break;
			}
		}

	}

	@Override
	protected Group loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Integer columnIndex = columnIndexForProperty("id");
		if (columnIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIndex], 0);
			Group group = Group.findByID(em, id, state.getCompanyID());
			if (group != null)
				return group;
		}

		// Load by Name
		columnIndex = columnIndexForProperty("name");
		if (columnIndex != null)
		{
			Group group = Group.findByName(em, state.getCompanyID(), rowValues[columnIndex]);
			if (group != null)
				return group;
		}

		return null;
	}

	@Override
	protected Stage addNew(EntityManager em, State state, Group newInstance, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_INSERT, ET_GROUP) //

				.setName(newInstance.getName()) //
				.setDescription(newInstance.getDescription()) //
				.setState(newInstance.getState()) //
				.setTierID1(newInstance.getTier().getId()) //
				.setBd1(newInstance.getMaxDailyAmount()) //
				.setI1(newInstance.getMaxDailyCount()) //
				.setBd2(newInstance.getMaxMonthlyAmount()) //
				.setI2(newInstance.getMaxMonthlyCount()) //
				.setBd3(newInstance.getMaxTransactionAmount()) //
		;

		return stage;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, Group existing, Group updated, List<Object> other)
	{
		if (existing.getTierID() != updated.getTierID())
		{
			if (TransferRule.referencesGroup(em, existing.getId()) || Agent.referencesGroup(em, existing.getId()))
			{
				state.addIssue(BatchIssue.CANNOT_UPDATE, null, null, "Cannot change Tier - Group is in use");
				return null;
			}
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_UPDATE, ET_GROUP) //

				.setName(updated.getName()) //
				.setDescription(updated.getDescription()) //
				.setState(updated.getState()) //
				.setTierID1(updated.getTier().getId()) //
				.setBd1(updated.getMaxDailyAmount()) //
				.setI1(updated.getMaxDailyCount()) //
				.setBd2(updated.getMaxMonthlyAmount()) //
				.setI2(updated.getMaxMonthlyCount()) //
				.setBd3(updated.getMaxTransactionAmount()) //
				.setEntityID(existing.getId()) //
				.setEntityVersion(existing.getVersion()) //
		;

		return stage;
	}

	@Override
	protected Stage deleteExisting(EntityManager em, State state, Group existing, List<Object> other)
	{
		if (TransferRule.referencesGroup(em, existing.getId()) || Agent.referencesGroup(em, existing.getId()) //
				|| Transaction.referencesGroup(em, existing.getId()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Group is in use");
			return null;
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_DELETE, ET_GROUP) //

				.setEntityID(existing.getId()) //
		;

		return stage;
	}

	@Override
	protected void verifyExisting(EntityManager em, State state, Group existing, Group instance, List<Object> other)
	{
		super.verify(state, "name", existing.getName(), instance.getName());
		super.verify(state, "description", existing.getDescription(), instance.getDescription());
		super.verify(state, "state", existing.getState(), instance.getState());
		super.verify(state, "tier", existing.getTier().getName(), instance.getTier().getName());
		super.verify(state, "maxTransactionAmount", existing.getMaxTransactionAmount(), instance.getMaxTransactionAmount());
		super.verify(state, "maxDailyCount", existing.getMaxDailyCount(), instance.getMaxDailyCount());
		super.verify(state, "maxDailyAmount", existing.getMaxDailyAmount(), instance.getMaxDailyAmount());
		super.verify(state, "maxMonthlyCount", existing.getMaxMonthlyCount(), instance.getMaxMonthlyCount());
		super.verify(state, "maxMonthlyAmount", existing.getMaxMonthlyAmount(), instance.getMaxMonthlyAmount());
	}

	@Override
	public boolean complete(EntityManager em, State state)
	{
		// Exit if there are issues
		if (state.hasIssues())
			return false;

		// Check for duplicates
		if (!checkForDuplicates(em, state, ET_GROUP, "name", "name"))
			return false;

		// Insert
		try
		{
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_INSERT, ET_GROUP);
			if (expected > 0)
			{
				String sqlString = "insert et_group (company_id,description,lm_time,lm_userid,max_daily_amount,max_daily_count, " + //
						"max_monthly_amount,max_monthly_count,max_amount,name,state,tier_id,version) " + //
						"select company_id,description,lm_time,lm_userid,bd1 as max_daily_amount,i1 as max_daily_count, " + //
						"bd2 as max_monthly_amount, i2 as max_monthly_count, bd3 as max_amount,name,state,tier_id1,version " + //
						"from eb_stage where batch_id = :batch_id and action = :action and table_id = :tableID order by line_no";
				Query insertQuery = em.createNativeQuery(sqlString);
				insertQuery.setParameter("batch_id", state.getBatch().getId());
				insertQuery.setParameter("action", Stage.ACTION_INSERT);
				insertQuery.setParameter("tableID", ET_GROUP);
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
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_UPDATE, ET_GROUP);
			if (expected > 0)
			{
				String sqlString = "update  et_group as c \n" //
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
						+ ",       c.tier_id = s.tier_id1 \n" //
						+ ",       c.version = s.entity_version + 1 \n" //
						+ "where   s.action = :action \n" //
						+ "and     s.table_id = :tableID \n" //
						+ "and     s.batch_id = :batchID";
				Query updateQuery = em.createNativeQuery(sqlString);
				updateQuery.setParameter("batchID", state.getBatch().getId());
				updateQuery.setParameter("action", Stage.ACTION_UPDATE);
				updateQuery.setParameter("tableID", ET_GROUP);
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
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_DELETE, ET_GROUP);
			if (expected > 0)
			{
				int count = Stage.delete(em, "et_group", state.getBatch().getId(), ET_GROUP);
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
