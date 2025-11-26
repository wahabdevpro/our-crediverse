package hxc.utils.protocol.uiconnector.logtailer;

import java.io.Serializable;
import java.util.Date;

import hxc.services.logging.LoggingLevels;

@SuppressWarnings("serial")
public class LogFilterOptions implements Serializable
{
	private Date startDate = null;
	private Date endDate = null;
	private String text = null;
	private LoggingLevels[] loggingLevels;
	private boolean onlyNonBlankTID;
	private boolean latestRecordAtTop;

	public LogFilterOptions()
	{
	}

	public Date getStartDate()
	{
		return startDate;
	}

	public void setStartDate(Date startDate)
	{
		this.startDate = startDate;
	}

	public Date getEndDate()
	{
		return endDate;
	}

	public void setEndDate(Date endDate)
	{
		this.endDate = endDate;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public LoggingLevels[] getLoggingLevels()
	{
		return loggingLevels;
	}

	public void setLoggingLevels(LoggingLevels[] loggingLevels)
	{
		this.loggingLevels = loggingLevels;
	}

	public boolean isOnlyNonBlankTID()
	{
		return onlyNonBlankTID;
	}

	public void setOnlyNonBlankTID(boolean onlyNonBlankTID)
	{
		this.onlyNonBlankTID = onlyNonBlankTID;
	}

	public boolean isLatestRecordAtTop()
	{
		return latestRecordAtTop;
	}

	public void setLatestRecordAtTop(boolean latestRecordAtTop)
	{
		this.latestRecordAtTop = latestRecordAtTop;
	}

}
