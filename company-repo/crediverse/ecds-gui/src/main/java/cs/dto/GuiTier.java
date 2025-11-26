package cs.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeanUtils;

import hxc.ecds.protocol.rest.Tier;
import hxc.ecds.protocol.rest.TransferRule;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiTier extends Tier {

	protected boolean mayTransferFrom = false;
	protected boolean mayTransferTo = false;
	protected boolean mayReceiveBuyerTradeBonus = false;
	protected List<Integer> mayTransferFromTiers = new ArrayList<Integer>();
	protected List<Integer> mayTransferToTiers = new ArrayList<Integer>();
	protected String mayTransferFromTiersString = null;
	protected String mayTransferToTiersString = null;

	public enum TierType {
		ROOT("."),
		STORE("T"),
		WHOLESALER("W"),
		RETAILER("R"),
		SUBSCRIBER("S");

		private String value;
		private TierType(String value)
		{
			this.value = value;
		}

		public static TierType getTierType(String val)
		{
			for(TierType tt : TierType.values())
				if (tt.value.equals(val)) return tt;
			return null;
		}
	};

	public GuiTier()
	{
	}

	public GuiTier(Tier orig)
	{
		BeanUtils.copyProperties(orig, this);

		this.mayTransferFrom = TransferRule.mayTransferFrom(this);
		this.mayTransferTo = TransferRule.mayTransferTo(this);
		this.mayReceiveBuyerTradeBonus = TransferRule.mayReceiveBuyerTradeBonus(this);
	}

	public void initTransferBetweenTiers(List<Tier> tiers)
	{
		for (Tier tier :  tiers) {
			if (this.mayTransferTo( tier ))
				this.mayTransferToTiers.add(tier.getId());
			if (this.mayTransferFrom( tier ))
				this.mayTransferFromTiers.add(tier.getId());
		}

		{
			StringBuilder builder = new StringBuilder();
			for (Integer i : this.mayTransferToTiers) {
				if (builder.length()>0) builder.append(",");
				builder.append(i);
			}
			this.mayTransferToTiersString = builder.toString();
		}
		{
			StringBuilder builder = new StringBuilder();
			for (Integer i : this.mayTransferFromTiers) {
				if (builder.length()>0) builder.append(",");
				builder.append(i);
			}
			this.mayTransferFromTiersString = builder.toString();
		}
	}

	public boolean mayTransferTo( Tier other )
	{
		return TransferRule.mayTransferBetween( this, other );
	}

	public boolean mayTransferFrom( Tier other )
	{
		return TransferRule.mayTransferBetween( other, this );
	}
}

