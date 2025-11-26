package cs.dto;

import hxc.ecds.protocol.rest.AgentUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuiPortalAgent extends GuiAgentAccount
{
	private AgentUser agentUser;
	private String agentUserRoleName;

}
