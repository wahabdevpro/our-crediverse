package cs.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import cs.security.CustomHeaderWriter;
import cs.utility.Common;

//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityBaseConfiguration extends WebSecurityConfigurerAdapter
{
	@Autowired
	private Environment enviroment;

	protected boolean isPortalEnvironment() {
		boolean result = false;

		if (enviroment.getActiveProfiles() == null)
			result = true;
		else
		{
			for(String profile : enviroment.getActiveProfiles())
			{
				if (profile.equalsIgnoreCase(Common.CONST_PORTAL_PROFILE))
				{
					result = true;
					break;
				}
			}
		}

		return result;
	}

	protected void secured(HttpSecurity httpSecurity, SessionRegistry sessionRegistry) throws Exception
	{
		//RequestMatcher matcher = new AntPathRequestMatcher("/login");
		//DelegatingRequestMatcherHeaderWriter headerWriter = new DelegatingRequestMatcherHeaderWriter(matcher,new CustomHeaderWriter());
		httpSecurity.headers()
		.addHeaderWriter(new CustomHeaderWriter());
		httpSecurity.csrf().disable();

		if (Common.isAdmin())
		{
			httpSecurity.csrf().ignoringAntMatchers("/error**", "/api/context**");
		}
		else if (Common.isMobile())
		{
			httpSecurity.csrf().ignoringAntMatchers("/error**", "/mapi/context**");
		}
		else
		{
			httpSecurity.csrf().ignoringAntMatchers("/error**", "/papi/context**");
		}

		httpSecurity.authorizeRequests()
			.antMatchers("/css/**", "/js/**", "/fonts/**", "/img/**", "/**/cs-favicon.png", "/permissionDenied**", "/expired")
				.permitAll()
			.antMatchers("/auth/logout")
				.permitAll()
			.antMatchers("/logo/**")
				.permitAll()
			.antMatchers("/datatables/lang/**")
				.permitAll()
			.antMatchers("/error**")
				.permitAll()
			.antMatchers("/auth/**")
				.permitAll()
			.antMatchers("//auth/**")
				.permitAll()
			.antMatchers("/**")
				.fullyAuthenticated()
		.and()
			.formLogin()
				.loginPage("/login")
					.failureUrl("/login?error")
					.permitAll()
		.and()
			.logout()
				.logoutUrl("/logout")
				.permitAll()
				// .deleteCookies("remember-me")
				.logoutSuccessUrl("/login?loggedOut")
				.permitAll()
		.and()
			.formLogin()
				.defaultSuccessUrl("/")
		.and()
			.sessionManagement()
				.maximumSessions(99)
				.expiredUrl("/login")
				.sessionRegistry(sessionRegistry)
				.maxSessionsPreventsLogin(true)
		// .and()
		// .rememberMe()
		;
	}

	protected void unsecured(HttpSecurity httpSecurity, SessionRegistry sessionRegistry) throws Exception
	{
		httpSecurity.authorizeRequests().antMatchers("/").permitAll();
	}

	// Register HttpSessionEventPublisher, needed to get logout events
	@Bean
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ServletListenerRegistrationBean httpSessionEventPublisher()
	{
		return new ServletListenerRegistrationBean(new HttpSessionEventPublisher());
	}
}
