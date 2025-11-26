package hxc.services.logging;

public interface ILogger
{
	/*
	// Severe errors that cause premature termination
	public abstract void fatal(Object origin, String message, Object... args);

	// Other runtime errors or unexpected conditions
	public abstract void error(Object origin, String message, Object... args);

	// Use of deprecated APIs, poor use of API, 'almost' errors, other runtime situations that are undesirable
	// or unexpected, but not necessarily "wrong".
	public abstract void warn(Object origin, String message, Object... args);

	// Interesting runtime events (startup/shutdown)
	public abstract void info(Object origin, String message, Object... args);

	// Detailed information on the flow through the system.
	public abstract void debug(Object origin, String message, Object... args);

	// Most detailed information.
	public abstract void trace(Object origin, String message, Object... args);

	// Log an Exception
	public abstract void log(Object origin, Throwable e);
	
	// Log an Exception
	public abstract void log(LoggingLevels level, Object origin, Throwable e);

	// Create a log entry
	public abstract void log(LoggingLevels level, Object origin, String message, Object... args);

	public abstract LoggingLevels getLevel();
	*/

	public abstract String getLogHistory();
}
