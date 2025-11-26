package hxc.ui.cli.interpreter.elements;

import java.util.HashMap;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.util.CLIReader;
import hxc.ui.cli.util.CLIUtil;

public class SMSElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "sms" };
		description = "Sends an sms to the specified recipient.";
		function = true;
	}

	private HashMap<String, String> args;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	@Override
	public String regex()
	{
		return "^sms$|^sms\\s+.*";
	}

	@Override
	public boolean action(String action)
	{
		// Gets all the required arguments
		args = getAllArguments(action);

		// Sends the sms
		String response = connector.sendSMS(client.getUsername(), client.getSessionID(), args.get("-f"), args.get("-t"), args.get("special"));

		// Print the response
		CLIOutput.println(response != null ? response : "Could not send sms.");
		return true;
	}

	// Gets all the required arguments
	private HashMap<String, String> getAllArguments(String statement)
	{
		// Gets the arguments from the statement
		HashMap<String, String> args = CLIUtil.getArgumentSwitches(statement, "\"");

		// If the arguments does not contain the from
		if (!args.containsKey("-f"))
		{
			// Ask the user for the msisdn
			args.put("-f", CLIReader.readLine("Please enter in your MSISDN: "));
		}

		// If the arguments does not contain the to
		if (!args.containsKey("-t"))
		{
			// Ask the user for the recipient address
			args.put("-t", CLIReader.readLine("Please enter in the MSISDN you want to send to: "));
		}

		// If the arguments does not contain the message
		if (!args.containsKey("special"))
		{
			// Ask the user for the message
			args.put("special", CLIReader.readLine("Please enter in your message: "));
		}

		// Check if the user used quotes
		if (args.get("special").length() > 0 && args.get("special").charAt(0) == '\"')
		{
			// Remove the quotes from the message
			args.put("special", args.get("special").substring(1, args.get("special").length() - 1));
		}

		// Return the map
		return args;
	}

}
