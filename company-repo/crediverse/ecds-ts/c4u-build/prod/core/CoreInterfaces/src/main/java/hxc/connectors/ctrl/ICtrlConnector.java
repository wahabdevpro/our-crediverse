package hxc.connectors.ctrl;

import java.io.File;

import hxc.configuration.ValidationException;
import hxc.connectors.file.FileType;

public interface ICtrlConnector
{
	public static final String DATABASE_ROLE = "DatabaseServer";

	// Test if host is Incumbent custodian of the Server Role
	public abstract boolean isIncumbent(String serverRole);

	// Server
	public abstract IServerInfo[] getServerList();

	public abstract void setServerList(IServerInfo[] servers) throws ValidationException;

	// Server Role
	public abstract IServerRole[] getServerRoleList();

	public abstract void setServerRoleList(IServerRole[] serverRoles) throws ValidationException;

	public abstract void moveRole(String serverRole, String hostServer) throws ValidationException;

	// Config Change Notification
	public abstract void notifyConfigChange(long configSerialVersionUID);

	// File Distribution
	public abstract boolean distributeFile(String directory, String filename);

	public abstract boolean notifyFileProcessed(File file, String outputDir);

	// File Fail Over
	public abstract boolean checkFileServerRoleHasIncumbent(String inputDirectory, FileType fileType);

	public abstract String getThisTransactionNumberPrefix();

	public abstract void setMaxNodes(int maxNodes);
}
