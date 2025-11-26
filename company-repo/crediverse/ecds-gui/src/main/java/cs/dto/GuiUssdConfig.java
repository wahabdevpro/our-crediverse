package cs.dto;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiUssdConfig
{/**
	 *
	 */
	protected Map<String, String> ussdTypeMap;

	protected String ussdMenuCommand = "*116#";
	protected List<GuiUssdMenu> menus = null;

	protected Map<String, Map<String, String []>>variables;
}
