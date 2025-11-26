package cs.event;

import org.apache.catalina.session.StandardSessionFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.session.SessionDestroyedEvent;

import cs.service.ContextService;
import cs.service.GuiAuthenticationService;

@Configuration
public class SessionTerminationEvents implements ApplicationListener<SessionDestroyedEvent>
{
	@Autowired
	private GuiAuthenticationService authService;

	@Autowired
	private ContextService contextService;

	private static Logger logger = LoggerFactory.getLogger(SessionTerminationEvents.class);

	@Override
	public void onApplicationEvent(SessionDestroyedEvent event)
	{
		if (event.getSource() instanceof StandardSessionFacade)
		{
			StandardSessionFacade sessionFacade = (StandardSessionFacade)event.getSource();
			String sessionId = sessionFacade.getId();
			contextService.clearSession(sessionId);
			authService.logout(sessionId);
		}
		else
		{
			logger.error("Unable to obtain session ID ");
		}
		logger.info("Got session destroyed  event " + event.toString());
		//List<SecurityContext> lstSecurityContext = event.getSecurityContexts();
		/*UserDetails ud;
		for (SecurityContext securityContext : lstSecurityContext)
		{
			ud = (UserDetails) securityContext.getAuthentication().getPrincipal();
			// ...
		}*/
	}
}
