define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'models/ValidationModel', 'utils/CommonUtils', 'jqauth'],
    function($, _, App, BackBone, Marionette, ValidationModel, CommonUtils) {
        var TransferDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	
        	authorisationType: 'immediate',
        	
        	template: "AgentAccounts#transferView",
        	
        	initialize: function (options) {
        	},

        	onRender: function (options) {
        		
            	var self = this;

       			$.ajax({
    				type: "GET",
    				url: "api/transactions/validateroottransfer?agentId=" + self.model.get("id"),
           		    dataType: 'json',
           		    contentType: "application/json",
           		})
    			.done(function(data) {
    				if (!_.isUndefined(data.transferAllowed)) {
						if (!data.transferAllowed)
						{
							var msg = CommonUtils.getTemplateHtml("AgentAccounts#transferViewRulesErrors", data );
							
							self.model.displayRequiredStars = false;
							self.$('#error_message').html(msg).show();
							self.$('.hide-on-error').hide();
						}	
       					//$(".bonusProvision").val(data.bonus);
       					//self.checkCanAuthorize();
       					//self.model.validate();
    				}
    			})
    			.fail(function(dataResponse) {
               		App.error(dataResponse);
              	});
			},		
        	
        	ui: {
        		view: '',
                replenish: '.replenishButton',
                authorize: '.authorizeBtn',
                authorizeImmediate: '.immediateBtn',
                authorizeRequest: '.requestBtn',
                amount: '.amountField'
        	},
        	events: {
        		"click @ui.view": 'view',
            	"click @ui.replenish": 'replenishRootAccount',
            	"click @ui.authorize": 'startAuthorisation',
            	"click @ui.authorizeRequest": 'requestAuthorisation',
            	"click @ui.authorizeImmediate": 'immediateAuthorisation',
            	"keypress @ui.amount": 'capture',
            	"keyup @ui.amount": 'checkCanAuthorize',
            	"mouseleave @ui.amount": 'checkCanAuthorize',
            		
        	},
        	
        	capture: function(ev) {
        		if (ev.keyCode == 13) {
                    return this.startAuthorisation(ev);
                }
        	},
        	
        	checkCanAuthorize: function(ev) {1
        		var amountField = $(".amountField").val();
        		
        		var isDisabled = $(".authorizeBtn").is(':disabled');
        		
        		if ((amountField.length > 0) && isDisabled) {
        			$(".authorizeBtn").prop('disabled', false);
        		} if (amountField.length == 0) {
        			$(".authorizeBtn").prop('disabled', true);
        		}
        	},
        	
            view: function(ev) {
            },
            
            replenishRootAccount: function(ev) {
            	var self = this;
            	
            	return false;
            },
            
            startAuthorisation: function(ev) {
            	var self = this;
            	this.$('.amountField').attr('disabled', 'disabled');
            	$('.authorisation-type .requestBtn').attr('disabled', 'disabled');
            	$('.authorisation-type .immediateBtn').attr('disabled', 'disabled');
            	$(".authorizeBtn").prop('disabled', true);
            	$(".authorizedBy").prop('disabled', true);
            	try
            	{
            		var data = Backbone.Syphon.serialize(this);            		
                	_.extend(data, {targetMSISDN: self.model.get("mobileNumber")});
                	
                	var model = new ValidationModel();
                	model.bind(this.$('form'));
                	
                	if (self.authorisationType === 'request') {
                		model.set('language', App.contextConfig.languageID);
                		model.set('seperators', App.contextConfig.seperators[App.contextConfig.languageID]);
                		
                		model.url = 'api/workflow/transfer';
                		
                		model.save(data, {
                    		success: function(model, transaction) {
                    			var transId = transaction.uuid;
                    			$('#templates .modal').modal('hide');
                    		}
                    	});
                	}
                	else if (self.authorisationType === 'immediate') {
	                	model.url = 'api/transactions/transfer';
	                	model.save(data, {
	                		success: function(transaction) {
	                			var controlDiv = self.$(".authorisation");
	                			controlDiv.authController({
	    	           		 		coauth: true,
	    	           		 		uuid: transaction.attributes.uuid,
	    	           		 		authComplete: function(details) {
	    	           		 			$('#templates .modal').modal('hide');
	    	           		 			self.options.table.ajax.reload( null, false );
	    	           		 			App.vent.trigger('application:transfercomplete');
	    	           		 		},
	    	           		 		errorCallback: function(error) {
	    	           		 			try {
	    	           		 				self.$(".authorisation").remove();
	    	           		 				self.$(".authorisationContainer").append("<div class='authorisation'></div>");
	    	           		 				
	    	           						self.$('.amountField').removeAttr('disabled');
	    	           						$('.authorisation-type .requestBtn').removeAttr('disabled');
	    	           						$('.authorisation-type .immediateBtn').removeAttr('disabled');
	    	           		            	$(".authorizeBtn").prop('disabled', false);
	    	           		            	$(".authorizedBy").prop('disabled', false);		
	    	           		 				
	    	           		 				$.proxy(model.defaultErrorHandler(error), model);
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
	           	return false;
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
        return TransferDialogView;
    });
