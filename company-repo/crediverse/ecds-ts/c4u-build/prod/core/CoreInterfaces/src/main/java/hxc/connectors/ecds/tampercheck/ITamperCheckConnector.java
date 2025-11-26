package hxc.connectors.ecds.tampercheck;

public interface ITamperCheckConnector
{
	public ITamperedAgent[] getTamperedAgents();
	
	public ITamperedAccount [] getTamperedAccounts();
	
	public ITamperedBatch[] getTamperedBatches();
	
	public ITamperedAuditEntry [] getTamperedAuditEntries();
	
	public boolean resetAgents();
	
	public boolean resetAccounts();
	
	public boolean resetBatches();
	
	public boolean resetAuditEntries();
	
	public boolean resetAgent(String msisdn);
	
	public boolean resetAccount(String msisdn);
	
	public int checkTamperedAgent(String msisdn);
}
