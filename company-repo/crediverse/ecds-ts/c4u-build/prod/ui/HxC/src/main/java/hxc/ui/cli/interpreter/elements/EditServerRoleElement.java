package hxc.ui.cli.interpreter.elements;

import java.util.regex.Pattern;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.interpreter.elements.parent.IElement;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.util.CLIReader;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;

public class EditServerRoleElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "edit role <serverRoleName>" };
		description = "Edit a specific server role.";
		function = true;
	}

	private ServerRole[] serverRole;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	@Override
	public String regex()
	{
		return "^edit\\srole$|^edit\\srole\\s.+";
	}

	@Override
	public boolean action(String action)
	{
		// Check the action is correct
		if (action.length() == "edit role".length())
		{
			// Else show the proper usage
			error = "Usage: edit role <serverRoleName>";
			return false;
		}
		
		// Get the server role name
		String serverRoleName = action.substring(action.lastIndexOf(' ') + 1);
		
		// Get the server roles
		serverRole = getServerRole();
		
		// Iterate through the server roles
		for (ServerRole role : serverRole)
		{
			// Check if the server role name matches
			if (role.getServerRoleName().equalsIgnoreCase(serverRoleName))
			{
				boolean change = false;
				
				// Reference the input
				String input = null;
				do
				{
					// Get the user input
					input = CLIReader.readLine("Enter the column name followed by an '=' and the desired value: ('cancel' or 'exit') ").toLowerCase();
					
					// Check if they want to attach a command
					if (Pattern.matches("^attachcommand\\s*=\\s*.+", input))
					{
						// Get the command
						String attachCommand = input.substring(input.indexOf('=') + 1).trim();
						
						// Set the change flag
						change = true;
						
						// Set the attach command
						role.setAttachCommand(attachCommand);
					}
					// Else check if it is the detach command 
					else if (Pattern.matches("^detachcommand\\s*=\\s*.+", input))
					{
						// Get the detach command
						String detachCommand = input.substring(input.indexOf('=') + 1).trim();
						
						// Set the change flag
						change = true;
						
						// Set the detach command
						role.setDetachCommand(detachCommand);
					}
					// Else check if it is the owner
					else if (Pattern.matches("^owner\\s*=\\s*.+", input))
					{
						// Get the owner
						String owner = input.substring(input.indexOf('=') + 1).trim();
						
						// Set the change flag
						change = true;
						
						// Set the owner
						role.setOwner(owner);
					}
					// Else check if it is the exclusive
					else if (Pattern.matches("^exclusive\\s*=\\s*.+", input))
					{
						// Get the exclusive string
						String exclusiveString = input.substring(input.indexOf('=') + 1).trim();
						
						// Convert the exclusive string to a boolean
						boolean exclusive = Pattern.matches("y|yes|true|1", exclusiveString);
						
						// Set the change flag
						change = true;
						
						// Set the exclusive role
						role.setExclusive(exclusive);
					}
					// Else check if it is anything or than cancel or exit
					else if (!input.matches("^cancel$|^exit$"))
					{
						CLIOutput.println("No column found with that name.");
					}
					
				// While the input is not equal to cancel or exit
				} while (input == null || !input.matches("^cancel$|^exit$"));
				
				// Update the server role
				if (change && updateServerRole(serverRole))
				{
					CLIOutput.println("Updated the server role successfully.");
					return true;
				}
				else
				{
					error = "Failed to update the server role.";
					return false;
				}
			}
		}
		
		// Else say you could not find the server role
		error = "Could not find that server role.";
		return false;
	}

	// Gets an array of server roles
	private ServerRole[] getServerRole()
	{
		// Gets the role element
		IElement e = Element.generateElement("roles");
		
		// Set the properties
		e.setClient(client);
		e.setConnector(connector);
		e.setSystem(system);
		
		// Get the server roles
		return ((ServerRoleElement) e).getServerRoles();
	}

	// Updates the server roles
	private boolean updateServerRole(ServerRole[] serverRole)
	{
		return connector.setServerRole(client.getUsername(), client.getSessionID(), serverRole);
	}

}
