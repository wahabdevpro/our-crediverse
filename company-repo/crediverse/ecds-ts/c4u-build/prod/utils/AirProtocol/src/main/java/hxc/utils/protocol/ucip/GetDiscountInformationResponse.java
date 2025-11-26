package hxc.utils.protocol.ucip;

/**
 * GetDiscountInformationResponse
 * 
 * The message GetDiscountInformation retrieves discounts. Any number of discount IDs can be specified for retrieval. If no IDs are requested all the discounts will be returned.
 */
public class GetDiscountInformationResponse
{
	public GetDiscountInformationResponseMember member;

	public GetDiscountInformationResponse()
	{
		member = new GetDiscountInformationResponseMember();
	}
}
