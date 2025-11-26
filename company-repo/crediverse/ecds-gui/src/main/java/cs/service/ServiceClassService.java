package cs.service;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.ServiceClass;

@Service
public class ServiceClassService extends Exportable
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
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getServiceClassesUrl();
			configured = true;
		}
	}

	public Long countServiceClasses(String search) throws Exception
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

	public ServiceClass[] listServiceClasses() throws Exception
	{
		ServiceClass[] response = restTemplate.execute(restServerUrl, HttpMethod.GET, ServiceClass[].class);
		return response;
	}

	public ServiceClass[] listServiceClasses(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardSearch(uri, search);
		ServiceClass[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, ServiceClass[].class);
		return response;
	}

	public ServiceClass[] listServiceClasses(int offset, int limit) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardPaging(uri, offset, limit);
		ServiceClass[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, ServiceClass[].class);
		return response;
	}


	public ServiceClass getServiceClass(int serviceClassId) throws Exception
	{
		return getServiceClass( String.valueOf(serviceClassId) );
	}

	public ServiceClass getServiceClass(String serviceClassId) throws Exception
	{
		int scId = Integer.parseInt(serviceClassId);

//		TODO: This should work ... Isn't (looks like possible back-end issue
//		ServiceClass response = restTemplate.execute(String.format("%s/%s", restServerUrl, serviceClassId), HttpMethod.GET, ServiceClass.class);

		//TODO: Remove this Junk
		ServiceClass[] scs = listServiceClasses();
		for(ServiceClass sc : scs) {
			if (sc.getId() == scId)
				return sc;
		}

		return null;
	}

	public void create(ServiceClass newServiceClass) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newServiceClass, Void.class);
	}

	public void update(ServiceClass updatedServiceClass) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedServiceClass, Void.class);
	}

	public void delete(String serviceClassId) throws Exception
	{
		try {
			restTemplate.execute(String.format("%s/%s", restServerUrl, serviceClassId), HttpMethod.DELETE, Void.class);
		} catch(Exception e) {
			throw e;
		}
	}

	public Map<Integer, String> getServiceClassNameMap(Optional<String> type, Optional<String> query) throws Exception
	{
		Map<Integer, String>classMap = new TreeMap<Integer,String>();
		ServiceClass[] serviceClasses = null;

		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			serviceClasses = listServiceClasses(query.get());
		}
		else
		{
			serviceClasses = listServiceClasses();
		}

		if (serviceClasses != null)
		{
			Arrays.asList(serviceClasses).forEach(serviceClass ->{
				classMap.put(serviceClass.getId(), serviceClass.getName());
			});
		}

		return classMap;
	}
}
