define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'models/AdjustmentModel', 'jqauth'],
    function($, _, App, BackBone, Marionette, AdjustmentModel) {
        var AdjustmentDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	authorisationType: 'immediate',
        	
        	template: "AgentAccounts#adjustmentView",
        	
        	initialize: function (options) {
        	},
        	
        	ui: {
        		view: '',
                replenish: '.replenishButton',
                authorize: '.authorizeButton',
                authorizeImmediate: '.immediateBtn',
                authorizeRequest: '.requestBtn',
                reason: '#reason'
        	},
        	events: {
        		"click @ui.view": 'view',
            	"click @ui.replenish": 'replenishRootAccount',
            	"click @ui.authorize": 'startAuthorisation',
            	"click @ui.authorizeRequest": 'requestAuthorisation',
            	"click @ui.authorizeImmediate": 'immediateAuthorisation',
            	"keypress @ui.reason": 'capture'
        	},
        	
        	capture: function(ev) {
        		if (ev.keyCode == 13) {
                    return this.startAuthorisation(ev);
                }
        	},
        	
            view: function(ev) {
            },
            
            replenishRootAccount: function(ev) {
            	var self = this;
            	
            	return false;
            },
            
            startAuthorisation: function(ev) {
            	if (this.model.valid()) {
	            	this.$('form :input').attr('disabled', 'disabled');
	            	$('.authorisation-type .requestBtn').attr('disabled', 'disabled');
	            	$('.authorisation-type .immediateBtn').attr('disabled', 'disabled');
	            	try
	            	{
	            		var self = this;
	            		var data = Backbone.Syphon.serialize(this);            		
	                	_.extend(data, {targetMSISDN: self.options.model.get("mobileNumber"), agentID: self.options.model.get("id")});
	                	
	                	var model = new AdjustmentModel();
	                	model.bind(this.$('form'));
	                	
	                	if (self.authorisationType === 'request') {
	                		model.set('language', App.contextConfig.languageID);
	                		model.set('seperators', App.contextConfig.seperators[App.contextConfig.languageID]);
	                		
	                		model.url = 'api/workflow/adjust';
	                		
	                		model.save(data, {
	                    		success: function(model, transaction) {
	                    			var transId = transaction.uuid;
	                    			$('#templates .modal').modal('hide');
	                    		}
	                    	});
	                	}
	                	else if (self.authorisationType === 'immediate') {
		                	model.url = 'api/transactions/adjustment';
		                	model.save(data, {
		                		success: function(transaction) {
		                			var controlDiv = self.$(".authorisation");
		    	           		 	controlDiv.authController({
		    	           		 		coauth: true,
		    	           		 		uuid: transaction.attributes.uuid,
		    	           		 		authComplete: function(details) {
		    	           		 			$('#templates .modal').modal('hide');
											self.options.table.ajax.reload( null, false );
		    	           		 			App.vent.trigger('application:adjustmentcomplete');
		    	           		 		},
		    	           		 		errorCallback: function(details, showError, ctxt) {
		    	           					try {
		    	           						$.proxy(model.defaultErrorHandler(error), model);
		    	           						controlDiv.hide();
		    	           					} catch(err) {
		    	           						App.error(err);
		    	           					}
		    	           		 		}
		    	           		 	});  
		                		}
		                	});
	                	}
	                	else {
	                		App.log('Unknown type: '+self.authorisationType);
	                	}
		           	} catch(err) {
		           		if (console) console.error(err);
		           	}
            	}
	           	return false;
            },
            
            requestAuthorisation: function(ev) {
            	$('.authorisation-type .requestBtn').addClass('active');
            	$('.authorisation-type .immediateBtn').removeClass('active');
            	//this.configureSelect2($('#authorizedBy'), "api/wusers/dropdown");
            	//$('.authorisation-request').show();
            	$('.authorisation-request').hide();
            	this.authorisationType = 'request';
            	return false;
            },
            
            immediateAuthorisation: function(ev) {
        		$('.authorisation-type .immediateBtn').addClass('active');
            	$('.authorisation-type .requestBtn').removeClass('active');
            	$('.authorisation-request').hide();
            	this.authorisationType = 'immediate';
            	//$("#authorizedBy").select2('destroy'); 
            	return false;
            }

        });
        return AdjustmentDialogView;
    });
