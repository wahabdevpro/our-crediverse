package hxc.services.ecds.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import hxc.services.ecds.reports.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.ExResultList;
import hxc.ecds.protocol.rest.ResponseHeader;
import hxc.ecds.protocol.rest.config.ReportingConfig;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportListResult;
import hxc.ecds.protocol.rest.reports.AccountBalanceSummaryReportResult;
import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportListResult;
import hxc.ecds.protocol.rest.reports.DailyGroupSalesReportResult;
import hxc.ecds.protocol.rest.reports.DailyPerformanceByAreaListResult;
import hxc.ecds.protocol.rest.reports.ExecuteScheduleRequest;
import hxc.ecds.protocol.rest.reports.ExecuteScheduleResponse;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.MonthlySalesPerformanceReportResult;
import hxc.ecds.protocol.rest.reports.ReportsByArea;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.RetailerPerformanceReportResult;
import hxc.ecds.protocol.rest.reports.SalesSummaryReportListResult;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportListResult;
import hxc.ecds.protocol.rest.reports.WholesalerPerformanceReportResult;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.ReportScheduleExecutor;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.ReportSchedule;
import hxc.services.ecds.model.ReportSpecification;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.WebUser;
import hxc.services.ecds.reports.sales_summary.SalesSummaryReportParameters;
import hxc.services.ecds.reports.sales_summary.SalesSummaryReportSpecification;
import hxc.services.ecds.util.DbUtils;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.QueryBuilder;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

@Path("/reports")
public class Reports {
    final static Logger logger = LoggerFactory.getLogger(Reports.class);

    @Context
    private ICreditDistribution context;

    //@Context
    //private Response response;

    private void checkReportCountLimit(EntityManagerEx em, Session session) throws RuleCheckException {
        if (session.getUserType() == Session.UserType.AGENT) {
            List<ReportSpecification> specs = ReportSpecification.findByAgentID(em, session.getAgentID(), session.getCompanyID());

            Agent agent = Agent.findByID(em, session.getAgentID(), session.getCompanyID());
            Integer limit = agent.getReportCountLimit();
            if (limit == null) {
                CompanyInfo companyInfo = session.getCompanyInfo();
                ReportingConfig configuration = companyInfo.getConfiguration(em, ReportingConfig.class);
                limit = configuration.getAgentReportCountLimit();
            }

            if (specs.size() >= limit) {
                logger.trace("Reports.createReportSpecification: report count limit reached: {}", limit);
                throw new RuleCheckException(StatusCode.LIMIT_REACHED, null, "Report count limit reached (%s)", limit);
            }
        }
    }

    private int getScheduleDailyExecutionCount(hxc.ecds.protocol.rest.reports.ReportSchedule schedule) {
        switch (schedule.getPeriod()) {
            case MINUTE: // internal
                return 0;

            case HOUR:
                Integer endTime = schedule.getEndTimeOfDay();
                Integer startTime = schedule.getStartTimeOfDay();
                if (endTime == null) {
                    endTime = 86399;
                }
                if (startTime == null) {
                    startTime = 0;
                }
                return (int) Math.abs(endTime - startTime + 3599) / (int) 3600;

            case DAY:
                return 1;

            case WEEK:
            case MONTH: // ignore
                return 0;
        }

        return 0;
    }

    private void checkReportDailyScheduleLimit(EntityManagerEx em, Session session,
                                               hxc.ecds.protocol.rest.reports.ReportSchedule schedule) throws RuleCheckException {
        if (session.getUserType() == Session.UserType.AGENT) {
            Agent agent = Agent.findByID(em, session.getAgentID(), session.getCompanyID());
            Integer limit = agent.getReportDailyScheduleLimit();
            if (limit == null) {
                CompanyInfo companyInfo = session.getCompanyInfo();
                ReportingConfig configuration = companyInfo.getConfiguration(em, ReportingConfig.class);
                limit = configuration.getAgentReportDailyScheduleLimit();
            }

            int scheduleCount = getScheduleDailyExecutionCount(schedule);

            logger.trace("Reports.checkReportDailyScheduleLimit: report schedule id {} (the one being updated), counts as {} runs, limit is {}", schedule.getId(), scheduleCount, limit);

            List<ReportSpecification> reportSpecifications = ReportSpecification.findByAgentID(em, session.getAgentID(), session.getCompanyID());
            for (ReportSpecification reportSpecification : reportSpecifications) {
                List<ReportSchedule> reportSchedules = reportSpecification.getSchedules();
                for (ReportSchedule reportSchedule : reportSchedules) {
                    if (schedule.getId() == reportSchedule.getId()) {
                        continue;
                    }
                    if (!reportSchedule.getEnabled()) {
                        continue;
                    }

                    int executions = getScheduleDailyExecutionCount(reportSchedule);
                    scheduleCount += executions;
                    logger.trace("Reports.checkReportDailyScheduleLimit: report schedule id {}, counts as {} runs, total is {}, limit is {}", schedule.getId(), executions, scheduleCount, limit);
                }
            }

            if (scheduleCount > limit) {
                logger.trace("Reports.checkReportDailyScheduleLimit: report schedule count limit reached: {} >= {}", scheduleCount, limit);
                throw new RuleCheckException(StatusCode.LIMIT_REACHED, null, "Report schedule count limit reached (%s >= %s)", scheduleCount, limit);
            }
        }
    }

    // //////////////////////////////////////////////////////////////
    // Retailer Performance :: AdHoc (Live Preview/Export ?)
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/retailer_performance/adhoc/json")
    @Produces(MediaType.APPLICATION_JSON)
    public RetailerPerformanceReportResult getRetailerPerformanceReportAdhocJson(
            @HeaderParam(RestParams.SID) String sessionID,
            @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
            @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
            @QueryParam(RestParams.SORT) String sortString,
            @QueryParam(RestParams.FILTER) String filterString,
            @QueryParam("timeInterval.start") String timeIntervalStart,
            @QueryParam("timeInterval.end") String timeIntervalEnd,
            @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
            @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            logger.trace(
                    "Reports.getRetailerPerformanceReportAdhocJson: first = {}, max = {}, sortString = {}, filterString = {}, timeIntervalStart = {}, timeIntervalEnd = {}, relativeTimeRangeCode = {}, relativeTimeRangeReferenceString = {}",
                    first, max, sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
            Session session = context.getSession(sessionID);
            RetailerPerformanceReport.Processor processor = new RetailerPerformanceReport.Processor(apEm, session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode,
                                                                                                    relativeTimeRangeReferenceString);
            logger.trace("Reports.getRetailerPerformanceReportAdhocJson: processor = {}", processor);
            RetailerPerformanceReportResult result = processor.result(first, max);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/retailer_performance/adhoc/json", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/retailer_performance/adhoc/json", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/retailer_performance/adhoc/csv")
    @Produces("text/csv")

    public Response getRetailerPerformanceReportAdhocCsv(@HeaderParam(RestParams.SID) String sessionID,
                                                         @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                         @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                         @QueryParam(RestParams.SORT) String sortString,
                                                         @QueryParam(RestParams.FILTER) String filterString,
                                                         @QueryParam("timeInterval.start") String timeIntervalStart,
                                                         @QueryParam("timeInterval.end") String timeIntervalEnd,
                                                         @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
                                                         @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            logger.trace(
                    "Reports.getRetailerPerformanceReportAdhocCsv: first = {}, max = {}, sortString = {}, filterString = {}, timeIntervalStart = {}, timeIntervalEnd = {}, relativeTimeRangeCode = {}, relativeTimeRangeReferenceString = {}",
                    first, max, sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
            Session session = context.getSession(sessionID);
            RetailerPerformanceReport.Processor processor = new RetailerPerformanceReport.Processor(apEm, session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode,
                                                                                                    relativeTimeRangeReferenceString);
            logger.trace("Reports.getRetailerPerformanceReportAdhocCsv: processor = {}", processor);
            List<RetailerPerformanceReport.ResultEntry> entries = processor.entries(first, max);
            RetailerPerformanceReport.CsvExportProcessor csvExportProcessor = new RetailerPerformanceReport.CsvExportProcessor(first);
            String result = csvExportProcessor.add(entries);

            return Response.ok(result).header("recordCount", String.valueOf(entries.size())).build();
        } catch (RuleCheckException ex) {
            logger.error("/retailer_performance/adhoc/csv", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/retailer_performance/adhoc/csv", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    // //////////////////////////////////////////////////////////////
    // Retailer Performance :: Saved
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/retailer_performance")
    @Produces(MediaType.APPLICATION_JSON)
    public RetailerPerformanceReportListResult listRetailerPerformanceReports(@HeaderParam(RestParams.SID) String sessionID,
                                                                             @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                             @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                             @QueryParam(RestParams.SORT) String sort,
                                                                             @QueryParam(RestParams.SEARCH) String search,
                                                                             @QueryParam(RestParams.FILTER) String filter,
                                                                             @QueryParam(RestParams.WITHCOUNT) Integer withcount) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
            Session session = context.getSession(params.getSessionID());
            boolean performCount = ((withcount == null) || (withcount != null && withcount == 1));

            {
                String agentPredicate = String.format("+%s='%d'", "agentID", session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : 0);
                String currentFilter = params.getFilter();
                if (currentFilter != null && !currentFilter.isEmpty()) {
                    currentFilter = agentPredicate + "+" + currentFilter;
                } else {
                    currentFilter = agentPredicate;
                }
                params.setFilter(currentFilter);
            }

            RetailerPerformanceReportListResult result = new RetailerPerformanceReportListResult();
            List<ReportSpecification> reportSpecifications;
            // TODO Remove when slow queries have been properly fixed!
            try (QueryToken token = context.getQueryToken()) {
                reportSpecifications = ReportSpecification.findType(em, params, session.getCompanyID(), Report.Type.RETAILER_PERFORMANCE);
            }
            if (performCount) {
                // result.setFound(ReportSpecification.findTypeCount(em, params, session.getCompanyID(), Report.Type.RETAILER_PERFORMANCE));
                result.setFound(Long.valueOf(QueryBuilder.getFoundRows(em)));
            }

            List<RetailerPerformanceReportSpecification> entries = new ArrayList<RetailerPerformanceReportSpecification>();
            for (ReportSpecification reportSpecification : reportSpecifications) {
                entries.add(new RetailerPerformanceReportSpecification(reportSpecification));
            }
            result.setEntries(entries);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/retailer_performance", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/retailer_performance", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/retailer_performance/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RetailerPerformanceReportSpecification getRetailerPerformanceReport(@HeaderParam(RestParams.SID) String sessionID,
                                                                                @PathParam("id") int specificationID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.RETAILER_PERFORMANCE);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            RetailerPerformanceReportSpecification retailerPerformanceReportSpecification = new RetailerPerformanceReportSpecification(reportSpecification);
            return retailerPerformanceReportSpecification;
        } catch (RuleCheckException ex) {
            logger.error("/retailer_performance/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/retailer_performance/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/retailer_performance/{id}/json")
    @Produces(MediaType.APPLICATION_JSON)
    public RetailerPerformanceReportResult getRetailerPerformanceReportJson(@HeaderParam(RestParams.SID) String sessionID,
                                                                            @PathParam("id") int specificationID,
                                                                            @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                            @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                            @QueryParam(RestParams.SORT) String sortString,
                                                                            @QueryParam(RestParams.FILTER) String filterString,
                                                                            @QueryParam("timeInterval.start") String timeIntervalStart,
                                                                            @QueryParam("timeInterval.end") String timeIntervalEnd,
                                                                            @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
                                                                            @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.RETAILER_PERFORMANCE);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            RetailerPerformanceReportSpecification retailerPerformanceReportSpecification = new RetailerPerformanceReportSpecification(reportSpecification);

            logger.trace("Reports.getRetailerPerformanceReportJson: retailerPerformanceReportSpecification = {}", retailerPerformanceReportSpecification);
            RetailerPerformanceReport.Processor processor = new RetailerPerformanceReport.Processor(apEm, session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, retailerPerformanceReportSpecification.getParameters(), relativeTimeRangeReferenceString);
            processor.setParameters(sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
            logger.trace("Reports.getRetailerPerformanceReportJson: processor = {}", processor);
            RetailerPerformanceReportResult result = processor.result(first, max);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/retailer_performance/{id}/json", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/retailer_performance/{id}/json", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/retailer_performance/{id}/csv")
    @Produces("text/csv")
    public Response getRetailerPerformanceReportCsv(
            @HeaderParam(RestParams.SID) String sessionID,
            @PathParam("id") int specificationID,
            @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
            @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
            @QueryParam(RestParams.SORT) String sortString,
            @QueryParam(RestParams.FILTER) String filterString,
            @QueryParam("timeInterval.start") String timeIntervalStart,
            @QueryParam("timeInterval.end") String timeIntervalEnd,
            @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
            @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.RETAILER_PERFORMANCE);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            RetailerPerformanceReportSpecification retailerPerformanceReportSpecification = new RetailerPerformanceReportSpecification(reportSpecification);

            logger.trace("Reports.getRetailerPerformanceReportCsv: retailerPerformanceReportSpecification = {}", retailerPerformanceReportSpecification);
            RetailerPerformanceReport.Processor processor = new RetailerPerformanceReport.Processor(apEm, session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, retailerPerformanceReportSpecification.getParameters(), relativeTimeRangeReferenceString);
            processor.setParameters(sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
            logger.trace("Reports.getRetailerPerformanceReportCsv: processor = {}", processor);
            List<RetailerPerformanceReport.ResultEntry> entries = processor.entries(first, max);
            RetailerPerformanceReport.CsvExportProcessor csvExportProcessor = new RetailerPerformanceReport.CsvExportProcessor(first);
            String result = csvExportProcessor.add(entries);
            return Response.ok(result).header("recordCount", String.valueOf(entries.size())).build();
        } catch (RuleCheckException ex) {
            logger.error("/retailer_performance/{id}/csv", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/retailer_performance/{id}/csv", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/retailer_performance")
    @Produces(MediaType.APPLICATION_JSON)
    public RetailerPerformanceReportSpecification createRetailerPerformanceReportSpecification(
            @HeaderParam(RestParams.SID) String sessionID,
            @QueryParam("name") String name,
            @QueryParam("description") String description,
            @QueryParam(RestParams.SORT) String sortString,
            @QueryParam(RestParams.FILTER) String filterString,
            @QueryParam("timeInterval.start") String timeIntervalStart,
            @QueryParam("timeInterval.end") String timeIntervalEnd,
            @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            RetailerPerformanceReportParameters parameters = new RetailerPerformanceReportParameters(filterString, sortString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode);
            RetailerPerformanceReportSpecification specification = new RetailerPerformanceReportSpecification();
            specification.setCompanyID(session.getCompanyID());
            specification.setAgentID(session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : 0);
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);
            specification.setOriginator(Report.Originator.USER);

            logger.trace("specification = {}", specification);

            return this.createReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/retailer_performance", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/retailer_performance", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/retailer_performance/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public RetailerPerformanceReportSpecification updateRetailerPerformanceReportSpecification(@HeaderParam(RestParams.SID) String sessionID,
                                                                                               @PathParam("id") int specificationID,
                                                                                               @PathParam("version") int version,
                                                                                               @QueryParam("name") String name,
                                                                                               @QueryParam("description") String description,
                                                                                               @QueryParam(RestParams.SORT) String sortString,
                                                                                               @QueryParam(RestParams.FILTER) String filterString,
                                                                                               @QueryParam("timeInterval.start") String timeIntervalStart,
                                                                                               @QueryParam("timeInterval.end") String timeIntervalEnd,
                                                                                               @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            RetailerPerformanceReportParameters parameters = new RetailerPerformanceReportParameters(filterString, sortString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode);
            RetailerPerformanceReportSpecification specification = new RetailerPerformanceReportSpecification();

            specification.setId(specificationID);
            specification.setCompanyID(session.getCompanyID());
            specification.setAgentID(session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : 0);
            specification.setVersion(version);
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);

            logger.trace("specification = {}", specification);

            return this.updateReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/retailer_performance/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/retailer_performance/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/retailer_performance/{id}")
    public void deleteRetailerPerformanceReportSpecification(@PathParam("id") int specificationID, @HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            session.check(em, ReportSpecification.MAY_DELETE, "Not allowed to Delete Report %d", specificationID);
            ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.RETAILER_PERFORMANCE);
            if (existingSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            if (Objects.equals(existingSpecification.getOriginator(), Report.Originator.MINIMUM_REQUIRED_DATA)) {
                throw new RuleCheckException(StatusCode.FORBIDDEN, null, "May not delete reports with originator MINIMUM_REQUIRED_DATA", existingSpecification.getOriginator());
            }
            AuditEntryContext auditContext = new AuditEntryContext("RETAILER_PERFORMANCE_REPORT_REMOVE", existingSpecification.getName(), existingSpecification.getId());
            existingSpecification.remove(em, session, auditContext);
        } catch (RuleCheckException ex) {
            logger.error("/retailer_performance/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/retailer_performance/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    public RetailerPerformanceReportSpecification createReportSpecification(EntityManagerEx em, RetailerPerformanceReportSpecification specification,
                                                                            Session session, RestParams params)
            throws Exception {
        session.check(em, ReportSpecification.MAY_ADD, "Not allowed to Create Report");

        checkReportCountLimit(em, session);

        ReportSpecification newSpecification = new ReportSpecification();
        newSpecification.amend(specification);
        newSpecification.setParameters(RetailerPerformanceReportParameters.toJson(specification.getParameters()));
        newSpecification.setType(Report.Type.RETAILER_PERFORMANCE.toString());
        logger.trace("Reports.createReportSpecification: newSpecification = {}", newSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("RETAILER_PERFORMANCE_REPORT_CREATE", newSpecification.getName());
        newSpecification.persist(em, null, session, auditContext);
        specification.setId(newSpecification.getId());
        return specification;
    }

    public RetailerPerformanceReportSpecification updateReportSpecification(EntityManagerEx em, RetailerPerformanceReportSpecification specification,
                                                                            Session session, RestParams params)
            throws Exception {
        session.check(em, ReportSpecification.MAY_UPDATE, "Not allowed to Update Report %d", specification.getId());

        ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specification.getId(), session.getCompanyID(), Report.Type.RETAILER_PERFORMANCE);
        if (existingSpecification == null || existingSpecification.getCompanyID() != session.getCompanyID()) {
            throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specification.getId());
        }
        specification.setSchedules(existingSpecification.getSchedules());
        ReportSpecification updatedSpecification = existingSpecification;
        existingSpecification = new ReportSpecification(existingSpecification);
        logger.trace("Reports.updateReportSpecification: existingSpecification = {}", existingSpecification);

        RetailerPerformanceReportSpecification updatedSpecificationTyped = new RetailerPerformanceReportSpecification(updatedSpecification);
        updatedSpecificationTyped.amend(specification);
        logger.trace("Reports.updateReportSpecification: updatedSpecificationTyped = {}", updatedSpecificationTyped);
        updatedSpecification.amend(updatedSpecificationTyped.toReportSpecification());
        if (updatedSpecification.getOriginator() == null) {
            updatedSpecification.setOriginator(Report.Originator.USER);
        }
        logger.trace("Reports.updateReportSpecification: updatedSpecification = {}", updatedSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("RETAILER_REPORT_SPECIFICATION_UPDATE", updatedSpecification.getName(), updatedSpecification.getId());
        updatedSpecification.persist(em, existingSpecification, session, auditContext);
        return updatedSpecificationTyped;
    }

    // //////////////////////////////////////////////////////////////
    // Wholesaler Performance :: AdHoc
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/wholesaler_performance/adhoc/json")
    @Produces(MediaType.APPLICATION_JSON)
    public WholesalerPerformanceReportResult getWholesalerPerformanceReportAdhocJson(@HeaderParam(RestParams.SID) String sessionID,
                                                                                     @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                                     @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                                     @QueryParam(RestParams.SORT) String sortString,
                                                                                     @QueryParam(RestParams.FILTER) String filterString,
                                                                                     @QueryParam("timeInterval.start") String timeIntervalStart,
                                                                                     @QueryParam("timeInterval.end") String timeIntervalEnd,
                                                                                     @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
                                                                                     @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            logger.trace(
                    "Reports.getWholesalerPerformanceReportAdhocJson: first = {}, max = {}, sortString = {}, filterString = {}, timeIntervalStart = {}, timeIntervalEnd = {}, relativeTimeRangeCode = {}, relativeTimeRangeReferenceString = {}",
                    first, max, sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
            Session session = context.getSession(sessionID);
            WholesalerPerformanceReport.Processor processor = new WholesalerPerformanceReport.Processor(apEm, session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode,
                                                                                                        relativeTimeRangeReferenceString);
            logger.trace("Reports.getWholesalerPerformanceReportAdhocJson: processor = {}", processor);
            WholesalerPerformanceReportResult result = processor.result(first, max);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/wholesaler_performance/adhoc/json", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/wholesaler_performance/adhoc/json", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/wholesaler_performance/adhoc/csv")
    @Produces("text/csv")
    public Response getWholesalerPerformanceReportAdhocCsv(@HeaderParam(RestParams.SID) String sessionID,
                                                           @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                           @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                           @QueryParam(RestParams.SORT) String sortString,
                                                           @QueryParam(RestParams.FILTER) String filterString,
                                                           @QueryParam("timeInterval.start") String timeIntervalStart,
                                                           @QueryParam("timeInterval.end") String timeIntervalEnd,
                                                           @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
                                                           @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            logger.trace(
                    "Reports.getWholesalerPerformanceReportAdhocCsv: first = {}, max = {}, sortString = {}, filterString = {}, timeIntervalStart = {}, timeIntervalEnd = {}, relativeTimeRangeCode = {}, relativeTimeRangeReferenceString = {}",
                    first, max, sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
            Session session = context.getSession(sessionID);
            WholesalerPerformanceReport.Processor processor = new WholesalerPerformanceReport.Processor(apEm, session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode,
                                                                                                        relativeTimeRangeReferenceString);
            logger.trace("Reports.getWholesalerPerformanceReportAdhocCsv: processor = {}", processor);
            List<WholesalerPerformanceReport.ResultEntry> entries = processor.entries(first, max);
            WholesalerPerformanceReport.CsvExportProcessor csvExportProcessor = new WholesalerPerformanceReport.CsvExportProcessor(first);
            String result = csvExportProcessor.add(entries);
            return Response.ok(result).header("recordCount", String.valueOf(entries.size())).build();
        } catch (RuleCheckException ex) {
            logger.error("/wholesaler_performance/adhoc/csv", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/wholesaler_performance/adhoc/csv", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    // //////////////////////////////////////////////////////////////
    // Wholesaler Performance :: Saved
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/wholesaler_performance")
    @Produces(MediaType.APPLICATION_JSON)
    public WholesalerPerformanceReportListResult getWholesalerPerformanceReports(@HeaderParam(RestParams.SID) String sessionID,
                                                                                 @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                                 @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                                 @QueryParam(RestParams.SORT) String sort,
                                                                                 @QueryParam(RestParams.SEARCH) String search,
                                                                                 @QueryParam(RestParams.FILTER) String filter,
                                                                                 @QueryParam(RestParams.WITHCOUNT) Integer withcount) {
        try (EntityManagerEx em = context.getEntityManager()) {
            boolean performCount = ((withcount == null) || (withcount != null && withcount == 1));
            RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
            Session session = context.getSession(params.getSessionID());

            {
                String agentPredicate = String.format("+%s='%d'", "agentID", session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : 0);
                String currentFilter = params.getFilter();
                if (currentFilter != null && !currentFilter.isEmpty()) {
                    currentFilter = agentPredicate + "+" + currentFilter;
                } else {
                    currentFilter = agentPredicate;
                }
                params.setFilter(currentFilter);
            }

            WholesalerPerformanceReportListResult result = new WholesalerPerformanceReportListResult();
            List<ReportSpecification> reportSpecifications;
            // TODO Remove when slow queries have been properly fixed!
            try (QueryToken token = context.getQueryToken()) {
                reportSpecifications = ReportSpecification.findType(em, params, session.getCompanyID(), Report.Type.WHOLESALER_PERFORMANCE);
            }
            if (performCount) {
                // result.setFound(ReportSpecification.findTypeCount(em, params, session.getCompanyID(), Report.Type.WHOLESALER_PERFORMANCE));
                result.setFound(Long.valueOf(QueryBuilder.getFoundRows(em)));
            }

            List<WholesalerPerformanceReportSpecification> entries = new ArrayList<WholesalerPerformanceReportSpecification>();
            for (ReportSpecification reportSpecification : reportSpecifications) {
                entries.add(new WholesalerPerformanceReportSpecification(reportSpecification));
            }
            result.setEntries(entries);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/wholesaler_performance", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/wholesaler_performance", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/wholesaler_performance/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public WholesalerPerformanceReportSpecification getWholesalerPerformanceReports(@HeaderParam(RestParams.SID) String sessionID,
                                                                                    @PathParam("id") int specificationID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.WHOLESALER_PERFORMANCE);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            WholesalerPerformanceReportSpecification wholesalerPerformanceReportSpecification = new WholesalerPerformanceReportSpecification(reportSpecification);
            return wholesalerPerformanceReportSpecification;
        } catch (RuleCheckException ex) {
            logger.error("/wholesaler_performance/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/wholesaler_performance/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/wholesaler_performance/{id}/json")
    @Produces(MediaType.APPLICATION_JSON)
    public WholesalerPerformanceReportResult getWholesalerPerformanceReportJson(@HeaderParam(RestParams.SID) String sessionID,
                                                                                @PathParam("id") int specificationID,
                                                                                @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                                @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                                @QueryParam(RestParams.SORT) String sortString,
                                                                                @QueryParam(RestParams.FILTER) String filterString,
                                                                                @QueryParam("timeInterval.start") String timeIntervalStart,
                                                                                @QueryParam("timeInterval.end") String timeIntervalEnd,
                                                                                @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
                                                                                @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.WHOLESALER_PERFORMANCE);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            WholesalerPerformanceReportSpecification wholesalerPerformanceReportSpecification = new WholesalerPerformanceReportSpecification(reportSpecification);

            logger.trace("Reports.getWholesalerPerformanceReportJson: wholesalerPerformanceReportSpecification = {}", wholesalerPerformanceReportSpecification);
            WholesalerPerformanceReport.Processor processor = new WholesalerPerformanceReport.Processor(apEm, session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, wholesalerPerformanceReportSpecification.getParameters(),
                                                                                                        relativeTimeRangeReferenceString);
            processor.setParameters(sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
            logger.trace("Reports.getWholesalerPerformanceReportJson: processor = {}", processor);
            WholesalerPerformanceReportResult result = processor.result(first, max);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/wholesaler_performance/{id}/json", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/wholesaler_performance/{id}/json", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/wholesaler_performance/{id}/csv")
    @Produces("text/csv")
    public Response getWholesalerPerformanceReportCsv(@HeaderParam(RestParams.SID) String sessionID, @PathParam("id") int specificationID,
                                                      @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                      @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                      @QueryParam(RestParams.SORT) String sortString,
                                                      @QueryParam(RestParams.FILTER) String filterString,
                                                      @QueryParam("timeInterval.start") String timeIntervalStart,
                                                      @QueryParam("timeInterval.end") String timeIntervalEnd,
                                                      @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
                                                      @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.WHOLESALER_PERFORMANCE);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            WholesalerPerformanceReportSpecification wholesalerPerformanceReportSpecification = new WholesalerPerformanceReportSpecification(reportSpecification);

            logger.trace("Reports.getWholesalerPerformanceReportCsv: wholesalerPerformanceReportSpecification = {}", wholesalerPerformanceReportSpecification);
            WholesalerPerformanceReport.Processor processor = new WholesalerPerformanceReport.Processor(apEm, session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, wholesalerPerformanceReportSpecification.getParameters(),
                                                                                                        relativeTimeRangeReferenceString);
            processor.setParameters(sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
            logger.trace("Reports.getWholesalerPerformanceReportCsv: processor = {}", processor);
            List<WholesalerPerformanceReport.ResultEntry> entries = processor.entries(first, max);
            WholesalerPerformanceReport.CsvExportProcessor csvExportProcessor = new WholesalerPerformanceReport.CsvExportProcessor(first);
            String result = csvExportProcessor.add(entries);
            return Response.ok(result).header("recordCount", String.valueOf(entries.size())).build();
        } catch (RuleCheckException ex) {
            logger.error("/wholesaler_performance/{id}/csv", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/wholesaler_performance/{id}/csv", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/wholesaler_performance")
    @Produces(MediaType.APPLICATION_JSON)
    public WholesalerPerformanceReportSpecification createWholesalerPerformanceReportSpecification(@HeaderParam(RestParams.SID) String sessionID,
                                                                                                   @QueryParam("name") String name,
                                                                                                   @QueryParam("description") String description,
                                                                                                   @QueryParam(RestParams.SORT) String sortString,
                                                                                                   @QueryParam(RestParams.FILTER) String filterString,
                                                                                                   @QueryParam("timeInterval.start") String timeIntervalStart,
                                                                                                   @QueryParam("timeInterval.end") String timeIntervalEnd,
                                                                                                   @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            WholesalerPerformanceReportParameters parameters = new WholesalerPerformanceReportParameters(filterString, sortString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode);
            WholesalerPerformanceReportSpecification specification = new WholesalerPerformanceReportSpecification();
            specification.setCompanyID(session.getCompanyID());
            specification.setAgentID(session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : 0);
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);
            specification.setOriginator(Report.Originator.USER);

            logger.trace("specification = {}", specification);

            return this.createReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/wholesaler_performance", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/wholesaler_performance", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/wholesaler_performance/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public WholesalerPerformanceReportSpecification updateWholesalerPerformanceReportSpecification(@HeaderParam(RestParams.SID) String sessionID,
                                                                                                   @PathParam("id") int specificationID,
                                                                                                   @PathParam("version") int version,
                                                                                                   @QueryParam("name") String name,
                                                                                                   @QueryParam("description") String description,
                                                                                                   @QueryParam(RestParams.SORT) String sortString,
                                                                                                   @QueryParam(RestParams.FILTER) String filterString,
                                                                                                   @QueryParam("timeInterval.start") String timeIntervalStart,
                                                                                                   @QueryParam("timeInterval.end") String timeIntervalEnd,
                                                                                                   @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            WholesalerPerformanceReportParameters parameters = new WholesalerPerformanceReportParameters(filterString, sortString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode);
            WholesalerPerformanceReportSpecification specification = new WholesalerPerformanceReportSpecification();

            specification.setId(specificationID);
            specification.setCompanyID(session.getCompanyID());
            specification.setAgentID(session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : 0);
            specification.setVersion(version);
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);

            logger.trace("specification = {}", specification);

            return this.updateReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/wholesaler_performance/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/wholesaler_performance/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/wholesaler_performance/{id}")
    public void deleteWholesalerPerformanceReportSpecification(@PathParam("id") int specificationID, @HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            session.check(em, ReportSpecification.MAY_DELETE, "Not allowed to Delete Report %d", specificationID);
            ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.WHOLESALER_PERFORMANCE);
            if (existingSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            if (Objects.equals(existingSpecification.getOriginator(), Report.Originator.MINIMUM_REQUIRED_DATA)) {
                throw new RuleCheckException(StatusCode.FORBIDDEN, null, "May not delete reports with originator MINIMUM_REQUIRED_DATA", existingSpecification.getOriginator());
            }
            AuditEntryContext auditContext = new AuditEntryContext("WHOLESALER_PERFORMANCE_REPORT_DELETE", existingSpecification.getName(), existingSpecification.getId());
            existingSpecification.remove(em, session, auditContext);
        } catch (RuleCheckException ex) {
            logger.error("/wholesaler_performance/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/wholesaler_performance/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    public WholesalerPerformanceReportSpecification createReportSpecification(EntityManagerEx em,
                                                                              WholesalerPerformanceReportSpecification specification, Session session,
                                                                              RestParams params)
            throws Exception {
        session.check(em, ReportSpecification.MAY_ADD, "Not allowed to Create Report");

        checkReportCountLimit(em, session);

        ReportSpecification newSpecification = new ReportSpecification();
        newSpecification.amend(specification);
        newSpecification.setParameters(WholesalerPerformanceReportParameters.toJson(specification.getParameters()));
        newSpecification.setType(Report.Type.WHOLESALER_PERFORMANCE.toString());
        logger.trace("Reports.createReportSpecification: newSpecification = {}", newSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("WHOLESALER_PERFORMANCE_REPORT_CREATE", newSpecification.getName());
        newSpecification.persist(em, null, session, auditContext);
        specification.setId(newSpecification.getId());
        return specification;
    }

    public WholesalerPerformanceReportSpecification updateReportSpecification(EntityManagerEx em,
                                                                              WholesalerPerformanceReportSpecification specification, Session session,
                                                                              RestParams params)
            throws Exception {
        session.check(em, ReportSpecification.MAY_UPDATE, "Not allowed to Update Report {}", specification.getId());

        ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specification.getId(), session.getCompanyID(), Report.Type.WHOLESALER_PERFORMANCE);
        if (existingSpecification == null || existingSpecification.getCompanyID() != session.getCompanyID()) {
            throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specification.getId());
        }
        specification.setSchedules(existingSpecification.getSchedules());
        ReportSpecification updatedSpecification = existingSpecification;
        existingSpecification = new ReportSpecification(existingSpecification);
        logger.trace("Reports.updateReportSpecification: existingSpecification = {}", existingSpecification);

        WholesalerPerformanceReportSpecification updatedSpecificationTyped = new WholesalerPerformanceReportSpecification(updatedSpecification);
        updatedSpecificationTyped.amend(specification);
        logger.trace("Reports.updateReportSpecification: updatedSpecificationTyped = {}", updatedSpecificationTyped);
        updatedSpecification.amend(updatedSpecificationTyped.toReportSpecification());
        if (updatedSpecification.getOriginator() == null) {
            updatedSpecification.setOriginator(Report.Originator.USER);
        }
        logger.trace("Reports.updateReportSpecification: updatedSpecification = {}", updatedSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("WHOLESALER_REPORT_SPECIFICATION_UPDATE", updatedSpecification.getName(), updatedSpecification.getId());
        updatedSpecification.persist(em, existingSpecification, session, auditContext);
        return updatedSpecificationTyped;
    }

    // //////////////////////////////////////////////////////////////
    // Wholesaler Performance :: Saved
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/sales_summary")
    @Produces(MediaType.APPLICATION_JSON)
    public SalesSummaryReportListResult getSalesSummaryReports(@HeaderParam(RestParams.SID) String sessionID,
                                                               @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                               @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                               @QueryParam(RestParams.SORT) String sort, @QueryParam(RestParams.SEARCH) String search,
                                                               @QueryParam(RestParams.FILTER) String filter,
                                                               @QueryParam(RestParams.WITHCOUNT) Integer withcount) {
        try (EntityManagerEx em = context.getEntityManager()) {
            boolean performCount = ((withcount == null) || (withcount != null && withcount == 1));
            RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
            Session session = context.getSession(params.getSessionID());
            SalesSummaryReportListResult result = new SalesSummaryReportListResult();
            List<ReportSpecification> reportSpecifications;
            // TODO Remove when slow queries have been properly fixed!
            try (QueryToken token = context.getQueryToken()) {
                reportSpecifications = ReportSpecification.findType(em, params, session.getCompanyID(), Report.Type.SALES_SUMMARY);
            }
            if (performCount) {
                // result.setFound(ReportSpecification.findTypeCount(em, params, session.getCompanyID(), Report.Type.SALES_SUMMARY));
                result.setFound(Long.valueOf(QueryBuilder.getFoundRows(em)));
            }

            List<SalesSummaryReportSpecification> entries = new ArrayList<SalesSummaryReportSpecification>();
            for (ReportSpecification reportSpecification : reportSpecifications) {
                entries.add(new SalesSummaryReportSpecification(reportSpecification));
            }
            result.setEntries(entries);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/sales_summary", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/sales_summary", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/sales_summary/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public SalesSummaryReportSpecification getSalesSummaryReports(@HeaderParam(RestParams.SID) String sessionID,
                                                                  @PathParam("id") int specificationID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.SALES_SUMMARY);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            SalesSummaryReportSpecification salesSummaryReportSpecification = new SalesSummaryReportSpecification(reportSpecification);
            return salesSummaryReportSpecification;
        } catch (RuleCheckException ex) {
            logger.error("/sales_summary/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/sales_summary/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/sales_summary/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public SalesSummaryReportSpecification updateSalesSummaryReportSpecification(@HeaderParam(RestParams.SID) String sessionID,
                                                                                 @PathParam("id") int specificationID,
                                                                                 @PathParam("version") int version, @QueryParam("name") String name,
                                                                                 @QueryParam("description") String description) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            SalesSummaryReportParameters parameters = new SalesSummaryReportParameters();
            SalesSummaryReportSpecification specification = new SalesSummaryReportSpecification();

            specification.setId(specificationID);
            specification.setCompanyID(session.getCompanyID());
            specification.setVersion(version);
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);

            logger.trace("specification = {}", specification);

            return this.updateReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/sales_summary/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/sales_summary/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    public SalesSummaryReportSpecification createReportSpecification(EntityManagerEx em, SalesSummaryReportSpecification specification,
                                                                     Session session, RestParams params) throws Exception {
        session.check(em, ReportSpecification.MAY_ADD, "Not allowed to Create Report");

        checkReportCountLimit(em, session);

        ReportSpecification newSpecification = new ReportSpecification();
        newSpecification.amend(specification);
        newSpecification.setParameters(SalesSummaryReportParameters.toJson(specification.getParameters()));
        newSpecification.setType(Report.Type.SALES_SUMMARY.toString());
        logger.trace("Reports.createReportSpecification: newSpecification = {}", newSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("SALES_SUMMARY_REPORT_CREATE", newSpecification.getName());
        newSpecification.persist(em, null, session, auditContext);
        specification.setId(newSpecification.getId());
        return specification;
    }

    public SalesSummaryReportSpecification updateReportSpecification(EntityManagerEx em, SalesSummaryReportSpecification specification,
                                                                     Session session, RestParams params) throws Exception {
        session.check(em, ReportSpecification.MAY_UPDATE, "Not allowed to Update Report %d", specification.getId());

        ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specification.getId(), session.getCompanyID(), Report.Type.SALES_SUMMARY);
        if (existingSpecification == null || existingSpecification.getCompanyID() != session.getCompanyID()) {
            throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specification.getId());
        }
        specification.setSchedules(existingSpecification.getSchedules());
        ReportSpecification updatedSpecification = existingSpecification;
        existingSpecification = new ReportSpecification(existingSpecification);
        logger.trace("Reports.updateReportSpecification: existingSpecification = {}", existingSpecification);

        SalesSummaryReportSpecification updatedSpecificationTyped = new SalesSummaryReportSpecification(updatedSpecification);
        updatedSpecificationTyped.amend(specification);
        logger.trace("Reports.updateReportSpecification: updatedSpecificationTyped = {}", updatedSpecificationTyped);
        updatedSpecification.amend(updatedSpecificationTyped.toReportSpecification());
        if (updatedSpecification.getOriginator() == null) {
            updatedSpecification.setOriginator(Report.Originator.USER);
        }
        logger.trace("Reports.updateReportSpecification: updatedSpecification = {}", updatedSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("SALES_SUMMARY_REPORT_SPECIFICATION_UPDATE", updatedSpecification.getName(), updatedSpecification.getId());
        updatedSpecification.persist(em, existingSpecification, session, auditContext);
        return updatedSpecificationTyped;
    }

    // //////////////////////////////////////////////////////////////
    // Daily Group Sales :: AdHoc
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/daily_group_sales/adhoc/json")
    @Produces(MediaType.APPLICATION_JSON)
    public DailyGroupSalesReportResult getDailyGroupSalesReportAdhocJson(@HeaderParam(RestParams.SID) String sessionID,
                                                                         @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                         @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                         @QueryParam(RestParams.SORT) String sortString,
                                                                         @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            logger.trace("Reports.getDailyGroupSalesReportAdhocJson: first = {}, max = {}, sortString = {}, filterString = {}", first, max, sortString, filterString);
            Session session = context.getSession(sessionID);
            DailyGroupSalesReport.Processor processor = new DailyGroupSalesReport.Processor(em, apEm, session.getCompanyID(), sortString, filterString);
            logger.trace("Reports.getDailyGroupSalesReportAdhocJson: processor = {}", processor);
            DailyGroupSalesReportResult result = processor.result(first, max);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/daily_group_sales/adhoc/json", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_group_sales/adhoc/json", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/daily_group_sales/adhoc/csv")
    @Produces("text/csv")
    public Response getDailyGroupSalesReportAdhocCsv(@HeaderParam(RestParams.SID) String sessionID,
                                                     @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                     @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                     @QueryParam(RestParams.SORT) String sortString,
                                                     @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            logger.trace("Reports.getDailyGroupSalesReportAdhocCsv: first = {}, max = {}, sortString = {}, filterString = {}", first, max, sortString, filterString);
            Session session = context.getSession(sessionID);
            DailyGroupSalesReport.Processor processor = new DailyGroupSalesReport.Processor(em, apEm, session.getCompanyID(), sortString, filterString);
            logger.trace("Reports.getDailyGroupSalesReportAdhocCsv: processor = {}", processor);
            List<DailyGroupSalesReport.ResultEntry> entries = processor.entries(first, max);
            DailyGroupSalesReport.CsvExportProcessor csvExportProcessor = new DailyGroupSalesReport.CsvExportProcessor(first);
            String result = csvExportProcessor.add(entries);
            return Response.ok(result).header("recordCount", String.valueOf(entries.size())).build();
        } catch (RuleCheckException ex) {
            logger.error("/daily_group_sales/adhoc/csv", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_group_sales/adhoc/csv", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    // //////////////////////////////////////////////////////////////
    // Daily Group Sales :: Saved
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/daily_group_sales")
    @Produces(MediaType.APPLICATION_JSON)
    public DailyGroupSalesReportListResult getDailyGroupSalesReports(@HeaderParam(RestParams.SID) String sessionID,
                                                                     @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                     @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                     @QueryParam(RestParams.SORT) String sort,
                                                                     @QueryParam(RestParams.SEARCH) String search,
                                                                     @QueryParam(RestParams.FILTER) String filter,
                                                                     @QueryParam(RestParams.WITHCOUNT) Integer withcount) {
        try (EntityManagerEx em = context.getEntityManager()) {
            boolean performCount = ((withcount == null) || (withcount != null && withcount == 1));
            RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
            Session session = context.getSession(params.getSessionID());
            DailyGroupSalesReportListResult result = new DailyGroupSalesReportListResult();
            List<ReportSpecification> reportSpecifications;
            // TODO Remove when slow queries have been properly fixed!
            try (QueryToken token = context.getQueryToken()) {
                reportSpecifications = ReportSpecification.findType(em, params, session.getCompanyID(), Report.Type.DAILY_GROUP_SALES);
            }
            if (performCount) {
                // result.setFound(ReportSpecification.findTypeCount(em, params, session.getCompanyID(), Report.Type.DAILY_GROUP_SALES));
                result.setFound(Long.valueOf(QueryBuilder.getFoundRows(em)));
            }

            List<DailyGroupSalesReportSpecification> entries = new ArrayList<DailyGroupSalesReportSpecification>();
            for (ReportSpecification reportSpecification : reportSpecifications) {
                entries.add(new DailyGroupSalesReportSpecification(reportSpecification));
            }
            result.setEntries(entries);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/daily_group_sales", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_group_sales", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/daily_group_sales/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DailyGroupSalesReportSpecification getDailyGroupSalesReports(@HeaderParam(RestParams.SID) String sessionID,
                                                                        @PathParam("id") int specificationID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.DAILY_GROUP_SALES);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            DailyGroupSalesReportSpecification dailyGroupSalesReportSpecification = new DailyGroupSalesReportSpecification(reportSpecification);
            return dailyGroupSalesReportSpecification;
        } catch (RuleCheckException ex) {
            logger.error("/daily_group_sales/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_group_sales/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/daily_group_sales/{id}/json")
    @Produces(MediaType.APPLICATION_JSON)
    public DailyGroupSalesReportResult getDailyGroupSalesReportJson(@HeaderParam(RestParams.SID) String sessionID,
                                                                    @PathParam("id") int specificationID,
                                                                    @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                    @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                    @QueryParam(RestParams.SORT) String sortString,
                                                                    @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.DAILY_GROUP_SALES);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            DailyGroupSalesReportSpecification dailyGroupSalesReportSpecification = new DailyGroupSalesReportSpecification(reportSpecification);

            logger.trace("Reports.getDailyGroupSalesReportJson: dailyGroupSalesReportSpecification = {}", dailyGroupSalesReportSpecification);
            DailyGroupSalesReport.Processor processor = new DailyGroupSalesReport.Processor(em, apEm, session.getCompanyID(), dailyGroupSalesReportSpecification.getParameters());
            processor.setParameters(sortString, filterString);
            logger.trace("Reports.getDailyGroupSalesReportJson: processor = {}", processor);
            DailyGroupSalesReportResult result = processor.result(first, max);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/daily_group_sales/{id}/json", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_group_sales/{id}/json", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/daily_group_sales/{id}/csv")
    @Produces("text/csv")
    public Response getDailyGroupSalesReportCsv(@HeaderParam(RestParams.SID) String sessionID, @PathParam("id") int specificationID,
                                                @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                @QueryParam(RestParams.SORT) String sortString, @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.DAILY_GROUP_SALES);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            DailyGroupSalesReportSpecification dailyGroupSalesReportSpecification = new DailyGroupSalesReportSpecification(reportSpecification);

            logger.trace("Reports.getDailyGroupSalesReportCsv: dailyGroupSalesReportSpecification = {}", dailyGroupSalesReportSpecification);
            DailyGroupSalesReport.Processor processor = new DailyGroupSalesReport.Processor(em, apEm, session.getCompanyID(), dailyGroupSalesReportSpecification.getParameters());
            processor.setParameters(sortString, filterString);
            logger.trace("Reports.getDailyGroupSalesReportCsv: processor = {}", processor);
            List<DailyGroupSalesReport.ResultEntry> entries = processor.entries(first, max);
            DailyGroupSalesReport.CsvExportProcessor csvExportProcessor = new DailyGroupSalesReport.CsvExportProcessor(first);
            String result = csvExportProcessor.add(entries);
            return Response.ok(result).header("recordCount", String.valueOf(entries.size())).build();
        } catch (RuleCheckException ex) {
            logger.error("/daily_group_sales/{id}/csv", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_group_sales/{id}/csv", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/daily_group_sales")
    @Produces(MediaType.APPLICATION_JSON)
    public DailyGroupSalesReportSpecification createDailyGroupSalesReportSpecification(@HeaderParam(RestParams.SID) String sessionID,
                                                                                       @QueryParam("name") String name,
                                                                                       @QueryParam("description") String description,
                                                                                       @QueryParam(RestParams.SORT) String sortString,
                                                                                       @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            DailyGroupSalesReportParameters parameters = new DailyGroupSalesReportParameters(filterString, sortString);
            DailyGroupSalesReportSpecification specification = new DailyGroupSalesReportSpecification();
            specification.setCompanyID(session.getCompanyID());
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);
            specification.setOriginator(Report.Originator.USER);

            logger.trace("specification = {}", specification);

            return this.createReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/daily_group_sales", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_group_sales", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/daily_group_sales/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DailyGroupSalesReportSpecification updateDailyGroupSalesReportSpecification(@HeaderParam(RestParams.SID) String sessionID,
                                                                                       @PathParam("id") int specificationID,
                                                                                       @PathParam("version") int version,
                                                                                       @QueryParam("name") String name,
                                                                                       @QueryParam("description") String description,
                                                                                       @QueryParam(RestParams.SORT) String sortString,
                                                                                       @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            DailyGroupSalesReportParameters parameters = new DailyGroupSalesReportParameters(filterString, sortString);
            DailyGroupSalesReportSpecification specification = new DailyGroupSalesReportSpecification();

            specification.setId(specificationID);
            specification.setCompanyID(session.getCompanyID());
            specification.setVersion(version);
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);

            logger.trace("specification = {}", specification);

            return this.updateReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/daily_group_sales/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_group_sales/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/daily_group_sales/{id}")
    public void deleteDailyGroupSalesReportSpecification(@PathParam("id") int specificationID, @HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            session.check(em, ReportSpecification.MAY_DELETE, "Not allowed to Delete Report %d", specificationID);
            ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.DAILY_GROUP_SALES);
            if (existingSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            if (Objects.equals(existingSpecification.getOriginator(), Report.Originator.MINIMUM_REQUIRED_DATA)) {
                throw new RuleCheckException(StatusCode.FORBIDDEN, null, "May not delete reports with originator MINIMUM_REQUIRED_DATA", existingSpecification.getOriginator());
            }
            AuditEntryContext auditContext = new AuditEntryContext("DAILY_GROUP_SALES_REPORT_DELETE", existingSpecification.getName(), existingSpecification.getId());
            existingSpecification.remove(em, session, auditContext);
        } catch (RuleCheckException ex) {
            logger.error("/daily_group_sales/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_group_sales/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    public DailyGroupSalesReportSpecification createReportSpecification(EntityManagerEx em, DailyGroupSalesReportSpecification specification,
                                                                        Session session, RestParams params) throws Exception {
        session.check(em, ReportSpecification.MAY_ADD, "Not allowed to Create Report");

        checkReportCountLimit(em, session);

        ReportSpecification newSpecification = new ReportSpecification();
        newSpecification.amend(specification);
        newSpecification.setParameters(DailyGroupSalesReportParameters.toJson(specification.getParameters()));
        newSpecification.setType(Report.Type.DAILY_GROUP_SALES.toString());
        logger.trace("Reports.createReportSpecification: newSpecification = {}", newSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("DAILY_GROUP_SALES_REPORT_CREATE", newSpecification.getId());
        newSpecification.persist(em, null, session, auditContext);
        specification.setId(newSpecification.getId());
        return specification;
    }

    public DailyGroupSalesReportSpecification updateReportSpecification(EntityManagerEx em, DailyGroupSalesReportSpecification specification,
                                                                        Session session, RestParams params) throws Exception {
        session.check(em, ReportSpecification.MAY_UPDATE, "Not allowed to Update Report %d", specification.getId());

        ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specification.getId(), session.getCompanyID(), Report.Type.DAILY_GROUP_SALES);
        if (existingSpecification == null || existingSpecification.getCompanyID() != session.getCompanyID()) {
            throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specification.getId());
        }
        specification.setSchedules(existingSpecification.getSchedules());
        ReportSpecification updatedSpecification = existingSpecification;
        existingSpecification = new ReportSpecification(existingSpecification);
        logger.trace("Reports.updateReportSpecification: existingSpecification = {}", existingSpecification);

        DailyGroupSalesReportSpecification updatedSpecificationTyped = new DailyGroupSalesReportSpecification(updatedSpecification);
        updatedSpecificationTyped.amend(specification);
        logger.trace("Reports.updateReportSpecification: updatedSpecificationTyped = {}", updatedSpecificationTyped);
        updatedSpecification.amend(updatedSpecificationTyped.toReportSpecification());
        if (updatedSpecification.getOriginator() == null) {
            updatedSpecification.setOriginator(Report.Originator.USER);
        }
        logger.trace("Reports.updateReportSpecification: updatedSpecification = {}", updatedSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("DAILY_GROUP_SALES_REPORT_SPECIFICATION_UPDATE", updatedSpecification.getName(), updatedSpecification.getId());
        updatedSpecification.persist(em, existingSpecification, session, auditContext);
        return updatedSpecificationTyped;
    }

    /////////


    // //////////////////////////////////////////////////////////////
    // Daily Performance Reports by Area 
    // //////////////////////////////////////////////////////////////

    @POST
    @Path("/daily_performance_by_area")
    @Produces(MediaType.APPLICATION_JSON)
    public DailyPerformanceByAreaSpecification createSalesPerformanceReportByArea(@HeaderParam(RestParams.SID) String sessionID,
                                                                                  @QueryParam("name") String name,
                                                                                  @QueryParam("description") String description,
                                                                                  @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            DailyPerformanceByAreaReportParameters parameters = new DailyPerformanceByAreaReportParameters(null,null, relativeTimeRangeCode);
            DailyPerformanceByAreaSpecification specification = new DailyPerformanceByAreaSpecification();
            specification.setCompanyID(session.getCompanyID());
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);
            specification.setOriginator(Report.Originator.USER);

            logger.trace("specification = {}", specification);

            return this.createReportByAreaSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/daily_performance_by_area", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_performance_by_area", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/daily_performance_by_area")
    @Produces(MediaType.APPLICATION_JSON)
    public DailyPerformanceByAreaListResult getSalesPerformanceReportsByArea(@HeaderParam(RestParams.SID) String sessionID,
                                                                     @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                     @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                     @QueryParam(RestParams.SORT) String sort,
                                                                     @QueryParam(RestParams.SEARCH) String search,
                                                                     @QueryParam(RestParams.FILTER) String filter,
                                                                     @QueryParam(RestParams.WITHCOUNT) Integer withcount) {
        try (EntityManagerEx em = context.getEntityManager()) {
            boolean performCount = ((withcount == null) || (withcount != null && withcount == 1));
            RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
            Session session = context.getSession(params.getSessionID());
            DailyPerformanceByAreaListResult result = new DailyPerformanceByAreaListResult();
            List<ReportSpecification> reportSpecifications;
        
            try (QueryToken token = context.getQueryToken()) {
                reportSpecifications = ReportSpecification.findType(em, params, session.getCompanyID(), Report.Type.DAILY_PERFORMANCE_BY_AREA);
            }
            if (performCount) {
                // result.setFound(ReportSpecification.findTypeCount(em, params, session.getCompanyID(), Report.Type.DAILY_GROUP_SALES));
                result.setFound(Long.valueOf(QueryBuilder.getFoundRows(em)));
            }

            List<DailyPerformanceByAreaSpecification> entries = new ArrayList<DailyPerformanceByAreaSpecification>();
            for (ReportSpecification reportSpecification : reportSpecifications) {
                entries.add(new DailyPerformanceByAreaSpecification(reportSpecification));
            }
            result.setEntries(entries);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/daily_performance_by_area", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_performance_by_area", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    public DailyPerformanceByAreaSpecification createReportByAreaSpecification(EntityManagerEx em, DailyPerformanceByAreaSpecification specification,
                                                                        Session session, RestParams params) throws Exception {
        session.check(em, ReportSpecification.MAY_ADD, "Not allowed to Create Report");

        checkReportCountLimit(em, session);

        ReportSpecification newSpecification = new ReportSpecification();
        newSpecification.amend(specification);
        newSpecification.setParameters(DailyPerformanceByAreaReportParameters.toJson(specification.getParameters()));
        newSpecification.setType(Report.Type.DAILY_PERFORMANCE_BY_AREA.toString());
        logger.trace("Reports.createReportSpecification: newSpecification = {}", newSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("DAILY_PERFORMANCE_BY_AREA_REPORT_CREATE", newSpecification.getId());
        newSpecification.persist(em, null, session, auditContext);
        specification.setId(newSpecification.getId());
        return specification;
    }
    @GET
    @Path("/daily_performance_by_area/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DailyPerformanceByAreaSpecification getPerformanceByAreaReport(@HeaderParam(RestParams.SID) String sessionID,
                                                                        @PathParam("id") int specificationID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.DAILY_PERFORMANCE_BY_AREA);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            DailyPerformanceByAreaSpecification dailyPerformanceByAreaSpecification = new DailyPerformanceByAreaSpecification(reportSpecification);
            return dailyPerformanceByAreaSpecification;
        } catch (RuleCheckException ex) {
            logger.error("/daily_performance_by_area/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_performance_by_area/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/daily_performance_by_area/{id}")
    public void deleteDailySalesByAreaReportSpecification(@PathParam("id") int specificationID, @HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            session.check(em, ReportSpecification.MAY_DELETE, "Not allowed to Delete Report %d", specificationID);
            ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.DAILY_PERFORMANCE_BY_AREA);
            if (existingSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            if (Objects.equals(existingSpecification.getOriginator(), Report.Originator.MINIMUM_REQUIRED_DATA)) {
                throw new RuleCheckException(StatusCode.FORBIDDEN, null, "May not delete reports with originator MINIMUM_REQUIRED_DATA", existingSpecification.getOriginator());
            }
            AuditEntryContext auditContext = new AuditEntryContext("DAILY_SALES_REPORT_BY_AREA_DELETE", existingSpecification.getName(), existingSpecification.getId());
            existingSpecification.remove(em, session, auditContext);
        } catch (RuleCheckException ex) {
            logger.error("/daily_performance_by_area/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_performance_by_area/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }
    public DailyPerformanceByAreaSpecification updateReportSpecification(EntityManagerEx em, DailyPerformanceByAreaSpecification specification,
                                                                        Session session, RestParams params) throws Exception {
        session.check(em, ReportSpecification.MAY_UPDATE, "Not allowed to Update Report %d", specification.getId());

        ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specification.getId(), session.getCompanyID(), Report.Type.DAILY_PERFORMANCE_BY_AREA);
        if (existingSpecification == null || existingSpecification.getCompanyID() != session.getCompanyID()) {
            throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specification.getId());
        }
        specification.setSchedules(existingSpecification.getSchedules());
        ReportSpecification updatedSpecification = existingSpecification;
        existingSpecification = new ReportSpecification(existingSpecification);
        logger.trace("Reports.updateReportSpecification: existingSpecification = {}", existingSpecification);

        DailyPerformanceByAreaSpecification updatedSpecificationTyped = new DailyPerformanceByAreaSpecification(updatedSpecification);
        updatedSpecificationTyped.amend(specification);
        logger.trace("Reports.updateReportSpecification: updatedSpecificationTyped = {}", updatedSpecificationTyped);
        updatedSpecification.amend(updatedSpecificationTyped.toReportSpecification());
        if (updatedSpecification.getOriginator() == null) {
            updatedSpecification.setOriginator(Report.Originator.USER);
        }
        logger.trace("Reports.updateReportSpecification: updatedSpecification = {}", updatedSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("DAILY_PERFORMANCE_BY_AREA_REPORT_UPDATE", updatedSpecification.getName(), updatedSpecification.getId());
        updatedSpecification.persist(em, existingSpecification, session, auditContext);
        return updatedSpecificationTyped;
    }

    @PUT
    @Path("/daily_performance_by_area/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public DailyPerformanceByAreaSpecification updateSalesReportSpecificationByArea(@HeaderParam(RestParams.SID) String sessionID,
                                                                                    @PathParam("id") int specificationID,
                                                                                    @PathParam("version") int version,
                                                                                    @QueryParam("name") String name,
                                                                                    @QueryParam("description") String description,
                                                                                    @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode){
                                                                              //      @QueryParam(RestParams.SORT) String sortString,
                                                                               //     @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            DailyPerformanceByAreaReportParameters parameters = new DailyPerformanceByAreaReportParameters(null, null , relativeTimeRangeCode);
            DailyPerformanceByAreaSpecification specification = new DailyPerformanceByAreaSpecification();

            specification.setId(specificationID);
            specification.setCompanyID(session.getCompanyID());
            specification.setVersion(version);
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);

            logger.trace("specification = {}", specification);

            return this.updateReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/daily_performance_by_area/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/daily_performance_by_area/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }
    /////////


    // //////////////////////////////////////////////////////////////
    // Monthly Group Sales :: AdHoc
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/monthly_sales_performance/adhoc/json")
    @Produces(MediaType.APPLICATION_JSON)
    public MonthlySalesPerformanceReportResult getMonthlySalesPerformanceReportAdhocJson(
            @HeaderParam(RestParams.SID) String sessionID,
            @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
            @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
            @QueryParam(RestParams.SORT) String sortString,
            @QueryParam(RestParams.FILTER) String filterString,
            @QueryParam("timeInterval.start") String timeIntervalStart,
            @QueryParam("timeInterval.end") String timeIntervalEnd,
            @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
            @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            logger.trace("Reports.getMonthlySalesPerformanceReportAdhocJson: first = {}, max = {}, sortString = {}, filterString = {}", first, max, sortString, filterString);
            MonthlySalesPerformanceReport.Processor processor = new MonthlySalesPerformanceReport.Processor(apEm, sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
            logger.trace("Reports.getMonthlySalesPerformanceReportAdhocJson: processor = {}", processor);
            MonthlySalesPerformanceReportResult result = processor.result(first, max);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/monthly_sales_performance/adhoc/json", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/monthly_sales_performance/adhoc/json", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/monthly_sales_performance/adhoc/csv")
    @Produces("text/csv")
    public Response getMonthlySalesPerformanceReportAdhocCsv(
            @HeaderParam(RestParams.SID) String sessionID,
            @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
            @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
            @QueryParam(RestParams.SORT) String sortString,
            @QueryParam(RestParams.FILTER) String filterString,
            @QueryParam("timeInterval.start") String timeIntervalStart,
            @QueryParam("timeInterval.end") String timeIntervalEnd,
            @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
            @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            logger.trace("Reports.getMonthlySalesPerformanceReportAdhocCsv: first = {}, max = {}, sortString = {}, filterString = {}", first, max, sortString, filterString);
            MonthlySalesPerformanceReport.Processor processor = new MonthlySalesPerformanceReport.Processor(apEm, sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeReferenceString);
            logger.trace("Reports.getMonthlySalesPerformanceReportAdhocCsv: processor = {}", processor);
            List<MonthlySalesPerformanceReport.ResultEntry> entries = processor.entries(first, max);
            MonthlySalesPerformanceReport.CsvExportProcessor csvExportProcessor = new MonthlySalesPerformanceReport.CsvExportProcessor(first);
            String result = csvExportProcessor.add(entries);
            return Response.ok(result).header("recordCount", String.valueOf(entries.size())).build();
        } catch (RuleCheckException ex) {
            logger.error("/monthly_sales_performance/adhoc/csv", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/monthly_sales_performance/adhoc/csv", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    // //////////////////////////////////////////////////////////////
    // Monthly Group Sales :: Saved
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/monthly_sales_performance")
    @Produces(MediaType.APPLICATION_JSON)
    public MonthlySalesPerformanceReportListResult getMonthlySalesPerformanceReports(@HeaderParam(RestParams.SID) String sessionID,
                                                                                     @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                                     @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                                     @QueryParam(RestParams.SORT) String sort,
                                                                                     @QueryParam(RestParams.SEARCH) String search,
                                                                                     @QueryParam(RestParams.FILTER) String filter,
                                                                                     @QueryParam(RestParams.WITHCOUNT) Integer withcount) {
        try (EntityManagerEx em = context.getEntityManager()) {
            boolean performCount = ((withcount == null) || (withcount != null && withcount == 1));
            RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
            Session session = context.getSession(params.getSessionID());
            MonthlySalesPerformanceReportListResult result = new MonthlySalesPerformanceReportListResult();
            List<ReportSpecification> reportSpecifications;
            // TODO Remove when slow queries have been properly fixed!
            try (QueryToken token = context.getQueryToken()) {
                reportSpecifications = ReportSpecification.findType(em, params, session.getCompanyID(), Report.Type.MONTHLY_SALES_PERFORMANCE);
            }
            if (performCount) {
                // result.setFound(ReportSpecification.findTypeCount(em, params, session.getCompanyID(), Report.Type.DAILY_GROUP_SALES));
                result.setFound(Long.valueOf(QueryBuilder.getFoundRows(em)));
            }

            List<MonthlySalesPerformanceReportSpecification> entries = new ArrayList<MonthlySalesPerformanceReportSpecification>();
            for (ReportSpecification reportSpecification : reportSpecifications) {
                entries.add(new MonthlySalesPerformanceReportSpecification(reportSpecification));
            }
            result.setEntries(entries);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/monthly_sales_performance", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/monthly_sales_performance", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/monthly_sales_performance/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public MonthlySalesPerformanceReportSpecification getMonthlySalesPerformanceReports(@HeaderParam(RestParams.SID) String sessionID,
                                                                                        @PathParam("id") int specificationID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.MONTHLY_SALES_PERFORMANCE);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            MonthlySalesPerformanceReportSpecification monthlySalesPerformanceReportSpecification = new MonthlySalesPerformanceReportSpecification(reportSpecification);
            return monthlySalesPerformanceReportSpecification;
        } catch (RuleCheckException ex) {
            logger.error("/monthly_sales_performance/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/monthly_sales_performance/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/monthly_sales_performance/{id}/json")
    @Produces(MediaType.APPLICATION_JSON)
    public MonthlySalesPerformanceReportResult getMonthlySalesPerformanceReportJson(
            @HeaderParam(RestParams.SID) String sessionID,
            @PathParam("id") int specificationID,
            @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
            @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
            @QueryParam(RestParams.SORT) String sortString,
            @QueryParam(RestParams.FILTER) String filterString,
            @QueryParam("timeInterval.start") String timeIntervalStart,
            @QueryParam("timeInterval.end") String timeIntervalEnd,
            @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
            @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.MONTHLY_SALES_PERFORMANCE);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            MonthlySalesPerformanceReportSpecification monthlySalesPerformanceReportSpecification = new MonthlySalesPerformanceReportSpecification(reportSpecification);

            logger.trace("Reports.getMonthlySalesPerformanceReportJson: monthlySalesPerformanceReportSpecification = {}", monthlySalesPerformanceReportSpecification);
            MonthlySalesPerformanceReport.Processor processor = new MonthlySalesPerformanceReport.Processor(apEm, monthlySalesPerformanceReportSpecification.getParameters(), relativeTimeRangeReferenceString);
            processor.setParameters(sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeCode);
            logger.trace("Reports.getMonthlySalesPerformanceReportJson: processor = {}", processor);
            MonthlySalesPerformanceReportResult result = processor.result(first, max);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/monthly_sales_performance/{id}/json", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/monthly_sales_performance/{id}/json", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/monthly_sales_performance/{id}/csv")
    @Produces("text/csv")
    public Response getMonthlySalesPerformanceReportCsv(
            @HeaderParam(RestParams.SID) String sessionID,
            @PathParam("id") int specificationID,
            @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
            @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
            @QueryParam(RestParams.SORT) String sortString,
            @QueryParam(RestParams.FILTER) String filterString,
            @QueryParam("timeInterval.start") String timeIntervalStart,
            @QueryParam("timeInterval.end") String timeIntervalEnd,
            @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode,
            @QueryParam("relativeTimeRange.reference") String relativeTimeRangeReferenceString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.MONTHLY_SALES_PERFORMANCE);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            MonthlySalesPerformanceReportSpecification monthlySalesPerformanceReportSpecification = new MonthlySalesPerformanceReportSpecification(reportSpecification);

            logger.trace("Reports.getMonthlySalesPerformanceReportCsv: monthlySalesPerformanceReportSpecification = {}", monthlySalesPerformanceReportSpecification);
            MonthlySalesPerformanceReport.Processor processor = new MonthlySalesPerformanceReport.Processor(em, monthlySalesPerformanceReportSpecification.getParameters(), relativeTimeRangeReferenceString);
            processor.setParameters(sortString, filterString, timeIntervalStart, timeIntervalEnd, relativeTimeRangeCode, relativeTimeRangeCode);
            logger.trace("Reports.getMonthlySalesPerformanceReportCsv: processor = {}", processor);
            List<MonthlySalesPerformanceReport.ResultEntry> entries = processor.entries(first, max);
            MonthlySalesPerformanceReport.CsvExportProcessor csvExportProcessor = new MonthlySalesPerformanceReport.CsvExportProcessor(first);
            String result = csvExportProcessor.add(entries);
            return Response.ok(result).header("recordCount", String.valueOf(entries.size())).build();
        } catch (RuleCheckException ex) {
            logger.error("/monthly_sales_performance/{id}/csv", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/monthly_sales_performance/{id}/csv", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/monthly_sales_performance")
    @Produces(MediaType.APPLICATION_JSON)
    public MonthlySalesPerformanceReportSpecification createMonthlySalesPerformanceReportSpecification(
            @HeaderParam(RestParams.SID) String sessionID,
            @QueryParam("name") String name,
            @QueryParam("description") String description,
            @QueryParam(RestParams.SORT) String sortString,
            @QueryParam(RestParams.FILTER) String filterString,
            @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            MonthlySalesPerformanceReportParameters parameters = new MonthlySalesPerformanceReportParameters(filterString, sortString, relativeTimeRangeCode);
            MonthlySalesPerformanceReportSpecification specification = new MonthlySalesPerformanceReportSpecification();
            specification.setCompanyID(session.getCompanyID());
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);
            specification.setOriginator(Report.Originator.USER);

            logger.trace("specification = {}", specification);

            return this.createReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/monthly_sales_performance", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/monthly_sales_performance", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/monthly_sales_performance/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public MonthlySalesPerformanceReportSpecification updateMonthlySalesPerformanceReportSpecification(
            @HeaderParam(RestParams.SID) String sessionID,
            @PathParam("id") int specificationID,
            @PathParam("version") int version,
            @QueryParam("name") String name,
            @QueryParam("description") String description,
            @QueryParam(RestParams.SORT) String sortString,
            @QueryParam(RestParams.FILTER) String filterString,
            @QueryParam("relativeTimeRange.code") String relativeTimeRangeCode) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            MonthlySalesPerformanceReportParameters parameters = new MonthlySalesPerformanceReportParameters(filterString, sortString, relativeTimeRangeCode);
            MonthlySalesPerformanceReportSpecification specification = new MonthlySalesPerformanceReportSpecification();

            specification.setId(specificationID);
            specification.setCompanyID(session.getCompanyID());
            specification.setVersion(version);
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);

            logger.trace("specification = {}", specification);

            return this.updateReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/monthly_sales_performance/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/monthly_sales_performance/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/monthly_sales_performance/{id}")
    public void deleteMonthlySalesPerformanceReportSpecification(@PathParam("id") int specificationID,
                                                                 @HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            session.check(em, ReportSpecification.MAY_DELETE, "Not allowed to Delete Report %d", specificationID);
            ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.MONTHLY_SALES_PERFORMANCE);
            if (existingSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            if (Objects.equals(existingSpecification.getOriginator(), Report.Originator.MINIMUM_REQUIRED_DATA)) {
                throw new RuleCheckException(StatusCode.FORBIDDEN, null, "May not delete reports with originator MINIMUM_REQUIRED_DATA", existingSpecification.getOriginator());
            }
            AuditEntryContext auditContext = new AuditEntryContext("MONTHLY_SALES_PERFORMANCE_REPORT_DELETE", existingSpecification.getName(), existingSpecification.getId());
            existingSpecification.remove(em, session, auditContext);
        } catch (RuleCheckException ex) {
            logger.error("/monthly_sales_performance/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/monthly_sales_performance/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    public MonthlySalesPerformanceReportSpecification createReportSpecification(EntityManagerEx em,
                                                                                MonthlySalesPerformanceReportSpecification specification,
                                                                                Session session, RestParams params) throws Exception {
        session.check(em, ReportSpecification.MAY_ADD, "Not allowed to Create Report");

        checkReportCountLimit(em, session);

        ReportSpecification newSpecification = new ReportSpecification();
        newSpecification.amend(specification);
        newSpecification.setParameters(MonthlySalesPerformanceReportParameters.toJson(specification.getParameters()));
        newSpecification.setType(Report.Type.MONTHLY_SALES_PERFORMANCE.toString());
        logger.trace("Reports.createReportSpecification: newSpecification = {}", newSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("MONTHLY_SALES_PERFORMANCE_REPORT_CREATE", newSpecification.getId());
        newSpecification.persist(em, null, session, auditContext);
        specification.setId(newSpecification.getId());
        return specification;
    }

    public MonthlySalesPerformanceReportSpecification updateReportSpecification(EntityManagerEx em,
                                                                                MonthlySalesPerformanceReportSpecification specification,
                                                                                Session session, RestParams params) throws Exception {
        session.check(em, ReportSpecification.MAY_UPDATE, "Not allowed to Update Report %d", specification.getId());

        ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specification.getId(), session.getCompanyID(), Report.Type.MONTHLY_SALES_PERFORMANCE);
        if (existingSpecification == null || existingSpecification.getCompanyID() != session.getCompanyID()) {
            throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specification.getId());
        }
        specification.setSchedules(existingSpecification.getSchedules());
        ReportSpecification updatedSpecification = existingSpecification;
        existingSpecification = new ReportSpecification(existingSpecification);
        logger.trace("Reports.updateReportSpecification: existingSpecification = {}", existingSpecification);

        MonthlySalesPerformanceReportSpecification updatedSpecificationTyped = new MonthlySalesPerformanceReportSpecification(updatedSpecification);
        updatedSpecificationTyped.amend(specification);
        logger.trace("Reports.updateReportSpecification: updatedSpecificationTyped = {}", updatedSpecificationTyped);
        updatedSpecification.amend(updatedSpecificationTyped.toReportSpecification());
        if (updatedSpecification.getOriginator() == null) {
            updatedSpecification.setOriginator(Report.Originator.USER);
        }
        logger.trace("Reports.updateReportSpecification: updatedSpecification = {}", updatedSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("MONTHLY_SALES_PERFORMANCE_REPORT_SPECIFICATION_UPDATE", updatedSpecification.getName(), updatedSpecification.getId());
        updatedSpecification.persist(em, existingSpecification, session, auditContext);
        return updatedSpecificationTyped;
    }

    // //////////////////////////////////////////////////////////////
    // Account Balance Summary :: AdHoc
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/account_balance_summary/adhoc/json")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountBalanceSummaryReportResult getAccountBalanceSummaryReportAdhocJson(@HeaderParam(RestParams.SID) String sessionID,
                                                                                     @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                                     @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                                     @QueryParam(RestParams.SORT) String sortString,
                                                                                     @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            logger.trace("Reports.getAccountBalanceSummaryReportAdhocJson: first = {}, max = {}, sortString = {}, filterString = {}", first, max, sortString, filterString);
            Session session = context.getSession(sessionID);
            AccountBalanceSummaryReport.Processor processor = new AccountBalanceSummaryReport.Processor(em, apEm, session.getCompanyID(), session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, sortString, filterString);
            logger.trace("Reports.getAccountBalanceSummaryReportAdhocJson: processor = {}s", processor);
            AccountBalanceSummaryReportResult result = processor.result(first, max);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/account_balance_summary/adhoc/json", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/account_balance_summary/adhoc/json", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/account_balance_summary/adhoc/csv")
    @Produces("text/csv")
    public Response getAccountBalanceSummaryReportAdhocCsv(@HeaderParam(RestParams.SID) String sessionID,
                                                           @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                           @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                           @QueryParam(RestParams.SORT) String sortString,
                                                           @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            logger.trace("Reports.getAccountBalanceSummaryReportAdhocCsv: first = {}, max = {}, sortString = {}, filterString = {}", first, max, sortString, filterString);
            Session session = context.getSession(sessionID);
            AccountBalanceSummaryReport.Processor processor = new AccountBalanceSummaryReport.Processor(em, apEm, session.getCompanyID(), session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, sortString, filterString);
            logger.trace("Reports.getAccountBalanceSummaryReportAdhocCsv: processor = {}", processor);
            List<AccountBalanceSummaryReport.ResultEntry> entries = processor.entries(first, max);
            AccountBalanceSummaryReport.CsvExportProcessor csvExportProcessor = new AccountBalanceSummaryReport.CsvExportProcessor(first);
            String result = csvExportProcessor.add(entries);
            return Response.ok(result).header("recordCount", String.valueOf(entries.size())).build();
        } catch (RuleCheckException ex) {
            logger.error("/account_balance_summary/adhoc/csv", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/account_balance_summary/adhoc/csv", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }


    // //////////////////////////////////////////////////////////////
    // Account Balance Summary :: Saved
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/account_balance_summary")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountBalanceSummaryReportListResult getAccountBalanceSummaryReports(@HeaderParam(RestParams.SID) String sessionID,
                                                                                 @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                                 @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                                 @QueryParam(RestParams.SORT) String sort,
                                                                                 @QueryParam(RestParams.SEARCH) String search,
                                                                                 @QueryParam(RestParams.FILTER) String filter,
                                                                                 @QueryParam(RestParams.WITHCOUNT) Integer withcount) {
        try (EntityManagerEx em = context.getEntityManager()) {
            boolean performCount = ((withcount == null) || (withcount != null && withcount == 1));
            RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
            Session session = context.getSession(params.getSessionID());

            {
                String agentPredicate = String.format("+%s='%d'", "agentID", session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : 0);
                String currentFilter = params.getFilter();
                if (currentFilter != null && !currentFilter.isEmpty()) {
                    currentFilter = agentPredicate + "+" + currentFilter;
                } else {
                    currentFilter = agentPredicate;
                }
                params.setFilter(currentFilter);
            }

            AccountBalanceSummaryReportListResult result = new AccountBalanceSummaryReportListResult();
            List<ReportSpecification> reportSpecifications;
            // TODO Remove when slow queries have been properly fixed!
            try (QueryToken token = context.getQueryToken()) {
                reportSpecifications = ReportSpecification.findType(em, params, session.getCompanyID(), Report.Type.ACCOUNT_BALANCE_SUMMARY);
            }
            if (performCount) {
                // result.setFound(ReportSpecification.findTypeCount(em, params, session.getCompanyID(), Report.Type.ACCOUNT_BALANCE_SUMMARY));
                result.setFound(Long.valueOf(QueryBuilder.getFoundRows(em)));
            }

            List<AccountBalanceSummaryReportSpecification> entries = new ArrayList<AccountBalanceSummaryReportSpecification>();
            for (ReportSpecification reportSpecification : reportSpecifications) {
                entries.add(new AccountBalanceSummaryReportSpecification(reportSpecification));
            }
            result.setEntries(entries);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/account_balance_summary", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/account_balance_summary", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/account_balance_summary/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountBalanceSummaryReportSpecification getAccountBalanceSummaryReports(@HeaderParam(RestParams.SID) String sessionID,
                                                                                    @PathParam("id") int specificationID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.ACCOUNT_BALANCE_SUMMARY);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            AccountBalanceSummaryReportSpecification accountBalanceSummaryReportSpecification = new AccountBalanceSummaryReportSpecification(reportSpecification);
            return accountBalanceSummaryReportSpecification;
        } catch (RuleCheckException ex) {
            logger.error("/account_balance_summary/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/account_balance_summary/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/account_balance_summary/{id}/json")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountBalanceSummaryReportResult getAccountBalanceSummaryReportJson(@HeaderParam(RestParams.SID) String sessionID,
                                                                                @PathParam("id") int specificationID,
                                                                                @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                                @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                                @QueryParam(RestParams.SORT) String sortString,
                                                                                @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.ACCOUNT_BALANCE_SUMMARY);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            AccountBalanceSummaryReportSpecification accountBalanceSummaryReportSpecification = new AccountBalanceSummaryReportSpecification(reportSpecification);

            logger.trace("Reports.getAccountBalanceSummaryReportJson: accountBalanceSummaryReportSpecification = {}", accountBalanceSummaryReportSpecification);
            AccountBalanceSummaryReport.Processor processor = new AccountBalanceSummaryReport.Processor(em, apEm, session.getCompanyID(), session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, accountBalanceSummaryReportSpecification.getParameters());
            processor.setParameters(sortString, filterString);
            logger.trace("Reports.getAccountBalanceSummaryReportJson: processor = {}", processor);
            AccountBalanceSummaryReportResult result = processor.result(first, max);
            return result;
        } catch (RuleCheckException ex) {
            logger.error("/account_balance_summary/{id}/jso", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/account_balance_summary/{id}/jso", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/account_balance_summary/{id}/csv")
    @Produces("text/csv")
    public Response getAccountBalanceSummaryReportCsv(@HeaderParam(RestParams.SID) String sessionID, @PathParam("id") int specificationID,
                                                      @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                      @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                      @QueryParam(RestParams.SORT) String sortString,
                                                      @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx apEm = context.getApEntityManager(); EntityManagerEx em = context.getEntityManager()) {
            DbUtils.makeReadUncommitted(apEm);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.ACCOUNT_BALANCE_SUMMARY);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            AccountBalanceSummaryReportSpecification accountBalanceSummaryReportSpecification = new AccountBalanceSummaryReportSpecification(reportSpecification);

            logger.trace("Reports.getAccountBalanceSummaryReportCsv: accountBalanceSummaryReportSpecification = {}", accountBalanceSummaryReportSpecification);
            AccountBalanceSummaryReport.Processor processor = new AccountBalanceSummaryReport.Processor(em, apEm, session.getCompanyID(), session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : null, accountBalanceSummaryReportSpecification.getParameters());
            processor.setParameters(sortString, filterString);
            logger.trace("Reports.getAccountBalanceSummaryReportCsv: processor = {}", processor);
            List<AccountBalanceSummaryReport.ResultEntry> entries = processor.entries(first, max);
            AccountBalanceSummaryReport.CsvExportProcessor csvExportProcessor = new AccountBalanceSummaryReport.CsvExportProcessor(first);
            String result = csvExportProcessor.add(entries);
            return Response.ok(result).header("recordCount", String.valueOf(entries.size())).build();
        } catch (RuleCheckException ex) {
            logger.error("/account_balance_summary/{id}/csv", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/account_balance_summary/{id}/csv", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/account_balance_summary")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountBalanceSummaryReportSpecification createReportSpecification(@HeaderParam(RestParams.SID) String sessionID,
                                                                              @QueryParam("name") String name,
                                                                              @QueryParam("description") String description,
                                                                              @QueryParam(RestParams.SORT) String sortString,
                                                                              @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            AccountBalanceSummaryReportParameters parameters = new AccountBalanceSummaryReportParameters(filterString, sortString);
            AccountBalanceSummaryReportSpecification specification = new AccountBalanceSummaryReportSpecification();
            specification.setCompanyID(session.getCompanyID());
            specification.setAgentID(session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : 0);
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);
            specification.setOriginator(Report.Originator.USER);

            logger.trace("specification = {}", specification);

            return this.createReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/account_balance_summary", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/account_balance_summary", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/account_balance_summary/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountBalanceSummaryReportSpecification updateReportSpecification(@HeaderParam(RestParams.SID) String sessionID,
                                                                              @PathParam("id") int specificationID, @PathParam("version") int version,
                                                                              @QueryParam("name") String name,
                                                                              @QueryParam("description") String description,
                                                                              @QueryParam(RestParams.SORT) String sortString,
                                                                              @QueryParam(RestParams.FILTER) String filterString) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            AccountBalanceSummaryReportParameters parameters = new AccountBalanceSummaryReportParameters(filterString, sortString);
            AccountBalanceSummaryReportSpecification specification = new AccountBalanceSummaryReportSpecification();

            specification.setId(specificationID);
            specification.setCompanyID(session.getCompanyID());
            specification.setAgentID(session.getUserType() == Session.UserType.AGENT ? session.getAgentID() : 0);
            specification.setVersion(version);
            specification.setName(name);
            specification.setDescription(description);
            specification.setParameters(parameters);

            logger.trace("specification = {}", specification);

            return this.updateReportSpecification(em, specification, session, params);
        } catch (RuleCheckException ex) {
            logger.error("/account_balance_summary/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/account_balance_summary/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/account_balance_summary/{id}")
    public void deleteAccountBalanceSummaryReportSpecification(@PathParam("id") int specificationID, @HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            session.check(em, ReportSpecification.MAY_DELETE, "Not allowed to Delete Report %d", specificationID);
            // FIXME filter by agentID as well, if applicable
            ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), Report.Type.ACCOUNT_BALANCE_SUMMARY);
            if (existingSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            if (Objects.equals(existingSpecification.getOriginator(), Report.Originator.MINIMUM_REQUIRED_DATA)) {
                throw new RuleCheckException(StatusCode.FORBIDDEN, null, "May not delete reports with originator MINIMUM_REQUIRED_DATA", existingSpecification.getOriginator());
            }
            AuditEntryContext auditContext = new AuditEntryContext("ACCOUNT_BALANCE_SUMMARY_REPORT_REMOVE", existingSpecification.getName(), existingSpecification.getId());
            existingSpecification.remove(em, session, auditContext);
        } catch (RuleCheckException ex) {
            logger.error("/account_balance_summary/{id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/account_balance_summary/{id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    public AccountBalanceSummaryReportSpecification createReportSpecification(EntityManagerEx em,
                                                                              AccountBalanceSummaryReportSpecification specification, Session session,
                                                                              RestParams params) throws Exception {
        session.check(em, ReportSpecification.MAY_ADD, "Not allowed to Create Report");

        checkReportCountLimit(em, session);

        ReportSpecification newSpecification = new ReportSpecification();
        newSpecification.amend(specification);
        newSpecification.setParameters(AccountBalanceSummaryReportParameters.toJson(specification.getParameters()));
        newSpecification.setType(Report.Type.ACCOUNT_BALANCE_SUMMARY.toString());
        logger.trace("Reports.createReportSpecification: newSpecification = {}", newSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("ACCOUNT_BALANCE_SUMMARY_REPORT_CREATE", newSpecification.getName());
        newSpecification.persist(em, null, session, auditContext);
        specification.setId(newSpecification.getId());
        return specification;
    }

    public AccountBalanceSummaryReportSpecification updateReportSpecification(EntityManagerEx em,
                                                                              AccountBalanceSummaryReportSpecification specification, Session session,
                                                                              RestParams params) throws Exception {
        session.check(em, ReportSpecification.MAY_UPDATE, "Not allowed to Update Report %d", specification.getId());

        ReportSpecification existingSpecification = ReportSpecification.findByIDAndType(em, specification.getId(), session.getCompanyID(), Report.Type.ACCOUNT_BALANCE_SUMMARY);
        if (existingSpecification == null || existingSpecification.getCompanyID() != session.getCompanyID()) {
            throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specification.getId());
        }
        specification.setSchedules(existingSpecification.getSchedules());
        ReportSpecification updatedSpecification = existingSpecification;
        existingSpecification = new ReportSpecification(existingSpecification);
        logger.trace("Reports.updateReportSpecification: existingSpecification = {}", existingSpecification);

        AccountBalanceSummaryReportSpecification updatedSpecificationTyped = new AccountBalanceSummaryReportSpecification(updatedSpecification);
        updatedSpecificationTyped.amend(specification);
        logger.trace("Reports.updateReportSpecification: updatedSpecificationTyped = {}", updatedSpecificationTyped);
        updatedSpecification.amend(updatedSpecificationTyped.toReportSpecification());
        if (updatedSpecification.getOriginator() == null) {
            updatedSpecification.setOriginator(Report.Originator.USER);
        }
        logger.trace("Reports.updateReportSpecification: updatedSpecification = {}", updatedSpecification);
        AuditEntryContext auditContext = new AuditEntryContext("ACCOUNT_BALANCE_SUMMARY_REPORT_UPDATE", updatedSpecification.getName(), updatedSpecification.getId());
        updatedSpecification.persist(em, existingSpecification, session, auditContext);
        return updatedSpecificationTyped;
    }

    // //////////////////////////////////////////////////////////////
    // Report Schedules ::
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/{report_type}/{specification_id}")
    public ExResultList<hxc.ecds.protocol.rest.reports.ReportSchedule> listReportSchedules(@PathParam("report_type") String reportTypeString,
                                                                                           @PathParam("specification_id") int specificationID,
                                                                                           @HeaderParam(RestParams.SID) String sessionID,
                                                                                           @DefaultValue("0") @QueryParam(RestParams.FIRST) int first,
                                                                                           @DefaultValue("-1") @QueryParam(RestParams.MAX) int max,
                                                                                           @QueryParam(RestParams.SORT) String sort,
                                                                                           @QueryParam(RestParams.SEARCH) String search,
                                                                                           @QueryParam(RestParams.FILTER) String filter,
                                                                                           @QueryParam(RestParams.WITHCOUNT) Integer withcount) {
        try (EntityManagerEx em = context.getEntityManager()) {
            boolean performCount = ((withcount == null) || (withcount != null && withcount == 1));
            Report.Type reportType = Report.Type.fromPathSegment(reportTypeString);
            RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), reportType);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            List<ReportSchedule> reportSchedules = ReportSchedule.findByReportSpecificationID(em, params, session.getCompanyID(), specificationID);
            Long foundRows = null;
            if (performCount) {
                // foundRows = ReportSchedule.findByReportSpecificationIDCount(em, params, session.getCompanyID(), specificationID);
                foundRows = Long.valueOf(QueryBuilder.getFoundRows(em));
            }

            return new ExResultList<hxc.ecds.protocol.rest.reports.ReportSchedule>(foundRows, reportSchedules);
        } catch (RuleCheckException ex) {
            logger.error("/{report_type}/{specification_id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/{report_type}/{specification_id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @GET
    @Path("/{report_type}/{specification_id}/schedule/{schedule_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public hxc.ecds.protocol.rest.reports.ReportSchedule getReportSchedule(@HeaderParam(RestParams.SID) String sessionID,
                                                                           @PathParam("report_type") String reportTypeString,
                                                                           @PathParam("specification_id") int specificationID,
                                                                           @PathParam("schedule_id") int scheduleID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            Report.Type reportType = Report.Type.fromPathSegment(reportTypeString);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), reportType);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            ReportSchedule reportSchedule = ReportSchedule.findByIDAndReportSpecificationID(em, session.getCompanyID(), scheduleID, specificationID);
            if (reportSchedule == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report schedule %d for report %d not found", scheduleID, specificationID);
            }
            return reportSchedule;
        } catch (RuleCheckException ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{report_type}/{specification_id}/schedule")
    public hxc.ecds.protocol.rest.reports.ReportSchedule createReportSchedule(@HeaderParam(RestParams.SID) String sessionID,
                                                                              @PathParam("report_type") String reportTypeString,
                                                                              @PathParam("specification_id") int specificationID,
                                                                              hxc.ecds.protocol.rest.reports.ReportSchedule reportSchedule) {
        try (EntityManagerEx em = context.getEntityManager()) {
            Report.Type reportType = Report.Type.fromPathSegment(reportTypeString);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), reportType);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            if (reportSchedule.getOriginator() == null) {
                reportSchedule.setOriginator(Report.Originator.USER);
            }
            if (!Objects.equals(reportSchedule.getOriginator(), Report.Originator.USER)) {
                throw new RuleCheckException(StatusCode.FORBIDDEN, null, "May not create report schedules with originator != USER (actual %s)", reportSchedule.getOriginator());
            }
            return this.createReportSchedule(em, reportSchedule, session, params, reportSpecification);
        } catch (RuleCheckException ex) {
            logger.error("/{report_type}/{specification_id}/schedule", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/{report_type}/{specification_id}/schedule", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{report_type}/{specification_id}/schedule/{schedule_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public hxc.ecds.protocol.rest.reports.ReportSchedule updateReportSchedule(@HeaderParam(RestParams.SID) String sessionID,
                                                                              @PathParam("report_type") String reportTypeString,
                                                                              @PathParam("specification_id") int specificationID,
                                                                              @PathParam("schedule_id") int scheduleID,
                                                                              hxc.ecds.protocol.rest.reports.ReportSchedule reportSchedule) {
        try (EntityManagerEx em = context.getEntityManager()) {
            Report.Type reportType = Report.Type.fromPathSegment(reportTypeString);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            logger.trace("reportSchedule = {}", reportSchedule);
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), reportType);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }

            return this.updateReportSchedule(em, reportSchedule, session, params, reportSpecification);
        } catch (RuleCheckException ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/{report_type}/{specification_id}/schedule/{schedule_id}")
    public void deleteReportSchedule(@HeaderParam(RestParams.SID) String sessionID, @PathParam("report_type") String reportTypeString,
                                     @PathParam("specification_id") int specificationID, @PathParam("schedule_id") int scheduleID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            Report.Type reportType = Report.Type.fromPathSegment(reportTypeString);
            RestParams params = new RestParams(sessionID);
            Session session = context.getSession(params.getSessionID());

            session.check(em, ReportSpecification.MAY_DELETE, "Not allowed to Delete Report Schedule %d for report %s", scheduleID, specificationID);
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), reportType);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            ReportSchedule existingReportSchedule = ReportSchedule.findByIDAndReportSpecificationID(em, session.getCompanyID(), scheduleID, specificationID);
            if (existingReportSchedule == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report schedule %d for report %d not found", scheduleID, specificationID);
            }
            if (Objects.equals(existingReportSchedule.getOriginator(), Report.Originator.MINIMUM_REQUIRED_DATA)) {
                throw new RuleCheckException(StatusCode.FORBIDDEN, null, "May not delete report schedules with originator MINIMUM_REQUIRED_DATA", existingReportSchedule.getOriginator());
            }
            AuditEntryContext auditContext = new AuditEntryContext("REPORT_SCHEDULE_REMOVE", existingReportSchedule.getInternalName(), existingReportSchedule.getId());
            existingReportSchedule.remove(em, session, auditContext);
        } catch (RuleCheckException ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    public ReportSchedule createReportSchedule(EntityManagerEx em, hxc.ecds.protocol.rest.reports.ReportSchedule reportSchedule, Session session,
                                               RestParams params, ReportSpecification reportSpecification)
            throws Exception {
        session.check(em, ReportSpecification.MAY_ADD, "Not allowed to Create Report");

        logger.trace("Reports.createReportSchedule: reportSchedule = {}", reportSchedule);

        if (reportSchedule.getEnabled()) {
            checkReportDailyScheduleLimit(em, session, reportSchedule);
        }

        ReportSchedule newReportSchedule = new ReportSchedule();
        newReportSchedule.amend(em, reportSchedule);
        newReportSchedule.setReportSpecificationID(reportSpecification.getId());
        // newReportSchedule.setReportSpecification(reportSpecification);
        logger.trace("Reports.createReportSchedule: newReportSchedule = {}", newReportSchedule);
        AuditEntryContext auditContext = new AuditEntryContext("REPORT_SCHEDULE_CREATE", newReportSchedule.getInternalName());
        newReportSchedule.persist(em, null, session, auditContext);
        reportSchedule.setId(newReportSchedule.getId());
        return newReportSchedule;
    }

    public ReportSchedule updateReportSchedule(EntityManagerEx em, hxc.ecds.protocol.rest.reports.ReportSchedule reportSchedule, Session session,
                                               RestParams params, ReportSpecification reportSpecification)
            throws Exception {
        logger.trace("Reports.updateReportSchedule: reportSchedule = {}", reportSchedule);
        session.check(em, ReportSpecification.MAY_UPDATE, "Not allowed to Update Report %d", reportSchedule.getId());

        ReportSchedule existingReportSchedule = ReportSchedule.findByIDAndReportSpecificationID(em, session.getCompanyID(), reportSchedule.getId(), reportSpecification.getId());
        if (existingReportSchedule == null || existingReportSchedule.getCompanyID() != session.getCompanyID()) {
            throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report schedule %d not found", reportSchedule.getId());
        }
        ReportSchedule previousReportSchedule = existingReportSchedule.copy(em);
        logger.trace("Reports.updateReportSchedule: previousReportSchedule = {}", previousReportSchedule);

        if (reportSchedule.getEnabled()) {
            checkReportDailyScheduleLimit(em, session, reportSchedule);
        }

        existingReportSchedule.amend(em, reportSchedule);
        existingReportSchedule.setReportSpecificationID(reportSpecification.getId());
        if (Objects.equals(previousReportSchedule.getOriginator(), Report.Originator.USER) && !Objects.equals(existingReportSchedule.getOriginator(), Report.Originator.USER)) {
            throw new RuleCheckException(StatusCode.FORBIDDEN, null, "May not change originator of report schedule from MINIMUM_REQUIRED_DATA to %s", existingReportSchedule.getOriginator());
        }
        if (existingReportSchedule.getOriginator() == null) {
            existingReportSchedule.setOriginator(Report.Originator.USER);
        }
        logger.trace("Reports.updateReportSchedule: existingReportSchedule = {}", existingReportSchedule);
        AuditEntryContext auditContext = new AuditEntryContext("REPORT_SCHEDULE_UPDATE", existingReportSchedule.getId());
        existingReportSchedule.persist(em, previousReportSchedule, session, auditContext);
        return existingReportSchedule;
    }

    // //////////////////////////////////////////////////////////////
    // Report Schedule :: Manual Execution
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/aux/schedule/ready")
    public ExResultList<hxc.ecds.protocol.rest.reports.ReportSchedule> listReportSchedulesReady(@HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            Session session = context.getSession(sessionID);
            List<ReportSchedule> reportSchedules = ReportSchedule.findReady(em, session.getCompanyID(), new Date());
            return new ExResultList<hxc.ecds.protocol.rest.reports.ReportSchedule>(reportSchedules.size(), reportSchedules);
        } catch (RuleCheckException ex) {
            logger.error("/aux/schedule/ready", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/aux/schedule/ready", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @POST
    @Path("/{report_type}/{specification_id}/schedule/{schedule_id}/execute")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public ExecuteScheduleResponse listReportSchedulesReady(@PathParam("report_type") String reportTypeString,
                                                            @PathParam("specification_id") int specificationID,
                                                            @PathParam("schedule_id") int scheduleID, ExecuteScheduleRequest request) {
        ExecuteScheduleResponse response = request.createResponse();
        response.setReturnCode(ResponseHeader.RETURN_CODE_SUCCESS);
        try (EntityManagerEx em = context.getEntityManager(); EntityManagerEx apEm = context.getApEntityManager();) {
            DbUtils.makeReadUncommitted(apEm);
            Report.Type reportType = Report.Type.fromPathSegment(reportTypeString);
            Session session = context.getSession(request.getSessionID());

            session.check(em, ReportSpecification.MAY_ADD, "Not allowed to Execute Report Schedule %d for report %s", scheduleID, specificationID);
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), reportType);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            ReportSchedule reportSchedule = ReportSchedule.findByIDAndReportSpecificationID(em, session.getCompanyID(), scheduleID, specificationID);
            if (reportSchedule == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report schedule %d for report %d not found", scheduleID, specificationID);
            }
            
            Date referenceDate = request.getReferenceDate() == null ? new Date() : request.getReferenceDate();
            ReportScheduleExecutor.SchedulesProcessor schedulesProcessor = new ReportScheduleExecutor.SchedulesProcessor(context, session.getCompanyInfo(), referenceDate, false);
            ExecuteScheduleResponse.NotExecutedReason notExecutedReason = schedulesProcessor.process(em, apEm, reportSchedule, true);
            
            if (notExecutedReason != null) {
                response.setExecuted(false);
                response.setNotExecutedReason(notExecutedReason);
            } else {
                response.setExecuted(true);
            }
        } catch (RuleCheckException ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}/execute", ex);
            response.setReturnCode(ex.getError());
            response.setAdditionalInformation(ex.getMessage());
        } catch (Throwable ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}/execute", ex);
            response.setReturnCode(TransactionsConfig.ERR_TECHNICAL_PROBLEM);
            response.setAdditionalInformation(ex.getMessage());
        }
        return response;
    }

    // //////////////////////////////////////////////////////////////
    // Report Schedule Users :: Saved
    // //////////////////////////////////////////////////////////////

    @GET
    @Path("/{report_type}/{specification_id}/schedule/{schedule_id}/web_user")
    public ExResultList<hxc.ecds.protocol.rest.WebUser> listReportScheduleWebUsers(@PathParam("report_type") String reportTypeString,
                                                                                   @PathParam("specification_id") int specificationID,
                                                                                   @PathParam("schedule_id") int scheduleID,
                                                                                   @HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            Report.Type reportType = Report.Type.fromPathSegment(reportTypeString);
            Session session = context.getSession(sessionID);
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), reportType);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            ReportSchedule reportSchedule = ReportSchedule.findByIDAndReportSpecificationID(em, session.getCompanyID(), scheduleID, specificationID);
            if (reportSchedule == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report schedule %d for report %d not found", scheduleID, specificationID);
            }
            return new ExResultList<hxc.ecds.protocol.rest.WebUser>(reportSchedule.getWebUsers().size(), reportSchedule.getWebUsers());
        } catch (RuleCheckException ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}/web_user", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}/web_user", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/{report_type}/{specification_id}/schedule/{schedule_id}/web_user/{web_user_id}")
    // @Consumes(MediaType.APPLICATION_JSON)
    public void addReportScheduleWebUser(@PathParam("report_type") String reportTypeString, @PathParam("specification_id") int specificationID,
                                         @PathParam("schedule_id") int scheduleID, @PathParam("web_user_id") int webUserID,
                                         @HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            Report.Type reportType = Report.Type.fromPathSegment(reportTypeString);
            Session session = context.getSession(sessionID);
            session.check(em, ReportSpecification.MAY_UPDATE, "Not allowed to Update Report %d", specificationID);
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), reportType);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            ReportSchedule reportSchedule = ReportSchedule.findByIDAndReportSpecificationID(em, session.getCompanyID(), scheduleID, specificationID);
            if (reportSchedule == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report schedule %d for report %d not found", scheduleID, specificationID);
            }
            WebUser webUser = WebUser.findByID(em, webUserID, session.getCompanyID());
            if (webUser == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", webUserID);
            }
            ReportSchedule existingReportSchedule = new ReportSchedule(reportSchedule);
            reportSchedule.getWebUsers().add(webUser);
            AuditEntryContext auditContext = new AuditEntryContext("REPORT_SCHEDULE_WEBUSER_ADD", webUserID, reportSchedule.getId());
            reportSchedule.persist(em, existingReportSchedule, session, auditContext);
        } catch (RuleCheckException ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}/web_user/{web_user_id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}/web_user/{web_user_id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("/{report_type}/{specification_id}/schedule/{schedule_id}/web_user/{web_user_id}")
    public void removeReportScheduleWebUser(@PathParam("report_type") String reportTypeString, @PathParam("specification_id") int specificationID,
                                            @PathParam("schedule_id") int scheduleID, @PathParam("web_user_id") int webUserID,
                                            @HeaderParam(RestParams.SID) String sessionID) {
        try (EntityManagerEx em = context.getEntityManager()) {
            Report.Type reportType = Report.Type.fromPathSegment(reportTypeString);
            Session session = context.getSession(sessionID);
            session.check(em, ReportSpecification.MAY_UPDATE, "Not allowed to Update Report %d", specificationID);
            ReportSpecification reportSpecification = ReportSpecification.findByIDAndType(em, specificationID, session.getCompanyID(), reportType);
            if (reportSpecification == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report %d not found", specificationID);
            }
            ReportSchedule reportSchedule = ReportSchedule.findByIDAndReportSpecificationID(em, session.getCompanyID(), scheduleID, specificationID);
            if (reportSchedule == null) {
                throw new RuleCheckException(StatusCode.NOT_FOUND, null, "Report schedule %d for report %d not found", scheduleID, specificationID);
            }
            ReportSchedule existingReportSchedule = new ReportSchedule(reportSchedule);
            ListIterator<WebUser> iter = reportSchedule.getWebUsers().listIterator();
            while (iter.hasNext()) {
                if (iter.next().getId() == webUserID) {
                    iter.remove();
                }
            }
            AuditEntryContext auditContext = new AuditEntryContext("REPORT_SCHEDULE_WEBUSER_REMOVE", webUserID, reportSchedule.getId());
            reportSchedule.persist(em, existingReportSchedule, session, auditContext);
        } catch (RuleCheckException ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}/web_user/{web_user_id}", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/{report_type}/{specification_id}/schedule/{schedule_id}/web_user/{web_user_id}", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Configuration
    //
    // /////////////////////////////////
    @GET
    @Path("/config")
    @Produces(MediaType.APPLICATION_JSON)
    public hxc.ecds.protocol.rest.config.ReportingConfig getConfig(@HeaderParam(RestParams.SID) String sessionID) {
        RestParams params = new RestParams(sessionID);
        try (EntityManagerEx em = context.getEntityManager()) {
            Session session = context.getSession(params.getSessionID());
            return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.ReportingConfig.class);
        } catch (RuleCheckException ex) {
            logger.error("/config", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/config", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @PUT
    @Path("/config")
    @Consumes(MediaType.APPLICATION_JSON)
    public void setConfig(hxc.ecds.protocol.rest.config.ReportingConfig configuration, @HeaderParam(RestParams.SID) String sessionID) {
        RestParams params = new RestParams(sessionID);
        try (EntityManagerEx em = context.getEntityManager()) {
            Session session = context.getSession(params.getSessionID());
            session.check(em, Transaction.MAY_CONFIG_REPORTING);
            context.findCompanyInfoByID(session.getCompanyID()).setConfiguration(em, configuration, session);

        } catch (RuleCheckException ex) {
            logger.error("/config", ex);
            throw ex.toWebException();
        } catch (Throwable ex) {
            logger.error("/config", ex);
            throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
        }
    }

}
