package cs.controller;

import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
import cs.dto.security.LoginSessionData;
import cs.service.ConfigurationService;
import cs.service.ServiceClassService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.ServiceClass;

@RestController
@RequestMapping("/api/serviceclass")
public class ServiceClassController {

	@Autowired
	private ConfigurationService configService;

	@Autowired
	private ServiceClassService serviceClassService;

	@Autowired
	private LoginSessionData sessionData;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list() throws Exception
	{
		ServiceClass []  serviceClasses = serviceClassService.listServiceClasses();
		return new GuiDataTable(serviceClasses);
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) count = serviceClassService.countServiceClasses(search);

		return serviceClassService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.SC, ".csv"));

		long scCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		scCount = serviceClassService.countServiceClasses(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(scCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		serviceClassService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, scCount, true, null);
	}

	@RequestMapping(value="{serviceClassId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ServiceClass getServiceClass(@PathVariable("serviceClassId") String serviceClassId) throws Exception
	{
		return serviceClassService.getServiceClass(serviceClassId);
	}

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(@RequestParam(value = "_type") Optional<String> type, @RequestParam(value = "term") Optional<String> query) throws Exception
	{
		return serviceClassService.getServiceClassNameMap(type, query);
	}

	@RequestMapping(value="{serviceClassId}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("serviceClassId") String serviceClassId) throws Exception
	{
		serviceClassService.delete(serviceClassId);
		return "{}";
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ServiceClass create(@RequestBody(required = true) ServiceClass newServiceClass, Locale locale) throws Exception
	{
		serviceClassService.create(newServiceClass);
		return newServiceClass;
	}

	@RequestMapping(value="{serviceClassId}", method = RequestMethod.PUT)
	public ServiceClass update(@PathVariable("serviceClassId") String serviceClassId, @RequestBody(required = true) ServiceClass updatedServiceClass, Locale locale) throws Exception
	{
		serviceClassService.update(updatedServiceClass);
		return updatedServiceClass;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public ServiceClass update(@RequestBody(required = true) ServiceClass updatedServiceClass, Locale locale) throws Exception
	{
		serviceClassService.update(updatedServiceClass);
		return updatedServiceClass;
	}
}
