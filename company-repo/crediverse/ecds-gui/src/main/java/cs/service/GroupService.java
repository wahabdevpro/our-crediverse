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
import hxc.ecds.protocol.rest.Group;

@Service
public class GroupService extends Exportable
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
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getGroupurl();
			configured = true;
		}
	}

	public Long countGroups(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/*");
		if (search != null && search.length() > 0) RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
	}

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardFilter(uri, filter);
		String response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;

	}

	public Group getGroup(String groupid) throws Exception
	{
		Group result = null;
		result = restTemplate.execute(restServerUrl+"/"+groupid, HttpMethod.GET, Group.class);
		return result;
	}

	public Group[] listGroups() throws Exception
	{
		//generateData();
		Group[] response = null;

		//response = dummyData.toArray(new Group[this.dummyData.size()]);
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Group[].class);
		return response;
	}

	public Group[] listGroups(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardSearch(uri, search);
		Group[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Group[].class);
		return response;
	}

	public Group[] listGroups(int offset, int limit) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardPaging(uri, offset, limit);
		Group[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Group[].class);
		return response;
	}

	public void create(Group newGroup) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newGroup, Void.class);
	}

	public void delete(String groupid) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+groupid, HttpMethod.DELETE, Void.class);
	}

	public void update(Group newGroup) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newGroup, Void.class);
	}
}
