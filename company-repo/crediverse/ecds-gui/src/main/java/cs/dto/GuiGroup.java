package cs.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;

import hxc.ecds.protocol.rest.Group;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_ABSENT)
public class GuiGroup extends Group
{
	private String tierName;

	public GuiGroup()
	{
		this.maxDailyAmount = new BigDecimal(0);
		this.maxDailyCount = Integer.valueOf(0);
		this.maxMonthlyAmount = new BigDecimal(0);
		this.maxMonthlyCount = Integer.valueOf(0);
		this.maxTransactionAmount = new BigDecimal(0);
	}
}
