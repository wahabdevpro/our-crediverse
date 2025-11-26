package cs.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.dto.GuiCsvResponse;
import cs.dto.GuiMonthlySalesPerformanceReportSpecification;
import cs.dto.security.LoginSessionData;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.BatchUtility;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.ExResultList;
import hxc.ecds.protocol.rest.WebUser;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportListResult;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportResult;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportSpecification;
import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportListResult;
import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportResult;
import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportSpecification;
import hxc.ecds.protocol.rest.reports.DailyPerformanceByAreaListResult;
import hxc.ecds.protocol.rest.reports.DailyPerformanceByAreaSpecification;
import hxc.ecds.protocol.rest.reports.ExecuteScheduleRequest;
import hxc.ecds.protocol.rest.reports.ExecuteScheduleResponse;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReport.FilterField;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReport.FilterItem;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportResult;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportSpecification;
import hxc.ecds.protocol.rest.reports.ReportSchedule;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportResult;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportSpecification;
import hxc.ecds.protocol.rest.reports.SalesSummaryReportListResult;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportResult;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportSpecification;

@Service
public class ReportsService extends Exportable
{
	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	@Autowired
	private LoginSessionData sessionData;

	private boolean configured = false;

	private String restServerUrl;

	@Autowired
	private AgentService agentService;


	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getReportsUrl();
			configured = true;
		}
	}

	@Override
	public GuiCsvResponse listAsCsv(UriComponentsBuilder uri, String filter, String search, long offset, IQueryStringParameters queryStringParams) throws Exception
	{
		return listAsCsv(uri, filter, search, offset, BatchUtility.getRecordsPerChunk(), queryStringParams);
	}

	@Override
	public GuiCsvResponse listAsCsv(UriComponentsBuilder uri, String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception
	{
		RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET);
	}

	private void addPeriodParams(UriComponentsBuilder uri, String fromDate, String toDate, String relTime)
	{
		if (fromDate != null )
			uri.queryParam("timeInterval.start", fromDate);
		if (toDate != null )
			uri.queryParam("timeInterval.end", toDate);
		if (relTime != null )
			uri.queryParam("relativeTimeRange.code", relTime);
	}

	public UriComponentsBuilder addRetailerPerformanceReportParams(String fromDate, String toDate, String relTime)
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/retailer_performance/adhoc/csv");
		addPeriodParams(uri, fromDate, toDate, relTime);
		return uri;
	}

	public RetailerPerformanceReportResult generateRetailerReport(String fromDate, String toDate, String relTime, String filter, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/retailer_performance/adhoc/json");
		addPeriodParams(uri, fromDate, toDate, relTime);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, RetailerPerformanceReportResult.class);
	}

	public RetailerPerformanceReportSpecification getRetailerReport(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/retailer_performance/"+id);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, RetailerPerformanceReportSpecification.class);
	}

	public int createRetailerReport(String name, String desc, String fromDate, String toDate, String relTime, String filter, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/retailer_performance");
		
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		addPeriodParams(uri, fromDate, toDate, relTime);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		RetailerPerformanceReportSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, RetailerPerformanceReportSpecification.class);
		return reportSpecification.getId();
	}

	public int updateRetailerReport(String id, String name, String desc, String fromDate, String toDate, String relTime, String filter, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/retailer_performance/" + id);
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		addPeriodParams(uri, fromDate, toDate, relTime);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		RetailerPerformanceReportSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, RetailerPerformanceReportSpecification.class);
		return reportSpecification.getId();
	}

	public void deleteRetailerReport(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/retailer_performance/" + id);
		restTemplate.execute(uri.build(true).toUri(), HttpMethod.DELETE, String.class);
	}

	public RetailerPerformanceReportListResult listRetailerReports(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/retailer_performance");
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, RetailerPerformanceReportListResult.class);
	}

	///////////////////////////////////////////////////////////////////////////

	public UriComponentsBuilder addWholesalerPerformanceReportParams(String fromDate, String toDate, String relTime)
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/wholesaler_performance/adhoc/csv");
		addPeriodParams(uri, fromDate, toDate, relTime);
		return uri;
	}

	public WholesalerPerformanceReportResult generateWholesalerReport(String fromDate, String toDate, String relTime, String filter, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/wholesaler_performance/adhoc/json");
		addPeriodParams(uri, fromDate, toDate, relTime);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, WholesalerPerformanceReportResult.class);
	}

	public WholesalerPerformanceReportSpecification getWholesalerReport(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/wholesaler_performance/"+id);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, WholesalerPerformanceReportSpecification.class);
	}

	public int createWholesalerReport(String name, String desc, String fromDate, String toDate, String relTime, String filter, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/wholesaler_performance");
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		addPeriodParams(uri, fromDate, toDate, relTime);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		WholesalerPerformanceReportSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, WholesalerPerformanceReportSpecification.class);
		return reportSpecification.getId();
	}

	public int updateWholesalerReport(String id, String name, String desc, String fromDate, String toDate, String relTime, String filter, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/wholesaler_performance/" + id);
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		addPeriodParams(uri, fromDate, toDate, relTime);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		WholesalerPerformanceReportSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, WholesalerPerformanceReportSpecification.class);
		return reportSpecification.getId();
	}

	public void deleteWholesalerReport(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/wholesaler_performance/" + id);
		restTemplate.execute(uri.build(true).toUri(), HttpMethod.DELETE, String.class);
	}

	public WholesalerPerformanceReportListResult listWholesalerReports(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/wholesaler_performance");
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, WholesalerPerformanceReportListResult.class);
	}

	///////////////////////////////////////////////////////////////////////////

	public SalesSummaryReportListResult listSalesSummaryReports(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/sales_summary");
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, SalesSummaryReportListResult.class);
	}

	///////////////////////////////////////////////////////////////////////////

	public UriComponentsBuilder addGroupSalesReportParams()
	{
		return UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_group_sales/adhoc/csv");
	}

	public DailyGroupSalesReportResult generateDailyGroupSalesReport(String filter, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_group_sales/adhoc/json");
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, DailyGroupSalesReportResult.class);
	}

	public DailyGroupSalesReportSpecification getDailyGroupSalesReport(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_group_sales/"+id);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, DailyGroupSalesReportSpecification.class);
	}

	public int createDailyGroupSalesReport(String name, String desc, String filter, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_group_sales");
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		DailyGroupSalesReportSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, DailyGroupSalesReportSpecification.class);
		return reportSpecification.getId();
	}

	public int updateDailyGroupSalesReport(String id, String name, String desc, String filter, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_group_sales/" + id);
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		DailyGroupSalesReportSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, DailyGroupSalesReportSpecification.class);
		return reportSpecification.getId();
	}

	public void deleteDailyGroupSalesReport(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_group_sales/" + id);
		restTemplate.execute(uri.build(true).toUri(), HttpMethod.DELETE, String.class);
	}

	public DailyGroupSalesReportListResult listDailyGroupSalesReports(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_group_sales");
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, DailyGroupSalesReportListResult.class);
	}

	///////////////////////////////////////////////////////////////////////////
	// Monthly Sales Performance Report.

	public UriComponentsBuilder addMonthlySalesPerformanceReportParams(String fromDate, String toDate, String relTime)
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/monthly_sales_performance/adhoc/csv");
		addPeriodParams(uri, fromDate, toDate, relTime);
		return uri;
	}

	public MonthlySalesPerformanceReportResult generateMonthlySalesPerformanceReport(String fromDate, String toDate, String relTime, String filter, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/monthly_sales_performance/adhoc/json");
		addPeriodParams(uri, fromDate, toDate, relTime);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, MonthlySalesPerformanceReportResult.class);
	}

	public MonthlySalesPerformanceReportSpecification getMonthlySalesPerformanceReport(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/monthly_sales_performance/"+id);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, MonthlySalesPerformanceReportSpecification.class);
	}

	public int createMonthlySalesPerformanceReport(String name, String desc, String filter, String sort, String period) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/monthly_sales_performance");
		addPeriodParams(uri, null, null, period);
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		MonthlySalesPerformanceReportSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, MonthlySalesPerformanceReportSpecification.class);
		return reportSpecification.getId();
	}

	// Create Performance Report by Area
	public DailyPerformanceByAreaSpecification getPerformanceReportByArea(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_performance_by_area/"+id);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, DailyPerformanceByAreaSpecification.class);
	}

	public int createSalesPerformanceReportByArea(String name, String desc, String relTime) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_performance_by_area");
		addPeriodParams(uri,null ,null, relTime);
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		DailyPerformanceByAreaSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.POST, DailyPerformanceByAreaSpecification.class);
		return reportSpecification.getId();
	}

	// Get Performance Reports by Area
	public DailyPerformanceByAreaListResult listPerformanceReportsByArea() throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_performance_by_area");
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, DailyPerformanceByAreaListResult.class);
	}
	public void deletePerformanceReportByArea(String id) throws Exception
	{
		//daily_sales_by_area || daily_performance_by_area
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_performance_by_area/" + id);
		restTemplate.execute(uri.build(true).toUri(), HttpMethod.DELETE, String.class);
	}
	public int updatePerformanceReportByArea(String id, String name, String desc, String filter, String sort, String relTime) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_performance_by_area/" + id);
		addPeriodParams(uri, null, null, relTime);
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		DailyPerformanceByAreaSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, DailyPerformanceByAreaSpecification.class);
		return reportSpecification.getId();
	}

	public int updateMonthlySalesPerformanceReport(String id, String name, String desc, String filter, String sort, String period) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/monthly_sales_performance/" + id);
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		addPeriodParams(uri, null, null, period);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		MonthlySalesPerformanceReportSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, MonthlySalesPerformanceReportSpecification.class);
		return reportSpecification.getId();
	}

	public void deleteMonthlySalesPerformanceReport(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/monthly_sales_performance/" + id);
		restTemplate.execute(uri.build(true).toUri(), HttpMethod.DELETE, String.class);
	}
	public void listDailySalesPerformanceByAreaReports(String search) throws Exception
	{
		/*
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/daily_sales_performance");
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Void.class);
		*/
	}

	public MonthlySalesPerformanceReportListResult listMonthlySalesPerformanceReports(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/monthly_sales_performance");
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, MonthlySalesPerformanceReportListResult.class);
	}
	
	public GuiMonthlySalesPerformanceReportSpecification convertToGuiMonthlySalesPerformanceReportSpecification(MonthlySalesPerformanceReportSpecification spec) throws Exception
	{
		GuiMonthlySalesPerformanceReportSpecification guiSpec = new GuiMonthlySalesPerformanceReportSpecification();
		List<? extends FilterItem<? extends Object>> filterItems = spec.getParameters().getFilter().getItems();
		for(FilterItem<? extends Object> item : filterItems)
		{
			FilterField field = item.getField();		
			switch(field)
			{
			case TIERS:
				guiSpec.setTiers((ArrayList<String>)item.getValue());
				break;
			case GROUPS:
				guiSpec.setGroups((ArrayList<String>)item.getValue());
				break;
			case OWNER_AGENTS:
				guiSpec.setOwnerAgents((ArrayList<String>)item.getValue());
				break;
			case AGENTS:
				guiSpec.setAgents((ArrayList<String>)item.getValue());
				break;
			case TRANSACTION_TYPES:
				guiSpec.setTransactionTypes((String)item.getValue());
				break;
			case TRANSACTION_STATUS:
				if(item.getValue() instanceof Boolean)
					guiSpec.setTransactionStatus((Boolean)item.getValue());
				break;

			default:
				break;
			}		
		}
		guiSpec.setName(spec.getName());
		guiSpec.setDescription(spec.getDescription());
		guiSpec.setId(spec.getId());
		guiSpec.setPeriod(spec.getParameters().getRelativeTimeRange().toString());
		return guiSpec;
	}

	///////////////////////////////////////////////////////////////////////////

	public AccountBalanceSummaryReportResult generateAccountBalanceSummaryReport(String filter, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/account_balance_summary/adhoc/json");
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AccountBalanceSummaryReportResult.class);
	}

	public UriComponentsBuilder addAccountBalanceSummaryReportParams()
	{
		return UriComponentsBuilder.fromHttpUrl(restServerUrl + "/account_balance_summary/adhoc/csv");
	}

	public AccountBalanceSummaryReportSpecification getAccountBalanceSummaryReport(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/account_balance_summary/"+id);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AccountBalanceSummaryReportSpecification.class);
	}

	public int createAccountBalanceSummaryReport(String name, String desc, String filter, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/account_balance_summary");
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		AccountBalanceSummaryReportSpecification reportSpecification = restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, AccountBalanceSummaryReportSpecification.class);
		return reportSpecification.getId();
	}

	public int updateAccountBalanceSummaryReport(String id, String name, String desc, String filter, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/account_balance_summary/" + id);
		uri.queryParam("name", URLEncoder.encode(name,"UTF-8").replaceAll("\\+", "%20"));
		uri.queryParam("description", URLEncoder.encode(desc,"UTF-8").replaceAll("\\+", "%20"));

		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);

		if (sort != null && !sort.isEmpty())
			RestRequestUtil.standardSorting(uri, sort);
		
		AccountBalanceSummaryReportSpecification reportSpecification
			= restTemplate.execute(uri.build(true).toUri(), HttpMethod.PUT, AccountBalanceSummaryReportSpecification.class);
		
		return reportSpecification.getId();
	}

	public void deleteAccountBalanceSummaryReport(String id) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/account_balance_summary/" + id);
		restTemplate.execute(uri.build(true).toUri(), HttpMethod.DELETE, String.class);
	}

	public AccountBalanceSummaryReportListResult listAccountBalanceSummaryReports(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/account_balance_summary");
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AccountBalanceSummaryReportListResult.class);
	}

	///////////////////////////////////////////////////////////////////////////

	public ExResultList<ReportSchedule> listSchedules(String reportType, String reportId) throws Exception
	{
		ParameterizedTypeReference<ExResultList<ReportSchedule>> type = new ParameterizedTypeReference<ExResultList<ReportSchedule>>() {};
		return restTemplate.execute(restServerUrl + "/" + reportType + "/" + reportId + "/schedule", HttpMethod.GET, type);
	}

	public ReportSchedule getSchedule(String reportType, String reportId, String scheduleId) throws Exception
	{
		return restTemplate.execute(restServerUrl + "/" + reportType + "/" + reportId + "/schedule/" + scheduleId, HttpMethod.GET, ReportSchedule.class);
	}

	public ReportSchedule createSchedule(String reportType, String reportId, ReportSchedule schedule) throws Exception
	{
		return restTemplate.putForObject(restServerUrl + "/" + reportType + "/" + reportId + "/schedule", schedule, ReportSchedule.class);
	}

	public ReportSchedule updateSchedule(String reportType, String reportId, String scheduleId, ReportSchedule schedule) throws Exception
	{
		return restTemplate.putForObject(restServerUrl + "/" + reportType + "/" + reportId + "/schedule/" + scheduleId, schedule, ReportSchedule.class);
	}

	public ExecuteScheduleResponse executeSchedule(String reportType, String reportId, String scheduleId, ExecuteScheduleRequest req) throws Exception
	{
		req.setSessionID(this.sessionData.getServerSessionID());
		return restTemplate.postForObject(restServerUrl + "/" + reportType + "/" + reportId + "/schedule/" + scheduleId + "/execute", req, ExecuteScheduleResponse.class);
	}

	public void deleteSchedule(String reportType, String reportId, String scheduleId) throws Exception
	{
		restTemplate.execute(restServerUrl + "/" + reportType + "/" + reportId + "/schedule/" + scheduleId, HttpMethod.DELETE, String.class);
	}


	public ExResultList<WebUser> listScheduleUsers(String reportType, String reportId, String scheduleId) throws Exception
	{
		ParameterizedTypeReference<ExResultList<WebUser>> type = new ParameterizedTypeReference<ExResultList<WebUser>>() {};
		return restTemplate.execute(restServerUrl + "/" + reportType + "/" + reportId + "/schedule/" + scheduleId, HttpMethod.GET, type);
	}

	public void linkScheduleUser(String reportType, String reportId, String scheduleId, String webUserId) throws Exception
	{
		restTemplate.execute(restServerUrl + "/" + reportType + "/" + reportId + "/schedule/" + scheduleId + "/web_user/" + webUserId, HttpMethod.PUT, String.class);
	}

	public void unlinkScheduleUser(String reportType, String reportId, String scheduleId, String webUserId) throws Exception
	{
		restTemplate.execute(restServerUrl + "/" + reportType + "/" + reportId + "/schedule/" + scheduleId + "/web_user/" + webUserId, HttpMethod.DELETE, String.class);
	}

	public Map<Integer, String> getAgentByMsisdnMap(Optional<String> type, Optional<String> query) throws Exception
	{
		Map<Integer, String> agentMap = new TreeMap<Integer,String>();
		Agent[] agents = null;

		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			agents = agentService.getAllAgentsByMsisdn(query.get());
		}
		else {
			return agentMap;
		}

		HashMap<String, String> stateMap = new HashMap<>();
		stateMap.put("A", "Active");
		stateMap.put("S", "Suspended");
		stateMap.put("D", "Deactivated");

		if (agents != null)
		{
			Arrays.asList(agents).forEach(agent ->{
				String state = stateMap.get(agent.getState());
				state = state == null ? "Unknown" : state;
				String s =
					"ID: " + String.valueOf(agent.getId()) +
					" | Mobile-Number: " + agent.getMobileNumber() +
					" | Full-name: " + agent.getFirstName() + " " + agent.getSurname() +
					" | " + state;
				// Example:
				//  ID: 1 | First-Name: Name Here | Active
				agentMap.put(agent.getId(), s);
			});
		}

		return agentMap;
	}
}

