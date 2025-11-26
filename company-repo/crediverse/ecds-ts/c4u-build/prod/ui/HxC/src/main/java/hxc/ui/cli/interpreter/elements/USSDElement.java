package hxc.ui.cli.interpreter.elements;

import java.util.HashMap;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.util.CLIReader;
import hxc.ui.cli.util.CLIUtil;
import hxc.utils.protocol.hux.HandleUSSDResponse;
import hxc.utils.protocol.hux.HandleUSSDResponseMembers;

public class USSDElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "ussd" };
		description = "Sends a USSD.";
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
		return "^ussd$|^ussd\\s+.*";
	}

	@Override
	public boolean action(String action)
	{
		try
		{
			// Get all the arguments supplied by the action
			args = getAllArguments(action);

			// Ensure args is not null
			if (args == null)
			{
				return false;
			}

			// Get the session ID
			int sessionID = 1;
			try
			{
				// Get the session ID
				sessionID = Integer.parseInt(args.get("-s"));
			}
			catch (NumberFormatException exc)
			{
				sessionID = 1;
			}

			// Send the ussd request
			HandleUSSDResponse response = connector.sendUSSD(args.get("-f"), args.get("-t"), args.get("special"), sessionID, false, false);

			// Set the response default message
			String sResponse = "Not detecting any USSD commands on server. Change server address.";

			// Ensure the response is not null
			if (response != null)
			{
				// Set the response string
				sResponse = response.members.USSDResponseString;
			}

			// While the ussd has not exitted continue with the session
			while ((response != null) && (!response.members.action.equals(HandleUSSDResponseMembers.Actions.end)))
			{
				// Get the user input for the reply
				CLIOutput.println();
				String reply = CLIReader.readLine(sResponse, true);

				// Send the ussd
				response = connector.sendUSSD(args.get("-f"), args.get("-t"), reply, sessionID, true, true);

				// get the response
				sResponse = response.members.USSDResponseString;
			}

			// Check if response is not null and print it out
			if (sResponse != null)
				CLIOutput.println(sResponse);

			return true;
		}
		catch (Exception exc)
		{
			error = "Please use a session ID with ussd using the '-s' option.";
			return false;
		}
	}

	// Creates a hash map of the arguments
	private HashMap<String, String> getAllArguments(String statement)
	{
		// Get the arguments
		HashMap<String, String> args = CLIUtil.getArgumentSwitches(statement, "*");

		// Check if it does not contain the from
		if (!args.containsKey("-f"))
		{
			// Then ask the user for it
			args.put("-f", CLIReader.readLine("Please enter in MSISDN: "));
		}

		// Check if it does not contain the to
		if (!args.containsKey("-t"))
		{
			// Then ask the user for the recipient address
			args.put("-t", CLIReader.readLine("Please enter in Recipient Address: "));
		}

		// If it doesn't contain the session ID
		if (!args.containsKey("-s"))
		{
			// Put the default session id
			args.put("-s", "1");
		}

		// If it does not contain the ussd string
		if (!args.containsKey("special"))
		{
			// Ask the the user for the ussd string
			args.put("special", CLIReader.readLine("Please enter in USSD String: "));
		}

		// Return the map
		return args;
	}

}
