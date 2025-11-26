package hxc.ecds.protocol.rest.reports;

import java.util.List;

public interface IReportSpecification
{
	public static final int NAME_MAX_LENGTH = 50;
	public static final int DESCRIPTION_MAX_LENGTH = 80;
	public static final int ORIGINATOR_MAX_LENGTH = 64;

	/*
	public enum Originator
	{
		USER,
		MINIMUM_REQUIRED_DATA;
	}
	*/

	public IReportSpecification amend(IReportSpecification other);

	public Report.Originator getOriginator();
	public IReportSpecification setOriginator(Report.Originator originator);

	public int getId();
	public IReportSpecification setId(int id);

	public int getCompanyID();
	public IReportSpecification setCompanyID(int companyID);
	
	public Integer getAgentID();
	public IReportSpecification setAgentID(Integer agentID);

	public int getVersion();
	public IReportSpecification setVersion(int version);

	public String getName();
	public IReportSpecification setName(String name);

	public String getDescription();
	public IReportSpecification setDescription(String description);

	public List<? extends ReportSchedule> getSchedules();
	public IReportSpecification setSchedules(List<? extends ReportSchedule> schedules);
}
