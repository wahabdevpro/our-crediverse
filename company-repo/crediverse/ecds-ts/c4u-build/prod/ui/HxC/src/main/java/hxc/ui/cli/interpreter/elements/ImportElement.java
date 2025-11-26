package hxc.ui.cli.interpreter.elements;

import java.io.File;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;

public class ImportElement  extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "import", "import <element> <CSV file location>" };
		description = "Imports parameters into system from CSV (Note that parameters are currently replaced!)";
		function = true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	@Override
	public String regex()
	{
		return "^import$|^import\\s+.+";
	}

	public boolean action(String action)
	{
		String [] parms = action.split(" ");
		
		//Check Parameters
		if (parms.length < 3)
		{
			CLIOutput.println("Format: import <Parameter> <FileName>");
		}
		else
		{
			String parmName = parms[1];
			String fileName = (parms.length == 2)? parms[1] + ".csv" : parms[2];
			
			File file = new File(fileName);
			if (!file.exists())
				CLIOutput.println( String.format("Could not find file: %s", fileName) );

			try
			{
				boolean success = system.importCSVData(parmName, fileName);
				if (success)
					CLIOutput.println( String.format("Import of '%s' successful", parmName) );
				else
					CLIOutput.println( String.format("There was a priblem importing '%s'", parmName) );
			} 
			catch (Exception e)
			{
				CLIOutput.println(String.format("Import did not go as planned, reason: %s", e.getMessage()));
			}

		}
		
		return true;
	}
	
}
