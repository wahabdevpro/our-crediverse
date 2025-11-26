package hxc.userinterfaces.gui.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Map;

import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public class GuiUtils
{
	public static WebAppContext createResourceHandler(String contextPath, String baseDir, String path) throws IOException
	{
		WebAppContext handler = new WebAppContext();
		handler.setContextPath(contextPath);
		ResourceCollection resource = new ResourceCollection(new String[] { PathUtils.expandPath(baseDir + path) });
		handler.setBaseResource(resource);
		return handler;
	}

	public static String renderWithOptions(String name, String value, String options, boolean disabled)
	{
		String[] ops = options.split(",");
		StringBuilder sb = new StringBuilder();
		sb.append("<select name='").append(name).append("' ").append(disabled ? "disabled='true' " : "").append(">\n");
		for (String op : ops)
		{
			boolean selected = value.equalsIgnoreCase(op);
			sb.append("<option value='").append(op).append(selected ? "' selected='1' " : "' ").append(">").append(makePretty(op)).append("</option>\n");
		}
		sb.append("</select>\n");
		return sb.toString();
	}

	public static String splitCamelCaseString(String s)
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
				if (c >= 65 && c <= 90)
				{
					sb.append(" ");
				}
				sb.append(c);
			}
		}
		return sb.toString();
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
			double d = Double.parseDouble(value);
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

	public static String convertToLanguage(String standard)
	{
		Locale locale = new Locale(standard);
		return locale.getDisplayLanguage();
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

	public static boolean checkPortAvailable(int port)
	{

		ServerSocket ss = null;
		DatagramSocket ds = null;
		try
		{
			ss = new ServerSocket(port);
			ss.setReuseAddress(true);
			ds = new DatagramSocket(port);
			ds.setReuseAddress(true);
			return true;
		}
		catch (IOException e)
		{
		}
		finally
		{
			if (ds != null)
			{
				ds.close();
			}

			if (ss != null)
			{
				try
				{
					ss.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		return false;
	}

	public boolean isSimpleField(Class c)
	{
		return c.isPrimitive() || c == String.class || c == Boolean.class || c == Byte.class || c == Short.class || c == Character.class || c == Integer.class || c == Float.class || c == Double.class
				|| c == Long.class;
	}

	// public String extractContact(String addressDigits, List<ContactInfo> contactInfo)
	// {
	// String result = "UNKNOWN";
	// if (contactInfo != null)
	// {
	// for(ContactInfo ci : contactInfo)
	// {
	// if addressDigits.equals(ci.)
	// }
	// }
	// return null;
	// }
}
