package hxc.services.ecds;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.connectors.IInteraction;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.AgentUser;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.RuleCheckException;
import hxc.services.ecds.util.StatusCode;

public class Sessions implements Runnable
{
	final static Logger logger = LoggerFactory.getLogger(Sessions.class);
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	private static final long SESSION_TIMEOUT_MS = 30 * 60000L;
	private static final long SUPER_SESSION_TIMEOUT_MS = 30 * 60000L;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private static ConcurrentMap<String, Session> sessions = new ConcurrentHashMap<String, Session>();
	private static ConcurrentMap<String, Session> agentSessions = new ConcurrentHashMap<String, Session>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////

	public Session get(String sessionID) throws RuleCheckException
	{
		if (sessionID == null || sessionID.isEmpty())
			throw new RuleCheckException(StatusCode.UNAUTHORIZED, null, "Session %s not specified", sessionID);

		Session session = sessions.get(sessionID);

		if (session == null)
			throw new RuleCheckException(StatusCode.UNAUTHORIZED, null, "Session %s doesn't exist", sessionID);

		if (session != null)
		{
			// Test if Expired
			if (session.expired())
			{
				sessions.remove(session.getSessionID(), session);
				throw new RuleCheckException(StatusCode.UNAUTHORIZED, null, "Session %s expired", sessionID);
			}

			// Advance Expiry Time
			Date now = new Date();
			Date expiryDate = new Date(now.getTime() + SESSION_TIMEOUT_MS);
			session.setExpiryTime(expiryDate);
		}

		return session;
	}
	
	// Get an Active Agent Session
	public Session getActiveAgentSession(EntityManager em, ICreditDistribution context, IInteraction interaction)
	{
		String msisdn = interaction.getMSISDN();
		if (msisdn == null || msisdn.isEmpty())
			return null;
		msisdn = context.toMSISDN(msisdn);
		Session session = agentSessions.get(msisdn);
		if (session == null)
			return null;

		// Test if Expired
		if (!session.expired())
		{
			// Advance Expiry Time
			Date now = new Date();
			Date expiryDate = new Date(now.getTime() + SESSION_TIMEOUT_MS);
			session.setExpiryTime(expiryDate);
		}		
		//Refresh Agent and AgentUser from Hibernate, otherwise call to getTiers will result in:
		//LazyInitializationException: could not initialize proxy - no Session
		//Might be necessary to refresh other Hibernate entity instances?
		session.setAgent(Agent.findByID(em, session.getAgentID(), session.getCompanyID()));
		session.setAgentUser(Agent.findByID(em, session.getAgentID(), session.getCompanyID()));
		return session;
	}

	// Get Agent Session from IInteraction
	public Session getAgentSession(EntityManager em, ICreditDistribution context, int companyID, IInteraction interaction)
	{
		String channel;
		switch (interaction.getChannel())
		{
			case USSD:
				channel = Session.CHANNEL_USSD;
				break;

			case SMS:
				channel = Session.CHANNEL_SMS;
				break;

			default:
				channel = "?";
		}

		return getAgentSession(em, context, companyID, interaction.getMSISDN(), channel, interaction.getIMSI());
	}

	// Get Agent Session with PIN
	public Session getAgentSession(EntityManager em, ICreditDistribution context, int companyID, String msisdn, String channel, String imsi)
	{
		msisdn = context.toMSISDN(msisdn);

		Agent agent = Agent.findByMSISDN(em, msisdn, companyID);
		AgentUser agentUser = null;
		if (agent == null)
		{
			agentUser = AgentUser.findByMSISDN(em, msisdn, companyID);
			if (agentUser == null)
			{
				logger.info("Could not find Agent (User) ( msisdn = {}, companyID = {} )", msisdn, companyID);
				return null;
			}
			agent = agentUser.getAgent();
		}

		CompanyInfo companyInfo = context.findCompanyInfoByID(em, companyID);
		Session session = context.getSessions().getNew(companyInfo);

		int channels = agentUser == null ? agent.getAllowedChannels() : agentUser.getAllowedChannels();
		Boolean allowed = Session.checkAgentAllowedChannels(channel, channels);
		if (allowed == null)
		{
			logger.warn("Allowed for agent ( msisdn = {}, companyID = {} ) returned null ({})", msisdn, companyID, allowed);
			return null;
		}
		if (!allowed)
		{
			logger.info("Agent (msisdn = {}, companyID = {}) is not allowed for channel {}", msisdn, companyID, channel);
			return null;
		}

		session.withAgent(agent, agentUser) //
				.setChannel(channel) //
				.setState(Session.State.AUTHENTICATED) //
				.setMachineName("localhost");
		session.set("IMSI", imsi);
		agentSessions.put(msisdn, session);

		logger.trace("Created session ( companyID = {}, agentID = {}, msisdn = {}, channel = {}, countryID = {}, languageID = {} )", session.getCompanyID(), session.getAgentID(),
				session.getMobileNumber(), session.getChannel(), session.getCountryID(), session.getLanguageID());
		return session;
	}

	// Create a new Session
	public Session getNew(CompanyInfo companyInfo)
	{
		Session session = new Session(companyInfo);
		UUID uuid = UUID.randomUUID();
		session.setSessionID(uuid.toString().replace("-", ""));
		Date expiryDate = new Date(new Date().getTime() + SESSION_TIMEOUT_MS);
		session.setExpiryTime(expiryDate);
		sessions.put(session.getSessionID(), session);
		return session;
	}

	// Create a super Session (for MRD)
	public Session getSuperSession()
	{
		Session session = new Session();
		session.setWebUserID(Session.SUPER_SESSION_ID);
		session.setCompanyID(Session.SUPER_SESSION_ID);
		UUID uuid = UUID.randomUUID();
		session.setSessionID(uuid.toString().replace("-", ""));
		Date expiryDate = new Date(new Date().getTime() + SUPER_SESSION_TIMEOUT_MS);
		session.setExpiryTime(expiryDate);
		session.setCountryID("ZA");
		session.setLanguageID("en");
		session.setDomainAccountName("MRD");
		sessions.put(session.getSessionID(), session);
		return session;
	}

	// Delete an existing session
	public void delete(String sessionID)
	{
		sessions.remove(sessionID);
	}

	// Return List of Sessions
	public Session[] toArray()
	{
		Session[] result = sessions.values().toArray(new Session[sessions.size()]);
		return result;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Runnable
	//
	// /////////////////////////////////
	@Override
	public void run()
	{
		Set<String> keys = sessions.keySet();
		Date cutOff = new Date(new Date().getTime() - 2 * SESSION_TIMEOUT_MS);
		for (String key : keys)
		{
			Session session = sessions.get(key);
			if (session != null && session.getExpiryTime().before(cutOff))
			{
				sessions.remove(key);
			}
		}

		keys = agentSessions.keySet();
		for (String key : keys)
		{
			Session session = agentSessions.get(key);
			if (session != null && session.getExpiryTime().before(cutOff))
			{
				agentSessions.remove(key);
			}
		}

	}

}
