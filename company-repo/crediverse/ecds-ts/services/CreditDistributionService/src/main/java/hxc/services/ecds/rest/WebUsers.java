package hxc.services.ecds.rest;

import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.persistence.EntityManager;
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
import javax.ws.rs.core.Response.Status;

import jakarta.mail.internet.InternetAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.IAuthenticatable;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import hxc.ecds.protocol.rest.config.WebUsersConfig;
import hxc.services.ecds.AuditEntryContext;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.Session;
import hxc.services.ecds.model.AuditEntry;
import hxc.services.ecds.model.Department;
import hxc.services.ecds.model.Role;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.WebUser;
import hxc.services.ecds.model.WorkItem;
import hxc.services.ecds.rest.batch.CsvExportProcessor;
import hxc.services.ecds.rest.batch.WebUserProcessor;
import hxc.services.ecds.util.AuthenticationHelper;
import hxc.services.ecds.util.EmailUtils;
import hxc.services.ecds.util.EntityManagerEx;
import hxc.services.ecds.util.FormatHelper;
import hxc.services.ecds.util.QueryToken;
import hxc.services.ecds.util.RandomString;
import hxc.services.ecds.util.RuleCheck;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;
import hxc.services.ecds.util.StringExpander;

@Path("/web_users")
public class WebUsers
{
	final static Logger logger = LoggerFactory.getLogger(WebUsers.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final String VALUE_RSA_KEY = "RSA_PRIVATE";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Context
	//
	// /////////////////////////////////
	@Context
	private ICreditDistribution context;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get Currently Logged In WebUser
	//
	// /////////////////////////////////
	@GET
	@Path("/profile")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.WebUser getCurrentUserProfile(@HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			WebUser webUser = WebUser.findByID(em, session.getWebUserID(), session.getCompanyID());
			if (webUser == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", session.getWebUserID());
			return webUser;
		}
		catch (RuleCheckException ex)
		{
			logger.error("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/profile", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update Currently Logged In Webuser
	//
	// /////////////////////////////////
	@PUT
	@Path("/profile")
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateCurrentUserProfile(hxc.ecds.protocol.rest.WebUser webUser, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Normalize MSISDN
			webUser.setMobileNumber(context.toMSISDN(webUser.getMobileNumber()));

			// Test Permission
			session.check(em, WebUser.MAY_UPDATE_OWN, "Not allowed to Update Own Details %d", webUser.getId());

			// Only allow my own details to be updated here
			Integer webUserId = Integer.valueOf(webUser.getId());
			Integer sessionUserId = session.getWebUserID();
			RuleCheck.equals("Id", webUserId, sessionUserId);

			// Get the Existing Instance
			WebUser updated = WebUser.findByID(em, webUser.getId(), session.getCompanyID());
			if (updated == null || webUser.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", webUser.getId());
			WebUser existing = updated.copy(em);

			/* F0393 Only the following fields are to be updated */
			updated.setTitle(webUser.getTitle());
			updated.setFirstName(webUser.getFirstName());
			updated.setSurname(webUser.getSurname());
			updated.setLanguage(webUser.getLanguage());
			updated.setEmail(webUser.getEmail());
			updated.setInitials(webUser.getInitials());
			updated.setDepartmentID(webUser.getDepartmentID());

			updated.setDepartment(Department.findByID(em, updated.getDepartmentID(), updated.getCompanyID()));
			RuleCheck.notNull("departmentID", updated.getDepartment());

			// Persist to Database
			AuditEntryContext auditContext = new AuditEntryContext("WEBUSER_PROFILE_UPDATE", updated.getId());
			updated.persist(em, existing, session, auditContext);

		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/profile", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get WebUser
	//
	// /////////////////////////////////
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.WebUser getWebUser(@PathParam("id") int webUserID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			WebUser webUser = WebUser.findByID(em, webUserID, session.getCompanyID());
			if (webUser == null)
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", webUserID);
			return webUser;
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("getWebUser", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Get WebUsers
	//
	// /////////////////////////////////
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.WebUser[] getWebUsers( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{

		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
			Session session = context.getSession(params.getSessionID());
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				List<WebUser> webUsers;
				if (params.getFilter() == null && params.getSearch() == null && params.getSort() == null) {
					webUsers = WebUser.findAllNonServiceUsers(em, params, session.getCompanyID());
				} else {
					webUsers = WebUser.findAll(em, params, session.getCompanyID());
				}
				return webUsers.toArray(new WebUser[webUsers.size()]);
			}
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("getWebUsers", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("*")
	@Produces(MediaType.APPLICATION_JSON)
	public Long getWebUserCount( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID, 0, -1, null, search, filter);
			Session session = context.getSession(params.getSessionID());
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				if (params.getFilter() == null && params.getSearch() == null && params.getSort() == null) {
					return WebUser.findNonServiceUsersCount(em, session.getCompanyID());
				} else {
					return WebUser.findCount(em, params, session.getCompanyID());
				}
			}
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("getWebUserCount", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Path("/csv")
	@Produces("text/csv")
	public String getWebUserCsv( //
			@HeaderParam(RestParams.SID) String sessionID, //
			@DefaultValue("0") @QueryParam(RestParams.FIRST) int first, //
			@DefaultValue("-1") @QueryParam(RestParams.MAX) int max, //
			@QueryParam(RestParams.SORT) String sort, //
			@QueryParam(RestParams.SEARCH) String search, //
			@QueryParam(RestParams.FILTER) String filter //
	)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID, first, max, sort, search, filter);
			Session session = context.getSession(params.getSessionID());
			List<WebUser> serviceClasses;
			// TODO Remove when slow queries have been properly fixed!
			try (QueryToken token = context.getQueryToken())
			{
				if (params.getFilter() == null && params.getSearch() == null && params.getSort() == null) {
					serviceClasses = WebUser.findAllNonServiceUsers(em, params, session.getCompanyID());
				} else {
					serviceClasses = WebUser.findAll(em, params, session.getCompanyID());
				}
			}

			CsvExportProcessor<WebUser> processor = new CsvExportProcessor<WebUser>(WebUserProcessor.HEADINGS, first)
			{
				@Override
				protected void write(WebUser record)
				{
					put("id", record.getId());
					put("first_name", record.getFirstName());
					put("surname", record.getSurname());
					put("account_number", record.getAccountNumber());
					put("auth_method", record.getAuthenticationMethod());
					put("domain_account", record.getDomainAccountName());
					put("initials", record.getInitials());
					put("title", record.getTitle());
					put("status", record.getState());
					put("language", record.getLanguage());
					put("msisdn", record.getMobileNumber());
					put("department", record.getDepartment().getName());
					put("email", record.getEmail());
					put("activation_date", record.getActivationDate());
					put("deactivation_date", record.getDeactivationDate());
					put("expiration_date", record.getExpirationDate());
					List<Role> roles = record.getRoles();
					if (roles != null && !roles.isEmpty())
						put("role", roles.get(0).getName());
				}
			};
			return processor.add(serviceClasses);

		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("getWebUserCsv", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Update
	//
	// /////////////////////////////////
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void updateWebUser(hxc.ecds.protocol.rest.WebUser webUser, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());

			// Normalize MSISDN
			webUser.setMobileNumber(context.toMSISDN(webUser.getMobileNumber()));

			// Determine if this is a new Instance
			boolean isNew = webUser.getId() <= 0;
			if (isNew)
			{
				createWebUser(em, webUser, session, params);
				return;
			}

			// Test Permission
			session.check(em, WebUser.MAY_UPDATE, "Not allowed to Update WebUser %d", webUser.getId());

			// Get the Existing Instance
			WebUser updated = WebUser.findByID(em, webUser.getId(), session.getCompanyID());
			if (updated == null || webUser.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", webUser.getId());
			WebUser existing = updated.copy(em);
			//Copy properties that aren't updated via this update operation
			webUser.setKey1(existing.getKey1());
			webUser.setKey2(existing.getKey2());
			
			updated.amend(em, webUser);

			updated.setDepartment(Department.findByID(em, updated.getDepartmentID(), updated.getCompanyID()));
			RuleCheck.notNull("departmentID", updated.getDepartment());

			// Persist to Database
			AuditEntryContext auditContext = new AuditEntryContext("WEBUSER_UPDATE", updated.getId());
			updated.persist(em, existing, session, auditContext);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("updateWebUser", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Create
	//
	// /////////////////////////////////
	private void createWebUser(EntityManagerEx em, hxc.ecds.protocol.rest.WebUser webUser, Session session, RestParams params) throws RuleCheckException
	{
		try {
			// Test Permission
			session.check(em, WebUser.MAY_ADD, "Not allowed to Create WebUser");
	
			// Persist it
			WebUser newWebUser = new WebUser();
			newWebUser.amend(em, webUser);
			
			//Check constraint violations before we continue...
			if(WebUser.findByDomainAccountName(em, newWebUser.getCompanyID(), newWebUser.getDomainAccountName()) != null)
			{
				logger.info("Duplicate webuser found on Domain Account Name.");
				throw new RuleCheckException(StatusCode.FAILED_TO_SAVE, "domainAccountName", "Duplicate"); 
			}
			if(WebUser.findByAccountNo(em, newWebUser.getCompanyID(), newWebUser.getAccountNumber()) != null)
			{
				logger.info("Duplicate webuser found on Account Number.");
				throw new RuleCheckException(StatusCode.FAILED_TO_SAVE, "accountNumber", "Duplicate");				
			}	
	
			newWebUser.setDepartment(Department.findByID(em, newWebUser.getDepartmentID(), newWebUser.getCompanyID()));
			RuleCheck.notNull("departmentID", newWebUser.getDepartment());
	
			// Add Default Account Number
			newWebUser.autoNumber(em, session.getCompanyID());
	
			if (WebUser.STATE_PERMANENT.equals(newWebUser.getState()))
				throw new RuleCheckException(StatusCode.INVALID_VALUE, "state", "Cannot Add new permanent WebUser");
			String password = AuthenticationHelper.createRandomPassword(newWebUser);
			AuditEntryContext auditContext = new AuditEntryContext("WEBUSER_CREATE");
			newWebUser.persist(em, null, session, auditContext);
			
    		CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
    		WebUsersConfig webUsersConfig = companyInfo.getConfiguration(em, WebUsersConfig.class);
			if(IAuthenticatable.AUTHENTICATE_PASSWORD_2FACTOR.equals(newWebUser.getAuthenticationMethod()))
			{
				sendEmailNotification(em, session, newWebUser, webUsersConfig.getWelcomePasswordUserEmailSubject(), webUsersConfig.getWelcomePasswordUserEmailBody(), password);	
			} else if(IAuthenticatable.AUTHENTICATE_EXTERNAL_2FACTOR.equals(newWebUser.getAuthenticationMethod()))
			{
    			sendEmailNotification(em, session, newWebUser, webUsersConfig.getWelcomeDomainAccUserEmailSubject(), webUsersConfig.getWelcomeDomainAccUserEmailBody(), password);
			}
		} catch (RuleCheckException rce) {
			logger.info("rulecheck error while creating web user", rce);
			throw rce;
		} catch (Throwable ex)
		{
			logger.error("Failed to create web user", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	// /////////////////////////////////
	@DELETE
	@Path("/{id}")
	public void deleteWebUser(@PathParam("id") int webUserID, @HeaderParam(RestParams.SID) String sessionID)
	{
		try (EntityManagerEx em = context.getEntityManager())
		{
			RestParams params = new RestParams(sessionID);
			Session session = context.getSession(params.getSessionID());
			logger.info("Request from session {} with web user id {} to delete web user with id {}", session.getWebUserID(), sessionID, webUserID);
			session.check(em, WebUser.MAY_DELETE, "Not allowed to Delete WebUser %d", webUserID);

			// Get the Existing Instance
			WebUser existing = WebUser.findByID(em, webUserID, session.getCompanyID());
			if (existing == null || existing.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", webUserID);

			// Cannot delete self ...
			if (webUserID == session.getWebUserID())
				throw new RuleCheckException(StatusCode.CANNOT_DELETE_SELF, null, "Cannot delete self");

			// May not be Permanent
			if (WebUser.STATE_PERMANENT.equals(existing.getState()))
				throw new RuleCheckException(StatusCode.CANNOT_DELETE, null, "May not delete Permanent WebUser %d", webUserID);

			// Test if in use
			if (AuditEntry.referencesWebUser(em, webUserID) || WorkItem.referencesWebUser(em, webUserID) //
					|| Transaction.referencesWebUser(em, existing.getMobileNumber()))
				throw new RuleCheckException(StatusCode.RESOURCE_IN_USE, null, "WebUser %d is in use", webUserID);

			// Delete from Database
			AuditEntryContext auditContext = new AuditEntryContext("WEBUSER_REMOVE", existing.getId());
			existing.remove(em, session, auditContext);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/{id}", ex);
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
	public hxc.ecds.protocol.rest.config.WebUsersConfig getConfig(@HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			return context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.WebUsersConfig.class);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/config", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	@PUT
	@Path("/config")
	@Consumes(MediaType.APPLICATION_JSON)
	public void setConfig(hxc.ecds.protocol.rest.config.WebUsersConfig configuration, @HeaderParam(RestParams.SID) String sessionID)
	{
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			session.check(em, WebUser.MAY_CONFIGURE);
			context.findCompanyInfoByID(session.getCompanyID()).setConfiguration(em, configuration, session);
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			throw ex.toWebException();
		}
		catch (Throwable ex)
		{
			logger.error("/config", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Passwords
	//
	// /////////////////////////////////
	@POST
	@Path("/change_password")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.ChangePasswordResponse changePassword(hxc.ecds.protocol.rest.ChangePasswordRequest request, @HeaderParam(RestParams.SID) String sessionID)
	{
		hxc.ecds.protocol.rest.ChangePasswordResponse response = new hxc.ecds.protocol.rest.ChangePasswordResponse();
		response.setReturnCode(hxc.ecds.protocol.rest.ChangePasswordResponse.RETURN_CODE_SUCCESS);
		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			Integer sessionWebUserID = session.getWebUserID();
			if(sessionWebUserID == null )
				throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Cannot change password. Session does not belong to a WebUser. Only the WebUser can change the its password.");
			if(sessionWebUserID != null  && !sessionWebUserID.equals(request.getEntityID()))
				throw new RuleCheckException(StatusCode.FORBIDDEN, null, "Cannot change another WebUser's password. webUserID %d changing webUserID %s password", sessionWebUserID, request.getEntityID());

			// Get the WebUser
			Integer webUserID = request.getEntityID();
			WebUser webUser = WebUser.findByID(em, webUserID, session.getCompanyID());
			if (webUser == null || webUser.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", webUserID);
			if(!webUser.testIfSamePin(request.getCurrentPassword()))
				throw new RuleCheckException(StatusCode.INVALID_PIN, null, "Autentication credentials are incorrect");
			String newPassword = request.getNewPassword();
			if (newPassword == null || newPassword.isEmpty())
				throw new RuleCheckException(StatusCode.INVALID_PIN, null, "Agent %d password is empty", webUserID);
			WebUsers.validateNewPassword(em, context.findCompanyInfoByID(em, session.getCompanyID()), webUser, newPassword);
			byte[] encryptedPassword = AuthenticationHelper.encryptPin(newPassword);
			webUser.updatePin(em, encryptedPassword, session);
    		{//Send Email...
	    		CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
	    		WebUsersConfig webUsersConfig = companyInfo.getConfiguration(em, WebUsersConfig.class);
	    		sendEmailNotification(em, session, webUser, webUsersConfig.getPasswordChangeEmailSubject(), webUsersConfig.getPasswordChangeEmailBody(), newPassword);
    		}
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			response.setReturnCode(ex.getError());
		}
		catch (Throwable ex)
		{
			logger.error("/change_password", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}

		return response;
	}
	
	@POST
	@Path("/reset_password")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public hxc.ecds.protocol.rest.ChangePasswordResponse resetPassword(hxc.ecds.protocol.rest.ChangePasswordRequest request, @HeaderParam(RestParams.SID) String sessionID)
	{
		hxc.ecds.protocol.rest.ChangePasswordResponse response = new hxc.ecds.protocol.rest.ChangePasswordResponse();
		response.setReturnCode(hxc.ecds.protocol.rest.ChangePasswordResponse.RETURN_CODE_SUCCESS);

		RestParams params = new RestParams(sessionID);
		try (EntityManagerEx em = context.getEntityManager())
		{
			Session session = context.getSession(params.getSessionID());
			if (request.getEntityID() == null || session.getWebUserID() == null || ! request.getEntityID().equals(session.getWebUserID()))
				session.check(em, WebUser.MAY_RESET_PASSWORDS);

			// Get the WebUser in question
			Integer webUserID = request.getEntityID();
			WebUser webUser = WebUser.findByID(em, webUserID, session.getCompanyID());
			if (webUser == null || webUser.getCompanyID() != session.getCompanyID())
				throw new RuleCheckException(StatusCode.NOT_FOUND, null, "WebUser %d not found", webUserID);

			RandomString randomString = new RandomString(8, new SecureRandom());
	    	String password = randomString.nextString();
    		byte[] encryptedPassword = AuthenticationHelper.encryptPin(password);
    		webUser.updatePin(em, encryptedPassword, session);
    		{//Send Email...
	    		CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
	    		WebUsersConfig webUsersConfig = companyInfo.getConfiguration(em, WebUsersConfig.class);
	    		sendEmailNotification(em, session, webUser, webUsersConfig.getPasswordResetEmailSubject(), webUsersConfig.getPasswordResetEmailBody(), password);
    		}
		}
		catch (RuleCheckException ex)
		{
			logger.info("rulecheck", ex);
			response.setReturnCode(ex.getError());
		}
		catch (Throwable ex)
		{
			logger.error("/reset_password", ex);
			throw new WebApplicationException(ex, Status.INTERNAL_SERVER_ERROR);
		}
		return response;
	}
	
	private void sendEmailNotification(EntityManagerEx em, Session session, WebUser webUser, Phrase emailSubject, Phrase emailBody, String password) throws Exception
	{
		EmailUtils emailer = new EmailUtils(context);
		CompanyInfo companyInfo = context.findCompanyInfoByID(em, session.getCompanyID());
		WebUsersConfig webUsersConfig = companyInfo.getConfiguration(em, WebUsersConfig.class);
		InternetAddress fromEmailAddress = new InternetAddress(webUsersConfig.getFromEmailAddress());
		//String notificationType;
		StringExpander<WebUser> expander = new StringExpander<WebUser>()
		{
			@Override
			protected String expandField(String englishName, Locale locale, WebUser webUser)
			{					
				switch (englishName)
				{
					case WebUsersConfig.DATE:
						return FormatHelper.formatDate(context, this, locale, new Date());
					case WebUsersConfig.TIME:
						return FormatHelper.formatTime(context, this, locale, new Date());
					case WebUsersConfig.RECEPIENT_TITLE:
						return webUser.getTitle();
					case WebUsersConfig.RECEPIENT_FIRST_NAME:
						return webUser.getFirstName();
					case WebUsersConfig.RECEPIENT_SURNAME:
						return webUser.getSurname();
					case WebUsersConfig.RECEPIENT_INITIALS:
						return webUser.getInitials();
					case WebUsersConfig.USERNAME:
						return webUser.getDomainAccountName();
					case WebUsersConfig.PASSWORD:
						return password;
					default:
						return "";
				}
			}

		};
		Locale locale = new Locale(webUser.getLanguage(), companyInfo.getCompany().getCountry());
		String subject = expander.expandNotification(emailSubject, locale, webUsersConfig.listPasswordEmailFields(), webUser);
		String body = expander.expandNotification(emailBody, locale, webUsersConfig.listPasswordEmailFields(), webUser);    		
		hxc.ecds.protocol.rest.config.GeneralConfig generalConfig =
				context.findCompanyInfoByID(session.getCompanyID()).getConfiguration(em, hxc.ecds.protocol.rest.config.GeneralConfig.class);
		emailer.sendEmail(fromEmailAddress, webUser.getEmail(), subject, body, null, generalConfig.getSmtpRetries(), true);
	}

	public static byte[] validateNewPin(EntityManager em, CompanyInfo company, WebUser webUser, String newPin) throws RuleCheckException
	{
		// Test if Valid
		if (newPin == null || newPin.isEmpty() || webUser == null)
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PIN, "password", "Empty Password");

		// Test Min Max Length
		WebUsersConfig config = company.getConfiguration(em, WebUsersConfig.class);
		if (newPin.length() < config.getMinPasswordLength())
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PIN, "password", "Password too short");
		
		// Encrypt
		byte[] key = AuthenticationHelper.encryptPin(newPin);

		// Test History
		if (AuthenticationHelper.testIfSameKey(key, webUser.getKey1()) //
				|| AuthenticationHelper.testIfSameKey(key, webUser.getKey2()) //
				|| AuthenticationHelper.testIfSameKey(key, webUser.getKey3()) //
				|| AuthenticationHelper.testIfSameKey(key, webUser.getKey4()))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PIN, "password", "Repeated Password");

		return key;

	}

	public static byte[] validateNewPassword(EntityManager em, CompanyInfo company, WebUser webUser, String newPassword) throws RuleCheckException
	{
		// Test if Valid
		if (newPassword == null || newPassword.isEmpty() || webUser == null)
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Empty Password");

		// Test Min Max Length
		WebUsersConfig config = company.getConfiguration(em, WebUsersConfig.class);
		if (newPassword.length() < config.getMinPasswordLength())
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Password too short");
		
		// Test if Valid
		if (newPassword == null || newPassword.isEmpty() || webUser == null)
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Empty Password");
		
		if (!newPassword.matches(".*[a-zA-Z]+.*"))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Password must contain atleast one alphabetic character");
		
		if (!newPassword.matches(".*[0-9]+.*"))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Password must contain atleast one numeric character");
		
		if (!newPassword.matches(".*[ !\"#$%&'()*+,-./:;<=>?@\\[\\\\\\]^_`{|}~]+.*"))
			throw new RuleCheckException(TransactionsConfig.ERR_INVALID_PASSWORD, "password", "Password must contain atleast one special character");

		// Encrypt
		byte[] key = AuthenticationHelper.encryptPin(newPassword);

		// Test History
		if (AuthenticationHelper.testIfSameKey(key, webUser.getKey1()) //
				|| AuthenticationHelper.testIfSameKey(key, webUser.getKey2()) //
				|| AuthenticationHelper.testIfSameKey(key, webUser.getKey3()) //
				|| AuthenticationHelper.testIfSameKey(key, webUser.getKey4()))
			throw new RuleCheckException(TransactionsConfig.ERR_HISTORIC_PASSWORD, "password", "Repeated Password");
		
		return key;
	}
}
