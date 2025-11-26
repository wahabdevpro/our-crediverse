package hxc.utils.protocol.ucip;

/**
 * GetAccountServiceFeeDataResponse
 * 
 * The GetAccountServiceFeeData message is used to fetch service fee data tied to an account.
 */
public class GetAccountServiceFeeDataResponse
{
	public GetAccountServiceFeeDataResponseMember member;

	public GetAccountServiceFeeDataResponse()
	{
		member = new GetAccountServiceFeeDataResponseMember();
	}
}
