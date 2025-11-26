package hxc.utils.protocol.uiconnector.logtailer;

import java.util.ArrayList;
import java.util.List;

import hxc.utils.protocol.uiconnector.response.UiBaseResponse;

public class LogFileResponse extends UiBaseResponse
{

	private static final long serialVersionUID = -4711227979266403804L;
	private List<LogDTO> logRecords;
	private int lastPosition = 0;

	public LogFileResponse()
	{

	}

	public LogFileResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
		logRecords = new ArrayList<LogDTO>();
	}

	public List<LogDTO> getLogRecords()
	{
		return logRecords;
	}

	public void setLogRecords(List<LogDTO> logRecords)
	{
		this.logRecords = logRecords;
	}

	public int getLastPosition()
	{
		return lastPosition;
	}

	public void setLastPosition(int lastPosition)
	{
		this.lastPosition = lastPosition;
	}

}
