package cs.dto.portal;

import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.WebUsersConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuiPinRules
{
	private int minPinLength;
	private int maxPinLength;
	private int minPasswordLength;
	private int maxPasswordLength;

	public static GuiPinRules extractPinRuleSet(AgentsConfig agentsConfig)
	{
		GuiPinRules result = new GuiPinRules();
		result.setMinPinLength(agentsConfig.getMinPinLength());
		result.setMaxPinLength(agentsConfig.getMaxPinLength());
		result.setMinPasswordLength(agentsConfig.getMinPasswordLength());
		result.setMaxPasswordLength(agentsConfig.getMaxPasswordLength());
		return result;
	}

	public static GuiPinRules extractPinRuleSet(WebUsersConfig webUsersConfig)
	{
		GuiPinRules result = new GuiPinRules();
		result.setMinPasswordLength(webUsersConfig.getMinPasswordLength());
		result.setMaxPasswordLength(webUsersConfig.getMaxPasswordLength());
		return result;
	}
}
