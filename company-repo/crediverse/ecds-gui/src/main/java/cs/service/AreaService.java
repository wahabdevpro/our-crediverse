package cs.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Area;

@Service
public class AreaService extends Exportable
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
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getAreaUrl();
			configured = true;
		}
	}

	public Long countAreas(String search) throws Exception
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

	public Area getArea(String areaid) throws Exception
	{
		Area result = null;
		result = restTemplate.execute(restServerUrl+"/"+areaid, HttpMethod.GET, Area.class);
		return result;
	}

	public Area getArea(int areaid) throws Exception
	{
		Area result = null;
		result = restTemplate.execute(restServerUrl+"/"+ Integer.toString(areaid), HttpMethod.GET, Area.class);
		return result;
	}

	public Area [] listAreas() throws Exception
	{
		Area[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Area[].class);
		return response;
	}

	public Area [] listAreas(String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);

		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);

		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);

		if (sort != null)
			RestRequestUtil.standardSorting(uri, sort);

		Area[] response = null;
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Area[].class);
		return response;
	}

	public Area[] listAreas(String filter) throws Exception {
		Area[] response = null;
		StringBuilder searchUrl = new StringBuilder(restServerUrl);
		if (filter != null && filter.length() > 0)
		{
			searchUrl.append("?filter=name:'%");
			searchUrl.append(filter);
			searchUrl.append("%'");
		}
		response = restTemplate.execute(searchUrl.toString(), HttpMethod.GET, Area[].class);
		return response;
	}

	public Area[] listAreas(int start, int length) throws Exception
	{
		Area[] response = null;
		StringBuilder urlString = new StringBuilder(restServerUrl);
		urlString.append("?");
		urlString.append("first=");
		urlString.append(String.valueOf(start));
		urlString.append("&max=");
		urlString.append(String.valueOf(length));

		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Area[].class);
		return response;
	}

	public void create(Area newArea) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newArea, Void.class);
	}

	public void delete(String areaId) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+areaId, HttpMethod.DELETE, Void.class);
	}

	public void update(Area updatedArea) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedArea, Void.class);
	}

	public Area getAreaByName(String name) throws Exception
	{
		Area response = null;
		Area[]responseList = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		//RestRequestUtil.standardFilter(uri, "name='"+name+"'+companyID='2'");
		responseList = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Area[].class);
		response = responseList[0];

		//response = restTemplate.execute(restServerUrl+"/"+agentId, HttpMethod.GET, Agent.class);
		return response;
	}

	public Map<Integer, Area> getAreaMap() throws Exception
	{
		Area[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Area[].class);
		Map<Integer, Area> result = new HashMap<>();
		for(Area dep : response)
		{
			result.put(dep.getId(), dep);
		}
		return result;
	}
}
