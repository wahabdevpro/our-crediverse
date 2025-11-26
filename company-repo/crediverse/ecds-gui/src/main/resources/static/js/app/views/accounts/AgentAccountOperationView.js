define( ['jquery', 'App', 'backbone', 'marionette',
         "views/accounts/AgentAccountsDialogView", "handlebars", "models/ValidationModel",
         'utils/HandlebarHelpers', 'models/AgentModel', 'models/AdjustmentModel', 'models/TransferModel',
         'collections/TierCollection', 'views/accounts/TransferDialogView',
         'views/accounts/AdjustmentDialogView',
         'models/RootAccountModel', 'views/ReplenishView', 'utils/CommonUtils',
         'views/PasswordChangeDialogView', 'models/PasswordChangeModel', 'datatables'],
    function($, App, BackBone, Marionette, AgentAccountsDialogView, Handlebars, ValidationModel,
    		HBHelper, AgentModel, AdjustmentModel, TransferModel, TierCollection, TransferDialogView, AdjustmentDialogView,
    		RootAccountModel, ReplenishView, CommonUtils, PasswordChangeDialogView, PasswordChangeModel) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.agentAccounts;

	        var AgentAccountOperationView =  Marionette.ItemView.extend( {
	        	baseUrl: 'api/agents',
	        	dataFromEvent: function(ev) {
	        		var self = this;
	        		var checkType = this.$('#viewSingleAgent');
	            	var data = {};
	            	if (checkType.length == 1) {
	            		data = this.model.attributes;
	            		data.row = checkType;
	            		data.redraw = function() {
	            			Backbone.history.loadUrl(Backbone.history.fragment);
		            	}
	            	}
	            	else {
	            		var row = $(ev.currentTarget).closest('tr');
	            		var clickedRow = this.dataTable.row(row);
		            	data = clickedRow.data();
		            	data.row = row;
		            	data.redraw = function() {
		            		data.row.fadeOut("slow", function() {
			            		clickedRow.remove().draw();
			            	});
		            	}
	            	};
	            	return data;
	        	},

	        	editAgent: function(ev) {
	            	var self = this;
	            	var data = this.dataFromEvent(ev);

	            	var model = new AgentModel({
	            		id: data.id,
						mode: 'update',
						rules : {
							'accountNumber'  : {
								required : true
							}
						}
	            	});
	            	model.set(data);

	            	App.vent.trigger('application:dialog', {
	            		name: "viewDialog",
	            		view: AgentAccountsDialogView,
						class: model.attributes.state != 'P' ? 'modal-lg modal-xl' : '',
						title:CommonUtils.renderHtml(App.i18ntxt.agentAccounts.editModalTitle, {name: data.firstName + ' ' + data.surname, uniqueID: data.id}),
	            		hide: function() {
	            			data.redraw();
		        		},
	            		params: {
	            			model: model
	            		}
	            	});

	            	return false;
	            },

	            performTransfer: function(ev) {
	            	var self = this;
	            	var data = this.dataFromEvent(ev);

	            	// Model data (MSISDN) required for transfer
	            	var model = new TransferModel();
	            	model.set(data);

	            	// Open Dialog and request Amount
	            	App.vent.trigger('application:dialog', {
	            		name: "viewDialog",
	            		view: TransferDialogView,
	            		params: {
	            			model: model,
	            			table: data
	            		}
	            	,
            		hide: function() {
            			data.redraw();
	        		}
	            	});

	            	return false;
	            },

	            performAdjustment: function(ev) {
	            	var self = this;
	            	var data = this.dataFromEvent(ev);
	            	// Model data (MSISDN) required for adjustment
	            	var model = new AdjustmentModel({
	            		url: 'api/transactions/adjustment'
	            	});
	            	model.set(data);

	            	// Open Dialog and request Amount
	            	App.vent.trigger('application:dialog', {
	            		name: "viewDialog",
	            		view: AdjustmentDialogView,
	            		//backdrop: true,
	            		//view:"views/roles/PermissionView",
	            		params: {
	            			model: model,
							table: data
	            		},
	            		hide: function() {
	            			data.redraw();
		        		}
	            	});

	            	return false;
	            },

				performReplenish: function(ev) {
	            	var self = this;
	            	var data = this.dataFromEvent(ev);
	            	var model = new RootAccountModel({
	            		url: this.url
	            	});
	            	model.set({amount : 0});
	            	App.vent.trigger('application:dialog', {
	            		name: "viewDialog",
	            		view: ReplenishView,
	            		//backdrop: true,
	            		//view:"views/roles/PermissionView",
	            		params: {
	            			model: model
	            		},
	            		hide: function() {
	            			data.redraw();
		        		}
	            	});

	            	return false;
	            },

	            agentPinReset: function(ev) {
	            	var self = this;
	            	var data = this.dataFromEvent(ev);
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
	            	if (true) {
	            		App.vent.trigger('application:dialog', {
	    	        		text: content,
	    	        		name: "yesnoDialog",
	    	        		events: {
	    	        			"click .yesButton":
	    	        			function(event) {
	    	        				var dialog = this;
	    	        				$.ajax({
	    	                    	    url: self.baseUrl+'/pinreset/'+data.id,
	    	                    	    type: 'PUT',
	    	                    	    success: function(result) {
	    	                    	    	//var successMessage = CommonUtils.getRenderedTemplate( "AgentAccount#pinResetSuccess",  headingModel).html();
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
	    	                    	    	data.redraw();
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
	            	}
	            },

	            suspendAgent: function(ev) {
	            	var self = this;
	            	var data = this.dataFromEvent(ev);
	            	if (data.currentState === "ACTIVE") {
	            		CommonUtils.delete({
	    	        		title: i18ntxt.suspendAgentMessage,
	    	        		msg: "Suspend User account <b>{{account}}</b> ({{name}} {{surname}})",
	    	        		context: {
	    	        			account: data.accountNumber,
	    	        			name: data.firstName,
	    	        			surname: data.surname
	    	        		},
	    	        		url: self.baseUrl+'/suspend/'+data.id,
	    	        		actionBtnText: "Suspend",
	    	        		actionBtnClass: "btn-warning",
	    	        		modalType: CommonUtils.modalType.put,
	    	        		data: data,
	    	        		rowElement: data.row,
	    	        		highlightCss: "warningHighlight",
	    	        	}, {
	    	        		success: function(model, response) {
	    	        			data.redraw();
	    	        		},
	    	        		error: function(model, response) {
	    	        			App.error(reponse);
	    	        		}
	    	        	});
	            	}
	            },

	            activateAgent: function(ev) {
	            	var self = this;
	            	var data = this.dataFromEvent(ev);
	            	if (data.currentState === "SUSPENDED" || data.currentState === "DEACTIVATED") {
	            		CommonUtils.delete({
	    	        		title: i18ntxt.activateAgentMessage,
	    	        		msg: "Re-Activate User account <b>{{account}}</b> ({{name}} {{surname}})",
	    	        		context: {
	    	        			account: data.accountNumber,
	    	        			name: data.firstName,
	    	        			surname: data.surname
	    	        		},
	    	        		url: self.baseUrl+'/activate/'+data.id,
	    	        		actionBtnText: "Activate",
	    	        		actionBtnClass: "btn-warning",
	    	        		modalType: CommonUtils.modalType.put,
	    	        		data: data,
	    	        		rowElement: data.row,
	    	        		highlightCss: "warningHighlight",
	    	        	}, {
	    	        		success: function(model, response) {
	    	        			data.redraw();
	    	        		},
	    	        		error: function(model, response) {
	    	        			App.error(reponse);
	    	        		}
	    	        	});
	            	}
	            },

	            deactivateAgent: function(ev) {
	            	var self = this;
	            	var data = this.dataFromEvent(ev);

	            	if (data.currentState === "SUSPENDED" || data.currentState === "ACTIVE") {

	    	        	CommonUtils.delete({
	    	        		title: i18ntxt.deactivateAgentMessage,
	    	        		msg: "Deactivate User account <b>{{account}}</b> ({{name}} {{surname}})",
	    	        		context: {
	    	        			account: data.accountNumber,
	    	        			name: data.firstName,
	    	        			surname: data.surname
	    	        		},
	    	        		url: self.baseUrl+'/deactivate/'+data.id,
	    	        		actionBtnText: "Deactivate",
	    	        		modalType: CommonUtils.modalType.put,
	    	        		data: data,
	    	        		rowElement: data.row,
	    	        		highlightCss: "warningHighlight",
	    	        	}, {
	    	        		success: function(model, response) {
	    	        			data.redraw();
	    	        		},
	    	        		error: function(model, response) {
	    	        			App.error(reponse);
	    	        		}
	    	        	});
	            	}
	            },
	        })
        	return AgentAccountOperationView;
        });