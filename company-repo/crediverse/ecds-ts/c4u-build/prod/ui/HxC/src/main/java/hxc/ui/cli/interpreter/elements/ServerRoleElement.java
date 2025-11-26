package hxc.ui.cli.interpreter.elements;

import java.util.Arrays;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;

public class ServerRoleElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "roles", "serverRole", "get roles" };
		description = "Displays the roles used in the system.";
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
		return "^roles$|^serverRoles$|^get\\sroles$";
	}

	@Override
	public boolean action(String action)
	{
		// Create the string builder
		StringBuilder sb = new StringBuilder();

		// Append the heading
		sb.append("\nRoles\n");
		sb.append("=====\n");

		// Set the max length for the server roles
		int maxServerName = 4, maxServerAttach = 4, maxServerDetach = 4, maxServerOwner = 4;

		// Iterate through the roles
		for (ServerRole role : getServerRoles())
		{
			// Check if the name length is greater than the current max length
			if (role.getServerRoleName() != null && role.getServerRoleName().length() > maxServerName)
			{
				maxServerName = role.getServerRoleName().length();
			}

			// Check if the attach command length is greater than the current max length
			if (role.getAttachCommand() != null && role.getAttachCommand().length() > maxServerAttach)
			{
				maxServerAttach = role.getAttachCommand().length();
			}

			// Check if the detach command length is greater than the current max length
			if (role.getDetachCommand() != null && role.getDetachCommand().length() > maxServerDetach)
			{
				maxServerDetach = role.getDetachCommand().length();
			}

			// Check if the owner length is greater than the current max length
			if (role.getOwner() != null && role.getOwner().length() > maxServerOwner)
			{
				maxServerOwner = role.getOwner().length();
			}
		}

		// Create the spaces to fill in for the server name
		char spaces[] = new char[maxServerName];
		Arrays.fill(spaces, ' ');

		// Create the heading for the server role
		sb.append("\nServerRole" + new String(spaces));

		// Create the spaces to fill in for the exclusivity
		spaces = new char["Exclusive".length() - 5];
		Arrays.fill(spaces, ' ');

		// Create the heading for the exclusive role
		sb.append("Exclusive" + new String(spaces));

		// Create the spaces to fill in for the attach command
		spaces = new char[maxServerAttach];
		Arrays.fill(spaces, ' ');

		// Create the heading for the attach command
		sb.append("AttachCommand" + new String(spaces));

		// Create the spaces to fill in for the detach command
		spaces = new char[maxServerDetach];
		Arrays.fill(spaces, ' ');

		// Create the heading for the detach command
		sb.append("DetachCommand" + new String(spaces));

		// Create the heading for the owner
		sb.append("Owner\n");

		// Create the underline
		spaces = new char[maxServerName + maxServerAttach + maxServerDetach + maxServerOwner + 50];
		Arrays.fill(spaces, '=');
		sb.append(new String(spaces) + '\n');

		// Iterate through the roles
		for (ServerRole role : getServerRoles())
		{
			// Print out the server role name
			spaces = new char[("ServerRole".length() - 1 + maxServerName - role.getServerRoleName().length())];
			Arrays.fill(spaces, ' ');
			sb.append(role.getServerRoleName() + new String(spaces) + '|');

			// Print out the exclusive
			spaces = new char["Exclusive".length()];
			Arrays.fill(spaces, ' ');
			sb.append(((role.isExclusive()) ? "YES" : "NO") + new String(spaces) + '|');

			// Print out the attach command
			spaces = new char[("AttachCommand".length() - 1 + maxServerAttach - ((role.getAttachCommand() != null) ? role.getAttachCommand().length() : 4))];
			Arrays.fill(spaces, ' ');
			sb.append(role.getAttachCommand() + new String(spaces) + '|');

			// Print out the detach command
			spaces = new char[("DetachCommand".length() - 1 + maxServerDetach - ((role.getDetachCommand() != null) ? role.getDetachCommand().length() : 4))];
			Arrays.fill(spaces, ' ');
			sb.append(role.getDetachCommand() + new String(spaces) + '|');

			// Print out the owner
			sb.append(role.getOwner() + '\n');
		}

		// Print out the roles
		CLIOutput.println(sb.toString());

		return true;
	}

	public ServerRole[] getServerRoles()
	{
		// Get the server roles
		return connector.getServerRoles(client.getUsername(), client.getSessionID());
	}

}
