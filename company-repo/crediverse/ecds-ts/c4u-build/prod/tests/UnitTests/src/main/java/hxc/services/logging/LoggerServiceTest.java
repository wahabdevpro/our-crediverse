package hxc.services.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import hxc.services.logging.LoggerService.LoggerServiceConfig;
import hxc.testsuite.RunAllTestsBase;

public class LoggerServiceTest extends RunAllTestsBase
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Private Properties
	//
	// /////////////////////////////////
	private final String directoryName = "/tmp/hxclog";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	// /////////////////////////////////

	//@Test
	public void testThrowableProcessing()
	{
		File directory;
		try
		{
			directory = new File(directoryName).getCanonicalFile();
			prepareDirectory( directory );

			LoggerService loggerService = new LoggerService();
			LoggerServiceConfig config = (LoggerServiceConfig) loggerService.getConfiguration();
			config.setDirectoryName(directoryName);
			config.setRotationIntervalSeconds( Integer.MAX_VALUE );
			config.setLoggingLevel( LoggingLevels.TRACE );
			config.setInterimFileName( "tmp.log" );
			loggerService.start( null );

			File interimFile = new File( directory, config.getInterimFileName() );
			/*
				Have to wait for file to exist before we continue ...
			*/
			while( ! interimFile.exists() )
			{
                // FIXME - we do not exit, this could be an indefinite wait if we don't timeout after a reasonable period
				Thread.sleep( 50 );
			}

			Throwable throwable = new Throwable( "message.t0" );
			throwable.setStackTrace( new StackTraceElement[]{
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 2 ),
			} );

			////
			throwable.addSuppressed( new Throwable( "message.t0.s0" ) );
			throwable.getSuppressed()[ 0 ].setStackTrace( new StackTraceElement[]{
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s0", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s0", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 2 ),
			} );
			throwable.addSuppressed( new Throwable( "message.t0.s1" ) );
			throwable.getSuppressed()[ 1 ].setStackTrace( new StackTraceElement[]{
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s1", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s1", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s1", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 2 ),
			} );

			throwable.getSuppressed()[ 1 ].initCause( new Throwable( "message.t0.s1.c0" ) );
			throwable.getSuppressed()[ 1 ].getCause().setStackTrace( new StackTraceElement[]{
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s1.c0", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s1.c0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s1.c0", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s1", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s1", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.s1", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 2 ),
			} );
			////
			throwable.initCause( new Throwable( "message.t0.c0" ) );
			throwable.getCause().setStackTrace( new StackTraceElement[]{
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 2 ),
			} );
			throwable.getCause().addSuppressed( new Throwable( "message.t0.c0.s0" ) );
			throwable.getCause().getSuppressed()[ 0 ].setStackTrace( new StackTraceElement[]{
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s0", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s0", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 2 ),
			} );
			throwable.getCause().addSuppressed( new Throwable( "message.t0.c0.s1" ) );
			throwable.getCause().getSuppressed()[ 1 ].setStackTrace( new StackTraceElement[]{
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s1", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s1", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s1", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 2 ),
			} );

			////
			throwable.getCause().initCause( new Throwable( "message.t0.c0.c0" ) );
			throwable.getCause().getCause().setStackTrace( new StackTraceElement[]{
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.c0", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.c0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.c0", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0", 2 ),
			} );
			throwable.getCause().getCause().addSuppressed( new Throwable( "message.t0.c0.s0" ) );
			throwable.getCause().getCause().getSuppressed()[ 0 ].setStackTrace( new StackTraceElement[]{
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s0", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s0", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 0 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 2 ),
			} );
			throwable.getCause().getCause().addSuppressed( new Throwable( "message.t0.c0.s1" ) );
			throwable.getCause().getCause().getSuppressed()[ 1 ].setStackTrace( new StackTraceElement[]{
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s1", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0.s1", 2 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 1 ),
				new StackTraceElement( "delcaringClass", "methodName", "fileName.t0.c0", 2 ),
			} );

			//throwable.getCause().getCause().initCause( throwable.getSuppressed()[ 1 ] );
			//throwable.getCause().getCause().getSuppressed()[ 1 ].initCause( throwable );

			// To get a reference, use printStackTrace
			StringWriter sw = new StringWriter();
			System.err.println("+++++++++++++++++++++++++++++++++++++++++");
			throwable.printStackTrace();
			System.err.println("+++++++++++++++++++++++++++++++++++++++++");
			// Replace tabs with two spaces since the logger's throwable processing will use 2*SPACE instead of TAB
			String reference = sw.toString().replaceAll( "\\t", "  " );

			// Replace tabs with two spaces since the logger's throwable processing will use 2*SPACE instead of TAB
			//loggerService.log( this, throwable );
			String content = new String( Files.readAllBytes( Paths.get( interimFile.getPath() ) ), "UTF-8" );

			System.err.printf( "content\n%s\n", content );
			// Strip out log line preamble
			content = Pattern
				.compile( "^[0-9T.]{19}\\|"
					+ "ERROR\\|"
					+ "                    \\|"
					+ LoggerServiceTest.class.getName() + "[ ]*\\|"
					+ "testThrowableProcess\\|"
					// Accomodate negative line numbers since apparently that is a thing ...
					+ "[ ]*[0-9-]+\\|"
					, Pattern.MULTILINE )
				.matcher( content ).replaceAll( "" );

			// De-indent CIRCULAR REFERENCE since printStackTrace on throwable does not indent it.
			content = Pattern
				.compile( "^  [ ]+\\[CIRCULAR REFERENCE:" , Pattern.MULTILINE )
				.matcher( content ).replaceAll( "  [CIRCULAR REFERENCE:" );

			System.err.printf( "content\n%s\n", content );
			System.err.printf( "reference\n%s\n", reference );
			//System.err.printf( "testExceptionProcessing:\n%s\n", reference );
			//System.err.printf( "testExceptionProcessing:\n%s\n", content );
			//System.err.printf( "testExceptionProcessing: equals = %s", content.equals( reference ) );
			assertEquals( content, reference );
		}
		catch (Exception e)
		{
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	@Test
	public void logfileTest1()
	{
	}
	public void logfileTest()
	{
		File directory;
		try
		{
			// Create Directory
			directory = new File(directoryName).getCanonicalFile();
			if (!directory.exists())
				directory.mkdirs();

			// Delete log files in it
			File[] files = getLogfiles(directory);
			for (File file : files)
			{
				file.delete();
			}

			// Create a Logger
			LoggerService loggerService = new LoggerService();
			LoggerServiceConfig config = (LoggerServiceConfig) loggerService.getConfiguration();
			config.setDirectoryName(directoryName);
			config.setRotationIntervalSeconds(10);
			config.setLoggingLevel(LoggingLevels.TRACE);
			config.setInterimFileName("tmp.log");
			loggerService.start(null);

			// Test if (only) interim file exists
			Thread.sleep(1000);
			files = getLogfiles(directory);
			assertEquals(1, files.length);
			assertEquals(files[0].getName(), config.getInterimFileName());

			// Create Log Entries
			config.setLoggingLevel(LoggingLevels.TRACE);
			//loggerService.trace(this, transactionID, "Trace%d", 1);
			//loggerService.warn(this, transactionID, "Warn%d", 2);
			config.setLoggingLevel(LoggingLevels.INFO);
			//loggerService.debug(this, transactionID, "Debug%d", 3);
			//loggerService.fatal(this, transactionID, "Fatal%d", 4);

			// Wait for rotation to occur
			Thread.sleep(12000);

			// Make another entry
			//loggerService.error(this, transactionID, "Error%d", 5);
			Thread.sleep(500);

			// Stop the logger
			loggerService.stop();
			Thread.sleep(500);

			// Get the log files
			files = getLogfiles(directory);
			Arrays.sort(files);
			List<String> levels = new ArrayList<String>();
			for (File file : files)
			{
				FileReader fr = new FileReader(file);
				BufferedReader rdr = new BufferedReader(fr);
				while (true)
				{
					String line = rdr.readLine();
					if (line == null)
						break;
					String[] parts = line.split("\\|");
					levels.add(parts[1].trim());
				}
				rdr.close();
				fr.close();
			}
			assertEquals(6, levels.size());
			// Original Order was TRACE(0) WARN(1) FATAL(2) ERROR(3) WARN(4)
			assertEquals("TRACE", levels.get(0));
			assertEquals("WARN", levels.get(1));
			assertEquals("FATAL", levels.get(2));
			assertEquals("INFO", levels.get(3));
			assertEquals("ERROR", levels.get(4));
			assertEquals("INFO", levels.get(5));

		}
		catch (Exception e)
		{
			fail(e.getMessage());
		}

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Helper Methods
	//
	// /////////////////////////////////

	private void prepareDirectory( File directory )
		throws IOException
	{
		// Create Directory
		if (!directory.exists())
			directory.mkdirs();

		// Delete log files in it
		File[] files = getLogfiles(directory);
		for (File file : files)
		{
			file.delete();
		}
	}

	private File[] getLogfiles(File directory)
	{
		File[] files = directory.listFiles();
		List<File> result = new ArrayList<File>();
		for (File file : files)
		{
			String name = file.getName().toLowerCase();
			if (name.endsWith(".log") || name.endsWith(".txt"))
				result.add(file);
		}

		return result.toArray(new File[] {});
	}

}
