package hxc.services.ecds.rest;

import hxc.connectors.kerberos.IAuthenticator;
import hxc.ecds.protocol.rest.AuthenticationRequest;
import hxc.ecds.protocol.rest.AuthenticationResponse;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.WebUsersConfig;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.*;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.Summariser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Objects;

import static hxc.connectors.kerberos.IAuthenticator.Result.*;
import static hxc.ecds.protocol.rest.AuthenticationResponse.*;
import static hxc.ecds.protocol.rest.IAuthenticatable.AUTHENTICATE_PASSWORD_2FACTOR;
import static hxc.ecds.protocol.rest.IAuthenticatable.AUTHENTICATE_PIN_2FACTOR;
import static hxc.ecds.protocol.rest.WebUser.STATE_ACTIVE;
import static hxc.ecds.protocol.rest.WebUser.STATE_PERMANENT;
import static hxc.ecds.protocol.rest.config.TransactionsConfig.*;
import static hxc.services.ecds.Session.CHANNEL_3PP;
import static hxc.services.ecds.Session.CHANNEL_WUI;
import static hxc.services.ecds.Session.State.*;
import static hxc.services.ecds.Session.UserType.*;
import static hxc.services.ecds.model.WebUser.NAME_SUPPLIER;
import static java.util.Arrays.asList;

@Path("/authentication")
public class Authentication {
    final static Logger logger = LoggerFactory.getLogger(Authentication.class);
    private static final String VALUE_PRIVATE_KEY = "VALUE_PRIVATE_KEY";
    private static final SecureRandom random = new SecureRandom();

    @Context
    private ICreditDistribution context;

    @POST
    @Path("/auth")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public hxc.ecds.protocol.rest.AuthenticationResponse auth(hxc.ecds.protocol.rest.AuthenticationRequest request) {
        try {
            AuthenticationResponse response = new AuthenticationResponse();
            response.setMoreInformationRequired(false);

            // Get the Session
            String sessionID = request.getSessionID();
            Session session;
            try {
                session = populateSession(sessionID, request, response);
            } catch (CannotPopulateSessionException ex) {
                return ex.response;
            }

            updateMacIpHostname(request, session);

            if (session.getUserType() == WEBUSER && (session.getAgentID() != null || session.getAgent() != null)) {
                logger.error("AgentID {} or Agent {} is present on WebUser session", session.getAgentID(), session.getAgent());
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_OTHER_ERROR);
                return response;
            }

            if (session.getUserType() == AGENT && (session.getWebUserID() != null)) {
                logger.error("AgentID {} or Agent {} is present on WebUser session", session.getAgentID(), session.getAgent());
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_OTHER_ERROR);
                return response;
            }

            response.setSessionID(session.getSessionID());

            switch (session.getState()) {
                // Not Authenticated - Requires UTF-8 Username
                case UNAUTHENTICATED:
                    response.setReturnCode(CODE_OK_NOW_REQUIRE_UTF8_USERNAME);
                    session.setState(Session.State.USERNAME);
                    response.setMoreInformationRequired(true);
                    break;

                // Has Username - Requires RSA Password
                case USERNAME:
                    AuthenticationResponse returnedResponse = authUsername(session, request, response);
                    if (returnedResponse != null) {
                        return returnedResponse;
                    }
                    break;

                // Has Username & Password for WebUser via SMART_DEVICE
                case IMSI:
                    authenticateImsi(session, request, response);
                    break;

                // Has Username & Password - Requires OTP
                case OTP:
                    authenticateOtp(request, response, session, request.getOneTimePin());
                    break;

                case AUTHENTICATED:
                    response.setReturnCode(CODE_OK_AUTHENTICATED);
                    break;

                default:
                    response.setReturnCode(CODE_FAIL_SESSION_INVALID);
            }
            return response;
        } catch (Exception e) {
            logger.error("Authentication failure", e);
        }
        return new AuthenticationResponse();
    }

    private void updateMacIpHostname(AuthenticationRequest request, Session session) {
        if (AuditEntry.isValidMacAddress(request.getMacAddress())) {
            session.setMacAddress(request.getMacAddress());
        }
        if (AuditEntry.isValidIpAddress(request.getIpAddress())) {
            session.setIpAddress(request.getIpAddress());
        }
        if (AuditEntry.isValidMachineName(request.getHostName())) {
            session.setMachineName(request.getHostName());
        }
    }

    private Session populateSession(String sessionID, AuthenticationRequest request, AuthenticationResponse response)
            throws CannotPopulateSessionException {
        Session session;
        if (sessionID == null || sessionID.isEmpty()) {
            CompanyInfo companyInfo = context.findCompanyInfoByID(request.getCompanyID());
            if (companyInfo == null) {
                logger.info("Invalid Company ID {} supplied", request.getCompanyID());
                response.setReturnCode(CODE_FAIL_INVALID_COMPANY);
                throw new CannotPopulateSessionException("Invalid Company ID", response);
            }

            // Set Channel
            session = context.getSessions().getNew(companyInfo);
            session.setChannel(request.getChannel());
            if (request.getUserType() != null) {
                switch (request.getUserType()) {
                    case WEBUSER:
                        session.setUserType(WEBUSER);
                        break;

                    case AGENT:
                        if (request.getData() != null && isServiceUser(fromUTF8(request.getData()), request.getCompanyID())) {
                            session.setUserType(SERVICE_USER);
                        } else {
                            session.setUserType(AGENT);
                        }
                        break;
                }
            } else {
                // XXX TODO FIXME : remove this once there is compliance
                session.setUserType(WEBUSER);
            }
            session.setCoSignForSessionID(request.getCoSignForSessionID());
            session.setCoSignatoryTransactionID(request.getCoSignatoryTransactionID());
            if (session.getCoSignatoryTransactionID() != null) {
                session.setCoSignOnly(true);
            }
            logger.trace("session.channel = {}, session.type = {}, authenticationRequest.userType = {}, session.coSignForSessionID = {}," +
                                 " session.coSignatoryTransactionID = {}, session.coSignOnly = {}",
                         session.getChannel(), session.getUserType(),
                         request.getUserType(), session.getCoSignForSessionID(),
                         session.getCoSignatoryTransactionID(),
                         session.getCoSignOnly());
        } else {
            try {
                session = context.getSession(sessionID);
            } catch (RuleCheckException ex) {
                logger.info("Rulecheck Issue", ex);
                session = null;
            }
            if (session == null) {
                logger.info("Invalid Session ID {} supplied", sessionID);
                response.setReturnCode(CODE_FAIL_SESSION_INVALID);
                throw new CannotPopulateSessionException("Invalid Session ID", response);
            }

            if (request.getUsername() != null && isServiceUser(request.getUsername(), request.getCompanyID())) {
                session.setUserType(SERVICE_USER);
            }
        }
        return session;
    }

    private void authenticateOtp(AuthenticationRequest request, AuthenticationResponse response, Session session, String otp) {
        byte[] sessionPIN = session.getTempPIN();
        session.setTempPIN(null);

        Long now = System.nanoTime();
        Long tempPINExpiry = session.getTempPINExpiry();
        session.setTempPINExpiry(null);
        logger.trace("now {} tempPINExpiry {}", now, tempPINExpiry);

        if (tempPINExpiry != null && now > tempPINExpiry) {
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_OTP_EXPIRED);
            logger.info("OTP expired {} > {} -> true", now, tempPINExpiry);
        } else if (otp == null || sessionPIN == null || !testIfSameCode(otp, sessionPIN) || !generateKeyPair(session, response)) {
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_OTP_INVALID);
            logger.info("Invalid OTP Supplied");
        } else {
            response.setReturnCode(CODE_OK_AUTHENTICATED);
            session.setState(AUTHENTICATED);
            if (session.getUserType() == WEBUSER) {
                logger.info("WebUser {} Authenticated", session.getWebUserID());
            } else if (session.getUserType() == AGENT) {
                logger.info("Agent {} Authenticated", session.getAgentID());
            }
        }
    }

    private void authenticateImsi(Session session, AuthenticationRequest request, AuthenticationResponse response) {
        IAgentUser abstractAgentUser = session.getAgentUser();
        if (request.getData() == null || request.getData().length == 0) {
            logger.trace("Agent (msisdn = {}, companyID = {}, imsi = {}) IMSI data.length = '{}' is null or empty ... continuing without it",
                         abstractAgentUser.getMobileNumber(),
                         abstractAgentUser.getCompanyID(),
                         abstractAgentUser.getImsi(),
                         (request.getData() == null ? null : request.getData().length));
            response.setReturnCode(CODE_OK_NOW_REQUIRE_UTF8_OTP);
            sendNewPinNotification(session, abstractAgentUser, request.getCustomPinChangeMessage());
            session.setState(OTP);
            response.setMoreInformationRequired(true);
        } else {
            String imsi = fromUTF8(request.getData());
            Date lastImsiChange = abstractAgentUser.getLastImsiChange();
            session.set("IMSI", imsi);
            logger.trace("Agent (msisdn = {}, companyID = {}, imsi = {}, lastImsiChange = {}) imsi = {}",
                         abstractAgentUser.getMobileNumber(),
                         abstractAgentUser.getCompanyID(),
                         abstractAgentUser.getImsi(), lastImsiChange, imsi);
            if (!validateAgentImsi(session)) {
                logger.info("Agent (msisdn = {}, companyID = {}, imsi = {}) is imsi locked (lastImsiChange = {})",
                            abstractAgentUser.getMobileNumber(),
                            abstractAgentUser.getCompanyID(), imsi,
                            lastImsiChange);
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_IMSI_LOCKOUT);
            } else {
                logger.info("Agent (msisdn = {}, companyID = {}, imsi = {}) is NOT imsi locked (lastImsiChange = {})",
                            abstractAgentUser.getMobileNumber(),
                            abstractAgentUser.getCompanyID(), imsi,
                            lastImsiChange);
                response.setReturnCode(CODE_OK_NOW_REQUIRE_UTF8_OTP);
                sendNewPinNotification(session, abstractAgentUser, request.getCustomPinChangeMessage());
                session.setState(OTP);
                response.setMoreInformationRequired(true);
            }
        }
    }

    private AuthenticationResponse authUsername(Session session, AuthenticationRequest request,
                                                AuthenticationResponse response) throws RuleCheckException {
        if (session.getUserType() != null && session.getUserType() == AGENT) {
            logger.trace("requested agent session ... determening nature of supplied username");
            String agentUsername = request.getUsername();
            Agent agent;
            AgentUser agentUser = null;
            try (EntityManagerEx em = context.getEntityManager()) {
                agent = Agent.findByDomainAccountName(em, session.getCompanyID(), agentUsername);
                if (agent == null) {
                    agentUser = AgentUser.findByDomainAccountName(em, session.getCompanyID(), agentUsername);
                }
            }
            logger.trace("agent = {}, agentUser = {}",
                         (agent == null ? null : Summariser.summarise(agent)),
                         (agentUser == null ? null : Summariser.summarise(agentUser)));
            if (agent != null || agentUser != null) {
                session.setDomainAccountName(agentUsername);
                response.setReturnCode(CODE_OK_NOW_REQUIRE_RSA_PASSWORD);
            } else {
                session.setMobileNumber(context.toMSISDN(agentUsername));
                response.setReturnCode(CODE_OK_NOW_REQUIRE_RSA_PIN);
            }
        } else {
            String domainAccountName = request.getUsername();
            session.setDomainAccountName(domainAccountName);
            response.setReturnCode(CODE_OK_NOW_REQUIRE_RSA_PASSWORD);
        }

        // Decode RSA Password
        String password = request.getPassword();

        WebUser webUser = null;
        Agent agent = null;
        AgentUser agentUser = null;
        IAgentUser abstractAgentUser = null;
        IAuthenticator.Result result;

        if (session.getUserType() == WEBUSER || session.getUserType() == SERVICE_USER) {
            try (EntityManagerEx em = context.getEntityManager()) {
                String domainAccountName = session.getDomainAccountName();
                webUser = WebUser.findByDomainAccountName(em, session.getCompanyID(), domainAccountName);
            }

            if (webUser == null) {
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_CREDENTIALS_INVALID);
                return response;
            }

            // Verify
            if (AUTHENTICATE_PASSWORD_2FACTOR.equals(webUser.getAuthenticationMethod())) {
                try (EntityManagerEx em = context.getEntityManager()) {
                    String offerResult = webUser.offerPIN(em, session, context.findCompanyInfoByID(session.getCompanyID()), password);
                    result = new IAuthenticator.Result();
                    if (offerResult == null) {
                        result.code = SUCCESS;
                        result.description = "Password OK";
                    } else if (ERR_PASSWORD_LOCKOUT.equals(offerResult) || ERR_PIN_LOCKOUT.equals(offerResult)) {
                        result.code = KDC_ERR_CLIENT_REVOKED;
                        result.description = offerResult;
                    } else if (ERR_INVALID_PASSWORD.equals(offerResult) || ERR_INVALID_PIN.equals(offerResult)) {
                        result.code = KRB_AP_ERR_BAD_INTEGRITY;
                        result.description = offerResult;
                    } else {
                        result.code = UNKNOWN_FAILURE;
                        result.description = offerResult;
                    }
                }
            } else {
                result = context.tryAuthenticate(webUser, password);
            }
        } else if (session.getUserType() == AGENT) {
            try (EntityManagerEx em = context.getEntityManager()) {
                if (session.getDomainAccountName() != null && session.getMobileNumber() == null) {
                    String domainAccountName = session.getDomainAccountName();
                    agent = Agent.findByDomainAccountName(em, session.getCompanyID(), domainAccountName);
                    if (agent == null) {
                        agentUser = AgentUser.findByDomainAccountNameWithAgent(em, session.getCompanyID(), domainAccountName);
                        abstractAgentUser = agentUser;
                    } else {
                        abstractAgentUser = agent;
                    }
                } else if (session.getMobileNumber() != null && session.getDomainAccountName() == null) {
                    agent = Agent.findByMSISDN(em, session.getMobileNumber(), session.getCompanyID());
                    if (agent == null) {
                        agentUser = AgentUser.findByMSISDNWithAgent(em, session.getMobileNumber(), session.getCompanyID());
                        abstractAgentUser = agentUser;
                    } else {
                        abstractAgentUser = agent;
                    }

                    if (abstractAgentUser != null && abstractAgentUser.getDomainAccountName() != null) {
                        logger.info("User with domainAccountName = {} may not use mobileNumber = {} for authentication",
                                    agent.getDomainAccountName(),
                                    session.getMobileNumber());
                        session.setState(UNAUTHENTICATED);
                        response.setReturnCode(CODE_FAIL_CREDENTIALS_INVALID);
                        return null;
                    }
                } else {
                    logger.error("Invalid user identity for agent session domainAccountName = {}, mobileNumber = {}",
                                 session.getDomainAccountName(),
                                 session.getMobileNumber());
                    session.setState(UNAUTHENTICATED);
                    response.setReturnCode(CODE_FAIL_OTHER_ERROR);
                    return null;
                }
                if (agentUser != null) {
                    agent = agentUser.getAgent();
                }
            }
            logger.trace("agent = {}, agentUser = {}",
                         (agent == null ? null : Summariser.summarise(agent)),
                         (agentUser == null ? null : Summariser.summarise(agentUser)));
            if (agent == null || abstractAgentUser == null) {
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_CREDENTIALS_INVALID);
                return response;
            }

            if (AUTHENTICATE_PASSWORD_2FACTOR.equals(abstractAgentUser.getAuthenticationMethod())) {
                try (EntityManagerEx em = context.getEntityManager()) {
                    String offerResult = abstractAgentUser.offerPIN(em, session, context.findCompanyInfoByID(session.getCompanyID()), password);
                    result = new IAuthenticator.Result();
                    if (offerResult == null) {
                        result.code = SUCCESS;
                        result.description = "Password OK";
                    } else if (ERR_PASSWORD_LOCKOUT.equals(offerResult) || ERR_PIN_LOCKOUT.equals(offerResult)) {
                        result.code = KDC_ERR_CLIENT_REVOKED;
                        result.description = offerResult;
                    } else if (ERR_INVALID_PASSWORD.equals(offerResult) || ERR_INVALID_PIN.equals(offerResult)) {
                        result.code = KRB_AP_ERR_BAD_INTEGRITY;
                        result.description = offerResult;
                    } else {
                        result.code = UNKNOWN_FAILURE;
                        result.description = offerResult;
                    }
                }
            } else {
                result = context.tryAuthenticate(abstractAgentUser, password);
            }
        } else {
            logger.error("Invalid session type {}", session.getUserType());
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_OTHER_ERROR);
            return null;
        }

        if (abstractAgentUser != null && webUser != null) {
            logger.error("Both agent and webuser set ... someone changed the code without understanding it");
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_OTHER_ERROR);
            return null;
        }

        if (result.code == IAuthenticator.Result.KDC_ERR_KEY_EXPIRED) {
            logger.info("KDC_ERR_KEY_EXPIRED reply was returned from Kerberos server");
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_PASSWORD_EXPIRED);
        } else if (result.code == KDC_ERR_C_PRINCIPAL_UNKNOWN || result.code == KRB_AP_ERR_BAD_INTEGRITY) {
            logger.info("KDC_ERR_C_PRINCIPAL_UNKNOWN or KRB_AP_ERR_BAD_INTEGRITY returned from Kerberos server");
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_CREDENTIALS_INVALID);
        } else if (result.code == KDC_ERR_CLIENT_REVOKED) {
            if (webUser != null && AUTHENTICATE_PASSWORD_2FACTOR.equals(webUser.getAuthenticationMethod())) {
                logger.info("CODE_FAIL_CREDENTIALS_INVALID from ECDS Password Authentication");
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_PASSWORD_LOCKOUT);
            } else {
                logger.info("CODE_FAIL_CREDENTIALS_INVALID reply was returned from Kerberos server");
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_CREDENTIALS_INVALID);
            }
        } else if (result.code != SUCCESS) {
            logger.info("Other error returned from Kerberos server ({})", result.code);
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_OTHER_ERROR);
        } else if (session.getChannel() == null) {
            logger.info("Login rejected as initially supplied channel is null ...");
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_CHANNEL_NOT_ALLOWED);
        }

        // THIS MUST COME AFTER SUCCESS CHECK ... ONLY SEND NOT ACTIVE
        // IF AUTH IS SUCCESFUL.
        else if (session.getUserType() == WEBUSER || session.getUserType() == SERVICE_USER) {
            if (!asList(CHANNEL_WUI, CHANNEL_3PP).contains(session.getChannel())) {
                logger.info("WebUser (id = {}, companyID = {}) is not allowed for channel {}",
                            webUser.getId(), webUser.getCompanyID(), session.getChannel());
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_CHANNEL_NOT_ALLOWED);
            } else if (!Objects.equals(webUser.getState(), STATE_PERMANENT) && !Objects.equals(webUser.getState(), STATE_ACTIVE)) {
                logger.info("WebUser state not permanent or active");
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_ACCOUNT_NOT_ACTIVE);
            } else {
                String channel = session.getChannel();
                session.setWebUser(webUser);
                boolean isServiceUser = false;
                if (webUser.getDomainAccountName().equals(NAME_SUPPLIER) || (isServiceUser = isServiceUser(webUser))) {
                    if (isServiceUser) {
                        session.setServiceUser(webUser, channel);
                    }
                    response.setReturnCode(CODE_OK_AUTHENTICATED);
                    session.setState(AUTHENTICATED);
                    logger.info("WebUser {} Authenticated without OTP as WebUser is supplier or service user {}",
                                session.getWebUserID(), webUser.getDomainAccountName());
                } else {
                    session.setState(OTP);
                    response.setReturnCode(CODE_OK_NOW_REQUIRE_UTF8_OTP);
                    response.setMoreInformationRequired(true);
                    sendNewPinNotification(session, webUser, request.getCustomPinChangeMessage());
                }
            }
        } else if (session.getUserType() == AGENT) {
            Boolean allowed = Session.checkAgentAllowedChannels(session.getChannel(), abstractAgentUser.getAllowedChannels());
            if (allowed == null || !allowed) {
                logger.info("Agent (id = {}, companyID = {}) is not allowed for channel {} (allowed={})",
                            abstractAgentUser.getId(), abstractAgentUser.getCompanyID(), session.getChannel(), allowed);
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_CHANNEL_NOT_ALLOWED);
            } else if (!Objects.equals(agent.getState(), STATE_PERMANENT) && !Objects.equals(agent.getState(), STATE_ACTIVE)) {
                logger.info("Agent state not permanent or active");
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_ACCOUNT_NOT_ACTIVE);
            } else if (agentUser != null && !Objects.equals(agentUser.getState(), STATE_ACTIVE)) {
                logger.info("AgentUser state not permanent or active");
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_ACCOUNT_NOT_ACTIVE);
            } else {
                session.withAgent(agent, agentUser); // TODO
                response.setMoreInformationRequired(true);
                if (Session.CHANNEL_3PP.equals(session.getChannel())) {
                    response.setReturnCode(CODE_OK_AUTHENTICATED);
                    session.setState(AUTHENTICATED);
                } else {
                    sendNewPinNotification(session, abstractAgentUser, request.getCustomPinChangeMessage());
                    response.setReturnCode(CODE_OK_NOW_REQUIRE_UTF8_OTP);
                    session.setState(OTP);
                }
            }
        } else {
            logger.error("Username stage of authentication not handled correctly", session.getDomainAccountName(), session.getMobileNumber());
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_OTHER_ERROR);
        }
        return null;
    }

    private boolean isServiceUser(WebUser webUser) {
        return webUser.getServiceUser() != null && webUser.getServiceUser();
    }

    private boolean isServiceUser(String domainAccountName, int companyId) {
        WebUser webUser;
        try (EntityManagerEx em = context.getEntityManager()) {
            webUser = WebUser.findByDomainAccountName(em, companyId, domainAccountName);
        }

        if (webUser != null) {
            return isServiceUser(webUser);
        } else {
            logger.info("Cannot find user with domain name " + domainAccountName);
            return false;
        }
    }

    @POST
    @Path("/authenticate")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public hxc.ecds.protocol.rest.AuthenticationResponse authenticate(hxc.ecds.protocol.rest.AuthenticationRequest request) {
        try {
            AuthenticationResponse response = new AuthenticationResponse();
            response.setMoreInformationRequired(false);

            // Get the Session
            String sessionID = request.getSessionID();
            Session session;
            try {
                session = populateSession(sessionID, request, response);
            } catch (CannotPopulateSessionException ex) {
                return ex.response;
            }

            updateMacIpHostname(request, session);

            if (session.getUserType() == WEBUSER && (session.getAgentID() != null || session.getAgent() != null)) {
                logger.error("AgentID {} or Agent {} is present on WebUser session", session.getAgentID(), session.getAgent());
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_OTHER_ERROR);
                return response;
            }

            if (session.getUserType() == AGENT && (session.getWebUserID() != null)) {
                logger.error("AgentID {} or Agent {} is present on WebUser session", session.getAgentID(), session.getAgent());
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_OTHER_ERROR);
                return response;
            }

            response.setSessionID(session.getSessionID());

            switch (session.getState()) {
                // Not Authenticated - Requires UTF-8 Username
                case UNAUTHENTICATED:
                    response.setReturnCode(CODE_OK_NOW_REQUIRE_UTF8_USERNAME);
                    session.setState(Session.State.USERNAME);
                    response.setMoreInformationRequired(true);
                    break;

                // Has Username - Requires RSA Password
                case USERNAME:
                    authenticateUsername(session, request, response);
                    break;

                // Decode Password
                case PASSWORD:
                    AuthenticationResponse returnedResponse = authenticatePassword(session, request, response);
                    if (returnedResponse != null) {
                        return returnedResponse;
                    }
                    break;

                // Has Username & Password for WebUser via SMART_DEVICE
                case IMSI:
                    authenticateImsi(session, request, response);
                    break;

                // Has Username & Password - Requires OTP
                case OTP:
                    authenticateOtp(request, response, session, decodeOtp(request));
                    break;

                case AUTHENTICATED:
                    response.setReturnCode(CODE_OK_AUTHENTICATED);
                    break;

                default:
                    response.setReturnCode(CODE_FAIL_SESSION_INVALID);
            }

            return response;
        } catch (Exception e) {
            logger.error("Authentication failure", e);
        }

        return new AuthenticationResponse();
    }

    private AuthenticationResponse authenticatePassword(Session session, AuthenticationRequest request,
                                                        AuthenticationResponse response) throws RuleCheckException {
        // Decode RSA Password
        String password = null;
        byte[] data = request.getData();
        PrivateKey privateKey = session.get(VALUE_PRIVATE_KEY);
        if (data != null && data.length > 0 && privateKey != null) {
            try {
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.DECRYPT_MODE, privateKey);
                byte[] dectypted = cipher.doFinal(data);
                password = new String(dectypted, "UTF-8");
            } catch (UnsupportedEncodingException | NoSuchAlgorithmException | NoSuchPaddingException
                    | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                logger.error("Unable to decode password", e);
                password = null;
            }
        }

        WebUser webUser = null;
        Agent agent = null;
        AgentUser agentUser = null;
        IAgentUser abstractAgentUser = null;
        IAuthenticator.Result result;
        if (session.getUserType() == WEBUSER || session.getUserType() == SERVICE_USER) {
            try (EntityManagerEx em = context.getEntityManager()) {
                String domainAccountName = session.getDomainAccountName();
                webUser = WebUser.findByDomainAccountName(em, session.getCompanyID(), domainAccountName);
            }

            if (webUser == null) {
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_CREDENTIALS_INVALID);
                return response;
            }

            // Verify
            if (AUTHENTICATE_PASSWORD_2FACTOR.equals(webUser.getAuthenticationMethod())) {
                try (EntityManagerEx em = context.getEntityManager()) {
                    String offerResult = webUser.offerPIN(em, session, context.findCompanyInfoByID(session.getCompanyID()), password);
                    result = new IAuthenticator.Result();
                    if (offerResult == null) {
                        result.code = SUCCESS;
                        result.description = "Password OK";
                    } else if (ERR_PASSWORD_LOCKOUT.equals(offerResult) || ERR_PIN_LOCKOUT.equals(offerResult)) {
                        result.code = KDC_ERR_CLIENT_REVOKED;
                        result.description = offerResult;
                    } else if (ERR_INVALID_PASSWORD.equals(offerResult) || ERR_INVALID_PIN.equals(offerResult)) {
                        result.code = KRB_AP_ERR_BAD_INTEGRITY;
                        result.description = offerResult;
                    } else {
                        result.code = UNKNOWN_FAILURE;
                        result.description = offerResult;
                    }
                }
            } else {
                result = context.tryAuthenticate(webUser, password);
            }
        } else if (session.getUserType() == AGENT) {
            try (EntityManagerEx em = context.getEntityManager()) {
                if (session.getDomainAccountName() != null && session.getMobileNumber() == null) {
                    String domainAccountName = session.getDomainAccountName();
                    agent = Agent.findByDomainAccountName(em, session.getCompanyID(), domainAccountName);
                    if (agent == null) {
                        agentUser = AgentUser.findByDomainAccountNameWithAgent(em, session.getCompanyID(), domainAccountName);
                        abstractAgentUser = agentUser;
                    } else {
                        abstractAgentUser = agent;
                    }
                } else if (session.getMobileNumber() != null && session.getDomainAccountName() == null) {
                    agent = Agent.findByMSISDN(em, session.getMobileNumber(), session.getCompanyID());
                    if (agent == null) {
                        agentUser = AgentUser.findByMSISDNWithAgent(em, session.getMobileNumber(), session.getCompanyID());
                        abstractAgentUser = agentUser;
                    } else {
                        abstractAgentUser = agent;
                    }
                    session.setDomainAccountName(session.getMobileNumber());
                    if (abstractAgentUser != null && abstractAgentUser.getDomainAccountName() != null) {
                        logger.info("User with domainAccountName = {} may not use mobileNumber = {} for authentication",
                                    agent.getDomainAccountName(), session.getMobileNumber());
                        session.setState(UNAUTHENTICATED);
                        response.setReturnCode(CODE_FAIL_CREDENTIALS_INVALID);
                        return null;
                    }
                } else {
                    logger.error("Invalid user identity for agent session domainAccountName = {}, mobileNumber = {}",
                                 session.getDomainAccountName(), session.getMobileNumber());
                    session.setState(UNAUTHENTICATED);
                    response.setReturnCode(CODE_FAIL_OTHER_ERROR);
                    return null;
                }
                if (agentUser != null) {
                    agent = agentUser.getAgent();
                }
            }
            logger.trace("agent = {}, agentUser = {}",
                         agent == null ? null : Summariser.summarise(agent),
                         agentUser == null ? null : Summariser.summarise(agentUser));
            if (agent == null || abstractAgentUser == null) {
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_CREDENTIALS_INVALID);
                return response;
            }

            if (AUTHENTICATE_PASSWORD_2FACTOR.equals(abstractAgentUser.getAuthenticationMethod()) ||
                AUTHENTICATE_PIN_2FACTOR.equals(abstractAgentUser.getAuthenticationMethod())) {
                try (EntityManagerEx em = context.getEntityManager()) {
                    String offerResult = abstractAgentUser.offerPIN(em, session, context.findCompanyInfoByID(session.getCompanyID()), password);
                    result = new IAuthenticator.Result();
                    if (offerResult == null) {
                        result.code = SUCCESS;
                        result.description = "Password OK";
                    } else if (ERR_PASSWORD_LOCKOUT.equals(offerResult) || ERR_PIN_LOCKOUT.equals(offerResult)) {
                        result.code = KDC_ERR_CLIENT_REVOKED;
                        result.description = offerResult;
                    } else if (ERR_INVALID_PASSWORD.equals(offerResult) || ERR_INVALID_PIN.equals(offerResult)) {
                        result.code = KRB_AP_ERR_BAD_INTEGRITY;
                        result.description = offerResult;
                    } else {
                        result.code = UNKNOWN_FAILURE;
                        result.description = offerResult;
                    }
                }
            } else {
                result = context.tryAuthenticate(abstractAgentUser, password);
            }
        } else {
            logger.error("Invalid session type {}", session.getUserType());
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_OTHER_ERROR);
            return null;
        }

        if (abstractAgentUser != null && webUser != null) {
            logger.error("Both agent and webuser set ... someone changed the code without understanding it");
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_OTHER_ERROR);
            return null;
        }

        if (result.code == IAuthenticator.Result.KDC_ERR_KEY_EXPIRED) {
            logger.info("KDC_ERR_KEY_EXPIRED reply was returned from Kerberos server");
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_PASSWORD_EXPIRED);
        } else if (result.code == KDC_ERR_C_PRINCIPAL_UNKNOWN || result.code == KRB_AP_ERR_BAD_INTEGRITY) {
            logger.info("KDC_ERR_C_PRINCIPAL_UNKNOWN or KRB_AP_ERR_BAD_INTEGRITY returned from Kerberos server");
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_CREDENTIALS_INVALID);
        } else if (result.code == KDC_ERR_CLIENT_REVOKED) {
            if (webUser != null && AUTHENTICATE_PASSWORD_2FACTOR.equals(webUser.getAuthenticationMethod())) {
                logger.info("CODE_FAIL_CREDENTIALS_INVALID from ECDS Password Authentication");
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_PASSWORD_LOCKOUT);
            } else {
                logger.info("CODE_FAIL_CREDENTIALS_INVALID reply was returned from Kerberos server");
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_CREDENTIALS_INVALID);
            }
        } else if (result.code != SUCCESS) {
            logger.info("Other error returned from Kerberos server ({})", result.code);
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_OTHER_ERROR);
        } else if (session.getChannel() == null) {
            logger.info("Login rejected as initially supplied channel is null ...");
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_CHANNEL_NOT_ALLOWED);
        }
        // THIS MUST COME AFTER SUCCESS CHECK ... ONLY SEND NOT ACTIVE
        // IF AUTH IS SUCCESFUL.
        else if (session.getUserType() == WEBUSER || session.getUserType() == SERVICE_USER) {
            if (!Session.CHANNEL_WUI.equals(session.getChannel())) {
                logger.info(String.format("WebUser (id = %s, companyID = %d) is not allowed for channel %s",
                                          webUser.getId(), webUser.getCompanyID(), session.getChannel()));
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_CHANNEL_NOT_ALLOWED);
            } else if (!Objects.equals(webUser.getState(), STATE_PERMANENT) && !Objects.equals(webUser.getState(), STATE_ACTIVE)) {
                logger.info("WebUser state not permanent or active");
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_ACCOUNT_NOT_ACTIVE);
            } else {
                String channel = session.getChannel();
                session.setWebUser(webUser);
                boolean isServiceUser = false;
                if (webUser.getDomainAccountName().equals(NAME_SUPPLIER)) {
                    if (isServiceUser) {
                        session.setServiceUser(webUser, channel);
                    }
                    response.setReturnCode(CODE_OK_AUTHENTICATED);
                    session.setState(AUTHENTICATED);
                    logger.info("WebUser {} Authenticated without OTP as WebUser is supplier {}",
                                session.getWebUserID(), webUser.getDomainAccountName());
                } else {
                    session.setState(OTP);
                    response.setReturnCode(CODE_OK_NOW_REQUIRE_UTF8_OTP);
                    response.setMoreInformationRequired(true);
                    sendNewPinNotification(session, webUser, request.getCustomPinChangeMessage());
                }
            }
        } else if (session.getUserType() == AGENT) {
            Boolean allowed = Session.checkAgentAllowedChannels(session.getChannel(), abstractAgentUser.getAllowedChannels());
            if (allowed == null || !allowed) {
                logger.info("Agent (id = {}, companyID = {}) is not allowed for channel {} (allowed={})",
                            abstractAgentUser.getId(), abstractAgentUser.getCompanyID(), session.getChannel(), allowed);
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_CHANNEL_NOT_ALLOWED);
            } else if (!Objects.equals(agent.getState(), STATE_PERMANENT) && !Objects.equals(agent.getState(), STATE_ACTIVE)) {
                logger.info("Agent state not permanent or active");
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_ACCOUNT_NOT_ACTIVE);
            } else if (agentUser != null && !Objects.equals(agentUser.getState(), STATE_ACTIVE)) {
                logger.info("AgentUser state not permanent or active");
                session.setState(UNAUTHENTICATED);
                response.setReturnCode(CODE_FAIL_ACCOUNT_NOT_ACTIVE);
            } else {
                session.withAgent(agent, agentUser); // TODO
                response.setMoreInformationRequired(true);
                    sendNewPinNotification(session, abstractAgentUser, request.getCustomPinChangeMessage());
                    response.setReturnCode(CODE_OK_NOW_REQUIRE_UTF8_OTP);
                    session.setState(OTP);
            }
        } else {
            logger.error("Username stage of authentication not handled correctly", session.getDomainAccountName(), session.getMobileNumber());
            session.setState(UNAUTHENTICATED);
            response.setReturnCode(CODE_FAIL_OTHER_ERROR);
        }
        return null;
    }

    private void authenticateUsername(Session session, AuthenticationRequest request, AuthenticationResponse response) {
        if (session.getUserType() != null && session.getUserType() == AGENT) {
            logger.trace("requested agent session ... determening nature of supplied username");
            String agentUsername = fromUTF8(request.getData());
            Agent agent;
            AgentUser agentUser = null;
            try (EntityManagerEx em = context.getEntityManager()) {
                agent = Agent.findByDomainAccountName(em, session.getCompanyID(), agentUsername);
                if (agent == null) {
                    agentUser = AgentUser.findByDomainAccountName(em, session.getCompanyID(), agentUsername);
                }
            }
            logger.trace("agent = {}, agentUser = {}",
                         (agent == null ? null : Summariser.summarise(agent)),
                         (agentUser == null ? null : Summariser.summarise(agentUser)));
            if (agent != null || agentUser != null) {
                session.setDomainAccountName(agentUsername);
                response.setReturnCode(CODE_OK_NOW_REQUIRE_RSA_PASSWORD);
            } else {
                session.setMobileNumber(context.toMSISDN(agentUsername));
                response.setReturnCode(CODE_OK_NOW_REQUIRE_RSA_PIN);
            }
        } else {
            String domainAccountName = fromUTF8(request.getData());
            session.setDomainAccountName(domainAccountName);
            response.setReturnCode(CODE_OK_NOW_REQUIRE_RSA_PASSWORD);
        }

        // Generate Key Pair
        generateKeyPair(session, response);
        response.setMoreInformationRequired(true);
        session.setState(Session.State.PASSWORD);
    }

    private String decodeOtp(AuthenticationRequest request) {
        String otp = null;
        byte[] data = request.getData();
        if (data != null && data.length > 0) {
            try {
                otp = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                logger.error("unsupported encoding", e);
            }
        }
        return otp;
    }

    @POST
    @Path("/fulfill")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public hxc.ecds.protocol.rest.AuthenticationRequest fullfil(
            hxc.ecds.protocol.rest.AuthenticationResponse response) {
        AuthenticationRequest result = new AuthenticationRequest();
        result.setSessionID(response.getSessionID());

        try {
            switch (response.getReturnCode()) {
                case CODE_OK_NOW_REQUIRE_UTF8_OTP:
                case CODE_OK_NOW_REQUIRE_UTF8_USERNAME:
                    result.setData(response.getValue().getBytes("UTF-8"));
                    break;

                case AuthenticationResponse.CODE_OK_NOW_REQUIRE_RSA_PASSWORD: {
                    Cipher encryptor = Cipher.getInstance("RSA");

                    X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(
                            response.getKey1());
                    PublicKey pubkey = KeyFactory.getInstance("RSA")
                            .generatePublic(pubKeySpec);

                    encryptor.init(Cipher.ENCRYPT_MODE, pubkey);
                    byte[] encodedData = encryptor
                            .doFinal(response.getValue().getBytes("UTF-8"));
                    result.setData(encodedData);
                }
                break;

                default:
                    throw new WebApplicationException(Status.BAD_REQUEST);
            }

            return result;

        } catch (Throwable tr) {
            logger.error(tr.getMessage(), tr);
            throw new WebApplicationException(tr, Status.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean validateAgentImsi(Session session) {
        try (EntityManagerEx em = context.getEntityManager()) {
            return validateAgentImsi(em, session, session.getAgent());
        }
    }

    private boolean validateAgentImsi(EntityManagerEx em, Session session,
                                      Agent agent) {
        try {
            CompanyInfo companyInfo = session.getCompanyInfo();
            TransactionsConfig transactionsConfig = companyInfo
                    .getConfiguration(em, TransactionsConfig.class);
            Agents.validateAgentImsi(context, em, transactionsConfig, session,
                                     agent);
        } catch (RuleCheckException exception) {
            logger.info(
                    "Agent (msisdn = {}, companyID = {}) - imsi validation : {}",
                    agent.getMobileNumber(), agent.getCompanyID(),
                    exception.getMessage());
            return false;
        }
        return true;
    }

    private String fromUTF8(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.info("UnsupportedEncodingException : {}", e.getMessage());
            return "";
        }
    }

    // Generate a Public/Private Key Pair
    private boolean generateKeyPair(Session session,
                                    AuthenticationResponse response) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
            keyGen.initialize(1024, random);
            KeyPair key = keyGen.generateKeyPair();
            PublicKey publicKey = key.getPublic();
            response.setKey1(publicKey.getEncoded());
            PrivateKey privateKey = key.getPrivate();
            session.set(VALUE_PRIVATE_KEY, privateKey);
        } catch (Exception ex) {
            logger.error("Failed to generate keypair", ex);
            return false;
        }

        return true;
    }

    private void sendNewPinNotification(Session session, WebUser user, String customMessage) {
        try {
            session.setTempPIN(null);
            int newPin = random.nextInt(90000) + 10000;
            String newPIN = String.format("%05d", newPin);
		    //logger.info("\n\n\n\n{}\n\n\n\n\n", newPIN);

	    byte[] encPin = encryptCode(newPIN);

	    CompanyInfo company = context
		    .findCompanyInfoByID(session.getCompanyID());

	    try (EntityManagerEx em = context.getEntityManager()) {
		    WebUsersConfig config = company.getConfiguration(em,
				    WebUsersConfig.class);

		    String text = customMessage;
		    if (text == null || text.length() == 0) {
			    text = config.getNewPinNotification()
				    .safe(user.getLanguage(), "");
		    }
		    session.setTempPINExpiry(System.nanoTime()
				    + ((long) config.getOtpExpiry() * 1000L * 1000L
					    * 1000L));
		    text = text.replace(WebUsersConfig.OTP, newPIN);

		    context.sendSMS(user.getMobileNumber(),
				    user.getLanguage(), text);

		    session.setTempPIN(encPin);
	    }
	} catch (Exception ex) {
		logger.error("Unable to generate webusers pin", ex);
	}
    }

    private void sendNewPinNotification(Session session,
                                        IAgentUser abstractAgentUser, String customMessage) {
        try {
            session.setTempPIN(null);
            int newPin = random.nextInt(90000) + 10000;
            String newPIN = String.format("%05d", newPin);

            //logger.info("\n\n\n\n\n ===== NEW PIN ====\n  {}\n\n\n\n", newPin);

            // Send SMS
            // XXX TODO FIXME ... nonsensical check here ... also if it made
            // sense then there should be at least some logging if it is null
            // (DON'T WRITE JOKE CODE !!!!)
            if (newPIN != null) {
                byte[] encPin = encryptCode(newPIN);

                if (encPin.length > 0) {
                    CompanyInfo company = context
                            .findCompanyInfoByID(session.getCompanyID());

                    try (EntityManagerEx em = context.getEntityManager()) {
                        AgentsConfig config = company.getConfiguration(em,
                                                                       AgentsConfig.class);
                        String text = customMessage;
                        if (text == null || text.length() == 0) {
                            text = config.getOtpNotification()
                                    .safe(abstractAgentUser.getLanguage(), "");
                        }

                        text = text.replace(AgentsConfig.OTP, newPIN);
                        session.setTempPINExpiry(System.nanoTime()
                                                         + ((long) config.getOtpExpiry() * 1000L * 1000L
                                * 1000L));
                        context.sendSMS(abstractAgentUser.getMobileNumber(),
                                        abstractAgentUser.getLanguage(), text);
                    }
                    session.setTempPIN(encPin);

                } // if (encPin.length > 0)
                // XXX TODO FIXME ... what if it is null length == 0 !? why no
                // logging ... (DON'T WRITE JOKE CODE !!!!)
            }

        } catch (Exception ex) {
            logger.error("Unable to generate agent users pin", ex);
        }
    }

    // /////////////////////////////////

    public byte[] encryptCode(String code) {
        try {
            final String FIXED_PREFIX = "%*CS!A1*%Z";

            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            code = FIXED_PREFIX + code;
            crypt.update(code.getBytes("UTF-8"));
            return crypt.digest();
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            logger.error("Unrecognized algorithm", e);
            return null;
        }
    }

    public boolean testIfSameCode(String code, byte[] key1) {
        if (key1 == null || key1.length == 0 || code == null || code.isEmpty()) {
            return false;
        }

        byte[] key = encryptCode(code);

        if (key.length != key1.length) {
            return false;
        }

        for (int index = 0; index < key.length; index++) {
            if (key[index] != key1[index]) {
                return false;
            }
        }

        return true;
    }
}
