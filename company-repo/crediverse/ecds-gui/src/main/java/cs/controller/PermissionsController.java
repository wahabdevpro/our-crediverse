package cs.controller;

import java.util.Arrays;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import cs.dto.GuiDataTable;
import cs.service.PermissionService;
import hxc.ecds.protocol.rest.Permission;

@RestController
@RequestMapping("/api/permissions")
public class PermissionsController
{

	@Autowired
	private PermissionService permissionService;

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Permission[] serverList() throws Exception
	{
		Permission[] permissions = permissionService.listNonSupplierPermissions();
		return permissions;
	}

	// TODO @JsonView(PermissionViews.Ajax.class)
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list() throws Exception
	{
		Permission[] permissionList = permissionService.listNonSupplierPermissions();

		Arrays.sort(permissionList, new Comparator<Permission>(){

			@Override
			public int compare(Permission o1, Permission o2)
			{
				int status = o1.getGroup().compareTo(o2.getGroup());
				if(status == 0)
				{
					status = o1.getName().compareTo(o2.getName());
				}
				return status;
			}

		});
		return new GuiDataTable(permissionList);
	}

	@RequestMapping("{perm}")
	public String index(@PathVariable("perm") String perm) throws Exception
	{
		return "{}";
	}
}
