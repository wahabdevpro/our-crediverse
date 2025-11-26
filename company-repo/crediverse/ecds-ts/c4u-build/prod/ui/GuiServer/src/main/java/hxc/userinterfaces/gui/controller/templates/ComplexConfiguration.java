package hxc.userinterfaces.gui.controller.templates;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.ValidationException;

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
import hxc.userinterfaces.gui.data.GuiUpdateResponse;
import hxc.userinterfaces.gui.data.GuiUpdateResponse.OperationStatus;
import hxc.userinterfaces.gui.data.User;
import hxc.userinterfaces.gui.thymeleaf.IThymeleafController;
import hxc.userinterfaces.gui.uiconnect.UiConnectionClient;
import hxc.userinterfaces.gui.utils.GuiUtils;
import hxc.userinterfaces.gui.utils.UssdProcessHelper;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

public abstract class ComplexConfiguration implements IThymeleafController
{
	/*
	 * Rules: fields names stored as: session.FieldName -> BasicConfigurableParm[] (Values) field Structure stores as: session.FieldNameStruct -> ConfigurableResponseParam[] (Structure) field was
	 * updated: session.FieldNameUpdated -> true / null
	 */

	public static final String STRUCTURE_SUFFIX = "Struct";
	public static final String TYPE_SUFFIX = "Type";
	public static final String UPDATED_SUFFIX = "Updated";
	public static final String CPMLEX_FIELDNAME_STRUCT_LIST = "StructAdvConfig";
	public static final String CPMLEX_FIELDNAME_UNSTRUCT_LIST = "UnStructAdvConfig";

	/**
	 * Parameters passed from page: act := action comp := field index := index for array
	 */
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
							json = getFieldDataValues(component, session, index);
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
					// ------- Properties required for creating forms
					case "struct":
						json = retrieveStructure(session, component);
						break;
					case "table": // data | template request?
						// Generate table from data for this field (Show first 5 columns)
						String prms = request.getParameter("prms");
						String[] params = (prms == null) ? null : (prms.split(","));
						json = getContentForTable(session, component, params);
						break;
					case "tabsinfo":
						// Pass field information to buildTab contends
						json = getFieldInfoForTabs(session);
						break;
					default:
						handle(component, action);
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

	/**
	 * Extract Field Structure
	 * 
	 * @param fieldName
	 *            FieldName in configuration
	 */
	private ConfigurableResponseParam[] extractFieldStructure(HttpSession session, String fieldName)
	{
		ConfigurableResponseParam[] result = (ConfigurableResponseParam[]) session.getAttribute(fieldName + STRUCTURE_SUFFIX);

		return result;
	}

	/**
	 * Extract Values relating to a specific Field
	 * 
	 * @param fieldName
	 *            FieldName in configuration
	 */
	private List<BasicConfigurableParm[]> extractConfig(HttpSession session, String fieldName)
	{
		Object obj = session.getAttribute(fieldName);
		if (obj != null)
			return ((List<BasicConfigurableParm[]>) obj);
		else
			return null;
	}

	private BasicConfigurableParm[] createDefaultValueSet(HttpSession session, String component)
	{
		ConfigurableResponseParam[] structure = extractFieldStructure(session, component);

		BasicConfigurableParm[] fields = new BasicConfigurableParm[structure.length];
		for (int i = 0; i < structure.length; i++)
		{
			fields[i] = new BasicConfigurableParm(structure[i].getFieldName());
			Object value = ConfigurableResponseParamToBaseValue(structure[i].getValueType());
			fields[i].setValue(value);
		}
		return fields;
	}

	private Object convertDataToArray(String data, String valueType)
	{
		String[] values = data.split(",");

		if (valueType.equalsIgnoreCase("[I"))
		{
			int[] value = new int[values.length];
			for (int i = 0; i < values.length; i++)
			{
				value[i] = Integer.parseInt(values[i]);
			}
			return value;
		}
		else if (valueType.equalsIgnoreCase("[Ljava.lang.Integer;"))
		{
			Integer[] value = new Integer[values.length];
			for (int i = 0; i < values.length; i++)
			{
				value[i] = Integer.parseInt(values[i]);
			}
			return value;
		}
		else if (valueType.equalsIgnoreCase("[Ljava.lang.String;"))
		{
			return values;
		}
		return null;
	}

	/**
	 * Create Some "Default Value" for a field type
	 */
	// private Object ConfigurableResponseParamToBaseValue(ConfigurableResponseParam param)
	private Object ConfigurableResponseParamToBaseValue(String type)
	{
		Object value = null;
		if (type.equalsIgnoreCase("int"))
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
		else if (type.equalsIgnoreCase("java.lang.String"))
			value = new String();
		else if (type.equalsIgnoreCase("java.lang.Integer"))
			value = Integer.valueOf(0);
		else if (type.equalsIgnoreCase("hxc.utils.calendar.TimeUnits"))
			value = new String();
		else if (type.equalsIgnoreCase("[I"))
			value = new int[0];
		else if (type.equalsIgnoreCase("[Ljava.lang.Integer;"))
			value = new Integer[0];
		else if (type.equalsIgnoreCase("[Ljava.lang.String;"))
			value = new String[0];
		else if (type.equalsIgnoreCase("hxc.services.notification.Phrase"))
			value = new Phrase();
		else
		{
			value = "";
		}
		return value;
	}

	private Object castToType(Class clazz, String value)
	{
		if (Boolean.class == clazz || Boolean.TYPE == clazz)
			return Boolean.parseBoolean(value);
		if (Byte.class == clazz || Byte.TYPE == clazz)
			return Byte.parseByte(value);
		if (Short.class == clazz || Short.TYPE == clazz)
			return Short.parseShort(value);
		if (Integer.class == clazz || Integer.TYPE == clazz)
			return Integer.parseInt(value);
		if (Long.class == clazz || Long.TYPE == clazz)
			return Long.parseLong(value);
		if (Float.class == clazz || Float.TYPE == clazz)
			return Float.parseFloat(value);
		if (Double.class == clazz || Double.TYPE == clazz)
			return Double.parseDouble(value);
		if (clazz.toString().startsWith("["))
			return convertDataToArray(value, clazz.toString());
		return value;
	}

	private String extractObjectArray(Object[] values)
	{
		StringBuilder sb = new StringBuilder();
		for (Object v : values)
		{
			if (sb.length() > 0)
				sb.append(',');
			sb.append(v.toString());
		}
		return sb.toString();
	}

	private String extractObjectArray(String[] values)
	{
		StringBuilder sb = new StringBuilder();
		for (String v : values)
		{
			if (sb.length() > 0)
				sb.append(',');
			sb.append(v);
		}
		return sb.toString();
	}

	private String extractObjectArray(int[] values)
	{
		StringBuilder sb = new StringBuilder();
		for (int v : values)
		{
			if (sb.length() > 0)
				sb.append(',');
			sb.append(v);
		}
		return sb.toString();
	}

	private String extractArray(Object array)
	{
		if (array instanceof int[])
			return extractObjectArray((int[]) array);
		else if (array instanceof String[])
			return extractObjectArray((String[]) array);
		else if (array instanceof Object[])
			return extractObjectArray((Object[]) array);
		return array.toString();
	}

	private JsonObject extractFields(BasicConfigurableParm[] parms)
	{
		JsonObject dataJob = new JsonObject();
		try
		{
			// Data (fields + values)

			for (int i = 0; i < parms.length; i++)
			{
				// String fieldName = parms[i].getFieldName().substring(0, 1).toLowerCase() + parms[i].getFieldName().substring(1);
				String fieldName = parms[i].getFieldName();

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
					dataJob.add(fieldName, jarr);
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
				else if ((parms[i].getValue() != null) && (parms[i].getValue() instanceof List))
				{
					@SuppressWarnings("unchecked")
					List<IConfigurableParam[]> data = (List<IConfigurableParam[]>) parms[i].getValue();
					JsonArray arrOuter = new JsonArray();
					for (int j = 0; j < data.size(); j++)
					{
						JsonArray arrInner = new JsonArray();
						IConfigurableParam[] row = (IConfigurableParam[]) data.get(j);
						for (IConfigurableParam ri : row)
						{
							String sdata = null;
							if ((ri.getValue() != null) && ((ri.getValue() instanceof Object[]) || (ri.getValue() instanceof int[]) || (ri.getValue() instanceof String[])))
								sdata = extractArray(ri.getValue());
							else
								sdata = ri.getValue().toString();
							arrInner.add(new JsonPrimitive(sdata));
						}
						arrOuter.add(arrInner);
					}
					dataJob.add(parms[i].getFieldName(), arrOuter);
				}
				else if ((parms[i].getValue() != null) && ((parms[i].getValue() instanceof Object[]) || (parms[i].getValue() instanceof int[]) || (parms[i].getValue() instanceof String[])))
				{
					String data = extractArray(parms[i].getValue());
					dataJob.add(fieldName, new JsonPrimitive(data));
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

	private String extractRenderHints(String component, HttpSession session)
	{
		ConfigurableResponseParam[] struct = extractFieldStructure(session, component);
		JsonObject dec = extractDecorations(struct);
		return dec.toString();
	}

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

	/**
	 * Extract text/JSON representation of component/field values for page
	 */
	private String getFieldDataValues(String component, HttpSession session, int index)
	{
		String result = null;
		List<BasicConfigurableParm[]> config = extractConfig(session, component);

		if (config != null && index < config.size())
		{
			JsonObject job = extractFields(config.get(index));
			ConfigurableResponseParam[] struct = extractFieldStructure(session, component);
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

	/**
	 * Delete Configuration component @ index
	 */
	private String deleteConfig(String component, HttpSession session, int index)
	{
		List<BasicConfigurableParm[]> config = extractConfig(session, component);

		if (config != null && index < config.size())
		{
			// First Check for unique field
			ConfigurableResponseParam[] structure = extractFieldStructure(session, component);
			if (structure != null)
			{
				for (int i = 0; i < structure.length; i++)
				{
					if (structure[i].isUnique())
					{
						String fullFieldName = String.format("%s.%s", component, structure[i].getFieldName());
						String currentValue = String.class.cast(config.get(index)[i].getValue());
						updateReferencedValues(session, fullFieldName, currentValue, null);
					}
				}
			}

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

	/**
	 * Save Updated Configuration Values back and mark as updated
	 */
	private <T> void saveList(HttpSession session, String component, List<T> configToSave)
	{
		session.setAttribute(component, configToSave);
		session.setAttribute(component + UPDATED_SUFFIX, true);
	}

	private int findConfigurationIndex(IConfigurableParam[] parms, String fieldName)
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

	private boolean isUniqueKey(List<BasicConfigurableParm[]> config, int indexField, int indexUpdating, String value)
	{
		for (int i = 0; i < config.size(); i++)
		{
			if (i != indexUpdating)
			{
				BasicConfigurableParm[] parms = (BasicConfigurableParm[]) config.get(i);
				if (String.class.cast(parms[indexField].getValue()).equals(value))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 
	 * @param session
	 * @param referenceField
	 * @param oldValue
	 * @param newValue
	 *            null for removal of value
	 */
	private void updateReferencedValues(HttpSession session, String referenceField, String oldValue, String newValue)
	{
		// Find components
		@SuppressWarnings("unchecked")
		List<String> structFields = (List<String>) session.getAttribute(CPMLEX_FIELDNAME_STRUCT_LIST);

		// Search all parameters for values
		if (structFields != null)
		{
			for (String component : structFields)
			{

				ConfigurableResponseParam[] structure = extractFieldStructure(session, component);
				if (structure != null)
				{
					for (int index = 0; index < structure.length; index++)
					{
						if ((structure[index].getReferencesKey() != null) && structure[index].getReferencesKey().equalsIgnoreCase(referenceField))
						{
							List<BasicConfigurableParm[]> config = (List<BasicConfigurableParm[]>) extractConfig(session, component);
							for (int i = 0; i < config.size(); i++)
							{
								if (structure[index].getValueType().startsWith("["))
								{
									Object[] values = (Object[]) config.get(i)[index].getValue();
									boolean update = false;
									if (oldValue != null)
									{
										int indexToRemove = -1;
										for (int in = 0; in < values.length; in++)
										{
											if (values[in] != null && String.class.cast(values[in]).equals(oldValue))
											{
												if (newValue == null)
													indexToRemove = in;
												else
													values[in] = values[in].getClass().cast(newValue);
												update = true;
												break;
											}
										}
										if (indexToRemove >= 0)
										{
											List<?> objs = new LinkedList(Arrays.asList(values));
											objs.remove(indexToRemove);

											// Deal with type erasure
											Object[] arr = (Object[]) Array.newInstance(values.getClass().getComponentType(), objs.size());
											values = objs.toArray(arr);
										}
									}

									if (update)
									{
										config.get(i)[index].setValue(values);
									}
								}
							}
							saveList(session, component, config);
						}
					}
				}
			}
		}
	}

	private String updateComponentValues(HttpSession session, HttpServletRequest request, String component, int index)
	{
		String lastField = null;
		try
		{
			int currencyDigits = (int) session.getAttribute("curDigits");

			// Get the structure to the Component to update
			List<BasicConfigurableParm[]> config = (List<BasicConfigurableParm[]>) extractConfig(session, component);
			ConfigurableResponseParam[] structure = extractFieldStructure(session, component);
			BasicConfigurableParm[] fields = null;

			// Get a basic template to fill in
			if (index < 0)
				fields = createDefaultValueSet(session, component);
			else
				fields = (BasicConfigurableParm[]) config.get(index);

			for (String key : request.getParameterMap().keySet())
			{
				lastField = key;

				if (!(key.equals("index") || key.equals("comp") || key.equals("act")))
				{
					String value = request.getParameterMap().get(key)[0];

					if (key.indexOf('_') > 0)
					{
						if (key.indexOf("OARR_") != 0)
						{
							// iText Messages
							String[] keys = key.split("_");
							int msgIndex = 0;
							lastField = keys[0];
							int parmIndex = findConfigurationIndex(fields, lastField);
							
							try
							{
								// Texts
								msgIndex = Integer.parseInt(keys[1]);
								((Texts) fields[parmIndex].getValue()).setText(msgIndex, value);
							}
							catch (Exception e)
							{
								// Phrases
								((Phrase) fields[parmIndex].getValue()).set(keys[1], value);
							}
						}
					}
					else
					{
						// Other properties (Not text)
						int parmIndex = findConfigurationIndex(fields, lastField);
						if (parmIndex >= 0)
						{
							// Find structure
							int structIndex = -1;
							for (int i = 0; i < structure.length; i++)
							{
								if (structure[i].getFieldName().equalsIgnoreCase(lastField))
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

								// Check for uniqueness
								if (structure[structIndex].isUnique())
								{
									if (!isUniqueKey(config, parmIndex, index, value))
										throw new ValidationException("Field not unique");
									else
									{
										// Check for fields depending on this field in the other structures
										String fullFieldName = String.format("%s.%s", component, lastField);
										String currentValue = String.class.cast(fields[parmIndex].getValue());

										updateReferencedValues(session, fullFieldName, currentValue, value);
									}
								}

								// TODO:Refactor Me ==>
								if (fields[parmIndex].getValue() == null)
								{
									// Find type from structure
									for (ConfigurableResponseParam cresp : structure)
									{
										if (cresp.getFieldName().equals(fields[parmIndex].getFieldName()))
										{
											fields[parmIndex].setValue(ConfigurableResponseParamToBaseValue(cresp.getValueType()));
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

									if (fields[parmIndex].getValue().getClass().equals(Integer.class))
										try
										{
											fields[parmIndex].setValue(Integer.parseInt(value));
										}
										catch (Exception e)
										{
											throw new ValidationException("Integer value required");
											// fields[parmIndex].setValue(null);
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

			// Now try and find complex configurations by going through the structure
			for (ConfigurableResponseParam crp : structure)
			{
				if (crp.getStructure() != null)
				{
					@SuppressWarnings("unchecked")
					List<String> pageParms = new ArrayList<String>();
					Set<Integer> indexes = new TreeSet<Integer>();
					for (String field : request.getParameterMap().keySet())
					{
						if (field.indexOf("OARR_" + crp.getFieldName()) == 0)
						{
							pageParms.add(field);
							try
							{
								String[] s = field.split("_");
								int idx = Integer.parseInt(s[2]);
								indexes.add(idx);
							}
							catch (Exception e)
							{
							}
						}
					}

					// Destroy and Rebuild Data structure (first find Field)
					int fieldNo = -1;
					for (int i = 0; i < fields.length; i++)
					{
						if (fields[i].getFieldName().equals(crp.getFieldName()))
						{
							fieldNo = i;
							break;
						}
					}

					if (fieldNo >= 0)
					{
						// Create Structure
						List<IConfigurableParam[]> dataSet = new ArrayList<>();
						for (Integer pageIndex : indexes)
						{
							IConfigurableParam[] row = new BasicConfigurableParm[crp.getStructure().length];
							for (int i = 0; i < row.length; i++)
							{
								row[i] = new BasicConfigurableParm();
								row[i].setFieldName(crp.getStructure()[i].getFieldName());
								String field = "OARR_" + crp.getFieldName() + "_" + pageIndex + "_" + i;
								lastField = field;
								String value = request.getParameter(field);

								if (crp.getStructure()[i].getValueType().indexOf("[") == 0)
									row[i].setValue(convertDataToArray(value, crp.getStructure()[i].getValueType()));
								else
								{
									String type = crp.getStructure()[i].getValueType();
									Object obj = ConfigurableResponseParamToBaseValue(type);
									obj = castToType(obj.getClass(), value);
									// obj.getClass().cast(value);
									row[i].setValue(obj);
								}
								// row[i].setValue(value);

							}
							dataSet.add(row);
						}

						// Now Insert
						fields[fieldNo].setValue(dataSet);
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
			String errorField = String.format("%s[%d].%s", component, index, lastField);
			return (new GuiUpdateResponse(OperationStatus.fail, e.getMessage(), errorField)).toString();
		}
		return (new GuiUpdateResponse(OperationStatus.pass, "updated")).toString();
	}

	// ------ Structure
	private String simplifyType(String typeFromServer)
	{
		String value = typeFromServer;
		if (typeFromServer.indexOf('[') == 0)
		{
			if (typeFromServer.equalsIgnoreCase("[I") || typeFromServer.equalsIgnoreCase("[Ljava.lang.Integer;"))
				value = "IArray";
			else if (typeFromServer.equalsIgnoreCase("[Ljava.lang.String;"))
				value = "IString";
			else
				value = "OArray";
		}
		else if (typeFromServer.indexOf('.') > 0)
		{
			value = (typeFromServer.substring(typeFromServer.lastIndexOf('.') + 1));
		}

		return value;
	}

	private JsonObject retrieveJsonStructure(ConfigurableResponseParam[] structures) throws Exception
	{
		JsonObject job = new JsonObject();
		try
		{
			JsonArray allParams = new JsonArray();

			for (ConfigurableResponseParam param : structures)
			{
				JsonObject jo = new JsonObject();
				jo.add("name", new JsonPrimitive(param.getFieldName()));
				String description = param.getDescription();

				if (description == null || description.length() == 0)
				{
					description = GuiUtils.splitCamelCaseString(param.getFieldName());
				}
				jo.add("lbl", new JsonPrimitive(description));

				if (param.getPossibleValues() != null && param.getPossibleValues().length > 0)
				{
					jo.add("type", new JsonPrimitive("sel"));
					JsonArray jarr = new JsonArray();
					for (String opt : param.getPossibleValues())
					{
						jarr.add(new JsonPrimitive(opt));
					}
					jo.add("opts", jarr);
				}
				else if (param.getStructure() != null)
				{
					jo.add("type", new JsonPrimitive(simplifyType(param.getValueType())));
					ConfigurableResponseParam[] struct = param.getStructure();
					jo.add("parms", retrieveJsonStructure(struct).get("params"));
				}
				else
				{
					jo.add("type", new JsonPrimitive(simplifyType(param.getValueType())));
				}

				// ???
				if ((param.getGroup()) != null && (param.getGroup().length() > 0))
					jo.add("group", new JsonPrimitive(param.getGroup()));

				if (param.isUnique())
					jo.add("unique", new JsonPrimitive(true));

				// String refKey = param.getReferencesKey();
				if (param.getReferencesKey() != null && (param.getReferencesKey().length() > 0))
					jo.add("refKey", new JsonPrimitive(param.getReferencesKey()));

				allParams.add(jo);
			}
			job.add("params", allParams);
			return job;
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	private String retrieveStructure(HttpSession session, String fieldName)
	{
		try
		{
			ConfigurableResponseParam[] structures = extractFieldStructure(session, fieldName);
			return retrieveJsonStructure(structures).toString();
		}
		catch (Exception e)
		{
			return (new GuiUpdateResponse(OperationStatus.fail, e.getMessage())).toString();
		}
	}

	private <T> String arrayToValue(T[] array)
	{
		StringBuilder sb = new StringBuilder();
		if (array != null)
		{
			for (T e : array)
			{
				if (sb.length() > 0)
				{
					sb.append(",");
				}
				if (e != null && (e instanceof String))
					sb.append((String) e);
				else
					sb.append(String.valueOf(e));
			}
		}
		return sb.toString();
	}

	private String getContentForTable(HttpSession session, String fieldName, String[] params)
	{
		try
		{
			JsonObject job = new JsonObject();

			// Get entries
			List<BasicConfigurableParm[]> config = extractConfig(session, fieldName);
			ConfigurableResponseParam[] structures = extractFieldStructure(session, fieldName);
			List<String> parmsToExtract = new ArrayList<>();

			// Step 1 : Headers
			JsonArray headers = new JsonArray();
			if (params != null)
			{
				for (String prm : params)
				{
					parmsToExtract.add(prm);
					int index = findConfigurationIndex(structures, prm);
					String header = null;
					if ((index >= 0) && (structures[index].getDescription() != null && structures[index].getDescription().length() > 0))
						header = structures[index].getDescription();
					else
						header = GuiUtils.splitCamelCaseString(prm);
					headers.add(new JsonPrimitive(header));
				}
			}
			else
			{

				for (ConfigurableResponseParam cp : structures)
				{
					if ((cp.getDescription() != null && cp.getDescription().length() > 0))
						headers.add(new JsonPrimitive(cp.getDescription()));
					else
						headers.add(new JsonPrimitive(GuiUtils.splitCamelCaseString(cp.getFieldName())));

					parmsToExtract.add(cp.getFieldName());
					if (parmsToExtract.size() > 4)
						break;
				}
			}
			job.add("headers", headers);

			// Step 2 content
			JsonArray items = new JsonArray();
			if (config != null && config.size() > 0)
			{
				for (BasicConfigurableParm[] cpl : config)
				{
					JsonArray ja = new JsonArray();
					for (String prm : parmsToExtract)
					{
						int index = findConfigurationIndex(cpl, prm);
						if (index >= 0)
						{
							String valueType = structures[index].getValueType().toLowerCase();
							String value = "";
							if (cpl[index].getValue() != null && cpl[index].getValue().getClass().isArray())
							{
								value = arrayToValue((Object[]) cpl[index].getValue());
							}
							else
							{
								switch (valueType)
								{
									case "int":
									case "long":
									case "double":
									case "float":
									case "char":
										value = String.valueOf(cpl[index].getValue());
										break;
									case "hxc.services.notification.texts":
										value = ((Texts) cpl[index].getValue()).getSafeText(1);
										break;
									default:
										value = cpl[index].getValue().toString();
								}
							}

							ja.add(new JsonPrimitive(value));
						}
					}
					items.add(ja);
				}
			}
			job.add("items", items);
			return job.toString();
		}
		catch (Exception e)
		{
			return (new GuiUpdateResponse(OperationStatus.fail, e.getMessage())).toString();
		}
	}

	private String getFieldInfoForTabs(HttpSession session)
	{
		JsonObject job = new JsonObject();
		List<String> structFields = (List<String>) session.getAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_STRUCT_LIST);
		List<String> unstructFields = (List<String>) session.getAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_UNSTRUCT_LIST);

		List<String> all = new ArrayList<String>();
		if (structFields != null && structFields.size() > 0)
			all.addAll(structFields);
		if (unstructFields != null && structFields.size() > 0)
			all.addAll(unstructFields);

		for (String fieldName : all)
		{
			String type = (String) session.getAttribute(fieldName + ComplexConfiguration.TYPE_SUFFIX);

			if (type == null)
			{
				type = "UNKNOWN";
			}

			if (type.indexOf("VasCommand") > 0)
				type = "VasCommand";

			job.add(fieldName, new JsonPrimitive(type));
		}

		return job.toString();
	}

	// ------ Abstrct Handlers
	public abstract void handle(String component, String action);

	public abstract String getComponentPage(String component);
}
