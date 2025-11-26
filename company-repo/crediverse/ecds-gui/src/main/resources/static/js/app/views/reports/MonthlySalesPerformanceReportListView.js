define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'models/ReportModel', 'models/ReportScheduleModel', 'views/reports/MonthlySalesPerformanceReportListTableView', 'views/reports/ReportScheduleDialogView', 
		 'utils/CommonUtils', 'collections/UserCollection', 'datatables'],
    function($, _, App, BackBone, Marionette, Handlebars, HBHelper, ReportModel, ReportScheduleModel, MonthlySalesPerformanceReportListTableView, ReportScheduleDialogView, CommonUtils, UserCollection) {

		var i18ntxt = App.i18ntxt.reports.monthlySalesPerformance;
        var MonthlySalesPerformanceReportListView =  MonthlySalesPerformanceReportListTableView.extend( {
        	tagName: 'div',
        	attributes: {
        		//class: "row",
        		id: 'monthlysalesperformancelist'
        	},
  		  	template: "Reports#monthlysalesperformancelist",
  		  	url: 'api/reports/monthlysalesperformance/list',
			urlSchedule: 'api/reports/monthly_sales_performance',
			reportType: 'monthly_sales_performance',
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
  		  			heading: txt.monthlySalesPerformanceList.heading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
						text: txt.report,
					iclass: "fa fa-file-text"
					},{
  		  				text: txt.monthlySalesPerformanceList.headingBC,
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
            
            deleteReport: function(ev) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = this.dataTable.row(row);
            	var data = clickedRow.data();
            	
            	CommonUtils.delete({
	        		itemType: App.i18ntxt.reports.report,
	        		url: 'api/reports/monthlysalesperformance/'+data.id,
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
        return MonthlySalesPerformanceReportListView;
    });
