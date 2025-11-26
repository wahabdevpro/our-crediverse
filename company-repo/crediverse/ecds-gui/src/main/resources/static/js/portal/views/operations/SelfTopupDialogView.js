define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/CommonUtils'],
    function($, _, App, BackBone, Marionette, CommonUtils) {
        var SelfTopupDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	
        	authorisationType: 'immediate',
        	
        	template: "Operations#selfTopupView",
        	isTransferring: false,
        	
        	initialize: function (options) {
        	},

        	onRender: function (options) {
            	var self = this;
			},		
        	
        	ui: {
        		view: '',
        		selfTopup: '.selfTopupBtn',
                amount: '.amountField'
        	},
        	
        	events: {
        		"click @ui.view": 'view',
            	"keyup @ui.amount": 'checkCanTopup',
        		"click @ui.selfTopup": "performTopup",
        	},
        	
            view: function(ev) {
            },
            
        	checkCanTopup: function(ev) {
        		try {
	        		var amountField = $(".amountField").val();
	        		
	        		var isDisabled = $(".selfTopupBtn").is(':disabled');
	        		
	        		if ((amountField.length > 0) && isDisabled) {
	        			$(".selfTopupBtn").prop('disabled', false);
	        		} else if (amountField.length == 0) {
	        			$(".selfTopupBtn").prop('disabled', true);
	        		}
	    		} catch(err) {
	    			App.error(err);
	    		}
	    		return true;
        	},
        	
            updateSpinner: function(showSpinner) {
            	this.$(".amountField").prop("disabled", showSpinner);
            	
            	if (showSpinner) {
                	this.$(".enterDetails").addClass("hide");
                	this.$(".performingSelfTopup").removeClass("hide");
            	} else {
                	this.$(".performingSelfTopup").addClass("hide");
                	this.$(".enterDetails").removeClass("hide");
            		
            	}
            },
            
            showOkDialog: function() {
				var title = CommonUtils.renderHtml( App.translate("operations.selfTopupTitle") );
				var body = CommonUtils.renderHtml( App.translate("operations.selfTopupSuccess") );
				CommonUtils.showOkDialog({
					title: title,
					text: body,
					callback: function() {}
				});
            },
            
            performTopup: function(ev) {
            	
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
    					self.showOkDialog();
    					App.vent.trigger('operations:success', {});
            		},
            		error: function(model, response) {
            			self.isTransferring = false;
            			self.updateSpinner(false);
            		}
            	});
                
            },
            
            

        });
        return SelfTopupDialogView;
    });