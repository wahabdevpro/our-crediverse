package cs.controller;

import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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

import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.dto.GuiReportSchedule;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.service.ReportsService;
import cs.utility.BatchUtility;
import hxc.ecds.protocol.rest.ExResultList;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.WebUser;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportListResult;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportResult;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportResultEntry;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportSpecification;
import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportListResult;
import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportResult;
import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportResultEntry;
import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportSpecification;
import hxc.ecds.protocol.rest.reports.ExecuteScheduleRequest;
import hxc.ecds.protocol.rest.reports.ExecuteScheduleResponse;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportResult;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportResultEntry;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportSpecification;
import hxc.ecds.protocol.rest.reports.ReportSchedule;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportResult;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportResultEntry;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportSpecification;
import hxc.ecds.protocol.rest.reports.SalesSummaryReportListResult;
import hxc.ecds.protocol.rest.reports.SalesSummaryReportSpecification;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportResult;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportResultEntry;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportSpecification;


@RestController
@RequestMapping("/api/reports")
public class ReportsController
{
	private static Logger logger = LoggerFactory.getLogger(ReportsController.class);

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private ReportsService reportsService;

	private static String addFilter(String filter, String name, String operator, String value)
	{
		if ( value != null && !value.isEmpty() )
		{
			if ( filter != null && !filter.isEmpty() )
				filter += "+";
			filter += name + operator + "'" + value + "'";
		}
		return filter;
	}

	private static Date getDate(List<Violation> violations, String value, String name, Date min)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try
		{
			Date date = sdf.parse(value);
			if ( (min != null) && (date.compareTo(min) < 0) )
				violations.add(new Violation(Violation.TOO_SMALL, name, sdf.format(min), String.format("Must not be before %s", sdf.format(min))));
			return date;
		}
		catch(ParseException e)
		{
		}
		return null;
	}

	public static class Period
	{
		protected String period = null;
		protected String timeFrom = null;
		protected String timeTo = null;

		public Period( Map<String, String> params, ArrayList<Violation> violations )
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

			Date dateFrom = null;
			Date dateTo = null;

			if ( params.containsKey( "dateFrom" ) )
			{
				dateFrom = getDate(violations, params.get("dateFrom").trim(), "dateFrom", dateFrom);
				if ( dateFrom != null )
					timeFrom = sdf.format(dateFrom) + "T000000";
			}
			if ( params.containsKey( "dateTo" ) )
			{
				dateTo = getDate(violations, params.get("dateTo").trim(), "dateTo", dateFrom);
				if ( dateTo != null )
					timeTo = sdf.format(dateTo) + "T235959";
			}

			if ( params.containsKey( "period" ) )
			{
				if ( !params.get("period").trim().isEmpty() )
					period = params.get("period").trim();
			}
		}

		public String getTimeFrom()
		{
			return timeFrom;
		}

		public String getTimeTo()
		{
			return timeTo;
		}

		public String getPeriod()
		{
			return period;
		}
	}

	///////////////////////////////////////////////////////////////////////////
	// Retailer Performance Report

	private String getRetailerReportSorting( GuiDataTableRequest dtr )
	{
		String order = "";
		for ( int i = 0; i < dtr.getOrder().size(); ++i )
		{
			GuiDataTableRequest.Order ro = dtr.getOrder().get( i );
			String dir = ro.isAscending() ? "+" : "-";
			switch( ro.getColumn().getData() )
			{
			case "date": order += "date" + dir; break;
			case "transactionType": order += "transactionType" + dir; break;
			case "transactionStatus": order += "transactionStatus" + dir; break;
			case "followUp": order += "followUp" + dir; break;
			case "a_AgentID": order += "a_AgentID" + dir; break;
			case "a_AccountNumber": order += "a_AccountNumber" + dir; break;
			case "a_MobileNumber": order += "a_MobileNumber" + dir; break;
			case "a_IMEI": order += "a_IMEI" + dir; break;
			case "a_IMSI": order += "a_IMSI" + dir; break;
			case "a_Name": order += "a_Name" + dir; break;
			case "a_TierName": order += "a_TierName" + dir; break;
			case "a_GroupName": order += "a_GroupName" + dir; break;
			case "a_ServiceClassName": order += "a_ServiceClassName" + dir; break;
			case "a_OwnerMobileNumber": order += "a_OwnerMobileNumber" + dir; break;
			case "a_OwnerImsi": order += "a_OwnerImsi" + dir; break;
			case "a_OwnerName": order += "a_OwnerName" + dir; break;
			case "totalAmount": order += "totalAmount" + dir; break;
			case "totalBonus": order += "totalBonus" + dir; break;
			case "transactionCount": order += "transactionCount" + dir; break;
			}
		}
		return order;
	}

	private String getRetailerReportFilter(Map<String, String> params) throws Exception
	{
		String filter = "";
		if ( params.containsKey( "transactionType" ) )
			filter = addFilter( filter, "transactionType", "~", params.get( "transactionType" ).trim() );
		if ( params.containsKey( "transactionStatus" ) )
			filter = addFilter( filter, "transactionStatus", "=", params.get( "transactionStatus" ) );
		if ( params.containsKey( "followUp" ) )
			filter = addFilter( filter, "followUp", "=", params.get( "followUp" ) );
		if ( params.containsKey( "a_AgentID" ) )
			filter = addFilter( filter, "a_AgentID", "=", params.get( "a_AgentID" ).trim() );
		if ( params.containsKey( "a_MobileNumber" ) )
			filter = addFilter( filter, "a_MobileNumber", "=", params.get( "a_MobileNumber" ).trim() );
		if ( params.containsKey( "a_TierName" ) )
			filter = addFilter( filter, "a_TierName", "=", params.get( "a_TierName" ).trim() );
		if ( params.containsKey( "a_GroupName" ) )
			filter = addFilter( filter, "a_GroupName", "=", params.get( "a_GroupName" ).trim() );
		if ( params.containsKey( "a_ServiceClassName" ) )
			filter = addFilter( filter, "a_ServiceClassName", "=", params.get( "a_ServiceClassName" ).trim() );
		if ( params.containsKey( "a_OwnerID" ) )
			filter = addFilter( filter, "a_OwnerID", "=", params.get( "a_OwnerID" ).trim() );
		if ( params.containsKey( "a_OwnerMobileNumber" ) )
			filter = addFilter( filter, "a_OwnerMobileNumber", "=", params.get( "a_OwnerMobileNumber" ).trim() );
		if ( params.containsKey( "a_imei" ) )
			filter = addFilter( filter, "a_IMEI", "=", params.get( "a_imei" ).trim() );
		if ( params.containsKey( "totalAmountMin" ) )
			filter = addFilter( filter, "totalAmount", ">=", params.get( "totalAmountMin" ).trim() );
		if ( params.containsKey( "totalAmountMax" ) )
			filter = addFilter( filter, "totalAmount", "<=", params.get( "totalAmountMax" ).trim() );

		return filter;
	}

	@RequestMapping(value="agentsByMsisdn/dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> agentListDropdown(@RequestParam(value = "_type") Optional<String> type, @RequestParam(value = "term") Optional<String> query) throws Exception
	{
		return reportsService.getAgentByMsisdnMap(type, query);
	}

	@RequestMapping(value="retailerperformance/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode countRetailerReport(@RequestParam Map<String, String> params, @RequestParam boolean docount) throws Exception
	{
		long recordCount = 0L;
		if (docount)
		{
			ArrayList<Violation> violations = new ArrayList<Violation>();

			Period period = new Period( params, violations );

			if (violations != null && violations.size() > 0)
				throw new GuiValidationException(violations, "Invalid search criteria");

			String filter = this.getRetailerReportFilter(params);

			RetailerPerformanceReportResult report = reportsService.generateRetailerReport(period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, null, 1, null);
			recordCount = report.getFound();
		}

		return reportsService.track(recordCount);
	}

	@RequestMapping(value = "retailerperformance", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable getRetailerReport(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		Period period = new Period( params, violations );

		String filter = this.getRetailerReportFilter(params);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		RetailerPerformanceReportResult report = null;
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			report = reportsService.generateRetailerReport(period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, dtr.getStart(), dtr.getLength(), getRetailerReportSorting(dtr));
			return new GuiDataTable(report.getEntries().toArray(new RetailerPerformanceReportResultEntry[0]), report.getFound() == null ? dtr.getStart() + ((report.getEntries().size() < dtr.getLength()) ? report.getEntries().size() : (dtr.getLength() * 2)) : report.getFound().intValue());
		}

		report = reportsService.generateRetailerReport(period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, null, null, getRetailerReportSorting(dtr));
		return new GuiDataTable(report.getEntries().toArray(new RetailerPerformanceReportResultEntry[0]));
	}

	@RequestMapping(value = "retailerperformance/csv", method = RequestMethod.GET)
	public void getRetailerReportCsv(@RequestParam Map<String, String> params, HttpServletResponse response, @RequestParam boolean docount) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		Period period = new Period( params, violations );

		String filter = this.getRetailerReportFilter(params);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), "retailer_performance_report", ".csv"));

		OutputStream outputStream = response.getOutputStream();
		reportsService.csvExport(reportsService.addRetailerPerformanceReportParams(period.getTimeFrom(), period.getTimeTo(), period.getPeriod()), params.get("uniqid"), outputStream, filter, null, null);
	}

	@RequestMapping(value="retailerperformance/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listRetailerReports() throws Exception
	{
		RetailerPerformanceReportListResult report = null;
		report = reportsService.listRetailerReports(null);
		return new GuiDataTable(report.getEntries().toArray(new RetailerPerformanceReportSpecification[0]));
	}

	@RequestMapping(value = "retailerperformance", method = RequestMethod.POST)
	public int createRetailerReport(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		Period period = new Period( params, violations );

		String filter = this.getRetailerReportFilter(params);

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

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

		return reportsService.createRetailerReport(name, desc, period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, getRetailerReportSorting(dtr));
	}

	@RequestMapping(value = "retailerperformance/{id}", method = RequestMethod.PUT)
	public int updateRetailerReport(@PathVariable("id") String reportId, @RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		Period period = new Period( params, violations );

		String filter = this.getRetailerReportFilter(params);

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

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

		return reportsService.updateRetailerReport(reportId, name, desc, period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, getRetailerReportSorting(dtr));
	}

	@RequestMapping(value="retailerperformance/{id}", method = RequestMethod.DELETE)
	public String deleteRetailerReport(@PathVariable("id") String reportId) throws Exception
	{
		reportsService.deleteRetailerReport(reportId);
		return "{}";
	}

	@RequestMapping(value = "retailerperformance/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public RetailerPerformanceReportSpecification getRetailerReport(@PathVariable("id") String reportId) throws Exception
	{
		return reportsService.getRetailerReport(reportId);
	}


	///////////////////////////////////////////////////////////////////////////
	// Wholesaler Performance Report

	private String getWholesalerReportSorting( GuiDataTableRequest dtr )
	{
		String order = "";
		for ( int i = 0; i < dtr.getOrder().size(); ++i )
		{
			GuiDataTableRequest.Order ro = dtr.getOrder().get( i );
			String dir = ro.isAscending() ? "+" : "-";
			switch( ro.getColumn().getData() )
			{
			case "date": order += "date" + dir; break;
			case "transactionType": order += "transactionType" + dir; break;
			case "transactionStatus": order += "transactionStatus" + dir; break;
			case "followUp": order += "followUp" + dir; break;
			case "a_AgentID": order += "a_AgentID" + dir; break;
			case "a_AccountNumber": order += "a_AccountNumber" + dir; break;
			case "a_MobileNumber": order += "a_MobileNumber" + dir; break;
			case "a_IMEI": order += "a_IMEI" + dir; break;
			case "a_IMSI": order += "a_IMSI" + dir; break;
			case "a_Name": order += "a_Name" + dir; break;
			case "a_TierName": order += "a_TierName" + dir; break;
			case "a_GroupName": order += "a_GroupName" + dir; break;
			case "a_ServiceClassName": order += "a_ServiceClassName" + dir; break;
			case "a_OwnerMobileNumber": order += "a_OwnerMobileNumber" + dir; break;
			case "a_OwnerImsi": order += "a_OwnerImsi" + dir; break;
			case "a_OwnerName": order += "a_OwnerName" + dir; break;
			case "b_AgentID": order += "b_AgentID" + dir; break;
			case "b_AccountNumber": order += "b_AccountNumber" + dir; break;
			case "b_MobileNumber": order += "b_MobileNumber" + dir; break;
			case "b_IMEI": order += "b_IMEI" + dir; break;
			case "b_IMSI": order += "b_IMSI" + dir; break;
			case "b_Name": order += "b_Name" + dir; break;
			case "b_TierName": order += "b_TierName" + dir; break;
			case "b_GroupName": order += "b_GroupName" + dir; break;
			case "b_ServiceClassName": order += "b_ServiceClassName" + dir; break;
			case "b_OwnerMobileNumber": order += "b_OwnerMobileNumber" + dir; break;
			case "b_OwnerImsi": order += "b_OwnerImsi" + dir; break;
			case "b_OwnerName": order += "b_OwnerName" + dir; break;
			case "totalAmount": order += "totalAmount" + dir; break;
			case "totalBonus": order += "totalBonus" + dir; break;
			case "transactionCount": order += "transactionCount" + dir; break;
			}
		}
		return order;
	}

	private String getWholesalerReportFilter(Map<String, String> params) throws Exception
	{
		String filter = "";
		if ( params.containsKey( "transactionType" ) )
			filter = addFilter( filter, "transactionType", "~", params.get( "transactionType" ).trim() ); //operator ~ is 'in'; Bodged fix because reversal and partial reversal are two different things!!! Go figure???
		if ( params.containsKey( "transactionStatus" ) )
			filter = addFilter( filter, "transactionStatus", "=", params.get( "transactionStatus" ) );
		if ( params.containsKey( "followUp" ) )
			filter = addFilter( filter, "followUp", "=", params.get( "followUp" ) );
		if ( params.containsKey( "a_AgentID" ) )
			filter = addFilter( filter, "a_AgentID", "=", params.get( "a_AgentID" ).trim() );
		if ( params.containsKey( "a_MobileNumber" ) )
			filter = addFilter( filter, "a_MobileNumber", "=", params.get( "a_MobileNumber" ).trim() );
		if ( params.containsKey( "a_TierName" ) )
			filter = addFilter( filter, "a_TierName", "=", params.get( "a_TierName" ).trim() );
		if ( params.containsKey( "a_GroupName" ) )
			filter = addFilter( filter, "a_GroupName", "=", params.get( "a_GroupName" ).trim() );
		if ( params.containsKey( "a_ServiceClassName" ) )
			filter = addFilter( filter, "a_ServiceClassName", "=", params.get( "a_ServiceClassName" ).trim() );
		if ( params.containsKey( "a_OwnerID" ) )
			filter = addFilter( filter, "a_OwnerID", "=", params.get( "a_OwnerID" ).trim() );
		if ( params.containsKey( "a_OwnerMobileNumber" ) )
			filter = addFilter( filter, "a_OwnerMobileNumber", "=", params.get( "a_OwnerMobileNumber" ).trim() );
		if ( params.containsKey( "a_imei" ) )
			filter = addFilter( filter, "a_IMEI", "=", params.get( "a_imei" ).trim() );
		if ( params.containsKey( "b_AgentID" ) )
			filter = addFilter( filter, "b_AgentID", "=", params.get( "b_AgentID" ).trim() );
		if ( params.containsKey( "b_MobileNumber" ) )
			filter = addFilter( filter, "b_MobileNumber", "=", params.get( "b_MobileNumber" ).trim() );
		if ( params.containsKey( "b_TierName" ) )
			filter = addFilter( filter, "b_TierName", "=", params.get( "b_TierName" ).trim() );
		if ( params.containsKey( "b_GroupName" ) )
			filter = addFilter( filter, "b_GroupName", "=", params.get( "b_GroupName" ).trim() );
		if ( params.containsKey( "b_ServiceClassName" ) )
			filter = addFilter( filter, "b_ServiceClassName", "=", params.get( "b_ServiceClassName" ).trim() );
		if ( params.containsKey( "b_OwnerID" ) )
			filter = addFilter( filter, "b_OwnerID", "=", params.get( "b_OwnerID" ).trim() );
		if ( params.containsKey( "b_OwnerMobileNumber" ) )
			filter = addFilter( filter, "b_OwnerMobileNumber", "=", params.get( "b_OwnerMobileNumber" ).trim() );
		if ( params.containsKey( "b_imei" ) )
			filter = addFilter( filter, "b_IMEI", "=", params.get( "b_imei" ).trim() );
		if ( params.containsKey( "totalAmountMin" ) )
			filter = addFilter( filter, "totalAmount", ">=", params.get( "totalAmountMin" ).trim() );
		if ( params.containsKey( "totalAmountMax" ) )
			filter = addFilter( filter, "totalAmount", "<=", params.get( "totalAmountMax" ).trim() );

		return filter;
	}

	@RequestMapping(value="wholesalerperformance/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode countWholesalerReport(@RequestParam Map<String, String> params, @RequestParam boolean docount) throws Exception
	{
		long recordCount = 0L;
		if (docount)
		{
			ArrayList<Violation> violations = new ArrayList<Violation>();

			Period period = new Period( params, violations );

			if (violations != null && violations.size() > 0)
				throw new GuiValidationException(violations, "Invalid search criteria");

			String filter = this.getWholesalerReportFilter(params);

			WholesalerPerformanceReportResult report = reportsService.generateWholesalerReport(period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, null, 1, null);
			recordCount = report.getFound();
		}
		return reportsService.track(recordCount);
	}

	@RequestMapping(value = "wholesalerperformance", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable getWholesalerReport(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		Period period = new Period( params, violations );

		String filter = this.getWholesalerReportFilter(params);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		WholesalerPerformanceReportResult report = null;
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			report = reportsService.generateWholesalerReport(period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, dtr.getStart(), dtr.getLength(), getWholesalerReportSorting(dtr));
			return new GuiDataTable(report.getEntries().toArray(new WholesalerPerformanceReportResultEntry[0]), report.getFound() == null ? dtr.getStart() + ((report.getEntries().size() < dtr.getLength()) ? report.getEntries().size() : (dtr.getLength() * 2)) : report.getFound().intValue());
		}

		report = reportsService.generateWholesalerReport(period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, null, null, getWholesalerReportSorting(dtr));
		return new GuiDataTable(report.getEntries().toArray(new WholesalerPerformanceReportResultEntry[0]));
	}

	@RequestMapping(value = "wholesalerperformance/csv", method = RequestMethod.GET)
	public void getWholesalerReportCsv(@RequestParam Map<String, String> params, HttpServletResponse response, @RequestParam boolean docount) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		Period period = new Period( params, violations );

		String filter = this.getWholesalerReportFilter(params);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), "wholesaler_performance_report", ".csv"));

		OutputStream outputStream = response.getOutputStream();
		reportsService.csvExport(reportsService.addWholesalerPerformanceReportParams(period.getTimeFrom(), period.getTimeTo(), period.getPeriod()), params.get("uniqid"), outputStream, filter, null);
	}

	@RequestMapping(value="wholesalerperformance/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listWholesalerReports() throws Exception
	{
		WholesalerPerformanceReportListResult report = null;
		report = reportsService.listWholesalerReports(null);
		return new GuiDataTable(report.getEntries().toArray(new WholesalerPerformanceReportSpecification[0]));
	}

	@RequestMapping(value = "wholesalerperformance", method = RequestMethod.POST)
	public int createWholesalerReport(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		Period period = new Period( params, violations );

		String filter = this.getWholesalerReportFilter(params);

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

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

		return reportsService.createWholesalerReport(name, desc, period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, getWholesalerReportSorting(dtr));
	}

	@RequestMapping(value = "wholesalerperformance/{id}", method = RequestMethod.PUT)
	public int updateWholesalerReport(@PathVariable("id") String reportId, @RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		Period period = new Period( params, violations );

		String filter = this.getWholesalerReportFilter(params);

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

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

		return reportsService.updateWholesalerReport(reportId, name, desc, period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, getWholesalerReportSorting(dtr));
	}

	@RequestMapping(value="wholesalerperformance/{id}", method = RequestMethod.DELETE)
	public String deleteWholesalerReport(@PathVariable("id") String reportId) throws Exception
	{
		reportsService.deleteWholesalerReport(reportId);
		return "{}";
	}

	@RequestMapping(value = "wholesalerperformance/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public WholesalerPerformanceReportSpecification getWholesalerReport(@PathVariable("id") String reportId) throws Exception
	{
		return reportsService.getWholesalerReport(reportId);
	}


	///////////////////////////////////////////////////////////////////////////
	// Sales Summary a.k.a. Hourly SMS Flash Report

	@RequestMapping(value="salessummary/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listSalesSummaryReports() throws Exception
	{
		SalesSummaryReportListResult report = null;
		report = reportsService.listSalesSummaryReports(null);
		return new GuiDataTable(report.getEntries().toArray(new SalesSummaryReportSpecification[0]));
	}

	///////////////////////////////////////////////////////////////////////////
	// Daily Group Sales Performance Report

	private String getDailyGroupSalesReportSorting( GuiDataTableRequest dtr )
	{
		String order = "";
		for ( int i = 0; i < dtr.getOrder().size(); ++i )
		{
			GuiDataTableRequest.Order ro = dtr.getOrder().get( i );
			String dir = ro.isAscending() ? "+" : "-";
			switch( ro.getColumn().getData() )
			{
			case "groupName": order += "groupName" + dir; break;
			case "agentTotalCount": order += "agentTotalCount" + dir; break;
			case "agentTransactedCount": order += "agentTransactedCount" + dir; break;
			case "transactionCount": order += "transactionCount" + dir; break;
			case "agentAverageAmount": order += "agentAverageAmount" + dir; break;
			case "transactionAverageAmount": order += "transactionAverageAmount" + dir; break;
			case "totalAmount": order += "totalAmount" + dir; break;
			}
		}
		return order;
	}

	private String getDailyGroupSalesReportFilter(Map<String, String> params) throws Exception
	{
		String filter = "";
		if ( params.containsKey( "a_TierName" ) )
			filter = addFilter( filter, "tierName", "=", params.get( "a_TierName" ).trim() );
		if ( params.containsKey( "a_GroupName" ) )
			filter = addFilter( filter, "groupName", "=", params.get( "a_GroupName" ).trim() );
		if ( params.containsKey( "transactionType" ) )
			filter = addFilter( filter, "transactionType", "=", params.get( "transactionType" ).trim() );

		return filter;
	}

	@RequestMapping(value="dailygroupsales/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode countDailyGroupSalesReport(@RequestParam Map<String, String> params, @RequestParam boolean docount) throws Exception
	{
		long recordCount = 0L;
		if (docount)
		{
			ArrayList<Violation> violations = new ArrayList<Violation>();

			if (violations != null && violations.size() > 0)
				throw new GuiValidationException(violations, "Invalid search criteria");

			String filter = this.getDailyGroupSalesReportFilter(params);

			DailyGroupSalesReportResult report = reportsService.generateDailyGroupSalesReport(filter, null, 1, null);
			recordCount = report.getFound();
		}
		return reportsService.track(recordCount);
	}

	@RequestMapping(value = "dailygroupsales", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable getDailyGroupSalesReport(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.getDailyGroupSalesReportFilter(params);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		DailyGroupSalesReportResult report = null;
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			report = reportsService.generateDailyGroupSalesReport(filter, dtr.getStart(), dtr.getLength(), getDailyGroupSalesReportSorting(dtr));
			return new GuiDataTable(report.getEntries().toArray(new DailyGroupSalesReportResultEntry[0]), report.getFound() == null ? dtr.getStart() + ((report.getEntries().size() < dtr.getLength()) ? report.getEntries().size() : (dtr.getLength() * 2)) : report.getFound().intValue());
		}

		report = reportsService.generateDailyGroupSalesReport(filter, null, null, getDailyGroupSalesReportSorting(dtr));
		return new GuiDataTable(report.getEntries().toArray(new DailyGroupSalesReportResultEntry[0]));
	}

	@RequestMapping(value = "dailygroupsales/csv", method = RequestMethod.GET)
	public void getDailyGroupSalesReportCsv(@RequestParam Map<String, String> params, HttpServletResponse response, @RequestParam boolean docount) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.getDailyGroupSalesReportFilter(params);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), "daily_group_sales_report", ".csv"));

		OutputStream outputStream = response.getOutputStream();
		reportsService.csvExport(reportsService.addGroupSalesReportParams(), params.get("uniqid"), outputStream, filter, null);
	}

	@RequestMapping(value="dailygroupsales/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listDailyGroupStatusReports() throws Exception
	{
		DailyGroupSalesReportListResult report = null;
		report = reportsService.listDailyGroupSalesReports(null);
		return new GuiDataTable(report.getEntries().toArray(new DailyGroupSalesReportSpecification[0]));
	}

	@RequestMapping(value = "dailygroupsales", method = RequestMethod.POST)
	public int createDailyGroupSalesReport(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.getDailyGroupSalesReportFilter(params);

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

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

		return reportsService.createDailyGroupSalesReport(name, desc, filter, getDailyGroupSalesReportSorting(dtr));
	}

	@RequestMapping(value = "dailygroupsales/{id}", method = RequestMethod.PUT)
	public int updateDailyGroupSalesReport(@PathVariable("id") String reportId, @RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.getDailyGroupSalesReportFilter(params);

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

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

		return reportsService.updateDailyGroupSalesReport(reportId, name, desc, filter, getDailyGroupSalesReportSorting(dtr));
	}

	@RequestMapping(value="dailygroupsales/{id}", method = RequestMethod.DELETE)
	public String deleteDailyGroupSalesReport(@PathVariable("id") String reportId) throws Exception
	{
		reportsService.deleteDailyGroupSalesReport(reportId);
		return "{}";
	}

	@RequestMapping(value = "dailygroupsales/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public DailyGroupSalesReportSpecification getDailyGroupSalesReport(@PathVariable("id") String reportId) throws Exception
	{
		return reportsService.getDailyGroupSalesReport(reportId);
	}


	///////////////////////////////////////////////////////////////////////////
	// Account Balance Summary Performance Report

	private String getAccountBalanceSummaryReportSorting( GuiDataTableRequest dtr )
	{
		String order = "";
		for ( int i = 0; i < dtr.getOrder().size(); ++i )
		{
			GuiDataTableRequest.Order ro = dtr.getOrder().get( i );
			String dir = ro.isAscending() ? "+" : "-";
			switch( ro.getColumn().getData() )
			{
			case "msisdn": order += "msisdn" + dir; break;
			case "agentName": order += "agentName" + dir; break;
			case "balance": order += "bonus" + dir; break;
			case "bonusBalance": order += "bonusBalance" + dir; break;
			case "holdBonus": order += "holdBonus" + dir; break;
			case "tierType": order += "tierType" + dir; break;
			case "tierName": order += "tierName" + dir; break;
			case "groupName": order += "groupName" + dir; break;
			}
		}
		return order;
	}
	
	private String getAccountBalanceSummaryReportFilter(Map<String, String> params, boolean activityAsDays) throws Exception {
		String filter = "";

		if (params.containsKey("tierType"))
			filter = addFilter(filter, "tierType", "=", params.get("tierType").trim());

		if (params.containsKey("tierName"))
			filter = addFilter(filter, "tierName", "=", params.get("tierName").trim());

		if (params.containsKey("groupName"))
			filter = addFilter(filter, "groupName", "=", params.get("groupName").trim());

		if (params.containsKey("includeZeroBalance") && params.get("includeZeroBalance").equals("1"))
			filter = addFilter(filter, "includeZeroBalance", "=", "1");
		else
			filter = addFilter(filter, "includeZeroBalance", "=", "0");
		
		if (params.containsKey("includeDeleted") && params.get("includeDeleted").equals("1"))
			filter = addFilter(filter, "includeDeleted", "=", "1");
		else
			filter = addFilter(filter, "includeDeleted", "=", "0");

		if (params.containsKey("activityScale") && !params.get("activityScale").equals("disabled")) {
			Integer activityValue;
			String activityScale = params.get("activityScale");
			if (activityAsDays) {
				
				long diffDays = 1L;
	
				try {
					if (params.get("activityValue") == null || params.get("activityValue").trim().isEmpty()) {
						throw new GuiValidationException("Activity cutoff cannot be empty.");
					}
					activityValue = Integer.parseInt(params.get("activityValue").trim());
					if (activityValue <= 0)
						throw new NumberFormatException("is negative or zero");
					
					Calendar calendarNow	= Calendar.getInstance();
					Calendar calendarThen	= Calendar.getInstance();
	
					switch (activityScale) {
					case "days":
						calendarThen.add(Calendar.DATE, (activityValue * -1));
						break;
					case "weeks":
						calendarThen.add(Calendar.DATE, (activityValue * -7));
						break;
					case "months":
						calendarThen.add(Calendar.MONTH, (activityValue * -1));
						break;
					case "years":
						calendarThen.add(Calendar.YEAR, (activityValue * -1));
						break;
					default:
						throw new IllegalArgumentException(
								"Invalid activityScale, must be one of [days, weeks, months, years]");
					}
					
					Date now	= calendarNow.getTime();
					Date then	= calendarThen.getTime();
					
					long	diffTime = now.getTime() - then.getTime();
							diffDays = diffTime / (1000 * 60 * 60 * 24);
					
					logger.debug("activityValue difference \"in days\" is: " + String.valueOf(diffDays));
	
					// ---- Don't need to set 'diffDays' in either exception
					//		In the event of a NumberFormat failure, it will retain it's original value of '1L'
					//		In the event of a IllegalArgument failure, it will have a value value usable by the "days" scale
				} catch (NumberFormatException ex) {
					activityScale = "days";
					logger.warn("Invalid activityValue, is negative or zero. defaulting to: ", String.valueOf(diffDays));
				} catch (IllegalArgumentException ex) {
					activityScale = "days";
					logger.warn("Invalid activityScale, not one of [days, weeks, months, years], forcing \"days\"");
				}
	
				filter = addFilter( filter, "activityValue", "=", String.valueOf(diffDays) );
			}
			else {
				try {
					activityValue = Integer.parseInt(params.get("activityValue").trim());
					// TODO :	Activity Scale is not sanitised.
					//			But it is a <select> on the GUI side so is assumed it can be ignored.
					//			It is checked when actually talking to the database, so shouldn't need additional checking necessarily
					filter = addFilter( filter, "activityScale", "=", String.valueOf(activityScale) );
 					filter = addFilter( filter, "activityValue", "=", String.valueOf(activityValue) );
				} catch (NumberFormatException ex) {
					activityValue = 0;
					activityScale = "disabled";
				}
			}
		}

		return filter;
	}

	private String getAccountBalanceSummaryReportFilter(Map<String, String> params) throws Exception {
		return getAccountBalanceSummaryReportFilter(params, true);
	}

	@RequestMapping(value="accountbalancesummary/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode countAccountBalanceSummaryReport(@RequestParam Map<String, String> params, @RequestParam boolean docount) throws Exception
	{
		long recordCount = 0L;
		if (docount)
		{
			ArrayList<Violation> violations = new ArrayList<Violation>();

			if (violations != null && violations.size() > 0)
				throw new GuiValidationException(violations, "Invalid search criteria");

			String filter = this.getAccountBalanceSummaryReportFilter(params);

			AccountBalanceSummaryReportResult report = reportsService.generateAccountBalanceSummaryReport(filter, null, 1, null);
			recordCount = report.getFound();
		}
		return reportsService.track(recordCount);
	}

	@RequestMapping(value = "accountbalancesummary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable getAccountBalanceSummaryReport(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.getAccountBalanceSummaryReportFilter(params);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		AccountBalanceSummaryReportResult report = null;
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			report = reportsService.generateAccountBalanceSummaryReport(filter, dtr.getStart(), dtr.getLength(), getAccountBalanceSummaryReportSorting(dtr));
			return new GuiDataTable(report.getEntries().toArray(new AccountBalanceSummaryReportResultEntry[0]), report.getFound() == null ? dtr.getStart() + ((report.getEntries().size() < dtr.getLength()) ? report.getEntries().size() : (dtr.getLength() * 2)) : report.getFound().intValue());
		}

		report = reportsService.generateAccountBalanceSummaryReport(filter, null, null, getAccountBalanceSummaryReportSorting(dtr));
		return new GuiDataTable(report.getEntries().toArray(new AccountBalanceSummaryReportResultEntry[0]));
	}

	@RequestMapping(value = "accountbalancesummary/csv", method = RequestMethod.GET)
	public void getRAccountBalanceSummaryReportCsv(@RequestParam Map<String, String> params, HttpServletResponse response, @RequestParam boolean docount) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.getAccountBalanceSummaryReportFilter(params);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), "account_balance_summary_report", ".csv"));

		OutputStream outputStream = response.getOutputStream();
		reportsService.csvExport(reportsService.addAccountBalanceSummaryReportParams(), params.get("uniqid"), outputStream, filter, null);
	}

	@RequestMapping(value="accountbalancesummary/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listAccountBalanceSummaryReports() throws Exception
	{
		AccountBalanceSummaryReportListResult report = null;
		report = reportsService.listAccountBalanceSummaryReports(null);
		return new GuiDataTable(report.getEntries().toArray(new AccountBalanceSummaryReportSpecification[0]));
	}

	@RequestMapping(value = "accountbalancesummary", method = RequestMethod.POST)
	public int createAccountBalanceSummaryReport(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.getAccountBalanceSummaryReportFilter(params);

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

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

		return reportsService.createAccountBalanceSummaryReport(name, desc, filter, getAccountBalanceSummaryReportSorting(dtr));
	}

	@RequestMapping(value = "accountbalancesummary/{id}", method = RequestMethod.PUT)
	public int updateAccountBalanceSummaryReport(@PathVariable("id") String reportId, @RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		String filter = this.getAccountBalanceSummaryReportFilter(params, false);

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

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

		return reportsService.updateAccountBalanceSummaryReport(reportId, name, desc, filter, getAccountBalanceSummaryReportSorting(dtr));
	}

	@RequestMapping(value="accountbalancesummary/{id}", method = RequestMethod.DELETE)
	public String deleteAccountBalanceSummaryReport(@PathVariable("id") String reportId) throws Exception
	{
		reportsService.deleteAccountBalanceSummaryReport(reportId);
		return "{}";
	}

	@RequestMapping(value = "accountbalancesummary/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public AccountBalanceSummaryReportSpecification getAccountBalanceSummaryReport(@PathVariable("id") String reportId) throws Exception
	{
		AccountBalanceSummaryReportSpecification data = reportsService.getAccountBalanceSummaryReport(reportId);
		return data;
	}

	///////////////////////////////////////////////////////////////////////////
	// Monthly Sales Performance Report

	///////////////////////////////////////////////////////////////////////////
	// Monthly Sales Performance Report

	private String getMonthlySalesPerformanceReportSorting( GuiDataTableRequest dtr )
	{
		String order = "";
		for ( int i = 0; i < dtr.getOrder().size(); ++i )
		{
			GuiDataTableRequest.Order ro = dtr.getOrder().get( i );
			String dir = ro.isAscending() ? "+" : "-";
			switch( ro.getColumn().getData() )
			{
			case "groupName": order += "groupName" + dir; break;
			case "agentTotalCount": order += "agentTotalCount" + dir; break;
			case "agentTransactedCount": order += "agentTransactedCount" + dir; break;
			case "transactionCount": order += "transactionCount" + dir; break;
			case "agentAverageAmount": order += "agentAverageAmount" + dir; break;
			case "transactionAverageAmount": order += "transactionAverageAmount" + dir; break;
			case "totalAmount": order += "totalAmount" + dir; break;
			}
		}
		return order;
	}

	private String getMonthlySalesPerformanceReportFilter(Map<String, String> params) throws Exception
	{
		String filter = "";
		if ( params.containsKey( "tiers" ) )
			filter = addFilter( filter, "tiers", "~", params.get( "tiers" ).trim() );
		if ( params.containsKey( "groups" ) )
			filter = addFilter( filter, "groups", "~", params.get( "groups" ).trim() );
		if ( params.containsKey( "agents" ) )
			filter = addFilter( filter, "agents", "~", params.get( "agents" ).trim() );
		if ( params.containsKey( "ownerAgents" ) )
			filter = addFilter( filter, "ownerAgents", "~", params.get( "ownerAgents" ).trim() );
		if ( params.containsKey( "transactionTypes" ) )
			filter = addFilter( filter, "transactionTypes", "~", params.get( "transactionTypes" ).trim() );
		if ( params.containsKey( "transactionStatus" ) )
			filter = addFilter( filter, "transactionStatus", "=", params.get( "transactionStatus" ).trim());
		return filter;
	}

	@RequestMapping(value="monthlysalesperformance/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode countMonthlySalesPerformanceReport(@RequestParam Map<String, String> params, @RequestParam boolean docount) throws Exception
	{
		long recordCount = 0L;
		if (docount)
		{
			ArrayList<Violation> violations = new ArrayList<Violation>();

			if (violations != null && violations.size() > 0)
				throw new GuiValidationException(violations, "Invalid search criteria");

			String filter = this.getMonthlySalesPerformanceReportFilter(params);

			DailyGroupSalesReportResult report = reportsService.generateDailyGroupSalesReport(filter, null, 1, null);
			recordCount = report.getFound();
		}
		return reportsService.track(recordCount);
	}

	@RequestMapping(value = "monthlysalesperformance", method = RequestMethod.GET)
	public GuiDataTable previewMonthlySalesPerformanceReportAdhoc(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		try
		{
			Period period = new Period( params, violations );
			String filter = getMonthlySalesPerformanceReportFilter(params);
			if (violations != null && violations.size() > 0)
				throw new GuiValidationException(violations, "Invalid search criteria");

			GuiDataTableRequest dtr = new GuiDataTableRequest(params);

			MonthlySalesPerformanceReportResult report = null;
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				report = reportsService.generateMonthlySalesPerformanceReport(period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, dtr.getStart(), dtr.getLength(), getMonthlySalesPerformanceReportSorting(dtr));
				return new GuiDataTable(report.getEntries().toArray(new MonthlySalesPerformanceReportResultEntry[0]), report.getFound() == null ? dtr.getStart() + ((report.getEntries().size() < dtr.getLength()) ? report.getEntries().size() : (dtr.getLength() * 2)) : report.getFound().intValue());
			}

			report = reportsService.generateMonthlySalesPerformanceReport(period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, null, null, getMonthlySalesPerformanceReportSorting(dtr));
			return new GuiDataTable(report.getEntries().toArray(new MonthlySalesPerformanceReportResultEntry[0]));
		} catch (Exception e) {
			throw e;
		}
	}
	
	@RequestMapping(value = "monthlysalesperformance/csv", method = RequestMethod.GET)
	public void getMonthlySalesPerformanceReportCsvAdHoc(@RequestParam Map<String, String> params, HttpServletResponse response, @RequestParam boolean docount) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		Period period = new Period( params, violations );
		String filter = this.getMonthlySalesPerformanceReportFilter(params);
		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), "monthly_sales_performance_report", ".csv"));

		OutputStream outputStream = response.getOutputStream();
		reportsService.csvExport(reportsService.addMonthlySalesPerformanceReportParams(period.getTimeFrom(), period.getTimeTo(), period.getPeriod()), params.get("uniqid"), outputStream, filter, null, null);
	}

	@RequestMapping(value="monthlysalesperformance/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listMonthlyGroupStatusReports() throws Exception
	{
		MonthlySalesPerformanceReportListResult report = null;
		report = reportsService.listMonthlySalesPerformanceReports(null);
		GuiDataTable result = new GuiDataTable(report.getEntries().toArray(new MonthlySalesPerformanceReportSpecification[0]));
		return result;
	}

	@RequestMapping(value = "monthlysalesperformance", method = RequestMethod.POST)
	public int createMonthlySalesPerformanceReport(@RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		Period period = new Period(params, violations);
		String filter = this.getMonthlySalesPerformanceReportFilter(params);
		String name = "";
		String description = "";
		if ( params.containsKey( "name" ) )
			name = params.get( "name" ).trim();
		else
			violations.add(new Violation(Violation.CANNOT_BE_EMPTY, "name", null, "Name cannot be empty"));
		if ( params.containsKey( "description" ) )
			description = params.get( "description" ).trim();
		if (violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid parameters");
		return reportsService.createMonthlySalesPerformanceReport(name, description, filter, "", period.getPeriod());
	}

	@RequestMapping(value = "monthlysalesperformance/{id}", method = RequestMethod.PUT)
	public int updateMonthlySalesPerformanceReport(@PathVariable("id") String reportId, @RequestBody(required = true) @RequestParam Map<String, String> params) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		Period period = new Period(params, violations);
		String filter = this.getMonthlySalesPerformanceReportFilter(params);
		String name = "";
		String description = "";
		if ( params.containsKey( "name" ) )
			name = params.get( "name" ).trim();
		else
			violations.add(new Violation(Violation.CANNOT_BE_EMPTY, "name", null, "Name cannot be empty"));
		if ( params.containsKey( "description" ) )
			description = params.get( "description" ).trim();
		if (violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid parameters");
		return reportsService.updateMonthlySalesPerformanceReport(reportId, name, description, filter, "", period.getPeriod());
	}

	@RequestMapping(value="monthlysalesperformance/{id}", method = RequestMethod.DELETE)
	public String deleteMonthlySalesPerformanceReport(@PathVariable("id") String reportId) throws Exception
	{
		reportsService.deleteMonthlySalesPerformanceReport(reportId);
		return "{}";
	}

	@RequestMapping(value = "monthlysalesperformance/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public MonthlySalesPerformanceReportSpecification getMonthlySalesPerformanceReport(@PathVariable("id") String reportId) throws Exception
	{
		MonthlySalesPerformanceReportSpecification spec = reportsService.getMonthlySalesPerformanceReport(reportId);
		return spec;
	}

	///////////////////////////////////////////////////////////////////////////
	// Report Scheduling
	// type:
	// - retailer_performance
	// - wholesaler_performance
	// - sales_summary

	private Integer stringToSeconds(String time, String name, List<Violation> violations)
	{
		if (time == null) return null;

		if (time.trim().isEmpty()) return null;

		String[] split = time.trim().split(":");
		if (split.length != 2 )
		{
			violations.add(new Violation("timeInvalid", name, null, "Invalid time, must be HH:MM format"));
			return null;
		}

		int hours = Integer.valueOf(split[0]);
		int minutes = Integer.valueOf(split[1]);
		if (hours < 0 || hours > 23 )
			violations.add(new Violation("timeInvalidHour", name, null, "Invalid hour, must be 00-23"));
		if (minutes < 0 || minutes > 59 )
			violations.add(new Violation("timeInvalidMinute", name, null, "Invalid minute, must be 00-59"));

		return Integer.valueOf(hours * 3600 + minutes * 60);
	}

	@RequestMapping(value = "{type}/{id}/schedule", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable getSchedules(@PathVariable("type") String reportType, @PathVariable("id") String reportId) throws Exception
	{
		ExResultList<ReportSchedule> result = reportsService.listSchedules(reportType, reportId);
		return new GuiDataTable(result.getInstances().toArray(new ReportSchedule[0]));
	}

	@RequestMapping(value = "{type}/{id}/schedule/{sid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReportSchedule getSchedule(@PathVariable("type") String reportType, @PathVariable("id") String reportId, @PathVariable("sid") String scheduleId) throws Exception
	{
		return reportsService.getSchedule(reportType, reportId, scheduleId);
	}

	public void transformGuiReportScheduleToReportSchedule(GuiReportSchedule schedule, List<Violation> violations) throws Exception
	{
		schedule.setTimeOfDay(stringToSeconds(schedule.getTimeOfDayString(), "timeOfDayString", violations));
		schedule.setStartTimeOfDay(stringToSeconds(schedule.getStartTimeOfDayString(), "startTimeOfDayString", violations));
		schedule.setEndTimeOfDay(stringToSeconds(schedule.getEndTimeOfDayString(), "endTimeOfDayString", violations));

		if(schedule.getDeliveryChannelEmail() == null && schedule.getDeliveryChannelSms() == null)
			schedule.setChannels(null);
		else
		{
			Set<String> channels = new HashSet<String>();
			if(schedule.getDeliveryChannelEmail() != null && schedule.getDeliveryChannelEmail() == true)
				channels.add(ReportSchedule.Channel.EMAIL.getChannel());
			if(schedule.getDeliveryChannelSms() != null && schedule.getDeliveryChannelSms() == true)
				channels.add(ReportSchedule.Channel.SMS.getChannel());
			schedule.setChannels(String.join(",", channels));
		}

		if (schedule.getWebUsers() != null)
			schedule.getWebUsers().clear();
		else
			schedule.setWebUsers(new ArrayList<WebUser>());
		for(Integer userId : schedule.getWebUserIds()) {
			//WebUser user = webUserService.getWebUser( userId );
			WebUser user = new WebUser();
			user.setId(userId);
			((List<WebUser>)schedule.getWebUsers()).add(user);
		}
	}

	@RequestMapping(value = "{type}/{id}/schedule", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReportSchedule createSchedule(@PathVariable("type") String reportType, @PathVariable("id") String reportId, @RequestBody(required = true) GuiReportSchedule schedule) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		transformGuiReportScheduleToReportSchedule(schedule, violations);

		if (violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid parameters");

		return reportsService.createSchedule(reportType, reportId, schedule.getReportSchedule());
	}

	@RequestMapping(value = "{type}/{id}/schedule/{sid}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ReportSchedule updateSchedule(@PathVariable("type") String reportType, @PathVariable("id") String reportId, @PathVariable("sid") String scheduleId, @RequestBody(required = true) GuiReportSchedule schedule) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		transformGuiReportScheduleToReportSchedule(schedule, violations);

		if (violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid parameters");

		return reportsService.updateSchedule(reportType, reportId, scheduleId, schedule.getReportSchedule());
	}

	@RequestMapping(value = "{type}/{id}/schedule/{sid}/execute", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ExecuteScheduleResponse executeSchedule(@PathVariable("type") String reportType, @PathVariable("id") String reportId, @PathVariable("sid") String scheduleId, @RequestBody(required = true) ExecuteScheduleRequest req) throws Exception
	{
		return reportsService.executeSchedule(reportType, reportId, scheduleId, req);
	}

	@RequestMapping(value="{type}/{id}/schedule/{sid}", method = RequestMethod.DELETE)
	public String deleteSchedule(@PathVariable("type") String reportType, @PathVariable("id") String reportId, @PathVariable("sid") String scheduleId) throws Exception
	{
		reportsService.deleteSchedule(reportType, reportId, scheduleId);
		return "{}";
	}
}

