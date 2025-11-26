package hxc.services.ecds.rest.non_airtime;

import javax.annotation.PostConstruct;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.non_airtime.DebitRequest;
import hxc.ecds.protocol.rest.non_airtime.RefundRequest;
import hxc.ecds.protocol.rest.non_airtime.Response;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.rest.RestParams;
import hxc.services.ecds.util.EntityManagerEx;

import static hxc.services.ecds.rest.non_airtime.CommonService.*;

@Path("/service/agent/{msisdn}")
public class NonAirtimeController {
    final static Logger logger = LoggerFactory.getLogger(NonAirtimeController.class);

    @Context
    private ICreditDistribution context;
    
    private DebitService debitService;
    private RefundService refundService;
    
    @PostConstruct
    public void init() {
        debitService = new DebitService(context);
        refundService = new RefundService(context);
    }

    @GET
    @Path("/details")
    @Produces(MediaType.APPLICATION_JSON)
    public hxc.ecds.protocol.rest.Agent getAgentByMsisdn(@PathParam("msisdn") String msisdn, @HeaderParam(RestParams.SID) String sessionID) {
        long start = System.nanoTime();
        logger.info("Enter /service/agent/" + msisdn + "/details endpoint.");
        try (EntityManagerEx em = context.getEntityManager()) {
            Agent agent = getAgent(msisdn, sessionID, em, context);
            logger.info("Exit /service/agent/" + msisdn + "/details endpoint, " + executionTime(start));
            return agent;
        }
    }

    @GET
    @Path("/transaction/{clientTransactionId}/status")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response getStatus(@PathParam("msisdn") String msisdn, @PathParam("clientTransactionId") String clientTransactionId,
                          @HeaderParam(RestParams.SID) String sessionID) {
        long start = System.nanoTime();
        logger.info("Enter /service/agent/" + msisdn + "/transaction/" + clientTransactionId + "/status");
        Response response = getTransactionStatus(clientTransactionId, sessionID, context);
        logger.info("Exit /service/agent/" + msisdn + "/transaction/" + clientTransactionId + "/status, " + executionTime(start));
        return response;
    }

    @POST
    @Path("/debit")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response debit(@PathParam("msisdn") String msisdn, DebitRequest debitRequest, @HeaderParam(RestParams.SID) String sessionID) {
        long start = System.nanoTime();
        logger.info("Enter /service/agent/" + msisdn + "/debit Client trn id: " + debitRequest.getClientTransactionId());
        checkPermission(context, sessionID);
        debitRequest.setSessionID(sessionID);
        debitRequest.setMsisdn(msisdn);
        Response response;
        if (isExpired(debitRequest)) {
            response = prepareRequestIsStaleResponse(msisdn, debitRequest);
        } else {
            response = debitService.execute(debitRequest);
        }
        logger.info("Exit /service/agent/" + msisdn + "/debit Client trn id: " + debitRequest.getClientTransactionId() + ", " + executionTime(start));
        return response;
    }

    @POST
    @Path("/refund")
    @Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
    public Response refund(@PathParam("msisdn") String msisdn, RefundRequest refundRequest, @HeaderParam(RestParams.SID) String sessionID) {
        long start = System.nanoTime();
        logger.info("Enter /service/agent/" + msisdn + "/refund Client trn id: " + refundRequest.getClientTransactionId());
        checkPermission(context, sessionID);
        refundRequest.setSessionID(sessionID);
        refundRequest.setMsisdn(msisdn);
        Response response;
        if (isExpired(refundRequest)) {
            response = prepareRequestIsStaleResponse(msisdn, refundRequest);
        } else {
            response = refundService.execute(refundRequest);
        }
        logger.info("Exit /service/agent/" + msisdn + "/refund Client trn id: " + refundRequest.getClientTransactionId()
                            + ", " + executionTime(start));
        return response;
    }

    void checkPermission(ICreditDistribution context, String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            checkPermissionAndGetSession(em, context, sessionID);
        }
    }

}
