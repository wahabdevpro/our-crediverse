package hxc.utils.protocol.acip;

/**
 * DeleteAccumulatorsResponse
 * 
 * This message is intended to remove one or more accumulators identified by their accumulatorID. If additional conditions need to be processed, the message offers the possibility to use optional
 * input parameters to be verified with the subscriber (serviceClassCurrent) and accumulator (accumulatorEndDate) configurations.
 */
public class DeleteAccumulatorsResponse
{
	public DeleteAccumulatorsResponseMember member;

	public DeleteAccumulatorsResponse()
	{
		member = new DeleteAccumulatorsResponseMember();
	}
}
