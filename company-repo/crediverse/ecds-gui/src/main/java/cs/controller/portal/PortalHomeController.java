package cs.controller.portal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cs.controller.HomeController;
import cs.utility.Common;

@Controller
@Profile(Common.CONST_PORTAL_PROFILE)
public class PortalHomeController  extends HomeController
{

	@RequestMapping("/")
	public String index() throws Exception
	{
		return "portalhome";
	}

}
