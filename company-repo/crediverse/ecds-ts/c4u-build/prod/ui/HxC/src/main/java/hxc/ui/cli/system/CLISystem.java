package hxc.ui.cli.system;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import au.com.bytecode.opencsv.CSVReader;
import hxc.services.notification.IPhrase;
import hxc.services.notification.Phrase;
import hxc.services.notification.ReturnCodeTexts;
import hxc.ui.cli.CLIClient;
import hxc.ui.cli.connector.CLIConnector;
import hxc.ui.cli.out.CLIError;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.util.ConfigurableParameterHelper;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.response.ConfigurableMethod;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;

public class CLISystem implements ICLISystem
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private String currentPath;
	private TreeMap<String, Configurable> innerPathSystem;
	private TreeMap<String, TreeMap<String, Configurable>> pathSystem;
	private Iterator<String> paths;

	private List<Configurable> configurations;
	private Configurable configuration;
	private int numConfigs;

	private boolean executedConfiguration = false;

	private CLIConnector connector;
	private CLIClient client;

	public static final String separator = ".";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public CLISystem(List<Configurable> configurations, CLIConnector connector)
	{
		setConfigurations(configurations);
		loadPathSystem(false);
		this.connector = connector;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	// Sets the configurations
	public void setConfigurations(List<Configurable> configurations)
	{
		this.configurations = configurations;
	}

	// Gets the configurations
	public List<Configurable> getConfigurations()
	{
		return this.configurations;
	}

	// Checks if the current path is in the inner system
	public boolean isInnerSystemNull()
	{
		return (innerPathSystem == null) ? true : false;
	}

	// Sets the client
	public void setClient(CLIClient client)
	{
		this.client = client;
	}

	// Loads a path system
	public boolean loadPathSystem(boolean update)
	{
		// Ensures that configurations contains all the configurables
		if (configurations == null)
		{
			return false;
		}

		// Check if in inner system
		boolean innerNull = isInnerSystemNull();

		// If it is not an update
		if (!update)
		{
			// Create the path system
			pathSystem = new TreeMap<String, TreeMap<String, Configurable>>();
		}

		// Get iterator to iterate through the configurables
		Iterator<Configurable> configs = configurations.iterator();
		
		// Iterate
		while (configs.hasNext())
		{
			// Get the config
			Configurable config = configs.next();
			
			// Check if path already exists
			if (pathSystem.containsKey(config.getPath().toLowerCase()))
			{
				// Use existing inner path
				innerPathSystem = pathSystem.get(config.getPath().toLowerCase());
			}
			else
			{
				// Create inner path
				innerPathSystem = new TreeMap<String, Configurable>();
			}
			
			// Put the config into the hashmaps
			innerPathSystem.put(config.getName().toLowerCase().replace(separator.charAt(0), '/'), config);
			pathSystem.put(config.getPath().toLowerCase().replace(separator.charAt(0), '/'), innerPathSystem);
		}

		// If it is not an update
		if (!update)
		{
			// Initialise necessary variables
			currentPath = "";
			paths = pathSystem.keySet().iterator();
			innerPathSystem = null;
		}

		// If it is in the inner path
		if (innerNull)
		{
			// Set it to null
			innerPathSystem = null;
		}

		return true;
	}

	// Gets the configurations
	public boolean updateConfigs()
	{
		// Gets all the configurables
		List<Configurable> configs = connector.getAllConfigurables(client.getUsername(), client.getSessionID());
		
		// If the configs are null
		if (configs == null)
		{
			// Exit
			System.out.println("Cannot connect to the server at this time, please try again later.");
			System.exit(0);
		}

		return true;
	}

	// Update the current path
	@Override
	public boolean updateCurrentPath(String dir)
	{
		// Temporarily replace the . with a '~'
		dir = dir.replace('.', '~');
		
		// Reference the current path
		String prev = currentPath;
		
		// Initialise variables
		boolean gotPath = false;
		
		// Get the path number
		int pathsNum = 0;
		
		try
		{
			// Check if it is a number
			if (configuration == null)
			{
				// Try parse the dir
				pathsNum = Integer.parseInt(dir);
			}
		}
		catch (NumberFormatException e)
		{
			// Else make the number 0
			pathsNum = 0;
		}

		// Check if it is a vaid number
		if (pathsNum > 0)
		{
			// Reference the position
			int pos = 1;
			
			// Ensure that paths exist, otherwise initialise it
			if (paths == null)
			{
				// Get the path
				paths = pathSystem.keySet().iterator();
				
				// Set the current path
				currentPath = "";
			}
			
			// Go through the different subdirectories
			while (paths.hasNext())
			{
				// Get the next path name
				String pathName = paths.next();
				
				// Increase the position
				if (pos++ == pathsNum)
				{
					// Found path
					gotPath = true;
					
					// If the inner path has been set
					if (innerPathSystem != null)
					{
						// Append the separator
						currentPath += separator;
					}
					
					// Append the path name to the current path
					currentPath += pathName;
					
					break;
				}
			}
		}
		else
		{
			// If the current path has characters
			if (currentPath.length() > 0)
			{
				// Add the dir to the current path
				currentPath += separator + dir;
			}
			else
			{
				// Else set the current path to the dir
				currentPath = dir;
			}
		}
		
		// Replace the '~' to '.'
		dir = dir.replace('~', '.');
		
		String rootPath = null;
		
		// Check for a root directory, anything before the '.'
		if (currentPath.indexOf(separator) > 0)
		{
			// Extract the different paths
			rootPath = currentPath.substring(0, currentPath.lastIndexOf(separator) + 1);
			currentPath = currentPath.substring(currentPath.lastIndexOf(separator) + 1);
		}
		
		
		// Convert to lowercase
		currentPath = currentPath.toLowerCase();
		
		// Check the configurations
		updateConfigs();
		
		// Check if configurations are found
		if (configuration != null)
		{
			// Set the current path to null
			currentPath = "";
			
			// Ensure the root path is not null
			if (rootPath != null)
			{
				rootPath = rootPath.substring(0, rootPath.length() - 1);
			}

			// Ensure the client exists
			if (client == null)
			{
				return false;
			}
			
			// Determine the action, get or set or help
			executedConfiguration = determineConfigurationAction(rootPath, dir, client);
			
			gotPath = true;
		}
		// Else check if in the inner path
		else if (innerPathSystem != null) // Check if the current path is in the inner path system
		{
			// Check if the path exists in the inner path
			if (innerPathSystem.containsKey(currentPath))
			{
				// Get the configuration
				configuration = innerPathSystem.get(currentPath);
				numConfigs = 1;

				gotPath = true;
			}
		}
		else if (pathSystem.containsKey(currentPath)) // Check if the path exists in the outer path system
		{
			// Get the inner path
			innerPathSystem = pathSystem.get(currentPath);
			paths = innerPathSystem.keySet().iterator();
			gotPath = true;
		}
		else
		{
			// Initialise the paths to the outer path
			paths = pathSystem.keySet().iterator();
			innerPathSystem = null;
			currentPath = "";
		}

		// Combine the root and current paths
		if (rootPath != null)
		{
			currentPath = rootPath + currentPath;
		}

		// If the path was not found
		if (!gotPath)
		{
			if (currentPath.indexOf(separator) > 0)
			{
				currentPath = currentPath.substring(0, currentPath.lastIndexOf(separator));
			}
			else
			{
				currentPath = prev;
			}

			return false;
		}
		return true;
	}

	// Displays the paths to the user
	@Override
	public boolean displayPaths()
	{
		int pathsNum = 1;
		
		// Checks if it must show the configurations
		if (configuration != null)
		{
			// Ensure the configurable is not null
			if (configuration.getConfigurable() != null)
			{
				// Iterate through the configurables
				for (Configurable con : configuration.getConfigurable())
				{
					// Print out the configuration names
					CLIOutput.println(pathsNum++ + ". " + con.getName());
				}
			}
			
			// Iterates through the configuration params
			for (IConfigurableParam con : configuration.getParams())
			{
				// Check if it is read only
				String readonly = "";
				if (((ConfigurableResponseParam) con != null) && ((ConfigurableResponseParam) con).isReadOnly())
				{
					readonly = " [readonly]";
				}
				
				// Prints out the configurations
				CLIOutput.println(pathsNum++ + ". " + con.getFieldName() + " = " + con.getValue().toString().trim() + readonly);
			}
			
			// Check if there are no configuration methods
			if (configuration.getMethods() == null)
			{
				return true;
			}
			
			// Iterate through the methods
			for (ConfigurableMethod con : configuration.getMethods())
			{
				// Print out the method names
				CLIOutput.println(pathsNum++ + ". " + con.getMethodName());
			}
			
			// Return out of the method
			return true;
		}
		
		// Re-initialise the paths variable to the necessary path system
		if (innerPathSystem != null)
		{
			paths = innerPathSystem.keySet().iterator();
		}
		else
		{
			paths = pathSystem.keySet().iterator();
		}

		// Iterate through the paths
		while (paths.hasNext())
		{
			// Print out the paths
			CLIOutput.println(pathsNum++ + ". " + paths.next());
		}

		// Re-initialise the paths variable to the necessary path system
		if (innerPathSystem != null)
		{
			paths = innerPathSystem.keySet().iterator();
		}
		else
		{
			paths = pathSystem.keySet().iterator();
		}

		return true;
	}

	// Updates the configuration
	public void updateConfiguration(String root)
	{
		// Check if inside the first configuration
		if (numConfigs == 1)
		{
			// Set the path system
			innerPathSystem = pathSystem.get(root.substring(0, root.indexOf(separator)));
			
			// Get the configuration
			configuration = innerPathSystem.get(root.substring(root.lastIndexOf(separator) + separator.length()));
		}
		// Else check what configuration level you're in
		else
		{
			// First start at the first configuration
			numConfigs = 1;
			
			// Get the inner system
			innerPathSystem = pathSystem.get(root.substring(0, root.indexOf(separator)));
			
			// Adjust the root string
			root = root.substring(root.indexOf(separator) + separator.length());
			
			// Get the configuration
			configuration = innerPathSystem.get(root.substring(0, root.indexOf(separator)));
			
			// Adjust the root path
			root = root.substring(root.indexOf(separator) + separator.length());
			
			// While there are more paths
			while (root.contains(separator))
			{
				// Update the current path
				updateCurrentPath(root.substring(0, root.indexOf(separator)));
				
				// Adjust the root path
				root = root.substring(root.indexOf(separator) + separator.length());
				
				// Incremenet the number of configurables
				numConfigs++;
			}
			
			// Update the current path
			updateCurrentPath(root);
			
		}
	}

	// Loads the array list with the results of configurations
	public void getConfigurationList(ArrayList<String> result, String path, String innerPath, String query)
	{
		// Ensure results is not null
		if (result != null)
		{
			
			// Get the configuration parameters
			IConfigurableParam[] configs;
			
			// Ensure the path is not null
			if (path == null)
			{
				// Get the configs from the inner system
				configs = innerPathSystem.get(innerPath).getParams();
			}
			else
			{
				// Get the configs from the path system
				configs = pathSystem.get(path).get(innerPath).getParams();
			}

			// Iterate through the config parameters
			for (IConfigurableParam configNames : configs)
			{
				// Get the field name
				String fieldName = configNames.getFieldName();
				
				// Make the field name lowercase
				fieldName = fieldName.toLowerCase();
				
				// Compare the query to the field name
				if (query == null || fieldName.contains(query))
				{
					// Add the full path to the results
					result.add(path + separator + innerPath + "->" + configNames.getFieldName());
				}
			}
			
			// Get the configurable
			Configurable con;
			if (path == null)
			{
				con = innerPathSystem.get(innerPath);
			}
			else
			{
				con = pathSystem.get(path).get(innerPath);
			}
			
			// Check if there are any methods
			if (con.getMethods() == null)
			{
				return;
			}
			
			// Iterate through the methods
			for (ConfigurableMethod configMethods : con.getMethods())
			{
				// Get the method names
				String methodNames = configMethods.getMethodName();
				
				// Lowercase the method name
				methodNames = methodNames.toLowerCase();

				// Compare against the query
				if (query == null || methodNames.contains(query))
				{
					// Add the full path to the results
					result.add(path + separator + innerPath + "->" + configMethods.getMethodName());
				}
			}
		}
	}

	// Determines whether it must get or set the configurables
	public boolean determineConfigurationAction(String root, String action, CLIClient client)
	{
		// Trim the action string
		action = action.trim();
		
		// Check if it is equal to the get command
		if (action.contains("="))
		{
			// Determine if the configurable is settable
			if (determineConfiguration(action.substring(0, action.lastIndexOf("=")).trim()) != null)
			{
				CLIError.raiseError("Cannot set a configurable.");
				return false;
			}
			
			// Get the parameter
			IConfigurableParam configParam = determineConfigParam(action.substring(0, action.lastIndexOf("=")).trim());
			
			// Check if it is valid
			if (configParam == null)
			{
				// Determine it is a method
				if ((determineConfigMethod(action.substring(0, action.lastIndexOf("=")).trim())) != null)
				{
					CLIError.raiseError("Can't set a method.");
				}
				
				return false;
			}
			
			// Check if there is a value
			if (!(action.lastIndexOf(" ") < action.length()))
			{
				return false;
			}
			
			// Set the value to the new value
			configParam.setValue(action.substring(action.lastIndexOf("=") + 1).trim());
			boolean update = true;
			
			// Update the configuration
			update = connector.updateConfiguration(getConfiguration(), configParam, client.getUsername(), client.getSessionID());
			
			// Get all the configurables again
			setConfigurations(connector.getAllConfigurables(client.getUsername(), client.getSessionID()));
			
			// Reload the system
			if (!loadPathSystem(true))
			{
				CLIOutput.println("Could not update configurables.");
			}
			
			// Update the configuration
			updateConfiguration(root);
			
			// If it failed to update
			if (!update)
			{
				CLIOutput.println("Failed to update configuration.");
				return false;
			}
			
			// Display the new configurable value
			determineConfigurationAction(root, action.substring(0, action.lastIndexOf("=")).trim(), client);
		}
		
		// Check if it needs help
		else if ((action.length() > 4 && action.trim().substring(0, 4).equals("help")) || (action.length() > 1 && action.trim().charAt(0) == '?')) // Check if it is equal to the set command
		{
			
			// Get the configuration
			Configurable con = null;
			if ((con = determineConfiguration(action.substring(4, action.length()).trim())) != null)
			{
				// Return the help
				return helpConfiguration(con);
			}
			
			// Get the parameter
			IConfigurableParam configParam = determineConfigParam(action.substring(4, action.length()).trim());
			
			// Check if it is valid
			if (configParam == null)
			{
				// Check if it is a method
				ConfigurableMethod configMethod = determineConfigMethod(action.substring(4, action.length()).trim());
				if (configMethod != null)
				{
					// Return the help of the method
					return helpConfigMethodDetails(configMethod);
				}
				
				return false;
			}
			
			// Else return the help of the parameter
			return helpConfigParamDetails(configParam);
		}
		// Else it is a get or execute action
		else
		{
			// Get the configuration
			Configurable con = determineConfiguration(action);
			
			// Ensure the configuration is valid
			if (con != null)
			{
				// Set the current configuration
				configuration = con;
				numConfigs++;
				if (root != null)
				{
					currentPath = separator + con.getName().toLowerCase();
				}
				
				return true;
			}
			
			// Get the parameter
			IConfigurableParam configParam = determineConfigParam(action);
			
			// Check if it is valid
			if (configParam == null)
			{
				// Determine if it is a method
				ConfigurableMethod configMethod = determineConfigMethod(action);
				if (configMethod != null)
				{
					// Execute the method
					return connector.executeConfigMethod(configMethod, getConfiguration(), client.getUsername(), client.getSessionID());
				}
				
				return false;
			}
			
			// Else print out the value of the parameter
			CLIOutput.println(configParam.getFieldName() + " has a value of " + configParam.getValue().toString());
		}

		return true;
	}

	public Configurable determineConfiguration(String con)
	{
		if (getConfiguration() == null || getConfiguration().getConfigurable() == null)
		{
			return null;
		}
		Configurable config = null;

		int configNum;
		try
		{
			// Check if user entered number
			configNum = Integer.parseInt(con);
		}
		catch (NumberFormatException e)
		{
			configNum = 0;
		}

		if (configNum > 0)
		{
			int pos = 1;
			for (Configurable conf : getConfiguration().getConfigurable())
			{
				if (pos++ == configNum)
				{
					config = conf;
					break;
				}
			}
		}
		else
		{
			for (Configurable conf : getConfiguration().getConfigurable())
			{
				if (con.equalsIgnoreCase(conf.getName()))
				{
					config = conf;
					break;
				}
			}
		}

		return config;
	}

	// Determines whether the user used the number or string for the parameters
	public IConfigurableParam determineConfigParam(String param)
	{
		if (getConfiguration() == null || getConfiguration().getParams() == null)
		{
			return null;
		}
		IConfigurableParam configParam = null;

		int paramNum;
		try
		{
			// Check if it is a number
			paramNum = Integer.parseInt(param);
		}
		catch (NumberFormatException e)
		{
			// If not, make it 0
			paramNum = 0;
		}

		// If it is a valid number
		if (paramNum > 0)
		{
			int pos = 1;
			if (getConfiguration().getConfigurable() != null)
			{
				pos = getConfiguration().getConfigurable().size() + 1;
			}
			// Iterate through it, the number of times specified
			for (IConfigurableParam params : getConfiguration().getParams())
			{
				if (pos++ == paramNum)
				{
					// Assign the configParam to the params
					configParam = params;
				}
			}
		}
		else
		// This means the user entered the name of the parameter
		{
			// Iterate through the parameters
			for (IConfigurableParam params : getConfiguration().getParams())
			{
				// Check for a match with the field names
				if (params.getFieldName().equalsIgnoreCase(param))
				{
					configParam = params;
				}
			}
		}

		// Return the IConfigurableParam
		return configParam;
	}

	public ConfigurableMethod determineConfigMethod(String method)
	{
		if (getConfiguration() == null || getConfiguration().getMethods() == null)
		{
			return null;
		}

		ConfigurableMethod configMethod = null;

		int methodNum;
		try
		{
			// Check if it is a number
			methodNum = Integer.parseInt(method);
		}
		catch (NumberFormatException e)
		{
			// If not, make it 0
			methodNum = 0;
		}

		// If it is a valid number
		if (methodNum > getConfiguration().getParams().length)
		{
			int pos = getConfiguration().getParams().length + 1;
			// Iterate through it, the number of times specified
			for (ConfigurableMethod methods : getConfiguration().getMethods())
			{
				if (pos++ == methodNum)
				{
					// Assign the configParam to the params
					configMethod = methods;
				}
			}
		}
		else
		// This means the user entered the name of the parameter
		{
			// Iterate through the parameters
			for (ConfigurableMethod methods : getConfiguration().getMethods())
			{
				// Check for a match with the field names
				if (methods.getMethodName().equalsIgnoreCase(method))
				{
					configMethod = methods;
				}
			}
		}

		// Return the IConfigurableParam
		return configMethod;
	}

	// Gets the configuration name and version
	public boolean helpConfiguration(Configurable con)
	{
		CLIOutput.println("Configurable " + con.getName());
		CLIOutput.println("Version: " + con.getVersion());
		return true;
	}

	// Gets details of the parameter
	public boolean helpConfigParamDetails(IConfigurableParam param)
	{
		// Gets the response param of the configuration
		ConfigurableResponseParam configResponseParam = (ConfigurableResponseParam) param;
		
		// Ensure it is not null
		if (configResponseParam != null)
		{
			// Print out the information of the parameter
			CLIOutput.println();
			CLIOutput.println(configResponseParam.getFieldName());
			CLIOutput.println("================================");
			CLIOutput.println("Description: " + configResponseParam.getDescription());
			CLIOutput.println("Value: " + configResponseParam.getValue());
			CLIOutput.println("Read only: " + (configResponseParam.isReadOnly() ? "Yes" : "No"));
			
			// Check if it is not read only
			if (!configResponseParam.isReadOnly())
			{
				CLIOutput.println("Max Value: " + configResponseParam.getMaxValue());
				CLIOutput.println("Min Value: " + configResponseParam.getMinValue());
				
				// Check if it has a max length
				if (configResponseParam.getMaxLength() > 0)
				{
					CLIOutput.println("Max Length: " + configResponseParam.getMaxLength());
				}
				
				// Return possible values
				CLIOutput.println("Possible Values: " + configResponseParam.getPossibleValues());
			}
			return true;

		}

		return false;
	}

	// Prints out information on methods
	public boolean helpConfigMethodDetails(ConfigurableMethod method)
	{
		CLIOutput.println(method.getMethodName());
		CLIOutput.println("Description: " + method.getDescription());
		return true;
	}

	// Searches through the system for particular keywords
	@Override
	public void searchPathSystem(String search, boolean ignoreCase, boolean allDir)
	{
		// Lower case the key if ignore was set
		String key = search;
		if (ignoreCase)
		{
			key = key.toLowerCase();
		}
		
		// Get a reference of the results
		ArrayList<String> results = new ArrayList<String>();
		
		// Check if it must start searching from the current directory
		if (allDir || innerPathSystem == null)
		{
			
			// Get the paths
			Iterator<String> temp = pathSystem.keySet().iterator();
			
			// Iterate through the paths
			while (temp.hasNext())
			{
				// Get the path
				String path = temp.next();
				
				// Set the temp path
				String tempPath = path;
				
				// If ignore case then lowercase it
				if (ignoreCase)
				{
					tempPath = path.toLowerCase();
				}
				
				// Check if the current path contains the keyword
				if (tempPath.contains(key))
				{
					// Add it to the results
					results.add(path);
				}
				
				// Iterate through the inner system
				Iterator<String> tempInner = pathSystem.get(path).keySet().iterator();
				
				while (tempInner.hasNext())
				{
					// Get the inner path
					String innerPath = tempInner.next();
					
					// Set the temp inner path
					String tempInnerPath = innerPath;
					
					// If ignore case then lowercase it
					if (ignoreCase)
					{
						tempInnerPath = innerPath.toLowerCase();
					}
					
					// Check if the inner path contains the keyword
					if (tempInnerPath.contains(key))
					{
						// Add it to the results
						results.add(path + separator + innerPath);
					}
					
					// Get the configuration list
					getConfigurationList(results, path, innerPath, key);
				}
			}
		}
		// Else check if not in a configuration
		else if (configuration == null)
		{
			// Get the inner path
			Iterator<String> tempInner = innerPathSystem.keySet().iterator();
			
			// Iterate through the inner paths
			while (tempInner.hasNext())
			{
				// Get the inner path
				String innerPath = tempInner.next();
				
				// Set the temp inner path
				String tempInnerPath = innerPath;
				
				// If ignore case then lowercase the temp
				if (ignoreCase)
				{
					tempInnerPath = innerPath.toLowerCase();
				}
				
				// If the inner path contains the keyword
				if (tempInnerPath.contains(key))
				{
					// Add it to the results
					results.add(innerPath);
				}

				// Get the configurations
				getConfigurationList(results, currentPath, innerPath, key);
			}
		}
		// Else it is inside a configuration
		else
		{
			// Get the results for the configuration
			getConfigurationList(results, currentPath.substring(0, currentPath.indexOf(separator)), currentPath.substring(currentPath.indexOf(separator) + 1), key);
		}

		// Iterate through the results and print the paths
		for (String path : results)
		{
			CLIOutput.println(path);
		}
	}

	// Resets the current path to the beginning
	private boolean setCurrentPathToRoot()
	{
		// Resets the variables
		currentPath = "";
		paths = pathSystem.keySet().iterator();
		innerPathSystem = null;
		configuration = null;
		return true;
	}

	// Sets the current path back one
	private boolean setCurrentPathBack()
	{
		// Go back one path
		currentPath = currentPath.substring(0, currentPath.lastIndexOf(separator));
		
		// Get paths from the inner path
		paths = innerPathSystem.keySet().iterator();
		
		// Set the configuration to null
		configuration = null;
		
		// Check if outside of config
		if (!(--numConfigs == 0))
		{
			// Update the current path
			String con = currentPath.substring(currentPath.lastIndexOf(separator) + 1);
			if (currentPath.lastIndexOf(separator) > 0)
			{
				currentPath = currentPath.substring(0, currentPath.lastIndexOf(separator));
			}
			updateCurrentPath(con);
			
			// Display the paths
			displayPaths();
		}
		return true;
	}

	// Goes back
	@Override
	public boolean back()
	{
		// Goes back if the current path contains other paths
		if (currentPath.contains("."))
		{
			return setCurrentPathBack();
		}
		
		// Goes back to the beginning
		return setCurrentPathToRoot();
	}

	// Gets the current path
	@Override
	public String getCurrentPath()
	{
		return this.currentPath;
	}

	// Gets the current configuration
	@Override
	public Configurable getConfiguration()
	{
		return this.configuration;
	}

	// Gets the executed configuration
	@Override
	public boolean getExecutedConfiguration(boolean ret)
	{
		// Checks if executed the configuration
		boolean temp = executedConfiguration;
		
		// If executed
		if (executedConfiguration)
		{
			// Set it to false
			executedConfiguration = false;
			
			// If return to the beginning
			if (ret)
			{
				// Set back to the beginning
				setCurrentPathToRoot();
			}
		}
		
		// Return the result
		return temp;
	}

	
// ----------------------------------------------------------------
 

	

	
	@SuppressWarnings("unchecked")
	@Override
	public String[] extractCSVData(String paramName) throws Exception
	{
		ConfigurableParameterHelper configHelper = new ConfigurableParameterHelper();
		
		List<String> result = new ArrayList<String>();
//		List<String> content = new ArrayList<>();
		
		if (configuration != null)
		{
			// Step 1: Extract Locale
			Set<String> langCodes = configHelper.extractLocale(connector, client.getUsername(), client.getSessionID());
			
			// Step 2: Extract Configuration
			Configurable config = connector.extractConfigurationContent(client.getUsername(), client.getSessionID(), configuration.getConfigSerialVersionUID());
			IConfigurableParam param = configHelper.extractFieldConfiguration(config, paramName);
			if (param != null)
			{
				ConfigurableResponseParam conParm = (ConfigurableResponseParam) param;
				List<Class<?>> classList = new ArrayList<>();
				// Extract the structure first
				if (conParm.getStructure() != null && (param.getValue() instanceof List<?>))
				{
					// Build Header
					StringBuilder header = new StringBuilder();
					for(ConfigurableResponseParam parm : conParm.getStructure())
					{
						Class<?> classType = null;
						
						try
						{
							// Will throw exception for primitives
							classType = Class.forName(parm.getValueType());	
						} catch(Exception e) {}
						classList.add(classType);
						
						if (header.length() > 0)
							header.append(",");
						
						if (classType != null && IPhrase.class.isAssignableFrom(classType))
						{
							header.append(configHelper.phraseHeader(langCodes, parm.getFieldName()));
						}
						else if (classType != null && ReturnCodeTexts.class.isAssignableFrom(classType))
						{
							header.append("returnCode,");
							header.append(configHelper.phraseHeader(langCodes, "phrase"));
						}
						else
							header.append( parm.getFieldName() );							
					}
					result.add(header.toString());
					
					//Build Content
					for (IConfigurableParam[] propGroup : (List<IConfigurableParam[]>) param.getValue())
					{
						StringBuilder csvLine = new StringBuilder();
						for(int i=0; i<propGroup.length; i++)
						{
							if (i > 0)
								csvLine.append(",");
							
							if (classList.get(i) != null && IPhrase.class.isAssignableFrom(classList.get(i)) )
							{
								IPhrase phrase = (IPhrase)propGroup[i].getValue();
								csvLine.append( configHelper.phraseContent(langCodes, phrase) );
							}
							else if (classList.get(i) != null && ReturnCodeTexts.class.isAssignableFrom(classList.get(i)) )
							{
								ReturnCodeTexts returnCodeTexts = (ReturnCodeTexts)propGroup[i].getValue();
								csvLine.append( returnCodeTexts.getReturnCode().toString() ).append(",");
								csvLine.append( configHelper.phraseContent(langCodes, returnCodeTexts.getPhrase()) );
							}
							else
								csvLine.append(propGroup[i].getValue());
						}
						result.add(csvLine.toString());
					}
				}
			}

		}
		
		return (result.toArray(new String[result.size()]));
	}
	
	/**
	 * Import CSV data into the system
	 */
	@Override
	public boolean importCSVData(String paramName, String fileName) throws Exception
	{
		ConfigurableParameterHelper configHelper = new ConfigurableParameterHelper();
		
		List<Pair<String,String>> headerNames = null;
		List<String []> content = new ArrayList<>();
		
		
		// Extract Locale
		Set<String> langCodes = configHelper.extractLocale(connector, client.getUsername(), client.getSessionID());

		if (configuration != null)
		{
			// Import Current Data (and structure -> config.getStructure())
			Configurable config = connector.extractConfigurationContent(client.getUsername(), client.getSessionID(), configuration.getConfigSerialVersionUID());
			IConfigurableParam param = configHelper.extractFieldConfiguration(config, paramName);
			if (param == null)
			{
				CLIOutput.println("Could not find field " + paramName);
				return false;
			}
			ConfigurableResponseParam conParm = (ConfigurableResponseParam) param;
			
			// Read in CSV File
		    try(CSVReader reader = new CSVReader(new FileReader(fileName)))
		    {
			    String [] nextLine;
			    while ((nextLine = reader.readNext()) != null) 
			    {
			    	if (headerNames == null)
		    		{
			    		headerNames = new ArrayList<>();
			    		for(String header : nextLine)
			    		{
			    			headerNames.add( configHelper.extractFieldAndLanguguage(header) );
			    		}
		    		}
			    	else
			    	{
			    		String [] data = new String[nextLine.length];
			    		System.arraycopy(nextLine, 0, data, 0, nextLine.length);
			    		content.add(data);
			    	}
			    }
		    }
		    
		    // Now create items to save
		    List<IConfigurableParam []> dataToSave = new ArrayList<>();
		    
		    // Iterate through csv content
		    for(int i=0; i<content.size(); i++)
		    {
		    	// Create set to save (and populate with default values 
//		    	BasicConfigurableParm[] fields = configHelper.createFieldSetToPopulate(conParm);
		    	IConfigurableParam[] fields = configHelper.createFieldSetToPopulate(conParm);
		    	
		    	// Populate fields for content row
		    	for (int headerIndex=0; headerIndex<headerNames.size(); headerIndex++)
		    	{
		    		int fieldIndex = configHelper.findFieldIndex(fields, headerNames.get(headerIndex).first);
		    		if (fieldIndex < 0)
		    		{
		    			CLIOutput.println( String.format("Could not find field named '%s' check that you have not renamed CSV header (IMPORT ABORTED)", headerNames.get(headerIndex).first) );
		    			return false;
		    		}
		    		
		    		// Value can be NULL and String values are quoted (including arrays) 
		    		String value = content.get(i)[headerIndex].equalsIgnoreCase("NULL")? null : content.get(i)[headerIndex];
		            if ((value != null) && (value.length() > 1) && (value.charAt(0) == '"' && value.charAt(value.length()-1) == '"'))
		                value = value.substring(1, value.length() - 1);
		    		
		    		if (headerNames.get(headerIndex).second != null) 
		    		{
		    			((Phrase)fields[fieldIndex].getValue()).set(headerNames.get(headerIndex).second, value);
		    		}
		    		else
		    		{
		    			configHelper.populateField(fields, fieldIndex, value);
		    		}
		    	}
		    	
		    	dataToSave.add(fields);
		    }
		    
		    connector.saveConfigurationStructure(client.getUsername(), client.getSessionID(), configuration.getConfigSerialVersionUID(), 
		    		config.getVersion(), paramName, dataToSave, true);
		    
		}

		return true;
	}
	
}
