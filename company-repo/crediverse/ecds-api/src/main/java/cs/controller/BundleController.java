package cs.controller;

import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.GuiBundle;
import cs.service.BundleService;

@RestController
@RequestMapping("/api/bundle")
public class BundleController 
{
	@Autowired //ask @Configuration-marked class for this
	private BundleService bundleService;
	
	@Resource(name="tokenStore")
	private TokenStore tokenStore;
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiBundle[] getBundleList(@RequestParam Map<String, String> params) throws Exception
	{
		Integer offset = params.containsKey("offset")? Integer.parseInt(params.get("offset")) : null;
		Integer limit = params.containsKey("limit")? Integer.parseInt(params.get("limit")) : null;
		GuiBundle[] bundleList = bundleService.listAvailableBundles(offset, limit);
		return bundleList;
	}
}