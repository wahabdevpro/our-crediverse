package cs.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import hxc.ecds.protocol.rest.UpdateTransferRulesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.TransferRule;

@Service
public class TransferRuleService extends Exportable
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
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getTransferRuleUrl();
			configured = true;
		}
	}

	public TransferRule[] listTransferRules(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardSearch(uri, search);
		TransferRule[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, TransferRule[].class);
		return response;
	}

	public TransferRule[] listTransferRules() throws Exception
	{
		return listTransferRules(null);
	}

	public Long countRules(String search) throws Exception
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

	public void create(TransferRule newTransferRule) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newTransferRule, UpdateTransferRulesResponse.class);
	}

	public void delete(String transferRuleId) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+transferRuleId, HttpMethod.DELETE, Void.class);
	}

	public void update(TransferRule updatedRule) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedRule, UpdateTransferRulesResponse.class);
	}

	public TransferRule getRule(String ruleid) throws Exception
	{
		TransferRule result = null;
		result = restTemplate.execute(restServerUrl+"/"+ruleid, HttpMethod.GET, TransferRule.class);
		return result;
	}

	public TransferRule[] getTierIncomingRules(String tierId) throws Exception
	{
		TransferRule[] response = null;
		StringBuilder urlString = new StringBuilder(restServerUrl);
		urlString.append("?");
		urlString.append(String.format("filter=targetTierID='%s'", tierId));
		response = restTemplate.execute(urlString.toString(), HttpMethod.GET, TransferRule[].class);

		return response;
	}

	public TransferRule[] getTierOutgoingRules(String tierId) throws Exception
	{
		TransferRule[] response = null;
		StringBuilder urlString = new StringBuilder(restServerUrl);
		urlString.append("?");
		urlString.append(String.format("filter=sourceTierID='%s'", tierId));
		response = restTemplate.execute(urlString.toString(), HttpMethod.GET, TransferRule[].class);
		return response;
	}

	public TransferRule[] getActiveRulesBetweenTiers(int sourceTierId, int targetTierId) throws Exception
	{
		TransferRule[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, String.format(
			"filter=state='A'+sourceTierID='%d'+targetTierID='%d'",
			sourceTierId, targetTierId));
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, TransferRule[].class);
		return response;
	}

//	public ObjectNode getDropdownData()
//	{
//		//groupService.
//		//tierService
//		//serviceClassService
//
//		return null;
//	}

	public Map<Integer, String> getTransferRuleNameMap(Optional<String> type, Optional<String> query) throws Exception
	{
		Map<Integer, String> resultMap = new TreeMap<Integer,String>();
		TransferRule[] transferRules = null;

		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			transferRules = listTransferRules(query.get());
		}
		else
		{
			transferRules = listTransferRules();
		}

		if (transferRules != null)
		{
			Arrays.asList(transferRules).forEach(transferRule ->{
				resultMap.put(transferRule.getId(), transferRule.getName());
			});
		}

		return resultMap;
	}

	public BigDecimal getMaxCumulativeBonusPercentage() throws Exception
	{
		BigDecimal response = restTemplate.execute(String.format("%s/max_cumulative_bonus", restServerUrl), HttpMethod.GET, BigDecimal.class);
		return response;
	}
}
