package cs.service.portal;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.dto.security.LoginSessionData;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Account;
import hxc.ecds.protocol.rest.AgentAccount;
import hxc.ecds.protocol.rest.AgentAccountEx;
import hxc.ecds.protocol.rest.ExResult;

@Service
public class PortalAccountService
{
	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	@Autowired
	private LoginSessionData sessionData;

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

	private String updateFilterAddOwnerCheck(String existingFilter)
	{
		StringBuilder result = null;

		String ownerFilter = String.format("agent.ownerAgentID='%d'", sessionData.getAgentId());

		if (existingFilter==null)
		{
			result = new StringBuilder(ownerFilter);
		}
		else
		{
			result = new StringBuilder(existingFilter);
			result.append('+');
			result.append(ownerFilter);
		}

		return result.toString();
	}

	private String getAgentAccountIdFilter(String agentId)
	{
		return String.format("agentID='%s'", agentId);
	}

//  ----------- Filtered Calls -------------

	public Long listAgentAccountsCount(String filter, String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent/*");

		filter = this.updateFilterAddOwnerCheck(filter);
		RestRequestUtil.standardFilter(uri, filter);

		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);

		Long response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return response;
	}

	public AgentAccount[] listAgentAccounts(String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent");

		filter = this.updateFilterAddOwnerCheck(filter);
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

	public AgentAccount[] searchAgentAccounts(String filter, String search) throws Exception
	{
		AgentAccount[] response = null;

		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent");

		filter = this.updateFilterAddOwnerCheck(filter);
		RestRequestUtil.standardFilter(uri, filter);

		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);

		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentAccount[].class);
		return response;
	}

	public AgentAccount[] searchAgentAccounts(String filter, int offset, int limit, String sort) throws Exception
	{
		AgentAccount[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/agent");
		filter = this.updateFilterAddOwnerCheck(filter);
		RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentAccount[].class);
		return response;
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


//  ----------- Direct Calls -------------

	public Long listAgentAccountsCount() throws Exception
	{
		Long response = listAgentAccountsCount(null, null);
		return response;
	}

	public Long searchAgentAccountsCount(String filter) throws Exception
	{
		Long response = listAgentAccountsCount(filter, null);
		return response;
	}

	public AgentAccount[] listAgentAccounts() throws Exception
	{
		AgentAccount[] response = searchAgentAccounts(null, null);
		return response;
	}


	public AgentAccount[] listAgentAccounts(int offset, int limit, String sort) throws Exception
	{
		AgentAccount[] response = listAgentAccounts(null, null, offset, limit, sort);
		return response;
	}

	public Long listAgentAccountsCount(String search) throws Exception
	{
		Long response = listAgentAccountsCount(null, search);
		return response;
	}

	public AgentAccount[] listAgentAccounts(String search) throws Exception
	{
		AgentAccount[] response = searchAgentAccounts(null, search);
		return response;
	}

	public AgentAccount[] listAgentAccounts(String search, int offset, int limit, String sort) throws Exception
	{
		AgentAccount[] response = listAgentAccounts(null, search, offset, limit, sort);
		return response;
	}

	public AgentAccount getAgentAccount(int agentId) throws Exception
	{
		return getAgentAccount(String.valueOf(agentId));
	}

	public AgentAccount getAgentAccount(String agentId) throws Exception
	{
		if (agentId != null )
		{

			if (agentId.equals(String.valueOf(sessionData.getAgentId())))
			{
				AgentAccount result = restTemplate.execute(restServerUrl+"/agent/"+agentId, HttpMethod.GET, AgentAccount.class);
				return result;
			}
			else
			{
				AgentAccount [] accounts = this.searchAgentAccounts(getAgentAccountIdFilter(agentId), null);
				if (accounts.length > 0)
				{
					return accounts[0];
				}
			}
		}

		// This is a BAD Practice (We need a AgentAccount.NOBODY)
		return null;
	}

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
//		String agentIdFilter = String.format("agentID='%d'", accountId);
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
