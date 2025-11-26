package cs.service;

import java.text.DecimalFormatSymbols;
import java.time.*;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import cs.dto.*;
import hxc.ecds.protocol.rest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import cs.dto.GuiTier.TierType;
import cs.dto.GuiTransferRule.StateEnum;
import cs.dto.GuiWorkflowRequest.WorkflowRequestType;
import cs.dto.error.GuiViolation;
import cs.dto.security.LoginSessionData;
import cs.service.workflow.WorkFlowService;
import hxc.ecds.protocol.rest.config.TransactionsConfig;

@Service
public class TypeConvertorService
{
	private static final Logger logger = LoggerFactory.getLogger(TypeConvertorService.class);
	public static final DecimalFormatSymbols PERCENTAGE_LOCALE = new DecimalFormatSymbols(Locale.US);

	@Autowired
	ObjectMapper mapper;

	@Autowired
	WebUserService webUserService;

	@Autowired
	AgentUserService agentUserService;

	@Autowired
	TierService tierService;

	@Autowired
	AccountService accountService;

	@Autowired
	AgentService agentService;

	@Autowired
	GroupService groupService;

	@Autowired
	TdrService tdrService;

	@Autowired
	TransferRuleService transferRuleService;

	@Autowired
	ServiceClassService serviceClassService;

	@Autowired
	private RoleService roleService;

	@Autowired
	AreaService areaService;

	@Autowired
	CellGroupService cellGroupService;

	@Autowired
	BundlesService bundlesService;

	@Autowired
	PromotionsService promotionsService;

	@Autowired
	private WorkFlowService workFlowService;

	@Autowired
	LoginSessionData sessionData;

	public GuiGroup[] getGuiGroupFromGroup(Group[] groups)
	{
		Map<Integer, Tier> tierMap = new HashMap<Integer, Tier>();
		List<GuiGroup> groupList = new ArrayList<GuiGroup>();
		Arrays.asList(groups).forEach((group) ->{
			GuiGroup guiGroup = getGuiGroupFromGroup(group, tierMap);
			groupList.add(guiGroup);
		});
		return groupList.toArray(new GuiGroup[groupList.size()]);
	}

	public Group getGroupFromGuiGroup(GuiGroup guiGroup)
	{
		Group group = new GuiGroup();
		BeanUtils.copyProperties(guiGroup, group);
		return group;
	}

	public GuiGroup getGuiGroupFromGroup(Group group, Map<Integer, Tier> tierMap)
	{
		GuiGroup guiGroup = new GuiGroup();
		//BeanUtils.copyProperties(group, guiGroup, Common.getNullPropertyNames(group));
		BeanUtils.copyProperties(group, guiGroup);
		Tier tier;
		try
		{
			Integer key = group.getTierID();
			if (tierMap != null && tierMap.containsKey(key))
			{
				tier = tierMap.get(key);
			}
			else
			{
				tier = tierService.getTier(key);
				if (tierMap != null) tierMap.put(tier.getId(), tier);
			}
			guiGroup.setTierName(tier.getName());
		}
		catch (Exception e)
		{
			guiGroup.setTierName("Missing Tier");
		}

		return guiGroup;
	}

	public GuiTier[] getGuiTierFromTier(Tier[] tiers)
	{
		List<Tier> allTiers = new ArrayList<Tier>(Arrays.asList(tiers));
		List<GuiTier> tierList = new ArrayList<GuiTier>();
		Arrays.asList(tiers).forEach((tier) ->{
			GuiTier currentTier = getGuiTierFromTier(tier);
			currentTier.initTransferBetweenTiers(allTiers);
			tierList.add(currentTier);
		});
		return tierList.toArray(new GuiTier[tierList.size()]);
	}

	public GuiTier getGuiTierFromTier(Tier tier)
	{
		GuiTier currentTier = new GuiTier(tier);
		return currentTier;
	}

	public static Integer timeOfDayStringToSeconds(String timeOfDayString, String fieldName, List<Violation> violations)
	{
		Objects.requireNonNull(timeOfDayString, "timeOfDayString may not be null");
		Objects.requireNonNull(fieldName, "fieldName may not be null");
		Objects.requireNonNull(violations, "violations may not be null");
		String[] split = timeOfDayString.split(":");
		boolean failure = false;
		Integer result = null;
		int initialViolationCount = violations.size();
		if (split.length == 3 )
		{
			try
			{
				int hours = Integer.valueOf(split[0]);
				int minutes = Integer.valueOf(split[1]);
				int seconds = Integer.valueOf(split[2]);
				if (hours < 0 || hours > 23 )
				{
					violations.add(new Violation(fieldName + "InvalidHour", fieldName + "String", timeOfDayString, String.format("Invalid hour, must be 00-23")));
					failure = true;
				}
				if (minutes < 0 || minutes > 59 )
				{
					violations.add(new Violation(fieldName + "InvalidMinute", fieldName + "String", timeOfDayString, String.format("Invalid minute, must be 00-59")));
					failure = true;
				}
				if (seconds < 0 || seconds > 59 )
				{
					violations.add(new Violation(fieldName + "InvalidSecond", fieldName + "String", timeOfDayString, String.format("Invalid second, must be 00-59")));
					failure = true;
				}
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
				simpleDateFormat.parse(timeOfDayString);
				if ( !failure ) result = hours * 3600 + minutes * 60 + seconds;
			}
			catch(NumberFormatException|ParseException exception)
			{
				violations.add(new Violation(fieldName + "Invalid", fieldName + "String", timeOfDayString, String.format("Must be in HH:MM:SS format")));
				failure = true;
			}
		}
		else
		{
			violations.add(new Violation(fieldName + "Invalid", fieldName + "String", timeOfDayString, String.format("Must be in HH:MM:SS format")));
			failure = true;
		}
		// If somehow there is no result and no violations ...
		if ( result == null && initialViolationCount == violations.size() )
		{
			violations.add(new Violation(fieldName + "Invalid", fieldName + "String", timeOfDayString, String.format("Could not decode value ...")));
			failure = true;
		}
		return result;
	}

	public static LocalTime timeOfDayStringToLocalTime(String timeOfDayString, String fieldName, List<Violation> violations)
	{
		Integer seconds = timeOfDayStringToSeconds(timeOfDayString, fieldName, violations);
		if (seconds == null) return null;
		LocalTime localTime = LocalTime.ofSecondOfDay(seconds);
		return localTime;
	}

	public static String timeOfDaySecondsToString( int timeOfDaySeconds )
	{
		int hour = (timeOfDaySeconds / (60 * 60 ));
		int minute = (timeOfDaySeconds % (60 * 60)) / 60;
		int second = (timeOfDaySeconds % (60 * 60)) % 60;
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	public static String localTimeToString( LocalTime localTime )
	{
		return timeOfDaySecondsToString(localTime.toSecondOfDay());
	}

	public GuiTransactionsConfig getGuiTransactionsConfigFromTransactionsConfig(TransactionsConfig transactionsConfig)
	{
		GuiTransactionsConfig guiTransactionsConfig = new GuiTransactionsConfig();
		BeanUtils.copyProperties(transactionsConfig, guiTransactionsConfig);
		guiTransactionsConfig.setOltpTransactionCleanupTimeOfDayString(localTimeToString(transactionsConfig.getOltpTransactionCleanupTimeOfDay()));
		guiTransactionsConfig.setOlapTransactionCleanupTimeOfDayString(timeOfDaySecondsToString(transactionsConfig.getOlapTransactionCleanupTimeOfDay()));
		guiTransactionsConfig.setOlapSyncTimeOfDayString(timeOfDaySecondsToString(transactionsConfig.getOlapSyncTimeOfDay()));
		return guiTransactionsConfig;
	}

	public TransactionsConfig getTransactionsConfigFromGuiTransactionsConfig(GuiTransactionsConfig guiTransactionsConfig, List<Violation> violations)
	{
		TransactionsConfig transactionsConfig = new TransactionsConfig();
		BeanUtils.copyProperties(guiTransactionsConfig, transactionsConfig);
		Objects.requireNonNull(guiTransactionsConfig, "guiTransactionsConfig may not be null");
		Objects.requireNonNull(transactionsConfig, "transactionsConfig may not be null");
		LocalTime oltpTransactionCleanupTimeOfDay = timeOfDayStringToLocalTime(guiTransactionsConfig.getOltpTransactionCleanupTimeOfDayString(), "oltpTransactionCleanupTimeOfDay", violations);
		if ( oltpTransactionCleanupTimeOfDay != null )
		{
			transactionsConfig.setOltpTransactionCleanupTimeOfDay( oltpTransactionCleanupTimeOfDay );
		}
		Integer olapTransactionCleanupTimeOfDay = timeOfDayStringToSeconds(guiTransactionsConfig.getOlapTransactionCleanupTimeOfDayString(), "olapTransactionCleanupTimeOfDay", violations);
		if ( olapTransactionCleanupTimeOfDay != null )
		{
			transactionsConfig.setOlapTransactionCleanupTimeOfDay( olapTransactionCleanupTimeOfDay );
		}
		Integer olapSyncTimeOfDay = timeOfDayStringToSeconds(guiTransactionsConfig.getOlapSyncTimeOfDayString(), "olapSyncTimeOfDay", violations);
		if ( olapSyncTimeOfDay != null )
		{
			transactionsConfig.setOlapSyncTimeOfDay( olapSyncTimeOfDay );
		}
		return transactionsConfig;
	}

	public GuiTransferRule[] getGuiTransferRulesFromTransferRules(TransferRule[] rules)
	{
		List<GuiTransferRule> ruleList = new ArrayList<GuiTransferRule>();
		Arrays.asList(rules).forEach((rule) ->{
			GuiTransferRule currentRule = getGuiTransferRuleFromTransferRule(rule);
			ruleList.add(currentRule);
		});
		return ruleList.toArray(new GuiTransferRule[ruleList.size()]);
	}


	public GuiTransferRule getGuiTransferRuleFromTransferRule(TransferRule rule)
	{

		GuiTransferRule currentRule = new GuiTransferRule(rule);
		try
		{
			Tier sourceTier = tierService.getTier(currentRule.getSourceTierID());

			currentRule.setSourceTierName(sourceTier.getName());

			Tier targetTier = tierService.getTier(currentRule.getTargetTierID());
			currentRule.setTargetTierName(targetTier.getName());

			Integer serviceClassId = currentRule.getServiceClassID();
			if (serviceClassId != null) currentRule.setServiceClassName(serviceClassService.getServiceClass(serviceClassId).getName());

			Integer targetServiceClassId = currentRule.getTargetServiceClassID();
			if (targetServiceClassId != null) currentRule.setTargetServiceClassName(serviceClassService.getServiceClass(targetServiceClassId).getName());

			Integer groupId = currentRule.getGroupID();
			if (groupId != null) currentRule.setGroupName(groupService.getGroup(String.valueOf(groupId)).getName());

			Integer targetGroupId = currentRule.getTargetGroupID();
			if (targetGroupId != null) currentRule.setTargetGroupName(groupService.getGroup(String.valueOf(targetGroupId)).getName());

			String state = rule.getState();
			if (state != null && state.equals(TransferRule.STATE_ACTIVE)) {
				currentRule.setCurrentState(StateEnum.ACTIVE);
				currentRule.setRuleActive(true);
			}
			else {
				currentRule.setCurrentState(StateEnum.INACTIVE);
			}

			// Trade Bonus Percentage
			DecimalFormat buyerTradeBonusDecimalFormat = new DecimalFormat("###.000", PERCENTAGE_LOCALE);
			BigDecimal buyerTradeBonusPercentage = currentRule.getBuyerTradeBonusPercentage();
			String buyerTradeBonusString = buyerTradeBonusDecimalFormat.format(buyerTradeBonusPercentage.multiply(new BigDecimal(100)));
			currentRule.setBuyerTradeBonusPercentageString((buyerTradeBonusString.startsWith(".")?"0":"")+buyerTradeBonusString);

			// Target Bonus Percentage
			DecimalFormat targetBonusDecimalFormat = new DecimalFormat("###.0000", PERCENTAGE_LOCALE);
			BigDecimal targetBonusPercentage = currentRule.getTargetBonusPercentage();
			String targetBonusPercentageString = targetBonusPercentage != null ? targetBonusDecimalFormat.format(targetBonusPercentage.multiply(new BigDecimal(100))) : "";
			currentRule.setTargetBonusPercentageString((targetBonusPercentageString.startsWith(".")?"0":"")+targetBonusPercentageString);

			// Cumulative Trade Bonus
			DecimalFormat decimalFormat = new DecimalFormat("###.0000", PERCENTAGE_LOCALE);
			BigDecimal targetTierDownStream = targetTier.getDownStreamPercentage();
			targetTierDownStream = targetTierDownStream.multiply(new BigDecimal(100));
			BigDecimal dividend = new BigDecimal(100);
			
			String tradeBonusCumulativeString = decimalFormat.format(targetTierDownStream);

			BigDecimal buyerTradeBonus = currentRule.getBuyerTradeBonusPercentage();

			if ( buyerTradeBonus != null ) {
				buyerTradeBonus = buyerTradeBonus.multiply(new BigDecimal(100));
				tradeBonusCumulativeString = decimalFormat.format(targetTierDownStream
							.add(buyerTradeBonus)
							.add(targetTierDownStream
							.multiply(buyerTradeBonus)
							.divide(dividend)));
			}
			currentRule.setTradeBonusCumulativePercentageString((tradeBonusCumulativeString.startsWith(".")?"0":"")+tradeBonusCumulativeString);

			// Start Time of Day
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
			if (rule.getStartTimeOfDay() != null)
				currentRule.setStartTimeOfDayString(simpleDateFormat.format(rule.getStartTimeOfDay()));

			// End Time of Day
			if (rule.getEndTimeOfDay() != null)
				currentRule.setEndTimeOfDayString(simpleDateFormat.format(rule.getEndTimeOfDay()));

			// Area Name
			if(rule.getAreaID() != null)
			{
				int areaID = rule.getAreaID();
				Area area = areaService.getArea(areaID);
				currentRule.setAreaName(area.getName());
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return currentRule;
	}

	public TransferRule getTransferRuleFromGuiTransferRule(GuiTransferRule newTransferRule)
	{
		ArrayList<Violation> violations = new ArrayList<Violation>();
		return getTransferRuleFromGuiTransferRule(newTransferRule, violations);
	}

	public TransferRule getTransferRuleFromGuiTransferRule(GuiTransferRule newTransferRule, List<Violation> violations)
	{
		TransferRule transferRule = new TransferRule();
		BeanUtils.copyProperties(newTransferRule, transferRule);
		StateEnum currentState = newTransferRule.getCurrentState();
		transferRule.setState((currentState != null && currentState == StateEnum.ACTIVE)?TransferRule.STATE_ACTIVE:TransferRule.STATE_INACTIVE);
		String buyerTradeBonusPercentageString = newTransferRule.getBuyerTradeBonusPercentageString();
		BigDecimal convertedBuyerTradeBonus = new BigDecimal((buyerTradeBonusPercentageString != null && buyerTradeBonusPercentageString.length() > 0)?buyerTradeBonusPercentageString:"0.0");
		Integer daysOfWeek = GuiTransferRule.toInteger(newTransferRule.getCurrentDays());
		transferRule.setDaysOfWeek(daysOfWeek);
		transferRule.setBuyerTradeBonusPercentage(convertedBuyerTradeBonus.divide(new BigDecimal(100)));

		////
		String targetBonusPercentageString = newTransferRule.getTargetBonusPercentageString();
		if( targetBonusPercentageString != null && targetBonusPercentageString.length() > 0 )
		{
			BigDecimal convertedTargetBonusValue = new BigDecimal(targetBonusPercentageString);
			transferRule.setTargetBonusPercentage(convertedTargetBonusValue.divide(new BigDecimal(100)));
		}
		else
		{
			transferRule.setTargetBonusPercentage(null);
		}

		transferRule.setTargetBonusProfile(newTransferRule.getTargetBonusProfile() != null && newTransferRule.getTargetBonusProfile().length() > 0 ? newTransferRule.getTargetBonusProfile() : null);
		////
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		int startTimeSec = 0;
		int endTimeSec = 0;
		boolean validStartTime = false;
		boolean validEndTime = false;
		if (!newTransferRule.getStartTimeOfDayString().isEmpty())
		{
			if (newTransferRule.getEndTimeOfDayString().isEmpty())
				violations.add(new Violation("endTimeOfDayEmpty", "endTimeOfDayString", newTransferRule.getStartTimeOfDayString(), String.format("Required when From Time is set")));

			String[] split = newTransferRule.getStartTimeOfDayString().split(":");
			if (split.length == 3 )
			{
				int hours = Integer.valueOf(split[0]);
				int minutes = Integer.valueOf(split[1]);
				int seconds = Integer.valueOf(split[2]);
				if (hours < 0 || hours > 23 )
					violations.add(new Violation("startTimeOfDayInvalidHour", "startTimeOfDayString", newTransferRule.getStartTimeOfDayString(), String.format("Invalid hour, must be 00-23")));
				if (minutes < 0 || minutes > 59 )
					violations.add(new Violation("startTimeOfDayInvalidMinute", "startTimeOfDayString", newTransferRule.getStartTimeOfDayString(), String.format("Invalid minute, must be 00-59")));
				if (seconds < 0 || seconds > 59 )
					violations.add(new Violation("startTimeOfDayInvalidSecond", "startTimeOfDayString", newTransferRule.getStartTimeOfDayString(), String.format("Invalid second, must be 00-59")));
				startTimeSec = hours * 3600 + minutes * 60 + seconds;
				try
				{
					transferRule.setStartTimeOfDay(sdf.parse(newTransferRule.getStartTimeOfDayString()));
					validStartTime = true;
				}
				catch( ParseException e )
				{
					violations.add(new Violation("startTimeOfDayInvalid", "startTimeOfDayString", newTransferRule.getStartTimeOfDayString(), String.format("Must be in HH:MM:SS format")));
				}
			}
			else
				violations.add(new Violation("startTimeOfDayInvalid", "startTimeOfDayString", newTransferRule.getStartTimeOfDayString(), String.format("Must be in HH:MM:SS format")));
		}
		else transferRule.setStartTimeOfDay(null);
		if (!newTransferRule.getEndTimeOfDayString().isEmpty())
		{
			if (newTransferRule.getStartTimeOfDayString().isEmpty())
				violations.add(new Violation("startTimeOfDayEmpty", "startTimeOfDayString", newTransferRule.getEndTimeOfDayString(), String.format("Required when To Time is set")));

			String[] split = newTransferRule.getEndTimeOfDayString().split(":");
			if (split.length == 3 )
			{
				int hours = Integer.valueOf(split[0]);
				int minutes = Integer.valueOf(split[1]);
				int seconds = Integer.valueOf(split[2]);
				if (hours < 0 || hours > 23 )
					violations.add(new Violation("endTimeOfDayInvalidHour", "endTimeOfDayString", newTransferRule.getEndTimeOfDayString(), String.format("Invalid hour, must be 00-23")));
				if (minutes < 0 || minutes > 59 )
					violations.add(new Violation("endTimeOfDayInvalidMinute", "endTimeOfDayString", newTransferRule.getEndTimeOfDayString(), String.format("Invalid minute, must be 00-59")));
				if (seconds < 0 || seconds > 59 )
					violations.add(new Violation("endTimeOfDayInvalidSecond", "endTimeOfDayString", newTransferRule.getEndTimeOfDayString(), String.format("Invalid second, must be 00-59")));
				endTimeSec = hours * 3600 + minutes * 60 + seconds;
				try
				{
					transferRule.setEndTimeOfDay(sdf.parse(newTransferRule.getEndTimeOfDayString()));
					validEndTime = true;
				}
				catch( ParseException e )
				{
					violations.add(new Violation("endTimeOfDayInvalid", "endTimeOfDayString", newTransferRule.getEndTimeOfDayString(), String.format("Must be in HH:MM:SS format")));
				}
			}
			else
				violations.add(new Violation("endTimeOfDayInvalid", "endTimeOfDayString", newTransferRule.getEndTimeOfDayString(), String.format("Must be in HH:MM:SS format")));
		}
		else transferRule.setEndTimeOfDay(null);

		if ( validStartTime && validEndTime )
		{
			if ( startTimeSec >= endTimeSec )
			{
				violations.add(new Violation("endTimeOfDayInvalid", "endTimeOfDayString", newTransferRule.getStartTimeOfDayString(), String.format("Must be after start time")));
			}
		}

		String jsonInString;
		try
		{
			jsonInString = mapper.writeValueAsString(transferRule);
			logger.info(jsonInString);
		}
		catch (JsonProcessingException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}

		return transferRule;
	}

	public WebUser guiWebUserToWebUser(GuiWebUser guser) throws Exception
	{

		WebUser wuser = new WebUser();
		BeanUtils.copyProperties(guser, wuser);

		// Now sort out roles
		Role [] allRoles = roleService.listRoles();
		List<Role> roles = new ArrayList<>();
		for (Role role : allRoles)
		{
			if (role.getId() == guser.getRoleID())
			{
				roles.add(role);
				break;
			}
		}

		wuser.setRoles(roles);

		return wuser;
	}

	public AgentUser guiAgentUserToAgentUser(GuiAgentUser guser) throws Exception
	{

		AgentUser auser = guser.extractAgentUser();
		return auser;
	}



	public TransferRule[] getTransferRulesFromGuiTransferRules(GuiTransferRule[] rules)
	{
		List<TransferRule> ruleList = new ArrayList<TransferRule>();
		Arrays.asList(rules).forEach((rule) ->{
			TransferRule currentRule = getTransferRuleFromGuiTransferRule(rule);
			ruleList.add(currentRule);
		});
		return ruleList.toArray(new TransferRule[ruleList.size()]);
	}

	/*
	 * @param role is the original role from the backend seerver
	 * @param permmap is a map of permissions with the group as the key
	 *
	 * This method will take the map of all available permissions and set
	 * a boolean on each permission to indicate if the permission is
	 * available to this user or not.
	 */
	public GuiRole getGuiRoleFromRole(Role role, Map<String, GuiPermissionGroup> permmap)
	{
		Map<String, Permission>actualPermissions = new HashMap<String, Permission>();
		GuiRole convertedRole = new GuiRole();

		for (Permission perm : role.getPermissions())
		{
			String key = perm.getGroup().toLowerCase()+perm.getName().toLowerCase();
			if (!key.contains("tiers")) // Used for testing
				actualPermissions.put(key, perm);
		}

		for (String key: permmap.keySet())
		{
			GuiPermissionGroup permGroup = permmap.get(key);
			for (GuiPermission perm: permGroup.getPermissions())
			{
				try
				{
					GuiPermission newPermission = (GuiPermission) perm.clone();
					String permName = key.toLowerCase() + newPermission.getName().toLowerCase();
					newPermission.setEnabled(actualPermissions.containsKey(permName));
					newPermission.setGroup(permGroup.getGroupName());
					convertedRole.addPermission(newPermission);
				}
				catch (CloneNotSupportedException e)
				{
					logger.error("", e);
				}
			}
		}
		convertedRole.setPermanent(role.isPermanent());
		convertedRole.setId(role.getId());
		convertedRole.setCompanyID(role.getCompanyID());
		convertedRole.setVersion(role.getVersion());
		convertedRole.setName(role.getName());
		convertedRole.setDescription(role.getDescription());
		return convertedRole;
	}

	public Role getRoleFromGuiRole(GuiRole role)
	{
		Role convertedRole = new Role();
		List<Permission> permissionSet = new ArrayList<Permission>();

		role.getPermissions().forEach((permission) ->{
			Permission perm = new Permission();
			perm.setDescription(permission.getDescription());
			perm.setId(permission.getId());
			perm.setName(permission.getName());
			perm.setSupplierOnly(permission.isSupplierOnly());
			perm.setVersion(permission.getVersion());
			perm.setGroup(permission.getGroup());
			permissionSet.add(perm);
		});
		convertedRole.setPermissions(permissionSet);
		convertedRole.setDescription(role.getDescription());
		if (role.getId() > 0) convertedRole.setId(role.getId());
		convertedRole.setName(role.getName());
		convertedRole.setType(role.getType());
		return convertedRole;
	}

	public GuiRole[] getGuiRoleFromRole(Role[] roles)
	{
		List<GuiRole>guiRoleList = new ArrayList<GuiRole>();

		List<Role> roleList = Arrays.asList(roles);
		roleList.forEach((role) ->{
			Map<String,ArrayNode>permMap = new TreeMap<String, ArrayNode>();
			role.getPermissions().forEach((perm) ->{
				String name = String.valueOf(perm.getGroup());
				ArrayNode jsonPermList = null;
				if (permMap.containsKey(name))
				{
					jsonPermList = permMap.get(name);
				}
				else
				{
					jsonPermList = mapper.createArrayNode();
				}
				ObjectNode jsonPerm = mapper.createObjectNode();

				jsonPerm.put("id", perm.getId());
				jsonPerm.put("group", perm.getGroup());
				jsonPerm.put("description", perm.getDescription());
				jsonPermList.add(jsonPerm);
				permMap.put(name, jsonPermList);
			});

			permMap.forEach((key, value) ->{
				GuiRole guiRole = new GuiRole();

				guiRole.setId(role.getId());
				guiRole.setCompanyID(role.getCompanyID());
				guiRole.setName(role.getName());
				guiRole.setDescription(role.getDescription());
				guiRole.setPermanent(role.isPermanent());
				guiRole.setPermissionGroup(key);

				guiRole.setPermList(value.toString());

				guiRoleList.add(guiRole);
			});
		});
		return guiRoleList.toArray(new GuiRole[guiRoleList.size()]);
	}

	public Role[] getRoleFromGuiRole(GuiRole[] roles)
	{
		List<Role>roleList = new ArrayList<Role>();
		for (GuiRole role : roles)
		{
			roleList.add(getRoleFromGuiRole(role));
		}

		return roleList.toArray(new Role[roleList.size()]);
	}

	public GuiAgentAccount getGuiAgentAccountFromAgent(Agent agent)
	{
		GuiAgentAccount guiAgent = new GuiAgentAccount(agent);
		try
		{
			Account account = accountService.getAccount(agent.getId());
			guiAgent.setBalance(account.getBalance());
			guiAgent.setBonusBalance(account.getBonusBalance());
			guiAgent.setOnHoldBalance(account.getOnHoldBalance());
			guiAgent.setAccountTamperedWith(account.isTamperedWith());
			Tier tier = tierService.getTier(agent.getTierID());
			if (tier != null) {
				guiAgent.setTierName(tier.getName());
				guiAgent.setTierTypeCode(tier.getType());
			}

			// Lookup supplier Name
			if(agent.getSupplierAgentID() != null) {
				Agent supplierAgent = agentService.getAgent(agent.getSupplierAgentID());
				guiAgent.setSupplierName(supplierAgent.getFirstName() + " " + supplierAgent.getSurname());
			}

			// Lookup owner agent Name
			if(agent.getOwnerAgentID() != null) {
				Agent ownerAgent = agentService.getAgent(agent.getOwnerAgentID());
				guiAgent.setOwnerName(ownerAgent.getFirstName() + " " + ownerAgent.getSurname());
			}

			// Lookup area Name
			if(agent.getAreaID() != null) {
				Area area = areaService.getArea(agent.getAreaID());
				guiAgent.setAreaName(area.getName());
				guiAgent.setAreaType(area.getType());
			}

			// Lookup role Name
			if(agent.getRoleID() > 0) {
				Role role = roleService.getRole(agent.getRoleID());
				guiAgent.setRoleName( role.getName() );
			}

			// Retrieve Summary Information

			Integer serviceClassId = agent.getServiceClassID();
			if (serviceClassId != null) guiAgent.setServiceClassName(serviceClassService.getServiceClass(serviceClassId).getName());

			Integer groupId = agent.getGroupID();
			if (groupId != null) guiAgent.setGroupName(groupService.getGroup(String.valueOf(groupId)).getName());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return guiAgent;
	}

	public GuiPortalAgent getGuiPortalAgentFromGuiAgentAccount(GuiAgentAccount guiAgentAccount) throws Exception
	{
		GuiPortalAgent gpa = new GuiPortalAgent();
		BeanUtils.copyProperties(guiAgentAccount, gpa);

		if (sessionData.getAgentUserID() != null) {
			AgentUser au = agentUserService.getAgentUser( sessionData.getAgentUserID() );

			gpa.setAgentUser(au);
			if (au.getRoleID() > 0)
			{
				Role role = roleService.getRole(au.getRoleID());
				gpa.setAgentUserRoleName(role.getName());
			}

		}

		return gpa;
	}

	public GuiAgentAccount[] getGuiAgentAccountFromAgent(Agent[] agents, Map<Integer, Tier> tierMap)
	{
		List<GuiAgentAccount>guiAgentAccountList = new ArrayList<GuiAgentAccount>();

		List<Agent> agentList = Arrays.asList(agents);
		agentList.forEach((agent) ->{
			GuiAgentAccount guiAgent = getGuiAgentAccountFromAgent(agent);
			// Convert To GuiTier Type using String type from Tier
			guiAgent.setTierType( TierType.getTierType(tierMap.get(agent.getTierID()).getType()) );
			guiAgent.setTierTypeCode( tierMap.get(agent.getTierID()).getType() );
			guiAgentAccountList.add(guiAgent);
		});
		return guiAgentAccountList.toArray(new GuiAgentAccount[guiAgentAccountList.size()]);
	}

	public GuiAgentAccount getGuiAgentAccountFromAgentAccount(AgentAccount agent, Map<Integer, Tier> tierMap)
	{
		GuiAgentAccount guiAgent = new GuiAgentAccount(agent);
		try
		{
			//Account account = accountService.getAccount(agent.getAgent().getId());
			guiAgent.setBalance(agent.getBalance());
			guiAgent.setBonusBalance(agent.getBonusBalance());
			guiAgent.setOnHoldBalance(agent.getOnHoldBalance());
			Tier tier = tierMap.get(agent.getAgent().getTierID());
			if (tier != null)
			{
				guiAgent.setTierName(tier.getName());
				// Convert To GuiTier Type using String type from Tier
				guiAgent.setTierTypeCode(tier.getType());
				guiAgent.setTierType(TierType.getTierType(tier.getType()));
			}

			if(agent.getAgent().getSupplierAgentID() != null)
			{
				Agent supplierAgent = agentService.getAgent(agent.getAgent().getSupplierAgentID());
				guiAgent.setSupplierName(supplierAgent.getFirstName() + " " + supplierAgent.getSurname());
			}

			// Lookup owner agent Name
			if(agent.getAgent().getOwnerAgentID() != null) {
				Agent ownerAgent = agentService.getAgent(agent.getAgent().getOwnerAgentID());
				guiAgent.setOwnerName(ownerAgent.getFirstName() + " " + ownerAgent.getSurname());
			}

			Integer serviceClassId = agent.getAgent().getServiceClassID();
			if (serviceClassId != null) guiAgent.setServiceClassName(serviceClassService.getServiceClass(serviceClassId).getName());

			Integer groupId = agent.getAgent().getGroupID();
			if (groupId != null) guiAgent.setGroupName(groupService.getGroup(String.valueOf(groupId)).getName());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return guiAgent;
	}

	public GuiAgentAccount[] getGuiAgentAccountFromAgentAccount(AgentAccount[] agents, Map<Integer, Tier> tierMap)
	{
		List<GuiAgentAccount>guiAgentAccountList = new ArrayList<GuiAgentAccount>();

		List<AgentAccount> agentList = Arrays.asList(agents);
		agentList.forEach((agent) ->{
			GuiAgentAccount guiAgent = getGuiAgentAccountFromAgentAccount(agent, tierMap);
			guiAgentAccountList.add(guiAgent);
		});
		return guiAgentAccountList.toArray(new GuiAgentAccount[guiAgentAccountList.size()]);
	}

	public GuiAgentAccount getGuiAgentAccountFromAgentAccountEx(AgentAccountEx agent)
	{
		GuiAgentAccount guiAgent = new GuiAgentAccount(agent);
		//Get Area Name and Set in GuiAgentAccount
		try {
			Integer areaID = agent.getAreaID();
			if(areaID != null)
			{
				Area area = areaService.getArea(areaID);
				guiAgent.setAreaName(area.getName());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return guiAgent;
	}

	public GuiAgentAccount[] getGuiAgentAccountFromAgentAccountEx(AgentAccountEx[] agents)
	{
		List<GuiAgentAccount>guiAgentAccountList = new ArrayList<GuiAgentAccount>();

		List<AgentAccountEx> agentList = Arrays.asList(agents);
		agentList.forEach((agent) ->{
			GuiAgentAccount guiAgent = getGuiAgentAccountFromAgentAccountEx(agent);
			guiAgentAccountList.add(guiAgent);
		});
		return guiAgentAccountList.toArray(new GuiAgentAccount[guiAgentAccountList.size()]);
	}

	/*
	 * @author martinc
	 * Adjusted code to return reversals (if any) along with the transaction.
	 */
	public GuiTransaction getGuiTransactionFromTransaction(Transaction originalTransaction,
														   TreeMap<Integer, Agent> agentCache, TreeMap<Integer, Group> groupCache, Map<Integer, Tier> tierMap, boolean traverse)
	{
		Transaction transaction = null;
		GuiTransaction guiTransaction = null;
		// deal with reversals
		switch(originalTransaction.getType())
		{
			case Transaction.TYPE_TRANSFER:
			case Transaction.TYPE_SELL:
			case Transaction.TYPE_SELF_TOPUP:
				// Find reversal for this transaction
				transaction = originalTransaction;
				guiTransaction = new GuiTransaction(transaction);

				if (!originalTransaction.isFollowUp())
				{
					try
					{
						Transaction[] reversals = tdrService.getReversalsForTransaction(String.valueOf(transaction.getId()));
						if (reversals != null && reversals.length > 0)
						{
							for (Transaction current : reversals)
							{
								guiTransaction.addReversal(getGuiTransactionFromTransaction(current, agentCache, groupCache, tierMap, false));
							}
						}
					}
					catch(Exception ex)
					{
						// We can likely ignore these errors.
						logger.error("", ex);
						transaction = originalTransaction;
						guiTransaction = new GuiTransaction(transaction);
					}
				}
				break;
			case Transaction.TYPE_REVERSE:
			case Transaction.TYPE_REVERSE_PARTIALLY:
				if (traverse)
				{
					try
					{
						transaction = tdrService.getTransaction(String.format("%011d", originalTransaction.getReversedID()), false);
						if (transaction != null)
						{
							transaction =  getGuiTransactionFromTransaction(transaction, agentCache, groupCache, tierMap, false);
						}
						else
						{
							transaction = originalTransaction;
						}
					}
					catch(Exception ex)
					{
						// original transaction was missing.
						logger.error("", ex);
						transaction = originalTransaction;
					}
				}
				else
				{
					transaction = originalTransaction;
				}
				guiTransaction = new GuiTransaction(transaction);
				break;
			default:
				transaction = originalTransaction;
				guiTransaction = new GuiTransaction(transaction);
				break;
		}

		if ( guiTransaction != null && guiTransaction.isFollowUp() )
		{
			try
			{
				WorkItem workItem = workFlowService.getWorkItemFromTransactionNumber(guiTransaction.getNumber());
				guiTransaction.setWorkflowState(workItem != null?workItem.getState():WorkItem.STATE_COMPLETED);
				Transaction adjudication = tdrService.getAdjudicationForTransaction(String.valueOf(guiTransaction.getId()));
				if (adjudication != null )
				{
					guiTransaction.setAdjudication(getGuiTransactionFromTransaction(adjudication, agentCache, groupCache, tierMap, false));
				}
				guiTransaction.setAdjudicated(adjudication != null ? true : false);
			}
			catch(Exception ex)
			{
				logger.error("", ex);
			}
		}

		if ( guiTransaction != null && originalTransaction.getBuyerTradeBonusPercentage() != null)
		{
			DecimalFormat df = new DecimalFormat("###.000000", PERCENTAGE_LOCALE);
			BigDecimal bonus = originalTransaction.getBuyerTradeBonusPercentage();
			String stringValue = df.format(bonus.multiply(new BigDecimal(100)));
			guiTransaction.setBuyerTradeBonusPercentageString((stringValue.startsWith(".")?"0":"")+stringValue);
		}

		try
		{
			if (transaction.getA_AgentID() != null)
			{
				Agent agent = agentCache.get(transaction.getA_AgentID());
				if (agent == null)
				{
					agent = agentService.getAgent(transaction.getA_AgentID());
					agentCache.put(transaction.getA_AgentID(), agent);
				}
				guiTransaction.setAPartyName(agent.getFirstName() + " " + agent.getSurname());
			}
			if (transaction.getB_AgentID() != null)
			{
				Agent agent = agentCache.get(transaction.getB_AgentID());
				if (agent == null)
				{
					agent = agentService.getAgent(transaction.getB_AgentID());
					agentCache.put(transaction.getB_AgentID(), agent);
				}
				guiTransaction.setBPartyName(agent.getFirstName() + " " + agent.getSurname());
			}
			if (transaction.getA_OwnerAgentID() != null)
			{
				Agent agent = agentCache.get(transaction.getA_OwnerAgentID());
				if (agent == null)
				{
					agent = agentService.getAgent(transaction.getA_OwnerAgentID());
					agentCache.put(transaction.getA_OwnerAgentID(), agent);
				}
				guiTransaction.setAOwnerName(agent.getFirstName() + " " + agent.getSurname());
			}
			if (transaction.getB_OwnerAgentID() != null)
			{
				Agent agent = agentCache.get(transaction.getB_OwnerAgentID());
				if (agent == null)
				{
					agent = agentService.getAgent(transaction.getB_OwnerAgentID());
					agentCache.put(transaction.getB_OwnerAgentID(), agent);
				}
				guiTransaction.setBOwnerName(agent.getFirstName() + " " + agent.getSurname());
			}
			if (transaction.getA_GroupID() != null)
			{
				Group group = groupCache.get(transaction.getA_GroupID());
				if (group == null)
				{
					group = groupService.getGroup(Integer.toString(transaction.getA_GroupID()));
					groupCache.put(transaction.getA_GroupID(), group);
				}
				guiTransaction.setAGroupName(group.getName());
			}
			if (transaction.getB_GroupID() != null)
			{
				Group group = groupCache.get(transaction.getB_GroupID());
				if (group == null)
				{
					group = groupService.getGroup(Integer.toString(transaction.getB_GroupID()));
					groupCache.put(transaction.getB_GroupID(), group);
				}
				guiTransaction.setBGroupName(group.getName());
			}
			if (transaction.getA_TierID() != null)
			{
				Tier tier = tierMap.get(transaction.getA_TierID());
				if (tier !=  null)
					guiTransaction.setATierName(tier.getName());
			}
			if (transaction.getB_TierID() != null)
			{
				Tier tier = tierMap.get(transaction.getB_TierID());
				if (tier !=  null)
					guiTransaction.setBTierName(tier.getName());
			}

			if(transaction.getA_AreaID() != null)
			{
				String aAreaID = transaction.getA_AreaID().toString();
				Area aArea = areaService.getArea(aAreaID);
				if(aArea != null)
				{
					guiTransaction.setAAreaName(aArea.getName());
					guiTransaction.setAAreaType(aArea.getType());
				}
			}
			if(transaction.getB_AreaID() != null)
			{
				String bAreaID = transaction.getB_AreaID().toString();
				Area bArea = areaService.getArea(bAreaID);
				if(bArea != null)
				{
					guiTransaction.setBAreaName(bArea.getName());
					guiTransaction.setBAreaType(bArea.getType());
				}
			}
			if(transaction.getBundleID() != null)
			{
				Bundle bundle = bundlesService.retrieve( transaction.getBundleID() );
				guiTransaction.setBundleName( bundle.getName() );
			}
			if(transaction.getPromotionID() != null)
			{
				Promotion promotion = promotionsService.retrieve( transaction.getPromotionID() );
				guiTransaction.setPromotionName( promotion.getName() );
			}
			/*if(transaction.getReturnCode() != null && transaction.getReturnCode().equals(ResponseHeader.RETURN_CODE_SUCCESS))
			{
				guiTransaction.setReversable(false);
			} else if(transaction.getReturnCode() != null && !transaction.getReturnCode().equals(ResponseHeader.RETURN_CODE_SUCCESS) && transaction.isFollowUp())
			{
				guiTransaction.setReversable(false);
			}*/
			guiTransaction.setReversable(true);
			if(transaction.getReturnCode() != null )
			{
				boolean reversable = !((!transaction.getReturnCode().equals(ResponseHeader.RETURN_CODE_SUCCESS) || (transaction.getReturnCode().equals(ResponseHeader.RETURN_CODE_SUCCESS) && transaction.isFollowUp())));
				guiTransaction.setReversable(reversable);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return guiTransaction;
	}

	public GuiTransaction getGuiTransactionFromTransactionEx(TransactionEx originalTransaction, boolean traverse)
	{
		TransactionEx transaction = null;
		GuiTransaction guiTransaction = null;
		// deal with reversals
		switch(originalTransaction.getType())
		{
			case Transaction.TYPE_TRANSFER:
			case Transaction.TYPE_SELL:
			case Transaction.TYPE_SELF_TOPUP:
				// Find reversal for this transaction
				transaction = originalTransaction;
				guiTransaction = new GuiTransaction(transaction);

				guiTransaction.setDedicatedAccountInfo(originalTransaction.getDedicatedAccountRefillInfos());
				guiTransaction.setDedicatedAccountReverseInfo(originalTransaction.getDedicatedAccountReverseInfo());
				guiTransaction.setMainAccountCurrentBalance(originalTransaction.getMainAccountCurrentBalance());
				guiTransaction.setDedicatedAccountCurrentBalanceInfo(originalTransaction.getDedicatedAccountCurrentBalanceInfos());
				guiTransaction.setDABonusReversalEnabled(originalTransaction.isDABonusReversalEnabled());

				if (traverse && !originalTransaction.isFollowUp())
				{
					try
					{
						TransactionEx[] reversals = tdrService.getReversalsForTransactionEx(String.valueOf(transaction.getId()));
						if (reversals != null && reversals.length > 0)
						{
							for (TransactionEx current : reversals)
							{
								guiTransaction.addReversal(getGuiTransactionFromTransactionEx(current, false));
							}
						}
					}
					catch(Exception ex)
					{
						// We can likely ignore these errors.
						logger.error("", ex);
						transaction = originalTransaction;
						guiTransaction = new GuiTransaction(transaction);
					}
				}
				break;
			case Transaction.TYPE_REVERSE:
			case Transaction.TYPE_REVERSE_PARTIALLY:
				if (traverse)
				{
					try
					{
						TransactionEx reversedTransaction = tdrService.getTransactionEx(String.format("%011d", originalTransaction.getReversedID()));
						if (reversedTransaction != null)
						{
							transaction = reversedTransaction;
						}
						else
						{
							transaction = originalTransaction;
						}
					}
					catch(Exception ex)
					{
						// original transaction was missing.
						logger.error("", ex);
						transaction = originalTransaction;
					}
				}
				else
				{
					transaction = originalTransaction;
				}
				guiTransaction = new GuiTransaction(transaction);
				guiTransaction.setDedicatedAccountReverseInfo(transaction.getDedicatedAccountReverseInfo());
				break;
			default:
				transaction = originalTransaction;
				guiTransaction = new GuiTransaction(transaction);
				break;
		}

		if ( guiTransaction != null && guiTransaction.isFollowUp() )
		{
			try
			{
				WorkItem workItem = workFlowService.getWorkItemFromTransactionNumber(guiTransaction.getNumber());
				guiTransaction.setWorkflowState(workItem != null?workItem.getState():WorkItem.STATE_COMPLETED);
				TransactionEx adjudication = tdrService.getAdjudicationForTransactionEx(String.valueOf(guiTransaction.getId()));
				if (adjudication != null )
					guiTransaction.setAdjudication(getGuiTransactionFromTransactionEx(adjudication, false));
				guiTransaction.setAdjudicated(adjudication != null ? true : false);
			}
			catch(Exception ex)
			{
				logger.error("", ex);
			}
		}

		if ( guiTransaction != null && originalTransaction.getBuyerTradeBonusPercentage() != null)
		{
			DecimalFormat df = new DecimalFormat("###.000000", PERCENTAGE_LOCALE);
			BigDecimal bonus = originalTransaction.getBuyerTradeBonusPercentage();
			String stringValue = df.format(bonus.multiply(new BigDecimal(100)));
			guiTransaction.setBuyerTradeBonusPercentageString((stringValue.startsWith(".")?"0":"")+stringValue);
		}

		try
		{
			if (transaction.getA_AgentID() != null)
				guiTransaction.setAPartyName(transaction.getA_FirstName() + " " + transaction.getA_Surname());
			if (transaction.getB_AgentID() != null)
				guiTransaction.setBPartyName(transaction.getB_FirstName() + " " + transaction.getB_Surname());
			if (transaction.getA_OwnerAgentID() != null)
				guiTransaction.setAOwnerName(transaction.getA_OwnerFirstName() + " " + transaction.getA_OwnerSurname());
			if (transaction.getB_OwnerAgentID() != null)
				guiTransaction.setBOwnerName(transaction.getA_OwnerFirstName() + " " + transaction.getB_OwnerSurname());
			if (transaction.getA_GroupID() != null)
				guiTransaction.setAGroupName(transaction.getA_GroupName());
			if (transaction.getB_GroupID() != null)
				guiTransaction.setBGroupName(transaction.getB_GroupName());
			if (transaction.getA_TierID() != null)
				guiTransaction.setATierName(transaction.getA_TierName());
			if (transaction.getB_TierID() != null)
				guiTransaction.setBTierName(transaction.getB_TierName());
			if (transaction.getA_AreaID() != null)
				guiTransaction.setAAreaName(transaction.getA_AreaName() );
				guiTransaction.setAAreaType(transaction.getA_AreaType() );
			if (transaction.getB_AreaID() != null)
				guiTransaction.setBAreaName(transaction.getB_AreaName() );
				guiTransaction.setBAreaType(transaction.getB_AreaType() );
			if (transaction.getA_CellGroupCode() != null)
				guiTransaction.setACellGroupCode(transaction.getA_CellGroupCode() );
			if (transaction.getB_CellGroupCode() != null)
				guiTransaction.setBCellGroupCode(transaction.getB_CellGroupCode() );
			if(transaction.getA_CellID() != null) {
				guiTransaction.setA_CellID(transaction.getA_CellID() );
				guiTransaction.setACgi(transaction.getACgi() );
			}
			if(transaction.getB_CellID() != null) {
				guiTransaction.setB_CellID(transaction.getB_CellID() );
				guiTransaction.setBCgi(transaction.getBCgi() );
			}
			if (transaction.getNonAirtimeItemDescription() != null) {
				guiTransaction.setItemDescription( transaction.getNonAirtimeItemDescription() );
			}

			if (traverse)
			{
				if (transaction.getPromotionID() != null)
				{
					Promotion promotion = promotionsService.retrieve( transaction.getPromotionID() );
					guiTransaction.setPromotionName( promotion.getName() );
				}
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return guiTransaction;
	}

	public GuiTransaction getGuiTransactionFromTransaction(Transaction transaction/*, Map<Integer, Tier> tierMap*/) throws Exception
	{
		TreeMap<Integer, Agent> agentCache = new TreeMap<Integer, Agent>();
		TreeMap<Integer, Group> groupCache = new TreeMap<Integer, Group>();
		Map<Integer, Tier> tierMap = tierService.tierMap();
		GuiTransaction guiTransaction = getGuiTransactionFromTransaction(transaction, agentCache, groupCache, tierMap, false);
		return guiTransaction;
	}

	public GuiTransaction getGuiTransactionFromTransaction(Transaction transaction/*, Map<Integer, Tier> tierMap*/, boolean traverse) throws Exception
	{
		TreeMap<Integer, Agent> agentCache = new TreeMap<Integer, Agent>();
		TreeMap<Integer, Group> groupCache = new TreeMap<Integer, Group>();
		Map<Integer, Tier> tierMap = tierService.tierMap();
		GuiTransaction guiTransaction = getGuiTransactionFromTransaction(transaction, agentCache, groupCache, tierMap, traverse);
		return guiTransaction;
	}


	public GuiTransaction[] getGuiTransactionFromTransaction(Transaction[] transactions/*, Map<Integer, Tier> tierMap*/) throws Exception
	{
		List<GuiTransaction>guiTransactionList = new ArrayList<GuiTransaction>();

		List<Transaction> transactionList = Arrays.asList(transactions);

		TreeMap<Integer, Agent> agentCache = new TreeMap<Integer, Agent>();
		TreeMap<Integer, Group> groupCache = new TreeMap<Integer, Group>();
		Map<Integer, Tier> tierMap = tierService.tierMap();
		transactionList.forEach((transaction) ->{
			GuiTransaction guiTransaction= getGuiTransactionFromTransaction(transaction, agentCache, groupCache, tierMap, false);
			// Convert To GuiTier Type using String type from Tier
			//guiAgent.setTierType( TierType.getTierType(tierMap.get(agent.getTierID()).getType()) );
			guiTransactionList.add(guiTransaction);
		});
		return guiTransactionList.toArray(new GuiTransaction[guiTransactionList.size()]);
	}

	public GuiTransaction[] getGuiTransactionFromTransactionEx(TransactionEx[] transactions) throws Exception
	{
		List<GuiTransaction>guiTransactionList = new ArrayList<GuiTransaction>();

		List<TransactionEx> transactionList = Arrays.asList(transactions);

		transactionList.forEach((transaction) ->{
			GuiTransaction guiTransaction= getGuiTransactionFromTransactionEx(transaction, false);
			// Convert To GuiTier Type using String type from Tier
			//guiAgent.setTierType( TierType.getTierType(tierMap.get(agent.getTierID()).getType()) );
			guiTransactionList.add(guiTransaction);
		});
		return guiTransactionList.toArray(new GuiTransaction[guiTransactionList.size()]);
	}

	/////////////////////////////

	public GuiAuditEntry getGuiAuditEntryFromAuditEntry(AuditEntry entry, TreeMap<Integer, WebUser> wuserCache, TreeMap<Integer, Agent> agentCache)
	{
		GuiAuditEntry guiAuditEntry = new GuiAuditEntry(entry);

		try
		{
			if ( entry.getUserID() > 0 )
			{
				WebUser wuser = wuserCache.get(entry.getUserID());
				if (wuser == null)
				{
					wuser = webUserService.getWebUser(Integer.toString(entry.getUserID()));
					wuserCache.put(entry.getUserID(), wuser);
				}
				guiAuditEntry.setUserName(wuser.getFirstName() + " " + wuser.getSurname());
				guiAuditEntry.setUserType(GuiAuditEntry.UserTypeEnum.WEBUSER);
			}
			else if ( entry.getUserID() < 0 )
			{
				Agent agent = agentCache.get(Math.abs(entry.getUserID()));
				if (agent == null)
				{
					agent = agentService.getAgent(Math.abs(entry.getUserID()));
					agentCache.put(Math.abs(entry.getUserID()), agent);
				}
				guiAuditEntry.setUserName(agent.getFirstName() + " " + agent.getSurname());
				guiAuditEntry.setUserType(GuiAuditEntry.UserTypeEnum.AGENT);
			}
			else
			{
				guiAuditEntry.setUserName("(not applicable)");
				guiAuditEntry.setUserType(GuiAuditEntry.UserTypeEnum.NONE);
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return guiAuditEntry;
	}

	public GuiAuditEntry getGuiAuditEntryFromAuditEntry(AuditEntry entry)
	{
		TreeMap<Integer, Agent> agentCache = new TreeMap<Integer, Agent>();
		TreeMap<Integer, WebUser> wuserCache = new TreeMap<Integer, WebUser>();
		GuiAuditEntry guiAuditEntry = getGuiAuditEntryFromAuditEntry(entry, wuserCache, agentCache);
		return guiAuditEntry;
	}


	public GuiAuditEntry[] getGuiAuditEntryFromAuditEntry(AuditEntry[] entries)
	{
		List<GuiAuditEntry>guiAuditEntryList = new ArrayList<GuiAuditEntry>();

		List<AuditEntry> entryList = Arrays.asList(entries);

		TreeMap<Integer, Agent> agentCache = new TreeMap<Integer, Agent>();
		TreeMap<Integer, WebUser> wuserCache = new TreeMap<Integer, WebUser>();
		entryList.forEach((entry) ->{
			GuiAuditEntry guiAuditEntry = getGuiAuditEntryFromAuditEntry(entry, wuserCache, agentCache);
			guiAuditEntryList.add(guiAuditEntry);
		});
		return guiAuditEntryList.toArray(new GuiAuditEntry[guiAuditEntryList.size()]);
	}

	/////////////////////////////

	public GuiBatch getGuiBatchFromBatch(Batch batch, TreeMap<Integer, WebUser> wuserCache)
	{
		GuiBatch guiBatch = new GuiBatch(batch);

		try
		{
			{
				WebUser wuser = wuserCache.get(batch.getWebUserID());
				if (wuser == null)
				{
					wuser = webUserService.getWebUser(Integer.toString(batch.getWebUserID()));
					wuserCache.put(batch.getWebUserID(), wuser);
				}
				guiBatch.setWebUserName(wuser.getFirstName() + " " + wuser.getSurname());
			}
			if ( batch.getCoAuthWebUserID() != null )
			{
				WebUser wuser = wuserCache.get(batch.getCoAuthWebUserID());
				if (wuser == null)
				{
					wuser = webUserService.getWebUser(Integer.toString(batch.getCoAuthWebUserID()));
					wuserCache.put(batch.getCoAuthWebUserID(), wuser);
				}
				guiBatch.setCoAuthWebUserName(wuser.getFirstName() + " " + wuser.getSurname());
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return guiBatch;
	}

	public GuiBatch getGuiBatchFromBatch(Batch batch)
	{
		TreeMap<Integer, WebUser> wuserCache = new TreeMap<Integer, WebUser>();
		GuiBatch guiBatch = getGuiBatchFromBatch(batch, wuserCache);
		return guiBatch;
	}


	public GuiBatch[] getGuiBatchFromBatch(Batch[] batches)
	{
		List<GuiBatch>guiBatchList = new ArrayList<GuiBatch>();

		List<Batch> batchList = Arrays.asList(batches);

		TreeMap<Integer, WebUser> wuserCache = new TreeMap<Integer, WebUser>();
		batchList.forEach((batch) ->{
			GuiBatch guiBatch = getGuiBatchFromBatch(batch, wuserCache);
			guiBatchList.add(guiBatch);
		});
		return guiBatchList.toArray(new GuiBatch[guiBatchList.size()]);
	}

	/////////////////////////////

	public ReplenishRequest getReplenishRequestFromGuiReplenishRequest(GuiReplenishRequest request)
	{
		ReplenishRequest replenish = new ReplenishRequest();
		BeanUtils.copyProperties(request, replenish);
		return replenish;
	}

	public TransferRequest getTransferRequestFromGuiTransferRequest(GuiTransferRequest request)
	{
		TransferRequest transfer = new TransferRequest();
		BeanUtils.copyProperties(request, transfer);
		return transfer;
	}

	public List<GuiViolation> convertToGuiViolation(List<Violation> violations, String violationId)
	{
		Map<String, GuiViolation> volationMap = new HashMap<>();
		for(Violation violation : violations)
		{
			String field = violation.getProperty();
			if (!volationMap.containsKey(field)) {
				volationMap.put(field, new GuiViolation(field, violationId));
			}
			volationMap.get(field).extractViolation(violation);
		}
		return new ArrayList<GuiViolation>(volationMap.values());
	}

	public AdjustmentRequest getAdjustmentRequestFromGuiAdjustmentRequest(GuiAdjustmentRequest request)
	{
		AdjustmentRequest adjustment = new AdjustmentRequest();
		BeanUtils.copyProperties(request, adjustment);
		return adjustment;
	}

	public GuiWebUser [] convertWebUsersArrayoGuiWebUserArray(WebUser [] wusers, Map<Integer, Department> departmentMap)
	{
		if (wusers == null)
			return null;

		GuiWebUser [] result = new GuiWebUser[wusers.length];

		for(int i=0; i<wusers.length; i++)
		{

			result[i] = new GuiWebUser(wusers[i], (departmentMap != null)? departmentMap.get(wusers[i].getDepartmentID()).getName() : null);
		}
		return result;
	}

	public GuiAgentUser convertAgentUserToGuiAgentUser(AgentUser auser, Map<Integer, Agent> agentMap, Map<Integer, Role> roleMap) throws Exception
	{
		Agent agent = null;
		int agentId = auser.getAgentID();
		if (!agentMap.containsKey(agentId))
		{
			agent = agentService.getAgent( String.valueOf(agentId) );
			agentMap.put(agentId, agent);
		}
		if (agent == null || agent.getId() != agentId)
		{
			agent = agentMap.get(agentId);
		}

		GuiAgentUser guAgent = new GuiAgentUser(auser);
		guAgent.updateAvailableChannels(agent);

		Role role = null;
		int roleId = auser.getRoleID();
		if (!roleMap.containsKey(roleId))
		{
			role = roleService.getRole( roleId );
			roleMap.put(roleId, role);
		}
		if (role == null || role.getId() != roleId)
		{
			role = roleMap.get(roleId);
		}

		guAgent.setRole( role );

		return guAgent;
	}

	public GuiAgentUser [] convertAgentUserToGuiAgentUser(AgentUser [] ausers) throws Exception
	{
		if (ausers == null)
			return null;

		Map<Integer, Agent> agentMap = new HashMap<>();
		Map<Integer, Role> roleMap = new HashMap<>();

		GuiAgentUser [] result = new GuiAgentUser[ausers.length];

		for(int i=0; i<ausers.length; i++)
		{
			result[i] = convertAgentUserToGuiAgentUser(ausers[i], agentMap, roleMap);
		}
		return result;
	}

	public PartialReversalRequest getPartialReversalRequestFromGuiReversal(GuiReversalRequest reversal)
	{
		PartialReversalRequest request = new PartialReversalRequest();
		BeanUtils.copyProperties(reversal, request);
		return request;
	}

	public Object getPartialReversalRequestWithCoAuthFromGuiReversal(GuiReversalCoAuthRequest reversal) {
		PartialReversalRequestWithCoAuth request = new PartialReversalRequestWithCoAuth();
		BeanUtils.copyProperties(reversal, request);
		return request;
	}

	public AdjudicateRequest getAdjudicateRequestFromGuiAdjudicateRequest(GuiAdjudicateRequest adjudicate)
	{
		AdjudicateRequest request = new AdjudicateRequest();
		BeanUtils.copyProperties(adjudicate, request);
		return request;
	}

	public ReversalRequest getReversalRequestFromGuiReversal(GuiReversalRequest reversal)
	{
		ReversalRequest request = new ReversalRequest();
		BeanUtils.copyProperties(reversal, request);
		return request;
	}

	public Object getReversalRequestWithCoAuthFromGuiReversal(GuiReversalCoAuthRequest reversal) {
		ReversalRequestWithCoAuth request = new ReversalRequestWithCoAuth();
		BeanUtils.copyProperties(reversal, request);
		return request;
	}

	public GuiWorkItem[] getGuiWorkItemFromWorkItem(WorkItem[] workItems)
	{


		List<GuiWorkItem> workItemList = new ArrayList<GuiWorkItem>();
		Arrays.asList(workItems).forEach((item) ->{
			GuiWorkItem guiWorkItem = getGuiWorkItemFromWorkItem(item);
			workItemList.add(guiWorkItem);
		});
		return workItemList.toArray(new GuiWorkItem[workItemList.size()]);
	}

	private void addName(GuiWorkItem item, String forename, String surname)
	{
		StringBuilder name = new StringBuilder();
		if (forename != null)
		{
			name.append(forename);
		}
		name.append(" ");
		if (surname != null)
		{
			name.append(surname);
		}
		item.setCreatedByName(name.toString().trim());
	}

	public GuiWorkItem getGuiWorkItemFromWorkItem(WorkItem workItem)
	{
		Map<Integer, Agent>agentMap = new HashMap<Integer, Agent>();
		Map<Integer, GuiWebUser>webuserMap = new HashMap<Integer, GuiWebUser>();
		return getGuiWorkItemFromWorkItem(workItem, agentMap, webuserMap);
	}

	public GuiWorkItem getGuiWorkItemFromWorkItem(WorkItem workItem, Map<Integer, Agent>agentMap, Map<Integer, GuiWebUser>webuserMap)
	{
		GuiWorkItem guiWorkItem = new GuiWorkItem();
		BeanUtils.copyProperties(workItem, guiWorkItem);
		String workflowRequest = guiWorkItem.getRequest();
		if (workflowRequest != null)
		{
			try
			{
				GuiWorkflowRequest guiRequest = mapper.readValue(workflowRequest, GuiWorkflowRequest.class);
				WorkflowRequestType requestType = guiRequest.getRequestType();
				String workItemType = workItem.getType();
				if (requestType == null && workItemType.equals(WorkItem.TYPE_EXECUTE))
				{
					requestType = WorkflowRequestType.WORKFLOWREQUEST;
				}
				guiWorkItem.setRequestType(requestType);
			}
			catch(Exception ex){}
		}
		String url = workItem.getUri();
		if (url != null && url.equals("transaction/batchupload"))
		{
			String request = workItem.getRequest();
			if (request != null)
			{
				try
				{
					GuiWorkflowRequest currentRequest = mapper.readValue(request, GuiWorkflowRequest.class);
					//GuiBatchUploadRequest batchRequest = mapper.readValue(request, GuiBatchUploadRequest.class);
					if (currentRequest != null)
					{
						GuiBatchUploadRequest batchRequest = currentRequest.getBatchUpload();
						guiWorkItem.setBatchID(batchRequest.getBatchID());
					}
				}
				catch(Exception ex)
				{
					// Nothing todo here
				}
			}
		}
		if (guiWorkItem.getCreatedByWebUserID() == null)
		{
			try
			{
				Agent item =  null;
				if (agentMap.containsKey(guiWorkItem.getCreatedByAgentID()))
				{
					item = agentMap.get(guiWorkItem.getCreatedByAgentID());
				}
				else
				{
					item = agentService.getAgent(guiWorkItem.getCreatedByAgentID());
				}

				addName(guiWorkItem, item.getFirstName(), item.getSurname());
			}
			catch(Exception ex){}
		}
		else
		{
			try
			{
				GuiWebUser item = null;
				if (webuserMap.containsKey(guiWorkItem.getCreatedByWebUserID()))
				{
					item = webuserMap.get(guiWorkItem.getCreatedByWebUserID());
				}
				else
				{
					item = webUserService.getGuiWebUser(String.valueOf(guiWorkItem.getCreatedByWebUserID()));
				}
				addName(guiWorkItem, item.getFirstName(), item.getSurname());
			}
			catch(Exception ex){}
		}
		return guiWorkItem;
	}

	public WorkItem getWorkItemFromGuiWorkItem(GuiWorkItem guiWorkItem)
	{
		WorkItem workItem = new WorkItem();
		BeanUtils.copyProperties(guiWorkItem, workItem);
		return workItem;
	}

	public void convertTransactionIdsToNames(GuiTransaction convertedTransaction)
	{
		try
		{
			if (convertedTransaction.getTransferRuleID() != null)
			{
				TransferRule rule = transferRuleService.getRule(String.valueOf(convertedTransaction.getTransferRuleID()));
				if (rule != null)
				{
					convertedTransaction.setTransferRuleName(rule.getName());
				}
			}
		}
		catch(Exception ex){}
		try
		{
			if (convertedTransaction.getA_ServiceClassID() != null)
			{
				ServiceClass clazz = serviceClassService.getServiceClass(convertedTransaction.getA_ServiceClassID());
				if (clazz != null)
				{
					convertedTransaction.setAServiceClassName(clazz.getName());
				}
			}
		}
		catch(Exception ex){}
		try
		{
			if (convertedTransaction.getB_ServiceClassID() != null)
			{
				ServiceClass clazz = serviceClassService.getServiceClass(convertedTransaction.getB_ServiceClassID());
				if (clazz != null)
				{
					convertedTransaction.setBServiceClassName(clazz.getName());
				}
			}
		}
		catch(Exception ex){}
	}

	public GuiCell[] getGuiCellFromCell(Cell[] cells)
	{
		List<GuiCell> cellList = new ArrayList<GuiCell>();
		Arrays.asList(cells).forEach((cell) ->{
			GuiCell currentCell = getGuiCellFromCell(cell);
			cellList.add(currentCell);
		});
		return cellList.toArray(new GuiCell[cellList.size()]);
	}


	public GuiCell getGuiCellFromCell(Cell cell)
	{
		GuiCell currentCell = new GuiCell(cell);
		try
		{
			//Get list of Areas that cell belongs to.
			//Build up an array of areas.
			//List<GuiAreas> areaList = areaService.getArea(currentCell.getID);
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return currentCell;
	}

	public Cell getCellFromGuiCell(GuiCell newCell)
	{
		Cell cell = new Cell();
		BeanUtils.copyProperties(newCell, cell);

		//Populate the areas list of hxc.ecds.protocol.rest.Cell
		ArrayList<Area> areas = new ArrayList<Area>();
		try
		{
			List<GuiArea> guiAreas = newCell.getAreas();
			if(guiAreas != null)
			{
				for(GuiArea guiArea : guiAreas)
				{
					Area area = areaService.getArea(guiArea.getId());
					areas.add(area);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		cell.setAreas(areas);

		//Populate the cell groups list of hxc.ecds.protocol.rest.Cell
		ArrayList<CellGroup> cellGroups = new ArrayList<CellGroup>();
		try
		{
			List<GuiCellGroup> guiCellGroups = newCell.getCellGroups();
			if(guiCellGroups != null)
			{
				for(GuiCellGroup guiCellGroup : guiCellGroups)
				{
					CellGroup cellGroup = cellGroupService.getCellGroup(guiCellGroup.getId());
					cellGroups.add(cellGroup);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		cell.setCellGroups(cellGroups);

		String jsonInString;
		try
		{
			jsonInString = mapper.writeValueAsString(cell);
			logger.info(jsonInString);
		}
		catch (JsonProcessingException e)
		{
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		return cell;
	}

	public GuiPromotion [] getGuiPromotionsFromPromotions(Promotion []  promotions) throws Exception
	{
		List<GuiPromotion> guiPromos = new ArrayList<>();

		// Load Transfer Rules, Areas, Service Classes & Bundles
		Map<Integer, String> transferRuleNames = transferRuleService.getTransferRuleNameMap(Optional.empty(), Optional.empty());
		Map<Integer, Area> areaMap = areaService.getAreaMap();
		Map<Integer, String> serviceClassNames = serviceClassService.getServiceClassNameMap(Optional.empty(), Optional.empty());
		Map<Integer, String> bundleNames = bundlesService.getDropDownMap(Optional.empty(), Optional.empty());

		Arrays.asList(promotions).forEach((promo) ->{
			GuiPromotion gpromo = new GuiPromotion(promo);

			if (gpromo.getTransferRuleID() != null) gpromo.setTransferRuleName( transferRuleNames.get(gpromo.getTransferRuleID()) );
			if (gpromo.getAreaID() != null) gpromo.setAreaName( areaMap.get(gpromo.getAreaID()).getName() );
			if (gpromo.getServiceClassID() != null) gpromo.setServiceClassName( serviceClassNames.get(gpromo.getServiceClassID()) );
			if (gpromo.getBundleID() != null) gpromo.setBundleName( bundleNames.get(gpromo.getBundleID()) );

			guiPromos.add(gpromo);
		});

		return guiPromos.toArray(new GuiPromotion[guiPromos.size()]);
	}

	public GuiPromotionDetailed getGuiDetailedPromotionsFromPromotion(Promotion promotion) throws Exception
	{
		GuiPromotionDetailed gpromo = new GuiPromotionDetailed(promotion);

		if (promotion.getTransferRuleID() != null)
		{
			TransferRule tr = transferRuleService.getRule( String.valueOf(promotion.getTransferRuleID()) );
			gpromo.setTransferRule(tr);
			gpromo.setTransferRuleName((tr !=null)? tr.getName() : "");
		}

		if (promotion.getAreaID() != null)
		{
			Area area = areaService.getArea(promotion.getAreaID());
			gpromo.setArea(area);
			gpromo.setAreaName((area !=null)? area.getName() : "");
		}

		if (promotion.getServiceClassID() != null)
		{
			ServiceClass sc = serviceClassService.getServiceClass(promotion.getServiceClassID());
			gpromo.setServiceClass(sc);
			gpromo.setServiceClassName((sc !=null)? sc.getName() : "");
		}

		if (promotion.getBundleID() != null)
		{
			Bundle bun = bundlesService.retrieve(promotion.getBundleID());
			gpromo.setBundle(bun);
			gpromo.setBundleName((bun !=null)? bun.getName() : "");
		}

		return gpromo;
	}

	public GuiBundle [] getGuiBundlesFromBundles(Bundle [] tsBundles, BundleInfo[] pccBundleInfo) throws Exception
	{
		Map<String, GuiBundle> bundleMap = new TreeMap<>();

		for(Bundle tsb : tsBundles)
		{
			GuiBundle gbundle = new GuiBundle(tsb);
			bundleMap.put(gbundle.getTag(), gbundle);
		}

		for(BundleInfo bi : pccBundleInfo)
		{
			if (!bundleMap.containsKey(bi.getTag())) {
				GuiBundle gbundle = new GuiBundle(bi);
				bundleMap.put(bi.getTag(), gbundle);
			} else {
				bundleMap.get(bi.getTag()).updateConfirmedState();
			}
		}

		List<GuiBundle> list = bundleMap.values().stream().collect(Collectors.toList());
		list.sort(Comparator.comparing(GuiBundle::getBundleState));

		return bundleMap.values().toArray( new GuiBundle[bundleMap.size()] );
	}

	public BatchUploadRequest getBatchUploadRequestFromGuiBatchUploadRequest(GuiBatchUploadRequest guiUploadRequest)
	{
		BatchUploadRequest uploadRequest = new BatchUploadRequest();
		BeanUtils.copyProperties(guiUploadRequest, uploadRequest);
		return uploadRequest;
	}

	public GuiArea[] getGuiAreaFromArea(Area[] areas)
	{
		Map<Integer, Area> areaMap = new HashMap<Integer, Area>();
		List<GuiArea> areaList = new ArrayList<GuiArea>();
		Arrays.asList(areas).forEach((area) ->{
			GuiArea guiArea = getGuiAreaFromArea(area, areaMap);
			areaList.add(guiArea);
		});
		return areaList.toArray(new GuiArea[areaList.size()]);
	}

	public Area getAreaFromGuiArea(GuiArea guiArea)
	{
		Area area = new Area();
		BeanUtils.copyProperties(guiArea, area);
		return area;
	}

	public GuiArea getGuiAreaFromArea(Area area, Map<Integer, Area> areaMap)
	{
		GuiArea guiArea = new GuiArea();
		//BeanUtils.copyProperties(group, guiGroup, Common.getNullPropertyNames(group));
		BeanUtils.copyProperties(area, guiArea);
		Area parentArea;
		try
		{
			Integer key = area.getParentAreaID();
			if (areaMap != null && areaMap.containsKey(key))
			{
				parentArea = areaMap.get(key);
			}
			else
			{
				parentArea = areaService.getArea(key);
				if (areaMap != null)
					areaMap.put(parentArea.getId(), parentArea);
			}
			guiArea.setParentAreaName(parentArea.getName());
		}
		catch (Exception e)
		{
			guiArea.setParentAreaName("");
		}

		return guiArea;
	}
}
