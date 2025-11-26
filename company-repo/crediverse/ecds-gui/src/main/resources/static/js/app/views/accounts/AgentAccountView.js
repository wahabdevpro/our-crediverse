define( ['jquery', 'App', 'backbone', 'marionette', "views/accounts/AgentAccountsDialogView", "views/accounts/AgentUserDialogView", "views/accounts/ApiUserDialogView", "handlebars",
         'utils/HandlebarHelpers', 'models/AgentModel', 'models/AgentUserModel', 'collections/TierCollection', 'views/accounts/TransferDialogView',
         'views/accounts/AgentAccountOperationView',
         'utils/CommonUtils', 'views/PasswordChangeDialogView', 'models/PasswordChangeModel', 'models/TdrModel', 'moment', 'datatables'],
    function($, App, BackBone, Marionette, AgentAccountsDialogView, AgentUserDialogView, ApiUserDialogView, Handlebars,
    		HBHelper, AgentModel, AgentUserModel, TierCollection, TransferDialogView,
    		AgentAccountOperationView, CommonUtils, PasswordChangeDialogView, PasswordChangeModel, TdrModel, moment) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.agentAccounts;
        var AgentAccountView =  AgentAccountOperationView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "AgentAccount#accountdetails",
  		  	url: 'api/agents/',
  		  	ausersUrl: 'api/ausers',
  		  	tdrsUrl: 'api/tdrs',//XXX
  		  	error: null,
			tierList: null,
			rolesModel: null,
			channelTypesModel: null,
			id: null,
			currentFilter: {
				'A': {},
				'B': {},
			},
			model: null,
			dataTable: {
				'A': null,
				'B': null,
				'users': null,
			},

			currentUserFilter: {},

  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.agentAccounts;
  		  		return {
  		  			heading: txt.accountHeading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.agentAccounts,
  		  				href: "#accountList",
						iclass: "fa fa-users"
  		  			}, {
  		  				text: txt.accountPageBC,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},

            initialize: function (options) {
//            	// Extract i18n / id
            	if (!_.isUndefined(options)) {
            		this.i18ntxt = options;
            		if (!_.isUndefined(options.id)) {
            			this.id = options.id;
            		}
            	}

//				try {
//        			HBHelper.registerIfCondition();
//        		} catch(err) {
//        			App.error(err);
//        		}
            },

			initTdrTable: function(selector, filter, fdata, side, callback) {
				var self = this;
				var tri18ntxt = App.i18ntxt.transactions;
            	var table = this.$(selector);
            	
            	var withcount = this.$('#withcount').val();
            	var pagingType = "simple";
				var bInfo = false;
            	if(withcount == 'true'){
    				pagingType = "full_numbers";
    				bInfo = true;	        				
    			}
            	
            	this.dataTable[side] = table.DataTable( {
            		//"searching": false,
					"processing": true,
					"serverSide": true,
					"autoWidth": false,
					"responsive": true,
					"pagingType": pagingType,
					"bInfo" : bInfo,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
		            },
		            "initComplete": function(settings, json) {
		            	if(!_.isUndefined(callback))
		            		callback();
		            },
          			"ajax": function(data, callback, settings) {
          				App.log('fetching agent transaction data: ' + self.id);	
          				var url = self.tdrsUrl + "?" + filter;
						$.extend(data, fdata);
          				var jqxhr = $.ajax(url, {
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
					  "language": {
					  	"emptyTable": "No transactions found.",
						"searchPlaceholder": App.i18ntxt.agentAccounts.viewQuickSearchPlaceholder,
					  },
					  
					  "order": [[ 0, "desc" ]],
                      "columns": [
                    	   {
                    		   data: "number",
                    		   title: tri18ntxt.transactionNo,
                   	           render: function(data, type, row, meta) {
							   		return '<a class="routerlink" href="#transaction/' + row['number'] + '">' + data + '</a>';
							   },
                    	   },
                    	   {
                    		   data: "transactionTypeName",
                    		   title: tri18ntxt.type,
                    		   render: function(data, type, row, meta) {
                    			   return App.translate("enums.transactionType." + data, data); 
                    		   }
                    	   },
                    	   {
                    		   data: "amount",
                    		   title: tri18ntxt.amount,
							   class: "right",
							   defaultContent: "-",
							   render: function(data, type, row, meta) {
								   return CommonUtils.formatNumber(data);
							   }
								   
                    	   },
                    	   {
                    		   data: "buyerTradeBonusAmount",
                    		   title: tri18ntxt.bonus,
							   class: "right",
							   defaultContent: "-",
							   render: function(data, type, row, meta) {
								   return CommonUtils.formatNumber(data);
							   }
                    	   },
                    	   {
                    		   data: "channelName",
                    		   title: tri18ntxt.channel,
							   defaultContent: "-",
                    	   },
                    	   {
                    		   data: "endTimeString",
                    		   title: tri18ntxt.time,
                    	   },
                    	   {
						   	   visible: side == 'A' ? false : true, 
                    		   data: "apartyName",
                    		   title: tri18ntxt.agentA,
							   sortable: false,
							   defaultContent: "-",
                   	           render: function(data, type, row, meta) {
							   		if ( data )
							   			return '<a class="routerlink" href="#account/' + row['a_AgentID'] + '">' + data + '</a>';
									return '-';	
							   },
                    	   },
                    	   {
						   	   visible: side == 'A' ? false : true, 
                    		   data: "a_MSISDN",
                    		   title: tri18ntxt.msisdnA,
							   defaultContent: "-"
                    	   },
                    	   {
						   	   visible: side == 'B' ? false : true, 
                    		   data: "bpartyName",
                    		   title: tri18ntxt.agentB,
							   sortable: false,
							   defaultContent: "-",
                   	           render: function(data, type, row, meta) {
							   		if ( data )
							   			return '<a class="routerlink" href="#account/' + row['b_AgentID'] + '">' + data + '</a>';
									return '-';	
							   },
                    	   },
                    	   {
						   	   visible: side == 'B' ? false : true, 
                    		   data: "b_MSISDN",
                    		   title: tri18ntxt.msisdnB,
							   defaultContent: "-"
                    	   },
                    	   {
                    		   data: "returnCode",
                    		   title: tri18ntxt.code,
                   	           render: function(data, type, row, meta) {
							   		return '<span class="label label-' + (data == 'SUCCESS' ? 'success' : 'danger') + '">' + data + '</span>';
							   },
                    	   }
                    	  ]
                  } );
			},
			initAgentUserTable: function(selector, fdata) {

				$(window).load(this.dropupAdjust);
				$(window).bind('resize scroll touchstart touchmove mousewheel', this.dropupAdjust);

				var self = this;
				var labels = App.i18ntxt.userman;
            	var table = this.$(selector);
            	this.dataTable['agentUsers'] = table.DataTable({
					"processing": true,
					//"serverSide": true,
					"autoWidth": false,
					"responsive": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": "User name, MSISDN, ...",
		            },
          			"ajax": function(data, callback, settings) {
          				App.log('fetching agent user data: ' + self.id);

						self.currentUserFilter.data = data;

						$.extend(data, fdata);
          				var jqxhr = $.ajax('api/ausers/agents/data/'+self.id, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
                      	    callback(dataResponse);
							self.dropupAdjust();
                      	  })
                      	  .fail(function(dataResponse) {
                      		  self.error = dataResponse;
                      			App.error(dataResponse);
								//App.vent.trigger('application:tdrssearcherror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
					  dom: "<'row'<'col-lg-4 col-md-5 col-sm-6'f><'col-lg-8 col-md-7 col-sm-6 right dtAgentUserButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					  "initComplete": function(settings, json) {
		                //if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
					 	self.$('.dtAgentUserButtonBar').html(self.$('#dtButtonBarAgentUsersTemplate').html());
		              },
					  "order": [[ 0, "desc" ]],
                      "columns": [
                	 	   		{
                   		 		   	data: "id",
                   			 	   	title: App.i18ntxt.global.uniqueID,
									class: "all center",
									width: "80px",
                   		 		},
                           	   {
                        		   data: "fullName",
                        		   title: labels.fullNameHead,
    							   class: "all",
    							   render: function(data, type, row, meta) {
    							   		return '<a href="#agentuser/'+row['id']+'" class="routerlink">' + data + '</a>';
    							   }
                        	   },
                        	   {
                        		   data: "mobileNumber",
                        		   title: labels.mobileNumberHead,
    							   defaultContent: "-"
                        	   },
                        	   {
                        		   data: "accountNumber",
                        		   title: labels.accountNumber,
    							   defaultContent: "-"
                        	   },
                        	   {
                        		   data: "department",
                        		   title: labels.departmentHead,
    							   defaultContent: "-"
                        	   },
                        	   {
                        		   data: "role.name",
                        		   title: labels.roleHead,
                        		   defaultContent: "-"
                        	   },
                        	   {
                        		   data: "activationDateFormatted",
                        		   title: labels.actDateHead
                        	   },
                        	   {
                        		   data: "language",
                        		   title: labels.languageHead,
                        		   render: function(data, type, row, meta) {
                        			   return IsoLanguage[data].name;
                        		   }
                        	   },
                        	   {
                        		   data: "state",
                        		   title: labels.statusHead,
    							   className: "center",
    							   //width: "100px"
                        		   render: function(data, type, row, meta) {
                        			   var response = [];
                        			   response.push('<span class="label');
                        			   if (data === 'A') {
                        				   response.push('label-success">');
                        				   response.push(App.i18ntxt.enums.state.active);
                        				   response.push('</span>');
                        			   } else if (data === 'D') {
                        				   response.push('label-danger">');
                        				   response.push(App.i18ntxt.enums.state.deactivated);
                        				   response.push('</span>');
                        			   }
                        			   return response.join(' ');
                        		   }
                        	   },
                        	   {
                      	            targets: -1,
                       	            data: null,
                       	            title: "",
                       	            sortable: false,
    								class: "nowrap right all",
    								width: "95px",
                       	            render: function(data, type, row, meta) {
                       	            	var buttons = [];
                       	            	//TODO: use handlebars template
                   	            		//buttons.push('<button class="btn btn-primary pinResetButton btn-xs">' + App.i18ntxt.global.editBtn+'</button>');
                       	            	if (App.hasPermission("AgentUser", "ResetPin")) {
                           	            	buttons.push("<li style='cursor:pointer;'><a class='resetPinAgentButton'><i class='fa fa-fw fa-key'></i>&nbsp;&nbsp;"+i18ntxt.menuPinResetBtn+"</a></li>");
                       	            	}
                       	            	if (App.hasPermission("AgentUser", "Update")) {
                       	            		buttons.push("<li style='cursor:pointer;'><a class='editAgentUserButton'><i class='fa fa-fw fa-pencil'></i>&nbsp;&nbsp;" + App.i18ntxt.global.editBtn+"</a></li>");
                       	            	}
                       	            	if (App.hasPermission("AgentUser", "Delete")) {
                       	            		buttons.push("<li style='cursor:pointer;'><a class='deleteAgentUserButton'"+(row.state=='P'?' style="visibility:hidden;"':'')+"><i class='fa fa-fw fa-trash'></i>&nbsp;&nbsp;"+App.i18ntxt.global.deleteBtn+"</a></li>");
                       	            	}
										var html = '';
										if (buttons.length > 0) {
											html += '<div class="btn-group" style="width:95px;">';
											html += ' <a class="btn btn-primary btn-xs routerlink" style="padding-left:10px; padding-right:10px; margin-right:0;" href="#agentuser/'+row['id']+'">'+i18ntxt.menuViewBtn+'</a>';
											html += ' <button type="button" class="btn btn-primary btn-xs dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" style="margin-right:0;">';
											html += '    <span class="caret"></span>';
											html += ' </button>';
											html += ' <ul class="dropdown-menu dropdown-menu-right">';
											html += buttons.join(' ');
											html += ' </ul>';
											html += '</div>';
										} else {
											html += '<button class="btn btn-primary btn-xs routerlink" href="#agentuser/'+row['id']+'">';
											html += i18ntxt.menuViewBtn;
											html += '</button>';
										}
    	        	            		return html;
                	            	}
                       	       }
                    	  ]
                  } );
			},

			initApiUserTable: function(selector, fdata) {

				$(window).load(this.dropupAdjust);
				$(window).bind('resize scroll touchstart touchmove mousewheel', this.dropupAdjust);

				var self = this;
				var labels = App.i18ntxt.userman;
            	var table = this.$(selector);
            	this.dataTable['apiUsers'] = table.DataTable({
					"processing": true,
					//"serverSide": true,
					"autoWidth": false,
					"responsive": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json",
						"searchPlaceholder": "User name, MSISDN, ...",
		            },
          			"ajax": function(data, callback, settings) {
          				App.log('fetching agent user data: ' + self.id);

						self.currentUserFilter.data = data;

						$.extend(data, fdata);
          				var jqxhr = $.ajax('api/ausers/api/data/'+self.id, {
          					data: data
          				})
                      	  .done(function(dataResponse) {
                      	    callback(dataResponse);
							self.dropupAdjust();
                      	  })
                      	  .fail(function(dataResponse) {
                      		  self.error = dataResponse;
                      			App.error(dataResponse);
								//App.vent.trigger('application:tdrssearcherror', dataResponse);
                      	  })
                      	  .always(function(data) {
                      	  });
                      },
					  dom: "<'row'<'col-lg-4 col-md-5 col-sm-6'f><'col-lg-8 col-md-7 col-sm-6 right dtApiUserButtonBar'>><'row'<'col-sm-12'tr>><'row'<'col-sm-4'l><'col-sm-4 center'i><'col-sm-4'p>>",
					  "initComplete": function(settings, json) {
		                //if (!tableSettings.searchBox) $('.advancedSearchResults .dataTables_filter label').hide();
					 	self.$('.dtApiUserButtonBar').html(self.$('#dtButtonBarApiUsersTemplate').html());
		              },
					  "order": [[ 0, "desc" ]],
                      "columns": [
                	 	   		{
                   		 		   	data: "id",
                   			 	   	title: App.i18ntxt.global.uniqueID,
									class: "all center",
									width: "80px",
                   		 		},
                           	   {
                        		   data: "fullName",
                        		   title: labels.fullNameHead,
    							   class: "all",
    							   render: function(data, type, row, meta) {
    							   		return '<a href="#agentuser/'+row['id']+'" class="routerlink">' + data + '</a>';
    							   }
                        	   },
                        	   {
                        		   data: "mobileNumber",
                        		   title: labels.mobileNumberHead,
    							   defaultContent: "-"
                        	   },
                        	   {
                        		   data: "accountNumber",
                        		   title: labels.accountNumber,
    							   defaultContent: "-"
                        	   },
                        	   /*{
                        		   data: "email",
                        		   title: labels.email,
    							   defaultContent: "-"
                        	   },*/
                        	   {
                        		   data: "role.name",
                        		   title: labels.roleHead,
                        		   defaultContent: "-"
                        	   },
                        	   {
                        		   data: "activationDateFormatted",
                        		   title: labels.actDateHead
                        	   },
                        	   {
                        		   data: "language",
                        		   title: labels.languageHead,
                        		   render: function(data, type, row, meta) {
                        			   return IsoLanguage[data].name;
                        		   }
                        	   },
                        	   {
                        		   data: "state",
                        		   title: labels.statusHead,
    							   className: "center",
    							   //width: "100px"
                        		   render: function(data, type, row, meta) {
                        			   var response = [];
                        			   response.push('<span class="label');
                        			   if (data === 'A') {
                        				   response.push('label-success">');
                        				   response.push(App.i18ntxt.enums.state.active);
                        				   response.push('</span>');
                        			   } else if (data === 'D') {
                        				   response.push('label-danger">');
                        				   response.push(App.i18ntxt.enums.state.deactivated);
                        				   response.push('</span>');
                        			   }
                        			   return response.join(' ');
                        		   }
                        	   },
                        	   {
                      	            targets: -1,
                       	            data: null,
                       	            title: "",
                       	            sortable: false,
    								class: "nowrap right all",
    								width: "95px",
                       	            render: function(data, type, row, meta) {
                       	            	var buttons = [];
                       	            	//TODO: use handlebars template
                   	            		//buttons.push('<button class="btn btn-primary pinResetButton btn-xs">' + App.i18ntxt.global.editBtn+'</button>');
                       	            	if (App.hasPermission("AgentUser", "ResetPin")) {
                       	            		buttons.push("<li style='cursor:pointer;'><a class='resetPinApiButton'><i class='fa fa-fw fa-key'></i>&nbsp;&nbsp;"+i18ntxt.menuPasswordResetBtn+"</a></li>");
                       	            	}
                       	            	if (App.hasPermission("AgentUser", "Update")) {
                       	            		buttons.push("<li style='cursor:pointer;'><a class='editApiUserButton'><i class='fa fa-fw fa-pencil'></i>&nbsp;&nbsp;" + App.i18ntxt.global.editBtn+"</a></li>");
                       	            	}
                       	            	if (App.hasPermission("AgentUser", "Delete")) {
                       	            		buttons.push("<li style='cursor:pointer;'><a class='deleteApiUserButton'"+(row.state=='P'?' style="visibility:hidden;"':'')+"><i class='fa fa-fw fa-trash'></i>&nbsp;&nbsp;"+App.i18ntxt.global.deleteBtn+"</a></li>");
                       	            	}
										var html = '';
										if (buttons.length > 0) {
											html += '<div class="btn-group" style="width:95px;">';
											html += ' <a class="btn btn-primary btn-xs routerlink" style="padding-left:10px; padding-right:10px; margin-right:0;" href="#agentuser/'+row['id']+'">'+i18ntxt.menuViewBtn+'</a>';
											html += ' <button type="button" class="btn btn-primary btn-xs dropdown-toggle" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false" style="margin-right:0;">';
											html += '    <span class="caret"></span>';
											html += ' </button>';
											html += ' <ul class="dropdown-menu dropdown-menu-right">';
											html += buttons.join(' ');
											html += ' </ul>';
											html += '</div>';
										} else {
											html += '<button class="btn btn-primary btn-xs routerlink" href="#agentuser/'+row['id']+'">';
											html += i18ntxt.menuViewBtn;
											html += '</button>';
										}
    	        	            		return html;
                	            	}
                       	       }
                    	  ]
                  } );
			},
			
			dropupAdjust: function() {
				$(".agentuserstable .dropdown-toggle").each(CommonUtils.adjustDropdownDir);
			},

			loadModels: function() {
            	var self = this;
				this.rolesModel = new Backbone.Model();
            	this.rolesModel.url = 'api/roles/agent/data';

				this.channelTypesModel = new Backbone.Model();
            	this.channelTypesModel.url = 'api/ausers/channel_types';

        		$.when( this.rolesModel.fetch() )
    			.done(function() {
    				self.rolesModel.set( "roles", self.rolesModel.get("data") );
					self.render();
        		});

				$.when(this.channelTypesModel.fetch()).done( function() {
					var channelTypesObj = self.channelTypesModel.attributes;
					var channelTypes = [];
					Object.keys(channelTypesObj)
						.forEach(function eachKey(key) { 
							var channelObj = {
								id: key,
								name: channelTypesObj[key]
							};
							channelTypes.push(channelObj);
						});
					self.channelTypesModel.attributes = {};
					self.channelTypesModel.set("channelTypes", channelTypes);
					self.render();
				});

			},

            onRender: function () {

				if (this.model == null)
				{
            		this.retrieveAgentData();
					return;
            	}

				if (this.rolesModel == null || this.channelTypesModel == null)
				{
					this.loadModels();
					return;
				}

            	var self = this;
            	this.$('#viewSingleAgent .availableOperations').show();
            	var bal = this.model.get("balance");
   	         	if (!_.isUndefined(bal)) {
      		      		var formattedBal = CommonUtils.formatNumber( this.model.get("balance") );
       	     		this.model.set("formatted_balance", formattedBal);
       	     	} else {
       	     		this.model.set("formatted_balance", "----");
       	     	}

				this.$('#dateOfBirth').html(CommonUtils.formatDate(this.model.attributes.dateOfBirth, '<span class="no-data">'+i18ntxt.notSet+'</span>'));
				this.$('#activationDate').html(CommonUtils.formatDate(this.model.attributes.activationDate));
				this.$('#deactivationDate').html(CommonUtils.formatDate(this.model.attributes.deactivationDate, '<span class="no-data">'+i18ntxt.notSet+'</span>'));
				this.$('#expirationDate').html(CommonUtils.formatDate(this.model.attributes.expirationDate, '<span class="no-data">'+i18ntxt.notSet+'</span>'));
				this.$('#lastImeiUpdate').html(CommonUtils.formatDate(this.model.attributes.lastImeiUpdate, '<span class="no-data">'+i18ntxt.notSet+'</span>'));
				//this.$('#lastImsiChange').html(CommonUtils.formatDate(this.model.attributes.lastImsiChange, '<span class="no-data">'+i18ntxt.notSet+'</span>'));

				//self.initTdrTable('.tdrstableA', { agentIDA: self.id }, 'A');
				//self.initTdrTable('.tdrstableB', { agentIDB: self.id }, 'B');
				var filter = self.getFormData();
				self.initTdrTable('.tdrstablex', 'advanced=true&agentID='+self.id+'&'+filter, { agentID: self.id }, 'X', self.enableSearchButton);
				self.initAgentUserTable('.agentuserstable', { agentID: self.id });
				self.initApiUserTable('.apiuserstable', { agentID: self.id });
            },

			retrieveAgentData: function() {
            	var self = this;
            	this.model = new AgentModel({id: self.id});
            	this.model.fetch({
            		success: function(ev){
						var total = 0.0;
                    	var bal = self.model.get("balance");
                    	if (!_.isUndefined(bal)) {
							total += parseFloat(bal);
                    		var formattedBal = CommonUtils.formatNumber( bal );
                    		self.model.set("formatted_balance", formattedBal);
                    	} else {
                    		self.model.set("formatted_balance", "----");
                    	}

                    	//bonusBalance
                    	var bbal = self.model.get("bonusBalance");
                    	if (!_.isUndefined(bbal)) {
							total += parseFloat(bbal);
                    		var formattedBal = CommonUtils.formatNumber( bbal );
                    		self.model.set("formatted_bonusBalance", formattedBal);
                    	} else {
                    		self.model.set("formatted_bonusBalance", "----");
                    	}

						if ( total != 0.0 ) {
                    		var formattedBal = CommonUtils.formatNumber( total );
                    		self.model.set("formatted_totalBalance", formattedBal);
                    	} else {
                    		self.model.set("formatted_totalBalance", "----");
                    	}

						var ohbal = self.model.get("onHoldBalance");
                    	if (!_.isUndefined(ohbal)) {
                    		var formattedBal = CommonUtils.formatNumber( ohbal );
                    		self.model.set("formatted_onHoldBalance", formattedBal);
                    	} else {
                    		self.model.set("formatted_onHoldBalance", "----");
                    	}

						self.render();
					},
				});
			},

            ui: {
                imsiUnlock: '.imsiUnlockButton',
                deleteAccount: '.deleteAccountButton',

                exportTdrs: '.exportTdrsButton',
				createAgentUser: '.createAgentUsersButton',
				createApiUser: '.createApiUsersButton',
				exportUsers: '.exportAgentUsersButton',
				
                editAgentUser: '.editAgentUserButton',
                editApiUser: '.editApiUserButton',
                
				resetPinAgentUser: '.resetPinAgentButton',
				resetPinApiUser: '.resetPinApiButton',
				
                deleteAgentUser: '.deleteAgentUserButton',
                deleteApiUser: '.deleteApiUserButton',

                editAgent: '.editAgentButton',
                suspendAgent: '.suspendAgentButton',
                unsuspendAgent: '.unsuspendAgentButton',
                activateAgent: '.reactivateAgentButton',
                deactivateAgent: '.deactivateAgentButton',
                performTransfer: '.performTransferButton',
                performAdjustment: '.performAdjustmentButton',
                performReplenish: '.performReplenishButton',
                exportAgent: '.exportAgentButton',
                agentPinReset: '.agentPinResetButton',
				transactionsx:	".transactionsXTab",
				search: '.tdrSearchButton',
				tdrSearchReset: '.tdrSearchResetButton',
            },

            // View Event Handlers
            events: {
            	"click @ui.imsiUnlock": 'imsiUnlock',
            	"click @ui.deleteAccount": 'deleteAccount',

            	"click @ui.exportTdrs": 'exportTdrs',
				"click @ui.createAgentUser": 'createAgentUser',
				"click @ui.createApiUser": 'createApiUser',
            	
				//"click @ui.editUser": 'editUser',
            	"click @ui.editAgentUser": 'editAgentUser',
            	"click @ui.editApiUser": 'editApiUser',
            	
            	//"click @ui.pinReset": 'pinReset',
            	"click @ui.resetPinAgentUser": 'resetPinAgentUser',
            	"click @ui.resetPinApiUser": 'resetPinApiUser',
            	
            	"click @ui.changePassword": 'changePassword',
            	
            	//"click @ui.deleteUser": 'deleteUser',
            	"click @ui.deleteApiUser": 'deleteApiUser',
            	"click @ui.deleteAgentUser": 'deleteAgentUser',
            	
				"click @ui.exportUsers": 'exportUsers',

				"click @ui.editAgent": 'editAgent',
            	"click @ui.suspendAgent": 'suspendAgent',
            	"click @ui.unsuspendAgent": 'activateAgent',
            	"click @ui.activateAgent": 'activateAgent',
            	"click @ui.deactivateAgent": 'deactivateAgent',
            	"click @ui.performTransfer": 'performTransfer',
            	"click @ui.performAdjustment": 'performAdjustment',
            	"click @ui.performReplenish": 'performReplenish',
            	"click @ui.exportAgent": 'exportAgent',
            	"click @ui.agentPinReset": 'agentPinReset',
            	
            	"click @ui.transactionsx":	'transactionsXRefresh',
            	"click @ui.search": 'tdrSearch',
            	"click @ui.tdrSearchReset": 'tdrSearchReset',
            },

            exportUsers: function(ev) {
				var self = this;

				var table = this.$('.tableview').DataTable();
				CommonUtils.exportAsCsv(ev, self.url, table.search());
			},

            createAgentUser: function(ev) {
            	var self = this;

            	var model = new AgentUserModel({
            		availRoles: this.rolesModel.attributes.roles,
					agentID: self.id,
					channelUssdAvailable: self.model.get("channelUssd"),
					channelSmsAvailable: self.model.get("channelSms"),
					channelWuiAvailable: self.model.get("channelWui"),
					channelAppAvailable: self.model.get("channelApp"),
					channelApiAvailable: self.model.get("channelApi"),
					mode: 'create'
            	});

            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		title: App.i18ntxt.agentAccounts.addAgentUserTitle,
            		hide: function() {
	        			self
				        .dataTable['agentUsers'].ajax.reload().draw();
	        		},
            		view: AgentUserDialogView,
            		params: {
            			model: model
            		}
            	});
            },
            
            createApiUser: function(ev) {
            	var self = this;

            	var model = new AgentUserModel({
            		availRoles: this.rolesModel.attributes.roles,
					availableChannelTypes: this.channelTypesModel.attributes.channelTypes,
					agentID: self.id,
					channelUssdAvailable: self.model.get("channelUssd"),
					channelSmsAvailable: self.model.get("channelSms"),
					channelWuiAvailable: self.model.get("channelWui"),
					channelAppAvailable: self.model.get("channelApp"),
					channelApiAvailable: self.model.get("channelApi"),
					mode: 'create'
            	});

            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		title: App.i18ntxt.agentAccounts.addApiUserTitle,
            		hide: function() {
	        			self
				        .dataTable['apiUsers'].ajax.reload().draw();
						
	        		},
            		view: ApiUserDialogView,
            		params: {
            			model: model
            		}
            	});
            },

            editAgentUser: function(ev) {
            	var self = this;
            	var currentRow = this.dataTable['agentUsers'].row($(ev.currentTarget).closest('tr'));
            	var userData = _.extend({}, currentRow.data());
            	this.editUser(ev, userData, self.dataTable['agentUsers'], AgentUserDialogView, App.i18ntxt.agentAccounts.editAgentUserTitle);
            	return false;
            },
            
            editApiUser: function(ev) {
            	var self = this;
            	var currentRow = this.dataTable['apiUsers'].row($(ev.currentTarget).closest('tr'));
            	var userData = _.extend({}, currentRow.data());
            	this.editUser(ev, userData, self.dataTable['apiUsers'], ApiUserDialogView, App.i18ntxt.agentAccounts.editApiUserTitle);
            	return false;
            },
            
            editUser: function(ev, userData, userDataTable, view, heading) {
            	var self = this;
            	// url ?!?
            	var model = new AgentUserModel({
            		availRoles: this.rolesModel.attributes.roles,
					agentID: self.id,
					mode: 'update'
            	});

            	model.set(userData);

            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: view,
            		title: CommonUtils.renderHtml(heading, {user: userData.fullName, uniqueID: userData.id}),
            		hide: function() {
	        			userDataTable.ajax.reload().draw();
	        		},
            		params: {
            			model: model
            		}
            	});
            },

            deleteAgentUser: function(ev) {
            	this.deleteUser(ev, this.dataTable['agentUsers'])
            },
            
            deleteApiUser: function(ev) {
            	this.deleteUser(ev, this.dataTable['apiUsers'])
            },
            
            deleteUser: function(ev, userDataTable) {
            	var self = this;
            	var row = $(ev.currentTarget).closest('tr');
            	var clickedRow = userDataTable.row(row);
            	var data = clickedRow.data();

            	App.log( data );

            	if (data.state != "P") {
    	        	CommonUtils.delete({
    	        		itemType: App.i18ntxt.userman.user,
    	        		url: self.ausersUrl+'/'+data.id,
    	        		data: data,
    	        		context: {
    	        			what: App.i18ntxt.userman.user,
    	        			name: data.fullName,
    	        			description: data.mobileNumber
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
            	}
            },

			resetPinAgentUser: function(ev) {
            	this.resetPin(ev, this.dataTable['agentUsers']);
            },
            
            resetPinApiUser: function(ev) {
            	this.resetPin(ev, this.dataTable['apiUsers']);
            },
            
            resetPin: function(ev, userDataTable) {
            	var self = this;            	
            	var clickedRow = userDataTable.row($(ev.currentTarget).closest('tr'));
            	var data = clickedRow.data();

            	// pinResetModalMessage
            	var headingModel = "";
            	var content = "";
            	if(data.authenticationMethod == "A"){
            		headingModel = {name: (data.firstName + " " + data.surname), email: data.email, authenticationMethod: data.authenticationMethod};
                	content = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#passwordResetModalMessage",  headingModel).html();
            	} else {
            		headingModel = {name: (data.firstName + " " + data.surname), msisdn: data.mobileNumber, authenticationMethod: data.authenticationMethod};
            		content = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#pinResetModalMessage",  headingModel).html();
            	}
        		App.vent.trigger('application:dialog', {
	        		text: content,
	        		name: "yesnoDialog",
	        		events: {
	        			"click .yesButton":
	        			function(event) {
	        				var dialog = this;
	        				$.ajax({
	                    	    url: 'api/ausers/pinreset/'+data.id,
	                    	    type: 'PUT',
	                    	    success: function(result) {
	                    	    	var successMessage = ""
	                    	    	if(data.authenticationMethod == "A"){
	                    	    		successMessage = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#passwordResetSuccess",  headingModel).html();
	                    	    	} else {
	                    	    		successMessage = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#pinResetSuccess",  headingModel).html();
	                    	    	}
	                    	    	$(dialog).find(".msg-content").hide();
	                    	    	$(dialog).find(".yesButton").hide();
	                    	    	$(dialog).find(".noButton").text(App.i18ntxt.global.okBtn);
	                    	    	$(dialog).find(".modal-text").html( successMessage );
	                    	    },
	                    	    error: function(error) {
	                    	    	$(dialog).find(".msg-content").hide();
	                    	    	$(dialog).find(".yesButton").hide();
	                    	    	$(dialog).find(".noButton").text(App.i18ntxt.global.cancelBtn);
	                    	    	var tmpModel = new ValidationModel({
	                    	    		form: $(dialog).find("form")
	                    	    	});
	                    	    	$.proxy(tmpModel.defaultErrorHandler(error), tmpModel);
	                    	    }
	                    	});

	        			}
	        		}
        		});
            },

			exportTdrs: function(ev,side,filter) {
				var self = this;
				var table = this.$('.tdrstable'+side);
				var tableObj = table.DataTable();
				CommonUtils.exportAsCsv(ev, 'api/tdrs/search', self.currentFilter[side].data, filter);
            	return false;
			},

            deleteAccount: function(ev) {
            	var self = this;
				var txt = App.i18ntxt.agentAccounts;

            	CommonUtils.delete({
	        		itemType: txt.agentAccount,
	        		url: self.url+self.id,
	        		data: self.model,
	        		context: {
	        			what: txt.agentAccount,
	        			name: self.model.get("firstName") + ' ' + self.model.get("surname"),
	        			description: self.model.get("mobileNumber")
	        		},
	        		rowElement: null,
	        	}, {
	        		success: function(model, response) {
    					window.location = '#accountList';
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
            },

			imsiUnlock: function(ev) {
            	var self = this;

           		App.vent.trigger('application:dialog', {
   	        		text: i18ntxt.imsiUnlockTitle,
   	        		name: "yesnoDialog",
   	        		events: {
   	        			"click .yesButton":
   	        			function(event) {
   	        				$.ajax({
   	                    	    url: self.url+'imsiUnlock/'+self.id,
   	                    	    type: 'PUT',
   	                    	    success: function(result) {
									self.retrieveAgentData();
   	                    	    }
   	                    	});
   	        				this.modal('hide');
   	        			}
   	        		}
           		});
           	},
           	
			transactionsXRefresh: function() {
				App.log( '*** refreshing transactions' );
            	var self = this;
            	self.disableSearchButton();
            	var tdrModel = new TdrModel();
            	var content = CommonUtils.getRenderedTemplate("AgentAccount#TdrsXContent", tdrModel.attributes);
    			$("#transactionsx").html( $(content) );
				
				var tx = this.$('#transactionsx');
				tx.find('#date-from').datepicker({autoclose: true, todayHighlight: true});
				tx.find('#date-to').datepicker({autoclose: true, todayHighlight: true});
				var now = new Date().toJSON().slice(0,10);
				var dateFrom = tx.find('#date-from');
				var dateFromValue = moment().subtract(10, 'days')
				var dateFromString = dateFromValue.format('YYYY-MM-DD');
				tx.find('#date-from').val(moment().subtract(7, 'days').format('YYYY-MM-DD'));
				tx.find('#date-to').val(moment().add(1, 'days').format('YYYY-MM-DD'));
				tx.find('#time-from').val('00:00');
				tx.find('#time-to').val('23:59');
				tx.find('#date-from').mask('9999-99-99');
				tx.find('#date-to').mask('9999-99-99');
				tx.find('#time-from').mask('99:99');
				tx.find('#time-to').mask('99:99');
				tx.find('.time-from-picker').clockpicker({
					placement: 'left',
					align: 'top',
					autoclose: true,
					donetext: 'Done',
				});
				tx.find('.time-to-picker').clockpicker({
					placement: 'left',
					align: 'top',
					autoclose: true,
					donetext: 'Done',
				});
            	
				//checkbox hack:
				tx.find('#withcount').on('change', function(){
					self.$(this).val(self.$(this).is(':checked') ? 'true' : 'false');
				});
				
				tx.find('#withquery').on('change', function(){
					self.$(this).val(self.$(this).is(':checked') ? 'true' : 'false');
				});

				var filter = self.getFormData();
				self.initTdrTable('.tdrstablex', 'agentID='+self.id+'&'+filter, { agentID: self.id }, 'X', self.enableSearchButton);
            },
			
			tdrSearchReset: function(ev) {
            	var self = this;
            	var form = self.$('form')[0];
				form.reset();
			},

            getFormData: function() {
            	var self = this;
            	var criteria = Backbone.Syphon.serialize(self.$( "#searchform" ));
            	var args = "";
            	
            	for (var key in criteria) {
					if ( criteria[key] != "" ) {
    					if (args != "") args += "&";
	    				args += key + "=" + encodeURIComponent(criteria[key]);
					}	
				}
            	self.criteria = criteria;
				return args;
            },
            
            verifyCriteria: function(criteria) {
            	var result = false;
            	var count = 0;
            	/*
            	 * For performance reasons, verify that at least 1 criteria has been set.
            	 */
            	if (!_.isUndefined(criteria) || _.isObject(criteria)) {
            		App.log(JSON.stringify(criteria, null, 2));
            		for (var property in criteria) {
            		    if (criteria.hasOwnProperty(property)) {
            		        switch (property) {
            		        case "relation":
            		        case "withcount":
            		        case "withquery":
            		        	break;
            		        default:
            		        	if (criteria[property].trim().length > 0)
            		        		count++;
            		        	break;
            		        }
            		    }
            		}
            		result = (count > 0);
            	}
            	
            	return result;
            },
            
            tdrSearch: function(ev) {
            	var self = this;
            	if (self.$( "#searchform" ).valid()) {
   	            	self.disableSearchButton();
	        		var url = self.url+'/search?advanced=true&'+self.getFormData();
	        		if (self.verifyCriteria(self.criteria)) {
	        			App.appRouter.navigate(url, {trigger: false, replace: true});
		        		if (!_.isUndefined(self.dataTable) && !_.isUndefined(self.dataTable['X'])) 
		        			self.dataTable['X'].destroy();
		        		else
		        			App.error("Failed to obtain reference to transactions table.");
		        		var options = self.getFormData()
		        		//self.renderTable(options, url);
		        		self.initTdrTable('.tdrstablex', 'advanced=true&'+self.getFormData(), { agentID: self.id }, 'X', self.enableSearchButton);
		        		$('.advancedSearchResults .dataTables_filter label').show();
		        		//self.$('.advancedSearchResults').show();
	        		}
	        		else {
	        			CommonUtils.showOkDialog({
							title: CommonUtils.renderHtml(i18ntxt.invalidSearchCriteriaTitle),
							text: CommonUtils.renderHtml(i18ntxt.invalidSearchCriteriaText),
							callback: function() {
								self.enableSearchButton();
							}
						});
	        		}
	            }
            	return false;
            },

            exportTdrs: function(ev) {
				var self = this;
				if (self.$( "#searchform" ).valid()) {
					var filter = self.getFormData();
					self.criteria.agentID = self.id;
					//var table = this.$('.tdrstable');
					var pos = self.tdrsUrl.indexOf('?')
					var baseUrl= (pos >=0)?self.tdrsUrl.substr(0, pos):self.tdrsUrl;
					CommonUtils.exportAsCsv(ev, baseUrl+'/search', {}, self.criteria, true);
				}
            	return false;
			},
			
            enableSearchButton: function() {
            	self.$('.tdrSearchButton').prop('disabled', false).find('i').removeClass('fa-spinner fa-spin').addClass('fa-search');
            },
            
            disableSearchButton: function() {
            	self.$('.tdrSearchButton').prop('disabled', true).find('i').removeClass('fa-search').addClass('fa-spinner fa-spin');
            },
           	
        });
        return AgentAccountView;
    });
