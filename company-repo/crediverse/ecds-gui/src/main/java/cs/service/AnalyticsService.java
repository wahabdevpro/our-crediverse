package cs.service;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

/*
 * Retaining Auto-wired imports as comments for potential future use -- see next comment
 *

import cs.config.ApplicationDetailsConfiguration;

 */

import cs.config.RestServerConfiguration;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.config.AnalyticsConfig;

@Service
public class AnalyticsService extends Exportable
{
	private static Logger logger = LoggerFactory.getLogger(AnalyticsService.class);
	
	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;
	
	/*
	 * Retaining Auto-wired content as comments for potential future use -- in this case, App Configuration 
	 * 

	@Autowired
	private ApplicationDetailsConfiguration appConfig;

	 */
	
	private boolean configured = false;

	private String restServerUrl;
	
	public enum Type {
		TRANSFERS("transfers"),
		UNIQUE_AGENTS("uniqueAgents"),
		SALES("sales"),
		REPLENISHES("replenishes");
		
		private String url;		
		Type(String url)			{ this.url = url;		}
		
		public String urlCount()	{ return url + "Count";	}		
		public String urlValue()	{ return url + "Value";	}
	}

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getAnalyticsUrl();
			configured = true;
		}
	}

	///
	///
	/// Receiver of REST calls :: hxc.services.ecds.rest.ServicesRestAnalytics
	///
	///
	
	public Map<String, Object> getConfig() throws Exception
	{
		logger.info("REST Request // Analytics / getConfig()");
		
		AnalyticsConfig ac					= restTemplate.execute(restServerUrl+ "/config", HttpMethod.GET, AnalyticsConfig.class);
		
		HashMap<String, Object> returnMap	= new HashMap<String, Object>();
		
		LocalTime scheduledTime = ac.getScheduledHistoryGenerationTimeOfDay();
		
		logger.trace("Time: {}", scheduledTime.toString());
		logger.trace("Enabled: {}", ac.isEnableAnalytics());
		
			// ----
			// ---- Purposeful conversion to string... configuration is displayed to the user as a String in the UI, no need for Date conversion on UI side when JSON is the return type
			// ----
		returnMap.put("scheduledHistoryGenerationTimeOfDayString", scheduledTime.toString());
		returnMap.put("enableAnalytics", ac.isEnableAnalytics());
		
		return returnMap; // Convert to GUI type so there is a BOOLEAN and STRING returned instead of BOOLEAN/LocalTime (which is invalid
	}
	
	public void setConfig(AnalyticsConfig updatedAnalyticsConfig) throws Exception
	{
		try {
				// --- Specifically note the suffix ...String for second parameter -- see cs.dto.config.GuiAnalyticsConfig.java 
			logger.info("REST Request // Analytics / setConfig( \"{ enabledAnalytics: true|false, scheduledHistoryGenerationTimeOfDayString: \"HH:mm:ss\" }\" )");
			
			restTemplate.execute(restServerUrl+ "/config", HttpMethod.PUT, updatedAnalyticsConfig, Void.class);
			
		} catch(Exception ex) {
			
			logger.error(ex.getMessage(), ex);
			
			throw ex;
		}
	}
	
	public String getAnalyticsData( String url, String startDate, String endDate) throws Exception
	{
		logger.info("REST Request // Analytics / " + url + " / between " + startDate + " and " + endDate);
		
		return "{ \"return\": " + restTemplate.execute(restServerUrl + "/" + url + "?startDate=" + startDate + "&endDate=" + endDate, HttpMethod.GET, String.class) + " }";
	}
}
