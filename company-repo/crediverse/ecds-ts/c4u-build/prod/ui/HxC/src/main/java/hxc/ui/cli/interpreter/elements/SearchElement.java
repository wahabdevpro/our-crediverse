package hxc.ui.cli.interpreter.elements;

import hxc.ui.cli.interpreter.elements.parent.Element;

public class SearchElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "search" };
		description = "Searches items and configurations and displays the paths to them.";
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
		return "^search\\s+.+";
	}

	@Override
	public boolean action(String action)
	{
		// Get the keyword to search
		action = action.substring(action.indexOf("search") + "search".length());
		
		// Check if the all dir flag was set
		boolean allDir = (action.contains("-a"));
		if (allDir)
		{
			// Take out the all dir flag out
			action = action.substring(action.indexOf("-a") + "-a".length());
		}
		
		// Search the path system
		system.searchPathSystem(action.trim(), true, allDir);
		return true;
	}

}
