package cs.controller.portal;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import cs.dto.GuiAgentAccount;
import cs.dto.GuiChangePasswordRequest;
import cs.dto.GuiChangePasswordResponse;
import cs.dto.GuiDataTable;
import cs.dto.GuiDataTableRequest;
import cs.dto.error.GuiValidationException;
import cs.dto.portal.GuiPinRules;
import cs.dto.security.LoginSessionData;
import cs.service.AccountService;
import cs.service.AgentService;
import cs.service.AgentUserService;
import cs.service.ConfigurationService;
import cs.service.TypeConvertorService;
import cs.utility.BatchUtility;
import cs.utility.BatchUtility.BatchFileType;
import hxc.ecds.protocol.rest.Agent;
import hxc.ecds.protocol.rest.AgentAccountEx;
import hxc.ecds.protocol.rest.ExResult;
import hxc.ecds.protocol.rest.Violation;

@RestController
@RequestMapping("/papi/agents")
public class PortalAgentController
{
	@Autowired
	private ConfigurationService configService;

	@Autowired //ask @Configuration-marked class for this
	private AgentService agentService;

	@Autowired //ask @Configuration-marked class for this
	private AgentUserService agentUserService;

	@Autowired //ask @Configuration-marked class for this
	private AccountService accountService;

	@Autowired
	private LoginSessionData sessionData;

	@Autowired
	private TypeConvertorService typeConvertorService;

	private String compileExportFilter(Map<String, String> params) throws Exception
	{
		String filter = "";

		// Needs to be a number else the TS will break
		if ( params.containsKey( "id" ) ) {
			long idValue = 0;
			try {
				idValue = Long.valueOf(params.get( "id" ).trim());
			} catch(Exception e){}
			filter = addFilter( filter, "agentID", "=", String.valueOf(idValue) );
		}

		if ( params.containsKey( "accountNumber" ) )
			filter = addFilter( filter, "accountNumber", ":", params.get( "accountNumber" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "mobileNumber" ) )
			filter = addFilter( filter, "mobileNumber", ":", params.get( "mobileNumber" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "firstName" ) )
			filter = addFilter( filter, "firstName", ":", params.get( "firstName" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "surname" ) )
			filter = addFilter( filter, "surname", ":", params.get( "surname" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "domainAccountName" ) )
			filter = addFilter( filter, "domainAccountName", ":", params.get( "domainAccountName" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "state" ) && !params.get( "state" ).isEmpty() )
		{
			if ( params.get( "state" ).substring(0, 1).equals( "~" ) )
				filter = addFilter( filter, "state", "!=", params.get( "state" ).substring(1) );
			else
				filter = addFilter( filter, "state", "=", params.get( "state" ) );
		}
		if ( params.containsKey( "tierID" ) && !params.get( "tierID" ).isEmpty() )
			filter = addFilter( filter, "tierID", "=", params.get( "tierID" ) );
		if ( params.containsKey( "groupID" ) && !params.get( "groupID" ).isEmpty() )
			filter = addFilter( filter, "groupID", "=", params.get( "groupID" ) );
		if ( params.containsKey( "serviceClassID" ) && !params.get( "serviceClassID" ).isEmpty() )
			filter = addFilter( filter, "serviceClassID", "=", params.get( "serviceClassID" ) );
		if ( params.containsKey( "ownerAgentID" ) && !params.get( "ownerAgentID" ).isEmpty() )
			filter = addFilter( filter, "ownerAgentID", "=", params.get( "ownerAgentID" ) );
		if ( params.containsKey( "supplierAgentID" ) && !params.get( "supplierAgentID" ).isEmpty() )
			filter = addFilter( filter, "supplierAgentID", "=", params.get( "supplierAgentID" ) );
		return filter;
	}

	private String addAgentsOwnedByLoggedInAgentFilter(String filter)
	{
		filter = addFilter( filter, "agent.ownerAgentID", "=", String.valueOf( sessionData.getAgentId()));
		return filter;
	}

	private String compileFilter(Map<String, String> params) throws Exception
	{
		String filter = "";
		if ( params.containsKey( "id" ) ) {
			long idValue = 0;
			try {
				idValue = Long.valueOf(params.get( "id" ).trim());
			} catch(Exception e){}
			filter = addFilter( filter, "agentID", "=", String.valueOf(idValue) );
		}
		if ( params.containsKey( "accountNumber" ) )
			filter = addFilter( filter, "agent.accountNumber", ":", params.get( "accountNumber" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "mobileNumber" ) )
			filter = addFilter( filter, "agent.mobileNumber", ":", params.get( "mobileNumber" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "firstName" ) )
			filter = addFilter( filter, "agent.firstName", ":", params.get( "firstName" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "surname" ) )
			filter = addFilter( filter, "agent.surname", ":", params.get( "surname" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "domainAccountName" ) )
			filter = addFilter( filter, "agent.domainAccountName", ":", params.get( "domainAccountName" ).trim().replaceFirst("[*]$", "%") );
		if ( params.containsKey( "state" ) && !params.get( "state" ).isEmpty() )
		{
			if ( params.get( "state" ).substring(0, 1).equals( "~" ) )
				filter = addFilter( filter, "agent.state", "!=", params.get( "state" ).substring(1) );
			else
				filter = addFilter( filter, "agent.state", "=", params.get( "state" ) );
		}
		if ( params.containsKey( "tierID" ) && !params.get( "tierID" ).isEmpty() )
			filter = addFilter( filter, "agent.tierID", "=", params.get( "tierID" ) );
		if ( params.containsKey( "groupID" ) && !params.get( "groupID" ).isEmpty() )
			filter = addFilter( filter, "agent.groupID", "=", params.get( "groupID" ) );
		if ( params.containsKey( "serviceClassID" ) && !params.get( "serviceClassID" ).isEmpty() )
			filter = addFilter( filter, "agent.serviceClassID", "=", params.get( "serviceClassID" ) );


		// Always add a filter to filter out those agents which this agent is not an owner of
		filter = addAgentsOwnedByLoggedInAgentFilter(filter);

		return filter;
	}

	@RequestMapping(value="{agentId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentAccount getAccount(@PathVariable("agentId") String agentId) throws Exception
	{
		Agent agent = agentService.getAgent(agentId);
		return typeConvertorService.getGuiAgentAccountFromAgent(agent);
	}

	@RequestMapping(value="/account/root", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiAgentAccount getRootAccount() throws Exception
	{
		Agent agent = agentService.getRootAgent();
		return typeConvertorService.getGuiAgentAccountFromAgent(agent);
	}

	@RequestMapping(value="dropdown", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<Integer, String> agentListDropdown(@RequestParam(value = "_type") Optional<String> type, @RequestParam(value = "term") Optional<String> query) throws Exception
	{
		Map<Integer, String>classMap = new TreeMap<Integer,String>();
		Agent[] agents = null;
		if (type.isPresent() && query.isPresent() && type.get().equals("query"))
			agents = agentService.listAgents(query.get());
		else
			agents = agentService.listAgents();
		if (agents != null)
		{
			Arrays.asList(agents).forEach(agent ->{
				classMap.put(agent.getId(), agent.getFirstName() + " " + agent.getSurname());
			});
		}
		return classMap;
	}

	private String tsSortFromDtSort( GuiDataTableRequest dtr )
	{
		String order = "";
		for ( int i = 0; i < dtr.getOrder().size(); ++i )
		{
			GuiDataTableRequest.Order ro = dtr.getOrder().get( i );
			String dir = ro.isAscending() ? "+" : "-";
			switch( ro.getColumn().getData() )
			{
			case "accountNumber": order += "agent.accountNumber" + dir; break;
			case "mobileNumber": order += "agent.mobileNumber" + dir; break;
			case "firstName": order += "agent.firstName" + dir + "agent.surname" + dir; break;
			case "domainAccountName": order += "agent.domainAccountName" + dir; break;
			//case "supplierName": order += "" + dir; break;
			case "tierName": order += "agent.tier.name" + dir; break;
			case "groupName": order += "agent.group.name" + dir; break;
			case "balance": order += "balance" + dir; break;
			//case "bonusBalance": order += "" + dir; break;
			case "currentState": order += "agent.state" + dir; break;
			}
		}
		return order;
	}

	@RequestMapping(value="owned", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable list(@RequestParam Map<String, String> params) throws Exception
	{
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );
		String filter = this.compileFilter( params );

		//Map<Integer, Tier> tierMap = tierService.tierMap();

		ExResult<AgentAccountEx> agents = null;
		if (dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty())
		{
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				//Long agentCount = accountService.listAgentAccountsCount( filter, null );
				agents = accountService.listAgentAccountsEx( filter, null, dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr) );
				return new GuiDataTable(typeConvertorService.getGuiAgentAccountFromAgentAccountEx(agents.getInstances()), agents.getFoundRows() == null ? dtr.getStart() + ((agents.getInstances().length < dtr.getLength()) ? agents.getInstances().length : (dtr.getLength() * 2)) : agents.getFoundRows().intValue());
			}

			agents = accountService.listAgentAccountsEx( filter, null, null, null, null );
		}
		else
		{
			if ( dtr.getStart() != null && dtr.getLength() != null )
			{
				//Long agentCount = accountService.listAgentAccountsCount(filter, dtr.getSearch().getValue());
				agents = accountService.listAgentAccountsEx(filter, dtr.getSearch().getValue(), dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr) );
				return new GuiDataTable(typeConvertorService.getGuiAgentAccountFromAgentAccountEx(agents.getInstances()), agents.getFoundRows() == null ? dtr.getStart() + ((agents.getInstances().length < dtr.getLength()) ? agents.getInstances().length : (dtr.getLength() * 2)) : agents.getFoundRows().intValue());
			}

			agents = accountService.listAgentAccountsEx( filter, dtr.getSearch().getValue(), null, null, null );
		}
		return new GuiDataTable(typeConvertorService.getGuiAgentAccountFromAgentAccountEx(agents.getInstances()));
	}

	@RequestMapping(value="count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode count(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long count = 0L;
		boolean validSearch = !(dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty());

		if (docount) count = (validSearch)?accountService.listAgentAccountsCount(dtr.getSearch().getValue()):accountService.listAgentAccountsCount();

		return accountService.track(count);
	}

	@RequestMapping(value="csv", method = RequestMethod.GET)
	@ResponseBody
	public void listCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		// Need to add Filter for Agents owned by user


		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.ACCOUNT, ".csv"));

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long agentCount = 0L;
		boolean validSearch = !(dtr.getSearch() == null || dtr.getSearch().getValue().isEmpty());

		agentCount = (validSearch)?accountService.listAgentAccountsCount(dtr.getSearch().getValue()):accountService.listAgentAccountsCount();

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(agentCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		agentService.csvExport(params.get("uniqid"), outputStream, (validSearch)?dtr.getSearch().getValue():null, recordsPerChunk, agentCount, true, null);
	}

	private String addFilter(String filter, String name, String operator, String value)
	{
		if ( !value.equals("") )
		{
			if ( !filter.equals("") )
				filter += "+";
			filter += name + operator + "'" + value + "'";
		}
		return filter;
	}

	@RequestMapping(value="search/count", method = RequestMethod.GET)
	@ResponseBody
	public ObjectNode countSearchResults(@RequestParam Map<String, String> params, @RequestParam(defaultValue="true") boolean docount) throws Exception
	{
		String filter = this.compileExportFilter( params );

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long count = 0L;

		if (docount) count = agentService.listAgentsCount(filter, dtr.getSearch().getValue());

		return accountService.track(count);
	}

	@RequestMapping(value="search", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiDataTable search(@RequestParam Map<String, String> params) throws Exception
	{
		String filter = this.compileFilter( params );

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		//Map<Integer, Tier> tierMap = tierService.tierMap();

		ExResult<AgentAccountEx> agents = null;
		if ( dtr.getStart() != null && dtr.getLength() != null )
		{
			//Long agentCount = accountService.listAgentAccountsCount( filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue() );
			agents = accountService.listAgentAccountsEx( filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue(), dtr.getStart(), dtr.getLength(), tsSortFromDtSort(dtr) );
			return new GuiDataTable(typeConvertorService.getGuiAgentAccountFromAgentAccountEx(agents.getInstances()), agents.getFoundRows() == null ? dtr.getStart() + ((agents.getInstances().length < dtr.getLength()) ? agents.getInstances().length : (dtr.getLength() * 2)) : agents.getFoundRows().intValue());
		}

		agents = accountService.listAgentAccountsEx(filter, dtr.getSearch() == null ? null : dtr.getSearch().getValue(), null, null, null);
		return new GuiDataTable(typeConvertorService.getGuiAgentAccountFromAgentAccountEx(agents.getInstances()));
	}

	@RequestMapping(value="search/csv", method = RequestMethod.GET)
	@ResponseBody
	public void listSearchResultsCsv(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception
	{
		BatchUtility.setExportHeaders(response, BatchUtility.getFilename(sessionData.getCompanyPrefix(), BatchFileType.ACCOUNT, ".csv"));

		String filter = this.compileExportFilter( params );

		GuiDataTableRequest dtr = new GuiDataTableRequest( params );

		long agentCount = 0L;

		agentCount = agentService.listAgentsCount(filter, dtr.getSearch().getValue());

		int recordsPerChunk = BatchUtility.getRecordsPerChunk(agentCount, configService.getBatchConfig().getBatchDownloadChunkSize());

		OutputStream outputStream = response.getOutputStream();
		agentService.csvExport(params.get("uniqid"), outputStream, dtr.getSearch().getValue(), recordsPerChunk, agentCount, true, null);
	}

	@RequestMapping(value="all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public Agent[] serverList() throws Exception
	{
		Agent[] accounts = agentService.listAgents();
		return accounts;
	}

	private void validateAgent(GuiAgentAccount agent) throws GuiValidationException
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();

		if (agent.getDateOfBirth() != null)
		{
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			if (agent.getDateOfBirth().compareTo(now) >= 0 )
			{
				violations.add(new Violation("dateOfBirthInFuture", "dateOfBirth", sdf.format(now), String.format("Birth date must be in the past")));
			}
		}

		if (agent.getExpirationDate() != null)
		{
			Date now = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			if (agent.getExpirationDate().compareTo(now) < 0 )
			{
				violations.add(new Violation("expirationDatePassed", "expirationDate", sdf.format(now), String.format("Expiration date must be not be in the past")));
			}
		}

		if (violations != null && violations.size() > 0)
			throw new GuiValidationException(violations, "Invalid search criteria");
	}

	@RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public Agent create(@RequestBody(required = true) GuiAgentAccount newAgent, Locale locale) throws Exception
	{
		validateAgent(newAgent);

		newAgent.setImei("?"); // FIXME
		newAgent.setImsi("?"); // FIXME
		agentService.create(newAgent.getAgent());
		return newAgent;
	}

	@RequestMapping(value="{agent}", method = RequestMethod.DELETE)
	public String delete(@PathVariable("agent") String agentid) throws Exception
	{
		agentService.delete(agentid);
		return "{}";
	}

	@RequestMapping(value="{agent}", method = RequestMethod.PUT)
	public String update(@PathVariable("agent") String agentId, @RequestBody(required = true) GuiAgentAccount newAgent, Locale locale) throws Exception
	{
		validateAgent(newAgent);
		Agent agent = newAgent.getAgent();
		agentService.update(/*typeConvertorService.getAgentFromGuiAgent(newAgent)*/agent);
		return "{}";
	}

	@RequestMapping(value="suspend/{agent}", method = RequestMethod.PUT)
	public String suspend(@PathVariable("agent") String agentId, Locale locale) throws Exception
	{
		Agent agent = agentService.getAgent(agentId);
		agent.setState(Agent.STATE_SUSPENDED);
		agentService.update(/*typeConvertorService.getAgentFromGuiAgent(newAgent)*/agent);
		return "{}";
	}

	@RequestMapping(value="deactivate/{agent}", method = RequestMethod.PUT)
	public String deactivate(@PathVariable("agent") String agentId, Locale locale) throws Exception
	{
		Agent agent = agentService.getAgent(agentId);
		agent.setState(Agent.STATE_DEACTIVATED);
		agentService.update(/*typeConvertorService.getAgentFromGuiAgent(newAgent)*/agent);
		return "{}";
	}

	@RequestMapping(value="activate/{agent}", method = RequestMethod.PUT)
	public String activate(@PathVariable("agent") String agentId, Locale locale) throws Exception
	{
		Agent agent = agentService.getAgent(agentId);
		agent.setState(Agent.STATE_ACTIVE);
		agentService.update(/*typeConvertorService.getAgentFromGuiAgent(newAgent)*/agent);
		return "{}";
	}

	@RequestMapping(value="pinreset/{agent}", method = RequestMethod.PUT)
	public String pinReset(@PathVariable("agent") String agentId, Locale locale) throws Exception
	{
		Agent agent = agentService.getAgent(agentId);
		if (agent.isTemporaryPin()) {
			agent.setTemporaryPin(false);
			agentService.update(agent);
		}
		agent.setTemporaryPin(true);
		agentService.update(agent);
		return "{}";
	}

	@RequestMapping(value="/change_password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiChangePasswordResponse changePassword(@RequestBody(required = true) GuiChangePasswordRequest changePasswordRequest, Locale locale) throws Exception
	{
		return agentUserService.changePassword(changePasswordRequest);
	}

	/**
	 * Retrieve Configuration Settings reflecting the PIN rules
	 */
	@RequestMapping(value = "passwordrules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public GuiPinRules getPinRules() throws Exception
	{
		return agentService.extractPinChangeRules();
	}

	@RequestMapping(value="imsiUnlock/{agent}", method = RequestMethod.PUT)
	public String imsiUnlock(@PathVariable("agent") String agentId, Locale locale) throws Exception
	{
		Agent agent = agentService.getAgent(agentId);
		agent.setImsiLockedOut(false);
		agentService.update(agent);
		return "{}";
	}
}
