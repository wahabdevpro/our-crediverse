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
import hxc.ecds.protocol.rest.Department;

@Service
public class DepartmentService extends Exportable
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
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getDepartmenturl();
			configured = true;
		}
	}

	public Long countDepartments(String search) throws Exception
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

	public Department getDepartment(String departmentId) throws Exception
	{
		Department result = null;
		result = restTemplate.execute(restServerUrl+"/"+departmentId, HttpMethod.GET, Department.class);
		return result;
	}

	public Department getDepartment(int departmentId) throws Exception
	{
		Department result = null;
		result = restTemplate.execute(restServerUrl+"/"+ Integer.toString(departmentId), HttpMethod.GET, Department.class);
		return result;
	}

	public Department [] listDepartments() throws Exception
	{
		Department[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Department[].class);
		return response;
	}

	public Department[] listDepartments(int start, int length) throws Exception
	{
		Department[] response = null;
		StringBuilder urlString = new StringBuilder(restServerUrl);
		urlString.append("?");
		urlString.append("first=");
		urlString.append(String.valueOf(start));
		urlString.append("&max=");
		urlString.append(String.valueOf(length));

		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Department[].class);
		return response;
	}

	public void create(Department newDepartment) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newDepartment, Void.class);
	}

	public void delete(String departmentId) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+departmentId, HttpMethod.DELETE, Void.class);
	}

	public void update(Department updatedDepartment) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedDepartment, Void.class);
	}

	public void updateDetails(Department updatedDepartment) throws Exception
	{
		Department currentDepartment = getDepartment(String.valueOf(updatedDepartment.getId()));
		if (currentDepartment != null)
		{
			currentDepartment.setName(updatedDepartment.getName());
			//currentDepartment.setDescription(updatedDepartment.getDescription());
			restTemplate.execute(restServerUrl, HttpMethod.PUT, currentDepartment, Void.class);
		}
	}

	public Department getDepartmentByName(String name) throws Exception
	{
		Department response = null;
		Department[]responseList = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		responseList = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Department[].class);
		response = responseList[0];

		//response = restTemplate.execute(restServerUrl+"/"+agentId, HttpMethod.GET, Agent.class);
		return response;
	}

	public Map<Integer, Department> getDepartmentMap() throws Exception
	{
		Department[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Department[].class);
		Map<Integer, Department> result = new HashMap<>();
		for(Department dep : response)
		{
			result.put(dep.getId(), dep);
		}
		return result;
	}
}
