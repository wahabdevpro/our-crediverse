define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'models/ReversalModel', 'jqauth'],
    function($, _, App, BackBone, Marionette, ReversalModel) {
        var coAuthorizationEnabled = true;
        var TdrReverseView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	
        	authorisationType: 'immediate',
        	
        	template: "Tdrs#reversalView",
        	
        	initialize: function (options) {
        	},
        	
        	ui: {
        		view: '',
        		amountfull: '#amount_full',
        		amountpartial: '#amount_partial',
                reverseTransaction: '.reverseButton',
                authorize: '.authorizeButton',
                reason: '#reason',
                authorizeImmediate: '.immediateBtn',
                authorizeRequest: '.requestBtn',
                reverseFullRadioButtonKeyPress: '#amount_full',
                reversePartialRadioButtonKeyPress: '#amount_partial'
        	},
        	events: {
        		"click @ui.amountfull": 'changeReversalType',
        		"click @ui.amountpartial": 'changeReversalType',
        		"click @ui.view": 'view',
            	"click @ui.reverseTransaction": 'reverseTransaction',
            	"click @ui.authorizeRequest": 'requestAuthorisation',
            	"click @ui.authorizeImmediate": 'immediateAuthorisation',
            	"click @ui.authorize": 'startAuthorisation',
            	"keypress @ui.reason": 'capture',
            	"keypress @ui.reverseFullRadioButtonKeyPress": 'doNothing',
            	"keypress @ui.reversePartialRadioButtonKeyPress": 'doNothing'
        	},

        	doNothing: function(ev) {
        	},
        	
        	changeReversalType: function(ev) {
        		var state = this.$('input[name="amount"]:checked').val();
        		if (state === 'full') {
        			this.$('input[name="reverseAmount"]').prop('disabled', true);
        			this.$('.reverseAmountFull').attr('disabled', false);
        			this.$('input#reverseAmount').each(function() {
                        $(this).rules("remove");
                    });
        			this.model.clearValidationMarkers($('input#reverseAmount'));
        		}
        		else if (state === 'partial') {
        			this.$('input[name="reverseAmount"]').prop('disabled', false);
        			this.$('.reverseAmountFull').attr('disabled', true);
        			var fullAmount = this.model.get('amount');
        			this.$('input#reverseAmount').each(function() {
                        $(this).rules("add", 
                            {
                                required: true,
                                min: 0.01,
                                max: fullAmount,
                                numeric:true,
                                places: 2
                            })
                    });
        			this.model.clearValidationMarkers($('input#reverseAmount'));
        		}
        		try {
        			this.$('.reverseButton').prop('disabled', false);	
        		} catch(err) {}
        		
        		this.$('#reason').prop('disabled', false);
        	},
        	
        	capture: function(ev) {
        		if (ev.keyCode == 13) {
                    return this.startAuthorisation(ev);
                }
        	},
        	
            view: function(ev) {
            },
            
            onRender: function () {
            	var self = this;
            	App.log(self.model);
			
				var ReversalConfigModel = Backbone.Model.extend({
					url: 'api/config/reversal',
				});
				var reversalConfigModel = new ReversalConfigModel();
				
            	reversalConfigModel.fetch().then(function() {
					//set var for use on the reversal call, set up here so that the variable is defined when reverse is actioned
					coAuthorizationEnabled = reversalConfigModel.attributes.enableCoAuthReversal;
					
					if (!coAuthorizationEnabled) {
						$('.authorisation-type').remove();
					}
				});
            },
            
            reverseTransaction: function(ev) {
            	var self = this;
            	
            	var ReversalConfigModel = Backbone.Model.extend({
					url: 'api/config/reversal',
				});
				
				if (coAuthorizationEnabled) {
					console.log('CO coAuthorization ENABLED: ' + coAuthorizationEnabled	 )
					this.startAuthorisation(ev);
				} else {
					console.log('CO Authorization DISABLED: ' + coAuthorizationEnabled)
					this.processWithoutSecondAuthorization(ev);
				}
				return false;    			
				
            },
            
            processWithoutSecondAuthorization: function(ev) {
            	if (this.model.valid()) {
            		this.$('form :input').attr('disabled', 'disabled');
            		this.$('.reverseButton').attr('disabled', 'disabled');
            		
            		try {
            			var self = this;
                		var data = Backbone.Syphon.serialize(this);
                    	var model = new ReversalModel();
                		var updated = {
							transactionNumber: this.model.get('number'),
							reason: data.reason,
							type: (data.amount === 'partial') ? 'PARTIAL' : 'FULL',
							amount: (data.amount === 'partial') ? data.reverseAmount : this.model.get('amount')
						};
                        	
                        model.bind(this.$('form'));
						model.url = 'api/transactions/reversal_without_co_auth';
						model.save(updated, {
							success: function(transaction) {
								$('#templates .modal').modal('hide');
							},
							error: function(info) {
								console.log("ERROR: " + info);
							}
						});  
                        	
            		} catch(err) {
    	           		if (console) console.error(err);
    	           	}
            	}
            	return false;
            },
            
            startAuthorisation: function(ev) {
            	if (this.model.valid()) {
            		this.$('form :input').attr('disabled', 'disabled');
            		this.$('.reverseButton').attr('disabled', 'disabled');
                	try
                	{
                		var self = this;
                		var data = Backbone.Syphon.serialize(this);
                		

                		var updated = {
                        		transactionNumber: this.model.get('number'),
                        		reason: data.reason,
                        		type: data.amount.toUpperCase(),
                        		amount: (data.amount === 'partial')?data.reverseAmount:this.model.get('amount')
                        	};
                    	var model = new ReversalModel();
                    	if (self.authorisationType === 'request') {
                    		model.set('language', App.contextConfig.languageID);
                    		model.set('seperators', App.contextConfig.seperators[App.contextConfig.languageID]);
                    		
                    		model.bind(this.$('form'));
                        	model.url = 'api/workflow/reversal';
                        	model.save(updated, {
                        		success: function(model, transaction) {
                        			var transId = transaction.uuid;
                        			$('#templates .modal').modal('hide');
                        		}
                        	});
                        	
                    	}
                    	else if (self.authorisationType === 'immediate') {
                    		
                    		model.bind(this.$('form'));
                        	model.url = 'api/transactions/reversal';
                        	model.save(updated, {
                        		success: function(transaction) {
                        			var controlDiv = self.$(".authorisation");
                        			controlDiv.authController({
            	           		 		coauth: true,
            	           		 		uuid: transaction.attributes.uuid,
            	           		 		authComplete: function(details) {
            	           		 			$('#templates .modal').modal('hide');
            	           		 			App.vent.trigger('application:adjustmentcomplete');
            	           		 		},
            	           		 		errorCallback: function(details, showError, ctxt) {
            	           					try {

            	           						$.proxy(model.defaultErrorHandler(details), model);
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
        return TdrReverseView;
    });
