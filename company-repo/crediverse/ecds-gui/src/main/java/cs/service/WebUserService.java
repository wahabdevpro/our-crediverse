package cs.service;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import cs.config.RestServerConfiguration;
import cs.dto.GuiChangePasswordRequest;
import cs.dto.GuiChangePasswordResponse;
import cs.dto.GuiWebUser;
import cs.dto.portal.GuiPinRules;
import cs.service.interfaces.IQueryStringParameters;
import cs.template.CsRestTemplate;
import cs.utility.RestRequestUtil;
import hxc.ecds.protocol.rest.ChangePasswordRequest;
import hxc.ecds.protocol.rest.ChangePasswordResponse;
import hxc.ecds.protocol.rest.Department;
import hxc.ecds.protocol.rest.WebUser;
import hxc.ecds.protocol.rest.config.Phrase;
import hxc.ecds.protocol.rest.config.WebUsersConfig;

@Service
public class WebUserService extends Exportable
{
	@Autowired
	private CsRestTemplate restTemplate;

	@Autowired
	private DepartmentService departmentService;

	@Autowired
	private RestServerConfiguration restServerConfig;

	@Autowired
	private TypeConvertorService typeConverterservice;

	@Autowired
	private ConfigurationService configService;

	private boolean configured = false;
	private String restServerWebUser;

	@PostConstruct
	public void configure()
	{
		if (!configured)
		{
			this.restServerWebUser = restServerConfig.getRestServer() + restServerConfig.getWebUserurl();
			configured = true;
		}
	}

	///// -----------------------------------------------------

	public GuiWebUser [] listGuiWebUsers() throws Exception
	{
		WebUser[] wusers = restTemplate.execute(restServerWebUser, HttpMethod.GET, WebUser[].class);
		Map<Integer, Department> departmentMap = departmentService.getDepartmentMap();

		return typeConverterservice.convertWebUsersArrayoGuiWebUserArray(wusers, departmentMap);
	}

	public GuiWebUser [] listGuiWebUsers(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerWebUser);
		RestRequestUtil.standardSearch(uri, search);
		WebUser[] wusers = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, WebUser[].class);

		Map<Integer, Department> departmentMap = departmentService.getDepartmentMap();
		return typeConverterservice.convertWebUsersArrayoGuiWebUserArray(wusers, departmentMap);
	}

	@Override
	public String listAsCsv(String filter, String search, long offset, int limit, IQueryStringParameters queryStringParams) throws Exception {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerWebUser+"/csv");

		RestRequestUtil.standardSearch(uri, search);
		RestRequestUtil.standardPaging(uri, offset, limit);

		RestRequestUtil.standardFilter(uri, filter);

		String response = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, String.class);
		return response;
	}

	public Long countWebUsers(String search) throws Exception
	{
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerWebUser+"/*");
		if (search != null && search.length() > 0) RestRequestUtil.standardSearch(uri, search);
		return restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, Long.class);
	}

	public WebUser getWebUser(String userId) throws Exception
	{
		WebUser wuser = restTemplate.execute(restServerWebUser+"/"+userId, HttpMethod.GET, WebUser.class);
		return wuser;
	}

	public WebUser getWebUser(Integer userId) throws Exception
	{
		return getWebUser( String.valueOf(userId) );
	}

	public GuiWebUser getLoggedinUser() throws Exception
	{
		WebUser wuser = restTemplate.execute(restServerWebUser+"/profile", HttpMethod.GET, WebUser.class);
		return new GuiWebUser(wuser);
	}

	/**
	 * Convert GuiWebUser to WebUser and update through REST
	 */
	public WebUser updateLoggedinUser(GuiWebUser updatedGuiUser) throws Exception
	{
		// First Check if the User being updated is not permanent
		WebUser wuser = getWebUser(updatedGuiUser.getId());
		if (wuser.getState().equals(WebUser.STATE_PERMANENT)) {
			// Permanent User
			wuser.setDomainAccountName( updatedGuiUser.getDomainAccountName() );
			wuser.setMobileNumber( updatedGuiUser.getMobileNumber() );
		} else {
			// Non-permanent user
			wuser = typeConverterservice.guiWebUserToWebUser(updatedGuiUser);
		}

		restTemplate.execute(restServerWebUser+"/profile", HttpMethod.PUT, wuser, Void.class);

		return wuser;
	}

	public GuiWebUser getGuiWebUser(String userId) throws Exception
	{
		WebUser wuser = getWebUser(userId);
		Department department = departmentService.getDepartment(wuser.getDepartmentID());
		return new GuiWebUser(wuser, department.getName());
	}

	public GuiWebUser getGuiWebUser(Integer userId) throws Exception
	{
		GuiWebUser webUser = null;
		if (userId != null && userId > 0)
		{
			WebUser wuser = getWebUser(userId);
			webUser = new GuiWebUser(wuser);
		}
		return webUser;
	}

	public GuiWebUser getGuiWebUserByUsername(String username) throws Exception
	{
		GuiWebUser user = null;
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(restServerWebUser);
		RestRequestUtil.standardFilter(uri, "domainAccountName='"+username+"'");
		WebUser[]userList = restTemplate.execute(uri.build(true).toUri(), HttpMethod.GET, WebUser[].class);
		if (userList != null && userList.length == 1)
		{
			user = new GuiWebUser(userList[0]);
		}
		else if (userList != null && userList.length > 1)
		{
			throw new Exception("MULTIPLE_USERS");
		}
		return user;
	}

	public void createWebUser(WebUser newUser) throws Exception
	{
		restTemplate.execute(restServerWebUser, HttpMethod.PUT, newUser, Void.class);
	}

	public WebUser createWebUser(GuiWebUser newGuiUser) throws Exception
	{
		// Get List of Roles
		newGuiUser.setState(WebUser.STATE_ACTIVE);
		WebUser wuser = typeConverterservice.guiWebUserToWebUser(newGuiUser);

		restTemplate.execute(restServerWebUser, HttpMethod.PUT, wuser, Void.class);

		return wuser;
	}

	public void deleteWebUser(String userid) throws Exception
	{
		restTemplate.execute(restServerWebUser+"/"+userid, HttpMethod.DELETE, Void.class);
	}

	/**
	 * Convert GuiWebUser to WebUser and update through REST
	 */
	public WebUser updateWebUser(GuiWebUser updatedGuiUser) throws Exception
	{
		// First Check if the User being updated is not permanent
		WebUser wuser = getWebUser(updatedGuiUser.getId());
		if (wuser.getState().equals(WebUser.STATE_PERMANENT)) {
			// Permanent User
			wuser.setDomainAccountName( updatedGuiUser.getDomainAccountName() );
			wuser.setMobileNumber( updatedGuiUser.getMobileNumber() );
		} else {
			// Non-permanent user
			wuser = typeConverterservice.guiWebUserToWebUser(updatedGuiUser);
		}

		restTemplate.execute(restServerWebUser, HttpMethod.PUT, wuser, Void.class);

		return wuser;
	}

	public WebUsersConfig getConfiguration() throws Exception
	{
		return restTemplate.execute(restServerWebUser + "/config", HttpMethod.GET, WebUsersConfig.class);
	}

	public void updateConfiguration(WebUsersConfig updatedWebUsersConfig) throws Exception
	{
		try {
			restTemplate.execute(restServerWebUser+ "/config", HttpMethod.PUT, updatedWebUsersConfig, Void.class);
		} catch(Exception e) {
			throw e;
		}
	}

	public Map<String, Phrase[]> getConfigurationVariables()
	{
		Map<String, Phrase[]> result = new HashMap<>();
		WebUsersConfig ac = new WebUsersConfig();
		result.put("newPinNotification", ac.listNotificationFields());
		result.put("passwordChangeEmailSubject", ac.listPasswordEmailFields());
		result.put("passwordChangeEmailBody", ac.listPasswordEmailFields());
		result.put("passwordResetEmailSubject", ac.listPasswordEmailFields());
		result.put("passwordResetEmailBody", ac.listPasswordEmailFields());
		result.put("welcomePasswordUserEmailSubject", ac.listPasswordEmailFields());
		result.put("welcomePasswordUserEmailBody", ac.listPasswordEmailFields());
		result.put("welcomeDomainAccUserEmailSubject", ac.listPasswordEmailFields());
		result.put("welcomeDomainAccUserEmailBody", ac.listPasswordEmailFields());
		return result;
	}

	public GuiPinRules extractPinChangeRules() throws Exception
	{
		WebUsersConfig webUserConfig = configService.getWebUsersConfig();
		GuiPinRules result = GuiPinRules.extractPinRuleSet(webUserConfig);
		return result;
	}

	public GuiChangePasswordResponse changePassword(GuiChangePasswordRequest gui) throws Exception
	{
		try {
			ChangePasswordRequest request = new ChangePasswordRequest();//getChangePasswordRequestFromGuiChangePasswordRequest(gui);
			request.setEntityID(gui.getEntityId());
			request.setNewPassword(gui.getNewPassword());
			request.setCurrentPassword(gui.getCurrentPassword());
			ChangePasswordResponse tsResponse = restTemplate.postForObject(restServerWebUser + "/change_password", request, ChangePasswordResponse.class);
			GuiChangePasswordResponse response = new GuiChangePasswordResponse();
			response.setReturnCode(tsResponse.getReturnCode());
			return response;
		} catch(Exception e) {
			throw e;
		}
	}

	public ChangePasswordRequest getChangePasswordRequestFromGuiChangePasswordRequest(GuiChangePasswordRequest gui)
	{
		ChangePasswordRequest result = new ChangePasswordRequest();
		result.setEntityID(gui.getEntityId());
		result.setNewPassword(gui.getNewPassword());
		result.setCurrentPassword(gui.getCurrentPassword());
		return result;
	}

	public GuiChangePasswordRequest getGuiChangePasswordRequestFromChangePasswordRequest(ChangePasswordRequest protocol)
	{
		GuiChangePasswordRequest result = new GuiChangePasswordRequest();
		result.setEntityId(protocol.getEntityID());
		result.setNewPassword(protocol.getNewPassword());
		result.setCurrentPassword(protocol.getCurrentPassword());
		return result;
	}

	public ChangePasswordResponse getChangePasswordResponseFromGuiChangePasswordRequest(GuiChangePasswordResponse gui)
	{
		ChangePasswordResponse result = new ChangePasswordResponse();
		return result;
	}

	public GuiChangePasswordResponse resetPassword(GuiChangePasswordRequest gui) throws Exception
	{
		GuiChangePasswordResponse response = new GuiChangePasswordResponse();
		ChangePasswordRequest request = new ChangePasswordRequest();
		request.setEntityID(gui.getEntityId());
		ChangePasswordResponse tsResponse = restTemplate.postForObject(restServerWebUser+ "/reset_password", request, ChangePasswordResponse.class);
		response.setReturnCode(tsResponse.getReturnCode());
		return response;
	}
}

