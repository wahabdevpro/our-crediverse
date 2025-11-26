package cs.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.controller.FeatureBarController;
import cs.dto.GuiAgentUser;
import cs.dto.GuiChangePasswordRequest;
import cs.dto.GuiChangePasswordRequest.EntityType;
import cs.dto.GuiChangePasswordResponse;
//import cs.dto.users.GuiAgentUser2;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.AgentUser;
import hxc.ecds.protocol.rest.ChangePasswordRequest;
import hxc.ecds.protocol.rest.ChangePasswordResponse;
import hxc.ecds.protocol.rest.Role;
import hxc.ecds.protocol.rest.util.ChannelTypes;

@Service
public class AgentUserService
{
	final static Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private FeatureBarController featureBarController;

	@Autowired
	private TypeConvertorService typeConverterservice;

	private boolean configured = false;
	private String restServerAgentUser;
	private String restServerAgent;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerAgentUser = restServerConfig.getRestServer() + restServerConfig.getAgentUserUrl();
			this.restServerAgent = restServerConfig.getRestServer() + restServerConfig.getAgentUrl();
			configured = true;
		}
	}

	///// -----------------------------------------------------

	public GuiAgentUser [] listGuiAgentUsers() throws Exception
	{
		AgentUser[] ausers = restTemplate.execute(restServerAgentUser, HttpMethod.GET, AgentUser[].class);
		return typeConverterservice.convertAgentUserToGuiAgentUser(ausers);
	}

	public GuiAgentUser [] listGuiAgentUsers(String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerAgentUser);
		if (search != null && search.length() > 0)
			RestRequestUtil.standardSearch(uri, search);
		if (filter != null && filter.length() > 0)
			RestRequestUtil.standardFilter(uri, filter);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null)
			RestRequestUtil.standardSorting(uri, sort);
		AgentUser[] ausers = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentUser[].class);
		return typeConverterservice.convertAgentUserToGuiAgentUser(ausers);
	}

	public Boolean isEnabledMobileMoney()
	{
		return featureBarController.featureState("mobileMoneyNotificationFeature");
	}

	public GuiAgentUser [] listGuiAgentUsersAgents(String agentID, String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerAgentUser + "/agentusers/" + agentID);
		if (search != null && search.length() > 0)
			RestRequestUtil.standardSearch(uri, search);
		if (filter != null && filter.length() > 0)
			RestRequestUtil.standardFilter(uri, filter);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null)
			RestRequestUtil.standardSorting(uri, sort);
		AgentUser[] ausers = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentUser[].class);
		return typeConverterservice.convertAgentUserToGuiAgentUser(ausers);
	}

	public GuiAgentUser [] listGuiAgentUsersApi(String agentID, String filter, String search, Integer offset, Integer limit, String sort) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerAgentUser + "/apiusers/" + agentID);
		if (search != null && search.length() > 0)
			RestRequestUtil.standardSearch(uri, search);
		if (filter != null && filter.length() > 0)
			RestRequestUtil.standardFilter(uri, filter);
		if (offset != null && limit != null)
			RestRequestUtil.standardPaging(uri, offset, limit);
		if (sort != null)
			RestRequestUtil.standardSorting(uri, sort);
		AgentUser[] ausers = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentUser[].class);
		return typeConverterservice.convertAgentUserToGuiAgentUser(ausers);
	}

	public String listAgentUsersAsCsv(String filter, String search, long offset, int limit) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerAgentUser+"/csv");

		if (search != null && search.length() > 0)
			RestRequestUtil.standardSearch(uri, search);
		if (filter != null && filter.length() > 0)
			RestRequestUtil.standardFilter(uri, filter);
		RestRequestUtil.standardPaging(uri, offset, limit);

		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
	}

	public HashMap<String,String> getChannelTypes() {
		return ChannelTypes.getChannelTypes();
	}

	public String listAgentUsersAsCsv(String search, long offset, int limit) throws Exception
	{
		return this.listAgentUsersAsCsv(null,  search, offset, limit);
	}

	public Long countAgentUsers(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerAgentUser+"/*");
		if (search != null && search.length() > 0)
			RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
	}

	public AgentUser getAgentUser(String userId) throws Exception
	{
		AgentUser wuser = restTemplate.execute(restServerAgentUser+"/"+userId, HttpMethod.GET, AgentUser.class);
		return wuser;
	}

	public AgentUser getAgentUser(Integer userId) throws Exception
	{
		return getAgentUser( String.valueOf(userId) );
	}

	public GuiAgentUser getGuiAgentUser(String userId) throws Exception
	{
		AgentUser wuser = getAgentUser(userId);
		Map<Integer, Agent> agentMap = new HashMap<>();
		Map<Integer, Role> roleMap = new HashMap<>();
		return typeConverterservice.convertAgentUserToGuiAgentUser(wuser, agentMap, roleMap);
	}

	public GuiAgentUser getGuiAgentUser(Integer userId) throws Exception
	{
		GuiAgentUser webUser = null;
		if (userId != null && userId > 0)
		{
			AgentUser wuser = restTemplate.execute(restServerAgentUser+"/"+userId, HttpMethod.GET, AgentUser.class);
			Map<Integer, Agent> agentMap = new HashMap<>();
			Map<Integer, Role> roleMap = new HashMap<>();
			webUser = typeConverterservice.convertAgentUserToGuiAgentUser(wuser, agentMap, roleMap);
		}
		return webUser;
	}

	public GuiAgentUser getGuiAgentUserByUsername(String username) throws Exception
	{
		GuiAgentUser user = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerAgentUser);
		RestRequestUtil.standardFilter(uri, "domainAccountName='"+username+"'");
		AgentUser[]userList = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, AgentUser[].class);
		if (userList != null && userList.length == 1)
		{
			user = new GuiAgentUser(userList[0]);
		}
		else if (userList != null && userList.length > 1)
		{
			throw new Exception("MULTIPLE_USERS");
		}
		return user;
	}

	public void createAgentUser(AgentUser newUser) throws Exception
	{
		restTemplate.execute(restServerAgentUser, HttpMethod.PUT, newUser, Void.class);
	}

	public AgentUser createAgentUser(GuiAgentUser newGuiUser) throws Exception
	{
		// Get List of Roles
		newGuiUser.setState(AgentUser.STATE_ACTIVE);
		AgentUser wuser = typeConverterservice.guiAgentUserToAgentUser(newGuiUser);

		restTemplate.execute(restServerAgentUser, HttpMethod.PUT, wuser, Void.class);

		return wuser;
	}

	public void deleteAgentUser(String userid) throws Exception
	{
		restTemplate.execute(restServerAgentUser+"/"+userid, HttpMethod.DELETE, Void.class);
	}

	/**
	 * Convert GuiAgentUser to AgentUser and update through REST
	 */
	public AgentUser updateAgentUser(GuiAgentUser updatedGuiUser) throws Exception
	{
		AgentUser wuser = typeConverterservice.guiAgentUserToAgentUser(updatedGuiUser);
		restTemplate.execute(restServerAgentUser, HttpMethod.PUT, wuser, Void.class);
		return wuser;
	}

	public AgentUser updateAgentUser(AgentUser updatedUser) throws Exception
	{
		restTemplate.execute(restServerAgentUser, HttpMethod.PUT, updatedUser, Void.class);
		return updatedUser;
	}

	public GuiChangePasswordResponse changePassword(GuiChangePasswordRequest gui) throws Exception
	{
		try {
			ChangePasswordRequest request = new ChangePasswordRequest();//getChangePasswordRequestFromGuiChangePasswordRequest(gui);
			request.setEntityID(gui.getEntityId());
			request.setNewPassword(gui.getNewPassword());
			request.setCurrentPassword(gui.getCurrentPassword());
			ChangePasswordResponse tsResponse = null;
			GuiChangePasswordResponse response = new GuiChangePasswordResponse();
			if(gui.getEntityType().equals(EntityType.AGENTUSER))
			{
				tsResponse = restTemplate.postForObject(restServerAgentUser + "/change_password", request, ChangePasswordResponse.class);
				response.setReturnCode(tsResponse.getReturnCode());
			} else if(gui.getEntityType().equals(EntityType.AGENT)) {
				tsResponse = restTemplate.postForObject(restServerAgent + "/change_password", request, ChangePasswordResponse.class);
				response.setReturnCode(tsResponse.getReturnCode());
			} else { //Shouldn't ever happen, but possible if WebUser entity data is posted here.
				response.setReturnCode("INVALID_USER_TYPE");
			}
			return response;
		} catch(Exception e) {
			throw e;
		}
	}

	/*
	public AgentUsersConfig getConfiguration() throws Exception
	{
		return restTemplate.execute(restServerAgentUser + "/config", HttpMethod.GET, AgentUsersConfig.class);
	}

	public void updateConfiguration(AgentUsersConfig updatedAgentUsersConfig) throws Exception
	{
		try {
			restTemplate.execute(restServerAgentUser+ "/config", HttpMethod.PUT, updatedAgentUsersConfig, Void.class);
		} catch(Exception e) {
			throw e;
		}
	}

	public Map<String, Phrase[]> getConfigurationVariables()
	{
		Map<String, Phrase[]> result = new HashMap<>();

		AgentUsersConfig ac = new AgentUsersConfig();
		result.put("newPinNotification", ac.listNotificationFields());

		return result;
	}
	*/
}

