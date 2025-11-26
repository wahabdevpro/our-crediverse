package hxc.ecds.protocol.rest.config;

import java.util.Arrays;
import java.util.List;

//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import hxc.ecds.protocol.rest.Validator;
import hxc.ecds.protocol.rest.Violation;

public class ReportingConfig implements IConfiguration
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final String DATE = "{Date}";
	public static final String TIME = "{Time}";

	public static final String PERIOD = "{Period}";
	public static final String TOTAL_AMOUNT = "{TotalAmount}";
	public static final String TOTAL_AIRTIME_AMOUNT = "{TotalAmountAirtime}";
	public static final String TOTAL_AMOUNT_NON_AIRTIME = "{TotalAmountNonAirtime}";
	public static final String AGENT_COUNT = "{AgentCount}";
	public static final String AGENT_COUNT_AIRTIME = "{AgentCountAirtime}";
	public static final String AGENT_COUNT_NON_AIRTIME = "{AgentCountNonAirtime}";
	public static final String SUCCESSFUL_TRANSACTION_COUNT = "{SuccessfulTransactionCount}";
	public static final String SUCCESSFUL_AIRTIME_TRANSACTION_COUNT = "{SuccessfulAirtimeTransactionCount}";
    public static final String SUCCESSFUL_NON_AIRTIME_TRANSACTION_COUNT = "{SuccessfulNonAirtimeTransactionCount}";
	public static final String FAILED_TRANSACTION_COUNT = "{FailedTransactionCount}";
	public static final String FAILED_AIRTIME_TRANSACTION_COUNT = "{FailedAirtimeTransactionCount}";
    public static final String FAILED_NON_AIRTIME_TRANSACTION_COUNT = "{FailedNonAirtimeTransactionCount}";
	public static final String AVERAGE_AMOUNT_PER_AGENT = "{AverageAmountPerAgent}";
	public static final String AVERAGE_AIRTIME_AMOUNT_PER_AGENT = "{AverageAirtimeAmountPerAgent}";
	public static final String AVERAGE_NON_AIRTIME_AMOUNT_PER_AGENT = "{AverageNonAirtimeAmountPerAgent}";
	public static final String AVERAGE_AMOUNT_PER_TRANSACTION = "{AverageAmountPerTransaction}";
	public static final String AVERAGE_AIRTIME_AMOUNT_PER_TRANSACTION = "{AverageAirtimeAmountPerTransaction}";
	public static final String AVERAGE_NON_AIRTIME_AMOUNT_PER_TRANSACTION = "{AverageNonAirtimeAmountPerTransaction}";

	public static final String MOBILE_MONEY_TRANSFER_COUNT_SUCCESS = "{SuccessfulMMTransferCount}";
	public static final String MOBILE_MONEY_TRANSFER_COUNT_FAILED = "{FailedMMTransfersCount}";
	public static final String MOBILE_MONEY_TOTAL_TRANSFER_AMOUNT = "{TotalAmountMMTransfer}";
	public static final String MOBILE_MONEY_RECEIVING_AGENT_COUNT = "{AgentCountReceivingMM}";
	public static final String MOBILE_MONEY_AVERAGE_TRASNFER_AMOUNT_PER_AGENT = "{AveMMTransferAmountPerAgent}";
	public static final String MOBILE_MONEY_AVERAGE_AMOUNT_PER_TRANSACTION = "{AveMMAmountPerTransaction}";


	public static final String REPORT_PERIOD_START_DATE = "{ReportPeriodStartDate}";
	public static final String REPORT_PERIOD_START_TIME = "{ReportPeriodStartTime}";

	public static final String REPORT_PERIOD_END_DATE = "{ReportPeriodEndDate}";
	public static final String REPORT_PERIOD_END_TIME = "{ReportPeriodEndTime}";

	public static final String INTERVAL_START_DATE = "{IntervalStartDate}";
	public static final String INTERVAL_START_TIME = "{IntervalStartTime}";

	public static final String INTERVAL_END_DATE = "{IntervalEndDate}";
	public static final String INTERVAL_END_TIME = "{IntervalEndTime}";

	public static final String INTERVAL_DESCRIPTION = "{IntervalDescription}";

	public static final String REPORT_NAME = "{ReportName}";
	public static final String REPORT_DESCRIPTION = "{ReportDescription}";
	public static final String SCHEDULE_DESCRIPTION = "{ScheduleDescription}";
	public static final String SCHEDULE_PERIOD_DESCRIPTION = "{SchedulePeriodDescription}";
	public static final String RECEPIENT_TITLE = "{RecepientTitle}";
	public static final String RECEPIENT_FIRST_NAME = "{RecepientFirstName}";
	public static final String RECEPIENT_SURNAME = "{RecepientSurname}";
	public static final String RECEPIENT_INITIALS = "{RecepientInitials}";

    private static Phrase[] emptyFields = new Phrase[]{};

	private static Phrase[] salesSummaryReportNotificationFields = new Phrase[] {
		Phrase.en(DATE),
		Phrase.en(TIME),
		Phrase.en(REPORT_PERIOD_START_DATE),
		Phrase.en(REPORT_PERIOD_START_TIME),
		Phrase.en(REPORT_PERIOD_END_DATE),
		Phrase.en(REPORT_PERIOD_END_TIME),
		Phrase.en(PERIOD),
		Phrase.en(TOTAL_AMOUNT),
		Phrase.en(TOTAL_AIRTIME_AMOUNT),
		Phrase.en(TOTAL_AMOUNT_NON_AIRTIME),
		Phrase.en(AGENT_COUNT),
		Phrase.en(AGENT_COUNT_AIRTIME),
		Phrase.en(AGENT_COUNT_NON_AIRTIME),
		Phrase.en(SUCCESSFUL_TRANSACTION_COUNT),
		Phrase.en(SUCCESSFUL_AIRTIME_TRANSACTION_COUNT),
		Phrase.en(SUCCESSFUL_NON_AIRTIME_TRANSACTION_COUNT),
		Phrase.en(FAILED_TRANSACTION_COUNT),
		Phrase.en(FAILED_AIRTIME_TRANSACTION_COUNT),
		Phrase.en(FAILED_NON_AIRTIME_TRANSACTION_COUNT),
		Phrase.en(AVERAGE_AMOUNT_PER_AGENT),
		Phrase.en(AVERAGE_AIRTIME_AMOUNT_PER_AGENT),
		Phrase.en(AVERAGE_NON_AIRTIME_AMOUNT_PER_AGENT),
		Phrase.en(AVERAGE_AMOUNT_PER_TRANSACTION),
		Phrase.en(AVERAGE_AIRTIME_AMOUNT_PER_TRANSACTION),
		Phrase.en(AVERAGE_NON_AIRTIME_AMOUNT_PER_TRANSACTION)
	};

	private static Phrase[] mobileMoneyReportNotificationFields = new Phrase[] {
		Phrase.en(DATE),
		Phrase.en(TIME),
		Phrase.en(PERIOD),
		Phrase.en(REPORT_PERIOD_START_DATE),
		Phrase.en(REPORT_PERIOD_START_TIME),
		Phrase.en(REPORT_PERIOD_END_DATE),
		Phrase.en(REPORT_PERIOD_END_TIME),
		Phrase.en(MOBILE_MONEY_TRANSFER_COUNT_SUCCESS),
		Phrase.en(MOBILE_MONEY_TOTAL_TRANSFER_AMOUNT),
		Phrase.en(MOBILE_MONEY_TRANSFER_COUNT_FAILED),
		Phrase.en(MOBILE_MONEY_RECEIVING_AGENT_COUNT),
		Phrase.en(MOBILE_MONEY_AVERAGE_TRASNFER_AMOUNT_PER_AGENT),
		Phrase.en(MOBILE_MONEY_AVERAGE_AMOUNT_PER_TRANSACTION)
	};

	private static Phrase[] reportEmailSubjectFields = new Phrase[] {
		Phrase.en(DATE),
		Phrase.en(TIME),
		Phrase.en(INTERVAL_START_DATE),
		Phrase.en(INTERVAL_START_TIME),
		Phrase.en(INTERVAL_END_DATE),
		Phrase.en(INTERVAL_END_TIME),
		Phrase.en(INTERVAL_DESCRIPTION),
		Phrase.en(REPORT_NAME),
		Phrase.en(REPORT_DESCRIPTION),
		Phrase.en(SCHEDULE_DESCRIPTION),
		Phrase.en(SCHEDULE_PERIOD_DESCRIPTION),
		Phrase.en(RECEPIENT_TITLE),
		Phrase.en(RECEPIENT_FIRST_NAME),
		Phrase.en(RECEPIENT_SURNAME),
		Phrase.en(RECEPIENT_INITIALS)
	};

	private static Phrase[] reportEmailBodyFields = new Phrase[] {
		Phrase.en(DATE),
		Phrase.en(TIME),
		Phrase.en(INTERVAL_START_DATE),
		Phrase.en(INTERVAL_START_TIME),
		Phrase.en(INTERVAL_END_DATE),
		Phrase.en(INTERVAL_END_TIME),
		Phrase.en(INTERVAL_DESCRIPTION),
		Phrase.en(REPORT_NAME),
		Phrase.en(REPORT_DESCRIPTION),
		Phrase.en(SCHEDULE_DESCRIPTION),
		Phrase.en(SCHEDULE_PERIOD_DESCRIPTION),
		Phrase.en(RECEPIENT_TITLE),
		Phrase.en(RECEPIENT_FIRST_NAME),
		Phrase.en(RECEPIENT_SURNAME),
		Phrase.en(RECEPIENT_INITIALS)
	};

	private static final long serialVersionUID = 3302812555522157159L;

	// //////////////////////////////////////////////////////////////////////////////////////

	protected int version;

	protected String fromEmailAddress = "ecds@crediverse.com";
	protected String smsSourceAddress = "110";

	protected Boolean smsConfigurationEnabled = false;
	protected String smsCurrency = "";
	protected String smsThousandSeparator = "";
	protected String smsDecimalSeparator = "";

	protected Boolean compressWholesalerPerformanceReportEmail = true;
	protected Boolean compressRetailerPerformanceReportEmail = true;
	protected Boolean compressDailyGroupSalesReportEmail = false;
	protected Boolean compressDailySalesReportByAreaEmail = false;
	protected Boolean compressSalesSummaryReportEmail = false;
	protected Boolean compressMobileMoneySummaryReportEmail = false;
	protected Boolean compressAccountBalanceSummaryReportEmail = true;
	protected Boolean compressMonthlySalesPerformanceReportEmail = true;
	protected Integer compressionLevel = java.util.zip.Deflater.DEFAULT_COMPRESSION;
	protected Integer agentReportCountLimit = 10;
	protected Integer agentReportDailyScheduleLimit = 30;
	//Report Aggregation Switches
	/*
	protected Boolean usePreaggregatedRetailerReportData = false;
	protected Boolean usePreaggregatedWholesalerReportData = false;
	protected Boolean usePreaggregatedDailySalesSummaryReportData = false;
	protected Boolean usePreaggregatedDailyGroupSalesReportData = false;
	protected Boolean usePreaggregatedMonthlySalesPerformanceReportData = false;
	protected Boolean usePreaggregatedAccountBalanceSummaryReportData = false;
	*/
	protected Phrase intervalPreviousDayDescription = Phrase.en("previous day");
	protected Phrase intervalCurrentDayDescription = Phrase.en("current day");
	protected Phrase intervalPreviousWeekDescription = Phrase.en("previous week");
	protected Phrase intervalCurrentWeekDescription = Phrase.en("current week");
	protected Phrase intervalPrevious30DaysDescription = Phrase.en("previous 30 days");
	protected Phrase intervalPreviousMonthDescription = Phrase.en("previous month");
	protected Phrase intervalCurrentMonthDescription = Phrase.en("current month");
	protected Phrase intervalPreviousYearDescription = Phrase.en("previous year");
	protected Phrase intervalCurrentYearDescription = Phrase.en("current year");
	protected Phrase intervalPreviousHourDescription = Phrase.en("previous hour");
	protected Phrase intervalCurrentHourDescription = Phrase.en("current hour");
	protected Phrase intervalFixedDescription = Phrase.en("fixed interval");

	protected Phrase schedulePeriodHourDescription = Phrase.en("hourly");
	protected Phrase schedulePeriodDayDescription = Phrase.en("daily");
	protected Phrase schedulePeriodWeekDescription = Phrase.en("weekly");
	protected Phrase schedulePeriodMonthDescription = Phrase.en("monthly");
	protected Phrase schedulePeriodMinuteDescription = Phrase.en("minute by minute");

	protected Phrase salesSummaryReportNotification = Phrase.en(
		"Hourly SMS Report generated on " + DATE + "" +
		" at " + TIME + "" +
		" for period " + REPORT_PERIOD_START_DATE + " " + REPORT_PERIOD_START_TIME + "to " + 
		REPORT_PERIOD_END_DATE + " " + REPORT_PERIOD_END_TIME + ". " +
		"Total amount: " + TOTAL_AMOUNT + ", " +
		"Total airtime amount: " + TOTAL_AIRTIME_AMOUNT + ", " +
		"Total non-airtime amount: " + TOTAL_AMOUNT_NON_AIRTIME + ", " +
		"Agent count: " + AGENT_COUNT + "" + ", " +
		"Agent count airtime: " + AGENT_COUNT_AIRTIME + "" + ", " +
		"Agent non-airtime count: " + AGENT_COUNT_NON_AIRTIME + "" + ", " +
		"Successful transactions " + SUCCESSFUL_TRANSACTION_COUNT + ", " +
		"Successful airtime transactions " + SUCCESSFUL_AIRTIME_TRANSACTION_COUNT + ", " +
		"Successful non-airtime transactions " + SUCCESSFUL_NON_AIRTIME_TRANSACTION_COUNT + ", " +
		"Failed transaction count: " + FAILED_TRANSACTION_COUNT + ", " +
		"Failed airtime transaction count: " + FAILED_AIRTIME_TRANSACTION_COUNT + ", " +
		"Failed non-airtime transactions " + FAILED_NON_AIRTIME_TRANSACTION_COUNT + ", " +
		"Average amount per agent: " + AVERAGE_AMOUNT_PER_AGENT + "" +
		"Average airtime amount per agent: " + AVERAGE_AIRTIME_AMOUNT_PER_AGENT + "" +
		"Average non-airtime amount per agent: " + AVERAGE_NON_AIRTIME_AMOUNT_PER_AGENT + "" +
		"Average amount per transaction: " + AVERAGE_AMOUNT_PER_TRANSACTION + "" +
		"Average airtime amount per transaction: " + AVERAGE_AIRTIME_AMOUNT_PER_TRANSACTION + "" +
		"Average non-airtime amount per transaction: " + AVERAGE_NON_AIRTIME_AMOUNT_PER_TRANSACTION + ""
	);

	protected Phrase mobileMoneyReportNotification = Phrase.en(
		"Hourly SMS Mobile Money Report generated on " + DATE + "" +
		" at " + TIME + "" +
		" for period " + REPORT_PERIOD_START_DATE + " " + REPORT_PERIOD_START_TIME + "to " + 
		REPORT_PERIOD_END_DATE + " " + REPORT_PERIOD_END_TIME + ". " +
		"Total Mobile Money transfer amount: " + MOBILE_MONEY_TOTAL_TRANSFER_AMOUNT + ", " +
		"Successful Mobile Money transactions: " + MOBILE_MONEY_TRANSFER_COUNT_SUCCESS + ", " +
		"Failed Mobile Money transactions: " + MOBILE_MONEY_TRANSFER_COUNT_FAILED + ", " +
		"Number of Agents that received Mobile Money Transfers : " + MOBILE_MONEY_RECEIVING_AGENT_COUNT + ", " +
		"Average amount per Mobile Money transaction: " + MOBILE_MONEY_AVERAGE_AMOUNT_PER_TRANSACTION + ", " +
		"Average Mobile Money per Agent: " + MOBILE_MONEY_AVERAGE_TRASNFER_AMOUNT_PER_AGENT + ""
	);
	
	protected Phrase salesSummaryReportEmailSubject = Phrase.en(
		"Sales Summary report data for period " + REPORT_PERIOD_START_DATE + " " + REPORT_PERIOD_START_TIME + "to " + 
		REPORT_PERIOD_END_DATE + " " + REPORT_PERIOD_END_TIME
	);

	protected Phrase salesSummaryReportEmailBody = Phrase.en(
		"Hourly Email Report generated on " + DATE + "" +
		" at " + TIME + "" +
		" for period " + REPORT_PERIOD_START_DATE + " " + REPORT_PERIOD_START_TIME + "to " + 
		REPORT_PERIOD_END_DATE + " " + REPORT_PERIOD_END_TIME + ". " +
		"Total amount: " + TOTAL_AMOUNT + ", " +
		"Total airtime amount: " + TOTAL_AIRTIME_AMOUNT + ", " +
		"Total non-airtime amount: " + TOTAL_AMOUNT_NON_AIRTIME + ", " +
		"Agent count: " + AGENT_COUNT + "" + ", " +
		"Agent count airtime: " + AGENT_COUNT_AIRTIME + "" + ", " +
		"Agent non-airtime count: " + AGENT_COUNT_NON_AIRTIME + "" + ", " +
		"Successful transactions " + SUCCESSFUL_TRANSACTION_COUNT + ", " +
		"Successful airtime transactions " + SUCCESSFUL_AIRTIME_TRANSACTION_COUNT + ", " +
		"Successful non-airtime transactions " + SUCCESSFUL_NON_AIRTIME_TRANSACTION_COUNT + ", " +
		"Failed transaction count: " + FAILED_TRANSACTION_COUNT + ", " +
		"Failed airtime transaction count: " + FAILED_AIRTIME_TRANSACTION_COUNT + ", " +
		"Failed non-airtime transactions " + FAILED_NON_AIRTIME_TRANSACTION_COUNT + ", " +
		"Average amount per agent: " + AVERAGE_AMOUNT_PER_AGENT + "" +
		"Average airtime amount per agent: " + AVERAGE_AIRTIME_AMOUNT_PER_AGENT + "" +
		"Average non-airtime amount per agent: " + AVERAGE_NON_AIRTIME_AMOUNT_PER_AGENT + "" +
		"Average amount per transaction: " + AVERAGE_AMOUNT_PER_TRANSACTION + "" +
		"Average airtime amount per transaction: " + AVERAGE_AIRTIME_AMOUNT_PER_TRANSACTION + "" +
		"Average non-airtime amount per transaction: " + AVERAGE_NON_AIRTIME_AMOUNT_PER_TRANSACTION + "" +
		"\n" +
		"\n" +
		"Generated by Crediverse ECDS."
	);


	protected Phrase reportEmailSubject = Phrase.en(
		REPORT_NAME + " report data from " + INTERVAL_START_DATE + " to " + INTERVAL_END_DATE
	);

	protected Phrase reportEmailBody = Phrase.en(
		"Dear " + RECEPIENT_TITLE + " " + RECEPIENT_INITIALS + " " + RECEPIENT_SURNAME + "\n" +
		"\n" +
 		"Please find attached the data for the '" + REPORT_NAME + "' report generated at " + DATE + " " + TIME + ".\n" +
		"The report data is from " + INTERVAL_START_DATE + " " + INTERVAL_START_TIME + " to " + INTERVAL_END_DATE + " " + INTERVAL_END_TIME + " ( " + INTERVAL_DESCRIPTION + " ).\n" +
		"This report is described as '" + REPORT_DESCRIPTION + "' and is generated " + SCHEDULE_PERIOD_DESCRIPTION + ".\n" +
		"The schedule that generated this email for the report is described as '" + SCHEDULE_DESCRIPTION + "'.\n" +
		"\n" +
		"\n" +
		"Generated by Crediverse ECDS."
	);



	// //////////////////////////////////////////

	public Phrase[] listSalesSummaryReportNotificationFields()
	{
		return salesSummaryReportNotificationFields;
	}

	public Phrase[] listSalesSummaryWithMobileMoneyReportNotificationFields()
	{
		Phrase[] combined = Arrays.copyOf(salesSummaryReportNotificationFields, salesSummaryReportNotificationFields.length + mobileMoneyReportNotificationFields.length);
		System.arraycopy(mobileMoneyReportNotificationFields, 0, combined, salesSummaryReportNotificationFields.length, mobileMoneyReportNotificationFields.length);
		
		Phrase[] uniquePhrase = Arrays.asList(combined).stream().distinct()
            .toArray(Phrase[]::new);

		return uniquePhrase;
	}

	public Phrase[] listMobileMoneyReportNotificationFields()
	{
		return mobileMoneyReportNotificationFields;
	}

	public Phrase[] listIntervalPreviousDayDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalCurrentDayDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalPreviousWeekDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalCurrentWeekDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalPrevious30DaysDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalPreviousMonthDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalCurrentMonthDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalPreviousYearDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalCurrentYearDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalPreviousHourDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalCurrentHourDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listIntervalFixedDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listSchedulePeriodHourDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listSchedulePeriodDayDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listSchedulePeriodWeekDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listSchedulePeriodMonthDescriptionFields()
	{
		return emptyFields;
	}

	public Phrase[] listSchedulePeriodMinuteDescriptionFields()
	{
		return emptyFields;
	}


	public Phrase[] listReportEmailSubjectFields()
	{
		return reportEmailSubjectFields;
	}
	
	public Phrase[] listReportEmailBodyFields()
	{
		return reportEmailBodyFields;
	}

	// //////////////////////////////////////////

	public String getFromEmailAddress()
	{
		return this.fromEmailAddress;
	}
	public ReportingConfig setFromEmailAddress( String fromEmailAddress )
	{
		this.fromEmailAddress = fromEmailAddress;
		return this;
	}
	
	public String getSmsSourceAddress()
	{
		return this.smsSourceAddress;
	}
	public ReportingConfig setSmsSourceAddress( String smsSourceAddress )
	{
		this.smsSourceAddress = smsSourceAddress;
		return this;
	}

	public Boolean getSmsConfigurationEnabled()
	{
		return this.smsConfigurationEnabled;
	}
	public ReportingConfig setSmsConfigurationEnabled( Boolean smsConfigurationEnabled )
	{
		this.smsConfigurationEnabled = smsConfigurationEnabled;
		return this;
	}

	public String getSmsCurrency()
	{
		return this.smsCurrency;
	}
	public ReportingConfig setSmsCurrency( String smsCurrency )
	{
		this.smsCurrency = smsCurrency;
		return this;
	}

	public String getSmsThousandSeparator()
	{
		return this.smsThousandSeparator;
	}
	public ReportingConfig setSmsThousandSeparator( String smsThousandSeparator )
	{
		this.smsThousandSeparator = smsThousandSeparator;
		return this;
	}

	public String getSmsDecimalSeparator()
	{
		return this.smsDecimalSeparator;
	}
	public ReportingConfig setSmsDecimalSeparator( String smsDecimalSeparator )
	{
		this.smsDecimalSeparator = smsDecimalSeparator;
		return this;
	}

	public Boolean getCompressRetailerPerformanceReportEmail()
	{
		return this.compressRetailerPerformanceReportEmail;
	}
	public ReportingConfig setCompressRetailerPerformanceReportEmail( Boolean compressRetailerPerformanceReportEmail )
	{
		this.compressRetailerPerformanceReportEmail = compressRetailerPerformanceReportEmail;
		return this;
	}

	public Boolean getCompressWholesalerPerformanceReportEmail()
	{
		return this.compressWholesalerPerformanceReportEmail;
	}
	public ReportingConfig setCompressWholesalerPerformanceReportEmail( Boolean compressWholesalerPerformanceReportEmail )
	{
		this.compressWholesalerPerformanceReportEmail = compressWholesalerPerformanceReportEmail;
		return this;
	}

	public Boolean getCompressDailyGroupSalesReportEmail()
	{
		return this.compressDailyGroupSalesReportEmail;
	}
	public ReportingConfig setCompressDailyGroupSalesReportEmail( Boolean compressDailyGroupSalesReportEmail )
	{
		this.compressDailyGroupSalesReportEmail = compressDailyGroupSalesReportEmail;
		return this;
	}

	public Boolean getCompressDailySalesReportByAreaEmail()
	{
		return this.compressDailySalesReportByAreaEmail;
	}
	public ReportingConfig setCompressDailySalesReportByAreaEmail( Boolean compressDailySalesReportByAreaEmail )
	{
		this.compressDailySalesReportByAreaEmail = compressDailySalesReportByAreaEmail;
		return this;
	}

	public Boolean getCompressSalesSummaryReportEmail()
	{
		return this.compressSalesSummaryReportEmail;
	}
	public ReportingConfig setCompressSalesSummaryReportEmail( Boolean compressSalesSummaryReportEmail )
	{
		this.compressSalesSummaryReportEmail = compressSalesSummaryReportEmail;
		return this;
	}

	public Boolean getCompressMobileMoneySummaryReportEmail()
	{
		return this.compressMobileMoneySummaryReportEmail;
	}
	public ReportingConfig setCompressMobileMoneySummaryReportEmail( Boolean compressMobileMoneySummaryReportEmail )
	{
		this.compressMobileMoneySummaryReportEmail = compressMobileMoneySummaryReportEmail;
		return this;
	}

	public Boolean getCompressAccountBalanceSummaryReportEmail()
	{
		return this.compressAccountBalanceSummaryReportEmail;
	}
	public ReportingConfig setCompressAccountBalanceSummaryReportEmail( Boolean compressAccountBalanceSummaryReportEmail )
	{
		this.compressAccountBalanceSummaryReportEmail = compressAccountBalanceSummaryReportEmail;
		return this;
	}
	
	public Boolean getCompressMonthlySalesPerformanceReportEmail()
	{
		return this.compressMonthlySalesPerformanceReportEmail;
	}
	public ReportingConfig setCompressMonthlySalesPerformanceReportEmail( Boolean compressMonthlySalesPerformanceReportEmail )
	{
		this.compressMonthlySalesPerformanceReportEmail = compressMonthlySalesPerformanceReportEmail;
		return this;
	}

	public Integer getCompressionLevel()
	{
		return this.compressionLevel;
	}
	public ReportingConfig setCompressionLevel( Integer compressionLevel )
	{
		this.compressionLevel = compressionLevel;
		return this;
	}

	public Integer getAgentReportCountLimit()
	{
		return this.agentReportCountLimit;
	}
	public ReportingConfig setAgentReportCountLimit( Integer agentReportCountLimit )
	{
		this.agentReportCountLimit = agentReportCountLimit;
		return this;
	}

	public Integer getAgentReportDailyScheduleLimit()
	{
		return this.agentReportDailyScheduleLimit;
	}
	public ReportingConfig setAgentReportDailyScheduleLimit( Integer agentReportDailyScheduleLimit )
	{
		this.agentReportDailyScheduleLimit = agentReportDailyScheduleLimit;
		return this;
	}
	
	/*protected Boolean usePreaggregatedRetailerReportData = false;
	protected Boolean usePreaggregatedWholesalerReportData = false;
	protected Boolean usePreaggregatedDailySalesSummaryReportData = false;
	protected Boolean usePreaggregatedDailyGroupSalesReportData = false;
	protected Boolean usePreaggregatedMonthlySalesPerformanceReportData = false;
	protected Boolean usePreaggregatedAccountBalanceSummaryReportData = false;*/
	
/*	Report Aggregation Switches
	public Boolean getUsePreaggregatedRetailerReportData() {
		return usePreaggregatedRetailerReportData;
	}

	public ReportingConfig setUsePreaggregatedRetailerReportData(Boolean usePreaggregatedRetailerReportData)
	{
		this.usePreaggregatedRetailerReportData = usePreaggregatedRetailerReportData;
		return this;
	}

	public Boolean getUsePreaggregatedWholesalerReportData()
	{
		return usePreaggregatedWholesalerReportData;
	}

	public ReportingConfig setUsePreaggregatedWholesalerReportData(Boolean usePreaggregatedWholesalerReportData)
	{
		this.usePreaggregatedWholesalerReportData = usePreaggregatedWholesalerReportData;
		return this;
	}

	public Boolean getUsePreaggregatedDailySalesSummaryReportData()
	{
		return usePreaggregatedDailySalesSummaryReportData;
	}

	public ReportingConfig setUsePreaggregatedDailySalesSummaryReportData(Boolean usePreaggregatedDailySalesSummaryReportData)
	{
		this.usePreaggregatedDailySalesSummaryReportData = usePreaggregatedDailySalesSummaryReportData;
		return this;
	}

	public Boolean getUsePreaggregatedDailyGroupSalesReportData()
	{
		return usePreaggregatedDailyGroupSalesReportData;
	}

	public ReportingConfig setUsePreaggregatedDailyGroupSalesReportData(Boolean usePreaggregatedDailyGroupSalesReportData)
	{
		this.usePreaggregatedDailyGroupSalesReportData = usePreaggregatedDailyGroupSalesReportData;
		return this;
	}

	public Boolean getUsePreaggregatedMonthlySalesPerformanceReportData() 
	{
		return usePreaggregatedMonthlySalesPerformanceReportData;
	}

	public ReportingConfig setUsePreaggregatedMonthlySalesPerformanceReportData(Boolean usePreaggregatedMonthlySalesPerformanceReportData)
	{
		this.usePreaggregatedMonthlySalesPerformanceReportData = usePreaggregatedMonthlySalesPerformanceReportData;
		return this;
	}

	public Boolean getUsePreaggregatedAccountBalanceSummaryReportData()
	{
		return usePreaggregatedAccountBalanceSummaryReportData;
	}

	public void setUsePreaggregatedAccountBalanceSummaryReportData(Boolean usePreaggregatedAccountBalanceSummaryReportData) 
	{
		this.usePreaggregatedAccountBalanceSummaryReportData = usePreaggregatedAccountBalanceSummaryReportData;
	}
*/
	public Phrase getSalesSummaryReportNotification()
	{
		return this.salesSummaryReportNotification;
	}

	public ReportingConfig setSalesSummaryReportNotification( Phrase salesSummaryReportNotification )
	{
		this.salesSummaryReportNotification = salesSummaryReportNotification;
		return this;
	}

	public Phrase getMobileMoneyReportNotification()
	{
		return this.mobileMoneyReportNotification;
	}

	public ReportingConfig setMobileMoneyReportNotification( Phrase mobileMoneyReportNotification )
	{
		this.mobileMoneyReportNotification = mobileMoneyReportNotification;
		return this;
	}
	
	public Phrase getSalesSummaryReportEmailSubject()
	{
		return this.salesSummaryReportEmailSubject;
	}

	public ReportingConfig setSalesSummaryReportEmailSubject( Phrase salesSummaryReportEmailSubject )
	{
		this.salesSummaryReportEmailSubject = salesSummaryReportEmailSubject;
		return this;
	}

	public Phrase getSalesSummaryReportEmailBody()
	{
		return this.salesSummaryReportEmailBody;
	}

	public ReportingConfig setSalesSummaryReportEmailBody( Phrase salesSummaryReportEmailBody )
	{
		this.salesSummaryReportEmailBody = salesSummaryReportEmailBody;
		return this;
	}


	public Phrase getIntervalPreviousDayDescription()
	{
		return this.intervalPreviousDayDescription;
	}
	public ReportingConfig setIntervalPreviousDayDescription( Phrase intervalPreviousDayDescription )
	{
		this.intervalPreviousDayDescription = intervalPreviousDayDescription;
		return this;
	}

	public Phrase getIntervalCurrentDayDescription()
	{
		return this.intervalCurrentDayDescription;
	}
	public ReportingConfig setIntervalCurrentDayDescription( Phrase intervalCurrentDayDescription )
	{
		this.intervalCurrentDayDescription = intervalCurrentDayDescription;
		return this;
	}

	public Phrase getIntervalPreviousWeekDescription()
	{
		return this.intervalPreviousWeekDescription;
	}
	public ReportingConfig setIntervalPreviousWeekDescription( Phrase intervalPreviousWeekDescription )
	{
		this.intervalPreviousWeekDescription = intervalPreviousWeekDescription;
		return this;
	}

	public Phrase getIntervalCurrentWeekDescription()
	{
		return this.intervalCurrentWeekDescription;
	}
	public ReportingConfig setIntervalCurrentWeekDescription( Phrase intervalCurrentWeekDescription )
	{
		this.intervalCurrentWeekDescription = intervalCurrentWeekDescription;
		return this;
	}

	public Phrase getIntervalPrevious30DaysDescription()
	{
		return this.intervalPrevious30DaysDescription;
	}
	public ReportingConfig setIntervalPrevious30DaysDescription( Phrase intervalPrevious30DaysDescription )
	{
		this.intervalPrevious30DaysDescription = intervalPrevious30DaysDescription;
		return this;
	}

	public Phrase getIntervalPreviousMonthDescription()
	{
		return this.intervalPreviousMonthDescription;
	}
	public ReportingConfig setIntervalPreviousMonthDescription( Phrase intervalPreviousMonthDescription )
	{
		this.intervalPreviousMonthDescription = intervalPreviousMonthDescription;
		return this;
	}

	public Phrase getIntervalCurrentMonthDescription()
	{
		return this.intervalCurrentMonthDescription;
	}
	public ReportingConfig setIntervalCurrentMonthDescription( Phrase intervalCurrentMonthDescription )
	{
		this.intervalCurrentMonthDescription = intervalCurrentMonthDescription;
		return this;
	}

	public Phrase getIntervalPreviousYearDescription()
	{
		return this.intervalPreviousYearDescription;
	}
	public ReportingConfig setIntervalPreviousYearDescription( Phrase intervalPreviousYearDescription )
	{
		this.intervalPreviousYearDescription = intervalPreviousYearDescription;
		return this;
	}

	public Phrase getIntervalCurrentYearDescription()
	{
		return this.intervalCurrentYearDescription;
	}
	public ReportingConfig setIntervalCurrentYearDescription( Phrase intervalCurrentYearDescription )
	{
		this.intervalCurrentYearDescription = intervalCurrentYearDescription;
		return this;
	}

	public Phrase getIntervalPreviousHourDescription()
	{
		return this.intervalPreviousHourDescription;
	}
	public ReportingConfig setIntervalPreviousHourDescription( Phrase intervalPreviousHourDescription )
	{
		this.intervalPreviousHourDescription = intervalPreviousHourDescription;
		return this;
	}

	public Phrase getIntervalCurrentHourDescription()
	{
		return this.intervalCurrentHourDescription;
	}
	public ReportingConfig setIntervalCurrentHourDescription( Phrase intervalCurrentHourDescription )
	{
		this.intervalCurrentHourDescription = intervalCurrentHourDescription;
		return this;
	}

	public Phrase getIntervalFixedDescription()
	{
		return this.intervalFixedDescription;
	}
	public ReportingConfig setIntervalFixedDescription( Phrase intervalFixedDescription )
	{
		this.intervalFixedDescription = intervalFixedDescription;
		return this;
	}



	public Phrase getSchedulePeriodHourDescription()
	{
		return this.schedulePeriodHourDescription;
	}
	public ReportingConfig setSchedulePeriodHourDescription( Phrase schedulePeriodHourDescription )
	{
		this.schedulePeriodHourDescription = schedulePeriodHourDescription;
		return this;
	}

	public Phrase getSchedulePeriodDayDescription()
	{
		return this.schedulePeriodDayDescription;
	}
	public ReportingConfig setSchedulePeriodDayDescription( Phrase schedulePeriodDayDescription )
	{
		this.schedulePeriodDayDescription = schedulePeriodDayDescription;
		return this;
	}

	public Phrase getSchedulePeriodWeekDescription()
	{
		return this.schedulePeriodWeekDescription;
	}
	public ReportingConfig setSchedulePeriodWeekDescription( Phrase schedulePeriodWeekDescription )
	{
		this.schedulePeriodWeekDescription = schedulePeriodWeekDescription;
		return this;
	}

	public Phrase getSchedulePeriodMonthDescription()
	{
		return this.schedulePeriodMonthDescription;
	}
	public ReportingConfig setSchedulePeriodMonthDescription( Phrase schedulePeriodMonthDescription )
	{
		this.schedulePeriodMonthDescription = schedulePeriodMonthDescription;
		return this;
	}

	public Phrase getSchedulePeriodMinuteDescription()
	{
		return this.schedulePeriodMinuteDescription;
	}
	public ReportingConfig setSchedulePeriodMinuteDescription( Phrase schedulePeriodMinuteDescription )
	{
		this.schedulePeriodMinuteDescription = schedulePeriodMinuteDescription;
		return this;
	}

	public Phrase getReportEmailSubject()
	{
		return this.reportEmailSubject;
	}
	public ReportingConfig setReportEmailSubject( Phrase reportEmailSubject )
	{
		this.reportEmailSubject = reportEmailSubject;
		return this;
	}

	public Phrase getReportEmailBody()
	{
		return this.reportEmailBody;
	}
	public ReportingConfig setReportEmailBody( Phrase reportEmailBody )
	{
		this.reportEmailBody = reportEmailBody;
		return this;
	}


	@Override
	public long uid()
	{
		return serialVersionUID;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	public ReportingConfig setVersion(int version)
	{
		this.version = version;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Post-Load fix up
	//
	// /////////////////////////////////
	@Override
	public void onPostLoad()
	{
		if ( smsConfigurationEnabled == null ) smsConfigurationEnabled = false;
		if ( compressWholesalerPerformanceReportEmail == null ) compressWholesalerPerformanceReportEmail = true;
		if ( compressRetailerPerformanceReportEmail == null ) compressRetailerPerformanceReportEmail = true;
		if ( compressDailyGroupSalesReportEmail == null ) compressDailyGroupSalesReportEmail = false;
		if ( compressDailySalesReportByAreaEmail == null ) compressDailySalesReportByAreaEmail = false;
		if ( compressSalesSummaryReportEmail == null ) compressSalesSummaryReportEmail = false;
		if ( compressMobileMoneySummaryReportEmail == null ) compressMobileMoneySummaryReportEmail = false;
		if ( compressAccountBalanceSummaryReportEmail == null ) compressAccountBalanceSummaryReportEmail = true;
		if ( compressMonthlySalesPerformanceReportEmail == null ) compressMonthlySalesPerformanceReportEmail = true;
		if ( compressionLevel == null ) compressionLevel = java.util.zip.Deflater.DEFAULT_COMPRESSION;
		if ( agentReportCountLimit == null ) agentReportCountLimit = 10;
		if ( agentReportDailyScheduleLimit == null ) agentReportDailyScheduleLimit = 30;
/*
		//Report Aggregation Switches
		if ( usePreaggregatedRetailerReportData == null ) usePreaggregatedRetailerReportData = false;
		if ( usePreaggregatedWholesalerReportData == null ) usePreaggregatedWholesalerReportData = false;
		if ( usePreaggregatedDailySalesSummaryReportData == null ) usePreaggregatedDailySalesSummaryReportData = false;
		if ( usePreaggregatedDailyGroupSalesReportData == null ) usePreaggregatedDailyGroupSalesReportData = false;
		if ( usePreaggregatedMonthlySalesPerformanceReportData == null ) usePreaggregatedMonthlySalesPerformanceReportData = false;
		if ( usePreaggregatedAccountBalanceSummaryReportData == null ) usePreaggregatedAccountBalanceSummaryReportData = false;
*/
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Validation
	//
	// /////////////////////////////////
	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator() //
			.validExpandableText("salesSummaryReportNotification", salesSummaryReportNotification, salesSummaryReportNotificationFields) //
			;

		/**
		 * FIXME
		 * WARNING: We must add a check if the feature is enabled .... If it is NOT enabled ... we should ignore validation
		 *          otherwise it may prevent saving (even though the feature is not present)
		 */
		if (this.smsConfigurationEnabled) {
			validator
				.notLess("smsCurrency", smsCurrency.length(), 0) //
				.notMore("smsCurrency", smsCurrency.length(), 5) //
				.matchesRegex("smsThousandSeparator", smsThousandSeparator, "^([,.]| )?$", "can only be empty, a space, comma or period")
				.matchesRegex("smsDecimalSeparator", smsDecimalSeparator, "^([,.]| )?$", "can only be empty, a space, comma or period")
				.notEquals("smsThousandSeparator", "smsDecimalSeparator", smsThousandSeparator, smsDecimalSeparator);
		}

		validator
			.validExpandableText("salesSummaryReportEmailSubject", salesSummaryReportEmailSubject, listSalesSummaryWithMobileMoneyReportNotificationFields()) //
			.validExpandableText("salesSummaryReportEmailBody", salesSummaryReportEmailBody, listSalesSummaryWithMobileMoneyReportNotificationFields()) //
			.validExpandableText("mobileMoneyReportNotification", mobileMoneyReportNotification, mobileMoneyReportNotificationFields) //
			.validExpandableText("reportEmailSubject", reportEmailSubject, reportEmailSubjectFields) //
			.validExpandableText("reportEmailBody", reportEmailBody, reportEmailBodyFields) //
			.notMore("compressionLevel", compressionLevel, java.util.zip.Deflater.BEST_COMPRESSION) //
			.notLess("compressionLevel", compressionLevel, Math.min(java.util.zip.Deflater.NO_COMPRESSION, java.util.zip.Deflater.DEFAULT_COMPRESSION))
			.notLess("agentReportCountLimit", agentReportCountLimit, 1) //
			;

		return validator.toList();
	}

}
