package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * DeleteAccumulatorsRequest
 * 
 * This message is intended to remove one or more accumulators identified by their accumulatorID. If additional conditions need to be processed, the message offers the possibility to use optional
 * input parameters to be verified with the subscriber (serviceClassCurrent) and accumulator (accumulatorEndDate) configurations.
 */
@XmlRpcMethod(name = "DeleteAccumulators")
public class DeleteAccumulatorsRequest
{
	public DeleteAccumulatorsRequestMember member;

	public DeleteAccumulatorsRequest()
	{
		member = new DeleteAccumulatorsRequestMember();
	}
}
