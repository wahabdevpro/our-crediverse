package hxc.connectors.smpp;

// http://opensmpp.org/specs/SMPP_v3_4_Issue1_2.pdf : 5.2.5

public enum SmppTon
{
	UNKNOWN( 0b00000000 ),
	INTERNATIONAL( 0b00000001 ),
	NATIONAL( 0b00000010 ),
	NETWORK_SPECIFIC( 0b00000011 ),
	SUBSCRIBER_NUMBER( 0b00000100 ),
	ALPHANUMERIC( 0b00000101 ),
	ABBREVIATED( 0b00000110 );

	private final int value;

	private SmppTon(final int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
};
