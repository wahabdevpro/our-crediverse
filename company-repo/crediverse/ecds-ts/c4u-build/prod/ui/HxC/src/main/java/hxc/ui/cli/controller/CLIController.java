package hxc.ui.cli.controller;

import java.util.HashMap;
import java.util.List;

import hxc.ui.cli.CLIClient;
import hxc.ui.cli.connector.CLIConnector;
import hxc.ui.cli.interpreter.CLIInterpreter;
import hxc.ui.cli.out.CLIError;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.system.CLISystem;
import hxc.ui.cli.util.CLIReader;
import hxc.ui.cli.util.CLIUtil;
import hxc.utils.protocol.uiconnector.common.Configurable;

public class CLIController
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private static CLIConnector connector;
	private static CLIClient client;
	private static CLISystem system;

	public static Thread thread;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////

	public CLIController(String host, int port)
	{
		// Create the connector
		connector = new CLIConnector();

		// Connect to the server
		if (!connector.connectUIClient(host, port))
		{
			// If not, then exit method
			return;
		}
		
		// Create client
		client = new CLIClient();
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	// Requests the user his/her user name
	public boolean requestUsername(CLIClient client)
	{
		try
		{
			// Sets the username based on the input of the person
			client.setUsername(CLIReader.readLine("Enter in username: "));
		}
		catch (Exception e)
		{
			return CLIError.raiseError(this, "Error while entering username.", e);
		}

		// Ensures the username is not equals
		if (client.getUsername().equals("exit"))
		{
			return false;
		}

		return true;
	}

	// Creates a session for the user
	public boolean login(String user, boolean isFile)
	{
		// Assign the username
		String username = user;
		String password = null;
		int numEntries = 0;
		
		// Check if there is a session already
		if (client.checkForSession())
		{
			// Ensure the configurables are retrievable
			if (connector.getAllConfigurables(client.getUsername(), client.getSessionID()) != null)
			{
				// Check if the username is the same as the previous session
				if (client.getUsername().equals(username))
				{
					return true;
				}
				
				// Get the client username otherwise
				username = client.getUsername();
				
				// Request the username 3 times
				while (numEntries < 3 && requestUsername(client))
				{
					// Check if the username equals the session username
					if (username.equals(client.getUsername()))
					{
						return true;
					}
					
					// Increment the tries
					numEntries++;
					
					// If it is 3, then show error message
					if (numEntries == 3)
					{
						CLIOutput.println("Too many incorrect entries, please log in again.");
					}
					
					// Else show incorrect username
					else
					{
						CLIOutput.println("Wrong user name entered. Please try again.");
					}
				}
				
				// Ask for details
				CLIOutput.println("\nPlease enter in your details.");
			}
			else
			{
				// Else the session has expired
				CLIOutput.println("Session has expired, please login again.");
			}
			
			// Remove the session key
			connector.removeSessionKey();
		}
		
		// Get the username entered
		username = user;
		numEntries = 0;
		
		// Ensure it is valid
		while ((username == null || !username.equals("exit")) && (numEntries < 3))
		{
			// Get the username
			if (username == null && !requestUsername(client))
			{
				return false;
			}
			
			// Ensure the username is not null
			if (username == null)
			{
				username = client.getUsername();
			}
			
			// Get the public key
			byte[] publicKey = connector.retrievePublicKey(username);
			
			// Ensure the public key is valid
			if (publicKey != null)
			{
				// If isFile flag is set
				if (!isFile)
				{ 
					// TEMP FILE CAUSE EXPOSES SECURITY
					password = String.valueOf(System.console().readPassword("Enter in password: "));
				}
				// Else request user to enter password
				else
				{
					try
					{
						// Enter the password in
						password = CLIReader.readLine("Enter in password: ");
					}
					catch (Exception e)
					{
						System.out.println("Error has occurred with entering password via file.");
					}
				}
				
				// Create a session if the credentials are correct
				String sessionID = null;
				if ((sessionID = connector.checkUserDetails(username, password, publicKey)) != null)
				{
					// Set the username and session id
					client.setUsername(username);
					client.setSessionID(sessionID);
					return true;
				}
			}
			
			// Else set the values to null
			username = null;
			password = null;
			
			// Increment the tries
			numEntries++;
			
			// If incorrect 3 times then exit
			if (numEntries == 3)
			{
				CLIOutput.println("Too many incorrect entries, exiting...");
				System.exit(0);
			}
			
			// Else ask for username and password again
			CLIOutput.println("Username or password was incorrect. Please try again.");
		}

		return false;
	}

	// Loads the system up
	public boolean loadSystem()
	{
		// Gets the configurations
		List<Configurable> configurations = connector.getAllConfigurables(client.getUsername(), client.getSessionID());
		
		// Ensure the configurations are valid
		if (configurations != null)
		{
			// Create the system
			system = new CLISystem(configurations, connector);
			
			// Set the system to the reader
			CLIReader.setSystem(system);
			
			// Set the client for the system
			system.setClient(client);
			
			// Check whether the system is not null
			return (system != null) ? true : false;
		}

		return false;
	}

	// Executes the interpreter
	public boolean executeTerminal(String args)
	{
		// Get the arguments from the java process
		HashMap<String, String> arguments = CLIUtil.getArgumentSwitches(args, null);
		
		// Check if it is an enquiry for the version number
		if (arguments.get("-v") != null || arguments.get("--version") != null)
		{
			CLIOutput.println("Current version: " + connector.version(null, null));
			return true;
		}
		
		// Get the username
		String user = arguments.get("-u");
		
		// Check if it is a file
		boolean isFile = (arguments.containsKey("-f")); // ///////TEMP
		
		// Get the rest of arguments
		args = arguments.get("rest");

		// Try log the user in and load the system
		if (!(login(user, isFile) && loadSystem()))
		{
			CLIError.raiseError("The path system could not be loaded or your login failed.");
			// return false;
		}
		
		// Load the components into the interpreter
		CLIInterpreter.loadComponents(connector, system, client);
		
		// Present the logged in user
		CLIOutput.println("Logged in as: " + client.getUsername());
		
		// If there are no more arguments then display the greeting
		if (args == null)
		{
			printGreeting();
		}

		// Interpret the user input from there on
		CLIInterpreter.interpret(args);
		thread = null;
		return true;
	}

	public void printGreeting()
	{
		CLIOutput.println("Welcome to Credit4U Services");
		CLIOutput.println("Your session id is " + client.getSessionID());
		CLIOutput.println();
		CLIOutput.println("Copyright (c) 2014, Concurrent and/or its affiliates. All rights reserved.");
		CLIOutput.println();
		CLIOutput.println("Type in \"help\" or \"?\" for any help.");
	}
}
