package cs.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import hxc.ecds.protocol.rest.Permission;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties({"supplierOnly"})
public class GuiPermissionGroup extends Permission
{
	protected String groupName;
	protected List<GuiPermission> permissions = new ArrayList<GuiPermission>();
	
	public boolean addPermission(GuiPermission perm) {
		boolean result = true;
		permissions.add(perm);
		return result;
	}
	
	public boolean addPermission(Permission perm) {
		boolean result = true;
		GuiPermission guiPerm = new GuiPermission();
		guiPerm.setDescription(perm.getDescription());
		guiPerm.setId(perm.getId());
		guiPerm.setVersion(perm.getVersion());
		guiPerm.setName(perm.getName());
		permissions.add(guiPerm);
		return result;
	}
}
