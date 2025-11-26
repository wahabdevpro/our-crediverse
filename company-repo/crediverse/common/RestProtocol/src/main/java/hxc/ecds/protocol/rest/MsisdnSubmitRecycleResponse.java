package hxc.ecds.protocol.rest;

import java.util.List;

@Deprecated
/*
 * Functionality on hold MSISDN-RECYCLING
 */
public class MsisdnSubmitRecycleResponse extends ResponseHeader{
	//
	// /////////////////////////////////
	protected List<Integer> agentsRecycled;
	protected List<Integer> nonRecyclableAgents;
	protected List<MsisdnRecycleIssue> issues;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public List<MsisdnRecycleIssue> getIssues()
	{
		return issues;
	}

	public void setIssues(List<MsisdnRecycleIssue> issues)
	{
		this.issues = issues;
	}

	public List<Integer> getAgentsRecycled() {
		return agentsRecycled;
	}

	public void setAgentsRecycled(List<Integer> agentsRecycled) {
		this.agentsRecycled = agentsRecycled;
	}

	public List<Integer> getNonRecyclableAgents() {
		return nonRecyclableAgents;
	}

	public void setNonRecyclableAgents(List<Integer> nonRecyclableAgents) {
		this.nonRecyclableAgents = nonRecyclableAgents;
	}



	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////
	public MsisdnSubmitRecycleResponse()
	{

	}

	public MsisdnSubmitRecycleResponse(MsisdnSubmitRecycleRequest request)
	{
		super(request);
	}

}
