package hxc.utils.protocol.uiconnector.request;

import hxc.services.logging.LoggingLevels;

public class LogRequest extends UiBaseRequest
{

	private static final long serialVersionUID = 4599535734560700192L;
	LoggingLevels loggingLevel = LoggingLevels.INFO;

	private String logMessage;

	public LogRequest()
	{
	}

	public LogRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
	}

	/**
	 * @return the logMessage
	 */
	public String getLogMessage()
	{
		return logMessage;
	}

	/**
	 * @param logMessage
	 *            the logMessage to set
	 */
	public void setLogMessage(String logMessage)
	{
		this.logMessage = logMessage;
	}

	/**
	 * @return the loggingLevel
	 */
	public LoggingLevels getLoggingLevel()
	{
		return loggingLevel;
	}

	/**
	 * @param loggingLevel
	 *            the loggingLevel to set
	 */
	public void setLoggingLevel(LoggingLevels loggingLevel)
	{
		this.loggingLevel = loggingLevel;
	}

}
