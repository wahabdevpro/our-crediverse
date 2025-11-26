package cs.dto;

import hxc.ecds.protocol.rest.config.UssdMenuButton;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiUssdMenuButton extends UssdMenuButton
{
	private Integer nextMenuOffset;
	private Integer id;
	private String typeName;
	private String commandName;
	private String nextMenuName;
	private Integer captureCommandID;
	protected Integer myMenuID;
}
