package hxc.services.ecds.olapmodel;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import hxc.services.ecds.model.Group;
import hxc.services.ecds.util.RequiresTransaction;

@Table(name = "ap_group", uniqueConstraints = { @UniqueConstraint(name = "ap_group_name", columnNames = { "comp_id", "name" }) },
	indexes = {
	}
)
@Entity
//@NamedQueries({ @NamedQuery(name = "OlapGroup.replace", query = "REPLACE INTO OlapGroup (d, comp_id, name) VALUES(:id, :comp_id, :name)") })
public class OlapGroup implements Serializable
{
	private static final long serialVersionUID = -8100837629032701137L;

	protected int id;
	protected int companyID;
	protected String name;

	@Id
	public int getId()
	{
		return this.id;
	}

	public OlapGroup setId(int id)
	{
		this.id = id;
		return this;
	}

	@Column(name = "comp_id", nullable = false)
	public int getCompanyID()
	{
		return this.companyID;
	}

	public OlapGroup setCompanyID(int companyID)
	{
		this.companyID = companyID;
		return this;
	}

	@Column(name = "name", nullable = false, length = Group.NAME_MAX_LENGTH)
	public String getName()
	{
		return this.name;
	}

	public OlapGroup setName(String name)
	{
		this.name = name;
		return this;
	}


	// Constructors

	public OlapGroup()
	{
	}
	
	public OlapGroup(Group group)
	{
		this.id = group.getId();
		this.companyID = group.getCompanyID();
		this.name = group.getName();
	}

	public OlapGroup( int id, int companyID, String name )
	{
		this.id = id;
		this.companyID = companyID;
		this.name = name;
	}

	public String describe(String extra)
	{
		return String.format("%s@%s("
			+ "id = '%s', companyID = '%s', number = '%s' "
			+ "%s%s)",
			this.getClass().getName(), Integer.toHexString(this.hashCode()),
			id, companyID, name,
			(extra.isEmpty() ? "" : ", "), extra);
	}

	public String describe()
	{
		return this.describe("");
	}

	public String toString()
	{
		return this.describe();
	}

	// Statics

	public static int synchronize(EntityManager oltpEm, EntityManager apEm)
	{
		int count = 0;
		int start = 0;
		int limit = 10000;
		do
		{
			List<Group> groups = (List<Group>)oltpEm.createQuery("SELECT g FROM Group g").setFirstResult(start).setMaxResults(limit).getResultList(); 
			count = groups.size();
			Iterator i = groups.iterator();
			Group group;
		
			try (RequiresTransaction scope = new RequiresTransaction(apEm))
			{
				try
				{
					while(i.hasNext())
					{
						group = (Group)i.next();
			
						OlapGroup olapGroup = new OlapGroup();
						olapGroup.setId(group.getId());
						olapGroup.setCompanyID(group.getCompanyID());
						olapGroup.setName(group.getName());
						apEm.merge(olapGroup);
					}
				}	
				finally
				{
					scope.commit();
				}
			}	

			start += count;
		}
		while(count == limit);

		return start;
	}
}

