package hxc.ui.cli.interpreter.elements;

import java.io.File;
import java.io.PrintWriter;

import hxc.ui.cli.interpreter.elements.parent.Element;
import hxc.ui.cli.out.CLIOutput;

public class ExportElement extends Element
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Initialising
	//
	// /////////////////////////////////

	{
		names = new String[] { "export", "export <parameter> <CSV file location>" };
		description = "Displays the fitness of the current server that the application is connected to.";
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
		return "^export$|^export\\s+.+";
	}

	public boolean action(String action)
	{
		String [] parms = action.split(" ");
		
		//Check Parameters
		if (parms.length < 2)
		{
			CLIOutput.println("Format: export <Parameter> [FileName]");
			CLIOutput.println("e.g. export Quotas Quotas.csv -> This will save Quotas to Quotas.csv");
			CLIOutput.println("Note that if filename is left out the csv file will be named after the parameter");
		}
		else
		{
			String parmName = parms[1];
			String fileName = (parms.length == 2)? parms[1] + ".csv" : parms[2];

			try
			{
				String []  content = system.extractCSVData(parmName);
				saveData(fileName, content);
				CLIOutput.println(String.format("Check file %s for CSV export", fileName));
			} catch (Exception e)
			{
				CLIOutput.println(String.format("Export did not go as planned, reason: %s", e.getMessage()));
		}

		}
		
		return true;
	}
	
	private void saveData(String fileName, String [] content) throws Exception
	{
		try(PrintWriter pw = new PrintWriter(new File(fileName)))
		{
			for (String line : content)
			{
				pw.write(line + "\n");
			}
		} 
		catch(Exception e) 
		{
			throw e;
		}
	}
	
}
