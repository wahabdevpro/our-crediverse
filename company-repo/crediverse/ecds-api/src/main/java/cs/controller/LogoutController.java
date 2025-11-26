package cs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.security.LoginSessionData;
import cs.service.GuiAuthenticationService;

@RestController
@RequestMapping("/api/logout")
public class LogoutController {

	@Autowired
	private LoginSessionData sessionData;
	
	@Autowired 
	private GuiAuthenticationService guiAuthenticationService;
	
	@RequestMapping(method = RequestMethod.POST)
	public void updateUserProfile() throws Exception
	{
		String sessionID = sessionData.getServerSessionID();
		guiAuthenticationService.logout(sessionID);
	}
}
