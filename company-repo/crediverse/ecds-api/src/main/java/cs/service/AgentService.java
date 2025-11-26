package cs.service;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import cs.dto.non_airtime.AgentDetails;
import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import cs.config.RestServerConfiguration;
import cs.dto.GuiAgent;
import cs.dto.GuiChangePasswordRequest;
import cs.dto.GuiChangePasswordResponse;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.ChangePasswordRequest;
import hxc.ecds.protocol.rest.ChangePasswordResponse;
import hxc.ecds.protocol.rest.Tier;
import hxc.ecds.protocol.rest.Violation;
import hxc.ecds.protocol.rest.config.AgentsConfig;

@Service
public class AgentService
{
	private static final Logger logger = LoggerFactory.getLogger(AgentService.class);

	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	@Autowired
	private LoginSessionData sessionData;
	
	@Autowired
	private TierService tierService;

	/*@Autowired
	private ConfigurationService configService;*/

	private String agentUrl;
	private String agentProfileUrl;
	private String agentUserUrl;
	private String agentServiceUrl;

	@PostConstruct
	public void configure()
	{
		this.agentUrl = restServerConfig.getRestServer() + restServerConfig.getAgentUrl();
		this.agentProfileUrl = this.agentUrl+"/profile";
		this.agentUserUrl = restServerConfig.getRestServer() + restServerConfig.getAgentUserUrl();
		this.agentServiceUrl = restServerConfig.getRestServer() + restServerConfig.getAgentServiceUrl();
	}

	public Agent getAgent(int agentId) throws Exception
	{
		return getAgent(String.valueOf(agentId));
	}
	
	public GuiAgent getGuiAgent(int agentId) throws Exception
	{
		Agent agent = getAgent(String.valueOf(agentId));
		Tier tier = tierService.getTier(agent.getTierID());
		return new GuiAgent(agent, tier);
	}

	public Agent getAgent(String agentId) throws Exception
	{
		Agent response = null;
		response = restTemplate.execute(agentUrl+"/"+agentId, HttpMethod.GET, Agent.class);
		return response;
	}

	public AgentDetails getGuiAgentByMsisdn(String msisdn) throws Exception {
		Agent agent = restTemplate.execute(agentServiceUrl + "/" + msisdn + "/details", HttpMethod.GET, Agent.class);
		Tier tier = tierService.getTier(agent.getTierID());
		return new AgentDetails(agent, tier);
	}
	
	public void update(GuiAgent updatedAgent) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		Agent agent = getAgent(String.valueOf(updatedAgent.getAccountID()));
		if(agent != null)
		{
			//Update only the relevant properties
			if(updatedAgent.getAltPhoneNumber() != null && !updatedAgent.getAltPhoneNumber().isEmpty())
				agent.setAltPhoneNumber(updatedAgent.getAltPhoneNumber());
			else {
				String error = String.format("Email altPhoneNumber %s is not valid.", updatedAgent.getAltPhoneNumber());
				violations.add(new Violation(Violation.INVALID_VALUE, "altPhoneNumber", null, error));
			}
			
			if(updatedAgent.getEmail() != null && !updatedAgent.getEmail().isEmpty() && EmailValidator.getInstance().isValid(updatedAgent.getEmail()))
				agent.setEmail(updatedAgent.getEmail());
			else {
				String error = String.format("Email address %s is not valid.", updatedAgent.getEmail());
				violations.add(new Violation(Violation.INVALID_VALUE, "email", null, error));
			}
			
			if(updatedAgent.getFirstName() != null && !updatedAgent.getFirstName().isEmpty())
				agent.setFirstName(updatedAgent.getFirstName());
			else 
			{
				String error = String.format("Field firstName %s is invalid.", updatedAgent.getFirstName());
				violations.add(new Violation(Violation.INVALID_VALUE, "firstName", null, error));
			}
			
			if(updatedAgent.getInitials() != null && !updatedAgent.getInitials().isEmpty())
				agent.setInitials(updatedAgent.getInitials());
			else 
			{
				String error = String.format("Field initials %s is invalid.", updatedAgent.getInitials());
				violations.add(new Violation(Violation.INVALID_VALUE, "firstName", null, error));
			}
			
			if(updatedAgent.getLanguage() != null)
				agent.setLanguage(updatedAgent.getLanguage().getVal());
			else 
			{
				String error = String.format("Field language %s is invalid.", updatedAgent.getLanguage());
				violations.add(new Violation(Violation.INVALID_VALUE, "language", null, error));
			}
			
			if(updatedAgent.getSurname() != null && !updatedAgent.getSurname().isEmpty())
				agent.setSurname(updatedAgent.getSurname());
			else 
			{
				String error = String.format("Field surname %s is invalid.", updatedAgent.getSurname());
				violations.add(new Violation(Violation.INVALID_VALUE, "surname", null, error));
			}
			
			if(updatedAgent.getTitle() != null && !updatedAgent.getTitle().isEmpty())
				agent.setTitle(updatedAgent.getTitle());
			else 
			{
				String error = String.format("Field title %s is invalid.", updatedAgent.getTitle());
				violations.add(new Violation(Violation.INVALID_VALUE, "title", null, error));
			}
			
			if(!violations.isEmpty())
				throw new GuiValidationException(violations);
			
			restTemplate.execute(agentUrl, HttpMethod.PUT, agent, Void.class);
		} else {
			//This could only happen if the agent was removed after the access token was issued.
			String error = String.format("Could not update agent. Agent not found. agentID=%d", updatedAgent.getAccountID());
			logger.error(error);
		}
	}

	public AgentsConfig getConfiguration() throws Exception
	{
		AgentsConfig config = restTemplate.execute(agentUrl + "/config", HttpMethod.GET, AgentsConfig.class);

		// Remove CompanyID from scheduledAccountDumpDirectory (User should not see)
		int indexVariable = config.getScheduledAccountDumpDirectory().indexOf(AgentsConfig.COMPANY_ID);
		if (indexVariable > 0)
		{
			String sadDir = config.getScheduledAccountDumpDirectory().substring(0, indexVariable - 1);
			config.setScheduledAccountDumpDirectory(sadDir);
		}

		return config;
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

	public GuiChangePasswordResponse changePassword(int userID, GuiChangePasswordRequest apiRequest) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		if(!apiRequest.getNewPassword().equals(apiRequest.getRepeatPassword()))
		{
			String error = String.format("Password fields do not match.");
			violations.add(new Violation(Violation.INVALID_VALUE, "newPassword", null, error));
			violations.add(new Violation(Violation.INVALID_VALUE, "repeatPassword", null, error));
		}
		if(!violations.isEmpty())
			throw new GuiValidationException(violations);
		try {
			ChangePasswordRequest request = new ChangePasswordRequest();//getChangePasswordRequestFromGuiChangePasswordRequest(gui);
			request.setEntityID(userID);			
			request.setNewPassword(apiRequest.getNewPassword());
			request.setCurrentPassword(apiRequest.getCurrentPassword());
			ChangePasswordResponse tsResponse = restTemplate.postForObject(agentUserUrl + "/change_password", request, ChangePasswordResponse.class);
			GuiChangePasswordResponse response = new GuiChangePasswordResponse(tsResponse);
			return response;
		} catch(Exception e) {
			throw e;
		}
	}
}
