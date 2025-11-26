package hxc.utils.protocol.acip;

/**
 * UpdateCommunicationIDResponse
 * 
 * The communication ID change operation changes the Communication ID. A Communication ID can be one or several out of MSISDN, NAI, IMSI, SIP-URI and PRIVATE. In order to change an identifier, both
 * the old and new identifier needs to be included. Example: imsiCurrentimsiNew. For simplicity NAI, IMSI, SIP-URI and PRIVATE are referred to as a group, extended address. Valid for all combinations
 * is that, in order for the operation to be successful; the new MSISDN and/or the new extended address cannot be occupied by another subscriber already. During a Communication ID change, where the
 * MSISDN is changed from current to new, only extended addresses included in the operation, will be connected to the new MSISDN by the operation. It is possible to have this change done afterwards by
 * running an offline job. It is possible to differentiate charging using the parameter chargingInformation, and to indicate that charging is done outside Charging Compound using externalContract
 * parameter.
 */
public class UpdateCommunicationIDResponse
{
	public UpdateCommunicationIDResponseMember member;

	public UpdateCommunicationIDResponse()
	{
		member = new UpdateCommunicationIDResponseMember();
	}
}
