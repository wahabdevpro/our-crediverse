package hxc.userinterfaces.gui.controller.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.servicebus.ReturnCodes;
import hxc.services.ServiceType;
import hxc.services.notification.IPhrase;
import hxc.services.notification.Phrase;
import hxc.services.notification.Texts;
import hxc.userinterfaces.gui.controller.manage.ServiceConfigurationLoader;
import hxc.userinterfaces.gui.controller.service.confighandlers.BaseServiceConfigHandler;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.userinterfaces.gui.utils.UssdProcessHelper;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

public class GroupSharedAccounts implements IThymeleafController
{

	// Will need to store the structure of Objects for creating new Objects
	public static final String SC_FIELD_STRUCT = "scstruct";
	public static final String VAR_FIELD_STRUCT = "varstruct";
	public static final String QUOTA_FIELD_STRUCT = "quotastruct";

	// Fields from configuration (Also used for session)
	public static final String SC_FIELD = "ServiceClasses";
	public static final String VAR_FIELD = "Variants";
	public static final String QUOTA_FIELD = "Quotas";

	// Page variables
	public static final String REQ_QUOTA = "quota";
	public static final String REQ_VARIANT = "var";
	public static final String REQ_SERVICE_CLASS = "sc";

	// Update status variables
	public static final String SC_FIELD_UPDATED = "serviceclassesupdated";
	public static final String QUOTA_FIELD_UPDATED = "quotasupdated";
	public static final String VAR_FIELD_UPDATED = "variantsupdated";

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext, TemplateEngine templateEngine) throws Exception
	{

		WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		HttpSession session = request.getSession(true);

		User user = (User) session.getAttribute("user");

		String action = request.getParameter("act");
		String component = request.getParameter("comp");
		int index = -1;
		try
		{
			index = Integer.parseInt(request.getParameter("index"));
		}
		catch (Exception e)
		{
		}
		String json = null;
		String page = null;

		if (action != null)
		{
			String node = request.getParameter("node");
			String aid = request.getParameter("aid"); // Attribute ID (e.g. Caption)
			String nid = request.getParameter("nid"); // NodeId (e.g. 421604538)

			UssdProcessHelper ussdHelper = null;

			if (!validateUserSession(user))
			{
				json = sendInvalidUserJson();
			}
			else
			{
				switch (action)
				{
				// Called for node update
					case "propdata":
						ussdHelper = new UssdProcessHelper();
						json = ussdHelper.extractPropertyData(session, nid, aid, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);
						break;
					case "propupd":
						ussdHelper = new UssdProcessHelper();
						json = ussdHelper.savePropertyData(session, request, nid, aid, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);
						break;
					case "rcdata":
						// ReturnCodes
						json = extractReturnCodes(session);
						break;
					case "edit":
						String prop = request.getParameter("prop");
						ussdHelper = new UssdProcessHelper();
						json = ussdHelper.extractPropertyData(session, node, prop, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);
						break;
					case "lang":
						ussdHelper = new UssdProcessHelper();
						json = ussdHelper.extractSystemLanguageInfo(user);
						break;
					case "data":
						// request node data
						if (component == null)
						{
							ussdHelper = new UssdProcessHelper();
							json = ussdHelper.extractNodeData(session, node, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE, ServiceConfigurationLoader.PROCESS_ACTION_ACTION_VARIABLES);
						}
						else
						{
							json = extractStructuredConfig(component, session, index);
						}
						break;
					case "del":
						json = deleteConfig(component, session, index);
						break;
					case "upd":
						if (component != null)
						{
							// Modal Add / Edit update
							json = updateComponent(session, request, component, index);
						}
						else
						{
							// Process model update
							GuiUpdateResponse gur = null;
							try
							{
								ussdHelper = new UssdProcessHelper();
								ussdHelper.updateAction(session, node, request, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);
								gur = new GuiUpdateResponse(OperationStatus.pass, "Update passed");
							}
							catch (Exception e)
							{
								gur = new GuiUpdateResponse(OperationStatus.fail, String.format("update failed: %s", e.getMessage()));
							}
							GuiUtils.sendResponse(response, gur.toString());
						}
						break;
					case "refresh":
						page = getComponentPage(component);
						break;
				}
			}

			// Check on session information
			List<?> list = (List<?>) session.getAttribute(SC_FIELD);

			if (page == null)
				GuiUtils.sendResponse(response, json);
			else
			{
				ctx.setVariable("utils", new GuiUtils());
				templateEngine.process(page, ctx, response.getWriter());
			}
		}
	}

	private boolean validateUserSession(User user)
	{
		return UiConnectionClient.getInstance().validateSession(user);
	}

	private String sendInvalidUserJson()
	{
		GuiUpdateResponse resp = new GuiUpdateResponse(OperationStatus.fail, "USER_INVALID");
		return resp.toString();
	}

	private String extractReturnCodes(HttpSession session)
	{
		JsonObject job = new JsonObject();
		JsonArray jarr = new JsonArray();
		for (ReturnCodes rc : ReturnCodes.values())
		{
			jarr.add(new JsonPrimitive(rc.toString()));
		}
		job.add("returncodes", jarr);

		return job.toString();
	}

	// ------------------------------------------------------------------------------

	// --------------------------------- STRUCTURED CONFIGURATION ---------------
	@SuppressWarnings("unchecked")
	private List<BasicConfigurableParm[]> extractConfig(HttpSession session, String component)
	{
		if (component.equals("sc"))
			return ((List<BasicConfigurableParm[]>) session.getAttribute(SC_FIELD));
		else if (component.equals("var"))
			return ((List<BasicConfigurableParm[]>) session.getAttribute(VAR_FIELD));
		else if (component.equals("quota"))
			return ((List<BasicConfigurableParm[]>) session.getAttribute(QUOTA_FIELD));
		return null;
	}

	// BasicConfigurableParm[]
	private <T> void saveList(HttpSession session, String component, List<T> configToSave)
	{
		if (component.equals(REQ_SERVICE_CLASS))
		{
			session.setAttribute(SC_FIELD, configToSave);
			session.setAttribute(SC_FIELD_UPDATED, true);
		}
		else if (component.equals(REQ_VARIANT))
		{
			session.setAttribute(VAR_FIELD, configToSave);
			session.setAttribute(VAR_FIELD_UPDATED, true);
		}
		else if (component.equals(REQ_QUOTA))
		{
			session.setAttribute(QUOTA_FIELD, configToSave);
			session.setAttribute(QUOTA_FIELD_UPDATED, true);
		}
	}

	private BasicConfigurableParm [] cloneConfig(BasicConfigurableParm [] config)
	{
		BasicConfigurableParm [] result = new BasicConfigurableParm[config.length];
		
		for(int i=0; i<config.length; i++)
		{
			result[i] = new BasicConfigurableParm(config[i].getFieldName(), config[i].getValue());
		}
		
		return result;
	}
	
	private JsonObject extractFields(BasicConfigurableParm[] parms)
	{
		JsonObject job = new JsonObject();
		try
		{
			for (int i = 0; i < parms.length; i++)
			{
				String fieldName = parms[i].getFieldName().substring(0, 1).toLowerCase() + parms[i].getFieldName().substring(1);

				if ((parms[i].getValue() != null) && (parms[i].getValue() instanceof Texts))
				{
					JsonObject tjob = new JsonObject();
					JsonArray jarr = new JsonArray();
					Texts texts = (Texts) parms[i].getValue();
					for (int j = 0; j <= IPhrase.MAX_LANGUAGES; j++)
					{
						if (texts.getText(j) != null)
							jarr.add(new JsonPrimitive(texts.getText(j)));
						else
							jarr.add(null);
					}
					tjob.add("texts", jarr);
					job.add(fieldName, tjob);
				}
				else if ((parms[i].getValue() != null) && (parms[i].getValue() instanceof Phrase) || parms[i].getValue() instanceof IPhrase)
				{
					IPhrase phrase = (IPhrase) parms[i].getValue();
					for (String lang : phrase.getLanguageCodes())
					{
						if (phrase.get(lang) != null)
							job.add(fieldName + "_" + lang, new JsonPrimitive(phrase.get(lang)));
						else
							job.add(fieldName + "_" + lang, new JsonPrimitive(""));
					}
				}
				else
				{
					String value = (parms[i].getValue() == null)? "" : String.valueOf(parms[i].getValue());
					job.add(fieldName, new JsonPrimitive(value));
				}
			}
		}
		catch (Exception e)
		{
		}
		return job;
	}

	private String extractStructuredConfig(String component, final HttpSession session, int index)
	{
		List<BasicConfigurableParm[]> config = extractConfig(session, component);

		if (config != null && index < config.size())
		{
			BasicConfigurableParm[] pageConfig = cloneConfig(config.get(index));
			
			// Perform page transformation
			DataTransformation transform = new DataTransformation(new String[] {
						"priceCents", "addConsumerCharge", "removeConsumerCharge", "removeQuotaCharge", 
						"providerBalanceEnquiryCharge", "unsubscribeCharge", "subscriptionCharge", "renewalCharge"
					})
			{
				@Override
				public void performTransformation(BasicConfigurableParm data)
				{
					// For normal currency
					int currencyDigits = 0;
					try {
						currencyDigits = (int)session.getAttribute(BaseServiceConfigHandler.CURRENCY_DIGITS);
					} catch(Exception e) {
					}
					BigDecimal value = (new BigDecimal((int)data.getValue()));
					value = value.movePointLeft(currencyDigits);

					// for princeCents move further left
					if (data.getFieldName().equalsIgnoreCase("priceCents"))
						value = value.movePointLeft(2);
					
					// Serve as String to page
					data.setValue(value.stripTrailingZeros().toString());	
				}
			};
			transform.search(pageConfig);
					
			JsonObject job = extractFields(pageConfig);
			
			if (component.equals(REQ_VARIANT)) 
			{
				// Extract (Is Perpetual)
				int vpIndex = findConfigurationIndex(pageConfig, "validityPeriodDays");
				if (vpIndex >= 0 && pageConfig[vpIndex].getValue() == null)
				{
					job.add("isperpetual", new JsonPrimitive("true"));
				}
			}
			return job.toString();
		}
		else
		{
			if (config == null)
				return (new GuiUpdateResponse(OperationStatus.fail, String.format("No data found for component %s", component)).toString());
			else
				return (new GuiUpdateResponse(OperationStatus.fail, String.format("Index out of range for component %s", component)).toString());
		}
	}

	private String deleteConfig(String component, HttpSession session, int index)
	{
		List<BasicConfigurableParm[]> config = extractConfig(session, component);

		if (config != null && index < config.size())
		{
			config.remove(index);
			saveList(session, component, config);
			return (new GuiUpdateResponse(OperationStatus.pass, String.format("Item deleted")).toString());
		}
		else
		{
			if (config == null)
				return (new GuiUpdateResponse(OperationStatus.fail, String.format("No data found for component %s", component)).toString());
			else
				return (new GuiUpdateResponse(OperationStatus.fail, String.format("Index out of range for component %s", component)).toString());
		}
	}

	private String getComponentPage(String component)
	{
		if (component.equals(REQ_SERVICE_CLASS))
			return "groupsharedaccounts/serviceclasses";
		else if (component.equals(REQ_VARIANT))
			return "groupsharedaccounts/variants";
		else if (component.equals(REQ_QUOTA))
			return "groupsharedaccounts/quotas";
		return null;
	}

	private ConfigurableResponseParam[] getStructure(HttpSession session, String component)
	{
		ConfigurableResponseParam[] structure = null;
		
		if (component.equals(REQ_SERVICE_CLASS))
			structure = (ConfigurableResponseParam[]) session.getAttribute(SC_FIELD_STRUCT);
		else if (component.equals(REQ_VARIANT))
			structure = (ConfigurableResponseParam[]) session.getAttribute(VAR_FIELD_STRUCT);
		else if (component.equals(REQ_QUOTA))
			structure = (ConfigurableResponseParam[]) session.getAttribute(QUOTA_FIELD_STRUCT);
		
		return structure;
	}
	
	private Object extractDefaultStructureValue(String type)
	{
		Object value = null;
		
		if (type.equalsIgnoreCase("int") || type.equalsIgnoreCase("java.lang.Integer"))
			value = Integer.valueOf(0);
		else if (type.equalsIgnoreCase("boolean"))
			value = Boolean.valueOf(false);
		else if (type.equalsIgnoreCase("long"))
			value = Long.valueOf(0);
		else if (type.equalsIgnoreCase("byte"))
			value = Byte.valueOf((byte) 0);
		else if (type.equalsIgnoreCase("float"))
			value = Float.valueOf(0f);
		else if (type.equalsIgnoreCase("double"))
			value = Double.valueOf(0D);
		else if (type.equalsIgnoreCase("hxc.services.notification.Texts"))
			value = new Texts();
		else if (type.equalsIgnoreCase("hxc.services.ServiceType"))
			value = ServiceType.DATA;
		else if (type.equalsIgnoreCase("hxc.services.notification.Phrase"))
			value = new Phrase();
		else
			value = "";
		
		return value;
	}
	
	private BasicConfigurableParm[] extractStructure(HttpSession session, String component)
	{
		ConfigurableResponseParam[] structure = getStructure(session, component);

		BasicConfigurableParm[] fields = new BasicConfigurableParm[structure.length];
		for (int i = 0; i < structure.length; i++)
		{
			fields[i] = new BasicConfigurableParm(structure[i].getFieldName());
			Object value = extractDefaultStructureValue(structure[i].getValueType());
			fields[i].setValue(value);
		}
		return fields;
	}
	
	private Object extractDefaultValueForField(HttpSession session, String component, String fieldName)
	{
		Object value = null;
		
		ConfigurableResponseParam[] structure = getStructure(session, component);
		for (int i = 0; i < structure.length; i++)
		{
			if (structure[i].getFieldName().equalsIgnoreCase(fieldName))
			{
				value = extractDefaultStructureValue(structure[i].getValueType());
				break;
			}
		}
		return value;
	}

	private int findConfigurationIndex(BasicConfigurableParm[] parms, String fieldName)
	{
		for (int i = 0; i < parms.length; i++)
		{
			if (parms[i].getFieldName().equalsIgnoreCase(fieldName))
			{
				return i;
			}
		}
		return -1;
	}

	private String updateComponent(HttpSession session, HttpServletRequest request, String component, int index)
	{
		try
		{
			BasicConfigurableParm[] fields = null;

			List<BasicConfigurableParm[]> config = (List<BasicConfigurableParm[]>) extractConfig(session, component);

			if (index < 0)
				fields = extractStructure(session, component);
			else
				fields = (BasicConfigurableParm[]) config.get(index);

			// Booleans are only reported if they are true so default all to false
			for (BasicConfigurableParm field : fields)
			{
				if ((field.getValue() != null) && field.getValue().getClass().equals(Boolean.class))
				{
					field.setValue(Boolean.valueOf(false));
				}
			}

			int currencyDigits = 0;
			try {
				currencyDigits = (int)session.getAttribute(BaseServiceConfigHandler.CURRENCY_DIGITS);
			} catch(Exception e) {
			}
			
			for (String key : request.getParameterMap().keySet())
			{
				String value = request.getParameterMap().get(key)[0];
				String fieldName = key;
				 
				// Transformation to cents
				if (key.equalsIgnoreCase("priceCents"))
				{
					// +2 decimal places
					int digits = currencyDigits + 2;
					BigDecimal dbValue = (new BigDecimal(value)).movePointRight(digits);
					value = String.valueOf( dbValue.longValue() );

				} else if (key.equalsIgnoreCase("addConsumerCharge") || key.equalsIgnoreCase("removeConsumerCharge") 
						|| key.equalsIgnoreCase("removeQuotaCharge") || key.equalsIgnoreCase("providerBalanceEnquiryCharge") 
						|| key.equalsIgnoreCase("unsubscribeCharge") || key.equalsIgnoreCase("subscriptionCharge") 
						|| key.equalsIgnoreCase("renewalCharge"))
				{
					BigDecimal dbValue = (new BigDecimal(value)).movePointRight(currencyDigits);
					value = String.valueOf( dbValue.longValue() );
				}

				int parmIndex = -1;
				if (!(key.equals("index") || key.equals("comp") || key.equals("act")))
				{
					if (key.indexOf('_') > 0)
					{
						String[] keys = key.split("_");

						fieldName = keys[0];
						parmIndex = findConfigurationIndex(fields, fieldName);
						try
						{
							// Texts
							int msgIndex = Integer.parseInt(keys[1]);
							((Texts) fields[parmIndex].getValue()).setText(msgIndex, value);
						}
						catch (Exception e)
						{
							// Phrases
							((Phrase) fields[parmIndex].getValue()).set(keys[1], value);
						}
					}
					else
					{
						parmIndex = findConfigurationIndex(fields, fieldName);
						if (parmIndex >= 0)
						{
							// Safety Check
							if (fields[parmIndex].getValue() == null)
								fields[parmIndex].setValue(extractDefaultValueForField(session, component, fieldName));
							
							if (fields[parmIndex].getValue().getClass().equals(Boolean.class))
							{
								fields[parmIndex].setValue(true);
							}
							else if (fields[parmIndex].getValue().getClass().equals(Integer.class) || fields[parmIndex].getValue().getClass().equals(Long.class))
							{

								if (fields[parmIndex].getValue().getClass().equals(Integer.class))
									fields[parmIndex].setValue(Integer.parseInt(value));
								else
									fields[parmIndex].setValue(Long.parseLong(value));
							}
							else if (fields[parmIndex].getValue().getClass().isEnum())
							{
								@SuppressWarnings({ "unchecked", "rawtypes" })
								Object avalue = Enum.valueOf((Class<Enum>) fields[parmIndex].getValue().getClass(), value);
								fields[parmIndex].setValue(avalue);
							}
							else
							{
								fields[parmIndex].setValue(value);
							}
						}
					}
				}
			}

			// Update
			if (index < 0 || index >= config.size())
			{
				config.add(fields); // Add operation
				index = config.size() - 1; 
			}
			else
				config.set(index, fields); // Edit operation

			if (REQ_VARIANT.equals(component))
			{
				// Replace "Validity Period" with Null if "isperpetual" (from page) set to TRUE
				String perpetualParameter = request.getParameter("isperpetual");
				boolean isPerpetual = (perpetualParameter == null)? false : ("on".equalsIgnoreCase(perpetualParameter) || "true".equalsIgnoreCase(perpetualParameter));
				if (isPerpetual)
				{
					// Find Perpetual index and replace
					int vpIndex = findConfigurationIndex(config.get(index), "validityPeriodDays");
					config.get(index)[vpIndex].setValue(null);
				}
			}

			// save back
			saveList(session, component, config);
		}
		catch (Exception e)
		{
			return (new GuiUpdateResponse(OperationStatus.fail, e.getMessage())).toString();
		}
		return (new GuiUpdateResponse(OperationStatus.pass, "updated")).toString();
	}

    private abstract class DataTransformation
    {
    	private List<String> fieldsToSearchFor;
    	
        public DataTransformation(final String [] fieldsToFind)
        {
        	this.fieldsToSearchFor = Arrays.asList(fieldsToFind);
        }
        
        public void search(BasicConfigurableParm[] configData)
        {
            for(BasicConfigurableParm data : configData)
            {
            	for(String name : fieldsToSearchFor)
            	{
                    if (data.getFieldName().equalsIgnoreCase(name))
                    {
                    	performTransformation(data);
                    	break;
                    }
            	}
            }
        }
        
        public abstract void performTransformation(BasicConfigurableParm data);
    }
    
}
