package cs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cs.dto.GuiWorkflowRequest.WorkflowRequestType;
import hxc.ecds.protocol.rest.WorkItem;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(value = { "request", "response" })
public class GuiWorkItem extends WorkItem
{
	public enum WorkItemStatus {NEW, INPROGRESS, ONHOLD, COMPLETED, CANCELLED, DECLINED, FAILED}
	public enum WorkItemType {AUTHENTICATION_REQUEST, REVERSAL_REQUEST, SCHEDULED_REPORT, UNKNOWN}
	public enum WorkItemAction {STATUS, APPROVE, DECLINE, HOLD, UPDATE, EXECUTEWORKFLOW, UNHOLD, CANCELLED, LOCK}


	private WorkItemStatus workItemStatus;
	private WorkItemType  workItemType;
	private String workItemOTP;


	private WorkItemAction action = WorkItemAction.STATUS;
	private String createdByName;
	private int batchID;
	private WorkflowRequestType requestType;


	public GuiWorkItem setType(String type)
	{
		super.setType(type);
		switch (type)
		{
			case WorkItem.TYPE_AUTHENTICATION_REQUEST:
				workItemType = WorkItemType.AUTHENTICATION_REQUEST;
				break;
			case WorkItem.TYPE_REVERSAL_REQUEST:
				workItemType = WorkItemType.REVERSAL_REQUEST;
				break;
			case WorkItem.TYPE_SCHEDULED_REPORT:
				workItemType = WorkItemType.SCHEDULED_REPORT;
				break;
			default:
				workItemType = WorkItemType.UNKNOWN;
		}
		return this;
	}

	public GuiWorkItem setState(String state)
	{
		super.setState(state);
		switch (state)
		{
			case WorkItem.STATE_COMPLETED:
				workItemStatus = WorkItemStatus.COMPLETED;
				break;
			case WorkItem.STATE_DECLINED:
				workItemStatus = WorkItemStatus.DECLINED;
				break;
			case WorkItem.STATE_CANCELLED:
				workItemStatus = WorkItemStatus.CANCELLED;
				break;
			case WorkItem.STATE_ON_HOLD:
				workItemStatus = WorkItemStatus.ONHOLD;
				break;
			case WorkItem.STATE_IN_PROGRESS:
				workItemStatus = WorkItemStatus.INPROGRESS;
				break;
			case WorkItem.STATE_NEW:
			default:
				workItemStatus = WorkItemStatus.NEW;
		}
		return this;
	}
}
