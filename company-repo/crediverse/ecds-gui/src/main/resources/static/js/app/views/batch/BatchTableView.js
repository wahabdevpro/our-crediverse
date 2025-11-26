define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/CommonUtils'],
    function($, _, App, BackBone, Marionette, CommonUtils) {
        //ItemView provides some default rendering logic
        var BatchHistoryTableView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	currentFilter: {}, // Used to keep track of filter settings for use by export.
        	i18ntxt: App.i18ntxt.batch,
        	
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
            	
            	var table = this.$('.batchhistorytable');
            	this.dataTable = table.DataTable({
            		'stateSave': 'hash',
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
						"searchPlaceholder": "Batch filename",
		            },
					//"dom": "<'row'<'col-sm-6'l><'col-sm-6'<'headerToolbar'>>><'row'<'col-sm-12'tr>><'row'<'col-sm-5'i><'col-sm-7'p>>",
					//dom: '<f<t>lip>',
					dom: "<'row'<'col-lg-3 col-md-4 col-sm-5'f><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					"initComplete": function(settings, json) {
		                //if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
						self.$('.dtButtonBar').html(self.$('#dtButtonBarTemplate').html());
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
								App.vent.trigger('application:batchhistorysearcherror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
					  "order": [[ 0, "desc" ]],
                      "columns": [
                    	   {
                    		   data: "id",
                    		   title: self.i18ntxt.batchNo,
							   class: "center",
                   	           render: function(data, type, row, meta) {
                   	        	   return '<a class="routerlink" href="#batch/' + row['id'] + '">' + data + '</a>';
							   },
                    	   },
                    	   {
                    		   data: "timestamp",
                    		   title: self.i18ntxt.timestamp,
							   render: function(data, type, row, meta) {
							   	   if ( type == 'type' || type == 'sort' ) return parseInt(data);
								   return CommonUtils.formatTimeStamp(data);
							   }
                    	   },
                    	   {
                    		   data: "filename",
                    		   title: self.i18ntxt.filename,
                    	   },
                    	   {
                    		   data: "fileSize",
                    		   title: self.i18ntxt.fileSize,
							   defaultContent: "-",
							   class: "right nowrap",
							   render: function(data, type, row, meta) {
							   	   if ( type == 'type' || type == 'sort' ) return parseInt(data);
								  return CommonUtils.formatDecimal(data,0) + ' B';
							   }
                    	   },
                    	   {
                    		   data: "type",
                    		   title: self.i18ntxt.batchType,
                    	   },
                    	   {
                    		   data: "lineCount",
                    		   title: self.i18ntxt.lineCount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
							   	   if ( type == 'type' || type == 'sort' ) return parseInt(data);
								   return data!='-'?CommonUtils.formatDecimal(data,0):'-';
							   }
                    	   },
                    	   {
                    		   data: "failureCount",
                    		   title: self.i18ntxt.failureCount,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
							   	   if ( type == 'type' || type == 'sort' ) return parseInt(data);
								   return data!='0'?CommonUtils.formatDecimal(data,0):'-';
							   }
                    	   },
                    	   {
                    		   data: "totalValue",
                    		   title: self.i18ntxt.totalValue,
							   defaultContent: "-",
							   class: "right",
							   render: function(data, type, row, meta) {
							   	   if ( type == 'type' || type == 'sort' ) return parseInt(data);
								   return CommonUtils.formatCurrency(data);
							   }
                    	   },
                    	   {
                    		   data: "webUserName",
                    		   title: self.i18ntxt.webUserName,
                    	   },
                    	   {
                    		   data: "coAuthWebUserName",
                    		   title: self.i18ntxt.coAuthWebUserName,
							   defaultContent: "-",
                    	   },
                    	   {
                    		   data: null,
                    		   title: self.i18ntxt.state,
							   class: "right",
							   orderable: false,
                   	           render: function(data, type, row, meta) {
                   	        	var label = "NEW";
								var cls = "warning";
                   	        	   switch(row.batchStatus) {
                   	        	   	case "NEW":
                   	        	   	label = "NEW";
                   	        		   break;
	                   	        	case "QUEUED":
	                   	        		label = "UPLOADED";
	                	        		   break;
	                   	        	case "VERIFYING":
	                   	        		label = "VERIFYING";
	                	        		   break;
	                   	        	case "VERIFIED":
	                   	        		label = "VERIFIED";
	                	        		   break;
	                   	        	case "PENDING_AUTHORIZATION":
	                   	        		label = "AUTHORIZING";
	                	        		   break;
	                   	        	case "READY_FOR_PROCESSING":
	                   	        		label = "PROCESSING";
	                	        		   break;
	                   	        	case "ONHOLD":
	                   	        		label = "ONHOLD";
	                	        		   break;
	                   	        	case "PROCESSING":
	                   	        		label = "PROCESSING";
	                	        		   break;
	                   	        	case "COMPLETE":
	                   	        		label = "COMPLETE";
	                   	        		cls = "success";
	                	        		   break;
	                   	        	case "CANCELLED":
	                   	        		label = "CANCELLED";
	                	        		   break;
	                   	        	case "DECLINED":
	                   	        		label = "DECLINED";
	                   	        		cls = "danger";
	                	        		   break;
	                   	        	case "ERROR":
	                   	        		label = "FAILED";
	                   	        		cls = "danger";
	                	        		   break;
                   	        	   }

							   		/*var label = 'PENDING';
									var cls = 'warning';
							   		if(row['completed']) {
										if(row['failureCount'] > 0) {
											label = 'FAILED';
											cls = 'danger';
										} else { 
											label = 'SUCCESS';
											cls = 'success';
										}
									}*/	
							   		return '<span class="label label-' + cls + '">' + label + '</span>';
							   },
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
        return BatchHistoryTableView;
    });
