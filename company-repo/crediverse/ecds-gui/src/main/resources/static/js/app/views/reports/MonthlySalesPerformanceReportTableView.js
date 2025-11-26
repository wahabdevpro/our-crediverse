define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/CommonUtils'],
    function($, _, App, BackBone, Marionette, CommonUtils) {
        //ItemView provides some default rendering logic
        var MonthlySalesPerformanceReportTableView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	currentFilter: {}, // Used to keep track of filter settings for use by export.
        	i18ntxt: App.i18ntxt.reports.monthlySalesPerformance,
        	
        	attributes: {
        		class: "row"
        	},
            
            renderTable: function(options) {
            	App.log( 'rendering table' );
            	var self = this;
            	
            	var tableSettings = {
            			searchBox: true,
            			newurl: self.url,
						order: [[ 0, "desc" ]]
            	};
            	
            	if (!_.isUndefined(options)) jQuery.extend(tableSettings, options);
            	
            	var table = this.$('.monthlysalesperformancetable');
            	this.dataTable = table.DataTable({
            		"stateSave": 'hash',
                    "ordering": false,
					"processing": true,
					"serverSide": true,
					"autoWidth": false,
					"responsive": true,
					"deferLoading": 0,
					//"bSort": false,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": "Search ...",
		            },
					//dom: "<'row'<'col-lg-3 col-md-4 col-sm-5'><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					dom: "<'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'><'col-sm-4'p>>",
					"initComplete": function(settings, json) {
		                //if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
						//self.$('.dtButtonBar').html(self.$('#dtButtonBarTemplate').html());
		              },
          			  "ajax" : function(data, callback, settings) {
            				App.log('fetching data: ' + tableSettings.newurl);
              				self.currentFilter.url = tableSettings.newurl;
              				self.currentFilter.data = data;
              				var jqxhr = $.ajax(tableSettings.newurl, {
              					data: data
              				})
                          	  .done(function(dataResponse) {
                          	    callback(dataResponse);
                          	  })
                          	  .fail(function(dataResponse) {
                          		  self.error = dataResponse;
                          			App.error(dataResponse);
            						App.vent.trigger('application:reportfiltererror', dataResponse);
                          	  })
                          	  .always(function(data) {
                          	  });
                          },
					  "order": [[0, 'desc']],
                      "columns": [
	                   	   {
	                		   data: "month",
	                		   title: self.i18ntxt.month,
							   defaultContent: "-"
	                	   },
                    	   {
                    		   data: "groupName",
                    		   title: self.i18ntxt.groupName,
							   defaultContent: "(none)"
                    	   },
                    	   {
                    		   data: "ownerMsisdn",
                    		   title: self.i18ntxt.ownerAgentMsisdn,
							   defaultContent: "-",
                    	   },
                    	   {
                    		   data: "msisdn",
                    		   title: self.i18ntxt.agentMsisdn,
							   defaultContent: "-",
                    	   },
                    	   {
                    		   data: "totalAmount",
                    		   title: self.i18ntxt.totalAmount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
							   	   if ( type == 'type' || type == 'sort' ) return parseInt(data);
								   return CommonUtils.formatCurrency(data);
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
            },
            
            onRender: function () {
            	this.renderTable();
  		  	}
        });
        return MonthlySalesPerformanceReportTableView;
    });
