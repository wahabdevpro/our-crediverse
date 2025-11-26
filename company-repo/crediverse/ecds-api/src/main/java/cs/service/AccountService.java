package cs.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import cs.config.RestServerConfiguration;
import cs.dto.GuiAccount;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.Account;

@Service
public class AccountService
{
	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getAccountUrl();
	}

	public Account getAccount(int agentId) throws Exception
	{
		return getAgentAccount(String.valueOf(agentId));
	}
	
	public GuiAccount getGuiAccount(int agentId) throws Exception
	{
		Account agentAccount = getAgentAccount(String.valueOf(agentId));
		return getGuiAccountFromAgentAccount(agentAccount);
	}

	public Account getAgentAccount(String agentId) throws Exception
	{
		Account response = null;
		response = restTemplate.execute(restServerUrl+"/"+agentId, HttpMethod.GET, Account.class);
		return response;
	}
	
	public GuiAccount getGuiAccountFromAgentAccount(Account agentAccount)
	{
		GuiAccount guiAccount = new GuiAccount();
		guiAccount.setAgentID(agentAccount.getAgentID());
		guiAccount.setBalance(agentAccount.getBalance());
		guiAccount.setBonusBalance(agentAccount.getBonusBalance());
		guiAccount.setOnHoldBalance(agentAccount.getOnHoldBalance());
		return guiAccount;
	}
}
