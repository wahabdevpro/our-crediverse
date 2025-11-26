package hxc.ecds.protocol.rest;

public class AgentAccount extends Account
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private Agent agent;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public Agent getAgent()
	{
		return agent;
	}

	public AgentAccount setAgent(Agent agent)
	{
		this.agent = agent;
		return this;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Construction
	//
	// /////////////////////////////////

	// Default
	public AgentAccount()
	{
	}

	// Copy
	public AgentAccount(Account that)
	{
		this.agentID = that.agentID;
		this.version = that.version;
		this.balance = that.balance;
		this.bonusBalance = that.bonusBalance;
		this.onHoldBalance = that.onHoldBalance;
		this.signature = that.signature;
		this.tamperedWith = that.tamperedWith;
		this.day = that.day;
		this.dayCount = that.dayCount;
		this.dayTotal = that.dayTotal;
		this.monthCount = that.monthCount;
		this.monthTotal = that.monthTotal;
	}

}
