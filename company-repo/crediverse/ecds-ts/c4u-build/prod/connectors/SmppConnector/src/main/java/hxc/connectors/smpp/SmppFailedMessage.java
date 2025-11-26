package hxc.connectors.smpp;

import java.util.Date;

import com.cloudhopper.smpp.pdu.SubmitSm;

public class SmppFailedMessage
{
	private SubmitSm message;
	private Date dateFailed;

	public SmppFailedMessage()
	{
		dateFailed = new Date();
	}

	public SubmitSm getMessage()
	{
		return message;
	}

	public void setMessage(SubmitSm message)
	{
		this.message = message;
	}

	public Date getDateFailed()
	{
		return dateFailed;
	}

	public void setDateFailed(Date dateFailed)
	{
		this.dateFailed = dateFailed;
	}
}
