package cs.controller.admin;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cs.controller.ContextController;
import cs.utility.Common;

@RestController
@RequestMapping("/api/context")
@Profile(Common.CONST_ADMIN_PROFILE)
public class AdminContextController extends ContextController
{

}
