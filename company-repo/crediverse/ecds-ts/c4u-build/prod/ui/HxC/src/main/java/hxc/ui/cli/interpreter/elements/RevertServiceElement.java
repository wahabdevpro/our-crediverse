package hxc.ui.cli.interpreter.elements;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.util.CLIReader;

public class RevertServiceElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "revert <service>" };
		description = "Reverts the particular service back to its original settings. e.g. revert CrShr";
		function = true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	@Override
	public String regex()
	{
		return "^revert\\s+.+";
	}

	@Override
	public boolean action(String action)
	{
		// Ensure there is something after the revert string
		if (action.length() < "revert".length())
		{
			error = "Usage: revert <service>";
			return false;
		}

		// Get the service
		String service = action.substring(action.indexOf(' ') + 1);

		// Ask for confirmation
		boolean areSure = CLIReader.readLine("Are you sure you want to revert " + service + "? (Y/N) ").equalsIgnoreCase("Y");

		// If not sure then abort
		if (!areSure)
		{
			error = "Revert aborted.";
			return false;
		}

		// Else revert the service
		CLIOutput.println(connector.revertService(client.getUsername(), client.getSessionID(), service));
		return true;
	}

}
