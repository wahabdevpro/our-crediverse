package hxc.connectors.smtp;

import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.TransportEvent;

public class SmtpEvent
{
	private TransportEvent transportEvent;
	private ConnectionEvent connectionEvent;
	private Throwable throwable;

	public SmtpEvent(TransportEvent transportEvent)
	{
		this.transportEvent = transportEvent;
	}

	public SmtpEvent(ConnectionEvent connectionEvent)
	{
		this.connectionEvent = connectionEvent;
	}

	public SmtpEvent(Throwable throwable)
	{
		this.throwable = throwable;
	}

	//////////////////////////////////////////////////////////////////////////////

	public String describe(String extra)
	{
		return String.format("%s@%s(transportEvent = %s, connectionEvent = %s, throwable = %s%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			transportEvent, connectionEvent, throwable,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{   
		return this.describe();
	}
}
