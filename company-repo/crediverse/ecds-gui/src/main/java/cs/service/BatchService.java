package cs.service;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.template.CsRestTemplate;
import cs.utility.BatchUtility;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Batch;

@Service
public class BatchService
{
	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	private boolean configured = false;

	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getBatchUrl();
			configured = true;
		}
	}

	public Batch[] listBatches() throws Exception
	{
		Batch[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Batch[].class);
		return response;
	}

	public Long listBatchesCount(String filter, String search) throws Exception
	{
		Long count = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/*");
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		count = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return count;
	}

	public Batch[] listBatches(String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		Batch[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null)
			RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Batch[].class);
		return response;
	}

	public String listBatchesAsCsv(String filter, String search, long offset, int limit) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardPaging(uri, offset, limit);
		String response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

	public String getCsv(HttpServletResponse response, int batchId) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/historic/csv/" + batchId);
		BatchUtility.setExportHeaders(response, "batch-" + batchId + ".csv");
		String data = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return data;
	}

	public Batch getBatch(int batchId) throws Exception
	{
		return getBatch(String.valueOf(batchId));
	}

	public Batch getBatch(String batchId) throws Exception
	{
		Batch response = null;
		response = restTemplate.execute(restServerUrl+"/"+batchId, HttpMethod.GET, Batch.class);

		return response;
	}
}
