package hxc.utils.protocol.acip;

import hxc.utils.xmlrpc.XmlRpcMethod;

/**
 * UpdateAccumulatorsRequest
 * 
 * The message UpdateAccumulators performs an adjustment to the counter values of the chosen accumulators. It is possible to do a relative adjustment or an absolute adjustment of an accumulator value.
 * Relative adjustment of the accumulator value is possible to do both in positive and negative direction. The accumulator is cleared by setting the absolute accumulator value to 0. Note: It is only
 * allowed to do unified actions to multiple accumulators. This means that absolute and relative adjustments has to be ordered in separate requests. When using relative adjustment, negative or
 * positive adjustments of accumulator values has to be ordered in separate requests. It is not allowed to combine any of these types of actions in the same request. If additional conditions need to
 * be processed, the message offers the possibility to use an optional input parameter to be verified with the subscriber (serviceClassCurrent) configuration.
 */
@XmlRpcMethod(name = "UpdateAccumulators")
public class UpdateAccumulatorsRequest
{
	public UpdateAccumulatorsRequestMember member;

	public UpdateAccumulatorsRequest()
	{
		member = new UpdateAccumulatorsRequestMember();
	}
}
