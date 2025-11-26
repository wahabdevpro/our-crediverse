package cs.controller;

import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import cs.dto.GuiCell;
import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.dto.security.LoginSessionData;
import cs.service.CellService;
import cs.service.ConfigurationService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.Cell;
import hxc.ecds.protocol.rest.ExResult;

@RestController
@RequestMapping(value={"api/cells"})

public class CellController {
	private static final Logger logger = LoggerFactory.getLogger(CellController.class);

	@Autowired
	private ConfigurationService configService;

	@Autowired
	private CellService cellsService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list(@RequestParam Map<String, String> params) throws Exception
	{
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		String filter = this.compileFilter( params );
		ExResult<Cell> cells = null;
		if (dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty())
		{
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				cells = cellsService.listCells( filter, null, dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr) );
				return new GuiDataTable(typeConvertorService.getGuiCellFromCell(cells.getInstances()), cells.getFoundRows() == null ? dtr.getStart() + ((cells.getInstances().length < dtr.getLength()) ? cells.getInstances().length : (dtr.getLength() * 2)) : cells.getFoundRows().intValue());
			}

			cells = cellsService.listCells( filter, null, null, null, null );
		}
		else
		{
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				cells = cellsService.listCells(filter, dtr.getSearch().getValue(), dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr) );
				return new GuiDataTable(typeConvertorService.getGuiCellFromCell(cells.getInstances()), cells.getFoundRows() == null ? dtr.getStart() + ((cells.getInstances().length < dtr.getLength()) ? cells.getInstances().length : (dtr.getLength() * 2)) : cells.getFoundRows().intValue());
			}

			cells = cellsService.listCells( filter, dtr.getSearch().getValue(), null, null, null );
		}
		return new GuiDataTable(typeConvertorService.getGuiCellFromCell(cells.getInstances()));
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) count = cellsService.countCells(search);

		return cellsService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.CELL, ".csv"));

		long cellCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) cellCount = cellsService.countCells(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(cellCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		cellsService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, cellCount, false, null);
	}

	@RequestMapping(value="{cellId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Cell getCell(@PathVariable("cellId") String cellId) throws Exception
	{
		return cellsService.getCell(cellId);
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Cell[] serverList() throws Exception
	{
		Cell[] cells = cellsService.listCells();
		return cells;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiCell create(@RequestBody(required = true) GuiCell newCell, Locale locale) throws Exception
	{
		Cell cell = typeConvertorService.getCellFromGuiCell(newCell);
		cellsService.create(cell);
		return newCell;
	}

	@RequestMapping(value="{cell}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("cell") String cellid) throws Exception
	{
		cellsService.delete(cellid);
		return "{}";
	}

	@RequestMapping(method = RequestMethod.PUT)
	public GuiCell update(@RequestBody(required = true) GuiCell updatedCell, Locale locale) throws Exception
	{
		Cell cell = typeConvertorService.getCellFromGuiCell(updatedCell);
		cellsService.update(cell);
		return updatedCell;
	}

	private String compileFilter(Map<String, String> params) throws Exception
	{
		String filter = "";
		for (Map.Entry<String, String> entry : params.entrySet())
		{
			logger.info(entry.getKey() + "/" + entry.getValue());
		}
		if ( params.containsKey( "mobileCountryCode" ) && !params.get( "mobileCountryCode" ).isEmpty())
			filter = addFilter( filter, "mobileCountryCode", "=", params.get( "mobileCountryCode" ).trim().replaceFirst("[*]$", "%"));
		if ( params.containsKey( "mobileNetworkCode" ) && !params.get( "mobileNetworkCode" ).isEmpty())
			filter = addFilter( filter, "mobileNetworkCode", "=", params.get( "mobileNetworkCode" ).trim().replaceFirst("[*]$", "%"));
		if ( params.containsKey( "localAreaCode" ) && !params.get( "localAreaCode" ).isEmpty())
			filter = addFilter( filter, "localAreaCode", "=", params.get( "localAreaCode" ).trim().replaceFirst("[*]$", "%"));
		if ( params.containsKey( "cellID" ) && !params.get( "cellID" ).isEmpty())
			filter = addFilter( filter, "cellID", "=", params.get( "cellID" ).trim().replaceFirst("[*]$", "%"));
		if ( params.containsKey( "areaID" ) && !params.get( "areaID" ).isEmpty() )
		{
			filter = addFilter( filter, "areaID", ":", params.get( "areaID" ) );
			if (params.containsKey( "recursive") )
			{
				filter = addFilter(filter, "recursive",  "=", params.get("recursive"));
			}
		}
		if ( params.containsKey( "cellGroupID" ) && !params.get( "cellGroupID" ).isEmpty() )
			filter = addFilter( filter, "cellGroupID", ":", params.get( "cellGroupID" ) );
		return filter;
	}

	private String addFilter(String filter, String name, String operator, String value)
	{
		if ( !value.equals("")  && !value.equals("null"))
		{
			if ( !filter.equals("") )
				filter += "+";
			filter += name + operator + "'" + value + "'";
		}
		return filter;
	}

	private String tsSortFromDtSort( GuiDataTableRequest dtr )
	{
		String order = "";
		for ( int i = 0; i < dtr.getOrder().size(); ++i )
		{
			GuiDataTableRequest.Order ro = dtr.getOrder().get( i );
			String dir = ro.isAscending() ? "+" : "-";
			switch( ro.getColumn().getData() )
			{
			case "id": order += "agent.id" + dir; break;
			case "mobileCountryCode": order += "cell.mobileCountryCode" + dir; break;
			case "mobileNetworkCode": order += "cell.mobileNetworkCode" + dir; break;
			case "localAreaCode": order += "cell.localAreaCode" + dir; break;
			case "cellID": order += "cell.cellID" + dir; break;
			//TODO: latitude & longitude
			}
		}
		return order;
	}
}
