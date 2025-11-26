package hxc.userinterfaces.cc.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import javax.xml.datatype.XMLGregorianCalendar;

import com.concurrent.hxc.GetLocaleSettingsResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

public class CCUtils
{
	public static enum sdf
	{
	};

	public String formatCurrency(int cents)
	{
		BigDecimal bd = new BigDecimal(cents);
		bd = bd.movePointLeft(2);
		String currency = bd.toPlainString().replace('.', ',');
		return currency;
	}

	public <E extends Enum<E>> String enumValue(E e)
	{
		return e.toString();
	}

	public String extractField(Object obj, String fieldName, int index)
	{
		String result = null;
		try
		{
		}
		catch (Exception e)
		{
			result = "-";
		}
		return result;
	}

	public String frenchToHtml(String french)
	{
		String[] entities = new String[] { "&amp;", "&#39;", "&OElig;", "&oelig;", "&Scaron;", "&scaron;", "&Yuml;", "&circ;", "&tilde;", "&ndash;", "&mdash;", "&lsquo;", "&rsquo;", "&sbquo;",
				"&ldquo;", "&rdquo;", "&bdquo;", "&dagger;", "&Dagger;", "&hellip;", "&permil;", "&lsaquo;", "&rsaquo;", "&euro;", "&Agrave;", "&Aacute;", "&Acirc;", "&Atilde;", "&Auml;", "&Aring;",
				"&AElig;", "&Ccedil;", "&Egrave;", "&Eacute;", "&Ecirc;", "&Euml;", "&Igrave;", "&Iacute;", "&Icirc;", "&Iuml;", "&ETH;", "&Ntilde;", "&Ograve;", "&Oacute;", "&Ocirc;", "&Otilde;",
				"&Ouml;", "&Oslash;", "&Ugrave;", "&Uacute;", "&Ucirc;", "&Uuml;", "&Yacute;", "&THORN;", "&szlig;", "&agrave;", "&aacute;", "&acirc;", "&atilde;", "&auml;", "&aring;", "&aelig;",
				"&ccedil;", "&egrave;", "&eacute;", "&ecirc;", "&euml;", "&igrave;", "&iacute;", "&icirc;", "&iuml;", "&eth;", "&ntilde;", "&ograve;", "&oacute;", "&ocirc;", "&otilde;", "&ouml;",
				"&oslash;", "&ugrave;", "&uacute;", "&ucirc;", "&uuml;", "&yacute;", "&thorn;", "&yuml;", "&iexcl;", "&curren;", "&cent;", "&pound;", "&yen;", "&brvbar;", "&sect;", "&uml;", "&copy;",
				"&ordf;", "&laquo;", "&not;", "&reg;", "&trade;", "&macr;", "&deg;", "&plusmn;", "&sup2;", "&sup3;", "&acute;", "&micro;", "&para;", "&middot;", "&cedil;", "&sup1;", "&ordm;",
				"&raquo;", "&frac14;", "&frac12;", "&frac34;", "&iquest;", "&times;", "&divide;" };

		String[] uChar = new String[] { "&", "\'", "Œ", "œ", "Š", "š", "Ÿ", "ˆ", "˜", "–", "—", "‘", "’", "‚", "“", "”", "„", "†", "‡", "…", "‰", "‹", "›", "€", "À", "Á", "Â", "Ã", "Ä", "Å", "Æ",
				"Ç", "È", "É", "Ê", "Ë", "Ì", "Í", "Î", "Ï", "Ð", "Ñ", "Ò", "Ó", "Ô", "Õ", "Ö", "Ø", "Ù", "Ú", "Û", "Ü", "Ý", "Þ", "ß", "à", "á", "â", "ã", "ä", "å", "æ", "ç", "è", "é", "ê", "ë",
				"ì", "í", "î", "ï", "ð", "ñ", "ò", "ó", "ô", "õ", "ö", "ø", "ù", "ú", "û", "ü", "ý", "þ", "ÿ", "¡", "¤", "¢", "£", "¥", "¦", "§", "¨", "©", "ª", "«", "¬", "®", "™", "¯", "°", "±",
				"²", "³", "´", "µ", "¶", "·", "¸", "¹", "º", "»", "¼", "½", "¾", "¿", "×", "÷" };

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < french.length(); i++)
		{
			String oldChar = french.substring(i, i + 1);
			String newChar = oldChar;
			for (int j = 0; j < uChar.length; j++)
			{
				if (uChar[j].equals(oldChar))
				{
					newChar = entities[j];
					break;
				}
			}
			sb.append(newChar);
		}

		return sb.toString();
	}

	// public static void main(String [] args) {
	// CustomerCareUtils cc = new CustomerCareUtils();
	// String deciphered = cc.frenchToHtml("ê");
	// System.out??.println(deciphered);
	//
	// }

	public static String currencyConversion(long chargingSystemAmount)
	{
		BigDecimal bd = new BigDecimal(chargingSystemAmount);
		bd = bd.movePointLeft(2);
		return bd.toString();
	}

	public static long scaleNumber(String number, long numerator, long denominator)
	{
		BigDecimal result = new BigDecimal(number);
		result = result.multiply(new BigDecimal(numerator));
		result = result.divide(new BigDecimal(denominator));
		return result.longValue();
	}

	public static JsonArray createJsonDataArray(String[] data)
	{
		JsonArray jarr = new JsonArray();
		for (String s : data)
		{
			jarr.add(new JsonPrimitive(s));
		}
		return jarr;
	}

	public static String displayAmount(long amount, long numerator, long denominator)
	{
		BigDecimal bd = new BigDecimal(amount);
		bd = bd.multiply(new BigDecimal(denominator));
		bd = bd.divide(new BigDecimal(numerator));
		String result = bd.setScale(2, RoundingMode.CEILING).toPlainString();

		return result;
	}

	public static String formatDate(XMLGregorianCalendar date)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		GregorianCalendar gc = date.toGregorianCalendar();
		return sdf.format(gc.getTime());
	}
	
    public static String convertCurrencyToBaseUnits(GetLocaleSettingsResponse locale, long cents)
    {
    	BigDecimal usd = new BigDecimal(cents);
        usd = usd.movePointLeft(locale.getCurrencyDecimalDigits());
        
        return usd.toString();
    }
    
    public static long convertFromBaseToCents(GetLocaleSettingsResponse locale, String currency)
    {
    	BigDecimal cents = new BigDecimal(currency);
    	cents = cents.movePointRight(locale.getCurrencyDecimalDigits());
    	
    	return cents.longValue();
    }

	public static void main(String[] args)
	{
		long amount = 0;
		System.out.println(currencyConversion(amount));
	}
}
