package hxc.utils.protocol.uiconnector.bam;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "bm_layout")
public class LayoutRecord
{
	@Column(primaryKey = true, maxLength = 15)
	public String userId;

	@Column(maxLength = 65535, nullable = true)
	public String layout;
}
