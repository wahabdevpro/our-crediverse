package cs.controller;

import java.io.OutputStream;
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

import cs.dto.GuiDataTable;
import cs.dto.GuiGroup;
import cs.dto.security.LoginSessionData;
import cs.service.ConfigurationService;
import cs.service.GroupService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.Group;

@RestController
@RequestMapping("/api/groups")
public class GroupController
{
	@Autowired
	private ConfigurationService configService;

	@Autowired
	private GroupService groupService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list(@RequestParam Map<String, String> params) throws Exception
	{
		Group[] groupList = null;
		if (params.containsKey("start") && params.containsKey("length"))
		{
			int start = Integer.parseInt(params.get("start"));
			int length = Integer.parseInt(params.get("length"));
			groupList = groupService.listGroups(start, length);
		}
		else
		{
			groupList = groupService.listGroups();
		}

		return new GuiDataTable(typeConvertorService.getGuiGroupFromGroup(groupList));
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		long count = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		if (docount) count = groupService.countGroups(search);

		return groupService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listAsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.GROUP, ".csv"));

		long groupCount = 0L;
		String search = null;
		if (params.containsKey("q")) search =  params.get("q");
		groupCount = groupService.countGroups(search);

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(groupCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		groupService.csvExport(params.get("uniqid"), outputStream, search, recordsPerChunk, groupCount, true, null);
	}

	@RequestMapping(value="{groupId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiGroup getGroup(@PathVariable("groupId") String groupId) throws Exception
	{
		return typeConvertorService.getGuiGroupFromGroup(groupService.getGroup(groupId), null);
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Group[] serverList() throws Exception
	{
		Group[] groups = groupService.listGroups();
		return groups;
	}

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(
		@RequestParam(value = "_type") Optional<String> type,
		@RequestParam(value = "term") Optional<String> query,
		@RequestParam(value = "tierID") Optional<Integer> tierID) throws Exception
	{
		Group[] groups = null;
		Map<Integer, String>groupMap = new TreeMap<Integer,String>();
		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			groups = groupService.listGroups(query.get());
		}
		else
		{
			groups = groupService.listGroups();
		}

		if (groups != null)
		{
			Arrays.asList(groups).forEach(group ->{
				if ( !tierID.isPresent() || ( group.getTierID() == tierID.get().intValue() ) )
					groupMap.put(group.getId(), group.getName());
			});
		}
		return groupMap;
	}
	
	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Group create(@RequestBody(required = true) Group newGroup, Locale locale) throws Exception
	{
		groupService.create(newGroup);
		return newGroup;
	}

	@RequestMapping(value="{group}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("group") String groupid) throws Exception
	{
		groupService.delete(groupid);
		return "{}";
	}

	@RequestMapping(value="{groupId}", method = RequestMethod.PUT)
	public String update(@PathVariable("groupId") String groupId, @RequestBody(required = true) Group newGroup, Locale locale) throws Exception
	{
		//groupService.update(typeConvertorService.getRoleFromGuiRole(newGroup));
		groupService.update(newGroup);
		return "{}";
	}

	@RequestMapping(method = RequestMethod.PUT)
	public String update(@RequestBody(required = true) Group newGroup, Locale locale) throws Exception
	{
		//groupService.update(typeConvertorService.getRoleFromGuiRole(newGroup));
		groupService.update(newGroup);
		return "{}";
	}

	/*
	 * http://localhost:8084/ecds-gui/api/groups?
	 * draw=1&
	 * columns[0][data]=name&
	 * columns[0][name]=&
	 * columns[0][searchable]=true&
	 * columns[0][orderable]=true&
	 * columns[0][search][value]=&
	 * columns[0][search][regex]=false&
	 * columns[1][data]=description&
	 * columns[1][name]=&
	 * columns[1][searchable]=true&
	 * columns[1][orderable]=true&
	 * columns[1][search][value]=&
	 * columns[1][search][regex]=false&
	 * columns[2][data]=tierName&
	 * columns[2][name]=&
	 * columns[2][searchable]=true&
	 * columns[2][orderable]=true&
	 * columns[2][search][value]=&
	 * columns[2][search][regex]=false&
	 * columns[3][data]=maxTransactionAmount&
	 * columns[3][name]=&
	 * columns[3][searchable]=true&
	 * columns[3][orderable]=true&
	 * columns[3][search][value]=&
	 * columns[3][search][regex]=false&
	 * columns[4][data]=maxDailyCount&
	 * columns[4][name]=&
	 * columns[4][searchable]=true&
	 * columns[4][orderable]=true&
	 * columns[4][search][value]=&
	 * columns[4][search][regex]=false&
	 * columns[5][data]=maxDailyAmount&
	 * columns[5][name]=&
	 * columns[5][searchable]=true&
	 * columns[5][orderable]=true&
	 * columns[5][search][value]=&
	 * columns[5][search][regex]=false&
	 * columns[6][data]=maxMonthlyCount&
	 * columns[6][name]=&
	 * columns[6][searchable]=true&
	 * columns[6][orderable]=true&
	 * columns[6][search][value]=&
	 * columns[6][search][regex]=false&
	 * columns[7][data]=maxMonthlyAmount&
	 * columns[7][name]=&
	 * columns[7][searchable]=true&
	 * columns[7][orderable]=true&
	 * columns[7][search][value]=&
	 * columns[7][search][regex]=false&
	 * columns[8][data]=&
	 * columns[8][name]=&
	 * columns[8][searchable]=true&
	 * columns[8][orderable]=false&
	 * columns[8][search][value]=&
	 * columns[8][search][regex]=false&
	 * order[0][column]=0&
	 * order[0][dir]=asc&
	 * start=0&
	 * length=10&
	 * search[value]=&
	 * search[regex]=false
	 */
	@RequestMapping(value="data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable serverListAsTable(@RequestParam Map<String, String> params) throws Exception
	{
/*		params.forEach((key, value) -> {
			logger.error(key+" => "+value);
		});*/
		GuiDataTable groupData = null;
		if (params.containsKey("start") && params.containsKey("length"))
		{
			/*
			int start = Integer.parseInt(params.get("start"));
			int length = Integer.parseInt(params.get("length"));
			GuiRole[] roleList = typeConvertorService.getGuiRoleFromRole(groupService.listRoles(start, length));
			roleData = new GuiDataTable(Arrays.copyOfRange(roleList, start, ((start + length) >= roleList.length)?roleList.length:start + length));
			roleData.setRecordsFiltered(roleList.length);
			roleData.setRecordsTotal(roleList.length);
			roleData.setDraw(params.get("draw"));*/
		}
		else
		{
			//GuiRole[] roleList = typeConvertorService.getGuiRoleFromRole(groupService.listGroups());
			Group[] groupList = groupService.listGroups();
			groupData = new GuiDataTable(typeConvertorService.getGuiGroupFromGroup(groupList));
		}

		//GuiRole[] roles = typeConvertorService.getGuiRoleFromRole(rolesService.listRoles());
		return groupData;
	}
}
