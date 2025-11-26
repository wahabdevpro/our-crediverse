package cs.controller;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiAgentUser;
import cs.dto.GuiChangePasswordRequest;
import cs.dto.GuiChangePasswordResponse;
import cs.dto.GuiDataTable;
import cs.dto.portal.GuiPinRules;
import cs.dto.security.LoginSessionData;
import cs.service.AgentService;
import cs.service.AgentUserService;
import cs.service.ConfigurationService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.AgentUser;

@RestController
@RequestMapping("/api/ausers")
public class AgentUserController
{
	private static final Logger logger = LoggerFactory.getLogger(AgentUserController.class);

	@Autowired
	private ConfigurationService configService;

	@Autowired
	private AgentUserService agentUserService;

	@Autowired
	private AgentService agentService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private ObjectMapper mapper;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentUser[] listAgentUsers() throws Exception
	{
		GuiAgentUser[] users = agentUserService.listGuiAgentUsers();
		return users;
	}

	/**
	 * Request for User data (url: http://localhost:8084/ecds-gui/api/ausers/data)
	 */
	@RequestMapping(value="data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable userListAsTable() throws Exception
	{
		GuiAgentUser[] users = agentUserService.listGuiAgentUsers();
		return new GuiDataTable(users);
	}

	@RequestMapping(value="data/{agentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable agentUserListAsTable(@PathVariable("agentId") String agentId) throws Exception
	{
		GuiAgentUser[] users = agentUserService.listGuiAgentUsers( "agentID='"+agentId+"'", null, null, null, null );
		return new GuiDataTable(users);
	}
	
	@RequestMapping(value="agents/data/{agentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable agentUserOnlyListAsTable(@PathVariable("agentId") String agentId) throws Exception
	{
		GuiAgentUser[] users = agentUserService.listGuiAgentUsersAgents(agentId, null, null, null, null, null);
		return new GuiDataTable(users);
	}
	
	@RequestMapping(value="api/data/{agentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable apiUserOnlyListAsTable(@PathVariable("agentId") String agentId) throws Exception
	{
		GuiAgentUser[] users = agentUserService.listGuiAgentUsersApi(agentId, null, null, null, null, null);
		return new GuiDataTable(users);
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		count = agentUserService.countAgentUsers(search);

		ObjectNode response = mapper.createObjectNode();
		response.put("count", count);
		return response;
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.USER, ".csv"));

		long userCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		userCount = agentUserService.countAgentUsers(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(userCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		long runningTotal = 0;
		try
		{
			OutputStream outputStream = response.getOutputStream();
			while (runningTotal < userCount)
			{
				String outputResult = agentUserService.listAgentUsersAsCsv(search, runningTotal, recordsPerChunk);
				outputStream.write(outputResult.getBytes());
				runningTotal+=recordsPerChunk;
			}
			outputStream.flush();
			outputStream.close();
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}

	/**
	 *	Delete Agent User call
	 */
	@RequestMapping(method = RequestMethod.DELETE, value="{auser}")
	public String deleteAgentUser(@PathVariable("auser") String userid) throws Exception
	{
		agentUserService.deleteAgentUser(userid);
		return "{}";
	}

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(@RequestParam(value = "_type") Optional<String> type, @RequestParam(value = "term") Optional<String> query) throws Exception
	{
		AgentUser[] users = null;
		Map<Integer, String>userMap = new TreeMap<Integer,String>();
		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			users = agentUserService.listGuiAgentUsers(null, query.get(), null, null, null);
		}
		else
		{
			users = agentUserService.listGuiAgentUsers();
		}

		if (users != null)
		{
			Arrays.asList(users).forEach(user ->{
				userMap.put(user.getId(), user.getFirstName() + " " + user.getSurname());
			});
		}
		return userMap;
	}

	/**
	 *	Add new User
	 */
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentUser createAgentUser(@RequestBody(required = true) GuiAgentUser newUser, Locale locale) throws Exception
	{
		if(newUser.getDomainAccountName().isEmpty())
			newUser.setDomainAccountName(null);
		agentUserService.createAgentUser(newUser);
		return newUser;
	}

	/**
	 * Update Agent User
	 */
	@RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentUser persisteUserDetails(@RequestBody(required = true) GuiAgentUser updatedUser, Locale locale) throws Exception
	{
		if(updatedUser.getDomainAccountName().isEmpty())
			updatedUser.setDomainAccountName(null);
		agentUserService.updateAgentUser(updatedUser);
		return updatedUser;
	}

	@RequestMapping(value="{userId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentUser persisteUserDetails(@PathVariable("userId") String userId, @RequestBody(required = true) GuiAgentUser updatedUser, Locale locale) throws Exception
	{
		if(updatedUser.getDomainAccountName().isEmpty())
			updatedUser.setDomainAccountName(null);
		agentUserService.updateAgentUser(updatedUser);
		return updatedUser;
	}

	@RequestMapping(value="{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentUser getAgentUser(@PathVariable("userId") String userId) throws Exception
	{
		GuiAgentUser user =  agentUserService.getGuiAgentUser(userId);
		return user;
	}

	@RequestMapping(value="pinreset/{userId}", method = RequestMethod.PUT)
	public String pinReset(@PathVariable("userId") String userId, Locale locale) throws Exception
	{
		AgentUser user = agentUserService.getAgentUser(userId);
		if (user.isTemporaryPin()) {
			user.setTemporaryPin(false);
			agentUserService.updateAgentUser(user);
		}
		user.setTemporaryPin(true);
		agentUserService.updateAgentUser(user);
		return "{}";
	}

	@RequestMapping(value="/change_password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiChangePasswordResponse changePassword(@RequestBody(required = true) GuiChangePasswordRequest changePasswordRequest, Locale locale) throws Exception
	{
		return agentUserService.changePassword(changePasswordRequest);
	}

	@RequestMapping(value = "channel_types", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public HashMap<String, String> getChannelTypes() {
		return agentUserService.getChannelTypes();
	}

	/**
	 * Retrieve Configuration Settings reflecting the PIN rules
	 */
	@RequestMapping(value = "passwordrules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiPinRules getPinRules() throws Exception
	{
		return agentService.extractPinChangeRules();
	}
}
