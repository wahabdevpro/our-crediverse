package cs.dto;

import hxc.ecds.protocol.rest.config.AgentsConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuiPasswordRules {
	  private Integer minimumLength;
	  private Integer maximumLength;
	  private String specialCharacterList;
	  
	  public GuiPasswordRules(AgentsConfig agentsConfig)
	  {
		  minimumLength = agentsConfig.getMinPasswordLength();
		  maximumLength = agentsConfig.getMaxPasswordLength();
		  specialCharacterList = agentsConfig.getSpecialCharacterList();
	  }
}
