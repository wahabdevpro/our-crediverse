package hxc.utils.protocol.ucip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateFaFListRequest
 * 
 * The message UpdateFaFList is used to update the Family and Friends list for either the account or subscriber. Note: Charged FaF number change is not supported on account level. It is only supported
 * on subscription level. The field fafIndicator in fafInformation is mandatory for non-charging operations, and it is optional for charged operations.
 */
@XmlRpcMethod(name = "UpdateFaFList")
public class UpdateFaFListRequest
{
	public UpdateFaFListRequestMember member;

	public UpdateFaFListRequest()
	{
		member = new UpdateFaFListRequestMember();
	}
}
