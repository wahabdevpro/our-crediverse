package hxc.services.ecds;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.persistence.PersistenceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.snmp.ISnmpConnector;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.ecds.protocol.rest.config.AnalyticsConfig;
import hxc.ecds.protocol.rest.config.IConfiguration;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.ecds.olapmodel.OlapAnalyticsHistory;
import hxc.services.ecds.olapmodel.OlapTransaction;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.FormatHelper;
import hxc.services.ecds.util.IConfigurationChange;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import hxc.services.notification.IPhrase;

public class OlapAnalyticsHistoryProcessor implements Runnable, AutoCloseable, IConfigurationChange
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int HISTORY_DAYS	= 30; // This is the maximum days of HISTORY to update in the ap_analytics database
	
	final static Logger logger				= LoggerFactory.getLogger(OlapAnalyticsHistoryProcessor.class);
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ICreditDistribution context;
	private CompanyInfo company;
	private ScheduledFuture<?> future;
	private ISnmpConnector snmp;
	private IService service;
	private final Object futureMonitor = new Object();

	private int runStartCounter = 0;
	private int runCompleteCounter = 0;
	private final Object runCounterMonitor = new Object();
	
	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	private boolean isEnabled								= true;
	private LocalTime scheduledHistoryGenerationTimeOfDay	= LocalTime.parse("02:00");

	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public OlapAnalyticsHistoryProcessor(IServiceBus esb, ICreditDistribution context, IService service, CompanyInfo company)
	{
		this.context = context;
		this.company = company;
		this.service = service;
		
		this.company.registerForConfigurationChangeNotifications(this);
		this.snmp = esb.getFirstConnector(ISnmpConnector.class);
		this.scheduledThreadPoolExecutor = esb.getScheduledThreadPool();
	}
	
    private long computeNextDelay() 
    {
    	Date now						= new Date();
		long secondsOfDayToday			= now.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime().withNano(0).toSecondOfDay();
		long secondsOfDayConfig			= this.scheduledHistoryGenerationTimeOfDay.withNano(0).toSecondOfDay();
		
		long delaySeconds				= 0L;
		
		if ( secondsOfDayToday <= secondsOfDayConfig )
		{
			delaySeconds = secondsOfDayConfig - secondsOfDayToday;
		}
		else
		{
			//				24hours - ( <todaysRemainingSeconds> + <tomorrowSecondsFromStartOfDay> )
			//		e.g.	24hours - ( 6h30 + 2AM ) .... == 24hours - 8h 30min == run@ 17h 30m from now....
			delaySeconds = (86400L - secondsOfDayToday) + secondsOfDayConfig;
		}
        
        return delaySeconds;
    }
    
	private ScheduledFuture<?> schedule()
	{
		long initialDelay	= this.computeNextDelay();
		long period			= 24L * 60L * 60L;
		TimeUnit unit		= TimeUnit.SECONDS;
		
		logger.info("Scheduling with initialDelay = {}, period = {}, unit = {}", initialDelay, period, unit);
		
		this.future = this.scheduledThreadPoolExecutor.scheduleAtFixedRate(this, initialDelay, period, unit);
		
		return this.future;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public boolean waitForRunStartCount(int count, Long timeout) throws Exception
	{
		Long deadline = null;
		if (timeout != null)
		{
			if (timeout < 0)
				throw new IllegalArgumentException("timeout may not be negative");
			long start = System.nanoTime() / 1000 / 1000;
			deadline = start + timeout;
		}
		synchronized (this.runCounterMonitor)
		{
			while (this.runStartCounter < count)
			{
				if (deadline != null)
				{
					long now = (System.nanoTime() / 1000 / 1000);
					if (deadline <= now)
						break;
					long useTimeout = deadline - now;
					logger.trace("Waiting up to {} milliseconds for runCounterMonitor ...", useTimeout);
					this.runCounterMonitor.wait(useTimeout);
				}
				else
				{
					this.runCounterMonitor.wait();
				}
			}
			logger.trace("Returning !( {} < {} )", this.runStartCounter, count);
			return !(this.runStartCounter < count);
		}
	}
	
	public boolean waitForRunCompleteCount(int count, Long timeout) throws Exception
	{
		Long deadline = null;
		if (timeout != null)
		{
			if (timeout < 0)
				throw new IllegalArgumentException("timeout may not be negative");
			long start = System.nanoTime() / 1000 / 1000;
			deadline = start + timeout;
		}
		synchronized (this.runCounterMonitor)
		{
			while (this.runCompleteCounter < count)
			{
				if (deadline != null)
				{
					long now = (System.nanoTime() / 1000 / 1000);
					if (deadline <= now)
						break;
					long useTimeout = deadline - now;
					logger.trace("Waiting up to {} milliseconds for runCounterMonitor ...", useTimeout);
					this.runCounterMonitor.wait(useTimeout);
				}
				else
				{
					this.runCounterMonitor.wait();
				}
			}
			logger.trace("Returning !( {} < {} )", this.runCompleteCounter, count);
			return !(this.runCompleteCounter < count);
		}
	}
	
	public void start()
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			AnalyticsConfig acConfig			= company.getConfiguration(em, AnalyticsConfig.class);
			onConfigurationChanged(acConfig);
		}
		catch ( Throwable ex )
		{
			logger.error("Unable to start OlapAnalyticsHistoryProcessor");
			logger.error(ex.toString());
		}
	}
	
	private void restart()
	{
		logger.info("Restarting ...");
		
		synchronized (this.futureMonitor)
		{
			if (this.future != null)
			{
				this.future.cancel(true);
				this.future = null;
			}
			synchronized (this.runCounterMonitor)
			{
				this.runStartCounter = 0;
				this.runCompleteCounter = 0;
				this.runCounterMonitor.notify();
			}
			this.future = this.schedule();
		}
	}
	
	@Override
	public void close() throws Exception {
		this.stop();
	}

	public boolean stop()
	{
		logger.info("Stopping ...");
		
		synchronized (this.futureMonitor)
		{
			if (this.future != null)
			{
				this.future.cancel(true);
				this.future = null;
				this.futureMonitor.notifyAll();
				return true;
			}
			else
			{
				return false;
			}
		}
	}
	
	@Override
	public void run()
	{
		synchronized (this.runCounterMonitor)
		{
			this.runStartCounter++;
			this.runCounterMonitor.notify();
		}
		try (EntityManagerEx em = context.getEntityManager(); EntityManagerEx apEm = context.getApEntityManager())
		{
			logger.trace("(isEnabled and isTime) // Waking Up... ");
					
			for (OlapAnalyticsHistory.QueryData queryData: OlapAnalyticsHistory.QueryData.values())
			{
				logger.info("(Running) // History data processor: {} for the last 30 days... ", queryData);
					    
				List<OlapAnalyticsHistory> olapHistoryList	= OlapAnalyticsHistory.getAnalyticsHistory(apEm, queryData, FormatHelper.longDateDaysAgo(-HISTORY_DAYS), FormatHelper.longDateDaysAgo(-1));
				LinkedHashMap<String, Long> historyData		= new LinkedHashMap<String, Long>();

				for (int i = 0; i < olapHistoryList.size(); i++)
				{
					historyData.put(olapHistoryList.get(i).getDate().toString(), olapHistoryList.get(i).getValue());
				}
				        
				updateHistory(apEm, queryData, historyData);
				        
				logger.info("(Running) // Completed update for {}", queryData);
				logger.info("(Running)");
			}
			logger.info("(Finishing) // History data processor: Completed");
		}
		catch (Throwable throwable)
		{
			String msg = String.format("OlapAnalyticsHistoryProcessor: run failed with: %s", throwable);
			logger.error(msg, throwable);
			this.snmp.jobFailed(this.service.getConfiguration().getName(IPhrase.ENG), IncidentSeverity.MINOR, msg);
			return;
		}
		finally
		{
			synchronized (this.runCounterMonitor)
			{
				this.runCompleteCounter = this.runStartCounter;
				this.runCounterMonitor.notify();
			}
		}
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Analytics Update
	//
	// /////////////////////////////////
	
	private void updateHistory(
			EntityManagerEx apEm,
			OlapAnalyticsHistory.QueryData queryData,
			LinkedHashMap<String, Long> historyData) throws Exception
	{
		String displayTxType	= "";
		int updatedRecords		= 0;
		
	    for (int day = -HISTORY_DAYS; day < 0; day++)
		{
			OlapAnalyticsHistory oHistory	= new OlapAnalyticsHistory();
			long resultValue				= 0L;
			String longDate					= FormatHelper.longDateDaysAgo(day);
			
			if ( ! historyData.containsKey(longDate) )
			{
				updatedRecords += 1;
				
				resultValue = OlapTransaction.analyticsQuery(apEm, queryData, day);
		        
		        displayTxType = queryData.txTypeToString();
		        
				logger.info("(Running) // Updating {} for {}: for Date: {}; Found Value: {}", queryData.dataType(), displayTxType, longDate, resultValue );
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				oHistory.setDate(sdf.parse(longDate));
				
				oHistory.setTxType(displayTxType);
				oHistory.setDataType(queryData.dataType().toString());
				oHistory.setValue(resultValue);
				
				try (RequiresTransaction transaction = new RequiresTransaction(apEm))
				{
					apEm.persist(oHistory);
					transaction.commit();
				}
				catch (PersistenceException ex)
				{
					throw new RuleCheckException(ex, StatusCode.FAILED_TO_SAVE, null, ex.getMessage());
				}
			}
			oHistory		= (OlapAnalyticsHistory) null;
			resultValue		= 0L;
		}
		
		if (updatedRecords > 0) {
			logger.info("(Running) // [Updated] - {} records added (LIVE database was queried to update history database)", updatedRecords);
		} else {
			logger.info("(Running) // [Unchanged] - no missing records in history table (did not query LIVE database)");
		}
	}
	
	public LocalTime getScheduleTime()
	{
		return this.scheduledHistoryGenerationTimeOfDay;
	}

	public boolean isEnabled()
	{
		return this.isEnabled;
	}
	
	@Override
	public void onConfigurationChanged(IConfiguration configuration)
	{
		logger.info("Loading configuration ...");
		if (!(configuration instanceof AnalyticsConfig))
		{
			logger.trace("ignoring configuration change notification for {}", configuration);
			return;
		}
		
		AnalyticsConfig config						= (AnalyticsConfig) configuration;
		this.isEnabled								= config.isEnableAnalytics();
		this.scheduledHistoryGenerationTimeOfDay	= config.getScheduledHistoryGenerationTimeOfDay();
		
		if ( this.isEnabled )
		{
			this.restart();
		}
		else
		{
			this.stop();
		}
	}

}
