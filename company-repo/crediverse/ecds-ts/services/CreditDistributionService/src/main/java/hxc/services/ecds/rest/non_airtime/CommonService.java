package hxc.services.ecds.rest.non_airtime;

import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.non_airtime.Request;
import hxc.ecds.protocol.rest.non_airtime.Response;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.WebUser;
import hxc.services.ecds.model.non_airtime.NonAirtimeTransactionDetails;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;

import java.util.Date;
import java.util.List;

import static hxc.ecds.protocol.rest.WebUser.STATE_ACTIVE;
import static hxc.ecds.protocol.rest.WebUser.STATE_PERMANENT;
import static hxc.services.ecds.model.Transaction.findByID;
import static hxc.services.ecds.model.non_airtime.NonAirtimeTransactionDetails.findByUserAndClientTransactionId;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

public class CommonService {
    final static Logger logger = LoggerFactory.getLogger(CommonService.class);
    public static final String REQUEST_IS_STALE = "Request is stale";
    public static final String FORBIDDEN_MESSAGE = "You are not authorized to call this endpoint.";
    public static final String INVALID_SESSION_MESSAGE = "Looks like your session is invalid.";

    static Session checkPermissionAndGetSession(EntityManagerEx em, ICreditDistribution context, String sessionID) {
        try {
            Session session = context.getSession(sessionID);
            WebUser webUser = WebUser.findByDomainAccountName(em, session.getCompanyID(), session.getDomainAccountName());
            if (webUser == null || webUser.getServiceUser() == null
                    || !webUser.getServiceUser()
                    || (!webUser.getState().equals(STATE_ACTIVE) && !webUser.getState().equals(STATE_PERMANENT))) {
                throw new ForbiddenException(FORBIDDEN_MESSAGE);
            }
            return session;
        } catch (RuleCheckException ex) {
            if (ex.getStatus() == StatusCode.UNAUTHORIZED.getStatus()) {
                throw new NotAuthorizedException(INVALID_SESSION_MESSAGE);
            } else {
                throw new WebApplicationException(ex, INTERNAL_SERVER_ERROR);
            }
        }
    }

    static boolean isExpired(Request request) {
        return request.getExpiryTimeInMillisecondsSinceUnixEpoch() != null
                && new Date().getTime() > request.getExpiryTimeInMillisecondsSinceUnixEpoch();
    }

    static Response prepareRequestIsStaleResponse(String msisdn, Request request) {
        logger.info(REQUEST_IS_STALE + " MSISDN: " + msisdn + " Client trn id: " + request.getClientTransactionId());
        Response response = request.createResponse();
        response.setReturnCode(TransactionsConfig.ERR_TIMED_OUT);
        response.setAdditionalInformation(REQUEST_IS_STALE);
        response.setResponse(REQUEST_IS_STALE);
        return response;
    }

    public static String executionTime(long start) {
        long time = System.nanoTime() - start;
        long seconds = time / 1_000_000_000;
        long ms = Math.round((time - (seconds * 1_000_000_000)) / 1_000_000);
        return "execution time: " + (seconds == 0 ? "" : seconds + " s. ") + ms + " ms.";
    }

    static Agent getAgent(String msisdn, String sessionID, EntityManagerEx em, ICreditDistribution context) {
        Session session = checkPermissionAndGetSession(em, context, sessionID);
        Agent agent = Agent.findByMSISDN(em, context.toMSISDN(msisdn), session.getCompanyID());
        if (agent == null) {
            throw new NotFoundException(String.format("Agent with msisdn %s not found", msisdn));
        }
        return agent;
    }

    static Response getTransactionStatus(String clientTransactionId, String sessionID, ICreditDistribution context) {
        Response response = new Response();
        try (EntityManagerEx em = context.getEntityManager()) {
            Session session = checkPermissionAndGetSession(em, context, sessionID);
            List<NonAirtimeTransactionDetails> nonAirtimeTransactionDetailsList =
                    findByUserAndClientTransactionId(em, session.getWebUserID(), clientTransactionId);
            if (nonAirtimeTransactionDetailsList.isEmpty()) {
                throw new NotFoundException(String.format("Non-airtime transaction with clientTransactionId %s not found", clientTransactionId));
            }
            NonAirtimeTransactionDetails nonAirtimeTransaction = nonAirtimeTransactionDetailsList.get(0);

            Transaction transaction = findByID(em, nonAirtimeTransaction.getId(), session.getCompanyID());
            if (transaction == null) {
                String message = String.format(
                        "Transaction for non-airtime transaction with clientTransactionId %s not found", clientTransactionId);
                logger.error(message);
                throw new InternalServerErrorException(message);
            }

            response.setReturnCode(ResponseHeader.RETURN_CODE_SUCCESS);
            response.setStatus(transaction.getReturnCode());
            response.setCrediverseTransactionId(transaction.getNumber());
            response.setTransactionEndTimestamp(transaction.getLastTime().getTime());
            response.setClientTransactionId(nonAirtimeTransaction.getClientTransactionId());
        }
        return response;
    }
}
