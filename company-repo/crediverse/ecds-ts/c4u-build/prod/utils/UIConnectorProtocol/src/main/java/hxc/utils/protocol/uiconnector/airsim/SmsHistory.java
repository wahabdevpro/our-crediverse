package hxc.utils.protocol.uiconnector.airsim;

import java.io.Serializable;
import java.util.Date;

import hxc.services.airsim.protocol.ISmsHistory;

public class SmsHistory implements Serializable, ISmsHistory
{
	private static final long serialVersionUID = 6902326145356525301L;
	private Date time;
	private String fromMsisdn;
	private String toMsisdn;
	private String text;

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
		return fromMsisdn;
	}

	public void setFromMSISDN(String fromMsisdn)
	{
		this.fromMsisdn = fromMsisdn;
	}

	@Override
	public String getToMSISDN()
	{
		return toMsisdn;
	}

	public void setToMSISDN(String toMsisdn)
	{
		this.toMsisdn = toMsisdn;
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

	public SmsHistory(Date date, String from, String to, String message)
	{
		super();
		this.time = date;
		this.fromMsisdn = from;
		this.toMsisdn = to;
		this.text = message;
	}

	public SmsHistory()
	{

	}

}
