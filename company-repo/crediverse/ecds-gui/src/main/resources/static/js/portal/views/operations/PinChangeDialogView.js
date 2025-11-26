define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/CommonUtils'],
    function($, _, App, BackBone, Marionette, CommonUtils) {
        var PinChangeDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	
        	authorisationType: 'immediate',
        	
        	template: "Operations#pinChangeView",
        	isTransferring: false,
        	
        	initialize: function (options) {
        	},

        	onRender: function (options) {
			},		
        	
        	ui: {
        		view: '',
        		changePinBtn: '.changePinBtn',
                newPin: '.newPin',
                repeatPin: '.repeatPin'
        	},
        	
        	events: {
        		"click @ui.view": 'view',
            	"keyup @ui.newPin": 'checkCanTransfer',
            	"keyup @ui.repeatPin": 'checkCanTransfer',
        		"click @ui.changePinBtn": "performPinChange",
        	},
        	
            view: function(ev) {
            },
            
        	checkCanTransfer: function(ev) {
        		try {
            		var pinPin = $(".newPin").val();
            		var repeatPin = $(".repeatPin").val();
            		
            		var isDisabled = $(".changePinBtn").is(':disabled');
            		if ((pinPin.length > 0 && repeatPin.length > 0) && isDisabled) {
            			$(".changePinBtn").prop('disabled', false);
            		} else if (pinPin.length == 0 || repeatPin.length == 0) {
            			$(".changePinBtn").prop('disabled', true);
            		}
        		} catch(err) {
        			App.error(err);
        		}
        		return true;
        	},
        	
            updateSpinner: function(showSpinner) {
            	this.$(".newPin").prop("disabled", showSpinner);
            	this.$(".repeatPin").prop("disabled", showSpinner);
            	
            	if (showSpinner) {
                	this.$(".enterDetails").addClass("hide");
                	this.$(".performingPinChange").removeClass("hide");
            	} else {
                	this.$(".performingPinChange").addClass("hide");
                	this.$(".enterDetails").removeClass("hide");
            		
            	}
            },
            
            showOkDialog: function() {
				var title = CommonUtils.renderHtml( App.translate("operations.newPinTitle") );
				var body = CommonUtils.renderHtml( App.translate("operations.pinChangeSuccees") );
				CommonUtils.showOkDialog({
					title: title,
					text: body,
					callback: function() {}
				});
            },
            
            performPinChange: function(ev) {
            	var self = this;
            	
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
        return PinChangeDialogView;
    });