package cs.dto.portal;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuiAgentAccountSummary
{
	private String title;
	private String firstName;
	private String surname;

	private BigDecimal balance;
	private BigDecimal bonusBalance;

	private int depositsCount;
	private BigDecimal depositsAmount;

	private int transferCount;
	private BigDecimal transferAmount;

	private int salesCount;
	private BigDecimal salesAmount;

	private int selfTopupsCount;
	private BigDecimal selfTopupsAmount;
}
