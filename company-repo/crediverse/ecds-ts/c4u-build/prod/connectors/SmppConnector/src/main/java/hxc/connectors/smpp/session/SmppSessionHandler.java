package hxc.connectors.smpp.session;

import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudhopper.smpp.PduAsyncResponse;
import com.cloudhopper.smpp.pdu.PduRequest;
import com.cloudhopper.smpp.pdu.PduResponse;
import com.cloudhopper.smpp.type.RecoverablePduException;
import com.cloudhopper.smpp.type.UnrecoverablePduException;

public class SmppSessionHandler implements com.cloudhopper.smpp.SmppSessionHandler
{
	final static Logger logger = LoggerFactory.getLogger(SmppSessionHandler.class);

	private String smscName = "Smpp Connector";

	public SmppSessionHandler(String smscName)
	{
		this.smscName = smscName;
	}

	@Override
	public String lookupResultMessage(int lookUpResultMessage)
	{
		return null;
	}

	@Override
	public String lookupTlvTagName(short lookUpTlvTagName)
	{
		return null;
	}

	@Override
	public void fireChannelUnexpectedlyClosed()
	{
		logger.warn("{} - Channel unexpectedly closed.", smscName);
	}

	@Override
	public void fireExpectedPduResponseReceived(PduAsyncResponse pduAsyncResponse)
	{
		logger.info("{} - Received expected response from SMSC asynchronously.", smscName);
	}

	@Override
	public void firePduRequestExpired(@SuppressWarnings("rawtypes") PduRequest pduRequest)
	{
		logger.warn("{} - Expired request was discarded.", smscName);
	}

	@Override
	public PduResponse firePduRequestReceived(@SuppressWarnings("rawtypes") PduRequest pduRequest)
	{
		PduResponse response = pduRequest.createResponse();

		logger.info("{} - Sending response back to origin of request.", smscName);

		return response;
	}

	@Override
	public void fireRecoverablePduException(RecoverablePduException exc)
	{
		logger.error("{} - Recoverable exception has been thrown: {} with Partial Pdu: {}", smscName, exc.getLocalizedMessage(), exc.getPartialPdu().toString());
	}

	@Override
	public void fireUnexpectedPduResponseReceived(PduResponse pduResponse)
	{
		logger.warn("{} - Unexpected response has been received.", smscName);
	}

	@Override
	public void fireUnknownThrowable(Throwable throwable)
	{
		if (throwable instanceof ClosedChannelException)
		{
			logger.error("{} - Channel closed.", smscName);
			fireChannelUnexpectedlyClosed();
		}
		else
		{
			logger.error("{} - Unknown exception has been thrown: {}", smscName, throwable.getLocalizedMessage());
		}
	}

	@Override
	public void fireUnrecoverablePduException(UnrecoverablePduException exc)
	{
		logger.error("{} - Unrecoverable exception has been thrown: {}", smscName, exc.getLocalizedMessage());
	}

}
