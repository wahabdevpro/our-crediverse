define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/CommonUtils'],
    function($, _, App, BackBone, Marionette, CommonUtils) {
        var TransferFundsDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	
        	authorisationType: 'immediate',
        	
        	template: "Operations#transferFundsView",
        	isTransferring: false,
        	rulesChecked: false,
        	
        	initialize: function (options) {
        		
        	},

        	checkRulesValid: function() {
        		var self = this;
       			$.ajax({
    				type: "GET",
    				url: "papi/transactions/validatetransfer?agentId=" + this.model.get("agentId"),
           		    dataType: 'json',
           		    contentType: "application/json",
           		})
    			.done(function(data) {
    				if ((!_.isUndefined(data.transferAllowed)) && (!data.transferAllowed)) {
						var msg = CommonUtils.getTemplateHtml("AgentAccounts#transferViewRulesErrors", data );
						
						self.model.displayRequiredStars = false;
						self.$('#error_message').html(msg).show();
						self.$('.enterDetails').hide();
    				}
    			})
    			.fail(function(dataResponse) {
               		App.error(dataResponse);
              	});
        	},
        	
        	onRender: function (options) {
            	this.checkRulesValid();
            	
            	if (!_.isUndefined(this.model) && !_.isUndefined(this.model.get("agentTransfer")) && this.model.get("agentTransfer")) {
            		$(".msisdnField").prop('disabled', true);
            	}
            },		
        	
        	ui: {
        		view: '',
        		msisdn: '.msisdnField',
                amount: '.amountField',
                transfer: '.transferBtn',
        	},
        	
        	events: {
        		"click @ui.view": 'view',
            	"keyup @ui.msisdn": 'checkCanTransfer',
            	"keyup @ui.amount": 'checkCanTransfer',
        		"click @ui.transfer": "performTransfer",
        	},
        	
            view: function(ev) {
            },
            
        	checkCanTransfer: function(ev) {
        		try {
	        		var amountField = $(".amountField").val();
	        		var msisdnField = $(".msisdnField").val();
	        		
	        		var isDisabled = $(".transferBtn").is(':disabled');
	        		
	        		if ((amountField.length > 0 && msisdnField.length > 0) && isDisabled) {
	        			$(".transferBtn").prop('disabled', false);
	        		} else if ((amountField.length == 0 || msisdnField.length == 0)) {
	        			$(".transferBtn").prop('disabled', true);
	        		}
	    		} catch(err) {
	    			App.error(err);
	    		}
	    		return true;
        	},
        	
            updateSpinner: function(showSpinner) {
            	this.$(".amountField").prop("disabled", showSpinner);
            	this.$(".msisdnField").prop("disabled", showSpinner);
            	
            	if (showSpinner) {
                	this.$(".enterDetails").addClass("hide");
                	this.$(".performingTransfer").removeClass("hide");
            	} else {
                	this.$(".performingTransfer").addClass("hide");
                	this.$(".enterDetails").removeClass("hide");
            	}
            },
            
            showOkDialog: function(formattedAmount) {
				var title = CommonUtils.renderHtml( App.translate("operations.transferTitle") );
				var body = CommonUtils.renderHtml( App.translate("operations.transferSuccess"), {amount: formattedAmount} );
				CommonUtils.showOkDialog({
					title: title,
					text: body,
					callback: function() {}
				});
            },
            
            performTransfer: function(ev) {
            	
               	var self = this;
            	var amount = parseFloat( this.$(".amountField").val() );
            	var formattedAmount = CommonUtils.formatNumber(amount);
            	
            	if(this.model.isValid(true)){
                    // this.model.save();
                	this.updateSpinner(true);
                	this.isTransferring = true;
                } else {
                	return;
                }
            	// Prevent dialog closing (Depending on this.isTransferring flasg)
            	$('#viewDialog').on('hide.bs.modal', function(e){
            		if (self.isTransferring) {
           		     e.preventDefault();
        		     e.stopImmediatePropagation();
        		     return false; 
            		}
        		});
            	
            	this.model.save({
            		success: function(model, transaction) {
            			self.isTransferring = false;
        				self.updateSpinner(false);
    					$('#templates .modal').modal('hide');
    
    					// Show success message
    					self.showOkDialog(formattedAmount);
        					
    					// pass update event to caller
    					App.vent.trigger('operations:success', {});
            		},
            		error: function(model, response) {
            			self.isTransferring = false;
            			self.updateSpinner(false);
            		}
            	});
            	
            },

        });
        return TransferFundsDialogView;
    });