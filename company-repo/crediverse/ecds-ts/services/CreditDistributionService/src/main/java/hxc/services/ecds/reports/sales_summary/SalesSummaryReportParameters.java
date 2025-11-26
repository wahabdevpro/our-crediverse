package hxc.services.ecds.reports.sales_summary;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

@JsonIgnoreProperties({ "handler", "hibernateLazyInitializer" })
public class SalesSummaryReportParameters extends hxc.ecds.protocol.rest.reports.SalesSummaryReportParameters
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

	public SalesSummaryReportParameters()
	{
		super();
	}

	public String toJson() throws Exception
	{
		return toJson(this);
	}

	public static String toJson(hxc.ecds.protocol.rest.reports.SalesSummaryReportParameters parameters) throws Exception
	{
		return mapper.writeValueAsString(parameters);
	}
}
