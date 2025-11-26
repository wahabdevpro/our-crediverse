package cs.dto;

import java.util.List;

import hxc.ecds.protocol.rest.config.TransfersConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiTransfersConfig extends TransfersConfig
{
	List<GuiUssdMenu> ussdmenu;
}
