define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var LastTransactionEnquiriesConfigView = GenericConfigView.extend( {
        	template: 'Configuration#lastTransactionEnquiriesConfig',
        	dialogTemplate: "Configuration#lastTransactionEnquiriesConfigModal",
        	
        	url: 'api/config/last_Transaction_enquiries',
        	
        	ui: {
        	    showUpdateDialog: '.showLastTransactionEnquiriesConfigDialog'
        	},
        	
        	dialogTitle: App.i18ntxt.config.lastTransQryModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.lastTransQryBC,
	  				href: "#config-lastTransactionEnquiries"
	  			};
        	}        	
        	
        });
        
        return LastTransactionEnquiriesConfigView;
        
});