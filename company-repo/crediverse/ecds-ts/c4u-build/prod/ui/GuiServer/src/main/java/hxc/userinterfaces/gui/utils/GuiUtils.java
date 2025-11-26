package hxc.userinterfaces.gui.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.servicebus.ReturnCodes;
import hxc.services.notification.IPhrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.services.notification.Texts;
import hxc.userinterfaces.gui.data.LocaleInfo;
import hxc.userinterfaces.gui.data.NameDescription;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;
import hxc.utils.protocol.uiconnector.response.BasicConfigurableParm;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuiUtils
{
	final static Logger logger = LoggerFactory.getLogger(GuiUtils.class);
	public static String renderWithOptions(String name, String value, String[] options, boolean disabled)
	{
		// String [] ops = options.split(",");
		StringBuilder sb = new StringBuilder();
		sb.append("<select name='").append(name).append("' ").append(disabled ? "disabled='true' " : "").append(">\n");
		for (String op : options)
		{
			boolean selected = value.equalsIgnoreCase(op);
			sb.append("<option value='").append(op).append(selected ? "' selected='1' " : "' ").append(">").append(makePretty(op)).append("</option>\n");
		}
		sb.append("</select>\n");
		return sb.toString();
	}

	private static Object extractFieldValue(BasicConfigurableParm[] parms, String fieldName)
	{
		if (parms != null)
		{
			for (int i = 0; i < parms.length; i++)
			{
				if (parms[i].getFieldName().equalsIgnoreCase(fieldName))
				{
					return parms[i].getValue();
				}
			}
		}
		return null;
	}

	public static String extractVieldAsCurrency(Object params, String fieldName, int currencyDigits)
	{
		if (params != null)
		{
			BasicConfigurableParm[] parms = (BasicConfigurableParm[]) params;
			try
			{
				Object value = extractFieldValue(parms, fieldName);
				BigDecimal bd = new BigDecimal(value.toString());
				bd = bd.divide(new BigDecimal(Math.pow(10, currencyDigits)));
				bd = bd.setScale(currencyDigits, RoundingMode.CEILING);
				return bd.toString();
			}
			catch (Exception e)
			{
				return "---";
			}
		}
		return "";
	}

	public static Object extractField(Object params, String fieldName)
	{
		if (params != null)
		{
			BasicConfigurableParm[] parms = (BasicConfigurableParm[]) params;
			return extractFieldValue(parms, fieldName);
		}
		return "";
	}

	public static Object extractFieldText(Object params, String fieldName, int textFieldIndex)
	{
		if (params != null)
		{
			BasicConfigurableParm[] parms = (BasicConfigurableParm[]) params;
			Object value = extractFieldValue(parms, fieldName);
			if (value != null)
			{
				Texts text = (Texts) value;
				return text.getText(textFieldIndex);
			}
		}
		return "NOT FOUND";
	}

	public static Object extractPhraseField(Object params, String fieldName, String lang)
	{
		if (params != null)
		{
			Object value = extractField(params, fieldName);
			if (value != null)
			{
				IPhrase phrase = (IPhrase) value;
				return phrase.get(lang);
			}
		}
		return "NOT FOUND";
	}

	public static String stringToContentEditable(String text)
	{
		// String result = text.replaceAll("\n", "<br/>");
		if (text.indexOf("\n") > 0)
		{
			text = text.replaceAll("\n", "<br/>");
		}
		return text;
	}

	public static String extractFieldIds(Object field, String fieldName)
	{
		JsonArray jarr = new JsonArray();
		try
		{
			if (field != null)
			{
				List<BasicConfigurableParm[]> list = (List<BasicConfigurableParm[]>) field;
				for (BasicConfigurableParm[] bc : list)
				{
					String value = null;
					try
					{
						value = (String) extractFieldValue(bc, fieldName);
					}
					catch (Exception e)
					{
						value = String.valueOf(extractFieldValue(bc, fieldName));
					}
					jarr.add(new JsonPrimitive(value));
				}
			}
		}
		catch (Exception e)
		{
			return e.getMessage();
		}

		return jarr.toString();
	}

	public static String writeFieldComment(ConfigurableResponseParam field)
	{
		if (field.getComment() != null && field.getComment().length() > 0)
			return String.format("* %s", field.getComment());
		else
			return "";
	}
	
	public static String writeFieldLabel(ConfigurableResponseParam field)
	{
		if (field.getDescription() != null && field.getDescription().length() > 0)
			return field.getDescription();
		else 
			return splitCamelCaseString(field.getFieldName());
	}
	
	public static String splitCamelCaseString(String s)
	{
		StringBuilder sb = new StringBuilder();

		char prev = 0;
		char next = 0;
		s = s.replaceAll("_", " ");
		for (int i = 0; i < s.length(); i++)
		{
			if (i < (s.length() - 1))
				next = s.charAt(i + 1);
			else
				next = 0;

			char c = s.charAt(i);
			if (i == 0)
			{
				sb.append(String.valueOf(c).toUpperCase());
			}
			else
			{
				if (c >= 65 && c <= 90)
				{
					if ((prev < 65 || prev > 90) || (next > 0 && (next < 65 || next > 90)))
					{
						sb.append(" ");
					}
				}
				sb.append(c);
			}
			prev = c;
		}

		// Correct SMS / MMS
		String result = sb.toString();
		result = result.replaceFirst("(?i)sms ", "SMS ");
		result = result.replaceFirst("(?i)mms ", "MMS ");

		return result;
	}

	public String stringValue(Object objValue)
	{
		return objValue.toString();
	}

	public boolean stringEqualsValue(Object objValue, String value)
	{
		if (objValue == null)
			return value.equals("");
		else 
			return value.equals(objValue.toString());
	}
	
	public static String makePretty(String s)
	{
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (i == 0)
			{
				sb.append(String.valueOf(c).toUpperCase());
			}
			else
			{
				sb.append(String.valueOf(c).toLowerCase());
			}
		}
		return sb.toString();
	}

	public static boolean isNumber(String value)
	{
		try
		{
			Double.parseDouble(value);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	public static String printCurrency(String value)
	{
		BigDecimal bd = new BigDecimal(value);
		bd = bd.divide(new BigDecimal("100"));
		DecimalFormat df = new DecimalFormat("#0.00");
		return df.format(bd.doubleValue());
	}

	public static String extractCurrency(String value)
	{
		BigDecimal bd = new BigDecimal(value);
		bd = bd.multiply(new BigDecimal("100"));
		return String.valueOf(bd.toBigInteger().intValue());
	}

	public static String arrayAsCSV(Object[] value)
	{
		if (value == null)
			return "";

		StringBuilder sb = new StringBuilder();
		for (Object o : value)
		{
			if (sb.length() > 0)
				sb.append(",");

			if (o instanceof String)
				sb.append(o);
			else
				sb.append(String.valueOf(o));
		}
		return sb.toString();
	}

	public static String convertToLanguage(String standard)
	{
		Locale locale = new Locale(standard);
		return locale.getDisplayLanguage();
	}

	public static LocaleInfo extractLanguageInfo(String standard)
	{
		Locale locale = new Locale(standard);
		LocaleInfo li = new LocaleInfo();
		li.setLangDesc(locale.getDisplayLanguage());

		// TODO change to three letter languages
		String lang = locale.getLanguage();
		if ("ara".equals(lang) || "div".equals(lang) || "fas".equals(lang) || "hau".equals(lang) || "heb".equals(lang) || "iw".equals(lang) || "ji".equals(lang) || "pus".equals(lang)
				|| "urd".equals(lang) || "ji".equals(lang))
			li.setLangRTL(true);
		li.setLangCode(locale.getLanguage());
		return li;
	}

	public static String extractLanguageDirection(LocaleInfo info)
	{
		if (info == null)
			return "ltr";

		return info.isLangRTL() ? "rtl" : "ltr";
	}

	public static String buildJSON(Map<String, String> parms)
	{
		JsonObject job = new JsonObject();
		for (String key : parms.keySet())
		{
			job.add(key, new JsonPrimitive(parms.get(key)));
		}
		return job.toString();
	}

	public static void sortServerRoles(ServerRole[] sroles)
	{
		if (sroles != null)
		{
			Arrays.sort(sroles, new Comparator<ServerRole>()
			{

				@Override
				public int compare(ServerRole s1, ServerRole s2)
				{
					return s1.getServerRoleName().compareTo(s2.getServerRoleName());
				}
			});
		}
	}

	public static void sortServerHosts(ServerInfo[] sinfos)
	{
		if (sinfos != null)
		{
			Arrays.sort(sinfos, new Comparator<ServerInfo>()
			{

				@Override
				public int compare(ServerInfo s1, ServerInfo s2)
				{
					return s1.getServerHost().compareTo(s2.getServerHost());
				}
			});
		}
	}

	public static String getLocalhost()
	{
		String host;
		try
		{
			host = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			host = "localhost";
		}
		return host;
	}

	public static void sendResponse(HttpServletResponse response, String htmlString) throws IOException
	{
		PrintWriter out = response.getWriter();
		out.print(htmlString);
		out.flush();
	}

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

	public static Class<?> stringToClass(String className)
	{
		Class<?> typeClass = null;
		try
		{
			if (className.equals("int"))
			{
				return int.class;
			}
			else if (className.equals("long"))
			{
				return long.class;
			}
			else if (className.equals("byte"))
			{
				return byte.class;
			}
			else if (className.equals("float"))
			{
				return float.class;
			}
			else if (className.equals("double"))
			{
				return double.class;
			}
			else if (className.equals("boolean"))
			{
				return boolean.class;
			}
			else
			{
				typeClass = Class.forName(className);
			}
		}
		catch (Exception e)
		{
		}

		return typeClass;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T importFields(HttpServletRequest request, Class cls) throws InstantiationException, IllegalAccessException
	{
		@SuppressWarnings("unchecked")
		T result = (T) cls.newInstance();
		for (String key : request.getParameterMap().keySet())
		{
			String value = request.getParameterMap().get(key)[0];
			String fieldName = key;
			int index = -1;

			if (!(key.equals("index") || key.equals("comp") || key.equals("act")))
			{
				try
				{
					if (key.indexOf('_') > 0)
					{
						String[] keys = key.split("_");
						try
						{
							index = Integer.parseInt(keys[1]);
						}
						catch (Exception e)
						{
						}
						fieldName = keys[0];
						String methodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

						Method getter = result.getClass().getMethod(methodName);
						Texts it = (Texts) getter.invoke(result);

						if (it == null)
						{
							methodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
							Method setter = result.getClass().getMethod(methodName, Texts.class);
							setter.invoke(result, new Texts());
							it = (Texts) getter.invoke(result);
						}

						it.setText(index, value);
					}
					else
					{
						boolean convertToCents = false;
						if (fieldName.indexOf("CONVERTTOCENTS") > 0)
						{
							fieldName = fieldName.substring(0, fieldName.indexOf("CONVERTTOCENTS"));
							convertToCents = true;
						}

						Field field = result.getClass().getDeclaredField(fieldName);
						field.setAccessible(true);
						if (field.getType().equals(boolean.class))
						{
							field.set(result, true);
						}
						else if (field.getType().equals(int.class))
						{
							if (convertToCents)
							{
								BigDecimal bd = new BigDecimal(value);
								bd = bd.movePointRight(2);
								field.set(result, bd.intValue());
							}
							else
							{
								field.set(result, Integer.parseInt(value));
							}

						}
						else if (field.getType().isEnum())
						{
							field.set(result, Enum.valueOf((Class<Enum>) field.getType(), value));
						}
						else
						{
							field.set(result, value);
						}
					}
				}
				catch (NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException e)
				{
				}

			}
		}
		return result;
	}

	private static ReturnCodeTexts extractReturnCodeTexts(IConfigurableParam[] bcv)
	{
		if (bcv != null)
		{
			IConfigurableParam[] parms = (IConfigurableParam[]) bcv;
			return (ReturnCodeTexts) parms[0].getValue();
		}
		return null;
	}

	public static String extractReturnCodeLabel(IConfigurableParam[] bcv)
	{
		ReturnCodeTexts ret = extractReturnCodeTexts(bcv);
		if (ret != null)
		{
			return ret.getReturnCode().toString();
		}
		return "-";
	}

	public static String extractReturnCodeLanguage(IConfigurableParam[] bcv, String languageCode)
	{
		ReturnCodeTexts ret = extractReturnCodeTexts(bcv);
		if (ret != null)
		{
			return ret.getPhrase().get(languageCode);
		}

		return "-";
	}

	public static String[] returnCodeValues(Object existingValues)
	{
		Set<String> values = new TreeSet<>();
		for (ReturnCodes rc : ReturnCodes.values())
		{
			values.add(rc.name());
		}

		if (existingValues != null)
		{
			List<Object[]> lobjs = (List<Object[]>) existingValues;
			for (Object[] obj : lobjs)
			{
				IConfigurableParam[] parms = (IConfigurableParam[]) obj;
				ReturnCodeTexts rct = extractReturnCodeTexts(parms);
				values.remove(rct.getReturnCode().name());
			}
		}

		return values.toArray(new String[values.size()]);
	}

	public String extractFieldValue(IConfigurableParam[] bcv, String fieldName)
	{
		try
		{
			for (IConfigurableParam cp : bcv)
			{
				if (cp.getFieldName().equalsIgnoreCase(fieldName))
				{
					if ((cp.getValue() != null) && (cp.getValue() instanceof String))
						return (String) cp.getValue();
					else
						return cp.getValue().toString();
				}
			}
		}
		catch (Exception e)
		{
		}
		return "-n-";
	}

	// ---------------------------- Generic Advanced Page creation ----------------------

	public List<NameDescription> extractTabs(List<ConfigurableResponseParam> fields)
	{
		List<NameDescription> result = new ArrayList<>();

		// Tabs where order counts (general first, resultCodes second, USDD last)
		boolean isGeneral = false;
		String returnCodeField = null;
		String ussdField = null;

		// General Tab??
		for (ConfigurableResponseParam field : fields)
		{
			if (field.getStructure() == null)
				isGeneral = true;
			else
			{
				if (field.getValueType().indexOf("ReturnCodeTexts") > 0)
					returnCodeField = field.getFieldName();
				else
				{
					result.add(new NameDescription(field.getFieldName(), splitCamelCaseString(field.getFieldName())));
				}
			}

		}

		// Add items where order is standardized
		if (returnCodeField != null)
			result.add(0, new NameDescription("returnCodeTexts", "Return Codes"));

		if (isGeneral)
			result.add(0, new NameDescription("general", "General"));

		return result;
	}

	public String[] availableNonStructuredConfiguration(Object session)
	{
		List<String> list = new ArrayList<>();
		if (session != null)
		{
			list.add("item");
			// if (session.getAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_UNSTRUCT_LIST) != null) {
			// list = (List<String>)session.getAttribute(ComplexConfiguration.CPMLEX_FIELDNAME_UNSTRUCT_LIST);
			// }
		}
		return list.toArray(new String[list.size()]);
	}

	public boolean listContains(List<String> list, String element)
	{
		return (list != null && list.contains(element));
	}

	public static String extractLocaleDateFormat()
	{
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
		return ((SimpleDateFormat) dateFormat).toPattern();
	}

	public static String getPhraseLangString(String fieldName, Object phraseField, String langCode)
	{
		try
		{
			if (phraseField != null)
			{
				IPhrase phrase = (IPhrase) phraseField;
				String result = (phrase.get(langCode) == null) ? "" : phrase.get(langCode);
				logger.info(result);
				return result;
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			return e.getMessage();
		}
		// [Lhxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;@179fbfea
		return "";
	}
}
