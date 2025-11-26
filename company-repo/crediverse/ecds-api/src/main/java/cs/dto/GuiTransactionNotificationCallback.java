package cs.dto;

import java.util.ArrayList;

public class GuiTransactionNotificationCallback {
	ArrayList<GuiTransaction> transactions = new ArrayList<GuiTransaction>();
	String sessionID;
	int agentID;
	
	public ArrayList<GuiTransaction> getTransactions()
	{
		return transactions;
	}
	
	public void setTransactions(ArrayList<GuiTransaction> transactions)
	{
		this.transactions = transactions;
	}
	
	public String getSessionID()
	{
		return sessionID;
	}
	
	public void setSessionID(String sessionID)
	{
		this.sessionID = sessionID;
	}
	
	public int getAgentID()
	{
		return agentID;
	}
	
	public void setAgentID(int agentID)
	{
		this.agentID = agentID;
	}
}
