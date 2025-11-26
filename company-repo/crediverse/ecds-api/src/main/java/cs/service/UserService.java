package cs.service;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.apache.commons.validator.routines.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import cs.config.RestServerConfiguration;
import cs.dto.GuiUser;
import cs.dto.error.GuiValidationException;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.AgentUser;
import hxc.ecds.protocol.rest.Violation;

@Service
public class UserService
{
	private static final Logger logger = LoggerFactory.getLogger(UserService.class);

	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;
	
	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getAgentUserUrl();
	}

	public AgentUser getAgentUser(int agentUserId) throws Exception
	{
		return getAgentUser(String.valueOf(agentUserId));
	}
	
	public GuiUser getGuiUser(int agentUserId) throws Exception
	{
		AgentUser agentUser = getAgentUser(String.valueOf(agentUserId));
		GuiUser user = new GuiUser(agentUser);
		return user;
	}

	public AgentUser getAgentUser(String agentUserId) throws Exception
	{
		AgentUser response = null;
		response = restTemplate.execute(restServerUrl+"/"+agentUserId, HttpMethod.GET, AgentUser.class);
		return response;
	}
	
	public void update(GuiUser updatedUser) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();	
		AgentUser agentUser = getAgentUser(String.valueOf(updatedUser.getUserID()));
		if(agentUser != null)
		{
			//Update only the relevant properties
			if(updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty() && EmailValidator.getInstance().isValid(updatedUser.getEmail()))
				agentUser.setEmail(updatedUser.getEmail());
			else {
				String error = String.format("field email %s is invalid.", updatedUser.getEmail());
				violations.add(new Violation(Violation.INVALID_VALUE, "email", null, error));
			}
			
			if(updatedUser.getFirstName() != null && !updatedUser.getFirstName().isEmpty())
				agentUser.setFirstName(updatedUser.getFirstName());	
			else 
			{
				String error = String.format("Field firstName %s is invalid.", updatedUser.getFirstName());
				violations.add(new Violation(Violation.INVALID_VALUE, "firstName", null, error));
			}
			
			if(updatedUser.getInitials() != null && !updatedUser.getInitials().isEmpty())
				agentUser.setInitials(updatedUser.getInitials());
			else
			{
				String error = String.format("Field initials %s is invalid.", updatedUser.getInitials());
				violations.add(new Violation(Violation.INVALID_VALUE, "initials", null, error));
			}
			
			if(updatedUser.getLanguage() != null)
				agentUser.setLanguage(updatedUser.getLanguage().getVal());
			else
			{
				String error = String.format("Field language %s is invalid.", updatedUser.getLanguage());
				violations.add(new Violation(Violation.INVALID_VALUE, "language", null, error));
			}
			
			if(updatedUser.getSurname() != null && !updatedUser.getSurname().isEmpty())
				agentUser.setSurname(updatedUser.getSurname());
			else
			{
				String error = String.format("Field surname %s is invalid.", updatedUser.getSurname());
				violations.add(new Violation(Violation.INVALID_VALUE, "surname", null, error));
			}
			
			if(updatedUser.getTitle() != null && !updatedUser.getTitle().isEmpty())
				agentUser.setTitle(updatedUser.getTitle());
			else
			{
				String error = String.format("Field title %s is invalid.", updatedUser.getTitle());
				violations.add(new Violation(Violation.INVALID_VALUE, "title", null, error));
			}
			
			if(updatedUser.getMobileNumber() != null && !updatedUser.getMobileNumber().isEmpty())
				agentUser.setMobileNumber(updatedUser.getMobileNumber());
			else 
			{
				String error = String.format("Field mobileNumber %s is invalid.", updatedUser.getMobileNumber());
				violations.add(new Violation(Violation.INVALID_VALUE, "mobileNumber", null, error));
			}
			
			if(!violations.isEmpty())
				throw new GuiValidationException(violations);
			restTemplate.execute(restServerUrl, HttpMethod.PUT, agentUser, Void.class);
		} else {
			//TODO: Return 404 (Not found) and log
			logger.error("Could not update user. User not found. userID={}", updatedUser.getUserID());		
		}
	}
}
