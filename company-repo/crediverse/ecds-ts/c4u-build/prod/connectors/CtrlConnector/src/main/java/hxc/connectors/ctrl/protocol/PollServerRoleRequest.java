package hxc.connectors.ctrl.protocol;

public class PollServerRoleRequest extends ServerRequest
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static final long serialVersionUID = -6790464752192335828L;
	private String serverRole;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public String getServerRole()
	{
		return serverRole;
	}

	public void setServerRole(String serverRole)
	{
		this.serverRole = serverRole;
	}

}
