package cs.service;

import java.net.URI;
import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.dto.GuiBundle;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Bundle;

@Service
public class BundleService //extends GenericService<BundleInfo>
{
	@Autowired //ask @Configuration-marked class for this
	private CsRestTemplate restTemplate;

	@Autowired //ask @Configuration-marked class for this
	private RestServerConfiguration restServerConfig;

	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getBundlesUrl();
	}

	public GuiBundle[] listAvailableBundles(Integer offset, Integer limit) throws Exception
	{
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		if (offset != null && limit != null)	
			RestRequestUtil.standardPaging(uriBuilder, offset, limit);
		URI uri = uriBuilder.build(true).toUri();
		Bundle[] bundles = restTemplate.execute(uri, HttpMethod.GET, Bundle[].class);
		return getGuiBundleArrayFromBundleArray(bundles);
	}

	private GuiBundle[] getGuiBundleArrayFromBundleArray(Bundle[] bundles)
	{
		ArrayList<GuiBundle> guiBundles = new ArrayList<GuiBundle>();
		for(Bundle bundle:bundles)
		{
			GuiBundle guiBundle = new GuiBundle(bundle);
			guiBundles.add(guiBundle);
		}
		return guiBundles.toArray(new GuiBundle[guiBundles.size()]);
	}
}
