package hxc.ui.cli.system;

import hxc.utils.protocol.uiconnector.common.Configurable;

public interface ICLISystem
{

	// Gets the current path in the system
	public abstract String getCurrentPath();

	// Displays the available paths to the user
	public abstract boolean displayPaths();

	// Goes back one path
	public abstract boolean back();

	// Searches the system
	public abstract void searchPathSystem(String search, boolean ignoreCase, boolean allDir);

	// Gets the configuration
	public abstract Configurable getConfiguration();

	// Changes the current path
	public abstract boolean updateCurrentPath(String dir);

	// Gets the executed configuration
	public abstract boolean getExecutedConfiguration(boolean ret);
	
	// Temporary for Extracting Information for CSV
	public String [] extractCSVData(String parmName) throws Exception; 
	
	public boolean importCSVData(String paramName, String fileName) throws Exception;
	
}
