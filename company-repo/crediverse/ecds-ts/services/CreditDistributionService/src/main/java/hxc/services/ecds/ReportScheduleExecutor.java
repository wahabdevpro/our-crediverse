package hxc.services.ecds;

import static java.math.BigDecimal.ZERO;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import jakarta.activation.DataHandler;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.util.ByteArrayDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.snmp.ISnmpConnector;
import hxc.connectors.snmp.IncidentSeverity;
import hxc.ecds.protocol.rest.config.GeneralConfig;
import hxc.ecds.protocol.rest.config.IConfiguration;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.ReportingConfig;
import hxc.ecds.protocol.rest.reports.ExecuteScheduleResponse;
import hxc.ecds.protocol.rest.reports.SalesSummaryReportResultEntry;
import hxc.ecds.protocol.rest.util.DateHelper;
import hxc.ecds.protocol.rest.util.TimeInterval;
import hxc.servicebus.IServiceBus;
import hxc.services.IService;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.AgentUser;
import hxc.services.ecds.model.ReportSchedule;
import hxc.services.ecds.model.ReportSpecification;
import hxc.services.ecds.model.WebUser;
import hxc.services.ecds.reports.AccountBalanceSummaryReport;
import hxc.services.ecds.reports.AccountBalanceSummaryReportSpecification;
import hxc.services.ecds.reports.DailyGroupSalesReport;
import hxc.services.ecds.reports.DailyPerformanceByAreaReport;
import hxc.services.ecds.reports.DailyGroupSalesReportSpecification;
import hxc.services.ecds.reports.MonthlySalesPerformanceReport;
import hxc.services.ecds.reports.MonthlySalesPerformanceReportSpecification;
import hxc.services.ecds.reports.DailyPerformanceByAreaSpecification;
import hxc.services.ecds.reports.Report;
import hxc.services.ecds.reports.RetailerPerformanceReport;
import hxc.services.ecds.reports.RetailerPerformanceReportSpecification;
import hxc.services.ecds.reports.WholesalerPerformanceReport;
import hxc.services.ecds.reports.WholesalerPerformanceReportSpecification;
import hxc.services.ecds.reports.sales_summary.SalesSummaryCsvExportProcessor;
import hxc.services.ecds.reports.sales_summary.MobileMoneySummaryCsvExportProcessor;
import hxc.services.ecds.reports.sales_summary.SalesSummaryReport;
import hxc.services.ecds.reports.sales_summary.SalesSummaryReportSpecification;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.util.CustomCurrencyFormatter;
import hxc.services.ecds.util.EmailUtils;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.FormatHelper;
import hxc.services.ecds.util.IConfigurationChange;
import hxc.services.ecds.util.RequiresTransaction;
import hxc.services.ecds.util.StringExpander;
import hxc.services.notification.IPhrase;
import hxc.utils.calendar.DateTime;
import hxc.services.ecds.config.RestServerConfiguration;

public class ReportScheduleExecutor implements Runnable, AutoCloseable, IConfigurationChange

{
	private final static Logger logger = LoggerFactory.getLogger(ReportScheduleExecutor.class);

	private IServiceBus esb;
	private ICreditDistribution context;
	private IService service;
	private CompanyInfo companyInfo;

	private ISnmpConnector snmpConnector;
	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

	private ScheduledFuture<?> future = null;
	private final Object futureMonitor = new Object();

	private int runStartCounter = 0;
	private int runCompleteCounter = 0;
	private final Object runCounterMonitor = new Object();

	public ReportScheduleExecutor(ICreditDistribution context, CompanyInfo companyInfo) throws RuntimeException {
		this.context = context;
		this.esb = context.getServiceBus();
		this.service = context.getService();
		this.snmpConnector = context.getSnmpConnector();
		this.companyInfo = companyInfo;
		this.companyInfo.registerForConfigurationChangeNotifications(this);
		this.scheduledThreadPoolExecutor = esb.getScheduledThreadPool();
	}

	public boolean waitForRunStartCount(int count, Long timeout) throws Exception {
		Long deadline = null;
		if (timeout != null) {
			if (timeout < 0)
				throw new IllegalArgumentException("timeout may not be negative");
			long start = System.nanoTime() / 1000 / 1000;
			deadline = start + timeout;
		}
		synchronized (this.runCounterMonitor) {
			while (this.runStartCounter < count) {
				if (deadline != null) {
					long now = (System.nanoTime() / 1000 / 1000);
					if (deadline <= now)
						break;
					long useTimeout = deadline - now;
					logger.trace("Waiting up to {} milliseconds for runCounterMonitor ...", useTimeout);
					this.runCounterMonitor.wait(useTimeout);
				} else {
					this.runCounterMonitor.wait();
				}
			}
			logger.trace("Returning !( {} < {} )", this.runStartCounter, count);
			return !(this.runStartCounter < count);
		}
	}

	public boolean waitForRunCompleteCount(int count, Long timeout) throws Exception {
		Long deadline = null;
		if (timeout != null) {
			if (timeout < 0)
				throw new IllegalArgumentException("timeout may not be negative");
			long start = System.nanoTime() / 1000 / 1000;
			deadline = start + timeout;
		}
		synchronized (this.runCounterMonitor) {
			while (this.runCompleteCounter < count) {
				if (deadline != null) {
					long now = (System.nanoTime() / 1000 / 1000);
					if (deadline <= now)
						break;
					long useTimeout = deadline - now;
					logger.trace("Waiting up to {} milliseconds for runCounterMonitor ...", useTimeout);
					this.runCounterMonitor.wait(useTimeout);
				} else {
					this.runCounterMonitor.wait();
				}
			}
			logger.trace("Returning !( {} < {} )", this.runCompleteCounter, count);
			return !(this.runCompleteCounter < count);
		}
	}

	public void start() {
		try (EntityManagerEx entityManager = context.getEntityManager()) {
			ReportingConfig configuration = companyInfo.getConfiguration(entityManager, ReportingConfig.class);
			onConfigurationChanged(configuration);
		}
	}

	private void restart() {
		logger.info("Restarting ...");
		synchronized (this.futureMonitor) {
			if (this.future != null) {
				this.future.cancel(true);
				this.future = null;
			}
			synchronized (this.runCounterMonitor) {
				this.runStartCounter = 0;
				this.runCompleteCounter = 0;
				this.runCounterMonitor.notify();
			}
			this.future = this.schedule();
		}
	}

	/*
	 * private boolean startActual()
	 * {
	 * logger.info("Starting ...");
	 * synchronized (this.futureMonitor)
	 * {
	 * if (this.future == null)
	 * {
	 * synchronized (this.runCounterMonitor)
	 * {
	 * this.runStartCounter = 0;
	 * this.runCompleteCounter = 0;
	 * this.runCounterMonitor.notify();
	 * }
	 * this.future = this.schedule();
	 * this.futureMonitor.notifyAll();
	 * return true;
	 * }
	 * else
	 * {
	 * return false;
	 * }
	 * }
	 * }
	 */

	public boolean stop() {
		logger.info("Stopping ...");
		synchronized (this.futureMonitor) {
			if (this.future != null) {
				this.future.cancel(true);
				this.future = null;
				this.futureMonitor.notifyAll();
				return true;
			} else {
				return false;
			}
		}
	}

	@Override
	public void close() {
		this.stop();
	}

	public static class SchedulesProcessor {
		private IServiceBus esb;
		private ICreditDistribution context;
		private IService service;
		private CompanyInfo companyInfo;

		private ISnmpConnector snmpConnector;

		private ReportScheduleExecutor reportScheduleExecutor;
		private EmailUtils emailer = null;

		public int companyID;
		public Date referenceDate;
		public long secondsSinceMidnight;
		public DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS z");
		public ReportingConfig configuration;
		public long minSecondsAfterPeriod = 7 * 60;

		public boolean updateLastExecuted;

		public SchedulesProcessor(ICreditDistribution context, CompanyInfo companyInfo, Date referenceDate,
				boolean updateLastExecuted) {
			this.initializeFromContext(context);
			logger.trace("SchedulesProcessor.init ...");
			this.companyInfo = companyInfo;
			this.referenceDate = referenceDate;
			this.updateLastExecuted = updateLastExecuted;

			this.companyID = companyInfo.getCompany().getId();
			this.secondsSinceMidnight = new DateTime(referenceDate).getSecondsSinceMidnight();
			emailer = new EmailUtils(context);
		}

		public void initializeFromContext(ICreditDistribution context) {
			this.context = context;
			this.esb = context.getServiceBus();
			this.service = context.getService();
			this.snmpConnector = context.getSnmpConnector();
		}

		public void process() throws Exception {
			logger.trace("SchedulesProcessor.process: entry ...");
			try (
					EntityManagerEx em = context.getEntityManager();
					EntityManagerEx apEm = context.getApEntityManager();) {
				configuration = companyInfo.getConfiguration(em, ReportingConfig.class);
				logger.info("Processing schedules for company {} with referenceDate = {} and secondsSinceMidnight = {}",
						companyID, dateFormat.format(referenceDate), secondsSinceMidnight);
				List<ReportSchedule> readySchedules = ReportSchedule.findReady(em, companyID, referenceDate);
				for (ReportSchedule schedule : readySchedules) {
					if (reportScheduleExecutor != null && reportScheduleExecutor.future == null) {
						logger.info("is stopped: bailing ...");
						return;
					}
					this.process(em, apEm, schedule, false);
				}
			} finally {
				logger.trace("SchedulesProcessor.process: end ...");
			}
		}

		public ExecuteScheduleResponse.NotExecutedReason process(EntityManagerEx em, EntityManagerEx apEm,
				ReportSchedule schedule, boolean rethrow) throws Exception {
			// rethrow: when false it is executed by reports scheduler and will trigger SNMP
			// alarm on error.
			// rethrow: when true it is executed by GUI which needs to know about errors.
			if (configuration == null)
				configuration = companyInfo.getConfiguration(em, ReportingConfig.class);
			logger.info("Processing schedule {}", schedule);
			if (schedule.getTimeOfDay() != null && secondsSinceMidnight < schedule.getTimeOfDay()) {
				logger.info("Skipping schedule (id={}) because secondsSinceMidnight({}) < schedule.timeOfDay({})",
						schedule.getId(), secondsSinceMidnight, schedule.getTimeOfDay());
				return ExecuteScheduleResponse.NotExecutedReason.BEFORE_TIME_OF_DAY;
			}
			if (schedule.getStartTimeOfDay() != null && secondsSinceMidnight < schedule.getStartTimeOfDay()) {
				logger.info("Skipping schedule (id={}) because secondsSinceMidnight({}) < schedule.startTimeOfDay({})",
						schedule.getId(), secondsSinceMidnight, schedule.getStartTimeOfDay());
				return ExecuteScheduleResponse.NotExecutedReason.BEFORE_START_TIME_OF_DAY;
			}
			if (schedule.getEndTimeOfDay() != null && secondsSinceMidnight > schedule.getEndTimeOfDay()) {
				logger.info("Skipping schedule (id={}) because secondsSinceMidnight({}) > schedule.endTimeOfDay({})",
						schedule.getId(), secondsSinceMidnight, schedule.getEndTimeOfDay());
				return ExecuteScheduleResponse.NotExecutedReason.AFTER_END_TIME_OF_DAY;
			}

			CustomCurrencyFormatter ccf = null;
			if (configuration.getSmsConfigurationEnabled()) {
				ccf = CustomCurrencyFormatter.fromReportingConfig(configuration);
			}

			return new ScheduleProcessor(em, apEm, schedule, ccf).process(rethrow);
		}

		public class ScheduleProcessor {
			ReportSchedule schedule;
			int specificationID;
			ReportSpecification specification;
			Report.Type type;
			EntityManagerEx em;
			EntityManagerEx apEm;
			CustomCurrencyFormatter customCurrencyFormatter;

			public ScheduleProcessor(EntityManagerEx em, EntityManagerEx apEm, ReportSchedule schedule,
					CustomCurrencyFormatter ccf) {
				this.schedule = schedule;
				this.em = em;
				this.apEm = apEm;
				this.customCurrencyFormatter = ccf;
				specificationID = schedule.getReportSpecificationID();
				specification = ReportSpecification.findByID(em, specificationID, companyID);
				logger.trace("ScheduleProcessor.init: specification  = {}", specification);
				type = Report.Type.valueOf(specification.getType());
				logger.trace("ScheduleProcessor.init: type {} -> {}", specification.getType(), type);
			}

			public ExecuteScheduleResponse.NotExecutedReason process(boolean rethrow) throws Exception {
				ExecuteScheduleResponse.NotExecutedReason result = null;
				try {
					switch (type) {
						case RETAILER_PERFORMANCE:
							RetailerPerformanceReportSpecification retailerPerformanceReportSpecification = new RetailerPerformanceReportSpecification(
									specification);
							result = process(retailerPerformanceReportSpecification, rethrow);
							break;
						case WHOLESALER_PERFORMANCE:
							WholesalerPerformanceReportSpecification wholesalerPerformanceReportSpecification = new WholesalerPerformanceReportSpecification(
									specification);
							result = process(wholesalerPerformanceReportSpecification, rethrow);
							break;
						case SALES_SUMMARY:
							SalesSummaryReportSpecification salesSummaryReportSpecification = new SalesSummaryReportSpecification(
									specification);
							result = process(salesSummaryReportSpecification, rethrow);
							break;
						case DAILY_GROUP_SALES:
							DailyGroupSalesReportSpecification dailyGroupSalesReportSpecification = new DailyGroupSalesReportSpecification(
									specification);
							result = process(dailyGroupSalesReportSpecification, rethrow);
							break;
						case ACCOUNT_BALANCE_SUMMARY:
							AccountBalanceSummaryReportSpecification accountBalanceSummaryReportSpecification = new AccountBalanceSummaryReportSpecification(
									specification);
							result = process(accountBalanceSummaryReportSpecification, rethrow);
							break;
						case MONTHLY_SALES_PERFORMANCE:
							MonthlySalesPerformanceReportSpecification monthlySalesPerformanceReportSpecification = new MonthlySalesPerformanceReportSpecification(
									specification);
							result = process(monthlySalesPerformanceReportSpecification, rethrow);
							break;
						case DAILY_PERFORMANCE_BY_AREA:
							DailyPerformanceByAreaSpecification dailyPerformanceByAreaSpecification = new DailyPerformanceByAreaSpecification(
									specification);
							result = process(dailyPerformanceByAreaSpecification, rethrow);
							break;
					}
					if (result == null) {
						if (updateLastExecuted) {
							logger.info(
									"ScheduleProcessor.process: updating lastExecuted as updateLastExecuted == true");
							try (RequiresTransaction transaction = new RequiresTransaction(em)) {
								schedule.setLastExecuted(referenceDate);
								em.persist(schedule);
								transaction.commit();
							}
						} else {
							logger.info(
									"ScheduleProcessor.process: not updating lastExecuted as updateLastExecuted == false");
						}
					}
				} catch (Throwable throwable) {
					logger.error("ScheduleProcessor.process failed", throwable);
					if (rethrow)
						throw throwable;
					else
						snmpConnector.jobFailed(service.getConfiguration().getName(IPhrase.ENG),
								IncidentSeverity.CRITICAL,
								String.format("ReportScheduleExecutor: run failed with: %s", throwable));
				}
				return result;
			}

			public boolean checkCurrentSecondsAfterPeriod(TimeInterval timeInterval, Date referenceDate) {
				logger.info("checkCurrentSecondsAfterPeriod( timeInterval = {}, referenceDate = {} )", timeInterval,
						dateFormat.format(referenceDate));
				if (timeInterval != null && timeInterval.getEndDate() != null
						&& referenceDate.getTime() > timeInterval.getEndDate().getTime()) {
					long currentSecondsAfterPeriod = (referenceDate.getTime() - timeInterval.getEndDate().getTime())
							/ 1000L;
					boolean belowMinSecondsAfterPeriod = (currentSecondsAfterPeriod < minSecondsAfterPeriod);
					logger.info(
							"minSecondsAfterPeriod = {}, currentSecondsAfterPeriod = {}, belowMinSecondsAfterPeriod = {}",
							minSecondsAfterPeriod, currentSecondsAfterPeriod, belowMinSecondsAfterPeriod);
					if (belowMinSecondsAfterPeriod) {
						logger.info(
								"Not processing reports as it is currently too early in the period (timeInterval = {}, referenceDate = {}) ( minSecondsAfterPeriod = {}, currentSecondsAfterPeriod = {}, belowMinSecondsAfterPeriod = {} )",
								timeInterval, dateFormat.format(referenceDate), minSecondsAfterPeriod,
								currentSecondsAfterPeriod, belowMinSecondsAfterPeriod);
						return false;
					}
				}
				return true;
			}

			// MARK 00000000

			public ExecuteScheduleResponse.NotExecutedReason process(
					RetailerPerformanceReportSpecification retailerPerformanceReportSpecification, boolean rethrow)
					throws Exception {
				logger.info("Processing specification {}", retailerPerformanceReportSpecification);
				RetailerPerformanceReport.Processor processor = new RetailerPerformanceReport.Processor(apEm,
						retailerPerformanceReportSpecification.getAgentID(),
						retailerPerformanceReportSpecification.getParameters(), referenceDate);
				if (checkCurrentSecondsAfterPeriod(processor.getTimeInterval(), referenceDate) == false)
					return ExecuteScheduleResponse.NotExecutedReason.BELOW_MINIMUM_SECONDS_AFTER_PERIOD;

				List<RetailerPerformanceReport.ResultEntry> entries = processor.entries(-1, -1);
				RetailerPerformanceReport.CsvExportProcessor csvExportProcessor = new RetailerPerformanceReport.CsvExportProcessor(
						0);
				logger.trace("csvExportProcessor = {}, processor = {}", csvExportProcessor, processor);
				this.sendEmail(retailerPerformanceReportSpecification, processor.getTimeInterval(),
						processor.getRelativeTimeRange(), csvExportProcessor, entries,
						configuration.getCompressRetailerPerformanceReportEmail(), rethrow);
				return null;
			}

			public ExecuteScheduleResponse.NotExecutedReason process(
					WholesalerPerformanceReportSpecification wholesalerPerformanceReportSpecification, boolean rethrow)
					throws Exception {
				logger.info("Processing specification {}", wholesalerPerformanceReportSpecification);
				WholesalerPerformanceReport.Processor processor = new WholesalerPerformanceReport.Processor(apEm,
						wholesalerPerformanceReportSpecification.getAgentID(),
						wholesalerPerformanceReportSpecification.getParameters(), referenceDate);
				if (checkCurrentSecondsAfterPeriod(processor.getTimeInterval(), referenceDate) == false)
					return ExecuteScheduleResponse.NotExecutedReason.BELOW_MINIMUM_SECONDS_AFTER_PERIOD;

				List<WholesalerPerformanceReport.ResultEntry> entries = processor.entries(-1, -1);
				WholesalerPerformanceReport.CsvExportProcessor csvExportProcessor = new WholesalerPerformanceReport.CsvExportProcessor(
						0);
				logger.trace("csvExportProcessor = {}, processor = {}", csvExportProcessor, processor);
				this.sendEmail(wholesalerPerformanceReportSpecification, processor.getTimeInterval(),
						processor.getRelativeTimeRange(), csvExportProcessor, entries,
						configuration.getCompressWholesalerPerformanceReportEmail(), rethrow);
				return null;
			}

			public ExecuteScheduleResponse.NotExecutedReason process(
					DailyGroupSalesReportSpecification dailyGroupSummaryReportSpecification, boolean rethrow)
					throws Exception {
				logger.info("Processing specification {}", dailyGroupSummaryReportSpecification);
				DailyGroupSalesReport.Processor processor = new DailyGroupSalesReport.Processor(em, apEm, companyID,
						dailyGroupSummaryReportSpecification.getParameters(), referenceDate);
				if (checkCurrentSecondsAfterPeriod(processor.getTimeInterval(), referenceDate) == false)
					return ExecuteScheduleResponse.NotExecutedReason.BELOW_MINIMUM_SECONDS_AFTER_PERIOD;

				List<DailyGroupSalesReport.ResultEntry> entries = processor.entries(-1, -1);
				DailyGroupSalesReport.CsvExportProcessor csvExportProcessor = new DailyGroupSalesReport.CsvExportProcessor(
						0);
				logger.trace("csvExportProcessor = {}, processor = {}", csvExportProcessor, processor);
				this.sendEmail(dailyGroupSummaryReportSpecification, processor.getTimeInterval(),
						processor.getRelativeTimeRange(), csvExportProcessor, entries,
						configuration.getCompressDailyGroupSalesReportEmail(), rethrow);
				return null;
			}

			public ExecuteScheduleResponse.NotExecutedReason process(
					DailyPerformanceByAreaSpecification dailyPerformanceByAreaSpecification, boolean rethrow)
					throws Exception {
				logger.info("Processing specification {}", dailyPerformanceByAreaSpecification);
				DailyPerformanceByAreaReport.Processor processor = new DailyPerformanceByAreaReport.Processor(em, apEm,
						companyID, dailyPerformanceByAreaSpecification.getParameters(), referenceDate);
				if (checkCurrentSecondsAfterPeriod(processor.getTimeInterval(), referenceDate) == false)
					return ExecuteScheduleResponse.NotExecutedReason.BELOW_MINIMUM_SECONDS_AFTER_PERIOD;

				List<DailyPerformanceByAreaReport.ResultEntry> entries = processor.entries(-1, -1);
				DailyPerformanceByAreaReport.CsvExportProcessor csvExportProcessor = new DailyPerformanceByAreaReport.CsvExportProcessor(
						0);
				logger.trace("csvExportProcessor = {}, processor = {}", csvExportProcessor, processor);
				this.sendEmail(dailyPerformanceByAreaSpecification, processor.getTimeInterval(),
						processor.getRelativeTimeRange(), csvExportProcessor, entries,
						configuration.getCompressDailySalesReportByAreaEmail(), rethrow);
				return null;
			}

			public ExecuteScheduleResponse.NotExecutedReason process(
					AccountBalanceSummaryReportSpecification accountBalanceSummaryReportSpecification, boolean rethrow)
					throws Exception {
				logger.info("Processing specification {}", accountBalanceSummaryReportSpecification);
				AccountBalanceSummaryReport.Processor processor = new AccountBalanceSummaryReport.Processor(em, apEm,
						companyID, accountBalanceSummaryReportSpecification.getAgentID(),
						accountBalanceSummaryReportSpecification.getParameters(), referenceDate);
				if (checkCurrentSecondsAfterPeriod(processor.getTimeInterval(), referenceDate) == false)
					return ExecuteScheduleResponse.NotExecutedReason.BELOW_MINIMUM_SECONDS_AFTER_PERIOD;

				List<AccountBalanceSummaryReport.ResultEntry> entries = processor.entries(-1, -1);
				AccountBalanceSummaryReport.CsvExportProcessor csvExportProcessor = new AccountBalanceSummaryReport.CsvExportProcessor(
						0);
				logger.trace("csvExportProcessor = {}, processor = {}", csvExportProcessor, processor);
				this.sendEmail(accountBalanceSummaryReportSpecification, processor.getTimeInterval(),
						processor.getRelativeTimeRange(), csvExportProcessor, entries,
						configuration.getCompressAccountBalanceSummaryReportEmail(), rethrow);
				return null;
			}

			public ExecuteScheduleResponse.NotExecutedReason process(
					MonthlySalesPerformanceReportSpecification monthlySalesPerformanceReportSpecification,
					boolean rethrow) throws Exception {
				logger.info("Processing specification {}", monthlySalesPerformanceReportSpecification);
				MonthlySalesPerformanceReport.Processor processor = new MonthlySalesPerformanceReport.Processor(apEm,
						monthlySalesPerformanceReportSpecification.getParameters(), referenceDate);
				if (checkCurrentSecondsAfterPeriod(processor.getTimeInterval(), referenceDate) == false)
					return ExecuteScheduleResponse.NotExecutedReason.BELOW_MINIMUM_SECONDS_AFTER_PERIOD;
				List<MonthlySalesPerformanceReport.ResultEntry> entries = processor.entries(-1, -1);
				MonthlySalesPerformanceReport.CsvExportProcessor csvExportProcessor = new MonthlySalesPerformanceReport.CsvExportProcessor(
						0);
				logger.trace("csvExportProcessor = {}, processor = {}", csvExportProcessor, processor);
				this.sendEmail(monthlySalesPerformanceReportSpecification, processor.getTimeInterval(),
						processor.getRelativeTimeRange(), csvExportProcessor, entries,
						configuration.getCompressAccountBalanceSummaryReportEmail(), rethrow);
				return null;
			}

			public <ResultType> MimeBodyPart createAttachment(CsvExportProcessor<ResultType> csvExportProcessor,
					List<ResultType> entries, boolean zip) throws Exception {
				logger.trace("creating mime body part via ({}) with zip = {} (level = {})", csvExportProcessor, zip,
						configuration.getCompressionLevel());
				MimeBodyPart mimeBodyPart = new MimeBodyPart();
				if (zip) {
					try (
							ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
							ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);) {
						zipOutputStream.setLevel(configuration.getCompressionLevel());
						ZipEntry entry = new ZipEntry("report-output.csv");
						zipOutputStream.putNextEntry(entry);
						csvExportProcessor.addToBacking(zipOutputStream, entries);
						zipOutputStream.flush();
						ByteArrayDataSource dataSource = new ByteArrayDataSource(byteArrayOutputStream.toByteArray(),
								"application/zip");
						mimeBodyPart.setDataHandler(new DataHandler(dataSource));
						mimeBodyPart.setFileName("report-output.zip");
					}
				} else {
					String result = csvExportProcessor.add(entries);
					ByteArrayDataSource dataSource = new ByteArrayDataSource(result, "text/csv");
					mimeBodyPart.setDataHandler(new DataHandler(dataSource));
					mimeBodyPart.setFileName("report-output.csv");
				}
				return mimeBodyPart;
			}

			/**
			 * Send mail and check for SocketTimeoutException. If such exists and rethrow is
			 * false, returns false. Returns true otherwise.
			 *
			 * @param fromAddress See the caller method.
			 * @param email       See the caller method.
			 * @param subject     See the caller method.
			 * @param body        See the caller method.
			 * @param attachment  See the caller method.
			 * @param rethrow     Whether to rethrow the exception.
			 * @return False if rethrow is false and there is SocketTimeoutException. True
			 *         otherwise.
			 * @throws Exception Throws exception if such exists and rethrow is true.
			 */
			private boolean sendMainAndCheckTimeout(InternetAddress fromAddress, String email, String subject,
					String body,
					MimeBodyPart attachment, int timesToTry, boolean rethrow) throws Exception {
				try {
					emailer.sendEmail(fromAddress, email, subject, body, attachment, timesToTry, true);
					return true;
				} catch (Exception e) {
					if (rethrow) {
						throw e;
					}

					if (e.getCause() instanceof SocketTimeoutException) {
						logger.error(
								"████████ SocketTimeoutException has been thrown when sending mail. Skipping rest of the emails.");
						return false;
					}

					return true;
				}
			}

			public <ResultType> void sendEmail(hxc.ecds.protocol.rest.reports.ReportSpecification reportSpecification,
					TimeInterval timeInterval,
					Report.RelativeTimeRange relativeTimeRange, CsvExportProcessor<ResultType> csvExportProcessor,
					List<ResultType> entries, boolean zip, boolean rethrow) throws Exception {
				GeneralConfig generalConfig = context.findCompanyInfoByID(companyInfo.getCompany().getId())
						.getConfiguration(em, GeneralConfig.class);
				TimeInterval fixedTimeInterval = new TimeInterval(timeInterval);
				if (fixedTimeInterval.getEndDate() == null)
					fixedTimeInterval.setEndDate(new Date());
				if (fixedTimeInterval.getStartDate() == null)
					fixedTimeInterval.setStartDate(new Date(0));
				Report.EmailData emailData = new Report.EmailData()
						.setDate(referenceDate)
						.setTimeInterval(fixedTimeInterval)
						.setRelativeTimeRange(relativeTimeRange)
						.setReportSchedule(schedule)
						.setReportSpecification(reportSpecification)
						.setReportingConfig(configuration);
				logger.debug("Using fromAddress {}", configuration.getFromEmailAddress());
				InternetAddress fromAddress = new InternetAddress(configuration.getFromEmailAddress());
				MimeBodyPart attachment = this.createAttachment(csvExportProcessor, entries, zip);

				if ((reportSpecification.getAgentID() != 0) && (reportSpecification.getAgentID() != null)) {
					Agent agent = Agent.findByID(em, reportSpecification.getAgentID(), companyID);
					if (schedule.getEmailToAgent()) {
						String email = agent.getEmail();
						if (email == null || email.isEmpty()) {
							logger.warn("Not sending to agent {} because it has no email address {}", agent.getId(),
									email);
						} else {
							emailData.setRecipientTitle(agent.getTitle());
							emailData.setRecipientFirstName(agent.getFirstName());
							emailData.setRecipientSurname(agent.getSurname());
							emailData.setRecipientInitials(agent.getInitials());

							Locale locale = new Locale(agent.getLanguage(), companyInfo.getCompany().getCountry());
							String subject = expandNotification(configuration.getReportEmailSubject(),
									configuration.listReportEmailSubjectFields(), locale, emailData);
							String body = expandNotification(configuration.getReportEmailBody(),
									configuration.listReportEmailBodyFields(), locale, emailData);

							if (!sendMainAndCheckTimeout(fromAddress, email, subject, body, attachment,
									generalConfig.getSmtpRetries(), rethrow)) {
								return;
							}
						}
					}

					for (String recipient : schedule.getRecipientEmails()) {
						emailData.setRecipientTitle("");
						emailData.setRecipientFirstName("");
						emailData.setRecipientSurname("");
						emailData.setRecipientInitials("");

						Locale locale = new Locale(agent.getLanguage(), companyInfo.getCompany().getCountry());
						String subject = expandNotification(configuration.getReportEmailSubject(),
								configuration.listReportEmailSubjectFields(), locale, emailData);
						String body = expandNotification(configuration.getReportEmailBody(),
								configuration.listReportEmailBodyFields(), locale, emailData);

						if (!sendMainAndCheckTimeout(fromAddress, recipient, subject, body, attachment,
								generalConfig.getSmtpRetries(), rethrow)) {
							return;
						}
					}

					for (AgentUser agentUser : schedule.getAgentUsers()) {
						if (agentUser.getEmail() == null || agentUser.getEmail().isEmpty()) {
							logger.warn("Not sending to agent user {} because it has no email address {}",
									agentUser.getId(), agentUser.getEmail());
							continue;
						}
						emailData.setAgentUser(agentUser);
						Locale locale = new Locale(agentUser.getLanguage(), companyInfo.getCompany().getCountry());
						String subject = expandNotification(configuration.getReportEmailSubject(),
								configuration.listReportEmailSubjectFields(), locale, emailData);
						String body = expandNotification(configuration.getReportEmailBody(),
								configuration.listReportEmailBodyFields(), locale, emailData);

						if (!sendMainAndCheckTimeout(fromAddress, agentUser.getEmail(), subject, body, attachment,
								generalConfig.getSmtpRetries(), rethrow)) {
							return;
						}
					}
				} else {
					for (WebUser webUser : schedule.getWebUsers()) {
						if (webUser.getEmail() == null || webUser.getEmail().isEmpty()) {
							logger.warn("Not sending to webuser {} because it has no email address {}", webUser.getId(),
									webUser.getEmail());
							continue;
						}
						emailData.setWebUser(webUser);
						Locale locale = new Locale(webUser.getLanguage(), companyInfo.getCompany().getCountry());
						String subject = expandNotification(configuration.getReportEmailSubject(),
								configuration.listReportEmailSubjectFields(), locale, emailData);
						String body = expandNotification(configuration.getReportEmailBody(),
								configuration.listReportEmailBodyFields(), locale, emailData);

						if (!sendMainAndCheckTimeout(fromAddress, webUser.getEmail(), subject, body, attachment,
								generalConfig.getSmtpRetries(), rethrow)) {
							return;
						}
					}
				}
			}

			private StringExpander<Report.EmailData> emailExpander = new StringExpander<Report.EmailData>() {
				@Override
				protected String expandField(String englishName, Locale locale, Report.EmailData emailData) {
					return expandFieldActual(englishName, locale, emailData);
				}
			};

			public String expandFieldActual(String englishName, Locale locale, Report.EmailData emailData) {
				switch (englishName) {
					case ReportingConfig.DATE:
						return FormatHelper.formatDate(context, this, locale, emailData.getDate());
					case ReportingConfig.TIME:
						return FormatHelper.formatTime(context, this, locale, emailData.getDate());

					case ReportingConfig.INTERVAL_START_DATE:
						return FormatHelper.formatDate(context, this, locale,
								emailData.getTimeInterval().getStartDate());
					case ReportingConfig.INTERVAL_START_TIME:
						return FormatHelper.formatTime(context, this, locale,
								emailData.getTimeInterval().getStartDate());

					case ReportingConfig.INTERVAL_END_DATE:
						return FormatHelper.formatDate(context, this, locale, emailData.getTimeInterval().getEndDate());
					case ReportingConfig.INTERVAL_END_TIME:
						return FormatHelper.formatTime(context, this, locale, emailData.getTimeInterval().getEndDate());

					case ReportingConfig.INTERVAL_DESCRIPTION:
						return emailData.getIntervalDescription().safe(locale.getLanguage(), "");

					case ReportingConfig.REPORT_NAME:
						return emailData.getReportSpecification().getName();
					case ReportingConfig.REPORT_DESCRIPTION:
						return (emailData.getReportSpecification().getDescription() == null ? ""
								: emailData.getReportSpecification().getDescription());

					case ReportingConfig.SCHEDULE_DESCRIPTION:
						return (emailData.getReportSchedule().getDescription() == null ? ""
								: emailData.getReportSchedule().getDescription());

					case ReportingConfig.SCHEDULE_PERIOD_DESCRIPTION:
						return emailData.getSchedulePeriodDescription().safe(locale.getLanguage(), "");

					case ReportingConfig.RECEPIENT_TITLE:
						return emailData.getRecipientTitle();
					case ReportingConfig.RECEPIENT_FIRST_NAME:
						return emailData.getRecipientFirstName();
					case ReportingConfig.RECEPIENT_SURNAME:
						return emailData.getRecipientSurname();
					case ReportingConfig.RECEPIENT_INITIALS:
						return emailData.getRecipientInitials();

					default:
						return "  ";
				}
			}

			public String expandNotification(Phrase notification, Phrase[] fields, Locale locale,
					Report.EmailData emailData) {
				return emailExpander.expandNotification(notification, locale, fields, emailData);
			}

			public ExecuteScheduleResponse.NotExecutedReason process(
					SalesSummaryReportSpecification salesSummaryReportSpecification, boolean rethrow) throws Exception {
				logger.info("Processing specification {}", salesSummaryReportSpecification);
				SalesSummaryReport processor = new SalesSummaryReport(apEm,
						salesSummaryReportSpecification.getParameters(), referenceDate);
				logger.info("Created processor {}", processor);

				if (checkCurrentSecondsAfterPeriod(processor.getTimeInterval(), referenceDate) == false)
					return ExecuteScheduleResponse.NotExecutedReason.BELOW_MINIMUM_SECONDS_AFTER_PERIOD;
				List<SalesSummaryReportResultEntry> entries = processor.entries(-1, -1,
						companyInfo.getCompany().getId());
				logger.trace("SalesSummaryReport entries = {}", entries);
				SalesSummaryReportResultEntry entry = null;
				if (entries.size() > 0)
					entry = entries.get(0);
				else {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					Calendar calendar = Calendar.getInstance();
					calendar.setTime(referenceDate);
					calendar.add(Calendar.HOUR_OF_DAY, -1);
					entry = new SalesSummaryReportResultEntry();
					entry.setDate(DateHelper.startOf(calendar, Calendar.HOUR_OF_DAY).getTime());
					entry.setTotalAmount(ZERO);
					entry.setTotalAirtimeAmount(ZERO);
					entry.setTotalAmountNonAirtime(ZERO);
					entry.setAgentCount(0);
					entry.setAgentCountAirtime(0);
					entry.setAgentCountNonAirtime(0);
					entry.setSuccessfulTransactionCount(0);
					entry.setSuccessfulAirtimeTransactionCount(0);
					entry.setSuccessfulNonAirtimeTransactionCount(0);
					entry.setFailedTransactionCount(0);
					entry.setFailedAirtimeTransactionCount(0);
					entry.setFailedNonAirtimeTransactionCount(0);
					entry.setAverageAmountPerAgent(ZERO);
					entry.setAverageAirtimeAmountPerAgent(ZERO);
					entry.setAverageNonAirtimeAmountPerAgent(ZERO);
					entry.setAverageAmountPerTransaction(ZERO);
					entry.setAverageAirtimeAmountPerTransaction(ZERO);
					entry.setAverageNonAirtimeAmountPerTransaction(ZERO);
					entry.setMMTotalTransferAmount(ZERO);
					entry.setMMTransferCountSuccess(0);
					entry.setMMTransferCountFailed(0);
					entry.setMMReceivingAgentCount(0);
					entry.setMMAverageTransferAmountPerAgent(ZERO);
					entry.setMMAverageTransferAmountPerTransaction(ZERO);
					entries.add(entry);
				}

				TimeInterval fixedTimeInterval = new TimeInterval();
				fixedTimeInterval.setEndDate(new Date());
				fixedTimeInterval.setStartDate(entry.getDate());
				logger.debug("Using fromAddress {}", configuration.getFromEmailAddress());
				InternetAddress fromAddress = new InternetAddress(configuration.getFromEmailAddress());

				logger.trace("SalesSummaryReport configuration = {}, entry = {}", configuration, entry);
				HashMap<Locale, ArrayList<String>> localeAddressMap = new HashMap<Locale, ArrayList<String>>();
				for (WebUser webUser : schedule.getWebUsers()) {
					if (schedule.getChannels() == null || schedule.hasChannel(ReportSchedule.Channel.SMS)) // default is
																											// SMS
					{
						if (webUser.getMobileNumber() == null) {
							logger.warn("Not sending to webuser {} because it has no mobile number", webUser.getId());
							continue;
						}
						Locale locale = new Locale(webUser.getLanguage(), companyInfo.getCompany().getCountry());
						sendNotification(webUser.getMobileNumber(), configuration.getSalesSummaryReportNotification(),
								configuration.listSalesSummaryReportNotificationFields(), locale, entry);
						if (RestServerConfiguration.getInstance().isEnabledMobileMoney()) {
							sendNotification(webUser.getMobileNumber(),
									configuration.getMobileMoneyReportNotification(),
									configuration.listMobileMoneyReportNotificationFields(), locale, entry);
						}
					}
					// Record email addresses
					if (schedule.getChannels() != null && schedule.hasChannel(ReportSchedule.Channel.EMAIL)) {
						if (webUser.getEmail() == null || webUser.getEmail().isEmpty()) {
							logger.warn("Not sending to webuser {} because it has no email address {}", webUser.getId(),
									webUser.getEmail());
							continue;
						}
						Locale locale = new Locale(webUser.getLanguage(), companyInfo.getCompany().getCountry());
						if (localeAddressMap.containsKey(locale)) {
							ArrayList<String> addresses = localeAddressMap.get(locale);
							addresses.add(webUser.getEmail());
						} else {
							ArrayList<String> addresses = new ArrayList<String>();
							addresses.add(webUser.getEmail());
							localeAddressMap.put(locale, addresses);
						}
					}
				}

				// Send emails per locale
				// SalesSummaryCsvExportProcessor csvExportProcessor = new
				// SalesSummaryCsvExportProcessor(0);
				for (Locale locale : localeAddressMap.keySet()) {
					SalesSummaryCsvExportProcessor csvExportProcessor = new SalesSummaryCsvExportProcessor(0);
					ArrayList<String> bccAddresses = localeAddressMap.get(locale);
					String subject = expandNotification(configuration.getSalesSummaryReportEmailSubject(),
							configuration.listSalesSummaryReportNotificationFields(), locale, entry);
					String body = expandNotification(configuration.getSalesSummaryReportEmailBody(),
							configuration.listSalesSummaryReportNotificationFields(), locale, entry);
					ArrayList<MimeBodyPart> attachments = new ArrayList<MimeBodyPart>();
					MimeBodyPart attachment = this.createAttachment(csvExportProcessor, entries,
							configuration.getCompressSalesSummaryReportEmail());
					attachment.setFileName("sales_summary_report.csv");
					attachments.add(attachment);

					// Add Mobile money report if enabled
					if (RestServerConfiguration.getInstance().isEnabledMobileMoney()) {
						MobileMoneySummaryCsvExportProcessor mmCsvExportProcessor = new MobileMoneySummaryCsvExportProcessor(
								0);
						MimeBodyPart attachment2 = this.createAttachment(mmCsvExportProcessor, entries,
								configuration.getCompressMobileMoneySummaryReportEmail());
						attachment2.setFileName("mobile_money_summary_report.csv");
						attachments.add(attachment2);
					}

					GeneralConfig generalConfig = context.findCompanyInfoByID(companyInfo.getCompany().getId())
							.getConfiguration(em, GeneralConfig.class);
					emailer.sendEmail(fromAddress, null, bccAddresses, subject, body, attachments,
							generalConfig.getSmtpRetries(), rethrow);
				}
				return null;
			}

			private StringExpander<SalesSummaryReportResultEntry> expander = new StringExpander<SalesSummaryReportResultEntry>() {
				@Override
				protected String expandField(String englishName, Locale locale, SalesSummaryReportResultEntry entry) {
					return expandFieldActual(englishName, locale, entry);
				}
			};

			public String expandFieldActual(String englishName, Locale locale, SalesSummaryReportResultEntry entry) {
				switch (englishName) {
					case ReportingConfig.DATE:
						return FormatHelper.formatDate(context, this, locale, referenceDate);
					case ReportingConfig.TIME:
						return FormatHelper.formatTime(context, this, locale, referenceDate);
					case ReportingConfig.PERIOD:
						TimeInterval timeInterval = Report.RelativeTimeRange.PREVIOUS_HOUR.resolve(referenceDate);
						timeInterval
								.setStartDate(DateHelper.startOf(timeInterval.getStartDate(), Calendar.DATE).getTime());

						Calendar startDateCalendar = Calendar.getInstance();
						startDateCalendar.setTime(timeInterval.getStartDate());

						Calendar endDateCalendar = Calendar.getInstance();
						endDateCalendar.setTime(timeInterval.getEndDate());

						logger.trace(
								"For SMS variables: timeInterval = {}, timeInterval.getStartDate() = {}, startDateCalendar = {}, endDateCalendar = {}",
								timeInterval, timeInterval.getStartDate(),
								dateFormat.format(startDateCalendar.getTime()),
								dateFormat.format(endDateCalendar.getTime()));
						return String.format("%02d-%02d", startDateCalendar.get(Calendar.HOUR_OF_DAY),
								endDateCalendar.get(Calendar.HOUR_OF_DAY) + 1);
					case ReportingConfig.REPORT_PERIOD_START_DATE:
						timeInterval = Report.RelativeTimeRange.PREVIOUS_HOUR.resolve(referenceDate);
						return FormatHelper.formatDate(context, this, locale, timeInterval.getStartDate());
					case ReportingConfig.REPORT_PERIOD_END_DATE:
						timeInterval = Report.RelativeTimeRange.CURRENT_HOUR.resolve(referenceDate);
						return FormatHelper.formatDate(context, this, locale, timeInterval.getEndDate());
					case ReportingConfig.REPORT_PERIOD_START_TIME:
						timeInterval = Report.RelativeTimeRange.PREVIOUS_HOUR.resolve(referenceDate);
						timeInterval
								.setStartDate(DateHelper.startOf(timeInterval.getStartDate(), Calendar.DATE).getTime());
						return FormatHelper.formatTime(context, this, locale, timeInterval.getStartDate());
					case ReportingConfig.REPORT_PERIOD_END_TIME:
						timeInterval = Report.RelativeTimeRange.CURRENT_HOUR.resolve(referenceDate);
						return FormatHelper.formatTime(context, this, locale, timeInterval.getStartDate());
					case ReportingConfig.TOTAL_AMOUNT:
						return format(locale, entry.getTotalAmount());
					case ReportingConfig.TOTAL_AIRTIME_AMOUNT:
						return format(locale, entry.getTotalAirtimeAmount());
					case ReportingConfig.TOTAL_AMOUNT_NON_AIRTIME:
						return format(locale, entry.getTotalAmountNonAirtime());
					case ReportingConfig.AGENT_COUNT:
						return Integer.toString(entry.getAgentCount());
					case ReportingConfig.AGENT_COUNT_AIRTIME:
						return Integer.toString(entry.getAgentCountAirtime());
					case ReportingConfig.AGENT_COUNT_NON_AIRTIME:
						return Integer.toString(entry.getAgentCountNonAirtime());
					case ReportingConfig.SUCCESSFUL_TRANSACTION_COUNT:
						return Integer.toString(entry.getSuccessfulTransactionCount());
					case ReportingConfig.SUCCESSFUL_AIRTIME_TRANSACTION_COUNT:
						return Integer.toString(entry.getSuccessfulAirtimeTransactionCount());
					case ReportingConfig.SUCCESSFUL_NON_AIRTIME_TRANSACTION_COUNT:
						return Integer.toString(entry.getSuccessfulNonAirtimeTransactionCount());
					case ReportingConfig.FAILED_TRANSACTION_COUNT:
						return Integer.toString(entry.getFailedTransactionCount());
					case ReportingConfig.FAILED_AIRTIME_TRANSACTION_COUNT:
						return Integer.toString(entry.getFailedAirtimeTransactionCount());
					case ReportingConfig.FAILED_NON_AIRTIME_TRANSACTION_COUNT:
						return Integer.toString(entry.getFailedNonAirtimeTransactionCount());
					case ReportingConfig.AVERAGE_AMOUNT_PER_AGENT:
						return format(locale, entry.getAverageAmountPerAgent());
					case ReportingConfig.AVERAGE_AIRTIME_AMOUNT_PER_AGENT:
						return format(locale, entry.getAverageAirtimeAmountPerAgent());
					case ReportingConfig.AVERAGE_NON_AIRTIME_AMOUNT_PER_AGENT:
						return format(locale, entry.getAverageNonAirtimeAmountPerAgent());
					case ReportingConfig.AVERAGE_AMOUNT_PER_TRANSACTION:
						return format(locale, entry.getAverageAmountPerTransaction());
					case ReportingConfig.AVERAGE_AIRTIME_AMOUNT_PER_TRANSACTION:
						return format(locale, entry.getAverageAirtimeAmountPerTransaction());
					case ReportingConfig.AVERAGE_NON_AIRTIME_AMOUNT_PER_TRANSACTION:
						return format(locale, entry.getAverageNonAirtimeAmountPerTransaction());
					case ReportingConfig.MOBILE_MONEY_TRANSFER_COUNT_SUCCESS:
						return Integer.toString(entry.getMMTransferCountSuccess());
					case ReportingConfig.MOBILE_MONEY_TOTAL_TRANSFER_AMOUNT:
						return format(locale, entry.getMMTotalTransferAmount());
					case ReportingConfig.MOBILE_MONEY_TRANSFER_COUNT_FAILED:
						return Integer.toString(entry.getMMTransferCountFailed());
					case ReportingConfig.MOBILE_MONEY_RECEIVING_AGENT_COUNT:
						return Integer.toString(entry.getMMReceivingAgentCount());
					case ReportingConfig.MOBILE_MONEY_AVERAGE_TRASNFER_AMOUNT_PER_AGENT:
						return format(locale, entry.getMMAverageTransferAmountPerAgent());
					case ReportingConfig.MOBILE_MONEY_AVERAGE_AMOUNT_PER_TRANSACTION:
						return format(locale, entry.getMMAverageTransferAmountPerTransaction());
					default:
						return "  ";
				}
			}

			public String expandNotification(Phrase notification, Phrase[] fields, Locale locale,
					SalesSummaryReportResultEntry entry) {
				return expander.expandNotification(notification, locale, fields, entry);
			}

			public String format(Locale locale, BigDecimal amount) {
				if (amount == null || locale == null) {
					logger.warn("format called with null amount or locale ...");
					return "";
				}

				if (customCurrencyFormatter == null) {
					NumberFormat numberFormat = context.getCurrencyFormat(locale);
					return numberFormat.format(amount);
				} else {
					return customCurrencyFormatter.format(amount);
				}
				// return numberFormat.format(amount);
			}

			public void sendNotification(String mobileNumber, Phrase notification, Phrase[] fields, Locale locale,
					SalesSummaryReportResultEntry entry) {
				logger.trace("Sending notification with locale {}", locale);
				if (mobileNumber == null) {
					logger.error("Attempted to send notification with null mobileNumber");
					return;
				}
				if (mobileNumber.isEmpty()) {
					logger.error("Attempted to send notification with empty mobileNumber");
					return;
				}
				if (notification == null) {
					logger.error("Attempted to send notification with null notification");
					return;
				}
				logger.debug("Using SMS source address {}", configuration.getSmsSourceAddress());
				String text = expandNotification(notification, fields, locale, entry);
				logger.trace("Sending notification {} to {}", text, mobileNumber);
				context.sendSMS(configuration.getSmsSourceAddress(), mobileNumber, locale.getISO3Language(), text);
			}
		}
	}

	public void runActual() throws Exception {
		synchronized (this.runCounterMonitor) {
			this.runStartCounter++;
			this.runCounterMonitor.notify();
		}
		try {
			logger.info("starting run actual with processor ...");
			if (this.context.isMasterServer() == false) {
				logger.info("Not master server ... ignoring ...");
				return;
			}
			new SchedulesProcessor(this.context, this.companyInfo, new Date(), true).process();
		} finally {
			synchronized (this.runCounterMonitor) {
				this.runCompleteCounter = this.runStartCounter;
				this.runCounterMonitor.notify();
			}
		}
	}

	// Runnable
	@Override
	public void run() {
		// TODO Ensure no concurrent runs ...
		logger.info("starting run ...");
		try {
			this.runActual();
		} catch (Throwable throwable) {
			String msg = String.format("ReportScheduleExecutor: run failed with: %s", throwable);
			logger.error(msg, throwable);
			this.snmpConnector.jobFailed(this.service.getConfiguration().getName(IPhrase.ENG),
					IncidentSeverity.CRITICAL, msg);
		} finally {
			logger.info("run ended");
		}
	}

	private ScheduledFuture<?> schedule() {
		long initialDelay = 0L;
		long period = 30L;
		TimeUnit unit = TimeUnit.SECONDS;
		logger.info("Scheduling with initialDelay = {}, period = {}, unit = {}", initialDelay, period, unit);
		return this.scheduledThreadPoolExecutor.scheduleAtFixedRate(this, initialDelay, period, unit);
	}

	@Override
	public void onConfigurationChanged(IConfiguration configuration) {
		logger.info("loading configuration ...");
		if (!(configuration instanceof ReportingConfig)) {
			logger.trace("ignoring configuration change notification for {}", configuration);
			return;
		}
		this.restart();
	}
}
