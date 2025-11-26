package hxc.utils.protocol.acip;

/**
 * UpdateTimeRestrictionResponse
 * 
 * This message handles both creation and updates to time restrictions. If a restriction id is given that does not exist the restriction will be created. If the corresponding restriction exists it
 * will be updated instead.
 */
public class UpdateTimeRestrictionResponse
{
	public UpdateTimeRestrictionResponseMember member;

	public UpdateTimeRestrictionResponse()
	{
		member = new UpdateTimeRestrictionResponseMember();
	}
}
