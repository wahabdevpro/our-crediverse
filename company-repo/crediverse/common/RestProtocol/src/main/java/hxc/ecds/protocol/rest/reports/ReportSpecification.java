package hxc.ecds.protocol.rest.reports;

import java.util.ArrayList;
import java.util.List;

import hxc.ecds.protocol.rest.IValidatable;
import hxc.ecds.protocol.rest.Violation;

public class ReportSpecification implements IReportSpecification, IValidatable
{
	protected int id;
	protected int companyID;
	protected Integer agentID = 0;
	protected int version;
	protected String name;
	protected String description;
	protected Report.Originator originator;
	protected List<? extends ReportSchedule> schedules;

	@Override
	public ReportSpecification amend(IReportSpecification other)
	{
		this.id = other.getId();
		this.companyID = other.getCompanyID();
		this.agentID = other.getAgentID();
		this.version = other.getVersion();
		this.name = other.getName();
		this.description = other.getDescription();
		this.originator = other.getOriginator();
		this.schedules = other.getSchedules();
		return this;
	}

	@Override
	public int getId()
	{
		return this.id;
	}

	@Override
	public ReportSpecification setId(int id)
	{
		this.id = id;
		return this;
	}

	@Override
	public int getCompanyID()
	{
		return this.companyID;
	}

	@Override
	public ReportSpecification setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Override
	public Integer getAgentID()
	{
		return this.agentID;
	}

	@Override
	public ReportSpecification setAgentID(Integer agentID)
	{
		this.agentID = agentID;
		return this;
	}

	@Override
	public int getVersion()
	{
		return this.version;
	}

	@Override
	public ReportSpecification setVersion(int version)
	{
		this.version = version;
		return this;
	}

	@Override
	public String getName()
	{
		return this.name;
	}

	@Override
	public ReportSpecification setName(String name)
	{
		this.name = name;
		return this;
	}

	@Override
	public String getDescription()
	{
		return this.description;
	}

	@Override
	public ReportSpecification setDescription(String description)
	{
		this.description = description;
		return this;
	}

	@Override
	public Report.Originator getOriginator()
	{
		return this.originator;
	}

	@Override
	public ReportSpecification setOriginator( Report.Originator originator )
	{
		this.originator = originator;
		return this;
	}

	public List<? extends ReportSchedule> getSchedules()
	{
		return this.schedules;
	}

	public ReportSpecification setSchedules(List<? extends ReportSchedule> schedules)
	{
		this.schedules = schedules;
		return this;
	}

	public List<Violation> validate()
	{
		return new ArrayList<Violation>();
	}

	public String describe(String extra)
	{
		return String.format("%s@%s(id = %s, companyID = %s, agentID = %s, version = %s, name = '%s', description = '%s', originator = '%s', schedules = %s%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			id, companyID, agentID, version, name, description, originator, schedules,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}
}
