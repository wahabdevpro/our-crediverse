package cs.controller.portal;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cs.controller.ContextController;
import cs.utility.Common;

@RestController
@RequestMapping("/papi/context")
@Profile(Common.CONST_PORTAL_PROFILE)
public class PortalContextController extends ContextController
{

}
