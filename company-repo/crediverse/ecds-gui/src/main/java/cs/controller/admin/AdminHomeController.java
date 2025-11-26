package cs.controller.admin;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cs.controller.HomeController;
import cs.utility.Common;

@Controller
@Profile(Common.CONST_ADMIN_PROFILE)
public class AdminHomeController extends HomeController
{

	@RequestMapping("/")
	public String index() throws Exception
	{
		return "dashboard";
	}

}
