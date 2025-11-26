package hxc.connectors.smpp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.cloudhopper.commons.charset.CharsetUtil;
import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerConfiguration;
import com.cloudhopper.smpp.SmppServerHandler;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.pdu.DeliverSm;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppInvalidArgumentException;
import com.cloudhopper.smpp.type.SmppProcessingException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;

import hxc.connectors.smpp.client.SmppClient;
import hxc.connectors.smpp.session.SmppSession;

/*
	This test suite is mostly garbage and is testing code which is not being used and exists for reasons that seem backwards and wrong (e.g. instead of adapting slf4j the code was copied and edited to work with C4U logging framework)

	DO NOT ADD TO IT. Add to SmppConnectorTests rather ...
*/

public class SMSTest
{

	private static DefaultSmppServer server;
	private SmppClient client;
	private SmppSession session;
	private static boolean mustStop = false;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception
	{

		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		ScheduledThreadPoolExecutor monitorExecutor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory()
		{
			private AtomicInteger sequence = new AtomicInteger(0);

			@Override
			public Thread newThread(Runnable r)
			{
				Thread t = new Thread(r);
				t.setName("SmppServerSessionWindowMonitorPool-" + sequence.getAndIncrement());
				return t;
			}
		});

		// Setup the server so that it can host on the specified port
		SmppServerConfiguration config = new SmppServerConfiguration();
		config.setPort(2776);
		config.setMaxConnectionSize(10);
		config.setNonBlockingSocketsEnabled(true);
		config.setDefaultRequestExpiryTimeout(30000);
		config.setDefaultWindowMonitorInterval(15000);
		config.setDefaultWindowSize(5);
		config.setDefaultWindowWaitTimeout(config.getDefaultRequestExpiryTimeout());
		config.setDefaultSessionCountersEnabled(true);
		config.setJmxEnabled(true);

		// Create the server
		server = new DefaultSmppServer(config, new SmppServerHandler()
		{

			@Override
			public void sessionDestroyed(Long value, SmppServerSession session)
			{
				// Destroy the session
				session.destroy();
			}

			@Override
			public void sessionCreated(Long value, SmppServerSession session, BaseBindResp response) throws SmppProcessingException
			{
				assertNotNull(session);
				// Notify that the session is ready
				session.serverReady(new com.cloudhopper.smpp.SmppSessionHandler()
				{

					@Override
					public String lookupTlvTagName(short arg0)
					{
						return null;
					}

					@Override
					public String lookupResultMessage(int arg0)
					{
						return null;
					}

					@Override
					public void fireUnrecoverablePduException(UnrecoverablePduException e)
					{
						fail("Unrecoverable exception was thrown: " + e.getLocalizedMessage());
					}

					@Override
					public void fireUnknownThrowable(Throwable e)
					{
					}

					@Override
					public void fireUnexpectedPduResponseReceived(PduResponse e)
					{
						fail("Unexpected Pdu response recieved: " + e.toString());
					}

					@Override
					public void fireRecoverablePduException(RecoverablePduException e)
					{
						fail(e.getLocalizedMessage());
					}

					@Override
					public PduResponse firePduRequestReceived(@SuppressWarnings("rawtypes") PduRequest pduRequest)
					{
						// Check if it is a sm that has been received
						if (SmppConstants.CMD_ID_DELIVER_SM == pduRequest.getCommandId())
						{
							// Get the sm
							DeliverSm sm = (DeliverSm) pduRequest;
							// Check fields are equal to the original message
							assertTrue(sm.getSourceAddress().getAddress().equals("9876543210"));
							assertTrue(sm.getDestAddress().getAddress().equals("0123456789"));
							assertTrue(new String(sm.getShortMessage()).equals("SMSTest Message"));
						}
						// Ensure that you can stop the test
						mustStop = true;
						// Return response
						return pduRequest.createResponse();
					}

					@Override
					public void firePduRequestExpired(@SuppressWarnings("rawtypes") PduRequest arg0)
					{
						fail("Pdu has expired.");
					}

					@Override
					public void fireExpectedPduResponseReceived(PduAsyncResponse arg0)
					{
					}

					@Override
					public void fireChannelUnexpectedlyClosed()
					{
						fail("Channel unexpectedly closed.");
					}
				});
			}

			@Override
			public void sessionBindRequested(Long value, SmppSessionConfiguration configuration, @SuppressWarnings("rawtypes") BaseBind baseBind) throws SmppProcessingException
			{
				// Change configuration name for the new session bind
				configuration.setName("Application.SMPP." + configuration.getSystemId());
			}

		}, executor, monitorExecutor);

		// Start the smsc
		server.start();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception
	{
		// Stop the smsc
		server.stop();
	}

	@Test
	public void testSendMessage()
	{

		// Create an executor for separate threads
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		// Created for asynchronous sending
		ScheduledThreadPoolExecutor monitor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory()
		{

			private int nextNum = 0;

			@Override
			public Thread newThread(Runnable runnable)
			{
				Thread thread = new Thread(runnable);
				thread.setName("C4USmppClientSession-" + ++nextNum);
				return thread;
			}

		});
		// Create the client
		client = new SmppClient(executor, 4, monitor);

		// Setup the location and system id and password.
		SmppSessionConfiguration config = new SmppSessionConfiguration();
		config.setName("esme 1");
		config.setType(SmppBindType.TRANSCEIVER);
		config.setHost("127.0.0.1");
		config.setPort(2776);
		config.setSystemId("hxc");
		config.setPassword("password");
		config.setBindTimeout(10000);
		config.getLoggingOptions().setLogBytes(true);
		config.setConnectTimeout(10000);
		config.setRequestExpiryTimeout(15000);
		config.setWindowMonitorInterval(15000);
		config.setWindowSize(1);
		config.setCountersEnabled(true);

		try
		{
			// Bind the client to a new session
			session = client.bind(config);
		}
		catch (SmppTimeoutException | SmppChannelException | UnrecoverablePduException | InterruptedException e)
		{
			fail("Failed to connect to the SMSC server. Error: " + e);
		}
		assertNotNull(session);

		EnquireLinkResp response = null;
		try
		{
			// Link the session to the smsc
			response = session.enquireLink(new EnquireLink(), 10000);
		}
		catch (RecoverablePduException | UnrecoverablePduException | SmppTimeoutException | SmppChannelException | InterruptedException e)
		{
			fail("Failed to enquire link. Error: " + e);
		}

		assertNotNull(response);
		assertTrue(response.getResultMessage().equals("OK"));

		// Create a basic message
		SubmitSm sm = new SubmitSm();
		sm.setSourceAddress(new Address((byte) 0x00, (byte) 0x00, "0123456789"));
		sm.setDestAddress(new Address((byte) 0x00, (byte) 0x00, "9876543210"));
		try
		{
			sm.setShortMessage(CharsetUtil.encode("SMSTest Message", CharsetUtil.CHARSET_GSM));
		}
		catch (SmppInvalidArgumentException e)
		{
			fail("Failed to set the shortmessage with encoding. Error: " + e);
		}

		try
		{
			// Send the sm to the smsc
			session.submit(sm, 1000);
		}
		catch (RecoverablePduException | UnrecoverablePduException | SmppTimeoutException | SmppChannelException | InterruptedException e)
		{
			fail("Failed to send the message. Error: " + e);
		}

		// Wait while the sm is being assessed
		while (!mustStop)
		{
		}

		// Finally unbind the session from the smsc
		session.unbind(10000);
	}

	@Test
	public void testSendDgramMessage()
	{

		// Create an executor for separate threads
		ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		// Created for asynchronous sending
		ScheduledThreadPoolExecutor monitor = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(1, new ThreadFactory()
		{

			private int nextNum = 0;

			@Override
			public Thread newThread(Runnable runnable)
			{
				Thread thread = new Thread(runnable);
				thread.setName("C4USmppClientSession4Dgram-" + ++nextNum);
				return thread;
			}

		});
		// Create the client
		client = new SmppClient(executor, 4, monitor);

		// Setup the location and system id and password.
		SmppSessionConfiguration config = new SmppSessionConfiguration();
		config.setName("esme 1");
		config.setType(SmppBindType.TRANSCEIVER);
		config.setHost("127.0.0.1");
		config.setPort(2776);
		config.setSystemId("hxc");
		config.setPassword("password");
		config.setBindTimeout(10000);
		config.getLoggingOptions().setLogBytes(true);
		config.setConnectTimeout(10000);
		config.setRequestExpiryTimeout(15000);
		config.setWindowMonitorInterval(15000);
		config.setWindowSize(1);
		config.setCountersEnabled(true);

		try
		{
			// Bind the client to a new session
			session = client.bind(config);
		}
		catch (SmppTimeoutException | SmppChannelException | UnrecoverablePduException | InterruptedException e)
		{
			fail("Failed to connect to the SMSC server. Error: " + e);
		}
		assertNotNull(session);

		EnquireLinkResp response = null;
		try
		{
			// Link the session to the smsc
			response = session.enquireLink(new EnquireLink(), 10000);
		}
		catch (RecoverablePduException | UnrecoverablePduException | SmppTimeoutException | SmppChannelException | InterruptedException e)
		{
			fail("Failed to enquire link. Error: " + e);
		}

		assertNotNull(response);
		assertTrue(response.getResultMessage().equals("OK"));

		// Create a basic message
		SubmitSm sm = new SubmitSm();
		sm.setSourceAddress(new Address((byte) 0x00, (byte) 0x00, "0123456789"));
		sm.setDestAddress(new Address((byte) 0x00, (byte) 0x00, "9876543210"));
    sm.setEsmClass( (byte)( ( (byte) sm.getEsmClass() & (byte) 0x01 ) ) ); 
    /*ESM_CLASS_MM_DATAGRAM      = 0x01;  */
		try
		{
			sm.setShortMessage(CharsetUtil.encode("SMSTest Message", CharsetUtil.CHARSET_GSM));
		}
		catch (SmppInvalidArgumentException e)
		{
			fail("Failed to set the shortmessage with encoding. Error: " + e);
		}

		try
		{
			// Send the sm to the smsc
			session.submit(sm, 1000);
		}
		catch (RecoverablePduException | UnrecoverablePduException | SmppTimeoutException | SmppChannelException | InterruptedException e)
		{
			fail("Failed to send the message. Error: " + e);
		}

		// Wait while the sm is being assessed
		while (!mustStop)
		{
		}

		// Finally unbind the session from the smsc
		session.unbind(10000);
	}

}
