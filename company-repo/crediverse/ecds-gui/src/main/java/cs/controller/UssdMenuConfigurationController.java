package cs.controller;

import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiUssdConfig;
import cs.service.ConfigurationService;
import cs.service.UssdMenuConfigService;
import cs.utility.Common;
import hxc.ecds.protocol.rest.UssdCommand;
import hxc.ecds.protocol.rest.config.UssdConfig;

@RestController
@RequestMapping("/api/config/ussdmenu")
public class UssdMenuConfigurationController
{
	private static final Logger logger = LoggerFactory.getLogger(UssdMenuConfigurationController.class);

	@Autowired
	private UssdMenuConfigService ussdMenuConfigService;

	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	private ObjectMapper mapper;

	// -------------------------------------------------------------------------
	// Embedded USSD Configuration (for menu embedded in other settings such as transfers)
	// -------------------------------------------------------------------------
	@RequestMapping(value="transfers", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiUssdConfig getTransfersUssdMenuConfig() throws Exception
	{
		// GuiUssdConfig

		return configurationService.getTransfersUssdConfig();
	}

	@RequestMapping(value="transfers", method = {RequestMethod.POST, RequestMethod.PUT})
	public String postTransfersUssdMenuConfig(@RequestBody(required = true) UssdConfig config, Locale locale) throws Exception
	{
		configurationService.updateTransfersUssdMenuConfig(config);
		return "{}";
	}

	@RequestMapping(value="transfersdeduplication", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiUssdConfig getTransfersDeduplicationUssdMenuConfig() throws Exception
	{
		// GuiUssdConfig

		return configurationService.getTransfersDeduplicationUssdConfig();
	}

	@RequestMapping(value="transfersdeduplication", method = {RequestMethod.POST, RequestMethod.PUT})
	public String postTransfersDeduplicationUssdMenuConfig(@RequestBody(required = true) UssdConfig config, Locale locale) throws Exception
	{
		configurationService.updateTransfersDeduplicationUssdMenuConfig(config);
		return "{}";
	}

	@RequestMapping(value="bundle_sales", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiUssdConfig getBundlesUssdMenuConfig() throws Exception
	{
		// GuiUssdConfig

		return configurationService.getBundleSalesUssdMenuConfig();
	}

	@RequestMapping(value="bundle_sales", method = {RequestMethod.POST, RequestMethod.PUT})
	public String postBundlesUssdMenuConfig(@RequestBody(required = true) UssdConfig config, Locale locale) throws Exception
	{
		configurationService.updateBundleSalesUssdMenuConfig(config);
		return "{}";
	}

	@RequestMapping(value="bundle_sales_deduplication", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiUssdConfig getBundlesDeduplicationUssdMenuConfig() throws Exception
	{
		// GuiUssdConfig

		return configurationService.getBundleSalesDeduplicationUssdMenuConfig();
	}

	@RequestMapping(value="bundle_sales_deduplication", method = {RequestMethod.POST, RequestMethod.PUT})
	public String postBundlesDeduplicationUssdMenuConfig(@RequestBody(required = true) UssdConfig config, Locale locale) throws Exception
	{
		configurationService.updateBundleSalesDeduplicationUssdMenuConfig(config);
		return "{}";
	}

	@RequestMapping(value="airtimesales", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiUssdConfig getAirtimeUssdMenuConfig() throws Exception
	{
		// GuiUssdConfig
		return configurationService.getSalesUssdMenuConfig();
	}

	@RequestMapping(value="airtimesales", method = {RequestMethod.POST, RequestMethod.PUT})
	public String postAirtimeUssdMenuConfig(@RequestBody(required = true) UssdConfig config, Locale locale) throws Exception
	{
		configurationService.updateSalesUssdMenuConfig(config);
		return "{}";
	}

	@RequestMapping(value="airtimesalesdeduplication", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiUssdConfig getAirtimeDeduplicationUssdMenuConfig() throws Exception
	{
		// GuiUssdConfig
		return configurationService.getSalesDeduplicationUssdMenuConfig();
	}

	@RequestMapping(value="airtimesalesdeduplication", method = {RequestMethod.POST, RequestMethod.PUT})
	public String postAirtimeDeduplicationUssdMenuConfig(@RequestBody(required = true) UssdConfig config, Locale locale) throws Exception
	{
		configurationService.updateSalesDeduplicationUssdMenuConfig(config);
		return "{}";
	}

	@RequestMapping(value="self_topups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiUssdConfig getSelftopupUssdMenuConfig() throws Exception
	{
		return configurationService.getSelfTopUpsUssdMenuConfig();
	}

	@RequestMapping(value="self_topups", method = {RequestMethod.POST, RequestMethod.PUT})
	public String postSelftopupUssdMenuConfig(@RequestBody(required = true) UssdConfig config, Locale locale) throws Exception
	{
		configurationService.updateSelfTopUpsUssdMenuConfig(config);
		return "{}";
	}

	@RequestMapping(value="self_topups_deduplication", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiUssdConfig getSelftopupDeduplicationUssdMenuConfig() throws Exception
	{
		return configurationService.getSelfTopUpsDeduplicationUssdMenuConfig();
	}

	@RequestMapping(value="self_topups_deduplication", method = {RequestMethod.POST, RequestMethod.PUT})
	public String postSelftopupDeduplicationUssdMenuConfig(@RequestBody(required = true) UssdConfig config, Locale locale) throws Exception
	{
		configurationService.updateSelfTopUpsDeduplicationUssdMenuConfig(config);
		return "{}";
	}



	// -------------------------------------------------------------------------
	// USSD Configuration Menu (for main USSD menu)
	// -------------------------------------------------------------------------

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiUssdConfig getUssdMenuConfig() throws Exception
	{
		// GuiUssdConfig

		return ussdMenuConfigService.getUssdMenuConfig();
	}

	@RequestMapping(value="idlist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ArrayNode getUssdMenuIdList() throws Exception
	{
		return ussdMenuConfigService.getUssdMenuIdList();
	}

	@RequestMapping(value="commands", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public UssdCommand[] getUssdCommands() throws Exception
	{
		// GuiUssdConfig

		return ussdMenuConfigService.getUssdCommands();
	}

	@RequestMapping(value="typelist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, String> getUssdTypeList() throws Exception
	{
		return ussdMenuConfigService.listUssdTypes();
	}

	@RequestMapping(value="reset", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String resetUssdMenuConfig() throws Exception
	{
		ussdMenuConfigService.resetUssdMenuConfig();
		return "{}";
	}

	@RequestMapping(method = RequestMethod.POST)
	public String postUssdMenuConfig(@RequestBody(required = true) GuiUssdConfig config, Locale locale) throws Exception
	{
		ussdMenuConfigService.updateUssdMenuConfig(config);
		return "{}";
	}

	@RequestMapping(method = RequestMethod.PUT)
	public String putUssdMenuConfig(@RequestBody(required = true) GuiUssdConfig config, Locale locale) throws Exception
	{
		ussdMenuConfigService.updateUssdMenuConfig(config);
		return "{}";
	}

	@RequestMapping(value="export", method = RequestMethod.GET)
	@ResponseBody
	public void exportAsJson(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		ussdMenuConfigService.setExportHeaders(response);
		try
		{
			OutputStream outputStream = response.getOutputStream();

			/*
			 * Use Jackson writer directly so that the json export can be indented
			 * without needing to enable indentation on all JSON output.
			 */
			//ObjectWriter writer = mapper.defaultPrettyPrintingWriter(); // Use this instead when Jackson is upgraded above 1.9
			ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();

			outputStream.write(writer.writeValueAsBytes(ussdMenuConfigService.getUssdMenuConfigRaw()));
			outputStream.flush();
			outputStream.close();
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}

	@RequestMapping(value = "import")
	public @ResponseBody ObjectNode handleFileUpload(HttpServletRequest request,
			@RequestParam Map<String,String> allRequestParams,
			//@RequestParam("filename") String filename,
			@RequestParam("fileimport") MultipartFile file,
			ModelMap model,
			RedirectAttributes redirectAttributes) throws Exception
	{
		ObjectNode response = mapper.createObjectNode();
		try
		{
			String filename = file.getOriginalFilename();
			if (!filename.toLowerCase().endsWith(".json"))
			{
				throw new Exception("MUST_CONFORM_TO_PATTERN");
			}
			if (Common.isDevelopment())
			{
				//filename = fudgeFilename(filename);
			}

			response.put("success", true);
			response.put("coauth", false);
			response.put("uuid", ussdMenuConfigService.importUssdMenu(file));
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			response.put("error", true);
			response.put("message", e.getMessage());
			logger.error("", e);
			throw e;

		}
		return response;
	}
}
