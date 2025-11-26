package hxc.ui.cli.connector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;

import hxc.configuration.ValidationException;
import hxc.ui.cli.out.CLIError;
import hxc.ui.cli.out.CLIOutput;
import hxc.ui.cli.util.CLIUtil;
import hxc.utils.protocol.hux.HandleUSSDRequest;
import hxc.utils.protocol.hux.HandleUSSDRequestMembers;
import hxc.utils.protocol.hux.HandleUSSDResponse;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigServerRoleUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigurationRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.CtrlConfigurationUpdateRequest;
import hxc.utils.protocol.uiconnector.ctrl.request.FitnessRequest;
import hxc.utils.protocol.uiconnector.ctrl.response.ComponentFitness;
import hxc.utils.protocol.uiconnector.ctrl.response.CtrlConfigurationResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.FitnessResponse;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;
import hxc.utils.protocol.uiconnector.request.AuthenticateRequest;
import hxc.utils.protocol.uiconnector.request.CallConfigurableMethodRequest;
import hxc.utils.protocol.uiconnector.request.ConfigurableRequestParam;
import hxc.utils.protocol.uiconnector.request.ConfigurationUpdateRequest;
import hxc.utils.protocol.uiconnector.request.ESBShutdownRequest;
import hxc.utils.protocol.uiconnector.request.GetAllConfigurablesRequest;
import hxc.utils.protocol.uiconnector.request.GetConfigurableRequest;
import hxc.utils.protocol.uiconnector.request.GetLocaleInformationRequest;
import hxc.utils.protocol.uiconnector.request.PublicKeyRequest;
import hxc.utils.protocol.uiconnector.request.RevertServiceRequest;
import hxc.utils.protocol.uiconnector.request.SendSMSRequest;
import hxc.utils.protocol.uiconnector.request.SystemInfoRequest;
import hxc.utils.protocol.uiconnector.request.ValidateSessionRequest;
import hxc.utils.protocol.uiconnector.response.AuthenticateResponse;
import hxc.utils.protocol.uiconnector.response.CallConfigurableMethodResponse;
import hxc.utils.protocol.uiconnector.response.ConfigurableMethod;
import hxc.utils.protocol.uiconnector.response.ConfigurableResponseParam;
import hxc.utils.protocol.uiconnector.response.ConfigurationUpdateResponse;
import hxc.utils.protocol.uiconnector.response.ConfirmationResponse;
import hxc.utils.protocol.uiconnector.response.ESBShutdownResponse;
import hxc.utils.protocol.uiconnector.response.ErrorResponse;
import hxc.utils.protocol.uiconnector.response.GetAllConfigurablesResponse;
import hxc.utils.protocol.uiconnector.response.GetConfigurableResponse;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;
import hxc.utils.protocol.uiconnector.response.PublicKeyResponse;
import hxc.utils.protocol.uiconnector.response.RevertServiceResponse;
import hxc.utils.protocol.uiconnector.response.SendSMSResponse;
import hxc.utils.protocol.uiconnector.response.SystemInfoResponse;
import hxc.utils.protocol.uiconnector.response.UiBaseResponse;
import hxc.utils.protocol.uiconnector.userman.response.ValidSessionResponse;
import hxc.utils.uiconnector.client.UIClient;
import hxc.utils.xmlrpc.XmlRpcClient;
import hxc.utils.xmlrpc.XmlRpcConnection;

public class CLIConnector implements ICLIConnector
{

	private static final String sessionFile = "hxc_session";

	// XmlRpc components
	private XmlRpcClient xmlClient;

	// HsxConnector components
	private HandleUSSDRequest USSDRequest;
	private HandleUSSDResponse USSDResponse;
	private int USSDTransactionID = 0;

	// UIConnector components
	private UIClient uiClient;
	private UiBaseResponse response;

	// Constructor for the output stream
	public CLIConnector()
	{
	}

	// /////////////////////////////////////////////////////////////////////////////////////////
	//
	// UIConnector Methods
	//
	// /////////////////////////////////

	// Connect the client to the host and port
	@Override
	public boolean connectUIClient(String host, int port)
	{
		// Instantiate the client
		uiClient = new UIClient();

		try
		{
			// Connect the client
			uiClient.connect(host, port);
		}
		catch (IOException e)
		{
			return CLIError.raiseError(this, "Could not connect to the server.", e);
		}
		return true;
	}

	// Gets the public key from the server
	@Override
	public byte[] retrievePublicKey(String username)
	{
		// Checks if the client is connected
		if (uiClient == null)
		{
			return null;
		}

		// Stores the public key
		byte[] publicKey = null;

		// Requests for the public key
		PublicKeyRequest pkr = new PublicKeyRequest(username);
		
		// Retrieves the response for the public key
		PublicKeyResponse pr = null;
		response = null;
		try
		{
			// Send request
			response = uiClient.call(pkr, UiBaseResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			CLIError.raiseError(this, e.getLocalizedMessage(), e);
		}

		// If an error response print the message
		if (response instanceof ErrorResponse)
		{
			CLIError.raiseError(((ErrorResponse) response).getErrorCode().toString());
		}
		
		// Get the public key response
		pr = (PublicKeyResponse) response;
		
		// Store the public key
		publicKey = pr.getPublicKey();
		
		// Return the public key
		return publicKey;
	}

	// Writes the session to a file with the username
	private boolean writeSessionKey(String sessionID, String username)
	{
		// Create the file output stream
		try (FileOutputStream fo = new FileOutputStream("/tmp/" + sessionFile))
		{
			// Write the session to the file
			fo.write((new String(sessionID + "_" + username)).getBytes());
		}
		catch (Exception e)
		{
			return CLIError.raiseError(this, "Failed to write the session token.", e);
		}
		
		return true;
	}

	// Remove the session file
	@Override
	public boolean removeSessionKey()
	{
		// Create a reference to the file
		File f = new File("/tmp/" + sessionFile);
		
		// Check if it exists
		if (!f.exists())
		{
			return false;
		}
		
		// Delete the file
		return f.delete();
	}

	// Validate the current user session
	@Override
	public boolean validateSession(String username, String sessionId)
	{
		// Create the request
		ValidateSessionRequest request = new ValidateSessionRequest(username, sessionId);

		try
		{
			// Call the request
			response = uiClient.call(request, ValidSessionResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			return CLIError.raiseError("Error occurred: " + e.getMessage());
		}

		// Check if it is an error response
		if (response instanceof ErrorResponse)
		{
			return CLIError.raiseError("Invalid session. Exiting application.");
		}

		return true;
	}

	// Authenticates the user
	@Override
	public String checkUserDetails(String username, String password, byte[] publicKey)
	{
		String sessionID = null;
		
		// Get the authentication requests
		AuthenticateRequest authReq = new AuthenticateRequest(username);
		
		try
		{
			// Generate salted
			authReq.generateSalted(publicKey, password);
		}
		catch (NoSuchAlgorithmException e)
		{
			CLIError.raiseError(this, "Was not able to generate the salted.", e);
			return null;
		}

		response = null;

		try
		{
			// Call for the response
			response = uiClient.call(authReq, AuthenticateResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			CLIError.raiseError(this, e.getLocalizedMessage(), e);
			return null;
		}

		// Check if it is an error response
		if (response instanceof ErrorResponse)
		{
			CLIError.raiseError(((ErrorResponse) response).getError());
			return null;
		}

		// Check if it is an instance of the authenticate response and that it gives a valid session id
		if ((response instanceof AuthenticateResponse) && ((sessionID = response.getSessionId()) != null))
		{
			// Write the session to a file
			if (!writeSessionKey(sessionID, username))
			{
				return null;
			}
			
			// Return the session ID
			return sessionID;
		}

		return null;
	}

	// Retrieves all of the configurables
	@Override
	public List<Configurable> getAllConfigurables(String username, String sessionID)
	{
		// Reference to the list of configurables
		List<Configurable> configurations = null;
		
		// Get the request
		GetAllConfigurablesRequest allConfigRequests = new GetAllConfigurablesRequest(username, sessionID);
		response = null;

		try
		{
			// Call for the response
			response = uiClient.call(allConfigRequests, GetAllConfigurablesResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			CLIError.raiseError(this, "Could not get a valid response from the server.\nError: " + e.getLocalizedMessage(), e);
		}

		// Check if the response is an error response
		if (response instanceof ErrorResponse)
		{
			CLIError.raiseError(((ErrorResponse) response).getError());
		}

		// Check if the response is an instance of getallconfigurableresponse and that it gives out valid configs
		if ((response instanceof GetAllConfigurablesResponse) && ((configurations = ((GetAllConfigurablesResponse) response).getConfigs()) != null))
		{
			return configurations;
		}

		return null;
	}

	// Update the configuration
	@Override
	public boolean updateConfiguration(Configurable updateConfig, IConfigurableParam updateParam, String username, String sessionID)
	{
		// Get the parameter to update
		ConfigurableResponseParam cnfRParam = (ConfigurableResponseParam) updateParam;
		
		// Ensure it is a valid configurable and is not read only
		if (cnfRParam != null && cnfRParam.isReadOnly())
		{
			CLIOutput.println("This field is read only.");
		}
		
		// Get the request
		ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(username, sessionID);
		
		// Set the updated configuration to the request
		configUpdateRequest.setName(updateConfig.getName());
		configUpdateRequest.setPath(updateConfig.getPath());
		configUpdateRequest.setParams(new IConfigurableParam[1]);
		configUpdateRequest.getParams()[0] = new ConfigurableRequestParam(updateParam.getFieldName(), updateParam.getValue());
		configUpdateRequest.setVersion(updateConfig.getVersion() + 1);
		configUpdateRequest.setConfigurableSerialVersionUID(updateConfig.getConfigSerialVersionUID());
		
		// Retrieve the update config response
		ConfigurationUpdateResponse configUpdateResponse = null;
		try
		{
			// Call for the response
			response = uiClient.call(configUpdateRequest, ConfigurationUpdateResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			return CLIError.raiseError(this, e.getLocalizedMessage(), e);
		}
		
		// Check if it is an instance of error response
		if (response instanceof ErrorResponse)
		{
			return CLIError.raiseError(((ErrorResponse) response).getError());
		}
		
		configUpdateResponse = (ConfigurationUpdateResponse) response;
		
		// Check that the config update was successful
		if (configUpdateResponse == null)
		{
			return false;
		}
		
		return true;
	}

	// Executes a method in the backend
	@Override
	public boolean executeConfigMethod(ConfigurableMethod configMethod, Configurable config, String username, String sessionID)
	{
		// Create the request
		CallConfigurableMethodRequest methodRequest = new CallConfigurableMethodRequest(username, sessionID);

		// Set the parameters
		methodRequest.setConfigName(config.getName());
		methodRequest.setConfigPath(config.getPath());
		methodRequest.setMethod(configMethod.getMethodName());

		response = null;
		try
		{
			// Send the request
			response = uiClient.call(methodRequest, UiBaseResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			return CLIError.raiseError(this, e.getLocalizedMessage(), e);
		}

		// Check if it is an instance of an error response
		if (response instanceof ErrorResponse)
		{
			CLIError.raiseError(((ErrorResponse) response).getError());
		}
		else
		{
			// Else create the response
			CallConfigurableMethodResponse methodResponse = (CallConfigurableMethodResponse) response;
			
			// Print the method message
			CLIOutput.println(methodResponse.getMethodCallResponse());
			
			return true;
		}
		
		return false;
	}

	// Gets the version from the server
	@Override
	public String version(String userId, String sessionId)
	{
		String version = null;

		// Create the version request
		SystemInfoRequest request = new SystemInfoRequest(null, null);

		response = null;
		try
		{
			// Send the request
			response = uiClient.call(request, UiBaseResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			CLIError.raiseError(this, e.getLocalizedMessage(), e);
			return "No version number could be found from current version.";
		}

		// Check if the response is an error response
		if (response instanceof ErrorResponse)
		{
			CLIError.raiseError(((ErrorResponse) response).getError());
			return "Error occurred while trying to retrieve the version number.";
		}
		
		// Get the version from the response
		version = ((SystemInfoResponse) response).getVersion();
		return version;
	}

	// Gets the fitness components for the services
	@Override
	public ComponentFitness[] getServiceFitness(String username, String sessionId)
	{
		// Create the request
		FitnessRequest request = new FitnessRequest(username, sessionId);

		response = null;
		try
		{
			// Send the request
			response = uiClient.call(request, UiBaseResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			CLIError.raiseError(this, e.getLocalizedMessage(), e);
			return null;
		}

		// Check if it is an error response
		if (response instanceof ErrorResponse)
		{
			CLIError.raiseError(((ErrorResponse) response).getError());
			return null;
		}
		
		// Return the services
		return ((FitnessResponse) response).getServices();
	}

	// Gets the fitness components for the connectors
	@Override
	public ComponentFitness[] getConnectorFitness(String username, String sessionId)
	{
		// Create the request
		FitnessRequest request = new FitnessRequest(username, sessionId);

		response = null;
		try
		{
			// Sends the request
			response = uiClient.call(request, UiBaseResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			CLIError.raiseError(this, e.getLocalizedMessage(), e);
			return null;
		}

		// Check if the response is an error
		if (response instanceof ErrorResponse)
		{
			CLIError.raiseError(((ErrorResponse) response).getError());
			return null;
		}
		
		// Return the response components
		return ((FitnessResponse) response).getComponents();
	}

	private int serverRoleVersionNumber = -1;
	private int serverInfoVersionNumber = -1;

	// Get the server roles from the server
	@Override
	public ServerRole[] getServerRoles(String username, String sessionID)
	{
		// Create the request
		CtrlConfigurationRequest request = new CtrlConfigurationRequest(username, sessionID);

		response = null;
		try
		{
			// Send the request
			response = uiClient.call(request, UiBaseResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			CLIError.raiseError(this, e.getLocalizedMessage(), e);
			return null;
		}

		// Check if the response is an error response
		if (response instanceof ErrorResponse)
		{
			CLIError.raiseError(((ErrorResponse) response).getError());
			return null;
		}
		
		// Set the last server role version
		serverRoleVersionNumber = ((CtrlConfigurationResponse) response).getVersionNumber();
		
		// Return the server role list
		return ((CtrlConfigurationResponse) response).getServerRoleList();
	}

	// Sets the server roles for the server
	@Override
	public boolean setServerRole(String username, String sessionID, ServerRole[] serverRole)
	{
		// Create the request
		CtrlConfigServerRoleUpdateRequest request = new CtrlConfigServerRoleUpdateRequest(username, sessionID);
		
		// Set the properties of the request
		request.setServerRoleList(serverRole);
		request.setVersionNumber(++serverRoleVersionNumber);
		
		response = null;
		try
		{
			// Send the request
			response = uiClient.call(request, UiBaseResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			return CLIError.raiseError(this, e.getLocalizedMessage(), e);
		}

		// Check if the response is an error response
		if (response instanceof ErrorResponse)
		{
			return CLIError.raiseError(((ErrorResponse) response).getError());
		}

		// Check if the response is a confirmation response
		if (response instanceof ConfirmationResponse)
		{
			return true;
		}

		return false;
	}

	// Gets the server info from the server
	@Override
	public ServerInfo[] getServerInfo(String username, String sessionID)
	{
		// Create the request
		CtrlConfigurationRequest request = new CtrlConfigurationRequest(username, sessionID);

		response = null;
		try
		{
			// Send the request
			response = uiClient.call(request, UiBaseResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			CLIError.raiseError(this, e.getLocalizedMessage(), e);
			return null;
		}

		// Check if the response is an error response
		if (response instanceof ErrorResponse)
		{
			CLIError.raiseError(((ErrorResponse) response).getError());
			return null;
		}
		
		// Set the server info version
		serverInfoVersionNumber = ((CtrlConfigurationResponse) response).getVersionNumber();
		
		// Return the server info
		ServerInfo[] serverInfo = ((CtrlConfigurationResponse) response).getServerList();
		return serverInfo;
	}

	// Sets the server info to the server
	@Override
	public boolean setServerInfo(String username, String sessionID, ServerInfo[] serverInfo)
	{
		// Create the request
		CtrlConfigurationUpdateRequest request = new CtrlConfigurationUpdateRequest(username, sessionID);

		// Set the properties
		request.setServerInfoList(serverInfo);
		request.setServerRoleList(getServerRoles(username, sessionID));
		request.setVersionNumber(++serverInfoVersionNumber);
		
		response = null;
		try
		{
			// Send the request
			response = uiClient.call(request, UiBaseResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{
			CLIError.raiseError(this, e.getLocalizedMessage(), e);
			return false;
		}

		// Check if the response is an error response
		if (response instanceof ErrorResponse)
		{
			return CLIError.raiseError(((ErrorResponse) response).getError());
		}

		// Check if the response is a confirmation response
		if (response instanceof ConfirmationResponse)
		{
			return true;
		}
		return false;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////
	//
	// HuxConnector Methods
	//
	// /////////////////////////////////

	private String originalTransID;

	// Sends the USSD to the server
	@Override
	public HandleUSSDResponse sendUSSD(String MSISDN, String Recipient, String USSDString, int sessionID, boolean response, boolean reply)
	{
		// Check if it is a reply or not
		if (!reply)
		{
			// Create the ussd request
			USSDRequest = new HandleUSSDRequest();
			
			// Set the properties
			USSDRequest.members = new HandleUSSDRequestMembers();
			USSDRequest.members.MSISDN = MSISDN;
			USSDRequest.members.response = response;
			USSDRequest.members.SessionId = sessionID;
			USSDRequest.members.TransactionTime = new Date();
			USSDRequest.members.TransactionId = getUSSDTransactionID();
			originalTransID = USSDRequest.members.TransactionId;
			try
			{
				// Set the ussd information
				USSDRequest.members.USSDServiceCode = CLIUtil.extractServiceCode(USSDString);
				USSDRequest.members.USSDRequestString = CLIUtil.extractUSSDRequestString(USSDString);
			}
			catch (Exception exc)
			{
				System.out.println("Please use format: *XXX*XXXXX...#");
			}
		}
		else
		{
			// Check if the request is not null
			if (USSDRequest == null)
			{
				return null;
			}
			
			// Set the properties of the ussd request
			USSDRequest.members.USSDRequestString = USSDString;
			USSDRequest.members.response = response;
			USSDRequest.members.SessionId = (sessionID < 0) ? Integer.parseInt(originalTransID) : sessionID;
			USSDRequest.members.TransactionId = getUSSDTransactionID();
		}

		// Get the port
		String port = CLIUtil.hasPort(Recipient, 14000);
		
		// Create the client
		xmlClient = new XmlRpcClient("http://" + Recipient + port + "/RPC2");
		USSDResponse = null;
		
		// Create the connection
		try (XmlRpcConnection connection = xmlClient.getConnection())
		{
			// Send the request
			USSDResponse = connection.call(USSDRequest, HandleUSSDResponse.class);
			
			// Return the response
			return USSDResponse;
		}
		catch (Exception e)
		{
			CLIError.raiseError(this, e.getLocalizedMessage(), e);
			return null;
		}
	}

	// Gets the transaction ID
	private String getUSSDTransactionID()
	{
		return String.format("%d", ++USSDTransactionID);
	}

	// /////////////////////////////////////////////////////////////////////////////////////////
	//
	// HuxConnector Methods
	//
	// /////////////////////////////////

	// Sends an SM to the backend
	@Override
	public String sendSMS(String username, String sessionId, String fromMSISDN, String toMSISDN, String message)
	{
		// Create the request
		SendSMSRequest request = new SendSMSRequest(username, sessionId);
		
		// Set the properties
		request.setFromMSISDN(fromMSISDN);
		request.setToMSISDN(toMSISDN);
		request.setMessage(message);

		response = null;
		try
		{
			// Send the request
			response = uiClient.call(request, SendSMSResponse.class);
		}
		catch (Exception exc)
		{
			CLIError.raiseError("Could not send sms request. Error: " + exc.getMessage());
		}

		// Check the response is valid
		if (!(response instanceof ErrorResponse) && response instanceof SendSMSResponse)
		{
			// Return the sms response string
			return ((SendSMSResponse) response).getResponse();
		}

		return null;
	}

	// /////////////////////////////////////////////////////////////////////////////////////////
	//
	// ESB Methods
	//
	// /////////////////////////////////

	// Shuts down the esb
	@Override
	public String shutdownEsb(String userId, String sessionId)
	{
		// Create the request
		ESBShutdownRequest request = new ESBShutdownRequest(userId, sessionId);
		
		try
		{
			// Send the request
			response = uiClient.call(request, ESBShutdownResponse.class);
		}
		catch (ClassNotFoundException | IOException e)
		{

		}
		
		// Print the process ID
		CLIOutput.println("Process id is: " + ((ESBShutdownResponse) response).getPID());
		return "Not yet!";
	}

	// Reverts a service
	@Override
	public String revertService(String username, String sessionId, String serviceId)
	{
		// Create the request
		RevertServiceRequest request = new RevertServiceRequest(username, sessionId);
		
		// Set the properties
		request.setServiceID(serviceId);

		try
		{
			// Send the request
			response = uiClient.call(request, RevertServiceResponse.class);
		}
		catch (ClassNotFoundException | IOException exc)
		{
			return "Could not revert service: " + exc.getMessage();
		}

		// Ensure the response is not null
		if (response == null)
			return "Failed to receive valid response. Try again later.";

		// Ensure the response is not an error response
		if (response instanceof ErrorResponse)
		{
			return ((ErrorResponse) response).getError();
		}

		// Return the response message
		return ((RevertServiceResponse) response).getResponse();
	}
	
	@Override
	public GetLocaleInformationResponse extractLocaleInformation(String username, String sessionId) throws Exception
	{
		GetLocaleInformationResponse localeResponse = null;
		try
		{
			GetLocaleInformationRequest request = new GetLocaleInformationRequest(username, sessionId);
			UiBaseResponse response = uiClient.call(request, UiBaseResponse.class);
			if (response instanceof ErrorResponse)
			{
				throw new Exception(((ErrorResponse) response).getError());
			}
			else
			{
				localeResponse = (GetLocaleInformationResponse) response;
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		return localeResponse;
	}
	
	/**
	 * This is more detailed than normal configuration content
	 */
	@Override
	public Configurable extractConfigurationContent(String username, String sessionId, long UID) throws Exception
	{
		Configurable content = null;
		try
		{
			GetConfigurableRequest request = new GetConfigurableRequest(username, sessionId);
			request.setConfigurableSerialVersionID(UID);
			UiBaseResponse response = uiClient.call(request, UiBaseResponse.class);
			if (response instanceof ErrorResponse)
			{
				throw new Exception(((ErrorResponse) response).getError());
			}
			else
			{
				GetConfigurableResponse gcr = (GetConfigurableResponse) response;
				content = gcr.getConfig();
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		return content;
	}

	/**
	 * Save detailed configuration (Used to implement import of CSV
	 */	
	@Override
	public Configurable saveConfigurationStructure(String username, String sessionId, long UID, int version, 
			String fieldName, List<IConfigurableParam[]> fieldValue, boolean saveToDB) throws Exception
	{
		ConfigurationUpdateRequest configUpdateRequest = new ConfigurationUpdateRequest(username, sessionId);
		configUpdateRequest.setConfigurableSerialVersionUID(UID);
		configUpdateRequest.setVersion(version);
		configUpdateRequest.setSaveToDB(saveToDB);

		ConfigurableRequestParam[] parms = new ConfigurableRequestParam[1];
		parms[0] = new ConfigurableRequestParam(fieldName, fieldValue);
		configUpdateRequest.setParams(parms);

		UiBaseResponse baseResponse = uiClient.call(configUpdateRequest, UiBaseResponse.class);
		if (baseResponse instanceof ErrorResponse)
		{
			ErrorResponse err = (ErrorResponse) baseResponse;
			throw ValidationException.createFieldValidationException(err.getField(), err.getError());
		}
		else if (baseResponse instanceof ConfigurationUpdateResponse)
		{
			ConfigurationUpdateResponse configResponse = (ConfigurationUpdateResponse) baseResponse;
			return (configResponse.getConfig());
		}

		return null;
	}	

}
