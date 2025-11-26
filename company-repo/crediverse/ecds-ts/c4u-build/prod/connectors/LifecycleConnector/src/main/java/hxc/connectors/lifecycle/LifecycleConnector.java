package hxc.connectors.lifecycle;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.lifecycle.reporting.MembershipReport;
import hxc.connectors.lifecycle.reporting.MembershipReportData;
import hxc.connectors.lifecycle.reporting.SubscriptionReport;
import hxc.connectors.lifecycle.reporting.SubscriptionReportData;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.soap.ISubscriber;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.numberplan.INumberPlan;
import hxc.services.reporting.IReportingService;
import hxc.services.reporting.ReportParameters;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
import hxc.services.security.SupplierOnly;
import hxc.utils.calendar.DateTime;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.thread.TimedThread;
import hxc.utils.thread.TimedThread.TimedThreadType;

public class LifecycleConnector implements IConnector, ILifecycle
{
	final static Logger logger = LoggerFactory.getLogger(LifecycleConnector.class);

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private IDatabase database;
	private ICtrlConnector control;
	private IReportingService reporting;
	private TimedThread lifecycleWorkerThread;
	private TimedThread triggerWorkerThread;

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
		// Get Database
		database = esb.getFirstConnector(IDatabase.class);
		if (database == null)
			return false;

		// Needs Control Connector
		control = esb.getFirstConnector(ICtrlConnector.class);
		if (control == null)
			return false;

		reporting = esb.getFirstService(IReportingService.class);
		if (reporting == null)
			return false;

		reporting.addReport(new SubscriptionReport()
		{

			@Override
			public Collection<SubscriptionReportData> getReportData(ReportParameters parameters)
			{
				try (IDatabaseConnection con = database.getConnection(null))
				{
					List<SubscriptionReportData> subscriptions = con.selectList(SubscriptionReportData.class, "SELECT serviceID, variantID, serviceClass, count(*) as subscriptions " //
							+ "FROM lc_subscription " //
							+ "GROUP BY serviceID, variantID, serviceClass " //
							+ "ORDER BY 1,2,3;");

					ISoapConnector soap = esb.getFirstConnector(ISoapConnector.class);
					if (soap != null)
					{
						for (SubscriptionReportData subscription : subscriptions)
						{
							subscription.setServiceID(soap.getServiceName(subscription.getServiceID()));
						}
					}

					return subscriptions;
				}
				catch (Exception e)
				{
					logger.error("Failed to get report data", e.getMessage());
				}
				return null;
			}

		});

		reporting.addReport(new MembershipReport()
		{

			@Override
			public Collection<MembershipReportData> getReportData(ReportParameters parameters)
			{
				try (IDatabaseConnection con = database.getConnection(null))
				{
					List<MembershipReportData> memberships = con.selectList(MembershipReportData.class, "SELECT serviceID, variantID, serviceClass, memberServiceClass, count(*) as members " //
							+ "FROM lc_member " //
							+ "GROUP BY serviceID, variantID, serviceClass, memberServiceClass " //
							+ "ORDER BY 1,2,3,4;");

					ISoapConnector soap = esb.getFirstConnector(ISoapConnector.class);
					if (soap != null)
					{
						for (MembershipReportData membership : memberships)
						{
							membership.setServiceID(soap.getServiceName(membership.getServiceID()));
						}
					}

					return memberships;
				}
				catch (Exception e)
				{
					logger.error("Failed to get report data", e.getMessage());
				}
				return null;
			}

		});

		// Start Lifecycle Worker Thread
		lifecycleWorkerThread = new TimedThread("Lifecycle Worker Thread", config.pollingIntervalSeconds * 1000L, TimedThreadType.INTERVAL)
		{
			@Override
			public void action()
			{
				try
				{
					doLifecycleWork();
				}
				catch (Exception e)
				{
					logger.error("Lifecycle Worker Thread", e);
				}
			}
		};
		lifecycleWorkerThread.start();

		// Start Trigger Worker Thread
		triggerWorkerThread = new TimedThread("Lifecycle Trigger Worker Thread", config.pollingIntervalSeconds * 1000L, TimedThreadType.INTERVAL)
		{
			@Override
			public void action()
			{
				try
				{
					doTemporalTriggersWork();
				}
				catch (Exception e)
				{
					logger.error("Lifecycle Worker Thread", e);
				}
			}
		};
		triggerWorkerThread.start();

		// Log Information
		logger.info("Lifecycle Connector Started");

		return true;
	}

	@Override
	public void stop()
	{

		if (lifecycleWorkerThread != null)
		{
			try
			{
				// Stop the thread
				lifecycleWorkerThread.kill();

				// Wait for it to die
				lifecycleWorkerThread.join(200000);

			}
			catch (InterruptedException ex)
			{
				logger.warn("Lifecycle worker failed to stop", ex);
			}
			finally
			{
				lifecycleWorkerThread = null;
			}

		}

		if (triggerWorkerThread != null)
		{
			try
			{
				// Stop the thread
				triggerWorkerThread.kill();

				// Wait for it to die
				triggerWorkerThread.join(200000);

			}
			catch (InterruptedException ex)
			{
				logger.warn("Lifecycle worker interupted", ex);
			}
			finally
			{
				triggerWorkerThread = null;
			}

		}

		// try
		// {
		// Thread.sleep(10000);
		// }
		// catch (InterruptedException e)
		// {
		// }

		// Log Information
		logger.info("Lifecycle Connector Stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (LifecycleConfiguration) config;

		if (lifecycleWorkerThread != null)
			lifecycleWorkerThread.setWaitTime(this.config.pollingIntervalSeconds * 1000L);

		if (triggerWorkerThread != null)
			triggerWorkerThread.setWaitTime(this.config.pollingIntervalSeconds * 1000L);
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
		return database.isFit();
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Configuration
	//
	// /////////////////////////////////
	// @Perms(perms = { @Perm(name = "ViewHsxParameters", description = "View HsX Connector Parameters", category = "HsX", supplier = true),
	// @Perm(name = "ChangeHsxParameters", implies = "ViewHsxParameters", description = "Change HsX Connector Parameters", category = "HsX", supplier = true),
	// @Perm(name = "ViewHsxNotifications", description = "View HsX Connector Notifications", category = "HsX", supplier = true),
	// @Perm(name = "ChangeHsxNotifications", implies = "ViewHsxNotifications", description = "Change HsX Connector Notifications", category = "HsX", supplier = true) })

	@Perms(perms = { @Perm(name = "ChangeLifecycleParameters", implies = "ViewLifecycleParameters", description = "Change Lifecycle Parameters", category = "Lifecycle", supplier = true),
			@Perm(name = "ViewLifecycleParameters", description = "View Lifecycle Parameters", category = "Lifecycle", supplier = true),
			@Perm(name = "PerformMigration", description = "Perform NPC Migration", category = "Lifecycle", supplier = true), })
	public class LifecycleConfiguration extends ConfigurationBase
	{
		private int pollingIntervalSeconds = 60;
		private int maxBatchSize = 1000;
		public int maxAgeDays = 60;
		public int minRetryIntervalSeconds = 3600;

		@SupplierOnly
		public int getPollingIntervalSeconds()
		{
			check(esb, "ViewLifecycleParameters");
			return pollingIntervalSeconds;
		}

		@SupplierOnly
		public void setPollingIntervalSeconds(int pollingIntervalSeconds) throws ValidationException
		{
			check(esb, "ChangeLifecycleParameters");

			ValidationException.min(1, pollingIntervalSeconds);
			this.pollingIntervalSeconds = pollingIntervalSeconds;
		}

		@SupplierOnly
		public int getMaxBatchSize()
		{
			check(esb, "ViewLifecycleParameters");
			return maxBatchSize;
		}

		@SupplierOnly
		public void setMaxBatchSize(int maxBatchSize) throws ValidationException
		{
			check(esb, "ChangeLifecycleParameters");

			ValidationException.min(1, maxBatchSize);
			this.maxBatchSize = maxBatchSize;
		}

		@SupplierOnly
		public int getMaxAgeDays()
		{
			check(esb, "ViewLifecycleParameters");
			return maxAgeDays;
		}

		@SupplierOnly
		public void setMaxAgeDays(int maxAgeDays)
		{
			check(esb, "ChangeLifecycleParameters");
			this.maxAgeDays = maxAgeDays;
		}

		@SupplierOnly
		public int getMinRetryIntervalSeconds()
		{
			check(esb, "ViewLifecycleParameters");
			return minRetryIntervalSeconds;
		}

		@SupplierOnly
		public void setMinRetryIntervalSeconds(int minRetryIntervalSeconds)
		{
			check(esb, "ChangeLifecycleParameters");
			this.minRetryIntervalSeconds = minRetryIntervalSeconds;
		}

		@SupplierOnly
		public String MigrateNPC()
		{
			check(esb, "PerformMigration");
			String result = LifecycleConnector.this.migrateNPC();
			return result;
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
			return 637135914853154410L;
		}

		@Override
		public String getName(String languageCode)
		{
			return "Lifecycle Connector";
		}

		@Override
		public void validate() throws ValidationException
		{

		}

		@Override
		public void performUpdateNotificationSecurityCheck()
		{
		}

		@Override
		public void performGetNotificationSecurityCheck()
		{

		}

	};

	LifecycleConfiguration config = new LifecycleConfiguration();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ILifecycle Subscriptions
	//
	// /////////////////////////////////
	@Override
	public boolean isSubscribed(IDatabaseConnection database, ISubscriber subscriber, String serviceID, String variantID) throws SQLException
	{
		return getSubscription(database, subscriber, serviceID, variantID) != null;
	}

	@Override
	public boolean isSubscribed(IDatabaseConnection database, ISubscriber subscriber, String serviceID) throws SQLException
	{
		return getSubscriptions(database, subscriber, serviceID).length > 0;
	}

	@Override
	public ISubscription getSubscription(IDatabaseConnection database, ISubscriber subscriber, String serviceID, String variantID) throws SQLException
	{
		if (variantID == null)
			return database.select(Subscription.class, "where msisdn = %s and serviceID = %s limit 1", subscriber.getInternationalNumber(), serviceID);
		else
			return database.select(Subscription.class, "where msisdn = %s and serviceID = %s and variantID = %s", subscriber.getInternationalNumber(), serviceID, variantID);
	}

	@Override
	public ISubscription[] getSubscriptions(IDatabaseConnection database, ISubscriber subscriber, String serviceID) throws SQLException
	{
		List<Subscription> results = database.selectList(Subscription.class, "where msisdn = %s and serviceID = %s", subscriber.getInternationalNumber(), serviceID);
		return results.toArray(new ISubscription[0]);
	}

	@Override
	public ISubscription[] getSubscriptions(IDatabaseConnection database, ISubscriber subscriber) throws SQLException
	{
		List<Subscription> results = database.selectList(Subscription.class, "where msisdn = %s", subscriber.getInternationalNumber());
		return results.toArray(new ISubscription[0]);
	}

	@Override
	public boolean addSubscription(IDatabaseConnection database, ISubscriber subscriber, String serviceID, String variantID, int state, Date nextDateTime, Date... dates) throws SQLException
	{
		Subscription subscription = new Subscription();
		subscription.setMsisdn(subscriber.getInternationalNumber());
		subscription.setServiceID(serviceID);
		subscription.setVariantID(variantID);
		subscription.setServiceClass(subscriber.getServiceClass());
		subscription.setState(state);
		subscription.setNextDateTime(nextDateTime);

		if (dates != null && dates.length > 0)
		{
			int index = 0;
			for (Date date : dates)
			{
				switch (index++)
				{
					case 0:
						subscription.setDateTime1(date);
						break;
					case 1:
						subscription.setDateTime2(date);
						break;
					case 2:
						subscription.setDateTime3(date);
						break;
					case 3:
						subscription.setDateTime4(date);
						break;
				}
			}
		}

		if (subscription.getServiceClass() <= 0)
			throw new SQLException(String.format("Invalid Service Class %d", subscription.getServiceClass()));

		if (database.update(subscription) > 0)
			return true;

		database.insert(subscription);

		return false;
	}

	@Override
	public boolean adjustLifecycle(IDatabaseConnection database, ISubscriber subscriber, String serviceID, String variantID, Boolean isBeingProcessed, Date timeStamp) throws SQLException
	{
		boolean result = false;

		if (isBeingProcessed != null)
		{
			if (variantID == null || variantID.length() == 0)
				result |= database.executeNonQuery("update lc_subscription set beingProcessed = %s where msisdn = %s and serviceID = %s;", //
						isBeingProcessed, subscriber.getInternationalNumber(), serviceID) > 0;
			else
				result |= database.executeNonQuery("update lc_subscription set beingProcessed = %s where msisdn = %s and serviceID = %s and variantID = %s;", //
						isBeingProcessed, subscriber.getInternationalNumber(), serviceID, variantID) > 0;
		}

		if (timeStamp != null)
		{
			if (variantID == null || variantID.length() == 0)
				result |= database.executeNonQuery("update lc_subscription set ts = %s where msisdn = %s and serviceID = %s;", //
						timeStamp, subscriber.getInternationalNumber(), serviceID) > 0;
			else
				result |= database.executeNonQuery("update lc_subscription set ts = %s where msisdn = %s and serviceID = %s and variantID = %s;", //
						timeStamp, subscriber.getInternationalNumber(), serviceID, variantID) > 0;
		}

		return result;
	}

	@Override
	public boolean removeSubscription(IDatabaseConnection database, ISubscriber subscriber, String serviceID, String variantID) throws SQLException
	{
		return database.delete(Subscription.class, "where msisdn = %s and serviceID = %s and variantID = %s", subscriber.getInternationalNumber(), serviceID, variantID);
	}

	@Override
	public boolean removeSubscriptions(IDatabaseConnection database, ISubscriber subscriber, String serviceID) throws SQLException
	{
		return database.delete(Subscription.class, "where msisdn = %s and serviceID = %s", subscriber.getInternationalNumber(), serviceID);
	}

	@Override
	public boolean updateSubscription(IDatabaseConnection database, ISubscription subscription) throws SQLException
	{
		Subscription subs = new Subscription(subscription);

		if (subscription.getServiceClass() <= 0)
			throw new SQLException(String.format("Invalid Service Class %d", subscription.getServiceClass()));

		return database.update(subs) > 0;

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// ILifecycle Members
	//
	// /////////////////////////////////
	@Override
	public boolean addMember(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID, ISubscriber member) throws SQLException
	{
		Membership membership = database.select(Membership.class, "where msisdn = %s and serviceID = %s and variantID = %s and memberMsisdn = %s", owner.getInternationalNumber(), serviceID,
				variantID, member.getInternationalNumber());
		if (membership != null)
			return false;

		membership = new Membership();
		membership.setMsisdn(owner.getInternationalNumber());
		membership.setServiceID(serviceID);
		membership.setVariantID(variantID);
		membership.setServiceClass(owner.getServiceClass());
		membership.setMemberMsisdn(member.getInternationalNumber());
		membership.setMemberServiceClass(member.getServiceClass());
		database.insert(membership);

		return true;
	}

	@Override
	public boolean isMember(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID, ISubscriber member) throws SQLException
	{
		Membership membership = null;
		if (variantID == null)
			membership = database.select(Membership.class, "where msisdn = %s and serviceID = %s and memberMsisdn = %s limit 1", owner.getInternationalNumber(), serviceID,
					member.getInternationalNumber());
		else
			membership = database.select(Membership.class, "where msisdn = %s and serviceID = %s and variantID = %s and memberMsisdn = %s", owner.getInternationalNumber(), serviceID, variantID,
					member.getInternationalNumber());
		return membership != null;
	}

	@Override
	public boolean isMember(IDatabaseConnection database, ISubscriber owner, String serviceID, ISubscriber member) throws SQLException
	{
		Membership membership = database.select(Membership.class, "where msisdn = %s and serviceID = %s and memberMsisdn = %s limit 1", owner.getInternationalNumber(), serviceID,
				member.getInternationalNumber());
		return membership != null;
	}

	@Override
	public boolean isMember(IDatabaseConnection database, String serviceID, ISubscriber member) throws SQLException
	{
		Membership membership = database.select(Membership.class, "where serviceID = %s and memberMsisdn = %s limit 1", serviceID, member.getInternationalNumber());
		return membership != null;
	}

	@Override
	public boolean removeMember(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID, ISubscriber member) throws SQLException
	{
		if (variantID == null)
			return database.delete(Membership.class, "where msisdn = %s and serviceID = %s and memberMsisdn = %s", owner.getInternationalNumber(), serviceID, member.getInternationalNumber());
		else
			return database.delete(Membership.class, "where msisdn = %s and serviceID = %s and variantID = %s and memberMsisdn = %s", owner.getInternationalNumber(), serviceID, variantID,
					member.getInternationalNumber());
	}

	@Override
	public boolean removeMembers(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID) throws SQLException
	{
		if (variantID == null || variantID.length() == 0)
			return database.delete(Membership.class, "where msisdn = %s and serviceID = %s", owner.getInternationalNumber(), serviceID);
		else
			return database.delete(Membership.class, "where msisdn = %s and serviceID = %s and variantID = %s", owner.getInternationalNumber(), serviceID, variantID);
	}

	@Override
	public String[] getMembers(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID) throws SQLException
	{
		database.testCreateTable(Membership.class);

		String where = "select memberMsisdn from `lc_member` where msisdn = %s and serviceID = %s";
		int count = 0;
		Object[] params = new Object[3];
		params[count++] = owner.getInternationalNumber();
		params[count++] = serviceID;
		if (variantID != null)
		{
			params[count++] = variantID;
			where += "  and variantID = %s";
		}
		List<String> result = database.selectVector(String.class, where, java.util.Arrays.copyOf(params, count));
		return result.toArray(new String[0]);
	}

	@Override
	public boolean hasMembers(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID) throws SQLException
	{
		Membership membership = database.select(Membership.class, "where msisdn = %s and serviceID = %s and variantID = %s limit 1", owner.getInternationalNumber(), serviceID, variantID);
		return membership != null;
	}

	@Override
	public boolean hasMembers(IDatabaseConnection database, ISubscriber owner, String serviceID) throws SQLException
	{
		Membership membership = database.select(Membership.class, "where msisdn = %s and serviceID = %s limit 1", owner.getInternationalNumber(), serviceID);
		return membership != null;
	}

	@Override
	public String[] getOwners(IDatabaseConnection database, ISubscriber member, String serviceID) throws SQLException
	{
		database.testCreateTable(Membership.class);
		List<String> result = database.selectVector(String.class, "select msisdn from `lc_member` where memberMsisdn = %s and serviceID = %s", member.getInternationalNumber(), serviceID);
		return result.toArray(new String[0]);
	}

	@Override
	public boolean addQuota(IDatabaseConnection database, IMemberQuota quota) throws SQLException
	{
		MemberQuota memberQuota = new MemberQuota(quota);

		if (database.update(memberQuota) > 0)
			return true;

		database.insert(memberQuota);

		return false;
	}

	@Override
	public IMemberQuota[] getQuotas(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID, ISubscriber member) throws SQLException
	{
		String where = "where msisdn = %s and serviceID = %s";
		int count = 0;
		Object[] params = new Object[4];
		params[count++] = owner.getInternationalNumber();
		params[count++] = serviceID;

		if (variantID != null && variantID.length() > 0)
		{
			where += " and variantID = %s";
			params[count++] = variantID;
		}

		if (member != null)
		{
			where += " and memberMsisdn = %s";
			params[count++] = member.getInternationalNumber();
		}

		List<MemberQuota> result = database.selectList(MemberQuota.class, where + ";", java.util.Arrays.copyOf(params, count));

		return result.toArray(new IMemberQuota[result.size()]);
	}

	@Override
	public IMemberQuota getQuota(IDatabaseConnection database, ISubscriber owner, String serviceID, String variantID, ISubscriber member, String quotaID) throws SQLException
	{
		String where = "where msisdn = %s and serviceID = %s";
		int count = 0;
		Object[] params = new Object[5];
		params[count++] = owner.getInternationalNumber();
		params[count++] = serviceID;

		if (variantID != null && variantID.length() > 0)
		{
			where += " and variantID = %s";
			params[count++] = variantID;
		}

		if (member != null)
		{
			where += " and memberMsisdn = %s";
			params[count++] = member.getInternationalNumber();
		}

		where += " and quotaID = %s";
		params[count++] = quotaID;

		MemberQuota result = database.select(MemberQuota.class, where + " limit 1;", java.util.Arrays.copyOf(params, count));

		return result;
	}

	@Override
	public boolean updateQuota(IDatabaseConnection database, IMemberQuota quota) throws SQLException
	{
		MemberQuota memberQuota = new MemberQuota(quota);
		return database.update(memberQuota) > 0;
	}

	@Override
	public boolean removeQuota(IDatabaseConnection database, IMemberQuota quota) throws SQLException
	{
		MemberQuota memberQuota = new MemberQuota(quota);
		return database.delete(memberQuota);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Worker Threads
	//
	// /////////////////////////////////
	private void doLifecycleWork()
	{
		// Exit if not the Incumbent Database Server
		if (!control.isIncumbent(ICtrlConnector.DATABASE_ROLE))
		{
			logger.trace("Not Incumbent {}. Lifecycle doing dormant", ICtrlConnector.DATABASE_ROLE);
			return;
		}

		try (IDatabaseConnection db = database.getConnection(""))
		{
			while (control.isIncumbent(ICtrlConnector.DATABASE_ROLE))
			{
				// Read the top N Lifecycle Records which will trigger in the next M seconds
				logger.debug("Reading next {} subscriptions which will trigger within {} seconds", config.maxBatchSize, config.pollingIntervalSeconds);
				DateTime now = DateTime.getNow();
				DateTime timeLimit = now.addSeconds(config.pollingIntervalSeconds);
				DateTime minAge = now.addDays(-config.maxAgeDays);
				DateTime maxAge = now.addSeconds(-config.minRetryIntervalSeconds);

				List<Subscription> subscriptions = db.selectList(Subscription.class, //
						"where nextDateTime <= %s and (beingProcessed = 0 or ts > %s and ts < %s) order by nextDateTime limit %s;", //
						timeLimit, minAge, maxAge, config.maxBatchSize);
				logger.debug("Found {} subscription(s) which will trigger within {} seconds", subscriptions.size(), config.pollingIntervalSeconds);

				// Go wait if there are none
				if (subscriptions.size() == 0)
					break;

				// Process each subscription
				for (Subscription subscription : subscriptions)
				{
					// Calculate milliseconds to wait
					long millisToWait = subscription.getNextDateTime().getTime() - (new Date()).getTime();
					if (millisToWait > 100)
					{
						logger.trace("Waiting for {} ms", millisToWait);
						Thread.sleep(millisToWait);
					}

					// Mark as being processed
					subscription.setBeingProcessed(true);
					// db.update(subscription, "beingProcessed");
					db.executeNonQuery("update lc_subscription set beingProcessed = 1, ts = now() where msisdn = %s and serviceID = %s and variantID = %s;", //
							subscription.getMsisdn(), subscription.getServiceID(), subscription.getVariantID());

					// Dispatch
					logger.trace("Dispatching {}/{} lifecycle in state {}", //
							subscription.getServiceID(), subscription.getVariantID(), subscription.getState());
					esb.dispatch(subscription, null);
				}
			}

		}
		catch (Exception e)
		{
			logger.error("doLifecycleWork failed", e);
		}

	}

	private void doTemporalTriggersWork()
	{
		// Exit if not the Incumbent Database Server
		if (!control.isIncumbent(ICtrlConnector.DATABASE_ROLE))
		{
			logger.trace("Not Incumbent {}. Lifecycle doing dormant", ICtrlConnector.DATABASE_ROLE);
			return;
		}

		try (IDatabaseConnection db = database.getConnection(""))
		{
			while (control.isIncumbent(ICtrlConnector.DATABASE_ROLE))
			{
				// Read the top N Trigger Records which will trigger in the next M seconds
				logger.debug("Reading next {} temporal triggers which will trigger within {} seconds", config.maxBatchSize, config.pollingIntervalSeconds);

				DateTime now = DateTime.getNow();
				DateTime timeLimit = now.addSeconds(config.pollingIntervalSeconds);
				DateTime minAge = now.addDays(-config.maxAgeDays);
				DateTime maxAge = now.addSeconds(-config.minRetryIntervalSeconds);
				List<TemporalTrigger> triggers = db.selectList(TemporalTrigger.class, //
						"where nextDateTime <= %s and (beingProcessed = 0 or ts > %s and ts < %s) order by nextDateTime limit %s;", //
						timeLimit, minAge, maxAge, config.maxBatchSize);
				logger.debug("Found {} triggers(s) which will trigger within {} seconds", triggers.size(), config.pollingIntervalSeconds);

				// Go wait if there are none
				if (triggers.size() == 0)
					break;

				// Process each subscription
				for (TemporalTrigger trigger : triggers)
				{
					// Calculate milliseconds to wait
					long millisToWait = trigger.getNextDateTime().getTime() - (new Date()).getTime();
					if (millisToWait > 100)
					{
						logger.trace("Waiting for {} ms", millisToWait);
						Thread.sleep(millisToWait);
					}

					// Mark as being processed
					trigger.setBeingProcessed(true);
					// db.update(trigger, "beingProcessed");
					db.executeNonQuery("update lc_timetrigger set beingProcessed = 1, ts = now() where msisdnA = %s and serviceID = %s and variantID = %s;", //
							trigger.getMsisdnA(), trigger.getServiceID(), trigger.getVariantID());

					// Dispatch
					logger.trace("Dispatching {}/{} temporal trigger in state {}", //
							trigger.getServiceID(), trigger.getVariantID(), trigger.getState());
					esb.dispatch(trigger, null);
				}
			}

		}
		catch (Exception e)
		{
			logger.error("doTemporalTriggersWork error", e);
		}

	}

	@Override
	public boolean addTemporalTrigger(IDatabaseConnection database, ITemporalTrigger trigger) throws SQLException
	{
		TemporalTrigger temporalTrigger = new TemporalTrigger(trigger);

		if (database.update(temporalTrigger) > 0)
			return true;

		database.insert(temporalTrigger);

		return false;
	}

	@Override
	public ITemporalTrigger[] getTemporalTriggers(IDatabaseConnection database, String serviceID, String variantID, ISubscriber subscriberA, ISubscriber subscriberB, String keyValue)
			throws SQLException
	{
		String where = "where serviceID = %s";
		int count = 0;
		Object[] params = new Object[5];
		params[count++] = serviceID;

		if (variantID != null)
		{
			where += " and variantID = %s";
			params[count++] = variantID;
		}

		if (subscriberA != null)
		{
			where += " and MsisdnA = %s";
			params[count++] = subscriberA.getInternationalNumber();
		}

		if (subscriberB != null)
		{
			where += " and MsisdnB = %s";
			params[count++] = subscriberB.getInternationalNumber();
		}

		if (keyValue != null)
		{
			where += " and KeyValue = %s";
			params[count++] = keyValue;
		}

		List<TemporalTrigger> result = database.selectList(TemporalTrigger.class, where + " limit 1000;", java.util.Arrays.copyOf(params, count));

		return result.toArray(new ITemporalTrigger[result.size()]);
	}

	@Override
	public boolean updateTemporalTrigger(IDatabaseConnection database, ITemporalTrigger trigger) throws SQLException
	{
		TemporalTrigger temporalTrigger = new TemporalTrigger(trigger);
		return database.update(temporalTrigger) > 0;
	}

	@Override
	public boolean removeTemporalTrigger(IDatabaseConnection database, ITemporalTrigger trigger) throws SQLException
	{
		TemporalTrigger temporalTrigger = new TemporalTrigger(trigger);
		return database.delete(temporalTrigger);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private String migrateNPC()
	{
		// Get Number Plan
		INumberPlan numberPlan = esb.getFirstService(INumberPlan.class);
		if (numberPlan == null)
			return "Number Plan Service not found";

		// Get Migrations
		String[] legacyNumbers = numberPlan.getLegacyOnnetNumberRanges();
		if (legacyNumbers == null || legacyNumbers.length == 0)
			return "No Migrations Defined";

		// Connect Database
		int count = 0;
		try (IDatabaseConnection db = database.getConnection(null))
		{

			for (String legacyNumber : legacyNumbers)
			{
				String like = numberPlan.getNationalDailingCode() + legacyNumber.replace('x', '_').replace('X', '_');
				count += migrateNPC(numberPlan, db, like, Subscription.class, "msisdn");
				count += migrateNPC(numberPlan, db, like, Membership.class, "msisdn", "memberMsisdn");
				count += migrateNPC(numberPlan, db, like, TemporalTrigger.class, "msisdnA", "msisdnB");
			}

		}
		catch (Exception e)
		{
			logger.error("migrateNPC failed", e);
			return "Migration Failed";
		}

		return String.format("Updated %d Number(s)", count);
	}

	private <T> int migrateNPC(INumberPlan numberPlan, IDatabaseConnection db, String like, Class<T> table, String... columnNames) throws SQLException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException
	{
		StringBuilder whereClause = new StringBuilder("where ");
		boolean first = true;
		for (String columnName : columnNames)
		{
			if (first)
				first = false;
			else
				whereClause.append("or ");
			whereClause.append(String.format("%s like '%s' ", columnName, like));
		}
		whereClause.append("limit 1000");

		int count = 0;
		while (true)
		{
			List<T> records = db.selectList(table, whereClause.toString());
			if (records.size() == 0)
				break;

			for (T record : records)
			{
				int index = 0;
				Object[] newNumbers = new Object[columnNames.length];
				for (String columnName : columnNames)
				{
					Field msisdnField = table.getDeclaredField(columnName);
					msisdnField.setAccessible(true);
					newNumbers[index++] = numberPlan.getInternationalFormat((String) msisdnField.get(record));
				}
				count += db.update(record, columnNames, newNumbers);
			}
		}

		return count;
	}

}
