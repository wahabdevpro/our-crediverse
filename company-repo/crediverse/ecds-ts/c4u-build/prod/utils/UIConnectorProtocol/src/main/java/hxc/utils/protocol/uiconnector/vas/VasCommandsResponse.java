package hxc.utils.protocol.uiconnector.vas;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class VasCommandsResponse extends UiBaseResponse
{

	private static final long serialVersionUID = 3974049105568945507L;
	private String[] commandVariables;

	public VasCommandsResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	public String[] getCommandVariables()
	{
		return commandVariables;
	}

	public void setCommandVariables(String[] commandVariables)
	{
		this.commandVariables = commandVariables;
	}

}
