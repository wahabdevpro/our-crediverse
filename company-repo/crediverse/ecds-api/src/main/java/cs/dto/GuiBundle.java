package cs.dto;

import java.math.BigDecimal;

import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.Bundle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuiBundle
{
	private int bundleID;
	private String name;
	private String description;
	private String type;
	private StateEnum state;
	private BigDecimal price;
	private BigDecimal tradeDiscountPercentage;
	
	public GuiBundle(Bundle bundle)
	{
		this.bundleID = bundle.getId();
		this.name = bundle.getName();
		this.description = bundle.getDescription();
		this.type = bundle.getType();
		this.state = StateEnum.fromString(bundle.getState());
		this.price = bundle.getPrice();
		this.tradeDiscountPercentage = bundle.getTradeDiscountPercentage();
	}
	
	public enum StateEnum {
		ACTIVE(Agent.STATE_ACTIVE),
		DEACTIVATED(Agent.STATE_DEACTIVATED);
		private String val;
		private StateEnum(String val) {
			this.val = val.toUpperCase();
		}
		
		public String getVal()
		{
			return this.val;
		}
		
		public static StateEnum fromString(String val)
		{
			StateEnum result = DEACTIVATED;
			if (val != null)
			{
				switch (val)
				{
					case Agent.STATE_ACTIVE:
						result = ACTIVE;
						break;
					case Agent.STATE_DEACTIVATED:
						result = DEACTIVATED;
						break;
				}
			}
			return result;
		}
	}
	
}
