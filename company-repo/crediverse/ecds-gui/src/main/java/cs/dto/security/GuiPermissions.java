package cs.dto.security;

import hxc.ecds.protocol.rest.Permission;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class GuiPermissions
{
	public enum PermissionGroup
	{
		Agent(Permission.GROUP_AGENTS),
		AgentUser(Permission.GROUP_AGENT_USERS),
		Area(Permission.GROUP_AREAS),
		Batch(Permission.GROUP_BATCHES),
		Cell(Permission.GROUP_CELLS),
		CellGroups(Permission.GROUP_CELLGROUPS),
		Company(Permission.GROUP_COMPANIES),
		Department(Permission.GROUP_DEPARTMENTS),
		General(Permission.GROUP_GENERAL),
		Group(Permission.GROUP_GROUPS),
		Perm(Permission.GROUP_PERMISSIONS),
		Role(Permission.GROUP_ROLES),
		ServiceClass(Permission.GROUP_SERVICECLASSES),
		Tier(Permission.GROUP_TIERS),
		Transaction(Permission.GROUP_TRANSACTIONS),
		Webui(Permission.GROUP_WEB_UI),
		TransferRule(Permission.GROUP_TRANSFERRULES),
		WebUser(Permission.GROUP_WEBUSERS),
		WorkItem(Permission.GROUP_WORKITEMS),
		Promotion(Permission.GROUP_PROMOTIONS),
		Bundle(Permission.GROUP_BUNDLES),
		AuditLog(Permission.GROUP_AUDIT_LOG),
		Reports(Permission.GROUP_REPORTS),
		Analytics(Permission.GROUP_ANALYTICS),
		UnKnown("UnknownGroup");

		private String group;
		private PermissionGroup(String group)
		{
			this.group = group;
		}

		public static PermissionGroup getPermissionGroup(String group)
		{
			for(PermissionGroup pg: PermissionGroup.values())
				if (pg.group.equals(group)) return pg;
			return PermissionGroup.UnKnown;
		}
	}

	public enum PermissionName
	{
		Add(Permission.PERM_ADD),
		Update(Permission.PERM_UPDATE),
		Delete(Permission.PERM_DELETE),
		Configure(Permission.PERM_CONFIGURE),
		View(Permission.PERM_VIEW),
		ResetImsi(Permission.PERM_RESET_IMSI),
		ResetPin(Permission.PERM_RESET_PIN),
		Upload(Permission.PERM_UPLOAD),
		Download(Permission.PERM_DOWNLOAD),
		Replenish(Permission.PERM_REPLENISH),
		AuthReplenish(Permission.PERM_AUTHORISE_REPLENISH),
		Transfer(Permission.PERM_TRANSFER),
		UpdateOwn(Permission.PERM_UPDATE_OWN),
		ResetPasswords(Permission.PERM_RESET_PASSWORDS),

		Sell(Permission.PERM_SELL),
		Adjust(Permission.PERM_ADJUST),
		Register(Permission.PERM_REGISTER_PIN),
		ChangePin(Permission.PERM_CHANGE_PIN),
		SelfTopup(Permission.PERM_SELF_TOPUP),
		SellBundle(Permission.PERM_SELL_BUNDLES),
		QueryBalance(Permission.PERM_QUERY_BALANCE),
		QueryDeposit(Permission.PERM_QUERY_DEPOSITS),
		QueryLast(Permission.PERM_QUERY_LAST),
		QuerySales(Permission.PERM_QUERY_SALES),
		QueryStatus(Permission.PERM_QUERY_STATUS),
		AuthAdjust(Permission.PERM_AUTHORISE_ADJUST),
		Reverse(Permission.PERM_REVERSE),
		AuthReverse(Permission.PERM_AUTHORISE_REVERSE),

		ConfigTransaction(Permission.PERM_CONFIGURE_TRANSACTIONS),
		ConfigBalEnquiry(Permission.PERM_CONFIGURE_BALANCE_ENQUIRIES),
		ConfigReplenish(Permission.PERM_CONFIGURE_REPLENISHMENT),
		ConfigTransfers(Permission.PERM_CONFIGURE_TRANSFERS),
		ConfigSales(Permission.PERM_CONFIGURE_SALES),
		ConfigPinReg(Permission.PERM_CONFIGURE_PIN_REGISTRATION),
		ConfigPinChange(Permission.PERM_CONFIGURE_PIN_CHANGE),
		TransferFromRoot(Permission.PERM_TRANSFER_FROM_ROOT_ACCOUNT),
		AuthRootTransfer(Permission.PERM_AUTHORISE_TRANSFER_FROM_ROOT_ACCOUNT),
		ConfigAdjust(Permission.PERM_CONFIGURE_ADJUSTMENTS),
		ConfigBatch(Permission.PERM_CONFIGURE_BATCH),
		ConfigReversal(Permission.PERM_CONFIGURE_REVERSALS),
		ConfigWebUi(Permission.PERM_CONFIGURE_WEB_UI),
		ConfigBundleSales(Permission.PERM_CONFIGURE_BUNDLE_SALES),
		ConfigDepQry(Permission.PERM_CONFIGURE_DEPOSITS_QUERY),
		ConfigSaleQry(Permission.PERM_CONFIGURE_SALES_QUERY),
		ConfigSelfTopup(Permission.PERM_CONFIGURE_SELF_TOPUP),
		ConfigTransStatEnq(Permission.PERM_CONFIGURE_TRANSACTION_STATUS_ENQUIRIES),
		ConfigTransEnq(Permission.PERM_CONFIGURE_TRANSACTION_ENQUIRY),
		ConfigReporting(Permission.PERM_CONFIGURE_REPORTING),
		ViewConfig(Permission.PERM_VIEW_CONFIGURATIONS),

		ConfigUssd(Permission.PERM_CONFIGURE_USSD),
		ConfigRewards(Permission.PERM_CONFIGURE_REWARDS),

		ConfigAdjudicate(Permission.PERM_CONFIGURE_ADJUDICATION),
		Adjudicate(Permission.PERM_ADJUDICATE),
		AuthoriseAdjdicate(Permission.PERM_AUTHORISE_ADJUDICATE),

		EsculateAgent(Permission.PERM_ESCALATE_AGENT),
		GeneralConfig(Permission.PERM_CONFIGURE_GENERAL_SETTINGS),
		Unknown("UnknownName");


		private String name;
		private PermissionName(String name)
		{
			this.name = name;
		}

		public static PermissionName getPermissionName(String name)
		{
			for(PermissionName pn: PermissionName.values())
				if (pn.name.equals(name)) return pn;
			return PermissionName.Unknown;
		}
	}

	private PermissionGroup group;
	private PermissionName name;

	public GuiPermissions(String group, String name)
	{
		this.group = PermissionGroup.getPermissionGroup(group);
		this.name = PermissionName.getPermissionName(name);
	}

	public static String formatPermission(String group, String name)
	{
		return String.format("%s_%s", group, name);
	}
	@Override
	public String toString()
	{
		return formatPermission(group.toString(), name.toString());
	}

}
