package cs.controller;

import cs.dto.GuiAccount;
import cs.dto.GuiAgent;
import cs.dto.security.LoginSessionData;
import cs.service.AccountService;
import cs.service.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static cs.utility.Common.checkAgentIdForNull;

@RestController
@RequestMapping("/api/account")
public class AccountController 
{
	@Autowired //ask @Configuration-marked class for this
	private AgentService agentService;
	
	@Autowired
	private AccountService accountService;
	
	@Resource(name="tokenStore")
	private TokenStore tokenStore;
	
	@Autowired
	private LoginSessionData sessionData;
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgent getAgentProfile() throws Exception {
		Integer agentID = sessionData.getAgentId();
		checkAgentIdForNull(agentID);
		GuiAgent agent = agentService.getGuiAgent(agentID);
		return agent;
	}

	@RequestMapping(value="/balance", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAccount getAgentBalance() throws Exception
	{
		Integer agentID = sessionData.getAgentId();
		checkAgentIdForNull(agentID);
		GuiAccount account = accountService.getGuiAccount(agentID);
		return account;
	}
	
	@RequestMapping(method = RequestMethod.PUT)
	public void updateAgentProfile(@RequestBody(required = true) GuiAgent updatedAgent) throws Exception
	{
		Integer agentID = sessionData.getAgentId();
		checkAgentIdForNull(agentID);
		updatedAgent.setAccountID(agentID);
		agentService.update(updatedAgent);
	}
}