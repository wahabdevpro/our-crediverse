package cs.service;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.ApplicationDetailsConfiguration;
import cs.config.RestServerConfiguration;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Tier;

@Service
public class TierService extends Exportable
{
	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	@Autowired
	private ApplicationDetailsConfiguration appConfig;

	private boolean configured = false;

	private String restServerUrl;






	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getTierUrl();
			configured = true;
		}
	}

	public Long countTiers(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/*");
		if (search != null && search.length() > 0) RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
	}

	public Tier[] listTiers() throws Exception
	{
		Tier[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Tier[].class);
		return response;
	}

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");
		if (search != null && search.length() > 0) RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);
		if (filter != null && filter.length() > 0) RestRequestUtil.standardFilter(uri, filter);

		String response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

	public Map<Integer, Tier> tierMap() throws Exception
	{
		Tier[] tiers = restTemplate.execute(restServerUrl, HttpMethod.GET, Tier[].class);
		Map<Integer, Tier> tierMap = Arrays.asList(tiers)
										.stream()
										.collect(Collectors.toMap(Tier::getId, t-> t));
		return tierMap;
	}

	public Tier getTier(int tierId) throws Exception
	{
		return getTier(String.valueOf(tierId));
	}

	public Tier getTier(String tierId) throws Exception
	{
		Tier response = null;
		response = restTemplate.execute(restServerUrl+"/"+tierId, HttpMethod.GET, Tier.class);
		return response;
	}

	public void create(Tier newTier) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newTier, Void.class);
	}

	public void update(Tier updatedTier) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedTier, Void.class);
	}

	public void delete(String tierId) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+tierId, HttpMethod.DELETE, Void.class);
	}

	public Tier getRootTier() throws Exception
	{
		Tier response = null;
		response = restTemplate.execute(restServerUrl+"/root", HttpMethod.GET, Tier.class);
		return response;
	}

	/*
	 * first, max. sort=group+name-
	 * filter=name="tosh"+group:"%ment"
	 * search=jon
	 *
	 */
	public Tier[] listTiers(String filter) throws Exception {
		Tier[] response = null;
		StringBuilder searchUrl = new StringBuilder(restServerUrl);
		if (filter != null && filter.length() > 0)
		{
			searchUrl.append("?filter=name:'%");
			searchUrl.append(filter);
			searchUrl.append("%'");
		}
		response = restTemplate.execute(searchUrl.toString(), HttpMethod.GET, Tier[].class);
		return response;
	}
	
	public Tier[] listTiersByType(String type) throws Exception {
		Tier[] response = null;
		
		StringBuilder searchUrl = new StringBuilder(restServerUrl);
		if (type != null && type.length() > 0)
		{
			searchUrl.append("?filter=type:'");
			searchUrl.append(type);
			searchUrl.append("'");
		}
		response = restTemplate.execute(searchUrl.toString(), HttpMethod.GET, Tier[].class);
		return response;
	}

	public Tier getTierByName(String name) throws Exception
	{
		Tier response = null;
		Tier[]responseList = null;
		String companyID = Integer.toString(appConfig.getCompanyid());
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, "name='"+name+"'+companyID='"+ companyID +"'");
		responseList = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Tier[].class);
		response = responseList[0];

		//response = restTemplate.execute(restServerUrl+"/"+agentId, HttpMethod.GET, Agent.class);
		return response;
	}

	public Map<Integer, String> tierListMap(Optional<String> type, Optional<String> query, Optional<Integer> tierID) throws Exception
		{

			Tier[] tiers = null;
			Map<Integer, String>tierMap = new TreeMap<Integer,String>();
			if (type.isPresent() && query.isPresent() && type.get().equals("query"))
			{
				tiers = listTiers(query.get());
			}
			else
			{
				tiers = listTiers();
			}

			if (tiers != null)
			{
				Arrays.asList(tiers).forEach(tier ->{
					if ( !tierID.isPresent() || ( tier.getId() == tierID.get().intValue() ) )
						tierMap.put(tier.getId(), tier.getName());
				});
			}
			return tierMap;
		}
}
