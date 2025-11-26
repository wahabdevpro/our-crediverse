package hxc.supervisor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class Supervisor
{

	// Exit values
	private static final int RESTART_EXIT_VALUE = SupervisorConstants.RESTART_EXIT_CODE;
	private static final int SHUTDOWN_EXIT_VALUE = SupervisorConstants.SHUTDOWN_EXIT_CODE;
	private static final int CANT_STARTUP_EXIT_VALUE = SupervisorConstants.CANT_STARTUP_EXIT_CODE;

	private static final long CHECK_INTERVAL = SupervisorConstants.CHECK_INTERVAL;

	// A reference of the current threads
	private static ProcessThread[] threads;

	public static void main(String args[])
	{
		// Create a supervisor
		Supervisor supervisor = new Supervisor();

		// Create a thread group to monitor
		final ThreadGroup threadGroup = new ThreadGroup("SupervisorThreadGroup");

		// Get the java
		String java = getJava(args);

		// Get the commands for each application
		String hostprocessCommand = getHostProcessCommand(java);
		String guiserverCommand = getGuiServerCommand(java);

		// Create the process threads for each application under the custom thread group
		ProcessThread hostprocessThread = supervisor.new ProcessThread(threadGroup, hostprocessCommand);
		ProcessThread guiserverThread = supervisor.new ProcessThread(threadGroup, guiserverCommand);

		// Add the threads to the 'threads' variable
		threads = new ProcessThread[] { hostprocessThread, guiserverThread };

		// Start the monitoring thread
		supervisor.new MonitoringThread(threadGroup, threads).start();

		println("Starting Supervisor...");

		// Finally add the shutdown hook to shutdown all the threads
		addShutdownHook();
	}

	// Outputs text
	private static void println(String output, Object... args)
	{
		// Outputs to the console at the moment
		System.out.println(String.format(output, args));
	}

	// Gets the location of the java command
	private static String getJava(String args[])
	{
		// Check if there are arguments supplied to the supervisor
		if (args != null && args.length > 0)
		{
			return args[0];
		}

		// Get the version of java on the machine
		String version = "";
		try
		{
			// Execute the java -d64 -version command to see if 64 bit exists
			Process proc = Runtime.getRuntime().exec("java -d64 -version");

			// Wait for the process to end
			proc.waitFor();

			// Get the exit value
			if (proc.exitValue() == 0)
			{
				version = "-d64";
			}
		}
		catch (Exception exc)
		{
			version = "";
		}

		// Return the java command
		return "java " + version;

	}

	// Gets the current path of the process
	private static String getCurrentPath()
	{

		// Get the path of the supervisor
		String path = Supervisor.class.getProtectionDomain().getCodeSource().getLocation().getPath();

		// Return the path
		return path.substring(0, path.lastIndexOf('/') + 1);

	}

	// Gets the relative path to the hostprocess application
	private static String getHostProcessRelativePath()
	{
		return String.format("%s/../../hostprocess", getCurrentPath());
	}

	// Gets the absolute path to the hostprocess application
	private static String getHostProcessAbsolutePath()
	{
		try
		{
			return new File(getHostProcessRelativePath()).getCanonicalPath();
		}
		catch (IOException e)
		{
			return getHostProcessAbsolutePath();
		}
	}

	// Gets the relative path to the guiserver application
	private static String getGuiServerRelativePath()
	{
		return String.format("%s/../../guiserver", getCurrentPath());
	}

	// Gets the absolute path to the guiserver application
	private static String getGuiServerAbsolutePath()
	{
		try
		{
			return new File(getGuiServerRelativePath()).getCanonicalPath();
		}
		catch (IOException e)
		{
			return getGuiServerRelativePath();
		}
	}

	// Gets the executable command for the hostprocess
	private static String getHostProcessCommand(String java)
	{
		String hostprocess = getHostProcessAbsolutePath();
		return String.format("%s -server -cp %s/lib:%s/lib/*:%s/lib/*.jar hxc.test.HostObject", java, hostprocess, hostprocess, hostprocess);
	}

	// Gets the executable command for the guiserver
	private static String getGuiServerCommand(String java)
	{
		String guiserver = getGuiServerAbsolutePath();
		return String.format("%s -server -cp %s/lib:%s/lib/*:%s/lib/*.jar hxc.userinterfaces.gui.jetty.JettyMain", java, guiserver, guiserver, guiserver);
	}

	// Adds a shutdown hook
	private static void addShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			@Override
			public void run()
			{
				println("Ending Supervisor...");

				for (int i = 0; i < threads.length; i++)
				{
					while (threads[i] != null && threads[i].isAlive())
					{
						threads[i].shutdown();
					}
				}

				println("Finished.");
			}
		});
	}

	// Responsible for monitoring process threads
	class MonitoringThread extends Thread
	{

		// Properties
		private ThreadGroup threadGroup;
		private ProcessThread processThreads[];

		public MonitoringThread(ThreadGroup threadGroup, ProcessThread processThreads[])
		{
			this.threadGroup = threadGroup;
			this.processThreads = processThreads;
		}

		@Override
		public void run()
		{
			// Start all the processes
			startProcesses();

			// Delay
			delay(DELAY);

			// Run infinite loop
			while (true)
			{
				// Check if all processes are dead
				if (allDead())
				{
					// Exit the loop
					break;
				}

				// Keep status
				int status = 0;

				// Iterate through the processes
				for (int i = 0; i < processThreads.length; i++)
				{
					// Get process
					ProcessThread process = processThreads[i];

					// Ensure process is not null
					if (process == null)
						continue;

					// Check if the process is alive
					if (!process.isAlive())
					{
						// Check the exit value of the process
						if (process.getExitValue() != SHUTDOWN_EXIT_VALUE && process.getExitValue() != RESTART_EXIT_VALUE && process.getExitValue() != CANT_STARTUP_EXIT_VALUE)
						{
							// Create process thread
							process = new ProcessThread(threadGroup, process.getName());

							// If not any of them, then start it up again
							process.start();

							// Set the process thread
							processThreads[i] = process;
						}
					}

					// Check if process cannot start up
					if (process.getExitValue() == CANT_STARTUP_EXIT_VALUE)
					{
						// Shutdown the process
						process.shutdown();

						// Set it to null
						process = null;
						processThreads[i] = null;

						continue;
					}

					// Check if it is a restart exit value
					if (process.getExitValue() == RESTART_EXIT_VALUE)
					{
						// Set the status
						status = RESTART_EXIT_VALUE;

						break;
					}

					// Check if it is a shutdown exit value
					if (process.getExitValue() == SHUTDOWN_EXIT_VALUE)
					{
						// Set the status
						status = SHUTDOWN_EXIT_VALUE;

						break;
					}

				}

				// Check if a restart is required
				if (status == RESTART_EXIT_VALUE)
				{
					println("Restarting...");

					stopProcesses();
					startProcesses();
				}

				// Check if a shutdown is required
				if (status == SHUTDOWN_EXIT_VALUE)
				{
					println("Shutting Down...");

					// Stop all the processes
					stopProcesses();

					break;
				}

				// Delay the check
				delay(CHECK_INTERVAL);
			}
		}

		// Starts all the processes
		private void startProcesses()
		{
			// Iterate through the processes
			for (int i = 0; i < processThreads.length; i++)
			{
				// Get process
				ProcessThread process = processThreads[i];

				// Ensure the process is not null
				if (process == null)
					continue;

				// Delay before starting applications
				delay(DELAY);

				// Check if the process is not alive
				if (!process.isAlive())
				{
					// Create process
					process = new ProcessThread(threadGroup, process.getName());

					// Start the process
					process.start();

					// Set the process thread
					processThreads[i] = process;
				}
			}
		}

		// Stops all processes
		private void stopProcesses()
		{
			// Iterate through the processes
			for (int i = 0; i < processThreads.length; i++)
			{
				// Get process
				ProcessThread process = processThreads[i];

				// Ensure the process is not null
				if (process == null)
					continue;

				// Delay before stopping applications
				delay(DELAY);

				// Check if the process is alive
				if (process.isAlive())
				{
					// Shutdown the process
					process.shutdown();

					try
					{
						// Wait for the process to shutdown
						process.join();
					}
					catch (InterruptedException e)
					{

					}
				}
			}
		}

		// Checks if all processes are dead
		private boolean allDead()
		{
			// Iterate through the processes
			for (int i = 0; i < processThreads.length; i++)
			{
				// Get process
				ProcessThread process = processThreads[i];

				// Ensure the process is not null
				if (process == null)
					continue;

				// Check if process is alive
				if (process.isAlive())
					return false;

				// Check the exit value
				if (process.getExitValue() != SHUTDOWN_EXIT_VALUE && process.getExitValue() != RESTART_EXIT_VALUE && process.getExitValue() != CANT_STARTUP_EXIT_VALUE)
					return false;

			}
			return true;
		}

		// Waits for a specific time
		private void delay(long delay)
		{
			try
			{
				Thread.sleep(delay);
			}
			catch (InterruptedException exc)
			{

			}
		}

		private final long DELAY = 2000;

	}

	// Responsible for executing a command
	class ProcessThread extends Thread
	{

		// The command that will be executed
		private String command;

		// Reference to the process
		private Process proc;

		// The exit value of the process
		private int exitValue;

		public ProcessThread(ThreadGroup group, String command)
		{
			super(group, command);
			this.command = command;
		}

		@Override
		public void run()
		{
			println("Starting %s", command);

			try
			{
				// Execute the command
				proc = new ProcessBuilder( Arrays.asList( command.split( "\\s+" ) ) ).inheritIO().start();
			}
			catch (IOException e)
			{
				println("Running %s Failed %s", command, e);
				e.printStackTrace();

				// Set the exit value to the SHUTDOWN exit value
				exitValue = SHUTDOWN_EXIT_VALUE;

				return;
			}

			try
			{
				// Wait for the process to end
				proc.waitFor();
			}
			catch (InterruptedException e)
			{
				println("Could not wait for process to end.");
			}

			// Get the exit value
			exitValue = proc.exitValue();

			println("Exiting " + command + ".");
		}

		// Shuts the thread down by stopping the process
		public void shutdown()
		{
			// Check if the thread is alive
			if (isAlive())
			{
				// Destroy the process
				proc.destroy();
			}
		}

		// Accessor methods

		public String getCommand()
		{
			return this.command;
		}

		public int getExitValue()
		{
			return this.exitValue;
		}
	}

}
