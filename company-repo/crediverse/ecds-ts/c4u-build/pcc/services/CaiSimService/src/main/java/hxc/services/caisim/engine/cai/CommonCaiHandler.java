package hxc.services.caisim.engine.cai;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.caisim.ICaiData;
import hxc.services.caisim.utils.QuotedTokenizer;
import hxc.utils.protocol.caisim.CaiCommon;
import hxc.utils.protocol.caisim.Protocol;
import hxc.utils.tcp.TcpRequest;

/**
 * A class to handle common CAI protocol features (login, basic request sanitation, etc. ).
 * 
 * @author petar
 *
 */
public class CommonCaiHandler extends BaseHandler
{
	final static Logger logger = LoggerFactory.getLogger(CommonCaiHandler.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	static final String SAPCSUB_CMD = "SAPCSUB";
	static final String ZAINSAPCSV_CMD = "ZAINSAPCSV";
	static final String SAPCUSAGE_CMD = "SAPCACCUMULATEDUSAGE";
	static final String HLRSUB_CMD = "HLRSUB";
	static final String DUMMY_CMD = "DUMMY";
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	public CommonCaiHandler(ICaiData caiData)
	{
		super(caiData);
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////
	
	/**
	 * Callback passed to TCPServer to handle a TCP request.
	 * 
	 * @param request the TCP request to handle
	 */
	public void handle(TcpRequest request)
	{
		try
		{
			// Log the request
			logger.debug("CAI Request: {}", request.getRequest());

			// Handle the request
			handleRequest(request);
		}
		catch (Exception e)
		{
			logger.error("CAI request failed", e);
		}
	}
	
	/**
	 * Executes basic CAI request handling before dispatching to more specific handlers.
	 * 
	 * @param request the TCP request to handle
	 * @throws IOException
	 */
	private void handleRequest(TcpRequest request) throws IOException
	{
		String requestStr = request.getRequest();
		
		if (requestStr.isEmpty())
			throw new IOException("Empty TCP Request");
		
		if (!requestStr.endsWith(String.valueOf(CaiCommon.COMMAND_DELIMITER)))
		{
			request.respond(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND));
			return;
		}
		
		// Remove the ending COMMAND_DELIMITER character
		requestStr = requestStr.substring(0, requestStr.length() - 1);
		
		String[] parsedRequest = QuotedTokenizer.tokenize(requestStr, CaiCommon.ATTRIBUTE_DELIMITER);
		if (parsedRequest.length == 0)
		{
			request.respond(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND));
			return;
		}
		
		if (!getCaiData().isAuthenticated(request.getSocket()))
		{
			if (parsedRequest.length < 3)
			{
				request.respond(getFailureResponse(Protocol.RESPONSE_CODE_REJECTION_MUST_LOGIN_FIRST));
				return;
			}
			
			String user = parsedRequest[1];
			String password = parsedRequest[2];
			
			if (!getCaiData().authenticate(user, password, request.getSocket()))
			{
				request.respond(getFailureResponse(Protocol.RESPONSE_CODE_UNSUCCESSFUL_LOGIN));
				return;
			}
			
			request.respond(getSuccessResponse());
			return;
		}
		
		// Handle non-CAI HLR MML requests separately
		String command = parsedRequest[0];
		if (command.equals(HlrHandler.HGPDI) || command.equals(HlrHandler.HGPDE))
		{
			request.respond(new HlrHandler(getCaiData()).handleMml(parsedRequest));
			return;
		}
		
		// Expect at least COMMAND:COMMAND_TYPE:MSISDN,MSISDN_VAL, e.g. SET:SAPCSUB:SUBID,12345
		if (parsedRequest.length < 3)
		{
			request.respond(getFailureResponse(Protocol.RESPONSE_CODE_INVALID_COMMAND));
			return;
		}
		
		String commandType = parsedRequest[1];
		
		// Before dispatching - check for injected responses
		Integer injectedResponse = getCaiData().handleInjectedResponse(command, commandType);
		if (injectedResponse != 0)
		{
			request.respond(getFailureResponse(injectedResponse));
			return;
		}

		// Dispatch CAI commands
		switch (commandType)
		{
			case SAPCSUB_CMD:
			case ZAINSAPCSV_CMD:
			case SAPCUSAGE_CMD:
				request.respond(new SapcHandler(getCaiData()).handle(parsedRequest));
				break;
			case HLRSUB_CMD:
				request.respond(new HlrHandler(getCaiData()).handleCai(parsedRequest));
				break;
			case DUMMY_CMD:
				request.respond(new DummyHandler(getCaiData()).handleCai(parsedRequest));
				break;
			default:
				request.respond(getFailureResponse(Protocol.RESPONSE_CODE_OPERATION_NOT_SUPPORTED));
		}
	}
}
