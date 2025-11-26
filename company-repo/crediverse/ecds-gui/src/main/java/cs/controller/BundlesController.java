package cs.controller;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.GuiBundle;
import cs.dto.GuiDataTable;
import cs.service.BundleInfoService;
import cs.service.BundlesService;
import cs.service.TypeConvertorService;
import cs.utility.Common;
import hxc.ecds.protocol.rest.Bundle;
import hxc.ecds.protocol.rest.BundleInfo;

@RestController
@RequestMapping("/api/bundles")
@Profile(Common.CONST_ADMIN_PROFILE)
public class BundlesController
{
	@Autowired
	private BundlesService bundlesService;

	@Autowired
	private BundleInfoService bundleInfoService;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@RequestMapping(value="availbundles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public BundleInfo [] listAvailableBindleInfo() throws Exception
	{
		return bundleInfoService.listAvailableBundles( bundlesService.list() );
	}


	@RequestMapping(value="allbundles", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public BundleInfo [] listAllBindleInfo() throws Exception
	{
		return bundleInfoService.list();
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	@RequiredPermission(group=PermissionGroup.Cell.toString(), name=Permission.PERM_VIEW)
	public GuiDataTable list(@RequestParam Map<String, String> params) throws Exception
	{
		// Get Bundles List (What is configured in the system
		Bundle[] tsBundles = bundlesService.list();

		// Get PCC Bundles (What is available)
		BundleInfo [] pccBundleInfo = bundleInfoService.list();

		GuiBundle[] guiBundles = typeConvertorService.getGuiBundlesFromBundles(tsBundles, pccBundleInfo);
		return new GuiDataTable(guiBundles);
	}

	@RequestMapping(value="{bundleId}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("bundleId") String bundleId) throws Exception
	{
		bundlesService.delete(bundleId);
		return "{}";
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiBundle create(@RequestBody(required = true) GuiBundle newBundle, Locale locale) throws Exception
	{
		bundlesService.create( newBundle.exportBundle() );
		return newBundle;
	}

	@RequestMapping(method = RequestMethod.PUT)
	public GuiBundle update(@RequestBody(required = true) GuiBundle updatedBundle, Locale locale) throws Exception
	{
		bundlesService.updateAndValidateNoRepeats( updatedBundle.exportBundle() );
		return updatedBundle;
	}

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(@RequestParam(value = "_type") Optional<String> type, @RequestParam(value = "term") Optional<String> query) throws Exception
	{
		return bundlesService.getDropDownMap(type, query, "id", "name");
	}

}
