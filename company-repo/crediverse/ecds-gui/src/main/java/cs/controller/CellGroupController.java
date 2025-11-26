package cs.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiDataTable;
import cs.dto.GuiDataTable.TableColumn;
import cs.dto.security.LoginSessionData;
import cs.service.CellGroupService;
import cs.service.ConfigurationService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.CellGroup;


@RestController
@RequestMapping("/api/cellgroups")
public class CellGroupController
{
	@Autowired
	private ConfigurationService configService;

	@Autowired
	private CellGroupService cellGroupsService;

	@Autowired
	private LoginSessionData sessionData;

	private List<TableColumn> columns = new ArrayList<TableColumn>();

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list() throws Exception
	{
		CellGroup[] cellGroups = cellGroupsService.listCellGroups();
		ArrayList<CellGroup> fcellGroup = new ArrayList<CellGroup>();
		Arrays.asList(cellGroups).forEach((cellGroup)->{
				fcellGroup.add(cellGroup);
		});
		return new GuiDataTable(fcellGroup.toArray());
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) count = cellGroupsService.countCellGroups(search);

		return cellGroupsService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.CELLGROUP, ".csv"));

		long cellGroupCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		cellGroupCount = cellGroupsService.countCellGroups(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(cellGroupCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		cellGroupsService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, cellGroupCount, true, null);
	}

	@RequestMapping(value="{cellGroupId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public CellGroup getCellGroup(@PathVariable("cellGroupId") String cellGroupId) throws Exception
	{
		return cellGroupsService.getCellGroup(cellGroupId);
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public CellGroup[] serverList() throws Exception
	{
		CellGroup[] cellGroups = cellGroupsService.listCellGroups();
		return cellGroups;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public CellGroup create(@RequestBody(required = true) CellGroup newCellGroup, Locale locale) throws Exception
	{
		cellGroupsService.create(newCellGroup);
		return newCellGroup;
	}

	@RequestMapping(value="{cellGroup}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("cellGroup") String cellGroupid) throws Exception
	{
		cellGroupsService.delete(cellGroupid);
		return "{}";
	}

	@RequestMapping(value="columns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TableColumn> listColumns() throws Exception
	{
		columns.clear();
		//columns.add(new TableColumn("id", "ID"));
		columns.add(new TableColumn("name", "CellGroup Name"));
		return columns;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public CellGroup update(@RequestBody(required = true) CellGroup updatedCellGroup, Locale locale) throws Exception
	{
		cellGroupsService.update(updatedCellGroup);
		return updatedCellGroup;
	}

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(
		@RequestParam(value = "_type") Optional<String> type,
		@RequestParam(value = "term") Optional<String> query,
		@RequestParam(value = "cellGroupID") Optional<Integer> cellGroupID) throws Exception
	{
		CellGroup[] cellGroups = null;

		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			cellGroups = cellGroupsService.listCellGroups(query.get());
		}
		else
		{
			cellGroups = cellGroupsService.listCellGroups();
		}

		Map<Integer, String>cellGroupMap = new TreeMap<Integer, String>();
		if (cellGroups != null)
		{
			Arrays.asList(cellGroups).forEach(cellGroup ->{
				if ( !cellGroupID.isPresent() || ( cellGroup.getId() == cellGroupID.get().intValue() ) )
					cellGroupMap.put(cellGroup.getId(), cellGroup.getName());
			});
		}
		return cellGroupMap;
	}
}
