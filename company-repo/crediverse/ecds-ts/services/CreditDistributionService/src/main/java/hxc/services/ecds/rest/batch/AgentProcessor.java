package hxc.services.ecds.rest.batch;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hxc.ecds.protocol.rest.BatchIssue;
import hxc.ecds.protocol.rest.IAuthenticatable;
import hxc.ecds.protocol.rest.config.AgentsConfig;
import hxc.services.ecds.CompanyInfo;
import hxc.services.ecds.model.Account;
import hxc.services.ecds.model.Agent;
import hxc.services.ecds.model.Area;
import hxc.services.ecds.model.AuditEntry;
import hxc.services.ecds.model.Group;
import hxc.services.ecds.model.Role;
import hxc.services.ecds.model.ServiceClass;
import hxc.services.ecds.model.Stage;
import hxc.services.ecds.model.Tier;
import hxc.services.ecds.model.Transaction;
import hxc.services.ecds.model.WorkItem;
import hxc.services.ecds.rest.Agents;
import hxc.services.ecds.rest.ICreditDistribution;
import hxc.services.ecds.util.AuthenticationHelper;

public class AgentProcessor extends Processor<Agent>
{
	final static Logger logger = LoggerFactory.getLogger(AgentProcessor.class);

	private static final SimpleDateFormat defaultTimeFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	public static final Pattern TWO_TO_FIVE_DIGITS = Pattern.compile("\\d{2,5}");
	public static final Pattern PIN_PATTERN = Pattern.compile("pin:(\\d+)");

	public static String[] HEADINGS = new String[] { //
			"temp_pin", //
			"max_daily_amount", //
			"max_monthly_amount", //
			"max_amount", //
			"warning_threshold", //
			"activation_date", //
			"deactivation_date", //
			"expiration_date", //
			"dob", //
			"surname", //
			"id", //
			"max_daily_count", //
			"max_monthly_count", //
			"max_report_count", //
			"max_report_daily_schedule_count", //
			"channels", //
			"first_name", //
			"status", //
			"tier", //
			"gender", //
			"area_name", //
			"area_type", //
			"group", //
			"service_class", //
			"supplier", //
			"owner", //
			"account_number", //
			"alt_phone", //
			"email", //
			"domain_account", //
			"imei", //
			"imsi", //
			"initials", //
			"language", //
			"msisdn", //
			"postal_city", //
			"postal_line1", //
			"postal_line2", //
			"postal_suburb", //
			"postal_zip", //
			"street_city", //
			"street_line1", //
			"street_line2", //
			"street_suburb", //
			"street_zip", //
			"title", //
			"role", //
			"ussd_confirmation", //
			"send_daily_bundle_commission_report", //
			/*
			 * Functionality on hold MSISDN-RECYCLING - uncomment when re-instating
			 */
			//"msisdn_recycled", //
	};

	private AgentsConfig config = null;

	public AgentProcessor(ICreditDistribution context, boolean mayInsert, boolean mayUpdate, boolean mayDelete)
	{
		super(context, mayInsert, mayUpdate, mayDelete);
	}

	@Override
	protected String getProperty(String heading, boolean lastColumn)
	{
		switch (heading)
		{
			case "temp_pin":
				return "temporaryPin";

			case "max_daily_amount":
				return "maxDailyAmount";

			case "max_monthly_amount":
				return "maxMonthlyAmount";

			case "max_amount":
				return "maxTransactionAmount";

			case "warning_threshold":
				return "warningThreshold";

			case "activation_date":
				return "activationDate";

			case "deactivation_date":
				return "deactivationDate";

			case "expiration_date":
				return "expirationDate";

			case "dob":
				return "dateOfBirth";

			case "surname":
				return "surname";

			case "id":
				return "id";

			case "max_daily_count":
				return "maxDailyCount";

			case "max_monthly_count":
				return "maxMonthlyCount";

			case "max_report_count":
				return "reportCountLimit";

			case "max_report_daily_schedule_count":
				return "reportDailyScheduleLimit";

			case "channels":
				return "allowedChannels";

			case "first_name":
				return "firstName";

			case "status":
				return "state";

			case "tier":
				return "tierID";

			case "gender":
				return "gender";

			case "area_name":
				return "areaName";

			case "area_type":
				return "areaType";

			case "group":
				return "groupID";

			case "service_class":
				return "serviceClassID";

			case "role":
				return "roleID";

			case "supplier":
				return "supplierAgentID";

			case "owner":
				return "ownerAgentID";

			case "account_number":
				return "accountNumber";

			case "alt_phone":
				return "altPhoneNumber";

			case "email":
				return "email";

			case "domain_account":
				return "domainAccountName";

			case "imei":
				return "imei";

			case "imsi":
				return "imsi";

			case "initials":
				return "initials";

			case "language":
				return "language";

			case "msisdn":
				return "mobileNumber";

			case "postal_city":
				return "postalAddressCity";

			case "postal_line1":
				return "postalAddressLine1";

			case "postal_line2":
				return "postalAddressLine2";

			case "postal_suburb":
				return "postalAddressSuburb";

			case "postal_zip":
				return "postalAddressZip";

			case "street_city":
				return "streetAddressCity";

			case "street_line1":
				return "streetAddressLine1";

			case "street_line2":
				return "streetAddressLine2";

			case "street_suburb":
				return "streetAddressSuburb";

			case "street_zip":
				return "streetAddressZip";

			case "title":
				return "title";
				
			case "ussd_confirmation":
				return "confirmUssd";
			case "send_daily_bundle_commission_report":
				return "sendBundleCommissionReport";

			/*
			 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
			 */
			/*case "msisdn_recycled":
				return "msisdnRecycled";*/

			default:
				return null;
		}

	}

	@Override
	protected String getAuditType()
	{
		return AuditEntry.TYPE_AGENT;
	}

	@Override
	protected Agent instantiate(EntityManager em, State state, Agent from)
	{
		Agent result = new Agent();

		// Amend From
		if (from != null)
			result.amend(from, false);

		// Else Load Default Role
		else
		{
			result.setRole(Role.findAgentAll(em, state.getCompanyID()));
			result.setRoleID(result.getRole().getId());
		}

		return result;
	}

	@Override
	protected void amend(EntityManager em, State state, Agent agent, String[] rowValues, List<Object> other)
	{
		String areaName = null, areaType = null;
		for (int index = 0; index < rowValues.length && index < headings.length; index++)
		{
			String property = propertyMap.get(index);
			if (property == null)
				continue;
			String value = rowValues[index];
			String heading = headings[index];
			switch (property)
			{
				case "temporaryPin":
					// Workaround to use temp_pin value (which is boolean by design)
					// for setting pin. This happens if temp_pin consist of 2 to 5 digits.
					if (TWO_TO_FIVE_DIGITS.matcher(value).matches()) {
						other.add("pin:" + value);
					}
					// agent.setTemporaryPin(state.parseBoolean(heading, value));
					// Cannot change
					break;

				case "maxDailyAmount":
					agent.setMaxDailyAmount(state.parseBigDecimal(heading, value));
					break;

				case "maxMonthlyAmount":
					agent.setMaxMonthlyAmount(state.parseBigDecimal(heading, value));
					break;

				case "maxTransactionAmount":
					agent.setMaxTransactionAmount(state.parseBigDecimal(heading, value));
					break;

				case "warningThreshold":
					agent.setWarningThreshold(state.parseBigDecimal(heading, value));
					break;

				case "activationDate":
					if (value == null) {
						value = defaultTimeFormat.format(new Date());
					}
					agent.setActivationDate(state.parseDate(heading, value));
					break;

				case "deactivationDate":
					agent.setDeactivationDate(state.parseDate(heading, value));
					break;

				case "expirationDate":
					agent.setExpirationDate(state.parseDate(heading, value));
					break;

				case "dateOfBirth":
					agent.setDateOfBirth(state.parseDate(heading, value));
					break;

				case "surname":
					agent.setSurname(value);
					break;

				case "id":
					agent.setId(state.parseInt(heading, value, 0));
					break;

				case "maxDailyCount":
					agent.setMaxDailyCount(state.parseInteger(heading, value));
					break;

				case "maxMonthlyCount":
					agent.setMaxMonthlyCount(state.parseInteger(heading, value));
					break;

				case "reportCountLimit":
					agent.setReportCountLimit(state.parseInteger(heading, value));
					break;

				case "reportDailyScheduleLimit":
					agent.setReportDailyScheduleLimit(state.parseInteger(heading, value));
					break;

				case "allowedChannels":
					agent.setAllowedChannels(state.parseInteger(heading, value));
					break;

				case "firstName":
					agent.setFirstName(value);
					break;

				case "state":
					agent.setState(value);
					break;

				case "tierID":
					if (value == null || value.isEmpty())
					{
						agent.setTier(null);
						agent.setTierID(0);
					}
					else
					{
						Tier tier = Tier.findByName(em, state.getCompanyID(), value);
						if (tier == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "tierID", null, value + " is not a valid Tier");
						else
						{
							agent.setTier(tier);
							agent.setTierID(tier.getId());
						}
					}
					break;

				case "gender":
					agent.setGender(value);
					break;

				case "areaName":
					areaName = ((value != null) && value.isEmpty()) ? null : value;
					break;

				case "areaType":
					areaType = ((value != null) && value.isEmpty()) ? null : value;
					break;

				case "groupID":
					if (value == null || value.isEmpty())
					{
						agent.setGroup(null);
						agent.setGroupID(null);
					}
					else
					{
						Group group = Group.findByName(em, state.getCompanyID(), value);
						if (group == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "groupID", null, value + " is not a valid Group");
						else
						{
							agent.setGroup(group);
							agent.setGroupID(group.getId());
						}
					}
					break;

				case "serviceClassID":
					if (value == null || value.isEmpty())
					{
						agent.setServiceClass(null);
						agent.setServiceClass(null);
					}
					else
					{
						ServiceClass serviceClass = ServiceClass.findByName(em, state.getCompanyID(), value);
						if (serviceClass == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "serviceClassID", null, value + " is not a valid ServiceClass");
						else
						{
							agent.setServiceClass(serviceClass);
							agent.setServiceClassID(serviceClass.getId());
						}
					}
					break;

				case "roleID":
				{
					Role role = Role.findByName(em, state.getCompanyID(), value);
					if (role == null || !Role.TYPE_AGENT.equals(role.getType()))
						state.addIssue(BatchIssue.INVALID_VALUE, "roleID", null, value + " is not a valid Role");
					else
					{
						agent.setRole(role);
						agent.setRoleID(role.getId());
					}
				}
					break;

				case "supplierAgentID":
					if (value == null || value.isEmpty())
					{
						agent.setSupplier(null);
						agent.setSupplierAgentID(null);
					}
					else
					{
						String msisdn = context.toMSISDN(value);
						Agent supplier = Agent.findByMSISDN(em, msisdn, state.getCompanyID());
						if (supplier == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "supplierAgentID", null, value + " is not a valid Agent");
						else
						{
							agent.setSupplier(supplier);
							agent.setSupplierAgentID(supplier.getId());
						}
					}
					break;

				case "ownerAgentID":
					if (value == null || value.isEmpty())
					{
						agent.setOwner(null);
						agent.setOwnerAgentID(null);
					}
					else
					{
						String msisdn = context.toMSISDN(value);
						Agent owner = Agent.findByMSISDN(em, msisdn, state.getCompanyID());
						if (owner == null)
							state.addIssue(BatchIssue.INVALID_VALUE, "ownerAgentID", null, value + " is not a valid Agent");
						else
						{
							agent.setOwner(owner);
							agent.setOwnerAgentID(owner.getId());
						}
					}
					break;

				case "accountNumber":
					agent.setAccountNumber(value);
					break;

				case "altPhoneNumber":
					agent.setAltPhoneNumber(value);
					break;

				case "email":
					agent.setEmail(value);
					break;

				case "domainAccountName":
					if(value == null)
					{
						agent.setAuthenticationMethod(IAuthenticatable.AUTHENTICATE_PIN_2FACTOR);
					} else {
						agent.setAuthenticationMethod(IAuthenticatable.AUTHENTICATE_EXTERNAL_2FACTOR);
					}
					agent.setDomainAccountName(value);
					break;

				case "imei":
					agent.setImei(value);
					break;

				case "imsi":
					agent.setImsi(value);
					break;

				case "initials":
					agent.setInitials(value);
					break;

				case "language":
					agent.setLanguage(toLowercase(value));
					break;

				case "mobileNumber":
					agent.setMobileNumber(context.toMSISDN(value));
					break;

				case "postalAddressCity":
					agent.setPostalAddressCity(value);
					break;

				case "postalAddressLine1":
					agent.setPostalAddressLine1(value);
					break;

				case "postalAddressLine2":
					agent.setPostalAddressLine2(value);
					break;

				case "postalAddressSuburb":
					agent.setPostalAddressSuburb(value);
					break;

				case "postalAddressZip":
					agent.setPostalAddressZip(value);
					break;

				case "streetAddressCity":
					agent.setStreetAddressCity(value);
					break;

				case "streetAddressLine1":
					agent.setStreetAddressLine1(value);
					break;

				case "streetAddressLine2":
					agent.setStreetAddressLine2(value);
					break;

				case "streetAddressSuburb":
					agent.setStreetAddressSuburb(value);
					break;

				case "streetAddressZip":
					agent.setStreetAddressZip(value);
					break;

				case "title":
					agent.setTitle(value);
					break;
					
				case "confirmUssd":
					agent.setConfirmUssd(state.parseBoolean(heading, value));
					break;
				case "sendBundleCommissionReport":
					agent.setSendBundleCommissionReport(state.parseBoolean(heading, value));
					break;	
				/*
				 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
				 */
				/*case "msisdnRecycled":
					agent.setMsisdnRecycled(state.parseBoolean(heading, value));
					break;*/

			}
		}
		
		if (areaName == null && areaType != null)
		{
			state.addIssue(BatchIssue.INVALID_VALUE, "area_type", null, "area type without area name");
			agent.setArea(null);
			agent.setAreaID(null);
		}
		else if (areaName != null && areaType == null)
		{
			state.addIssue(BatchIssue.INVALID_VALUE, "area_name", null, "area name without area type");
			agent.setArea(null);
			agent.setAreaID(null);
		}
		else if (areaName == null && areaType == null)
		{
			agent.setArea(null);
			agent.setAreaID(null);
		}
		else
		{
			Area area = Area.findByNameAndType(em, state.getCompanyID(), areaName, areaType);
			if (area == null)
				state.addIssue(BatchIssue.INVALID_VALUE, "area_name", null, "area " + areaName + ":" + areaType + " not found");
			else
			{
				agent.setArea(area);
				agent.setAreaID(area.getId());
			}
		}

		// Auto Number if Required
		agent.autoNumber(em, state.getCompanyID());

	}

	@Override
	protected Agent loadExisting(EntityManager em, State state, String[] rowValues)
	{
		// Load By ID
		Integer columnIndex = columnIndexForProperty("id");
		if (columnIndex != null)
		{
			int id = state.parseInt("id", rowValues[columnIndex], 0);
			Agent agent = Agent.findByID(em, id, state.getCompanyID());
			if (agent != null)
				return agent;
		}

		// Load by Name
		columnIndex = columnIndexForProperty("mobileNumber");
		if (columnIndex != null)
		{
			String msisdn = context.toMSISDN(rowValues[columnIndex]);
			Agent agent = Agent.findByMSISDN(em, msisdn, state.getCompanyID());
			if (agent != null)
				return agent;
		}

		return null;
	}

	@Override
	protected Stage addNew(EntityManager em, State state, Agent newInstance, List<Object> other)
	{
		// Set Temporary/Default PIN
		String pin = null;
		AgentsConfig config = getConfig(em, state.getCompanyID());
		
		// Check whether other contains pattern for pin set
		for (Object item : other) {
			Matcher matcher = PIN_PATTERN.matcher(item.toString());
			if (matcher.matches()) {
				pin = matcher.group(1);
				AuthenticationHelper.setPin(newInstance, pin);
				break;
			}
		}

		if (pin == null) {
			String defaultPin = config.getDefaultPin();
			boolean useDefaultPin = defaultPin != null && !defaultPin.isEmpty();
			if (useDefaultPin)
				pin = AuthenticationHelper.createDefaultPin(newInstance, defaultPin);
			else
				pin = AuthenticationHelper.createRandomPin(newInstance);
		}

		// Add Staging Entry
		newInstance.setSignature(newInstance.calcSecuritySignature());
		Stage stage = state.getStage(Stage.ACTION_INSERT, EA_AGENT) //

				.setB1(newInstance.isTemporaryPin()) //
				.setBd1(newInstance.getMaxDailyAmount()) //
				.setBd2(newInstance.getMaxMonthlyAmount()) //
				.setBd3(newInstance.getMaxTransactionAmount()) //
				.setBd4(newInstance.getWarningThreshold()) //
				.setD1(newInstance.getActivationDate()) //
				.setD2(newInstance.getDeactivationDate()) //
				.setD3(newInstance.getExpirationDate()) //
				.setD4(newInstance.getLastImsiChange()) //
				.setD5(newInstance.getDateOfBirth()) //
				.setCompanyID(newInstance.getCompanyID()) //
				.setDescription(newInstance.getSurname()) //
				.setI1(newInstance.getMaxDailyCount()) //
				.setI2(newInstance.getMaxMonthlyCount()) //
				.setI3(newInstance.getAllowedChannels()) //
				.setReportCountLimit(newInstance.getReportCountLimit()) //
				.setReportDailyScheduleLimit(newInstance.getReportDailyScheduleLimit()) //
				.setLastTime(newInstance.getLastTime()) //
				.setLastUserID(newInstance.getLastUserID()) //
				.setName(newInstance.getFirstName()) //
				.setState(newInstance.getState()) //
				.setTierID1(newInstance.getTierID()) //
				.setType(newInstance.getGender()) //
				.setAreaID(newInstance.getAreaID()) //
				.setGroupID(newInstance.getGroupID()) //
				.setServiceClassID(newInstance.getServiceClassID()) //
				.setI5(newInstance.getRoleID()) //
				.setSignature(newInstance.getSignature()) //
				.setAgentID(newInstance.getSupplierAgentID()) //
				.setI4(newInstance.getOwnerAgentID()) //
				.setAccountNumber(newInstance.getAccountNumber()) //
				.setAltPhoneNumber(newInstance.getAltPhoneNumber()) //
				.setEmail(newInstance.getEmail()) //
				.setDomainAccountName(newInstance.getDomainAccountName()) //
				.setImei(newInstance.getImei()) //
				.setImsi(newInstance.getImsi()) //
				.setInitials(newInstance.getInitials()) //
				.setLanguage(newInstance.getLanguage()) //
				.setMobileNumber(newInstance.getMobileNumber()) //
				.setS1(newInstance.getAuthenticationMethod()) //
				.setPostalAddressCity(newInstance.getPostalAddressCity()) //
				.setPostalAddressLine1(newInstance.getPostalAddressLine1()) //
				.setPostalAddressLine2(newInstance.getPostalAddressLine2()) //
				.setPostalAddressSuburb(newInstance.getPostalAddressSuburb()) //
				.setPostalAddressZip(newInstance.getPostalAddressZip()) //
				.setStreetAddressCity(newInstance.getStreetAddressCity()) //
				.setStreetAddressLine1(newInstance.getStreetAddressLine1()) //
				.setStreetAddressLine2(newInstance.getStreetAddressLine2()) //
				.setStreetAddressSuburb(newInstance.getStreetAddressSuburb()) //
				.setStreetAddressZip(newInstance.getStreetAddressZip()) //
				.setTitle(newInstance.getTitle()) //
				.setZip(pin) //
				.setB2(newInstance.isConfirmUssd()) //
				.setKey(newInstance.getKey1()) //
				.setB2(newInstance.isSendBundleCommissionReport()) //
		;

		return stage;
	}

	@Override
	protected Stage updateExisting(EntityManager em, State state, Agent existing, Agent updated, List<Object> other)
	{
		// Check whether the list called "other" contains pattern for pin update
		for (Object item : other) {
			Matcher matcher = PIN_PATTERN.matcher(item.toString());
			if (matcher.matches()) {
				AuthenticationHelper.setPin(updated, matcher.group(1));
				if (config == null) config = getConfig(em, state.getCompanyID());
				logger.info("Updating PIN for agent mobile '{}'", updated.getMobileNumber());
				Agents.sendPinNotification(context, config, updated.getMobileNumber(), updated.getLanguage(), updated.isTemporaryPin(), matcher.group(1));
				break;
			}
		}
	
		// Add Staging Entry
		// If any attributes need to be copied from `existing` to `updated`, 
		// do it before calculating the signature.
		if (updated.getKey1() == null) 
			updated.setKey1(existing.getKey1());
		//Calculate the signature last after copying attributes.
		updated.setSignature(updated.calcSecuritySignature());
		Stage stage = state.getStage(Stage.ACTION_UPDATE, EA_AGENT) //

				.setB1(updated.isTemporaryPin()) //
				.setBd1(updated.getMaxDailyAmount()) //
				.setBd2(updated.getMaxMonthlyAmount()) //
				.setBd3(updated.getMaxTransactionAmount()) //
				.setBd4(updated.getWarningThreshold()) //
				.setD1(updated.getActivationDate()) //
				.setD2(updated.getDeactivationDate()) //
				.setD3(updated.getExpirationDate()) //
				.setD4(updated.getLastImsiChange()) //
				.setD5(updated.getDateOfBirth()) //
				.setDescription(updated.getSurname()) //
				.setI1(updated.getMaxDailyCount()) //
				.setI2(updated.getMaxMonthlyCount()) //
				.setI3(updated.getAllowedChannels()) //
				.setReportCountLimit(updated.getReportCountLimit()) //
				.setReportDailyScheduleLimit(updated.getReportDailyScheduleLimit()) //
				.setName(updated.getFirstName()) //
				.setState(updated.getState()) //
				.setTierID1(updated.getTierID()) //
				.setType(updated.getGender()) //
				.setAreaID(updated.getAreaID()) //
				.setGroupID(updated.getGroupID()) //
				.setServiceClassID(updated.getServiceClassID()) //
				.setI5(updated.getRoleID()) //
				.setAgentID(updated.getSupplierAgentID()) //
				.setI4(updated.getOwnerAgentID()) //
				.setAccountNumber(updated.getAccountNumber()) //
				.setAltPhoneNumber(updated.getAltPhoneNumber()) //
				.setEmail(updated.getEmail()) //
				.setDomainAccountName(updated.getDomainAccountName()) //
				.setImei(updated.getImei()) //
				.setImsi(updated.getImsi()) //
				.setInitials(updated.getInitials()) //
				.setLanguage(updated.getLanguage()) //
				.setMobileNumber(updated.getMobileNumber()) //
				.setS1(updated.getAuthenticationMethod()) //
				.setPostalAddressCity(updated.getPostalAddressCity()) //
				.setPostalAddressLine1(updated.getPostalAddressLine1()) //
				.setPostalAddressLine2(updated.getPostalAddressLine2()) //
				.setPostalAddressSuburb(updated.getPostalAddressSuburb()) //
				.setPostalAddressZip(updated.getPostalAddressZip()) //
				.setStreetAddressCity(updated.getStreetAddressCity()) //
				.setStreetAddressLine1(updated.getStreetAddressLine1()) //
				.setStreetAddressLine2(updated.getStreetAddressLine2()) //
				.setStreetAddressSuburb(updated.getStreetAddressSuburb()) //
				.setStreetAddressZip(updated.getStreetAddressZip()) //
				.setTitle(updated.getTitle()) //
				.setSignature(updated.getSignature()) //
				.setEntityID(existing.getId()) //
				.setEntityVersion(existing.getVersion()) //
				.setB2(updated.isConfirmUssd()) //
				.setKey(updated.getKey1()) //
				.setB2(updated.isSendBundleCommissionReport()) //
				/*
				 *	Functionality on hold MSISDN-RECYCLING - uncomment when re-instated
				 */
				//.setMsisdnRecycled(updated.getMsisdnRecycled()) //
		;

		return stage;
	}

	@Override
	protected Stage deleteExisting(EntityManager em, State state, Agent existing, List<Object> other)
	{
		if (Agent.STATE_PERMANENT.equals(existing.getState()))
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Cannot delete Permanent Agent");
			return null;
		}

		String agentInUseMessage = null;
		if (Agent.referencesAgent(em, existing.getId())) {
			agentInUseMessage = "Another agent references this one.";
		} else if (Transaction.referencesAgent(em, existing.getId())) {
			agentInUseMessage = "A transaction references the agent.";
		} else if (AuditEntry.referencesAgent(em, existing.getId())) {
			agentInUseMessage = "An audit entry references the agent.";
		} else if (WorkItem.referencesAgent(em, existing.getId())) {
			agentInUseMessage = "A work item references the agent.";
		}

		if (agentInUseMessage != null) {
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, agentInUseMessage);
			return null;
		}

		// Check for 0 Balance
		Account account = Account.findByAgentID(em, existing.getId(), false);
		if (account == null || account.getBalance().signum() != 0 || account.getBonusBalance().signum() != 0)
		{
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, "Balances not Zero");
			return null;
		}

		// Add Staging Entry
		Stage stage = state.getStage(Stage.ACTION_DELETE, EA_AGENT) //

				.setEntityID(existing.getId()) //
		;

		return stage;
	}

	
	@Override
	public boolean complete(EntityManager em, State state)
	{
		// Exit if there are issues
		if (state.hasIssues())
			return false;

		// Check for duplicates
		if (!checkForDuplicates(em, state, EA_AGENT, "acc_no", "account_number") //
				|| !checkForDuplicates(em, state, EA_AGENT, "msisdn", "account_number") //
				|| !checkForDuplicates(em, state, EA_AGENT, "domain_account", "domain_account") //
		)
			return false;

		// Insert
		try
		{
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_INSERT, EA_AGENT);
			if (expected > 0)
			{
					String sqlString = "insert ea_agent " //
							+ "(pin_version, temp_pin, confirm_ussd, send_daily_bundle_commission_report, key1, max_daily_amount, max_monthly_amount, max_amount, warn_level, a_date, d_date, e_date, last_imsi, dob, comp_id, surname, version, max_daily_count, max_monthly_count, max_report_count, max_report_daily_schedule_count, channels, lm_time, lm_userid, first_name, state, tier_id, gender, area_id, group_id, sc_id, signature, supplier_id, owner_id, acc_no, alt_phone, email, domain_account, imei, imsi, intitials, language, msisdn, auth_method, postal_city, postal1, postal2, postal_suburb, postal_zip, street_city, street1, street2, street_suburb, street_zip, title, role_id) " //
							+ "select 1 as pin_version, b1 as temp_pin, b2 as confirm_ussd, b2 as send_daily_bundle_commission_report, key1, bd1 as max_daily_amount, bd2 as max_monthly_amount, bd3 as max_amount, bd4 as warn_level, d1 as a_date, d2 as d_date, d3 as e_date, d4 as last_imsi, d5 as dob, company_id as comp_id, description as surname, 0 as version, i1 as max_daily_count, i2 as max_monthly_count, max_report_count, max_report_daily_schedule_count, i3 as channels, lm_time, lm_userid, name as first_name, state, tier_id1 as tier_id, type as gender, area_id, group_id, sc_id, signature, agent_id as supplier_id, i4 as owner_id, acc_no, alt_phone, email, domain_account, imei, imsi, intitials, language, msisdn, s1 as auth_method, postal_city, postal1, postal2, postal_suburb, postal_zip, street_city, street1, street2, street_suburb, street_zip, title, i5 as role_id " //
							+ "from eb_stage where batch_id = :batch_id and action = :action and table_id = :tableID order by line_no";
				/*
				 * Functionality on hold MSISDN-RECYCLING - uncomment when re-instating
				 */
					/*String sqlString = "insert ea_agent " //
						+ "(pin_version, temp_pin, confirm_ussd, key1, max_daily_amount, max_monthly_amount, max_amount, warn_level, a_date, d_date, e_date, last_imsi, dob, comp_id, surname, version, max_daily_count, max_monthly_count, max_report_count, max_report_daily_schedule_count, channels, lm_time, lm_userid, first_name, state, tier_id, gender, area_id, group_id, sc_id, signature, supplier_id, owner_id, acc_no, alt_phone, email, domain_account, imei, imsi, intitials, language, msisdn, msisdn_recycled, auth_method, postal_city, postal1, postal2, postal_suburb, postal_zip, street_city, street1, street2, street_suburb, street_zip, title, role_id) " //
						+ "select 1 as pin_version, b1 as temp_pin, b2 as confirm_ussd, key1, bd1 as max_daily_amount, bd2 as max_monthly_amount, bd3 as max_amount, bd4 as warn_level, d1 as a_date, d2 as d_date, d3 as e_date, d4 as last_imsi, d5 as dob, company_id as comp_id, description as surname, 0 as version, i1 as max_daily_count, i2 as max_monthly_count, max_report_count, max_report_daily_schedule_count, i3 as channels, lm_time, lm_userid, name as first_name, state, tier_id1 as tier_id, type as gender, area_id, group_id, sc_id, signature, agent_id as supplier_id, i4 as owner_id, acc_no, alt_phone, email, domain_account, imei, imsi, intitials, language, msisdn, msisdn_recycled, s1 as auth_method, postal_city, postal1, postal2, postal_suburb, postal_zip, street_city, street1, street2, street_suburb, street_zip, title, i5 as role_id " //
						+ "from eb_stage where batch_id = :batch_id and action = :action and table_id = :tableID order by line_no";
*/
				Query insertQuery = em.createNativeQuery(sqlString);
				insertQuery.setParameter("batch_id", state.getBatch().getId());
				insertQuery.setParameter("action", Stage.ACTION_INSERT);
				insertQuery.setParameter("tableID", EA_AGENT);
				int count = insertQuery.executeUpdate();
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_ADD, "name", expected, String.format("%d/%d records inserted", count, expected));
					return false;
				}
				//Agent agent = Agent.findBy;
				
				// Add Accounts
				/**/

				sqlString = "insert ea_account (agent_id, balance, bonus, on_hold, day, day_count, day_total, lm_time, lm_userid, month_count, month_total, signature, version) " //
						+ "select a.id, :balance, :bonus, :on_hold, :day, :day_count, :day_total, s.lm_time, s.lm_userid, :month_count, :month_total, :signature, :version " //
						+ "from eb_stage as s join ea_agent as a on s.msisdn = a.msisdn " //
						+ "where batch_id = :batch_id and action = :action and table_id = :tableID order by line_no";
				insertQuery = em.createNativeQuery(sqlString);
				insertQuery.setParameter("batch_id", state.getBatch().getId());
				insertQuery.setParameter("action", Stage.ACTION_INSERT);
				insertQuery.setParameter("tableID", EA_AGENT);
				
				insertQuery.setParameter("balance", BigDecimal.ZERO); 
				insertQuery.setParameter("bonus", BigDecimal.ZERO);
				insertQuery.setParameter("on_hold", BigDecimal.ZERO);
				insertQuery.setParameter("day", state.getBatch().getTimestamp());
				insertQuery.setParameter("day_count", 0);
				insertQuery.setParameter("day_total", BigDecimal.ZERO);
				insertQuery.setParameter("month_count", 0);
				insertQuery.setParameter("month_total", BigDecimal.ZERO);
				insertQuery.setParameter("signature", 0);
				insertQuery.setParameter("version", 0);

				count = insertQuery.executeUpdate();
				
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_ADD, "name", expected, String.format("%d/%d records inserted", count, expected));
					return false;
				}

				state.getBatch().setInsertCount(count);
			}
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_ADD, tr);
			state.addIssue(BatchIssue.CANNOT_ADD, null, null, tr.getMessage());
			return false;
		}

		// Update
		try
		{
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_UPDATE, EA_AGENT);
			if (expected > 0)
			{
				String sqlString = "update  ea_agent as c \n" //
						+ "join    eb_stage as s on c.id = s.entity_id and c.version = s.entity_version  \n" //

						+ "set		c.temp_pin = s.b1 \n" //
						+ ",		c.confirm_ussd = s.b2 \n" //
						+ ", 		c.send_daily_bundle_commission_report = s.b2 \n" //
						+ ",		c.max_daily_amount = s.bd1 \n" //
						+ ",		c.max_monthly_amount = s.bd2 \n" //
						+ ",		c.max_amount = s.bd3 \n" //
						+ ",		c.warn_level = s.bd4 \n" //
						+ ",		c.a_date = s.d1 \n" //
						+ ",		c.d_date = s.d2 \n" //
						+ ",		c.e_date = s.d3 \n" //
						+ ",		c.last_imsi = s.d4 \n" //
						+ ",		c.dob = s.d5 \n" //
						+ ",		c.comp_id = s.company_id \n" //
						+ ",		c.surname = s.description \n" //
						+ ",		c.id = s.entity_id \n" //
						+ ",		c.version = s.entity_version \n" //
						+ ",		c.max_daily_count = s.i1 \n" //
						+ ",		c.max_monthly_count = s.i2 \n" //
						+ ",		c.max_report_count = s.max_report_count \n" //
						+ ",		c.max_report_daily_schedule_count = s.max_report_daily_schedule_count \n" //
						+ ",		c.channels = s.i3 \n" //
						+ ",		c.lm_time = s.lm_time \n" //
						+ ",		c.lm_userid = s.lm_userid \n" //
						+ ",		c.first_name = s.name \n" //
						+ ",		c.state = s.state \n" //
						+ ",		c.tier_id = s.tier_id1 \n" //
						+ ",		c.gender = s.type \n" //
						+ ",		c.area_id = s.area_id \n" //
						+ ",		c.group_id = s.group_id \n" //
						+ ",		c.sc_id = s.sc_id \n" //
						+ ",		c.role_id = s.i5 \n" //
						+ ",		c.signature = s.signature \n" //
						+ ",		c.supplier_id = s.agent_id \n" //
						+ ",		c.owner_id = s.i4 \n" //
						+ ",		c.acc_no = s.acc_no \n" //
						+ ",		c.alt_phone = s.alt_phone \n" //
						+ ",		c.email = s.email \n" //
						+ ",		c.domain_account = s.domain_account \n" //
						+ ",		c.imei = s.imei \n" //
						+ ",		c.imsi = s.imsi \n" //
						+ ",		c.intitials = s.intitials \n" //
						+ ",		c.language = s.language \n" //
						+ ",		c.msisdn = s.msisdn \n" //
						/*
						 * Functionality on hold MSISDN-RECYCLING - uncomment when re-instating
						 */
						//+ ",		c.msisdn_recycled = s.msisdn_recycled \n" //
						+ ",        c.auth_method = s.s1 \n" //
						+ ",		c.postal_city = s.postal_city \n" //
						+ ",		c.postal1 = s.postal1 \n" //
						+ ",		c.postal2 = s.postal2 \n" //
						+ ",		c.postal_suburb = s.postal_suburb \n" //
						+ ",		c.postal_zip = s.postal_zip \n" //
						+ ",		c.street_city = s.street_city \n" //
						+ ",		c.street1 = s.street1 \n" //
						+ ",		c.street2 = s.street2 \n" //
						+ ",		c.street_suburb = s.street_suburb \n" //
						+ ",		c.street_zip = s.street_zip \n" //
						+ ",		c.title = s.title \n" //
						+ ",		c.key1 = s.key1 \n" //

						+ "where   s.action = :action \n" //
						+ "and     s.table_id = :tableID \n" //
						+ "and     s.batch_id = :batchID";

				Query updateQuery = em.createNativeQuery(sqlString);
				updateQuery.setParameter("batchID", state.getBatch().getId());
				updateQuery.setParameter("action", Stage.ACTION_UPDATE);
				updateQuery.setParameter("tableID", EA_AGENT);
				int count = updateQuery.executeUpdate();
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_UPDATE, "name", expected, String.format("%d/%d records updated", count, expected));
					return false;
				}
				state.getBatch().setUpdateCount(count);
			}
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_UPDATE, tr);
			state.addIssue(BatchIssue.CANNOT_UPDATE, null, null, tr.getMessage());
			return false;
		}

		// Delete
		try
		{
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_DELETE, EA_AGENT);
			if (expected > 0)
			{
				logger.info("START time DELETE ACCOUNT: " + new Date());
				// Delete Accounts first
				String sql = "delete s.* from ea_account as s inner join eb_stage as b " //
						+ "on b.batch_id = :batchID and b.entity_id = s.agent_id and b.action = :action and b.table_id = :tableID and s.balance = 0 and s.bonus = 0";
				Query query = em.createNativeQuery(sql);
				query.setParameter("batchID", state.getBatch().getId());
				query.setParameter("action", Stage.ACTION_DELETE);
				query.setParameter("tableID", EA_AGENT);

				int count = query.executeUpdate();

				logger.info("END time DELETE ACCOUNT: " + new Date());
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_DELETE, "id", expected, String.format("%d/%d Account records deleted", count, expected));
					return false;
				}

				logger.info("START time DELETE AGENT: " + new Date());
				count = Stage.delete(em, "ea_agent", state.getBatch().getId(), EA_AGENT);
				logger.info("END time DELETE AGENT: " + new Date());
				if (count != expected)
				{
					state.addIssue(BatchIssue.CANNOT_DELETE, "id", expected, String.format("%d/%d Agent records deleted", count, expected));
					return false;
				}

				// Accounts as well
				state.getBatch().setDeleteCount(count);
			}
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_DELETE, tr);
			state.addIssue(BatchIssue.CANNOT_DELETE, null, null, tr.getMessage());
			return false;
		}

		// Finalize the account signatures
		try
		{			
			int expected = Stage.expected(em, state.getBatch().getId(), Stage.ACTION_INSERT, EA_AGENT);
			if (expected > 0)
			{
				int startPosition = 0;
				List<Stage> newAgents = Stage.findRecords(em, state.getCompanyID(), state.getBatch().getId(), EA_AGENT, Stage.ACTION_INSERT, startPosition, 50);
				boolean isEmpty = newAgents.isEmpty();
				while (!isEmpty)
				{					 					
					for (Stage batchAgent : newAgents)
					{	
						Agent agent = Agent.findByMSISDN(em, batchAgent.getMobileNumber(), state.getCompanyID());						
						Integer agentID = agent.getId();
						//Account account = Account.findByAgentID(em, agentID, true);
						Account account = new Account() //
								.setAgentID(agentID)
								.setVersion(0) //
								.setBalance(BigDecimal.ZERO) //
								.setBonusBalance(BigDecimal.ZERO) //
								.setOnHoldBalance(BigDecimal.ZERO) //
								.setDay(state.getBatch().getTimestamp()) //
								.setDayCount(0) //
								.setDayTotal(BigDecimal.ZERO) //
								.setMonthCount(0) //
								.setMonthTotal(BigDecimal.ZERO);
						long signature = account.calcSecuritySignature();
						
												
						String sqlString = "update  ea_account as acc \n" //
								+ "join    ea_agent as ag on acc.agent_id = ag.id \n" //
								+ "join    eb_stage as s on ag.msisdn = s.msisdn \n" //
								+ "set     acc.signature = :signature \n" //								
								+ "where   ag.msisdn = :msisdn \n"
								+ "and     s.action = :action \n" //
								+ "and     s.table_id = :tableID \n" //
								+ "and     s.batch_id = :batchID";

						Query updateQuery = em.createNativeQuery(sqlString);
						updateQuery.setParameter("signature", signature);
						updateQuery.setParameter("msisdn", batchAgent.getMobileNumber());
						updateQuery.setParameter("batchID", state.getBatch().getId());
						updateQuery.setParameter("action", Stage.ACTION_INSERT);
						updateQuery.setParameter("tableID", EA_AGENT);
						
						int count = updateQuery.executeUpdate();
						if (count != 1)
						{
							state.addIssue(BatchIssue.CANNOT_UPDATE, "name", expected, String.format("%d/%d records updated", count, expected));
							return false;
						}
					}
					startPosition += newAgents.size();
					newAgents = Stage.findRecords(em, state.getCompanyID(), state.getBatch().getId(), EA_AGENT, Stage.ACTION_INSERT, startPosition, 50);
					isEmpty = newAgents.isEmpty();
				}
			}
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_ADD, tr);
			state.addIssue(BatchIssue.CANNOT_ADD, null, null, tr.getMessage());
			return false;
		}
		
		// Send SMS'es
		try
		{
			AgentsConfig config = getConfig(em, state.getCompanyID());
			int startPosition = 0;
			while (true)
			{
				List<Stage> newAgents = Stage.findRecords(em, state.getCompanyID(), state.getBatch().getId(), EA_AGENT, Stage.ACTION_INSERT, startPosition, 50);
				if (newAgents.isEmpty())
					break;
				startPosition += newAgents.size();
				for (Stage agent : newAgents)
				{
					Agents.sendPinNotification(context, config, agent.getMobileNumber(), agent.getLanguage(), agent.getB1(), agent.getZip());
				}
			}
		}
		catch (Throwable tr)
		{
			logger.info(BatchIssue.CANNOT_ADD, tr);
			state.addIssue(BatchIssue.CANNOT_ADD, null, null, tr.getMessage());
			return false;
		}

		return true;
	}

	private AgentsConfig getConfig(EntityManager em, int companyID)
	{
		if (config == null)
		{
			CompanyInfo companyInfo = context.findCompanyInfoByID(companyID);
			config = companyInfo.getConfiguration(em, AgentsConfig.class);
		}
		return config;
	}
	

	@Override
	protected void verifyExisting(EntityManager em, State state, Agent existing, Agent instance, List<Object> other)
	{
		verify(state, "temporaryPin", existing.isTemporaryPin(), instance.isTemporaryPin());
		verify(state, "maxDailyAmount", existing.getMaxDailyAmount(), instance.getMaxDailyAmount());
		verify(state, "maxMonthlyAmount", existing.getMaxMonthlyAmount(), instance.getMaxMonthlyAmount());
		verify(state, "maxTransactionAmount", existing.getMaxTransactionAmount(), instance.getMaxTransactionAmount());
		verify(state, "warningThreshold", existing.getWarningThreshold(), instance.getWarningThreshold());
		verify(state, "activationDate", existing.getActivationDate(), instance.getActivationDate());
		verify(state, "deactivationDate", existing.getDeactivationDate(), instance.getDeactivationDate());
		verify(state, "expirationDate", existing.getExpirationDate(), instance.getExpirationDate());
		verify(state, "dateOfBirth", existing.getDateOfBirth(), instance.getDateOfBirth());
		verify(state, "surname", existing.getSurname(), instance.getSurname());
		verify(state, "maxDailyCount", existing.getMaxDailyCount(), instance.getMaxDailyCount());
		verify(state, "maxMonthlyCount", existing.getMaxMonthlyCount(), instance.getMaxMonthlyCount());
		verify(state, "reportCountLimit", existing.getReportCountLimit(), instance.getReportCountLimit());
		verify(state, "reportDailyScheduleLimit", existing.getReportDailyScheduleLimit(), instance.getReportDailyScheduleLimit());
		verify(state, "allowedChannels", existing.getAllowedChannels(), instance.getAllowedChannels());
		verify(state, "firstName", existing.getFirstName(), instance.getFirstName());
		verify(state, "state", existing.getState(), instance.getState());
		verify(state, "tierID", existing.getTierID(), instance.getTierID());
		verify(state, "gender", existing.getGender(), instance.getGender());
		verify(state, "areaID", existing.getAreaID(), instance.getAreaID());
		verify(state, "groupID", existing.getGroupID(), instance.getGroupID());
		verify(state, "serviceClassID", existing.getServiceClassID(), instance.getServiceClassID());
		verify(state, "roleID", existing.getRoleID(), instance.getRoleID());
		verify(state, "supplierAgentID", existing.getSupplierAgentID(), instance.getSupplierAgentID());
		verify(state, "ownerAgentID", existing.getOwnerAgentID(), instance.getOwnerAgentID());
		verify(state, "accountNumber", existing.getAccountNumber(), instance.getAccountNumber());
		verify(state, "altPhoneNumber", existing.getAltPhoneNumber(), instance.getAltPhoneNumber());
		verify(state, "email", existing.getEmail(), instance.getEmail());
		verify(state, "domainAccountName", existing.getDomainAccountName(), instance.getDomainAccountName());
		verify(state, "imei", existing.getImei(), instance.getImei());
		verify(state, "imsi", existing.getImsi(), instance.getImsi());
		verify(state, "initials", existing.getInitials(), instance.getInitials());
		verify(state, "language", existing.getLanguage().toLowerCase(), instance.getLanguage().toLowerCase());
		verify(state, "mobileNumber", existing.getMobileNumber(), instance.getMobileNumber());
		verify(state, "postalAddressCity", existing.getPostalAddressCity(), instance.getPostalAddressCity());
		verify(state, "postalAddressLine1", existing.getPostalAddressLine1(), instance.getPostalAddressLine1());
		verify(state, "postalAddressLine2", existing.getPostalAddressLine2(), instance.getPostalAddressLine2());
		verify(state, "postalAddressSuburb", existing.getPostalAddressSuburb(), instance.getPostalAddressSuburb());
		verify(state, "postalAddressZip", existing.getPostalAddressZip(), instance.getPostalAddressZip());
		verify(state, "streetAddressCity", existing.getStreetAddressCity(), instance.getStreetAddressCity());
		verify(state, "streetAddressLine1", existing.getStreetAddressLine1(), instance.getStreetAddressLine1());
		verify(state, "streetAddressLine2", existing.getStreetAddressLine2(), instance.getStreetAddressLine2());
		verify(state, "streetAddressSuburb", existing.getStreetAddressSuburb(), instance.getStreetAddressSuburb());
		verify(state, "streetAddressZip", existing.getStreetAddressZip(), instance.getStreetAddressZip());
		verify(state, "title", existing.getTitle(), instance.getTitle());
		verify(state, "confirmUssd", existing.isConfirmUssd(), instance.isConfirmUssd());
		verify(state, "sendBundleCommissionReport", existing.isSendBundleCommissionReport(), instance.isSendBundleCommissionReport());

	}
}
