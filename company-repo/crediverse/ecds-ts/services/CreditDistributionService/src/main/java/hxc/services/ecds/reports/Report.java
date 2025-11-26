package hxc.services.ecds.reports;

import java.util.Date;

import hxc.ecds.protocol.rest.AgentUser;
import hxc.ecds.protocol.rest.WebUser;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.ReportingConfig;
import hxc.ecds.protocol.rest.reports.ReportSchedule;
import hxc.ecds.protocol.rest.reports.ReportSpecification;
import hxc.ecds.protocol.rest.util.TimeInterval;

public class Report
	extends hxc.ecds.protocol.rest.reports.Report
{
	protected Report()
	{
		super();
	}

	public static interface IResultFieldFactory
	{
		public Class<? extends IResultField> getResultFieldClass();
		public IResultField fromIdentifier(String identifier) throws Exception;
	}

	public static interface IFilterFieldFactory
	{
		public Class<? extends IFilterField> getFilterFieldClass();
		public IFilterField fromIdentifier(String identifier) throws Exception;
	}

	public static interface IFilterFieldValueFactory
	{
		public Class<? extends IFilterField> getFilterFieldClass();
		public Object fromString(IFilterField filterField, String value) throws Exception;
	}

	/*
	public static interface IFilterFieldValueFactory<FieldType extends IFilterField>
	{
		public Class<? extends IFilterField> getFilterFieldClass();
		public Object fromString(FieldType field, String string);
	}

	public static class ValueOfFilterFieldValueFactory<ValueType extends Object>
	{
		public Class<ValueType> getFilterFieldValueClass(){ return ValueType.class; }
		public ValueType fromString(String value) throws Exception
		{
			
		}
	}
	*/
	public static Phrase getSchedulePeriodDescription(ReportingConfig reportingConfig, ReportSchedule.Period period)
	{
		switch( period )
		{
			case HOUR:
				return reportingConfig.getSchedulePeriodHourDescription();
			case DAY:
				return reportingConfig.getSchedulePeriodDayDescription();
			case WEEK:
				return reportingConfig.getSchedulePeriodWeekDescription();
			case MONTH:
				return reportingConfig.getSchedulePeriodMonthDescription();
			case MINUTE:
				return reportingConfig.getSchedulePeriodMinuteDescription();
		}
		throw new IllegalArgumentException(String.format("Unsupported ReportSchedule.Period value %s", period));
	}

	public static Phrase getIntervalDescription(ReportingConfig reportingConfig, Report.RelativeTimeRange relativeTimeRange)
	{
		if ( relativeTimeRange == null ) return reportingConfig.getIntervalFixedDescription();
		switch( relativeTimeRange )
		{
			case PREVIOUS_DAY:
				return reportingConfig.getIntervalPreviousDayDescription();
			case CURRENT_DAY:
				return reportingConfig.getIntervalCurrentDayDescription();
			case PREVIOUS_WEEK:
				return reportingConfig.getIntervalPreviousWeekDescription();
			case CURRENT_WEEK:
				return reportingConfig.getIntervalCurrentWeekDescription();
			case PREVIOUS_30DAYS:
				return reportingConfig.getIntervalPrevious30DaysDescription();
			case PREVIOUS_MONTH:
				return reportingConfig.getIntervalPreviousMonthDescription();
			case CURRENT_MONTH:
				return reportingConfig.getIntervalCurrentMonthDescription();
			case PREVIOUS_YEAR:
				return reportingConfig.getIntervalPreviousYearDescription();
			case CURRENT_YEAR:
				return reportingConfig.getIntervalCurrentYearDescription();
			case PREVIOUS_HOUR:
				return reportingConfig.getIntervalPreviousHourDescription();
			case CURRENT_HOUR:
				return reportingConfig.getIntervalCurrentHourDescription();
		}
		throw new IllegalArgumentException(String.format("Unsupported Report.RelativeTimeRange value %s", relativeTimeRange));
	}

	public static class EmailData
	{
		private Date date;
		private TimeInterval timeInterval;
		private Report.RelativeTimeRange relativeTimeRange;
		private ReportSpecification reportSpecification;
		private ReportSchedule reportSchedule;
		private WebUser webUser;
		private AgentUser agentUser;
		private ReportingConfig reportingConfig;

		private String recipientTitle;
		private String recipientFirstName;
		private String recipientSurname;
		private String recipientInitials;

		public Date getDate()
		{
			return this.date;
		}
		public EmailData setDate( Date date )
		{
			this.date = date;
			return this;
		}

		public TimeInterval getTimeInterval()
		{
			return this.timeInterval;
		}
		public EmailData setTimeInterval( TimeInterval timeInterval )
		{
			this.timeInterval = timeInterval;
			return this;
		}

		public Report.RelativeTimeRange getRelativeTimeRange()
		{
			return this.relativeTimeRange;
		}
		public EmailData setRelativeTimeRange( Report.RelativeTimeRange relativeTimeRange )
		{
			this.relativeTimeRange = relativeTimeRange;
			return this;
		}

		public ReportSpecification getReportSpecification()
		{
			return this.reportSpecification;
		}
		public EmailData setReportSpecification( ReportSpecification reportSpecification )
		{
			this.reportSpecification = reportSpecification;
			return this;
		}

		public ReportSchedule getReportSchedule()
		{
			return this.reportSchedule;
		}
		public EmailData setReportSchedule( ReportSchedule reportSchedule )
		{
			this.reportSchedule = reportSchedule;
			return this;
		}

		public WebUser getWebUser()
		{
			return this.webUser;
		}
		public EmailData setWebUser( WebUser webUser )
		{
			this.webUser = webUser;
			this.recipientTitle = this.webUser.getTitle();
			this.recipientFirstName = this.webUser.getFirstName();
			this.recipientSurname = this.webUser.getSurname();
			this.recipientInitials = this.webUser.getInitials();
			return this;
		}
		public AgentUser getAgentUser()
		{
			return this.agentUser;
		}
		public EmailData setAgentUser( AgentUser agentUser )
		{
			this.agentUser = agentUser;
			this.recipientTitle = this.agentUser.getTitle();
			this.recipientFirstName = this.agentUser.getFirstName();
			this.recipientSurname = this.agentUser.getSurname();
			this.recipientInitials = this.agentUser.getInitials();
			return this;
		}

		public ReportingConfig getReportingConfig()
		{
			return this.reportingConfig;
		}

		public EmailData setReportingConfig( ReportingConfig reportingConfig )
		{
			this.reportingConfig = reportingConfig;
			return this;
		}

		public Phrase getIntervalDescription()
		{
			return Report.getIntervalDescription(this.reportingConfig, this.relativeTimeRange);
		}

		public Phrase getSchedulePeriodDescription()
		{
			return Report.getSchedulePeriodDescription(this.reportingConfig, this.reportSchedule.getPeriod());
		}
		
		public String getRecipientTitle()
		{
			return this.recipientTitle;
		}

		public EmailData setRecipientTitle( String recipientTitle )
		{
			this.recipientTitle = recipientTitle;
			return this;
		}

		public String getRecipientFirstName()
		{
			return this.recipientFirstName;
		}

		public EmailData setRecipientFirstName( String recipientFirstName )
		{
			this.recipientFirstName = recipientFirstName;
			return this;
		}

		public String getRecipientSurname()
		{
			return this.recipientSurname;
		}

		public EmailData setRecipientSurname( String recipientSurname )
		{
			this.recipientSurname = recipientSurname;
			return this;
		}

		public String getRecipientInitials()
		{
			return this.recipientInitials;
		}

		public EmailData setRecipientInitials( String recipientInitials )
		{
			this.recipientInitials = recipientInitials;
			return this;
		}

	}
}
