define(['underscore', 'App', 'backbone', 'marionette', 'utils/HandlebarHelpers', 'BaseFrontController', 'views/dashboard/DashboardView',
        'views/accounts/AgentAccountsView', 'views/accounts/AgentAccountView',
		'views/reports/RetailerPerformanceReportListView', 'views/reports/RetailerPerformanceReportView',
		'views/reports/WholesalerPerformanceReportListView', 'views/reports/WholesalerPerformanceReportView',
		'views/reports/AccountBalanceSummaryReportListView', 'views/reports/AccountBalanceSummaryReportView',
        'views/tdrs/TdrView', 'views/tdrs/TdrsView', 'views/NoPermissionView'
        ],
        
    function (_, App, Backbone, Marionette, HBHelper, BaseFrontController ,DashboardView,
    		AgentAccountsView, AgentAccountView,
			RetailerPerformanceReportListView, RetailerPerformanceReportView,
			WholesalerPerformanceReportListView, WholesalerPerformanceReportView,
			AccountBalanceSummaryReportListView, AccountBalanceSummaryReportView,
    		TdrView, TdrsView, NoPermissionView
    		) {

    var FrontController = BaseFrontController.extend({
    		
        // DashBoard
        viewDashboard: function() {
        	this.loadView(DashboardView);
        },
        
        viewProfile: function() {
        	this.loadView(DashboardView);
        },
        
        // Agent views
        viewAccounts: function() {
        	this.loadView(AgentAccountsView);
        },
        
        viewAccount: function(id) {
        	this.loadView(AgentAccountView, {id: id})
        },
        
		viewRetailerPerformanceReportList: function() {
        	this.loadView(RetailerPerformanceReportListView);
		},
		
		viewRetailerPerformanceReport: function() {
        	this.loadView(RetailerPerformanceReportView);
		},
		
		editRetailerPerformanceReport: function(id) {
        	this.loadView(RetailerPerformanceReportView, {id: id});
		},
		
		viewWholesalerPerformanceReportList: function() {
        	this.loadView(WholesalerPerformanceReportListView);
		},

		viewWholesalerPerformanceReport: function() {
        	this.loadView(WholesalerPerformanceReportView);
		},
		
		editWholesalerPerformanceReport: function(id) {
        	this.loadView(WholesalerPerformanceReportView, {id: id});
		},
		
		viewAccountBalanceSummaryReportList: function() {
        	this.loadView(AccountBalanceSummaryReportListView);
		},
		
		viewAccountBalanceSummaryReport: function() {
        	this.loadView(AccountBalanceSummaryReportView);
		},
		
		editAccountBalanceSummaryReport: function(id) {
        	this.loadView(AccountBalanceSummaryReportView, {id: id});
		},
		
        viewTdrs: function() {
        	this.loadView(TdrsView);
        },
        
        viewTdr: function(id) {
        	this.loadView(TdrView, {id: id});
        },
        
        viewNoPermissions: function() {
        	this.loadView(NoPermissionView);
        }
    });
    
    return FrontController;
});
