package hxc.connectors.ui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.configuration.IConfiguration;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnection;
import hxc.connectors.IConnector;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.database.IDatabase;
import hxc.connectors.ui.server.UIServer;
import hxc.connectors.ui.sessionman.UiSession;
import hxc.connectors.ui.sessionman.UiSessionManager;
import hxc.servicebus.IServiceBus;
import hxc.services.notification.INotifications;
import hxc.services.security.ISecurity;
import hxc.services.security.IUser;
import hxc.services.security.Perm;
import hxc.services.security.Perms;
//import hxc.services.security.Permissions;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.instrumentation.IMetric;
import hxc.utils.protocol.uiconnector.airsim.AirResponseResetRequest;
import hxc.utils.protocol.uiconnector.airsim.AirResponseUpdateRequest;
import hxc.utils.protocol.uiconnector.airsim.AirSimSmsRequest;
import hxc.utils.protocol.uiconnector.airsim.AirSimUssdRequest;
import hxc.utils.protocol.uiconnector.alarms.GetAlarmDataRequest;
import hxc.utils.protocol.uiconnector.bam.GetAvailablePluginMetricsRequest;
import hxc.utils.protocol.uiconnector.bam.GetMetricRequest;
import hxc.utils.protocol.uiconnector.bam.GetMetricsRequest;
import hxc.utils.protocol.uiconnector.bam.GetUserLayoutRequest;
import hxc.utils.protocol.uiconnector.bam.RegisterMetricRequest;
import hxc.utils.protocol.uiconnector.bam.SetUserLayoutRequest;
import hxc.utils.protocol.uiconnector.bam.UnregisterMetricRequest;
import hxc.utils.protocol.uiconnector.common.ConfigurationPath;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigServerInfoUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigServerRoleRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigServerRoleUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigurationRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigurationUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.FitnessRequest;
import hxc.utils.protocol.uiconnector.logtailer.LogFileRequest;
import hxc.utils.protocol.uiconnector.metrics.MetricsDataRequest;
import hxc.utils.protocol.uiconnector.metrics.MetricsRequest;
import hxc.utils.protocol.uiconnector.reports.GenerateReportRequest;
import hxc.utils.protocol.uiconnector.reports.GetAvailableReports;
import hxc.utils.protocol.uiconnector.reports.GetReportparametersRequest;
import hxc.utils.protocol.uiconnector.request.AirSimCommonRequest;
import hxc.utils.protocol.uiconnector.request.AirSimGetUsageRequest;
import hxc.utils.protocol.uiconnector.request.AirSimStartUsageRequest;
import hxc.utils.protocol.uiconnector.request.AirSimStopUsageRequest;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.CallConfigurableMethodRequest;
import hxc.utils.protocol.uiconnector.request.ConfigurationUpdateRequest;
import hxc.utils.protocol.uiconnector.request.ESBShutdownRequest;
import hxc.utils.protocol.uiconnector.request.GetAirsimHistoryRequest;
import hxc.utils.protocol.uiconnector.request.GetAllConfigurablesRequest;
import hxc.utils.protocol.uiconnector.request.GetAllConfigurationPathsRequest;
import hxc.utils.protocol.uiconnector.request.GetConfigurableRequest;
import hxc.utils.protocol.uiconnector.request.GetEcdsCheckTamperedAgentRequest;
import hxc.utils.protocol.uiconnector.request.GetEcdsTamperCheckRequest;
import hxc.utils.protocol.uiconnector.request.GetEcdsTamperResetRequest;
import hxc.utils.protocol.uiconnector.request.GetFacilityRequest;
import hxc.utils.protocol.uiconnector.request.GetLicenseDetailsRequest;
import hxc.utils.protocol.uiconnector.request.GetLocaleInformationRequest;
import hxc.utils.protocol.uiconnector.request.LogRequest;
import hxc.utils.protocol.uiconnector.request.ReturnCodeTextDefaultsRequest;
import hxc.utils.protocol.uiconnector.request.RevertServiceRequest;
import hxc.utils.protocol.uiconnector.request.SendSMSRequest;
import hxc.utils.protocol.uiconnector.request.SessionTimeOutTimeRequest;
import hxc.utils.protocol.uiconnector.request.SystemInfoRequest;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest.UiRequestCode;
import hxc.utils.protocol.uiconnector.request.ValidateSessionRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.CallConfigurableMethodResponse;
import hxc.utils.protocol.uiconnector.response.ConfirmationResponse;
import hxc.utils.protocol.uiconnector.response.ESBShutdownResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse.ErrorCode;
import hxc.utils.protocol.uiconnector.response.GetAllConfigurationPathsResponse;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.RevertServiceResponse;
import hxc.utils.protocol.uiconnector.response.SessionTimeOutTimeResponse;
import hxc.utils.protocol.uiconnector.response.SystemInfoResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.userman.common.SecurityRole;
import hxc.utils.protocol.uiconnector.userman.common.UserDetails;
import hxc.utils.protocol.uiconnector.userman.request.DeleteSecurityRoleRequest;
import hxc.utils.protocol.uiconnector.userman.request.DeleteUserRequest;
import hxc.utils.protocol.uiconnector.userman.request.ReadSecurityPermissionRequest;
import hxc.utils.protocol.uiconnector.userman.request.ReadSecurityRoleRequest;
import hxc.utils.protocol.uiconnector.userman.request.ReadUserDetailsRequest;
import hxc.utils.protocol.uiconnector.userman.request.UpdateMyDetailsRequest;
import hxc.utils.protocol.uiconnector.userman.request.UpdateSecurityRoleRequest;
import hxc.utils.protocol.uiconnector.userman.request.UpdateUserPasswordRequest;
import hxc.utils.protocol.uiconnector.userman.request.UpdateUserRequest;
import hxc.utils.protocol.uiconnector.userman.response.DeleteSecurityRolesResponse;
import hxc.utils.protocol.uiconnector.userman.response.DeleteUserResponse;
import hxc.utils.protocol.uiconnector.userman.response.ReadUserDetailsResponse;
import hxc.utils.protocol.uiconnector.userman.response.ValidSessionResponse;
import hxc.utils.protocol.uiconnector.vas.VasCommandsRequest;
import hxc.utils.uiconnector.client.UIClient;

public class UiConnector implements IConnector
{
	final static Logger logger = LoggerFactory.getLogger(UiConnector.class);
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private IServiceBus esb;
	private UIServer server;
	private IDatabase database;
	private ICtrlConnector control;
	private boolean firstLogin = true;

	private UiConnectorConfig config = new UiConnectorConfig();
	private UiSessionManager sessionManager;
	private UiMetricsMonitoring metricsMonitoring;
	private UiLogFileController logFileController;
	private UiReportsController reportsController;
	private UiAlarmController alarmController;
	private UiBamController bamController;

	// Utility Classes for extraction
	private UiServiceConfiguration serviceConfigUtils;// = new UiConfigurationExtraction();
	private UiUserConfiguration userConfigUtils;

	@Perms(perms = { @Perm(name = "ChangeUIConnectorParameters", implies = "ViewUIConnectorParameters", description = "Change UI Connector Parameters", category = "UI Connector", supplier = true),
			@Perm(name = "ViewUIConnectorParameters", description = "View UI Connector Parameters", category = "UI Connector", supplier = true), })
	class UiConnectorConfig extends ConfigurationBase
	{

		private final long serialVersionUID = -4508865263730149368L;
		private int serverPort = 10101;
		private boolean debugMode = false;
		
		// private int sessionTimeoutMilliseconds = 1000 * 60 * 21;
		private int sessionTimeoutMinutes = 21;

		@Override
		public String getPath(String languageCode)
		{
			return "Technical Settings";
		}

		@Override
		public INotifications getNotifications()
		{
			return null;
		}

		@Override
		public long getSerialVersionUID()
		{
			return serialVersionUID;
		}

		@Override
		public String getName(String languageCode)
		{
			return "UiConnector";
		}

		@Override
		public void validate() throws ValidationException
		{
			try
			{
				Objects.requireNonNull( sessionManager, "sessionManager must not be null" );
				Objects.requireNonNull( config, "config must not be null" );
				sessionManager.setSessionTimeOut(config.getSessionTimeoutMinutes() * 1000 * 60);				
			}
			catch(Exception e) 
			{
				logger.error("UI Connector Validation Error thrown", e);
			}
		}

		@SupplierOnly
		public int getServerPort()
		{
			check(esb, "ViewUIConnectorParameters");
			return serverPort;
		}

		// @SupplierOnly
		// public int getMaxAlarmsPerType()
		// {
		// check(esb, "ViewUIConnectorParameters");
		// return maxAlarmsPerType;
		// }

		public int getSessionTimeoutMinutes()
		{
			return sessionTimeoutMinutes;
		}

		public void setSessionTimeoutMinutes(int sessionTimeoutMinutes) throws ValidationException
		{
			check(esb, "ChangeUIConnectorParameters");

			ValidationException.min(1, sessionTimeoutMinutes);
			this.sessionTimeoutMinutes = sessionTimeoutMinutes;
		}
		
		@SupplierOnly
		public boolean getDebugMode()
		{
			return debugMode;
		}

		// @SupplierOnly
		// public void setMaxAlarmsPerType(int maxAlarmsPerType) throws ValidationException
		// {
		// ValidationException.min(0, maxAlarmsPerType);
		// this.maxAlarmsPerType = maxAlarmsPerType;
		// }

	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////
	// Initialization code
	// //////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public void initialise(IServiceBus esb)
	{
		this.esb = esb;
	}

	@Override
	public boolean start(String[] args)
	{
		logger.info("UiConnector.start( ... ): ..." );
		database = esb.getFirstConnector(IDatabase.class);
		if ( database == null )
		{
			logger.warn("UIConnector could not find database to start");
		}

		try
		{
			// Get Control Connector
			control = esb.getFirstConnector(ICtrlConnector.class);
			if (control == null)
			{
				logger.warn("UIConnector could not find ICtrlConnector to start");
				return false;
			}

			serviceConfigUtils = new UiServiceConfiguration(esb, database, control);
			sessionManager = new UiSessionManager(config.sessionTimeoutMinutes * 1000 * 60);
			userConfigUtils = new UiUserConfiguration();
			metricsMonitoring = new UiMetricsMonitoring(esb, sessionManager);
			logFileController = new UiLogFileController(esb, sessionManager);
			reportsController = new UiReportsController(esb, sessionManager);
			alarmController = new UiAlarmController(esb, control);
			bamController = new UiBamController(esb, sessionManager);

			// Create Server
			startUiConnectorServer();

			// Log Information
			logger.info("UI Connector started on {}", config.getServerPort());

			// Check if running in Debug mode
			if ((args !=null) && (Arrays.asList(args).contains("-debug") || Arrays.asList(args).contains("-d")))
				config.debugMode = true;
			else
				config.debugMode = false;
		}
		catch(Exception e)
		{
			logger.error("Exception thrown prevented Start UIConnector", e);
			return false;
		}
		
		return true;
	}

	@Override
	public void stop()
	{
		server.stop();
		logger.info("UI Connector stopped");
	}

	@Override
	public IConfiguration getConfiguration()
	{
		return config;
	}

	@Override
	public void setConfiguration(IConfiguration config) throws ValidationException
	{
		this.config = (UiConnectorConfig) config;
		stopUiConnectorServer();
		startUiConnectorServer();
	}

	private void startUiConnectorServer()
	{
		if (server != null)
		{
			stopUiConnectorServer();
		}
		server = new UIServer(esb)
		{
			@Override
			protected UiBaseResponse handleUiRequest(UiBaseRequest request) throws IOException
			{
				return handleUiControllerRequest(request);
			}
		};
		
		try
		{
			server.start(config.serverPort);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			server = null;
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
			server = null;
		}
	}

	private void stopUiConnectorServer()
	{
		try
		{
			if (server != null)
			{
				server.stop();
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		server = null;
	}

	private UiBaseResponse handleUiControllerRequest(UiBaseRequest request) throws IOException
	{
		// Integral part of security
		ISecurity security = esb.getFirstService(ISecurity.class);

		UiBaseResponse response = null;

		logger.trace("handleUiControllerRequest: request = {}", request);	

		if (request instanceof LogRequest)
		{
			LogRequest logRequest = (LogRequest) request;
			
			//logger.log(logRequest.getLoggingLevel(), this, logRequest.getLogMessage());
			switch (logRequest.getLoggingLevel())
			{
				case DEBUG:
					logger.debug(logRequest.getLogMessage());
					break;
				case ERROR:
					logger.error(logRequest.getLogMessage());
					break;
				case FATAL:
					logger.error(logRequest.getLogMessage());
					break;
				case INFO:
					logger.info(logRequest.getLogMessage());
					break;
				case TRACE:
					logger.trace(logRequest.getLogMessage());
					break;
				case WARN:
					logger.warn(logRequest.getLogMessage());
					break;
				default:
					break;
			}
			response = createConfirmationResponse(request.getUserId(), request.getSessionId(), "logged");
		}
		else if (request instanceof SessionTimeOutTimeRequest)
		{
			response = new SessionTimeOutTimeResponse(request.getUserId(), request.getSessionId(), config.getSessionTimeoutMinutes());
		}
		else if (request instanceof FitnessRequest)
		{
			response = serviceConfigUtils.findSystemFitness(request.getUserId(), request.getSessionId());
		}
		else if (request instanceof GetAlarmDataRequest)
		{
			response = alarmController.pollAlarmData((GetAlarmDataRequest) request);
		}
		else if (request instanceof ValidateSessionRequest)
		{
			UiSession session = sessionManager.validateSession(request.getUserId(), request.getSessionId());
			if (session != null)
			{
				response = new ValidSessionResponse(request.getUserId(), request.getSessionId());
			}
			else
			{
				response = createErrorResponse(request.getUserId(), ErrorCode.SESSION_EXPIRED, "User session invalid");
			}
		}
		else if (request instanceof SystemInfoRequest)
		{
			// UiSession session = sessionManager.validateSession(request.getUserId(), request.getSessionId());
			response = new SystemInfoResponse(esb.getVersion(), config.debugMode);
		}
		else if (request.getRequestCode() == UiRequestCode.PUBLIC_KEY)
		{
			// public key Request
			if (security == null)
			{
				// Security Failure
				response = createErrorResponse(request.getUserId(), ErrorCode.SYSTEM_FAILURE, "Security service not available");
			}
			else
			{
				byte[] publicKey = security.getPublicKey(request.getUserId());
				if (publicKey == null)
				{
					// Key Failure
					response = createErrorResponse(request.getUserId(), ErrorCode.AUTHENTICATION_FAILURE, "User does not exist");
				}
				else
				{
					// Key success
					response = new PublicKeyResponse(request.getUserId());
					((PublicKeyResponse) response).setPublicKey(publicKey);
				}
			}

		}
		else if (request.getRequestCode() == UiRequestCode.AUTHENTICATE)
		{
			// User Authentication (Note that this will throw a SecurityException if there is a problem)
			try
			{
				AuthenticateRequest authRequest = (AuthenticateRequest) request;
				IUser user = security.authenticate(request.getUserId(), authRequest.getCredentials());

				if (user == null)
				{
					response = createErrorResponse(request.getUserId(), ErrorCode.AUTHENTICATION_FAILURE, "User authentication failure");
				}
				else
				{
					if (firstLogin)
					{
						try
						{
							security.getPermissions();
							firstLogin = false;
						}
						catch (Exception fle)
						{
							logger.debug("UiConnector Authentication Exception", fle);
						}
					}
					response = new AuthenticateResponse(request.getUserId());
					// String userId = user.getUserId();
					byte[] credentials = authRequest.getCredentials();
					String sessionId = sessionManager.addSession(request.getUserId(), credentials);

					// Update user permissions
					((AuthenticateResponse) response).getPermissionIds().addAll(security.getUserPermissionIds(user));
					((AuthenticateResponse) response).setSessionId(sessionId);
//					((AuthenticateResponse) response).setName(((User) user).getInternalName());
					((AuthenticateResponse) response).setName(user.getInternalName());
				}
			}
			catch (SecurityException se)
			{
				response = createErrorResponse(request.getUserId(), ErrorCode.AUTHENTICATION_FAILURE, "User authentication failure");
			}
			catch (Exception oe)
			{
				response = createErrorResponse(request.getUserId(), ErrorCode.SYSTEM_FAILURE, "System authentication failure review logs");
			}

		}
		else
		{
			UiSession session = sessionManager.validateSession(request.getUserId(), request.getSessionId());

			IUser user = null;

			if (session != null)
			{
				// All other requests require user authentication (which throws exception)
				try
				{
					user = security.authenticate(session.getUserId(), session.getCredentials());
				}
				catch (SecurityException se)
				{
					user = null;
				}
			}

			if (user == null)
			{
				response = createErrorResponse(request.getUserId(), ErrorCode.SESSION_EXPIRED, "User session invalid");
			}
			else
			{
				// All other requests could fail with exceptions
				try
				{

					if (request instanceof GetLicenseDetailsRequest)
					{
						response = serviceConfigUtils.getLicenseDetails((GetLicenseDetailsRequest) request);
					}
					else if (request instanceof GetFacilityRequest)
					{
						response = serviceConfigUtils.getFacility((GetFacilityRequest) request);
					}
					else if (request instanceof GetAllConfigurablesRequest)
					{
						// Extract all configuration
						response = serviceConfigUtils.retrieveAllConfigurable(user);
						response.setUserId(request.getUserId());
						response.setSessionId(request.getSessionId());
					}
					else if (request instanceof GetAllConfigurationPathsRequest) // Used to Build GUI Menu
					{
						response = extractPaths(request, user);
					}
					else if (request instanceof GetConfigurableRequest)
					{
						response = serviceConfigUtils.retrieveConfigurable(user, (GetConfigurableRequest) request);
						if (response == null)
						{
							response = createErrorResponse(request.getUserId(), ErrorCode.GENERAL, "Configuration could not be found");
						}
						else
						{
							response.setUserId(request.getUserId());
							response.setSessionId(request.getSessionId());
						}
					}
					else if (request instanceof ConfigurationUpdateRequest)
					{
						// Update specific configuration (ConfigurationUpdateResponse)
						ConfigurationUpdateRequest update = (ConfigurationUpdateRequest) request;
						logger.info("Update request for {} by user {}", update.getConfigurableSerialVersionUID(), update.getUserId());

						response = serviceConfigUtils.updateConfiguration(update, user);
						logger.trace("DONE: Update request for {} by user {}", update.getConfigurableSerialVersionUID(), update.getUserId());
						if (response == null)
						{
							response = createErrorResponse(request.getUserId(), ErrorCode.GENERAL, "Configuration could be found to be updated");
						}
					}
					else if (request instanceof CallConfigurableMethodRequest)
					{
						try
						{
							String result = serviceConfigUtils.executeConfigurableMethod(user, (CallConfigurableMethodRequest) request);
							response = new CallConfigurableMethodResponse(result);
						}
						catch (SecurityException se)
						{
							response = createErrorResponse(request.getUserId(), ErrorCode.SECURITY_CONSTRAINT, "Cannot perform action due to security constraint");
						}
						catch (Exception e)
						{
							Throwable target = ((InvocationTargetException) e).getTargetException();
							String errorMessage = target.getMessage();
							
							if (target instanceof SecurityException)
								response = createErrorResponse(request.getUserId(), ErrorCode.SYSTEM_FAILURE, String.format("Cannot perform action due to security constraint: [%s]", errorMessage));
							else
								response = createErrorResponse(request.getUserId(), ErrorCode.SYSTEM_FAILURE, String.format("Could not perform method call: [%s]", errorMessage));
						}
					}
					else if (request instanceof GetLocaleInformationRequest)
					{
						response = serviceConfigUtils.retrieveLocaleInformation();
						if (response == null || !(response instanceof GetLocaleInformationResponse))
						{
							response = createErrorResponse(request.getUserId(), ErrorCode.GENERAL, "Could not retrieve the Locale Information.");
						}
					}
					else if (request instanceof ESBShutdownRequest)
					{
						response = new ESBShutdownResponse();
						((ESBShutdownResponse) response).setPID(esb.getPID());
						esb.stop();
					}
					else if (request instanceof RevertServiceRequest)
					{
						response = serviceConfigUtils.revertService(user, (RevertServiceRequest) request);
						if (response == null || !(response instanceof RevertServiceResponse))
						{
							response = createErrorResponse(request.getUserId(), ErrorCode.GENERAL, "Could not revert the service back to its original state.");
						}
					}
					// // USER MANAGEMENT
					else if (request instanceof ReadUserDetailsRequest)
					{
						ReadUserDetailsRequest rur = (ReadUserDetailsRequest) request;

						List<UserDetails> userList = new ArrayList<>();

						// Read current user details / List of details
						List<IUser> users = null;
						if (rur.getUserToReadId() == null)
						{
							// Read all users
							users = security.getUsers();
						}
						else
						{
							// Read specific user
							if (request.getUserId().equals(rur.getUserToReadId()))
							{
								users = new ArrayList<>();
								users.add(user);
							}
							else
							{
								IUser usr = security.getUser(rur.getUserToReadId());
								if (usr != null)
								{
									users = new ArrayList<>();
									users.add(usr);
								}
							}

						}

						if (users != null)
						{
							for (IUser usr : users)
							{
								userList.add(userConfigUtils.getUserDetails(security, usr));
							}
						}

						// Set User list
						response = new ReadUserDetailsResponse(rur.getUserId(), rur.getSessionId());
						((ReadUserDetailsResponse) response).setUserDetails(userList.toArray(new UserDetails[userList.size()]));
					}
					else if (request instanceof UpdateMyDetailsRequest)
					{
						user.setInternalMobileNumber(((UpdateMyDetailsRequest) request).getMobile());
						user.setInternalName(((UpdateMyDetailsRequest) request).getName());
						if (security.updateInternalUserDetails(user))
							response = createConfirmationResponse(request.getUserId(), request.getSessionId(), "User details updated");
						else
							response = createErrorResponse(user.getUserId(), ErrorCode.GENERAL, "Update failed");
					}
					else if (request instanceof UpdateUserRequest)
					{
						// Create / Update User
						UpdateUserRequest uur = (UpdateUserRequest) request;

						if (uur.getUserDetails() == null)
						{
							response = createErrorResponse(user.getUserId(), ErrorCode.GENERAL, "Nothing to update");
						}
						else
						{
							try
							{
								for (UserDetails ud : uur.getUserDetails())
								{
									userConfigUtils.createUpdateUserDetails(security, ud);
								}
								response = createConfirmationResponse(request.getUserId(), request.getSessionId(), uur.getUserDetails().length + " users updated");
							}
							catch (Exception ex)
							{
								response = createErrorResponse(user.getUserId(), ErrorCode.GENERAL, ex.getMessage());
							}
						}
					}
					else if (request instanceof DeleteUserRequest)
					{
						// Delete user
						DeleteUserRequest dur = (DeleteUserRequest) request;
						List<String> deletedUsers = new ArrayList<String>();
						List<String> coundNotDelete = new ArrayList<String>();

						if (dur.getUsersToDeleteIds() == null)
						{
							response = createErrorResponse(user.getUserId(), ErrorCode.GENERAL, "Nothing to delete");
						}
						else
						{
							for (String uid : dur.getUsersToDeleteIds())
							{
								boolean deleted = userConfigUtils.deleteUsers(security, uid);
								if (deleted)
								{
									deletedUsers.add(uid);
								}
								else
								{
									coundNotDelete.add(uid);
								}
							}
						}
						response = new DeleteUserResponse(user.getUserId(), request.getSessionId());

						if (deletedUsers.size() > 0)
						{
							((DeleteUserResponse) response).setDeletedUserIds(deletedUsers.toArray(new String[deletedUsers.size()]));
						}
						if (coundNotDelete.size() > 0)
						{
							((DeleteUserResponse) response).setNotRemovedUserIds(coundNotDelete.toArray(new String[coundNotDelete.size()]));
						}
					}
					else if (request instanceof UpdateUserPasswordRequest)
					{
						UpdateUserPasswordRequest req = (UpdateUserPasswordRequest) request;
						boolean isUpdated = false;

						if (request.getUserId().equals(req.getUserToUpdateId()))
						{
							// Update own password
							user.setInternalPassword(req.getCredentials());
							isUpdated = security.updateInternalUserDetails(user);
						}
						else
						{
							// Update other users
							IUser usr = security.getUser(req.getUserToUpdateId());
							usr.setPassword(req.getCredentials());
							isUpdated = usr.update();
						}

						// Check that the password is updated
						// IUser authUser = security.authenticate(req.getUserToUpdateId(), req.getCredentials());

						if (!isUpdated)
						{
							createErrorResponse(user.getUserId(), ErrorCode.GENERAL, "Password not updated");
						}
						else
						{
							response = createConfirmationResponse(request.getUserId(), request.getSessionId(), "Password updated");
						}
					}
					// Security Role Management
					else if (request instanceof ReadSecurityRoleRequest)
					{
						// Extract Security Role information
						response = userConfigUtils.readSecurityRoleRequest((ReadSecurityRoleRequest) request, security, user);
					}
					else if (request instanceof UpdateSecurityRoleRequest)
					{
						// Create/Update roles
						UpdateSecurityRoleRequest req = (UpdateSecurityRoleRequest) request;
						if (req.getSecurityRoles() == null)
						{
							response = createErrorResponse(user.getUserId(), ErrorCode.GENERAL, "Nothing to update");
						}
						else
						{
							for (SecurityRole sr : req.getSecurityRoles())
							{
								userConfigUtils.createUpdateSecurityRole(security, sr);
							}
							response = createConfirmationResponse(request.getUserId(), request.getSessionId(), req.getSecurityRoles().length + " roles updated");
						}
					}
					else if (request instanceof DeleteSecurityRoleRequest)
					{
						// Delete Roles
						DeleteSecurityRoleRequest req = (DeleteSecurityRoleRequest) request;
						List<Integer> deletedRoles = new ArrayList<>();
						List<Integer> coundNotDelete = new ArrayList<>();

						if (req.getRolesIdToDelete() == null)
						{
							response = createErrorResponse(request.getUserId(), ErrorCode.GENERAL, "Nothing to delete");
						}
						else
						{
							for (int id : req.getRolesIdToDelete())
							{
								boolean deleted = userConfigUtils.deleteSecurityRole(security, id);
								if (deleted)
								{
									deletedRoles.add(id);
								}
								else
								{
									coundNotDelete.add(id);
								}
							}
						}

						response = new DeleteSecurityRolesResponse(request.getUserId(), request.getSessionId());

						if (deletedRoles.size() > 0)
						{
							((DeleteSecurityRolesResponse) response).setDeletedRoleIds(deletedRoles.toArray(new Integer[deletedRoles.size()]));
						}
						if (coundNotDelete.size() > 0)
						{
							((DeleteSecurityRolesResponse) response).setNotRemovedRoleIds(coundNotDelete.toArray(new Integer[coundNotDelete.size()]));
						}
					}
					else if (request instanceof ReadSecurityPermissionRequest)
					{
						// Return User Secuirity Permissions
						response = userConfigUtils.readSecurityPermissions((ReadSecurityPermissionRequest) request, security, user);
					}
					else if (request instanceof CtrlConfigurationRequest)
					{
						response = serviceConfigUtils.getControlServiceConfig(request.getUserId(), request.getSessionId(), user);
					}
					else if (request instanceof CtrlConfigurationUpdateRequest)
					{

						if (serviceConfigUtils.updateControlServiceConfiguration((CtrlConfigurationUpdateRequest) request, user))
						{
							response = createConfirmationResponse(request.getUserId(), request.getSessionId(), "Control Service configuration updated");
						}
						else
						{
							response = createErrorResponse(request.getUserId(), ErrorCode.SECURITY_CONSTRAINT, "No Control Service configuration found to update");
						}
					}
					else if (request instanceof CtrlConfigServerInfoUpdateRequest)
					{
						CtrlConfigServerInfoUpdateRequest siUpdateRequest = (CtrlConfigServerInfoUpdateRequest) request;
						if (serviceConfigUtils.updateServerInfo(siUpdateRequest.getServerInfoList(), user, siUpdateRequest.getVersionNumber(), siUpdateRequest.isPersistToDatabase()))
						{
							response = createConfirmationResponse(request.getUserId(), request.getSessionId(), "Control Service Host configuration updated");
						}
						else
						{
							response = createErrorResponse(request.getUserId(), ErrorCode.SECURITY_CONSTRAINT, "No Service Host configuration found to update");
						}
					}
					else if (request instanceof CtrlConfigServerRoleUpdateRequest)
					{
						CtrlConfigServerRoleUpdateRequest srUpdateRequest = (CtrlConfigServerRoleUpdateRequest) request;
						if (serviceConfigUtils.udpateServerRoles(srUpdateRequest.getServerRoleList(), user, srUpdateRequest.getVersionNumber(), srUpdateRequest.isPersistToDatabase()))
						{
							response = createConfirmationResponse(request.getUserId(), request.getSessionId(), "Control Service Role configuration updated");
						}
						else
						{
							response = createErrorResponse(request.getUserId(), ErrorCode.SECURITY_CONSTRAINT, "No Service Role configuration found to update");
						}
					}
					else if (request instanceof CtrlConfigServerRoleRequest)
					{
						response = serviceConfigUtils.getControlServiceRoles(request.getUserId(), request.getSessionId(), user);
					}
					else if (request instanceof SendSMSRequest)
					{
						response = serviceConfigUtils.sendSms((SendSMSRequest) request);
					}
					else if (request instanceof MetricsRequest)
					{
						MetricsRequest metricRequest = (MetricsRequest) request;
						switch (((MetricsRequest) request).getRequestType())
						{
							case AvailableMetrics:
								response = metricsMonitoring.discoverAvailableMetrics(user, request.getSessionId());
								break;
							case RegisterPluginMetricsMonitor:
								response = metricsMonitoring.registerMetric(metricRequest);
								break;
							default:
								response = createErrorResponse(request.getUserId(), ErrorCode.GENERAL, "Unknown option");
								break;
						}
					}
					else if (request instanceof MetricsDataRequest)
					{
						MetricsDataRequest metricRequest = (MetricsDataRequest) request;
						response = metricsMonitoring.getLatestMetricData(metricRequest);
					}
					else if (request instanceof GetAvailablePluginMetricsRequest)
					{
						response = bamController.GetAvailablePluginMetrics((GetAvailablePluginMetricsRequest) request);
					}
					else if (request instanceof RegisterMetricRequest)
					{
						response = bamController.RegisterMetric((RegisterMetricRequest) request);
					}
					else if (request instanceof UnregisterMetricRequest)
					{
						response = bamController.UnregisterMetric((UnregisterMetricRequest) request);
					}
					else if (request instanceof GetMetricRequest)
					{
						response = bamController.GetMetric((GetMetricRequest) request);
					}
					else if (request instanceof GetMetricsRequest)
					{
						response = bamController.GetMetrics((GetMetricsRequest) request);
					}
					else if (request instanceof GetUserLayoutRequest)
					{
						response = bamController.GetUserLayout((GetUserLayoutRequest) request);
					}
					else if (request instanceof SetUserLayoutRequest)
					{
						response = bamController.SetUserLayout((SetUserLayoutRequest) request);
					}
					else if (request instanceof ReturnCodeTextDefaultsRequest)
					{
						response = serviceConfigUtils.retrieveReturnCodeTextDefaults(user.getUserId(), request.getSessionId());
					}
					else if (request instanceof LogFileRequest)
					{
						LogFileRequest logRequest = (LogFileRequest) request;
						if (logRequest.getReadPosition() < 0)
							response = logFileController.filterLogFileDetails(logRequest);
						else
							response = logFileController.processLogFileTail(logRequest);
					}
					else if (request instanceof GetAvailableReports)
					{
						response = reportsController.getAvailableReports((GetAvailableReports) request);
					}
					else if (request instanceof GetReportparametersRequest)
					{
						response = reportsController.getReportParameters((GetReportparametersRequest) request, user);
					}
					else if (request instanceof GenerateReportRequest)
					{
						GenerateReportRequest genReport = (GenerateReportRequest) request;
						switch (genReport.getReportType())
						{
							case HTML:
								response = reportsController.generateHtmlReport(genReport);
								break;
							case EXCEL:
							case PDF:
								response = reportsController.generateBinaryReport(genReport);
								break;
						}

					}
					else if (request instanceof VasCommandsRequest)
					{
						// extractVasCommands
						response = serviceConfigUtils.extractVasCommands(user, (VasCommandsRequest) request);
					}
					else if (request instanceof AirSimCommonRequest)
					{
						AirSimCommonRequest airRequest = (AirSimCommonRequest) request;
						  
						switch(airRequest.getAirSimRequestType())
						{
							case ClearHistory:
								response = serviceConfigUtils.clearAirSimHistory(airRequest);
								break;
							case ClearEmailHistory:
								response = serviceConfigUtils.clearAirSimEmailHistory(airRequest);
								break;
						}
					}
					else if (request instanceof GetAirsimHistoryRequest)
					{
						response = serviceConfigUtils.getAirsimHistory((GetAirsimHistoryRequest) request);
					}
					else if (request instanceof AirSimGetUsageRequest)
					{
						response = serviceConfigUtils.airSimUsageRequest((AirSimGetUsageRequest)request);
					}
					else if (request instanceof AirSimStartUsageRequest)
					{
						response = serviceConfigUtils.airSimUsageStartRequest((AirSimStartUsageRequest)request);
					}
					else if (request instanceof AirSimStopUsageRequest)
					{
						response = serviceConfigUtils.airSimUsageStopRequest((AirSimStopUsageRequest)request);
					}
					else if (request instanceof AirSimUssdRequest)
					{
						response = serviceConfigUtils.airSimUssdRequest((AirSimUssdRequest)request);
					}
					else if (request instanceof AirSimSmsRequest)
					{
						response = serviceConfigUtils.airSimSMSRequest((AirSimSmsRequest)request);
					} else if( request instanceof AirResponseUpdateRequest)
					{
						response = serviceConfigUtils.injectAirResponseRequest((AirResponseUpdateRequest)request);
					} 
					else if( request instanceof AirResponseResetRequest)
					{
						response = serviceConfigUtils.resetInjectedAirResponseRequest((AirResponseResetRequest)request);
					} 
					else if (request instanceof GetEcdsTamperCheckRequest)
					{
						response = serviceConfigUtils.getEcdsTampering((GetEcdsTamperCheckRequest) request);
					} 
					else if (request instanceof GetEcdsTamperResetRequest)
					{
						response = serviceConfigUtils.resetEcdsTampering((GetEcdsTamperResetRequest) request);
					} else if (request instanceof GetEcdsCheckTamperedAgentRequest)
					{
						response = serviceConfigUtils.checkTamperedAgent((GetEcdsCheckTamperedAgentRequest) request);
					}
					
				}
				catch (ValidationException e)
				{
					logger.error(e.getMessage(), e);
					response = createErrorResponse(request.getUserId(), ErrorCode.SECURITY_CONSTRAINT, e.getMessage());
				}
				catch (SecurityException e)
				{
					logger.error("Cannot perform action due to security constraint", e);
					response = createErrorResponse(request.getUserId(), ErrorCode.SECURITY_CONSTRAINT, "Cannot perform action due to security constraint");
				}
				catch (Exception e)
				{
					logger.error("Error prevented request from being executed", e);
					response = createErrorResponse(request.getUserId(), ErrorCode.SECURITY_CONSTRAINT, "Error prevented request from being executed: " + e.getMessage());
				}
			}
		}

		if (response == null)
		{
			response = createErrorResponse(request.getUserId(), ErrorCode.GENERAL, "No method found for dealing with request");
		}
		return response;
	}

	@Override
	public IConnection getConnection(String optionalConnectionString) throws IOException
	{
		return null;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////
	// Protocol Methods
	// ///////////////////////////////////////////////////////////////////////////////////////////
	private UiBaseResponse extractPaths(UiBaseRequest request, IUser user)
	{
		ConfigurationPath pathTree = serviceConfigUtils.retrieveAllConfigurablePaths(user);
		UiBaseResponse response = new GetAllConfigurationPathsResponse(request.getUserId(), request.getSessionId());
		((GetAllConfigurationPathsResponse) response).setPathTree(pathTree);
		return response;
	}

	// ///////////////////////////////////////////////////////////////////////////////////////////
	// Helper Methods
	// ///////////////////////////////////////////////////////////////////////////////////////////
	private ErrorResponse createErrorResponse(String userId, ErrorCode errorCode, String message)
	{
		ErrorResponse response = new ErrorResponse(userId, errorCode);
		response.setError(message);
		return response;
	}

	private ConfirmationResponse createConfirmationResponse(String userId, String sessionId, String message)
	{
		ConfirmationResponse response = new ConfirmationResponse(userId, sessionId);
		response.setResponse(message);
		return response;
	}

	@Override
	public boolean canAssume(String serverRole)
	{
		return false;
	}

	@Override
	public boolean isFit()
	{
		if (server == null)
			return false;

		UIClient client = new UIClient();
		try
		{
			client.connect("127.0.0.1", config.serverPort);

			SystemInfoRequest request = new SystemInfoRequest(null, null);

			UiBaseResponse response = client.call(request, SystemInfoResponse.class);

			if (response == null || response instanceof ErrorResponse || !(response instanceof SystemInfoResponse))
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
		finally
		{
			client.close();
		}

		return true;
	}

	@Override
	public IMetric[] getMetrics()
	{
		return null;
	}

}
