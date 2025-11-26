package cs.controller;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
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
import hxc.ecds.protocol.rest.Tier;
import hxc.ecds.protocol.rest.TransferRule;

@RestController
@RequestMapping("/api/tiers")
public class TierController
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

// ------------------------ Updated ------------------------

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list() throws Exception
	{
		//GuiTier[] tiers = conversionService.convert(tierService.listTiers(), GuiTier[].class);
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
		//GuiTier[] tiers = conversionService.convert(tierService.listTiers(filter), GuiTier[].class);
		GuiTier[] tiers = typeConvertorService.getGuiTierFromTier(tierService.listTiers(filter));

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

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(
		@RequestParam(value = "_type") Optional<String> type,
		@RequestParam(value = "term") Optional<String> query,
		@RequestParam(value = "tierID") Optional<Integer> tierID) throws Exception
	{
		Tier[] tiers = null;
		Map<Integer, String>tierMap = new TreeMap<Integer,String>();
		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			tiers = tierService.listTiers(query.get());
		}
		else
		{
			tiers = tierService.listTiers();
		}

		if (tiers != null)
		{
			Arrays.asList(tiers).forEach(tier ->{
				if ( !tierID.isPresent() || ( tier.getId() == tierID.get().intValue() ) )
					tierMap.put(tier.getId(), tier.getName());
			});
		}
		return tierMap;
	}

	@RequestMapping(value="dropdown/type/{tierType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(
		@PathVariable(value = "tierType") String tierType,
		@RequestParam(value = "_type") Optional<String> type,
		@RequestParam(value = "term") Optional<String> query,
		@RequestParam(value = "tierID") Optional<Integer> tierID) throws Exception
	{
		Tier[] tiers = null;
		Map<Integer, String>tierMap = new TreeMap<Integer,String>();
		tiers = tierService.listTiersByType(tierType);
		if (tiers != null)
		{
			Arrays.asList(tiers).forEach(tier ->{
				if ((!tierID.isPresent() || (tier.getId() == tierID.get().intValue())) && (!tier.getType().equals(Tier.TYPE_SUBSCRIBER) && (!tier.getType().equals(Tier.TYPE_ROOT))))
					tierMap.put(tier.getId(), tier.getName());
			});
		}
		return tierMap;
	}
	
	@RequestMapping(value="agents/dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdownForAgents(
		@RequestParam(value = "_type") Optional<String> type,
		@RequestParam(value = "term") Optional<String> query,
		@RequestParam(value = "tierID") Optional<Integer> tierID) throws Exception
	{
		Tier[] tiers = null;
		Map<Integer, String>tierMap = new TreeMap<Integer,String>();
		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			tiers = tierService.listTiers(query.get());
		}
		else
		{
			tiers = tierService.listTiers();
		}

		if (tiers != null)
		{
			Arrays.asList(tiers).forEach(tier ->{
				if ((!tierID.isPresent() || (tier.getId() == tierID.get().intValue())) && (!tier.getType().equals(Tier.TYPE_SUBSCRIBER) && (!tier.getType().equals(Tier.TYPE_ROOT))))
					tierMap.put(tier.getId(), tier.getName());
			});
		}
		return tierMap;
	}
	
// --------------------- /////////\\\\\\\\\ ----------------------


	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Tier[] serverList() throws Exception
	{
		Tier[] tiers = tierService.listTiers();
		return tiers;
	}
}
