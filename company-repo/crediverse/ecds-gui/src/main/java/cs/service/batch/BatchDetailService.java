package cs.service.batch;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Batch;

@Service
public class BatchDetailService
{
	private String batchHistoryUrl;
	private String batchStatusUrl;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private CsRestTemplate restTemplate;

	@PostConstruct
	public void configure()
	{
		// TODO URL will be ecds/batch/csv/history/<id> where <id> is the batch ID.
		this.batchHistoryUrl = restServerConfig.getRestServer() + restServerConfig.getBatchhistoryurl();
		this.batchStatusUrl = restServerConfig.getRestServer() + restServerConfig.getBatchstatusurl();
	}

	public Integer listTransactionsCount(int id) throws Exception
	{
		Integer count = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(this.batchStatusUrl+"/"+String.valueOf(id));

		Batch batchInfo = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Batch.class);
		if (batchInfo != null)
		{
			int linecount = batchInfo.getLineCount();
			/*
			 * Note that a value of 1 means a file with only a header row, which is 0 transactions.
			 */
			count = (linecount <= 1)?Integer.valueOf(0):Integer.valueOf(linecount - 1);
		}
		return count;
	}

	public List<String> getCsvHeader(int id) throws Exception
	{
		List<String>headingList = new ArrayList<String>();
		String[] headings = listTransactionsAsCsvInternal(id, 0L, 1, false);
		if (headings.length == 1)
		{
			for (String heading : headings[0].split(","))
			{
				headingList.add(heading);
			}
		}
		else
		{
			// Handle error
		}
		return headingList;
	}

	private long getOffsetValue(Long val, boolean withMin)
	{
		int min = (withMin)?1:0;
		int result = min;
		if (val != null && val > min) result = val.intValue();
		return result;
	}

	private int getLimitValue(Integer val)
	{
		int result = 0;
		if (val != null && val > 0) result = val.intValue();
		return result;
	}

	private String[] listTransactionsAsCsvInternal(int id, Long offset, Integer limit, boolean withMin) throws Exception
	{
		String[] response = null;
		Long start = Long.valueOf(getOffsetValue(offset, withMin));
		Integer length = Integer.valueOf(getLimitValue(limit));

		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(batchHistoryUrl+"/"+String.valueOf(id));
		RestRequestUtil.standardPaging(uri, start, length);



		String csvData = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		if (csvData == null)
		{
			response = new String[0];
		}
		else
		{
			response = csvData.split("\n");
		}
		return response;
	}


	public String[] listTransactionsAsCsv(int id, Long offset, Integer limit) throws Exception
	{
		return listTransactionsAsCsvInternal(id, offset, limit, true);
	}
}
