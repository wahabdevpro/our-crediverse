package hxc.userinterfaces.gui.uiconnect;

import java.io.IOException;
import java.net.InetAddress;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import com.google.common.base.Throwables;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.configuration.ValidationException;
import hxc.services.logging.LoggingLevels;
import hxc.services.notification.IPhrase;
import hxc.services.notification.ITexts;
import hxc.services.notification.Phrase;
import hxc.services.notification.Texts;
import hxc.userinterfaces.gui.bam.BAMService;
import hxc.userinterfaces.gui.bam.BAMServices;
import hxc.userinterfaces.gui.data.PermissionCategory;
import hxc.userinterfaces.gui.data.PermissionView;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.jetty.CmdArgs;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.utils.calendar.DateRange;
import hxc.utils.calendar.DateRange.Periods;
import hxc.utils.calendar.DateTime;
import hxc.utils.protocol.uiconnector.airsim.AirResponseResetRequest;
import hxc.utils.protocol.uiconnector.airsim.AirResponseResetResponse;
import hxc.utils.protocol.uiconnector.airsim.AirResponseUpdateRequest;
import hxc.utils.protocol.uiconnector.airsim.AirResponseUpdateResponse;
import hxc.utils.protocol.uiconnector.airsim.AirSimMSISDNUsage;
import hxc.utils.protocol.uiconnector.airsim.AirSimMSISDNUsage.TimeUnits;
import hxc.utils.protocol.uiconnector.airsim.AirSimSmsRequest;
import hxc.utils.protocol.uiconnector.airsim.AirSimUssdRequest;
import hxc.utils.protocol.uiconnector.airsim.AirSimUssdResponse;
import hxc.utils.protocol.uiconnector.alarms.GetAlarmDataRequest;
import hxc.utils.protocol.uiconnector.alarms.GetAlarmDataResponse;
import hxc.utils.protocol.uiconnector.bam.GetAvailablePluginMetricsRequest;
import hxc.utils.protocol.uiconnector.bam.GetAvailablePluginMetricsResponse;
import hxc.utils.protocol.uiconnector.bam.GetMetricRequest;
import hxc.utils.protocol.uiconnector.bam.GetMetricResponse;
import hxc.utils.protocol.uiconnector.bam.GetMetricsRequest;
import hxc.utils.protocol.uiconnector.bam.GetMetricsResponse;
import hxc.utils.protocol.uiconnector.bam.GetUserLayoutRequest;
import hxc.utils.protocol.uiconnector.bam.GetUserLayoutResponse;
import hxc.utils.protocol.uiconnector.bam.RegisterMetricRequest;
import hxc.utils.protocol.uiconnector.bam.RegisterMetricResponse;
import hxc.utils.protocol.uiconnector.bam.SetUserLayoutRequest;
import hxc.utils.protocol.uiconnector.bam.SetUserLayoutResponse;
import hxc.utils.protocol.uiconnector.bam.UnregisterMetricRequest;
import hxc.utils.protocol.uiconnector.bam.UnregisterMetricResponse;
import hxc.utils.protocol.uiconnector.common.ConfigNotification;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.ConfigurationPath;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigServerInfoUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigServerRoleRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigServerRoleUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigurationRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.FitnessRequest;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigServerRoleResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigurationResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.FitnessResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;
import hxc.utils.protocol.uiconnector.logtailer.LogFileRequest;
import hxc.utils.protocol.uiconnector.logtailer.LogFileResponse;
import hxc.utils.protocol.uiconnector.logtailer.LogFilterOptions;
import hxc.utils.protocol.uiconnector.metrics.AvailableMetricsResponse;
import hxc.utils.protocol.uiconnector.metrics.MetricsDataRequest;
import hxc.utils.protocol.uiconnector.metrics.MetricsDataResponse;
import hxc.utils.protocol.uiconnector.metrics.MetricsRequest;
import hxc.utils.protocol.uiconnector.metrics.MetricsRequest.MetricsRequestType;
import hxc.utils.protocol.uiconnector.registration.Registration;
import hxc.utils.protocol.uiconnector.reports.BinaryReportResponse;
import hxc.utils.protocol.uiconnector.reports.GenerateReportRequest;
import hxc.utils.protocol.uiconnector.reports.GenerateReportRequest.ReportType;
import hxc.utils.protocol.uiconnector.reports.GetAvailableReports;
import hxc.utils.protocol.uiconnector.reports.GetAvailableReportsResponse;
import hxc.utils.protocol.uiconnector.reports.GetReportparametersRequest;
import hxc.utils.protocol.uiconnector.reports.GetReportparametersResponse;
import hxc.utils.protocol.uiconnector.reports.HtmlReportResponse;
import hxc.utils.protocol.uiconnector.request.AirSimCommonRequest;
import hxc.utils.protocol.uiconnector.request.AirSimCommonRequest.AirSimRequestType;
import hxc.utils.protocol.uiconnector.request.AirSimGetUsageRequest;
import hxc.utils.protocol.uiconnector.request.AirSimStartUsageRequest;
import hxc.utils.protocol.uiconnector.request.AirSimStopUsageRequest;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.CallConfigurableMethodRequest;
import hxc.utils.protocol.uiconnector.request.ConfigurableRequestParam;
import hxc.utils.protocol.uiconnector.request.ConfigurationUpdateRequest;
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
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.request.ReturnCodeTextDefaultsRequest;
import hxc.utils.protocol.uiconnector.request.ReturnCodeTextDefaultsResponse;
import hxc.utils.protocol.uiconnector.request.SessionTimeOutTimeRequest;
import hxc.utils.protocol.uiconnector.request.SystemInfoRequest;
import hxc.utils.protocol.uiconnector.request.UiBaseRequest.UiRequestCode;
import hxc.utils.protocol.uiconnector.request.ValidateSessionRequest;
import hxc.utils.protocol.uiconnector.response.AirSimGetUsageResponse;
import hxc.utils.protocol.uiconnector.response.AirSimStartUsageResponse;
import hxc.utils.protocol.uiconnector.response.AirSimStopUsageResponse;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.CallConfigurableMethodResponse;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.response.ConfigurationUpdateResponse;
import hxc.utils.protocol.uiconnector.response.ConfirmationResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.GetAirsimHistoryResponse;
import hxc.utils.protocol.uiconnector.response.GetAllConfigurablesResponse;
import hxc.utils.protocol.uiconnector.response.GetAllConfigurationPathsResponse;
import hxc.utils.protocol.uiconnector.response.GetConfigurableResponse;
import hxc.utils.protocol.uiconnector.response.GetEcdsCheckTamperedAgentResponse;
import hxc.utils.protocol.uiconnector.response.GetEcdsTamperCheckResponse;
import hxc.utils.protocol.uiconnector.response.GetEcdsTamperResetResponse;
import hxc.utils.protocol.uiconnector.response.GetFacilityResponse;
import hxc.utils.protocol.uiconnector.response.GetLicenseDetailsResponse;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.SessionTimeOutTimeResponse;
import hxc.utils.protocol.uiconnector.response.SystemInfoResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse.UiResponseCode;
import hxc.utils.protocol.uiconnector.userman.common.SecurityPermissionInfo;
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
import hxc.utils.protocol.uiconnector.userman.response.ReadSecurityPermissionesponse;
import hxc.utils.protocol.uiconnector.userman.response.ReadSecurityRolesResponse;
import hxc.utils.protocol.uiconnector.userman.response.ReadUserDetailsResponse;
import hxc.utils.protocol.uiconnector.userman.response.ValidSessionResponse;
import hxc.utils.protocol.uiconnector.vas.VasCommandsRequest;
import hxc.utils.protocol.uiconnector.vas.VasCommandsResponse;
import hxc.utils.uiconnector.client.UIClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiConnectionClient
{
	final static Logger logger = LoggerFactory.getLogger(UiConnectionClient.class);
	//public static int UI_CONNECTOR_PORT = 10101;
	//private static String SERVER = "localhost";

	private List<Configurable> configurables = null;

	private UiConnectionClient()
	{

	}

	public static UiConnectionClient getInstance()
	{
		return UiConnectionClientHolder.INSTANCE;
	}

	private static class UiConnectionClientHolder
	{
		private static final UiConnectionClient INSTANCE = new UiConnectionClient();
	}

	private UIClient getclientConnect() throws IOException
	{
		UIClient uic = new UIClient();
		
		try
		{
			uic.connect(CmdArgs.serverHost, CmdArgs.serverPort );
		}
		catch (IOException ioe)
		{
			try
			{
				uic.close();
				uic = null;
			}
			catch (Exception e)
			{
			}
			throw ioe;
		}

		return uic;
	}

	private UIClient getclientConnect(String host) throws IOException
	{
		UIClient uic = new UIClient();

		try
		{
			uic.connect(host, CmdArgs.serverPort);
		}
		catch (IOException ioe)
		{
			try
			{
				uic.close();
				uic = null;
			}
			catch (Exception e)
			{
			}
			throw ioe;
		}

		return uic;
	}

	public boolean log(LoggingLevels logLevel, String message)
	{
		try (UIClient uic = getclientConnect())
		{
			LogRequest lreq = new LogRequest("", "");
			lreq.setLoggingLevel(logLevel);
			lreq.setLogMessage(message);
			UiBaseResponse lresp = uic.call(lreq, ConfirmationResponse.class);
			if (lresp instanceof ConfirmationResponse)
				return true;
		}
		catch (Exception e)
		{
			// TODO: Backup logging?
		}
		return false;
	}

	public boolean log(LoggingLevels logLevel, Throwable throwable)
	{
		try (UIClient uic = getclientConnect())
		{
			LogRequest lreq = new LogRequest("", "");
			lreq.setLoggingLevel(logLevel);
			lreq.setLogMessage(Throwables.getStackTraceAsString(throwable));
			UiBaseResponse lresp = uic.call(lreq, ConfirmationResponse.class);
			if (lresp instanceof ConfirmationResponse)
				return true;
		}
		catch (Exception e)
		{
			// TODO: Backup logging?
		}
		return false;
	}

	public int getSessionTimeoutMinutes(User user) throws Exception
	{
		int result = -1;
		try (UIClient uic = getclientConnect())
		{
			SessionTimeOutTimeRequest str = new SessionTimeOutTimeRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse lresp = uic.call(str, ConfirmationResponse.class);
			if (lresp instanceof SessionTimeOutTimeResponse)
			{
				result = ((SessionTimeOutTimeResponse) lresp).getSessionTimeoutMinutes();
			}
		}
		return result;
	}

	public String credentialsLogin(UIClient uic, User user, String host) throws Exception
	{
		String sessionId = null;
		try
		{
			AuthenticateRequest auth = new AuthenticateRequest(user.getUserId());
			auth.setCredentials(user.getCredentials());
			auth.setRequestCode(UiRequestCode.AUTHENTICATE);
			UiBaseResponse authResp = null;
			try
			{
				authResp = uic.call(auth, AuthenticateResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				log(LoggingLevels.ERROR, String.format("Authentication error: %s", e.getMessage()));
				log(LoggingLevels.ERROR, e);
			}

			if (authResp.getResponseCode() == UiResponseCode.ERROR)
			{
				sessionId = null;
			}
			else
			{
				sessionId = authResp.getSessionId();
				if (user.getSessionIds() == null)
				{
					user.setSessionIds(new HashMap<String, String>());
				}
				user.getSessionIds().put(host, sessionId);
			}
		}
		catch (Exception e)
		{
		}

		if (sessionId == null)
		{
			log(LoggingLevels.ERROR, String.format("Authentication failure, Remote Login to %s Failed!", host));
			throw new Exception("Authentication failure");
		}
		return sessionId;
	}

	/**
	 * Login in using client Details are: JohnE / JohnEPwd
	 * 
	 * @return User
	 * @throws NoSuchAlgorithmException
	 */
	public User login(String userId, String pass) throws NoSuchAlgorithmException
	{

		User user = new User();

		try (UIClient uic = new UIClient())
		{
			try
			{
				uic.connect(CmdArgs.serverHost, CmdArgs.serverPort );
			}
			catch (IOException e1)
			{
				user.setLastLoginError("No connection could be made to authentication server");
				return user;
			}

			// Step 1: Get Key
			PublicKeyRequest pkr = new PublicKeyRequest(userId);
			UiBaseResponse br = null;
			PublicKeyResponse pr = null;
			byte[] publicKey = null;
			try
			{
				br = uic.call(pkr, UiBaseResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				user.setLastLoginError("User authentication key");
				// logger.error("Login failed - Cound not get key");
				log(LoggingLevels.ERROR, String.format("Authentication Key error: %s", e.getMessage()));
			}

			if (br instanceof ErrorResponse)
			{
				log(LoggingLevels.INFO, "User login failed: " + ((ErrorResponse) br).getError());
			}
			pr = (PublicKeyResponse) br;
			publicKey = pr.getPublicKey();
			if (pr == null || pr.getPublicKey().length == 0)
			{
				log(LoggingLevels.INFO, "User login failed:  No public key!");
			}

			// Step 2: Authenticate
			AuthenticateRequest auth = new AuthenticateRequest(userId);
			auth.generateSalted(publicKey, pass);
			byte[] credentials = auth.getCredentials();

			UiBaseResponse authResp = null;
			try
			{
				authResp = uic.call(auth, AuthenticateResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				log(LoggingLevels.ERROR, String.format("Authentication error: %s", e.getMessage()));
			}
			String sessionId = authResp.getSessionId();

			if (sessionId != null)
			{
				user = new User(userId, sessionId, ((AuthenticateResponse) authResp).getPermissionIds());
				user.setCredentials(credentials);
				user.setName(((AuthenticateResponse) authResp).getName());
			}
			else
			{
				user.setLastLoginError("User authentication failed");
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("login error: %s", e.getMessage()));
		}

		return user;
	}

	public List<Configurable> getAllConfigurable(User user) throws Exception
	{

		if (user == null)
			return null;

		if (configurables != null)
		{
			return configurables;
		}

		UIClient uic = new UIClient();

		try
		{
			try
			{
				uic.connect(CmdArgs.serverHost, CmdArgs.serverPort );
			}
			catch (IOException e1)
			{
				log(LoggingLevels.ERROR, String.format("Retrieve configurable error: %s", e1.getMessage()));
			}

			// Step 3: Extract configuration
			GetAllConfigurablesRequest getRequest = new GetAllConfigurablesRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse configResponse = null;
			try
			{
				configResponse = uic.call(getRequest, GetAllConfigurablesResponse.class);
			}
			catch (ClassNotFoundException | IOException e)
			{
				log(LoggingLevels.ERROR, String.format("Retrieve configurable error: %s", "Client request failed"));
				throw new Exception("Client request failed");
			}

			if (configResponse instanceof ErrorResponse)
			{
				ErrorResponse resp = (ErrorResponse) configResponse;
				throw new Exception(resp.getError());
			}
			else if (configResponse instanceof GetAllConfigurablesResponse)
			{
				GetAllConfigurablesResponse resp = (GetAllConfigurablesResponse) configResponse;
				configurables = resp.getConfigs();
				return configurables;
			}
		}
		finally
		{
			uic.close();
		}
		return null;
	}

	/*
	 * Returns JSON menu Items
	 */
	public String getServiceMenuItems(User user)
	{
		JsonObject job = new JsonObject();
		JsonArray jarr = new JsonArray();
		try
		{
			// Get Root Menu tree
			ConfigurationPath rootMenu = getConfigurableMenuTree(user);
			if (rootMenu.getChildren() != null)
			{
				for (ConfigurationPath path : rootMenu.getChildren())
				{
					String system = path.getName().replaceAll(" ", "_");
					JsonObject menuObj = new JsonObject();
					menuObj.add("sys", new JsonPrimitive(system));
					menuObj.add("name", new JsonPrimitive(path.getName()));
					jarr.add(menuObj);
				}
			}
			job.add("menu", jarr);
			job.add("status", new JsonPrimitive("pass"));
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Retrieve Service menu items error: %s", e.getMessage()));
			job.add("status", new JsonPrimitive("fail"));
			job.add("msg", new JsonPrimitive(e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return job.toString();
	}

	// ////////////////////// Configuration \\\\\\\\\\\\\\\\\\\\\\
	public Configurable extractConfigurationContent(User user, long UID) throws Exception
	{
		Configurable content = null;
		try (UIClient uic = getclientConnect())
		{
			GetConfigurableRequest request = new GetConfigurableRequest(user.getUserId(), user.getSessionId());
			request.setConfigurableSerialVersionID(UID);
			UiBaseResponse response = uic.call(request, UiBaseResponse.class);
			if (response instanceof ErrorResponse)
			{
				throw new Exception(((ErrorResponse) response).getError());
			}
			else
			{
				GetConfigurableResponse gcr = (GetConfigurableResponse) response;
				content = gcr.getConfig();
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Extract Configuration Content error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
			throw e;
		}
		return content;
	}

	public GetLocaleInformationResponse extractLocaleInformation(User user) throws Exception
	{
		GetLocaleInformationResponse localeResponse = null;
		try (UIClient uic = getclientConnect())
		{
			GetLocaleInformationRequest request = new GetLocaleInformationRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse response = uic.call(request, UiBaseResponse.class);
			if (response instanceof ErrorResponse)
			{
				throw new Exception(((ErrorResponse) response).getError());
			}
			else
			{
				localeResponse = (GetLocaleInformationResponse) response;
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Extract Locales information Content error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
			throw e;
		}
		return localeResponse;
	}

	// public void extarctNotificationContent(User user, long UID) {
	//
	// }

	public String buildContent(String system, User user)
	{
		// Build the page
		StringBuilder sb = new StringBuilder();

		try
		{
			// Get configuration
			List<Configurable> config = getAllConfigurable(user);

			for (Configurable comp : config)
			{
				if (comp.getName().equals(system))
				{
					sb.append("<form action='").append(comp.getName()).append("' method='post'>");
					sb.append("<h3>").append(comp.getName()).append("</h3></br/>");
					if (comp.getParams() == null || comp.getParams().length == 0)
					{
						sb.append("<p>Nothing to configure</p>");
					}
					else
					{
						for (IConfigurableParam prm : comp.getParams())
						{
							ConfigurableResponseParam param = (ConfigurableResponseParam) prm;
							try
							{
								String value = String.valueOf(param.getValue());
								sb.append("<label style='display:inline-block;color: #0000FF; width:200px;' for='next'>").append(GuiUtils.splitCamelCaseString(param.getFieldName()))
										.append("</label>");
								if (param.getRenderAs() != null && param.getRenderAs().equalsIgnoreCase("currency"))
								{
									value = GuiUtils.printCurrency(value);
									sb.append("<span style='width:50px; display:inline-block;'>USD</span>");
									sb.append("<input type='text' ").append(GuiUtils.isNumber(value) ? "style='text-align:right; width:150px'" : "").append("value='").append(value).append("' name='")
											.append(param.getFieldName()).append("'/><br/>");
								}
								else if (param.getPossibleValues() != null)
								{
									sb.append(GuiUtils.renderWithOptions(param.getFieldName(), value, param.getPossibleValues(), param.isReadOnly()));
									sb.append("</br>");
								}
								else
								{
									sb.append("<input type='text' ").append(param.isReadOnly() ? "readonly='true' " : "")
											.append(GuiUtils.isNumber(value) ? "style='width:200px; text-align:right;'" : "style='width:300px;'").append(" value='").append(value).append("' name='")
											.append(param.getFieldName()).append("'/><br/>");
								}
							}
							catch (Exception e)
							{
								log(LoggingLevels.ERROR, String.format("Build Content error: %s", e.getMessage()));
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e);
			return "HUGE Error: " + e.getLocalizedMessage();
		}
		return sb.toString();
	}

	// Management
	// Read permissions
	public SecurityPermissionInfo[] getSecurityPermissionList(User user)
	{
		SecurityPermissionInfo[] result = null;
		try (UIClient uic = getclientConnect())
		{
			ReadSecurityPermissionRequest pr = new ReadSecurityPermissionRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse baseResponse = uic.call(pr, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				log(LoggingLevels.INFO, "Could not retrieve role list");
			}
			else
			{
				ReadSecurityPermissionesponse response = (ReadSecurityPermissionesponse) baseResponse;
				result = response.getSecurityPermissionInfo();
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Security Permission error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return result;
	}

	public List<PermissionCategory> getSecurityPermissions(User user)
	{
		List<PermissionCategory> result = new ArrayList<>();
		Map<String, PermissionCategory> perms = new TreeMap<>();
		try (UIClient uic = getclientConnect())
		{
			ReadSecurityPermissionRequest pr = new ReadSecurityPermissionRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse baseResponse = uic.call(pr, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				// logger.error("Could not retrieve role list");
			}
			else
			{
				ReadSecurityPermissionesponse permResponse = (ReadSecurityPermissionesponse) baseResponse;
				Map<String, List<String>> implications = new HashMap<>();

				// Add all permissions
				for (SecurityPermissionInfo si : permResponse.getSecurityPermissionInfo())
				{
					if (!perms.containsKey(si.getCategory()))
					{
						PermissionCategory pc = new PermissionCategory(si.getCategory());
						perms.put(si.getCategory(), pc);
					}
					if (si.getImplies().length() > 0)
					{
						if (!implications.containsKey(si.getImplies()))
						{
							implications.put(si.getImplies(), new ArrayList<String>());
						}
						implications.get(si.getImplies()).add(si.getPermId());
					}
					perms.get(si.getCategory()).getPermissions().add(new PermissionView(si.getPermId(), si.getPath(), si.getDescription(), si.getImplies(), si.isAssignable()));
				}

				// Iterate and add is implied by
				for (String pc : perms.keySet())
				{
					PermissionCategory permCat = perms.get(pc);
					for (PermissionView pv : permCat.getPermissions())
					{
						if (implications.containsKey(pv.getPermId()))
						{
							Collections.sort(implications.get(pv.getPermId()));
							pv.setImpliedBy(implications.get(pv.getPermId()));
						}
					}
					result.add(perms.get(pc));
				}
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("getSecurityPermissions Permission error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return result;
	}

	// Read Roles
	public SecurityRole[] getSecurityList(User user)
	{
		SecurityRole[] result = null;
		try (UIClient uic = getclientConnect())
		{
			ReadSecurityRoleRequest sr = new ReadSecurityRoleRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse baseResponse = uic.call(sr, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				// logger.error("Could not retrieve role list");
			}
			else
			{
				ReadSecurityRolesResponse response = (ReadSecurityRolesResponse) baseResponse;
				result = response.getSecurityRole();
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("getSecurityList Permission error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return result;
	}

	// Create Roles
	public boolean addUpdateRole(User user, SecurityRole role) throws ValidationException, ClassNotFoundException, IOException
	{
		try (UIClient uic = getclientConnect())
		{
			UpdateSecurityRoleRequest updateRequest = new UpdateSecurityRoleRequest(user.getUserId(), user.getSessionId());
			updateRequest.addSingleSecurityRole(role);
			UiBaseResponse baseResponse = uic.call(updateRequest, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				ErrorResponse err = (ErrorResponse) baseResponse;
				throw new ValidationException(err.getError());
			}
		}
		return true;
	}

	// Delete roles
	public boolean deleteRole(User user, int roleId)
	{
		try (UIClient uic = getclientConnect())
		{
			DeleteSecurityRoleRequest delRequest = new DeleteSecurityRoleRequest(user.getUserId(), user.getSessionId());
			delRequest.assSingleRoleToDelete(roleId);
			UiBaseResponse baseResponse = uic.call(delRequest, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				// logger.error("Could not delete Role");
				return false;
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Delete Role Permission error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return true;
	}

	public SecurityRole getRole(User user, int roleId)
	{
		SecurityRole result = null;
		try (UIClient uic = getclientConnect())
		{
			ReadSecurityRoleRequest sr = new ReadSecurityRoleRequest(user.getUserId(), user.getSessionId());
			sr.setRoleId(roleId);
			UiBaseResponse baseResponse = uic.call(sr, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				// logger.error("Could not retrieve role list");
			}
			else
			{
				ReadSecurityRolesResponse response = (ReadSecurityRolesResponse) baseResponse;
				result = response.getSecurityRole()[0];
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Get Role error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return result;
	}

	// User Management
	// Read Users
	public UserDetails[] getUserList(User user)
	{
		UserDetails[] result = null;
		try (UIClient uic = getclientConnect())
		{
			ReadUserDetailsRequest ur = new ReadUserDetailsRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse baseResponse = uic.call(ur, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				log(LoggingLevels.ERROR, "Could not retrieve user list");
			}
			else
			{
				ReadUserDetailsResponse response = (ReadUserDetailsResponse) baseResponse;
				result = response.getUserDetails();
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Get User List error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return result;
	}

	public boolean updatePassword(User user, String userToUpdateId, String password)
	{

		try (UIClient uic = getclientConnect())
		{
			// Get the user password key
			PublicKeyRequest pkr = new PublicKeyRequest(userToUpdateId);
			UiBaseResponse resp = uic.call(pkr, UiBaseResponse.class);
			if (resp instanceof ErrorResponse)
			{
				return false;
			}

			// Use key to create password credentials
			PublicKeyResponse keyResponse = (PublicKeyResponse) resp;
			UpdateUserPasswordRequest updateRequest = new UpdateUserPasswordRequest(user.getUserId(), user.getSessionId());
			updateRequest.setUserToUpdateId(userToUpdateId);
			updateRequest.generateSalted(keyResponse.getPublicKey(), password);

			// Update password
			resp = uic.call(updateRequest, UiBaseResponse.class);

			boolean success = (resp instanceof ConfirmationResponse);

			return success;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Update password error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
			return false;
		}
	}

	public boolean updateMyDetails(User user, String name, String mobile)
	{
		UiBaseResponse response = null;
		try (UIClient uic = getclientConnect())
		{
			UpdateMyDetailsRequest updateMyDetailsRequest = new UpdateMyDetailsRequest(user.getUserId(), user.getSessionId());
			updateMyDetailsRequest.setMobile(mobile);
			updateMyDetailsRequest.setName(name);
			response = uic.call(updateMyDetailsRequest, UiBaseResponse.class);
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Update My Details error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
			return false;
		}

		return ((response != null) && (response instanceof ConfirmationResponse));

	}

	// Create User
	public boolean updateUser(User user, UserDetails userDetails) throws ValidationException
	{
		try (UIClient uic = getclientConnect())
		{
			UpdateUserRequest updateRequest = new UpdateUserRequest(user.getUserId(), user.getSessionId());
			updateRequest.addSingleUserDetail(userDetails);
			UiBaseResponse baseResponse = uic.call(updateRequest, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				throw new ValidationException(((ErrorResponse) baseResponse).getError());
			}
		}
		catch (ValidationException ve)
		{
			throw ve;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e);
			return false;
		}
		return true;
	}

	// Delete User(s)
	public boolean deleteUser(User user, String userIdToDelete)
	{
		try (UIClient uic = getclientConnect())
		{
			DeleteUserRequest delRequest = new DeleteUserRequest(user.getUserId(), user.getSessionId());
			delRequest.addSingleUserToDelete(userIdToDelete);
			UiBaseResponse baseResponse = uic.call(delRequest, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				// logger.error("Could not delete User");
				return false;
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Delete User error: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return true;
	}

	public UserDetails retrieveDetails(User user, String userId)
	{
		UserDetails result = null;
		try (UIClient uic = getclientConnect())
		{
			ReadUserDetailsRequest ur = new ReadUserDetailsRequest(user.getUserId(), user.getSessionId());
			ur.setUserToReadId(userId);
			UiBaseResponse baseResponse = uic.call(ur, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				log(LoggingLevels.ERROR, "Could not retrieve user details");
			}
			else
			{
				ReadUserDetailsResponse response = (ReadUserDetailsResponse) baseResponse;
				result = response.getUserDetails()[0];
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e);
			log(LoggingLevels.ERROR, String.format("Retrieve Details: %s", e.getMessage()));
		}
		return result;
	}

	public UserDetails getUser(User user, String userIdToRead)
	{
		UserDetails result = null;
		try (UIClient uic = getclientConnect())
		{
			ReadUserDetailsRequest ur = new ReadUserDetailsRequest(user.getUserId(), user.getSessionId());
			ur.setUserToReadId(userIdToRead);
			UiBaseResponse baseResponse = uic.call(ur, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				log(LoggingLevels.ERROR, "Could not retrieve user list");
			}
			else
			{
				ReadUserDetailsResponse response = (ReadUserDetailsResponse) baseResponse;
				result = response.getUserDetails()[0];
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e);
			log(LoggingLevels.ERROR, String.format("Retrieve Details: %s", e.getMessage()));
		}
		return result;
	}

	/**
	 * Obtain path for tree (2nd level will go into menu)
	 * 
	 * @param user
	 * @return
	 */
	public ConfigurationPath getConfigurableMenuTree(User user) throws Exception
	{
		ConfigurationPath result = null;
		GetAllConfigurationPathsRequest request = new GetAllConfigurationPathsRequest(user.getUserId(), user.getSessionId());

		try (UIClient uic = getclientConnect())
		{
			UiBaseResponse response = uic.call(request, UiBaseResponse.class);
			if (response instanceof ErrorResponse)
			{
				throw new Exception(((ErrorResponse) response).getError());
			}
			else
			{
				GetAllConfigurationPathsResponse gar = (GetAllConfigurationPathsResponse) response;
				result = gar.getPathTree();
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Get Configuration Tree: %s", e.getMessage()));
		}
		return result;
	}

	public Configurable saveConfigurationStructure(User user, long uid, int version, ConfigurableRequestParam[] parms, boolean saveToDB) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(user.getUserId(), user.getSessionId());
			configUpdateRequest.setConfigurableSerialVersionUID(uid);
			configUpdateRequest.setVersion(version);
			configUpdateRequest.setSaveToDB(saveToDB);
			configUpdateRequest.setParams(parms);

			UiBaseResponse baseResponse = uic.call(configUpdateRequest, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				ErrorResponse err = (ErrorResponse) baseResponse;
				throw ValidationException.createFieldValidationException(err.getField(), err.getError());
			}
			else if (baseResponse instanceof ConfigurationUpdateResponse)
			{
				ConfigurationUpdateResponse configResponse = (ConfigurationUpdateResponse) baseResponse;
				return (configResponse.getConfig());
			}
		}

		return null;
	}

	/*
	 * Currently This is specific for FileConnector (This needs to be made more generic)
	 */
	public Configurable saveConfigurationStructure(User user, long uid, int version, String fieldName, List<IConfigurableParam[]> fieldValue, boolean saveToDB) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(user.getUserId(), user.getSessionId());
			configUpdateRequest.setConfigurableSerialVersionUID(uid);
			configUpdateRequest.setVersion(version);
			configUpdateRequest.setSaveToDB(saveToDB);

			ConfigurableRequestParam[] parms = new ConfigurableRequestParam[1];
			parms[0] = new ConfigurableRequestParam(fieldName, fieldValue);
			configUpdateRequest.setParams(parms);

			UiBaseResponse baseResponse = uic.call(configUpdateRequest, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				ErrorResponse err = (ErrorResponse) baseResponse;
				throw ValidationException.createFieldValidationException(err.getField(), err.getError());
			}
			else if (baseResponse instanceof ConfigurationUpdateResponse)
			{
				ConfigurationUpdateResponse configResponse = (ConfigurationUpdateResponse) baseResponse;
				return (configResponse.getConfig());
			}
		}

		return null;
	}

	public Configurable saveConfigurationUnStructured(User user, long uid, int version, String fieldName, IConfigurableParam[] fieldValue, boolean saveToDB) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(user.getUserId(), user.getSessionId());
			configUpdateRequest.setConfigurableSerialVersionUID(uid);
			configUpdateRequest.setVersion(version);
			configUpdateRequest.setSaveToDB(saveToDB);

			ConfigurableRequestParam[] parms = new ConfigurableRequestParam[1];
			parms[0] = new ConfigurableRequestParam(fieldName, fieldValue);
			configUpdateRequest.setParams(parms);

			UiBaseResponse baseResponse = uic.call(configUpdateRequest, UiBaseResponse.class);
			if (baseResponse instanceof ErrorResponse)
			{
				ErrorResponse err = (ErrorResponse) baseResponse;
				throw ValidationException.createFieldValidationException(err.getField(), err.getError());
			}
			else if (baseResponse instanceof ConfigurationUpdateResponse)
			{
				ConfigurationUpdateResponse configResponse = (ConfigurationUpdateResponse) baseResponse;
				return (configResponse.getConfig());
			}
		}

		return null;
	}

	public static boolean isNumeric(String str)
	{
		for (char c : str.toCharArray())
		{
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}

	public Configurable saveConfiguration(User user, long uid, int version, Map<String, String[]> params) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(user.getUserId(), user.getSessionId());
			configUpdateRequest.setConfigurableSerialVersionUID(uid);
			configUpdateRequest.setVersion(version);

			List<ConfigurableRequestParam> paramList = new ArrayList<>();
			Map<String, Object> textPhrases = new HashMap<>();

			for (String prm : params.keySet())
			{
				if (!(prm.startsWith("config_") || prm.startsWith("RC_")))
				{
					// Check for Texts

					if (!prm.startsWith("not_") && (prm.indexOf("_") > 0) && (prm.indexOf("_") == prm.length() - 2))
					{
						String[] s = prm.split("_");
						if (!textPhrases.containsKey(s[0]))
						{
							textPhrases.put(s[0], new Texts());
						}
						try
						{
							((ITexts) textPhrases.get(s[0])).setText(Integer.parseInt(s[1]), params.get(prm)[0]);
						}
						catch (Exception e)
						{
						}
					}
					else if (!prm.startsWith("not_") && (prm.indexOf("_") > 0) && (prm.indexOf("_") == prm.length() - 4))
					{
						String[] s = prm.split("_");
						if (!textPhrases.containsKey(s[0]))
						{
							textPhrases.put(s[0], new Phrase());
						}
						try
						{
							((IPhrase) textPhrases.get(s[0])).set(s[1], params.get(prm)[0]);
						}
						catch (Exception e)
						{
						}
					}
					else
					{
						ConfigurableRequestParam configParam = new ConfigurableRequestParam(prm, params.get(prm)[0]);
						paramList.add(configParam);
					}
				}
			}

			// Saving of Texts / Phrases
			if (textPhrases.size() > 0)
			{
				for (String key : textPhrases.keySet())
				{
					ConfigurableRequestParam configParam = new ConfigurableRequestParam(key, textPhrases.get(key));
					paramList.add(configParam);
				}
			}

			configUpdateRequest.setParams(paramList.toArray(new ConfigurableRequestParam[paramList.size()]));

			UiBaseResponse baseResponse = uic.call(configUpdateRequest, UiBaseResponse.class);

			if (baseResponse instanceof ErrorResponse)
			{
				ErrorResponse err = (ErrorResponse) baseResponse;
				throw ValidationException.createFieldValidationException(err.getField(), err.getError());
			}
			else if (baseResponse instanceof ConfigurationUpdateResponse)
			{
				ConfigurationUpdateResponse configResponse = (ConfigurationUpdateResponse) baseResponse;
				return (configResponse.getConfig());
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Save configuration: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
			if (e instanceof ValidationException)
				throw e;
			else
				throw new Exception(e.getMessage());
		}
		return null;
	}

	public boolean validateSession(User user)
	{
		boolean valid = false;
		try (UIClient uic = getclientConnect())
		{
			// First lets fail!
			ValidateSessionRequest validateRequest = new ValidateSessionRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse response = uic.call(validateRequest, UiBaseResponse.class);
			valid = response instanceof ValidSessionResponse;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Validate Session: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return valid;
	}

	public Configurable saveNotifications(User user, long uid, int version, Map<String, String[]> params) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(user.getUserId(), user.getSessionId());
			configUpdateRequest.setConfigurableSerialVersionUID(uid);
			configUpdateRequest.setVersion(version);

			Map<Integer, NotificationTextHelper> nots = new HashMap<>();

			// Sort out page data
			for (String prm : params.keySet())
			{
				if (prm.startsWith("not_"))
				{
					// Notification in the form not_0_1 where 0 = id, 1 = index
					String[] ns = prm.split("_");
					int id = Integer.parseInt(ns[1]);
					int index = Integer.parseInt(ns[2]);

					NotificationTextHelper helper = nots.get(id);
					if (helper == null)
					{
						helper = new NotificationTextHelper();
						nots.put(id, helper);
					}
					String msgText = params.get(prm)[0];
					helper.addText(index, msgText);
				}
			}

			// Sort out request object
			ConfigNotification[] confNots = new ConfigNotification[nots.size()];
			int index = 0;
			for (Integer id : nots.keySet())
			{
				confNots[index] = new ConfigNotification(id, nots.get(id).buildArray());
				index++;
			}
			configUpdateRequest.setNotifications(confNots);

			// Update
			UiBaseResponse baseResponse = uic.call(configUpdateRequest, UiBaseResponse.class);

			if (baseResponse instanceof ErrorResponse)
			{
				throw new Exception(((ErrorResponse) baseResponse).getError());
			}
			else if (baseResponse instanceof ConfigurationUpdateResponse)
			{
				ConfigurationUpdateResponse configResponse = (ConfigurationUpdateResponse) baseResponse;
				return (configResponse.getConfig());
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Save notifications: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
			throw new Exception(e.getMessage());
		}
		return null;
	}

	public class NotificationTextHelper
	{
		int id;
		Map<Integer, String> textMap = new HashMap<Integer, String>();

		public void addText(int index, String text)
		{
			textMap.put(index, text);
		}

		public String[] buildArray()
		{
			int highest = 0;
			for (int i : textMap.keySet())
			{
				if (i > highest)
				{
					highest = i;
				}
			}
			String[] result = new String[highest + 1];
			for (int i = 0; i <= highest; i++)
			{
				String test = textMap.get(i);
				result[i] = (test == null) ? "" : test;
			}
			return result;
		}
	}

	public String executeConfigMethod(User user, long configUID, String methodName) throws Exception
	{
		CallConfigurableMethodRequest methodRequest = new CallConfigurableMethodRequest(user.getUserId(), user.getSessionId());

		methodRequest.setConfigUID(configUID);
		methodRequest.setMethod(methodName);

		UiBaseResponse response = null;
		try (UIClient uic = getclientConnect())
		{
			response = uic.call(methodRequest, UiBaseResponse.class);

			if (response instanceof ErrorResponse)
			{
				throw new Exception(((ErrorResponse) response).getError());
				// Handle Error
				// CLIError.raiseError(((ErrorResponse) response).getError());
			}
			else
			{
				CallConfigurableMethodResponse methodResponse = (CallConfigurableMethodResponse) response;
				// CLIOutput.println(methodResponse.getMethodCallResponse());
				return methodResponse.getMethodCallResponse();
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Execute config method: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
			throw e;
		}
	}

	public SystemInfoResponse systemInfoRequest()
	{
		try (UIClient uic = getclientConnect())
		{
			SystemInfoRequest request = new SystemInfoRequest();
			UiBaseResponse response = uic.call(request, SystemInfoResponse.class);
			return ((SystemInfoResponse) response);
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("System Info Request: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}

	public CtrlConfigurationResponse getControlServiceConfiguration(User user)
	{
		CtrlConfigurationResponse response = null;
		try (UIClient uic = getclientConnect())
		{
			CtrlConfigurationRequest creq = new CtrlConfigurationRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse cresp = uic.call(creq, CtrlConfigurationResponse.class);
			if (cresp instanceof CtrlConfigurationResponse)
			{
				response = (CtrlConfigurationResponse) cresp;
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Get Control Service Config: %s", e.getMessage()));
			log(LoggingLevels.ERROR, e);
		}
		return response;
	}

	public CtrlConfigServerRoleResponse getControlServiceRoleConfiguration(User user)
	{
		CtrlConfigServerRoleResponse response = null;
		try (UIClient uic = getclientConnect())
		{
			CtrlConfigServerRoleRequest creq = new CtrlConfigServerRoleRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse cresp = uic.call(creq, CtrlConfigurationResponse.class);
			if (cresp instanceof CtrlConfigServerRoleResponse)
			{
				response = (CtrlConfigServerRoleResponse) cresp;
			}
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, String.format("Get Control Service Config: %s", e.getMessage()));
		}
		return response;
	}

	/**
	 * Persist Server info (hosts list) to UI Connector
	 */
	public void updateServerHosts(User user, ServerInfo[] serverInfo, int version, boolean persist) throws ValidationException
	{
		try (UIClient uic = getclientConnect())
		{
			CtrlConfigServerInfoUpdateRequest infoUpdate = new CtrlConfigServerInfoUpdateRequest(user.getUserId(), user.getSessionId());
			infoUpdate.setVersionNumber(version);
			infoUpdate.setPersistToDatabase(persist);
			infoUpdate.setServerInfoList(serverInfo);
			UiBaseResponse iresp = uic.call(infoUpdate, UiBaseResponse.class);
			if (iresp instanceof ErrorResponse)
			{
				throw new ValidationException(((ErrorResponse) iresp).getError());
			}
		}
		catch (Exception e)
		{
			throw new ValidationException(e.getMessage());
		}
	}

	/**
	 * Persist Server roles to UI Connector
	 */
	public void updateServerRoles(User user, ServerRole[] serverRoles, int version, boolean persist) throws ValidationException
	{
		try (UIClient uic = getclientConnect())
		{
			CtrlConfigServerRoleUpdateRequest roleUpdate = new CtrlConfigServerRoleUpdateRequest(user.getUserId(), user.getSessionId());
			roleUpdate.setVersionNumber(version);
			roleUpdate.setPersistToDatabase(persist);
			roleUpdate.setServerRoleList(serverRoles);
			UiBaseResponse rresp = uic.call(roleUpdate, UiBaseResponse.class);
			if (rresp instanceof ErrorResponse)
			{
				throw new ValidationException(((ErrorResponse) rresp).getError());
			}
		}
		catch (Exception e)
		{
			throw new ValidationException(e.getMessage());
		}
	}

	public FitnessResponse getServerFitnessLevels(String server, User user) throws Exception
	{
		try (UIClient uic = getclientConnect(server))
		{
			FitnessRequest freq = new FitnessRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse fresp = uic.call(freq, FitnessResponse.class);
			if (fresp != null && fresp instanceof FitnessResponse)
			{
				return (FitnessResponse) fresp;
			}
			else if (fresp != null)
			{
				throw new Exception(((ErrorResponse) fresp).getError());
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		
		return new FitnessResponse(null, null);

	}

	// / BAM Registration
	public void loadUserBamMetrics(User user, HttpSession session) throws Exception
	{
		// TODO: This is a temporary (need to find a way to save the registration for the
		// user saved BAM

		boolean isRegistered = UiConnectionClient.getInstance().registerBamMonitor(user, 783078316L, "TPS");

		if (isRegistered)
		{
			BAMServices bam = new BAMServices();
			BAMService bs = new BAMService();
			bam.getServices().add(new BAMService(783078316L, "TPS"));
			session.setAttribute("BAM", bs);
		}
	}

	public AvailableMetricsResponse retrieveAvailableMetrics(User user) throws Exception
	{
		AvailableMetricsResponse result = null;
		try (UIClient uic = getclientConnect())
		{
			MetricsRequest regMonitorRequest = new MetricsRequest(user.getUserId(), user.getSessionId(), MetricsRequestType.AvailableMetrics);
			UiBaseResponse resp = uic.call(regMonitorRequest, AvailableMetricsResponse.class);
			if (resp instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) resp).getError());
			else
				result = (AvailableMetricsResponse) resp;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e);
		}

		return result;
	}

	public boolean registerBamMonitor(User user, long configurationUid, String metricName) throws Exception
	{
		boolean result;
		try (UIClient uic = getclientConnect())
		{
			MetricsRequest regMonitorRequest = new MetricsRequest(user.getUserId(), user.getSessionId(), MetricsRequestType.RegisterPluginMetricsMonitor);
			regMonitorRequest.setPluginUID(configurationUid);
			regMonitorRequest.setMetricName(metricName);
			UiBaseResponse resp = uic.call(regMonitorRequest, ConfirmationResponse.class);
			if (resp instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) resp).getError());
			else
				result = (resp instanceof ConfirmationResponse);
		}
		return result;
	}

	public MetricsDataResponse getLatestMetricsData(User user, long lastMillisecondResponse) throws Exception
	{
		MetricsDataResponse result = null;
		try (UIClient uic = getclientConnect())
		{
			MetricsDataRequest metricsDataRequest = new MetricsDataRequest(user.getUserId(), user.getSessionId());
			metricsDataRequest.setLastMillisecondResponse(lastMillisecondResponse);
			UiBaseResponse resp = uic.call(metricsDataRequest, MetricsDataResponse.class);
			if (resp instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) resp).getError());
			else
				result = (MetricsDataResponse) resp;
		}
		return result;
	}

	public ReturnCodeTextDefaultsResponse getDefaultResponseCodes(User user) throws Exception
	{
		ReturnCodeTextDefaultsResponse result = null;
		try (UIClient uic = getclientConnect())
		{
			ReturnCodeTextDefaultsRequest metRequest = new ReturnCodeTextDefaultsRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse resp = uic.call(metRequest, ReturnCodeTextDefaultsResponse.class);
			if (resp instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) resp).getError());
			else
				result = (ReturnCodeTextDefaultsResponse) resp;
		}
		return result;
	}

	public List<String> extractServerList(User user)
	{
		CtrlConfigurationResponse ctrlConfig = this.getControlServiceConfiguration(user);
		List<String> servers = new ArrayList<>();
		if (ctrlConfig.getServerList() != null)
		{
			ServerInfo[] sinfos = ctrlConfig.getServerList();
			GuiUtils.sortServerHosts(sinfos);
			for (ServerInfo server : sinfos)
			{
				servers.add(server.getServerHost());
			}
		}
		return servers;
	}

	/**
	 * Connect to "host" and retrieve logs
	 * 
	 * @param user
	 *            Gui User
	 * @param host
	 *            Server to connect to
	 * @param start
	 *            Start Date (Can be NULL)
	 * @param end
	 *            End Date (Can be NULL)
	 * @param severity
	 *            Severity Level (Will return this level and everything Higher)
	 * @param searchText
	 *            Text to look for (NULL | Empty will allow for checking everything)
	 * @param lastPosition
	 *            This is required when tailing
	 * @return
	 * @throws Exception
	 */
	public LogFileResponse retrieveLogFileInfo(User user, String host, LogFilterOptions filter, int readPostion) throws Exception
	{
		LogFileResponse result = null;
		boolean failedLoginRetry = false;
		int tryCount = 0;

		try (UIClient uic = getclientConnect(host))
		{
			// Check if this is the current host
			String thisHost = getCurrentHostName();
			String sessionId = null;

			do
			{
				if (host.equalsIgnoreCase(thisHost))
				{
					sessionId = user.getSessionId();
				}
				else if ((!host.equalsIgnoreCase(thisHost)) && (failedLoginRetry || user.getSessionIds() == null || user.getSessionIds().get(host) == null))
				{
					// Login
					sessionId = credentialsLogin(uic, user, host);
				}
				else
				{
					sessionId = user.getSessionIds().get(host);
				}

				if (sessionId != null)
				{
					LogFileRequest request = new LogFileRequest(user.getUserId(), sessionId);
					request.setFilter(filter);
					request.setReadPosition(readPostion);

					UiBaseResponse resp = uic.call(request, LogFileResponse.class);
					if (resp instanceof ErrorResponse)
						if (host.equalsIgnoreCase(thisHost) || failedLoginRetry)
							throw new Exception(((ErrorResponse) resp).getError());
						else
							failedLoginRetry = true; // Maybe login expired
					else
					{
						result = (LogFileResponse) resp;
						break;
					}
				}
				else
				{
					throw new Exception(String.format("Could not login to %s", host));
				}

			} while (tryCount++ < 1);
		}

		return result;
	}

	public LogFileResponse retrieveLogFileInfo(User user, String host, LogFilterOptions filter) throws Exception
	{
		return retrieveLogFileInfo(user, host, filter, -1);
	}

	public void getTailResponse(User user, Date fromDate, String filterText)
	{

	}

	public GetAvailableReportsResponse getAvailableReports(User user) throws Exception
	{
		GetAvailableReportsResponse result = null;
		try (UIClient uic = getclientConnect())
		{
			GetAvailableReports request = new GetAvailableReports(user.getUserId(), user.getSessionId());
			UiBaseResponse resp = uic.call(request, GetAvailableReportsResponse.class);
			if (resp instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) resp).getError());
			else
				result = (GetAvailableReportsResponse) resp;
		}
		return result;
	}

	public GetReportparametersResponse getReportParameters(User user, String reportName) throws Exception
	{
		GetReportparametersResponse result = null;
		try (UIClient uic = getclientConnect())
		{
			GetReportparametersRequest request = new GetReportparametersRequest(user.getUserId(), user.getSessionId());
			request.setName(reportName);
			UiBaseResponse resp = uic.call(request, GetReportparametersResponse.class);
			if (resp instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) resp).getError());
			else
				result = (GetReportparametersResponse) resp;
		}
		return result;
	}

	private GenerateReportRequest createBaseReportRequest(User user, String reportName, Map<String, String> params)
	{
		GetReportparametersResponse parmRequest = null;

		try
		{
			parmRequest = UiConnectionClient.getInstance().getReportParameters(user, reportName);
			List<IConfigurableParam> paramList = new ArrayList<>();

			if (parmRequest != null && parmRequest.getParams() != null)
			{
				for (int i = 0; i < parmRequest.getParams().length; i++)
				{
					String fieldName = parmRequest.getParams()[i].getFieldName();
					paramList.add(new ConfigurableRequestParam(fieldName, null));

					if (DateRange.class.getCanonicalName().indexOf(parmRequest.getParams()[i].getValueType()) > 0)
					{
						if (params.get(fieldName).equalsIgnoreCase("custom"))
						{
							try
							{
								SimpleDateFormat sdf = new SimpleDateFormat(GuiUtils.extractLocaleDateFormat());
								DateTime fromDate = null;
								DateTime toDate = null;

								try
								{
									fromDate = new DateTime(sdf.parse(params.get(fieldName + "_from"))).getDatePart();
								}
								catch (Exception e)
								{
									toDate = DateTime.getNow().getDatePart();
								}

								try
								{
									toDate = new DateTime(sdf.parse(params.get(fieldName + "_to"))).getDatePart().addHours(24);
								}
								catch (Exception e)
								{
									toDate = DateTime.getNow().getDatePart().addHours(24);
								}

								paramList.get(i).setValue(new DateRange(Periods.Custom, "", fromDate, toDate));
							}
							catch (Exception e)
							{
							}
						}
						else
						{
							paramList.get(i).setValue(DateRange.GetRange(Periods.valueOf(params.get(fieldName))));
						}
					}
					else
					{
						paramList.get(i).setValue(params.get(fieldName));
					}
				}
			}
			else
			{
				for (String prm : params.keySet())
				{
					ConfigurableRequestParam configParam = new ConfigurableRequestParam(prm, params.get(prm));
					paramList.add(configParam);
				}
			}

			GenerateReportRequest request = new GenerateReportRequest(user.getUserId(), user.getSessionId());
			request.setReportName(reportName);
			request.setFields(paramList);

			return request;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e);
			logger.error("", e);
		}

		return null;
	}

	public HtmlReportResponse generateHtmlReport(User user, String reportName, Map<String, String> params) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			GenerateReportRequest request = createBaseReportRequest(user, reportName, params);
			request.setReportType(ReportType.HTML);
			UiBaseResponse resp = uic.call(request, HtmlReportResponse.class);
			if (resp instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) resp).getError());
			else
				return (HtmlReportResponse) resp;
		}
	}

	public BinaryReportResponse generateBinaryReport(User user, String reportName, Map<String, String> params, ReportType reportType) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			GenerateReportRequest request = createBaseReportRequest(user, reportName, params);
			request.setReportType(reportType);
			UiBaseResponse resp = uic.call(request, HtmlReportResponse.class);
			if (resp instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) resp).getError());
			else
				return (BinaryReportResponse) resp;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e);
			throw e;
		}
	}

	public VasCommandsResponse extractVasCommands(User user, long configUID) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			VasCommandsRequest request = new VasCommandsRequest(user.getUserId(), user.getSessionId());
			request.setConfigurationUID(configUID);
			UiBaseResponse resp = uic.call(request, VasCommandsResponse.class);
			if (resp instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) resp).getError());
			else
				return (VasCommandsResponse) resp;
		}
	}

	public GetAlarmDataResponse getLatestAlarms(User user, String host) throws Exception //DONO: CHECK
	{
		try (UIClient uic = getclientConnect(host != null ? host : CmdArgs.serverHost))
		{
			GetAlarmDataRequest request = new GetAlarmDataRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse response = uic.call(request, GetAlarmDataResponse.class);
			if (response instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) response).getError());
			else
				return (GetAlarmDataResponse) response;
		}
	}

	public GetAvailablePluginMetricsResponse getAvailablePluginMetrics(User user) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			GetAvailablePluginMetricsRequest request = new GetAvailablePluginMetricsRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse response = uic.call(request, GetAvailablePluginMetricsResponse.class);
			if (response instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) response).getError());
			else
				return (GetAvailablePluginMetricsResponse) response;
		}
	}

	public RegisterMetricResponse registerMetric(User user, String uid, String metricName) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			RegisterMetricRequest request = new RegisterMetricRequest(user.getUserId(), user.getSessionId());
			request.setUid(uid);
			request.setMetricName(metricName);
			UiBaseResponse response = uic.call(request, RegisterMetricResponse.class);
			if (response instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) response).getError());
			else
				return (RegisterMetricResponse) response;
		}
	}

	public UnregisterMetricResponse unregisterMetric(User user, String uid, String metricName) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			UnregisterMetricRequest request = new UnregisterMetricRequest(user.getUserId(), user.getSessionId());
			request.setUid(uid);
			request.setMetricName(metricName);
			UiBaseResponse response = uic.call(request, UnregisterMetricResponse.class);
			if (response instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) response).getError());
			else
				return (UnregisterMetricResponse) response;
		}
	}

	public GetMetricResponse getMetric(User user, String uid, String metricName, boolean force) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			GetMetricRequest request = new GetMetricRequest(user.getUserId(), user.getSessionId());
			request.setUid(uid);
			request.setMetricName(metricName);
			request.setForce(force);
			UiBaseResponse response = uic.call(request, GetMetricResponse.class);
			if (response instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) response).getError());
			else
				return (GetMetricResponse) response;
		}
	}

	public GetMetricsResponse getMetrics(User user, String uids[], String metricNames[]) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			GetMetricsRequest request = new GetMetricsRequest(user.getUserId(), user.getSessionId());
			request.setUids(uids);
			request.setMetricNames(metricNames);
			UiBaseResponse response = uic.call(request, GetMetricsResponse.class);
			if (response instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) response).getError());
			else
				return (GetMetricsResponse) response;
		}
	}

	public GetUserLayoutResponse getUserLayout(User user) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			GetUserLayoutRequest request = new GetUserLayoutRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse response = uic.call(request, GetUserLayoutResponse.class);
			if (response instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) response).getError());
			else
				return (GetUserLayoutResponse) response;
		}
	}

	public SetUserLayoutResponse setUserLayout(User user, String layout) throws Exception
	{
		try (UIClient uic = getclientConnect())
		{
			SetUserLayoutRequest request = new SetUserLayoutRequest(user.getUserId(), user.getSessionId());
			request.setLayout(layout);
			UiBaseResponse response = uic.call(request, SetUserLayoutResponse.class);
			if (response instanceof ErrorResponse)
				throw new Exception(((ErrorResponse) response).getError());
			else
				return (SetUserLayoutResponse) response;
		}
	}

	public boolean hasFacility(User user, String facilityID)
	{
		try (UIClient uic = getclientConnect())
		{
			GetFacilityRequest request = new GetFacilityRequest(user.getUserId(), user.getSessionId());
			request.setFacilityID(facilityID);
			UiBaseResponse response = uic.call(request, GetFacilityResponse.class);
			if (response instanceof ErrorResponse)
				return false;
			else
				return ((GetFacilityResponse) response).getFacility() != null;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}

		return false;
	}

	public Registration getLicenseDetails(User user)
	{
		try (UIClient uic = getclientConnect())
		{
			GetLicenseDetailsRequest request = new GetLicenseDetailsRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse response = uic.call(request, GetLicenseDetailsResponse.class);
			if (response instanceof ErrorResponse)
				return null;
			else
				return ((GetLicenseDetailsResponse) response).getLicenseDetails();
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}

		return null;
	}

	public GetAirsimHistoryResponse getAirsimHistory(User user)
	{
		try (UIClient client = getclientConnect())
		{
			GetAirsimHistoryRequest request = new GetAirsimHistoryRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse response = client.call(request, GetAirsimHistoryResponse.class);
			if (response instanceof ErrorResponse)
				return null;
			else
				return (GetAirsimHistoryResponse) response;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
	public UiBaseResponse clearAirSimHistory(User user)
	{
		try (UIClient client = getclientConnect())
		{
			AirSimCommonRequest request = new AirSimCommonRequest(user.getUserId(), user.getSessionId());
			request.setAirSimRequestType(AirSimRequestType.ClearHistory);
			
			UiBaseResponse response = client.call(request, UiBaseResponse.class);
			return response;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
	public UiBaseResponse clearAirSimEmailHistory(User user)
	{
		try (UIClient client = getclientConnect())
		{
			AirSimCommonRequest request = new AirSimCommonRequest(user.getUserId(), user.getSessionId());
			request.setAirSimRequestType(AirSimRequestType.ClearEmailHistory);
			
			UiBaseResponse response = client.call(request, UiBaseResponse.class);
			return response;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}

	public String getCurrentHostName()
	{
		String host = null;
		try
		{
			InetAddress ip = InetAddress.getLocalHost();
			host = ip.getHostName();
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e);
		}
		return host;
	}
	
	public GetEcdsTamperCheckResponse getEcdsTamperCheck(User user, GetEcdsTamperCheckRequest.Entity entity)
	{
		try (UIClient client = getclientConnect())
		{
			GetEcdsTamperCheckRequest request = new GetEcdsTamperCheckRequest(user.getUserId(), user.getSessionId());
			request.setEntity(entity);
			UiBaseResponse response = client.call(request, GetEcdsTamperCheckResponse.class);
			if (response instanceof ErrorResponse)
				return null;
			else
				return (GetEcdsTamperCheckResponse) response;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
	public GetEcdsTamperResetResponse resetEcdsTamperedRecords(User user, GetEcdsTamperResetRequest.Entity entity)
	{
		try (UIClient client = getclientConnect())
		{
			GetEcdsTamperResetRequest request = new GetEcdsTamperResetRequest(user.getUserId(), user.getSessionId());
			request.setEntity(entity);
			UiBaseResponse response = client.call(request, GetEcdsTamperResetResponse.class);
			if (response instanceof ErrorResponse)
				return null;
			else
				return (GetEcdsTamperResetResponse) response;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
	public GetEcdsTamperResetResponse resetEcdsTamperedRecord(User user, GetEcdsTamperResetRequest.Entity entity, String msisdn)
	{
		try (UIClient client = getclientConnect())
		{
			GetEcdsTamperResetRequest request = new GetEcdsTamperResetRequest(user.getUserId(), user.getSessionId());
			request.setEntity(entity);
			request.setMsisdn(msisdn);
			UiBaseResponse response = client.call(request, GetEcdsTamperResetResponse.class);
			if (response instanceof ErrorResponse)
				return null;
			else
				return (GetEcdsTamperResetResponse) response;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
	public GetEcdsCheckTamperedAgentResponse checkTamperedAgent(User user, String msisdn)
	{
		try (UIClient client = getclientConnect())
		{
			GetEcdsCheckTamperedAgentRequest request = new GetEcdsCheckTamperedAgentRequest(user.getUserId(), user.getSessionId());
			request.setMsisdn(msisdn);
			UiBaseResponse response = client.call(request, GetEcdsCheckTamperedAgentResponse.class);
			if (response instanceof ErrorResponse)
				return null;
			else
				return (GetEcdsCheckTamperedAgentResponse) response;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
// ---------------------------- AirSim Calls ----------------------------------	
	public AirSimGetUsageResponse getAirSimUsage(User user)
	{
		try (UIClient client = getclientConnect())
		{
			AirSimGetUsageRequest request = new AirSimGetUsageRequest(user.getUserId(), user.getSessionId());
			UiBaseResponse response = client.call(request, AirSimGetUsageResponse.class);
			
			if (response instanceof ErrorResponse)
				return null;
			else
				return (AirSimGetUsageResponse) response;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
	public AirSimStartUsageResponse setAirSimStartUsage(User user, String msisdn, int account, long amount, long interval, TimeUnits timeUnit, int standardDeviation, Long topupValue)
	{
		try (UIClient client = getclientConnect())
		{
			AirSimStartUsageRequest request = new AirSimStartUsageRequest(user.getUserId(), user.getSessionId());
			AirSimMSISDNUsage msisdnUsage = new AirSimMSISDNUsage(msisdn, account, amount, interval, timeUnit, standardDeviation, topupValue);	
			request.setMsisdnUsage(msisdnUsage);
			
			UiBaseResponse response = client.call(request, AirSimStartUsageResponse.class);
			
			if (response instanceof ErrorResponse)
				return null;
			else
				return (AirSimStartUsageResponse) response;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
	public AirSimStopUsageResponse setAirSimStopUsage(User user, String msisdn)
	{
		try (UIClient client = getclientConnect())
		{
			AirSimStopUsageRequest request = new AirSimStopUsageRequest(user.getUserId(), user.getSessionId());
			request.setMsisdn(msisdn);
			
			UiBaseResponse response = client.call(request, AirSimGetUsageResponse.class);
			
			if (response instanceof ErrorResponse)
				return null;
			else
				return (AirSimStopUsageResponse) response;
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
	
	public UiBaseResponse sendUssdRequest(User user, String msisdn, String ussd, String imsi)
	{
		try (UIClient client = getclientConnect())
		{
			AirSimUssdRequest request = new AirSimUssdRequest(user.getUserId(), user.getSessionId());
			
			request.setMsisdn(msisdn);
			request.setUssd(ussd);
			request.setImsi(imsi);
			
			return client.call(request, AirSimUssdResponse.class);
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
	public UiBaseResponse sendSmsRequest(User user, String from, String to, String smsText)
	{
		try (UIClient client = getclientConnect())
		{
			AirSimSmsRequest request = new AirSimSmsRequest(user.getUserId(), user.getSessionId());
			
			request.setFrom(from);
			request.setTo(to);
			request.setText(smsText);
			
			return client.call(request, AirSimUssdResponse.class);
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
	
	public UiBaseResponse sendUpdateAirRequest(User user, String airCall, String responseCode, String delay)
	{
		try (UIClient client = getclientConnect())
		{
			AirResponseUpdateRequest request = new AirResponseUpdateRequest(user.getUserId(), user.getSessionId());
			request.setAirCall(airCall); 
			request.setResponseCode(responseCode);
			request.setDelay(delay);
			return client.call(request, AirResponseUpdateResponse.class);
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}

	public UiBaseResponse sendResetAirRequest(User user, String airCall)
	{
		try (UIClient client = getclientConnect())
		{
			AirResponseResetRequest request = new AirResponseResetRequest(user.getUserId(), user.getSessionId());
			request.setAirCall(airCall); 
			return client.call(request, AirResponseResetResponse.class);
		}
		catch (Exception e)
		{
			log(LoggingLevels.ERROR, e.toString());
			log(LoggingLevels.ERROR, e);
		}
		return null;
	}
}
