package hxc.connectors.snmp;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.Rendering;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.diagnostic.IDiagnosticsTransmitter;
import hxc.connectors.snmp.agent.ISnmpAgent;
import hxc.connectors.snmp.agent.SnmpAgent;
import hxc.connectors.snmp.components.SnmpStatusException;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.instrumentation.Metric;

// Program to convert MIB to java: http://www.mibble.org

public class SnmpConnector implements IConnector, ISnmpConnector
{
	final static Logger logger = LoggerFactory.getLogger(SnmpConnector.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private ISnmpAgent agent;
	private final int NUM_ALARMS = 9;
	private IDiagnosticsTransmitter diagnostics;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Metric Data
	//
	// /////////////////////////////////

	private AtomicLong alarmCounters[] = new AtomicLong[NUM_ALARMS];
	private Metric alarms = Metric.CreateGraph("Alarm Counter", 60000, "Units", "Job Failed", "Transaction Failed", "Deadline Exceeded", "Element Service Status", "Element Management Status",
			"Element Disposition", "Element Boundary Status", "Task Execution Status", "Threshold Status");

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConnector implementation
	//
	// /////////////////////////////////
	@Override
	public void initialise(IServiceBus esb)
	{
		this.esb = esb;
	}

	@Override
	public boolean start(String[] args)
	{
		logger.info("Starting SnmpConnector ...");

		diagnostics = esb.getFirstConnector(IDiagnosticsTransmitter.class);
		if (diagnostics == null)
		{
			logger.error("SnmpConnector.start: diagnostics == null -> fail");
			return false;
		}

		logger.trace("SnmpConnector.start: config = {}", this.config);
		logger.trace("SnmpConnector.start: config.nmcAddress1 = {}", this.config.nmcAddress1);
		try
		{
			logger.trace("Trying to create SnmpAgent ...");
			agent = new SnmpAgent(SnmpAgent.DEFAULT_ADDRESS, esb);
			agent.start();

			agent.setTargetAddress(0, this.config.nmcAddress1);
			agent.setTargetAddress(1, this.config.nmcAddress2);
			agent.setTargetAddress(2, this.config.nmcAddress3);
			agent.setTargetAddress(3, this.config.nmcAddress4);
			agent.setCommunity(this.config.community);
			agent.setRetries(this.config.retries);
			agent.setTimeout(this.config.timeoutMilliSeconds);
		}
		catch (Exception exc)
		{
			logger.error(exc.getMessage(), exc);
			return false;
		}

		for (int i = 0; i < NUM_ALARMS; i++)
		{
			alarmCounters[i] = new AtomicLong();
		}

		// Log Information
		logger.info("Snmp Connector Started");

		return true;
	}

	@Override
	public void stop()
	{
		if (agent != null)
		{
			agent.stop();
		}
		// Log Information
		logger.info("Snmp Connector Stopped");

	}

	@Override
	public IConfiguration getConfiguration()
	{
		logger.trace("SnmpConnector.getConfiguration: entry ...");
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		logger.trace("SnmpConnector.setConfiguration: entry ...");
		this.config = (SnmpConfiguration) config;
		// Configure the Agent to the new Values
		try
		{
			logger.trace("SnmpConnector.setConfiguration: Configuring agent ...");

			agent.setTargetAddress(0, this.config.nmcAddress1);
			agent.setTargetAddress(1, this.config.nmcAddress2);
			agent.setTargetAddress(2, this.config.nmcAddress3);
			agent.setTargetAddress(3, this.config.nmcAddress4);
			agent.setCommunity(this.config.community);
			agent.setRetries(this.config.retries);
			agent.setTimeout(this.config.timeoutMilliSeconds);
		}
		catch (Exception exception)
		{
			logger.error(exception.getMessage(), exception);
		}
	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException
	{
		return null;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		if (alarms != null)
			alarms.report(esb, (Object[]) alarmCounters);

		return agent != null && agent.isWorking();
	}

	@Override
	public IMetric[] getMetrics()
	{
		return new IMetric[] { alarms };
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	@Perms(perms = { @Perm(name = "ViewSnmpParameters", description = "View SNMP Parameters", category = "SNMP", supplier = true),
			@Perm(name = "ChangeSnmpParameters", implies = "ViewSnmpParameters", description = "Change SNMP Parameters", category = "SNMP", supplier = true) })
	public class SnmpConfiguration extends ConfigurationBase
	{
		private String nmcAddress1 = "127.0.0.1/16200";
		private String nmcAddress2 = "";
		private String nmcAddress3 = "";
		private String nmcAddress4 = "";
		private String community = "public";
		private int retries = 5;
		private int timeoutMilliSeconds = 1000;

		@Config(description = "Address if 1st NMC ", renderAs = Rendering.IPADDRESS)
		public String getNmcAddress1()
		{
			check(esb, "ViewSnmpParameters");
			return nmcAddress1;
		}

		private boolean isAddressValid(String address)
		{
			if (address == null || address.length() == 0)
				return true;
			int posfs = address.lastIndexOf("/");
			int port = Integer.parseInt(address.substring(posfs + 1));
			if (port < 1 || port > 65535)
				return false;
			else
				return true;
		}

		public void setNmcAddress1(String nmcAddress1) throws ValidationException
		{
			check(esb, "ChangeSnmpParameters");
			if (!isAddressValid(nmcAddress1))
				throw new ValidationException("NmcAddress1 address invalid (PORT must be between 1 and 65635)");
			this.nmcAddress1 = nmcAddress1;
		}

		@Config(description = "Address if 2nd NMC ", renderAs = Rendering.IPADDRESS)
		public String getNmcAddress2()
		{
			check(esb, "ViewSnmpParameters");
			return nmcAddress2;
		}

		public void setNmcAddress2(String nmcAddress2) throws ValidationException
		{
			check(esb, "ChangeSnmpParameters");
			if (!isAddressValid(nmcAddress2))
				throw new ValidationException("NmcAddress2 address invalid (PORT must be between 1 and 65635)");
			this.nmcAddress2 = nmcAddress2;
		}

		@Config(description = "Address if 3rd NMC ", renderAs = Rendering.IPADDRESS)
		public String getNmcAddress3()
		{
			check(esb, "ViewSnmpParameters");
			return nmcAddress3;
		}

		public void setNmcAddress3(String nmcAddress3) throws ValidationException
		{
			check(esb, "ChangeSnmpParameters");
			if (!isAddressValid(nmcAddress3))
				throw new ValidationException("nmcAddress3 address invalid (PORT must be between 1 and 65635)");
			this.nmcAddress3 = nmcAddress3;
		}

		@Config(description = "Address if 4th NMC ", renderAs = Rendering.IPADDRESS)
		public String getNmcAddress4()
		{
			check(esb, "ViewSnmpParameters");
			return nmcAddress4;
		}

		public void setNmcAddress4(String nmcAddress4) throws ValidationException
		{
			check(esb, "ChangeSnmpParameters");
			if (!isAddressValid(nmcAddress4))
				throw new ValidationException("nmcAddress4 address invalid (PORT must be between 1 and 65635)");
			this.nmcAddress4 = nmcAddress4;
		}

		public String getCommunity()
		{
			check(esb, "ViewSnmpParameters");
			return community;
		}

		public void setCommunity(String community)
		{
			check(esb, "ChangeSnmpParameters");
			this.community = community;
		}

		public int getRetries()
		{
			check(esb, "ViewSnmpParameters");
			return retries;
		}

		public void setRetries(int retries) throws ValidationException
		{
			check(esb, "ChangeSnmpParameters");

			ValidationException.min(0, retries);
			this.retries = retries;
		}

		public int getTimeoutMilliSeconds()
		{
			check(esb, "ViewSnmpParameters");
			return timeoutMilliSeconds;
		}

		public void setTimeoutMilliSeconds(int timeoutMilliSeconds) throws ValidationException
		{
			check(esb, "ChangeSnmpParameters");

			ValidationException.min(1, timeoutMilliSeconds);
			this.timeoutMilliSeconds = timeoutMilliSeconds;
		}

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return -3768891347681403851L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "SNMP Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

	};

	SnmpConfiguration config = new SnmpConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ISnmpConnector Implementation
	//
	// /////////////////////////////////

	// When a job has failed to execute
	@Override
	public void jobFailed(String element, IncidentSeverity severity, String description)
	{
		incident(TrapCodes.csJobFailed, element, severity, description);
		alarmCounters[0].incrementAndGet();
	}

	// When a transaction has failed
	@Override
	public void transactionFailed(String element, IncidentSeverity severity, String description)
	{
		incident(TrapCodes.csTransactionFailed, element, severity, description);
		alarmCounters[1].incrementAndGet();
	}

	// When a deadline has not been met and has exceeded
	@Override
	public void deadlineExceeded(String element, IncidentSeverity severity, String description)
	{
		incident(TrapCodes.csDeadlineExceeded, element, severity, description);
		alarmCounters[2].incrementAndGet();
	}

	// The current status of the element (Connector/Service)
	@Override
	public void elementServiceStatus(String element, IndicationState state, IncidentSeverity severity, String description)
	{
		indication(TrapCodes.csElementServiceStatus, element, state, severity, description);
		alarmCounters[3].incrementAndGet();
	}

	// The general management status of the element (Connector/Service)
	@Override
	public void elementManagementStatus(String element, IndicationState state, IncidentSeverity severity, String description)
	{
		indication(TrapCodes.csElementManagementStatus, element, state, severity, description);
		alarmCounters[4].incrementAndGet();
	}

	// If the element (Connector/Service) is out of place
	@Override
	public void elementDisposition(String element, IndicationState state, IncidentSeverity severity, String description)
	{
		indication(TrapCodes.csElementDisposition, element, state, severity, description);
		alarmCounters[5].incrementAndGet();
	}

	// When we reach a boundary/constraint for a particular element
	@Override
	public void elementBoundaryStatus(String element, IndicationState state, IncidentSeverity severity, String description)
	{
		indication(TrapCodes.csElementBoundaryStatus, element, state, severity, description);
		alarmCounters[6].incrementAndGet();
	}

	// The current status of the task being executed
	@Override
	public void taskExecutionStatus(String element, IndicationState state, IncidentSeverity severity, String description)
	{
		indication(TrapCodes.csTaskExecutionStatus, element, state, severity, description);
		alarmCounters[7].incrementAndGet();
	}

	// Information about any threshold information
	@Override
	public void thresholdStatus(String element, IndicationState state, IncidentSeverity severity, String description)
	{
		indication(TrapCodes.csThresholdStatus, element, state, severity, description);
		alarmCounters[8].incrementAndGet();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	// Creates an incident and sends it
	private void incident(TrapCodes code, String element, IncidentSeverity severity, String description)
	{
		try
		{
			logger.trace("SnmpConnector.incident: code = {}, element = {}, severity = {}, description  = {}", code, element, severity, description);
			
			// Sends the incident
			agent.incident(code, element, severity, description);

			// Logs the alarm
			severity(severity, element, description);
		}
		catch (SnmpStatusException e)
		{
			logger.error("SnmpConnector could not send Incident Trap: {}", e);
		}
	}

	// Creates an indication and sends it
	private void indication(TrapCodes code, String element, IndicationState state, IncidentSeverity severity, String description)
	{
		try
		{
			logger.trace("SnmpConnector.indication: code = {}, element = {}, state = {}, severity = {}, description  = {}", code, element, state, severity, description);
			
			// Sends the indication
			agent.indication(code, element, state, severity, description);

			// Log the alarm
			severity(severity, element, description);

			// Set the diagnostics
			if (diagnostics != null)
			{
				diagnostics.set("Alarms", element, severity == IncidentSeverity.CLEAR);
			}
		}
		catch (SnmpStatusException e)
		{
			logger.error("SnmpConnector could not send Indication Trap: {}", e);
		}
	}

	// Logs the alarm according to severity of alarm
	private void severity(IncidentSeverity severity, String element, String description)
	{
		String msg = String.format("Element: %s, Status: %s", element, description);
		switch (severity)
		{
			case CLEAR:
				logger.info(msg);
				break;
			case MINOR:
				logger.warn(msg);
			case MAJOR:
				logger.error(msg);
				break;
			case CRITICAL:
				logger.error(msg);
				break;
			case UNKNOWN:
				logger.info(msg);
				break;
		}
		logger.info(msg);
	}
}
