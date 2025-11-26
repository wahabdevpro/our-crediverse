package cs.service.convertor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import cs.dto.GuiUssdConfig;
import cs.dto.GuiUssdMenu;
import cs.dto.GuiUssdMenuButton;
import hxc.ecds.protocol.rest.config.UssdConfig;
import hxc.ecds.protocol.rest.config.UssdMenu;
import hxc.ecds.protocol.rest.config.UssdMenuButton;

@Component
public class UssdConfigToGuiUssdConfig implements Converter<UssdConfig, GuiUssdConfig>
{
	private GuiUssdMenuButton convertButton(UssdMenuButton btn)
	{
		GuiUssdMenuButton guiBtn = null;
		if (btn != null)
		{
			guiBtn = new GuiUssdMenuButton();
			BeanUtils.copyProperties(btn, guiBtn);
		}
		return guiBtn;
	}

	private GuiUssdMenu convertmenu(UssdMenu menu, int offset)
	{
		GuiUssdMenu guiMenu = new GuiUssdMenu();
		guiMenu.setId(menu.getId());
		guiMenu.setName(menu.getName());
		guiMenu.setOffset(offset);
		List<GuiUssdMenuButton> buttons = guiMenu.getButtons(); // Will get an empty list
		for (UssdMenuButton menuBtn : menu.getButtons())
		{
			GuiUssdMenuButton newBtn = convertButton(menuBtn);
			if (newBtn != null) buttons.add(newBtn);
		}


		return guiMenu;
	}

	@Override
	public GuiUssdConfig convert(UssdConfig config)
	{
		Map<Integer, Integer>offsetMap = new TreeMap<Integer, Integer>();
		Map<Integer, String>nameMap = new HashMap<Integer, String>();
		GuiUssdConfig guiconfig = new GuiUssdConfig();
		BeanUtils.copyProperties(config, guiconfig);
		List<UssdMenu> serverMenuList = config.getMenus();
		if (serverMenuList != null)
		{
			int offset = 0;
			List<GuiUssdMenu>guiMenuList = new ArrayList<GuiUssdMenu>();
			for (offset = 0; offset < serverMenuList.size(); offset++)
			{
				UssdMenu currentServerMenu = serverMenuList.get(offset);
				offsetMap.put(currentServerMenu.getId(), offset);
				nameMap.put(currentServerMenu.getId(), currentServerMenu.getName());
				guiMenuList.add(convertmenu(currentServerMenu, offset));
			}

			for (GuiUssdMenu current : guiMenuList)
			{
				List<GuiUssdMenuButton> buttons = current.getButtons();
				if (buttons != null) {
					int buttonOffset = 0;
					for (GuiUssdMenuButton btn : buttons)
					{
						Integer menuid = btn.getNextMenuID();
						if (menuid != null)
						{
							btn.setNextMenuOffset(offsetMap.get(menuid));
							btn.setNextMenuName(nameMap.get(menuid));
						}
						btn.setId(buttonOffset);
						buttonOffset++;
					}
				}
			}
			guiconfig.setMenus(guiMenuList);
		}

		guiconfig.setUssdMenuCommand(config.getUssdMenuCommand());
		return guiconfig;
	}

}
