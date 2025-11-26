package hxc.services.ecds;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Role;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.utils.calendar.DateTime;

public class ScheduledAccountDumpProcessor implements Runnable
{
	private final static Logger logger = LoggerFactory.getLogger(ScheduledAccountDumpProcessor.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private ICreditDistribution context;
	private CompanyInfo company;
	private String directory;
	private ScheduledFuture<?> future;
	private ScheduledThreadPoolExecutor scheduledThreadPool;
	private DateTime next = DateTime.getNow();

	private static final int BATCH_SIZE = 50;
	private static final int SHORTEST_INTERVAL = 2; // Minutes

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	public ScheduledAccountDumpProcessor(ICreditDistribution context, CompanyInfo company)
	{
		this.context = context;
		this.company = company;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	public void start(ScheduledThreadPoolExecutor scheduledThreadPool)
	{
		this.scheduledThreadPool = scheduledThreadPool;

		future = scheduledThreadPool.scheduleAtFixedRate(this, 1, SHORTEST_INTERVAL, TimeUnit.MINUTES);
	}

	public void stop()
	{
		if (future != null)
		{
			future.cancel(true);
			future = null;
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Runnable
	//
	// /////////////////////////////////
	@Override
	public void run()
	{
		boolean testMode = false; // Set to true to force account dump to run
		// Get Time/Date info
		DateTime now = next;
		next = DateTime.getNow().addMinutes(SHORTEST_INTERVAL);
		DateTime today = now.getDatePart();
		DateTime tomorrow = today.addDays(1);

		// Get Config
		try (EntityManagerEx em = context.getEntityManager())
		{
			AgentsConfig config = company.getConfiguration(em, AgentsConfig.class);

			Boolean accountDumpIsEnabled = config.getScheduledAccountDumpEnabled();

			if (accountDumpIsEnabled == null || !accountDumpIsEnabled) return;

			// Save Parameters
			String compangNo = String.format("%03d", company.getCompany().getId());
			directory = config.getScheduledAccountDumpDirectory();
			String token = AgentsConfig.COMPANY_ID.replace("{", "\\{").replace("}", "\\}");
			directory = directory.replaceAll("(?i)" + token, compangNo);

			// Calculate the time to run next
			int minutes = config.getScheduledAccountDumpIntervalMinutes();
			if (minutes > 0 && minutes < SHORTEST_INTERVAL)
				minutes = SHORTEST_INTERVAL;
			Date startTimeOfDay = config.getScheduledAccountDumpStartTimeOfDay();
			if (startTimeOfDay == null)
				startTimeOfDay = new DateTime(2000, 1, 1, 0, 0, 0);
			long startSecondsPastMidnight = new DateTime(startTimeOfDay).getSecondsSinceMidnight();
			DateTime scheduled = today.addSeconds(startSecondsPastMidnight);
			while (scheduled.before(now))
			{
				scheduled = minutes <= 0 ? tomorrow : scheduled.addMinutes(minutes);
			}
			
			// New Day
			if (!scheduled.before(tomorrow))
				scheduled = tomorrow.addSeconds(startSecondsPastMidnight);

			if (!testMode) {
				// Exit if not before next poll
				if (!scheduled.before(next))
					return;
				
				// Exit if not incumbent
				if (!context.isMasterServer())
				{
					logger.trace("Not performing Account Dump - Not Master Server");
					return;
				}
			}
			

			
			
			PerformDumpRunnable runnable = new PerformDumpRunnable();
			runnable.configure(config);
			if (testMode) {
				runnable.run();
			}
			else {
				// Schedule the next run
				long delay_ms = scheduled.getTime() - new Date().getTime();
				
				scheduledThreadPool.schedule(runnable, delay_ms <= 1 ? 1 : delay_ms, TimeUnit.MILLISECONDS);
			}
		}
		catch (Exception ex)
		{
			logger.error("run error", ex);
		}
	}

	class PerformDumpRunnable implements Runnable
	{
		private boolean includeDeleted = false;
		private long activityValue = -1;
		public void configure(AgentsConfig config) {
			includeDeleted = config.isIncludedeleted();
			
			String activityScale = config.getActivityScale();
			int activityScaleValue = config.getActivityScaleValue();
			
			if (activityScaleValue > 0 && activityScale != null && !activityScale.equals("disabled")) {
				Calendar calendarNow	= Calendar.getInstance();
				Calendar calendarThen	= Calendar.getInstance();
				
				switch (activityScale) {
				case "days":
					calendarThen.add(Calendar.DATE, (activityScaleValue * -1));
					break;
				case "weeks":
					calendarThen.add(Calendar.DATE, (activityScaleValue * -7));
					break;
				case "months":
					calendarThen.add(Calendar.MONTH, (activityScaleValue * -1));
					break;
				case "years":
					calendarThen.add(Calendar.YEAR, (activityScaleValue * -1));
					break;
				}
				
				Date now	= calendarNow.getTime();
				Date then	= calendarThen.getTime();
				
				long	diffTime = now.getTime() - then.getTime();
				activityValue = diffTime / (1000 * 60 * 60 * 24);
			}
		}

		@Override
		public void run() {
			performDump();
		}
		
		// //////////////////////////////////////////////////////////////////////////////////////
		//
		// Perform Dump
		//
		// /////////////////////////////////
		void performDump()
		{
			int companyID = company.getCompany().getId();
			logger.info("Starting Scheduled Account Dump for Company {} to {}", companyID, directory);
			
			// Get Database
			try (EntityManagerEx em = context.getEntityManager())
			{
				// Create Folder
				File folder = new File(directory);
				folder.mkdirs();

				// Compile filename
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
				String tempFileName = String.format("%s_ecds_adump_%s.tmp", company.getCompany().getPrefix(), sdf.format(new Date()));
				File tempFile = new File(folder, tempFileName);

				// Create Writer
				CsvWriterSettings settings = new CsvWriterSettings();
				CsvWriter writer = new CsvWriter(tempFile, settings);

				// Write the record headers of this file
				writer.writeHeaders( //
						"account_number", //
						"msisdn", //
						"imei", //
						"imsi", //
						"title", //
						"first_name", //
						"initials", //
						"surname", //
						"gender", //
						"date_of_birth", //
						"street_address_line1", //
						"street_address_line2", //
						"street_address_suburb", //
						"street_address_city", //
						"email", //
						"tier", //
						"status", //
						"service_class", //
						"role", //
						"group", //
						"area", //
						"supplier", //
						"owner", //
						"monetary_balance", //
						"bonus_balance", //
						"creation_time", //
						"tamper", //
						"hold_account_balance");

				int lastId = 0;
				sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
				while (true)
				{
					List<Account> accounts = Account.findForDump(em, companyID, lastId, BATCH_SIZE, includeDeleted, activityValue);
					if (accounts == null || accounts.size() == 0)
						break;
					for (Account account : accounts)
					{
						lastId = account.getAgentID();
						// 121 Lazy Load Work Around
						try (EntityManagerEx em2 = context.getEntityManager())
						{
							Agent agent = Agent.findByID(em2, account.getAgentID(), companyID);
							writer.addValue("account_number", CsvExportProcessor.toText(agent.getAccountNumber()));
							writer.addValue("msisdn", agent.getMobileNumber());
							writer.addValue("imei", agent.getImei());
							writer.addValue("imsi", agent.getImsi());
							writer.addValue("title", CsvExportProcessor.toText(agent.getTitle()));
							writer.addValue("first_name", CsvExportProcessor.toText(agent.getFirstName()));
							writer.addValue("initials", CsvExportProcessor.toText(agent.getInitials()));
							writer.addValue("surname", CsvExportProcessor.toText(agent.getSurname()));
							writer.addValue("gender", agent.getGender());
							if (agent.getDateOfBirth() != null)
								writer.addValue("date_of_birth", CsvExportProcessor.toText(agent.getDateOfBirth()));
							writer.addValue("street_address_line1", CsvExportProcessor.toText(agent.getStreetAddressLine1()));
							writer.addValue("street_address_line2", CsvExportProcessor.toText(agent.getStreetAddressLine2()));
							writer.addValue("street_address_suburb", CsvExportProcessor.toText(agent.getStreetAddressSuburb()));
							writer.addValue("street_address_city", CsvExportProcessor.toText(agent.getStreetAddressCity()));
							writer.addValue("email", CsvExportProcessor.toText(agent.getEmail()));
							writer.addValue("tier", CsvExportProcessor.toText(agent.getTier().getName()));
							writer.addValue("status", agent.getState());
							{
								ServiceClass sc = agent.getServiceClass();
								if (sc != null)
									writer.addValue("service_class", CsvExportProcessor.toText(sc.getName()));
							}
							{
								Role role = agent.getRole();
								if (role != null)
									writer.addValue("role", CsvExportProcessor.toText(role.getName()));
							}
							{
								Group group = agent.getGroup();
								if (group != null)
									writer.addValue("group", CsvExportProcessor.toText(group.getName()));
							}
							{
								Area area = agent.getArea();
								if (area != null)
									writer.addValue("area", CsvExportProcessor.toText(area.getName()));
							}
							{
								Agent supplier = agent.getSupplier();
								if (supplier != null)
									writer.addValue("supplier", supplier.getMobileNumber());
							}
							{
								Agent owner = agent.getOwner();
								if (owner != null)
									writer.addValue("owner", owner.getMobileNumber());
							}
							writer.addValue("monetary_balance", CsvExportProcessor.toText(account.getBalance()));
							writer.addValue("bonus_balance", CsvExportProcessor.toText(account.getBonusBalance()));
							if (agent.getActivationDate() != null)
								writer.addValue("creation_time", sdf.format(agent.getActivationDate()));
							writer.addValue("tamper", CsvExportProcessor.toText(account.isTamperedWith()));
							
							writer.addValue("hold_account_balance", CsvExportProcessor.toText(account.getOnHoldBalance()));

							writer.writeValuesToRow();
						}
					}
				}

				writer.close();

				// Rename
				String csvFileName = tempFileName.replace(".tmp", ".csv");
				File csvFile = new File(folder, csvFileName);
				tempFile.renameTo(csvFile);

			}
			catch (Throwable tr)
			{
				logger.error(String.format("Error running Scheduled Account Dump for Company {}", companyID), tr);
				return;
			}

			logger.info("Completed Scheduled Account Dump for Company {}", companyID);

		}
		
	}
}
