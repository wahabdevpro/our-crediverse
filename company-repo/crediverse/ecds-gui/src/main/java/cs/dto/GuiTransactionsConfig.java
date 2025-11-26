package cs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;                                                                                                                                 
import hxc.ecds.protocol.rest.config.TransactionsConfig;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties({"oltpTransactionCleanupTimeOfDay", "olapTransactionCleanupTimeOfDay"})
public class GuiTransactionsConfig extends TransactionsConfig
{
	protected String oltpTransactionCleanupTimeOfDayString;
	protected String olapTransactionCleanupTimeOfDayString;
	protected String olapSyncTimeOfDayString;
}
