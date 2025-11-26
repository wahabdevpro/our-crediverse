package hxc.ui.cli.interpreter.elements;

import java.util.Arrays;

import hxc.ui.cli.connector.CLIConnector;
import hxc.ui.cli.connector.ICLIConnector;
import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.util.CLIUtil;
import hxc.utils.protocol.uiconnector.ctrl.response.ComponentFitness;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;

public class FitnessElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "fitness", "fitness <host>" };
		description = "Displays the fitness of the current server that the application is connected to.";
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
		return "^fitness$|^fitness\\s+.+";
	}

	@Override
	public boolean action(String action)
	{
		// Create the variables
		ComponentFitness[] services = null;
		ComponentFitness[] connectors = null;
		ServerRole[] roles = null;

		// Check there is more than just fitness
		if (action.length() > "fitness".length())
		{
			// Create a new connector
			ICLIConnector con = new CLIConnector();

			// Get the host
			String host = action.substring(action.indexOf(' ') + 1).trim();

			// Check if there is a port number
			host = host + CLIUtil.hasPort(host, 10101);

			// Connects to the client
			if (!con.connectUIClient(host.substring(0, host.indexOf(':')), Integer.parseInt(host.substring(host.indexOf(':') + 1))))
			{
				return true;
			}

			// Get the fitness
			services = con.getServiceFitness(client.getUsername(), client.getSessionID());
			connectors = con.getConnectorFitness(client.getUsername(), client.getSessionID());

			// Get the server roles
			roles = con.getServerRoles(client.getUsername(), client.getSessionID());
		}
		else
		{
			// Get the fitness
			services = connector.getServiceFitness(client.getUsername(), client.getSessionID());
			connectors = connector.getConnectorFitness(client.getUsername(), client.getSessionID());

			// Get the server roles
			roles = connector.getServerRoles(client.getUsername(), client.getSessionID());
		}

		// Print out the fitness
		CLIOutput.println("Server Fitness:");
		int column = 30;

		// Print out the services
		CLIOutput.println("\tServices: " + services.length);

		// Iterate through the services
		for (ComponentFitness cf : services)
		{
			// Print out the component
			char spaces[] = new char[column - cf.getName().length() - 1];
			Arrays.fill(spaces, ' ');
			CLIOutput.println('\t' + cf.getName() + ":" + new String(spaces) + (cf.isFit() ? "FIT" : "UNFIT"));
		}

		// Print out the connectors
		CLIOutput.println();
		CLIOutput.println("\tConnectors: " + connectors.length);

		// Iterate through the connectors
		for (ComponentFitness cf : connectors)
		{
			// Print out the component
			char spaces[] = new char[column - cf.getName().length() - 1];
			Arrays.fill(spaces, ' ');
			CLIOutput.println('\t' + cf.getName() + ":" + new String(spaces) + (cf.isFit() ? "FIT" : "UNFIT"));
		}

		// Check if there are roles
		CLIOutput.println();
		if (roles == null)
		{
			error = "No Roles were found.";
			return false;
		}

		// Print out the server roles
		CLIOutput.println("Server Roles:");

		// Iterate through the server roles
		for (ServerRole role : roles)
		{
			// Print out the server role
			char spaces[] = new char[column - role.getServerRoleName().length() - 1];
			Arrays.fill(spaces, ' ');
			CLIOutput.println('\t' + role.getServerRoleName() + ":" + new String(spaces) + ((role.getOwner() != null) ? role.getOwner() : "NO OWNER"));
		}

		// Return true
		return true;
	}

}
