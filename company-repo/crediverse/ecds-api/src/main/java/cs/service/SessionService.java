package cs.service;

import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import cs.config.RestServerConfiguration;
import cs.constants.ApplicationConstants;
import cs.dto.security.LoginSessionData;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.Session;

@Service
public class SessionService
{
	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;
	
	@Autowired
	private LoginSessionData sessionData;

	private String restServerUrl;
	
	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getSessionurl();
	}
	
	public Session getWorkItemSession(UUID uuid) throws Exception
	{
		Session result = null;
		String uri = restServerUrl+"/work_item/"+uuid;
		result = restTemplate.execute(uri, HttpMethod.GET, Session.class);
		return result;
	}
	
	public Session getWorkItemSession(String uuid) throws Exception
	{
		return getWorkItemSession(UUID.fromString(uuid));
	}
	
	public boolean isSessionValid(String sessionId)
	{
		boolean result = false;
		Session session = null;
		int retry = 0;
		Date now = new Date();
		while (retry <= ApplicationConstants.CONST_MAX_RETRY)
		{
			try
			{
				session = restTemplate.execute(restServerUrl+"/"+sessionId, HttpMethod.GET, Session.class);
				if (session != null)
				{
					Date serverExpiry = session.getExpiryTime();
					if (serverExpiry != null && serverExpiry.compareTo(now) > 0) {
						result = true;
					}
					break;
				}
			}
			catch(Throwable ex){
				retry++;
			}
		}
		return result;
	}
	
	public boolean isSessionValid()
	{
		boolean result = false;
		String session = sessionData.getServerSessionID();
		if (session != null && session.length() > 0) result = isSessionValid(session);
		
		return result;
	}
	
	public Session getSession(String sessionId) throws Exception
	{
		return restTemplate.execute(restServerUrl+"/"+sessionId, HttpMethod.GET, Session.class);
	}
}
