define( ['jquery', 'App', 'backbone', 'marionette', 'models/AuditLogModel', 'utils/CommonUtils'],
    function($, App, BackBone, Marionette, AuditLogModel, CommonUtils) {
        //ItemView provides some default rendering logic
        var AuditLogTableView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	currentFilter: {}, // Used to keep track of filter settings for use by export.
        	tableClass: ".auditlogtable",
        	attributes: {
        		class: "row"
        	},
        	
            ui: {
                role: '',
                exportLog: '.exportAuditButton'
            },

            // View Event Handlers
            events: {
            	"click @ui.entry": 'viewEntry',
            	"click @ui.exportLog": 'exportLog',
            },
            
            exportLog: function(ev) {
            	var self = this;
				
				var table = this.$('.auditlogtable');
				//CommonUtils.exportAsCsv(ev, self.currentFilter.url, self.currentFilter.data);
				CommonUtils.exportAsCsv(ev, self.url, self.currentFilter.data, self.options.criteria, true);
            },
            
            renderTable: function(options, urlToLoad, callback) {
            	App.log( 'rendering table' );
            	var self = this;
            	
            	var tableSettings = {
            			searchBox: true,
            			newurl: self.url
            	};
            	if (!_.isUndefined(urlToLoad) && urlToLoad.length > 0) {
            		tableSettings.newurl = urlToLoad;
  				}
            	
            	if (!_.isUndefined(options)) jQuery.extend(tableSettings, options);
            	var withcount = $('#withcount').val();
            	var pagingType = "simple";
				var bInfo = false;
            	if(!_.isUndefined(withcount) && withcount == 'true'){
    				pagingType = "full_numbers";
    				bInfo = true;	        				
    			}
            	
            	var table = this.$(this.tableClass);
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
					pagingType: pagingType,
					bInfo : bInfo,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": App.i18ntxt.auditLog.quickFilter,
		            },
					//"dom": "<'row'<'col-sm-6'l><'col-sm-6'<'headerToolbar'>>><'row'<'col-sm-12'tr>><'row'<'col-sm-5'i><'col-sm-7'p>>",
					//dom: '<f<t>lip>',
					dom: "<'row'<'col-lg-3 col-md-4 col-sm-5'><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					"initComplete": function(settings, json) {
		                //if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
						self.$('.dtButtonBar').html(self.$('#dtButtonBarTemplate').html());
						if(!_.isUndefined(callback))
							callback();
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
								App.vent.trigger('application:tdrssearcherror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
					  "order": [[ 0, "desc" ]],
                      "columns": [
                           	   {
                           		   data: "id",
                           		   title: App.i18ntxt.auditLog.entry,
       							   class: "all center",
                          	           render: function(data, type, row, meta) {
                          	        	   return '<a class="routerlink" href="#auditEntry/' + row['id'] + '">' + data + '</a>';
       							   		//return '<a target="_blank" href="#auditEntry/' + row['id'] + '">' + data + '</a>';
       							   },
                           	   },
                           	   {
                           		   data: "sequenceNo",
                           		   title: App.i18ntxt.auditLog.sequenceNo,
       							   defaultContent: "-",
       							   class: "center",
                           	   },
                           	   {
                           		   data: "timestampString",
       							   defaultContent: "-",
                           		   title: App.i18ntxt.auditLog.timestampString,
                           	   },
                           	   {
                           		   data: "userType",
                           		   title: App.i18ntxt.auditLog.userType,
                           		   render: function(data, type, row, meta) {
                           			   if (data === 'WEBUSER') {
                           				   return 'Web User';
                           			   } else if (data === 'AGENT') {
                           				   return 'Agent';
                           			   }
                           			   return '(none)';
                           		   }
                           	   },
                           	   {
                           		   data: "userName",
                           		   title: App.i18ntxt.auditLog.userName,
       							   defaultContent: "-",
       							   class: "all",
                           	   },
                           	   {
                           		   data: "ipAddress",
                           		   title: App.i18ntxt.auditLog.ipAddress,
       							   defaultContent: "-",
                           	   },
                           	   {
                           		   data: "domainName",
                           		   title: "Domain Name",
                           		   title: App.i18ntxt.auditLog.domainName,
       							   defaultContent: "-",
                           	   },
                           	   {
                           		   data: "dataType",
                           		   title: App.i18ntxt.auditLog.dataType,
       							   defaultContent: "-",
                           	   },
                           	   {
                           		   data: "action",
                           		   title: App.i18ntxt.auditLog.action,
       							   defaultContent: "-",
       							   class: "all right",
                           		   render: function(data, type, row, meta) {
                           			   var response = [];
                           			   response.push('<span class="label');
                           			   if (data === 'C') {
                           				   response.push('label-success">'+App.i18ntxt.enums.action.create+'</span>');
                           			   } else if (data === 'U') {
                           				   response.push('label-warning">'+App.i18ntxt.enums.action.update+'</span>');
                           			   } else if (data === 'D') {
                           				   response.push('label-danger">'+App.i18ntxt.enums.action.delete+'</span>');
                           			   } else {
                           				   response.push('label-default">'+App.i18ntxt.enums.action.uknown+'</span>');
                           			   }
                           			   return response.join(' ');
                           		   }
                           	   },
                          	  ],
                  } );
				
				//this.$('div.headerToolbar').html('<div style="text-align:right;"><a href="#transactionSearch" class="routerlink btn btn-primary"><i class="fa fa-search"></i> '+App.i18ntxt.global.searchBtn+'</a></div>');  

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
            	this.configureSearchForm();
            	this.renderTable();
  		  	},
            
            configureSearchForm:function() {
            }
            
        });
        return AuditLogTableView;
    });
	
