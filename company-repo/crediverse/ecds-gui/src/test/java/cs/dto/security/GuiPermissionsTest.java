package cs.dto.security;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import hxc.ecds.protocol.rest.Permission;

public class GuiPermissionsTest
{

	private List<Field> extractStaticFields(String prefix,Set<String> ignore)
	{
		if (ignore == null)
			ignore = new HashSet<String>();

		List<Field> staticFields = new ArrayList<Field>();

		Field[] declaredFields = Permission.class.getDeclaredFields();

		for (Field field : declaredFields)
		{
			if (Modifier.isStatic(field.getModifiers())
					&& field.getName().startsWith(prefix)
					&& !ignore.contains(field.getName()))
			{
				staticFields.add(field);
			}
		}

		return staticFields;
	}

	private String buildFailMessage(String what, List<String> errors)
	{
		StringBuilder sb = new StringBuilder();
		sb.append( String.format("The following %s in (hxc.ecds.protocol.rest.Permission) are not assigned in (hxc.ecds.protocol.rest.Permission.GuiPermissions)%n", what) );
		for(String error : errors)
		{
			sb.append( String.format("%s%n", error));
		}
		sb.append( String.format("Asign new Group Permissions in hxc.ecds.protocol.rest.Permission.GuiPermissions%n", what) );
		return sb.toString();
	}

	@Test
	public void testAllPermissionGroupsAssigned() throws Exception
	{
		Set<String> ignore = new HashSet<>(Arrays.asList( new String [] {"GROUP_MAX_LENGTH"} ));
		List<String> groupsNotAssigned = new ArrayList<String>();
		GuiPermissions guiPerms = new GuiPermissions();

		for(Field field : extractStaticFields("GROUP_", ignore))
		{
			String value = (String) field.get(guiPerms);
			if (GuiPermissions.PermissionGroup.getPermissionGroup(value) == GuiPermissions.PermissionGroup.UnKnown)
			{
				groupsNotAssigned.add(String.format("%s -> %s", field.getName(), value));
			}
		}

		if (groupsNotAssigned.size() > 0)
		{
			fail( buildFailMessage("Permission Groups (GROUP_)", groupsNotAssigned) );
		}
	}


	@Test
	public void testAllPermissionsAssigned() throws Exception
	{
		List<String> permsNotAssigned = new ArrayList<String>();
		GuiPermissions guiPerms = new GuiPermissions();

		for(Field field : extractStaticFields("PERM_", null))
		{
			String value = (String) field.get(guiPerms);
			if (GuiPermissions.PermissionName.getPermissionName(value) == GuiPermissions.PermissionName.Unknown)
			{
				permsNotAssigned.add(String.format("%s -> %s", field.getName(), value));
			}
		}

		if (permsNotAssigned.size() > 0)
		{
			fail( buildFailMessage("PermissionsName (PERM_)", permsNotAssigned) );
		}
	}

}
