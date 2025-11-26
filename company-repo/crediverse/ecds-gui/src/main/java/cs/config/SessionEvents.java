package cs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.session.SessionCreationEvent;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.session.SessionInformation;

@Configuration
public class SessionEvents
{
	private static Logger logger = LoggerFactory.getLogger(SessionEvents.class);

	@EventListener
	public void onSessionInformationEvent(SessionInformation event)
	{
		logger.info("SessionInformation " + event.toString());
	}

	@EventListener
	public void onSessionCreationEventEvent(SessionCreationEvent event)
	{
		logger.info("SessionCreationEvent " + event.toString());
	}

	@EventListener
	public void onContextRefreshedEvent(ContextRefreshedEvent event)
	{
		logger.info("ContextRefreshedEvent " + event.toString());

		// Read in current Configuration when web context is created
	}

	@EventListener
	public void onApplicationEvent(SessionDestroyedEvent event)
	{
		logger.info("SessionDestroyedEvent " + event.toString());
		/*
		 * List<SecurityContext> lstSecurityContext = event.getSecurityContexts(); UserDetails ud; for (SecurityContext securityContext : lstSecurityContext) { ud = (UserDetails)
		 * securityContext.getAuthentication().getPrincipal(); // ... }
		 */
	}
}
