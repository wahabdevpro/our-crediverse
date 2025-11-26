package cs.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

import cs.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.agents.AgentRulesValiationIssues;
import cs.service.AgentService;
import cs.service.TransactionService;
import cs.service.TransferRuleService;
import hxc.ecds.protocol.rest.AdjustmentRequest;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private TransferRuleService rulesService;

	@Autowired
	private AgentService agentService;

	@RequestMapping(value="replenish", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiReplenishRequest create(@RequestBody(required = true) GuiReplenishRequest request, Locale locale) throws Exception
	{
		transactionService.createTransaction(request);
		return request;
	}


	@RequestMapping(value="sugestbonusamount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ObjectNode suggestBonus(@RequestParam(value = "amount") String amount, Locale locale) throws Exception
	{
		BigDecimal result = new BigDecimal(amount).setScale(2, RoundingMode.UP);
		BigDecimal bonusPercentage = rulesService.getMaxCumulativeBonusPercentage();
		result = result.multiply(bonusPercentage).setScale(2, RoundingMode.UP);

		ObjectNode jsonNode = JsonNodeFactory.instance.objectNode();
		((ObjectNode)jsonNode).put("bonus", result);

		return jsonNode;
	}

	@RequestMapping(value="validateroottransfer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public AgentRulesValiationIssues validateRootTransfer(@RequestParam(value = "agentId") Integer agentId, Locale locale) throws Exception
	{
		AgentRulesValiationIssues validationIssues = agentService.validateAgentTransferRulesSet(null, agentId);

		return validationIssues;
	}

	@RequestMapping(value="transfer", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiTransferRequest create(@RequestBody(required = true) GuiTransferRequest request, Locale locale) throws Exception
	{
		transactionService.createTransaction(request);
		return request;
	}

	@RequestMapping(value="adjustment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public AdjustmentRequest create(@RequestBody(required = true) GuiAdjustmentRequest request, Locale locale) throws Exception
	{
		transactionService.createTransaction(request);
		return request;
	}

	@RequestMapping(value="reversal", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiReversalCoAuthRequest processReversal(@RequestBody(required = true) GuiReversalCoAuthRequest request, Locale locale) throws Exception
	{
		transactionService.createTransaction(request);
		return request;
	}

	@RequestMapping(value = "reversal_without_co_auth", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiReversalRequest processReversalWithoutCoAuth(@RequestBody GuiReversalRequest request, Locale locale) throws Exception {
		transactionService.processTransaction(request);
		return request;
	}

	@RequestMapping(value="adjudicate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAdjudicateRequest processAdjudicate(@RequestBody(required = true) GuiAdjudicateRequest request, Locale locale) throws Exception
	{
		transactionService.createTransaction(request);
		return request;
	}
}
