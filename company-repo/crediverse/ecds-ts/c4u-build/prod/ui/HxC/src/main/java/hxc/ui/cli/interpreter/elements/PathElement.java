package hxc.ui.cli.interpreter.elements;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;

public class PathElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	private boolean help;
	private String previousPath;
	private boolean reset;

	@Override
	public String regex()
	{
		reset = false;
		help = false;
		return "^.+";
	}

	@Override
	public boolean action(String action)
	{
		// If it is not reset
		if (!reset)
		{
			// Set the reset
			reset = true;
			
			// Get the current path
			previousPath = system.getCurrentPath();
		}
		
		// Check if there is help
		if (action.indexOf("help") == 0)
		{
			// Set the help flag
			help = true;
			
			// Get the action after the help
			action = action.substring(action.indexOf("help") + "help".length()).trim();
		}
		
		// Iterate through the paths
		while (action.contains(".") && ((action.indexOf('=') > 0) ? action.indexOf('=') > action.indexOf('.') : true))
		{
			// Execute the action method
			action(action.substring(0, action.indexOf('.')));
			
			// Adjust the action
			action = action.substring(action.indexOf('.') + 1);
		}
		
		// Get the configuration
		if (system.getConfiguration() != null)
		{
			// Get help for the configuration
			if (system.updateCurrentPath((help) ? "help " + action : action))
			{
				// Execute the configuration if need to
				if (!system.getExecutedConfiguration((previousPath.length() == 0)))
				{
					// Display the current path
					CLIOutput.println("No config found.");
					system.displayPaths();
				}
			}
			
			return true;
		}
		
		// Else just update the current path
		return system.updateCurrentPath(action);
	}

}
