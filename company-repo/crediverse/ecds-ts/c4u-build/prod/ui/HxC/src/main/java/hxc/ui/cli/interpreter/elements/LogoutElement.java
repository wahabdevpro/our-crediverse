package hxc.ui.cli.interpreter.elements;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;

public class LogoutElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "logout" };
		description = "Closes the current session and exits the application.";
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
		return "^logout$";
	}

	@Override
	public boolean action(String action)
	{
		// Remove the session key and log out
		CLIOutput.println("Logging Out...");
		return !connector.removeSessionKey();
	}

}
