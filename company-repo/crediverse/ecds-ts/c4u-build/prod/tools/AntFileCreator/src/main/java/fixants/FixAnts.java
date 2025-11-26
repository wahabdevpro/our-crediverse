/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fixants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import utils.FileUtils;

/**
 *
 * @author jceatwell
 */
public class FixAnts
{
	private String rootPath;
	private DependencyFinder depFinder;

	public FixAnts() throws Exception
	{
		init();
	}

	private void init() throws Exception
	{
		rootPath = FileUtils.findRoot();
		logConsole("rootPath = %s\n", rootPath);
		depFinder = new DependencyFinder(rootPath);
	}

	public void updateAntFiles(boolean useDebuggerSymbols) throws IOException, InterruptedException
	{
		for (String project : depFinder.getProjectLocations().keySet())
		{
			Path path = depFinder.getProjectLocations().get(project);
//			File propFile = new File(path.toString() + File.separator + ("jar.properties"));
			File buildFile = new File(path.toString() + File.separator + ("build.xml"));

			try
			{
				Properties jarProps = FileUtils.readPropertiesFile(path.toString() + File.separator + ("jar.properties"));
				logConsole("Updating %-120s", buildFile.getCanonicalPath());
				
				boolean createAntFile = true;
				try
				{
					createAntFile = !((jarProps != null) 
							&& (jarProps.containsKey(AntBuilder.IGNORE_FOLDER_DO_NOT_BUILD_ANT_FILE))
									&& Boolean.parseBoolean(jarProps.getProperty(AntBuilder.IGNORE_FOLDER_DO_NOT_BUILD_ANT_FILE)));
				} catch(Exception e)
				{
				}
				
				if (createAntFile)
				{
					AntBuilder ab = new AntBuilder(depFinder, project, jarProps);
					String antContent = ab.buildAntFileContent(useDebuggerSymbols);
					FileUtils.writeFile(buildFile, antContent);

					if (buildFile.exists())
						logConsole("SUCCESS" + (useDebuggerSymbols? " (DEBUG)%n" : "%n"));
					else
						logConsole("NOT FOUND!%n");					
				} 
				else 
				{
					logConsole("[INORED]%n");
				}

			}
			catch (Exception e)
			{
				System.err.printf("FAILED %s @ %s REASON: %s%n", project, path, e.getMessage());
			}
		}

	}

	private static void logConsole(String message, Object... args)
	{
		System.out.printf(message, args);
	}

    public static void printHelp() 
    {
        System.out.println("Usage: java -jar AntFileBuilder [OPTIONS]");
        System.out.println("[OPTIONS] Available:");
        System.out.println("\t-d, -debug\t\tCompile with debugging symbols (off by default)");
        System.out.println("\t-h, -help\t\tPrint Help information");
    }
    
	public static void main(String[] args) throws Exception
	{
        boolean useDebuggerSymbols = false;
        for(String arg : args) 
        {
            switch(arg)
            {
                case "-d":
                case "--d":
                case "-debug":
                case "--debug":
                    useDebuggerSymbols = true;
                    break;
                    
                case "?":
                case "-h":
                case "-help":
                case "--help":
                    printHelp();
                    System.exit(0);
                    break;
            }
        }
        
		FixAnts fa = new FixAnts();
		fa.updateAntFiles(useDebuggerSymbols);
	}

}
