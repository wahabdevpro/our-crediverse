package cs.controller.portal;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cs.service.GroupService;
import cs.service.ServiceClassService;
import cs.utility.Common;
import hxc.ecds.protocol.rest.Group;
import hxc.ecds.protocol.rest.ServiceClass;

@RestController
@Profile(Common.CONST_PORTAL_PROFILE)
@RequestMapping("/papi/search")
public class PortalAgentSearchController
{

	@Autowired
	private GroupService groupService;

	@Autowired
	private ServiceClassService serviceClassService;

	@RequestMapping(value = "groupslist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(@RequestParam(value = "_type") Optional<String> type,
			@RequestParam(value = "term") Optional<String> query,
			@RequestParam(value = "tierID") Optional<Integer> tierID) throws Exception
	{
		Group[] groups = null;
		Map<Integer, String> groupMap = new TreeMap<Integer, String>();
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
			Arrays.asList(groups).forEach(group -> {
				if (!tierID.isPresent() || (group.getTierID() == tierID.get().intValue()))
					groupMap.put(group.getId(), group.getName());
			});
		}
		return groupMap;
	}

	@RequestMapping(value="serviceclasslist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> serverListDropdown(@RequestParam(value = "_type") Optional<String> type, @RequestParam(value = "term") Optional<String> query) throws Exception
	{
		Map<Integer, String>classMap = new TreeMap<Integer,String>();
		ServiceClass[] serviceClasses = null;

		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
		{
			serviceClasses = serviceClassService.listServiceClasses(query.get());
		}
		else
		{
			serviceClasses = serviceClassService.listServiceClasses();
		}
		if (serviceClasses != null)
		{
			Arrays.asList(serviceClasses).forEach(serviceClass ->{
				classMap.put(serviceClass.getId(), serviceClass.getName());
			});
		}
		return classMap;
	}
}
