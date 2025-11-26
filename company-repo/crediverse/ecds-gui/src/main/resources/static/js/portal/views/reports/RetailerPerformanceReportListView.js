define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'models/ReportModel', 'models/ReportScheduleModel', 'views/reports/RetailerPerformanceReportListTableView', 'views/reports/ReportScheduleDialogView', 
		 'utils/CommonUtils', 'collections/AgentUserCollection', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, HBHelper, ReportModel, ReportScheduleModel, RetailerPerformanceReportListTableView, ReportScheduleDialogView, CommonUtils, AgentUserCollection) {

		var i18ntxt = App.i18ntxt.reports.retailerPerformance;
        var RetailerPerformanceReportListView =  RetailerPerformanceReportListTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		//class: "row",
        		id: 'retailerperformancelist'
        	},
  		  	template: "Reports#retailerperformancelist",
  		  	url: 'papi/reports/retailerperformance/list',
			urlSchedule: 'papi/reports/retailer_performance',
			reportType: 'retailer_performance',
  		  	error: null,
  		  	model: new ReportModel(),
  		  	
			ui: {
                deleteReport: '.deleteReportButton',
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
  		  			heading: txt.retailerPerformanceList.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
						text: txt.report,
						iclass: "fa fa-file-text"
					},{
  		  				text: txt.retailerPerformanceList.headingBC,
  		  			}]
  		  		}
  		  	},
        	
            initialize: function (options) {
            	this.model.displayRequiredStars = false;
            	
				var users = new AgentUserCollection();
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
            
            deleteReport: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	
            	CommonUtils.delete({
	        		itemType: App.i18ntxt.reports.report,
	        		url: 'papi/reports/retailerperformance/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.reports.report,
	        			name: data.name,
	        			description: data.description
	        		},
	        		rowElement: row,
	        	}, {
	        		success: function(model, response) {
		            	row.fadeOut("slow", function() {
		            		clickedRow.remove().draw();
		            	});
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
	        	
            },
            
        });
        return RetailerPerformanceReportListView;
    });
