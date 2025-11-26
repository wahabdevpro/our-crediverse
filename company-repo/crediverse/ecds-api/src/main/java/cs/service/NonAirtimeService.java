package cs.service;

import cs.config.RestServerConfiguration;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.non_airtime.DebitRequest;
import hxc.ecds.protocol.rest.non_airtime.RefundRequest;
import hxc.ecds.protocol.rest.non_airtime.Request;
import hxc.ecds.protocol.rest.non_airtime.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;

@Service
public class NonAirtimeService {
    @Autowired
    private CsRestTemplate restTemplate;

    @Autowired
    private RestServerConfiguration restServerConfig;

    private String agentServiceUrl;

    @PostConstruct
    public void configure() {
        this.agentServiceUrl = restServerConfig.getRestServer() + restServerConfig.getAgentServiceUrl();
    }

    public Response debit(String msisdn, DebitRequest debitRequest) throws Exception {
        addExpiryTimeIfMissing(debitRequest);
        return restTemplate.postForObject(agentServiceUrl + "/" + msisdn + "/debit", debitRequest, Response.class);
    }

    public Response refund(String msisdn, RefundRequest refundRequest) throws Exception {
        addExpiryTimeIfMissing(refundRequest);
        return restTemplate.postForObject(agentServiceUrl + "/" + msisdn + "/refund", refundRequest, Response.class);
    }

    public Response getTransactionStatus(String msisdn, String clientTransactionId) throws Exception {
        return restTemplate.execute(agentServiceUrl + "/" + msisdn + "/transaction/" + clientTransactionId + "/status",
                                    HttpMethod.GET, Response.class);
    }

    private void addExpiryTimeIfMissing(Request request) {
        if (request.getExpiryTimeInMillisecondsSinceUnixEpoch() == null
                && restServerConfig.getNonAirtimeRequestTimeoutMs() != null) {
            request.setExpiryTimeInMillisecondsSinceUnixEpoch(new Date().getTime() + restServerConfig.getNonAirtimeRequestTimeoutMs());
        }
    }
}
