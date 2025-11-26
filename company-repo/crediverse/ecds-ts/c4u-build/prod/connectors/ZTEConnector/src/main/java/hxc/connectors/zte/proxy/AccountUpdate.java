package hxc.connectors.zte.proxy;

import java.util.Date;

public class AccountUpdate
{
	public int id;
	public Integer unitType;
	public Long valueRelative;
	public Long valueNew;
	public Integer expiryRelative;
	public Date expiryNew;
	public Integer startRelative;
	public Date startNew;

	public AccountUpdate(int id, Integer unitType, Long valueRelative, Long valueNew, Integer expiryRelative, Date expiryNew, //
			Integer startRelative, Date startNew)
	{
		this.id = id;
		this.unitType = unitType;
		this.valueRelative = valueRelative;
		this.valueNew = valueNew;
		this.expiryRelative = expiryRelative;
		this.expiryNew = expiryNew;
		this.startRelative = startRelative;
		this.startNew = startNew;
	}

}
