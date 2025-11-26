package hxc.connectors.smtp;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import hxc.connectors.IConnector;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;

public interface ISmtpConnector extends IConnector
{
	@Override
	public ISmtpConnection getConnection(String optionalConnectionString) throws IOException;

	public ISmtpConnection getConnection(String optionalConnectionString, long timeout, TimeUnit timeUnit) throws Exception;

	public MimeMessage createMimeMessage();

	public void send(Message message) throws Exception;

	public void send(Message message, long timeout, TimeUnit timeUnit) throws Exception;

	public ISmtpHistory[] getHistory();

	public void clearHistory();
}
