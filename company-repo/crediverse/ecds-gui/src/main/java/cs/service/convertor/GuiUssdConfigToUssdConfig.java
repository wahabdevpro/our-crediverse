package cs.service.convertor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class GuiUssdConfigToUssdConfig implements Converter<GuiUssdConfig, UssdConfig>
{
	private static final Logger logger = LoggerFactory.getLogger(GuiUssdConfigToUssdConfig.class);

	@Override
	public UssdConfig convert(GuiUssdConfig guiconfig)
	{
		UssdConfig config = new UssdConfig();
		try
		{
			if (guiconfig.getMenus() != null)
			{
				List<UssdMenu>servermenuList = new ArrayList<UssdMenu>();
				for (GuiUssdMenu guiMenuItem : guiconfig.getMenus())
				{
					try
					{
						if (guiMenuItem != null)
						{
							UssdMenu menuItem = new UssdMenu();
							BeanUtils.copyProperties(guiMenuItem, menuItem);
							if (guiMenuItem.getButtons() != null)
							{
								List<UssdMenuButton> btnList = new ArrayList<UssdMenuButton>();
								for (GuiUssdMenuButton guiBtn : guiMenuItem.getButtons())
								{
									try
									{
										if (guiBtn != null)
										{
											UssdMenuButton btn = new UssdMenuButton();
											BeanUtils.copyProperties(guiBtn, btn);
											btnList.add(btn);
										}
									}
									catch (Throwable ex)
									{
										logger.error("", ex);
										throw ex;
									}
								}
								menuItem.setButtons(btnList);
							}
							servermenuList.add(menuItem);
						}
					}
					catch (Throwable ex)
					{
						logger.error("", ex);
						throw ex;
					}
				}
				config.setMenus(servermenuList);
			}
			config.setUssdMenuCommand(guiconfig.getUssdMenuCommand());
		}
		catch (Throwable ex)
		{
			logger.error("", ex);
			throw ex;
		}
		return config;
	}

}
