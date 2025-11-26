package hxc.userinterfaces.gui.controller.service;

import java.math.BigDecimal;
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
import hxc.services.notification.Texts;
import hxc.userinterfaces.gui.controller.manage.ServiceConfigurationLoader;
//import hxc.userinterfaces.gui.credshare.CSQuota;
//import hxc.userinterfaces.gui.credshare.CSServiceClass;
//import hxc.userinterfaces.gui.credshare.CSVariant;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.userinterfaces.gui.utils.UssdProcessHelper;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

public class CreditSharingService implements IThymeleafController
{

	// Will need to store the structure of Objects for creating new Objects
	public static final String SC_STRUCTURE_REF = "scstruct";
	public static final String VARIANT_STRUCTURE_REF = "varstruct";
	public static final String QUOTA_STRUCTURE_REF = "quotastruct";

	// Fields from configuration
	public static final String SC_FIELD = "ServiceClasses";
	public static final String VAR_FIELD = "Variants";
	public static final String QUOTA_FIELD = "Quotas";

	// Session variables
	public static final String QUOTA_INFO_VARIABLE = "quotas";
	public static final String SC_INFO_VARIABLE = "serviceclasses";
	public static final String VARIANTS_INFO_VARIABLE = "variants";

	// Page variables
	public static final String REQ_QUOTA = "quota";
	public static final String REQ_VARIANT = "var";
	public static final String REQ_SERVICE_CLASS = "sc";

	// Update status variables
	public static final String SC_INFO_VARIABLE_UPDATED = "serviceclassesupdated";
	public static final String QUOTA_INFO_VARIABLE_UPDATED = "quotasupdated";
	public static final String VARIANTS_INFO_VARIABLE_UPDATED = "variantsupdated";

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
			List<?> list = (List<?>) session.getAttribute(SC_INFO_VARIABLE);

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
			return ((List<BasicConfigurableParm[]>) session.getAttribute(SC_INFO_VARIABLE));
		else if (component.equals("var"))
			return ((List<BasicConfigurableParm[]>) session.getAttribute(VARIANTS_INFO_VARIABLE));
		else if (component.equals("quota"))
			return ((List<BasicConfigurableParm[]>) session.getAttribute(QUOTA_INFO_VARIABLE));
		return null;
	}

	// BasicConfigurableParm[]
	private <T> void saveList(HttpSession session, String component, List<T> configToSave)
	{
		if (component.equals("sc"))
		{
			session.setAttribute(SC_INFO_VARIABLE, configToSave);
			session.setAttribute(SC_INFO_VARIABLE_UPDATED, true);
		}
		else if (component.equals("var"))
		{
			session.setAttribute(VARIANTS_INFO_VARIABLE, configToSave);
			session.setAttribute(VARIANTS_INFO_VARIABLE_UPDATED, true);
		}
		else if (component.equals("quota"))
		{
			session.setAttribute(QUOTA_INFO_VARIABLE, configToSave);
			session.setAttribute(QUOTA_INFO_VARIABLE_UPDATED, true);
		}
	}

	private String extractFields(BasicConfigurableParm[] parms)
	{
		String result = null;
		try
		{
			JsonObject job = new JsonObject();
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
				else
				{
					String value = String.valueOf(parms[i].getValue());
					job.add(fieldName, new JsonPrimitive(value));
				}
			}
			result = job.toString();
		}
		catch (Exception e)
		{
		}
		return result;
	}

	private String extractStructuredConfig(String component, HttpSession session, int index)
	{
		String result = null;
		List<BasicConfigurableParm[]> config = extractConfig(session, component);

		if (config != null && index < config.size())
		{
			result = extractFields(config.get(index));
		}
		else
		{
			if (config == null)
				return (new GuiUpdateResponse(OperationStatus.fail, String.format("No data found for component %s", component)).toString());
			else
				return (new GuiUpdateResponse(OperationStatus.fail, String.format("Index out of range for component %s", component)).toString());
		}
		return result;
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
			return "creditsharing/serviceclasses";
		else if (component.equals(REQ_VARIANT))
			return "creditsharing/variants";
		else if (component.equals(REQ_QUOTA))
			return "creditsharing/quotas";
		return null;
	}

	private BasicConfigurableParm[] extractStructure(HttpSession session, String component)
	{
		ConfigurableResponseParam[] structure = null;

		if (component.equals(REQ_SERVICE_CLASS))
			structure = (ConfigurableResponseParam[]) session.getAttribute(SC_STRUCTURE_REF);
		else if (component.equals(REQ_VARIANT))
			structure = (ConfigurableResponseParam[]) session.getAttribute(VARIANT_STRUCTURE_REF);
		else if (component.equals(REQ_QUOTA))
			structure = (ConfigurableResponseParam[]) session.getAttribute(QUOTA_STRUCTURE_REF);

		BasicConfigurableParm[] fields = new BasicConfigurableParm[structure.length];
		for (int i = 0; i < structure.length; i++)
		{
			fields[i] = new BasicConfigurableParm(structure[i].getFieldName());
			Object value = null;
			if (structure[i].getValueType().equalsIgnoreCase("int"))
			{
				value = Integer.valueOf(0);
			}
			else if (structure[i].getValueType().equalsIgnoreCase("boolean"))
			{
				value = Boolean.valueOf(false);
			}
			else if (structure[i].getValueType().equalsIgnoreCase("long"))
			{
				value = Long.valueOf(0);
			}
			else if (structure[i].getValueType().equalsIgnoreCase("byte"))
			{
				value = Byte.valueOf((byte) 0);
			}
			else if (structure[i].getValueType().equalsIgnoreCase("float"))
			{
				value = Float.valueOf(0f);
			}
			else if (structure[i].getValueType().equalsIgnoreCase("double"))
			{
				value = Double.valueOf(0D);
			}
			else if (structure[i].getValueType().equalsIgnoreCase("hxc.services.notification.Texts"))
			{
				value = new Texts();
			}
			else if (structure[i].getValueType().equalsIgnoreCase("hxc.services.creditsharing.ServiceType"))
			{
				value = ServiceType.DATA;
			}
			else
			{
				value = "";
			}
			fields[i].setValue(value);
		}
		return fields;
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
				if (field.getValue().getClass().equals(Boolean.class))
				{
					field.setValue(Boolean.valueOf(false));
				}
			}

			for (String key : request.getParameterMap().keySet())
			{
				String value = request.getParameterMap().get(key)[0];
				String fieldName = key;

				int parmIndex = -1;
				if (!(key.equals("index") || key.equals("comp") || key.equals("act")))
				{
					if (key.indexOf('_') > 0)
					{
						String[] keys = key.split("_");
						int msgIndex = 0;
						try
						{
							msgIndex = Integer.parseInt(keys[1]);
						}
						catch (Exception e)
						{
						}
						fieldName = keys[0];
						parmIndex = findConfigurationIndex(fields, fieldName);
						((Texts) fields[parmIndex].getValue()).setText(msgIndex, value);
					}
					else
					{
						boolean convertToCents = false;
						if (fieldName.indexOf("CONVERTTOCENTS") > 0)
						{
							fieldName = fieldName.substring(0, fieldName.indexOf("CONVERTTOCENTS"));
							convertToCents = true;
						}
						parmIndex = findConfigurationIndex(fields, fieldName);
						if (parmIndex >= 0)
						{
							if (fields[parmIndex].getValue().getClass().equals(Boolean.class))
							{
								fields[parmIndex].setValue(true);
							}
							else if (fields[parmIndex].getValue().getClass().equals(Integer.class) || fields[parmIndex].getValue().getClass().equals(Long.class))
							{
								if (convertToCents)
								{
									BigDecimal bd = new BigDecimal(value);
									bd = bd.movePointRight(2);
									if (fields[parmIndex].getValue().getClass().equals(Integer.class))
										fields[parmIndex].setValue(bd.intValue());
									else
										fields[parmIndex].setValue(bd.longValue());
								}
								else
								{
									if (fields[parmIndex].getValue().getClass().equals(Integer.class))
										fields[parmIndex].setValue(Integer.parseInt(value));
									else
										fields[parmIndex].setValue(Long.parseLong(value));
								}
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
				config.add(fields); // Add operation
			else
				config.set(index, fields); // Edit operation

			// save back
			saveList(session, component, config);
		}
		catch (Exception e)
		{
			return (new GuiUpdateResponse(OperationStatus.fail, e.getMessage())).toString();
		}
		return (new GuiUpdateResponse(OperationStatus.pass, "updated")).toString();
	}

}
