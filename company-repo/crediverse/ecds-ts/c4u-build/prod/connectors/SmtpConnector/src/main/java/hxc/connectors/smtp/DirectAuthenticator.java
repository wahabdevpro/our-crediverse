package hxc.connectors.smtp;

import jakarta.mail.Authenticator;
import jakarta.mail.PasswordAuthentication;

public class DirectAuthenticator extends Authenticator
{
	String username;
	String password;

	public String getUsername()
	{
		return this.username;
	}

	public String getPassword()
	{
		return this.password;
	}

	public String describe(String extra)
	{   
		return String.format("%s@%s(" +
			"username = '%s', password = '%s'" +
			"%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			username, password,
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

	public DirectAuthenticator(String username, String password)
	{
		super();
		this.username = username;
		this.password = password;
	}
	public PasswordAuthentication getPasswordAuthentication()
	{
		return new PasswordAuthentication(username, password);
	}
}
