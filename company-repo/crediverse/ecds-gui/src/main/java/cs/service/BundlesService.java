package cs.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import cs.dto.error.GuiValidationException;
import cs.dto.error.GuiViolation.ViolationType;
import hxc.ecds.protocol.rest.Bundle;
import hxc.ecds.protocol.rest.Violation;

@Service
public class BundlesService extends GenericService<Bundle>
{
	@PostConstruct
	public void configure()
	{
		this.loadConfigurion( restServerConfig.getBundlesUrl() );
	}

	public Map<Integer, String> getDropDownMap(Optional<String> type, Optional<String> query) throws Exception {
		return getDropDownMap(type, query, "id", "name");
	}


	public List<Bundle> checkForBundleUssdRepeats(Bundle bundle, Bundle[] list) throws Exception
	{
		List<Bundle> repeatedCodes = new ArrayList<>();

		if (bundle.getUssdCode() != null && bundle.getUssdCode().length() > 0)
		{
			for(Bundle bnd : list)
			{
				if ((bnd.getId() != bundle.getId()))
				{
					if (bnd.getUssdCode() != null && bnd.getUssdCode().equals(bundle.getUssdCode()))
					{
						repeatedCodes.add(bnd);
					}
				}
			}
		}
		return repeatedCodes;
	}


	public List<Bundle> checkForBundleSmsKeywordRepeats(Bundle bundle, Bundle[] list) throws Exception
	{
		List<Bundle> repeatedCodes = new ArrayList<>();

		if (bundle.getSmsKeyword() != null && bundle.getSmsKeyword().length() > 0)
		{
			for(Bundle bnd : list)
			{
				if ((bnd.getId() != bundle.getId()))
				{
					if (bnd.getSmsKeyword() != null && bnd.getSmsKeyword().equals(bundle.getSmsKeyword()))
					{
						repeatedCodes.add(bnd);
					}
				}
			}
		}
		return repeatedCodes;
	}

	public void updateAndValidateNoRepeats(Bundle updatedBundle) throws Exception
	{
		Bundle[] list = this.list();
		List<Violation> violations = new ArrayList<>();

		if (checkForBundleUssdRepeats(updatedBundle, list).size() > 0)
		{
			violations.add( new Violation(ViolationType.duplicateValue.toString(), "ussdCode", null, "duplicateValue") );
		}

		if (checkForBundleSmsKeywordRepeats(updatedBundle, list).size() > 0)
		{
			violations.add( new Violation(ViolationType.duplicateValue.toString(), "smsKeyword", null, "duplicateValue") );
		}

		if (violations.size() > 0)
		{
			throw new GuiValidationException(violations);
		}
		this.update(updatedBundle);
	}
}
