package hxc.utils.protocol.uiconnector.response;

import hxc.connectors.ecds.tampercheck.ITamperedAccount;
import hxc.connectors.ecds.tampercheck.ITamperedAgent;
import hxc.connectors.ecds.tampercheck.ITamperedAuditEntry;
import hxc.connectors.ecds.tampercheck.ITamperedBatch;

public class GetEcdsTamperCheckResponse extends UiBaseResponse
{
	private static final long serialVersionUID = 655850442533235619L;
	private ITamperedAccount accounts[];
	private ITamperedAgent agents[];	
	private ITamperedAuditEntry auditEntries[];
	private ITamperedBatch batches[];

	public ITamperedAccount[] getTamperedAccounts()
	{
		return accounts;
	}

	public void setTamperedAccounts(ITamperedAccount[] accounts)
	{
		this.accounts = accounts;
	}
	
	public ITamperedAgent[] getTamperedAgents()
	{
		return agents;
	}

	public void setTamperedAgents(ITamperedAgent[] agents)
	{
		this.agents = agents;
	}
	
	public ITamperedAuditEntry[] getTamperedAuditEntries()
	{
		return auditEntries;
	}

	public void setTamperedAuditEntries(ITamperedAuditEntry[] auditEntries)
	{
		this.auditEntries = auditEntries;
	}
	
	public ITamperedBatch[] getTamperedBatches()
	{
		return batches;
	}

	public void setTamperedBatches(ITamperedBatch[] batches)
	{
		this.batches = batches;
	}
	
	public GetEcdsTamperCheckResponse(String userId, String sessionId)
	{
		super(userId, sessionId);
	}
}
