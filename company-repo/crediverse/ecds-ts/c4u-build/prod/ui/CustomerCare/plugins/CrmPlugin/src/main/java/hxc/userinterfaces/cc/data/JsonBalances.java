package hxc.userinterfaces.cc.data;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import com.concurrent.hxc.GetLocaleSettingsResponse;
import com.concurrent.hxc.ServiceBalance;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import hxc.userinterfaces.cc.utils.CCUtils;

public class JsonBalances
{
	private String title = null; // For table heading
	private String msg = null; // For alert
	private String dateFormat = null;
	private List<ServiceBalance> serviceBalances = null;
	private GetLocaleSettingsResponse locale = null;
	
	public JsonBalances()
	{
	}

	/**
	 * @return the msg
	 */
	public String getMsg()
	{
		return msg;
	}

	/**
	 * @param msg
	 *            the msg to set
	 */
	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	/**
	 * @return the serviceBalances
	 */
	public List<ServiceBalance> getServiceBalances()
	{
		return serviceBalances;
	}

	/**
	 * @param serviceBalances
	 *            the serviceBalances to set
	 */
	public void setServiceBalances(List<ServiceBalance> serviceBalances)
	{
		this.serviceBalances = serviceBalances;
	}

	/**
	 * @return the dateFormat
	 */
	public String getDateFormat()
	{
		return dateFormat;
	}

	/**
	 * @param dateFormat
	 *            the dateFormat to set
	 */
	public void setDateFormat(String dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	/**
	 * @return the title
	 */
	public String getTitle()
	{
		return title;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title)
	{
		this.title = title;
	}

	public GetLocaleSettingsResponse getLocale()
	{
		return locale;
	}

	public void setLocale(GetLocaleSettingsResponse locale)
	{
		this.locale = locale;
	}

	private JsonObject toJson()
	{
		JsonObject job = new JsonObject();
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			job.add("msg", new JsonPrimitive(msg));
			job.add("title", new JsonPrimitive(title));
			JsonArray jarr = new JsonArray();
			if (serviceBalances != null && (serviceBalances.size() > 0))
			{
				for (ServiceBalance sb : serviceBalances)
				{
					JsonObject bal = new JsonObject();
					bal.add("name", new JsonPrimitive(sb.getName()));
					
					String value = (sb.getUnit().equals(locale.getCurrencyCode()))? CCUtils.convertCurrencyToBaseUnits(locale, sb.getValue()) : String.valueOf(sb.getValue());
					bal.add("value", new JsonPrimitive(value));
					bal.add("unit", new JsonPrimitive(sb.getUnit()));

					XMLGregorianCalendar exdate = sb.getExpiryDate();
					GregorianCalendar gc = exdate.toGregorianCalendar();
					bal.add("expiry", new JsonPrimitive(sdf.format(gc.getTime())));
					jarr.add(bal);
				}
			}

			job.add("bal", jarr);
		}
		catch (Exception ex)
		{
		}

		return job;
	}

	@Override
	public String toString()
	{
		return toJson().toString();
	}

}
