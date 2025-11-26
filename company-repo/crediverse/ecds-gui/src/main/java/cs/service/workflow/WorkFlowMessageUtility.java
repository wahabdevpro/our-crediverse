package cs.service.workflow;

import cs.dto.GuiAdjudicateRequest;
import cs.dto.GuiAdjustmentRequest;
import cs.dto.GuiBatchUploadRequest;
import cs.dto.GuiReplenishRequest;
import cs.dto.GuiReversalCoAuthRequest;
import cs.dto.GuiTransferRequest;
import cs.dto.GuiWebUser;
import cs.dto.GuiWorkflowRequest;
import cs.dto.GuiWorkflowRequest.WorkflowRequestType;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.TransactionEx;
import hxc.ecds.protocol.rest.WorkItem;

public class WorkFlowMessageUtility
{
	public static void addFrenchDescription(WorkItem workItem, GuiWebUser webUser, GuiReplenishRequest request, WorkflowRequestType replenish)
	{
		StringBuilder description = new StringBuilder();
		//"{usera} wants to replenish root the account with {amount} and a bonus of {bonus}",
		description.append(webUser.getFullName());
		description.append(" veut reconstituer le compte root avec ");
		description.append(request.getAmount().toPlainString());
		description.append(" et un bonus de ");
		description.append(request.getBonusProvision().toPlainString());
		workItem.setDescription(description.toString());
	}

	public static void addEnglishDescription(WorkItem workItem, GuiWebUser webUser, GuiReplenishRequest request, WorkflowRequestType replenish)
	{
		StringBuilder description = new StringBuilder();
		//"{usera} wants to replenish root the account with {amount} and a bonus of {bonus}",
		description.append(webUser.getFullName());
		description.append(" wants to replenish the root account with ");
		description.append(request.getAmount().toPlainString());
		description.append(" and a bonus of ");
		description.append(request.getBonusProvision().toPlainString());
		workItem.setDescription(description.toString());
	}

	public static void addFrenchDescription(WorkItem workItem, GuiWebUser webUser, GuiTransferRequest request, WorkflowRequestType transfer)
	{
		StringBuilder description = new StringBuilder();
		//"{usera} wants to transfer {amount} to {userb}",
		description.append(webUser.getFullName());
		description.append(" veut transférer ");
		description.append(request.getAmount().toPlainString());
		description.append(" à ");
		description.append(request.getTargetMSISDN());
		workItem.setDescription(description.toString());
	}

	public static void addEnglishDescription(WorkItem workItem, GuiWebUser webUser, GuiTransferRequest request, WorkflowRequestType transfer)
	{
		StringBuilder description = new StringBuilder();
		//"{usera} wants to transfer {amount} to {userb}",
		description.append(webUser.getFullName());
		description.append(" wants to transfer ");
		description.append(request.getAmount().toPlainString());
		description.append(" to ");
		description.append(request.getTargetMSISDN());
		workItem.setDescription(description.toString());
	}

	public static void addFrenchDescription(WorkItem workItem, GuiWebUser webUser, GuiWebUser destinationWebUser, GuiWorkflowRequest request, WorkflowRequestType requestType)
	{
		StringBuilder description = new StringBuilder();

		description.append(webUser.getFullName());
		description.append(" demande un ");
		description.append(request.getTaskType());
		description.append(" Être créé pour ");
		description.append(request.getAmount());
		String bonus = request.getBonus();
		if (bonus != null && bonus.trim().length() > 0)
		{
			description.append(" avec un bonus de ");
			description.append(bonus);
		}
		String destinationAccount = request.getDestination();
		if (destinationAccount != null && destinationAccount.trim().length() > 0)
		{
			try
			{
				if (destinationWebUser != null)
				{
					description.append(" compte d'utilisateur ");
					description.append(destinationWebUser.getFullName());
					description.append(" (");
					description.append(destinationWebUser.getMobileNumber());
					description.append(")");
				}
			}
			catch (Exception e)
			{
			}
		}

		workItem.setDescription(description.toString());
	}

	public static void addEnglishDescription(WorkItem workItem, GuiWebUser webUser, GuiWebUser destinationWebUser, GuiWorkflowRequest request, WorkflowRequestType requestType)
	{
		StringBuilder description = new StringBuilder();

		description.append(webUser.getFullName());
		description.append(" requests a ");
		description.append(request.getTaskType());
		description.append(" be created for ");
		description.append(request.getAmount());
		String bonus = request.getBonus();
		if (bonus != null && bonus.trim().length() > 0)
		{
			description.append(" with a bonus of ");
			description.append(bonus);
		}
		String destinationAccount = request.getDestination();
		if (destinationAccount != null && destinationAccount.trim().length() > 0)
		{
			try
			{
				if (destinationWebUser != null)
				{
					description.append(" to account ");
					description.append(destinationWebUser.getFullName());
					description.append(" (");
					description.append(destinationWebUser.getMobileNumber());
					description.append(")");
				}
			}
			catch (Exception e)
			{
			}
		}

		workItem.setDescription(description.toString());
	}

	public static void addEnglishDescription(WorkItem workItem, GuiWebUser webUser, GuiAdjustmentRequest request, Agent agent, WorkflowRequestType adjustment)
	{
		StringBuilder description = new StringBuilder();
		//"{usera} wants to transfer {amount} to {userb}",
		description.append(webUser.getFullName());
		description.append(" wants to adjust agent ");
		description.append(agent.getFirstName());
		description.append(" ");
		description.append(agent.getSurname());
		String mobileNumber = agent.getMobileNumber();
		if (mobileNumber != null && mobileNumber.length() > 0) {
			description.append("(");
			description.append(mobileNumber);
			description.append(")");
		}
		description.append(" by ");
		description.append(request.getAmount().toPlainString());
		workItem.setDescription(description.toString());
	}

	public static void addFrenchDescription(WorkItem workItem, GuiWebUser webUser, GuiAdjustmentRequest request, Agent agent, WorkflowRequestType adjustment)
	{
		StringBuilder description = new StringBuilder();
		//"{usera} wants to transfer {amount} to {userb}",
		description.append(webUser.getFullName());
		description.append(" Veut ajuster l'agent ");
		description.append(agent.getFirstName());
		description.append(" ");
		description.append(agent.getSurname());
		String mobileNumber = agent.getMobileNumber();
		if (mobileNumber != null && mobileNumber.length() > 0) {
			description.append("(");
			description.append(mobileNumber);
			description.append(")");
		}
		description.append(" par ");
		description.append(request.getAmount().toPlainString());
		workItem.setDescription(description.toString());
	}

	public static void addEnglishDescription(WorkItem workItem, GuiWebUser webUser, GuiBatchUploadRequest request, WorkflowRequestType batchupload)
	{
		StringBuilder description = new StringBuilder();
		//"{usera} wants to process a batch adjustment",
		description.append(webUser.getFullName());
		description.append(" wants to process a batch adjustment");
		workItem.setDescription(description.toString());
	}

	public static void addFrenchDescription(WorkItem workItem, GuiWebUser webUser, GuiBatchUploadRequest request, WorkflowRequestType batchupload)
	{
		StringBuilder description = new StringBuilder();
		//"{usera} wants to process a batch adjustment",
		description.append(webUser.getFullName());
		description.append(" Veut traiter un ajustement de lot");
		workItem.setDescription(description.toString());
	}

	public static void addEnglishDescription(WorkItem workItem, GuiWebUser webUser, GuiReversalCoAuthRequest request, String transactionNumber, WorkflowRequestType adjustment)
	{
		StringBuilder description = new StringBuilder();
		//"{usera} wants to transfer {amount} to {userb}",
		description.append(webUser.getFullName());
		description.append(" wants to ");
		if (request.getType() == GuiReversalCoAuthRequest.ReversalType.FULL)
		{
			description.append(" fully reverse ");
		}
		else
		{
			description.append(" partially reverse ");
		}
		description.append(" transaction ");

		description.append(transactionNumber);
		description.append(", reversing an amount of ");
		description.append(request.getAmount().toPlainString());
		workItem.setDescription(description.toString());
	}

	public static void addFrenchDescription(WorkItem workItem, GuiWebUser webUser, GuiReversalCoAuthRequest request, String transactionNumber, WorkflowRequestType adjustment)
	{
		StringBuilder description = new StringBuilder();
		//"{usera} wants to transfer {amount} to {userb}",
		description.append(webUser.getFullName());
		description.append(" vouloir ");
		if (request.getType() == GuiReversalCoAuthRequest.ReversalType.FULL)
		{
			description.append(" complètement inversé ");
		}
		else
		{
			description.append(" partiellement inversé ");
		}
		description.append(" transaction ");

		description.append(transactionNumber);
		description.append(", inverser un montant de ");
		description.append(request.getAmount().toPlainString());
		workItem.setDescription(description.toString());
	}

	public static void addEnglishDescription(WorkItem workItem, GuiWebUser webUser, GuiAdjudicateRequest request, TransactionEx tdr, WorkflowRequestType adjustment) {
		StringBuilder description = new StringBuilder();
		//"{usera} wants to transfer {amount} to {userb}",
		description.append(webUser.getFullName());
		description.append(" wants to adjudicate transaction ");
		description.append(request.getTransactionNumber());
		switch (tdr.getType())
		{
			case "SL":
				description.append(" which was a sale for ");
				break;
			case "SB":
				description.append(" which was a bundle sale for ");
				break;
			case "ST":
				description.append(" which was a self topup for ");
				break;
			case "AJ":
				description.append(" which was an adjustment of ");
				break;
			case "FR":
				description.append(" which was a reversal for ");
				break;
			case "PA":
				description.append(" which was a partial reversal for ");
				break;
			default:
				description.append(" which was for ");
				break;
		}
		description.append(tdr.getAmount().toPlainString());
		//description.append(" by ");
		//description.append(request.getAmount().toPlainString());
		workItem.setDescription(description.toString());
	}

	public static void addFrenchDescription(WorkItem workItem, GuiWebUser webUser, GuiAdjudicateRequest request, TransactionEx tdr, WorkflowRequestType adjustment) {
		StringBuilder description = new StringBuilder();
		//"{usera} wants to transfer {amount} to {userb}",
		description.append(webUser.getFullName());
		description.append(" Veut juger la transaction ");
		description.append(request.getTransactionNumber());
		switch (tdr.getType())
		{
			case "SL":
				description.append(" qui était une vente pour ");
				break;
			case "SB":
				description.append(" qui était une vente forfaitaire pour ");
				break;
			case "ST":
				description.append(" qui était un topu pour ");
				break;
			case "AJ":
				description.append(" qui était un ajustement de ");
				break;
			case "FR":
				description.append(" qui était un renversement pour ");
				break;
			case "PA":
				description.append(" qui était un renversement partiel pour ");
				break;
			default:
				description.append(" qui était pour ");
				break;
		}
		description.append(tdr.getAmount().toPlainString());
		//description.append(" par ");
		//description.append(request.getAmount().toPlainString());
		workItem.setDescription(description.toString());
	}
}
