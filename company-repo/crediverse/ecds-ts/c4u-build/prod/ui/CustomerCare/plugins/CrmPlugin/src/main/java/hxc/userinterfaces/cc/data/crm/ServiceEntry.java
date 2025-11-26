package hxc.userinterfaces.cc.data.crm;

public class ServiceEntry
{
	private String id;
	private String ctrl;

	public ServiceEntry()
	{
	}

	public ServiceEntry(String id, String ctrl)
	{
		this.id = id;
		this.ctrl = ctrl;
	}

	public String getId()
	{
		return id;
	}

	public String getCtrl()
	{
		return ctrl;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public void setCtrl(String ctrl)
	{
		this.ctrl = ctrl;
	}

}
