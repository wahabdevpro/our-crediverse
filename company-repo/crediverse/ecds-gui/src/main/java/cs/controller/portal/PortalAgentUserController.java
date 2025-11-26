package cs.controller.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.GuiAgentUser;
import cs.dto.GuiDataTable;
import cs.service.AgentUserService;

@RestController
@RequestMapping("/papi/ausers")
public class PortalAgentUserController
{
	@Autowired
	private AgentUserService agentUserService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentUser[] listAgentUsers() throws Exception
	{
		GuiAgentUser[] users = agentUserService.listGuiAgentUsers();
		return users;
	}

	/**
	 * Request for User data (url: http://localhost:8084/ecds-gui/api/ausers/data)
	 */
	@RequestMapping(value="data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable userListAsTable() throws Exception
	{
		GuiAgentUser[] users = agentUserService.listGuiAgentUsers();
		return new GuiDataTable(users);
	}


}
