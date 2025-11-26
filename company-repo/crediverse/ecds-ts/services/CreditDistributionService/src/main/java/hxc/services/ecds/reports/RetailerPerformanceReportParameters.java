package hxc.services.ecds.reports;

import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

import hxc.ecds.protocol.rest.util.TimeInterval;

public class RetailerPerformanceReportParameters extends hxc.ecds.protocol.rest.reports.RetailerPerformanceReportParameters
{
	final static Logger logger = LoggerFactory.getLogger(RetailerPerformanceReportParameters.class);
	private static ObjectMapper mapper;

	static
	{
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		SimpleModule module = new SimpleModule();
		module.addSerializer(java.sql.Date.class, new DateSerializer());
		mapper.registerModule(module);
	}

	public RetailerPerformanceReportParameters()
	{
		super();
	}

	public RetailerPerformanceReportParameters(String filterString, String sortString, String timeIntervalStart, String timeIntervalEnd, String relativeTimeRangeCode)
		throws Exception
	{
		super();
		logger.info("filterString = {}, sortString = {}", filterString, sortString);
		if (filterString != null) this.filter = RetailerPerformanceReport.parseFilterString("+" + filterString);
		if (sortString != null) this.sort = RetailerPerformanceReport.parseSortString(sortString);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		if (relativeTimeRangeCode != null)
		{
			this.relativeTimeRange = Report.RelativeTimeRange.valueOf(relativeTimeRangeCode);
		}
		else if (timeIntervalStart != null || timeIntervalEnd != null)
		{
			this.timeInterval = new TimeInterval();
			if (timeIntervalStart != null) this.timeInterval.setStartDate(sdf.parse(timeIntervalStart));
			if (timeIntervalEnd != null) this.timeInterval.setEndDate(sdf.parse(timeIntervalEnd));
		}
	}

	public String toJson() throws Exception
	{
		return toJson(this);
	}

	public static String toJson(hxc.ecds.protocol.rest.reports.RetailerPerformanceReportParameters parameters) throws Exception
	{
		return mapper.writeValueAsString(parameters);
	}
}
