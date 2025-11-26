package cs.controller.mobile;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cs.controller.ContextController;
import cs.utility.Common;

@RestController
@RequestMapping("/mapi/context")
@Profile(Common.CONST_MOBILE_PROFILE)
public class MobileContextController extends ContextController
{

}
