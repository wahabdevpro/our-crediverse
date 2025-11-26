package cs.controller.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.portal.GuiAgentAccountSummary;
import cs.dto.portal.GuiPinRules;
import cs.service.portal.PortalConverterService;
import cs.utility.Common;

@RestController
@RequestMapping("/papi/dashboard")
@Profile(Common.CONST_PORTAL_PROFILE)
public class PortalDashboardController
{

	@Autowired
	private PortalConverterService portalConversionService;

	@RequestMapping(value = "summary", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentAccountSummary getAgentAccountSummary() throws Exception
	{
		return portalConversionService.produceAgentAccountSummary();
	}

	/**
	 * Retrieve Configuration Settings reflecting the PIN rules
	 */
	@RequestMapping(value = "pinrules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiPinRules getPinRules() throws Exception
	{
		return portalConversionService.extractPinChangeRules();
	}

}
