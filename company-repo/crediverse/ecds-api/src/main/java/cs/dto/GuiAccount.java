package cs.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class GuiAccount
{
	private int	agentID;
	private BigDecimal balance;
	private BigDecimal bonusBalance;
	private BigDecimal onHoldBalance;
	private BigDecimal totalBalance;
}
