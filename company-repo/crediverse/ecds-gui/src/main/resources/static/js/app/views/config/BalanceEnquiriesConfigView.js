define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var BalanceEnquiriesConfigView = GenericConfigView.extend( {
        	template: 'Configuration#balanceEnquiriesConfig',
        	dialogTemplate: "Configuration#balanceEnquiriesConfigModal",
        	
        	url: 'api/config/balance_enquiries',
        	
        	ui: {
        	    showUpdateDialog: '.showBalanceEnquiriesConfigDialog'
        	},
        	
        	dialogTitle: App.i18ntxt.config.balanceEnqModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.balanceEnqBC,
	  				href: "#config-balanceEnquiries"
	  			};
        	}
        });
        
        return BalanceEnquiriesConfigView;
        
});