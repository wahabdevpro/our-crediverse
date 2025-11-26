package cs.controller.portal;

import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiDataTable;
import cs.dto.GuiTransaction;
import cs.dto.security.LoginSessionData;
import cs.service.TdrService;
import cs.service.TierService;
import cs.service.TypeConvertorService;
import cs.utility.Common;
import hxc.ecds.protocol.rest.Transaction;

@RestController
@Profile(Common.CONST_PORTAL_PROFILE)
@RequestMapping("/papi/tdrs")
public class PortalTdrController
{
	@Autowired
	private LoginSessionData sessionData;

	@Autowired //ask @Configuration-marked class for this
	private TdrService tdrService;

	@Autowired
	private TierService tierService;

	@Autowired
	private TypeConvertorService typeConvertorService;

//	@Autowired
//	private ObjectMapper mapper;

	@RequestMapping(value="{tdrId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTransaction getTransaction(@PathVariable("tdrId") String tdrId) throws Exception
	{
		Transaction transaction = tdrService.getTransactionOrReversed(tdrId);
		GuiTransaction convertedTransaction = typeConvertorService.getGuiTransactionFromTransaction(transaction, true);
		typeConvertorService.convertTransactionIdsToNames(convertedTransaction);
		return convertedTransaction;
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params) throws Exception
	{
		return tdrService.transactionCount(params);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response, @RequestParam boolean docount) throws Exception
	{
		tdrService.listAsCsv(params, response, docount, null);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list(@RequestParam Map<String, String> params) throws Exception
	{
		if (params.containsKey("search[value]") && !params.get("search[value]").isEmpty())
		{
			params.put("msisdn", params.get("search[value]"));
			params.remove("search[value]");
		}	
		return tdrService.createTdrDataTableContent(params, typeConvertorService);
	}

	@RequestMapping(value="search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable search(@RequestParam Map<String, String> params) throws Exception
	{
		return tdrService.search(params, typeConvertorService);
	}

	@RequestMapping(value="search/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode countSearchResults(@RequestParam Map<String, String> params, @RequestParam boolean docount) throws Exception
	{
		return tdrService.countSearchResults(params, docount);
	}

	@RequestMapping(value="search/csv", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public void searchResultsExport(@RequestParam Map<String, String> params, HttpServletResponse response, @RequestParam boolean docount) throws Exception
	{
		tdrService.searchResultsExport(params, response, docount);
	}

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(
		@RequestParam(value = "_type") Optional<String> type,
		@RequestParam(value = "term") Optional<String> query,
		@RequestParam(value = "tierID") Optional<Integer> tierID) throws Exception
	{
		return tierService.tierListMap(type, query, tierID);
	}

	@RequestMapping(value="last", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listLast() throws Exception
	{
		Transaction[] transactions = tdrService.listTransactions(0,5,"id-");
		//Map<Integer, Tier> tierMap = tierService.tierMap();
		return new GuiDataTable(typeConvertorService.getGuiTransactionFromTransaction(transactions));
	}

	@RequestMapping(value="mySales", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable retrieveMyAPartyTransactions(@RequestParam Map<String, String> params) throws Exception
	{
		params.put("agentIDA", String.valueOf( sessionData.getAgentId() ));
		return tdrService.createTdrDataTableContent(params, typeConvertorService);
	}

	@RequestMapping(value="myPurchases", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable retrieveMyBPartyTransactions(@RequestParam Map<String, String> params) throws Exception
	{
		params.put("agentIDB", String.valueOf( sessionData.getAgentId() ));
		return tdrService.createTdrDataTableContent(params, typeConvertorService);
	}

	@RequestMapping(value="myTransactions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable retrieveMyTransactions(@RequestParam Map<String, String> params) throws Exception
	{
		//params.put("agentIDA", String.valueOf( sessionData.getAgentId() ));
		if (params.containsKey("search[value]") && !params.get("search[value]").isEmpty())
		{
			params.put("msisdn", params.get("search[value]"));
			params.remove("search[value]");
		}	
		params.put("relation", params.containsKey("relation") ? params.get("relation") : Transaction.RELATION_ALL);
		return tdrService.createTdrDataTableContent(params, typeConvertorService);
	}

	@RequestMapping(value="transactionTypes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ArrayNode retrieveTransactionTypes() throws Exception
	{
		return tdrService.getTransactionTypes();
	}
}
