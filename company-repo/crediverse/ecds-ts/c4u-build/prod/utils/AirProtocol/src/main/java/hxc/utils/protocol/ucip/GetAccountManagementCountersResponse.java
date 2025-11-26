package hxc.utils.protocol.ucip;

/**
 * GetAccountManagementCountersResponse
 * 
 * The message GetAccountManagementCounters will return account management counters.
 */
public class GetAccountManagementCountersResponse
{
	public GetAccountManagementCountersResponseMember member;

	public GetAccountManagementCountersResponse()
	{
		member = new GetAccountManagementCountersResponseMember();
	}
}
