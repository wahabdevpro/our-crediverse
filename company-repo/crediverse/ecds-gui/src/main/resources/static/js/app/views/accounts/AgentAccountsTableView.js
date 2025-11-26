define( ['jquery', 'App', 'backbone', 'marionette',
         "views/accounts/AgentAccountsDialogView", "handlebars", "models/ValidationModel",
         'utils/HandlebarHelpers', 'models/AgentModel', 'models/AdjustmentModel', 'models/TransferModel',
         'collections/TierCollection', 'views/accounts/TransferDialogView',
         'views/accounts/AdjustmentDialogView', 'views/accounts/AgentAccountOperationView',
         'models/RootAccountModel', 'views/ReplenishView', 'utils/CommonUtils',
         'datatables'],
    function($, App, BackBone, Marionette, AgentAccountsDialogView, Handlebars, ValidationModel,
    		HBHelper, AgentModel, AdjustmentModel, TransferModel, TierCollection, TransferDialogView,
    		AdjustmentDialogView, AgentAccountOperationView,
    		RootAccountModel, ReplenishView, CommonUtils) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.agentAccounts;
	
        var AgentAccountsTableView =  AgentAccountOperationView.extend( {
        	tagName: 'div',
			currentFilter: {}, // Used to keep track of filter settings for use by export.
        	attributes: {
        		class: "row"
        	},
        	
        	renderTable: function(options) {
				$(window).load(this.dropupAdjust);
				$(window).bind('resize scroll touchstart touchmove mousewheel', this.dropupAdjust);
            	
				var self = this;
            	var tableSettings = {
            			searchBox: true,
            			newurl: self.url
            	};
            	
            	if (!_.isUndefined(options)) jQuery.extend(tableSettings, options);

            	var table = this.$('.accountstable');
            	this.dataTable = table.DataTable({
            		//dom: '<f<t>lip>',
					dom: "<'row'<'col-lg-3 col-md-4 col-sm-5'f><'col-lg-9 col-md-8 col-sm-7 right dtButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					'stateSave': 'hash',
        			//serverSide: true,
        			// data is params to send
					//"pagingType": "simple",
					//"infoCallback": function( settings, start, end, max, total, pre ) {
					//	return 'Showing records from <strong>' + start + '</strong> to <strong>' + end + '</strong>';
					//},
					"processing": true,
					//"searching": false,
					"serverSide": true,
					"autoWidth": false,
					'stateSave': 'hash',
					"responsive": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": "Account #, Mobile Number, Agent Name ...",
		            },
		            "initComplete": function(settings, json) {
		                //if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
						self.$('.dtButtonBar').html(self.$('#dtButtonBarTemplate').html());
		              },
					//"dom": "<'row'<'col-sm-6'l><'col-sm-6'<'headerToolbar'>>><'row'<'col-sm-12'tr>><'row'<'col-sm-5'i><'col-sm-7'p>>",
          			"ajax": function(data, callback, settings) {
          				App.log('fetching data: ' + tableSettings.newurl);
          				self.currentFilter.url = tableSettings.newurl;
          				self.currentFilter.data = data;
          				var jqxhr = $.ajax(tableSettings.newurl, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
//						  console.dir( dataResponse);
                      	    callback(dataResponse);
							table.DataTable()
							   .columns.adjust()
							   .responsive.recalc();
							self.dropupAdjust();
                      	  })
                      	  .fail(function(dataResponse) {
                      			self.error = dataResponse;
                      			App.error(dataResponse);
								App.vent.trigger('application:accountsterror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
					  "order": [[ 0, "desc" ]],
                      "columns": [
                    	   {
                    		   data: "id",
                    		   title: i18ntxt.tableUniqueIDTitle,
							   class: "all center",
							   width: "80px",
							   render: function(data, type, row, meta) {
								   return '<a class="routerlink" href="#account/' + row['id'] + '">' + data + '</a>';
							   		//return '<a target="_blank" href="#account/'+row['id']+'">' + data + '</a>';
							   }
                    	   },
                    	   {
                    		   data: "accountNumber",
                    		   title: i18ntxt.tableAccountNumberTitle,	
                    		   defaultContent: "-"
                    	   },
                    	   {
                    		   data: "mobileNumber",
                    		   title: i18ntxt.tableMobileNumberTitle
                    	   },
                    	   {
                    		   data: "firstName",
                    		   title: i18ntxt.tableFirstNameTitle,
							   render: function(data, type, row, meta) {
							   		return row['firstName'] + " " + row['surname'];
							   }
                    	   },

                    	   //Stuart will probably ask for this later - at the moment the TS only sends the ownerAgentID in the list.
                    	   /*{
                    		   data: "ownerAgent",
                    		   title: i18ntxt.tableOwnerAgentTitle,
							   sortable: false,
							   defaultContent: "(none)"
                    	   },*/
                    	   {
                    		   data: "tierName",
                    		   title: i18ntxt.tableTierNameTitle,
							   //sortable: false,
							   defaultContent: "<span class='label label-danger'>(invalid)</span>"
                    	   },
                    	   {
                    		   data: "groupName",
                    		   title: i18ntxt.tableGroupNameTitle,
                    		   defaultContent: App.i18ntxt.global.none,
                    		   //sortable: false
                    	   },                    	   
                    	   {
                    		   data: "balance",
                    		   title: i18ntxt.tableBalanceTitle,
							   className: "right",
							   render: function(data, type, row, meta) {
							   		return CommonUtils.formatNumber(data);
							   }
                    	   },
                    	   
                    	   
						   /*
                    	   {
                    		   data: "bonusBalance",
                    		   title: i18ntxt.tableBonusBalanceTitle,
							   sortable: false,
							   className: "right"
                    	   },
						   */
                    	   {
                    		   data: "currentState",
                    		   title: i18ntxt.tableCurrentStateTitle,
							   className: "center",
							   //width: "100px"
                    		   render: function(data, type, row, meta) {
                    			   var response = [];
                    			   response.push('<span class="label');
                    			   if (data === 'ACTIVE') {
                    				   response.push('label-success">'+i18ntxt.responseStateActive+'</span>');
                    			   } else if (data === 'SUSPENDED') {
                    				   response.push('label-warning">'+i18ntxt.responseStateSuspended+'</span>');
                    			   } else if (data === 'DEACTIVATED') {
                    				   response.push('label-danger">'+i18ntxt.responseStateDeactivated+'</span>');
                    			   } else {
                    				   response.push('label-default">'+i18ntxt.responseStatePermanent+'</span>');
                    			   }
                    			   return response.join(' ');
                    		   }
                    	   },
                    	   {
                   	            targets: -1,
                   	            data: null,
                   	            title: "",
							    className: "right all",
								width: "108px",
                   	            sortable: false,
                   	            render: function(data, type, row, meta) {
                   	            	var menuData = {};
                   	            	menuData.rowID = row.id;
                   	            	menuData.canChange = (row.currentState!=="SUSPENDED" && row.currentState!=="DEACTIVATED")?true:false;
                   	            	menuData.transferFromRoot = ((row.tierType == "RETAILER") || (row.tierType == "WHOLESALER") || (row.tierType == "STORE"))?true:false;
                   	            	menuData.isRoot = (row.tierType === "ROOT")?true:false;
                   	            	menuData.notPermanent = (row.currentState==="PERMANENT")?false:true;
                   	            	menuData.isActive = (row.currentState==="ACTIVE")?true:false;
                   	            	menuData.isSuspended = (row.currentState==="SUSPENDED")?true:false;
                   	            	menuData.isDeactivated = (row.currentState==="DEACTIVATED")?true:false;
                   	            	menuData.authenticationMethod = row.authenticationMethod;
                   	            	var $html = $(CommonUtils.getTemplateHtml("AgentAccounts#tableMenuView", menuData));
                   	            	if ($html.find('.dropdown-menu li').length > 0) {
                   	            		$html.find('.actionDropdown').show();
                   	            		$html.find('.actionButton').hide();
                   	            	}
                   	            	else {
                   	            		$html.find('.actionDropdown').hide();
                   	            		$html.find('.actionButton').show();
                   	            	}
                   	            	return $html.html();
            	            	}
                   	        }
                    	  ]
                  })

				//this.$('div.headerToolbar').html('<div style="text-align:right;"><a href="#accountSearch" class="routerlink btn btn-primary"><i class="fa fa-search"></i> '+App.i18ntxt.global.searchBtn+'</a></div>');  
				
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

			dropupAdjust: function() {
				$(".accountstable .dropdown-toggle").each(CommonUtils.adjustDropdownDir);
			},
  		  
		  	// unused
            onRender: function () {
            	this.renderTable();
            },

            ui: {
                
            },

            // View Event Handlers
            events: {
            	
            },
            
            
			
            
            
            
			createAgent: function(ev) {
            	var self = this;
            	var model = new AgentModel({
					mode: 'create',
					rules : { 
						'accountNumber'  : { 
							required : false 
						} 
					}
            	});
            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: AgentAccountsDialogView,
					class: 'modal-lg modal-xl',
            		title: App.i18ntxt.agentAccounts.addModalTitle,
            		hide: function() {
	        			self
				        .dataTable.ajax.reload().draw();
	        		},
            		params: {
            			model: model
            		}
            	});
				return false;
			},

            
        });
        return AgentAccountsTableView;
    });
