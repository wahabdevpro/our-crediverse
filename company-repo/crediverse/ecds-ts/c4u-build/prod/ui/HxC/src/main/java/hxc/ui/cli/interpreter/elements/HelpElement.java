package hxc.ui.cli.interpreter.elements;

import java.util.Arrays;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;

public class HelpElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
		//
		// Initialising
		//
		// /////////////////////////////////
	
	{
		names = new String[] { "help", "?" };
		description = "Displays the various commands that can be used in the program. Displays this help.";
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
		return "^help$|^[?]$";
	}

	@Override
	public boolean action(String action)
	{
		// Display the help
		return displayHelp();
	}

	// Displays all the elements help information
	private boolean displayHelp()
	{
		// Prints out the header
		CLIOutput.println("\nHere is a list of all the Credit4U commands:");
		
		// Iterate through all the elements
		for (int i = 0; i < elements.length; i++)
		{
			// Check if the element is a function
			if (elements[i].function)
			{
				
				// Get the name of the element
				String name = elements[i].names[0];
				
				// Check if there are more than one name
				if (elements[i].names.length > 1)
				{
					// Add the names in brackets
					name += " (";
					
					// Iterate through names
					for (int j = 1; j < elements[i].names.length; j++)
					{
						// Append the name
						name += elements[i].names[j] + ",";
					}
					
					// Close the bracket in the name
					name = name.substring(0, name.lastIndexOf(',')) + ")";
				}
				
				// Print out the description
				char spaces[] = new char[50 - name.length()];
				Arrays.fill(spaces, ' ');
				CLIOutput.println(name + new String(spaces) + elements[i].description);
			}
		}
		
		// Return true
		return true;
	}

}
