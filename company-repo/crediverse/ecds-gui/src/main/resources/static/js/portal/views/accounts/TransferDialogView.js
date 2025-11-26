define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'models/ValidationModel', 'utils/CommonUtils'],
    function($, _, App, BackBone, Marionette, ValidationModel, CommonUtils) {
        var TransferDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	url: "papi/transactions/validateroottransfer",
        	
        	authorisationType: 'immediate',
        	
        	template: "AgentAccounts#transferView",
        	isTransferring: false,
        	
        	initialize: function (options) {
//        		this.on("transfer:success", this.successfulTransfer, this);
        	},

        	onRender: function (options) {
            	var self = this;

			},		
        	
        	ui: {
        		view: '',
                transfer: '.transferBtn',
                amount: '.amountField'
        	},
        	events: {
        		"click @ui.view": 'view',
            	"keyup @ui.amount": 'checkCanTransfer',
            	"keypress @ui.amount": 'capture',
        		"click @ui.transfer": "performTransfer",
        	},
        	
        	capture: function(ev) {
//        		if (ev.keyCode == 13) {
//                    return this.performTransfer(ev);
//                }
        	},
        	
        	checkCanTransfer: function(ev) {
        		var amountField = $(".amountField").val();
        		
        		var isDisabled = $(".transferBtn").is(':disabled');
        		
        		if ((amountField.length > 0) && isDisabled) {
        			$(".transferBtn").prop('disabled', false);
        		} if (amountField.length == 0) {
        			$(".transferBtn").prop('disabled', true);
        		}
        	},
        	
            view: function(ev) {
            },
            
            disableTransferButton: function() {
            	this.$(".amountField").prop("disabled", true);
            	this.$(".enterDetails").addClass("hide");
            	this.$(".performingTransfer").removeClass("hide");
            },
            
            enableTransferButton: function() {
            	this.$(".amountField").prop("disabled", false);
            	this.$(".performingTransfer").addClass("hide");
            	this.$(".enterDetails").removeClass("hide");
            },
            
            performTransfer: function(ev) {
            	var self = this;
            	this.disableTransferButton();
            	var amount = parseFloat( this.$(".amountField").val() );
            	var formattedAmount = CommonUtils.formatNumber(amount);
            	
            	// Check for invalid model event
            	this.model.on("invalid", function(model, error) {
            		App.error(error);
            		self.isTransferring = false;
            		self.enableTransferButton();
            	});
            	
            	// Prevent dialog closing (Depending on this.isTransferring flasg)
            	$('#viewDialog').on('hide.bs.modal', function(e){
            		if (self.isTransferring) {
           		     e.preventDefault();
        		     e.stopImmediatePropagation();
        		     return false; 
            		}
        		});
            	
            	this.isTransferring = true;
            	this.model.save({
            		success: function(model, transaction) {
            			self.isTransferring = false;
//            			var transId = transaction.uuid;
            			$('#templates .modal').modal('hide');
            			
            			// Show success message
            			
            			var message = CommonUtils.renderHtml( App.translate("agentAccounts.transferSuccess"), {amount: formattedAmount});
            			CommonUtils.showOkDialog(message, function() {
            			});
            		},
            		error: function(model, response) {
            			self.isTransferring = false;
            		}
            	});
            	
            	
            },
            
            

        });
        return TransferDialogView;
    });
