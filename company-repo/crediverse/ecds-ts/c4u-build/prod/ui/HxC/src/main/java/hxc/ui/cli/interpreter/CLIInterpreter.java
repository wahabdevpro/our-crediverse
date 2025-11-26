package hxc.ui.cli.interpreter;

import hxc.ui.cli.CLIClient;
import hxc.ui.cli.connector.CLIConnector;
import hxc.ui.cli.connector.ICLIConnector;
import hxc.ui.cli.interpreter.elements.ExitElement;
import hxc.ui.cli.interpreter.elements.LogoutElement;
import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.interpreter.elements.parent.IElement;
import hxc.ui.cli.out.CLIError;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.system.CLISystem;
import hxc.ui.cli.system.ICLISystem;
import hxc.ui.cli.util.CLIReader;

public class CLIInterpreter
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private static ICLISystem system;
	private static ICLIConnector connector;
	private static CLIClient client;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	// Loads the components into the interpretter
	public static void loadComponents(CLIConnector connector, CLISystem system, CLIClient client)
	{
		CLIInterpreter.connector = connector;
		CLIInterpreter.system = system;
		CLIInterpreter.client = client;
	}

	// Interprets user input
	public static boolean interpret(String input)
	{
		// Gets the input
		String select = input;
		
		// Execute the loop
		do
		{
			try
			{
				// Ensure the input is not null
				if (input == null)
				{
					// Get the user input
					CLIOutput.println();
					select = CLIReader.readLine();
				}

				try
				{
					// Ensure it is a valid session
					if (!connector.validateSession(client.getUsername(), client.getSessionID()))
					{
						return true;
					}
				}
				catch (Exception exc)
				{
					return CLIError.raiseError("Session is invalid. Server may have restarted.");
				}

				// Trim the input
				if (select != null)
					select = select.trim();

				// Get the correct element to execute
				IElement element = Element.generateElement(select);
				
				// If the element was not found, display error
				if (element == null)
				{
					CLIError.raiseError("Element error. Could not find any element to match the criterion.");
					continue;
				}
				
				// Set the various components
				element.setConnector(connector);
				element.setSystem(system);
				element.setClient(client);
				
				// Execute the element action. If it fails then check the type of element
				if (!(element != null && element.action(select)))
				{
					// If it is an ExitElement or LogoutElement then exit from the interpreter
					if (element instanceof ExitElement || element instanceof LogoutElement)
					{
						return true;
					}
					
					// Else print the error message
					CLIOutput.println(element.getError());
				}
				
			}
			catch (Exception exc)
			{
				CLIError.raiseError("Unable to commence the last action. Either the server is down or an invalid response was received from the server. Please try again later.");
			}

		} while (select != null && input == null);
		
		return true;
	}

}
