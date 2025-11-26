package hxc.ui.cli.interpreter.elements;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.interpreter.elements.parent.IElement;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.util.CLIReader;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;

public class AddServerInfoElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
		//
		// Initialising
		//
		// /////////////////////////////////
	
	{
		names = new String[] { "add server" };
		description = "Adds a specific server host and its peer.";
		function = true;
	}

	private String add_server = "hxc:resilience:add_server> ";

	// //////////////////////////////////////////////////////////////////////////////////////
		//
		// Implementation
		//
		// /////////////////////////////////
	
	@Override
	public String regex()
	{
		return "^add\\sserver";
	}

	@Override
	public boolean action(String action)
	{
		// Create the variables
		String serverHost = null, peerHost = null, transactionNumber = null;
		
		// Get the server host from the user input
		while (serverHost == null)
		{
			// Prompt the user to enter the server host
			serverHost = CLIReader.readLine(add_server + "Enter in Server Host: ");
		}
		
		// Check if the server host is equal to cancel to exit
		if (serverHost.equalsIgnoreCase("cancel"))
		{
			return false;
		}
		
		// Get the peer host from the user input
		while (peerHost == null)
		{
			// Prompt the user to enter the peer host
			peerHost = CLIReader.readLine(add_server + "Enter in Peer Host: ");
		}
		
		// Check if the peer host is equal to cancel
		if (peerHost.equalsIgnoreCase("cancel"))
		{
			return false;
		}
		
		// Get the transaction number from the user input
		while (transactionNumber == null)
		{
			// Prompt the user to enter the transaction number
			transactionNumber = CLIReader.readLine(add_server + "Enter in Transaction Number Prefix: ");
		}
		
		// Check if the transaction number is equal to cancel
		if (transactionNumber.equalsIgnoreCase("cancel"))
		{
			return false;
		}
		
		// Create the server info
		ServerInfo serverInfo = new ServerInfo(serverHost, peerHost, transactionNumber);
		
		// Add the server info
		return addServerInfo(serverInfo);
	}

	// Adds a server role to the backend
	public boolean addServerInfo(ServerInfo serverInfo)
	{
		// Get the server info
		ServerInfo[] servers = getServerInfo();
		
		// Create the array of server infos
		ServerInfo[] newServers = new ServerInfo[servers.length + 1];
		
		// Iterate through the new servers
		for (int i = 0; i < servers.length; i++)
		{
			newServers[i] = servers[i];
		}
		
		// Add the new server to the end of the array
		newServers[newServers.length - 1] = serverInfo;
		
		// Send the request to the backedn
		boolean answer = connector.setServerInfo(client.getUsername(), client.getSessionID(), newServers);
		
		// If it was successful then print a message
		if (answer)
		{
			CLIOutput.println("Record successfully added.");
		}
		
		// Return the result
		return answer;
	}

	// Gets the server information
	private ServerInfo[] getServerInfo()
	{
		// Get the element 
		IElement e = Element.generateElement("servers");
		
		// Set the properties
		e.setClient(client);
		e.setConnector(connector);
		e.setSystem(system);
		
		// Return the server info array
		return ((ServerInfoElement) e).getServerInfo();
	}

}
