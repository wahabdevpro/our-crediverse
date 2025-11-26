package cs.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import cs.dto.security.LoginSessionData;

@Controller
@RequestMapping("/home")
public class UserHomeController
{
	private static final Logger logger = LoggerFactory.getLogger(UserHomeController.class);

	@Autowired
	private LoginSessionData sessionData;

	@RequestMapping()
	public String getHome() {
		return "dashboard";
	}

	/*
	 * Currently does nothing useful, but will be used in the future.
	 */
	@RequestMapping("{username}")
	public String index(@PathVariable("username") String username) throws Exception
	{
		logger.error(username + " => " + sessionData.getServerSessionID());
		return "dashboard";
	}
}
