package hxc.utils.protocol.ucip;

/**
 * UpdateFaFListResponse
 * 
 * The message UpdateFaFList is used to update the Family and Friends list for either the account or subscriber. Note: Charged FaF number change is not supported on account level. It is only supported
 * on subscription level. The field fafIndicator in fafInformation is mandatory for non-charging operations, and it is optional for charged operations.
 */
public class UpdateFaFListResponse
{
	public UpdateFaFListResponseMember member;

	public UpdateFaFListResponse()
	{
		member = new UpdateFaFListResponseMember();
	}
}
