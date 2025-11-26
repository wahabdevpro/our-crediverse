package hxc.ecds.protocol.rest;

import java.util.List;

@Deprecated
/*
 * Functionality on hold
 */
public class MsisdnRecycleResponse extends BatchUploadResponse{
	//
	// /////////////////////////////////
	protected List<AgentAccountInfo> agentAccountInfoList;
	protected BatchIssue[] issues;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public BatchIssue[] getIssues()
	{
		return issues;
	}

	public MsisdnRecycleResponse setIssues(BatchIssue[] issues)
	{
		this.issues = issues;
		return this;
	}

	public List<AgentAccountInfo> getAgentAccountInfoList() {
		return agentAccountInfoList;
	}

	public void setAgentAccountInfoList(List<AgentAccountInfo> agentAccountInfoList) {
		this.agentAccountInfoList = agentAccountInfoList;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public MsisdnRecycleResponse()
	{

	}

	public MsisdnRecycleResponse(BatchUploadRequest request)
	{
		super(request);
	}

}
