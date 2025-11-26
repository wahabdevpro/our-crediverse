package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * GetDiscountInformationRequest
 * 
 * The message GetDiscountInformation retrieves discounts. Any number of discount IDs can be specified for retrieval. If no IDs are requested all the discounts will be returned.
 */
@XmlRpcMethod(name = "GetDiscountInformation")
public class GetDiscountInformationRequest
{
	public GetDiscountInformationRequestMember member;

	public GetDiscountInformationRequest()
	{
		member = new GetDiscountInformationRequestMember();
	}
}
