package hxc.utils.protocol.ucip;

/**
 * GetFaFListResponse
 * 
 * The GetFaFList message is used to fetch the list of Family and Friends numbers with attached FaF indicators.
 */
public class GetFaFListResponse
{
	public GetFaFListResponseMember member;

	public GetFaFListResponse()
	{
		member = new GetFaFListResponseMember();
	}
}
