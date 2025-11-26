package cs.service.portal;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cs.config.RestServerConfiguration;
import cs.dto.security.LoginSessionData;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.ChangePinRequest;
import hxc.ecds.protocol.rest.ChangePinResponse;
import hxc.ecds.protocol.rest.DepositsQueryRequest;
import hxc.ecds.protocol.rest.DepositsQueryResponse;
import hxc.ecds.protocol.rest.SalesQueryRequest;
import hxc.ecds.protocol.rest.SalesQueryResponse;
import hxc.ecds.protocol.rest.SelfTopUpRequest;
import hxc.ecds.protocol.rest.SelfTopUpResponse;
import hxc.ecds.protocol.rest.SellRequest;
import hxc.ecds.protocol.rest.SellResponse;
import hxc.ecds.protocol.rest.TransferRequest;
import hxc.ecds.protocol.rest.TransferResponse;


@Service
public class PortalTransactionService
{
	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private LoginSessionData sessionData;

	private boolean configured = false;
	private String restTransactionsServerUrl;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restTransactionsServerUrl = restServerConfig.getRestServer() + restServerConfig.getTransactionsUrl();
			configured = true;
		}
	}

	private String getConfigurationRESTUrl(String endPoint)
	{
		return String.format("%s/%s", restTransactionsServerUrl, endPoint);
	}

	public DepositsQueryResponse getDepositsQuery() throws Exception
	{
		DepositsQueryRequest request = new DepositsQueryRequest();
		request.setSessionID(sessionData.getServerSessionID());
		request.setSuppressSms(true);

		DepositsQueryResponse response = restTemplate.postForObject(getConfigurationRESTUrl("deposits_query"), request, DepositsQueryResponse.class);
		return response;
	}

	public SalesQueryResponse getSalesQuery() throws Exception
	{
		SalesQueryRequest request = new SalesQueryRequest();
		request.setSessionID(sessionData.getServerSessionID());
		request.setSuppressSms(true);

		SalesQueryResponse response = restTemplate.postForObject(getConfigurationRESTUrl("sales_query"), request, SalesQueryResponse.class);
		return response;
	}

	// /transactions/change_pin
	public ChangePinResponse changePin(ChangePinRequest request) throws Exception
	{
		request.setSessionID(sessionData.getServerSessionID());
		ChangePinResponse response = restTemplate.postForObject(getConfigurationRESTUrl("change_pin"), request, ChangePinResponse.class);
		return response;
	}

	// /transactions/transfer
	public TransferResponse transferRequest(TransferRequest request) throws Exception
	{
		request.setSessionID(sessionData.getServerSessionID());
		TransferResponse response = restTemplate.postForObject(getConfigurationRESTUrl("transfer"), request, TransferResponse.class);
		return response;
	}

	// /transactions/self_topup
	public SelfTopUpResponse performSelfTopup(SelfTopUpRequest request) throws Exception
	{
		request.setSessionID(sessionData.getServerSessionID());
		SelfTopUpResponse response = restTemplate.postForObject(getConfigurationRESTUrl("self_topup"), request, SelfTopUpResponse.class);
		return response;
	}

	// /transactions/sell
	public SellResponse sellRequest(SellRequest request) throws Exception
	{
		request.setSessionID(sessionData.getServerSessionID());
		SellResponse response = restTemplate.postForObject(getConfigurationRESTUrl("sell"), request, SellResponse.class);
		return response;
	}
}
