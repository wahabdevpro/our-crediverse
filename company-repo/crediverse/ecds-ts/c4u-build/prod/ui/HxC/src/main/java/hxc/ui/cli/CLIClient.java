package hxc.ui.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CLIClient implements ICLIClient
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private static final String sessionFile = "hxc_session";

	private String username;
	private String sessionID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Methods
	//
	// /////////////////////////////////

	@Override
	public void setUsername(String username)
	{
		this.username = username;
	}

	@Override
	public String getUsername()
	{
		return this.username;
	}

	@Override
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}

	@Override
	public String getSessionID()
	{
		return this.sessionID;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	// Checks to see if the session exists
	@Override
	public boolean checkForSession()
	{
		// Create the reference to the fil
		File f = new File("/tmp/" + sessionFile);

		// Check if the file exists
		if (f.exists())
		{
			try (BufferedReader br = new BufferedReader(new FileReader(f)))
			{
				// Get the first line
				String su = br.readLine();

				// Set the session ID and the username
				sessionID = su.substring(0, su.indexOf("_"));
				username = su.substring(su.indexOf("_") + 1);
				
				return true;
			}
			catch (IOException e)
			{
				return false;
			}
		}
		
		// Else return false
		return false;
	}

}
