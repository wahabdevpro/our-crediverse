package hxc.services.ecds.rest.batch;

import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Promotion;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.AgentUsers;
import hxc.services.ecds.rest.Areas;
import hxc.services.ecds.rest.ICreditDistribution;

public class AreaProcessor extends Processor<Area>
{
	final static Logger logger = LoggerFactory.getLogger(AgentUsers.class);
	
	public static final String[] HEADINGS = new String[] { //
			"id", //
			"name", //
			"type", //
			"parent_name", //
			"parent_type", //
	};

	public AreaProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
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

			case "type":
				return "type";

			case "parent_name":
				return "parent_name";

			case "parent_type":
				return "parent_type";

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
	protected Area instantiate(EntityManager em, State state, Area from)
	{
		Area result = new Area();
		if (from != null)
			result.amend(from);
		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, Area area, String[] rowValues, List<Object> other)
	{
		String parentName = null, parentType = null;
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
					area.setId(state.parseInt(heading, value, 0));
					break;

				case "name":
					area.setName(value);
					break;

				case "type":
					area.setType(value);
					break;
				
				case "parent_name":
					parentName = ((value != null) && value.isEmpty()) ? null : value;
					break;

				case "parent_type":
					parentType = ((value != null) && value.isEmpty()) ? null : value;
					break;
			}
		}

		if (parentName == null && parentType != null)
		{
			state.addIssue(BatchIssue.INVALID_VALUE, "parent_type", null, "parent type without parent name");
			area.setParentArea(null);
			area.setParentAreaID(null);
		}
		else if (parentName != null && parentType == null)
		{
			state.addIssue(BatchIssue.INVALID_VALUE, "parent_name", null, "parent name without parent type");
			area.setParentArea(null);
			area.setParentAreaID(null);
		}
		else if (parentName == null && parentType == null)
		{
			area.setParentArea(null);
			area.setParentAreaID(null);
		}
		else
		{
			Area parent = Area.findByNameAndType(em, state.getCompanyID(), parentName, parentType);
			if (parent == null)
				state.addIssue(BatchIssue.INVALID_VALUE, "parent_name", null, parentName + ":" + parentType + " is not a valid Area");
			else
			{
				area.setParentArea(parent);
				area.setParentAreaID(parent.getId());
			}
		}
	}

	@Override
	protected Area loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Integer columnIdIndex = columnIndexForProperty("id");
		if (columnIdIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIdIndex], 0);
			Area area = Area.findByID(em, id, state.getCompanyID());
			if (area != null)
				return area;
		}

		// Load by Name
		Integer columnNameIndex = columnIndexForProperty("name");
		Integer columnTypeIndex = columnIndexForProperty("type");
		if (columnNameIndex != null && columnTypeIndex != null)
		{
			Area area = Area.findByNameAndType(em, state.getCompanyID(), rowValues[columnNameIndex], rowValues[columnTypeIndex]);
			if (area != null)
				return area;
		}

		return null;
	}

	@Override
	protected Stage addNew(EntityManager em, State state, Area newInstance, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_INSERT, ET_AREA) //

				.setName(newInstance.getName()) //
				.setDescription(newInstance.getType()) //
				.setI1(newInstance.getParentAreaID()) //
		;

		return stage;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, Area existing, Area updated, List<Object> other)
	{
		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_UPDATE, ET_AREA) //

				.setName(updated.getName()) //
				.setDescription(updated.getType()) //
				.setI1(updated.getParentAreaID()) //
				.setEntityID(existing.getId()) //
				.setEntityVersion(existing.getVersion()) //
		;

		return stage;
	}

	@Override
	protected Stage deleteExisting(EntityManager em, State state, Area existing, List<Object> other)
	{
		if (Transaction.referencesArea(em, existing.getId()) || Area.referencesArea(em, existing.getId()) || Promotion.referencesArea(em, existing.getId()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Area is in use");
			return null;
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_DELETE, ET_AREA) //

				.setEntityID(existing.getId()) //
		;

		return stage;
	}

	@Override
	protected void verifyExisting(EntityManager em, State state, Area existing, Area instance, List<Object> other)
	{
		super.verify(state, "name", existing.getName(), instance.getName());
		super.verify(state, "type", existing.getType(), instance.getType());
		super.verify(state, "parent", getParentName(existing), getParentName(instance)); 
	}
	
	private String getParentName(Area parent)
	{
		return parent == null ? "<null>" : parent.getName();
	}

	@Override
	public boolean complete(EntityManager em, State state)
	{
		// Exit if there are issues
		if (state.hasIssues())
			return false;

		// Check for duplicates
		if (!checkForDuplicates(em, state, ET_AREA, "name", "name", "type", "type"))
			return false;

		Areas areas = new Areas(context);
		int batchID = state.getBatch().getId();
		int companyID = state.getCompanyID();
		Session session = state.getSession();

		// Insert
		try
		{
			List<Stage> inserts = Stage.findRecords(em, companyID, batchID, ET_AREA, Stage.ACTION_INSERT, 0, null);

			for (Stage stage : inserts)
			{
				Integer parentID = stage.getI1();
				hxc.ecds.protocol.rest.Area area = new hxc.ecds.protocol.rest.Area() //
						.setCompanyID(companyID) //
						.setName(stage.getName()) //
						.setType(stage.getDescription()) //
						.setParentAreaID(parentID) //
						;
				areas.updateArea(area, em, session);
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
			List<Stage> updates = Stage.findRecords(em, companyID, batchID, ET_AREA, Stage.ACTION_UPDATE, 0, null);

			for (Stage stage : updates)
			{
				Integer parentID = stage.getI1();
				hxc.ecds.protocol.rest.Area area = new hxc.ecds.protocol.rest.Area() //
						.setId(stage.getEntityID()) //
						.setCompanyID(companyID) //
						.setName(stage.getName()) //
						.setType(stage.getDescription()) //
						.setParentAreaID(parentID) //
						;
				areas.updateArea(area, em, session);
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
			List<Stage> deletes = Stage.findRecords(em, companyID, batchID, ET_AREA, Stage.ACTION_DELETE, 0, null);

			for (Stage stage : deletes)
			{
				areas.deleteArea(em, stage.getEntityID(), session);
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
