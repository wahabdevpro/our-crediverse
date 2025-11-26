package hxc.connectors.sut.database;

import java.math.BigInteger;
import java.util.Date;

import hxc.connectors.database.Column;
import hxc.connectors.database.Table;

@Table(name = "smsq_queue")
public class SmsqQueueRecord
{

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////

	@Column(primaryKey = true)
	public BigInteger id;

	@Column
	public BigInteger transaction_id;

	@Column(maxLength = 16)
	public String application;

	@Column(maxLength = 32)
	public String source_msisdn;

	@Column(maxLength = 32)
	public String destination_msisdn;

	@Column
	public int dcs_encoding;

	@Column(maxLength = 16384)
	public String message;

	@Column(maxLength = 256)
	public String kvpinfo;

	@Column
	public Date insertion_date;

	@Column
	public int start_hour;

	@Column
	public int end_hour;

	@Column
	public int ttl;

}
