package hxc.services.ecds.util;

import hxc.services.ecds.model.IAgentUser;

public class Summariser
{
	public static String summarise(IAgentUser agentUser)
	{
		return summarise(agentUser, "");
	}

	public static String summarise(IAgentUser agentUser, String extra)
	{
		return String.format("%s@%s(companyId = '%s', id = '%s', domainAccountName = '%s', mobileNumber = '%s', state = '%s'%s%s)",
			agentUser.getClass().getName(), Integer.toHexString(agentUser.hashCode()),
			agentUser.getCompanyID(), agentUser.getId(), agentUser.getDomainAccountName(), agentUser.getMobileNumber(), agentUser.getState(),
			(extra.isEmpty() ? "" : ", "), extra);
	}
}
