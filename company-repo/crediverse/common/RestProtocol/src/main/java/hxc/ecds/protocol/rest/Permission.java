package hxc.ecds.protocol.rest;

import java.util.List;

public class Permission implements IValidatable
{
	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Constants
	//
	// /////////////////////////////////
	public static final int GROUP_MAX_LENGTH = 50;
	public static final int NAME_MAX_LENGTH = 50;
	public static final int DESCRIPTION_MAX_LENGTH = 80;

	// Group Names
	public static final String GROUP_AGENTS = "Agents";
	public static final String GROUP_AGENT_USERS = "API/Agent Users";
	public static final String GROUP_AREAS = "Areas";
	public static final String GROUP_BATCHES = "Batches";
	public static final String GROUP_CELLS = "Cells";
	public static final String GROUP_CELLGROUPS = "CellGroups";
	public static final String GROUP_COMPANIES = "Companies";
	public static final String GROUP_DEPARTMENTS = "Departments";
	public static final String GROUP_GROUPS = "Groups";
	public static final String GROUP_PERMISSIONS = "Permissions";
	public static final String GROUP_ROLES = "Roles";
	public static final String GROUP_SERVICECLASSES = "ServiceClasses";
	public static final String GROUP_TIERS = "Tiers";
	public static final String GROUP_TRANSACTIONS = "Transactions";
	public static final String GROUP_WEB_UI = "Web UI";
	public static final String GROUP_TRANSFERRULES = "TransferRules";
	public static final String GROUP_WEBUSERS = "WebUsers";
	public static final String GROUP_WORKITEMS = "WorkItems";
	public static final String GROUP_PROMOTIONS = "Promotions";
	public static final String GROUP_BUNDLES = "Bundles";
	public static final String GROUP_AUDIT_LOG = "Audit Log";
	public static final String GROUP_REPORTS = "Reports";
	public static final String GROUP_ANALYTICS = "Analytics";
	public static final String GROUP_GENERAL = "General";

	// Permission Names
	public static final String PERM_ADD = "Add";
	public static final String PERM_UPDATE = "Update";
	public static final String PERM_DELETE = "Delete";
	public static final String PERM_CONFIGURE = "Configure";
	public static final String PERM_VIEW = "View";
	public static final String PERM_RESET_IMSI = "Reset IMSI";
	public static final String PERM_RESET_PIN = "Reset PIN";
	public static final String PERM_UPLOAD = "Upload";
	public static final String PERM_DOWNLOAD = "Download";
	public static final String PERM_REPLENISH = "Replenish";
	public static final String PERM_AUTHORISE_REPLENISH = "Authorise Replenish";
	public static final String PERM_TRANSFER = "Transfer";
	public static final String PERM_SELL = "Sell";
	public static final String PERM_ADJUST = "Adjust";
	public static final String PERM_REGISTER_PIN = "Register PIN";
	public static final String PERM_CHANGE_PIN = "Change PIN";
	public static final String PERM_SELF_TOPUP = "Self Topup";
	public static final String PERM_SELL_BUNDLES = "Sell Bundles";
	public static final String PERM_QUERY_BALANCE = "Query Balance";
	public static final String PERM_QUERY_DEPOSITS = "Query Deposits";
	public static final String PERM_QUERY_LAST = "Query Last Transaction";
	public static final String PERM_QUERY_SALES = "Query Sales";
	public static final String PERM_QUERY_STATUS = "Query Transaction Status";
	public static final String PERM_AUTHORISE_ADJUST = "Authorise Adjust";
	public static final String PERM_REVERSE = "Reverse";
	public static final String PERM_ADJUDICATE = "Adjudicate";
	public static final String PERM_AUTHORISE_REVERSE = "Authorise Reverse";
	public static final String PERM_AUTHORISE_ADJUDICATE = "Authorise Adjudication";
	public static final String PERM_CONFIGURE_TRANSACTIONS = "Configure Transactions";
	public static final String PERM_CONFIGURE_BALANCE_ENQUIRIES = "Configure Balance Enquiries";
	public static final String PERM_CONFIGURE_REPLENISHMENT = "Configure Replenishment";
	public static final String PERM_CONFIGURE_TRANSFERS = "Configure Transfers";
	public static final String PERM_CONFIGURE_SALES = "Configure Sales";
	public static final String PERM_CONFIGURE_PIN_REGISTRATION = "Configure Pin Registration";
	public static final String PERM_CONFIGURE_PIN_CHANGE = "Configure Pin Change";
	public static final String PERM_TRANSFER_FROM_ROOT_ACCOUNT = "Transfer from Root Account";
	public static final String PERM_AUTHORISE_TRANSFER_FROM_ROOT_ACCOUNT = "Authorise Transfer from Root Account";
	public static final String PERM_CONFIGURE_TRANSACTION_STATUS_ENQUIRIES = "Configure Transaction Status Enquiries";
	public static final String PERM_CONFIGURE_ADJUSTMENTS = "Configure Adjustments";
	public static final String PERM_CONFIGURE_BATCH = "Configure Batch";
	public static final String PERM_CONFIGURE_REVERSALS = "Configure Reversals";
	public static final String PERM_CONFIGURE_WEB_UI = "Configure Web UI";
	public static final String PERM_VIEW_CONFIGURATIONS = "View Configuration";
	public static final String PERM_ESCALATE_AGENT = "Escalate Agent";
	public static final String PERM_CONFIGURE_REPORTING = "Configure Reporting";

	public static final String PERM_CONFIGURE_SALES_QUERY = "Configure Sale Queries";
	public static final String PERM_CONFIGURE_DEPOSITS_QUERY = "Configure Deposit Queries";
	public static final String PERM_CONFIGURE_TRANSACTION_ENQUIRY = "Configure Transaction Enquiries";
	public static final String PERM_CONFIGURE_SELF_TOPUP = "Configure Self-Topups";
	public static final String PERM_CONFIGURE_BUNDLE_SALES = "Configure Bundle Sales";
	public static final String PERM_CONFIGURE_USSD = "Configure USSD and USSD Menu";
	public static final String PERM_CONFIGURE_REWARDS = "Configure Rewards";
	public static final String PERM_CONFIGURE_ADJUDICATION = "Configure Adjudication";
	public static final String PERM_CONFIGURE_GENERAL_SETTINGS = "Configure General Settings";

	public static final String PERM_UPDATE_OWN = "Update Own";
	public static final String PERM_RESET_PASSWORDS = "Reset Web-User Passwords";

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Fields
	//
	// /////////////////////////////////
	protected int id;
	protected int version;
	protected String group;
	protected String name;
	protected String description;
	protected boolean supplierOnly;
	protected boolean agentAllowed;

	// //////////////////////////////////////////////////////////////////////////////////////
	//
	// Properties
	//
	// /////////////////////////////////

	public int getId()
	{
		return id;
	}

	public Permission setId(int id)
	{
		this.id = id;
		return this;
	}

	public int getVersion()
	{
		return version;
	}

	public Permission setVersion(int version)
	{
		this.version = version;
		return this;
	}

	public String getGroup()
	{
		return group;
	}

	public Permission setGroup(String group)
	{
		this.group = group;
		return this;
	}

	public String getName()
	{
		return name;
	}

	public Permission setName(String name)
	{
		this.name = name;
		return this;
	}

	public String getDescription()
	{
		return description;
	}

	public Permission setDescription(String description)
	{
		this.description = description;
		return this;
	}

	public boolean isSupplierOnly()
	{
		return supplierOnly;
	}

	public Permission setSupplierOnly(boolean supplierOnly)
	{
		this.supplierOnly = supplierOnly;
		return this;
	}

	public boolean isAgentAllowed()
	{
		return agentAllowed;
	}

	public Permission setAgentAllowed(boolean agentAllowed)
	{
		this.agentAllowed = agentAllowed;
		return this;
	}

	@Override
	public String toString()
	{
		return String.format("%s:%s", group, name);
	}

	@Override
	public List<Violation> validate()
	{
		return new Validator() //
				.notEmpty("group", group, GROUP_MAX_LENGTH) //
				.notEmpty("name", name, GROUP_MAX_LENGTH) //
				.notEmpty("description", description, GROUP_MAX_LENGTH) //
				.toList();
	}

}
