package hxc.connectors.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.concurrent.hxc.GetReturnCodeTextRequest;
import com.concurrent.hxc.GetReturnCodeTextResponse;

import hxc.configuration.Config;
import hxc.configuration.IConfiguration;
import hxc.configuration.Rendering;
import hxc.configuration.ValidationException;
import hxc.connectors.IConnector;
import hxc.connectors.ctrl.ICtrlConnector;
import hxc.connectors.ctrl.IServerRole;
import hxc.connectors.database.IDatabase;
import hxc.connectors.database.IDatabaseConnection;
import hxc.connectors.ecds.tampercheck.ITamperCheckConnector;
import hxc.connectors.ecds.tampercheck.ITamperedAccount;
import hxc.connectors.ecds.tampercheck.ITamperedAgent;
import hxc.connectors.ecds.tampercheck.ITamperedAuditEntry;
import hxc.connectors.ecds.tampercheck.ITamperedBatch;
import hxc.connectors.sms.ISmsConnector;
import hxc.connectors.smtp.ISmtpConnector;
import hxc.connectors.smtp.ISmtpHistory;
import hxc.connectors.soap.ISoapConnector;
import hxc.connectors.ui.utils.ConfigurablesIterator;
import hxc.connectors.ui.utils.UiConnectorUtils;
import hxc.connectors.vas.VasService;
import hxc.processmodel.IProcess;
import hxc.servicebus.ILocale;
import hxc.servicebus.IPlugin;
import hxc.servicebus.IServiceBus;
import hxc.servicebus.ReturnCodes;
import hxc.services.IService;
import hxc.services.airsim.protocol.IAirSimService;
import hxc.services.airsim.protocol.ICdr;
import hxc.services.airsim.protocol.ISmsHistory;
import hxc.services.airsim.protocol.IUssdResponse;
import hxc.services.notification.INotification;
import hxc.services.notification.INotificationText;
import hxc.services.notification.INotifications;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.services.security.ISecurity;
import hxc.services.security.IUser;
import hxc.services.security.SupplierOnly;
import hxc.utils.configuration.ConfigurationBase;
import hxc.utils.processmodel.Start;
import hxc.utils.protocol.uiconnector.airsim.AirResponseResetRequest;
import hxc.utils.protocol.uiconnector.airsim.AirResponseResetResponse;
import hxc.utils.protocol.uiconnector.airsim.AirResponseUpdateRequest;
import hxc.utils.protocol.uiconnector.airsim.AirResponseUpdateResponse;
import hxc.utils.protocol.uiconnector.airsim.AirSimSmsRequest;
import hxc.utils.protocol.uiconnector.airsim.AirSimSmsResponse;
import hxc.utils.protocol.uiconnector.airsim.AirSimUssdRequest;
import hxc.utils.protocol.uiconnector.airsim.AirSimUssdResponse;
import hxc.utils.protocol.uiconnector.airsim.Cdr;
import hxc.utils.protocol.uiconnector.airsim.SmsHistory;
import hxc.utils.protocol.uiconnector.common.ConfigNotification;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.ConfigurationPath;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.common.NotificationVariable;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigurationUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.response.ComponentFitness;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigServerRoleResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigurationResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.FitnessResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;
import hxc.utils.protocol.uiconnector.registration.FacilityRegistration;
import hxc.utils.protocol.uiconnector.registration.Registration;
import hxc.utils.protocol.uiconnector.request.AirSimCommonRequest;
import hxc.utils.protocol.uiconnector.request.AirSimGetUsageRequest;
import hxc.utils.protocol.uiconnector.request.AirSimStartUsageRequest;
import hxc.utils.protocol.uiconnector.request.AirSimStopUsageRequest;
import hxc.utils.protocol.uiconnector.request.CallConfigurableMethodRequest;
import hxc.utils.protocol.uiconnector.request.ConfigurationUpdateRequest;
import hxc.utils.protocol.uiconnector.request.GetAirsimHistoryRequest;
import hxc.utils.protocol.uiconnector.request.GetConfigurableRequest;
import hxc.utils.protocol.uiconnector.request.GetEcdsCheckTamperedAgentRequest;
import hxc.utils.protocol.uiconnector.request.GetEcdsTamperCheckRequest;
import hxc.utils.protocol.uiconnector.request.GetEcdsTamperResetRequest;
import hxc.utils.protocol.uiconnector.request.GetFacilityRequest;
import hxc.utils.protocol.uiconnector.request.GetLicenseDetailsRequest;
import hxc.utils.protocol.uiconnector.request.ReturnCodeTextDefaultsResponse;
import hxc.utils.protocol.uiconnector.request.RevertServiceRequest;
import hxc.utils.protocol.uiconnector.request.SendSMSRequest;
import hxc.utils.protocol.uiconnector.response.AirSimGetUsageResponse;
import hxc.utils.protocol.uiconnector.response.AirSimStartUsageResponse;
import hxc.utils.protocol.uiconnector.response.AirSimStopUsageResponse;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableMethod;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.response.ConfigurationUpdateResponse;
import hxc.utils.protocol.uiconnector.response.ConfirmationResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse.ErrorCode;
import hxc.utils.protocol.uiconnector.response.GetAirsimHistoryResponse;
import hxc.utils.protocol.uiconnector.response.GetAllConfigurablesResponse;
import hxc.utils.protocol.uiconnector.response.GetConfigurableResponse;
import hxc.utils.protocol.uiconnector.response.GetEcdsCheckTamperedAgentResponse;
import hxc.utils.protocol.uiconnector.response.GetEcdsTamperCheckResponse;
import hxc.utils.protocol.uiconnector.response.GetEcdsTamperResetResponse;
import hxc.utils.protocol.uiconnector.response.GetFacilityResponse;
import hxc.utils.protocol.uiconnector.response.GetLicenseDetailsResponse;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;
import hxc.utils.protocol.uiconnector.response.RevertServiceResponse;
import hxc.utils.protocol.uiconnector.response.SendSMSResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.vas.VasCommandsRequest;
import hxc.utils.protocol.uiconnector.vas.VasCommandsResponse;
import hxc.utils.reflection.ClassInfo;
import hxc.utils.reflection.IPropertyInfo;
import hxc.utils.reflection.PropertyInfo;
import hxc.utils.reflection.ReflectionHelper;
import hxc.utils.registration.IFacilityRegistration;


public class UiServiceConfiguration
{
	final static Logger logger = LoggerFactory.getLogger(UiServiceConfiguration.class);
	
	private IServiceBus esb;
	private IDatabase database;
	private ICtrlConnector control;

	private Map<String, IPlugin> configCache;
	public final SimpleDateFormat LOGGER_TRANSACTION_ID_CREATOR = new SimpleDateFormat("yyyyMMddHHmmssSSS");

	public UiServiceConfiguration(IServiceBus esb, IDatabase database, ICtrlConnector control)
	{
		this.esb = esb;
		this.database = database;
		this.control = control;
		configCache = new HashMap<String, IPlugin>();
	}

	public String executeConfigurableMethod(IUser user, CallConfigurableMethodRequest request) throws SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		String result = null;
		IPlugin iplugin = null;

		if (request.getConfigUID() == 0)
		{
			// First find the correct config
			iplugin = configCache.get(request.getConfigName());
		}
		else
		{
			for (IPlugin plugin : configCache.values())
			{
				// Start looking here
				IConfiguration iconfig = findConfiguration(plugin.getConfiguration(), request.getConfigUID());
				if (iconfig != null)
				{
					iplugin = plugin;
					break;
				}
			}
		}

		if (iplugin == null)
		{
			retrieveAllConfigurable(user);
			iplugin = configCache.get(request.getConfigName());
		}

		IConfiguration config = iplugin.getConfiguration();
		Method[] methods = config.getMethods();
		for (int i = 0; i < methods.length; i++)
		{
			if (methods[i].getName().equals(request.getMethod()))
			{
				Object res = methods[i].invoke(iplugin.getConfiguration());
				result = (res == null) ? "success" : (String) res;
				break;
			}
		}
		return result;
	}

	public UiBaseResponse retrieveConfigurable(IUser iuser, GetConfigurableRequest request)
	{
		UiBaseResponse response = null;
		
		// Populate the config cache. Previously this was only done once upon first use. 
		// Changed to re-populate at each request.
		retrieveAllConfigurable(iuser);
		

		IConfiguration iconfig = null;

		for (IPlugin plugin : configCache.values())
		{
			// Start looking here
			iconfig = findConfiguration(plugin.getConfiguration(), request.getConfigurableSerialVersionID());
			if (iconfig != null)
			{
				break;
			}
		}

		if (iconfig == null)
		{
			return null;
		}

		response = new GetConfigurableResponse();

		Configurable config = extractConfig(iconfig, iuser, false);
		if (config == null)
		{
			return null;
		}
		((GetConfigurableResponse) response).setConfig(config);
		// ((GetConfigurableResponse) response).sort();

		return response;
	}

	public GetLocaleInformationResponse retrieveLocaleInformation()
	{
		ILocale locale = this.esb.getLocale();
		GetLocaleInformationResponse response = new GetLocaleInformationResponse();

		int maxLanguages = locale.getMaxLanguages();
		for (int i = 0; i < maxLanguages + 1; i++)
		{
			if (i > 0)
			{
				((GetLocaleInformationResponse) response).setLanguage(i - 1, locale.getLanguage(i));
				((GetLocaleInformationResponse) response).setAlphabet(i - 1, locale.getAlpabet(i));
			}

		}

		response.setCurrencyCode(locale.getCurrencyCode());
		response.setCurrencyDecimalDigits(locale.getCurrencyDecimalDigits());

		return (GetLocaleInformationResponse) response;
	}

	private ConfigurationPath huntForPath(ConfigurationPath configPath, String path)
	{
		if (path.length() == 0)
		{
			return configPath;
		}
		else
		{
			int pos = path.indexOf(".");
			String firstPath = (pos > 0) ? path.substring(0, pos) : path;
			String remainingPath = (pos > 0) ? path.substring(pos + 1) : "";

			ConfigurationPath found = null;
			if (configPath.getChildren() != null)
			{
				for (ConfigurationPath cp : configPath.getChildren())
				{
					if (cp.getName().equalsIgnoreCase(firstPath))
					{
						found = cp;
						break;
					}
				}
			}

			// Create if you cannot find
			if (found == null)
			{
				ConfigurationPath conpath = new ConfigurationPath(firstPath, 0);
				configPath.addChild(conpath);
				found = conpath;
			}
			return huntForPath(found, remainingPath);
		}
	}

	public ConfigurationPath retrieveAllConfigurablePaths(IUser user)
	{

		ConfigurationPath path = new ConfigurationPath("root", 0);
		ConfigurablesIterator configIterator = new ConfigurablesIterator(this.esb, path, user)
		{

			@Override
			public void processConfiguration(IPlugin iplugin, IConfiguration iconfig, String parentPath, Object... refs)
			{
				try
				{
					ConfigurationPath configPath = (ConfigurationPath) refs[0];
					IUser user = (IUser) refs[1];
					
					String fullConfigPath = parentPath + ((parentPath.length() > 0 && iconfig.getPath(Phrase.ENG).length() > 0) ? "." : "") + iconfig.getPath(Phrase.ENG);
					ConfigurationPath path = huntForPath(configPath, fullConfigPath);
					ConfigurationPath node = extractConfigInfo(iconfig, user);

//					if (node.getParametersCount() > 0 || node.getMethodsCount() > 0 || node.getNotificationsCount() > 0)
					if (node.getParametersCount() > 0 || node.getNotificationsCount() > 0)
					{
						path.addChild(node);
					}
				}
				catch (Exception e)
				{
				}

			}

		};

		try
		{
			configIterator.iterateThroughPlughinConfigurables(true);
		}
		catch (Exception e)
		{
		}

		path = cleanupConfigurationPath(path);

		return path;
	}

	private ConfigurationPath extractConfigInfo(IConfiguration iconfig, IUser user)
	{
		ConfigurationPath info = new ConfigurationPath();
		ISecurity securityService = esb.getFirstService(ISecurity.class);
		// General Stuff
		info.setName(iconfig.getName(Phrase.ENG));
		info.setConfigSerialVersionUID(iconfig.getSerialVersionUID());

		int parmCount = 0;

		// Extract Getter info
		IPropertyInfo[] properties = iconfig.getProperties();
		for (IPropertyInfo propertyInfo : properties)
		{
			Method getter = propertyInfo.getGetterMethod();
			// Check for annotation first
			if (getter == null && !propertyInfo.getName().equalsIgnoreCase("Currency"))
			{
				continue; // Move on there is no point to a property with no getter
			}
			try
			{
				Annotation anno = null;
				if (!propertyInfo.getName().equalsIgnoreCase("Currency"))
					anno = getter.getAnnotation(SupplierOnly.class);

				if (anno == null || securityService.isSupplier(user))
				{
					propertyInfo.get(iconfig); // if no side effects, we should remove this
					parmCount++;
				}

			}
			catch (InvocationTargetException | SecurityException | IllegalArgumentException | IllegalAccessException se)
			{
				// Ignore the exception, though the user will no be able to get the relavant property
			}
		}
		info.setParametersCount(parmCount);

		// Extract Method Info
		Method[] methods = iconfig.getMethods();
		info.setMethodsCount(methods.length);

		// Extract notification Info
		try
		{
			if (iconfig.getNotifications() != null)
			{
				// Notifications get requires a sesurity check (Explicit not implicit!)
				iconfig.performGetNotificationSecurityCheck();

				int notificationCount = iconfig.getNotifications().getNotificationIds().length;
				info.setNotificationsCount(notificationCount);
			}
		}
		catch (SecurityException se)
		{
			// Ignore the exception (desired behavour)
		}

		return info;
	}

	private ConfigurationPath cleanupConfigurationPath(ConfigurationPath startConfig)
	{

		ConfigurationPath result = new ConfigurationPath();
		try
		{
			if (startConfig.getChildren() != null && startConfig.getChildren().size() > 0)
			{
				result.setChildren(new ArrayList<ConfigurationPath>());

				for (ConfigurationPath cp : startConfig.getChildren())
				{
					ConfigurationPath path = cleanupConfigurationPath(cp);
					if (path != null)
					{
						result.addChild(path);
					}
				}
			}

			if ((startConfig.getParametersCount() > 0 || startConfig.getMethodsCount() > 0 || startConfig.getNotificationsCount() > 0)
					|| (result.getChildren() != null && result.getChildren().size() > 0))
			{
				result.copyAllButChildren(startConfig);
			}
			else
			{
				result = null;
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return result;
	}

	// Retrieve all configurable (list of services)
	public GetAllConfigurablesResponse retrieveAllConfigurable(IUser iuser)
	{

		GetAllConfigurablesResponse response = new GetAllConfigurablesResponse();
		response.setConfigs(new LinkedList<Configurable>());

		ConfigurablesIterator configIterator = new ConfigurablesIterator(this.esb, response, iuser)
		{

			@Override
			public void processConfiguration(IPlugin iplugin, IConfiguration iconfig, String parentPath, Object... refs)
			{
				try
				{
					Configurable config = extractConfig(iconfig, (IUser) refs[1], true);
					((GetAllConfigurablesResponse) refs[0]).getConfigs().add(config);
					configCache.put(config.getName(), iplugin);
				}
				catch (Exception e)
				{
				}
			}
		};
		configIterator.iterateThroughPlughinConfigurables(false);

		// response.sort();

		return response;
	}

	public IPropertyInfo[] extractPropertyInfo(Class cls)
	{
		List<IPropertyInfo> props = new ArrayList<IPropertyInfo>();

		Collection<PropertyInfo> properties = ReflectionHelper.getClassInfo(cls).getProperties().values();
		for (PropertyInfo property : properties)
		{
			String name = property.getName();
			if (name.equals("Version") || name.equals("Configurations") || name.equals("Methods") || name.equals("Properties") || name.equals("Path") || name.equals("Notifications")
					|| name.equals("SerialVersionUID"))
				continue;
			props.add(property);
		}

		IPropertyInfo[] result = props.toArray(new IPropertyInfo[props.size()]);
		return result;
	}

	private List<IConfigurableParam> extractParmValuesOnly(Object inspectedObject, IPropertyInfo[] properties, IUser user, ISecurity securityService)
	{
		List<IConfigurableParam> params = new ArrayList<>();
		for (IPropertyInfo propertyInfo : properties)
		{
			Method getter = propertyInfo.getGetterMethod();
			if (getter == null)
				continue;// Move on there is no point to a property with no getter

			Annotation anno = getter.getAnnotation(Config.class);
			if (anno != null)
			{
				Config ca = (Config) anno;
				if (ca.hidden())
					continue; // Leave hidden fields
			}

			IConfigurableParam param = new BasicConfigurableParm(propertyInfo.getName());

			try
			{
				Object ovalue = null;
				ovalue = propertyInfo.get(inspectedObject);

				if ((ovalue != null) && (ovalue instanceof Start))
				{
					String xml = ((Start) ovalue).serialize();
					param.setValue(xml);
				}
				else if ((ovalue != null) && (ovalue.getClass().isEnum()))
				{
					param.setValue(ovalue.toString());
				}
				else if ((ovalue != null) && (ovalue instanceof Object[]))
				{
					// Get Properties of this object and expand
					UiUtils uiutils = new UiUtils();
					if (uiutils.isUIHandledType(ovalue.getClass().getComponentType()))
					{
						param.setValue(ovalue);
					}
					else
					{
						List<IConfigurableParam[]> parmConfig = new ArrayList<>();
						Object[] objs = (Object[]) ovalue;
						for (int i = 0; i < objs.length; i++)
						{
							IConfigurableParam[] paramArray = extractObjectValues(objs[i], user, securityService);
							parmConfig.add(paramArray);
						}
						;
						param.setValue(parmConfig);
					}
				}
				else
				{
					param.setValue(ovalue);
				}
				params.add(param);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
		return params;
	}

	// private List<ConfigurableResponseParam> extractParams(IConfiguration iconfig, IPropertyInfo[] properties)
	private List<ConfigurableResponseParam> extractParams(Object inspectedObject, IPropertyInfo[] properties, IUser user)
	{
//		ISecurity securityService = (SecurityService) esb.getFirstService(SecurityService.class);
		ISecurity securityService = esb.getFirstService(ISecurity.class);

		UiUtils uiutils = new UiUtils();

		List<ConfigurableResponseParam> configParams = new ArrayList<>();
		for (IPropertyInfo propertyInfo : properties)
		{
			// First Check for supplieronly fields
			Method getter = propertyInfo.getGetterMethod();
			if (getter == null)
				continue;// Move on there is no point to a property with no getter

			// Hidden Fields => Move on
			Annotation anno = getter.getAnnotation(Config.class);
			if (anno != null)
			{
				Config ca = (Config) anno;
				if (ca.hidden())
				{
					continue;
				}
			}
			// Supplier only and user not supplier => Move on
			Annotation soanon = getter.getAnnotation(SupplierOnly.class);
			if (soanon != null && (!user.isSupplier()))
			{
				continue; // Supplier Only field any you're ont welcome
			}

			ConfigurableResponseParam param = new ConfigurableResponseParam(propertyInfo.getName());

			String[] enumValues = null;
			Method setter = propertyInfo.getSetterMethod();
			if (setter == null)
				param.setReadOnly(true); // If there is no setter this is also read-only
			else if (setter.getParameterTypes()[0].isEnum())
			{
				enumValues = Arrays.toString(setter.getParameterTypes()[0].getEnumConstants()).substring(1).replaceAll("]$", "").replaceAll(" ", "").split(",");
			}

			try
			{
				Object ovalue = null;
				ovalue = propertyInfo.get(inspectedObject);

				if (ovalue != null)
				{
					param.setValueType(ovalue.getClass().getName());

					if (ovalue instanceof Start)
					{
						// USSD Process
						String xml = ((Start) ovalue).serialize();
						param.setValue(xml);
					}
					else if (ovalue.getClass().isEnum())
					{
						param.setValue(ovalue.toString());
					}
					else if ((ovalue instanceof Object[]) && (!uiutils.isUIHandledType(ovalue.getClass().getComponentType())))
					{
						ConfigurableResponseParam[] parmStruct = uiutils.extractObjectStructure(getter.getReturnType().getComponentType(), user, securityService);
						param.setStructure(parmStruct);

						// Part 2: Object [] stored as List<IConfigurableParam[]>
						List<IConfigurableParam[]> parmConfig = new ArrayList<>();
						Object[] objs = (Object[]) ovalue;
						for (int i = 0; i < objs.length; i++)
						{
							IConfigurableParam[] paramArray = extractObjectValues(objs[i], user, securityService);
							parmConfig.add(paramArray);
						}
						param.setValue(parmConfig);

					}
					else if (!(ovalue instanceof Object[]) && (!uiutils.isUIHandledType(getter.getReturnType())))
					{
						try
						{
							IPropertyInfo[] objProperties = extractPropertyInfo(getter.getReturnType());
							List<ConfigurableResponseParam> list = extractParams(ovalue, objProperties, user);
							ConfigurableResponseParam[] arr = list.toArray(new ConfigurableResponseParam[list.size()]);
							param.setValue(arr);
						}
						catch (Exception e)
						{
						}
					}
					else
					{
						// Check if this object contains parameters ?!?
						param.setValue(ovalue);
					}
				}
				else
				{
					param.setValueType(getter.getReturnType().getName());
					param.setValue("");
				}

				// Is this an enum?
				// Annotation information

				String[] possibleValues = null;

				if (anno != null)
				{
					Config ca = (Config) anno;
					if (ca.hidden())
					{
						continue;
					}
					param.setDescription(ca.description());
					param.setComment(ca.comment());
					if (ca.renderAs() != Rendering.DEFAULT)
					{
						param.setRenderAs(ca.renderAs().toString());
					}
					param.setMaxLength(ca.maxLength());
					param.setMinValue(ca.minValue());
					param.setMaxValue(ca.maxValue());
					param.setDefaultValue(ca.defaultValue());
					param.setScaleFactor(ca.scaleFactor());
					param.setDecimalDigitsToDisplay(ca.decimalDigitsToDisplay());
					param.setGroup(ca.group());
					param.setUnique(ca.unique());
					param.setReferencesKey(ca.referencesKey());

					if (ca.possibleValues() != null && ca.possibleValues().length() > 0)
					{
						possibleValues = ca.possibleValues().split(",");
					}
					if ((possibleValues != null) || (ca.possibleValues() != null && ca.possibleValues().length() > 0))
					{
						Set<String> posSet = new HashSet<String>();
						if (ca.possibleValues() != null && ca.possibleValues().length() > 0)
						{
							posSet.addAll(Arrays.asList(ca.possibleValues().split(",")));
						}

						if (possibleValues != null)
						{
							Set<String> pos2 = new HashSet<String>();
							for (Object o : possibleValues)
							{
								String value = String.valueOf(o);
								pos2.add(value);
							}
							if (posSet.size() > 0)
							{
								posSet.retainAll(pos2);
							}
							else
							{
								posSet.addAll(pos2);
							}
						}
						if (posSet.size() > 0)
						{
							param.setPossibleValues(new String[posSet.size()]);
							int index = 0;
							for (String en : posSet)
							{
								param.getPossibleValues()[index++] = en;
							}
						}
					}
				}

				// Possible Values
				if (possibleValues != null)
					param.setPossibleValues(possibleValues);
				else if (enumValues != null)
					param.setPossibleValues(enumValues);

				configParams.add(param);

			}
			catch (SecurityException | InvocationTargetException se)
			{
				// Ignore security exception (just noise)
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		return configParams;
	}

	private IConfigurableParam[] extractObjectValues(Object obj, IUser user, ISecurity securityService)
	{
		List<IConfigurableParam> parms = null;

		if (obj != null)
		{
			if (obj.getClass().equals(ReturnCodeTexts.class))
			{
				parms = new ArrayList<>(1);
				parms.add(new BasicConfigurableParm(obj.getClass().getSimpleName(), obj));
			}
			else
			{
				IPropertyInfo[] props = extractPropertyInfo(obj.getClass());
				parms = extractParmValuesOnly(obj, props, user, securityService);
			}
		}

		if (parms == null)
			return (new IConfigurableParam[0]);
		else
			return parms.toArray(new IConfigurableParam[parms.size()]);
	}

	private Configurable extractConfig(IConfiguration iconfig, IUser iuser, boolean recur)
	{
		Configurable config = new Configurable();

		// Basics
		config.setName(iconfig.getName(Phrase.ENG));
		config.setPath(iconfig.getPath(Phrase.ENG));
		config.setVersion(iconfig.getVersion());
		config.setConfigSerialVersionUID(iconfig.getSerialVersionUID());

		// Extract methods and properties
		IPropertyInfo[] properties = iconfig.getProperties();

		List<ConfigurableResponseParam> configParams = extractParams(iconfig, properties, iuser);
		config.setParams(configParams.toArray(new ConfigurableResponseParam[configParams.size()]));

		// Now check for all methods that can be executed
		List<ConfigurableMethod> methodsList = new ArrayList<ConfigurableMethod>();
		Method[] methods = iconfig.getMethods();

		for (Method mi : methods)
		{
			String description = null;
			boolean hidden = false;
			Annotation anno = mi.getAnnotation(Config.class);
			if (anno != null)
			{
				Config ca = (Config) anno;
				description = ca.description();
				hidden = ca.hidden();
			}
			if (description == null)
			{
				description = UiConnectorUtils.splitCamelCaseString(mi.getName());
			}
			ConfigurableMethod configMethod = new ConfigurableMethod(mi.getName(), description);
			if(!hidden)
			{
				methodsList.add(configMethod);
			}
		}

		if (methodsList.size() > 0)
		{
			config.setMethods(methodsList.toArray(new ConfigurableMethod[methodsList.size()]));
		}

		boolean notificationsAvailable = false;
		try
		{
			if (iconfig.getNotifications() != null)
			{
				// Does this user have permission to use notifications
				iconfig.performGetNotificationSecurityCheck();
				notificationsAvailable = true;
			}
		}
		catch (Exception e)
		{
		}

		if (notificationsAvailable)
		{
			// Extract Notifications

			config.setNotifications(new ArrayList<ConfigNotification>());

			for (int notificationId : iconfig.getNotifications().getNotificationIds())
			{
				// Extract base info
				INotification inot = iconfig.getNotifications().getNotification(notificationId);
				ConfigNotification confNotification = new ConfigNotification();
				confNotification.setDescription(inot.getDescription());
				confNotification.setNotificationId(notificationId);

				// Extract specific text
				confNotification.setText(new String[esb.getLocale().getMaxLanguages()]);
				for (int i = 0; i < confNotification.getText().length; i++)
				{
					confNotification.getText()[i] = inot.getText(i + 1); // 1 based index ?!?
				}
				config.getNotifications().add(confNotification);
			}

			// Extract Variables
			try
			{
				if (iconfig.getNotifications() != null && iconfig.getNotifications().getVariables() != null)
				{
					config.setVariables(new ArrayList<NotificationVariable>());
					if (iconfig.getNotifications().getVariableDetails() != null)
					{
						for (String varName : iconfig.getNotifications().getVariableDetails().keySet())
						{
							NotificationVariable var = new NotificationVariable(varName, iconfig.getNotifications().getVariableDetails().get(varName));
							config.getVariables().add(var);
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}

		// Inner Configuration?
		if (iconfig.getConfigurations() != null && recur)
		{
			config.setConfigurable(new LinkedList<Configurable>());
			for (IConfiguration innerConfig : iconfig.getConfigurations())
			{
				config.getConfigurable().add(extractConfig(innerConfig, iuser, recur));
			}
		}

		// if (config != null)
		// {
		// config.sortAll();
		// }

		return config;
	}

	private IConfiguration findConfiguration(IConfiguration iconfig, long uid)
	{
		try
		{
			if (iconfig == null)
			{
				return null;
			}
			else if (iconfig.getSerialVersionUID() == uid)
			{
				return iconfig;
			}
			else if (iconfig.getConfigurations() != null && iconfig.getConfigurations().size() > 0)
			{
				for (IConfiguration config : iconfig.getConfigurations())
				{

					IConfiguration tmp = findConfiguration(config, uid);
					if (tmp != null)
					{
						return tmp;
					}

				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	private <T> IPlugin findPlugin(Class<T> cls, IUser user)
	{
		if (configCache.size() < 1)
		{
			retrieveAllConfigurable(user);
		}
		for (IPlugin iplugin : configCache.values())
		{
			if (cls.isInstance(iplugin))
			{
				return iplugin;
			}

		}
		return null;
	}

	public CtrlConfigurationResponse getControlServiceConfig(String userId, String sessionId, IUser user) throws Exception
	{
		IPlugin plugin = findPlugin(ICtrlConnector.class, user);
		if (plugin == null)
			throw new Exception("Could not find Resilliance Configuration");

		CtrlConfigurationResponse response = new CtrlConfigurationResponse(userId, sessionId);
		try
		{
			if (plugin instanceof ICtrlConnector)
			{
				response.setServerList(extractControlServiceInfo((ICtrlConnector) plugin));
				response.setServerRoleList(extractControlServiceRoles((ICtrlConnector) plugin));
				response.setVersionNumber(plugin.getConfiguration().getVersion());
			}
		}
		catch (Exception e)
		{
			throw e;
		}

		return response;
	}

	public CtrlConfigServerRoleResponse getControlServiceRoles(String userId, String sessionId, IUser user) throws Exception
	{
		IPlugin plugin = findPlugin(ICtrlConnector.class, user);
		if (plugin == null)
			throw new Exception("Could not find Resilliance Configuration");

		CtrlConfigServerRoleResponse response = new CtrlConfigServerRoleResponse(userId, sessionId);
		try
		{
			if (plugin instanceof ICtrlConnector)
			{
				response.setServerRoleList(extractControlServiceRoles((ICtrlConnector) plugin));
				response.setVersionNumber(plugin.getConfiguration().getVersion());
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		return response;
	}

	private ServerInfo[] extractControlServiceInfo(ICtrlConnector connector)
	{
		ServerInfo[] result = null;
		if (connector.getServerList() != null)
		{
			result = new ServerInfo[connector.getServerList().length];
			for (int i = 0; i < connector.getServerList().length; i++)
			{
				result[i] = new ServerInfo(connector.getServerList()[i].getServerHost(), connector.getServerList()[i].getPeerHost(), connector.getServerList()[i].getTransactionNumberPrefix());
			}
		}
		return result;
	}

	private ServerRole[] extractControlServiceRoles(ICtrlConnector connector)
	{
		ServerRole[] result = null;
		if (connector.getServerRoleList() != null)
		{
			result = new ServerRole[connector.getServerRoleList().length];
			for (int index = 0; index < connector.getServerRoleList().length; index++)
			{
				result[index] = new ServerRole(connector.getServerRoleList()[index].getServerRoleName(), connector.getServerRoleList()[index].isExclusive(),
						connector.getServerRoleList()[index].getOwner(), connector.getServerRoleList()[index].getAttachCommand(), connector.getServerRoleList()[index].getDetachCommand());
				result[index].setOriginalIndex(index);
			}
		}
		return result;
	}

	public boolean updateControlServiceConfiguration(CtrlConfigurationUpdateRequest request, IUser user) throws ValidationException
	{
		int configurationVersion = ((CtrlConfigurationUpdateRequest) request).getVersionNumber();
		boolean result = updateServerInfo(request.getServerInfoList(), user, configurationVersion, request.isPersistToDatabase());
		if (result && request.isPersistToDatabase())
		{
			configurationVersion++;
		}
		result = udpateServerRoles(request.getServerRoleList(), user, configurationVersion, request.isPersistToDatabase()) && result;
		return result;
	}

	public boolean updateServerInfo(ServerInfo[] serverInfos, IUser user, int version, boolean persist) throws ValidationException
	{
		boolean success = false;
		IPlugin plugin = findPlugin(ICtrlConnector.class, user);
		if (plugin != null)
		{
			if (version < plugin.getConfiguration().getVersion())
			{
				throw new ValidationException("Server host version out of date");
			}
			else
			{
				((ICtrlConnector) plugin).setServerList(serverInfos);
				if (persist)
				{
					success = persistPluginConfiguration(plugin);
					if (success)
					{
						ConfigurationBase config = (ConfigurationBase) plugin.getConfiguration();
						config.setVersion(version + 1);
					}
				}
				else
					success = true;
			}
		}

		return success;
	}

	public boolean udpateServerRoles(ServerRole[] serverRoles, IUser user, int version, boolean persist) throws ValidationException
	{
		boolean success = false;
		if (serverRoles != null)
		{
			Set<String> serverRoleNames = new HashSet<>();
			for (ServerRole sr : serverRoles)
			{
				if (serverRoleNames.contains(sr.getServerRoleName()))
				{
					throw new ValidationException("Server Role duplicate %s is not allowed", sr.getServerRoleName());
				}
				serverRoleNames.add(sr.getServerRoleName());
			}
		}
		IPlugin plugin = findPlugin(ICtrlConnector.class, user);
		if (plugin != null)
		{
			if (version < plugin.getConfiguration().getVersion())
			{
				throw new ValidationException("Server role version out of date");
			}

			IServerRole[] oldServerRoles = ((ICtrlConnector) plugin).getServerRoleList();

			if (oldServerRoles != null && oldServerRoles.length > 0)
			{
				for (int i = 0; i < serverRoles.length; i++)
				{
					if (serverRoles[i].getOriginalIndex() >= 0 && serverRoles[i].getOriginalIndex() < oldServerRoles.length)
					{
						serverRoles[i].setOwner(oldServerRoles[serverRoles[i].getOriginalIndex()].getOwner());
					}
				}
			}

			((ICtrlConnector) plugin).setServerRoleList(serverRoles);
			if (persist)
			{
				success = persistPluginConfiguration(plugin);
				if (success)
				{
					ConfigurationBase config = (ConfigurationBase) plugin.getConfiguration();
					config.setVersion(version + 1);
				}

			}
			else
				success = true;
		}
		return success;
	}

	private boolean persistPluginConfiguration(IPlugin plugin)
	{
		try (IDatabaseConnection connection = database.getConnection(null))
		{
			boolean updated = plugin.getConfiguration().save(connection, control);
			if (!updated)
			{
				logger.error("Persisting ServerInfo failed");
			}
			else
			{
				logger.info("ServerInfo updated");
				return true;
			}
		}
		catch (Exception e)
		{
			logger.error("Confguration should not be updated", e);
		}
		return false;
	}

	private boolean findAndUpdateConfiguration(IConfiguration baseConfig, IConfiguration config)
	{
		if (baseConfig.getConfigurations() != null)
		{
			IConfiguration configToUpdate = null;
			Iterator<IConfiguration> iterator = baseConfig.getConfigurations().iterator();
			while (iterator.hasNext())
			{
				IConfiguration iconfig = iterator.next();
				if (iconfig.getSerialVersionUID() == config.getSerialVersionUID())
				{
					configToUpdate = iconfig;
					break;
				}
				if (iconfig.getConfigurations() != null)
				{
					if (findAndUpdateConfiguration(iconfig, config))
					{
						return true;
					}
				}
			}

			if (configToUpdate != null)
			{
				UiUtils utils = new UiUtils();
				utils.deepCopy(config, configToUpdate);
				((ConfigurationBase) configToUpdate).setVersion(config.getVersion());
				return true;
			}
		}
		return false;
	}

	private boolean setPluginConfiguration(IPlugin plugin, IConfiguration config) throws ValidationException
	{
		if (plugin.getConfiguration().getSerialVersionUID() == config.getSerialVersionUID())
		{
			plugin.setConfiguration(config);
			return true;
		}
		else
		{
			return findAndUpdateConfiguration(plugin.getConfiguration(), config);
		}
	}

	public UiBaseResponse updateConfiguration(ConfigurationUpdateRequest updatedConfig, IUser iuser) throws Exception
	{
		UiBaseResponse response = null;

		// First find the correct config
		IPlugin iplugin = null;
		IConfiguration iconfig = null;

		for (IPlugin plugin : configCache.values())
		{
			// Start looking here
			iconfig = findConfiguration(plugin.getConfiguration(), updatedConfig.getConfigurableSerialVersionUID());
			if (iconfig != null)
			{
				iplugin = plugin;
				break;
			}
		}

		if (iconfig == null)
			logger.error("Update Failed: Could not find configuration {}", updatedConfig.getConfigurableSerialVersionUID());
		else if (iplugin == null)
			logger.error("Update Failed: Could not find Plugin for configuration {}", updatedConfig.getConfigurableSerialVersionUID());

		if (iplugin == null || iconfig == null)
		{
			return null;
		}
		// Check the version
		response = new ConfigurationUpdateResponse();
		Configurable oldConfig = extractConfig(iconfig, iuser, true);
		if (oldConfig.getVersion() > updatedConfig.getVersion())
		{
			response = new ErrorResponse();
			logger.error("Update Failed: Version outdated {}", updatedConfig.getConfigurableSerialVersionUID());
			((ErrorResponse) response).setError("Configuration version outdated");
		}
		else
		{
			String configName = iplugin.getConfiguration().getName(Phrase.ENG);

			try
			{
				UiUtils utils = new UiUtils();
				logger.debug("Cloning configeration for {}", iconfig.getName("en"));
				Object clone = utils.clone(iconfig, iplugin, this); // This will not clone the methods and properties list!
				logger.debug("Configeration cloned");

				ConfigurationBase configClone = (ConfigurationBase) clone;
				utils.cloneNotificaitons(esb, iplugin.getConfiguration(), configClone);

				configClone.setVersion(iconfig.getVersion());

				// Update clone
				populateconfiguration(configClone, updatedConfig, configName, iuser, true);

				try
				{
					configClone.validate();
				}
				catch (Exception ve)
				{
					logger.error(ve.getMessage(), ve);
					response = new ErrorResponse();
					if (ve instanceof ValidationException)
					{
						ValidationException valEx = (ValidationException) ve;
						if (valEx.getField() != null)
							((ErrorResponse) response).setField(valEx.getField());
					}
					((ErrorResponse) response).setError("System Validation error: " + ve.getMessage());
					return response;
				}

				try
				{
					configClone.setVersion(configClone.getVersion() + 1);
					setPluginConfiguration(iplugin, configClone);
					iplugin.setConfiguration(iplugin.getConfiguration());

					if (updatedConfig.isSaveToDB())
					{
						logger.info("updating config as isSaveToDB == true ...");
						try (IDatabaseConnection connection = database.getConnection(null))
						{
							IConfiguration saveConfig = iplugin.getConfiguration();
							boolean result = saveConfig.save(connection, control);
							if ( !result ) logger.error("failed to save configuration {}", saveConfig.getSerialVersionUID());
						}
						catch (Exception e)
						{
							logger.error("Confguration should not be updated", e);
						}
					}
					else
					{
						logger.info("not updating config as isSaveToDB == false ...");
					}

					// All's well return the latest config
					for (IPlugin plugin : configCache.values())
					{
						// Start looking here
						iconfig = findConfiguration(plugin.getConfiguration(), updatedConfig.getConfigurableSerialVersionUID());
						if (iconfig != null)
						{
							iplugin = plugin;
							break;
						}
					}
					Configurable newConfig = extractConfig(iconfig, iuser, true);

					((ConfigurationUpdateResponse) response).setConfig(newConfig);

				}
				catch (ValidationException ve)
				{
					logger.info("SYSTEM UPDATE {} system update failed: {} ", configName, ve.getMessage());
					response = new ErrorResponse();
					((ErrorResponse) response).setError(ve.getMessage());
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				if (e instanceof ValidationException)
				{
					response = new ErrorResponse();
					ValidationException vs = (ValidationException) e;
					((ErrorResponse) response).setError(vs.getMessage());
					((ErrorResponse) response).setField(vs.getField());
					logger.debug("SYSTEM UPDATE {} system update failed, field:{} msg: {}", configName, vs.getField(), vs.getMessage());
				}
				else
				{
					String msg = (e instanceof InvocationTargetException) ? ((InvocationTargetException) e).getTargetException().getMessage() : e.getMessage();

					response = new ErrorResponse();
					((ErrorResponse) response).setError("Value rejected: " + msg);
					logger.debug("SYSTEM UPDATE {} system update failed: {} ", configName, msg);
				}

			}
		}
		return response;
	}

	private void populateconfiguration(IConfiguration config, ConfigurationUpdateRequest update, String serviceName, IUser iuser, boolean log) throws SecurityException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, ValidationException, NoSuchMethodException, InstantiationException
	{
		UiUtils utils = new UiUtils();
		String lastField = null;

		// Properties
		if (update.getParams() != null)
		{
			try
			{
				IPropertyInfo[] propertyInfo = config.getProperties();
				for (IConfigurableParam cp : update.getParams())
				{
					for (int i = 0; i < propertyInfo.length; i++)
					{
						if (propertyInfo[i].getName().equals(cp.getFieldName()))
						{
							lastField = propertyInfo[i].getName();
							// Check that this property is not read-only
							Method setterMethod = propertyInfo[i].getSetterMethod();
							boolean isProcessModel = false;

							if (setterMethod != null)
							{
								Object oldValue = propertyInfo[i].get(config);
								Object newValue = null;

								if (oldValue != null && oldValue instanceof IProcess)
								{
									isProcessModel = true;
									@SuppressWarnings("unchecked")
									List<IConfigurableParam[]> list = (List<IConfigurableParam[]>) cp.getValue();
									newValue = (String) list.get(0)[0].getValue();
									oldValue = ((IProcess) oldValue).getStart().serialize();
								}
								else if (cp.getValue() instanceof IConfigurableParam[])
								{
									IConfigurableParam[] values = (IConfigurableParam[]) cp.getValue();
									Class<?> type = setterMethod.getParameterTypes()[0];
									newValue = utils.configurableParamsToObject(type, values, config);
								}
								else if (cp.getValue() instanceof List<?>)
								{
									String propertyName = lastField;
									@SuppressWarnings("unchecked")
									List<IConfigurableParam[]> list = (List<IConfigurableParam[]>) cp.getValue();
									Class<?> type = setterMethod.getParameterTypes()[0].getComponentType();

									newValue = Array.newInstance(type, list.size());
									Object obj = null;
									for (int ind = 0; ind < list.size(); ind++)
									{
										lastField = String.format("%s[%d]", propertyName, ind);
										obj = utils.configurableParamsToObject(type, list.get(ind), config);
										if (obj != null)
										{
											Array.set(newValue, ind, obj);
										}
										obj = null;
									}
								}
								else if (setterMethod.getParameterTypes()[0].isArray() && (cp.getValue() instanceof String))
								{
									String propertyName = lastField;
									String[] values = ((String) (cp.getValue())).split(",");
									Class<?> type = setterMethod.getParameterTypes()[0].getComponentType();
									newValue = Array.newInstance(type, values.length);
									for (int ind = 0; ind < values.length; ind++)
									{
										lastField = String.format("%s[%d]", propertyName, ind);
										Object obj = null;
										if (type.equals(Integer.class) || type.equals(int.class))
											obj = Integer.parseInt(values[ind].trim());
										else if (type.equals(Long.class) || type.equals(long.class))
											obj = Long.parseLong(values[ind].trim());

										Array.set(newValue, ind, obj);
									}
								}
								else if (oldValue != null)
								{
									if (oldValue.getClass().equals(Boolean.class))
									{
										newValue = Boolean.valueOf(cp.getValue().toString());
									}
									else
									{
										newValue = cp.getValue();
									}
								} else 
								{
									newValue = cp.getValue();
								}

								if ((oldValue == null && newValue != null) || (oldValue != null && !oldValue.equals(newValue)))
								{
									if (log)
									{
										logUpdate(utils, iuser.getUserId(), serviceName, propertyInfo[i].getName(), oldValue, newValue);
									}
									try
									{
										if (isProcessModel)
										{
											// Deserialize and save
											String xml = (String) newValue;
											IProcess ip = Start.deserialize(xml);
											propertyInfo[i].set(config, ip);
										}
										else
										{
											propertyInfo[i].set(config, newValue);
										}
									}
									catch (Exception ex)
									{

										try
										{
											if (ex instanceof InvocationTargetException)
											{
												Throwable target = ((InvocationTargetException) ex).getTargetException();
												if (target instanceof SecurityException)
													throw new ValidationException("You have insufficient permissions to update fields");
												else if (target instanceof ValidationException)
													throw (ValidationException) target;
												// else if (target instanceof ValidationException)
												// throw new ValidationException(target.getMessage());
											}
											UiConnectorUtils.setUsingString(config, String.valueOf(newValue), setterMethod);
										}
										catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
										{
											logger.error(e.getMessage(), e);
											if (e instanceof IllegalArgumentException)
											{
												throw new ValidationException(e, "Format for field %s invalid", propertyInfo[i].getName());
											}
											else if (e instanceof InvocationTargetException)
											{
												Throwable target = ((InvocationTargetException) e).getTargetException();
												if (target instanceof SecurityException)
													throw new ValidationException("You have insufficient permissions to update fields");
												throw new ValidationException("%s invalid: %s", propertyInfo[i].getName(), target.getMessage());
											}
											else
											{
												throw e;
											}

										}
									}
								}
								break;
							}
						}
					}
				}
			}
			catch (Exception e)
			{
				if (e instanceof ValidationException)
				{
					ValidationException ve = (ValidationException) e;
					String field = String.format("%s", lastField);
					if (ve.getField() != null)
					{
						field = String.format("%s.%s", lastField, ve.getField());
					}
					throw ValidationException.createFieldValidationException(field, ve.getMessage(), e);
				}
			}

		}

		// Notifications
		INotifications notifications = config.getNotifications();

		if (notifications != null && update.getNotifications() != null && update.getNotifications().length > 0)
		{
			int[] ids = notifications.getNotificationIds();
			if (ids != null && ids.length > 0)
			{
				for (ConfigNotification not : update.getNotifications())
				{
					for (int i = 0; i < ids.length; i++)
					{
						if (ids[i] == not.getNotificationId())
						{
							INotification toSave = config.getNotifications().getNotification(not.getNotificationId());
							if (toSave != null)
							{
								for (int langIndex = 0; langIndex < not.getText().length; langIndex++)
								{
									try
									{
										if (not.getText()[langIndex] != null)
										{
											try
											{
												String oldText = (toSave.getText(langIndex + 1) == null) ? "" : toSave.getText(langIndex + 1);
												String newText = (not.getText()[langIndex] == null) ? "" : not.getText()[langIndex];
												if (!oldText.equals(newText))
												{
													config.updateNotification(not.getNotificationId(), langIndex + 1, newText);
												}
											}
											catch (IllegalArgumentException e)
											{
												throw new ValidationException("Invalid property identifier %s in %s", e.getMessage(), toSave.getDescription());
											}
											catch (Exception e)
											{
											}

											// toSave.setText(langIndex + 1, not.getText()[langIndex]);
										}
									}
									catch (ValidationException e)
									{
										logger.error(e.getMessage(), e);
										throw e;
									}
									catch (Exception e)
									{
										logger.error(e.getMessage(), e);
										throw new ValidationException("You have insufficient permissions to update Notifications");
									}
								}
							}
						}
					}

				}
			}
		}

	}

	private static final int VALUE_LOGGING_LENGTH = 100;

	public void logUpdate(UiUtils utils, String user, String service, String prop, Object oldValue, Object newValue)
	{
		if ((newValue != null) && (newValue instanceof Object[]))
		{
			Object[] oa = (Object[]) newValue;
			for (int i = 0; i < oa.length; i++)
			{
				String action = "New";
				Object obj = oa[i];

				if (oldValue != null && oldValue instanceof Object[])
				{
					Object oa2[] = (Object[]) oldValue;
					for (int j = 0; j < oa2.length; j++)
					{
						Object oldObj = oa2[j];
						if (oldObj != null && oldObj.equals(obj))
						{
							action = "Edited";
							oa2[j] = null;
							break;
						}
					}
				}

				String snew = utils.trimToLength(obj, VALUE_LOGGING_LENGTH);
				logger.info("User:[{}] Service:[{}] Prop:[{}] IND:[{}] {}:[{}]", user, service, prop, i, action, snew);
			}

			if (oldValue != null && oldValue instanceof Object[])
			{
				oa = (Object[]) oldValue;
				for (int i = 0; i < oa.length; i++)
				{
					if (oa[i] == null)
						continue;

					String snew = utils.trimToLength(oa[i], VALUE_LOGGING_LENGTH);
					logger.info("User:[{}] Service:[{}] Prop:[{}] IND:[{}] Removed:[{}]", user, service, prop, i, snew);
				}
			}

		}
		else
		{
			String sold = utils.trimToLength(oldValue, VALUE_LOGGING_LENGTH);
			String snew = utils.trimToLength(newValue, VALUE_LOGGING_LENGTH);
			logger.info("User:[{}] Service:[{}] Prop:[{}] Old:[{}] New:[{}]", user, service, prop, sold, snew);
		}
	}

	public void systemDiscovery(IUser user)
	{
		try
		{
			retrieveAllConfigurable(user);
		}
		catch (Exception e)
		{
		}
	}

	private String getPluginName(IPlugin plugin)
	{
		String name = plugin.getClass().getSimpleName();
		if (plugin.getConfiguration() != null)
		{
			name = plugin.getConfiguration().getName(Phrase.ENG);
		}
		return name;
	}

	// Fitness checking
	public FitnessResponse findSystemFitness(String userId, String sessionId)
	{
		FitnessResponse response = new FitnessResponse(userId, sessionId);
		
		try 
		{
			
			// Extract fitness for Services
			List<IService> services = esb.getServices(IService.class);
			List<ComponentFitness> serviceFitness = new ArrayList<>();
			if (services != null)
			{
				for(IService service : services)
				{
					// Possible security access exception when trying to retrieve information
					try
					{
						ComponentFitness comp = new ComponentFitness(getPluginName(service), service.isFit());
						serviceFitness.add(comp);
					}
					catch(SecurityException se) {} // Ignore since only way of iterating list
				}
			}
			response.setServices( serviceFitness.toArray( new ComponentFitness[serviceFitness.size()] ) );
			
			// Extract fitness for Connectors
			List<IConnector> connectors = esb.getConnectors(IConnector.class);
			List<ComponentFitness> connectorFitness = new ArrayList<>();
			if (connectors != null)
			{
				for(IConnector connector : connectors)
				{
					try
					{
						ComponentFitness comp = new ComponentFitness(getPluginName(connector), connector.isFit());
						connectorFitness.add(comp);
					}
					catch(SecurityException se) {}
				}
			}
			response.setComponents( connectorFitness.toArray( new ComponentFitness[connectorFitness.size()] ) );
			
		}
		catch(Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return response;
	}

	public SendSMSResponse sendSms(final SendSMSRequest request)
	{
		ISmsConnector smsConnector = esb.getFirstConnector(ISmsConnector.class);
		if (smsConnector == null)
			return new SendSMSResponse(request.getUserId(), request.getSessionId(), "Failed to find SMS sending medium.");

		String response = "Failed to Send SMS.";
		if (smsConnector.send(request.getFromMSISDN(), request.getToMSISDN(), new INotificationText()
		{

			@Override
			public String getText()
			{
				return request.getMessage();
			}

			@Override
			public String getLanguageCode()
			{
				return esb.getLocale().getDefaultLanguageCode();
			}
		}, true))
			response = "Sent Message.";

		return new SendSMSResponse(request.getUserId(), request.getSessionId(), response);
	}

	public RevertServiceResponse revertService(IUser user, RevertServiceRequest request)
	{
		if ((user.getRoles() & 1) <= 0)
			return new RevertServiceResponse(request.getUserId(), request.getSessionId(), "Do not have permissions to revert.");

		String serviceId = request.getServiceID();
		List<VasService> services = esb.getServices(VasService.class);
		IService service = null;
		for (VasService vas : services)
		{
			if (vas.getServiceID().equalsIgnoreCase(serviceId))
			{
				service = (IService) vas;
				break;
			}
		}

		if (service == null)
			return new RevertServiceResponse(request.getUserId(), request.getSessionId(), "Could not find the service to revert.");

		ClassInfo info = new ClassInfo(service.getClass());
		IService newService = null;
		try
		{
			newService = info.newInstance();
		}
		catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException exc)
		{
			logger.error("Unable to revert {}. Cannot create new instance: {}", service.getConfiguration().getName(Phrase.ENG), exc.getMessage());
			return null;
		}

		try (IDatabaseConnection con = database.getConnection(null))
		{
			service.setConfiguration(newService.getConfiguration());
			service.getConfiguration().save(con, control);
		}
		catch (Exception exc)
		{
			logger.error("Could not save the service {} due to {}", request.getServiceID(), exc.getMessage());
			return null;
		}

		return new RevertServiceResponse(request.getUserId(), request.getSessionId(), "Revert Successful");
	}

	/**
	 * Get System Default ReturnCodeTexts
	 * 
	 * @param userId
	 *            User Identifier
	 * @param sessionId
	 *            Session Identifier
	 * @return
	 */
	public ReturnCodeTextDefaultsResponse retrieveReturnCodeTextDefaults(String userId, String sessionId)
	{
		ReturnCodeTextDefaultsResponse result = null;
		try
		{
			ISoapConnector isoap = esb.getFirstConnector(ISoapConnector.class);
			if (isoap == null)
			{
				throw new Exception("SOAP Connector not found");
			}

			int maxLanguageID = esb.getLocale().getMaxLanguages();
			for (int languageID = esb.getLocale().getMaxLanguages(); languageID > 0; languageID--)
			{
				if (esb.getLocale().getLanguage(languageID) != null && esb.getLocale().getLanguage(languageID).length() > 0)
				{
					maxLanguageID = languageID;
					break;
				}
			}
			if (maxLanguageID < 2)
				maxLanguageID = 2;

			List<ReturnCodeTexts> codesList = new ArrayList<>();
			for (ReturnCodes rc : ReturnCodes.values())
			{
				ReturnCodeTexts rct = new ReturnCodeTexts(rc, new Phrase());
				for (int languageID = 1; languageID <= maxLanguageID; languageID++)
				{
					GetReturnCodeTextRequest retCodeRequest = new GetReturnCodeTextRequest();
					retCodeRequest.setReturnCode(rc);
					retCodeRequest.setLanguageID(languageID);
					GetReturnCodeTextResponse retCodeResponse = isoap.getVasInterface().getReturnCodeText(retCodeRequest);
					if (retCodeResponse.getReturnCodeText() != null)
					{
						String languageCode = esb.getLocale().getLanguage(languageID);
						rct.getPhrase().set(languageCode, retCodeResponse.getReturnCodeText());
					}
				}
				codesList.add(rct);
			}
			result = new ReturnCodeTextDefaultsResponse(userId, sessionId);
			result.setDefaultReturnTexts(codesList.toArray(new ReturnCodeTexts[codesList.size()]));
		}
		catch (Exception e)
		{
			logger.error("Could not retrieve default return codes due to {}", e.getMessage());
			return null;
		}
		return result;
	}

	public VasCommandsResponse extractVasCommands(IUser user, VasCommandsRequest vasRequest)
	{
		VasCommandsResponse result = new VasCommandsResponse(user.getUserId(), vasRequest.getSessionId());
		IPlugin iplugin = null;

		if (configCache.size() < 1)
		{
			// Populate the config cache
			retrieveAllConfigurable(user);
		}

		for (IPlugin plugin : configCache.values())
		{
			IConfiguration iconfig = findConfiguration(plugin.getConfiguration(), vasRequest.getConfigurationUID());
			if (iconfig != null)
			{
				iplugin = plugin;
				break;
			}
		}

		if (iplugin instanceof VasService)
		{
			VasService vs = (VasService) iplugin;
			String[] variables = vs.getCommandParser().getCommandVariables();
			result.setCommandVariables(variables);
		}
		return result;
	}

	public GetFacilityResponse getFacility(GetFacilityRequest request)
	{
		GetFacilityResponse response = new GetFacilityResponse(request.getUserId(), request.getSessionId());

		IFacilityRegistration facility = esb.hasFacility(request.getFacilityID());
		if (facility != null)
			response.setFacility(new FacilityRegistration(facility));

		return response;
	}

	public GetLicenseDetailsResponse getLicenseDetails(GetLicenseDetailsRequest request)
	{
		GetLicenseDetailsResponse response = new GetLicenseDetailsResponse(request.getUserId(), request.getSessionId());
		response.setLicenseDetails(new Registration(esb.getLastRegistration()));
		return response;
	}

	public UiBaseResponse getAirsimHistory(GetAirsimHistoryRequest request)
	{
		GetAirsimHistoryResponse response = new GetAirsimHistoryResponse(request.getUserId(), request.getSessionId());

		IAirSimService airsim = esb.getFirstService(IAirSimService.class);
		if (airsim != null)
		{
			ICdr cdrHistory[] = airsim.getCdrHistory();
			if (cdrHistory != null && cdrHistory.length > 0)
			{
				List<Cdr> cdrs = new ArrayList<Cdr>();
				for (ICdr cdr : cdrHistory)
					if (cdr != null)
						cdrs.add(new Cdr(cdr));

				response.setCdrHistory(cdrs.toArray(new Cdr[cdrs.size()]));
			}

			ISmsHistory smsHistory[] = airsim.getSmsHistory();
			if (smsHistory != null && smsHistory.length > 0)
			{
				List<SmsHistory> smss = new ArrayList<SmsHistory>();
				for (ISmsHistory sms : smsHistory)
					if (sms != null)
						smss.add(new SmsHistory(sms.getTime(), sms.getFromMSISDN(), sms.getToMSISDN(), sms.getText()));

				response.setSmsHistory(smss.toArray(new SmsHistory[smss.size()]));
			}

		}
		ISmtpConnector smtpConnector = esb.getFirstConnector(ISmtpConnector.class);
		logger.trace("UiServiceConfiguration.getAirsimHistory: smtpConnector = {}", smtpConnector);
		if ( smtpConnector != null )
		{
			ISmtpHistory[] emailMessages = smtpConnector.getHistory();
			if (emailMessages != null && emailMessages.length > 0)
			{
				logger.trace("Found history on ISmtpConnector {}", emailMessages.length);
				response.setEmailHistory(emailMessages);
			}
			else
			{
				logger.trace("No history from ISmtpConnector ... not sending history");
			}
		}
		else
		{
			logger.trace("No ISmtpConnector ... not sending history");
		}

		return response;
	}
	
	public UiBaseResponse clearAirSimHistory(AirSimCommonRequest request)
	{
		IAirSimService airsim = esb.getFirstService(IAirSimService.class);
		if (airsim != null)
		{
			airsim.clearSmsHistory();
			return new ConfirmationResponse(request.getUserId(), request.getSessionId());
		}
		return new ErrorResponse(request.getUserId(), ErrorCode.GENERAL);
	}

	public UiBaseResponse clearAirSimEmailHistory(AirSimCommonRequest request)
	{
		ISmtpConnector smtpConnector = esb.getFirstConnector(ISmtpConnector.class);
		logger.trace("UiServiceConfiguration.clearAirSimHistory: smtpConnector = {}", smtpConnector);
		if ( smtpConnector != null )
		{
			smtpConnector.clearHistory();
			return new ConfirmationResponse(request.getUserId(), request.getSessionId());
		}
		return new ErrorResponse(request.getUserId(), ErrorCode.GENERAL);
	}
	
	public UiBaseResponse getTamperedAccounts(GetEcdsTamperCheckRequest request)
	{
		GetEcdsTamperCheckResponse response = new GetEcdsTamperCheckResponse(request.getUserId(), request.getSessionId());
		ITamperCheckConnector tamperCheckConnector = esb.getFirstConnector(ITamperCheckConnector.class);
		if (tamperCheckConnector != null)
		{			
			ITamperedAccount accounts[] = tamperCheckConnector.getTamperedAccounts();
			if (accounts != null && accounts.length > 0)
			{				
				response.setTamperedAccounts( accounts );				
			}
		}
		return response;
	}
	
	public UiBaseResponse getTamperedAgents(GetEcdsTamperCheckRequest request)
	{
		GetEcdsTamperCheckResponse response = new GetEcdsTamperCheckResponse(request.getUserId(), request.getSessionId());
		ITamperCheckConnector tamperCheckConnector = esb.getFirstConnector(ITamperCheckConnector.class);
		if (tamperCheckConnector != null)
		{			
			ITamperedAgent agents[] = tamperCheckConnector.getTamperedAgents();
			if (agents != null && agents.length > 0)
			{				
				response.setTamperedAgents(agents);				
			}
		}
		return response;
	}
	
	public UiBaseResponse getTamperedAuditEntries(GetEcdsTamperCheckRequest request)
	{
		GetEcdsTamperCheckResponse response = new GetEcdsTamperCheckResponse(request.getUserId(), request.getSessionId());
		ITamperCheckConnector tamperCheckConnector = esb.getFirstConnector(ITamperCheckConnector.class);
		if (tamperCheckConnector != null)
		{			
			ITamperedAuditEntry auditEntries[] = tamperCheckConnector.getTamperedAuditEntries();
			if (auditEntries != null && auditEntries.length > 0)
			{				
				response.setTamperedAuditEntries(auditEntries);				
			}
		}
		return response;
	}
	
	public UiBaseResponse getTamperedBatches(GetEcdsTamperCheckRequest request)
	{
		GetEcdsTamperCheckResponse response = new GetEcdsTamperCheckResponse(request.getUserId(), request.getSessionId());
		ITamperCheckConnector tamperCheckConnector = esb.getFirstConnector(ITamperCheckConnector.class);
		if (tamperCheckConnector != null)
		{			
			ITamperedBatch batches[] = tamperCheckConnector.getTamperedBatches();
			if (batches != null && batches.length > 0)
			{				
				response.setTamperedBatches(batches);				
			}
		}
		return response;
	}
	
	
	public UiBaseResponse getEcdsTampering(GetEcdsTamperCheckRequest request)
	{
		switch(request.getEntity())
		{
		//Not implemented due to performance issues relating to data size 
		//case ACCOUNT:
		//	return getTamperedAccounts(request);		
		//case AGENT:
		//	return getTamperedAgents(request);
		case AUDITENTRY:
			return getTamperedAuditEntries(request);
		case BATCH:
			return getTamperedBatches(request);
		default:
			return null;		
		}
	}
	
	public UiBaseResponse resetEcdsTampering(GetEcdsTamperResetRequest request)
	{
		GetEcdsTamperResetResponse response = new GetEcdsTamperResetResponse(request.getUserId(), request.getSessionId());

		ITamperCheckConnector tamperCheckConnector = esb.getFirstConnector(ITamperCheckConnector.class);
		if (tamperCheckConnector != null)
		{
			boolean result = false;
			GetEcdsTamperResetRequest.Entity entity = request.getEntity();
			String msisdn = request.getMsisdn();
			if(msisdn != null && !msisdn.isEmpty())
			{
				switch(entity)
				{
				case ACCOUNT:
					result = tamperCheckConnector.resetAccount(msisdn);
					break;
				case AGENT:
					result = tamperCheckConnector.resetAgent(msisdn);
					break;
				default:
					break;
				}
			} else {
				switch(entity)
				{
				case ACCOUNT:
					result = tamperCheckConnector.resetAccounts();
					break;
				case AGENT:
					result = tamperCheckConnector.resetAgents();
					break;
				case AUDITENTRY:
					result = tamperCheckConnector.resetAuditEntries();
					break;
				case BATCH:
					result = tamperCheckConnector.resetBatches();
					break;
				}
			}
			response.setResult(result);			
		}
		return response;
	}
	
	public UiBaseResponse checkTamperedAgent(GetEcdsCheckTamperedAgentRequest request)
	{
		GetEcdsCheckTamperedAgentResponse response = new GetEcdsCheckTamperedAgentResponse(request.getUserId(), request.getSessionId());
		ITamperCheckConnector tamperCheckConnector = esb.getFirstConnector(ITamperCheckConnector.class);
		if (tamperCheckConnector != null)
		{
			int result = 0;
			String msisdn = request.getMsisdn();
			result = tamperCheckConnector.checkTamperedAgent(msisdn);
			response.setAccountTampered((result & 2) == 2);
			response.setAgentTampered((result & 1) == 1);
			response.setResult(true);			
		}
		return response;
	}
	
	// ---------------------------- Air Simulator Service Calls -------------------------------
	
	public UiBaseResponse airSimUsageRequest(AirSimGetUsageRequest request)
	{
		AirSimGetUsageResponse response = new AirSimGetUsageResponse(request.getUserId(), request.getSessionId());
		
//		IAirSimProvider airSimProvider = esb.getFirstService(IAirSimProvider.class);
//		
//		if (airSimProvider != null)
//		{
//			// Access Airsim directly
//			IAirSim airsim = airSimProvider.getAirSim();
//			if (airsim != null)
//			{
//				GetUsageResponse usageResponse = airsim.getUsageTimers(null);
//				if (usageResponse != null)
//				{
//					response.setAirSimUsage(new AirSimMSISDNUsage[usageResponse.getUsageTimers().length]);
//					for(int i=0; i<usageResponse.getUsageTimers().length; i++)
//					{
//						UsageTimer ut = usageResponse.getUsageTimers()[i];
//						AirSimMSISDNUsage.TimeUnits timeUnit = AirSimMSISDNUsage.TimeUnits.valueOf(ut.getTimeUnit().toString()); 
//						response.getAirSimUsage()[i] = new AirSimMSISDNUsage(ut.getMsisdn(), 
//								ut.getAccount(), 
//								ut.getAmount(), 
//								ut.getInterval(), 
//								timeUnit, 
//								ut.getStandardDeviation(), 
//								ut.getTopupValue());
//					}					
//				}
//			}
//		}
		
		return response;
	}
	
	public UiBaseResponse airSimUsageStartRequest(AirSimStartUsageRequest request)
	{
		AirSimStartUsageResponse response = new AirSimStartUsageResponse(request.getUserId(), request.getSessionId());
		
//		IAirSimProvider airSimProvider = esb.getFirstService(IAirSimProvider.class);
//		
//		if (airSimProvider != null)
//		{
//			// Access Airsim directly
//			IAirSim airsim = airSimProvider.getAirSim();
//			if (airsim != null)
//			{
//				
//				List<String> msisdns = createMsisdnList( request.getMsisdnUsage().getMsisdn() );
//				
//				UsageTimer []  usageTimers = new UsageTimer[msisdns.size()];
//				for(int i=0; i<msisdns.size(); i++)
//				{
//					usageTimers[i] =  new UsageTimer();
//					usageTimers[i].setMsisdn( msisdns.get(i) );
//					usageTimers[i].setAmount( request.getMsisdnUsage().getAmount() );
//					usageTimers[i].setAccount( request.getMsisdnUsage().getAccount() );
//					usageTimers[i].setInterval( request.getMsisdnUsage().getInterval() );
//					usageTimers[i].setTimeUnit( TimeUnits.valueOf( request.getMsisdnUsage().getTimeUnit().toString() ));
//					usageTimers[i].setStandardDeviation( request.getMsisdnUsage().getStandardDeviation() );
//					usageTimers[i].setTopupValue( request.getMsisdnUsage().getTopupValue() );
//				}
//				
//				// Try and start on AIR
//				StartUsageRequest startRequest = new StartUsageRequest();
//				startRequest.setUsageTimers(usageTimers);
//				response.setStarted( airsim.startUsageTimers(startRequest) );
//			}
//		}
		
		return response;
	}
	
	public UiBaseResponse airSimUsageStopRequest(AirSimStopUsageRequest request)
	{
		AirSimStopUsageResponse response = new AirSimStopUsageResponse(request.getUserId(), request.getSessionId());
		
//		IAirSimProvider airSimProvider = esb.getFirstService(IAirSimProvider.class);
//		
//		if (airSimProvider != null)
//		{
//			// Access Air simulator directly
//			IAirSim airsim = airSimProvider.getAirSim();
//			if (airsim != null)
//			{
//				// airsim.stopUsage provides a boolean confirmation of stopping
//				boolean result =  airsim.stopUsageTimers(request.getMsisdn());
//				response.setUsageStopped( result );
//			}
//		}
		
		return response;
	}
	
	public UiBaseResponse airSimUssdRequest(AirSimUssdRequest request)
	{
		AirSimUssdResponse response = new AirSimUssdResponse(request.getUserId(), request.getSessionId());
		
		IAirSimService airService = esb.getFirstService(IAirSimService.class);
		String imsi = (request.getImsi() == null || request.getImsi().length() == 0)? null : request.getImsi();
		
		IUssdResponse ussdResponse = airService.injectMOUssd(request.getMsisdn(), request.getUssd(), imsi);
		response.setLast(ussdResponse.isLast());
		response.setText(ussdResponse.getText());
		
		return response;
	}
	
	public UiBaseResponse airSimSMSRequest(AirSimSmsRequest request)
	{
		AirSimSmsResponse response = new AirSimSmsResponse(request.getUserId(), request.getSessionId());
		
		IAirSimService airService = esb.getFirstService(IAirSimService.class);
		airService.injectMOSms(request.getFrom(), request.getTo(), request.getText());
		
		return response;
	}
	
	public UiBaseResponse injectAirResponseRequest(AirResponseUpdateRequest request)
	{
		AirResponseUpdateResponse response = new AirResponseUpdateResponse(request.getUserId(), request.getSessionId());
		
		IAirSimService airService = esb.getFirstService(IAirSimService.class);
		String airCall = request.getAirCall();
		String responseCode = request.getResponseCode();
		String delay = request.getDelay();		
		airService.injectAirResponse(airCall, responseCode, delay);
		return response;
	}
	
	
	public UiBaseResponse resetInjectedAirResponseRequest(AirResponseResetRequest request)
	{
		AirResponseResetResponse response = new AirResponseResetResponse(request.getUserId(), request.getSessionId());
		
		IAirSimService airService = esb.getFirstService(IAirSimService.class);
		String airCall = request.getAirCall();
		airService.resetInjectedAirResponse(airCall);
		return response;
	}
	
	
	// ---------------------------- Metric discovery and registration -------------------------------

	public void subscribeToMetric(IUser user, String sessionId)
	{

	}

	// ----------------------------------------------------------------------------------------------

	// HELPER Methods
	public static void saveDataToFile(String data, String file)
	{
		try (FileWriter fw = new FileWriter(new File(file)))
		{
			fw.write(data);
			fw.flush();
		}
		catch (IOException ioe)
		{
		}
	}

	// HELPER Calsses

}
