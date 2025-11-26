package hxc.services.ecds.rest.batch;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.AuditEntry;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.QualifyingTransaction;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.TransferRule;
import hxc.services.ecds.rest.ICreditDistribution;

public class ServiceClassProcessor extends Processor<ServiceClass>
{
	final static Logger logger = LoggerFactory.getLogger(ServiceClassProcessor.class);
	
	public static final String[] HEADINGS = new String[] { //
			"id", "name", "status", "description", "max_amount", "max_daily_count", "max_daily_amount", "max_monthly_count", "max_monthly_amount", };

	public ServiceClassProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
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
		return AuditEntry.TYPE_SCLASS;
	}

	@Override
	protected ServiceClass instantiate(EntityManager em, State state, ServiceClass from)
	{
		ServiceClass result = new ServiceClass();
		if (from != null)
			result.amend(from);
		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, ServiceClass sc, String[] rowValues, List<Object> other)
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
					sc.setId(state.parseInt(heading, value, 0));
					break;

				case "name":
					sc.setName(value);
					break;

				case "state":
					sc.setState(value);
					break;

				case "description":
					sc.setDescription(value);
					break;

				case "maxTransactionAmount":
					sc.setMaxTransactionAmount(state.parseBigDecimal(heading, value));
					break;

				case "maxDailyCount":
					sc.setMaxDailyCount(state.parseInteger(heading, value));
					break;

				case "maxDailyAmount":
					sc.setMaxDailyAmount(state.parseBigDecimal(heading, value));
					break;

				case "maxMonthlyCount":
					sc.setMaxMonthlyCount(state.parseInteger(heading, value));
					break;

				case "maxMonthlyAmount":
					sc.setMaxMonthlyAmount(state.parseBigDecimal(heading, value));
					break;
			}
		}

	}

	@Override
	protected ServiceClass loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Integer columnIndex = columnIndexForProperty("id");
		if (columnIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIndex], 0);
			ServiceClass sc = ServiceClass.findByID(em, id, state.getCompanyID());
			if (sc != null)
				return sc;
		}

		// Load by Name
		columnIndex = columnIndexForProperty("name");
		if (columnIndex != null)
		{
			ServiceClass sc = ServiceClass.findByName(em, state.getCompanyID(), rowValues[columnIndex]);
			if (sc != null)
				return sc;
		}

		return null;
	}

	@Override
	protected Stage addNew(EntityManager em, State state, ServiceClass newInstance, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_INSERT, ET_SCLASS) //

				.setName(newInstance.getName()) //
				.setDescription(newInstance.getDescription()) //
				.setState(newInstance.getState()) //
				.setBd1(newInstance.getMaxDailyAmount()) //
				.setI1(newInstance.getMaxDailyCount()) //
				.setBd2(newInstance.getMaxMonthlyAmount()) //
				.setI2(newInstance.getMaxMonthlyCount()) //
				.setBd3(newInstance.getMaxTransactionAmount()) //
		;
		
		return stage;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, ServiceClass existing, ServiceClass updated, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_UPDATE, ET_SCLASS) //

				.setName(updated.getName()) //
				.setDescription(updated.getDescription()) //
				.setState(updated.getState()) //
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
	protected Stage deleteExisting(EntityManager em, State state, ServiceClass existing, List<Object> other)
	{
		if (TransferRule.referencesServiceClass(em, existing.getId()) || Agent.referencesServiceClass(em, existing.getId()) //
				|| Transaction.referencesServiceClass(em, existing.getId()) || QualifyingTransaction.referencesServiceClass(em, existing.getId()) || Promotion.referencesServiceClass(em, existing.getId()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Service Class in use");
			return null;
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_DELETE, ET_SCLASS) //

				.setEntityID(existing.getId()) //
		;

		return stage;
	}

	@Override
	protected void verifyExisting(EntityManager em, State state, ServiceClass existing, ServiceClass instance, List<Object> other)
	{
		super.verify(state, "name", existing.getName(), instance.getName());
		super.verify(state, "description", existing.getDescription(), instance.getDescription());
		super.verify(state, "state", existing.getState(), instance.getState());
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
		if (!checkForDuplicates(em, state, ET_SCLASS, "name", "name"))
			return false;

		// Insert
		try
		{
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_INSERT, ET_SCLASS);
			if (expected > 0)
			{
				String sqlString = "insert et_sclass (company_id,description,lm_time,lm_userid,max_daily_amount,max_daily_count, " + //
						"max_monthly_amount,max_monthly_count,max_amount,name,state, version) " + //
						"select company_id,description,lm_time,lm_userid,bd1 as max_daily_amount,i1 as max_daily_count, " + //
						"bd2 as max_monthly_amount, i2 as max_monthly_count, bd3 as max_amount,name,state, version " + //
						"from eb_stage where batch_id = :batch_id and action = :action and table_id = :tableID order by line_no";
				Query insertQuery = em.createNativeQuery(sqlString);
				insertQuery.setParameter("batch_id", state.getBatch().getId());
				insertQuery.setParameter("action", Stage.ACTION_INSERT);
				insertQuery.setParameter("tableID", ET_SCLASS);
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
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_UPDATE, ET_SCLASS);
			if (expected > 0)
			{
				String sqlString = "update  et_sclass as c \n" //
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
						+ ",       c.version = s.entity_version + 1 \n" //
						+ "where   s.action = :action \n" //
						+ "and     s.table_id = :tableID \n" //
						+ "and     s.batch_id = :batchID";
				Query updateQuery = em.createNativeQuery(sqlString);
				updateQuery.setParameter("batchID", state.getBatch().getId());
				updateQuery.setParameter("action", Stage.ACTION_UPDATE);
				updateQuery.setParameter("tableID", ET_SCLASS);
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
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_DELETE, ET_SCLASS);
			if (expected > 0)
			{
				int count = Stage.delete(em, "et_sclass", state.getBatch().getId(), ET_SCLASS);
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
