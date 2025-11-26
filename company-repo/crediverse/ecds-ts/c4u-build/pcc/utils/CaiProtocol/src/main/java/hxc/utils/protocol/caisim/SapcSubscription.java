package hxc.utils.protocol.caisim;

import java.util.HashMap;

/**
 * Represents a subscriber's CAI subscription in CAISIM.
 * 
 * @author petar
 *
 */
public class SapcSubscription implements Cloneable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	private HashMap<String, SapcGroup> groups = new HashMap<String, SapcGroup>();

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	// /////////////////////////////////
	
	/**
	 * Adds the specified SAPC Group to the existing groups in the subscription.
	 * If the group already exists in the subscription (based on ID) and the preserveQuota flag
	 * is set, then the quota will be set to the value prior to adding. Other SapcGroup members are
	 * not preserved.
	 * 
	 * @param group the SapcGroup to add
	 * @param preserveQuota a flag to indicate whether the SAPC quota value us to be preserved
	 */
	public void addGroup(SapcGroup group, boolean preserveQuota)
	{
		if (preserveQuota)
		{
			SapcGroup old = this.groups.get(group.getId());
			if (old != null)
				group.setQuota(old.getQuota());
		}
		this.groups.put(group.getId(), group);
	}

	/**
	 * @see addGroup
	 * @param groups a list of SapcGroups to add
	 * @param preserveQuota a flag to indicate whether the SAPC quota value us to be preserved
	 */
	public void addGroups(SapcGroup groups[], boolean preserveQuota)
	{
		for (SapcGroup g : groups)
		{
			addGroup(g, preserveQuota);
		}
	}

	/**
	 * Sets the passed list of SapcGroups to the subscription. The list of groups in the subscription
	 * will be exactly the same as the one passed (nothing is preserved).
	 * 
	 * @param groups a list of SapcGroups to set
	 */
	public void setGroups(SapcGroup groups[])
	{
		this.groups.clear();
		addGroups(groups, false);
	}
	
	public void deleteGroup(SapcGroupId group)
	{
		groups.remove(group.getId());
	}
	
	public void deleteGroups(DeletedSapcGroups deletedGroups)
	{
		for (int i = 0; i < deletedGroups.length(); ++i)
		{
			this.groups.remove(deletedGroups.at(i));
		}		
	}

	public SapcGroup[] getGroups()
	{
		SapcGroup[] r = new SapcGroup[this.groups.size()];
		int idx = 0;
		for (SapcGroup g : this.groups.values())
		{
			r[idx++] = g;
		}
		
		return r;
	}
	
	public SapcGroup getGroup(SapcGroupId group)
	{
		return groups.get(group.getId());
	}
	
	@Override
	public Object clone()
	{
		SapcSubscription ret = new SapcSubscription();
		
		for (SapcGroup group : groups.values())
		{
			// no need to set the preserveQuota flag here as we copy to a new subscription anyway
			ret.addGroup((SapcGroup) group.clone(), false);
		}
		
		return ret;
	}
}
