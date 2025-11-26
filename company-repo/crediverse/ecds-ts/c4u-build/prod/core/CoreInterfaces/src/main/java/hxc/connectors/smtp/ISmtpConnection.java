package hxc.connectors.smtp;

import hxc.connectors.IConnection;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMessage;

public interface ISmtpConnection extends IConnection
{
	public MimeMessage createMimeMessage();

	public void send(Message message) throws Exception;
}
