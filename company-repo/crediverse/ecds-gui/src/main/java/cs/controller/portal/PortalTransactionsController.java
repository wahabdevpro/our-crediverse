package cs.controller.portal;

import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.agents.AgentRulesValiationIssues;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.service.AgentService;
import cs.service.portal.PortalTransactionService;
import cs.utility.Common;
import hxc.ecds.protocol.rest.ChangePinRequest;
import hxc.ecds.protocol.rest.SelfTopUpRequest;
import hxc.ecds.protocol.rest.SellRequest;
import hxc.ecds.protocol.rest.TransferRequest;
import hxc.ecds.protocol.rest.Violation;

@RestController


@RequestMapping("/papi/transactions")
@Profile(Common.CONST_PORTAL_PROFILE)
public class PortalTransactionsController
{

	@Autowired
	private PortalTransactionService portalTansactionService;

	@Autowired
	private AgentService agentService;

	@Autowired
	private LoginSessionData sessionData;

	@RequestMapping(value="changepin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String pinChangeRequest(@RequestBody(required = true) ChangePinRequest request, Locale locale) throws Exception
	{
		List<Violation> violations = request.validate();
		if (violations.size() > 0)
		{
			if (violations != null && violations.size() > 0)
				throw new GuiValidationException(violations, "InValid Pin");
		}
		portalTansactionService.changePin(request);
		return "{}";
	}

	@RequestMapping(value="transfer", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String transferRequest(@RequestBody(required = true) TransferRequest request, Locale locale) throws Exception
	{
		portalTansactionService.transferRequest(request);
		return "{}";
	}

	@RequestMapping(value="selftopup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String selfTopupRequest(@RequestBody(required = true) SelfTopUpRequest request, Locale locale) throws Exception
	{
		portalTansactionService.performSelfTopup(request);
		return "{}";
	}

	@RequestMapping(value="validatetransfer", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public AgentRulesValiationIssues validateRootTransfer(@RequestParam(value = "agentId") Integer agentBId, Locale locale) throws Exception
	{
		int agentAId = sessionData.getAgentId();

		AgentRulesValiationIssues validationIssues = agentService.validateAgentTransferRulesSet(agentAId, agentBId);

		return validationIssues;
	}

	@RequestMapping(value="sell", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public String sellRequest(@RequestBody(required = true) SellRequest request, Locale locale) throws Exception
	{
		portalTansactionService.sellRequest(request);
		return "{}";
	}
}
