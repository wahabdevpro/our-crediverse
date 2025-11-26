package hxc.connectors.smtp;

import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;

import java.util.Objects;

// Dont use from multiple threads ...
public class SmtpConnectionReference implements ISmtpConnection
{
	SmtpConnector connector;
	SmtpConnection connection;

	public SmtpConnectionReference( SmtpConnector connector, SmtpConnection connection )
	{
		this.connector = connector;
		this.connection = connection;
	}


	@Override
	public MimeMessage createMimeMessage()
	{
		Objects.requireNonNull(this.connection, "connection may not be null ... (already closed ?)");
		return this.connection.createMimeMessage();
	}

	@Override
	public void send(Message message) throws Exception
	{
		Objects.requireNonNull(this.connection, "connection may not be null ... (already closed ?)");
		this.connection.send(message);
	}

	@Override
	public void close()
	{
		if ( connection != null )
		{
			this.connector.returnConnection(connection);
			this.connection = null;
		}
	}

    //////////////////////////////////////////////////////////////////////////////
    public String describe(String extra)
    {
        return String.format("%s@%s(connection = %s%s%s)",
            this.getClass().getName(), Integer.toHexString(this.hashCode()),
			connection,		
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
