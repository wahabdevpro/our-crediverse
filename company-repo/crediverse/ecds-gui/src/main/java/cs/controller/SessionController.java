package cs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.constants.ApplicationConstants;
import cs.service.SessionService;
import hxc.ecds.protocol.rest.Session;


/*
 * Used for help with debugging transaction server based authorizations.
 *
 */
@Profile(ApplicationConstants.CONST_PROFILE_DEV)
@RestController
@RequestMapping("/api/session")
public class SessionController
{
	@Autowired
	SessionService sessionService;

	@RequestMapping(method = RequestMethod.GET, value="{sessionId}")
	public Session getSingleUser(@PathVariable("sessionId") String sessionId) throws Exception
	{
		return sessionService.getSession(sessionId);
	}
}
