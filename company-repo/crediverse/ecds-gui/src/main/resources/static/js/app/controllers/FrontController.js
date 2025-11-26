define(['underscore', 'App', 'backbone', 'marionette', 'utils/HandlebarHelpers', 'BaseFrontController',
        'views/users/UserMasterView', 'views/users/UserView', 'views/users/ProfileView', 'views/departments/ManageDepartmentsView',
        'views/roles/RoleManagerView', 'views/roles/PermissionView',
        'views/DashboardView', 'views/rules/TransferRulesView',
        'views/tiers/TierMasterView', 'views/tiers/TierRulesView',
        'views/groups/ManageGroupsView', 'views/groups/GroupView',
        'views/accounts/AgentAccountsView', 'views/accounts/AgentAccountView', 'views/accounts/AgentUserView',
		'views/tdrs/TdrsView', 'views/tdrs/TdrSearchView', 'views/tdrs/TdrView',
		'views/auditlog/AuditLogView', 'views/auditlog/AuditLogSearchView', 'views/auditlog/AuditEntryView',
        'views/serviceclasses/ServiceClassMasterView', 'views/serviceclasses/ServiceClassView',       
        'views/areas/ManageAreasView', 'views/areas/AreaView',
        'views/cells/ManageCellsView', 'views/cells/CellView',        
        'views/cellgroups/ManageCellGroupsView', 
        'views/config/AdjustmentsConfigView', 'views/config/AgentConfigView', 'views/config/BalanceEnquiriesConfigView','views/config/ChangePinsConfigView',
        'views/config/DepositsQueryConfigView','views/config/LastTransactionEnquiriesConfigView', 'views/config/RegisterPinsConfigView',
        'views/config/AnalyticsConfigView', 'views/config/GeneralConfigView', 'views/config/MobileNumberFormatView',
        'views/config/ReplenishConfigView',
        'views/config/RewardsConfigView', 'views/config/SalesConfigView', 'views/config/SalesQueryConfigView', 'views/config/SelfTopUpsConfigView', 'views/config/TransactionsConfigView',
        'views/config/TransactionStatusEnquiriesConfigView', 'views/config/TransfersConfigView', 'views/menuconfig/UssdMenuConfigView',
        'views/config/BatchConfigView', 'views/config/ReversalConfigView', 'views/config/AdjudicationConfigView', 'views/config/WebUsersConfigView',
        'views/batch/ManageBatchProcessingView', 'views/batch/BatchHistoryView', 'views/batch/BatchView',
        'views/msisdnrecycle/MsisdnRecyclingUploadView', 'views/msisdnrecycle/MsisdnRecyclingSubmitView',
        'views/tasks/TaskListView', 'views/tasks/TaskSearchView',
        'views/config/WorkflowConfigView', 'views/config/ReportingConfigView', 'views/config/BundleSalesConfigView',
        'views/promotions/PromotionsManagerView', 'views/promotions/PromotionView', 'views/bundles/BundlesManagerView',
        'views/analytics/AnalyticsView',
        'views/reports/RetailerPerformanceReportListView', 'views/reports/RetailerPerformanceReportView', 
		'views/reports/WholesalerPerformanceReportListView', 'views/reports/WholesalerPerformanceReportView', 
		'views/reports/SalesSummaryReportListView', 
		'views/reports/DailyGroupSalesReportListView', 'views/reports/DailyGroupSalesReportView',
		'views/reports/MonthlySalesPerformanceReportListView', 'views/reports/MonthlySalesPerformanceReportView',
		'views/reports/DailySalesByAreaReportListView', 'views/reports/DailySalesByAreaReportView',
		'views/reports/AccountBalanceSummaryReportListView', 'views/reports/AccountBalanceSummaryReportView',
        'views/NoPermissionView',
		'views/testinfo/TestInfoView'
        ],
        
    function (_, App, Backbone, Marionette, HBHelper, BaseFrontController,
    		UserMasterView, UserView, ProfileView, ManageDepartmentsView,
    		RoleManagerView, PermissionView,
    		DashboardView, TransferRulesView,
    		TierMasterView, TierRulesView,
    		ManageGroupsView, GroupView,
    		AgentAccountsView, AgentAccountView, AgentUserView,
			TdrsView, TdrSearchView, TdrView,
			AuditLogView, AuditLogSearchView, AuditEntryView,
    		ServiceClassMasterView, ServiceClassView,
    		ManageAreasView, AreaView, ManageCellsView, CellView, ManageCellGroupsView,
    		//AuditLogView,
    		AdjustmentsConfigView, AgentConfigView, BalanceEnquiriesConfigView, ChangePinsConfigView,
    		DepositsQueryConfigView, LastTransactionEnquiriesConfigView, RegisterPinsConfigView,
    		AnalyticsConfigView, GeneralConfigView, MobileNumberFormatView,
    		ReplenishConfigView,
    		RewardsConfigView,SalesConfigView, SalesQueryConfigView, SelfTopUpsConfigView, TransactionsConfigView,
    		TransactionStatusEnquiriesConfigView, TransfersConfigView, UssdMenuConfigView,
    		BatchConfigView, ReversalConfigView, AdjudicationConfigView, WebUsersConfigView,
    		ManageBatchProcessingView, BatchHistoryView, BatchView,
    		MsisdnRecyclingUploadView, MsisdnRecyclingSubmitView,
    		TaskListView, TaskSearchView,
    		WorkflowConfigView, ReportingConfigView, BundleSalesConfigView,
    		PromotionsManagerView, PromotionView, BundlesManagerView,
    		AnalyticsView,
			RetailerPerformanceReportListView, RetailerPerformanceReportView,
			WholesalerPerformanceReportListView, WholesalerPerformanceReportView,
			SalesSummaryReportListView,
			DailyGroupSalesReportListView, DailyGroupSalesReportView,
			MonthlySalesPerformanceReportListView, MonthlySalesPerformanceReportView,
			DailySalesByAreaReportListView, DailySalesByAreaReportView,
			AccountBalanceSummaryReportListView, AccountBalanceSummaryReportView,
    		NoPermissionView,
    		TestInfoView
    		) {

    var FrontController = BaseFrontController.extend({
    	
    	initialize:function (options) {
        	App.vent.listenTo(App.vent, 'application:replenishcomplete', function(ev) {
        		self.defaultPage();
        	});
        	
        	this.listenTo(App.vent, 'permissions:view', function(){
        		var dialog = new PermissionView();
        	});
        	
    	},

        // Default View
        viewDashboard: function() {
        	this.loadView(DashboardView);
        },
        
        viewTestInfo: function() {
        	this.loadView(TestInfoView);
        },

        viewBatchProcessing: function() {
        	this.loadView(ManageBatchProcessingView);
        },
        
		viewBatchHistory: function() {
        	this.loadView(BatchHistoryView);
        },
        
        viewBatch: function(id) {
        	this.loadView(BatchView, {id: id});
        },  
        
        viewMsisdnRecyclingUpload: function(id) {
        	this.loadView(MsisdnRecyclingUploadView);
        },
        
        viewMsisdnRecyclingSubmit: function(id) {
			this.loadView(MsisdnRecyclingSubmitView);
		},

		viewAnalytics: function() {
        	this.loadView(AnalyticsView);
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
		
		viewSalesSummaryReportList: function() {
        	this.loadView(SalesSummaryReportListView);
		},
		
		viewDailyGroupSalesReportList: function() {
        	this.loadView(DailyGroupSalesReportListView);
		},
		
		viewDailyGroupSalesReport: function() {
        	this.loadView(DailyGroupSalesReportView);
		},
		
		editDailyGroupSalesReport: function(id) {
        	this.loadView(DailyGroupSalesReportView, {id: id});
		},

		viewMonthlySalesPerformanceReportList: function() {
        	this.loadView(MonthlySalesPerformanceReportListView);
		},

		viewMonthlySalesPerformanceReport: function(params) {
        	this.loadView(MonthlySalesPerformanceReportView, {params: params});
		},

		editMonthlySalesPerformanceReport: function(id) {
        	this.loadView(MonthlySalesPerformanceReportView, {id: id});
		},

		viewDailySalesByAreaReportList: function() {
        	this.loadView(DailySalesByAreaReportListView);
		},

		viewDailySalesByAreaReport: function(params) {
        	this.loadView(DailySalesByAreaReportView, {params: params});
		},

		editDailySalesByAreaReport: function(id) {
        	this.loadView(DailySalesByAreaReportView, {id: id});
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
		
		viewHourlySmsFlash: function() {
		},
        
        viewUsers: function() {
        	this.loadView(UserMasterView);
        },
        
        viewDepartments: function() {
        	this.loadView(ManageDepartmentsView);
        },
        
        viewProfile: function(id) {        	
        	this.loadView(ProfileView);
        },
        
        viewUser: function(id) {        	
        	this.loadView(UserView, {id: id});
        },
        
        viewRoles: function() {
        	this.loadView(RoleManagerView);
        },
        
        // Not sure where called from ?!?
        viewPermissions: function() {
        	this.loadView(PermissionView);
        },
        
        viewGroups: function() {
        	this.loadView(ManageGroupsView);
        },
        
        viewGroup: function(id) {
        	this.loadView(GroupView, {id: id});
        },
        
        viewTiers: function() {
        	this.loadView(TierMasterView);
        },
        
        viewTransferRules: function() {
        	this.loadView(TransferRulesView);
        },
        
        viewTiersRules: function(id) {
        	this.loadView(TierRulesView, {id: id});
		},

        viewAccounts: function() {
			var self = this;
        	App.vent.listenTo(App.vent, 'application:adjustmentcomplete application:transfercomplete', function(ev) {
        		//self.loadView(AgentAccountsView, "agentaccounts");
          	});
        	this.loadView(AgentAccountsView);
        },

        viewAccount: function(id) {
        	this.loadView(AgentAccountView, {id: id})
        },
        
		viewAgentUser: function(id) {        	
        	this.loadView(AgentUserView, {id: id});
        },
        
        viewTdrs: function() {
        	this.loadView(TdrsView);
        },
        
        viewTdrsSearch: function(qs) {
        	this.loadView(TdrSearchView);
        },
        
        viewTdr: function(id) {
        	this.loadView(TdrView, {id: id});
        },
        
        viewAuditLog: function() {
        	this.loadView(AuditLogView);
        },
        
//		viewAuditLogSearch: function(qs) {
//        	this.loadView(AuditLogSearchView);
//        },
        
        viewAuditEntry: function(id) {
        	this.loadView(AuditEntryView, {id: id});
        },
        
        
        viewServiceClasses: function() {
        	this.loadView(ServiceClassMasterView);
        },
        
        viewServiceClass: function(id) {
        	this.loadView(ServiceClassView, {id: id});
        },
        
        viewAreas: function() {
        	this.loadView(ManageAreasView);
        },
        
        viewArea: function(id) {
        	this.loadView(AreaView, {id: id});
        },
        
        viewCells: function() {
        	this.loadView(ManageCellsView);
        },
        
        viewCell: function(id) {
        	this.loadView(CellView, {id: id});
        },
        
        viewCellGroups: function() {
        	this.loadView(ManageCellGroupsView);
        },
        
        // -- Configuration --
        viewAdjustmentsConfig: function() {
        	this.loadView(AdjustmentsConfigView);
        },
        
        viewAgentConfig: function() {
        	this.loadView(AgentConfigView);
        },
        
        viewBalanceEnquiriesConfig: function() {
        	this.loadView(BalanceEnquiriesConfigView);
        },
        
        viewChangePinsConfig: function() {
        	this.loadView(ChangePinsConfigView);
        },
        
        viewDepositsQueryConfig: function() {
        	this.loadView(DepositsQueryConfigView);
        },
        
        viewGeneralConfig: function() {
        	this.loadView(GeneralConfigView);
        },

        viewMobileNumberFormat: function() {
        	this.loadView(MobileNumberFormatView);
        }, 
        
        viewLastTransactionEnquiriesConfig: function() {
        	this.loadView(LastTransactionEnquiriesConfigView);
        },
        
        viewRegisterPinsConfig: function() {
        	this.loadView(RegisterPinsConfigView);
        },
        
        viewReplenishConfig: function() {
        	this.loadView(ReplenishConfigView);
        },
        
        viewAnalyticsConfig: function() {
        	this.loadView(AnalyticsConfigView);
        },
        
        viewRewardsConfig: function() {
        	this.loadView(RewardsConfigView);
        },
        
        viewSalesConfig: function() {
        	this.loadView(SalesConfigView, arguments);
        },
        
        viewSalesQueryConfig: function() {
        	this.loadView(SalesQueryConfigView);
        },
        
        viewSelfTopUpsConfig: function() {
        	this.loadView(SelfTopUpsConfigView, arguments);
        },
        
        viewTransactionsConfig: function() {
        	this.loadView(TransactionsConfigView);
        },
        
        viewTransactionStatusEnquiriesConfig: function() {
        	this.loadView(TransactionStatusEnquiriesConfigView);
        },
        
        viewTransfersConfig: function() {
        	this.loadView(TransfersConfigView, arguments);
        },
        
        viewBatchConfig: function() {
        	this.loadView(BatchConfigView);
        },
        
        viewReversalConfig: function() {
        	this.loadView(ReversalConfigView);
        },
        
        viewAdjudicationConfig: function() {
        	this.loadView(AdjudicationConfigView);
        },
        
        viewWebusersConfig: function() {
        	this.loadView(WebUsersConfigView);
        },
        
        viewBundleSalesConfig: function() {
        	this.loadView(BundleSalesConfigView, arguments);
        },
        
        viewUssdMenuConfig: function(id) {
        	this.loadView(UssdMenuConfigView, {id: id});
        },
        
        viewWorkflowConfig: function() {
        	this.loadView(WorkflowConfigView);
        },
        
        viewReportingConfig: function() {
        	this.loadView(ReportingConfigView);
        },
        
        viewTaskList: function() {
        	this.loadView(TaskListView);
        },
        
        viewTaskSearch: function() {
        	this.loadView(TaskSearchView);
        },
        
        viewPromotionsList: function() {
        	this.loadView(PromotionsManagerView);
        },
        
        viewPromotion: function(id) {
        	this.loadView(PromotionView, {id: id})
        },
        
        viewBundlesList: function() {
        	this.loadView(BundlesManagerView);
        },
        
        viewNoPermissions: function() {
        	this.loadView(NoPermissionView);
        }

    });
    
    return FrontController;
});
