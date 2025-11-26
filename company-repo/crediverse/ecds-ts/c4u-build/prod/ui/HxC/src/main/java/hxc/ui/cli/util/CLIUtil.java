package hxc.ui.cli.util;

import java.util.HashMap;

public class CLIUtil
{

	private static int USSDPos;

	// Converts the array to a string
	public static String convertArrayToString(String strings[], String separator)
	{
		// Ensure the separator is not null
		if (separator == null)
		{
			separator = "";
		}

		// Create a string builder
		StringBuilder builder = new StringBuilder();

		// Iterate through the array
		for (String string : strings)
		{
			builder.append(separator + string);
		}

		// Return the builder
		return builder.toString();
	}

	// Converts arguments to a map
	public static HashMap<String, String> getArgumentSwitches(String argumentLine, String special)
	{
		// Create the result map
		HashMap<String, String> ret = new HashMap<>();

		// Ensure the argument line is not null
		if (argumentLine == null)
		{
			return ret;
		}

		// Iterate while the argument line contains the -- characters
		while (argumentLine.contains("--"))
		{
			// Get the key
			String key = argumentLine.substring(argumentLine.indexOf("--"), argumentLine.indexOf("--") + 3).trim();

			// Remove the key
			argumentLine = argumentLine.substring(argumentLine.indexOf(key) + 3).trim();

			// Get the value
			String value = "";

			// Check for the quotations
			if (argumentLine.indexOf("\"") == 0)
			{
				// Get the value
				value = argumentLine.substring(1, argumentLine.lastIndexOf("\""));

				// Adjust the argument line
				argumentLine = argumentLine.substring(argumentLine.lastIndexOf("\"") + 1);
			}
			else
			{
				// Else get the value up until the next space
				if (argumentLine.indexOf(" ") > 0)
				{
					// Get the value
					value = argumentLine.substring(0, argumentLine.indexOf(" "));
				}
				else
				{
					// Else get the entire argument line
					value = argumentLine.substring(0);
				}

				// Adjust the argument list
				argumentLine = argumentLine.substring(argumentLine.indexOf(value) + value.length());
			}

			// Put the key and value in the result
			ret.put(key, value);
		}

		// Iterate while the argument line contains the - character
		while (argumentLine.contains("-"))
		{
			// Get the key
			String key = argumentLine.substring(argumentLine.indexOf("-"), argumentLine.indexOf("-") + 2).trim();

			// Adjust the argument line
			argumentLine = argumentLine.substring(argumentLine.indexOf(key) + 2).trim();

			// Get the value
			String value = "";

			// Check for the quotations
			if (argumentLine.indexOf("\"") == 0)
			{
				// Get the value
				value = argumentLine.substring(1, argumentLine.lastIndexOf("\""));

				// Adjust the argument line
				argumentLine = argumentLine.substring(argumentLine.lastIndexOf("\"") + 1);
			}
			else
			{
				// Else get the value up until the next space
				if (argumentLine.indexOf(" ") > 0)
				{
					// Get the value
					value = argumentLine.substring(0, argumentLine.indexOf(" "));
				}
				else
				{
					// Else get the entire argument line
					value = argumentLine.substring(0);
				}

				// Adjust the argument list
				argumentLine = argumentLine.substring(argumentLine.indexOf(value) + value.length());
			}

			// Put the key and value in the result
			ret.put(key, value);
		}

		// Check if a special variable has been set
		if (special != null && argumentLine.contains(special))
		{
			// Get the special from the argument list
			ret.put("special", argumentLine.substring(argumentLine.indexOf(special)));
		}

		// Get the rest of the argument list
		if (argumentLine.length() > 0)
		{
			ret.put("rest", argumentLine);
		}

		// Return the result map
		return ret;
	}

	// Gets the service code from the string
	public static String extractServiceCode(String USSDString)
	{
		// Gets the USSD start
		String USSD = USSDString.substring(USSDString.indexOf("*") + 1);

		// If there are more things after the next *
		if (USSD.indexOf('*') > 0)
			USSDPos = USSD.indexOf("*") + 1;
		
		// Else if it is the end of the USSD string
		else if (USSD.indexOf('#') > 0)
			USSDPos = USSD.indexOf("#") + 1;

		// Return the service code
		return USSDString.substring(USSDString.indexOf("*") + 1, (USSD.indexOf('*') > 0) ? USSDString.substring(USSDString.indexOf("*") + 1).indexOf("*") + 1 : USSDString.indexOf('#'));
	}

	// Returns the rest of the USSD string
	public static String extractUSSDRequestString(String USSDString)
	{
		// Get the rest of the USSD string
		return USSDString.substring(USSDPos); // ///////////////CHANGE////////////////////
	}

	// Checks if a string has a port
	public static String hasPort(String ip, int defaultPort)
	{
		// Check the port
		return (ip.contains(":")) ? "" : (":" + defaultPort);
	}
}
