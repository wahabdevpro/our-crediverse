package cs.dto;

import hxc.ecds.protocol.rest.PartialReversalRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiReversalRequest extends PartialReversalRequest
{
	public enum ReversalType {FULL, PARTIAL}
	private ReversalType type;
	private String uuid;
	private String language;
	private Seperators seperators;
	private String authorizedBy;
}

/*
 * 	protected String coSignatorySessionID;
	protected String coSignatoryTransactionID;
	protected String coSignatoryOTP;
	protected String transactionNumber;
	protected BigDecimal amount;
	protected String reason;
	*/

/*
protected String coSignatorySessionID;
protected String coSignatoryTransactionID;
protected String coSignatoryOTP;
protected String transactionNumber;
protected String reason;
*/
