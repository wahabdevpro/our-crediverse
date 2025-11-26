package hxc.ecds.protocol.rest;

import java.util.List;

// REST End-Point: ~/transfer_rules
public class UpdateTransferRulesRequest extends RequestHeader
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
	protected int[] transferRulesToRemove;
	protected TransferRule[] transferRulesToUpsert;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int[] getTransferRulesToRemove()
	{
		return transferRulesToRemove;
	}

	public UpdateTransferRulesRequest setTransferRulesToRemove(int[] transferRulesToRemove)
	{
		this.transferRulesToRemove = transferRulesToRemove;
		return this;
	}

	public TransferRule[] getTransferRulesToUpsert()
	{
		return transferRulesToUpsert;
	}

	public UpdateTransferRulesRequest setTransferRulesToUpsert(TransferRule[] transferRulesToUpsert)
	{
		this.transferRulesToUpsert = transferRulesToUpsert;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Methods
	//
	// /////////////////////////////////
	@Override
	public UpdateTransferRulesResponse createResponse()
	{
		return new UpdateTransferRulesResponse(this);
	}

	@Override
	public List<Violation> validate()
	{
		Validator validator = new Validator();

		if (transferRulesToUpsert != null)
		{
			for (TransferRule rule : transferRulesToUpsert)
			{
				validator.notNull("transferRulesToUpsert", rule);
				validator.validate(rule);
			}
		}

		return validator.toList();

	}

}
