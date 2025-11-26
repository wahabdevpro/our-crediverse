package hxc.services.ecds.rest.batch;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Cell;
import hxc.services.ecds.model.CellGroup;
import hxc.services.ecds.model.QualifyingTransaction;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.rest.AgentUsers;
import hxc.services.ecds.rest.Cells;
import hxc.services.ecds.rest.ICreditDistribution;

public class CellProcessor extends Processor<Cell>
{
	final static Logger logger = LoggerFactory.getLogger(AgentUsers.class);
	
	private static final double MAX_DELTA = 0.1 / 60.0 / 60.0;

	public static final String[] HEADINGS = new String[] { //
			"id", //
			"mcc", //
			"mnc", //
			"lac", //
			"cid", //
			"latitude", //
			"longitude", //
			"area_name", //
			"area_type", //
			"cellgroup", //
	};

	public CellProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
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

			case "mcc":
				return "mobileCountryCode";

			case "mnc":
				return "mobileNetworkCode";

			case "lac":
				return "localAreaCode";

			case "cid":
				return "cellID";

			case "latitude":
				return "latitude";

			case "longitude":
				return "longitude";

			case "area_name":
				return "areaName";

			case "area_type":
				return "areaType";

			case "cellgroup":
				return "cellGroup";

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
	protected Cell instantiate(EntityManager em, State state, Cell from)
	{
		Cell result = new Cell();
		result.setMobileCountryCode(Integer.MAX_VALUE);
		if (from != null)
			result.amend(em, from);
		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, Cell cell, String[] rowValues, List<Object> other)
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
					cell.setId(state.parseInt(heading, value, 0));
					break;

				case "mobileCountryCode":
					cell.setMobileCountryCode(state.parseInt(heading, value, 0));
					break;

				case "mobileNetworkCode":
					cell.setMobileNetworkCode(state.parseInt(heading, value, 0));
					break;

				case "localAreaCode":
					cell.setLocalAreaCode(state.parseInt(heading, value, 0));
					break;

				case "cellID":
					cell.setCellID(state.parseInt(heading, value, 0));
					break;

				case "latitude":
					cell.setLatitude(state.parseDouble(heading, value));
					break;

				case "longitude":
					cell.setLongitude(state.parseDouble(heading, value));
					break;

				case "areaName":
					areaName = ((value != null) && value.isEmpty()) ? null : value;
					break;

				case "areaType":
					areaType = ((value != null) && value.isEmpty()) ? null : value;
					break;

				case "cellGroup":
					if (value == null || value.isEmpty())
						other.add(null);
					else
					{
						List<CellGroup> groups = new ArrayList<CellGroup>();
						String[] cellGroupNames = value.split(",");
						for (String name : cellGroupNames)
						{
							if (name == null || name.isEmpty())
								other.add(null);
							else
							{
								CellGroup cellGroup = CellGroup.findByName(em, state.getCompanyID(), name);
								if (cellGroup == null)
									state.addIssue(BatchIssue.INVALID_VALUE, "cellgroup", null, name + " is not a valid CellGroup");
								else
								{
									other.add(cellGroup.getId());
									groups.add(cellGroup);
								}
							}
						}	
						cell.setCellGroups(groups);
					}	
					break;
			}
		}

		if (areaName == null && areaType != null)
		{
			state.addIssue(BatchIssue.INVALID_VALUE, "area_type", null, "area type without area name");
		}
		else if (areaName != null && areaType == null)
		{
			state.addIssue(BatchIssue.INVALID_VALUE, "area_name", null, "area name without area type");
		}
		else if (areaName == null && areaType == null)
		{
			other.add(null);
		}
		else
		{
			List<Area> areas = new ArrayList<Area>();
			String[] areaNames = areaName.split(",");
			String[] areaTypes = areaType.split(",");
			if (areaNames.length != areaTypes.length)
			{
				state.addIssue(BatchIssue.INVALID_VALUE, "area_name", null, 
					"different number of area name (" + areaNames.length + ") / area type (" + areaTypes.length + ") found");
			}
			else
			{
				for (int i = 0; i < areaNames.length; ++i)
				{
					Area area = Area.findByNameAndType(em, state.getCompanyID(), areaNames[i], areaTypes[i]);
					if (area == null)
						state.addIssue(BatchIssue.INVALID_VALUE, "area_name", null, "area (" + i + ") " + areaNames[i] + ":" + areaTypes[i] + " not found");
					else
					{
						other.add(area.getId());
						areas.add(area);
					}
				}
				cell.setAreas(areas);
			}	
		}
	}

	@Override
	protected Cell loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Integer columnIndex = columnIndexForProperty("id");
		if (columnIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIndex], 0);
			Cell cell = Cell.findByID(em, id, state.getCompanyID());
			if (cell != null)
				return cell;
		}

		// Load by Attributes
		int mcc = parseInt("mobileCountryCode", rowValues, -1);
		int mnc = parseInt("mobileNetworkCode", rowValues, -1);
		int lac = parseInt("localAreaCode", rowValues, -1);
		int cid = parseInt("cellID", rowValues, -1);
		Cell cell = Cell.find(em, mcc, mnc, lac, cid, state.getCompanyID());
		if (cell != null)
			return cell;

		return null;
	}

	private int parseInt(String property, String[] rowValues, int defaultValue)
	{
		Integer columnIndex = columnIndexForProperty(property);
		if (columnIndex == null)
			return defaultValue;

		try
		{
			return Integer.parseInt(rowValues[columnIndex], 10);
		}
		catch (NumberFormatException ex)
		{
			logger.error("CellProcessor.parseInt failed", ex);
			return defaultValue;
		}
	}

	@Override
	protected Stage addNew(EntityManager em, State state, Cell newInstance, List<Object> other)
	{
		// Load by Attributes
		int mcc = newInstance.getMobileCountryCode();
		int mnc = newInstance.getMobileNetworkCode();
		int lac = newInstance.getLocalAreaCode();
		int cid = newInstance.getCellID();
		Stage stage = Stage.findCell(em, mcc, mnc, lac, cid, state.getBatch().getId(), ET_CELL, Stage.ACTION_INSERT);
		if (stage != null)
		{
			state.addIssue(BatchIssue.ALREADY_EXISTS, null, null, "Cannot add because it already exists");
			return null;
		}

		List<String> areaIds = new ArrayList();
		List<String> cellGroupIds = new ArrayList();

		for (Area area : newInstance.getAreas())
		{
			areaIds.add(String.valueOf(area.getId()));
		}
		for (CellGroup cellGroup : newInstance.getCellGroups())
		{
			cellGroupIds.add(String.valueOf(cellGroup.getId()));
		}

		// Add Staging Entry
		stage = state.getStage(Stage.ACTION_INSERT, ET_CELL) //
				.setI1(newInstance.getMobileCountryCode()) //
				.setI2(newInstance.getMobileNetworkCode()) //
				.setI3(newInstance.getLocalAreaCode()) //
				.setI4(newInstance.getCellID()) //
				.setR1(newInstance.getLatitude()) //
				.setR2(newInstance.getLongitude()) //
				.setAreaIds(String.join(",", areaIds)) //
				.setCellGroupIds(String.join(",", cellGroupIds)) //
		;

		if (other.size() > 0)
			stage.setI5((Integer) other.get(0));

		return stage;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, Cell existing, Cell updated, List<Object> other)
	{
		List<String> areaIds = new ArrayList();
		List<String> cellGroupIds = new ArrayList();

		for (Area area : updated.getAreas())
		{
			areaIds.add(String.valueOf(area.getId()));
		}
		for (CellGroup cellGroup : updated.getCellGroups())
		{
			cellGroupIds.add(String.valueOf(cellGroup.getId()));
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_UPDATE, ET_CELL) //
				.setI1(updated.getMobileCountryCode()) //
				.setI2(updated.getMobileNetworkCode()) //
				.setI3(updated.getLocalAreaCode()) //
				.setI4(updated.getCellID()) //
				.setR1(updated.getLatitude()) //
				.setR2(updated.getLongitude()) //
				.setEntityID(existing.getId()) //
				.setEntityVersion(existing.getVersion()) //
				.setAreaIds(String.join(",", areaIds)) //
				.setCellGroupIds(String.join(",", cellGroupIds)) //
		;

		if (other.size() > 0)
			stage.setI5((Integer) other.get(0));

		return stage;
	}

	@Override
	protected Stage deleteExisting(EntityManager em, State state, Cell existing, List<Object> other)
	{
		if (Transaction.referencesCell(em, existing.getId()) || QualifyingTransaction.referencesCell(em, existing.getId()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Cell is in use");
			return null;
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_DELETE, ET_CELL) //

				.setEntityID(existing.getId()) //
		;

		return stage;
	}

	@Override
	protected void verifyExisting(EntityManager em, State state, Cell existing, Cell instance, List<Object> other)
	{
		super.verify(state, "mobileCountryCode", existing.getMobileCountryCode(), instance.getMobileCountryCode());
		super.verify(state, "mobileNetworkCode", existing.getMobileNetworkCode(), instance.getMobileNetworkCode());
		super.verify(state, "localAreaCode", existing.getLocalAreaCode(), instance.getLocalAreaCode());
		super.verify(state, "cellID", existing.getCellID(), instance.getCellID());
		super.verify(state, "latitude", existing.getLatitude(), instance.getLatitude(), MAX_DELTA);
		super.verify(state, "longitude(", existing.getLongitude(), instance.getLongitude(), MAX_DELTA);
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public boolean complete(EntityManager em, State state)
	{
		// Exit if there are issues
		if (state.hasIssues())
			return false;

		Cells cells = new Cells(context);
		int batchID = state.getBatch().getId();
		int companyID = state.getCompanyID();
		Session session = state.getSession();

		// Insert
		try
		{
			List<Stage> inserts = Stage.findRecords(em, companyID, batchID, ET_CELL, Stage.ACTION_INSERT, 0, null);

			for (Stage stage : inserts)
			{
				hxc.ecds.protocol.rest.Cell cell = new hxc.ecds.protocol.rest.Cell() //
						.setCompanyID(companyID) //
						.setMobileCountryCode(stage.getI1()) //
						.setMobileNetworkCode(stage.getI2()) //
						.setLocalAreaCode(stage.getI3()) //
						.setCellID(stage.getI4()) //
						.setLatitude(stage.getR1()) //
						.setLongitude(stage.getR2()) //
				;

				// Add area
				String areaIdsStr = stage.getAreaIds();
				String[] areaIds = areaIdsStr.split(",");
				for (int i = 0; i < areaIds.length; ++i)
				{
					if ( areaIds[i].isEmpty() ) continue;
					
					int areaId = Integer.parseInt(areaIds[i]);
					Area area = Area.findByID(em, areaId, stage.getCompanyID());
					if (area != null)
					{
						((List<Area>) cell.getAreas()).add(area);
					}
				}
				
				// Add cellGroup
				String cellGroupIdsStr = stage.getCellGroupIds();
				String[] cellGroupIds = cellGroupIdsStr.split(",");
				for (int i = 0; i < cellGroupIds.length; ++i)
				{
					if ( cellGroupIds[i].isEmpty() ) continue;
					
					int cellGroupId = Integer.parseInt(cellGroupIds[i]);
					CellGroup cellGroup = CellGroup.findByID(em, cellGroupId, stage.getCompanyID());
					if (cellGroup != null)
					{
						((List<CellGroup>) cell.getCellGroups()).add(cellGroup);
					}
				}

				cells.updateCell(em, cell, session);
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
			List<Stage> updates = Stage.findRecords(em, companyID, batchID, ET_CELL, Stage.ACTION_UPDATE, 0, null);

			for (Stage stage : updates)
			{
				hxc.ecds.protocol.rest.Cell cell = new hxc.ecds.protocol.rest.Cell() //
						.setId(stage.getEntityID()) //
						.setCompanyID(companyID) //
						.setMobileCountryCode(stage.getI1()) //
						.setMobileNetworkCode(stage.getI2()) //
						.setLocalAreaCode(stage.getI3()) //
						.setCellID(stage.getI4()) //
						.setLatitude(stage.getR1()) //
						.setLongitude(stage.getR2()) //
				;

				// Add area
				String areaIdsStr = stage.getAreaIds();
				if (areaIdsStr != null)
				{
					String[] areaIds = areaIdsStr.split(",");
					for (int i = 0; i < areaIds.length; ++i)
					{
						if ( areaIds[i].isEmpty() ) continue;
						
						int areaId = Integer.parseInt(areaIds[i]);
						Area area = Area.findByID(em, areaId, stage.getCompanyID());
						if (area != null)
						{
							((List<Area>) cell.getAreas()).add(area);
						}
					}
				}	
				
				// Add cellGroup
				String cellGroupIdsStr = stage.getCellGroupIds();
				if (cellGroupIdsStr != null)
				{
					String[] cellGroupIds = cellGroupIdsStr.split(",");
					for (int i = 0; i < cellGroupIds.length; ++i)
					{
						if ( cellGroupIds[i].isEmpty() ) continue;
						
						int cellGroupId = Integer.parseInt(cellGroupIds[i]);
						CellGroup cellGroup = CellGroup.findByID(em, cellGroupId, stage.getCompanyID());
						if (cellGroup != null)
						{
							((List<CellGroup>) cell.getCellGroups()).add(cellGroup);
						}
					}
				}	

				cells.updateCell(em, cell, session);
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
			List<Stage> deletes = Stage.findRecords(em, companyID, batchID, ET_CELL, Stage.ACTION_DELETE, 0, null);

			for (Stage stage : deletes)
			{
				cells.deleteCell(em, stage.getEntityID(), session);
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
