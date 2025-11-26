define( ['jquery', 'underscore', 'App', 'marionette', 'models/ProfileModel', 'models/SummaryModel', 
         'models/TransferModel','models/SelfTopupModel','models/PinChangeModel', 'models/ProfileModel',
         'views/operations/TransferFundsDialogView', 'views/operations/SelfTopupDialogView', 'views/operations/PinChangeDialogView',
         'views/users/PermanentUserDialogView', 'views/users/ProfileDialogView',
         'utils/CommonUtils', 'models/TdrsAModel', 'models/TdrsBModel', 'models/TdrsXModel', 'views/PasswordChangeDialogView', 'models/PasswordChangeModel', 'moment'],
    function($, _, App, Marionette, ProfileModel, SummaryModel, 
    		TransferModel, SelfTopupModel, PinChangeModel, ProfileModel,
    		TransferFundsDialogView, SelfTopupDialogView, PinChangeDialogView,
    		PermanentUserDialogView, ProfileDialogView,
    		CommonUtils, TdrsAModel, TdrsBModel, TdrsXModel, PasswordChangeDialogView, PasswordChangeModel, moment) {
        //ItemView provides some default rendering logic
        var DashboardView =  Marionette.LayoutView.extend( {
        	tagName: 'div',
        	tdrsUrl: 'papi/tdrs',
        	
        	dataTables : new Array(),

        	attributes: {
        		class: "row"
        	},
        	
        	regions: {
        		rootAccount: ".rootAccountDashBoard",
        		lastTransactions: ".lastTransactionsDashBoard"
        	},
        	
  		  	template: "Profile#accountdetails",
  		  	
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.profile;
  		  		return {
  		  			heading: txt.heading,
  		  			breadcrumb: [{
  		  				text: txt.pageBC,
  		  				href: "#profile",
  		  				iclass: "fa fa-dashboard"
  		  			}]
  		  		}
  		  	},
  		  	
  		  	initialize: function () {
  		  		var self = this;
				App.vent.on('operations:success', function(options) {
					var profileModel = new ProfileModel();
	              	profileModel.fetch({
	            		success: function() {
		              		self.updateProfileModel(profileModel);
	            			var content = CommonUtils.getRenderedTemplate("Profile#balanceSummary", profileModel.attributes);
	            			$(".balanceSummaryContent").html( $(content) );		            			
	            		}
	            	});
	              	var isSummaryTabActive = $('#salessummary').hasClass("active");
	              	var isTransactionsATabActive = $('#transactionsA').hasClass("active");
	              	if(isSummaryTabActive){
	              		self.salessummary();
	              	} else if(isTransactionsATabActive) {
	              		self.transactionsARefresh();
	              	}
				}, this);
            },
            
            ui: {
                dashview: '',
                transfer: '.transferbtn',
                selftopup: '.selftopupbtn',
                pinchange: '.pinchangebtn',
                changepassword: '.changepasswordbtn',
        		salessummary:	".salessummary",
        		transactionsx:	".transactionsXTab",
				search: '.tdrSearchButton',
				exportTdrs: '.exportTdrsButton',
        		tdrSearchReset: '.tdrSearchResetButton',
        		editProfile: '.editProfileAction'
            },

            // View Event Handlers
            events: {
            	"click @ui.dashview": 		'dashview',
            	"click @ui.transfer": 		'performTransfer',
            	"click @ui.selftopup": 		'performSelfTopup',
            	"click @ui.pinchange": 		'performPinChange',
            	"click @ui.salessummary":	'salessummary',
            	"click @ui.transactionsx":	'transactionsXRefresh',
            	"click @ui.editProfile":	'editProfile',
				"click @ui.search": 'tdrSearch',
				"click @ui.exportTdrs": 'exportTdrs',
				"click @ui.tdrSearchReset": 'tdrSearchReset',
            	"click @ui.changepassword":	'performPasswordChange'
            },
            
            dashview: function() {
            },
            
            editProfile: function() {
                	var self = this;
                	var userData = this.model.attributes;
                	
                	// url ?!?
                	var model = new ProfileModel();
                	
                	model.set(userData);
                	
                	App.vent.trigger('application:dialog', {
                		name: "viewDialog",
                		view: (userData.state == 'P')? PermanentUserDialogView : ProfileDialogView,
                		title: CommonUtils.renderHtml(App.i18ntxt.userman.editUserTitle, {user: userData.domainAccountName, uniqueID: userData.id}),
                		//title: CommonUtils.renderHtml("title"),
                		hide: function() {
                			if (_.isUndefined(self.dataTable)) {
                				self.model.set(model.attributes);
                				self.render();
                			}
                			else {
                				self
                    			.dataTable.ajax.reload().draw();
                			}
                		},
                		params: {
                			model: model
                		}
                	});
                	return false;
            },
            
            salessummary: function() {
            	var self = this;
            	var summaryModel = new SummaryModel();
              	summaryModel.fetch({
            		success: function() {
            			var content = CommonUtils.getRenderedTemplate("Profile#summaryContent", summaryModel.attributes);
            			$(".salessummaryTabContent").html( $(content) );
            			self.mustUpdateTableA = true;
            		}
            	});
            },

            transactionsXRefresh: function() {
            	var self = this;
            	var tdrsXModel = new TdrsXModel();
            	var content = CommonUtils.getRenderedTemplate("Profile#TdrsXContent", tdrsXModel.attributes);
    			$("#transactionsX").html( $(content) );
				
				var tx = $('#transactionsX');
				tx.find('#date-from').datepicker({autoclose: true, todayHighlight: true});
				tx.find('#date-to').datepicker({autoclose: true, todayHighlight: true});
				var now = new Date().toJSON().slice(0,10);
				tx.find('#date-from').val(moment().subtract(10, 'd').format('YYYY-MM-DD'));
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
    			self.initTransfersTable('.tdrstablex', 'myTransactions?'+filter, 'X', null);            	
            },
            
            retrieveAgentData: function() {
            	var self = this;
            	
            	this.model = new ProfileModel();
            	$.when( self.model.fetch() )
            		.done(function() {
            			self.updateProfileModel(self.model);
						self.render();
            		});
			},
			
			updateProfileModel: function(profileModel) {
				try {
	    			//Set Defaults
					profileModel.set("formatted_balance", "----");
					profileModel.set("formatted_bonusBalance", "----");
					profileModel.set("formatted_totalBalance", "----");
	    			
	    			// Set Balances
					var total = 0.0;
	            	var bal = profileModel.get("balance");
	            	if (!_.isUndefined(bal)) {
						total += parseFloat(bal);
	            		var formattedBal = CommonUtils.formatNumber( bal );
	            		profileModel.set("formatted_balance", formattedBal);
	            	}
	            	
	            	// Set Balances
					var total = 0.0;
	            	var bal = profileModel.get("balance");
	            	if (!_.isUndefined(bal)) {
						total += parseFloat(bal);
	            		var formattedBal = CommonUtils.formatNumber( bal );
	            		profileModel.set("formatted_balance", formattedBal);
	            	}
	            	
	            	var bbal = profileModel.get("bonusBalance");
	            	if (!_.isUndefined(bbal)) {
						total += parseFloat(bbal);
	            		var formattedBal = CommonUtils.formatNumber( bbal );
	            		profileModel.set("formatted_bonusBalance", formattedBal);
	            	}
	            	
	            	if ( total != 0.0 ) {
	            		var formattedBal = CommonUtils.formatNumber( total );
	            		profileModel.set("formatted_totalBalance", formattedBal);
	            	}
					
	            	var ohbal = profileModel.get("onHoldBalance");
	            	if (!_.isUndefined(ohbal)) {
	            		var formattedBal = CommonUtils.formatNumber( ohbal );
	            		profileModel.set("formatted_onHoldBalance", formattedBal);
	            	}
	            	
					// Update Gender
					var gender = profileModel.get("gender");
					if (!_.isUndefined(gender)) {
						var sLang = "enums.sexType." + gender.toLowerCase();
						profileModel.set("genderName", App.translate(sLang, gender));
					}
					
					var tierType = profileModel.get("tierTypeCode");
					if (!_.isUndefined(tierType)) {
						if (tierType == ".")
							tierType = App.translate("enums.tierType.root");
						else if (tierType == "T")
							tierType = App.translate("enums.tierType.store");
						else if (tierType == "W")
							tierType = App.translate("enums.tierType.wholesaler");
						else if (tierType == "R")
							tierType = App.translate("enums.tierType.retailer");
						else if (tierType == "S")
							tierType = App.translate("enums.tierType.subscriber");
						profileModel.set("tierType", tierType);
					}
				} catch(err) {
					App.error(err);
				}
			},
			
            onRender: function () {     
				var self = this;
            	if (_.isUndefined(this.model)) {
            		this.retrieveAgentData();
            	}        		
				
				$('label[data-toggle="tooltip"]').tooltip({
     				placement: "right",
	      			//trigger: "manual",
					container:'.advancedSearchForm',
					template: '<div class="tooltip" role="tooltip"><div class="tooltip-inner"></div></div>',
		  		});
  		  	},

			initTransfersTable: function(selector, fdata, side, complete) {
				var self = this;
				var tri18ntxt = App.i18ntxt.transactions;
            	var table = this.$(selector);
            	
            	if(!_.isUndefined(self.dataTables[side]))
            		self.dataTables[side].destroy();

            	var withcount = this.$('#withcount').val();
            	var pagingType = "simple";
				var bInfo = false;
            	if(withcount == 'true'){
    				pagingType = "full_numbers";
    				bInfo = true;	        				
    			}
            	
            	var ajaxConfig = {
            			type: "GET",
            			url: "papi/tdrs/transactionTypes",
            		    dataType: 'json',
            		    processResults: function (data) {
            	            return {
            	                results: $.map(data, function (item) {
            	                    return {
            	                        text: App.translate("enums.transactionType." + item.text, item.text),
            	                        id: item.id
            	                    }
            	                })
            	            };
            	        }
            		};
            	var dataTable = table.DataTable( {
            		//"searching": false,
					"processing": true,
					"serverSide": true,
					"autoWidth": false,
					"responsive": true,
					"pagingType": pagingType,
					"bInfo" : bInfo,
					language: {
		                url: "js/lib/datatables/lang/"+App.i18ntxt.languageName+".json"
		            },
          			"ajax": function(data, callback, settings) {
          				var url = self.tdrsUrl + "/" + fdata;
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
						"searchPlaceholder": App.i18ntxt.profile.viewQuickSearchPlaceholder,
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
                    	  ],
                       	  "initComplete": function(settings, json) {
                       		if (!_.isUndefined(complete) && _.isFunction(complete)) {
                       			complete(ajaxConfig, table.DataTable(), settings, json, side, fdata);
                       		}
                     	  }
                  } );
            	self.dataTables[side] = dataTable;
			},
			
			initTransactionFilter: function(ajaxConfig, dataTable, settings, json, side, fdata){
				var currentTable = dataTable;
				var filterInput =  $('#transactions' + side + ' .dataTables_filter');
				filterInput.html('<label>Filter By Type &nbsp;<select id="transactions' + side + 'Type" name="transactions' + side + 'Type" class="transactionType form-control" style="width: 300px"></select></label>');
				var typeList = $('#transactions' + side + ' .transactionType');
				
				typeList.data('config', ajaxConfig);
				typeList.select2({
					ajax: ajaxConfig,
					minimumInputLength: 0,
					allowClear: true, 
					placeholder: "Select Type",
				}).on('change', function(ev){
					var ajax = dataTable.ajax;
					var type = $(ev.target).val();
					var url = 'papi/tdrs/' + fdata;
					if(!_.isUndefined(type) && type !== null){
						var url = 'papi/tdrs/' + fdata +'?type='+type;
					}
					ajax.url(url).load( function(){}, true );
				})
				//Selectbox workaround:
				//Stop the selectbox from dropping down when it is cleared.
				.on('select2:unselecting', function() { 					
				    $(this).data('unselecting', true);
				}).on('select2:opening', function(e) {
				    if ($(this).data('unselecting')) {
				        $(this).removeData('unselecting');
				        e.preventDefault();
				    }
				});
				//Selectbox workaround end.
			},

			showViewDialog : function(model, viewClass) {
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: viewClass,
            		params: {
            			model: model
            		}
            	});
				
			},
			
            performTransfer: function() {
            	var model = new TransferModel();
            	this.showViewDialog(model, TransferFundsDialogView);
            },
            
            performSelfTopup: function() {
            	App.log("performSelfTopup");
            	
            	var model = new SelfTopupModel();
            	this.showViewDialog(model, SelfTopupDialogView);
            },

            performPinChange: function() {
            	var self = this;
            	App.log("performPinChange");
            	
            	var model = new PinChangeModel();
            	model.fetch({
            		url: "papi/dashboard/pinrules",
            		success: _.bind(function() {
            			model.updateRules();
            			this.showViewDialog(model, PinChangeDialogView);            			
            		}, this)
        		});          	
            },
            
            performPasswordChange : function() {
            	var self = this;
            	var data = this.model.attributes;
            	var model = new PasswordChangeModel();
            	var url = "";
            	var entityType = ""
            	if(!_.isUndefined(data.agentUser)){
            		model.attributes.entityId = data.agentUser.id;
            		url = '/papi/agents/change_password';
            		entityType = 'AGENTUSER';
            	} else {
            		model.attributes.entityId = data.id;
            		url = '/papi/agents/change_password';
            		entityType = 'AGENT';
            	}
            	
            	App.log("performPasswordChange");

            	model.fetch({
            		url: "/papi/agents/passwordrules",
            		success: _.bind(function() {
            			model.updateRules();
            			model.entityType = entityType;
            			App.vent.trigger('application:dialog', {
                    		name: "viewDialog",
                    		title:  CommonUtils.renderHtml(App.i18ntxt.changePasswordDialog.changePassword, {minPinLength: model.attributes.minPasswordLength}),
                    		hide: function() {},
                    		view: PasswordChangeDialogView,
                    		params: {
                    			model: model,
                    			url: '/papi/agents/change_password'
                    		}
                    	});

            		}, this)
        		});
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
					self.$('.tdrSearchButton').prop('disabled', true).find('i').removeClass('fa-search').addClass('fa-spinner fa-spin');
					var filter = self.getFormData();
	        		var url = self.url+'/search?'+filter;
					self.initTransfersTable('.tdrstablex', 'myTransactions?'+filter, 'X', function() {
    					self.$('.tdrSearchButton').prop('disabled', false).find('i').removeClass('fa-spinner fa-spin').addClass('fa-search');
    				} ); 
	            }
            	return false;
            },
            
			exportTdrs: function(ev) {
				var self = this;
				if (self.$( "#searchform" ).valid()) {
					var filter = self.getFormData();
					//var table = this.$('.tdrstable');
					var pos = self.tdrsUrl.indexOf('?')
					var baseUrl= (pos >=0)?self.tdrsUrl.substr(0, pos):self.tdrsUrl;
					CommonUtils.exportAsCsv(ev, baseUrl+'/search', {}, self.criteria, true);
				}
            	return false;
			},
        });
        return DashboardView;
    });
