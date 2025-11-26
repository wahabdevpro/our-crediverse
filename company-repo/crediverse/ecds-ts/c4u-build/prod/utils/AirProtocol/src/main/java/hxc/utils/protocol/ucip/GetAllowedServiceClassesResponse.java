package hxc.utils.protocol.ucip;

/**
 * GetAllowedServiceClassesResponse
 * 
 * The GetAllowedServiceClasses message is used to fetch a list of service classes the subscriber is allowed to change to.
 */
public class GetAllowedServiceClassesResponse
{
	public GetAllowedServiceClassesResponseMember member;

	public GetAllowedServiceClassesResponse()
	{
		member = new GetAllowedServiceClassesResponseMember();
	}
}
