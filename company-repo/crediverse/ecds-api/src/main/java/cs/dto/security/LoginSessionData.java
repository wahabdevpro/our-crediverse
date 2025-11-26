package cs.dto.security;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cs.dto.User;
import cs.dto.data.BaseRequest;
import cs.dto.security.AuthenticationData.AuthenticationState;
import hxc.ecds.protocol.rest.Session;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import za.co.concurrent.NetworkIdentifier.NetID;

@ToString
@Getter
public class LoginSessionData extends CommonProperties
{
	@Setter
	private String incomingIp;
	
	private Locale locale;
	
	private Date expiryTimeServer;
	
	private String companyPrefix;
	
	@Setter
	private User currentUser;
	
	@Setter
	private String mySessionId;
	
	@Setter
	private AuthenticationData sessionAuthentication;
	
	private Map<String, Object> transactionStore;
	
	@Setter
	public Integer agentId;
	
	@Setter
	public Integer webUserId;
	
	@Setter
	public Integer ownerAgentId;
	
	@Setter
	protected Integer agentUserID; //Horrible: uppercase ID while all other attributes are named Id
	
	@Setter
	protected String agentMsisdn;
	
	@Setter
	protected String agentUserMsisdn;
	
	
	public void setLocale(Locale local)
	{
		if (local != null)
		{
			this.locale = local;
		}
	}
	
	public <T>void addTransaction(String id, T transaction)
	{
		if (transactionStore == null) transactionStore = new ConcurrentHashMap<String, Object>();
		transactionStore.put(id, transaction);
	}
	
	public <T> T getTransaction(String id)
	{
		if (transactionStore != null)
		{
			return (T)transactionStore.get(id);
		}
		return null;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		sessionAuthentication.invalidate();
	}

	public synchronized void updateDetails(NetID result)
	{
		this.ipAddress = result.getIpAdress();
		this.hostName = result.getHostname();
		this.macAddress = result.getMacAddress();
	}
	
	public CommonProperties getCommonProperties()
	{
		return this;
	}

	public void captureData(BaseRequest req)
	{
		sessionAuthentication.captureData(req);
	}

	public void setCurrentState(AuthenticationState state)
	{
		sessionAuthentication.setCurrentState(state);
	}

	public AuthenticationState getCurrentState()
	{
		return sessionAuthentication.getCurrentState();
	}

	public byte[] getData1()
	{
		return sessionAuthentication.getData1();
	}

	public String getServerSessionID()
	{
		String sessionID = null;
		if (sessionAuthentication != null) sessionID = sessionAuthentication.getServerSessionID();
		return sessionID;
	}

	public void setServerSessionID(String sessionID)
	{
		if (sessionAuthentication != null)
			sessionAuthentication.setServerSessionID(sessionID);
	}

	public void setCurrentState(String newState)
	{
		sessionAuthentication.setCurrentState(newState);
	}

	public void setUsername(String username)
	{
		sessionAuthentication.setUsername(username);
	}

	public String getUsername()
	{
		return sessionAuthentication.getUsername();
	}

	public void setCurrentState(String newState, byte[] data1, byte[] data2)
	{
		sessionAuthentication.setCurrentState(newState, data1, data2);
	}

	public void updateSession(Session session)
	{
		this.expiryTimeServer = session.getExpiryTime();
		this.companyPrefix = session.getCompanyPrefix();
		
		// User could actually be both (no TS checks)
		this.setWebUserId(session.getWebUserID());
		this.setAgentId(session.getAgentID());
		this.setOwnerAgentId(session.getOwnerAgentID());
		this.setAgentUserID(session.getAgentUserID());
		if (this.companyPrefix == null) this.companyPrefix = "ci";
	}
	
	public boolean isOwnerAgent()
	{
		boolean isOwner = (agentId != null && ownerAgentId != null 
				&& (agentId == ownerAgentId));
		return isOwner;
	}
	
}
