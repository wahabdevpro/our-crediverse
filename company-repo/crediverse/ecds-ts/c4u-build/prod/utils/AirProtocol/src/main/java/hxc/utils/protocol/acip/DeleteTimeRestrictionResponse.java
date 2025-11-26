package hxc.utils.protocol.acip;

/**
 * DeleteTimeRestrictionResponse
 * 
 * This message removes any number of time restrictions. If no identifier is given all existing restrictions will be deleted.
 */
public class DeleteTimeRestrictionResponse
{
	public DeleteTimeRestrictionResponseMember member;

	public DeleteTimeRestrictionResponse()
	{
		member = new DeleteTimeRestrictionResponseMember();
	}
}
