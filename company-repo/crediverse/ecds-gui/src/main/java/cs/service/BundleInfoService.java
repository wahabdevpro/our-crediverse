package cs.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import hxc.ecds.protocol.rest.Bundle;
import hxc.ecds.protocol.rest.BundleInfo;

@Service
public class BundleInfoService extends GenericService<BundleInfo>
{
	@PostConstruct
	public void configure()
	{
		this.loadConfigurion( restServerConfig.getBundlesInfoUrl() );
	}

	public BundleInfo [] listAvailableBundles(Bundle [] bundles) throws Exception
	{
		List<BundleInfo> available = new ArrayList<>();

		Set<String> usedTags = new HashSet<>();
		for(Bundle b : bundles)
		{
			usedTags.add(b.getTag());
		}

		for(BundleInfo bi : list())
		{
			if (!usedTags.contains(bi.getTag()))
			{
				available.add(bi);
			}
		}

		return available.toArray( new BundleInfo[available.size()] );
	}
}
