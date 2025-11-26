package cs.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiChangePasswordRequest;
import cs.dto.GuiChangePasswordResponse;
import cs.dto.GuiDataTable;
import cs.dto.GuiWebUser;
import cs.dto.portal.GuiPinRules;
import cs.dto.security.LoginSessionData;
import cs.service.ConfigurationService;
import cs.service.WebUserService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.WebUser;

@RestController
@RequestMapping("/api/wusers")
public class WebUserController
{
	@Autowired
	private ConfigurationService configService;

	@Autowired
	private WebUserService webUserService;

	@Autowired
	private LoginSessionData sessionData;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiWebUser[] listWebUsers() throws Exception
	{
		GuiWebUser[] users = webUserService.listGuiWebUsers();
		return users;
	}

	/**
	 * Request for User data (url: http://localhost:8084/ecds-gui/api/wusers/data)
	 */
	@RequestMapping(value="data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable userListAsTable() throws Exception
	{
		GuiWebUser[] users = webUserService.listGuiWebUsers();
		ArrayList<GuiWebUser> fusers = new ArrayList<GuiWebUser>();
		Arrays.asList(users).forEach((user)->{
			if (!user.isSupplier())
				fusers.add(user);
		});
		return new GuiDataTable(fusers.toArray());
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) count = webUserService.countWebUsers(search);

		return webUserService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.USER, ".csv"));

		long userCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		userCount = webUserService.countWebUsers(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(userCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		webUserService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, userCount, true, null);
	}

	/**
	 *	Delete Web User call
	 */
	@RequestMapping(method = RequestMethod.DELETE, value="{wuser}")
	public String deleteWebUser(@PathVariable("wuser") String userid) throws Exception
	{
		webUserService.deleteWebUser(userid);
		return "{}";
	}

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(@RequestParam(value = "_type") Optional<String> type, @RequestParam(value = "term") Optional<String> query) throws Exception
	{
		WebUser[] users = null;
		Map<Integer, String>userMap = new TreeMap<Integer,String>();
		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			users = webUserService.listGuiWebUsers(query.get());
		}
		else
		{
			users = webUserService.listGuiWebUsers();
		}

		if (users != null)
		{
			Arrays.asList(users).forEach(user ->{
				userMap.put(user.getId(), user.getFirstName() + " " + user.getSurname());
			});
		}
		return userMap;
	}

	@RequestMapping(value="profile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiWebUser getProfile() throws Exception
	{
		return webUserService.getLoggedinUser();
	}

	/**
	 * Update Web User
	 */
	@RequestMapping(value="profile", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiWebUser persisteProfile(@RequestBody(required = true) GuiWebUser updatedUser, Locale locale) throws Exception
	{
		webUserService.updateLoggedinUser(updatedUser);
		return updatedUser;
	}

	/**
	 *	Add new User
	 */
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiWebUser createWebUser(@RequestBody(required = true) GuiWebUser newUser, Locale locale) throws Exception
	{
		webUserService.createWebUser(newUser);
		return newUser;
	}

	/**
	 * Update Web User
	 */
	@RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiWebUser persisteUserDetails(@RequestBody(required = true) GuiWebUser updatedUser, Locale locale) throws Exception
	{
		webUserService.updateWebUser(updatedUser);
		return updatedUser;
	}

	@RequestMapping(value="{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiWebUser getWebUser(@PathVariable("userId") String userId) throws Exception
	{
		GuiWebUser user =  webUserService.getGuiWebUser(userId);
		return user;
	}

	@RequestMapping(value="/change_password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiChangePasswordResponse changePassword(@RequestBody(required = true) GuiChangePasswordRequest changePasswordRequest, Locale locale) throws Exception
	{
		return webUserService.changePassword(changePasswordRequest);
	}

	@RequestMapping(value="/reset_password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiChangePasswordResponse resetPassword(@RequestBody(required = true) GuiChangePasswordRequest changePasswordRequest, Locale locale) throws Exception
	{
		return webUserService.resetPassword(changePasswordRequest);
	}

	/**
	 * Retrieve Configuration Settings reflecting the PIN rules
	 */
	@RequestMapping(value = "passwordrules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiPinRules getPinRules() throws Exception
	{
		return webUserService.extractPinChangeRules();
	}
}
