package cs.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiDataTable;
import cs.dto.GuiTransferRule;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.service.ConfigurationService;
import cs.service.TransferRuleService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.TransferRule;
import hxc.ecds.protocol.rest.Violation;

@RestController
@RequestMapping("/api/transfer_rules")
public class TransferRuleController
{
	private static final Logger logger = LoggerFactory.getLogger(TransferRuleController.class);

	@Autowired
	private ConfigurationService configService;

	@Autowired //ask @Configuration-marked class for this
	private TransferRuleService ruleService;

	@Autowired
	ObjectMapper mapper;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@Autowired
	private LoginSessionData sessionData;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list() throws Exception
	{
		TransferRule[] rules = ruleService.listTransferRules();

		return new GuiDataTable(typeConvertorService.getGuiTransferRulesFromTransferRules(rules));
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) count = ruleService.countRules(search);

		return ruleService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.RULE, ".csv"));

		long ruleCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		ruleCount = ruleService.countRules(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(ruleCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		ruleService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, ruleCount, true, null);
	}

	@RequestMapping(value="{ruleId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public TransferRule getRule(@PathVariable("ruleId") String ruleId) throws Exception
	{
		return typeConvertorService.getGuiTransferRuleFromTransferRule(ruleService.getRule(ruleId));
	}

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> transferRulesDropdown(@RequestParam(value = "_type") Optional<String> type, @RequestParam(value = "term") Optional<String> query) throws Exception
	{
		return ruleService.getTransferRuleNameMap(type, query);
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public TransferRule[] serverList() throws Exception
	{
		TransferRule[] rules = ruleService.listTransferRules();
		return rules;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public TransferRule create(@RequestBody(required = true) GuiTransferRule newTransferRule, Locale locale) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		TransferRule rule = typeConvertorService.getTransferRuleFromGuiTransferRule(newTransferRule, violations);

		if (violations.size() > 0)
			throw new GuiValidationException(violations);

		ruleService.create(rule);
		return newTransferRule;
	}

	@RequestMapping(value="{transfer_rule}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("transfer_rule") String ruleId) throws Exception
	{
		ruleService.delete(ruleId);
		return "{}";
	}

	@RequestMapping(value="{transfer_rule}", method = RequestMethod.PUT)
	public String update(@PathVariable("transfer_rule") String ruleId, @RequestBody(required = true) GuiTransferRule newRule, Locale locale) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		TransferRule rule = typeConvertorService.getTransferRuleFromGuiTransferRule(newRule, violations);

		if (violations.size() > 0)
			throw new GuiValidationException(violations);

		ruleService.update(rule);
		return "{}";
	}

	//Method for browser-based (GET) Dev testing purposes only
	//It is used to delete (DELETE) a specified transfer rule from the REST server
	@RequestMapping(value="delete/{rule_id:[\\d]+}", method = RequestMethod.GET)
	public String testDelete(@PathVariable("rule_id") String ruleId)
	{
		try {
			ruleService.delete(ruleId);
		}
		catch (Exception e)
		{
			logger.error(e.getLocalizedMessage(), e);
		}

		return "{}";
	}//testDelete()
}
