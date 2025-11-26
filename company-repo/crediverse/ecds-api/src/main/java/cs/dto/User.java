package cs.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cs.dto.security.GuiPermissions;
import hxc.ecds.protocol.rest.Permission;
import hxc.ecds.protocol.rest.Role;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class User implements UserDetails
{
	private static final long serialVersionUID = 3421682576624944678L;

	private int id;

	private String username;

	private String password;
	private String fullName;

	private boolean enabled;
	private boolean credentialsNonExpired;
	private boolean accountNonLocked;
	private boolean accountNonExpired;
	private String userMsisdn;
	private List<? extends Role> roles = new ArrayList<Role>();
	
	@JsonIgnore
	private List<Permission> permissions;
	
	@JsonIgnore
	private Set<String> permissionSet;
	
	public void addPermission(Permission perm)
	{
		// Old Method
		if (permissions == null)
		{
			permissions = new ArrayList<Permission>();
		}
		permissions.add(perm);

		
		// Mapping only Group and Name
		if (permissionSet == null) permissionSet = new HashSet<>();
		GuiPermissions gperms = new GuiPermissions(perm.getGroup(), perm.getName());
		
		if (gperms.getGroup() == GuiPermissions.PermissionGroup.UnKnown 
				|| gperms.getName() == GuiPermissions.PermissionName.Unknown)
		{
			LoggerFactory.getLogger(User.class).error( String.format("Group: '%s' and Permission: '%s', could not be converted to GuiPermissions", perm.getGroup(), perm.getName()) );
		}
		permissionSet.add( gperms.toString() );		
	}
	
	public void addCustomPermission(String group, String permission) 
	{
		if (permissionSet == null) permissionSet = new HashSet<>();
		permissionSet.add( String.format("%s_%s", group, permission) );		
	}
	
	public boolean hasPermission(String group, String name)
	{
		GuiPermissions checkPerm = new GuiPermissions(group, name);
		return ( (permissionSet != null) && (permissionSet.contains(checkPerm.toString())) );
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities()
	{
		// TODO Auto-generated method stub
		return AuthorityUtils.createAuthorityList("CANMANAGE");
	}
}
