package hxc.connectors.cai;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CaiConnection implements ICaiConnection 
{
	final static Logger logger = LoggerFactory.getLogger(CaiConnection.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	private CaiConnector connector;
	private Socket socket;
	private boolean isLoggedIn = false;
	private Date createDateTime = new Date();
	private long connectionTTL;
	boolean expireConnection;

	@Override
	public Socket getConnection()
	{
		return socket;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// IConnection Implementation
	//
	// /////////////////////////////////
	public CaiConnection(CaiConnector connector, String host, int port, int connectTimeout, int readTimeout, boolean expireConnection, long connectionTTL) throws IOException
	{
		this.connector = connector;
		//CaiConnector.CaiConfiguration config = connector.getConfiguration();
		try {
			logger.trace("Creating new connection to host:[{}] port:[{}] connection timeout:[{}], readTimeout:[{}]", host, port, connectTimeout, readTimeout);
			this.socket = new Socket();
			this.socket.connect(new InetSocketAddress(host, port), connectTimeout);
			this.socket.setSoTimeout(readTimeout);
			this.socket.setTcpNoDelay(false);
			logger.trace("Connection to host:[{}] port:[{}] created", host, port);
		} catch (IOException e) {			
			logger.error("Could not connect to host [{}] port [{}]. Exception [{}]", host, port, e.toString());
			throw e;
		}
		this.expireConnection = expireConnection;
		this.connectionTTL = connectionTTL;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Closeable Implementation
	//
	// /////////////////////////////////
	@Override
	public void close() throws IOException
	{
		try
		{
			connector.returnConnection(this);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			throw new IOException(e.getMessage(), e);
		}

	}
	
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fetch Cai Information
	//
	// /////////////////////////////////
	public String getImei(String msisdn) throws IOException
	{
		String imei = "";
		if(socket.isConnected() && isLoggedIn)
		{
			try {
				String request = buildImeiRequest(msisdn);
				CaiResponseParser responseParser = new CaiResponseParser();
				CaiResponse response = new CaiResponse();							
				InputStreamReader input = new InputStreamReader(socket.getInputStream());
				PeekBufferedReader reader = new PeekBufferedReader(input);
				PrintStream output = new PrintStream(socket.getOutputStream());
				// clear the junk (command prompt, etc) previously sent by the server.
				responseParser.clearStream(reader);
				logger.info("Requesting IMEI from CAI, request:[{}]", request);
				// send the request to the server.
 				output.print(request);
 				// parse the result into the response object
				responseParser.parseStream(reader, response);
				// log the result
				logger.info("CAI : getImei : Received response code from cai [{}:{}] content [{}]", response.getResponseCode(), response.getResponseDescription(), response.getContent());
				// do some sanity checks and obtain the imeisv value.
				if(response.getResponseCode().compareTo("0") == 0 && response.containsKey("IMEISV"))
				{
					imei = response.getReturnValue("IMEISV");					
				}
			} catch (IOException e) {
				logger.error("CAI : getImei : Socket exception when attempting to obtain the IMEISV value. {}", e.toString());
				throw e;				
		    }
		}
		return imei;
	}
	
	public boolean login(String username, String password) throws IOException
	{
		try {
			PrintStream output;
			InputStreamReader input;
			PeekBufferedReader reader;
			CaiResponseParser responseParser = new CaiResponseParser();
			CaiResponse response = new CaiResponse();
			String loginRequest = String.format("LOGIN:%s:%s;\r", username, password);
			if(socket.isConnected())
			{		
				output = new PrintStream(socket.getOutputStream());									
				input = new InputStreamReader(socket.getInputStream());
				reader = new PeekBufferedReader(input);
				// clear the junk (command prompt, etc) previously sent by the server.
				responseParser.clearStream(reader);
				logger.info("Logging into CAI service. Request:[{}]", loginRequest);
				// send the request to the server.
				output.print(loginRequest);
 				// parse the result into the response object
				responseParser.parseStream(reader, response);
				// login successful if RESP:0;
				isLoggedIn = response.getResponseCode().compareTo("0") == 0;
				logger.info("CAI : login : Received response code from cai [{}:{}]", response.getResponseCode(), response.getResponseDescription());
			} else {
				logger.error("CAI : login : Unexpected state of connection. Socket is closed.");
			}
		}
	    catch (IOException e) {
	    logger.error(e.getMessage(), e);
	       throw e;
	    }
		return isLoggedIn;
	}
	
	@Override
	public boolean isLoggedIn()
	{
		return isLoggedIn;
	}
	
	public String buildImeiRequest(String msisdn)
	{
		String request = new String();
		request = String.format("GET:HLRSUB:MSISDN,%s:IMEISV;\r", msisdn );
		return request;
	}
	
	public boolean isConnectionStale()
	{
		Date now = new Date();
		long connectionAge = (now.getTime() - createDateTime.getTime()) / 1000;
		return (expireConnection == true && connectionAge > connectionTTL);
	}
}
