package hxc.userinterfaces.gui.controller.manage;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.configuration.ValidationException;
import hxc.processmodel.IProcess;
import hxc.servicebus.ReturnCodes;
import hxc.services.logging.LoggingLevels;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.userinterfaces.gui.controller.service.AirSimController;
import hxc.userinterfaces.gui.controller.service.ControlService;
import hxc.userinterfaces.gui.controller.service.FileConnectorConfig;
import hxc.userinterfaces.gui.controller.service.GroupSharedAccounts;
import hxc.userinterfaces.gui.controller.service.confighandlers.ACTServiceConfigHandler;
import hxc.userinterfaces.gui.controller.service.confighandlers.BaseServiceConfigHandler;
import hxc.userinterfaces.gui.controller.service.confighandlers.CallMeBackServiceServiceConfigHandler;
import hxc.userinterfaces.gui.controller.service.confighandlers.CameroonCreditSharingServiceConfigHandler;
import hxc.userinterfaces.gui.controller.service.confighandlers.CreditTransferServiceConfigHandler;
import hxc.userinterfaces.gui.controller.service.confighandlers.FriendsAndFamilyServiceServiceConfigHandler;
import hxc.userinterfaces.gui.controller.service.confighandlers.GroupSharedAccountHandler;
import hxc.userinterfaces.gui.controller.service.confighandlers.PccConnectorServiceConfigHandler;
import hxc.userinterfaces.gui.controller.service.confighandlers.PinServiceServiceConfigHandler;
import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;
import hxc.userinterfaces.gui.data.FileConfigValues;
import hxc.userinterfaces.gui.data.FileConnectorConfiguration;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.utils.protocol.uiconnector.common.ConfigNotification;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.ConfigurationPath;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigurationResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableMethod;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.response.GetAirsimHistoryResponse;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;

public class ServiceConfigurationLoader implements IThymeleafController
{
	final static Logger logger = LoggerFactory.getLogger(ServiceConfigurationLoader.class);
	private static final List<BaseServiceConfigHandler> serviceHandlers;
	private static final String LAST_MENU_CLICKED_SESSION = "lastmenuuid";	// Session variable to hold last [Map<Service, UID>] Clicked
	
	static 
	{
		// Add Service Handlers
		serviceHandlers = new ArrayList<>();
		
		serviceHandlers.add(new ACTServiceConfigHandler());
		serviceHandlers.add(new CameroonCreditSharingServiceConfigHandler());
		serviceHandlers.add(new CreditTransferServiceConfigHandler());
		serviceHandlers.add(new GroupSharedAccountHandler());
		serviceHandlers.add(new PccConnectorServiceConfigHandler());
		serviceHandlers.add(new PinServiceServiceConfigHandler());
		serviceHandlers.add(new CallMeBackServiceServiceConfigHandler());
		serviceHandlers.add(new FriendsAndFamilyServiceServiceConfigHandler());
	}
	
	// Need further restructuring
	private static final long CONTROL_SERVICE_ID = -1411687343402146265L;
	
//	private static final long CREDIT_SHARING_SERVICE_ID = 3851988109129936993L; // 3851988109129936993
	private static final long FILE_CONNECTOR_ID = -2276295713134351271L;
	private static final long AIRSIM_SERVICE_ID = 4155099346172906269L;
	private static final long ECDS_TAMPERCHECK_ID = 1253827320892618922L;

	// Common Configuration
	public static final String CURRENT_UID_VARIABLE = "curUID";

	// USSD Menu Components
	public static final String PROCESS_ACTION_VARIABLE = "processActions";
	public static final String PROCESS_ACTION_JSON_VARIABLE = "ussdprocessJson";
	public static final String PROCESS_ACTION_ACTION_VARIABLES = "actionVariables";
	public static final String PROCESS_ACTION_PROCESS_VARIABLE = "processProcess";
	public static final String PROCESS_USSD_VARIABLE = "ussdVariable";
	public static final String USSD_UPDATED = "ussdupdated";

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{

		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		HttpSession session = request.getSession(false);
		if (session == null)
		{
			session = request.getSession(true);
		}

		User user = (User) session.getAttribute("user");

		boolean inValidateSession = (user == null);

		if (inValidateSession)
		{
			session.setAttribute("user", null);
			response.sendRedirect("/");
			return;
		}

		String page = null;
		String content = (String) request.getParameter("content");

		if (content != null)
		{
			page = contentRequestSerletHandler(request, response, ctx, session, user, page, content);
		}
		else
		{
			updateRequestServletHandler(request, response, session, user);
		}

		if (page != null)
		{

			try
			{
				templateEngine.process(page, ctx, response.getWriter());
			}
			catch (Exception e)
			{
				throw e;
			}
		}
	}

	/**
	 * @param request
	 * @param response
	 * @param session
	 * @param user
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void updateRequestServletHandler(HttpServletRequest request, HttpServletResponse response, HttpSession session, User user) throws NumberFormatException, IOException
	{
		// Possible page update
		String component = request.getParameter("config_comp");
		String act = request.getParameter("config_act");
		String uid = request.getParameter("config_uid");
		String version = request.getParameter("config_ver");

		// For method calls
		String methodName = request.getParameter("config_method");

		if (component != null && act != null && act.equalsIgnoreCase("upd"))
		{
			long luid = extractConfigurationUID(uid);
			int iversion = Integer.parseInt(version);
			Configurable updated = null;
			if (component.equalsIgnoreCase("config"))
			{
				// Update Configuration
				int faultCause = -1;

				Map<String, String[]> parms = request.getParameterMap();

				try
				{

					///////// \\\\\\\\\\
					
					for(BaseServiceConfigHandler handler : serviceHandlers)
					{
						if (handler.getServiceID() == luid)
						{
							updated = handler.processUpdateRequestHandler(session, luid, iversion, parms, user);
							if (updated != null)
								iversion = updated.getVersion();
						}
					}
					
					///////// \\\\\\\\\\
					
					if (luid == CONTROL_SERVICE_ID)
					{
						// Must Update?
						boolean updateInfo = (boolean) session.getAttribute(ControlService.SERVER_INFO_UPDATED_VARIABLE);
						boolean updateRoles = (boolean) session.getAttribute(ControlService.SERVER_ROLES_UPDATED_VARIABLE);

						// Control Service ServerInfo
						if (updateInfo)
						{
							faultCause = 1;
							ServerInfo[] serverInfo = (ServerInfo[]) session.getAttribute(ControlService.SERVER_INFO_VARIABLE);
							UiConnectionClient.getInstance().updateServerHosts(user, serverInfo, iversion, false);
							session.setAttribute(ControlService.SERVER_INFO_UPDATED_VARIABLE, false);
						}

						// Control Service ServerRoles
						if (updateRoles)
						{
							faultCause = 2;
							ServerRole[] serverRoles = (ServerRole[]) session.getAttribute(ControlService.SERVER_ROLES_VARIABLE);
							UiConnectionClient.getInstance().updateServerRoles(user, serverRoles, iversion, false);
							session.setAttribute(ControlService.SERVER_ROLES_UPDATED_VARIABLE, false);
						}
					}
					else if (luid == FILE_CONNECTOR_ID)
					{
						ConfigurableResponseParam[] structure = (ConfigurableResponseParam[]) session.getAttribute(FileConnectorConfig.CONFIG_RECORD_STRUCTURE_VARIABLE);
						FileConnectorConfiguration[] fileConfigs = (FileConnectorConfiguration[]) session.getAttribute(FileConnectorConfig.CONFIG_RECORD_VARIABLE);
						List<IConfigurableParam[]> toSave = extractParametersToReplace(structure, fileConfigs);
						String fieldName = (String) session.getAttribute(FileConnectorConfig.CONFIG_RECORD_FIELD);
						updated = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, fieldName, toSave, true);
					}

					if (luid != FILE_CONNECTOR_ID)
					{
						// This will save the main configuration to database
						updated = UiConnectionClient.getInstance().saveConfiguration(user, luid, iversion, parms);
					}

					int newVersion = updated.getVersion();
					sendResponse(response, createSuccessfuleUpdateJSON(luid, newVersion));
				}
				catch (Exception e)
				{
					if (e instanceof ValidationException)
					{
						ValidationException ve = (ValidationException) e;
						sendResponse(response, createFailedUpdateJSON(luid, (ve.getField() == null) ? "" : ve.getField(), ve.getMessage()));
					}
					else
					{
						String errorMessage = e.getMessage();
						if (faultCause == 1 && errorMessage.indexOf("Invalid Arguments") == 0)
						{
							errorMessage = "Server host configuration requires at least one item";
						}
						else if (faultCause == 1 && errorMessage.equalsIgnoreCase("Non-Existent Peer Host '{0}'"))
						{
							errorMessage = "Server host configuration path incomplete";
						}
						else if (faultCause == 2 && errorMessage.equalsIgnoreCase("Invalid Arguments"))
						{
							errorMessage = "There needs to exist at least one server role";
						}

						// Error caused Configuration not to be saved
						sendResponse(response, createFailedUpdateJSON(luid, errorMessage));
					}

				}

			}
			else if (component.equalsIgnoreCase("msg"))
			{
				// Update notifications
				try
				{
					updated = UiConnectionClient.getInstance().saveNotifications(user, luid, iversion, request.getParameterMap());
					sendResponse(response, createSuccessfuleUpdateJSON(luid, updated.getVersion()));
				}
				catch (Exception e)
				{
					// Error caused notifications not to be saved
					sendResponse(response, createFailedUpdateJSON(luid, e.getMessage()));
					// sendResponse(response, e.getMessage());
				}
			}
		}
		else if (methodName != null)
		{
			// Set a method call
			try
			{
				String message = UiConnectionClient.getInstance().executeConfigMethod(user, Long.valueOf(uid), methodName);
				sendResponse(response, createMessageResponse(uid, message, true));
			}
			catch (Exception e)
			{
				sendResponse(response, createMessageResponse(uid, e.getMessage(), false));
			}
			return;
		}
	}

	private void setLastSettingUIDClicked(HttpSession session, String menu, String uid)
	{
		try 
		{
			Map<String, String> lastUidMap = (Map<String, String>) session.getAttribute("lastmenuuid");	
			if (lastUidMap == null)
				lastUidMap = new HashMap<String, String>();
			if (menu != null)
			{
				lastUidMap.put(menu, uid);
				session.setAttribute(LAST_MENU_CLICKED_SESSION, lastUidMap);
			}
		} 
		catch(Exception e)
		{
			logger.error("", e);
		}
	}
	
	private String getLastSettingUIDClicked(HttpSession session, String service)
	{
		try
		{
			Map<String,String> lastUidMap = (Map<String, String>) session.getAttribute(LAST_MENU_CLICKED_SESSION);
			if (lastUidMap != null && lastUidMap.containsKey(service))
				return lastUidMap.get(service);
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		
		return null;
	}
	
	private long extractConfigurationUID(String fullUid)
	{
		if (fullUid.indexOf('_') > 0)
		{
			String [] uids = fullUid.split("_");
			return Long.parseLong(uids[uids.length-1]);
		}
		else
		{
			return Long.parseLong(fullUid);
		}
	}
	private String contentRequestSerletHandler(HttpServletRequest request, HttpServletResponse response, WebContext ctx, HttpSession session, User user, String page, String content) throws Exception,
			IOException
	{
		if (content.equals("menu"))
		{
			// Display menu content
			String loadmenu = request.getParameter("menu");
			ConfigurationPath configPath = UiConnectionClient.getInstance().getConfigurableMenuTree(user);
			String service = loadmenu.replaceAll("_", " ");
			session.setAttribute("lastMenu", service);

			// Last used Menu?
			String lastUid = getLastSettingUIDClicked(session, service);
			
			// New Menu
			JsonObject nav = buildJsonNavigationMenu(configPath, service, lastUid);
			sendResponse(response, nav.toString());
		}
		else if (content.equals("config"))
		{

			// Display configuration
			String uid = request.getParameter("uid");
			if (uid != null)
			{
				long UID = extractConfigurationUID(uid);
				
				session.setAttribute(CURRENT_UID_VARIABLE, UID);
				Configurable config = null;
				try
				{
					// First wipe
					session.setAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_STRUCT_LIST, null);
					session.setAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_UNSTRUCT_LIST, null);

					config = UiConnectionClient.getInstance().extractConfigurationContent(user, UID);
					String lastMenu = (String)session.getAttribute("lastMenu");
					setLastSettingUIDClicked(session, lastMenu, uid);
				}
				catch (Exception e)
				{
					UiConnectionClient.getInstance().log(LoggingLevels.FATAL, String.format("Could not read config %d: %s", UID, e.toString()));
				}

				ctx.setVariable("no_config", config == null);

				ConfigurableResponseParam[] fa = null;

				if (config != null)
				{
					fa = (ConfigurableResponseParam[]) config.getParams();
					List<ConfigurableResponseParam> fields = Arrays.asList(fa);

					// Check if there is anything on this form to update
					boolean canSaveUpdatedFields = false;
					if (fields != null)
					{
						for (ConfigurableResponseParam field : fields)
						{
							if (!field.isReadOnly())
							{
								canSaveUpdatedFields = true;
								break;
							}
						}
					}

					ConfigurableMethod[] ma = (ConfigurableMethod[]) config.getMethods();
					List<ConfigurableMethod> methods = new ArrayList<>();
					if (ma != null)
					{
						methods = Arrays.asList(ma);
					}

					ctx.setVariable("utils", new GuiUtils());
					ctx.setVariable("fields", fields);
					ctx.setVariable("service_name", config.getName());
					ctx.setVariable("canSaveUpdatedFields", canSaveUpdatedFields);
					ctx.setVariable("methods", methods);

					// Extra data for saving to go smoothly
					ctx.setVariable("service_version", config.getVersion());
				}
				ctx.setVariable("service_uid", uid);

				
				// Find Service Handler for incoming request and return template to load
				if (config != null)
				{
					for(BaseServiceConfigHandler handler : serviceHandlers)
					{
						if (handler.getServiceID() == config.getConfigSerialVersionUID())
						{
							return handler.processContentRequestHandler(session, ctx, config, user);
						}
					}
				}

				// if ((config != null) && (config.getName().equalsIgnoreCase("Control Connector")))
				if ((config != null) && (config.getConfigSerialVersionUID() == CONTROL_SERVICE_ID))
				{
					CtrlConfigurationResponse ctrlConfig = UiConnectionClient.getInstance().getControlServiceConfiguration(user);

					// Load further details
					ServerInfo[] sinfos = ctrlConfig.getServerList();
					GuiUtils.sortServerHosts(sinfos);
					ctx.setVariable(ControlService.SERVER_INFO_VARIABLE, sinfos);

					ServerRole[] sroles = ctrlConfig.getServerRoleList();
					GuiUtils.sortServerRoles(sroles);
					ctx.setVariable(ControlService.SERVER_ROLES_VARIABLE, sroles);

					// Needed in session for page updates
					session.setAttribute(ControlService.SERVER_INFO_VARIABLE, ctrlConfig.getServerList());
					session.setAttribute(ControlService.SERVER_ROLES_VARIABLE, ctrlConfig.getServerRoleList());

					session.setAttribute(ControlService.SERVER_INFO_UPDATED_VARIABLE, false);
					session.setAttribute(ControlService.SERVER_ROLES_UPDATED_VARIABLE, false);

					ctx.setVariable("utils", new GuiUtils());

					page = "controlService";
				}
				else if ((config != null) && (config.getConfigSerialVersionUID() == AIRSIM_SERVICE_ID))
				{
					// Get the airsim history
					GetAirsimHistoryResponse airsim = UiConnectionClient.getInstance().getAirsimHistory(user);

					// Check airsim is valid
					if (airsim != null)
					{
						ctx.setVariable("smsHistory", airsim.getSmsHistory());
						ctx.setVariable("cdrHistory", airsim.getCdrHistory());
						ctx.setVariable("emailHistory", airsim.getEmailHistory());
					}

					// Air MSISDN Usage
					AirSimController.getAirTimerUsage(ctx, user);
					
					// Set the gui utils
					ctx.setVariable("utils", new GuiUtils());

					// Set the page
					page = "airsim/airsimService";
				}
				else if ((config != null) && (config.getConfigSerialVersionUID() == FILE_CONNECTOR_ID))
				{
					// File Connector configuration
					ConfigurableResponseParam conParm = null;

					for (int i = 0; i < config.getParams().length; i++)
					{
						IConfigurableParam param = config.getParams()[i];
						conParm = (ConfigurableResponseParam) param;
						if (conParm.getStructure() != null)
						{
							session.setAttribute(FileConnectorConfig.CONFIG_RECORD_FIELD, conParm.getFieldName());

							// CONFIG RECORD ALLOWED VALUES
							FileConfigValues fileConfigValues = new FileConfigValues();
							for (ConfigurableResponseParam crp : conParm.getStructure())
							{
								fileConfigValues.importType(crp.getFieldName(), crp.getPossibleValues());
							}
							ctx.setVariable(FileConnectorConfig.CONFIG_RECORD_OPTIONS, fileConfigValues);

							// List<IConfigurableParam []> list = (List<IConfigurableParam []>) conParm.getValue();
							List<FileConnectorConfiguration> fileConfigs = new ArrayList<FileConnectorConfiguration>();
							for (IConfigurableParam[] propGroup : (List<IConfigurableParam[]>) conParm.getValue())
							{
								FileConnectorConfiguration fileConfig = new FileConnectorConfiguration();
								try
								{
									for (IConfigurableParam parm : propGroup)
									{
										fileConfig.importField(parm.getFieldName(), parm.getValue(), false);
									}
									fileConfigs.add(fileConfig);
								}
								catch (Exception exp)
								{
								}

							}
							FileConnectorConfiguration[] fileConfigurationsList = fileConfigs.toArray(new FileConnectorConfiguration[fileConfigs.size()]);
							ctx.setVariable(FileConnectorConfig.CONFIG_RECORD_VARIABLE, fileConfigurationsList);
							session.setAttribute(FileConnectorConfig.CONFIG_RECORD_VARIABLE, fileConfigurationsList);
							session.setAttribute(FileConnectorConfig.CONFIG_RECORD_STRUCTURE_VARIABLE, conParm.getStructure());

							break;
						}
					}
					page = "fileconnector/fileconnector";
				} else if((config != null) && (config.getConfigSerialVersionUID() == ECDS_TAMPERCHECK_ID)) {
					ctx.setVariable("utils", new GuiUtils());
					// Set the page
					page = "ecds/tampercheck";
				}
				else
					page = "serviceConfigContent";
			}
		}
		else if (content.equals("msg"))
		{
			// Display notifications
			String uid = request.getParameter("uid");
			if (uid != null)
			{
				// Notification information
				long UID = Long.parseLong(uid);

				Configurable config = null;
				try
				{
					config = UiConnectionClient.getInstance().extractConfigurationContent(user, UID);
				}
				catch (Exception e)
				{
				}
				ctx.setVariable("no_config", (config == null));
				if (config != null)
				{
					List<ConfigNotification> notifications = config.getNotifications();
					int count = (notifications == null) ? 0 : notifications.size();
					if (count == 0)
					{
						sendResponse(response, "<span style='margin-left: 20px'>No Notifications available</span>");
					}
					else
					{
						ctx.setVariable("notifications", notifications);

						// Locale info
						GetLocaleInformationResponse locale = UiConnectionClient.getInstance().extractLocaleInformation(user);
						ArrayList<String> languages = new ArrayList<>();
						ArrayList<String> languageDirections = new ArrayList<String>();
						for (int i = 0; i < locale.getLanguages().size(); i++)
						{
							languages.add(i, GuiUtils.convertToLanguage(locale.getLanguage(i)));
							languageDirections.add(i, GuiUtils.extractLanguageInfo(locale.getLanguage(i)).isLangRTL() ? "rtl" : "ltr");
						}

						ctx.setVariable("locale", languages);
						ctx.setVariable("directions", languageDirections);
						ctx.setVariable("service_uid", uid);
						ctx.setVariable("service_version", config.getVersion());
						ctx.setVariable("service_name", config.getName());
						ctx.setVariable("notification_variables", config.getVariables());
					}
				}
				ctx.setVariable("utils", new GuiUtils());
				page = "msgConfigContent";
			}
		}
		return page;
	}

	private List<IConfigurableParam[]> extractParametersToReplace(ConfigurableResponseParam[] structure, FileConnectorConfiguration[] fileConfigs)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		List<IConfigurableParam[]> result = new ArrayList<>();
		// FileConnectorConfiguration [] fileConfig = (FileConnectorConfiguration []) session.getAttribute(CONFIG_RECORD_VARIABLE);
		int index = 1;
		for (FileConnectorConfiguration fc : fileConfigs)
		{
			fc.setSequence(index++);
			List<IConfigurableParam> parms = new ArrayList<>();

			for (ConfigurableResponseParam struct : structure)
			{
				try
				{
					String name = struct.getFieldName().substring(0, 1).toLowerCase() + struct.getFieldName().substring(1);
					Field field = fc.getClass().getDeclaredField(name);
					field.setAccessible(true);
					if (field != null)
					{
						IConfigurableParam parm = new BasicConfigurableParm();
						parm.setFieldName(struct.getFieldName());
						try
						{
							if (struct.getValueType().equals(Date.class.getName()))
							{
								Date d = sdf.parse((String) field.get(fc));
								parm.setValue(d);
							}
							else
							{
								parm.setValue(field.get(fc));
							}
						}
						catch (Exception e)
						{
						}
						parms.add(parm);
					}
				}
				catch (Exception e)
				{
				}
			}
			IConfigurableParam[] parmArr = parms.toArray(new IConfigurableParam[parms.size()]);
			// Field field = this.getClass().getDeclaredField(name);
			result.add(parmArr);
		}
		return result;
	}

	private String createSuccessfuleUpdateJSON(long uid, long version)
	{
		JsonObject job = new JsonObject();
		job.add("update", new JsonPrimitive("success"));
		job.add("uid", new JsonPrimitive(uid));
		job.add("version", new JsonPrimitive(version));
		return job.toString();
	}

	private String createFailedUpdateJSON(long uid, String message)
	{
		JsonObject job = new JsonObject();
		job.add("update", new JsonPrimitive("fail"));
		job.add("uid", new JsonPrimitive(uid));
		job.add("error", new JsonPrimitive(message));
		return job.toString();
	}

	private String createFailedUpdateJSON(long uid, String field, String message)
	{
		JsonObject job = new JsonObject();
		job.add("update", new JsonPrimitive("fail"));
		job.add("uid", new JsonPrimitive(uid));
		job.add("field", new JsonPrimitive(field));
		job.add("error", new JsonPrimitive(message));
		return job.toString();
	}

	private String createMessageResponse(String uid, String message, boolean success)
	{
		JsonObject job = new JsonObject();
		job.add("update", new JsonPrimitive(success ? "success" : "fail"));
		job.add("uid", new JsonPrimitive(uid));
		job.add("message", new JsonPrimitive(message));
		return job.toString();
	}

	// -------------------------------------------------------------------------------------------------------------------------

	private JsonObject createBaseNavItem(String name, long uid, JsonArray children)
	{
		JsonObject job = new JsonObject();
		job.add("name", new JsonPrimitive(name));
		job.add("uid", new JsonPrimitive(String.valueOf(uid)));
		job.add("child", children);
		return job;
	}

	private JsonObject createJsonMenu(ConfigurationPath menuPath)
	{
		String name = menuPath.getName().replaceAll("_", " ");

		JsonArray jarr = new JsonArray();
		if (menuPath.getChildren() != null && menuPath.getChildren().size() > 0)
		{
			for (ConfigurationPath cp : menuPath.getChildren())
			{
				jarr.add(createJsonMenu(cp));
			}
		}
		JsonObject nav = createBaseNavItem(name, menuPath.getConfigSerialVersionUID(), jarr);
		return nav;
	}

	private LinkedHashSet<Long> extractUids(ConfigurationPath menuPath)
	{
		LinkedHashSet<Long> result = new LinkedHashSet<>();
		if (menuPath.getChildren() != null && menuPath.getChildren().size() > 0)
		{
			for (ConfigurationPath cp : menuPath.getChildren())
			{
				result.addAll(extractUids(cp));
			}
		}

		if (menuPath.getConfigSerialVersionUID() != 0)
		{
			result.add(menuPath.getConfigSerialVersionUID());
		}

		return result;
	}

	/**
	 * @param menuPath
	 * @param service
	 * @param lastUid	Note that this could be a 2 part id, i.e. SUBMENU_MAIN
	 * @return
	 */
	private JsonObject buildJsonNavigationMenu(ConfigurationPath menuPath, String service, String lastUid)
	{

		JsonArray jarr = new JsonArray();
		String firstName = "";
		Long firstUID = 0L;
		
		Set<Long> uids = new LinkedHashSet<Long>();

		if (menuPath.getChildren() != null)
		{
			for (ConfigurationPath configPath : menuPath.getChildren())
			{
				if (configPath.getName().equals(service))
				{
					for (ConfigurationPath path : configPath.getChildren())
					{
						jarr.add(createJsonMenu(path));
						Set<Long> uidSet = extractUids(path);
						uids.addAll(uidSet);
						
						if ((firstUID == 0 || (path.getName().compareToIgnoreCase(firstName) < 0)) && (uids.size() > 0))
						{
							firstUID = path.getConfigSerialVersionUID();
							firstName = path.getName();
						}
					}
					break;
				}
			}
		}

		JsonObject nav = new JsonObject();
		nav.add("system", new JsonPrimitive(service));
		try
		{
			if (lastUid == null)
				lastUid = String.valueOf(firstUID);
			
//			if (!uids.contains(uid))
//				uid = firstUID;
		}
		catch (Exception e)
		{
		}
		nav.add("lastuid", new JsonPrimitive(lastUid));
		nav.add("menu", jarr);

		return nav;
	}

	public void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}

	// ------------------------------

	// TODO: Remove since "moved to Base ServiceConfigHandler
	private Configurable persistReturnCodeConfiguration(User user, long luid, int iversion, Map<String, String[]> parms, String returnCodeFieldName) throws Exception
	{
		Configurable updated = null;

		// First Update ResultCodeText
		Map<String, ReturnCodeTexts> rtcMap = null;
		String fieldName = null;
		String fieldStartsWith = String.format("RC_%s", returnCodeFieldName);
		for (String prm : parms.keySet())
		{
			if (prm.startsWith(fieldStartsWith))
			{
				if (rtcMap == null)
					rtcMap = new TreeMap<>();
				String[] arr = prm.split("_");
				fieldName = arr[1];
				String langId = arr[3];
				
				try
				{
					ReturnCodes rc = ReturnCodes.valueOf(arr[2]);
					if (!rtcMap.containsKey(arr[2]))
					{
						rtcMap.put(arr[2], new ReturnCodeTexts(rc, new Phrase()));
					}
					rtcMap.get(arr[2]).getPhrase().set(langId, parms.get(prm)[0]);
				}
				catch (Exception e)
				{
					logger.error("", e);
				}
			}
		}

		// ResultCode information
		List<IConfigurableParam[]> toSave = new ArrayList<IConfigurableParam[]>();
		if (rtcMap != null)
		{
			for (ReturnCodeTexts prm : rtcMap.values())
			{
				IConfigurableParam[] cparms = new IConfigurableParam[1];
				cparms[0] = new BasicConfigurableParm(fieldName, prm);
				toSave.add(cparms);
			}
		}

		if (fieldName == null)
		{
			fieldName = returnCodeFieldName;
		}

		updated = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, fieldName, toSave, false);

		return updated;
	}

	/**
	 * Used to persist Group Shared Accounts details
	 */
	@SuppressWarnings("unchecked")
	public Configurable updateGSAParameters(HttpSession session, User user, long luid, int iversion, Map<String, String[]> parms) throws Exception
	{
		Configurable result = null;

		// Must Update?
		boolean updateServiceClass = (session.getAttribute(GroupSharedAccounts.SC_FIELD_UPDATED) != null);
		boolean updateQuotas = (session.getAttribute(GroupSharedAccounts.QUOTA_FIELD_UPDATED) != null);
		boolean updateVariants = (session.getAttribute(GroupSharedAccounts.VAR_FIELD_UPDATED) != null);
		boolean updateUssd = (session.getAttribute(USSD_UPDATED) != null);

		// Persist service Classes
		if (updateServiceClass)
		{
			List<IConfigurableParam[]> toSave = (List<IConfigurableParam[]>) session.getAttribute(GroupSharedAccounts.SC_FIELD);
			result = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, GroupSharedAccounts.SC_FIELD, toSave, false);
			iversion = result.getVersion();
			session.removeAttribute(GroupSharedAccounts.SC_FIELD_UPDATED);
		}

		// Persist Quotas
		if (updateQuotas)
		{
			List<IConfigurableParam[]> toSave = (List<IConfigurableParam[]>) session.getAttribute(GroupSharedAccounts.QUOTA_FIELD);
			result = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, GroupSharedAccounts.QUOTA_FIELD, toSave, false);
			iversion = result.getVersion();
			session.removeAttribute(GroupSharedAccounts.QUOTA_FIELD_UPDATED);
		}

		// Persist Variants
		if (updateVariants)
		{
			List<IConfigurableParam[]> toSave = (List<IConfigurableParam[]>) session.getAttribute(GroupSharedAccounts.VAR_FIELD);
			result = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, GroupSharedAccounts.VAR_FIELD, toSave, false);
			iversion = result.getVersion();
			session.removeAttribute(GroupSharedAccounts.VAR_FIELD_UPDATED);
		}

		// Persist UssdMenu
		if (updateUssd)
		{
			Configurable upd = udpateUSSDMenuProcess(session, user, iversion, luid, PROCESS_ACTION_PROCESS_VARIABLE, PROCESS_USSD_VARIABLE, USSD_UPDATED);
			if (upd != null)
			{
				iversion = upd.getVersion();
			}
		}

		// Persist Result Texts
		return persistReturnCodeConfiguration(user, luid, iversion, parms, "ReturnCodesTexts");
	}

	// TODO: This has been moved to BaseServiceConfigHandler
	private Configurable udpateUSSDMenuProcess(HttpSession session, User user, int iversion, long luid, String PROCESS_ACTION_PROCESS_VARIABLE, String PROCESS_USSD_VARIABLE, String USSD_UPDATED)
			throws Exception
	{
		boolean updateUssd = (session.getAttribute(USSD_UPDATED) != null);
		Configurable updated = null;

		if (updateUssd)
		{
			IProcess process = (IProcess) session.getAttribute(PROCESS_ACTION_PROCESS_VARIABLE);
			String xml = process.serialize();
			List<IConfigurableParam[]> toSave = new ArrayList<>();

			ConfigurableResponseParam cp = (ConfigurableResponseParam) session.getAttribute(PROCESS_USSD_VARIABLE);
			cp.setValue(xml);
			IConfigurableParam[] cpl = new IConfigurableParam[1];
			cpl[0] = cp;
			toSave.add(cpl);
			updated = UiConnectionClient.getInstance().saveConfigurationStructure(user, luid, iversion, cp.getFieldName(), toSave, false);
			session.removeAttribute(USSD_UPDATED);
		}

		return updated;
	}

}
