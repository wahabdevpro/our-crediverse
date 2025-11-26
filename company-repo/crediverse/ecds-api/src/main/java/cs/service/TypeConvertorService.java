package cs.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiPermission;
import cs.dto.GuiPermissionGroup;
import cs.dto.GuiRole;
import cs.dto.error.GuiViolation;
import cs.dto.security.LoginSessionData;
import hxc.ecds.protocol.rest.Permission;
import hxc.ecds.protocol.rest.Role;
import hxc.ecds.protocol.rest.Violation;

@Service
public class TypeConvertorService
{
	private static final Logger logger = LoggerFactory.getLogger(TypeConvertorService.class);
	
	@Autowired
	ObjectMapper mapper;
	
	@Autowired
	AgentService agentService;
	
	@Autowired
	LoginSessionData sessionData;

	public static Integer timeOfDayStringToSeconds(String timeOfDayString, String fieldName, List<Violation> violations)
	{
		Objects.requireNonNull(timeOfDayString, "timeOfDayString may not be null");
		Objects.requireNonNull(fieldName, "fieldName may not be null");
		Objects.requireNonNull(violations, "violations may not be null");
		String[] split = timeOfDayString.split(":");
		boolean failure = false;
		Integer result = null;
		int initialViolationCount = violations.size();
		if (split.length == 3 )
		{
			try
			{
				int hours = Integer.valueOf(split[0]);
				int minutes = Integer.valueOf(split[1]);
				int seconds = Integer.valueOf(split[2]);
				if (hours < 0 || hours > 23 )
				{
					violations.add(new Violation(fieldName + "InvalidHour", fieldName + "String", timeOfDayString, String.format("Invalid hour, must be 00-23")));
					failure = true;
				}
				if (minutes < 0 || minutes > 59 )
				{
					violations.add(new Violation(fieldName + "InvalidMinute", fieldName + "String", timeOfDayString, String.format("Invalid minute, must be 00-59")));
					failure = true;
				}
				if (seconds < 0 || seconds > 59 )
				{
					violations.add(new Violation(fieldName + "InvalidSecond", fieldName + "String", timeOfDayString, String.format("Invalid second, must be 00-59")));
					failure = true;
				}
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
				simpleDateFormat.parse(timeOfDayString);
				if ( !failure ) result = hours * 3600 + minutes * 60 + seconds;
			}
			catch(NumberFormatException|ParseException exception)
			{
				violations.add(new Violation(fieldName + "Invalid", fieldName + "String", timeOfDayString, String.format("Must be in HH:MM:SS format")));
				failure = true;
			}
		}
		else
		{
			violations.add(new Violation(fieldName + "Invalid", fieldName + "String", timeOfDayString, String.format("Must be in HH:MM:SS format")));
			failure = true;
		}
		// If somehow there is no result and no violations ...
		if ( result == null && initialViolationCount == violations.size() )
		{
			violations.add(new Violation(fieldName + "Invalid", fieldName + "String", timeOfDayString, String.format("Could not decode value ...")));
			failure = true;
		}
		return result;
	}

	public static String timeOfDaySecondsToString( int timeOfDaySeconds )
	{
		int hour = (timeOfDaySeconds / (60 * 60 ));
		int minute = (timeOfDaySeconds % (60 * 60)) / 60;
		int second = (timeOfDaySeconds % (60 * 60)) % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}
	

	
	/*
	 * @param role is the original role from the backend seerver
	 * @param permmap is a map of permissions with the group as the key
	 * 
	 * This method will take the map of all available permissions and set 
	 * a boolean on each permission to indicate if the permission is 
	 * available to this user or not.
	 */
	public GuiRole getGuiRoleFromRole(Role role, Map<String, GuiPermissionGroup> permmap)
	{
		Map<String, Permission>actualPermissions = new HashMap<String, Permission>();
		GuiRole convertedRole = new GuiRole();
		
		for (Permission perm : role.getPermissions())
		{
			String key = perm.getGroup().toLowerCase()+perm.getName().toLowerCase();
			if (!key.contains("tiers")) // Used for testing
			actualPermissions.put(key, perm);
		}
		
		for (String key: permmap.keySet())
		{
			GuiPermissionGroup permGroup = permmap.get(key);
			for (GuiPermission perm: permGroup.getPermissions())
			{
				try
				{
					GuiPermission newPermission = (GuiPermission) perm.clone();
					String permName = key.toLowerCase() + newPermission.getName().toLowerCase();
					newPermission.setEnabled(actualPermissions.containsKey(permName));
					newPermission.setGroup(permGroup.getGroupName());
					convertedRole.addPermission(newPermission);
				}
				catch (CloneNotSupportedException e)
				{
					logger.error("", e);
				}
			}
		}
		convertedRole.setPermanent(role.isPermanent());
		convertedRole.setId(role.getId());
		convertedRole.setCompanyID(role.getCompanyID());
		convertedRole.setVersion(role.getVersion());
		convertedRole.setName(role.getName());
		convertedRole.setDescription(role.getDescription());
		return convertedRole;
	}

	public Role getRoleFromGuiRole(GuiRole role)
	{
		Role convertedRole = new Role();
		List<Permission> permissionSet = new ArrayList<Permission>();
		
		role.getPermissions().forEach((permission) ->{
			Permission perm = new Permission();
			perm.setDescription(permission.getDescription());
			perm.setId(permission.getId());
			perm.setName(permission.getName());
			perm.setSupplierOnly(permission.isSupplierOnly());
			perm.setVersion(permission.getVersion());
			perm.setGroup(permission.getGroup());
			permissionSet.add(perm);
		});
		convertedRole.setPermissions(permissionSet);
		convertedRole.setDescription(role.getDescription());
		if (role.getId() > 0) convertedRole.setId(role.getId());
		convertedRole.setName(role.getName());
		convertedRole.setType(role.getType());
		return convertedRole;
	}
	
	public GuiRole[] getGuiRoleFromRole(Role[] roles)
	{
		List<GuiRole>guiRoleList = new ArrayList<GuiRole>();
		
		List<Role> roleList = Arrays.asList(roles);
		roleList.forEach((role) ->{
			Map<String,ArrayNode>permMap = new TreeMap<String, ArrayNode>();
			role.getPermissions().forEach((perm) ->{
				String name = String.valueOf(perm.getGroup());
				ArrayNode jsonPermList = null;
				if (permMap.containsKey(name))
				{
					jsonPermList = permMap.get(name);
				}
				else
				{
					jsonPermList = mapper.createArrayNode();
				}
				ObjectNode jsonPerm = mapper.createObjectNode();
				
				jsonPerm.put("id", perm.getId());
				jsonPerm.put("group", perm.getGroup());
				jsonPerm.put("description", perm.getDescription());
				jsonPermList.add(jsonPerm);
				permMap.put(name, jsonPermList);
			});
			
			permMap.forEach((key, value) ->{
				GuiRole guiRole = new GuiRole();
				
				guiRole.setId(role.getId());
				guiRole.setCompanyID(role.getCompanyID());
				guiRole.setName(role.getName());
				guiRole.setDescription(role.getDescription());
				guiRole.setPermanent(role.isPermanent());
				guiRole.setPermissionGroup(key);
				
				guiRole.setPermList(value.toString());
				
				guiRoleList.add(guiRole);
			});
		});
		return guiRoleList.toArray(new GuiRole[guiRoleList.size()]);
	}

	public Role[] getRoleFromGuiRole(GuiRole[] roles)
	{
		List<Role>roleList = new ArrayList<Role>();
		for (GuiRole role : roles)
		{
			roleList.add(getRoleFromGuiRole(role));
		}
		
		return roleList.toArray(new Role[roleList.size()]);
	}

	/////////////////////////////

	public List<GuiViolation> convertToGuiViolation(List<Violation> violations, String violationId)
	{
		Map<String, GuiViolation> volationMap = new HashMap<>();
		if (violations != null) {
			for (Violation violation : violations) {
				String field = violation.getProperty();
				if (!volationMap.containsKey(field)) {
					volationMap.put(field, new GuiViolation(field, violationId));
				}
				volationMap.get(field).extractViolation(violation);
			}
		}
		return new ArrayList<GuiViolation>(volationMap.values());
	}
}
