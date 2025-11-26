package cs.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.support.RequestContextUtils;

import cs.dto.Language;

public class HomeController  implements WebMvcConfigurer
{
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	@Autowired
	private ResourceLoader loader;

	@RequestMapping(value = "/datatable", method = RequestMethod.GET)
	public String getDatatable()
	{
		return "datatable";
	}

	@RequestMapping(value = "/resource/test", method = RequestMethod.GET)
	public Resource getBranding()
	{
		Resource item = loader.getResource("branding/default/css/dummy.css");
		return item;
	}

	@Autowired
	private RestTemplate restTemplate;

	private String url = "http://127.0.0.1:8080/helloworld/webresources/preferences/getlanguage";

	public void setUrl(String newUrl)
	{
		url = newUrl;
	}

	@RequestMapping(value = "/updatelang", method = RequestMethod.POST)
	public String save(HttpServletRequest request, HttpServletResponse response)
	{
		// Find the current Locale
		Locale locale = LocaleContextHolder.getLocale();
		String newLang = (locale.toString().equalsIgnoreCase("fr")) ? "en" : "fr";

		// Change the language
		LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
		localeResolver.setLocale(request, response, StringUtils.parseLocaleString(newLang));

		return "redirect:/";
	}

	@RequestMapping(value = "/updaterest", method = RequestMethod.POST)
	public String setLanguage(HttpServletRequest request, HttpServletResponse response)
	{
		Language lang = restTemplate.getForObject(url, Language.class);

		logger.info(lang.getLang());

		// Change the language
		LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
		localeResolver.setLocale(request, response, StringUtils.parseLocaleString(lang.getLang()));

		return "redirect:/";
	}
}
