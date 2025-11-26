define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/CommonUtils'],
    function($, _, App, BackBone, Marionette, CommonUtils) {
        //ItemView provides some default rendering logic
        var RetailerPerformanceReportTableView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	currentFilter: {}, // Used to keep track of filter settings for use by export.
        	i18ntxt: App.i18ntxt.reports.retailerPerformance,
        	
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
            	
            	var table = this.$('.retailerperformancetable');
            	this.dataTable = table.DataTable({
            		'stateSave': 'hash',
                    'ordering': false,
        			//serverSide: true,
        			// data is params to send
					//"pagingType": "simple",
					//"infoCallback": function( settings, start, end, max, total, pre ) {
					//	return 'Showing records from <strong>' + start + '</strong> to <strong>' + end + '</strong>';
					//},

					"processing": true,
					"serverSide": true,
					"autoWidth": false,
					"responsive": true,
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
          			"ajax": function(data, callback, settings) {
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
					  "order": tableSettings.order,
                      "columns": [
                    	   {
                    		   data: "a_AgentID",
                    		   title: self.i18ntxt.agentId,
							   class: "center",
                   	           //render: function(data, type, row, meta) {
                   	        //	   return '<a class="routerlink" href="#batch/' + row['id'] + '">' + data + '</a>';
							  // },
                    	   },
                    	   {
                    		   data: "date",
                    		   title: self.i18ntxt.date,
							   render: function(data, type, row, meta) {
							   	   if ( type == 'type' || type == 'sort' ) return parseInt(data);
								   return CommonUtils.formatTimeStampAsDate(data);
							   }
                    	   },
                    	   {
                    		   data: "transactionType",
                    		   title: self.i18ntxt.transactionType,
							   render: function(data, type, row, meta) {
							   		return App.translate("enums.transactionTypeCode." + data, data);
							   }
                    	   },
                    	   {
                    		   data: "transactionStatus",
                    		   title: self.i18ntxt.transactionStatus,
							   defaultContent: "-",
							   class: "center nowrap",
							   render: function(data, type, row, meta) {
							   	  if (data == true)
								  	return '<span class="label label-success">Successful</span>';
								  return '<span class="label label-danger">Failed</span>';
							   }
                    	   },
                    	   {
                    		   data: "a_AccountNumber",
                    		   title: self.i18ntxt.accountNumber,
                    	   },
                    	   {
                    		   data: "a_MobileNumber",
                    		   title: self.i18ntxt.mobileNumber,
                    	   },
                    	   {
                    		   data: "a_IMEI",
                    		   title: self.i18ntxt.imei,
                    	   },
                    	   {
                    		   data: "a_IMSI",
                    		   title: self.i18ntxt.imsi,
                    	   },
                    	   {
                    		   data: "a_Name",
                    		   title: self.i18ntxt.name,
                    	   },
                    	   {
                    		   data: "a_TierName",
                    		   title: self.i18ntxt.tierName,
                    	   },
                    	   {
                    		   data: "a_GroupName",
                    		   title: self.i18ntxt.groupName,
                    	   },
                    	   {
                    		   data: "a_ServiceClassName",
                    		   title: self.i18ntxt.serviceClassName,
                    	   },
                    	   {
                    		   data: "a_OwnerImsi",
                    		   title: self.i18ntxt.ownerImsi,
                    	   },
                    	   {
                    		   data: "a_OwnerMobileNumber",
                    		   title: self.i18ntxt.ownerMobileNumber,
                    	   },
                    	   {
                    		   data: "a_OwnerName",
                    		   title: self.i18ntxt.ownerName,
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
                    	   {
                    		   data: "totalBonus",
                    		   title: self.i18ntxt.totalBonus,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
							   	   if ( type == 'type' || type == 'sort' ) return parseInt(data);
								   return CommonUtils.formatCurrency(data);
							   }
                    	   },
                    	   {
                    		   data: "transactionCount",
                    		   title: self.i18ntxt.transactionCount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
							   	   if ( type == 'type' || type == 'sort' ) return parseInt(data);
								   return data!='0'?CommonUtils.formatDecimal(data,0):'-';
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
				
				table.on('xhr.dt', function ( e, settings, json, xhr ) {
					var ajax = self.dataTable.ajax;	
					self.currentFilter.dtData = ajax.params();
    			});

            	//var table = $('#tabledata');
            	if (this.error === null) {
            		
            	}
            },
            
            onRender: function () {
            	this.renderTable();
  		  	}
            
        });
        return RetailerPerformanceReportTableView;
    });
