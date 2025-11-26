package cs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;

import cs.dto.security.LoginSessionData;

@Configuration
public class SessionConfiguration
{

	@Bean
	@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = WebApplicationContext.SCOPE_SESSION)
	public LoginSessionData getSessionData()
	{
		LoginSessionData sess = new LoginSessionData();
		sess.setMySessionId(RequestContextHolder.getRequestAttributes().getSessionId());
		return sess;
	}
}
