package hxc.services.ecds.rest;

import static hxc.ecds.protocol.rest.TransactionServerResponse.error;
import static hxc.ecds.protocol.rest.TransactionServerResponse.ok;
import static hxc.services.ecds.rest.TransactionHelper.isEmpty;
import static hxc.services.ecds.util.MobileNumberFormatHelper.disableDualPhase;
import static hxc.services.ecds.util.MobileNumberFormatHelper.enableDualPhase;
import static hxc.services.ecds.util.MobileNumberFormatHelper.getProgress;
import static hxc.services.ecds.util.MobileNumberFormatHelper.loadConfig;
import static hxc.services.ecds.util.MobileNumberFormatHelper.loadMapping;
import static hxc.services.ecds.util.MobileNumberFormatHelper.saveConfig;
import static hxc.services.ecds.util.MobileNumberFormatHelper.saveMapping;
import static hxc.services.ecds.util.MobileNumberFormatHelper.stopTransformationInDatabase;
import static hxc.services.ecds.util.MobileNumberFormatHelper.transformNumbersInDatabase;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.MobileNumberFormatConfig;
import hxc.ecds.protocol.rest.MobileNumberFormatMapping;
import hxc.ecds.protocol.rest.TransactionServerResponse;
import hxc.ecds.protocol.rest.Violation;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.MobileNumberFormatException;

@Path("/mobile_numbers_transformation")
public class MobileNumbersTransformationController {
    final static Logger logger = LoggerFactory.getLogger(MobileNumbersTransformationController.class);

    @Context
    private ICreditDistribution context;

    @GET
    @Path("/mapping")
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionServerResponse getMapping(@HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            return loadMapping(em);
        }
    }

    @POST
    @Path("/mapping")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionServerResponse setMapping(MobileNumberFormatMapping mapping, @HeaderParam(RestParams.SID) String sessionID) {
        List<Violation> violations = mapping.validate();
        if (!isEmpty(violations)) {
            return error(violations.stream().map(Violation::getAdditionalInformation).collect(Collectors.joining("; ")));
        }
        try (EntityManagerEx em = context.getEntityManager()) {
            return saveMapping(em, mapping);
        }
    }

    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionServerResponse getConfig(@HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            return loadConfig(em);
        }
    }

    @POST
    @Path("/config")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionServerResponse changeConfig(MobileNumberFormatConfig config, @HeaderParam(RestParams.SID) String sessionID) {
        List<Violation> violations = config.validate();
        if (!isEmpty(violations)) {
            return error(violations.stream().map(Violation::getAdditionalInformation).collect(Collectors.joining("; ")));
        }
        try (EntityManagerEx em = context.getEntityManager()) {
            return saveConfig(em, config);
        }
    }

    @POST
    @Path("/start")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionServerResponse transformNumbers(@HeaderParam(RestParams.SID) String sessionID) {
        try {
            transformNumbersInDatabase(context);
            return ok();
        } catch (MobileNumberFormatException ex) {
            logger.warn("TRANSFORMER: Cannot start transformation.", ex);
            return error(ex.getMessage());
        }
    }

    @GET
    @Path("/progress")
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionServerResponse progress(@HeaderParam(RestParams.SID) String sessionID) {
        return getProgress();
    }

    @POST
    @Path("/stop")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionServerResponse stopTransformation(@HeaderParam(RestParams.SID) String sessionID) {
        stopTransformationInDatabase();
        return ok();
    }

    @POST
    @Path("/dual_phase/enable")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionServerResponse enable(@HeaderParam(RestParams.SID) String sessionID, @QueryParam("force") boolean force) {
        try (EntityManagerEx em = context.getEntityManager()) {
            enableDualPhase(em, force);
            return ok();
        } catch (MobileNumberFormatException ex) {
            logger.warn("TRANSFORMER: Cannot enable dual-phase.", ex);
            return error(ex.getMessage());
        }
    }

    @POST
    @Path("/dual_phase/disable")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TransactionServerResponse disable(@HeaderParam(RestParams.SID) String sessionID, @QueryParam("force") boolean force) {
        try (EntityManagerEx em = context.getEntityManager()) {
            disableDualPhase(em, force);
            return ok();
        } catch (MobileNumberFormatException ex) {
            logger.warn("TRANSFORMER: Cannot disable dual-phase.", ex);
            return error(ex.getMessage());
        }
    }
}
