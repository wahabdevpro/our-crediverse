package hxc.utils.protocol.caisim;

/**
 * Contains common functions and constants for use in CAI protocol handling.
 * 
 * @author petar
 *
 */
public class CaiCommon
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	
	public static final char ATTRIBUTE_DELIMITER = ':';
	public static final char SUB_ATTRIBUTE_DELIMITER = ',';
	public static final char COMMAND_DELIMITER = ';';
	
	public static final String CAI_DATE_FORMAT = "dd-MM-yyyy'T'HH:mm:ss";
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////
		
	/**
	 * Works according to EMA 4.0 CAI Specification - 4.1 Character Representation
	 * Doesn't support hex or octal for non-printable characters
	 * Escapes '"' and '\' characters.
	 * Quotes values that contain non-alphanumeric values (except for '-' and '_' ).
	 * Used when encoding a value and sending over CAI.
	 * 
	 * @param value the value to to escape
	 * @return the escaped value
	 */
	public static String escapeValue(String value)
	{
		String ret = new String(value);
		
		ret = ret.replace("\\", "\\\\");
		ret = ret.replace("\"", "\\\"");
		
		for (int i = 0; i < ret.length(); ++i)
		{
			final char ch = ret.charAt(i);
			if (!Character.isLetterOrDigit(ch) && ch != '-' && ch != '_')
			{
				ret = "\"" + ret + "\"";
				break;
			}
		}
		
		return ret;
	}
	
	/**
	 * Removes quotation (in the beginning of the value) and escaping inside.
	 * Used when receiving a value from CAI and consuming it inside CAISIM.
	 * 
	 * @param value the value to remove quotation and escaping from
	 * @return the value with removed quotation and escaping
	 */
	public static String removeQuotationAndEscaping(String value)
	{
		String ret = new String(value);
		
		if (ret.startsWith("\""))
			ret = ret.substring(1);
		
		if (ret.endsWith("\""))
			ret = ret.substring(0, ret.length() - 1);
		
		ret = ret.replace("\\", "");
		
		return ret;
	}
}
