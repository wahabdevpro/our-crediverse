package hxc.utils.protocol.uiconnector.logtailer;

import hxc.utils.protocol.uiconnector.request.UiBaseRequest;

public class LogFileRequest extends UiBaseRequest
{

	private static final long serialVersionUID = -3849182576631633428L;

	private LogFilterOptions filter;
	private int maxRecordsReturned = 1000;
	private int readPosition = -1; // Use for tailing

	public LogFileRequest(String userId, String sessionId)
	{
		super(userId, sessionId);
		filter = new LogFilterOptions();
	}

	public static long getSerialversionuid()
	{
		return serialVersionUID;
	}

	public int getMaxRecordsReturned()
	{
		return maxRecordsReturned;
	}

	public void setMaxRecordsReturned(int maxRecordsReturned)
	{
		this.maxRecordsReturned = maxRecordsReturned;
	}

	public int getReadPosition()
	{
		return readPosition;
	}

	public void setReadPosition(int readPosition)
	{
		this.readPosition = readPosition;
	}

	public LogFilterOptions getFilter()
	{
		return filter;
	}

	public void setFilter(LogFilterOptions filter)
	{
		this.filter = filter;
	}

}
