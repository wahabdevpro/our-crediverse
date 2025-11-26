package cs.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.ApplicationDetailsConfiguration;
import cs.config.RestServerConfiguration;
import cs.dto.users.GuiUserRole;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Role;

@Service
public class RoleService
{
	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;
	
	@Autowired
	private ApplicationDetailsConfiguration appConfig;
	
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
	
}
