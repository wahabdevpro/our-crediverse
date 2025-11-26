package cs.controller.portal;

import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import cs.dto.GuiDataTableRequest;
import cs.dto.GuiReportSchedule;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.service.ReportsService;
import cs.utility.BatchUtility;
import hxc.ecds.protocol.rest.AgentUser;
import hxc.ecds.protocol.rest.ExResultList;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportListResult;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportResult;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportResultEntry;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportSpecification;
import hxc.ecds.protocol.rest.reports.ExecuteScheduleRequest;
import hxc.ecds.protocol.rest.reports.ExecuteScheduleResponse;
import hxc.ecds.protocol.rest.reports.ReportSchedule;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportResult;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportResultEntry;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportSpecification;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportResult;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportResultEntry;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportSpecification;

@RestController
@RequestMapping("/papi/reports")
public class PortalReportsController
{
	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private ReportsService reportsService;

	private static String addFilter(String filter, String name, String operator, String value)
	{
		if ( !value.equals("") )
		{
			if ( !filter.equals("") )
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
			filter = addFilter( filter, "transactionType", "=", params.get( "transactionType" ).trim() );
		if ( params.containsKey( "transactionStatus" ) )
			filter = addFilter( filter, "transactionStatus", "=", params.get( "transactionStatus" ) );
		if ( params.containsKey( "followUp" ) )
			filter = addFilter( filter, "followUp", "=", params.get( "followUp" ) );
		if ( params.containsKey( "a_MobileNumber" ) )
			filter = addFilter( filter, "a_MobileNumber", "=", params.get( "a_MobileNumber" ).trim() );
		if ( params.containsKey( "a_TierName" ) )
			filter = addFilter( filter, "a_TierName", "=", params.get( "a_TierName" ).trim() );
		if ( params.containsKey( "a_GroupName" ) )
			filter = addFilter( filter, "a_GroupName", "=", params.get( "a_GroupName" ).trim() );
		if ( params.containsKey( "a_ServiceClassName" ) )
			filter = addFilter( filter, "a_ServiceClassName", "=", params.get( "a_ServiceClassName" ).trim() );
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
	public void getRetailerReportCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		Period period = new Period( params, violations );

		String filter = this.getRetailerReportFilter(params);

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), "retailer_performance_report", ".csv"));

		OutputStream outputStream = response.getOutputStream();
		reportsService.csvExport(reportsService.addRetailerPerformanceReportParams(period.getTimeFrom(), period.getTimeTo(), period.getPeriod()), params.get("uniqid"), outputStream, filter, null);
	}

	@RequestMapping(value="retailerperformance/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listRetailerReports() throws Exception
	{
		RetailerPerformanceReportListResult report = null;
		report = reportsService.listRetailerReports(null);
		return new GuiDataTable(report.getEntries().toArray(new RetailerPerformanceReportSpecification[0]));
	}

	/**
	 * FIXME ... this should be a POST method
	 * cannot change to POST until Portal GUI is updated to match
	 */
	@RequestMapping(value = "retailerperformance", method = RequestMethod.PUT)
	public void createRetailerReport(@RequestParam Map<String, String> params) throws Exception
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

		reportsService.createRetailerReport(name, desc, period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, getRetailerReportSorting(dtr));
	}

	@RequestMapping(value = "retailerperformance/{id}", method = RequestMethod.PUT)
	public void updateRetailerReport(@PathVariable("id") String reportId, @RequestParam Map<String, String> params) throws Exception
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

		reportsService.updateRetailerReport(reportId, name, desc, period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, getRetailerReportSorting(dtr));
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
			filter = addFilter( filter, "transactionType", "=", params.get( "transactionType" ).trim() );
		if ( params.containsKey( "transactionStatus" ) )
			filter = addFilter( filter, "transactionStatus", "=", params.get( "transactionStatus" ) );
		if ( params.containsKey( "followUp" ) )
			filter = addFilter( filter, "followUp", "=", params.get( "followUp" ) );
		if ( params.containsKey( "a_MobileNumber" ) )
			filter = addFilter( filter, "a_MobileNumber", "=", params.get( "a_MobileNumber" ).trim() );
		if ( params.containsKey( "a_TierName" ) )
			filter = addFilter( filter, "a_TierName", "=", params.get( "a_TierName" ).trim() );
		if ( params.containsKey( "a_GroupName" ) )
			filter = addFilter( filter, "a_GroupName", "=", params.get( "a_GroupName" ).trim() );
		if ( params.containsKey( "a_ServiceClassName" ) )
			filter = addFilter( filter, "a_ServiceClassName", "=", params.get( "a_ServiceClassName" ).trim() );
		if ( params.containsKey( "a_OwnerMobileNumber" ) )
			filter = addFilter( filter, "a_OwnerMobileNumber", "=", params.get( "a_OwnerMobileNumber" ).trim() );
		if ( params.containsKey( "a_imei" ) )
			filter = addFilter( filter, "a_IMEI", "=", params.get( "a_imei" ).trim() );
		if ( params.containsKey( "b_MobileNumber" ) )
			filter = addFilter( filter, "b_MobileNumber", "=", params.get( "b_MobileNumber" ).trim() );
		if ( params.containsKey( "b_TierName" ) )
			filter = addFilter( filter, "b_TierName", "=", params.get( "b_TierName" ).trim() );
		if ( params.containsKey( "b_GroupName" ) )
			filter = addFilter( filter, "b_GroupName", "=", params.get( "b_GroupName" ).trim() );
		if ( params.containsKey( "b_ServiceClassName" ) )
			filter = addFilter( filter, "b_ServiceClassName", "=", params.get( "b_ServiceClassName" ).trim() );
		if ( params.containsKey( "b_OwnerMobileNumber" ) )
			filter = addFilter( filter, "b_OwnerMobileNumber", "=", params.get( "b_OwnerMobileNumber" ).trim() );
		if ( params.containsKey( "b_imei" ) )
			filter = addFilter( filter, "b_IMEI", "=", params.get( "b_imei" ).trim() );
		if ( params.containsKey( "totalAmountMin" ) )
			filter = addFilter( filter, "totalAmount", ">=", params.get( "totalAmountMin" ).trim() );
		if ( params.containsKey( "totalAmountMax" ) )
			filter = addFilter( filter, "totalAmount", "<=", params.get( "totalAmountMax" ).trim() );
		if ( params.containsKey( "disposition" ) )
			filter = addFilter( filter, "disposition", "=", params.get( "disposition" ).trim() );

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
	public void getWholesalerReportCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
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

	/**
	 * FIXME ... this should be a POST method
	 * (CONFIRM THIS) cannot change to POST until Portal GUI is updated to match
	 */
	@RequestMapping(value = "wholesalerperformance", method = RequestMethod.PUT)
	public void createWholesalerReport(@RequestParam Map<String, String> params) throws Exception
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

		reportsService.createWholesalerReport(name, desc, period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, getWholesalerReportSorting(dtr));
	}

	@RequestMapping(value = "wholesalerperformance/{id}", method = RequestMethod.PUT)
	public void updateWholesalerReport(@PathVariable("id") String reportId, @RequestParam Map<String, String> params) throws Exception
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

		reportsService.updateWholesalerReport(reportId, name, desc, period.getTimeFrom(), period.getTimeTo(), period.getPeriod(), filter, getWholesalerReportSorting(dtr));
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

	private String getAccountBalanceSummaryReportFilter(Map<String, String> params) throws Exception
	{
		String filter = "";
		if ( params.containsKey( "tierType" ) )
			filter = addFilter( filter, "tierType", "=", params.get( "tierType" ).trim() );
		if ( params.containsKey( "tierName" ) )
			filter = addFilter( filter, "tierName", "=", params.get( "tierName" ).trim() );
		if ( params.containsKey( "groupName" ) )
			filter = addFilter( filter, "groupName", "=", params.get( "groupName" ).trim() );
		if ( params.containsKey( "includeZeroBalance" ) && params.get( "includeZeroBalance" ).equals("1") )
			filter = addFilter( filter, "includeZeroBalance", "=", "1" );
		else
			filter = addFilter( filter, "includeZeroBalance", "=", "0" );

		return filter;
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
	public void getRAccountBalanceSummaryReportCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
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

	/**
	 * FIXME ... this should be a POST method
	 * (CONFIRM THIS) cannot change to POST until Portal GUI is updated to match
	 */
	@RequestMapping(value = "accountbalancesummary", method = RequestMethod.PUT)
	public void createAccountBalanceSummaryReport(@RequestParam Map<String, String> params) throws Exception
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

		reportsService.createAccountBalanceSummaryReport(name, desc, filter, getAccountBalanceSummaryReportSorting(dtr));
	}

	@RequestMapping(value = "accountbalancesummary/{id}", method = RequestMethod.PUT)
	public void updateAccountBalanceSummaryReport(@PathVariable("id") String reportId, @RequestParam Map<String, String> params) throws Exception
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

		reportsService.updateAccountBalanceSummaryReport(reportId, name, desc, filter, getAccountBalanceSummaryReportSorting(dtr));
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
		return reportsService.getAccountBalanceSummaryReport(reportId);
	}


	///////////////////////////////////////////////////////////////////////////
	// Report Scheduling
	// type:
	// - account_balance_summary

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

	private void transformGuiReportScheduleToReportSchedule(GuiReportSchedule schedule, List<Violation> violations) throws Exception
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

		if (schedule.getAgentUsers() != null)
			schedule.getAgentUsers().clear();
		else
			schedule.setAgentUsers(new ArrayList<AgentUser>());
		for(Integer userId : schedule.getAgentUserIds()) {
			AgentUser user = new AgentUser();
			user.setId(userId);
			((List<AgentUser>)schedule.getAgentUsers()).add(user);
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

