package hxc.ui.cli;

import java.util.HashMap;

import hxc.ui.cli.controller.CLIController;
import hxc.ui.cli.out.CLIError;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.util.CLIUtil;

public class HxC
{
	private static int UI_CONNECTOR_PORT = 10101;

	public static void main(String[] args)
	{
		try
		{
			// Set the output stream to the Console
			CLIOutput.setOutputStream(System.out);
			
			// Get the arguments supplied
			HashMap<String, String> arguments = CLIUtil.getArgumentSwitches(CLIUtil.convertArrayToString(args, " "), null);
			
			// Set the host
			String ip = arguments.containsKey("-h") ? arguments.get("-h") : "localhost";
			
			// Create the controller
			CLIController controller = new CLIController(ip, UI_CONNECTOR_PORT);
			
			// Execute the main process
			controller.executeTerminal(CLIUtil.convertArrayToString(args, " "));
		}
		catch (Exception e)
		{
			CLIError.raiseError("An error has occurred.", e);
		}
		finally
		{
			CLIOutput.println("Exiting...");
			System.exit(0);
		}
	}

}
