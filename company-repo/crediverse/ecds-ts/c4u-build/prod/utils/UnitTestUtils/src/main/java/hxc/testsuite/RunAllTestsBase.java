package hxc.testsuite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import hxc.configuration.ValidationException;
import hxc.servicebus.IServiceBus;
import hxc.services.logging.LoggerService;
import hxc.services.logging.LoggerService.LoggerServiceConfig;
import hxc.services.logging.LoggingLevels;

public class RunAllTestsBase
{
	private static final Map<String, String> force_database =
		    Arrays.stream(new String[][] {
		        { "hxc", "hxctest" } 
		    }).collect(Collectors.toMap(kv -> kv[0], kv -> kv[1]));
	
	public static Map<String, String> getDatabaseConfigurationMap()
	{
		 return force_database;
	}
	
	public static String getDatabaseConfiguration(String dbname)
	{
		String name = null;
		if (force_database != null && force_database.containsKey(dbname))
			name = force_database.get(dbname);
		else 
		{
			System.err.println("Unable to locate database ||"+dbname+"||");
		}
		 return name;
	}

	private static final String EOL = String.format("%n");
	private static final byte ERROR_EXIT_CODE = 1;
	private static int HORIZONTAL_BAR_LENGTH = 100;
	
	// Configuration (To be overridden in Subclasses)
	protected Class<?> testClasses[] = {};
	protected String [] databasesToDrop = new String[] {getDatabaseConfiguration("hxc")};
	//protected String [] databasesToDrop = { "hxc" };
	
	
	
	// Specific to base class operations 
	private boolean failed = false;
	private Set<String> testCasesToSkip = null;
	private PrintWriter writer = null;
	
	private String classPath = null;
	private String javaExec = "java";
	
	private void setup()
	{
		URL[] urls = ((URLClassLoader) (Thread.currentThread().getContextClassLoader())).getURLs();
		StringBuilder sb = new StringBuilder();
		
		log("Currently Loaded Classes");
		for(int i=0; i<urls.length; i++)
		{
			if (i>0) sb.append(":");
			sb.append(urls[i].getPath());
			
			log(urls[i].toString());
		}
		
		classPath = sb.toString();
		if (System.getProperty("java.home") != null)
		{
			javaExec = String.format("%s%s%s%s%s", System.getProperty("java.home"), File.separator, "bin", File.separator, "java");
			log( String.format("Java runner: %s", javaExec) );
		}
	}
	
	public static void configureLogging(IServiceBus esb)
	{
		LoggerService loggerService = new LoggerService();

		esb.registerService(loggerService);
		// Create a logger configuration object and set the level to TRACE
		LoggerServiceConfig loggerConfig = (LoggerServiceConfig) loggerService.getConfiguration();
		try {
			loggerConfig.setLoggingLevel(LoggingLevels.TRACE);
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createFileWriter(String [] args) throws IOException
	{
		if (args.length < 1)
		{
			log("Usage: java... [output-filename] [skip-test-class-1, skip-test-class-2, ...]");
			writer = new PrintWriter(new PrintWriter(System.out));
		}
		else
		{
			File outFile = new File(args[0]);
			writer = new PrintWriter(new FileOutputStream(outFile, true));
			log( String.format("Writing output errors to: %s", outFile.getCanonicalFile()) );
		}
	}
	
	
	private void addAdditionalTestClasses(String [] args)
	{
		
		if (args.length > 1)
		{
			testCasesToSkip = new HashSet<String>();
			Collections.addAll(testCasesToSkip, Arrays.copyOfRange(args, 1, args.length));
		}
	}
	
	private void dropDatabases(int testIndex)
	{
		for(String dbName : databasesToDrop)
		{
			try
			{
				log( String.format("Clearing Database '%s'", dbName));
					
				String [] runCommands = new String [] {"mysql", "-u", "root", "-pussdgw", "-e", String.format("DROP DATABASE IF EXISTS %s;", dbName)};
					
				ProcessOutput pout = runProcess(runCommands);
					
				if ((pout.output != null) && (pout.output.length() > 0)) 
				{
					log( String.format("Running: %s", arrToString(runCommands)) );
					log( String.format("Exist Code: %s", pout.exitCode) );
					log( String.format("Output: %s", pout.output) );
				}
				
			}
			catch (Exception ex)
			{
				logError( String.format("Cleaning Database error: %s", ex.getMessage()));
			}
		}
	}

	/**
	 * Run individual Unit Test case
	 * @param testClass Class Containing JUnit Test cases
	 */
	private int runTestAsJUnitCore(Class<?> testClass)
	{
		int failures = 0;
		try
		{
			Result result = JUnitCore.runClasses(testClass);
			failures = result.getFailureCount();
			handleFailures(result.getFailures(), writer);
		}
		catch (Exception ex)
		{
			String error = String.format("Test %s threw an exception %s%n", testClass, ex.getMessage());
			logError(error);
			handleFailures(Arrays.asList(new Failure[] { new Failure(Description.createSuiteDescription(RunAllTestsBase.class), ex) }), writer);
			failures = 1;
		}
		return failures;
	}
	
	/**
	 * Run individual Unit Test case in a seperate process
	 * @param testClass Class Containing JUnit Test cases
	 */
	private int runTestAsProcess(Class<?> testClass)
	{
		int exitCode = 0;
		try
		{
			// Java and Classes
			String [] commandComponents = {"java", "-cp", classPath, "org.junit.runner.JUnitCore", testClass.getCanonicalName()};
			String command = arrToString(commandComponents);
			
			log( String.format("Running: %s", command) );
			
			ProcessOutput pout = runProcess(commandComponents);
			log( String.format("EXIT code: %d", pout.exitCode) );
			log( "Ouput:" );
			log( createTitle("TEST OUTPUT", "-"));
			log( pout.output );
			
			exitCode =  Math.abs(pout.exitCode);
			
			if (exitCode > 0) 
			{
				handleRunAsProcessFailures(testClass, pout);
			}
		}
		catch (Exception ex)
		{
			String error = String.format("Test %s threw an exception %s%n", testClass, ex.getMessage());
			logError(error);
			handleFailures(Arrays.asList(new Failure[] { new Failure(Description.createSuiteDescription(RunAllTestsBase.class), ex) }), writer);
			exitCode = 1;
		}
		return exitCode;
	}
	
	/**
	 * Main Test Runner
	 */
	private int runAllUnitTests()
	{
		
		int totalFailures = 0;
		log( String.format("Running %d tests", testClasses.length) );
		for(int testIndex = 0; testIndex < testClasses.length; testIndex++)
		{
			Class<?> testClass = testClasses[testIndex];
			
			try 
			{
				log( createTitle(String.format("TEST: %d CLASS: %s", (testIndex + 1), testClass.getName()), "-") );
				if (testCasesToSkip != null && testCasesToSkip.contains(testClass.getName()) != true)
				{
					log("Skipping: " + testClass);
					continue;
				}
				
				dropDatabases(testIndex);
				
				totalFailures += runTestAsProcess(testClass);

			} catch(Exception e)
			{
				logError("Run all tests Issue");
				e.printStackTrace();
			}
			
			// Wait then run next test
			try	{ 
				Thread.sleep(50); 
			} catch (Exception e) {}
			
		}
		
		return totalFailures;
	}
	
	public void startTests(String [] args) 
	{
		log(String.format("args.length = %s, args = %s", args.length, ( args == null ? null : args )));
		setup();
		
		try
		{
			createFileWriter(args);
			addAdditionalTestClasses(args);
			int totalErrorCount = runAllUnitTests();
			
			log("-------- Finished Test Suite --------");
			log( String.format("Total Errors: %d", totalErrorCount) );
			if (failed)
			{
				System.exit(ERROR_EXIT_CODE);
			}
		}
		catch(IOException iex)
		{
			logError( String.format("File %s could not be opened.%n", args[0]) );
			logError( String.format("Exception thrown: %s%n", iex.getMessage()) );
			// XXX TODO FIXME this is joke code ... but this whole test mess is a joke so w/e ... just laugh along.
			iex.printStackTrace();
			System.exit(1);
		}
		finally
		{
			if (writer != null)
			{
				writer.flush();
				writer.close();
			}
		}
		
		log("Existing with Status of 0");
		System.exit(0);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helpers
	//
	// /////////////////////////////////
	private static void log(String msg)
	{
		System.out.println(msg);
	}
	
	private static void logError(String msg)
	{
		System.err.println(msg);
	}
	
	private static String createTitle(String title, String padCharacter)
	{
		StringBuilder sb = new StringBuilder();
		
		int padding = (HORIZONTAL_BAR_LENGTH - title.length() - 2) / 2;
		if (padding > 0)
			sb.append(horizontalLine(padding, padCharacter));
		
		sb.append(" ").append(title).append(" ");
		if (padding > 0)
			sb.append(horizontalLine(padding, padCharacter));
		
		return sb.toString();
	}
	
	private static String horizontalLine(int length,String padCharacter)
	{
		return new String(new char[length]).replace("\0", "-");
	}
	
	private String createFailureMessage(Class<?> testClass, ProcessOutput pout)
	{
		 
		StringBuilder sb = new StringBuilder();
		
		sb.append( String.format("Test Case: %s%n", testClass.getCanonicalName()) );
		sb.append( String.format("Exist Code: %d%n", pout.exitCode) );
		sb.append( String.format("RESULT:%n") );
		sb.append(pout.output);
		
		return sb.toString();
	}
	
	private String createFailuresHeading() 
	{
		StringBuilder sb = new StringBuilder();
		if (!failed)
		{
			failed = true;
			sb.append(EOL).append("UnitTests ====================== ");
			sb.append( new java.util.Date() ).append(EOL).append(EOL);
		}
		
		return sb.toString();
	}
	
	private void handleRunAsProcessFailures(Class<?> testClass, ProcessOutput pout)
	{
		StringBuilder sb = new StringBuilder( createFailuresHeading() );
		sb.append( createFailureMessage(testClass, pout) ).append(EOL);
		
		writer.print( sb.toString() );
		writer.flush();
	}
	
	private void handleFailures(List<Failure> failures, PrintWriter writer)
	{
		log( String.format("Errors found in test: %d", (failures == null || failures.size()==0)? 0 : failures.size()) );
		StringBuilder sb = new StringBuilder();
		
		for (Failure failure : failures)
		{
			logError( failure.toString() );
			
			sb.append( createFailuresHeading() );
			sb.append( failure.toString() ).append(EOL);
			sb.append( String.format("\t%s%n", failure.getDescription()) );
			sb.append( String.format("\t%s%n", failure.getException()) );
			sb.append( String.format("\t%s%n", failure.getTrace()) );
			
		}
		
		log( sb.toString() );
		
		writer.print( sb.toString() );
		writer.flush();
	}
	
	private static String arrToString(String [] text)
	{
		return Arrays.toString(text).replaceAll(",|\\[|\\]", " ");	
	}
	
	private ProcessOutput runProcess(String [] command) throws Exception
	{
		ProcessOutput result = new ProcessOutput();
		
		ProcessBuilder pb = new ProcessBuilder(command);
		Process process = pb.redirectError(ProcessBuilder.Redirect.INHERIT).start();
		IOThreadHandler outputHandler = new IOThreadHandler(process.getInputStream());
		outputHandler.start();
		
		result.exitCode = process.waitFor();
		result.output = outputHandler.getOutput().toString();
		
		return result;
	}
	
	private static class IOThreadHandler extends Thread {
		private InputStream inputStream;
		private StringBuilder output = new StringBuilder();

		IOThreadHandler(InputStream inputStream) {
			this.inputStream = inputStream;
		}

		public void run() {
			Scanner br = null;
			try {
				br = new Scanner(new InputStreamReader(inputStream));
				String line = null;
				while (br.hasNextLine()) {
					line = br.nextLine();
					output.append(line
							+ System.getProperty("line.separator"));
				}
			} finally {
				br.close();
			}
		}

		public StringBuilder getOutput() {
			return output;
		}
	}
	
	private static class ProcessOutput
	{
		public int exitCode = 0;
		public String output = null;
	}
	
}
