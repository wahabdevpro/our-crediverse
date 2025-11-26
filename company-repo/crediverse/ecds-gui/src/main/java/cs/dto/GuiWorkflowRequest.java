package cs.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiWorkflowRequest extends GuiCosignInfo
{
	public enum WorkflowRequestType{REPLENISH, TRANSFER, ADJUSTMENT, REVERSAL, PARTIALREVERSAL, WORKFLOWREQUEST, BATCHUPLOAD, ADJUDICATION}

	private int agentID;
	private BigDecimal amount;
	private BigDecimal bonusProvision;
	private String reason;
	private String transactionNumber;
	private String targetMSISDN;





	private String uuid;
	private GuiReplenishRequest replenish;
	private GuiTransferRequest transfer;
	private GuiAdjustmentRequest adjustment;
	private GuiReversalCoAuthRequest reversal;
	private GuiBatchUploadRequest batchUpload;
	private GuiAdjudicateRequest adjudication;
	private WorkflowRequestType requestType;
	private String taskType;
	private String bonus;
	private String destination;
	private String sessionID;
	private boolean rootAccount;

	private String language;
	private Seperators seperators;
}
