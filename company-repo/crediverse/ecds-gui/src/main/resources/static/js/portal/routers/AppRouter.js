define(['App', 'marionette', "BaseAppRouter"],
		function(App, Marionette, BaseAppRouter) {
   var AppRouter =  BaseAppRouter.extend({
		controller: null,
		//"viewUsers" must be a method in AppRouter's controller
		appRoutes: {
			"dashboard": 			"viewDashboard",
		    "profile": 				"viewProfile",
		   
			"accountList": 			"viewAccounts",
			"accountList*splat": 	"viewAccounts",

			"account/:id": 			"viewAccount",
			
			"transactionList": "viewTdrs",
			"transactionList*splat": "viewTdrs",
			"transaction/:id": "viewTdr",
		   
		   "retailerPerformanceReportList": "viewRetailerPerformanceReportList",
		   "retailerPerformanceReportList*splat": "viewRetailerPerformanceReportList",
		   "retailerPerformanceReport": "viewRetailerPerformanceReport",
		   "retailerPerformanceReport/:id*splat": "editRetailerPerformanceReport",
		   "wholesalerPerformanceReportList": "viewWholesalerPerformanceReportList",
		   "wholesalerPerformanceReportList*splat": "viewWholesalerPerformanceReportList",
		   "wholesalerPerformanceReport": "viewWholesalerPerformanceReport",
		   "wholesalerPerformanceReport/:id*splat": "editWholesalerPerformanceReport",
		   "accountBalanceSummaryReportList": "viewAccountBalanceSummaryReportList",
		   "accountBalanceSummaryReportList*splat": "viewAccountBalanceSummaryReportList",
		   "accountBalanceSummaryReport": "viewAccountBalanceSummaryReport",
		   "accountBalanceSummaryReport/:id*splat": "editAccountBalanceSummaryReport",
			
			"noperms":	"viewNoPermissions",
		   
			"*other": 	"defaultPage"
		},

		permissions: {
			"dashboard":		["Agent_View"],
			"accountList":		["Agent_View"],
			"accountSearch":	["Agent_View"],
			"transactionList":	["Transaction_View"],
			"noperms":			[]
		},
		
       	breadCrumbHome:	{
       		"dashboard":		["dashboard", 		"fa-dashboard"],
       		"accountList":		["agentAccounts", 	"fa-users"],
       		"accountSearch":	["transactions",	"fa-history"],
       		"transactionList":	["transactions", 	"fa-history"],
       		"noperms":			["", ""]
       	}		
   });
   
   return AppRouter;
});
