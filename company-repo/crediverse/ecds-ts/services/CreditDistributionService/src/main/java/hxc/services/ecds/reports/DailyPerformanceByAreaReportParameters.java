package hxc.services.ecds.reports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

public class DailyPerformanceByAreaReportParameters extends hxc.ecds.protocol.rest.reports.DailyPerformanceByAreaReportParameters
{
	final static Logger logger = LoggerFactory.getLogger(DailyPerformanceByAreaReportParameters.class);
	private static ObjectMapper mapper;

	static
	{
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		SimpleModule module = new SimpleModule();
		module.addSerializer(java.sql.Date.class, new DateSerializer());
		mapper.registerModule(module);
	}

	public DailyPerformanceByAreaReportParameters()
	{
		super();
	}

	public DailyPerformanceByAreaReportParameters(String filterString, String sortString, String relativeTimeRangeCode)
		throws Exception
	{
		super();
		logger.info("filterString = {}, sortString = {}", filterString, sortString);
		if (relativeTimeRangeCode != null)
			this.relativeTimeRange = Report.RelativeTimeRange.valueOf(relativeTimeRangeCode);
		
	}

	public String toJson() throws Exception
	{
		return toJson(this);
	}

	public static String toJson(hxc.ecds.protocol.rest.reports.DailyPerformanceByAreaReportParameters parameters) throws Exception
	{
		return mapper.writeValueAsString(parameters);
	}
}
