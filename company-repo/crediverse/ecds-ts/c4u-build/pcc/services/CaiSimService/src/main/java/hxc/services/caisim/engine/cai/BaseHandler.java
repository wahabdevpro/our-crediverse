package hxc.services.caisim.engine.cai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.services.caisim.ICaiData;
import hxc.utils.tcp.TcpResponse;

/**
 * A base class for CAI protocol handlers.
 * 
 * @author petar
 *
 */
public class BaseHandler
{	
	final static Logger logger = LoggerFactory.getLogger(BaseHandler.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	
	private final ICaiData caiData;
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	// /////////////////////////////////
	
	BaseHandler(ICaiData caiData)
	{
		this.caiData = caiData;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Accessor Method
	//
	// /////////////////////////////////
	
	public ICaiData getCaiData()
	{
		return caiData;
	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////

	/**
	 * Gets a CAI failure response with the specified failure code.
	 * 
	 * @param failureCode the failure code to use in the failure response
	 * @return a TCP response containing the specified CAI failure
	 */
	public static TcpResponse getFailureResponse(int failureCode)
	{
		return new TcpResponse("RESP:" + failureCode + ';');
	}
	
	/**
	 * Gets a successful CAI response.
	 * 
	 * @return a TCP response containing a successful CAI response without additional data
	 */
	public static TcpResponse getSuccessResponse()
	{
		return new TcpResponse("RESP:0;");
	}
	
	/**
	 * Gets a successful CAI response with additional data.
	 * 
	 * @param data the additional data for the successful CAI response
	 * @return a TCP response containing a successful CAI response with additional data
	 */
	public static TcpResponse getSuccessResponse(String data)
	{
		if ( data.isEmpty() )
			return new TcpResponse("RESP:0;");
		
		return new TcpResponse("RESP:0:" + data + ';');
	}
}
