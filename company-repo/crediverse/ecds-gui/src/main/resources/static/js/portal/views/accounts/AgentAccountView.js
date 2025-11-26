define( ['jquery', 'App', 'backbone', 'marionette', "handlebars", 
         'models/AgentModel', 'models/ProfileModel', 'collections/TierCollection', 
         'views/users/PermanentUserDialogView', 'views/users/ProfileDialogView',
         'utils/CommonUtils', 'models/TdrsXModel', 'moment', 'datatables'],
    function($, App, BackBone, Marionette, Handlebars, 
    		AgentModel, ProfileModel, TierCollection, 
    		PermanentUserDialogView, ProfileDialogView,
    		CommonUtils, TdrsXModel, moment) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.agentAccounts;
        var AgentAccountView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "AgentAccount#accountdetails",
  		  	url: 'papi/agents/',
  		  	tdrsUrl: 'papi/tdrs',
  		  	
  		  	error: null,
			tierList: null,
			id: null,

			dataTable: {
				'A': null,
				'B': null,
				'X': null,
			},
        	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.agentAccounts;
				var navBarText = App.i18ntxt.navbar;

  		  		return {
  		  			heading: navBarText,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: navBarText.agentAccounts,
  		  				href: "#accountList",
						iclass: "fa fa-users"
  		  			}, {
  		  				text: txt.accountPageBC,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},
			
            initialize: function () {
				this.retrieveAgentData();
            },
            
            
            //I don't think this function is used. #DeadCode
			initTdrTable: function(selector, filter, fdata, side) {
				var self = this;
				var tri18ntxt = App.i18ntxt.transactions;
            	var table = this.$(selector);
            	this.dataTable[side] = table.DataTable( {
            		//"searching": false,
					"processing": true,
					"serverSide": true,
					"autoWidth": false,
					"responsive": true,
					"language": {
		                "url": "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
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
							   		return '<span class="label label-' + (data == 'SUCCESS' ? 'success' : 'danger') + '">' + App.translate('enums.returncode.' + data, data) + '</span>';
							   },
                    	   }
                    	  ]
                  } );
			},
			
            onRender: function () {
            	var self = this;
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
            
				//self.initTdrTable('.tdrstablea', '', { agentIDA: self.id }, 'A');
				//self.initTdrTable('.tdrstableb', '', { agentIDB: self.id }, 'B');
				var filter = self.getFormData();
				self.initTdrTable('.tdrstablex', 'agentID='+self.id+'&'+filter, { agentID: self.id }, 'X');
				
				$('label[data-toggle="tooltip"]').tooltip({
     				placement: "right",
	      			//trigger: "manual",
					container:'.advancedSearchForm',
					template: '<div class="tooltip" role="tooltip"><div class="tooltip-inner"></div></div>',
		  		});
				
				this.$('#accountdetails a[data-toggle="tab"]').on('shown.bs.tab', function(e){
					if($(e.target).data('tab'))
						self.dataTable[$(e.target).data('tab')].responsive.recalc(); 
				})
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
        		transactionsx:	".transactionsXTab",
				search: '.tdrSearchButton',
				exportTdrs: '.exportTdrsButton',
        		tdrSearchReset: '.tdrSearchResetButton',
                editAgentButton: '.editProfileAction'
            },

            // View Event Handlers
            events: {
            	"click @ui.imsiUnlock": 'imsiUnlock',
            	"click @ui.deleteAccount": 'deleteAccount',
            	"click @ui.transactionsx":	'transactionsXRefresh',
				"click @ui.search": 'tdrSearch',
				"click @ui.exportTdrs": 'exportTdrs',
				"click @ui.tdrSearchReset": 'tdrSearchReset',
            	"click @ui.editAgentButton": 'editSubAccountProfile'
            },
            
            editSubAccountProfile: function(ev) {
            	var self = this;
            	var userData = this.model.attributes;
            	
            	var model = new ProfileModel();
            	model.url = 'papi/profile/subagent';
            	
            	model.set(userData);
            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: (userData.state == 'P')? PermanentUserDialogView : ProfileDialogView,
            		title: CommonUtils.renderHtml(App.i18ntxt.userman.editUserTitle, {user: userData.domainAccountName, uniqueID: userData.id}),
            		hide: function() {
            			self.model.set(model.attributes);
        				self.render();
            		},
            		params: {
            			model: model
            		}
            	});
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
   	                    	    url: self.url+'/imsiUnlock/'+self.id,
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
            	var tdrsXModel = new TdrsXModel();
            	var content = CommonUtils.getRenderedTemplate("AgentAccount#TdrsXContent", tdrsXModel.attributes);
    			$("#transactionsx").html( $(content) );
				
				var tx = $('#transactionsx');
				tx.find('#date-from').datepicker({autoclose: true, todayHighlight: true});
				tx.find('#date-to').datepicker({autoclose: true, todayHighlight: true});
				var now = new Date().toJSON().slice(0,10);
				tx.find('#date-from').val(moment().subtract(7, 'd').format('YYYY-MM-DD'));
				tx.find('#date-to').val(now);
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
				self.initTdrTable('.tdrstablex', 'agentID='+self.id+'&'+filter, { agentID: self.id }, 'X');
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
            
			tdrSearch: function(ev) {
            	var self = this;
            	if (self.$( "#searchform" ).valid()) {
            		enableTdrSearchButton(false);
					var filter = self.getFormData();
	        		var url = self.url+'/search?'+filter;
	        		//if (self.verifyCriteria(self.criteria)) {
	        			//App.appRouter.navigate(url, {trigger: false, replace: true});
		        		//if (!_.isUndefined(self.dataTables['X'])) self.dataTables['X'].destroy();
		        		if (!_.isUndefined(self.dataTable['X'])) self.dataTable['X'].ajax.url(self.tdrsUrl + "?" + 'agentID='+self.id+'&'+filter).load(function() {
		        			enableTdrSearchButton(true);
		        			});
						else self.initTdrTable('.tdrstablex', 'agentID='+self.id+'&'+filter, { agentID: self.id }, 'X');
		        		//$('.advancedSearchResults .dataTables_filter label').show();
		        		//self.$('.advancedSearchResults').show();
	        		//}
	        		//else {
	        		//	CommonUtils.showOkDialog({
					//		title: CommonUtils.renderHtml(i18ntxt.invalidSearchCriteriaTitle),
					//		text: CommonUtils.renderHtml(i18ntxt.invalidSearchCriteriaText),
					//		callback: function() {}
					//	});
	        		//}
	            }
            	return false;
            },
            
            enableTdrSearchButton: function(isEnabled){
            	if(isEnabled)
            		self.$('.tdrSearchButton').prop('disabled', false).find('i').removeClass('fa-spinner fa-spin').addClass('fa-search');
            	else 
            		self.$('.tdrSearchButton').prop('disabled', true).find('i').removeClass('fa-search').addClass('fa-spinner fa-spin');
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
        });
        return AgentAccountView;
    });
