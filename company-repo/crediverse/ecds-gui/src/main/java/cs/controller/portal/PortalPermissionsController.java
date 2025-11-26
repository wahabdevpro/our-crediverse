package cs.controller.portal;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cs.controller.PermissionsController;

@RestController
@RequestMapping("/papi/permissions")
public class PortalPermissionsController extends PermissionsController
{

}
