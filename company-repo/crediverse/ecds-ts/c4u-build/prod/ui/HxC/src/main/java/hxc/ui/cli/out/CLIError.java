package hxc.ui.cli.out;

public class CLIError
{

	// Prints out an error message and returns false
	public static boolean raiseError(Object obj, String error, Throwable e)
	{
		CLIOutput.println(error);
		return false;
	}

	// Prints out an error message and returns false
	public static boolean raiseError(String error)
	{
		CLIOutput.println(error);
		return false;
	}

	// Prints out the possible method the error occurred
	public static boolean raiseError(String error, Throwable e)
	{
		// Raises the error
		raiseError(error);
		
		// Gets the stack trace
		if (e != null && e.getStackTrace() != null && e.getStackTrace().length > 0)
		{
			// Get the method name and line number
			StackTraceElement element = e.getStackTrace()[0];
			String filename = element.getFileName();
			String method = element.getMethodName();
			int line = element.getLineNumber();

			// Print out the location of the error
			CLIOutput.println("Error occurred with " + filename + " on method \"" + method + "\" on line " + line);
		}
		return false;
	}
}
