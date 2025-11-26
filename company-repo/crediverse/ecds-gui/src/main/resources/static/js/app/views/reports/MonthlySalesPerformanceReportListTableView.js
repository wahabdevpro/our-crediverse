define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/CommonUtils', 'views/reports/ReportListTableView'],
    function($, _, App, BackBone, Marionette, CommonUtils, ReportListTableView) {
        //ItemView provides some default rendering logic
        var MonthlySalesPerformanceReportListTableView = ReportListTableView.extend( {
        	currentFilter: {}, // Used to keep track of filter settings for use by export.
        	i18ntxt: App.i18ntxt.reports.monthlySalesPerformanceList,
        	
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
            	
            	var table = this.$('.monthlysalesperformancelisttable');
            	this.dataTable = table.DataTable({
            		'stateSave': 'hash',
        			//serverSide: true,
        			// data is params to send
					//"pagingType": "simple",
					//"infoCallback": function( settings, start, end, max, total, pre ) {
					//	return 'Showing records from <strong>' + start + '</strong> to <strong>' + end + '</strong>';
					//},

					"processing": true,
					//"serverSide": true,
					"autoWidth": false,
					"responsive": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": "Search ...",
		            },
					//"dom": "<'row'<'col-sm-6'l><'col-sm-6'<'headerToolbar'>>><'row'<'col-sm-12'tr>><'row'<'col-sm-5'i><'col-sm-7'p>>",
					//dom: '<f<t>lip>',
					dom: "<'row'<'col-lg-3 col-md-4 col-sm-5 left'f><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					"initComplete": function(settings, json) {
		                //if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
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
							   render: function(data, type, row, meta) {
								   var params = "";
								   if(!_.isUndefined(row.name)){
									   params += "/name!"+ encodeURIComponent(row.name);
								   }
								   if(!_.isUndefined(row.description)){
									   params += "/description!"+encodeURIComponent(row.description);
								   }
								   if(!_.isUndefined(row.parameters)) {
									   if(!_.isUndefined(row.parameters.relativeTimeRange)) {
										   params += "/period!"+encodeURIComponent(row.parameters.relativeTimeRange);
									   }
									   if(!_.isUndefined(row.parameters.filter) && !_.isUndefined(row.parameters.filter.items)){
										   var fieldValues = {
												   tiers : "",
												   groups : "",
												   transactionStatus : "",
												   ownerAgents : "",
												   agents : "",
												   transactionTypes : ""
										   }
										   for(var i = 0; i < row.parameters.filter.items.length; i++){
											   var item = row.parameters.filter.items[i];
											   switch(item.field){
												   case 'TRANSACTION_TYPES':{
													   fieldValues.transactionTypes = encodeURIComponent(item.value);
													   break;
												   }
												   case 'TRANSACTION_STATUS':{
													   fieldValues.transactionStatus = encodeURIComponent(item.value);
													   break;
												   }
												   case 'TIERS' : {
													   fieldValues.tiers = encodeURIComponent(item.value);
													   break;
												   }
												   case 'GROUPS' : {
													   fieldValues.groups = encodeURIComponent(item.value);
													   break;
												   }
												   case 'AGENTS' : {
													   fieldValues.agents = encodeURIComponent(item.value);
													   break;
												   }
												   case 'OWNER_AGENTS' : {
													   fieldValues.ownerAgents = encodeURIComponent(item.value);
													   break;
												   }
												   default:{
													   break;
												   }
											   }
										   }
										   for(var key in fieldValues){
											   params += "/" + key + "!" + (fieldValues[key]!=""?fieldValues[key]:"null");
										   }
									   }
								   }
								   
								   var url = '#monthlySalesPerformanceReport/' + row.id + '/asf!on' + params;
								   return '<a href="' + url + '" class="routerlink">'+data+'</a>';
							   }
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
								   return self.formatSchedule(data);;
							   }
                    	   },
                    	   {
                    		   data: null,
                    		   title: '',
							   class: "right",
							   render: function(data, type, row, meta) {
                   	            	var buttons = [];
                   	            	if (App.hasPermission("Reports", "Update")) {
                   	            		buttons.push('<button type="button" class="btn btn-primary btn-xs scheduleReportButton">'+App.i18ntxt.global.scheduleBtn+'</button>');
                   	            		buttons.push("<a href='#monthlySalesPerformanceReport/"+row.id+"/edit' class='btn btn-primary routerlink btn-xs'>"+App.i18ntxt.global.editBtn+"</a>");
                   	            	}
                   	            	if (App.hasPermission("Reports", "Delete")) {
                   	            		buttons.push("<button class='btn btn-danger deleteReportButton btn-xs'><i class='fa fa-times'></i></button>");
                   	            	}
            	            		return buttons.join('');
							   }
                    	   },
                    	 ]
                  });
				
				table.find('.dataTables_filter input').unbind();
				table.find('.dataTables_filter input').bind('keyup', function(e) {
					if(e.keyCode == 13) {
						self.dataTable.fnFilter(this.value);	
					}
				});	

            	//var table = $('#tabledata');
            	if (this.error === null) {
            		
            	}
            },
            
            onRender: function () {
            	this.renderTable();
  		  	}
            
        });
        return MonthlySalesPerformanceReportListTableView;
    });
