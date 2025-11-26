package hxc.services.airsim.protocol;

public class TnpThreshold
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Enumerations
	//
	// /////////////////////////////////
	public enum TnpTriggerTypes
	{
		TRAFFIC, ADJ, BATCH, ADMIN;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	private int serviceClass;
	private int thresholdID;
	private String directory;
	private String version;
	private String senderID;
	private String receiverID;
	private int accountID;
	private double level;
	private boolean upwards;
	private TnpTriggerTypes triggerType;
	private int accountGroupID;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////
	public int getServiceClass()
	{
		return serviceClass;
	}

	public void setServiceClass(int serviceClass)
	{
		this.serviceClass = serviceClass;
	}

	public int getThresholdID()
	{
		return thresholdID;
	}

	public void setThresholdID(int thresholdID)
	{
		this.thresholdID = thresholdID;
	}

	public String getDirectory()
	{
		return directory;
	}

	public void setDirectory(String directory)
	{
		this.directory = directory;
	}

	public String getVersion()
	{
		return version;
	}

	public void setVersion(String version)
	{
		this.version = version;
	}

	public String getSenderID()
	{
		return senderID;
	}

	public void setSenderID(String senderID)
	{
		this.senderID = senderID;
	}

	public String getReceiverID()
	{
		return receiverID;
	}

	public void setReceiverID(String receiverID)
	{
		this.receiverID = receiverID;
	}

	public int getAccountID()
	{
		return accountID;
	}

	public void setAccountID(int accountID)
	{
		this.accountID = accountID;
	}

	public double getLevel()
	{
		return level;
	}

	public void setLevel(double level)
	{
		this.level = level;
	}

	public boolean isUpwards()
	{
		return upwards;
	}

	public void setUpwards(boolean upwards)
	{
		this.upwards = upwards;
	}

	public TnpTriggerTypes getTriggerType()
	{
		return triggerType;
	}

	public void setTriggerType(TnpTriggerTypes triggerType)
	{
		this.triggerType = triggerType;
	}

	public int getAccountGroupID()
	{
		return accountGroupID;
	}

	public void setAccountGroupID(int accountGroupID)
	{
		this.accountGroupID = accountGroupID;
	}

}
