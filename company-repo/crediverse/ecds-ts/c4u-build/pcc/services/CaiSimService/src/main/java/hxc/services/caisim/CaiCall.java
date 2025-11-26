package hxc.services.caisim;

/**
 * Represents a CAI call in the SOAP interface.
 * 
 * NOTE1: The '_' character is used, because the ':' can't be used in the enum name. It is replaced
 * in the toString() method and that's how it is inserted in the injected responses maps.
 * 
 * NOTE2: Always use a single '_' character in the enum name.
 * @author petar
 *
 */
public enum CaiCall
{
	SET_SAPCSUB,
	GET_SAPCACCUMULATEDUSAGE,
	SET_HLRSUB,
	GET_HLRSUB,
	DUMMYCMD1_DUMMY,
	DUMMYCMD2_DUMMY,
	DUMMYCMD3_DUMMY,
	DUMMYCMD4_DUMMY,
	DUMMYCMD5_DUMMY,
	DUMMYCMD6_DUMMY,
	DUMMYCMD7_DUMMY,
	DUMMYCMD8_DUMMY,
	DUMMYCMD9_DUMMY,
	DUMMYCMD10_DUMMY,
	DUMMYCMD11_DUMMY,
	DUMMYCMD12_DUMMY,
	DUMMYCMD13_DUMMY,
	DUMMYCMD14_DUMMY,
	DUMMYCMD15_DUMMY,
	DUMMYCMD16_DUMMY,
	DUMMYCMD17_DUMMY,
	DUMMYCMD18_DUMMY,
	DUMMYCMD19_DUMMY,
	DUMMYCMD20_DUMMY;
	
	@Override
	public String toString()
	{
		return name().replace('_', ':');
	}
}
