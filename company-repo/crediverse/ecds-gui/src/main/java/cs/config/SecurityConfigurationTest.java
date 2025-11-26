package cs.config;

import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;

import cs.utility.Common;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
//@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@Order(SecurityProperties.DEFAULT_FILTER_ORDER)//TODO Verify how this works spring security 5
@Profile(Common.CONST_TEST_PROFILE)
public class SecurityConfigurationTest extends SecurityBaseConfiguration
{
	@Override
	protected void configure(HttpSecurity httpSecurity) throws Exception
	{
		secured(httpSecurity, sessionRegistry());
	}

	// Work around https://jira.spring.io/browse/SEC-2855
	@Bean
	public SessionRegistry sessionRegistry()
	{
		SessionRegistry sessionRegistry = new SessionRegistryImpl();
		return sessionRegistry;
	}
}
