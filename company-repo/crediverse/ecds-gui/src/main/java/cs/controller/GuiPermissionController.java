package cs.controller;

import java.util.Set;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.security.LoginSessionData;

@RestController
@RequestMapping("/api/uperms")
public class GuiPermissionController
{
	@Autowired
	private LoginSessionData sessionData;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Set<String> index(HttpSession session) throws Exception
	{
		return sessionData.getCurrentUser().getPermissionSet();
	}
}
