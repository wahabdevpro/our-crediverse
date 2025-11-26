package hxc.connectors.smpp.session;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudhopper.commons.util.PeriodFormatterUtil;
import com.cloudhopper.commons.util.windowing.DuplicateKeyException;
import com.cloudhopper.commons.util.windowing.OfferTimeoutException;
import com.cloudhopper.commons.util.windowing.Window;
import com.cloudhopper.commons.util.windowing.WindowFuture;
import com.cloudhopper.commons.util.windowing.WindowListener;
import com.cloudhopper.smpp.SmppBindType;
import com.cloudhopper.smpp.SmppConstants;
import com.cloudhopper.smpp.SmppServerSession;
import com.cloudhopper.smpp.SmppSessionConfiguration;
import com.cloudhopper.smpp.SmppSessionCounters;
import com.cloudhopper.smpp.impl.DefaultPduAsyncResponse;
import com.cloudhopper.smpp.impl.DefaultSmppServer;
import com.cloudhopper.smpp.impl.DefaultSmppSessionCounters;
import com.cloudhopper.smpp.impl.SmppSessionChannelListener;
import com.cloudhopper.smpp.pdu.BaseBind;
import com.cloudhopper.smpp.pdu.BaseBindResp;
import com.cloudhopper.smpp.pdu.EnquireLink;
import com.cloudhopper.smpp.pdu.EnquireLinkResp;
import com.cloudhopper.smpp.pdu.Pdu;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.pdu.SubmitSm;
import com.cloudhopper.smpp.pdu.SubmitSmResp;
import com.cloudhopper.smpp.pdu.Unbind;
import com.cloudhopper.smpp.tlv.Tlv;
import com.cloudhopper.smpp.tlv.TlvConvertException;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoder;
import com.cloudhopper.smpp.transcoder.DefaultPduTranscoderContext;
import com.cloudhopper.smpp.transcoder.PduTranscoder;
import com.cloudhopper.smpp.type.Address;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.SmppBindException;
import com.cloudhopper.smpp.type.SmppChannelException;
import com.cloudhopper.smpp.type.SmppTimeoutException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;
import com.cloudhopper.smpp.util.SequenceNumber;
import com.cloudhopper.smpp.util.SmppSessionUtil;
import com.cloudhopper.smpp.util.SmppUtil;

@SuppressWarnings("rawtypes")
public class SmppSession implements SmppServerSession, SmppSessionChannelListener, WindowListener<Integer, PduRequest, PduResponse>
{
	final static Logger logger = LoggerFactory.getLogger(SmppSession.class);

	// are we an "esme" or "smsc" session type?
	private final Type localType;
	// current state of this session
	private final AtomicInteger state;
	// the timestamp when we became "bound"
	private final AtomicLong boundTime;
	private final SmppSessionConfiguration configuration;
	private final Channel channel;
	private SmppSessionHandler sessionHandler;
	private final SequenceNumber sequenceNumber;
	private final PduTranscoder transcoder;
	private final Window<Integer, PduRequest, PduResponse> sendWindow;
	private byte interfaceVersion;
	// only for server sessions
	private DefaultSmppServer server;
	// the session id assigned by the server to this particular instance
	@SuppressWarnings("unused")
	private Long serverSessionId;
	// pre-prepared BindResponse to send back once we're flagged as ready
	private BaseBindResp preparedBindResponse;
	private ScheduledExecutorService monitorExecutor;
	private DefaultSmppSessionCounters counters;

	private Address addressRange;

	private boolean datagramMode = true;

	private boolean stale = false;

	public boolean getDatagramMode()
	{
		return datagramMode;
	}

	public synchronized void setDatagramMode(boolean mode)
	{
		datagramMode = mode;
	}

	public void setStale(boolean stale)
	{
		this.stale = stale;
	}

	public boolean getStale()
	{
		return this.stale;
	}

	/**
	 * Creates an SmppSession for a SMSC.
	 */
	public SmppSession(Type localType, SmppSessionConfiguration configuration, Channel channel, DefaultSmppServer server, Long serverSessionId, BaseBindResp preparedBindResponse,
			byte interfaceVersion, ScheduledExecutorService monitorExecutor)
	{
		this(localType, configuration, channel, (SmppSessionHandler) null, monitorExecutor);
		// default state for a server session is that it's binding
		this.state.set(STATE_BINDING);
		this.server = server;
		this.serverSessionId = serverSessionId;
		this.preparedBindResponse = preparedBindResponse;
		this.interfaceVersion = interfaceVersion;
		this.addressRange = configuration.getAddressRange();
	}

	/**
	 * Creates an SmppSession for a client-based session. This constructor will cause monitoring to be disabled.
	 * 
	 * @param localType
	 *            The type of local endpoint (ESME vs. SMSC)
	 * @param configuration
	 *            The session configuration
	 * @param channel
	 *            The channel associated with this session. The channel needs to already be opened.
	 * @param sessionHandler
	 *            The handler for session events
	 */
	public SmppSession(Type localType, SmppSessionConfiguration configuration, Channel channel, SmppSessionHandler sessionHandler)
	{
		this(localType, configuration, channel, sessionHandler, null);
	}

	/**
	 * Creates an SmppSession for a client-based session.
	 * 
	 * @param localType
	 *            The type of local endpoint (ESME vs. SMSC)
	 * @param configuration
	 *            The session configuration
	 * @param channel
	 *            The channel associated with this session. The channel needs to already be opened.
	 * @param sessionHandler
	 *            The handler for session events
	 * @param executor
	 *            The executor that window monitoring and potentially statistics will be periodically executed under. If null, monitoring will be disabled.
	 */
	public SmppSession(Type localType, SmppSessionConfiguration configuration, Channel channel, SmppSessionHandler sessionHandler, ScheduledExecutorService monitorExecutor)
	{
		this.localType = localType;
		this.state = new AtomicInteger(STATE_OPEN);
		this.configuration = configuration;
		this.channel = channel;
		this.boundTime = new AtomicLong(0);
		this.sessionHandler = (sessionHandler == null ? new SmppSessionHandler("Smpp Connector") : sessionHandler);
		this.sequenceNumber = new SequenceNumber();
		// always "wrap" the custom pdu transcoder context with a default one
		this.transcoder = new DefaultPduTranscoder(new DefaultPduTranscoderContext(this.sessionHandler));
		this.monitorExecutor = monitorExecutor;

		// different ways to construct the window if monitoring is enabled
		if (monitorExecutor != null && configuration.getWindowMonitorInterval() > 0)
		{
			// enable send window monitoring, verify if the monitoringInterval has been set
			this.sendWindow = new Window<Integer, PduRequest, PduResponse>(configuration.getWindowSize(), monitorExecutor, configuration.getWindowMonitorInterval(), this,
					configuration.getName() + ".Monitor");
		}
		else
		{
			this.sendWindow = new Window<Integer, PduRequest, PduResponse>(configuration.getWindowSize());
		}

		// these server-only items are null
		this.server = null;
		this.serverSessionId = null;
		this.preparedBindResponse = null;
		if (configuration.isCountersEnabled())
		{
			this.counters = new DefaultSmppSessionCounters();
		}
	}

	@Override
	public SmppBindType getBindType()
	{
		return this.configuration.getType();
	}

	@Override
	public Type getLocalType()
	{
		return this.localType;
	}

	@Override
	public Type getRemoteType()
	{
		if (this.localType == Type.CLIENT)
		{
			return Type.SERVER;
		}
		else
		{
			return Type.CLIENT;
		}
	}

	public void setBound()
	{
		this.state.set(STATE_BOUND);
		this.boundTime.set(System.currentTimeMillis());
	}

	public void setOpen()
	{
		this.state.set(STATE_OPEN);
	}

	@Override
	public long getBoundTime()
	{
		return this.boundTime.get();
	}

	@Override
	public String getStateName()
	{
		int s = this.state.get();
		if (s >= 0 || s < STATES.length)
		{
			return STATES[s];
		}
		else
		{
			return "UNKNOWN (" + s + ")";
		}
	}

	public synchronized void setAddressRange(Address addressRange)
	{
		this.addressRange = addressRange;
	}

	public Address getAddressRange()
	{
		return addressRange;
	}

	public synchronized void setInterfaceVersion(byte value)
	{
		this.interfaceVersion = value;
	}

	@Override
	public byte getInterfaceVersion()
	{
		return this.interfaceVersion;
	}

	@Override
	public boolean areOptionalParametersSupported()
	{
		return (this.interfaceVersion >= SmppConstants.VERSION_3_4);
	}

	@Override
	public boolean isOpen()
	{
		return (this.state.get() == STATE_OPEN);
	}

	@Override
	public boolean isBinding()
	{
		return (this.state.get() == STATE_BINDING);
	}

	@Override
	public boolean isBound()
	{
		return (this.state.get() == STATE_BOUND);
	}

	@Override
	public boolean isUnbinding()
	{
		return (this.state.get() == STATE_UNBINDING);
	}

	@Override
	public boolean isClosed()
	{
		return (this.state.get() == STATE_CLOSED);
	}

	@Override
	public SmppSessionConfiguration getConfiguration()
	{
		return this.configuration;
	}

	public Channel getChannel()
	{
		return this.channel;
	}

	public SequenceNumber getSequenceNumber()
	{
		return this.sequenceNumber;
	}

	public PduTranscoder getTranscoder()
	{
		return this.transcoder;
	}

	@Override
	public Window<Integer, PduRequest, PduResponse> getRequestWindow()
	{
		return getSendWindow();
	}

	@Override
	public Window<Integer, PduRequest, PduResponse> getSendWindow()
	{
		return this.sendWindow;
	}

	@Override
	public boolean hasCounters()
	{
		return (this.counters != null);
	}

	@Override
	public SmppSessionCounters getCounters()
	{
		return this.counters;
	}

	public SmppSessionHandler getHandler()
	{
		return sessionHandler;
	}

	public synchronized void serverReady(SmppSessionHandler sessionHandler)
	{
		// properly setup the session handler (to handle notifications)
		this.sessionHandler = sessionHandler;
		// send the prepared bind response
		try
		{
			this.sendResponsePdu(this.preparedBindResponse);
		}
		catch (Exception e)
		{
			logger.error("Could not send the response PDU.");
		}
		// flag the channel is ready to read
		this.channel.setReadable(true).awaitUninterruptibly();
		this.setBound();
	}

	public synchronized BaseBindResp bind(BaseBind request, long timeoutInMillis)
			throws RecoverablePduException, UnrecoverablePduException, SmppBindException, SmppTimeoutException, SmppChannelException, InterruptedException
	{
		assertValidRequest(request);
		boolean bound = false;
		try
		{
			this.state.set(STATE_BINDING);
			PduResponse response = sendRequestAndGetResponse(request, timeoutInMillis);
			logger.trace("Successfully recieved a response.");

			SmppSessionUtil.assertExpectedResponse(request, response);
			BaseBindResp bindResponse = (BaseBindResp) response;

			// check if the bind succeeded
			if (bindResponse == null || bindResponse.getCommandStatus() != SmppConstants.STATUS_OK)
			{
				if(bindResponse != null)
				{
					logger.error("Bind response command status is {}, which results in {}", bindResponse.getCommandStatus(),
								SmppConstants.STATUS_MESSAGE_MAP.get(bindResponse.getCommandStatus()));
				} else {
					logger.error("Bind response is null for bind request");
				}
				// bind failed for a specific reason
				throw new SmppBindException(bindResponse);
			}

			// if we make it all the way here, we're good and bound
			bound = true;

			//
			// negotiate version in use based on response back from server
			//
			Tlv scInterfaceVersion = bindResponse.getOptionalParameter(SmppConstants.TAG_SC_INTERFACE_VERSION);

			if (scInterfaceVersion == null)
			{
				// this means version 3.3 is in use
				this.interfaceVersion = SmppConstants.VERSION_3_3;
			}
			else
			{
				try
				{
					byte tempInterfaceVersion = scInterfaceVersion.getValueAsByte();
					if (tempInterfaceVersion >= SmppConstants.VERSION_3_4)
					{
						this.interfaceVersion = SmppConstants.VERSION_3_4;
					}
					else
					{
						this.interfaceVersion = SmppConstants.VERSION_3_3;
					}
				}
				catch (TlvConvertException e)
				{
					logger.warn("Unable to Convert Smpp Interface Version to a Byte Value: {}", e.getMessage());
					this.interfaceVersion = SmppConstants.VERSION_3_3;
				}
			}

			return bindResponse;
		}
		catch (Exception exc)
		{
			if (exc != null && exc instanceof RecoverablePduException && ((RecoverablePduException) exc).getPartialPdu() != null)
			{
				Pdu pdu = ((RecoverablePduException) exc).getPartialPdu();
				if (pdu.getCommandStatus() == SmppConstants.STATUS_ALYBND)
				{
					bound = true;
				}
			}
			throw exc;
		}
		finally
		{
			if (bound)
			{
				// this session is now successfully bound & ready for processing
				setBound();
			}
			else
			{
				// the bind failed, we need to clean up resources
				try
				{
					this.close();
				}
				catch (Exception e)
				{
				}
			}
		}
	}

	@Override
	public synchronized void unbind(long timeoutInMillis)
	{
		// is this channel still open?
		if (this.channel.isConnected())
		{
			this.state.set(STATE_UNBINDING);

			// try a "graceful" unbind by sending an "unbind" request
			try
			{
				sendRequestAndGetResponse(new Unbind(), timeoutInMillis);
			}
			catch (Exception e)
			{
				// not sure if an exception while attempting to unbind matters...
				// we are going to just print out a warning
				logger.warn("Did not cleanly receive an unbind response to our unbind request, safe to ignore: {}", e.getLocalizedMessage());
			}
		}
		else
		{
			logger.debug("Session channel is already closed, not going to unbind");
		}

		// always delegate the unbind to finish up with a "close"
		close(timeoutInMillis);
	}

	@Override
	public synchronized void close()
	{
		close(5000);
	}

	public synchronized void close(long timeoutInMillis)
	{
		if (channel.isConnected())
		{
			// temporarily set to "unbinding" for now
			this.state.set(STATE_UNBINDING);
			// make sure the channel is always closed
			if (channel.close().awaitUninterruptibly(timeoutInMillis))
			{
				logger.debug("Successfully closed channel.");
			}
			else
			{
				logger.warn("Unable to cleanly close channel.");
			}
		}
		this.state.set(STATE_CLOSED);
	}

	@Override
	public synchronized void destroy()
	{
		close();
		this.sendWindow.destroy();
		if (this.counters != null)
		{
			this.counters.reset();
		}
		// make sure to lose the reference to to the session handler - many
		// users of this class will probably pass themselves as the reference
		// and this may help to prevent a circular reference
		this.sessionHandler = null;
	}

	@Override
	public synchronized EnquireLinkResp enquireLink(EnquireLink request, long timeoutInMillis)
			throws RecoverablePduException, UnrecoverablePduException, SmppTimeoutException, SmppChannelException, InterruptedException
	{
		if(!isBound())
		{
			logger.error("Not enquiring. Session is not bound.");
			return null;
		}
		assertValidRequest(request);
		PduResponse response = sendRequestAndGetResponse(request, timeoutInMillis);
		SmppSessionUtil.assertExpectedResponse(request, response);
		return (EnquireLinkResp) response;
	}

	@Override
	public synchronized SubmitSmResp submit(SubmitSm request, long timeoutInMillis) throws RecoverablePduException, UnrecoverablePduException, SmppTimeoutException, SmppChannelException, InterruptedException
	{
		assertValidRequest(request);
		if (datagramMode)
			request.setEsmClass((byte) (((byte) request.getEsmClass() | SmppConstants.ESM_CLASS_MM_DATAGRAM)));
		PduResponse response = sendRequestAndGetResponse(request, timeoutInMillis);
		SmppSessionUtil.assertExpectedResponse(request, response);
		return (SubmitSmResp) response;
	}

	public void assertValidRequest(PduRequest request) throws NullPointerException, RecoverablePduException, UnrecoverablePduException
	{
		if (request == null)
		{
			throw new NullPointerException("PDU request cannot be null");
		}
	}

	/**
	 * Sends a PDU request and gets a PDU response that matches its sequence #. NOTE: This PDU response may not be the actual response the caller was expecting, it needs to verify it afterwards.
	 */
	public synchronized PduResponse sendRequestAndGetResponse(PduRequest requestPdu, long timeoutInMillis)
			throws RecoverablePduException, UnrecoverablePduException, SmppTimeoutException, SmppChannelException, InterruptedException
	{
		WindowFuture<Integer, PduRequest, PduResponse> future = sendRequestPdu(requestPdu, timeoutInMillis, true);
		boolean completedWithinTimeout = future.await();

		if (!completedWithinTimeout)
		{
			// since this is a "synchronous" request and it timed out, we don't
			// want it eating up valuable window space - cancel it before returning exception
			future.cancel();
			throw new SmppTimeoutException("Unable to get response within [" + timeoutInMillis + " ms]");
		}

		// 3 possible scenarios once completed: success, failure, or cancellation
		if (future.isSuccess())
		{
			return future.getResponse();
		}
		else if (future.getCause() != null)
		{
			Throwable cause = future.getCause();
			if (cause instanceof ClosedChannelException)
			{
				throw new SmppChannelException("Channel was closed after sending request, but before receiving response", cause);
			}
			else
			{
				throw new UnrecoverablePduException(cause.getMessage(), cause);
			}
		}
		else if (future.isCancelled())
		{
			throw new RecoverablePduException("Request was cancelled");
		}
		else
		{
			throw new UnrecoverablePduException("Unable to sendRequestAndGetResponse successfully (future was in strange state)");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized WindowFuture<Integer, PduRequest, PduResponse> sendRequestPdu(PduRequest pdu, long timeoutMillis, boolean synchronous)
			throws RecoverablePduException, UnrecoverablePduException, SmppTimeoutException, SmppChannelException, InterruptedException
	{
		if(!isBound() && !isBinding() && !isUnbinding())
		{
			throw new SmppChannelException("Session not bound/connected");
		}
		if (pdu == null)
			return null;

		// assign the next PDU sequence 
		pdu.setSequenceNumber(this.sequenceNumber.next());		

		if (logger != null)
		{
			logger.trace("Encoding PDU: {}", pdu.toString());
			if (pdu.getOptionalParameterCount() > 0 && pdu.getOptionalParameters() != null)
			{
				for (int i = 0; i < pdu.getOptionalParameterCount(); i++)
				{
					if (pdu.getOptionalParameters().get(i) == null)
						continue;
					logger.trace("Optional parameter [{}]: {}", i, pdu.getOptionalParameters().get(i).toString());
				}
			}
		}

		// encode the pdu into a buffer
		ChannelBuffer buffer = transcoder.encode(pdu);

		WindowFuture<Integer, PduRequest, PduResponse> future = null;
		try
		{
			future = sendWindow.offer(pdu.getSequenceNumber(), pdu, timeoutMillis, configuration.getRequestExpiryTimeout(), synchronous);
		}
		catch (DuplicateKeyException e)
		{
			throw new UnrecoverablePduException(e.getMessage(), e);
		}
		catch (OfferTimeoutException e)
		{
			throw new SmppTimeoutException(e.getMessage(), e);
		}

		// we need to log the PDU after encoding since some things only happen
		// during the encoding process such as looking up the result message
		if (configuration.getLoggingOptions().isLogPduEnabled())
		{
			if (synchronous)
			{
				logger.trace("Synchronously sending PDU: {}", pdu);
			}
			else
			{
				logger.trace("Asynchronously sending PDU: {}", pdu);
			}
		}

		// write the pdu out & wait timeout amount of time
		ChannelFuture channelFuture = this.channel.write(buffer);
		channelFuture.await(2000);

		// check if the write was a success
		if (!channelFuture.isSuccess())
		{
			// if (logger != null)
			// logger.error(this, "Write was not successful. Error: %s", channelFuture.getCause().getMessage());

			// the write failed, make sure to throw an exception
			throw new SmppChannelException(channelFuture.getCause().getMessage(), channelFuture.getCause());
		}

		this.countSendRequestPdu(pdu);

		return future;
	}

	/**
	 * Asynchronously sends a PDU and does not wait for a response PDU. This method will wait for the PDU to be written to the underlying channel.
	 * 
	 * @param pdu
	 *            The PDU to send (can be either a response or request)
	 * @throws RecoverablePduEncodingException
	 * @throws UnrecoverablePduEncodingException
	 * @throws SmppChannelException
	 * @throws InterruptedException
	 */
	@Override
	public synchronized void sendResponsePdu(PduResponse pdu) throws RecoverablePduException, UnrecoverablePduException, SmppChannelException, InterruptedException
	{
		// assign the next PDU sequence # if its not yet assigned
		if (!pdu.hasSequenceNumberAssigned())
		{
			pdu.setSequenceNumber(this.sequenceNumber.next());
		}

		// encode the pdu into a buffer
		ChannelBuffer buffer = transcoder.encode(pdu);

		// we need to log the PDU after encoding since some things only happen
		// during the encoding process such as looking up the result message
		if (configuration.getLoggingOptions().isLogPduEnabled())
		{
			logger.trace("Sending PDU: {}", pdu);
		}

		// write the pdu out & wait timeout amount of time
		ChannelFuture channelFuture = this.channel.write(buffer);
		channelFuture.await(2000);

		// check if the write was a success
		if (!channelFuture.isSuccess())
		{
			// the write failed, make sure to throw an exception
			throw new SmppChannelException(channelFuture.getCause().getMessage(), channelFuture.getCause());
		}
	}

	@Override
	public void firePduReceived(Pdu pdu)
	{
		if (configuration.getLoggingOptions().isLogPduEnabled())
		{
			logger.trace("Received PDU: {}", pdu);
		}

		if (pdu instanceof PduRequest)
		{
			// process this request and allow the handler to return a result
			PduRequest requestPdu = (PduRequest) pdu;

			this.countReceiveRequestPdu(requestPdu);

			long startTime = System.currentTimeMillis();
			PduResponse responsePdu = this.sessionHandler.firePduRequestReceived(requestPdu);

			// if the handler returned a non-null object, then we need to send it back on the channel
			if (responsePdu != null)
			{
				try
				{
					long responseTime = System.currentTimeMillis() - startTime;
					this.countSendResponsePdu(responsePdu, responseTime, responseTime);

					this.sendResponsePdu(responsePdu);
				}
				catch (Exception e)
				{
					logger.error("Unable to cleanly return response PDU: {}", e.getLocalizedMessage());
				}
			}
		}
		else
		{
			// this is a response -- we need to check if its "expected" or "unexpected"
			PduResponse responsePdu = (PduResponse) pdu;
			int receivedPduSeqNum = pdu.getSequenceNumber();

			try
			{
				// see if a correlating request exists in the window
				WindowFuture<Integer, PduRequest, PduResponse> future = this.sendWindow.complete(receivedPduSeqNum, responsePdu);
				if (future != null)
				{
					logger.trace("Found a future in the window for seqNum [{}]", receivedPduSeqNum + "");
					this.countReceiveResponsePdu(responsePdu, future.getOfferToAcceptTime(), future.getAcceptToDoneTime(), (future.getAcceptToDoneTime() / future.getWindowSize()));

					// if this isn't null, we found a match to a request
					int callerStateHint = future.getCallerStateHint();
					if (callerStateHint == WindowFuture.CALLER_WAITING)
					{
						logger.trace("Caller waiting for request: {}", future.getRequest());
						// if a caller is waiting, nothing extra needs done as calling thread will handle the response
						return;
					}
					else if (callerStateHint == WindowFuture.CALLER_NOT_WAITING)
					{
						logger.trace("Caller not waiting for request: {}", future.getRequest());
						// this was an "expected" response - wrap it into an async response
						this.sessionHandler.fireExpectedPduResponseReceived(new DefaultPduAsyncResponse(future));
						return;
					}
					else
					{
						logger.trace("Caller timed out waiting for request: {}", future.getRequest());
						// we send the request, but caller gave up on it awhile ago
						this.sessionHandler.fireUnexpectedPduResponseReceived(responsePdu);
					}
				}
				else
				{
					this.countReceiveResponsePdu(responsePdu, 0, 0, 0);

					// original request either expired OR was completely unexpected
					this.sessionHandler.fireUnexpectedPduResponseReceived(responsePdu);
				}
			}
			catch (InterruptedException e)
			{
				logger.warn("Interrupted while attempting to process response PDU and match it to a request via requesWindow: {}", e.getLocalizedMessage());
				// do nothing, continue processing
			}
		}
	}

	@Override
	public void fireExceptionThrown(Throwable t)
	{
		if (t instanceof UnrecoverablePduException)
		{
			this.sessionHandler.fireUnrecoverablePduException((UnrecoverablePduException) t);
		}
		else if (t instanceof RecoverablePduException)
		{
			this.sessionHandler.fireRecoverablePduException((RecoverablePduException) t);
		}
		else
		{
			// during testing under high load -- java.io.IOException: Connection reset by peer
			// let's check to see if this session was requested to be closed
			if (isUnbinding() || isClosed())
			{
				logger.debug("Unbind/close was requested, ignoring exception thrown.");
			}
			else
			{
				this.sessionHandler.fireUnknownThrowable(t);
			}
		}
	}

	@Override
	public void fireChannelClosed()
	{
		// if this is a server session, we need to notify the server first
		// NOTE: its important this happens first
		if (this.server != null)
		{
			this.server.destroy();
		}

		// most of the time when a channel is closed, we don't necessarily want
		// to do anything special -- however when a caller is waiting for a response
		// to a request and we know the channel closed, we should check for those
		// specific requests and make sure to cancel them
		if (this.sendWindow.getSize() > 0)
		{
			logger.trace("Channel closed and sendWindow has [{}] outstanding requests, some may need cancelled immediately", this.sendWindow.getSize() + "");
			Map<Integer, WindowFuture<Integer, PduRequest, PduResponse>> requests = this.sendWindow.createSortedSnapshot();
			Throwable cause = new ClosedChannelException();
			for (WindowFuture<Integer, PduRequest, PduResponse> future : requests.values())
			{
				// is the caller waiting?
				if (future.isCallerWaiting())
				{
					logger.debug("Caller waiting on request [{}], cancelling it with a channel closed exception", future.getKey() + "");
					try
					{
						future.fail(cause);
					}
					catch (Exception e)
					{
					}
				}
			}
		}

		// we need to check if this "unexpected" or "expected" based on whether
		// this session's unbind() or close() methods triggered a close request
		if (isUnbinding() || isClosed())
		{
			// do nothing -- ignore it
			logger.debug("Unbind/close was requested, ignoring channelClosed event");
		}
		else
		{
			this.sessionHandler.fireChannelUnexpectedlyClosed();
		}
	}

	@Override
	public void expired(WindowFuture<Integer, PduRequest, PduResponse> future)
	{
		this.countSendRequestPduExpired(future.getRequest());
		this.sessionHandler.firePduRequestExpired(future.getRequest());
	}

	private void countSendRequestPdu(PduRequest pdu)
	{
		if (this.counters == null)
		{
			return;
		}

		if (pdu.isRequest())
		{
			switch (pdu.getCommandId())
			{
				case SmppConstants.CMD_ID_SUBMIT_SM:
					this.counters.getTxSubmitSM().incrementRequestAndGet();
					break;
				case SmppConstants.CMD_ID_DELIVER_SM:
					this.counters.getTxDeliverSM().incrementRequestAndGet();
					break;
				case SmppConstants.CMD_ID_DATA_SM:
					this.counters.getTxDataSM().incrementRequestAndGet();
					break;
				case SmppConstants.CMD_ID_ENQUIRE_LINK:
					this.counters.getTxEnquireLink().incrementRequestAndGet();
					break;
			}
		}
	}

	private void countSendResponsePdu(PduResponse pdu, long responseTime, long estimatedProcessingTime)
	{
		if (this.counters == null)
		{
			return;
		}

		if (pdu.isResponse())
		{
			switch (pdu.getCommandId())
			{
				case SmppConstants.CMD_ID_SUBMIT_SM_RESP:
					this.counters.getRxSubmitSM().incrementResponseAndGet();
					this.counters.getRxSubmitSM().addRequestResponseTimeAndGet(responseTime);
					this.counters.getRxSubmitSM().addRequestEstimatedProcessingTimeAndGet(estimatedProcessingTime);
					this.counters.getRxSubmitSM().getResponseCommandStatusCounter().incrementAndGet(pdu.getCommandStatus());
					break;
				case SmppConstants.CMD_ID_DELIVER_SM_RESP:
					this.counters.getRxDeliverSM().incrementResponseAndGet();
					this.counters.getRxDeliverSM().addRequestResponseTimeAndGet(responseTime);
					this.counters.getRxDeliverSM().addRequestEstimatedProcessingTimeAndGet(estimatedProcessingTime);
					this.counters.getRxDeliverSM().getResponseCommandStatusCounter().incrementAndGet(pdu.getCommandStatus());
					break;
				case SmppConstants.CMD_ID_DATA_SM_RESP:
					this.counters.getRxDataSM().incrementResponseAndGet();
					this.counters.getRxDataSM().addRequestResponseTimeAndGet(responseTime);
					this.counters.getRxDataSM().addRequestEstimatedProcessingTimeAndGet(estimatedProcessingTime);
					this.counters.getRxDataSM().getResponseCommandStatusCounter().incrementAndGet(pdu.getCommandStatus());
					break;
				case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
					this.counters.getRxEnquireLink().incrementResponseAndGet();
					this.counters.getRxEnquireLink().addRequestResponseTimeAndGet(responseTime);
					this.counters.getRxEnquireLink().addRequestEstimatedProcessingTimeAndGet(estimatedProcessingTime);
					this.counters.getRxEnquireLink().getResponseCommandStatusCounter().incrementAndGet(pdu.getCommandStatus());
					break;
			}
		}
	}

	private void countSendRequestPduExpired(PduRequest pdu)
	{
		if (this.counters == null)
		{
			return; // noop
		}

		if (pdu.isRequest())
		{
			switch (pdu.getCommandId())
			{
				case SmppConstants.CMD_ID_SUBMIT_SM:
					this.counters.getTxSubmitSM().incrementRequestExpiredAndGet();
					break;
				case SmppConstants.CMD_ID_DELIVER_SM:
					this.counters.getTxDeliverSM().incrementRequestExpiredAndGet();
					break;
				case SmppConstants.CMD_ID_DATA_SM:
					this.counters.getTxDataSM().incrementRequestExpiredAndGet();
					break;
				case SmppConstants.CMD_ID_ENQUIRE_LINK:
					this.counters.getTxEnquireLink().incrementRequestExpiredAndGet();
					break;
			}
		}
	}

	private void countReceiveRequestPdu(PduRequest pdu)
	{
		if (this.counters == null)
		{
			return;
		}

		if (pdu.isRequest())
		{
			switch (pdu.getCommandId())
			{
				case SmppConstants.CMD_ID_SUBMIT_SM:
					this.counters.getRxSubmitSM().incrementRequestAndGet();
					break;
				case SmppConstants.CMD_ID_DELIVER_SM:
					this.counters.getRxDeliverSM().incrementRequestAndGet();
					break;
				case SmppConstants.CMD_ID_DATA_SM:
					this.counters.getRxDataSM().incrementRequestAndGet();
					break;
				case SmppConstants.CMD_ID_ENQUIRE_LINK:
					this.counters.getRxEnquireLink().incrementRequestAndGet();
					break;
			}
		}
	}

	private void countReceiveResponsePdu(PduResponse pdu, long waitTime, long responseTime, long estimatedProcessingTime)
	{
		if (this.counters == null)
		{
			return;
		}

		if (pdu.isResponse())
		{
			switch (pdu.getCommandId())
			{
				case SmppConstants.CMD_ID_SUBMIT_SM_RESP:
					this.counters.getTxSubmitSM().incrementResponseAndGet();
					this.counters.getTxSubmitSM().addRequestWaitTimeAndGet(waitTime);
					this.counters.getTxSubmitSM().addRequestResponseTimeAndGet(responseTime);
					this.counters.getTxSubmitSM().addRequestEstimatedProcessingTimeAndGet(estimatedProcessingTime);
					this.counters.getTxSubmitSM().getResponseCommandStatusCounter().incrementAndGet(pdu.getCommandStatus());
					break;
				case SmppConstants.CMD_ID_DELIVER_SM_RESP:
					this.counters.getTxDeliverSM().incrementResponseAndGet();
					this.counters.getTxDeliverSM().addRequestWaitTimeAndGet(waitTime);
					this.counters.getTxDeliverSM().addRequestResponseTimeAndGet(responseTime);
					this.counters.getTxDeliverSM().addRequestEstimatedProcessingTimeAndGet(estimatedProcessingTime);
					this.counters.getTxDeliverSM().getResponseCommandStatusCounter().incrementAndGet(pdu.getCommandStatus());
					break;
				case SmppConstants.CMD_ID_DATA_SM_RESP:
					this.counters.getTxDataSM().incrementResponseAndGet();
					this.counters.getTxDataSM().addRequestWaitTimeAndGet(waitTime);
					this.counters.getTxDataSM().addRequestResponseTimeAndGet(responseTime);
					this.counters.getTxDataSM().addRequestEstimatedProcessingTimeAndGet(estimatedProcessingTime);
					this.counters.getTxDataSM().getResponseCommandStatusCounter().incrementAndGet(pdu.getCommandStatus());
					break;
				case SmppConstants.CMD_ID_ENQUIRE_LINK_RESP:
					this.counters.getTxEnquireLink().incrementResponseAndGet();
					this.counters.getTxEnquireLink().addRequestWaitTimeAndGet(waitTime);
					this.counters.getTxEnquireLink().addRequestResponseTimeAndGet(responseTime);
					this.counters.getTxEnquireLink().addRequestEstimatedProcessingTimeAndGet(estimatedProcessingTime);
					this.counters.getTxEnquireLink().getResponseCommandStatusCounter().incrementAndGet(pdu.getCommandStatus());
					break;
			}
		}
	}

	public void resetCounters()
	{
		if (hasCounters())
		{
			this.counters.reset();
		}
	}

	public String getBindTypeName()
	{
		return this.getBindType().toString();
	}

	public String getBoundDuration()
	{
		return PeriodFormatterUtil.toLinuxUptimeStyleString(System.currentTimeMillis() - getBoundTime());
	}

	public String getInterfaceVersionName()
	{
		return SmppUtil.toInterfaceVersionString(interfaceVersion);
	}

	public String getLocalTypeName()
	{
		return this.getLocalType().toString();
	}

	public String getRemoteTypeName()
	{
		return this.getRemoteType().toString();
	}

	public int getNextSequenceNumber()
	{
		return this.sequenceNumber.peek();
	}

	public String getLocalAddressAndPort()
	{
		if (this.channel != null)
		{
			InetSocketAddress addr = (InetSocketAddress) this.channel.getLocalAddress();
			return addr.getAddress().getHostAddress() + ":" + addr.getPort();
		}
		else
		{
			return null;
		}
	}

	public String getRemoteAddressAndPort()
	{
		if (this.channel != null)
		{
			InetSocketAddress addr = (InetSocketAddress) this.channel.getRemoteAddress();
			return addr.getAddress().getHostAddress() + ":" + addr.getPort();
		}
		else
		{
			return null;
		}
	}

	public SocketAddress getAddress()
	{
		if (this.channel != null)
		{
			return this.channel.getRemoteAddress();
		}
		return null;
	}

	public String getName()
	{
		return this.configuration.getName();
	}

	public String getPassword()
	{
		return this.configuration.getPassword();
	}

	public long getRequestExpiryTimeout()
	{
		return this.configuration.getRequestExpiryTimeout();
	}

	public String getSystemId()
	{
		return this.configuration.getSystemId();
	}

	public String getSystemType()
	{
		return this.configuration.getSystemType();
	}

	public boolean isWindowMonitorEnabled()
	{
		return (this.monitorExecutor != null && this.configuration.getWindowMonitorInterval() > 0);
	}

	public long getWindowMonitorInterval()
	{
		return this.configuration.getWindowMonitorInterval();
	}

	public int getMaxWindowSize()
	{
		return this.sendWindow.getMaxSize();
	}

	public int getWindowSize()
	{
		return this.sendWindow.getSize();
	}

	public long getWindowWaitTimeout()
	{
		return this.configuration.getWindowWaitTimeout();
	}

	public String[] dumpWindow()
	{
		Map<Integer, WindowFuture<Integer, PduRequest, PduResponse>> sortedSnapshot = this.sendWindow.createSortedSnapshot();
		String[] dump = new String[sortedSnapshot.size()];
		int i = 0;
		for (WindowFuture<Integer, PduRequest, PduResponse> future : sortedSnapshot.values())
		{
			dump[i] = future.getRequest().toString();
			i++;
		}
		return dump;
	}

	public String getRxDataSMCounter()
	{
		return hasCounters() ? this.counters.getRxDataSM().toString() : null;
	}

	public String getRxDeliverSMCounter()
	{
		return hasCounters() ? this.counters.getRxDeliverSM().toString() : null;
	}

	public String getRxEnquireLinkCounter()
	{
		return hasCounters() ? this.counters.getRxEnquireLink().toString() : null;
	}

	public String getRxSubmitSMCounter()
	{
		return hasCounters() ? this.counters.getRxSubmitSM().toString() : null;
	}

	public String getTxDataSMCounter()
	{
		return hasCounters() ? this.counters.getTxDataSM().toString() : null;
	}

	public String getTxDeliverSMCounter()
	{
		return hasCounters() ? this.counters.getTxDeliverSM().toString() : null;
	}

	public String getTxEnquireLinkCounter()
	{
		return hasCounters() ? this.counters.getTxEnquireLink().toString() : null;
	}

	public String getTxSubmitSMCounter()
	{
		return hasCounters() ? this.counters.getTxSubmitSM().toString() : null;
	}

	public int getTxEnquireLink()
	{
		return hasCounters() ? this.counters.getTxEnquireLink().getRequest() : 0;
	}

	public int getTxSubmitSm()
	{
		return hasCounters() ? this.counters.getTxSubmitSM().getRequest() : 0;
	}

	public int getTxDeliverSm()
	{
		return hasCounters() ? this.counters.getTxDeliverSM().getRequest() : 0;
	}

	public int getTxDataSm()
	{
		return hasCounters() ? this.counters.getTxDataSM().getRequest() : 0;
	}

	public int getRxEnquireLink()
	{
		return hasCounters() ? this.counters.getRxEnquireLink().getResponse() : 0;
	}

	public int getRxSubmitSm()
	{
		return hasCounters() ? this.counters.getRxSubmitSM().getResponse() : 0;
	}

	public int getRxDeliverSm()
	{
		return hasCounters() ? this.counters.getRxDeliverSM().getResponse() : 0;
	}

	public int getRxDataSm()
	{
		return hasCounters() ? this.counters.getRxDataSM().getResponse() : 0;
	}

	public void enableLogBytes()
	{
		this.configuration.getLoggingOptions().setLogBytes(true);
	}

	public void disableLogBytes()
	{
		this.configuration.getLoggingOptions().setLogBytes(false);
	}

	public void enableLogPdu()
	{
		this.configuration.getLoggingOptions().setLogPdu(true);
	}

	public void disableLogPdu()
	{
		this.configuration.getLoggingOptions().setLogPdu(false);
	}

	@Override
	public void serverReady(com.cloudhopper.smpp.SmppSessionHandler sessionHandler)
	{
		return;
	}

}
