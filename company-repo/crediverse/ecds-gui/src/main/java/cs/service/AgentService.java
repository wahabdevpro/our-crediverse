package cs.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.ApplicationDetailsConfiguration;
import cs.config.RestServerConfiguration;
import cs.dto.GuiChangePasswordRequest;
import cs.dto.GuiChangePasswordResponse;
import cs.dto.agents.AgentRulesValiationIssues;
import cs.dto.agents.AgentRulesValiationIssues.IssueType;
import cs.dto.portal.GuiPinRules;
import cs.dto.security.LoginSessionData;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.ExistingAgentInfo;
import hxc.ecds.protocol.rest.ChangePasswordRequest;
import hxc.ecds.protocol.rest.ChangePasswordResponse;
import hxc.ecds.protocol.rest.Tier;
import hxc.ecds.protocol.rest.TransferRule;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.Phrase;

@Service
public class AgentService extends Exportable
{
	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	@Autowired
	private ApplicationDetailsConfiguration appConfig;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private TierService tierService;

	@Autowired
	private TransferRuleService transferRuleService;

	@Autowired
	private ConfigurationService configService;

	private String restServerUrl;
	private String agentProfileUrl;
	private String subAgentProfileUrl;

	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getAgentUrl();
		this.agentProfileUrl = this.restServerUrl+"/profile";
		this.subAgentProfileUrl = this.restServerUrl+"/subprofile";
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
	
	public Agent[] listAgentsByTierType(Integer offset, Integer limit, String tierType) throws Exception
	{
		Agent[] response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl + "/tiertype/" + tierType);
		RestRequestUtil.standardPaging(uri, offset, limit);
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
	
	public void deactivate(String msisdn) throws Exception
	{
		restTemplate.execute(restServerUrl+"/deactivate/by-msisdn/"+msisdn, HttpMethod.PUT, Void.class);
	}

	public ExistingAgentInfo getAgentByMsisdn(String msisdn) throws Exception
	{
		ExistingAgentInfo response = null;
		response = restTemplate.execute(restServerUrl+"/by-msisdn/"+msisdn, HttpMethod.GET, ExistingAgentInfo.class);
		return response;
	}

  public Agent[] getAllAgentsByMsisdn(String msisdn) throws Exception
	{
		Agent[] response = null;
    
		response = restTemplate.execute(restServerUrl+"/tdr-agent/agents-by-msisdn/"+msisdn, HttpMethod.GET, Agent[].class);
		return response;
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

		result.put("stateNameActive", ac.listDefaultPinNotificationFields());
		result.put("stateNameSuspended", ac.listDefaultPinNotificationFields());
		result.put("stateNameDeactivated", ac.listDefaultPinNotificationFields());
		result.put("stateNamePermanent", ac.listDefaultPinNotificationFields());
		
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

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		String response = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/csv");
		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardPaging(uri, offset, limit);
		//RestRequestUtil.standardSorting(uri, sort);
		response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

	public Long listAgentsCount(String filter, String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl+"/*");
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);
		if (search != null && !search.isEmpty())
			RestRequestUtil.standardSearch(uri, search);
		Long response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
		return response;
	}

	public Agent getLoggedInAgentProfile() throws Exception
	{
		if (sessionData.getAgentId() != null)
			return getAgent(String.valueOf(sessionData.getAgentId()));
		else
			throw new Exception("MissingApiConstants.FORBIDDEN");
	}

	public void updateLoggedinAgent(Agent updatedAgent) throws Exception
	{
		if (sessionData.getAgentId() != null && sessionData.getAgentId().intValue() == updatedAgent.getId())
		{
			restTemplate.execute(agentProfileUrl, HttpMethod.PUT, updatedAgent, Void.class);
		}
		else
			throw new Exception("MissingApiConstants.FORBIDDEN");
	}


	/**
	 * Validate there are rules between 2 agents
	 * @param agentAId	0/null => From Root
	 * @param agentBId	Needs to be specified
	 */
	public AgentRulesValiationIssues validateAgentTransferRulesSet(Integer agentAId, Integer agentBId) throws Exception
	{
		AgentRulesValiationIssues result = new AgentRulesValiationIssues();

		Agent agentA = (agentAId == null || agentAId == 1)? this.getRootAgent() : this.getAgent(agentAId);
		Tier tierA = (agentAId == null || agentAId == 1)? tierService.getRootTier() : tierService.getTier( agentA.getTierID() );

		Agent agentB = this.getAgent(agentBId);
		Tier tierB = tierService.getTier(agentB.getTierID());

		TransferRule[] rules = transferRuleService.getActiveRulesBetweenTiers(tierA.getId(), tierB.getId());

		if ( rules.length == 0 )
		{
			result.addIssue(IssueType.NO_RULES_FOUND);
		}

		for (int i = 0; i < rules.length; ++i )
		{
			TransferRule rule = rules[i];
			if ((rule.getTargetGroupID() != null) && (rule.getTargetGroupID() != agentB.getGroupID()))
			{
				result.addIssue(IssueType.TARGET_GROUP_DIFFERS, rule.getName());
				continue;
			}
			if ((rule.getTargetServiceClassID() != null) && (rule.getTargetServiceClassID() != agentB.getServiceClassID()))
			{
				result.addIssue(IssueType.SERVICE_CLASS_DIFFERS, rule.getName());
				continue;
			}
			Calendar now = Calendar.getInstance();
			if ( (rule.getDaysOfWeek() != null) && (rule.getDaysOfWeek() != TransferRule.DOW_ALL) )
			{
				int dow = rule.getDaysOfWeek().intValue();
				int cdow = 0;
				switch( now.get(Calendar.DAY_OF_WEEK) )
				{
				case Calendar.SUNDAY: cdow = TransferRule.DOW_SUNDAYS; break;
				case Calendar.MONDAY:  cdow = TransferRule.DOW_MONDAYS; break;
				case Calendar.TUESDAY: cdow = TransferRule.DOW_TUESDAYS; break;
				case Calendar.WEDNESDAY: cdow = TransferRule.DOW_WEDNESDAYS; break;
				case Calendar.THURSDAY: cdow = TransferRule.DOW_THURSDAYS; break;
				case Calendar.FRIDAY: cdow = TransferRule.DOW_FRIDAYS; break;
				case Calendar.SATURDAY: cdow = TransferRule.DOW_SATURDAYS; break;
				}

				if ( (cdow & dow) == 0 )
				{
					Date date = new Date();
					SimpleDateFormat dateFormat = new SimpleDateFormat("EEEEE");
					String sdow = dateFormat.format(date);
					result.addIssue(IssueType.RULE_NOT_ALLOWED_ON, rule.getName(), sdow);
					continue;
				}
			}

			int chour = now.get(Calendar.HOUR_OF_DAY);
			int cminute = now.get(Calendar.MINUTE);
			int csecond = now.get(Calendar.SECOND);
			int currentTime = chour * 3600 + cminute * 60 + csecond;

			if ( rule.getStartTimeOfDay() != null )
			{
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(rule.getStartTimeOfDay());
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int minute = calendar.get(Calendar.MINUTE);
				int second = calendar.get(Calendar.SECOND);
				int st = hour * 3600 + minute * 60 + second;

				if ( st > currentTime )
				{
					result.addIssue( IssueType.RULE_NOT_ALLOWED_BEFORE, rule.getName(), String.format("%02d:%02d:%02d", hour, minute, second) );
					continue;
				}
			}

			if ( rule.getEndTimeOfDay() != null )
			{
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(rule.getEndTimeOfDay());
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int minute = calendar.get(Calendar.MINUTE);
				int second = calendar.get(Calendar.SECOND);
				int et = hour * 3600 + minute * 60 + second;

				if ( et < currentTime )
				{
					result.addIssue( IssueType.RULE_NOT_ALLOWED_AFTER, rule.getName(), String.format("%02d:%02d:%02d", hour, minute, second) );
					continue;
				}
			}
			result.incrementAllowedRulesCount();
		}

		return result;
	}

	public void updateSubAgent(Agent updatedAgent) throws Exception
	{
		if (sessionData.getAgentId() != null && sessionData.getAgentId().intValue() == updatedAgent.getOwnerAgentID())
		{
			restTemplate.execute(subAgentProfileUrl, HttpMethod.PUT, updatedAgent, Void.class);
		}
		else
			throw new Exception("MissingApiConstants.FORBIDDEN");
	}

	public GuiPinRules extractPinChangeRules() throws Exception
	{
		AgentsConfig agentsConfig = configService.getAgentsConfiguration();
		GuiPinRules result = GuiPinRules.extractPinRuleSet(agentsConfig);
		return result;
	}

	public GuiChangePasswordResponse changePassword(GuiChangePasswordRequest gui) throws Exception
	{
		try {
			ChangePasswordRequest request = new ChangePasswordRequest();//getChangePasswordRequestFromGuiChangePasswordRequest(gui);
			request.setEntityID(gui.getEntityId());
			request.setNewPassword(gui.getNewPassword());
			request.setCurrentPassword(gui.getCurrentPassword());
			ChangePasswordResponse tsResponse = restTemplate.postForObject(restServerUrl + "/change_password", request, ChangePasswordResponse.class);
			GuiChangePasswordResponse response = new GuiChangePasswordResponse();
			response.setReturnCode(tsResponse.getReturnCode());
			return response;
		} catch(Exception e) {
			throw e;
		}
	}
}
