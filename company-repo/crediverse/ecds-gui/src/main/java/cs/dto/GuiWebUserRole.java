package cs.dto;

import java.util.ArrayList;
import java.util.List;

import hxc.ecds.protocol.rest.Permission;
import hxc.ecds.protocol.rest.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuiWebUserRole {

	private int id;
	private int version;
	private int companyID;

	private String name;
	private String description;

	private boolean permanent;
	private boolean hasRole;

	private List<Permission> permissions;

	public void importRole(Role role)
	{
		this.id = role.getId();
		this.companyID = role.getCompanyID();
		this.version = role.getId();
		this.name = role.getName();
		this.description = role.getDescription();
		this.permanent = role.isPermanent();
		this.permissions = new ArrayList<>();

		for(Permission permission : role.getPermissions())
		{
			Permission perm = new Permission();
			perm.setDescription(permission.getDescription());
			perm.setId(permission.getId());
			perm.setName(permission.getName());
			perm.setSupplierOnly(permission.isSupplierOnly());
			perm.setVersion(permission.getVersion());
			perm.setGroup(perm.getGroup());

			this.permissions.add(perm);
		}
	}

	public Role exportRole()
	{
		Role result = new Role();

		result.setId(this.id);
		result.setCompanyID(this.companyID);
		result.setVersion(this.version);
		result.setName(this.name);
		result.setDescription(this.description);
		result.setPermanent(this.permanent);
		result.setPermissions(this.permissions);

		return result;
	}
}
