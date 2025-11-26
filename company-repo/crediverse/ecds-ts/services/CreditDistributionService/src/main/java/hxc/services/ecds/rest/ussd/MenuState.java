package hxc.services.ecds.rest.ussd;

import java.util.Map;

import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.ecds.protocol.rest.config.UssdMenuButton;

public class MenuState
{
	public static final String PROP_USSD_MENU_STATE = "USSD_MENU_STATE";

	public Map<Integer, UssdMenu> menus;
	public Map<String, String> valueMap;
	public Map<String, UssdMenuButton> keyMap;
	public Integer commandID;
	public Integer menuID;
	public int offset = 0;
	public int lineCount = 0;
	public String transactionID;
	public boolean canProceed = false;
	public int options = 0;
}
