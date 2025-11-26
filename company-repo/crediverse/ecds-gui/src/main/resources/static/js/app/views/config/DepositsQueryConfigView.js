define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var DepositsQueryConfigView = GenericConfigView.extend( {
        	template: 'Configuration#depositsQueryConfig',        	
        	dialogTemplate: "Configuration#depositsQueryConfigModal",
        	
        	url: 'api/config/deposits_query',
        	
        	ui: {
        	    showUpdateDialog: '.showDepositsQueryDialog'
        	},

        	dialogTitle: App.i18ntxt.config.depositModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.depositBC,
	  				href: "#config-depositsQuery"
	  			};
        	}        	
        });
        
        return DepositsQueryConfigView;
        
});