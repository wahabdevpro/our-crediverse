package hxc.connectors.ecdsapi;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import hxc.connectors.ecdsapi.model.JsonWebToken;

public class AccessTokenCache 
{
	static final long ONE_MINUTE_IN_MILLIS=60000;
	private ConcurrentHashMap<String, JsonWebToken> accessTokenMap = new ConcurrentHashMap<String, JsonWebToken>();
	
	public boolean isCached(String uri)
	{
		return accessTokenMap.containsKey(uri);
	}
	
	public JsonWebToken getCachedToken(String uri)
	{
		JsonWebToken jwt = null;
		if(accessTokenMap.containsKey(uri))
		{
			Calendar date = Calendar.getInstance();
			long t = date.getTimeInMillis();
			Date datePlusSome = new Date(t + (5 * ONE_MINUTE_IN_MILLIS));
			jwt = accessTokenMap.get(uri);
			if(jwt.getPayload().getExp().before(datePlusSome))
			{
				accessTokenMap.remove(uri);
			}
		}
		removeExpired();
		return jwt;
	}
	
	public void putToken(String uri, JsonWebToken jwt)
	{
		removeExpired();
		if(checkTokenValid(jwt, 5)) //Token should have some life remaining.
		{
			accessTokenMap.put(uri, jwt);
		}
	}
	
	private void removeExpired()
	{
		Date now = new Date();
		for(String uri : accessTokenMap.keySet())
		{
			JsonWebToken jwt = accessTokenMap.get(uri);
			if(jwt.getPayload().getExp().before(now))
			{
				accessTokenMap.remove(uri);
			}
		}
	}
	
	private boolean checkTokenValid(JsonWebToken jwt, int minutes)
	{
		Calendar date = Calendar.getInstance();
		long t = date.getTimeInMillis();
		Date datePlusSome = new Date(t + (minutes * ONE_MINUTE_IN_MILLIS));
		return jwt.getPayload().getExp().after(datePlusSome);
	}
}
