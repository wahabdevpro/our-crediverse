package cs.controller;

import java.util.ArrayList;
import java.util.Map;

import hxc.ecds.protocol.rest.reports.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


import cs.dto.GuiDataTable;
import cs.dto.GuiReportSchedule;
import cs.dto.error.GuiValidationException;
import cs.service.ReportsService;
import hxc.ecds.protocol.rest.Violation;

@RestController
@RequestMapping("api/reports/area")
public class ReportsByAreaController {

	@Autowired
	private ReportsService reportsService;

	@Autowired
	private ReportsController reportsController;

    @RequestMapping(value = "daily_sales", method = RequestMethod.POST)
	public int createSalesPerformanceReportByArea(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		ReportsController.Period period = new ReportsController.Period( params, violations );

		String name = "";
		String desc = "";
		if ( params.containsKey( "name" ) )
			name = params.get( "name" ).trim();
		else
			violations.add(new Violation(Violation.CANNOT_BE_EMPTY, "name", null, "Name cannot be empty"));
		if ( params.containsKey( "description" ) )
			desc = params.get( "description" ).trim();

		if (violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid parameters");

		return reportsService.createSalesPerformanceReportByArea(name , desc, period.getPeriod());
	}

	@RequestMapping(value="daily_sales/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listDailyGroupStatusReports() throws Exception
	{
		DailyPerformanceByAreaListResult report = null;
		report = reportsService.listPerformanceReportsByArea();
		return new GuiDataTable(report.getEntries().toArray(new DailyPerformanceByAreaSpecification[0]));
	}

	@RequestMapping(value = "{type}/{id}/schedule", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReportSchedule createSchedule(@PathVariable("type") String reportType, @PathVariable("id") String reportId, @RequestBody(required = true) GuiReportSchedule schedule) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		reportsController.transformGuiReportScheduleToReportSchedule(schedule, violations);

		if (violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid parameters");

		return reportsService.createSchedule(reportType, reportId, schedule.getReportSchedule());
	}

	@RequestMapping(value = "{type}/{id}/schedule/{sid}/execute", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ExecuteScheduleResponse executeSchedule(@PathVariable("type") String reportType, @PathVariable("id") String reportId, @PathVariable("sid") String scheduleId, @RequestBody(required = true) ExecuteScheduleRequest req) throws Exception
	{
		return reportsService.executeSchedule(reportType, reportId, scheduleId, req);
	}
	@RequestMapping(value = "{type}/{id}/schedule/{sid}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReportSchedule updateSchedule(@PathVariable("type") String reportType, @PathVariable("id") String reportId, @PathVariable("sid") String scheduleId, @RequestBody(required = true) GuiReportSchedule schedule) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		reportsController.transformGuiReportScheduleToReportSchedule(schedule, violations);

		if (violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid parameters");

		return reportsService.updateSchedule(reportType, reportId, scheduleId, schedule.getReportSchedule());
	}
	@RequestMapping(value="{type}/{id}/schedule/{sid}", method = RequestMethod.DELETE)
	public String deleteSchedule(@PathVariable("type") String reportType, @PathVariable("id") String reportId, @PathVariable("sid") String scheduleId) throws Exception
	{
		reportsService.deleteSchedule(reportType, reportId, scheduleId);
		return "{}";
	}
	@RequestMapping(value="daily_sales/{id}", method = RequestMethod.DELETE)
	public String deleteSalesPerformanceReportByArea(@PathVariable("id") String reportId) throws Exception
	{
		reportsService.deletePerformanceReportByArea(reportId);
		return "{}";
	}
	@RequestMapping(value = "daily_sales/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public DailyPerformanceByAreaSpecification getRetailerReport(@PathVariable("id") String reportId) throws Exception
	{
		return reportsService.getPerformanceReportByArea(reportId);
	}
	@RequestMapping(value="daily_sales/{id}", method = RequestMethod.PUT)
	public int updateDailySalesReportByArea(@PathVariable("id") String reportId, @RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		ReportsController.Period period = new ReportsController.Period( params, violations );
		
		String name = "";
		String desc = "";
		if ( params.containsKey( "name" ) )
			name = params.get( "name" ).trim();
		else
			violations.add(new Violation(Violation.CANNOT_BE_EMPTY, "name", null, "Name cannot be empty"));

		if ( params.containsKey( "description" ) )
			desc = params.get( "description" ).trim();

		if (violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid parameters");

		return reportsService.updatePerformanceReportByArea(reportId, name, desc, "", "", period.getPeriod());

	}
}
