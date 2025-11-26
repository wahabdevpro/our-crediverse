package cs.service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.config.RestServerConfiguration;
import cs.constants.ApplicationConstants;
import cs.dto.GuiUssdConfig;
import cs.dto.GuiUssdMenu;
import cs.dto.GuiUssdMenuButton;
import cs.dto.security.LoginSessionData;
import cs.template.CsRestTemplate;
import cs.utility.BatchUtility;
import hxc.ecds.protocol.rest.UssdCommand;
import hxc.ecds.protocol.rest.config.UssdConfig;
import hxc.ecds.protocol.rest.config.UssdMenuButton;

@Service
public class UssdMenuConfigService
{
	private static final Logger logger = LoggerFactory.getLogger(UssdMenuConfigService.class);

	private String restServerUrl;
	private String restServerConfigUrl;
	private String restServerCommandUrl;
	private Map<String, String> ussdTypeMap;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private ConversionService conversionService;

	@Autowired
	private ObjectMapper mapper;

	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getUssdmenuurl();
		this.restServerConfigUrl = this.restServerUrl+"/config";
		this.restServerCommandUrl = this.restServerUrl+"/commands";

		ussdTypeMap = new TreeMap<String, String>();
		ussdTypeMap.put(UssdMenuButton.TYPE_AUTO_OPTIONS, "Auto Options");
		ussdTypeMap.put(UssdMenuButton.TYPE_CAPTURE, "Capture");
		ussdTypeMap.put(UssdMenuButton.TYPE_COMMAND, "Command");
		ussdTypeMap.put(UssdMenuButton.TYPE_EXIT, "Exit");
		ussdTypeMap.put(UssdMenuButton.TYPE_NAVIGATE, "Navigate");
		ussdTypeMap.put(UssdMenuButton.TYPE_NEXT, "Next");
		ussdTypeMap.put(UssdMenuButton.TYPE_OPTION, "Option");
		ussdTypeMap.put(UssdMenuButton.TYPE_PREVIOUS, "Previous");
		ussdTypeMap.put(UssdMenuButton.TYPE_RESULT, "Display Result");
		ussdTypeMap.put(UssdMenuButton.TYPE_TEXT, "Text");
	}

	public void setExportHeaders(HttpServletResponse response)
	{
		String filename = BatchUtility.getFilename(sessionData.getCompanyPrefix(), "menu", ".json");
		BatchUtility.setExportHeaders(response, filename, ApplicationConstants.CONST_CONTENT_TYPE_JSON);
	}

	public GuiUssdConfig convertConfig(UssdConfig ussdConfig) throws Exception
	{
		Map<Integer, UssdCommand> ussdCommands = getUssdCommandsAsMap();
		Map<Integer, UssdCommand> menuCommandLookup = new HashMap<Integer, UssdCommand>();
		Map<Integer, GuiUssdMenu> menuMap = new HashMap<Integer, GuiUssdMenu>();

		GuiUssdConfig guiUssdConfig = conversionService.convert(ussdConfig, GuiUssdConfig.class);

		if (guiUssdConfig != null && guiUssdConfig.getMenus() != null)
		{
			List<GuiUssdMenuButton>buttonsToExpand = new ArrayList<GuiUssdMenuButton>(); // These buttons need expanding to add a captureCommandId
			for (GuiUssdMenu menu : guiUssdConfig.getMenus())
			{
				menuMap.put(menu.getId(), menu);
				if (menu.getButtons() != null)
				{
					for ( GuiUssdMenuButton button : menu.getButtons())
					{
						button.setTypeName(ussdTypeMap.get(button.getType()));
						if (button.getCommandID() != null && ussdCommands != null)
						{
							Integer cmdId = button.getCommandID();
							UssdCommand cmd = ussdCommands.get(cmdId);
							if (cmd != null)
							{
								button.setCommandName(cmd.getName().getTexts().get("en"));
								menuCommandLookup.put(button.getNextMenuID(), cmd); // Add to command lookup, required for expanding capture buttons.
							}
						}
						if (button.getType().equals(UssdMenuButton.TYPE_CAPTURE) || button.getType().equals(UssdMenuButton.TYPE_AUTO_OPTIONS))
						{
							button.setMyMenuID(menu.getId());
							buttonsToExpand.add(button); // Add to buttons that need expanding.
						}
					}
				}
			}

			// Now expand all buttons that have a capture field
			List<GuiUssdMenuButton>buttonsCannotExpand = new ArrayList<GuiUssdMenuButton>();
			for (GuiUssdMenuButton button : buttonsToExpand)
			{
				if (menuCommandLookup.containsKey(button.getMyMenuID()))
				{
					UssdCommand cmd = menuCommandLookup.get(button.getMyMenuID());
					button.setCaptureCommandID(cmd.getId());
				}
				else
				{
					buttonsCannotExpand.add(button);
					logger.error("No command for "+button.getMyMenuID());
				}
			}
			/*for (GuiUssdMenuButton button : buttonsCannotExpand)
			{
				if (menuCommandLookup.containsKey(button.getMyMenuID()))
				{
					UssdCommand cmd = menuCommandLookup.get(button.getMyMenuID());
					button.setCaptureCommandID(cmd.getId());
				}
				else
				{
					buttonsCannotExpand.add(button);
					logger.error("No command for "+button.getMyMenuID());
				}
			}*/
		}
		guiUssdConfig.setUssdTypeMap(ussdTypeMap);
		return guiUssdConfig;
	}

	public GuiUssdConfig getUssdMenuConfig(String url) throws Exception
	{
		UssdConfig ussdConfig = restTemplate.execute(url, HttpMethod.GET, UssdConfig.class);

		return convertConfig(ussdConfig);
	}

	public GuiUssdConfig getUssdMenuConfig() throws Exception
	{
		return getUssdMenuConfig(restServerConfigUrl);
	}

	public Map<Integer, UssdCommand> getUssdCommandsAsMap() throws Exception
	{
		Map<Integer, UssdCommand> commandMap = new HashMap<Integer, UssdCommand>();
		UssdCommand[] ussdCommands = getUssdCommands();
		if (ussdCommands != null)
		{
			for (UssdCommand cmd : ussdCommands)
			{
				commandMap.put(cmd.getId(), cmd);
			}
		}
		return commandMap;
	}

	public UssdCommand[] getUssdCommands() throws Exception
	{
		UssdCommand[] ussdCommands = restTemplate.execute(restServerCommandUrl, HttpMethod.GET, UssdCommand[].class);

		return ussdCommands;
	}

	public void updateUssdMenuConfig(GuiUssdConfig guiConfig) throws Exception
	{
		UssdConfig config = conversionService.convert(guiConfig, UssdConfig.class);
		//UssdConfig config = restTemplate.execute(restServerConfigUrl, HttpMethod.GET, UssdConfig.class);
		restTemplate.execute(restServerConfigUrl, HttpMethod.PUT, config, Void.class);
	}

	public String importUssdMenu(MultipartFile file) throws Exception
	{
		Reader reader = new InputStreamReader(file.getInputStream());

		GuiUssdConfig config = mapper.readValue(reader, GuiUssdConfig.class);
		if (config != null)
		{
			updateUssdMenuConfig(config);
		}

		/*String line = null;
		while((line = inputStream.readLine()) != null)
		{
			if (line.trim().length() == 0) continue;
			logger.error(line);
		}*/
		return UUID.randomUUID().toString();
	}

	public GuiUssdConfig resetUssdMenuConfig() throws Exception
	{
		updateUssdMenuConfig(new GuiUssdConfig());
		//return getUssdMenuConfig();
		return null;
	}

	public Map<String, String> listUssdTypes()
	{
		return ussdTypeMap;
	}

	public ArrayNode getUssdMenuIdList() throws Exception
	{
		ArrayNode data = mapper.createArrayNode();
		GuiUssdConfig menuConfig = getUssdMenuConfig();
		if (menuConfig != null && menuConfig.getMenus() != null)
		{
			for (GuiUssdMenu menu : menuConfig.getMenus())
			{
				ObjectNode entry = mapper.createObjectNode();
				entry.put("menuId", menu.getId());
				entry.put("menuOffset", menu.getOffset());
				entry.put("menuName", "Menu "+menu.getId());
				data.add(entry);
			}
		}


		return data;
	}

	public String getTypeName(String type)
	{
		Map<String, String>typeList = listUssdTypes();
		return typeList.get(type);
	}

	public UssdConfig getUssdMenuConfigRaw() throws Exception
	{
		return restTemplate.execute(restServerConfigUrl, HttpMethod.GET, UssdConfig.class);
	}
}
