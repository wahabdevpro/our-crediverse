package cs.controller.portal;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.GuiAgentAccount;
import cs.dto.GuiPortalAgent;
import cs.service.AgentService;
import cs.service.TypeConvertorService;
import cs.utility.Common;
import hxc.ecds.protocol.rest.Agent;

@RestController
@RequestMapping("/papi/profile")
@Profile(Common.CONST_PORTAL_PROFILE)
public class AgentProfileController
{
	@Autowired
	private TypeConvertorService typeConvertorService;

	@Autowired //ask @Configuration-marked class for this
	private AgentService agentService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiPortalAgent getAgentProfile() throws Exception
	{
		Agent agent = agentService.getLoggedInAgentProfile();
		GuiAgentAccount guiAgentAccount = typeConvertorService.getGuiAgentAccountFromAgent(agent);
		return typeConvertorService.getGuiPortalAgentFromGuiAgentAccount(guiAgentAccount);
	}

	@RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public Agent persisteProfile(@RequestBody(required = true) Agent updatedAgent, Locale locale) throws Exception
	{
		agentService.updateLoggedinAgent(updatedAgent);
		return updatedAgent;
	}

	// TODO us this to get details of subagent
	@RequestMapping(value="/subagent", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiPortalAgent getSubAgentProfile() throws Exception
	{
		Agent agent = agentService.getLoggedInAgentProfile();
		GuiAgentAccount guiAgentAccount = typeConvertorService.getGuiAgentAccountFromAgent(agent);
		return typeConvertorService.getGuiPortalAgentFromGuiAgentAccount(guiAgentAccount);
	}

	@RequestMapping(value="/subagent", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public Agent persisteSubAgentProfile(@RequestBody(required = true) Agent updatedAgent, Locale locale) throws Exception
	{
		agentService.updateSubAgent(updatedAgent);
		return updatedAgent;
	}
}
