package hxc.connectors.datawarehouse;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "dw_cdrs")
public class CdrTracker
{
	@Column(primaryKey = true, maxLength = 24)
	public String transactionID;
}
