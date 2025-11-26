package hxc.ui.cli.interpreter.elements;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;

public class VersionElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "version" };
		description = "Displays the version.";
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
		return "^version$";
	}

	@Override
	public boolean action(String action)
	{
		// Get the version
		String version = connector.version(null, null);

		// Print out the version
		CLIOutput.println((version != null) ? version : "Cannot get the version.");
		return (version != null);
	}

}
