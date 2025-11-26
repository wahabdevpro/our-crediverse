package hxc.connectors.ctrl;

import java.io.Serializable;

public interface IServerRole extends Serializable
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public abstract String getServerRoleName();

	public abstract boolean isExclusive();

	public abstract String getOwner();

	public abstract String getAttachCommand();

	public abstract String getDetachCommand();

}