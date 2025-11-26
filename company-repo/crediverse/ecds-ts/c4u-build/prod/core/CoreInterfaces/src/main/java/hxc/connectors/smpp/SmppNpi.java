package hxc.connectors.smpp;

// http://opensmpp.org/specs/SMPP_v3_4_Issue1_2.pdf : 5.2.6

public enum SmppNpi
{
	UNKNOWN( 0b00000000 ),
	ISDN( 0b00000001 ),
	DATA( 0b00000011 ),
	TELEX( 0b00000100 ),
	LAND_MOBILE( 0b00000110 ),
	NATIONAL( 0b00001000 ),
	PRIVATE( 0b00001001 ),
	ERMES( 0b00001010 ),
	INTERNET( 0b00001110 ),
	WAP( 0b00010010 );

	private final int value;

	private SmppNpi(final int value)
	{
		this.value = value;
	}

	public int getValue()
	{
		return value;
	}
}
