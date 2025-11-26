package cs.controller.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
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
@RequestMapping("/mapi/profile")
@Profile(Common.CONST_MOBILE_PROFILE)
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
}
