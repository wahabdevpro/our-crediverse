define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'models/ReportModel', 'views/reports/SalesSummaryReportListTableView', 'utils/CommonUtils', 'collections/UserCollection', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, HBHelper, ReportModel, SalesSummaryReportListTableView, CommonUtils, UserCollection) {

		var i18ntxt = App.i18ntxt.reports.salesSummary;
        var SalesSummaryReportListView =  SalesSummaryReportListTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		//class: "row",
        		id: 'salessummarylist'
        	},
  		  	template: "Reports#salessummarylist",
  		  	url: 'api/reports/salessummary/list',
			urlSchedule: 'api/reports/sales_summary',
			reportType: 'sales_summary',
  		  	error: null,
  		  	model: new ReportModel(),
  		  	
			ui: {
                scheduleReport: '.deleteReportButton',
				scheduleReport: '.scheduleReportButton',
				scheduleUpdate: '.updateSchedule',
				scheduleDelete: '.deleteSchedule',
				scheduleExecute: '.executeSchedule',
			},
			
			events: {
            	"click @ui.deleteReport": 'deleteReport',
            	"click @ui.scheduleReport": 'addSchedule',
            	"click @ui.scheduleUpdate": 'updateSchedule',
            	"click @ui.scheduleDelete": 'deleteSchedule',
            	"click @ui.scheduleExecute": 'executeSchedule',
			},
        	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.reports;
  		  		return {
  		  			heading: txt.salesSummaryList.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
						text: txt.report,
						iclass: "fa fa-file-text"
					},{
  		  				text: txt.salesSummaryList.headingBC,
  		  			}]
  		  		}
  		  	},
        	
            initialize: function (options) {
            	this.model.displayRequiredStars = false;
            	
				var users = new UserCollection();
            	var self = this;
            	users.fetch({
            		success: function(ev){
            			var userData = users.toJSON();
            			self.model.set('userList', userData);
            		}
            	});
            },
            
            onRender: function () {
            	var self = this;
  		  		try {
  			  		self.renderTable({
  		  				searchBox: false
  		  			});
  		  		} catch(err) {
  		  			if (console) console.error(err);
  		  		}
            },
        });
        return SalesSummaryReportListView;
    });
