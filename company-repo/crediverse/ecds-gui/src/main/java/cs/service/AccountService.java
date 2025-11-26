package cs.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Account;
import hxc.ecds.protocol.rest.AgentAccount;
import hxc.ecds.protocol.rest.AgentAccountEx;
import hxc.ecds.protocol.rest.ExResult;

@Service
public class AccountService extends Exportable
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
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getAccountUrl();
			configured = true;
		}
	}

	///////////

	public ExResult<AgentAccountEx> listAgentAccountsEx() throws Exception
	{
		ParameterizedTypeReference<ExResult<AgentAccountEx>> type = new ParameterizedTypeReference<ExResult<AgentAccountEx>>() {};
		ExResult<AgentAccountEx> result = restTemplate.execute(restServerUrl + "/agent_ex", HttpMethod.GET, type);
		return result;
	}

	public ExResult<AgentAccountEx> listAgentAccountsEx(String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent_ex");
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);

		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);

		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);

		if (sort != null)
			RestRequestUtil.standardSorting(uri, sort);

		ParameterizedTypeReference<ExResult<AgentAccountEx>> type = new ParameterizedTypeReference<ExResult<AgentAccountEx>>() {};
		ExResult<AgentAccountEx> result = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, type);
		return result;
	}

	///////////

	public Long listAgentAccountsCount() throws Exception
	{
		Long response = null;
		response = restTemplate.execute(restServerUrl + "/agent/*", HttpMethod.GET, Long.class);
		return response;
	}

	public AgentAccount[] listAgentAccounts() throws Exception
	{
		AgentAccount[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, AgentAccount[].class);
		return response;
	}

	public AgentAccount[] listAgentAccounts(int offset, int limit, String sort) throws Exception
	{
		AgentAccount[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent");
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentAccount[].class);
		return response;
	}

	public Long listAgentAccountsCount(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent/*");
		RestRequestUtil.standardSearch(uri, search);
		Long response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return response;
	}

	public Long listAgentAccountsCount(String filter, String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent/*");
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		Long response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return response;
	}

	public AgentAccount[] listAgentAccounts(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent");
		RestRequestUtil.standardSearch(uri, search);
		AgentAccount[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentAccount[].class);
		return response;
	}

	public AgentAccount[] listAgentAccounts(String search, int offset, int limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent");
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		AgentAccount[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentAccount[].class);
		return response;
	}

	public AgentAccount[] listAgentAccounts(String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent");
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);

		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);

		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);

		if (sort != null)
			RestRequestUtil.standardSorting(uri, sort);

		AgentAccount[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentAccount[].class);
		return response;
	}

	public Long searchAgentAccountsCount(String filter) throws Exception
	{
		Long response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent/*");
		RestRequestUtil.standardFilter(uri, filter);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return response;
	}

	public AgentAccount[] searchAgentAccounts(String filter) throws Exception
	{
		AgentAccount[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent");
		RestRequestUtil.standardFilter(uri, filter);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentAccount[].class);
		return response;
	}

	public AgentAccount[] searchAgentAccounts(String filter, int offset, int limit, String sort) throws Exception
	{
		AgentAccount[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent");
		RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentAccount[].class);
		return response;
	}

	public AgentAccount getAgentAccount(int agentId) throws Exception
	{
		return getAgentAccount(String.valueOf(agentId));
	}

	public AgentAccount getAgentAccount(String agentId) throws Exception
	{
		AgentAccount response = null;
		response = restTemplate.execute(restServerUrl+"/agent/"+agentId, HttpMethod.GET, AgentAccount.class);
		return response;
	}

	///////////

	public Account[] listAccounts() throws Exception
	{
		Account[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Account[].class);
		return response;
	}

	public Account getAccount(int accountId) throws Exception
	{
		return getAccount(String.valueOf(accountId));
	}

	public Account getAccount(String accountId) throws Exception
	{
		Account response = null;
		response = restTemplate.execute(restServerUrl+"/"+accountId, HttpMethod.GET, Account.class);
		return response;
	}

	public void create(Account newAccount) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newAccount, Void.class);
	}

	public void update(Account updatedAccount) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedAccount, Void.class);
	}

	public void delete(String accountId) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+accountId, HttpMethod.DELETE, Void.class);
	}
}
