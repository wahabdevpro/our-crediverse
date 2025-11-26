package hxc.ui.cli.connector;

import java.util.List;

import hxc.utils.protocol.hux.HandleUSSDResponse;
import hxc.utils.protocol.uiconnector.common.Configurable;
import hxc.utils.protocol.uiconnector.common.IConfigurableParam;
import hxc.utils.protocol.uiconnector.ctrl.response.ComponentFitness;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerInfo;
import hxc.utils.protocol.uiconnector.ctrl.response.ServerRole;
import hxc.utils.protocol.uiconnector.response.ConfigurableMethod;
import hxc.utils.protocol.uiconnector.response.GetLocaleInformationResponse;

public interface ICLIConnector
{

	// UIConnector Methods
	public abstract String checkUserDetails(String username, String password, byte[] publicKey);

	public abstract boolean connectUIClient(String host, int port);

	public abstract boolean executeConfigMethod(ConfigurableMethod configMethod, Configurable config, String username, String sessionID);

	public abstract List<Configurable> getAllConfigurables(String username, String sessionID);

	public abstract ComponentFitness[] getServiceFitness(String username, String sessionID);

	public abstract ComponentFitness[] getConnectorFitness(String username, String sessionID);

	public abstract ServerRole[] getServerRoles(String username, String sessionID);

	public abstract boolean setServerRole(String username, String sessionID, ServerRole[] serverRole);

	public abstract ServerInfo[] getServerInfo(String username, String sessionID);

	public abstract boolean setServerInfo(String username, String sessionID, ServerInfo[] serverInfo);

	public abstract byte[] retrievePublicKey(String username);

	public abstract boolean updateConfiguration(Configurable updateConfig, IConfigurableParam updateParam, String username, String sessionID);

	public abstract String version(String userId, String sessionId);

	// HsX Connector Methods
	public abstract HandleUSSDResponse sendUSSD(String MSISDN, String Recipient, String USSDString, int sessionID, boolean response, boolean reply);

	// HuX Connector Methods
	public abstract String sendSMS(String username, String sessionId, String fromMSISDN, String toMSISDN, String message);

	public abstract String shutdownEsb(String userId, String sessionId);

	// Other Methods
	public abstract boolean removeSessionKey();

	public abstract boolean validateSession(String username, String sessionId);

	public abstract String revertService(String username, String sessionId, String serviceId);

	// Required for extracting and importing CSV
	public Configurable extractConfigurationContent(String username, String sessionId, long UID) throws Exception;

	public Configurable saveConfigurationStructure(String username, String sessionId, long UID, int version, String fieldName, List<IConfigurableParam[]> fieldValue, boolean saveToDB) throws Exception;

	public GetLocaleInformationResponse extractLocaleInformation(String username, String sessionId) throws Exception;
	
}
