package hxc.utils.protocol.hmx;

public class Mnp
{
	// The MNP status of a subscriber
	// 0 Not known to be ported
	// 1 Own number ported out
	// 2 Foreign number ported to foreign network
	// 3 Own number not ported out
	// 4 Own foreign number ported in
	// Mandatory
	public int mnpStatusId;
}
