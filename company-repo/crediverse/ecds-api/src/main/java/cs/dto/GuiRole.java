package cs.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hxc.ecds.protocol.rest.Permission;
import hxc.ecds.protocol.rest.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GuiRole extends Role
{
	String permissionGroup;
	String permissionName;
	String permissionDescription;
	String permList = "";
	
	
	
	
	
	@JsonIgnore
	protected Map<String, GuiPermissionGroup> permissionGroups = new HashMap<String, GuiPermissionGroup>();
	protected List<GuiPermissionGroup> permissionGroupList = new ArrayList<GuiPermissionGroup>();
	
	@Getter
	@Setter
	@ToString
	static public class PermissionGroup
	{
		String groupName;
		List<Permission> permissions = new ArrayList<Permission>();
	}
	
	public boolean addPermission(GuiPermission perm) {
		boolean result = true;
		GuiPermissionGroup permissionGroup = permissionGroups.get(perm.getGroup());
		if (permissionGroup == null)
		{
			permissionGroup = new GuiPermissionGroup();
			permissionGroup.setGroupName(perm.getGroup());
			permissionGroups.put(permissionGroup.getGroupName(), permissionGroup);
			permissionGroupList.add(permissionGroup);
		}
		perm.setGroup(null);
		permissionGroup.addPermission(perm);
		return result;
	}
	
	public boolean addPermission(Permission perm) {
		boolean result = true;
		GuiPermissionGroup permissionGroup = permissionGroups.get(perm.getGroup());
		if (permissionGroup == null)
		{
			permissionGroup = new GuiPermissionGroup();
			permissionGroup.setGroupName(perm.getGroup());
			permissionGroups.put(permissionGroup.getGroupName(), permissionGroup);
			permissionGroupList.add(permissionGroup);
		}
		perm.setGroup(null);
		permissionGroup.addPermission(perm);
		return result;
	}
}
