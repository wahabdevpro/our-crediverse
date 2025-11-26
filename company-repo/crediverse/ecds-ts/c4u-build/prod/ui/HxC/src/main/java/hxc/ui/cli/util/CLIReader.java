package hxc.ui.cli.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.system.ICLISystem;

public class CLIReader
{
	
	// Properties
	private static BufferedReader reader;
	private static ICLISystem system;

	// Sets the system
	public static void setSystem(ICLISystem systems)
	{
		system = systems;
	}

	// Reads a line with a statement before the user input
	public static String readLine(String statement)
	{
		return readLine(statement, false);
	}

	// Reads a line with a statement before the user input with the option to print on a new line
	public static String readLine(String statement, boolean print)
	{
		// Ensure the reader is set
		if (reader == null)
		{
			// Else create a new buffered reader
			reader = new BufferedReader(new InputStreamReader(System.in));
		}

		try
		{
			// Print the statement
			CLIOutput.print(statement);
			
			// If print then print a new line
			if (print)
			{
				CLIOutput.println();
			}
			
			// Read the line
			return reader.readLine();
		}
		catch (IOException exc)
		{
			return null;
		}
	}

	// Reads the user input
	public static String readLine()
	{
		// Ensure the buffered reader is set
		if (reader == null)
		{
			// Else create a new buffered reader
			reader = new BufferedReader(new InputStreamReader(System.in));
		}
		
		// Gets pre text before the user input
		String preText = "";
		if (system != null)
		{
			// Trys to get the current path of the user
			preText = "hxc" + ((system.getCurrentPath().length() > 0) ? "." + system.getCurrentPath() + "> " : "> ");
		}
		else
		{
			// Else just print the default
			preText = "hxc> ";
		}
		
		try
		{
			// Print the pretext
			CLIOutput.print(preText);
			
			// Read the user input
			return reader.readLine();
		}
		catch (IOException exc)
		{
			return null;
		}
	}
}
