package cs.controller;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.GuiChangePasswordRequest;
import cs.dto.GuiChangePasswordResponse;
import cs.dto.GuiPasswordRules;
import cs.dto.GuiUser;
import cs.dto.security.LoginSessionData;
import cs.service.AgentService;
import cs.service.UserService;
import hxc.ecds.protocol.rest.config.AgentsConfig;

import static cs.utility.Common.checkAgentIdForNull;

@RestController
@RequestMapping("/api/account/user")
public class UserController 
{
	@Autowired //ask @Configuration-marked class for this
	private UserService userService;
	
	@Autowired
	private AgentService agentService;
	
	@Resource(name="tokenStore")
	private TokenStore tokenStore;
	
	@Autowired
	private LoginSessionData sessionData;
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiUser getUserProfile() throws Exception
	{
		Integer userID = sessionData.getAgentUserID();
		checkAgentIdForNull(userID);
		GuiUser user = userService.getGuiUser(userID);
		return user;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public void updateUserProfile(@RequestBody(required = true) GuiUser updatedUser) throws Exception
	{
		Integer userID = sessionData.getAgentUserID();
		checkAgentIdForNull(userID);
		updatedUser.setUserID(userID);
		userService.update(updatedUser);
	}
	
	@RequestMapping(value="/password/rules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiPasswordRules getPasswordRules() throws Exception
	{
		AgentsConfig agentsConfig = agentService.getConfiguration();
		return new GuiPasswordRules(agentsConfig);
	}
	
	@RequestMapping(value="/password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiChangePasswordResponse changePassword(@RequestBody(required = true) GuiChangePasswordRequest changePasswordRequest) throws Exception
	{
		Integer userID = sessionData.getAgentUserID();
		checkAgentIdForNull(userID);
		GuiChangePasswordResponse changePasswordResponse = agentService.changePassword(userID, changePasswordRequest);
		return changePasswordResponse;
	}
}