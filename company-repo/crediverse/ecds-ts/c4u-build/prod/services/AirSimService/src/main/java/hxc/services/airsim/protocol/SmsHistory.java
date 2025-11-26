package hxc.services.airsim.protocol;

import java.util.Date;

public class SmsHistory implements ISmsHistory
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Date time;
	private String fromMSISDN;
	private String toMSISDN;
	private String text;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	@Override
	public Date getTime()
	{
		return time;
	}

	public void setTime(Date time)
	{
		this.time = time;
	}

	@Override
	public String getFromMSISDN()
	{
		return fromMSISDN;
	}

	public void setFromMSISDN(String fromMSISDN)
	{
		this.fromMSISDN = fromMSISDN;
	}

	@Override
	public String getToMSISDN()
	{
		return toMSISDN;
	}

	public void setToMSISDN(String toMSISDN)
	{
		this.toMSISDN = toMSISDN;
	}

	@Override
	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public SmsHistory()
	{

	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public SmsHistory(Date time, String fromMSISDN, String toMSISDN, String text)
	{
		this.time = time;
		this.fromMSISDN = fromMSISDN;
		this.toMSISDN = toMSISDN;
		this.text = text;
	}

	public SmsHistory(ISmsHistory history)
	{
		this.time = history.getTime();
		this.fromMSISDN = history.getFromMSISDN();
		this.toMSISDN = history.getToMSISDN();
		this.text = history.getText();
	}

}
