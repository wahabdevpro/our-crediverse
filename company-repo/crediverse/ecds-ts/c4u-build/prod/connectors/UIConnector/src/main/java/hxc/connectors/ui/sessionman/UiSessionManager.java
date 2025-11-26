package hxc.connectors.ui.sessionman;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UiSessionManager
{
	final static Logger logger = LoggerFactory.getLogger(UiSessionManager.class);
	
	private Map<String, UiSession> sessionMap = null;

	private SimpleDateFormat sessionGenerator = new SimpleDateFormat("yyyMMddHHmmssSS");
	private long sessionTimeOut;

	// private float sessionTimeoutFactor = 1.2F;

	public UiSessionManager(long sessionTimeOut)
	{
		this.sessionTimeOut = sessionTimeOut;
	}

	/**
	 * @return the sessionTimeOut
	 */
	public long getSessionTimeOut()
	{
		return sessionTimeOut;
	}

	/**
	 * @param sessionTimeOut
	 *            the sessionTimeOut to set
	 */
	public void setSessionTimeOut(long sessionTimeOut)
	{
		this.sessionTimeOut = sessionTimeOut;
	}

	/**
	 * For successful login
	 * 
	 * @param user
	 *            IUser details from security manager
	 * @return
	 */
	public String addSession(String userId, byte[] credentials)
	{
		String sessionId = null;
		synchronized (this)
		{
			// Start session management if not running
			if (sessionMap == null)
			{
				sessionMap = new HashMap<String, UiSession>();
			}

			// Generate session id and store
			sessionId = sessionGenerator.format(new Date());
			while (sessionMap.containsKey(sessionId))
			{
				sessionId = sessionGenerator.format(new Date());
			}

			UiSession session = new UiSession(userId, credentials);
			logger.debug("SessionManagement userId {}, sessionId {}", userId, sessionId );
			sessionMap.put(sessionId, session);
		}
		return sessionId;
	}

	public void removeSession(String sessionId)
	{
		
		if (sessionId == null || sessionMap == null)
		{
			return;
		}
		synchronized (this)
		{
			logger.debug("SessionManagement: Removing session: {}, SessionMap: {}", sessionId, sessionMap.keySet());
			sessionMap.remove(sessionId);
			if (sessionMap.size() == 0)
			{
				sessionMap = null;
			}
		}
	}

	public boolean isValueSessionId(String sessionId)
	{
		synchronized (this)
		{
			if (sessionMap.containsKey(sessionId))
			{
				UiSession session = sessionMap.get(sessionId);
				long currentTime = (new Date()).getTime();
				if ((currentTime - session.getLastUpdate()) >= sessionTimeOut)
				{
					logger.debug("SessionManagement:isValueSessionId sessionId {}, currentTime {}, session.getLastUpdate() {},  sessionTimeOut {}, diff {}", sessionId, currentTime, session.getLastUpdate(), sessionTimeOut, currentTime- session.getLastUpdate() - sessionTimeOut);
					sessionMap.remove(sessionId);
					return false;
				}
				else
				{
					return true;
				}
	
			}
			else
			{
				return false;
			}
		}
	}

	public boolean isValidSession(String userId, String sessionId)
	{
		if (userId == null || sessionId == null || sessionMap == null)
			return false;

		synchronized (this)
		{
			if (isValueSessionId(sessionId))
			{
				UiSession session = sessionMap.get(sessionId);
				if (session.getUserId().equals(userId))
				{
					session.setLastUpdate((new Date()).getTime());
					return true;
				}
				else
				{
					return false;
				}
			}
			else
				return false;
		}
	}

	public boolean hasUserValidSession(String userId)
	{
		long currentTime = (new Date()).getTime();
		synchronized (this)
		{
			for (UiSession session : sessionMap.values())
			{
				if (session.getUserId().equals(userId) && ((currentTime - session.getLastUpdate()) < sessionTimeOut))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Validate User session and return user details
	 * 
	 * @param userId
	 * @param sessionId
	 * @return IUser if the session is still valid else return null
	 */
	public UiSession validateSession(String userId, String sessionId)
	{
		if (userId == null || sessionId == null || sessionMap == null)
		{
			logger.debug("SessionManagement: Either of the following is null; userId: {}, sessionId: {}, sessionMap: {}", 
					(userId == null?"null":"not null"),
					(sessionId == null?"null":"not null"), 
					(sessionMap == null?"null":"not null"));
			return null;
		}
		synchronized (this)
		{
			if (sessionMap.containsKey(sessionId))
			{
				UiSession session = sessionMap.get(sessionId);
				long currentTime = (new Date()).getTime();
				if ((currentTime - session.getLastUpdate()) >= sessionTimeOut)
				{
					sessionMap.remove(sessionId);
					logger.info("SessionManagement: User session for {} expired. currentTime: {}, session.getLastUpdate() {}, sessionTimeOut {}, diff {}", userId, currentTime, session.getLastUpdate(), sessionTimeOut, currentTime - session.getLastUpdate() - sessionTimeOut);
					return null;
				}
				else
				{
					if (session.getUserId().equalsIgnoreCase(userId))
					{
						session.setLastUpdate(currentTime);
						return session;
					}
					else
					{
						logger.debug("SessionManagement: UserId not equal; session.userId {}, userId {}", session.getUserId(), userId);
						return null;
					}
				}
			}
			else
			{
				logger.debug("SessionManagement: sessionId {} not in sessionMap" , sessionId);
				return null;
			}
		}
	}

	public int sessionSize()
	{
		if (sessionMap == null)
		{
			return 0;
		}
		else
		{
			synchronized (this)
			{
				return sessionMap.size();
			}
		}
	}

	public void revalidateAll()
	{
		long currentTime = (new Date()).getTime();
		synchronized (this)
		{
			Iterator<Entry<String, UiSession>> it = sessionMap.entrySet().iterator();
			while (it.hasNext())
			{
				Entry<String, UiSession> entry = it.next();
				long lastUpdate = ((UiSession) entry.getValue()).getLastUpdate();
				if ((currentTime - lastUpdate) >= sessionTimeOut)
				{
					logger.debug("SessionManagement:revalidateAll key {}, currentTime {}, lastUpdate {}, sessionTimeout {}; diff {}", entry.getKey(), currentTime, lastUpdate, sessionTimeOut, currentTime - lastUpdate - sessionTimeOut);
					it.remove();
				}
			}
		}
	}
}
