package hxc.ui.cli.interpreter.elements;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.interpreter.elements.parent.IElement;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;

public class EditServerInfoElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "edit server <serverHost>" };
		description = "Edit a specific server host and its peer.";
		function = true;
	}

	private ServerInfo[] serverInfo = null;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	@Override
	public String regex()
	{
		return "^edit\\sserver\\s.+";
	}

	@Override
	public boolean action(String action)
	{
		// Get the server host
		String serverHost = action.substring(action.lastIndexOf(' ') + 1);

		// Get the server info
		serverInfo = getServerInfo();

		// Iterate through the server infos
		for (ServerInfo info : serverInfo)
		{
			// Check if the server host equals server host
			if (info.getServerHost().equalsIgnoreCase(serverHost))
			{

				// TODO Complete?
				return true;
			}
		}
		return false;
	}

	// Get the server info
	private ServerInfo[] getServerInfo()
	{
		// Get the element
		IElement e = Element.generateElement("servers");

		// Set the properties
		e.setClient(client);
		e.setConnector(connector);
		e.setSystem(system);

		// Get the server info array
		return ((ServerInfoElement) e).getServerInfo();
	}

}
