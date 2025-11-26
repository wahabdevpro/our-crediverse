define( ['jquery', 'App', 'backbone', 'marionette', 'models/RootAccountModel', 'jqauth', 'jquery.select2'],
    function($, App, BackBone, Marionette, RootAccountModel) {
        var ReplenishView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	
        	authorisationType: "immediate",
        	
        	template: "ManageDashBoard#replenishView",
        	
        	initialize: function (options) {
        	},
        	
        	ui: {
        		view: '',
                replenish: 	'.replenishButton',
                authorize: 	'.authorizeBtn',
                authorizeImmediate: '.immediateBtn',
                authorizeRequest: '.requestBtn',
                amount: 	'.amountField',
                bonusProvision:	'.bonusProvision',
                
                calcBonusButton: '.calcBonusButton',
                allowChangeBonus: '#allowChangeBonus',
                allowChangeBonusLabel: '#allowChangeBonusLabel'
        	},
        	events: {
        		"click @ui.view": 'view',
            	"click @ui.replenish": 'replenishRootAccount',
            	"click @ui.authorize": 'startAuthorisation',
            	"click @ui.authorizeRequest": 'requestAuthorisation',
            	"click @ui.authorizeImmediate": 'immediateAuthorisation',
            	"keypress @ui.amount": 'capture',
            	
        		"keyup @ui.amount": 			'checkCalcButtonStatus',
    			"click @ui.calcBonusButton": 	'suggestBonusAmount',
    			
    			"change @ui.amount": 			'checkCanAuthorize',
    			"change @ui.bonusProvision":	'checkCanAuthorize',
    			"change @ui.allowChangeBonus":	'enableChangeBonus',
    			//"change @ui.allowChangeBonusLabel": 'enableChangeBonus'
    				
        	},
        	configureSelect2: function(element, url) {
        		var ajaxConfig =  {
            			type: "GET",
            		    url: url,
            		    dataType: 'json',
            		    //contentType: "application/json",
            		    delay: 250,
            		    /*data: function (params) {
                            return  JSON.stringify({
                                term: params.term
                            });
                        },*/
                        processResults: function (data) {
                        	var mapped = $.map(data, function (item, i) {
				                                return {
				                                    text: item,
				                                    id: i
				                                }
				                            });
                        	mapped.unshift({
                        		id: "groupauth",
                        		text: "Any Available User"
                        	},
                        	{
                        		id: "groupAuth",
                        		text: "Any Available User"
                        	});
                            return {
                                results: mapped
                            };
                        }
            		};
        		
        		
        		
        		element.select2({
            		ajax: ajaxConfig,
            		minimumInputLength: 0,
					allowClear: true, 
					placeholder: {
					    id: 'groupauth', // the value of the option
					    text: 'Any Available User'
					  }
            	});
        	},
        	onRender: function() {
        		this.configureSelect2($('#authorizedBy'), "api/wusers/dropdown");
        		self.$("#bonusProvision").attr("disabled", true);
        	},
        	
        	updateBonusAmount: function() {
        		var value = $(".amountField").val();
        		var isDisabled = $(".calcBonusButton").is(':disabled');
        		
        		if (value.length > 0 && isDisabled) {
        			$(".calcBonusButton").prop('disabled', false);
        		} else if (value.length == 0 && !isDisabled) {
        			$(".calcBonusButton").prop('disabled', true);
        		}
        		
        		if (value.length > 0) {
        			this.suggestBonusAmount();
        		}
        	},
        	
        	enableChangeBonus: function(ev) {        		
        		if ($("#allowChangeBonus").prop('checked')) {
        			self.$("#bonusProvision").attr("disabled", false);
        			/*if(!_.isUndefined(this.userBonusValue) && this.userBonusValue.length > 0)
        				$("#bonusProvision").val(this.userBonusValue);*/
        		} else {
        			//this.userBonusValue = $("#bonusProvision").val();
        			self.$("#bonusProvision").attr("disabled", true);
        			this.updateBonusAmount();        			
        		}
        	},

        	checkCalcButtonStatus: function(ev) {
        		this.updateBonusAmount();        		
        	},
        	
        	checkCanAuthorize: function(ev) {
        		var amountField = $(".amountField").val();
        		var bonusProvision = $(".bonusProvision").val();
        		
        		var isDisabled = $(".authorizeBtn").is(':disabled');
        		
        		if ((amountField.length > 0) && (bonusProvision.length > 0) && isDisabled) {
        			$(".authorizeBtn").prop('disabled', false);
        		} if ((amountField.length == 0) ||  (bonusProvision.length == 0)) {
        			$(".authorizeBtn").prop('disabled', true);
        		}
        	},

        	suggestBonusAmount: function() {
        		var amount = null;
        		var self = this;
        		
        		try {
        			amount = parseFloat( $(".amountField").val() );
        		} catch(err) {
        		}
        		var isDisabled = $(".calcBonusButton").is(':disabled');
        		
        		if (_.isNumber(amount) && !isDisabled) {
        			$.ajax({
    					type: "GET",
    					url: "api/transactions/sugestbonusamount?amount=" + amount,
            		    dataType: 'json',
            		    contentType: "application/json",
            		})
    				.done(function(data) {
    					if (!_.isUndefined(data.bonus)) {
        					$(".bonusProvision").val(data.bonus);
        					self.checkCanAuthorize();
        					self.model.validate();
    					}
    				})
    				.fail(function(dataResponse) {
                  		App.error(dataResponse);
                  	});
        			
        		}
        	},
        	
        	capture: function(ev) {
        		if (ev.keyCode == 13) {
                    return this.startAuthorisation(ev);
                }
        	},
        	
            view: function(ev) {
            	//return false;
            },
            
            replenishRootAccount: function(ev) {
            	var self = this;
            	
            	return false;
            },
            
            startAuthorisation: function(ev) {
            	var self = this;
            	if (this.model.valid()) {
                	this.$('.amountField').attr('disabled', 'disabled');
                	this.$('.bonusProvision').attr('disabled', 'disabled');
                	this.$('.calcBonusButton').attr('disabled', 'disabled');
                	$('.authorisation-type .requestBtn').attr('disabled', 'disabled');
                	$('.authorisation-type .immediateBtn').attr('disabled', 'disabled');
                	$(".authorizeBtn").prop('disabled', true);
                	$(".authorizedBy").prop('disabled', true);
                	try
                	{
                    	var model = new RootAccountModel();
                    	model.bind(this.$('form'));
                    	
                    	if (self.authorisationType === 'request') {
                    		model.set('language', App.contextConfig.languageID);
                    		model.set('seperators', App.contextConfig.seperators[App.contextConfig.languageID]);
                    		
                    		model.url = 'api/workflow/replenish';
                    		model.save({
                        		success: function(model, transaction) {
                        			var transId = transaction.uuid;
                        			$('#templates .modal').modal('hide');
                        		}
                        	});
                    	}
                    	else if (self.authorisationType === 'immediate') {
                    		
                    		model.url = 'api/transactions/replenish';
                    		model.save({
                        		success: function(transaction) {
                        			var controlDiv = self.$(".authorisation");
                        			controlDiv.authController({
                        				coauth: true,
            	           		 		uuid: transaction.attributes.uuid,
            	           		 		authComplete: function(details) {
            	           		 			$('#templates .modal').modal('hide');
            	           		 			App.vent.trigger('application:replenishcomplete');
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
            },
        });
        return ReplenishView;
    });