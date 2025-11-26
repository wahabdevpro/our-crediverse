define( ['jquery', 'App', 'backbone', 'marionette', 'views/accounts/AgentAccountsTableView',
         'collections/TierCollection',
         'views/users/PermanentUserDialogView', 'views/users/ProfileDialogView',
         'models/ProfileModel',
         'utils/CommonUtils'],
    function($, App, BackBone, Marionette, AgentAccountsTableView, TierCollection,
    		PermanentUserDialogView, ProfileDialogView,
    		ProfileModel, CommonUtils) {


		var i18ntxt = App.i18ntxt.agentAccounts;
        var AgentAccountsView =  AgentAccountsTableView.extend( {
        	template: "AgentAccounts#accountmaster",
  		  	url: 'papi/agents',
  		  	error: null,
			tierList: null,
			model: null,
			tagName: 'div',
			attributes: {
        		id: "accountmaster"
        	},

        	i18ntxt: null,

        	ui: {
        		accountSearch: '.accountSearchButton',
				accountSearchExpand: '.accountSearchExpandButton',
        		accountSearchCancel: '.accountSearchCancelButton',
        		accountSearchReset: '.accountSearchResetButton',
                searchInput: '.advancedSearchInput .searchInput',
                role: '',
                createAgent: '.createAgentButton',
                editAgent: '.editAgentButton',
                suspendAgent: '.suspendAgentButton',
                unsuspendAgent: '.unsuspendAgentButton',
                activateAgent: '.reactivateAgentButton',
                deactivateAgent: '.deactivateAgentButton',
                performTransfer: '.performTransferButton',
                performAdjustment: '.performAdjustmentButton',
                performReplenish: '.performReplenishButton',
                exportAgent: '.exportAgentButton',
                pinReset: '.pinResetButton'
            },

            events: {
            	"click @ui.accountSearch": 'accountSearch',
            	"click @ui.accountSearchExpand": 'displayAdvancedSearch',
            	"click @ui.accountSearchCancel": 'accountSearchCancel',
            	"click @ui.accountSearchReset": 'accountSearchReset',
            	"focus @ui.searchInput": 'displayAdvancedSearch',
            	"click @ui.role": 'viewRole',
            	"click @ui.createAgent": 'createAgent',
            	"click @ui.editAgent": 'editSubAccountProfile',
            	"click @ui.suspendAgent": 'suspendAgent',
            	"click @ui.unsuspendAgent": 'activateAgent',
            	"click @ui.activateAgent": 'activateAgent',
            	"click @ui.deactivateAgent": 'deactivateAgent',
            	"click @ui.performTransfer": 'performTransfer',
            	"click @ui.performAdjustment": 'performAdjustment',
            	"click @ui.performReplenish": 'performReplenish',
            	"click @ui.exportAgent": 'exportAgent',
            	"click @ui.pinReset": 'pinReset'
            	//"click @ui.deleteAgent": 'deleteAgent',
            },

            displayAdvancedSearch: function(ev) {
            	$('.advancedSearchInput').hide();
            	$('.advancedSearchForm').slideDown({
            		duration: 'slow',
            		easing: 'linear',
            		start: function() {
            			$('.advancedSearchForm #firstName').focus();
            		},
            		complete: function() {
            		}
            	});

            	/*$('.advancedSearchForm').fadeIn({
            		duration: '5000',
            		easing: 'easeInBounce'
            	});*/
            },

            hideAdvancedSearch: function(ev) {
            	$('.advancedSearchInput').show().focus();
            	$('.advancedSearchForm').slideUp({
            		duration: 'slow',
            		easing: 'linear',
            		start: function() {
            		},
            		complete: function() {
            		}
            	});
            },

  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.agentAccounts;
  		  		var navBarText = App.i18ntxt.navbar;

  		  		return {
  		  			heading: navBarText.agentAccounts,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: navBarText.agentAccounts,
  		  				href: "#accountList",
						iclass: "fa fa-users"
  		  			}]
  		  		}
  		  	},

            initialize: function (options) {
            	var self = this;
            	if (!_.isUndefined(options)) this.i18ntxt = options;

            	this.url = this.url + "/owned";

				App.vent.on('operations:success', function(options) {
					// Not doing anything at present
        			self.dataTable.ajax.reload().draw();
				}, this);
            },

            loadData: function() {
            	var self = this;
  		  		var tiers = new TierCollection();
	  		  	$.when( tiers.fetch())
	        	.done(function() {
	        		function filterTiers(val){
						return (val.type != ".") && (val.type != "S");
					}
	        		self.model= new Backbone.Model();
	        		try {
	        			var tierData = tiers.toJSON();
	        			self.model.set('tierList', tierData.filter(filterTiers));
	        		} catch(err) {
	        			App.error(err);
	        		}
	    			self.render();
	        	});
            },

            getAjaxConfig: function(type) {
            	var config = {
            			type: "GET",
            			url: "api/"+type+"/dropdown",
            		    dataType: 'json',
            		    //contentType: "application/json",
            		    delay: 250,
            		    //data: selectedItem,
                        processResults: function (data) {
                            return {
                                results: $.map(data, function (item, i) {
                                    return {
                                        text: item,
                                        id: i
                                    }
                                })
                            };
                        }
            		};
            	return config;
            },

            configureSearchForm: function() {
            	$('#tierID').select2();

            	var groupID = $('#groupID');
				var baseUrl = '#accountList';

				var ajaxConfig =  this.getAjaxConfig('groups');
            	groupID.select2({
            		ajax: ajaxConfig,
            		minimumInputLength: 0,
					allowClear: true,
					placeholder: i18ntxt.selectGroupHint,
            	});
            	groupID.data('config', ajaxConfig);

            	var serviceClassID = $('#serviceClassID');

            	ajaxConfig =  this.getAjaxConfig('serviceclass');

            	serviceClassID.select2({
            		ajax: ajaxConfig,
            		minimumInputLength: 0,
					allowClear: true,
					placeholder: i18ntxt.selectServiceClassHint,
            	});
            	serviceClassID.data('config', ajaxConfig);

            	var pcs = window.location.hash.indexOf(baseUrl) == 0 ? window.location.hash.substr(baseUrl.length).split('/') : [];
            	var form = $('form');

            	setTimeout(function(){
					if (CommonUtils.urlDecodeForm(pcs, form)) {
						self.accountSearch({encode: false});
					}
				}, 100);

//				this.getSelect2Data("supplierAgentID", "api/agents/dropdown", 2, i18ntxt.searchSupplierAgentHint);
//            	this.getSelect2Data("ownerAgentID", "api/agents/dropdown", 2, i18ntxt.searchOwnerAgentHint);
            },

            editSubAccountProfile: function(ev) {
            	var self = this;
            	var clickedRow = this.dataTable.row($(ev.currentTarget).closest('tr'));
            	var userData = clickedRow.data();

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

            getSelect2Data: function(elementID, ajaxurl, minLength, placeholderText) {
				var jqElement = $('#' + elementID);
				jqElement.select2({
            		ajax: {
            			type: "GET",
            		    url: ajaxurl,
            		    dataType: 'json',
            		    //contentType: "application/json",
            		    delay: 250,
            		    //data: selectedItem,
                        processResults: function (data) {
                            return {
                                results: $.map(data, function (item, i) {
                                    return {
                                        text: item,
                                        id: i
                                    }
                                })
                            };
                        }
            		},
            		minimumInputLength: 2,
					allowClear: true,
					placeholder: placeholderText,
            	});
				return jqElement;
			},

            onRender: function () {
            	if (this.model == null) {
            		this.loadData()
            	} else {
            		this.configureSearchForm();
            		try {
            			this.renderTable();
            		} catch(err) {
            			App.error(err);
            		}
            	}
            },

            getFormData: function() {
            	var self = this;
            	var criteria = Backbone.Syphon.serialize($('form'));
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

            exportAgent: function(ev) {
				var self = this;

				var table = this.$('.accountstable');
				var pos = self.url.indexOf('?')
				var baseUrl= (pos >=0)?self.url.substr(0, pos):self.url;
				CommonUtils.exportAsCsv(ev, 'papi/agents/search', self.currentFilter.data, self.criteria);
			},

			accountSearchCancel: function(ev) {
            	var self = this;
				this.hideAdvancedSearch();
            	return false;
            },

			accountSearchReset: function(ev) {
            	var self = this;
            	var form = self.$('form')[0];
				form.reset();
				self.$("#groupID,#serviceClassID,#tierID,#state").val(null).trigger("change");
            	var ajax = self.dataTable.ajax;
        		var url = 'papi/agents/search';
        		ajax.url(url).load( function(){}, true );
			},

            accountSearch: function(ev) {
            	var self = this;
            	//if (_.isUndefined(ev.encode)) App.appRouter.navigate('#accountSearch' + CommonUtils.urlEncodeForm($('form')), {trigger: false, replace: true});

            	var ajax = this.dataTable.ajax;
        		var url = 'papi/agents/search?'+self.getFormData();
        		ajax.url(url).load( function(){}, true );
        		//$('.advancedSearchResults .dataTables_filter label').show();
            	//self.renderTable(self.getFormData());
            	return false;
            }
        });
        return AgentAccountsView;
    });
