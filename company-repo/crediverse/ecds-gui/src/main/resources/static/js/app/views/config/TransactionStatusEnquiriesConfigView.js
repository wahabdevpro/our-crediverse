define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var TransactionStatusEnquiriesConfigView = GenericConfigView.extend( {
        	template: 'Configuration#transactionStatusEnquiriesConfig',
        	dialogTemplate: "Configuration#transactionStatusEnquiriesConfigModal",
        	
        	url: 'api/config/transaction_status_enquiries',
        	
        	ui: {
        	    showUpdateDialog: '.showTransactionStatusEnquiriesConfigDialog'
        	},

        	dialogTitle: App.i18ntxt.config.transStatEnqModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.transStatEnqBC,
	  				href: "#config-transactionStatusEnquiries"
	  			};
        	}        	
        	
        });
        
        return TransactionStatusEnquiriesConfigView;
});