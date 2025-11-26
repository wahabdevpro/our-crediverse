package hxc.utils.protocol.uiconnector.logtailer;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("serial")
public class LogFileRecord implements Serializable
{
	private String host;
	private Date timeStamp;
	private String sevity;
	private String transactionID;
	private String text;

	public LogFileRecord()
	{
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public Date getTimeStamp()
	{
		return timeStamp;
	}

	public void setTimeStamp(Date timeStamp)
	{
		this.timeStamp = timeStamp;
	}

	public String getSevity()
	{
		return sevity;
	}

	public void setSevity(String sevity)
	{
		this.sevity = sevity;
	}

	public String getTransactionID()
	{
		return transactionID;
	}

	public void setTransactionID(String transactionID)
	{
		this.transactionID = transactionID;
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

}
