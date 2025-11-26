
package cs.controller.mobile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.portal.GuiAgentAccountSummary;
import cs.service.portal.PortalConverterService;
import cs.utility.Common;

@RestController
@RequestMapping("/mapi/dashboard")
@Profile(Common.CONST_MOBILE_PROFILE)
public class MobileDashboardController
{

	@Autowired
	private PortalConverterService portalConversionService;

	@RequestMapping(value = "summary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentAccountSummary getAgentAccountSummary() throws Exception
	{
		return portalConversionService.produceAgentAccountSummary();
	}

}
