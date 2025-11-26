package hxc.utils.protocol.ucip;

/**
 * GetBalanceAndDateResponse
 * 
 * The message GetBalanceAndDate is used to perform a balance enquiry on the account associated with a specific subscriber identity. Also lifecycle dates are presented. Information is given on both
 * main and dedicated accounts. Note: If pre-activation is wanted then messageCapabilityFlag.accountActivati onFlag should be included set to 1. For a product private (instantiated) DA, the
 * GetBalanceAndDate request should be used to only get their instance ID (productID). To get the capabilities which the DA share with the Offer, use the GetOffers request.
 */
public class GetBalanceAndDateResponse
{
	public GetBalanceAndDateResponseMember member;

	public GetBalanceAndDateResponse()
	{
		member = new GetBalanceAndDateResponseMember();
	}
}
