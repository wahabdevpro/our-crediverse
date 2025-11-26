package cs.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cs.service.AnalyticsService;
import cs.service.ConfigurationService;

/*
 * Retaining Auto-wired imports as comments for potential future use -- see next comment
 *

import cs.dto.security.LoginSessionData;
import cs.service.ConfigurationService;
import cs.service.TypeConvertorService;

 */

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController 
{
	private static Logger logger = LoggerFactory.getLogger(AnalyticsController.class);

	@Autowired
	private ConfigurationService configService;
	
	/*
	 * Retaining Auto-wired content as comments for potential future use -- in this case, configuration, session, and typeConverter services
	 *

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private TypeConvertorService typeConvertorService;

	*/
	
	@Autowired
	private AnalyticsService analyticsService;
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Object> analyticsHome() throws Exception
	{
		logger.info("GUI Navigation // Analytics / Landing Page / GETting Configuration to confirm service is enabled");

		return configService.getAnalyticsConfig();
	}
	
	@RequestMapping(value = "salesCount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getSalesCount(@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate) throws Exception
	{
		logger.info("GUI Navigation // Analytics / Sales Count from " + startDate + " to " + endDate);
		
		String responseJson = analyticsService.getAnalyticsData(AnalyticsService.Type.SALES.urlCount(), startDate, endDate);
		return responseJson;
	}
	
	@RequestMapping(value = "transfersCount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getTransfersCount(@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate) throws Exception
	{
		logger.info("GUI Navigation // Analytics / Transfers Count from " + startDate + " to " + endDate);
		
		String responseJson = analyticsService.getAnalyticsData(AnalyticsService.Type.TRANSFERS.urlCount(), startDate, endDate);
		return responseJson;
	}
	
	//////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////
	
	@RequestMapping(value = "salesValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getSalesValue(@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate) throws Exception
	{
		logger.info("GUI Navigation // Analytics / Sales Value from " + startDate + " to " + endDate);
		
		String responseJson = analyticsService.getAnalyticsData(AnalyticsService.Type.SALES.urlValue(), startDate, endDate);
		return responseJson;
	}
	
	@RequestMapping(value = "transfersValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getTransfersValue(@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate) throws Exception
	{
		logger.info("GUI Navigation // Analytics / Transfers Value from " + startDate + " to " + endDate);
		
		String responseJson = analyticsService.getAnalyticsData(AnalyticsService.Type.TRANSFERS.urlValue(), startDate, endDate);
		return responseJson;
	}
	
	@RequestMapping(value = "replenishesValue", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getReplenishesValue(@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate) throws Exception
	{
		logger.info("GUI Navigation // Analytics / Replenishes Value from " + startDate + " to " + endDate);
		
		String responseJson = analyticsService.getAnalyticsData(AnalyticsService.Type.REPLENISHES.urlValue(), startDate, endDate);
		return responseJson;
	}
	
	//////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////
	
	@RequestMapping(value = "uniqueAgentsCount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getUniqueAgentsCount(@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate) throws Exception
	{
		logger.info("GUI Navigation // Analytics / Unique Agents Count from " + startDate + " to " + endDate);

		String responseJson = analyticsService.getAnalyticsData( AnalyticsService.Type.UNIQUE_AGENTS.urlCount(), startDate, endDate);
		return responseJson;
	}

}
