package cs.controller;

import java.io.OutputStream;
import java.util.Arrays;
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

import cs.dto.GuiArea;
import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.dto.security.LoginSessionData;
import cs.service.AreaService;
import cs.service.ConfigurationService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.Area;

@RestController
@RequestMapping(value={"api/areas"})

public class AreaController {

	@Autowired
	private ConfigurationService configService;

	@Autowired
	private AreaService areasService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list(@RequestParam Map<String, String> params) throws Exception
	{
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		Area[] areas = null;

		if (dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty())
		{
			areas = areasService.listAreas();
		}
		else
		{
			areas = areasService.listAreas(null, dtr.getSearch().getValue(), null, null, null );
		}

		return new GuiDataTable(typeConvertorService.getGuiAreaFromArea(areas));
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) count = areasService.countAreas(search);

		return areasService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.AREA, ".csv"));

		long areaCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		areaCount = areasService.countAreas(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(areaCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		areasService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, areaCount, true, null);
	}

	@RequestMapping(value="{areaId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiArea getArea(@PathVariable("areaId") String areaId) throws Exception
	{
		return typeConvertorService.getGuiAreaFromArea(areasService.getArea(areaId), null);
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Area[] serverList() throws Exception
	{
		Area[] areas = areasService.listAreas();
		return areas;
	}

	@RequestMapping(value="filter", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiArea[] list(@RequestParam(value = "filter", required = false) String filter) throws Exception
	{
		GuiArea[] areas = typeConvertorService.getGuiAreaFromArea(areasService.listAreas(filter));
		return areas;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Area create(@RequestBody(required = true) GuiArea newArea, Locale locale) throws Exception
	{
		Area area = typeConvertorService.getAreaFromGuiArea(newArea);
		areasService.create(area);
		return newArea;
	}

	@RequestMapping(value="{area}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("area") String areaid) throws Exception
	{
		areasService.delete(areaid);
		return "{}";
	}

	@RequestMapping(method = RequestMethod.PUT)
	public Area update(@RequestBody(required = true) Area updatedArea, Locale locale) throws Exception
	{
		areasService.update(updatedArea);
		return updatedArea;
	}

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Area[] serverListDropdown(
		@RequestParam(value = "_type") Optional<String> type,
		@RequestParam(value = "term") Optional<String> query,
		@RequestParam(value = "areaID") Optional<Integer> areaID) throws Exception
	{
		Area[] areas = null;

		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			areas = areasService.listAreas(query.get());
		}
		else
		{
			areas = areasService.listAreas();
		}

		return areas;
	}
}
