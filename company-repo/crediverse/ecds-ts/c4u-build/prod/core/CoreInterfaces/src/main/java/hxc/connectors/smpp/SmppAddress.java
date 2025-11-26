package hxc.connectors.smpp;

public class SmppAddress
{
	//
	private String address;
	public String getAddress()
	{
		return this.address;
	}
	public SmppAddress setAddress( String address )
	{
		this.address = address;
		return this;
	}

	private SmppTon type;
	public SmppTon getType()
	{
		return this.type;
	}
	public SmppAddress setType( SmppTon type )
	{
		this.type = type;
		return this;
	}

	private SmppNpi plan;
	public SmppNpi getPlan()
	{
		return this.plan;
	}
	public SmppAddress setPlan( SmppNpi plan )
	{
		this.plan = plan;
		return this;
	}

	// Constructors
	public SmppAddress( String address, SmppTon type, SmppNpi plan )
	{
		this.address = address;
		this.type = type;
		this.plan = plan;
	}
};
