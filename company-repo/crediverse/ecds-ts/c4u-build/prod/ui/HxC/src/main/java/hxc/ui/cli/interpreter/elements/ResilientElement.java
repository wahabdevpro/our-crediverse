package hxc.ui.cli.interpreter.elements;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.interpreter.elements.parent.IElement;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.util.CLIReader;

public class ResilientElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "resilience" };
		description = "Enters the resilience shell.";
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
		return "^resilience$";
	}

	@Override
	public boolean action(String action)
	{
		// Reference the input
		String input = null;

		// Assign the elements to a temp array
		Element[] originalElements = elements;

		// Set the new elements
		setElements(new Element[] {
				// new AddServerInfoElement(),
				// new EditServerInfoElement(),
				new EditServerRoleElement(), new ExitElement(), new HelpElement(), new ServerInfoElement(), new ServerRoleElement(),
				// Must be the last one
				new FailOverElement() });

		// Execute a loop for the resilience
		do
		{
			// Ask user for input
			CLIOutput.println();
			input = CLIReader.readLine("hxc:resilience> ").trim();

			// Get the element
			IElement element = generateElement(input);

			// Ensure the element is valid
			if (element == null)
			{
				continue;
			}

			// Set the properties of the element
			element.setClient(client);
			element.setConnector(connector);
			element.setSystem(system);

			// Execute the action of the element
			if (!(element != null && element.action(input)))
			{
				// Check if the element exited
				if (element == null || element instanceof ExitElement)
				{
					break;
				}

				// Else print the error message
				CLIOutput.println(element.getError());
			}

			// Infinite loop until exit is called
		} while (true);

		// Set the elements back to the original
		setElements(originalElements);
		return true;
	}

}
