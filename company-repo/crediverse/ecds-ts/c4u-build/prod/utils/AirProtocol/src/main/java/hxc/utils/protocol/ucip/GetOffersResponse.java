package hxc.utils.protocol.ucip;

/**
 * GetOffersResponse
 * 
 * The message GetOffers will return a list of offers currently assigned to an account. The detail level of the returned list can be specified in the request using various flags. To get
 * subDedicatedAccounts, both requestSubDedicatedAccountDetailsFlag and requestDedicatedAccountDetailsFlag must be set to "1". For product private (instantiated) DA:s, the GetOffers request should be
 * used to get the capabilities which the DA share with the Offer. Such data are start and expiry date, dateTime, state, offer type, PAM service and offerProviderID.
 */
public class GetOffersResponse
{
	public GetOffersResponseMember member;

	public GetOffersResponse()
	{
		member = new GetOffersResponseMember();
	}
}
