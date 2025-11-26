package hxc.connectors.sut.database;

import java.util.Date;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "ash_subscription")
public class LifeCycleRecord
{
	@Column(primaryKey = true, maxLength = 16)
	public String msisdn;

	@Column(primaryKey = true, maxLength = 16)
	public String tag;

	@Column(primaryKey = true, maxLength = 32)
	public String instance;

	public Date time_created;

	public Date time_subscribe;

	public Date time_last_renew;

	public Date time_expiry;

	public Date time_removal;

	public String user_data;

	@Column(maxLength = 5)
	public String auto_renewal;

	public int last_attempt_renewal;

	public Date next_attempt_renewal;

	public String pending_cancellation;

	public int last_reminder;

	@Column(maxLength = 1)
	public String status;

	public int failure_count;

	public Date ts;
}
