package hxc.utils.protocol.hmx;

public class State
{
	// This parameter indicates the current state of the subscriber.
	// 0 Assumed Idle
	// 1 CAMEL Busy
	// 2 Detached
	// 3 Attached, not reachable for paging
	// 4 Attached, reachable for paging
	// 5 PDP Attached, not reachable for paging
	// 6 PDP Attached, reachable for paging
	// 7 Not provided from VLR
	// 8 Not provided from SGSN
	// 9 Not reachable - MS Purged
	// 10 Not reachable - IMSI Detached
	// 11 Not reachable - Restricted Area
	// 12 Not reachable - Not registered
	// Mandatory
	public int stateId;
}
