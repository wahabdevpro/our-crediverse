define({ 	

	// root namespace is specific to the EN file 
	// It should not be copied to other translation files. 

  "root": {
	  context: "papi/context",
	  
	  userman: {
		  editUserTitle:		"Edit User <b>{{user}}</b>; Unique ID: <b>{{uniqueID}}</b>"
	  },
	  
	  navbar: {
		  dashboard:			"Agent Profile",
		  agentAccounts:		"Sub-agents",
		  transactions:		"Transactions",
		  retailerPerformanceReport: "Retailers Performance",
		  wholesalerPerformanceReport: "Wholesalers Performance",
		  accountBalanceSummaryReport:   "Account Balance Summary",
		  reports: 				"Reports",
		  list:					"List",
		  search:				"Search",
		  profile:				"Profile",
		  version : "Version: <b>{{appVersion}}</b>",
		  buildNumber: "BuildNumber: <b>{{buildNumber}}</b>"
	  },
	  
	  dashboard: {
		  heading:	"Dashboard",
		  homeBC:	"Dashboard",
		  
		  amount:		"Amount",
		  count:		"Count",
		  deposits:		"Deposits",
		  transfers:	"Transfers",
		  sales:		"Sales",
		  selfTopups:	"Self-Topups"
	  },
	  
	  transactionsA: {
		  listHeading: "Transactions List (Outbound)",
		  listPageBC:  "Transactions List (Outbound)",
		  searchHeading: "Transactions Search (Outbound)",
		  searchPageBC:  "Transactions Search (Outbound)"
	  },
	  
	  transactionsB: {
		  listHeading: "Transactions List (Inbound)",
		  listPageBC:  "Transactions List (Inbound)",
		  searchHeading: "Transactions Search (Inbound)",
		  searchPageBC:  "Transactions Search (Inbound)"
	  },
	  
	  profile: {
			heading:	"Agent Profile",
			pageBC:		"Agent Profile",
			viewUniqueID 			: "Unique ID",
			
			viewAccountNumber:		"Agent Account #",
			viewAccountTotalBalance:"Total Agent Balance",
			viewAccountBalance:		"Agent Balance",
			viewAccountTradeBonus:	"Trade Bonus Provision",
			viewAccountOnHoldBalance:"On-hold Balance",
			
			profileTab:			"Agent Profile",
			amlLimitsTab:		"AML Limits",
			transactionsATab:	"Transactions (Outbound)",
			transactionsBTab:	"Transactions (Inbound)",
			transactionsXTab:	"Transactions",
			summaryTab:			"Daily Summary",
			myProfileTab:		"My Profile",
			
			viewPersonalDetails:	"Details",
			viewTitleLabel:			"Title",
			viewFirstNameLabel:		"First name",
			viewSurnameLabel:		"Last name",
			viewInitialsLabel:		"Initials",
			viewAuthenticationMethodLabel:	"Authentication Method",
			viewDomainAccountLabel:	"Domain account name",
			viewLanguageLabel:		"Language",
			viewGenderLabel:		"Gender",
			viewGenderValueMale:	"Male",
			viewGenderValueFemale:	"Female",
			viewGenderValueOther:	"Other",
			viewDOBLabel:			"Date of birth",
			viewEmailLabel:			"Email address",
			viewMobileNumberLabel:	"Mobile number",
			viewAltNumberLabel:	"Alt. Mobile number",
			viewRoleLabel:			"Role",
			viewSegmentationHeading:	"Segmentation",
			viewTierTypeLabel:			"Tier type",
			viewTierNameLabel:			"Tier name",
			viewGroupNameLabel:		"Group name",
			viewServiceClassLabel:	"Service class name",
			viewAreaLabel: 			"Area",
			viewIdentificationHeading:	"Identification",
			viewImeiLabel:			"IMEI",
			viewImsiLabel:			"IMSI",
			viewLockedOut:			"IMSI LOCKED OUT",
			viewUnlockButton:		"Unlock",
			viewOtherHeading:		"Other",
			viewOwnerAgentName: "Owner agent",
			viewUpstreamAgentName: "Upstream agent",
			viewActivationDate:		"Activation date",
			viewdeactivationDate:	"Deactivation date",
			viewExpiryDate:			"Expiration date",
			viewChannelLabel:		"Channels",
			viewChannelUssdLabel:	"USSD",
			viewChannelSmsLabel:	"SMS",
			viewChannelApiLabel:	"API",
			viewChannelWuiLabel:	"Web UI",
			viewChannelAppLabel:	"Smart Phone",
			viewTempPinLabel:	"Temporary PIN",
			viewWarningThresholdLabel:	"Warning threshold",
			viewStatusLabel:	"Status",
			viewAmlLimitHeading: "AML Limits",
			viewMaxTransAmountLabel: 	"Max Transaction Amount",
			viewMaxTransCountLabel: 	"Max Transaction Count",
			viewMaxDailyCountLabel:		"Max Daily Count",
			viewMaxDailyAmountLabel:	"Max Daily Amount",
			viewMaxMonthlyCountLabel:	"Max Monthly Count",
			viewMaxMonthlyAmountLabel:	"Max Monthly Amount",
			viewDepartmentLabel:	"Department",
			viewQuickSearchPlaceholder:	"Mobile Number",
			
			viewaltPhoneNumberLabel:	"Alt. phone number",
			
			viewPostalAddressHeading:	"Postal Address",
			viewStreetAddressHeading:	"Street Address",
			
			viewStreetAddressLine1Label:	"Address line 1",
			viewStreetAddressLine2Label:	"Address line 2",
			viewStreetAddressSuburbLabel:	"Address suburb",
			viewStreetAddressCityLabel:	"Address city",
			viewStreetAddressZipLabel:	"Address ZIP code",

			viewPostalAddressLine1Label:	"Address line 1",
			viewPostalAddressLine2Label:	"Address line 2",
			viewPostalAddressSuburbLabel:	"Address suburb",
			viewPostalAddressCityLabel:	"Address city",
			viewPostalAddressZipLabel:	"Address ZIP code",
			viewAltNumberLabel:	"Alt. Number",
			viewAltNumber:	"Alt. Number",
			viewTransferButton:	"Transfer",
			viewPinChangeButton:	"Change PIN",
			viewChangePasswordButton:	"Change Password"
	  },
	  
	  pinChange: {
		  heading:		"PIN Change",
		  pageBC:		"PIN Change"
	  },
	  
	  agentAccounts: {
	  		agentAccount:			"Agent Account",
		  	agentAccounts:			"Agent Accounts",
			accountsListHeading:	"Agent Accounts List",
			accountsSrchHeading:	"Agent Accounts Search",
			
			accountHeading:			"Agent Account",
			accountSectionBC:		"Agent Accounts",
			accountPageBC:			"Account View",
		  
		 	editModalTitle:			"Edit Agent Account <b>{{name}}</b>",
		  	addModalTitle:			"Add New Agent Account",
			
			viewQuickSearchPlaceholder:	"Mobile Number",
			
		  	viewUniqueID : 			"Unique ID",
			viewAccountNumber:		"Account #",
			viewAccountTotalBalance:"Total Balance",
			viewAccountBalance:		"Balance",
			viewAccountTradeBonus:	"Trade Bonus Provision",
			viewAccountOnHoldBalance:"On-hold Balance",
			viewAccountProfileTab:	"Profile",
			viewAccountAmlTab:		"AML Limits",
			viewTransactionsATab:	"Transactions (Outbound)",
			viewTransactionsBTab:	"Transactions (Inbound)",
			viewTransactionsXTab:	"Transactions",
			viewPersonalDetails:	"Details",
			viewTitleLabel:			"Title",
			viewFirstNameLabel:		"First name",
			viewSurnameLabel:		"Last name",
			viewInitialsLabel:		"Initials",
			viewAuthenticationMethodLabel:	"Authentication Method",
			viewDomainAccountLabel:	"Domain account name",
			viewLanguageLabel:		"Language",
			viewGenderLabel:		"Gender",
			viewGenderValueMale:	"Male",
			viewGenderValueFemale:	"Female",
			viewGenderValueOther:	"Other",
			
			viewDOBLabel:			"Date of birth",
			viewEmailLabel:			"Email address",
			viewMobileNumberLabel:	"Mobile number",
			viewAltNumberLabel:	"Alt. Mobile number",
			viewRoleLabel:			"Role",
			
			viewSegmentationHeading:	"Segmentation",
			viewTierTypeLabel:			"Tier type",
			viewTierNameLabel:			"Tier name",
			viewGroupNameLabel:		"Group name",
			viewServiceClassLabel:	"Service class name",
			viewAreaLabel: 			"Area",
			
			viewIdentificationHeading:	"Identification",
			viewImeiLabel:			"IMEI",
			viewImsiLabel:			"IMSI",
			viewLockedOut:			"IMSI LOCKED OUT",
			viewUnlockButton:		"Unlock",
			deleteAgentAccountButton: 	"Delete Agent Account",
			
			viewOtherHeading:		"Other",
			viewOwnerAgentName: "Owner agent",
			viewUpstreamAgentName: "Upstream agent",
			viewActivationDate:		"Activation date",
			viewdeactivationDate:	"Deactivation date",
			viewExpiryDate:			"Expiration date",
			viewChannelLabel:		"Channels",
			viewChannelUssdLabel:	"USSD",
			viewChannelSmsLabel:	"SMS",
			viewChannelApiLabel:	"API",
			viewChannelWuiLabel:	"Web UI",
			viewChannelAppLabel:	"Smart Phone",
			
			viewTempPinLabel:	"Temporary PIN",
			viewWarningThresholdLabel:	"Warning threshold",
			viewStatusLabel:	"Status",
			
			viewAmlLimitHeading: "AML Limits",
			
			viewMaxTransAmountLabel: 	"Max Transaction Amount",
			viewMaxTransCountLabel: 	"Max Transaction Count",
			viewMaxDailyCountLabel:		"Max Daily Count",
			viewMaxDailyAmountLabel:	"Max Daily Amount",
			viewMaxMonthlyCountLabel:	"Max Monthly Count",
			viewMaxMonthlyAmountLabel:	"Max Monthly Amount",
			
			viewMaxTransAmountHint: 	"Max Transaction Amount",
			viewMaxTransCountHint: 		"Max Transaction Count",
			viewMaxDailyCountHint:		"Max Daily Count",
			viewMaxDailyAmountHint:		"Max Daily Amount",
			viewMaxMonthlyCountHint:	"Max Monthly Count",
			viewMaxMonthlyAmountHint:	"Max Monthly Amount",
			
			editAgentHeading:	"New Agent",
			
			editTitleLabel:		"Title",
			editTitleHint:		"User title",
			
			editFirstNameLabel:		"First Name",
			editFirstNameHint:		"First Name",
			
			editSurnameLabel:		"Surname",
			editSurnameHint:		"Surname",
			
			editInitialsLabel:		"Initials",
			editInitialsHint:		"Initials",
			
			editMobilePhoneLabel:		"Mobile Phone",
			editMobilePhoneHint:		"Mobile Phone Number",
			
			editAltPhoneLabel:		"Alternative Phone",
			editAltPhoneHint:		"Alternative Phone Number",
			
			editAgentTierLabel:		"Agent Tier",
			editAgentTierHint:		"(select agent tier)",
			
			editServiceClassLabel:		"Service Class",
			editGroupLabel:				"Group",
			editUpstreamAgentLabel:		"Upstream Agent",
			editOwnerAgentLabel:		"Owner Agent",
			
			editAccountNumberLabel:		"Account Number",
			editAccountNumberHint:		"Account number",			
			
			editDomainAccountLabel:		"Domain Account",
			editDomainAccountHint:		"Domain account name",
			editDomainAccountComment:	"Domain controller (LDAP) Username",
			
			editDobLabel:			"Date of Birth",
			editDobHint:			"YYYY-MM-DD",
			
			editLanguageLabel:		"Language",
			editLanguageHint:		"Preferred user language",
			editLanguageDefault:	"(select your language)",
			
			editGenderLabel:		"Gender",
			editGenderHint:			"Agent gender",
			
			editWarningLabel:		"Warning Threshold",
			editWarningHint:		"Warning threshold",
			
			editExpirationLabel:	"Account Expiration",
			editExpirationHint:		"Account expiration date",
			
			editStateLabel:			"Account State",
			editStateHint:			"Agent account state",
			
			editAddressLine1Label:	"Address line 1",
			editAddressLine1Hint:	"Address line 1",
			
			editAddressLine2Label:	"Address line 2",
			editAddressLine2Hint:	"Address line 1",
			
			editAddressSuburbLabel:	"Address suburb",
			editAddressSuburbHint:	"Address suburb",
			
			editAddressCityLabel:	"Address city",
			editAddressCityHint:	"Address city",
			
			editAddressZipLabel:	"Address ZIP code",
			editAddressZipHint:		"Address ZIP code",
			
			editStreetAddressHeading:	"Street Address",
			editPostalAddressHeading:	"Postal Address",
			
			editSaveAgentButton:		"Save Agent",
			
			searchResultHeading:		"Agent Account Search Result",
			
			searchInputHint:			"Search",
			
			searchAccountSearchHeading:	"Agent Account Search",
			searchUniqueIDLabel:		"Unique ID",
			searchUniqueIDHint:			"Unique ID",
			
			searchOwnerAgentLabel:		"Owner",
			searchOwnerAgentHint:		"Owner",
			
			searchSupplierAgentLabel:	"Supplier",
			searchSupplierAgentHint:	"Supplier",
			
			searchAccountFirstNameLabel:	"Name",
			searchAccountFirstNameHint:	"First name",
			searchAccountSurnameHint:	"Last name",
			
			searchAccountNumberLabel:	"Account number",
			searchAccountNumberHint:	"Account number",
			
			searchAccountStatusLabel:	"Status",
			searchAccountStatusHint:	"(any status)",
			
			searchAccountTierLabel:	"Agent Tier",
			searchAccountTierHint:	"(any tier)",
			
			searchAccountServiceClassLabel:	"Service Class",
			searchAccountServiceClassHint:	"(any service class)",
			
			searchAccountGroupLabel:	"Group",
			searchAccountGroupHint:	"(any group)",
			
			masterCreateAgentButton:	"Add Agent",
			
			userDetailsHeading:	"User Details",
			userDetailsDelete:	"Delete",
			userDetailsEdit:	"Edit",
			
			userDetailsAccountNumberLabel:	"accountNumber",
			userDetailsUserNameLabel:	"userName",
			userDetailsSurnameLabel:	"surname",
			userDetailsActivationDtLabel:	"activationDate",
			userDetailsLanguageLabel:	"preferredLanguage",
			
			userDetailsRolesHeading:	"Assigned Roles",
			userDetailsNoRolesHeading:	"There are currently no roles assigned",
			
			userDetailsRolesCommentPrefix:	"Click",
			userDetailsRolesCommentName:	"Modify",
			userDetailsRolesCommentSuffix:	"to assign roles",
			
			emptyUserHeading:	"Web User Details",
			
			emptyUserCommentGroup:	"Select item on adjascent pane to view details of User",
			
			emptyUserCommentAddPrefix:	"Click",
			emptyUserCommentAddName:	"Add",
			emptyUserCommentAddSuffix:	"to add a new User",
			
			transferTitlePrefix:	"Transfer to MSISDN",
			transferButton:			"Transfer",
			transferSuccess:		"Transfer of FCFA {{amount}} successful",
			
			transferAmountLabel:	"Amount",
			transferAmountHint:		"Amount",
			transferAmountError:	"Error message",
			performingTransfer:		"performing transfer ...",
			
			adjustTitlePrefix:	"Adjust Account",
			adjustTitleItem:		"MSISDN",
			
			adjustAmountLabel:	"Amount",
			adjustAmountHint:	"Amount",
			
			adjustBonusAmountLabel:	"Bonus Amount",
			adjustBonusAmountHint:	"Bonus Amount",
			
			adjustReasonLabel:	"Reason",
			adjustReasonHint:	"Reason",
			
			emptyTableMessage:	"No agent accounts found.",
			
			tableUniqueIDTitle: 			"Unique ID",
			tableAccountNumberTitle:		"Account #",
			tableMobileNumberTitle:			"Mobile Number",
			tableFirstNameTitle:			"Agent Name",
			tableDomainAccountNameTitle:	"Domain Name",
			tableSupplierNameTitle:			"Supplier Name",
			tableTierNameTitle:				"Tier",
			tableGroupNameTitle: 			"Group Name",
			tableBalanceTitle:				"Balance",
			tableBonusBalanceTitle:			"Trade Bonus<br/>Provision",
			tableCurrentStateTitle:			"Status",
			
			responseStateActive:			"Active",
			responseStateSuspended:			"Suspended",
			responseStateDeactivated:		"Deactivated",
			responseStatePermanent:			"Permanent",
			
			menuViewBtn:				"View ...",
  		menuTransferBtn:			"Transfer",
  		menuAdjustBtn:				"Adjustment",
  		menuReplenishBtn:			"Replenish",
  		menuPinResetBtn:			"Reset Pin",
  		menuPasswordResetBtn: 		"Reset Password",
  		menuEditBtn:				"Edit",
  		menuSuspendBtn:				"Suspend",
  		menuUnsuspendBtn:			"Unsuspend",
  		menuReactivateBtn:			"Reactivate",
  		menuDeactivateBtn:			"Deactivate",
  		
  		suspendAgentMessage:		"Suspend agent account?",
  		activateAgentMessage:		"Activate agent account?",
  		deactivateAgentMessage:		"De-activate agent account?",
  		
  		createAgentBtn:				"Create Agent",
  		updateAgentBtn:				"Update Agent",
  		createAgentTitle:			"Create Agent",
  		updateAgentTitle:			"Update Agent",
  		noAgent:					"(no supplier agent)",
  		noOwner:					"(no owner agent)",
  		selectGroupHint:			"(any group)",
  		selectServiceClassHint:		"(any service class)",
  		imsiUnlockTitle:			"Unlock IMSI for this agent?",
		confirmUssdLabel:			"Confirm",
		confirmUssdPrefix:			"Transactions",
  		sendBundleCommissionReportLabel: "Send Daily Bundle Commission Report",

		transferNotAllowedMessage:	"Transfer not allowed, no transfer rule available.",
		transferRulesListHeading:	"The following rules are defined between the respective tiers but cannot be used due to segmentation or temporal constraints:",
		transferIssues: {
			NO_RULES_FOUND: 		"No active transfer rules found",
			TARGET_GROUP_DIFFERS:	"{{rule}}: target group differs",
			SERVICE_CLASS_DIFFERS:	"{{rule}}: target service class differs",
			RULE_NOT_ALLOWED_ON:	"{{rule}}: not allowed on {{dateTime}}",
			RULE_NOT_ALLOWED_BEFORE:"{{rule}}: not allowed before {{dateTime}}",
			RULE_NOT_ALLOWED_AFTER:	"{{rule}}: not allowed after {{dateTime}}"
		}
	  },
	  transactions: {
			heading:			"Transactions",
			historyHeading:		"Transaction History",
			
			transactionNo:		"Transaction #",
			type:				"Type",
			amount:				"Amount",
			bonus:				"Bonus",
			charge:				"Charge",
			channel:			"Channel",
			time:				"Time",
			agentA:				"(A) Agent",
			msisdnA:			"(A) MSISDN",
			agentB:				"(B) Agent",
			msisdnB:			"(B) MSISDN",
			code:				"Code",
			reverseButton:		"Reverse Transaction",
			tdrViewHeading:		"Transaction Record View",
			bonusAmount:		"Bonus Amount",
			
			searchInputHint:	"Search",
			
			general:			"General",
			hostName:			"Hostname",
			startTime:			"Start Time",
			endTime:			"End Time",
			callerID:			"Caller ID",
			inboundTransactionID:	"Inbound transaciton ID",
			inboundSessionID:	"Inbound session ID",
			requestMode:		"Request mode",
			transferRuleName: 	"Transfer rule",
			transferRuleID : 	"Transfer Rule ID",
			buyerTradeBonusProvisionAmount:	"Trade Bonus provision",
			buyerTradeBonusPercentage:	"Trade Bonus %",
			returnCode:				"Return Code",
			lastExternalResultCode:	"Last external result code",
			rolledBack:			"Rolled back",
			followUp:			"Follow Up",
			originalID:			"Original Transaction ID",
			additionalInformation:	"Additional information",
			
			agent:				"Agent",
			ownerName: 			"Owner agent",
			msisdn:				"MSISDN",
			tierID:				"Tier ID",
			tierName:			"Tier Name",
			serviceClassID : 	"Classe de service ID",
			serviceClassName:	"Service class",
			groupID:	"Group ID",
			groupName:	"Group Name",
			areaID:		"Area ID",
			areaName:	"Area Name",
			imsi:		"IMSI",
			imei:		"IMEI",
			cellID:		"Cell ID",
			cellGroupID:	"Cell Group ID",
			cgi:		"CGI",
			cellGroupCode:	"Cell Group Code",
			balanceBefore:	"Balance before",
			balanceAfter:"Balance after",
			tradeBonusProvisionBefore:	"Trade bonus provision before",
			tradeBonusProvisionAfter:	"Trade bonus provision after",
			onHoldBalanceBefore: 		"On-hold balance before",
			onHoldBalanceAfter: 		"On-hold balance after",
			doesntInvolveASide:			"(this transaction does not involve an A-side)",
			doesntInvolveBSide:			"(this transaction does not involve an B-side)",

			transferBonusAmount: "Transfer bonus amount",
			transferBonusProfile: "Transfer bonus profile",
			
			searchTierHintA:	"(any tier for A)",
			searchTierHintB:	"(any tier for B)",
			searchGroupHintA: 	"(any group for A)",
			searchGroupHintB: 	"(any group for B)",
			
			transactionSearch:	"Transaction Search",
			msisdnAHint:		"MSISDN A (sender)",
			msisdnBHint:		"MSISDN B (recipient)",
			tranactionNumber:	"Transaction number",
			transactionType:	"Transaction type",
			transactionChannel:	"Transaction channel",
			transactionDate:	"Transaction date",
			transactionDateFrom:	"From date",
			transactionDateTo:	"To date",
			transactionAmount:	"Transaction amount",
			transactionBonusAmount:	"Transaction bonus amount",
			minAmount:			"Min amount",
			maxAmount:			"Max amount",
			minBonus:			"Min bonus",
			maxBonus:			"Max bonus",
			reason:				"Reason",
			fullReversal:		"Full Reversal",
			reasonHint:			"Reason ?",
			transactionTierA:	"(Any tier for A)",
			transactionTierB:	"(Any tier for B)",
			transactionTierGroupName: "Transaction tier",
			transactionFollowUp: "Follow-up only",
			transactionFollowUpNA: "Follow-up non-adjudicated only",
			relation: "Relation",
			
			requesterMSISDN: "Requester MSISDN",
			requesterType: "Requester type",
			requesterTypeWebUser: "Web User",
			requesterTypeAgent: "Agent",
			requesterTypeAgentUser: "Agent User",
			itemDescription: "Item Description",
			promotionName: "Promotion name",
			
			reverseModalTitle:	"Reverse <b>{{transactionTypeName}}</b> From <b>{{apartyName}}</b> To <b>{{bpartyName}}</b> (transaction no {{number}})",
			transactionSearchResult: "Transaction Search Result",
			
			tdrViewSectionBC:	"Transactions",
			tdrViewPageBC:		"TDR View",
			selectGroupHintA:	"(any group for A)",
			selectGroupHintB:	"(any group for B)",
			
			invalidSearchCriteriaTitle:	"<b>Invalid search criteria</b>",
			invalidSearchCriteriaText:	"At least one criterion must be selected to perform the search query."
	  },
	  
	  reports: {
	  	retailerPerformanceList: {
			
			heading: "Retailer Performance Reports",
			headingBC: "Retailer Performance Reports",

			reportName: "Report Name",
			reportDescription: "Report Description",
			reportSchedules: "Report Schedules",

			buttonLoadReport: "Load ...",
			buttonNewReport: "New Report ..."
		},

	  	retailerPerformance: {
	
			heading: "Retailer Performance Report Settings",
			headingBC: "Retailer Performance Report Settings",

			agentId: "Agent ID",	
			date: "Date",
			transactionType: "Transaction Type",
			transactionStatus: "Transaction Status",
			accountNumber: "Account Number",
			mobileNumber: "Mobile Number",
			imei: "IMEI",
			imsi: "IMSI",
			name: "Name",
			tierName: "Tier Name",
			tierNameHint: "(all tiers)",
			groupName: "Group Name",
			groupNameHint: "(all groups)",
			serviceClassName: "Service Class Name",
			serviceClassNameHint: "(all service classes)",
			ownerImsi: "Owner IMSI",
			ownerMobileNumber: "Owner Mobile Number",
			ownerName: "Owner Name",
			totalAmount: "Total Amount",
			totalBonus: "Total Bonus",
			transactionCount: "Transaction Count"
		},
	  	
		wholesalerPerformanceList: {
			
			heading: "Wholesaler Performance Reports",
			headingBC: "Wholesaler Performance Reports",

			reportName: "Report Name",
			reportDescription: "Report Description",
			reportSchedules: "Report Schedules",

			buttonLoadReport: "Load ...",
			buttonNewReport: "New Report ..."
		},

	  	wholesalerPerformance: {
	
			heading: "Wholesaler Performance Report",
			headingBC: "Wholesaler Performance Report",

			agentId: "Agent ID",	
			date: "Date",
			transactionType: "Transaction Type",
			transactionStatus: "Transaction Status",
			accountNumber: "Account Number",
			mobileNumber: "Mobile Number",
			imei: "IMEI",
			imsi: "IMSI",
			name: "Name",
			tierName: "Tier Name",
			tierNameHint: "(all tiers)",
			groupName: "Group Name",
			groupNameHint: "(all groups)",
			serviceClassName: "Service Class Name",
			serviceClassNameHint: "(all service classes)",
			ownerImsi: "Owner IMSI",
			ownerMobileNumber: "Owner Mobile Number",
			ownerName: "Owner Name",
			totalAmount: "Total Amount",
			totalBonus: "Total Bonus",
			transactionCount: "Transaction Count"
		},
			
	  	accountBalanceSummary: {
			heading: "Account Balance Summary Report",
			headingBC: "Account Balance Summary Report",
			
			tierType: "Tier Type",
			tierTypeHint: "(all tier types)",
			tierName: "Tier Name",
			tierNameHint: "(all tiers)",
			groupName: "Group Name",
			groupNameHint: "(all groups)",

			msisdn: "MSISDN",
			name: "Name",
			balance: "Balance",
			bonusBalance: "Bonus Balance",
			holdBalance: "Hold Balance",
			tierType: "Tier Type",
			tierName: "Tier Name",
			groupName: "Group Name"
		},
		
		accountBalanceSummaryList: {
			heading: "Account Balance Summary Reports",
			headingBC: "Account Balance Summary Reports",

			reportName: "Report Name",
			reportDescription: "Report Description",
			reportSchedules: "Report Schedules",
			
			buttonLoadReport: "Load ...",
			buttonNewReport: "New Report ..."
		},

	  	
		schedule: {
			headingAdd: "Add Schedule", 
			headingUpdate: "Update Schedule", 
			headingExecute: "Execute Schedule", 

			scheduleEnabled:	"Schedule enabled",
			description: "Description",
			descriptionHint: "Description",
			referenceDate: "Reference Date", 
			referenceDateHint: "YYYY-MM-DD", 
			period: "Frequency",
			periodHOUR: "Hourly",
			periodDAY: "Daily",
			periodWEEK: "Weekly (on Monday)",
			periodMONTH: "Monthly (on 1st day of the month)",
			timeOfDay: "Time of Day",
			timeOfDayHint: "Time of Day",
			timeInterval: "Generate Report Between",
			startTimeOfDay: "From time",
			startTimeOfDayHint: "From time",
			endTimeOfDay: "To time",
			endTimeOfDayHint: "To time",
			webUsers: "Recipients",
			webUserFilterHint: "filter users by name ...",
			recipientEmails: "Recipient Emails",
			recipientEmailsHint: "Recipient email address",
			addRecipientButton: "Add Recipient",
			emailToAgent:	"Email to Agent",

			deliveryChannels: "Delivery Channels",
			deliveryChannelEmail: "Email",
			deliveryChannelSms: "SMS",
			deliveryChannelNotSelected: "Select a delivery channel",

			createButton: "Save Schedule", 
			updateButton: "Save Schedule", 
			executeButton: "Execute Schedule", 
			cancelButton: "Cancel",

			scheduleExecuted: "<strong>SUCCESS:</strong> Schedule executed successfully",
			scheduleNotExecuted: "<strong>NOT EXECUTED:</strong> Schedule not executed",
			scheduleExecuteError: "<strong>FAILED:</strong> Failed to execute schedule",
			notExecutedReason: {
				BEFORE_TIME_OF_DAY: "<strong>NOT EXECUTED:</strong> Before Time of Day",
				BEFORE_START_TIME_OF_DAY: "<strong>NOT EXECUTED:</strong> Before Start Time of Day",
				AFTER_END_TIME_OF_DAY: "<strong>NOT EXECUTED:</strong> Before End Time of Day",
				BELOW_MINIMUM_SECONDS_AFTER_PERIOD: "<strong>NOT EXECUTED:</strong> Below minimum seconds after period"
			}
		},

		report: 				"Report",
		reportSchedule: 		"Report Schedule Instance",
		
		searchTimestamp: 		"Date Range",
		searchTransactionType: 	"Transaction Type",
		searchTransactionTypeHint: 	"Transaction Type",
		searchTransactionStatus:"Transaction Status",
		searchTransactionStatusHint:"Transaction Status",
		searchIncludeZeroBalance: "Include zero-balance agents",
		searchTierType: 		"Tier Type",
		searchTierTypeHint: 		"Tier Type",
		searchTierName: 		"Tier Name",
		searchTierNameHint: 		"Tier Name",
		searchGroupName: 		"Group Name",
		searchGroupNameHint: 		"Group Name",
		searchPeriod: "Period",
		searchDisposition: 			"Owner",
		searchDispositionOutbound: 	"A Party",
		searchDispositionInbound: 	"B Party",
		
		transactionTypeAll: 		"(all transaction types)",
		transactionTypeTransfer: 	"Transfer",
		transactionTypeSell: 		"Sell",
		transactionTypeSelfTopup: 	"Self-Topup",
		transactionTypeNonAirtimeDebit: 	"Non-Airtime Debit",
		transactionTypeNonAirtimeRefund: 	"Non-Airtime Refund",
		transactionTypeReverse: 	"Reverse",
		transactionTypeAdjust: 		"Adjust",
		transactionTypePromotionReward:	"Promotion Reward",
		transactionTypeAdjudicate:	"Adjudicate",

		transactionStatusAll: 		"(all transaction statuses)",
		transactionStatusSuccess: 	"Successful",
		transactionStatusFail: 		"Failed",
		
		messageSaveFailed: "Failed to save report (possible duplicate name)", 
		messageSaveSuccess: "Report saved successfully",

		saveName: "Report name",
		saveNameHint: "Report name (only required when saving)",
		saveDescription: "Report description",
		saveDescriptionHint: "Report description (only used when saving)",
		
		period: {
			today: "Today",
			yesterday: "Yesterday",
			thisWeek: "This week",
			lastWeek: "Last week",
			last30Days: "Last 30 days",
			thisMonth: "This month",
			lastMonth: "Last month",
			thisYear: "This year",
			lastYear: "Last year",
			custom: "Custom"
		}
	  },
	  
	  operations: {
		  
		  newPinTitle:		"Change your current PIN",
		  newPin:			"New PIN",
		  newPinHint:		"Enter new numeric PIN ({{minPinLength}} to {{maxPinLength}} Numbers)",
		  newPinHintExact:	"Enter new numeric PIN ({{pinLengthExact}} numbers)",
		  repeatPin:		"Repeat PIN",
		  repeatHint:		"Repeat PIN",
		  changePinBtn:		"Change PIN",
		  performPinChangeSpinText: "performing pin change ...",
		  pinChangeSuccees:	"You have successfully changed your PIN",

		  transferTitle:	"Transfer funds",
		  msisdn:			"Destination MSISDN",
		  msisdnHint:		"Enter mobile number to transfer funds to",
		  amount:			"Amount",
		  amountHint:		"Amount (in FCFA)",
		  transferBtn:		"Transfer",
		  performTransferSpinText: "performing transfer ...",
		  transferSuccess:		"Transfer of FCFA {{amount}} successful",
		  
		  selfTopupTitle:	"Perform Self-Topup",
		  performselfTopupSpinText: "performing self-topup ...",
		  selfTopupBtn:		"Perform Top-Up",
		  selfTopupSuccess:	"Self top-up of FCFA {{amount}} successful"
	  }
  },
  
  // Specify what other languages are supported to map from this file.
  // The other translation files should not contain these flags.
  'fr': true
});
