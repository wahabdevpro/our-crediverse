package cs.controller.portal;

import java.io.OutputStream;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiDataTable;
import cs.dto.GuiStatusResponse;
import cs.dto.GuiTier;
import cs.dto.security.LoginSessionData;
import cs.service.ConfigurationService;
import cs.service.TierService;
import cs.service.TransferRuleService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import cs.utility.Common;
import hxc.ecds.protocol.rest.Tier;
import hxc.ecds.protocol.rest.TransferRule;

@RestController
@Profile(Common.CONST_PORTAL_PROFILE)
@RequestMapping("/papi/tiers")
public class PortalTierController
{
	@Autowired
	private ConfigurationService configService;

	@Autowired
	private TierService tierService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private TransferRuleService rulesService;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@Autowired
	private ConversionService conversionService;

// ------------------------ Updated ------------------------

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list() throws Exception
	{
		Tier[] tiers = tierService.listTiers();
		return (new GuiDataTable(tiers));
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		long tierCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) tierCount = tierService.countTiers(search);

		return tierService.track(tierCount);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.TIER, ".csv"));

		long tierCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		tierCount = tierService.countTiers(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(tierCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		tierService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, tierCount, true, null);
	}

	@RequestMapping(value="filter", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTier[] list(@RequestParam(value = "filter", required = false) String filter) throws Exception
	{
		GuiTier[] tiers = conversionService.convert(tierService.listTiers(filter), GuiTier[].class);
		//typeConvertorService.getGuiTierFromTier(tierService.listTiers(filter));
		return tiers;
	}

	@RequestMapping(value="{tier}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("tier") String tierId) throws Exception
	{
		tierService.delete(tierId);
		return "{}";
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Tier create(@RequestBody(required = true) Tier newTier, Locale locale) throws Exception
	{
		tierService.create(newTier);
		return newTier;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public GuiStatusResponse update(@RequestBody(required = true) Tier newTier, Locale locale) throws Exception
	{
		GuiStatusResponse response = null;

		tierService.update(newTier);
		response = GuiStatusResponse.operationSuccessful();

		return response;
	}

	@RequestMapping(method = RequestMethod.GET, value="{tierId}")
	public Tier getSingleUser(@PathVariable("tierId") String tierId) throws Exception
	{
		Tier tier = tierService.getTier(tierId);
		return tier;
	}

	@RequestMapping(method = RequestMethod.GET, value="incoming/{tierId}")
	public GuiDataTable incomingRules(@PathVariable("tierId") String tierId) throws Exception
	{
		TransferRule [] rules = rulesService.getTierIncomingRules(tierId);
		return new GuiDataTable(typeConvertorService.getGuiTransferRulesFromTransferRules(rules));
	}

	@RequestMapping(method = RequestMethod.GET, value="outgoing/{tierId}")
	public GuiDataTable outgoingRules(@PathVariable("tierId") String tierId) throws Exception
	{
		TransferRule [] rules = rulesService.getTierOutgoingRules(tierId);
		return new GuiDataTable(typeConvertorService.getGuiTransferRulesFromTransferRules(rules));
	}

// --------------------- /////////\\\\\\\\\ ----------------------


	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Tier[] serverList() throws Exception
	{
		Tier[] tiers = tierService.listTiers();
		return tiers;
	}
}
