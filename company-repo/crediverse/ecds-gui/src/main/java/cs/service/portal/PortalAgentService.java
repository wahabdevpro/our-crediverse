package cs.service.portal;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.ApplicationDetailsConfiguration;
import cs.config.RestServerConfiguration;
import cs.dto.security.LoginSessionData;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.Phrase;

@Service
public class PortalAgentService
{
	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	@Autowired
	private ApplicationDetailsConfiguration appConfig;

	@Autowired
	private LoginSessionData sessionData;

	private boolean configured = false;

	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getAgentUrl();
			configured = true;
		}
	}

	public Agent[] listAgents() throws Exception
	{
		Agent[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Agent[].class);
		return response;
	}

	public Agent[] listAgents(int offset, int limit, String sort) throws Exception
	{
		Agent[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Agent[].class);
		return response;
	}

	public Agent[] listAgents(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardSearch(uri, search);
		Agent[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Agent[].class);
		return response;
	}

	public Agent[] listAgents(String search, int offset, int limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		Agent[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Agent[].class);
		return response;
	}

	public Agent[] listAgents(String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if ( sort != null && !sort.isEmpty() )
			RestRequestUtil.standardSorting(uri, sort);
		Agent[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Agent[].class);
		return response;
	}

	public Agent[] searchAgents(String filter) throws Exception
	{
		Agent[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, filter);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Agent[].class);
		return response;
	}

	public Agent[] searchAgents(String filter, int offset, int limit, String sort) throws Exception
	{
		Agent[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardPaging(uri, offset, limit);
		RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Agent[].class);
		return response;
	}

	public Agent getAgent(int agentId) throws Exception
	{
		return getAgent(String.valueOf(agentId));
	}

	public Agent getAgent(String agentId) throws Exception
	{
		Agent response = null;
		response = restTemplate.execute(restServerUrl+"/"+agentId, HttpMethod.GET, Agent.class);
		return response;
	}

	public Agent getRootAgent() throws Exception
	{
		Agent response = null;
		response = restTemplate.execute(restServerUrl+"/root", HttpMethod.GET, Agent.class);
		return response;
	}

	public void create(Agent newAgent) throws Exception
	{
		newAgent.setState(Agent.STATE_ACTIVE);
		if (newAgent.getDomainAccountName() != null && newAgent.getDomainAccountName().length() == 0)
		{
			newAgent.setDomainAccountName(null);
		}
		if (newAgent.getAccountNumber() != null && newAgent.getAccountNumber().isEmpty())
		{
			newAgent.setAccountNumber(Agent.AUTO_NUMBER);
		}
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newAgent, Void.class);
	}

	public void update(Agent updatedAgent) throws Exception
	{

		if (updatedAgent.getDomainAccountName() != null && updatedAgent.getDomainAccountName().length() == 0)
		{
			updatedAgent.setDomainAccountName(null);
		}
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedAgent, Void.class);
	}

	public void delete(String agentId) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+agentId, HttpMethod.DELETE, Void.class);
	}

	public AgentsConfig getConfiguration() throws Exception
	{
		AgentsConfig config = restTemplate.execute(restServerUrl + "/config", HttpMethod.GET, AgentsConfig.class);

		// Remove CompanyID from scheduledAccountDumpDirectory (User should not see)
		int indexVariable = config.getScheduledAccountDumpDirectory().indexOf(AgentsConfig.COMPANY_ID);
		if (indexVariable > 0)
		{
			String sadDir = config.getScheduledAccountDumpDirectory().substring(0, indexVariable - 1);
			config.setScheduledAccountDumpDirectory(sadDir);
		}

		return config;
	}

	public void updateConfiguration(AgentsConfig updatedAgentsConfig) throws Exception
	{
		try {
			updatedAgentsConfig.setScheduledAccountDumpDirectory( String.format("%s/%s", updatedAgentsConfig.getScheduledAccountDumpDirectory(), AgentsConfig.COMPANY_ID) );
			restTemplate.execute(restServerUrl+ "/config", HttpMethod.PUT, updatedAgentsConfig, Void.class);
		} catch(Exception e) {
			throw e;
		}

	}

	public Map<String, Phrase[]> getConfigurationVariables()
	{
		Map<String, Phrase[]> result = new HashMap<>();

		AgentsConfig ac = new AgentsConfig();
		result.put("newPinNotification", ac.listNewPinNotificationFields());
		result.put("depletionNotification", ac.listDepletionFields());
		result.put("reActivatedNotification", ac.listReActivatedNotificationFields());
		result.put("deActivatedNotification", ac.listDeActivatedNotificationFields());
		result.put("suspendedNotification", ac.listSuspendedNotificationFields());
		result.put("defaultPinNotification", ac.listDefaultPinNotificationFields());
		result.put("passwordChangeEmailSubject", ac.listPasswordEmailFields());
		result.put("passwordChangeEmailBody", ac.listPasswordEmailFields());
		result.put("passwordResetEmailSubject", ac.listPasswordEmailFields());
		result.put("passwordResetEmailBody", ac.listPasswordEmailFields());

		return result;
	}

	public Agent getAgentByName(String name) throws Exception
	{
		Agent response = null;
		Agent[]responseList = null;
		String companyID = Integer.toString(appConfig.getCompanyid());
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, "accountNumber='"+name+"'+companyID='"+ companyID +"'");
		responseList = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Agent[].class);
		response = responseList[0];

		//response = restTemplate.execute(restServerUrl+"/"+agentId, HttpMethod.GET, Agent.class);
		return response;
	}

	public String listAgentsCsv(long offset, int limit) throws Exception
	{
		String response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");
		RestRequestUtil.standardPaging(uri, offset, limit);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

	public String listAgentsCsv(String search, long offset, int limit, String sort) throws Exception
	{

		String response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);
		//RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

	public Agent getLoggedInAgentProfile() throws Exception
	{
		if (sessionData.getAgentId() != null)
			return getAgent(String.valueOf(sessionData.getAgentId()));
		else
			throw new Exception("MissingApiConstants.FORBIDDEN");
	}

}
