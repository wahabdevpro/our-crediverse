package cs.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import cs.config.RestServerConfiguration;
import cs.template.CsRestTemplate;
import hxc.ecds.protocol.rest.Permission;

@Service
public class PermissionService
{
	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	private String restServerPerm;

	private Permission[] permissionList;
	private Map<String, Permission>permissionByName;
	private Map<String, Map<String, Permission>>permissionByGroup;

	@PostConstruct
	public void configure()
	{
		this.restServerPerm = restServerConfig.getRestServer() + restServerConfig.getPermurl();
		permissionByName = new HashMap<String, Permission>();
		permissionByGroup = new HashMap<String, Map<String, Permission>>();
	}

	private void updateGroup(Permission permission)
	{
		Map<String, Permission>group = permissionByGroup.get(permission.getGroup());
		if (group == null)
		{
			group = new HashMap<String, Permission>();
			permissionByGroup.put(permission.getGroup(), group);
		}
		group.put(permission.getName(), permission);
	}

	private void buildMaps()
	{
		if (permissionList != null)
		{
			for (Permission permission : permissionList)
			{
				String key = permission.getGroup()+permission.getName();
				if (!permissionByName.containsKey(key))
				{
					permissionByName.put(key, permission);
				}
				updateGroup(permission);
			}
		}
	}

	/*
	 *
	 */
	public Permission[] listPermissions() throws Exception
	{
		if (permissionList == null)
		{
			synchronized(this)
			{
				if (permissionList == null)
				{
					permissionList = restTemplate.execute(restServerPerm, HttpMethod.GET, Permission[].class);
					buildMaps();
				}
			}
		}
		return permissionList;
	}

	public Permission[] listNonSupplierPermissions() throws Exception
	{
		List<Permission> perms = Arrays.stream(listPermissions()).filter(perm -> !perm.isSupplierOnly()).collect(Collectors.toList());
		return perms.toArray(new Permission[perms.size()]);
	}

	public Permission getPermissionByName(String group, String name) throws Exception
	{
		listPermissions();
		return permissionByName.get(group+name);
	}

	public Permission[] getPermissionGroupByName(String group) throws Exception
	{
		listPermissions();
		return permissionByGroup.values().toArray(new Permission[permissionByGroup.size()]);// Done this way to avoid cast
	}

	public Integer getPermissionIdByName(String group, String name) throws Exception
	{
		Integer id = null;
		listPermissions();
		Permission perm = permissionByName.get(group+name);
		if (perm != null) id = perm.getId();
		return id;
	}

}
