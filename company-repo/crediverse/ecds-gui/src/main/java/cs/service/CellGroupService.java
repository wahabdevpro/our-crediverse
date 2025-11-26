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
import hxc.ecds.protocol.rest.CellGroup;

@Service
public class CellGroupService extends Exportable
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
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getCellGroupUrl();
			configured = true;
		}
	}

	public Long countCellGroups(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/*");
		if (search != null && search.length() > 0) RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
	}

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");

		RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);

		String response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

	public CellGroup getCellGroup(String cellGroupId) throws Exception
	{
		CellGroup result = null;
		result = restTemplate.execute(restServerUrl+"/"+cellGroupId, HttpMethod.GET, CellGroup.class);
		return result;
	}

	public CellGroup getCellGroup(int cellGroupId) throws Exception
	{
		CellGroup result = null;
		result = restTemplate.execute(restServerUrl+"/"+ Integer.toString(cellGroupId), HttpMethod.GET, CellGroup.class);
		return result;
	}

	public CellGroup [] listCellGroups() throws Exception
	{
		CellGroup[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, CellGroup[].class);
		return response;
	}

	public CellGroup[] listCellGroups(int start, int length) throws Exception
	{
		CellGroup[] response = null;
		StringBuilder urlString = new StringBuilder(restServerUrl);
		urlString.append("?");
		urlString.append("first=");
		urlString.append(String.valueOf(start));
		urlString.append("&max=");
		urlString.append(String.valueOf(length));

		response = restTemplate.execute(restServerUrl, HttpMethod.GET, CellGroup[].class);
		return response;
	}

	public CellGroup[] listCellGroups(String filter) throws Exception {
		CellGroup[] response = null;
		StringBuilder searchUrl = new StringBuilder(restServerUrl);
		if (filter != null && filter.length() > 0)
		{
			searchUrl.append("?filter=name:'%");
			searchUrl.append(filter);
			searchUrl.append("%'");
		}
		response = restTemplate.execute(searchUrl.toString(), HttpMethod.GET, CellGroup[].class);
		return response;
	}

	public void create(CellGroup newCellGroup) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newCellGroup, Void.class);
	}

	public void delete(String cellGroupId) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+cellGroupId, HttpMethod.DELETE, Void.class);
	}

	public void update(CellGroup updatedCellGroup) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedCellGroup, Void.class);
	}

	public void updateDetails(CellGroup updatedCellGroup) throws Exception
	{
		CellGroup currentCellGroup = getCellGroup(String.valueOf(updatedCellGroup.getId()));
		if (currentCellGroup != null)
		{
			currentCellGroup.setName(updatedCellGroup.getName());
			//currentCellGroup.setDescription(updatedCellGroup.getDescription());
			restTemplate.execute(restServerUrl, HttpMethod.PUT, currentCellGroup, Void.class);
		}
	}

	public CellGroup getCellGroupByName(String name) throws Exception
	{
		CellGroup response = null;
		CellGroup[]responseList = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		responseList = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, CellGroup[].class);
		response = responseList[0];

		//response = restTemplate.execute(restServerUrl+"/"+agentId, HttpMethod.GET, Agent.class);
		return response;
	}

	public Map<Integer, CellGroup> getCellGroupMap() throws Exception
	{
		CellGroup[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, CellGroup[].class);
		Map<Integer, CellGroup> result = new HashMap<>();
		for(CellGroup cg : response)
		{
			result.put(cg.getId(), cg);
		}
		return result;
	}
}
