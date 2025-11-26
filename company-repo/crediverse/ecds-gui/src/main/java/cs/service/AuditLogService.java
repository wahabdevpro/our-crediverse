package cs.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.AuditEntry;

@Service
public class AuditLogService extends Exportable
{
	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	private boolean configured = false;

	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getAuditLogUrl();
			configured = true;
		}
	}

	public AuditEntry getAuditEntry(String entryId) throws Exception
	{
		AuditEntry response = null;
		response = restTemplate.execute(restServerUrl+"/"+entryId, HttpMethod.GET, AuditEntry.class);
		return response;
	}

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		String response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);

		RestRequestUtil.standardFilter(uri, filter);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

	public Long count(String search) throws Exception
	{
		return count(null, search);
	}

	public Long count(String filter, String search) throws Exception
	{
		Long count = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/*");
		RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardSearch(uri, search);
		count = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return count;
	}

	public AuditEntry[] listAuditEntries(String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		AuditEntry[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AuditEntry[].class);
		return response;
	}

	public AuditEntry getAuditEntry(int entryId) throws Exception
	{
		return getAuditEntry(String.valueOf(entryId));
	}
}
