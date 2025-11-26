package hxc.ui.cli.interpreter.elements;

import java.util.Arrays;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;

public class ServerInfoElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "servers", "serverInfo", "get servers" };
		description = "Displays the servers used in the system.";
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
		return "^servers$|^serverinfo$|^get\\sservers$";
	}

	@Override
	public boolean action(String action)
	{
		// Create the string builder
		StringBuilder sb = new StringBuilder();
		
		// Create the heading
		sb.append("\nServers\n");
		sb.append("=======\n");
		int maxServer = 10;
		
		// Iterate through the server infos
		for (ServerInfo info : getServerInfo())
		{
			// Get the max length for the server host
			if (info.getServerHost() != null && info.getServerHost().length() > maxServer)
			{
				maxServer = info.getServerHost().length();
			}
		}
		
		// Create the spaces for the server host
		char spaces[] = new char[maxServer];
		Arrays.fill(spaces, ' ');
		
		// Print out the server host and peer host heading
		sb.append("\nServerHost" + new String(spaces) + "PeerHost\n");
		Arrays.fill(spaces, '=');
		
		// Underling the headings
		sb.append("==========" + new String(spaces) + "==========\n");
		
		// Iterate through server infos
		for (ServerInfo info : getServerInfo())
		{
			// Print out the server host
			spaces = new char[("ServerHost".length() + maxServer - ((info.getServerHost() != null) ? info.getServerHost().length() : 4) - 1)];
			Arrays.fill(spaces, ' ');
			sb.append(info.getServerHost() + new String(spaces) + '|' + info.getPeerHost() + '\n');
		}
		
		// Print out the string builder
		CLIOutput.println(sb.toString());

		return true;
	}

	public ServerInfo[] getServerInfo()
	{
		// Gets the server info array
		return connector.getServerInfo(client.getUsername(), client.getSessionID());
	}

}
