define({
    "root": {   	
      // Don't translate languageName
      languageName: 	"english",
  	  
  	  // For Items where no value available
  	  notSet:			"[Not set]",
  	  notSetDash:		"-",
  	  
  	  numberFormat: {
  		  rounding: "none",
//  		  rounding: 'up',
//  		  rounding: 'truncate',
  		  fractionalDigits: 2,
  		  currency:	"XAF",
  		  currencyDisplay: "Fcfa"
  	  },
  	  
  	  // Common labels used throughout UI
  	  global: {
  		  loading:		"Retrieving Content ...",
  		  logout:		"Logout",
  		  profile : 	"My Details",
  		  homeBC:		"Agent Accounts",
  		  english: 		"English",
  		  french:  		"French",
  		  
  		  deleteTitle: 	"Confirm {{what}} Deletion",
  		  deleteMsg:   	"You are about to delete {{what}} <b>{{name}}</b> ({{description}})",
  		  deleteMsgNoDesc : "You are about to delete {{what}} <b>{{name}}</b>",
  		  deleteError:	"Failed to delete {{what}} <b>{{name}}</b>",
  		  deleteBtn:	"Delete",
  		  deleteCancel:	"Cancel",
  		  
  		  action:			"Action",
  		  editBtn:			"Edit",
  		  saveBtn:			"Save",
  		  importBtn:		"Import ...",
  		  exportBtn:		"Export ...",
  		  cancelBtn:		"Cancel",
  		  closeBtn:		"Close",
  		  searchBtn:		"Search",
		  scheduleBtn: 	"Schedule",
  		  hideBtn:		"Hide",
  		  clearBtn:		"Clear",
  		  resetBtn:		"Reset",
  		  retrieveRecordCount: "Retrieve Record Count",
  		  retrieveRecordCountHint: "When selected, all available data will be scanned to determine the total number of matching transactions. This may slow the search response significantly.",
  		  includeQueryTransactions: "Include Queries",
  		  includeQueryTransactionsHint: "When selected, 'query' type transactions are included in the result. This may increase the result set significantly.",
  		  advancedSearchBtn:		"Advanced Search ...",
  		  reverseBtn:		"Reverse",
  		  adjudicateBtn:	"Adjudicate",
  		  authorizeBtn:		"Authorize",
  		  authorizeRequestBtn:		"Request Authorisation",
  		  noRulesDefined:	"No Rules Defined",
  		  transferBtn: 		"Transfer",
  		  okBtn:			"OK",
		  uniqueID: 		"Unique ID",

		  selectAllBtn:		"Select All",
		  clearAllBtn:		"Clear All",
		  expandAllBtn:		"Expand All",
		  collapseAllBtn:	"Collapse All",
  		  
  		  code:					"Code",
  		  name:					"Name",
  		  description:			"Description",
  		  details:			"Details",
  		  state:				"State",
  		  stateHint:			"(select state)",
  		  tier:					"Tier",
  		  maxTransactionAmount:	"Max Transaction Amount",
  		  maxDailyCount:		"Max Daily Count",
  		  maxDailyAmount:		"Max Daily Amount",
  		  maxMonthlyCount:		"Max Monthly Count",
  		  maxMonthlyAmount:		"Max Monthly Amount",
  		  
  		  maxTransactionAmountHint:	"Maximum Transaction Amount",
  		  maxDailyCountHint:		"Maximum Daily Count",
  		  maxDailyAmountHint:		"Maximum Maximum Daily Amount Limit",
  		  maxMonthlyCountHint:		"Maximum Monthly Count",
  		  maxMonthlyAmountHint:		"Maximum Monthly Amount Limit",
  		  
  		  viewBtn:				"View",
  		  menuViewBtn:			"View ...",
  		  previewBtn:			"Preview",
  		  requiredNote:			"<strong>Note: </strong> Fields marked with <span class='glyphicon glyphicon-asterisk required-note' aria-hidden='true'></span> are mandatory",
  		  notAvailable:			"(not available)",
  		  none:					"(none)",
  		  inbound:				"Inbound",
  		  outbound:				"Outbound",
  		  
  		  noPermissionHead:		"permission",
  		  noPermissionContent:	"permission",
  		  status:				"Status"
  	  },
  	  
  	  commonUtils: {
  		  undefinedNumber: "-",
  		  exportOkMessage: 			"Using the current search resulted in no records to export",
  		  exportErrorMessage: 		"Export failed due to a communications issue.  Please try again later.",
  		  deleteOptionsMessage: 	"options.data needs to be specified for delete!",
  		  deleteUrlMessage:			"A URL needs to be specified for delete!",
  		exportDialogTitle:	"Export in progress",
  		exportDialogMessage:	"Please be patient as some exports take a little time to process."
  	  },
  	  commonPartials: {
  		  undefinedNumber: ""
  	  },
  	  
  	  enums: {
  		  state: {
  			  active:			"Active",
  			  suspended:		"Suspended",
  			  deactivated:		"Deactivated",
  			  permanent:		"Permament",
  			  inactive:			"Inactive",
  			  notconfigured:	"Not Configured",
  			  unavailable: 		"Unavailable"
  		  },
  		 
  		 tierType: {
  			  any: 				"(any tier type)",
  			  root: 			"Root",
  			  store: 			"Store",
  			  wholesaler: 		"Wholesaler",
  			  retailer:			"Retailer",
  			  subscriber:		"Subscriber"
  		 },
  		 
  		 titleType: {
  			 mr:				"Mr",
  			 miss:				"Miss",
  			 mrs:				"Mrs",
  			 dr:				"Dr",
  			 prof:				"Prof"
  		 },
  		 
  		 langType: {
  			 en:	"English",
  			 fr:	"French"
  		 },
  		 
  		 sexType: {
  			 m:		"Male",
  			 f:		"Female",
  			 o:		"Other"
  		 },
  		 
  		 action: {
  			 all:				"(All operations)",
  		 	 create:			"Create",
  			 update:			"Update",
  			 delete:			"Delete",
  			 unknown:			"Unknown"
  		 },
  		 
  		 yesNo: {
  			 no:				"No",
  			 yes:				"Yes"
  		 },
  		 
  		 transactionType: {
  			ALL:				"(all transaction types)",
  			REPLENISH:			"Replenish",
  			TRANSFER:			"Transfer",
  			SELL:				"Sell",
  			NON_AIRTIME_DEBIT:		"Non-Airtime Debit",
  			NON_AIRTIME_REFUND:		"Non-Airtime Refund",
  			REGISTER_PIN:		"Register PIN",
  			CHANGE_PIN:			"Change PIN",
  			BALANCE_ENQUIRY:	"Balance Enquiry",
  			SELF_TOPUP:			"Self Top-up",
  			TRANSACTION_STATUS_ENQUIRY:		"Transaction Status Enquiry",
  			LAST_TRANSACTION_ENQUIRY:		"Last Transaction Enquiry",
  			ADJUST:				"Adjustment",
  			SALES_QUERY:		"Sales Query",
  			DEPOSITS_QUERY:		"Deposits Query",
  			REVERSE:			"Reversal",
  			REVERSE_PARTIALLY:	"Reversal (Partial)",
  			PROMOTION_REWARD:	"Promotion Reward",
  			ADJUDICATE:			"Adjudicate"
  		 },

		 transactionFollowUp: {
		 	IGNORE: "(ignore follow-up indicator)", 
			ALL: "All",
			PENDING: "Pending Adjudication",
			ADJUDICATED: "Adjudicated" 
		 },

		 transactionRelation: {
		 	ownerA: "Owner of A-Side",
		 	ownerB: "Owner of B-Side",
		 	own: "Own Transactions",
		 	ownA: "Own Outbound Transactions",
		 	ownB: "Own Inbound Transactions",
		   	all: "All transactions",
		   	inbound: "Inbound Transactions",
		   	outbound: "Outbound Transactions"
		 },
  		 
		 transactionTypeCode: {
  			RP:	"Replenish",
  			TX:	"Transfer",
  			SL:	"Sell",
  			ND:	"Non-Airtime Debit",
  			NR:	"Non-Airtime Refund",
  			PR:	"Register PIN",
  			CP:	"Change PIN",
  			BE:	"Balance Enquiry",
  			ST:	"Self Top-up",
  			TS:	"Transaction Status Enquiry",
  			LT:	"Last Transaction Enquiry",
  			AJ:	"Adjustment",
  			SQ:	"Sales Query",
  			DQ:	"Deposits Query",
  			FR: "Reversal",
  			PA:	"Reversal (Partial)",
  			RW:	"Promotion Reward",
			AD: "Adjudicate"
  		 },

		 batchType: {
			ALL: "(All batch types)",
			user: "Web-Users",
			cell: "Cells",
			area: "Areas",
			tier: "Tiers",
			sc: "Service Classes",
			account: "Accounts",
			group: "Groups",
			rule: "Transfer Rules",
			adjust: "Adjustments",
			dept: "Departments",
			prom: "Promotions"
		 },
  		 
  		 channel: {
  			 ALL:				"(all transaction channels)",
  			 USSD:				"USSD",
  			 SMS:				"SMS",
  			 API:				"API",
  			 MOBILE:			"Mobile App",
  			 WEB:				"Web UI",
  			 B:					"Batch"
  		 },
  		 
  		 dataType: {
  			 all:				"(All data types)",
  			 agent:				"Agent",
  			 auditEntry:		"Audit Entry",
  			 batch:				"Batch",
  			 configuration:		"Configuration",
  			 group:				"Group",
  			 permission:		"Permission",
  			 role:				"Role",
  			 serviceClass:		"Service Class",
  			 tier:				"Tier",
  			 transferRule:		"Transfer Rule",
  			 webUser:			"Web User"
  		 },
  		 
  		 loggingLevel:	{
  			 off:				"None",
  			 error:				"Error",
  			 warn:				"Warning",
  			 info:				"Information",
  			 debug:				"Debugging",
  			 trace:				"Trace",
  			 all:				"Everything"
  		 },
  		 
  		 loggingType: {
  			 file:				"File",
  			 console:			"Console"
  		 },
  		 
  		taskType: {
  			transfer:			"Transfer",
  			replenish:			"Replenish",
  			adjustment:			"Adjustment",
  			reversal:			"Reversal",
  			partialReversal:	"Partial Reversal"
  		},
  		
  		permissions: {
  			May_Add_Agents : "May Add Agents",
  			May_Configure_Agent_Parameters : "May Configure Agent Parameters",
  			May_Delete_Agents : "May Delete Agents",
  			May_Update_Agents : "May Update Agents",
  			May_reset_IMSI_Lockout : "May reset IMSI Lockout",
  			May_Download_CSV_Batches : "May Download CSV Batches",
  			May_Upload_CSV_Batches : "May Upload CSV Batches",
  			May_Add_Companies : "May Add Companies",
  			May_Delete_Companies : "May Delete Companies",
  			May_Update_Companies : "May Update Companies",
  			May_Add_Departments : "May Add Departments",
  			May_Delete_Departments : "May Delete Departments",
  			May_Update_Departments : "May Update Departments",
  			May_Add_Groups : "May Add Groups",
  			May_Delete_Groups : "May Delete Groups",
  			May_Update_Groups : "May Update Groups",
  			May_Update_Permissions : "May Update Permissions",
  			May_Add_Roles : "May Add Roles",
  			May_Delete_Roles : "May Delete Roles",
  			May_Update_Roles : "May Update Roles",
  			May_Add_ServiceClasses : "May Add ServiceClasses",
  			May_Delete_ServiceClasses : "May Delete ServiceClasses",
  			May_Update_ServiceClasses : "May Update ServiceClasses",
  			May_Add_Tiers : "May Add Tiers",
  			May_Delete_Tiers : "May Delete Tiers",
  			May_Update_Tiers : "May Update Tiers",
  			Configure_Transaction_Status_Enquiries : "Configure Transaction Status Enquiries",
  			May_Adjust : "May Adjust",
  			May_Authorise_Adjust : "May Authorise Adjust",
  			May_Authorise_Replenish : "May Authorise Replenish",
  			May_Authorise_Reverse : "May Authorise Reverse",
  			May_Authorise_Transfer_from_Root_Account : "May Authorise Transfer from Root Account",
  			May_Configure_Adjustments : "May Configure Adjustments",
  			May_Configure_Balance_Enquiries : "May Configure Balance Enquiries",
  			May_Configure_Batch_Processing : "May Configure Batch Processing",
  			May_Configure_Pin_Change : "May Configure Pin Change",
  			May_Configure_Pin_Registration : "May Configure Pin Registration",
  			May_Configure_Replenishment : "May Configure Replenishment",
  			May_Configure_Reversals : "May Configure Reversals",
  			May_Configure_Sales : "May Configure Sales",
  			May_Configure_Transactions : "May Configure Transactions",
  			May_Configure_Transfers : "May Configure Transfers",
  			May_Replenish : "May Replenish",
  			May_Reverse : "May Reverse",
  			May_Sell : "May Sell",
  			May_Transfer : "May Transfer",
  			May_Transfer_from_Root_Account : "May Transfer from Root Account",
  			May_Add_Transfer_Rules : "May Add Transfer Rules",
  			May_Delete_Transfer_Rules : "May Delete Transfer Rules",
  			May_Update_Transfer_Rules : "May Update Transfer Rules",
  			May_Configure_Web_UI : "May Configure Web UI",
  			May_Add_WebUsers : "May Add Web-Users",
  			May_Configure_WebUsers : "May Configure Web-Users",
  			May_Delete_WebUsers : "May Delete Web-Users",
  			May_Update_WebUsers : "May Update Web-Users",
  			May_Add_Workflow_Items : "May Add Workflow Items",
  			May_Configure_Workflow : "May Configure Workflow",
  			May_Delete_Workflow_Items : "May Delete Workflow Items",
  			May_Update_Workflow_Items : "May Update Workflow Items"
  		},
  		
  		period: {
  			perDay:				"per Day (24 hour period)",
  			perWeek:			"per Week",
  			perMonth:			"per Month",
  			perCalendarDay:		"per Calendar Day",
  			perCalendarWeek:	"per Calendar Week",
  			perCalendarMonth:	"per Calendar Month"
  		},
  		
  		returncode: {
  			ALL:                  "(All status codes)",
  			ACC_NOT_FOUND:		  "Account Not Found",
  			SUCCESS:			  "Success",
  			IN_PROGRESS:		  "In Progress",
  			REFILL_FAILED:		  "Refill Failed",
			INVALID_RECIPIENT: 	  "Invalid Recipient",
			TEMPORARY_BLOCKED:	  "Temporary Blocked",
			REFILL_DENIED: 		  "Refill Denied",
			REFILL_NOT_ACCEPTED:  "Refill Not Accepted", 
			BUNDLE_SALE_FAILED:   "Bundle Sale Failed", 
			TECHNICAL_PROBLEM: 	  "Technical Problem",
			INVALID_CHANNEL:      "Invalid Channel",
			FORBIDDEN:            "Forbidden",
			NO_TRANSFER_RULE:     "No Transfer Rule",
			INTRATIER_TRANSFER:	  "Intra-tier disallowed",
			NO_LOCATION:          "No Location",
			WRONG_LOCATION:       "Wrong Location",
			CO_AUTHORIZE:         "Co-authorize",
			INSUFFICIENT_FUNDS:   "Insufficient Funds",
			INSUFFICIENT_PROVISN: "Insufficient Provision",
			DAY_COUNT_LIMIT:      "Daily Count Limit",
			DAY_AMOUNT_LIMIT:     "Daily Amount Limit",
			MONTH_COUNT_LIMIT:    "Monthly Count Limit",
			MONTH_AMOUNT_LIMIT:   "Monthly Amount Limit",
			MAX_AMOUNT_LIMIT:     "Max Amount Limit",
			ALREADY_REGISTERED:   "Already Registered",
			ALREADY_ADJUDICATED:  "Already Adjudicated",
			NOT_REGISTERED:       "Not Registered",
			INVALID_STATE:        "Invalid State",
			NOT_ELIGIBLE:         "Not Eligible",
			PIN_LOCKOUT:          "Pin Lockout",			
			NOT_SELF:             "Not Self",
			TX_NOT_FOUND:         "Transaction Not Found",
			IMSI_LOCKOUT:         "IMSI Lockout",
			INVALID_AGENT:        "Invalid Agent",
			INVALID_AMOUNT:       "Invalid Amount",
			INVALID_TRAN_TYPE:    "Invalid Transaction Type",
			INVALID_BUNDLE:       "Invalid Bundle",
			ALREADY_REVERSED:     "Already Reversed",
			NOT_WEBUSER_SESSION:  "Not Webuser Session",
			CO_SIGN_ONLY_SESSION: "Co-signatory Only Session",
			SESSION_EXPIRED:      "Session Expired",
			TIMED_OUT:            "Timed-out",
			REFILL_BARRED:        "Refill Barred",
			NO_IMSI:              "No IMSI",
			INVALID_VALUE:        "Invalid Value",
			TOO_SMALL:            "Too Small",
			TOO_LARGE:            "Too Large",
			TOO_LONG:             "Too Long",
			TOO_SHORT:            "Too Short",
  			OTHER_ERROR:		  "General Failure",
  			PASSWORD_LOCKOUT:	  "Password Lockout",
  			INVALID_PASSWORD:	  "Invalid username and/or password",
  			INVALID_PIN:		  "Invalid username and/or pin",
  			HISTORIC_PASSWORD:	   "You have set this password before, please choose a new password"
  				
  		},
  		transactionOrdering: {
  			number: "Number",
  			time: "Time",
  			status: "Status"
  		},
  		authenticationMethod : {
  			password : "API / Password",
  			pin : "Pin",
  			external : "Domain Account"
  		},
  		usernameType : {
  			A : "ECDS Username",
  			X : "Domain Account",
  			P : "Domain Account"
  		},  		
  		usernameTypeComment : {
  			A : "ECDS Username",
  			X : "Domain controller (LDAP) Username",
  			P : "Not applicable for pin authentication"
  		}
  	  },
  	  
  	  violations: {
  	  	formGeneralMessage: "The following errors prevented operation from completing",
		errorIdMsg:			" (Error ID: {{correlationID}})",
		fieldViolations:	"See fields for specific errors",
		
		cannotBeEmpty: 		"A value is required",
		cannotHaveValue: 	"No value allowed",
		invalidValue: 		"Invalid value provided \"{{value}}\"",
		invalidValueGeneral:"Invalid value provided",
		cantBeChanged: 		"Value cannot me changed",
		notSame: 			"Value does not match {{match}}",
		notSameGeneral:		"Values provided do not match",
		tooSmall: 			"Value must be larger than {{min}}",
		tooSmallGeneral:	"Value provided is too small",
		tooLong: 			"Value must be shorter than {{maxSize}} characters",
		tooLongGeneral:		"Value provided is too long",
		tooShort: 			"Value must be longer than {{minSize}} charaters",
		recursive: 			"Recursive condition created",
		BAD_REQUEST:		"Technical Error",
		NOT_ACCEPTABLE:		"Technical Error",
		cannotBeChanged:	"Value cannot be modified",
		tooLarge:			"Value too large",
		failedToSave:		"Failed to save, possible duplicate",
		failedToDelete:		"Failed to Delete: {{item}} in use",
		 resourceInUse:		"Cannot delete as the item is in use",
		notFound:			"Value not found",
		forbidden:			"You are not allowed perform this operation",
		ambiguous:			"Ambiguous rule given",
		cannotAdd:			"Add forbidden",
		cannotDelete:		"Delete forbidden",
		cannotDeleteSelf:	"You cannot delete yourself",
		unauthorized:		"Unauthorized operation",
		tampered:			"Posible data tampering prevented operation",
		dailyCountLimit:	"Daily count limit exceeded",
		dailyAmountLimit:	"Daily amount limit exceeded",
		monthlyCountLimit:	"Monthly count limit exceeded",
		monthlyAmoutLimit:	"Monthly amount limit exceeded",
		insufficientFunds:	"Insufficient funds",
		insufficientProvision:	"Insufficient bonus provision balance",
		alreadyRegisterd:	"Already registered",
		notRegistered:		"Registration required",
		invalidPin:			"Invalid pin provided",
		invalidChannel:		"Invalid channel given",
		invalidState:		"Operation in invalid state",
		unknown:			"Unknown Error",
		noFieldMappingConsoleMsg:		"{{msg}} ({{field}})",
		noFieldMappingUserMsg:	"{{msg}} (Error ID: {{correlationID}})",
		modalOperationFailMessage:		"{{msg}} (Error ID: {{correlationID}})",
		dateOfBirthInFuture: "Birth date must be in the past",
		expirationDatePassed: "Expiration date must be not be in the past",
		
		invalidDir:			"Invalid Directory",
		filePermission:		"Unable to write to File",
		invalidConfiguration:	"Invalid configuration provided: \"{{value}}\"",
		
		emptyPin:			"Empty PIN",
		pinTooShort:		"PIN too short",
		pinTooLong:			"PIN too long",
		pinNotNumeric:		"PIN not numeric",
		repeatedPin:		"Repeated PIN",
		
		REFILL_FAILED:		"Refill Failed (Technical error)",
		NO_TRANSFER_RULE:	"You are unable to transfer to this agent at present",
		INTRATIER_TRANSFER: "Intra-tier transfer is not allowed on this tier",
		MAX_AMOUNT_LIMIT:	"You have exceeded the maximum allowed Amount",
		NOT_SELF:			"Cannot transfer to yourself",	
		CANT_SET_AS_PERMANENT:	"May not set as Permanent Type",
		CO_AUTHORIZE:		"Co-authorization Failed",
		duplicateValue:		"Duplicate value found"
  	  },
  	  
  	  noPermissions: {
  		  heading:		"Access Denied",
  		  content:		"Please contact a Systems Administrator to rectify"
  	  },
      changePasswordDialog: {
    	changePassword:		"Change Password",
		resetPassword:	 	"Reset Password",
		passwordChangeSuccess:	"You have successfully changed your password",
		passwordResetSuccess:	"Password reset successful, the new password was sent to {{email}}",
		currentPassword: "Current Password",
		newPassword:	"New Password",
		repeatPassword:	"Confirm Password",
		passwordRulesLabel: "Password Rules",
		passwordRules1: "One lower case",
		passwordRules2: "One upper case",
		passwordRules3: "One number",
		passwordRules4: "One special character",
		passwordRules5: "Length more than {{minPinLength}}",
		
		pinResetHeading:			"Confirm PIN Reset for <b>{{name}}</b>",
  		pinResetMessage:			"Reset PIN for <b>{{name}}</b> with mobile number <b>{{msisdn}}</b> ?",
  		pinResetHeadingSuccess:		"PIN reset for <b>{{name}}</b> Successful",
		pinResetMessageSuccess:		"PIN reset for agent account with mobile number <b>{{msisdn}}</b> was successful.",
			
		passwordResetHeading:		"Confirm password reset for <b>{{name}}</b>",
		passwordResetMessage:		"Reset password for <b>{{name}}</b> with email address <b>{{email}}</b> ?",
		passwordResetHeadingSuccess:"Password reset for <b>{{name}}</b> Successful",
		passwordResetMessageSuccess:"Password reset successful, the new password was sent to {{email}}",
		
		missingEmail: "There is no email address associated with <b>{{name}}</b>, please the update details."
	  }
    },
    "fr": true
});
