package hxc.services.ecds.rest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.config.AnalyticsConfig;
import hxc.services.ecds.Session;
import hxc.services.ecds.olapmodel.OlapAnalyticsHistory;
import hxc.services.ecds.olapmodel.OlapTransaction;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.FormatHelper;
import hxc.services.ecds.util.RuleCheckException;

@Path("/analytics")
public class ServicesRestAnalytics
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	
	final static Logger logger = LoggerFactory.getLogger(Sessions.class);
	
	public static final String QUERY_START_DATE = "startDate";
	public static final String QUERY_END_DATE = "endDate";
	
	public final int SUCCESS = 1;
	public final int FAILURE= 0;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Analytics Configuration
	//
	// /////////////////////////////////
	
	@GET
	@Path("/config")
	@Produces(MediaType.APPLICATION_JSON)
	public AnalyticsConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session			= context.getSession(sessionID);
			AnalyticsConfig ac		= context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, AnalyticsConfig.class);
			
			logger.info(	"GETting Analytics Configuration // " +
							"enabled: " + String.valueOf(ac.isEnableAnalytics()) + "; " +
							"scheduledRunTime: " + ac.getScheduledHistoryGenerationTimeOfDay().toString() + ";", AnalyticsConfig.class);
			
			return ac;
		}
		catch (RuleCheckException ex)
		{
			logger.error(ex.getMessage(), ex);
			
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PUT
	@Path("/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(
			AnalyticsConfig acConfig,
			@HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session			= context.getSession(sessionID);
			
			session.check(em, OlapTransaction.ANALYTICS_MAY_CONFIGURE);
			session.check(em, OlapAnalyticsHistory.MAY_CONFIGURE); 
			
			context.findCompanyInfoByID(session.getCompanyID()).setConfiguration(em, acConfig, session);
			
			logger.info(	"SETting Analytics Configuration // " +
							"enabled: " + String.valueOf(acConfig.isEnableAnalytics()) + "; " +
							"scheduledRunTime: " + acConfig.getScheduledHistoryGenerationTimeOfDay().toString() + ";", AnalyticsConfig.class);
		}
		catch (RuleCheckException ex)
		{
			logger.error(ex.getMessage(), ex);
			
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Unique Agents Count
	//
	// /////////////////////////////////
	@GET
	@Path("/uniqueAgentsCount")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Long> getUniqueAgentsCount(
			@HeaderParam(RestParams.SID) String sessionID,
			@QueryParam(ServicesRestAnalytics.QUERY_START_DATE) String startDate,
			@QueryParam(ServicesRestAnalytics.QUERY_END_DATE) String endDate)
	{
		return this.getAnalyticsHistory(sessionID, OlapAnalyticsHistory.QueryData.UACOUNT, startDate, endDate);

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Sales Count
	//
	// /////////////////////////////////
	@GET
	@Path("/salesCount")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Long> getSalesCount(
			@HeaderParam(RestParams.SID) String sessionID,
			@QueryParam(ServicesRestAnalytics.QUERY_START_DATE) String startDate,
			@QueryParam(ServicesRestAnalytics.QUERY_END_DATE) String endDate)
	{
		return this.getAnalyticsHistory(sessionID, OlapAnalyticsHistory.QueryData.SLCOUNT, startDate, endDate);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Transfers Value for TODAY/PERIOD
	//
	// /////////////////////////////////
	
	@GET
	@Path("/transfersCount")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Long> getTransfersCount(
			@HeaderParam(RestParams.SID) String sessionID,
			@QueryParam(ServicesRestAnalytics.QUERY_START_DATE) String startDate,
			@QueryParam(ServicesRestAnalytics.QUERY_END_DATE) String endDate)
	{
		return this.getAnalyticsHistory(sessionID, OlapAnalyticsHistory.QueryData.TXCOUNT, startDate, endDate);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Sales Value for TODAY/PERIOD
	//
	// /////////////////////////////////
	@GET
	@Path("/salesValue")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Long> getSalesValue(
			@HeaderParam(RestParams.SID) String sessionID,
			@QueryParam(ServicesRestAnalytics.QUERY_START_DATE) String startDate,
			@QueryParam(ServicesRestAnalytics.QUERY_END_DATE) String endDate)
	{
		return this.getAnalyticsHistory(sessionID, OlapAnalyticsHistory.QueryData.SLVALUE, startDate, endDate);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Transfers Value for TODAY/PERIOD
	//
	// /////////////////////////////////
	@GET
	@Path("/transfersValue")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Long> getTransfersValue(
			@HeaderParam(RestParams.SID) String sessionID,
			@QueryParam(ServicesRestAnalytics.QUERY_START_DATE) String startDate,
			@QueryParam(ServicesRestAnalytics.QUERY_END_DATE) String endDate)
	{
		return this.getAnalyticsHistory(sessionID, OlapAnalyticsHistory.QueryData.TXVALUE, startDate, endDate);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Replenishes Value for TODAY/PERIOD
	//
	// /////////////////////////////////
	@GET
	@Path("/replenishesValue")
	@Produces(MediaType.APPLICATION_JSON)
	public Map<String, Long> getReplenishesValue(
			@HeaderParam(RestParams.SID) String sessionID,
			@QueryParam(ServicesRestAnalytics.QUERY_START_DATE) String startDate,
			@QueryParam(ServicesRestAnalytics.QUERY_END_DATE) String endDate)
	{
		return this.getAnalyticsHistory(sessionID, OlapAnalyticsHistory.QueryData.RPVALUE, startDate, endDate);
	}
	
	private Map<String, Long> getAnalyticsHistory(
			String sessionID,
			OlapAnalyticsHistory.QueryData queryData,
			String startDate,
			String endDate)
	{
		try (EntityManagerEx em = context.getEntityManager(); EntityManagerEx apEm = context.getApEntityManager(); )
		{
			Session session = context.getSession(sessionID);
				// ----------
				// ---- Test Analytics Permissions
			session.check(em, OlapAnalyticsHistory.MAY_VIEW, "Not allowed to view historic Analytics data");
	        
			long resultValue = OlapTransaction.analyticsQuery(apEm, queryData);
	        
			List<OlapAnalyticsHistory> olapHistory	= OlapAnalyticsHistory.getAnalyticsHistory(apEm, queryData, startDate, endDate);
			LinkedHashMap<String, Long> returnMap	= new LinkedHashMap<String, Long>();
			
			for (int i = 0; i < olapHistory.size(); i++)
			{
				returnMap.put(olapHistory.get(i).getDate().toString(), olapHistory.get(i).getValue());
			}
			
			returnMap.put(FormatHelper.longDateDaysAgo(0), resultValue);
			
			return returnMap;
		}
		catch (Throwable ex)
		{
			logger.error(ex.getMessage(), ex);
			
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}
	
}
