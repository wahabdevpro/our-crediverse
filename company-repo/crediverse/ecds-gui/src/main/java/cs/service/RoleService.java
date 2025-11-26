package cs.service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.ApplicationDetailsConfiguration;
import cs.config.RestServerConfiguration;
import cs.dto.User;
import cs.dto.error.GuiValidationException;
import cs.dto.error.GuiViolation.ViolationType;
import cs.dto.security.LoginSessionData;
import cs.dto.users.GuiUserRole;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Permission;
import hxc.ecds.protocol.rest.Role;
import hxc.ecds.protocol.rest.Violation;

@Service
public class RoleService extends Exportable
{
	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private ApplicationDetailsConfiguration appConfig;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private LoginSessionData sessionData;

	private boolean configured = false;

	private String restServerUrl;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerUrl = restServerConfig.getRestServer() + restServerConfig.getRoleurl();
			configured = true;
		}
	}

	public Role getRole(String roleid) throws Exception
	{
		return restTemplate.execute(restServerUrl+"/"+roleid, HttpMethod.GET, Role.class);
	}

	public Role getRole(int roleid) throws Exception
	{
		return restTemplate.execute(restServerUrl+"/"+roleid, HttpMethod.GET, Role.class);
	}

	public Role[] listRoles() throws Exception
	{
		Role[] response = null;
		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Role[].class);
		return response;
	}

	public Role[] filterRoles(String filter) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		if (filter != null && !filter.isEmpty())
			RestRequestUtil.standardFilter(uri, filter);

		Role[] response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Role[].class);
		return response;
	}

	public Role[] listRoles(int start, int length) throws Exception
	{
		Role[] response = null;
		StringBuilder urlString = new StringBuilder(restServerUrl);
		urlString.append("?");
		urlString.append("first=");
		urlString.append(String.valueOf(start));
		urlString.append("&max=");
		urlString.append(String.valueOf(length));

		response = restTemplate.execute(restServerUrl, HttpMethod.GET, Role[].class);
		return response;
	}

	public void create(Role newRole) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, newRole, Void.class);
	}

	public void delete(String roleid) throws Exception
	{
		restTemplate.execute(restServerUrl+"/"+roleid, HttpMethod.DELETE, Void.class);
	}

	public void update(Role updatedRole) throws Exception
	{
		restTemplate.execute(restServerUrl, HttpMethod.PUT, updatedRole, Void.class);
	}

	public void updateDetails(Role updatedRole) throws Exception
	{
		Role currentRole = getRole(String.valueOf(updatedRole.getId()));
		if (currentRole != null)
		{
			currentRole.setName(updatedRole.getName());
			currentRole.setDescription(updatedRole.getDescription());
			currentRole.setType(updatedRole.getType());
			restTemplate.execute(restServerUrl, HttpMethod.PUT, currentRole, Void.class);
		}
	}

	public Role getRoleByName(String name) throws Exception
	{
		Role response = null;
		Role[]responseList = null;
		String companyID = Integer.toString(appConfig.getCompanyid());
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerUrl);
		RestRequestUtil.standardFilter(uri, "name='"+name+"'+companyID='"+ companyID +"'");
		responseList = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Role[].class);
		response = responseList[0];

		//response = restTemplate.execute(restServerUrl+"/"+agentId, HttpMethod.GET, Agent.class);
		return response;
	}

	public GuiUserRole[] getGuiUserRoles() throws Exception
	{
		List<GuiUserRole>guiUserRoles = new ArrayList<GuiUserRole>();
		Role[] roleList = listRoles();

		if (roleList != null)
		{
			for (Role role : roleList)
			{
				GuiUserRole currentRole = new GuiUserRole();
				currentRole.setId(role.getId());
				currentRole.setName(role.getName());
				guiUserRoles.add(currentRole);
			}
		}

		return guiUserRoles.toArray(new GuiUserRole[guiUserRoles.size()]);
	}

	private boolean roleMatches(Role role, String query)
	{
		boolean result = false;
		StringBuilder detail = new StringBuilder(role.getName());
		detail.append(role.getDescription());
		detail.append(role.getType());
		detail.append(role.getId());
		if (detail.toString().toLowerCase().contains(query.toLowerCase()))
		{
			result = true;
		}
		return result;
	}

	//NotOverride
	public String listAsCsv(String query) throws Exception
	{
		User currentUser = sessionData.getCurrentUser();
		if (!currentUser.hasPermission(Permission.GROUP_ROLES, Permission.PERM_DOWNLOAD))
		{
			throw new GuiValidationException(Arrays.asList(new Violation[] {new Violation(ViolationType.forbidden.toString(), null, null, "FORBIDDEN")}));
		}
		boolean filterSupplier = true;
		for (Role role : currentUser.getRoles())
		{
			if (role.getName().toLowerCase().equals("supplier"))
			{
				filterSupplier = false;
			}
		}
		List<String> headers = new ArrayList<String>();
		Map<String, Role>orderedRoles = new TreeMap<String, Role>();
		Map<String, Permission>orderedPermissions = new TreeMap<String, Permission>();

		headers.add("permission");
		headers.add("description");
		headers.add("agent_allowed");

		for (Role current : listRoles())
		{
			if (filterSupplier && current.getName().toLowerCase().equals("supplier"))
			{
				continue;
			}
			else
			{
				if (query != null && query.length() > 0)
				{
					if (roleMatches(current, query)) orderedRoles.put(current.getName(), current);
				}
				else
				{
					orderedRoles.put(current.getName(), current);
				}
			}
		}


		for (Permission perm : permissionService.listPermissions())
		{
			if (filterSupplier && perm.isSupplierOnly()) continue;
			String key = perm.getGroup()+"_"+perm.getName();
			orderedPermissions.put(key, perm);
		}

		// First add headings
		for (String key : orderedRoles.keySet())
		{
			headers.add(key);
		}


		String[] headerArray = new String[headers.size()];
		headerArray = headers.toArray(headerArray);

		StringWriter stringWriter = new StringWriter();
		CSVPrinter printer = new CSVPrinter(stringWriter, CSVFormat.DEFAULT.withHeader(headerArray));
		for (String permKey : orderedPermissions.keySet())
		{
			Permission perm = orderedPermissions.get(permKey);

			List<String> record = new ArrayList<String>();
			record.add(permKey);
			record.add(perm.getDescription());
			record.add(perm.isAgentAllowed()?"Yes":"No");
			for (String key : orderedRoles.keySet())
			{
				boolean isSet = false;
				Role currentRole = orderedRoles.get(key);
				for (Permission rolePermission : currentRole.getPermissions())
				{
					if (rolePermission.getGroup().equals(perm.getGroup()) && rolePermission.getName().equals(perm.getName()))
					{
						isSet = true;
						record.add("Yes");
						break;
					}
				}
				if (!isSet)
				{
					record.add("No");
				}
			}
			printer.printRecord(record.toArray());
		}
		printer.close();

		return stringWriter.toString();
	}

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		return listAsCsv(search);
	}
}
