package hxc.services.ecds.reports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

public class AccountBalanceSummaryReportParameters extends hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportParameters
{
	final static Logger logger = LoggerFactory.getLogger(AccountBalanceSummaryReportParameters.class);
	private static ObjectMapper mapper;

	static
	{
		mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		SimpleModule module = new SimpleModule();
		module.addSerializer(java.sql.Date.class, new DateSerializer());
		mapper.registerModule(module);
	}

	public AccountBalanceSummaryReportParameters()
	{
		super();
	}

	public AccountBalanceSummaryReportParameters(String filterString, String sortString)
		throws Exception
	{
		super();
		logger.info("filterString = {}, sortString = {}", filterString, sortString);
		if (filterString != null) this.filter = AccountBalanceSummaryReport.parseFilterString("+" + filterString);
		if (sortString != null) this.sort = AccountBalanceSummaryReport.parseSortString(sortString);
	}

	public String toJson() throws Exception
	{
		return toJson(this);
	}

	public static String toJson(hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportParameters parameters) throws Exception
	{
		return mapper.writeValueAsString(parameters);
	}
}
