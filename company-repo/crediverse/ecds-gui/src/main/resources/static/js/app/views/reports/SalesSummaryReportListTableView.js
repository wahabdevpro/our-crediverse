define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/CommonUtils', 'views/reports/ReportListTableView'],
    function($, _, App, BackBone, Marionette, CommonUtils, ReportListTableView) {
        //ItemView provides some default rendering logic
        var SalesSummaryReportListTableView = ReportListTableView.extend( {
        	tagName: 'div',
        	currentFilter: {}, // Used to keep track of filter settings for use by export.
        	i18ntxt: App.i18ntxt.reports.salesSummaryList,
        	
        	attributes: {
        		class: "row"
        	},
            
            renderTable: function(options) {
            	App.log( 'rendering table' );
            	var self = this;
            	
            	var tableSettings = {
            			searchBox: true,
            			newurl: self.url
            	};
            	
            	if (!_.isUndefined(options)) jQuery.extend(tableSettings, options);
            	
            	var table = this.$('.salessummarylisttable');
            	this.dataTable = table.DataTable({
            		'stateSave': 'hash',
					"processing": true,
					"autoWidth": false,
					"responsive": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": "Search ...",
		            },
					"dom": "<'row'<'col-lg-3 col-md-4 col-sm-5 left'f><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					"initComplete": function(settings, json) {
						self.$('.dtButtonBar').html(self.$('#dtButtonBarListTemplate').html());
		              },
          			"ajax": function(data, callback, settings) {
          				App.log('fetching data: ' + tableSettings.newurl);
          				self.currentFilter.url = tableSettings.newurl;
          				self.currentFilter.data = data;
          				var jqxhr = $.ajax(tableSettings.newurl, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
                      	    callback(dataResponse);
				
							table.find('.fadeout-bottom a').on('click', function(ev) {
								$(this).closest('.fadeout-bottom').hide();
								$(this).closest('.fadeout-bottom-wrapper').css('max-height','').find('.faded-user-list-div').css('max-height', '');
								$(this).closest('.fadeout-bottom-wrapper ').find('.fadeout-collapse').show();
								ev.preventDefault();
								ev.stopPropagation();
							});
							table.find('.fadeout-collapse a').on('click', function(ev) {
								$(this).closest('.fadeout-collapse').hide();
								$(this).closest('.fadeout-bottom-wrapper').css('max-height','110px').find('.faded-user-list-div').css('max-height', '110px');
								$(this).closest('.fadeout-bottom-wrapper ').find('.fadeout-bottom').show();
								ev.preventDefault();
								ev.stopPropagation();
							});
                      	  })
                      	  .fail(function(dataResponse) {
                      		  self.error = dataResponse;
                      			App.error(dataResponse);
								//App.vent.trigger('application:reportfiltererror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
					  "order": [[ 0, "desc" ]],
                      "columns": [
                    	   {
                    		   data: "name",
                    		   title: self.i18ntxt.reportName,
							   /*
							   render: function(data, type, row, meta) {
								   return '<a href="#salesSummaryReport/'+row.id+'" class="routerlink">'+data+'</a>';
							   }
							   */
                    	   },
                    	   {
                    		   data: "description",
                    		   title: self.i18ntxt.reportDescription,
							   render: function(data, type, row, meta) {
								   return data ? data.replace(/(?:\r\n|\r|\n)/g, '<br/>') : '';
							   }
                    	   },
                    	   {
                    		   data: "schedules",
                    		   title: self.i18ntxt.reportSchedules,
							   defaultContent: "ad-hoc",
							   render: function(data, type, row, meta) {
							   		if(!data) return 'ad-hoc';
								   return self.formatSchedule(data, true);
							   }
                    	   },
                    	   {
                    		   data: null,
                    		   title: '',
							   class: "right",
							   render: function(data, type, row, meta) {
                   	            	var buttons = [];
                   	            	if (App.hasPermission("Reports", "Update")) {
                   	            		buttons.push('<button type="button" data-channel-selection="1" class="btn btn-primary btn-xs scheduleReportButton">'+App.i18ntxt.global.scheduleBtn+'</button>');
                   	            	//	buttons.push("<a href='#retailerPerformanceReport/"+row.id+"' class='btn btn-primary routerlink btn-xs'>"+App.i18ntxt.global.editBtn+"</a>");
                   	            	}
                   	            	//if (App.hasPermission("Reports", "Delete")) {
                   	            	//	buttons.push("<button class='btn btn-danger deleteReportButton btn-xs'><i class='fa fa-times'></i></button>");
                   	            	//}
            	            		return buttons.join('');
							   }
                    	   },
                    	 ]
                  });
            },
            
            onRender: function () {
            	this.renderTable();
  		  	}
            
        });
        return SalesSummaryReportListTableView;
    });
