package hxc.services.ecds.reports.sales_summary;

import java.io.IOException;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

import hxc.services.ecds.model.ReportSpecification;
import hxc.services.ecds.reports.Report;

@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
public class SalesSummaryReportSpecification extends hxc.ecds.protocol.rest.reports.SalesSummaryReportSpecification
{
	private static ObjectMapper mapper;

	static
	{
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
		//mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		SimpleModule module = new SimpleModule();
		module.addSerializer(java.sql.Date.class, new DateSerializer());
		mapper.registerModule(module);
	}

	public SalesSummaryReportSpecification()
	{
	}

	public SalesSummaryReportSpecification(ReportSpecification specification) throws IOException
	{
		this.amend(specification);
		Objects.requireNonNull(mapper, "mapper may not be null");
		Objects.requireNonNull(specification, "specification may not be null");
		this.parameters = new SalesSummaryReportParameters();
		this.parameters.amend(mapper.readValue(specification.getParameters(), SalesSummaryReportParameters.class));
	}

	public ReportSpecification toReportSpecification() throws Exception
	{
		ReportSpecification reportSpecification = new ReportSpecification();
		reportSpecification.amend(this);
		reportSpecification.setType(Report.Type.SALES_SUMMARY.toString());
		reportSpecification.setParameters(mapper.writeValueAsString(this.parameters));
		return reportSpecification;
	}
}
