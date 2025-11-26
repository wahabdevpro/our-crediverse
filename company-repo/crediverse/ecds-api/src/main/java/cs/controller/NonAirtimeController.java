package cs.controller;

import cs.dto.non_airtime.AgentDetails;
import cs.dto.non_airtime.ApiResponse;
import cs.service.AgentService;
import cs.service.NonAirtimeService;
import hxc.ecds.protocol.rest.non_airtime.DebitRequest;
import hxc.ecds.protocol.rest.non_airtime.RefundRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/service/agent")
public class NonAirtimeController {
	private static final Logger logger = LoggerFactory.getLogger(NonAirtimeController.class);
	
	@Autowired
	private AgentService agentService;
	
	@Autowired
	private NonAirtimeService nonAirtimeService;

	@Resource(name="tokenStore")
	private TokenStore tokenStore;

	@RequestMapping(value="/{msisdn}/details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public AgentDetails getAgentByMsisdn(@PathVariable String msisdn) throws Exception {
    	logger.trace("Enter /api/service/agent/" + msisdn + "/details endpoint.");
		AgentDetails agentDetails = agentService.getGuiAgentByMsisdn(msisdn);
    	logger.trace("Exit /api/service/agent/" + msisdn + "/details endpoint.");
		return agentDetails;
	}

	@RequestMapping(value = "/{msisdn}/transaction/{clientTransactionId}/status",
			method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponse getTransactionStatus(@PathVariable String msisdn, @PathVariable String clientTransactionId) throws Exception {
		logger.trace("Enter /api/service/agent/" + msisdn + "/transaction/" + clientTransactionId + "/status endpoint.");
		ApiResponse response = new ApiResponse(nonAirtimeService.getTransactionStatus(msisdn, clientTransactionId));
		logger.trace("Exit /api/service/agent/" + msisdn + "/transaction/" + clientTransactionId + "/status endpoint.");
		return response;
	}

	@RequestMapping(value="/{msisdn}/debit", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponse debitAgent(@PathVariable String msisdn, @RequestBody DebitRequest debitRequest) throws Exception {
		String clientTransactionId = debitRequest == null ? "" : debitRequest.getClientTransactionId();
		logger.trace("Enter /api/service/agent/" + msisdn + "/debit client trn id: " + clientTransactionId);
		ApiResponse response = new ApiResponse(nonAirtimeService.debit(msisdn, debitRequest));
		logger.trace("Exit /api/service/agent/" + msisdn + "/debit client trn id: " + clientTransactionId + " status: " + response.getStatus());
		return response;
	}

	@RequestMapping(value="/{msisdn}/refund", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ApiResponse refundAgent(@PathVariable String msisdn, @RequestBody RefundRequest refundRequest) throws Exception {
    	logger.trace("Enter /api/service/agent/" + msisdn + "/refund endpoint.");
		ApiResponse response = new ApiResponse(nonAirtimeService.refund(msisdn, refundRequest));
    	logger.trace("Exit /api/service/agent/" + msisdn + "/refund endpoint with status: " + response.getStatus());
		return response;
	}
}
