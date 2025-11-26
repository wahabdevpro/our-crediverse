package hxc.userinterfaces.gui.controller.service.confighandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.context.WebContext;

import hxc.processmodel.IProcess;
import hxc.servicebus.ReturnCodes;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;
import hxc.userinterfaces.gui.data.LocaleInfo;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.processmodel.LocaleResponse;
import hxc.userinterfaces.gui.processmodel.ProcessModelTreeGenerator;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.FieldExtractor;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;

public abstract class BaseServiceConfigHandler
{
	final static Logger logger = LoggerFactory.getLogger(BaseServiceConfigHandler.class);
	public static final String LANG_VARIABLE = "langs";
	public static final String LANGUAGES_VARIABLE = "languages";
	public static final String CURRENCY_DIGITS = "curDigits";
	public static final String CURRENCY_SYMBOLS = "curSymbol";

	protected void extractUSSDMenuProcess(HttpSession session, WebContext ctx, ConfigurableResponseParam[] fa, String PROCESS_USSD_VARIABLE, String PROCESS_ACTION_JSON_VARIABLE, String PROCESS_ACTION_ACTION_VARIABLES, String PROCESS_ACTION_PROCESS_VARIABLE)
	{
		ProcessModelTreeGenerator pmg = new ProcessModelTreeGenerator();
		for (ConfigurableResponseParam cp : fa)
		{
			Class<?> clsType = GuiUtils.stringToClass(cp.getValueType());
			if (clsType != null && (IProcess.class.isAssignableFrom(clsType)))
			{
				session.setAttribute(PROCESS_USSD_VARIABLE, cp);
				pmg.extractProcessModel(ctx, session, PROCESS_ACTION_JSON_VARIABLE, cp.getValue().toString(), PROCESS_ACTION_ACTION_VARIABLES, PROCESS_ACTION_PROCESS_VARIABLE);
				break;
			}
		}
	}
	
	protected Configurable udpateUSSDMenuProcess(HttpSession session, User user, int iversion, long luid, String PROCESS_ACTION_PROCESS_VARIABLE, String PROCESS_USSD_VARIABLE, String USSD_UPDATED)
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
	
	protected void extractLocale(WebContext ctx, User user, HttpSession session) throws Exception
	{
		// Locale for texts
		GetLocaleInformationResponse locale = UiConnectionClient.getInstance().extractLocaleInformation(user);
		ArrayList<LocaleInfo> languages = new ArrayList<>();
		for (int i = 0; i < locale.getLanguages().size(); i++)
		{
			languages.add(i, GuiUtils.extractLanguageInfo(locale.getLanguage(i)));
		}
		ctx.setVariable(LANG_VARIABLE, languages);
		session.setAttribute(CURRENCY_SYMBOLS, locale.getCurrencyCode());
		session.setAttribute(CURRENCY_DIGITS, locale.getCurrencyDecimalDigits());

		// Language information for Texts
		GetLocaleInformationResponse locResp = UiConnectionClient.getInstance().extractLocaleInformation(user);
		LocaleResponse resp = new LocaleResponse();
		resp.setLang(locResp.getLanguages().toArray(new String[locResp.getLanguages().size()]));
		resp.convertShortCodesToFullLanuage();
		ctx.setVariable("directions", resp.getDir());
		ctx.setVariable(LANGUAGES_VARIABLE, resp.getLang());
	}
	
	protected Configurable persistReturnCodeConfiguration(User user, long luid, int iversion, Map<String, String[]> parms, String returnCodeFieldName) throws Exception
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
	
	protected void extractAdvancedConfiguration(HttpSession session, WebContext ctx, User user, Configurable config) throws Exception
	{
		extractLocale(ctx, user, session);
		FieldExtractor fe = new FieldExtractor(config, session, ctx)
		{
			@Override
			public void fieldsExtracted(String name, List<BasicConfigurableParm[]> fields)
			{
				logger.info("Field: " + name);	
				this.getSession().setAttribute(name, fields);
			}

			@Override
			public void objectStructure(String name, String type, ConfigurableResponseParam[] structure)
			{
				this.getSession().setAttribute(name + ComplexConfiguration.STRUCTURE_SUFFIX, structure);
				this.getSession().setAttribute(name + ComplexConfiguration.TYPE_SUFFIX, type);
			}

			@Override
			public void nonArrayFieldExtract(String name, ConfigurableResponseParam[] fields)
			{
				this.getSession().setAttribute(name, fields);
			}

		};

		fe.findStructuredConfigs();
		session.setAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_STRUCT_LIST, fe.getStructuredFieldList());
		session.setAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_UNSTRUCT_LIST, fe.getUnStructuredFieldList());
	}
	
	protected Configurable saveUnstructuredConfiguration(HttpSession session, User user, long luid, int version, Map<String, String[]> parms)
	{
		Configurable updated = null;

		try
		{
			int iversion = version;
			@SuppressWarnings("unchecked")
			List<String> unstructureConfigFields = (List<String>) session.getAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_UNSTRUCT_LIST);
			if (unstructureConfigFields != null)
			{
				for (String fieldName : unstructureConfigFields)
				{
					ConfigurableResponseParam[] currentFieldValues = (ConfigurableResponseParam[]) session.getAttribute(fieldName);
					IConfigurableParam[] newFieldValues = new IConfigurableParam[currentFieldValues.length];
					for (int i = 0; i < currentFieldValues.length; i++)
					{
						String pageField = "CMPL3X_" + fieldName + "_" + currentFieldValues[i].getFieldName();
						if (parms.containsKey(pageField))
						{
							String newValue = parms.get(pageField)[0];
							newFieldValues[i] = new BasicConfigurableParm(currentFieldValues[i].getFieldName(), newValue);
						}
					}

					// Persist to back end
					updated = UiConnectionClient.getInstance().saveConfigurationUnStructured(user, luid, iversion, fieldName, newFieldValues, false);
					if (updated != null)
					{
						iversion = updated.getVersion();
					}
				}
			}
		}
		catch (Exception e)
		{
		}

		return updated;
	}
	
	public abstract long getServiceID();
	
	public abstract String processContentRequestHandler(HttpSession session, WebContext ctx, Configurable config, User user) throws Exception;
	
	public abstract Configurable processUpdateRequestHandler(HttpSession session, long luid, int iversion, Map<String, String[]> parms, User user) throws Exception;	
}
