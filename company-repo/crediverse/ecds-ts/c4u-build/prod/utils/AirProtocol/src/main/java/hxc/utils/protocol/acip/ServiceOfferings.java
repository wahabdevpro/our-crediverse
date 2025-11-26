package hxc.utils.protocol.acip;

import hxc.connectors.air.Air;

/**
 * ServiceOfferings
 * 
 * The serviceOfferings parameter contains the values of the service offerings defined on an account. It has to be matched to the definitions of the tariff tree. It is enclosed in a <struct> of its
 * own. The structs are placed in an <array> with maximum 31 entries.
 */
public class ServiceOfferings
{
	/*
	 * The serviceOfferingID parameter contains the identity of a current service offering defined on an account.
	 */
	@Air(Mandatory = true, Range = "1:31")
	public int serviceOfferingID;

	/*
	 * The serviceOfferingActiveFlag indicates if a specific service offering pointed out by the serviceOfferingID parameter is active or not.
	 */
	@Air(Mandatory = true)
	public boolean serviceOfferingActiveFlag;

}
