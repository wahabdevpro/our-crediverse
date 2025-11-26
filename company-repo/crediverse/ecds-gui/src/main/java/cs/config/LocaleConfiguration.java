package cs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import cs.language.LanguageExceptionHandler;

@Configuration
public class LocaleConfiguration implements WebMvcConfigurer
{

	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		registry.addInterceptor(localeChangeInterceptor());
	}

	/*
	 * Added a session based LocaleResolver. Note, can fail on first request if no session exists However, once we have login authentication, this will not be a problem as a session will always exist.
	 */
	@Bean
	public LocaleResolver localeResolver()
	{
		AcceptHeaderLocaleResolver slr = new AcceptHeaderLocaleResolver();
		//slr.setDefaultLocale(Locale.ENGLISH);
		return slr;
	}

	@Bean
	public LocaleChangeInterceptor localeChangeInterceptor()
	{
		LocaleChangeInterceptor lci = new LanguageExceptionHandler();
		lci.setParamName("lang");
		return lci;
	}

	/**
	 * Change the location of the default locale files (for i18n)
	 */
	@Bean
	public ReloadableResourceBundleMessageSource messageSource()
	{
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasename("classpath:locale/messages");
		messageSource.setCacheSeconds(3600); // refresh cache once per hour
		return messageSource;
	}
}
