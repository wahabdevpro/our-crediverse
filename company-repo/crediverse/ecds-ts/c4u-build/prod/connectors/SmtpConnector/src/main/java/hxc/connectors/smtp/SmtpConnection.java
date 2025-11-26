package hxc.connectors.smtp;

import java.util.Objects;
import java.util.Queue;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.event.ConnectionEvent;
import jakarta.mail.event.TransportEvent;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.util.MailConnectException;

import hxc.servicebus.IServiceBus;

public class SmtpConnection implements ISmtpConnection
{
	final static Logger logger = LoggerFactory.getLogger(SmtpConnection.class);
	
	private SmtpConnector.Configuration configuration;
	private Session session;
	private Transport transport;
	private final Queue<SmtpHistory> historyQueue;

	public class ConnectionListener implements jakarta.mail.event.ConnectionListener
	{
		@Override
		public void closed(ConnectionEvent connectionEvent)
		{
			logger.info("{}.ConnectionListener.closed({})", connectionEvent);
		}

		@Override
		public void disconnected(ConnectionEvent connectionEvent)
		{
			logger.info("{}.ConnectionListener.disconnected({})", connectionEvent);
		}

		@Override
		public void opened(ConnectionEvent connectionEvent)
		{
			logger.info("{}.ConnectionListener.disconnected({})", connectionEvent);
		}
	}

	public class TransportListener implements jakarta.mail.event.TransportListener
	{
		@Override
		public void messageDelivered(TransportEvent transportEvent)
		{
			logger.info("{}.ConnectionListener.messageDelivered({})", transportEvent);
		}

		@Override
		public void messageNotDelivered(TransportEvent transportEvent)
		{
			logger.info("{}.ConnectionListener.messageNotDelivered({})", transportEvent);
		}

		@Override
		public void messagePartiallyDelivered(TransportEvent transportEvent)
		{
			logger.info("{}.ConnectionListener.opened({})", transportEvent);
		}
		
	}

	public SmtpConnection(IServiceBus esb, SmtpConnector smtpConnector, SmtpConnector.Configuration configuration, SmtpConnector.ServerConfiguration serverConfiguration, Queue<SmtpHistory> historyQueue) throws Exception
	{
		this.configuration = configuration;
		logger.trace("SmtpConnection: Creating connection with properties {}", serverConfiguration.asProperties());
		this.session = serverConfiguration.asSession();
		this.transport = this.session.getTransport(serverConfiguration.getProtocol());
		Objects.requireNonNull(this.transport, "this.transport may not be null ... config error ?");
		this.transport.addTransportListener(new TransportListener());
		this.transport.addConnectionListener(new ConnectionListener());
		this.historyQueue = historyQueue;
	}

	public MimeMessage createMimeMessage()
	{
		return new MimeMessage(this.session);
	}

	public synchronized void send(Message message) throws Exception
	{
		int retries = configuration.getRetriesPerServer();
		for ( int retry = 0; retry <= retries; ++retry )
		{
			try
			{
				logger.info("{}.send:(retry = {}) sending message {} via transport {}", this, retry, message, this.transport);
				Objects.requireNonNull(this.transport, "transport may not be null ... (already closed ?)");
				if (this.transport.isConnected() == false) this.transport.connect();
				this.transport.sendMessage(message, message.getAllRecipients());
				try
				{
					this.historyQueue.add(new SmtpHistory(message));
				}
				catch(Throwable e)
				{
					logger.error("Failed to add message to queue ...");
					logger.error(e.getMessage(), e);
				}
				return;
			}
			catch(MailConnectException mailConnectException)
			{
				if (retry < retries) logger.error(mailConnectException.getMessage(), mailConnectException);
				this.transport.close();
				if (retry >= retries) throw mailConnectException;
			}
			catch(MessagingException messagingException)
			{
				throw messagingException;
			}
			catch(Throwable throwable)
			{
				throw throwable;
			}
		}
	}

	@Override
	public synchronized void close()
	{
		logger.info("{}.close(): ...", this);
		if (this.transport != null)
		{
			logger.info("SmtpConnection.close(): closing ...");
			try
			{
				this.transport.close();
			}
			catch(Throwable e)
			{
				logger.error(e.getMessage(), e);
			}
			this.transport = null;
		}
		else
		{
			logger.info("SmtpConnection.close(): already closed ...");
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	public String describe(String extra)
	{
		return String.format("%s@%s(%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
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
