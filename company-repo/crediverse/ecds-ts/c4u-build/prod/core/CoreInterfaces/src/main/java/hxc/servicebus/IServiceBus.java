package hxc.servicebus;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.services.IService;
import hxc.utils.instrumentation.IMeasurement;
import hxc.utils.registration.IFacilityRegistration;
import hxc.utils.registration.IRegistration;

public interface IServiceBus
{
	// Starting and Stopping connectors and services
	public boolean start(String[] args);

	public void stop();

	// Connectors
	public <T> List<T> getConnectors(Class<T> cls);

	public <T> T getFirstConnector(Class<T> cls);

	// Services
	public <T> List<T> getServices(Class<T> cls);

	public <T> T getFirstService(Class<T> cls);

	// Dispatching
	public int dispatch(Object message, IConnection connection);

	public void addTrigger(ITrigger trigger);

	public void removeTrigger(ITrigger trigger);

	// Registration of connectors and services
	public void registerConnector(IConnector connector);

	public void registerService(IService service);

	// Unregisteration of connectors and services

	public void unregisterConnector(IConnector connector);

	public void unregisterService(IService service);

	public List<IPlugin> getRegisteredPlugins();

	public List<IPlugin> getStartedPlugins();

	// Threading
	public int getThreadQueueCapacity();

	public void setThreadQueueCapacity(int threadQueueCapacity);

	public int getMaxThreadPoolSize();

	public void setMaxThreadPoolSize(int maxThreadPoolSize);

	public int getCurrentTPS();

	public int getMaxTPS();

	public void setMaxTPS(int maxTPS);

	public int getMaxPeakTPS();

	public void setMaxPeakTPS(int maxPeakTPS);
	
	public  ThreadPoolExecutor getThreadPool();
	
	public ScheduledThreadPoolExecutor getScheduledThreadPool();

	// TPS
	public void countTPS();

	public int getConsecutiveLimiting();

	// Numbering
	public String getNextTransactionNumber(int length);

	// Locale
	public ILocale getLocale();

	// Resilience
	public IPlugin getCandidate(String serverRole);

	// Version
	public String getVersion();

	public String getBaseDirectory();

	// Metrics
	public void sendMeasurement(IMeasurement measurement);

	// Registeration
	public IFacilityRegistration hasFacility(String facilityID);

	public IRegistration getLastRegistration();

	// Shutdown Hook
	public void registerShutdown(IShutdown service);

	// UI Connector and Hxc(CLI) required PID
	public int getPID();
	
	// Sets the running indicator of the plugin framework
	public void setRunning(boolean running);
	
	// Other theads can check whether or not the ESB has completely started.
	public AtomicBoolean isRunning();	

	// Other theads can wait for the ESB to completely start.	
	public void waitForRunning() throws InterruptedException;
	public void waitForRunning(long timeout) throws InterruptedException;
}
