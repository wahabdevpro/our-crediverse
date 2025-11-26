package hxc.services.ecds.rest.batch;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.AuditEntry;
import hxc.ecds.protocol.rest.BatchIssue;
import hxc.services.ecds.model.Cell;
import hxc.services.ecds.model.CellGroup;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.rest.ICreditDistribution;

public class CellGroupProcessor extends Processor<CellGroup>
{
	final static Logger logger = LoggerFactory.getLogger(CellGroupProcessor.class);
	
	public static final String[] HEADINGS = new String[] { //
			"id", "code", "name", };

	public CellGroupProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
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
			
			case "code":
				return "code";

			case "name":
				return "name";

			default:
				return null;
		}

	}
	
	@Override
	protected String getAuditType()
	{
		return AuditEntry.TYPE_CELL_GROUP;
	}

	@Override
	protected CellGroup instantiate(EntityManager em, State state, CellGroup from)
	{
		CellGroup result = new CellGroup();
		if (from != null)
			result.amend(from);
		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, CellGroup sc, String[] rowValues, List<Object> other)
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

				case "code":
					sc.setCode(value);
					break;

				case "name":
					sc.setName(value);
					break;
			}
		}

	}

	@Override
	protected CellGroup loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Integer columnIndex = columnIndexForProperty("id");
		if (columnIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIndex], 0);
			CellGroup sc = CellGroup.findByID(em, id, state.getCompanyID());
			if (sc != null)
				return sc;
		}

		// Load by Code
		columnIndex = columnIndexForProperty("code");
		if (columnIndex != null)
		{
			CellGroup sc = CellGroup.findByCode(em, state.getCompanyID(), rowValues[columnIndex]);
			if (sc != null)
				return sc;
		}

		// Load by Name
		columnIndex = columnIndexForProperty("name");
		if (columnIndex != null)
		{
			CellGroup sc = CellGroup.findByName(em, state.getCompanyID(), rowValues[columnIndex]);
			if (sc != null)
				return sc;
		}

		return null;
	}

	@Override
	protected Stage addNew(EntityManager em, State state, CellGroup newInstance, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_INSERT, EL_CELLGROUP) //

				.setCode(newInstance.getCode()) //
				.setName(newInstance.getName()) //
		;

		return stage;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, CellGroup existing, CellGroup updated, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_UPDATE, EL_CELLGROUP) //

				.setCode(updated.getCode()) //
				.setName(updated.getName()) //
				.setEntityID(existing.getId()) //
				.setEntityVersion(existing.getVersion()) //
		;

		return stage;
	}

	@Override
	protected Stage deleteExisting(EntityManager em, State state, CellGroup existing, List<Object> other)
	{
		if (Cell.referencesCellGroup(em, existing.getId()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "CellGroup in use");
			return null;
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_DELETE, EL_CELLGROUP) //

				.setEntityID(existing.getId()) //
		;

		return stage;
	}

	@Override
	protected void verifyExisting(EntityManager em, State state, CellGroup existing, CellGroup instance, List<Object> other)
	{
		super.verify(state, "name", existing.getName(), instance.getName());
	}

	@Override
	public boolean complete(EntityManager em, State state)
	{
		// Exit if there are issues
		if (state.hasIssues())
			return false;

		// Check for duplicates
		if (!checkForDuplicates(em, state, EL_CELLGROUP, "name", "name"))
			return false;
		
		// Insert
		try
		{
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_INSERT, EL_CELLGROUP);
			if (expected > 0)
			{
				String sqlString = "insert el_cell_group (company_id,lm_time,lm_userid,code,name,version) " + //
						"select company_id,lm_time,lm_userid,code,name,version " + //
						"from eb_stage where batch_id = :batch_id and action = :action and table_id = :tableID order by line_no";
				Query insertQuery = em.createNativeQuery(sqlString);
				insertQuery.setParameter("batch_id", state.getBatch().getId());
				insertQuery.setParameter("action", Stage.ACTION_INSERT);
				insertQuery.setParameter("tableID", EL_CELLGROUP);
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
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_UPDATE, EL_CELLGROUP);
			if (expected > 0)
			{
				String sqlString = "update  el_cell_group as c \n" //
						+ "join    eb_stage as s on c.id = s.entity_id and c.version = s.entity_version  \n" //
						+ "set     c.lm_time = s.lm_time \n" //
						+ ",       c.lm_userid = s.lm_userid \n" //
						+ ",       c.code = s.code \n" //
						+ ",       c.name = s.name \n" //
						+ ",       c.version = s.entity_version + 1 \n" //
						+ "where   s.action = :action \n" //
						+ "and     s.table_id = :tableID \n" //
						+ "and     s.batch_id = :batchID";
				Query updateQuery = em.createNativeQuery(sqlString);
				updateQuery.setParameter("batchID", state.getBatch().getId());
				updateQuery.setParameter("action", Stage.ACTION_UPDATE);
				updateQuery.setParameter("tableID", EL_CELLGROUP);
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
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_DELETE, EL_CELLGROUP);
			if (expected > 0)
			{
				int count = Stage.delete(em, "el_cell_group", state.getBatch().getId(), EL_CELLGROUP);
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
