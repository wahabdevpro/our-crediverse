package cs.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import cs.service.ConfigurationService;
import cs.service.DepartmentService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.Department;


@RestController
@RequestMapping("/api/departments")
public class DepartmentController
{
	@Autowired
	private ConfigurationService configService;

	@Autowired
	private DepartmentService departmentsService;

	@Autowired
	private LoginSessionData sessionData;

	private List<TableColumn> columns = new ArrayList<TableColumn>();

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list() throws Exception
	{
		Department[] departments = departmentsService.listDepartments();
		ArrayList<Department> fdepartment = new ArrayList<Department>();
		Arrays.asList(departments).forEach((department)->{
				fdepartment.add(department);
		});
		return new GuiDataTable(fdepartment.toArray());
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) count = departmentsService.countDepartments(search);

		return departmentsService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.DEPT, ".csv"));

		long deptCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		deptCount = departmentsService.countDepartments(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(deptCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		departmentsService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, deptCount, true, null);
	}

	@RequestMapping(value="{departmentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Department getDepartment(@PathVariable("departmentId") String departmentId) throws Exception
	{
		return departmentsService.getDepartment(departmentId);
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Department[] serverList() throws Exception
	{
		Department[] departments = departmentsService.listDepartments();
		return departments;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Department create(@RequestBody(required = true) Department newDepartment, Locale locale) throws Exception
	{
		departmentsService.create(newDepartment);
		return newDepartment;
	}

	@RequestMapping(value="{department}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("department") String departmentid) throws Exception
	{
		departmentsService.delete(departmentid);
		return "{}";
	}

	@RequestMapping(value="columns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TableColumn> listColumns() throws Exception
	{
		columns.clear();
		//columns.add(new TableColumn("id", "ID"));
		columns.add(new TableColumn("name", "Department Name"));
		return columns;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public Department update(@RequestBody(required = true) Department updatedDepartment, Locale locale) throws Exception
	{
		departmentsService.update(updatedDepartment);
		return updatedDepartment;
	}

}
