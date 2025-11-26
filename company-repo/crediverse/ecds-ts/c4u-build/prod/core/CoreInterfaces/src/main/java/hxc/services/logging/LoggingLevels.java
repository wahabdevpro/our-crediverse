package hxc.services.logging;

public enum LoggingLevels
{
	// Severe errors that cause premature termination
	FATAL,

	// Other runtime errors or unexpected conditions
	ERROR,

	// Use of deprecated APIs, poor use of API, 'almost' errors, other runtime situations that are undesirable
	// or unexpected, but not necessarily "wrong".
	WARN,

	// Interesting runtime events (startup/shutdown)
	INFO,

	// Detailed information on the flow through the system.
	DEBUG,

	// Most detailed information.
	TRACE;

}
