package hxc.ecds.protocol.rest;

public class UpdateTransferRulesResponse extends ResponseHeader
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private TransferRuleIssue[] problematicRules;
	private int[] unreachableTierIDs;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public TransferRuleIssue[] getProblematicRules()
	{
		return problematicRules;
	}

	public UpdateTransferRulesResponse setProblematicRules(TransferRuleIssue[] problematicRules)
	{
		this.problematicRules = problematicRules;
		return this;
	}

	public int[] getUnreachableTierIDs()
	{
		return unreachableTierIDs;
	}

	public UpdateTransferRulesResponse setUnreachableTierIDs(int[] unreachableTierIDs)
	{
		this.unreachableTierIDs = unreachableTierIDs;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public UpdateTransferRulesResponse()
	{
	}

	public UpdateTransferRulesResponse(UpdateTransferRulesRequest request)
	{
		super(request);
	}
}
