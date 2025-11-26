package cs.controller;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import cs.dto.GuiDataTable.TableColumn;
import cs.dto.GuiRole;
import cs.dto.error.GuiValidationException;
import cs.dto.security.LoginSessionData;
import cs.service.CorrelationIdService;
import cs.service.RoleService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.Role;
import hxc.ecds.protocol.rest.Violation;

@RestController
@RequestMapping("/api/roles")
public class RoleController
{
	@Autowired
	private RoleService rolesService;

	@Autowired
	private TypeConvertorService typeConvertorService;

	@Autowired
	protected CorrelationIdService correlationIdService;

	@Autowired
	private LoginSessionData sessionData;

	private List<TableColumn> columns = new ArrayList<TableColumn>();

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list() throws Exception
	{
		Role[] roles = rolesService.listRoles();
		ArrayList<Role> froles = new ArrayList<Role>();
		Arrays.asList(roles).forEach((role)->{
			if (!(role.isPermanent() && role.getName().equals("Supplier")))
			{
				role.setPermissions(null);
				froles.add(role);
			}
		});
		return new GuiDataTable(froles.toArray());
	}

	@RequestMapping(value="agent", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Role[] listAgentRoles() throws Exception
	{
		Role[] roles = rolesService.listRoles();
		ArrayList<Role> froles = new ArrayList<Role>();
		Arrays.asList(roles).forEach((role)->{
			if (role.getType().equals("A"))
			{
				role.setPermissions(null);
				froles.add(role);
			}
		});
		return froles.toArray(new Role[0]);
	}

	@RequestMapping(value="agent/data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listAgentRolesData() throws Exception
	{
		Role[] roles = rolesService.listRoles();
		ArrayList<Role> froles = new ArrayList<Role>();
		Arrays.asList(roles).forEach((role)->{
			if (role.getType().equals("A"))
			{
				role.setPermissions(null);
				froles.add(role);
			}
		});
		return new GuiDataTable(froles.toArray());
	}

	@RequestMapping(value="webuser/data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable listWebUserRolesData() throws Exception
	{
		Role[] roles = rolesService.listRoles();
		ArrayList<Role> froles = new ArrayList<Role>();
		Arrays.asList(roles).forEach((role)->{
			if (!(role.isPermanent() && role.getName().equals("Supplier")) && role.getType().equals("W"))
			{
				role.setPermissions(null);
				froles.add(role);
			}
		});
		return new GuiDataTable(froles.toArray());
	}

	@RequestMapping(value="{roleId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Role getRole(@PathVariable("roleId") String roleId) throws Exception
	{
		return rolesService.getRole(roleId);
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Role[] serverList() throws Exception
	{
		Role[] roles = rolesService.listRoles();
		return roles;
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Role create(@RequestBody(required = true) Role newRole, Locale locale) throws Exception
	{
		rolesService.create(newRole);
		return newRole;
	}

	@RequestMapping(value="{role}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("role") String roleid) throws Exception
	{
		rolesService.delete(roleid);
		return "{}";
	}

	@RequestMapping(value="{role}", method = RequestMethod.PUT)
	public String update(@PathVariable("role") String roleid, @RequestBody(required = true) GuiRole newRole, Locale locale) throws Exception
	{
		rolesService.update(typeConvertorService.getRoleFromGuiRole(newRole));
		return "{}";
	}

	@RequestMapping(value="details/{role}", method = RequestMethod.PUT)
	public String updateDetails(@PathVariable("role") String roleid, @RequestBody(required = true) GuiRole newRole, Locale locale) throws Exception
	{
		rolesService.updateDetails(typeConvertorService.getRoleFromGuiRole(newRole));
		return "{}";
	}

	@RequestMapping(value="columns", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<TableColumn> listColumns() throws Exception
	{
		columns.clear();
		//columns.add(new TableColumn("id", "ID"));
		//columns.add(new TableColumn("companyID", "Company ID"));
		columns.add(new TableColumn("name", "Role Name"));
		//columns.add(new TableColumn("description", "Description"));
		columns.add(new TableColumn("permissionGroup", "Permission Group"));
		columns.add(new TableColumn("permList", "Permission Detail"));
		//columns.add(new TableColumn("permissionDescription", "Group Description"));
		//columns.add(new TableColumn("permissionSupplierOnly", "Supplier Only"));

		return columns;
	}

	@RequestMapping(value="data", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable serverListAsTable(@RequestParam Map<String, String> params) throws Exception
	{
/*		params.forEach((key, value) -> {
			logger.error(key+" => "+value);
		});*/
		GuiDataTable roleData = null;
		if (params.containsKey("start") && params.containsKey("length"))
		{
			int start = Integer.parseInt(params.get("start"));
			int length = Integer.parseInt(params.get("length"));
			GuiRole[] roleList = typeConvertorService.getGuiRoleFromRole(rolesService.listRoles(start, length));
			roleData = new GuiDataTable(Arrays.copyOfRange(roleList, start, ((start + length) >= roleList.length)?roleList.length:start + length));
			roleData.setRecordsFiltered(roleList.length);
			roleData.setRecordsTotal(roleList.length);
			roleData.setDraw(params.get("draw"));
		}
		else
		{
			GuiRole[] roleList = typeConvertorService.getGuiRoleFromRole(rolesService.listRoles());
			roleData = new GuiDataTable(roleList);
		}

		//GuiRole[] roles = typeConvertorService.getGuiRoleFromRole(rolesService.listRoles());
		return roleData;
	}

	@RequestMapping(value="search/csv", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public void searchResultsExport(@RequestParam(required = false, defaultValue = "", value="q") String query, @RequestParam(required = false) String uniqid, HttpServletResponse response) throws Exception
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.ROLE, ".csv"));

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");

		OutputStream outputStream = response.getOutputStream();
		rolesService.csvExport(uniqid, outputStream, query, 0, 0, true, null);
	}

	@RequestMapping(value="search/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		return rolesService.track(0L);
	}
	
	@RequestMapping(value="agent/dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(
			@RequestParam(value = "_type") Optional<String> type, 
			@RequestParam(value = "term") Optional<String> query,
			@RequestParam(value = "roleID") Optional<Integer> roleID) throws Exception
	{
		Role[] roles = rolesService.listRoles();
		Map<Integer, String> roleMap = new TreeMap<Integer, String>();
		if (roles != null)
		{
			Arrays.asList(roles).forEach(role ->{
				if ( (!roleID.isPresent() || ( role.getId() == roleID.get().intValue() )) && (role.getType().equals(Role.TYPE_AGENT) ) )
					roleMap.put(role.getId(), role.getName());
			});
		}
		return roleMap;
	}

}
