package hxc.services.ecds.reports;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

import hxc.services.ecds.model.ReportSpecification;

public class AccountBalanceSummaryReportSpecification extends hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportSpecification
{
	private static ObjectMapper mapper;

	static
	{
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		SimpleModule module = new SimpleModule();
		module.addSerializer(java.sql.Date.class, new DateSerializer());
		mapper.registerModule(module);
	}

	public AccountBalanceSummaryReportSpecification()
	{
	}

	public AccountBalanceSummaryReportSpecification(ReportSpecification specification) throws IOException
	{
		this.amend(specification);
		Objects.requireNonNull(mapper, "mapper may not be null");
		Objects.requireNonNull(specification, "specification may not be null");
		this.parameters = new AccountBalanceSummaryReportParameters();
		this.parameters.amend(mapper.readValue(specification.getParameters(), AccountBalanceSummaryReportParameters.class));
	}

	public ReportSpecification toReportSpecification() throws Exception
	{
		ReportSpecification reportSpecification = new ReportSpecification();
		reportSpecification.amend(this);
		reportSpecification.setType(Report.Type.ACCOUNT_BALANCE_SUMMARY.toString());
		reportSpecification.setParameters(mapper.writeValueAsString(this.parameters));
		return reportSpecification;
	}
}
