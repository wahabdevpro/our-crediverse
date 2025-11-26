package cs.controller;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.service.GuiAuthenticationService;
import cs.utility.Common;

@Controller
public class LoginController {
	@Autowired
	private GuiAuthenticationService authService;

	@Autowired
	private ObjectMapper mapper;

	private boolean isAjax(HttpServletRequest request) {
		String requestedWithHeader = request.getHeader("X-Requested-With");
		return "XMLHttpRequest".equals(requestedWithHeader);
	}

	@RequestMapping(value="/login**")
	public String loginHTML(HttpServletRequest request, HttpServletResponse response, Locale locale, Model model ) throws Exception
	{
		if (isAjax(request)) throw new Exception(request.getRequestURL().toString());
		String pageLocale = (locale == null)? LocaleContextHolder.getLocale().getLanguage() : locale.getLanguage();
		model.addAttribute("pageLocale", pageLocale);
		model.addAttribute("sessionTimeout", request.getSession().getMaxInactiveInterval());

		if (Common.isPortal())
			return "portallogin";
		else if (Common.isMobile())
			return "mobilelogin";
		else
			return "login";


	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public @ResponseBody ObjectNode handleSomeException(HttpServletRequest request, Exception ex)
	{
		ObjectNode node = mapper.createObjectNode();
		node.put("login", "required");
		node.put("url", request.getRequestURL().toString());
		return node;
	}

	@RequestMapping("/logout**")
	public String logout()
	{
		authService.logout();
		return "redirect:/";
	}
}
