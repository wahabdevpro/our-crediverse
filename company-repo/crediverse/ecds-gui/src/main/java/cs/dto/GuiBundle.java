package cs.dto;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.BeanUtils;

import hxc.ecds.protocol.rest.Bundle;
import hxc.ecds.protocol.rest.BundleInfo;
import hxc.ecds.protocol.rest.BundleLanguage;
import hxc.ecds.protocol.rest.Promotion;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuiBundle extends Bundle
{
	public enum BundleState
	{
		Active,
		Deactivated,
		NotConfigured,
		Unavailable
	}

	private boolean bundleActive = false;
	private BundleState bundleState = BundleState.Unavailable;

	private BigDecimal guiTradeDiscountPercentage;
	private Map<String, SimpleBundleLanguage> languagesMap;

	public GuiBundle(Bundle orig)
	{
		importBundle(orig);
	}

	public GuiBundle(BundleInfo pccBundleInfo)
	{
		importBundle(pccBundleInfo);
	}

	public void importBundle(Bundle orig)
	{
		BeanUtils.copyProperties(orig, this);

		languagesMap = new TreeMap<>();
		if (orig.getLanguages() != null)
		{
			for(BundleLanguage bl : orig.getLanguages())
			{
				languagesMap.put( bl.getLanguage(), new SimpleBundleLanguage(bl.getType(), bl.getName(), bl.getDescription(), bl.getSmsKeyword()) );
			}
		}

		this.bundleActive = ("A".equals( this.state ));

		if (this.tradeDiscountPercentage != null)
			this.guiTradeDiscountPercentage = this.tradeDiscountPercentage.multiply(new BigDecimal(100));
		else
			this.guiTradeDiscountPercentage = new BigDecimal(0);
	}

	public void updateConfirmedState()
	{
		bundleState = bundleActive? BundleState.Active : BundleState.Deactivated;
	}

	public void importBundle(BundleInfo pccBundleInfo)
	{
		tag = pccBundleInfo.getTag();
		name = pccBundleInfo.getName();
		description = pccBundleInfo.getDescription();
		type = pccBundleInfo.getType();
		bundleState = BundleState.NotConfigured;
	}

	public Bundle exportBundle() throws ParseException
	{
		Bundle bundle = new Bundle();
		this.state = (this.bundleActive)? Bundle.STATE_ACTIVE : Promotion.STATE_DEACTIVATED;
		this.tradeDiscountPercentage = this.guiTradeDiscountPercentage.divide(new BigDecimal(100));

		BeanUtils.copyProperties(this, bundle);
		List<BundleLanguage> bls = new ArrayList<>();

		if (languagesMap != null) {
			for(String lang : languagesMap.keySet())
			{
				String langLC = lang.toLowerCase();
				SimpleBundleLanguage sbl = languagesMap.get(lang);

				// Add New
				BundleLanguage bl = new BundleLanguage()
					.setLanguage(langLC)
					.setType(sbl.getType())
					.setName(sbl.getName())
					.setDescription(sbl.getDescription())
					.setSmsKeyword(sbl.getSmsKeyword())
					.setBundleID(this.getId());

				bls.add(bl);
			}
		}
		bundle.setLanguages(bls);

		return bundle;
	}

	@Setter
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class SimpleBundleLanguage
	{
		private String type;
		private String name;
		private String description;
		private String smsKeyword;
	}
}
