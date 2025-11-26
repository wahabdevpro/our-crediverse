package hxc.userinterfaces.gui.controller.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import hxc.services.notification.IPhrase;
import hxc.services.notification.Phrase;
import hxc.services.notification.Texts;
import hxc.userinterfaces.gui.controller.manage.ServiceConfigurationLoader;
import hxc.userinterfaces.gui.controller.templates.ComplexConfiguration;
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.userinterfaces.gui.utils.UssdProcessHelper;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

public class EnhancedCreditTransferService implements IThymeleafController
{
	// Session variables
	public static final String SC_INFO_VARIABLE = "serviceclasses";
	public static final String VARIANTS_INFO_VARIABLE = "variants";
	public static final String TM_INFO_VARIABLE = "transmode";

	// Fields from configuration
	public static final String SC_FIELD = "ServiceClasses";
	public static final String VAR_FIELD = "Variants";
	public static final String TM_FIELD = "TransferModes";

	// Will need to store the structure of Objects for creating new Objects
	public static final String SC_STRUCTURE_REF = "scstruct";
	public static final String VARIANT_STRUCTURE_REF = "varstruct";
	public static final String TM_STRUCTURE_REF = "tmstruct";

	// Update status variables
	public static final String SC_INFO_VARIABLE_UPDATED = "serviceclassesupdated";
	public static final String VARIANTS_INFO_VARIABLE_UPDATED = "variantsupdated";
	public static final String TM_INFO_VARIABLE_UPDATED = "tmupdated";
	public static final String USSD_UPDATED = "ussdupdated";

	// Page variables
	public static final String REQ_SERVICE_CLASS = "sc";
	public static final String REQ_VARIANT = "var";
	public static final String REQ_TM = "tm";

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
			if (!validateUserSession(user))
			{
				json = sendInvalidUserJson();
			}
			else
			{
				// USSD Information
				UssdProcessHelper ussdHelper = null;
				String node = request.getParameter("node");
				String aid = request.getParameter("aid"); // Attribute ID (e.g. Caption)
				String nid = request.getParameter("nid"); // NodeId (e.g. 421604538)

				switch (action)
				{
				// ------ StandardProperty Updates
					case "render":
						json = extractRenderHints(component, session);
						break;
					case "data":
						// request node data
						if (component == null)
						{
							ussdHelper = new UssdProcessHelper();
							json = ussdHelper.extractNodeData(session, node, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE, ServiceConfigurationLoader.PROCESS_ACTION_ACTION_VARIABLES);
						}
						else
							json = extractStructuredConfig(component, session, index);
						break;
					case "del":
						json = deleteConfig(component, session, index);
						break;
					case "upd": // Shared

						if (component != null)
						{
							// Modal Add / Edit update
							json = updateComponentValues(session, request, component, index);
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
					// ------- Properties required for Return Codes
					case "rcdata":
						// ReturnCodes
						json = extractReturnCodes(session);
						break;
					// ------- Properties required for USSD
					case "propdata":
						ussdHelper = new UssdProcessHelper();
						json = ussdHelper.extractPropertyData(session, nid, aid, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);
						break;
					case "propupd":
						ussdHelper = new UssdProcessHelper();
						json = ussdHelper.savePropertyData(session, request, nid, aid, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);
						break;
					case "lang":
						ussdHelper = new UssdProcessHelper();
						json = ussdHelper.extractSystemLanguageInfo(user);
						break;
					case "edit":
						String prop = request.getParameter("prop");
						ussdHelper = new UssdProcessHelper();
						json = ussdHelper.extractPropertyData(session, node, prop, ServiceConfigurationLoader.PROCESS_ACTION_PROCESS_VARIABLE);
						break;
					// ------- Extra helper Properties
					case "options": // To get Json list of values from a specified
						String fieldName = request.getParameter("field");
						json = getOptionsList(session, component, fieldName);
						break;
				}
			}

		}

		if (page == null)
			GuiUtils.sendResponse(response, json);
		else
		{
			ctx.setVariable("utils", new GuiUtils());
			templateEngine.process(page, ctx, response.getWriter());
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

	private String extractRenderHints(String component, HttpSession session)
	{
		ConfigurableResponseParam[] struct = extractConfigurableResponseParamArray(session, component);
		JsonObject dec = extractDecorations(struct);
		return dec.toString();
	}

	private String extractStructuredConfig(String component, HttpSession session, int index)
	{
		String result = null;
		List<BasicConfigurableParm[]> config = extractConfig(session, component);

		if (config != null && index < config.size())
		{
			JsonObject job = extractFields(config.get(index));
			
			if (component.equals(REQ_VARIANT)) 
			{
				// Extract (Is Perpetual)
				int vpIndex = findConfigurationIndex(config.get(index), "validityPeriod");
				if (vpIndex >= 0 && config.get(index)[vpIndex].getValue() == null)
				{
					job.add("isperpetual", new JsonPrimitive("true"));
				}
			}
			
			ConfigurableResponseParam[] struct = extractConfigurableResponseParamArray(session, component);
			JsonObject dec = extractDecorations(struct);
			job.add("render", dec);
			result = job.toString();
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

	@SuppressWarnings("unchecked")
	private List<BasicConfigurableParm[]> extractConfig(HttpSession session, String component)
	{
		if (component.equals("sc"))
			return ((List<BasicConfigurableParm[]>) session.getAttribute(SC_INFO_VARIABLE));
		else if (component.equals("var"))
			return ((List<BasicConfigurableParm[]>) session.getAttribute(VARIANTS_INFO_VARIABLE));
		else if (component.equals("tm"))
			return ((List<BasicConfigurableParm[]>) session.getAttribute(TM_INFO_VARIABLE));
		else
			return ((List<BasicConfigurableParm[]>) session.getAttribute(component));
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
		else if (component.equals("tm"))
		{
			session.setAttribute(TM_INFO_VARIABLE, configToSave);
			session.setAttribute(TM_INFO_VARIABLE_UPDATED, true);
		}
		else
		{
			session.setAttribute(component, configToSave);
			session.setAttribute(component + ComplexConfiguration.UPDATED_SUFFIX, true);
		}
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

	/*
	 * [{"field" : {"ra" : "TYPE", ...}, ...]
	 */
	private JsonObject extractDecorations(ConfigurableResponseParam[] struct)
	{
		JsonObject result = new JsonObject();
		for (ConfigurableResponseParam config : struct)
		{
			JsonObject jb = new JsonObject();
			if (config.getRenderAs() != null)
				jb.add("ra", new JsonPrimitive(config.getRenderAs()));
			if (config.getDecimalDigitsToDisplay() != 0)
				jb.add("dd", new JsonPrimitive(config.getDecimalDigitsToDisplay()));
			if (config.getScaleFactor() != 1)
				jb.add("sf", new JsonPrimitive(config.getScaleFactor()));
			if (!jb.toString().equals("{}"))
			{
				String fieldName = config.getFieldName().substring(0, 1).toLowerCase() + config.getFieldName().substring(1);
				result.add(fieldName, jb);
			}
		}

		return result;
	}

	private JsonObject extractFields(BasicConfigurableParm[] parms)
	{
		JsonObject dataJob = new JsonObject();
		try
		{
			// Data (fields + values)

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
					dataJob.add(fieldName, tjob);
				}
				else if ((parms[i].getValue() != null) && (parms[i].getValue() instanceof Phrase) || parms[i].getValue() instanceof IPhrase)
				{
					IPhrase phrase = (IPhrase) parms[i].getValue();
					for (String lang : phrase.getLanguageCodes())
					{
						if (phrase.get(lang) != null)
							dataJob.add(fieldName + "_" + lang, new JsonPrimitive(phrase.get(lang)));
						else
							dataJob.add(fieldName + "_" + lang, new JsonPrimitive(""));
					}
				}
				else if ((parms[i].getValue() != null) && (parms[i].getValue() instanceof Object[]))
				{
					Object[] values = (Object[]) parms[i].getValue();
					StringBuilder sb = new StringBuilder();
					for (Object v : values)
					{
						if (sb.length() > 0)
							sb.append(',');
						sb.append(v.toString());
					}
					dataJob.add(fieldName, new JsonPrimitive(sb.toString()));
				}
				else if ((parms[i].getValue() != null) && (parms[i].getValue() instanceof int[]))
				{
					int[] values = (int[]) parms[i].getValue();
					StringBuilder sb = new StringBuilder();
					for (int v : values)
					{
						if (sb.length() > 0)
							sb.append(',');
						sb.append(v);
					}
					dataJob.add(fieldName, new JsonPrimitive(sb.toString()));

				}
				else if ((parms[i].getValue() != null) && (parms[i].getValue() instanceof String[]))
				{
					String[] values = (String[]) parms[i].getValue();
					StringBuilder sb = new StringBuilder();
					for (String v : values)
					{
						if (sb.length() > 0)
							sb.append(',');
						sb.append(v);
					}
					dataJob.add(fieldName, new JsonPrimitive(sb.toString()));

				}
				else if (parms[i].getValue() == null)
				{
					dataJob.add(fieldName, new JsonPrimitive(""));
				}
				else
				{
					String value = String.valueOf(parms[i].getValue());
					dataJob.add(fieldName, new JsonPrimitive(value));
				}
			}
		}
		catch (Exception e)
		{
		}
		return dataJob;
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

	private ConfigurableResponseParam[] extractConfigurableResponseParamArray(HttpSession session, String component)
	{
		ConfigurableResponseParam[] result = null;

		if (component.equals(REQ_SERVICE_CLASS))
			result = (ConfigurableResponseParam[]) session.getAttribute(SC_STRUCTURE_REF);
		else if (component.equals(REQ_VARIANT))
			result = (ConfigurableResponseParam[]) session.getAttribute(VARIANT_STRUCTURE_REF);
		else if (component.equals(REQ_TM))
			result = (ConfigurableResponseParam[]) session.getAttribute(TM_STRUCTURE_REF);
		else
			result = (ConfigurableResponseParam[]) session.getAttribute(component + ComplexConfiguration.STRUCTURE_SUFFIX);
		return result;
	}

	private Object ConfigurableResponseParamToBaseValue(ConfigurableResponseParam param)
	{
		Object value = null;
		if (param.getValueType().equalsIgnoreCase("int"))
			value = Integer.valueOf(0);
		else if (param.getValueType().equalsIgnoreCase("boolean"))
			value = Boolean.valueOf(false);
		else if (param.getValueType().equalsIgnoreCase("long"))
			value = Long.valueOf(0);
		else if (param.getValueType().equalsIgnoreCase("byte"))
			value = Byte.valueOf((byte) 0);
		else if (param.getValueType().equalsIgnoreCase("float"))
			value = Float.valueOf(0f);
		else if (param.getValueType().equalsIgnoreCase("double"))
			value = Double.valueOf(0D);
		else if (param.getValueType().equalsIgnoreCase("hxc.services.notification.Texts"))
			value = new Texts();
		else if (param.getValueType().equalsIgnoreCase("java.lang.String"))
			value = new String();
		else if (param.getValueType().equalsIgnoreCase("java.lang.Integer"))
			value = Integer.valueOf(0);
		else if (param.getValueType().equalsIgnoreCase("java.lang.Long"))
			value = Long.valueOf(0);
		else if (param.getValueType().equalsIgnoreCase("hxc.utils.calendar.TimeUnits"))
			value = new String();
		else if (param.getValueType().equalsIgnoreCase("[I"))
			value = new int[0];
		else if (param.getValueType().equalsIgnoreCase("[Ljava.lang.Integer;"))
			value = new Integer[0];
		else if (param.getValueType().equalsIgnoreCase("[Ljava.lang.String;"))
			value = new String[0];
		else if (param.getValueType().equalsIgnoreCase("hxc.services.notification.Phrase"))
			value = new Phrase();
		else
		{
			value = "";
		}
		return value;
	}

	private BasicConfigurableParm[] extractStructure(HttpSession session, String component)
	{
		ConfigurableResponseParam[] structure = extractConfigurableResponseParamArray(session, component);

		BasicConfigurableParm[] fields = new BasicConfigurableParm[structure.length];
		for (int i = 0; i < structure.length; i++)
		{
			fields[i] = new BasicConfigurableParm(structure[i].getFieldName());
			Object value = ConfigurableResponseParamToBaseValue(structure[i]);
			fields[i].setValue(value);
		}
		return fields;
	}

	private String updateComponentValues(HttpSession session, HttpServletRequest request, String component, int index)
	{
		try
		{
			int currencyDigits = (int) session.getAttribute("curDigits");

			// Get the structure to the Component to update
			List<BasicConfigurableParm[]> config = (List<BasicConfigurableParm[]>) extractConfig(session, component);
			ConfigurableResponseParam[] structure = extractConfigurableResponseParamArray(session, component);
			BasicConfigurableParm[] fields = null;

			// Get a basic template to fill in
			if (index < 0)
				fields = extractStructure(session, component);
			else
				fields = (BasicConfigurableParm[]) config.get(index);

			for (String key : request.getParameterMap().keySet())
			{
				String fieldName = key;

				if (!(key.equals("index") || key.equals("comp") || key.equals("act")))
				{
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < request.getParameterMap().get(key).length; i++)
					{
						if (i > 0)
						{
							sb.append(",");
						}
						sb.append(request.getParameterMap().get(key)[i]);
					}

					String value = sb.toString().trim();

					if (key.indexOf('_') > 0)
					{
						String[] keys = key.split("_");

						fieldName = keys[0];
						int parmIndex = findConfigurationIndex(fields, fieldName);
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
						// Other properties (Not text)
						int parmIndex = findConfigurationIndex(fields, fieldName);
						if (parmIndex >= 0)
						{
							// Find structure
							int structIndex = -1;
							
							for (int i = 0; i < structure.length; i++)
							{
								if (structure[i].getFieldName().equalsIgnoreCase(fieldName))
								{
									structIndex = i;
									break;
								}
							}

							if (structIndex >= 0)
							{
								// First convert the value back
								if (structure[structIndex].getRenderAs() != null)
								{
									if (structure[structIndex].getRenderAs().equalsIgnoreCase("CURRENCY"))
									{
										BigDecimal bd = new BigDecimal(value);
										bd = bd.multiply(new BigDecimal(Math.pow(10, currencyDigits)));
										value = String.valueOf(bd.intValue());
									}
								}
								else if (structure[structIndex].getScaleFactor() > 1)
								{
									BigDecimal bd = new BigDecimal(value);
									bd = bd.multiply(new BigDecimal(structure[structIndex].getScaleFactor()));
									value = String.valueOf(bd.intValue());
								}

								// TODO:Refactor Me ==>
								if (fields[parmIndex].getValue() == null)
								{
									// Find type from structure
									for (ConfigurableResponseParam cresp : structure)
									{
										if (cresp.getFieldName().equals(fields[parmIndex].getFieldName()))
										{
											fields[parmIndex].setValue(ConfigurableResponseParamToBaseValue(cresp));
											break;
										}
									}
								}

								if (fields[parmIndex].getValue().getClass().isArray())
								{
									String[] values = null;
									if (value.trim().length() == 0)
										values = new String[0];
									else
									{
										values = value.split(",");
										for (int i = 0; i < values.length; i++)
										{
											values[i] = values[i].trim();
										}
									}

									Class<?> type = fields[parmIndex].getValue().getClass().getComponentType();
									if (type.equals(int.class))
									{
										fields[parmIndex].setValue(new int[values.length]);

										if (values.length > 0)
										{
											for (int i = 0; i < values.length; i++)
											{
												((int[]) fields[parmIndex].getValue())[i] = Integer.parseInt(values[i]);
											}
										}
									}
									else if (type.equals(String.class))
									{
										fields[parmIndex].setValue(values);
									}
									else if (type.equals(Integer.class))
									{
										fields[parmIndex].setValue(new Integer[values.length]);

										if (values.length > 0)
										{
											for (int i = 0; i < values.length; i++)
											{
												((Integer[]) fields[parmIndex].getValue())[i] = Integer.valueOf(values[i]);
											}
										}
									}
									else
									{
										// Unknown Array ?!?
									}
								}
								else if (fields[parmIndex].getValue().getClass().equals(Boolean.class))
								{
									fields[parmIndex].setValue(Boolean.valueOf(value));
								}
								else if (fields[parmIndex].getValue().getClass().equals(Integer.class) || fields[parmIndex].getValue().getClass().equals(Long.class))
								{
									if ("".equals(value))
										fields[parmIndex].setValue(null);
									else
										if (fields[parmIndex].getValue().getClass().equals(Integer.class))
										{
											try
											{
												fields[parmIndex].setValue(Integer.parseInt(value));
											}
											catch (Exception e)
											{
												fields[parmIndex].setValue(null);
											}
										}

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
			}

			if (REQ_VARIANT.equals(component))
			{
				// Replace "Validity Period" with Null if "isperpetual" (from page) set to TRUE
				String perpetualParameter = request.getParameter("isperpetual");
				boolean isPerpetual = (perpetualParameter == null)? false : Boolean.valueOf(perpetualParameter);
				if (isPerpetual)
				{
					// Find Perpetual index and replace
					int vpIndex = findConfigurationIndex(config.get(index), "validityPeriod");
					config.get(index)[vpIndex].setValue(null);
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

	private String getComponentPage(String component)
	{
		if (component.equals(REQ_SERVICE_CLASS))
			return "enhancedCS/serviceclasses";
		else if (component.equals(REQ_VARIANT))
			return "enhancedCS/variants";
		else if (component.equals(REQ_TM))
			return "enhancedCS/transferMode";
		return null;
	}

	private String getOptionsList(HttpSession session, String component, String fieldName)
	{
		List<BasicConfigurableParm[]> config = extractConfig(session, component);
		JsonObject job = new JsonObject();
		if (config != null && config.size() > 0)
		{
			int index = findConfigurationIndex(config.get(0), fieldName);
			if (index >= 0)
			{
				Set<String> values = new TreeSet<>();
				for (BasicConfigurableParm[] cps : config)
				{

					if (cps[index].getValue() != null)
					{
						String value = null;
						if (cps[index].getValue() instanceof String)
							value = (String) cps[index].getValue();
						else
							value = String.valueOf(cps[index].getValue());
						values.add(value);
					}
				}

				JsonArray jarr = new JsonArray();
				for (String val : values)
					jarr.add(new JsonPrimitive(val));
				job.add("options", jarr);
			}
		}
		else
			job.add("options", new JsonArray());
		return job.toString();
	}

}
