define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var ReversalConfigView = GenericConfigView.extend( {
        	template: 'Configuration#reversalConfig',
        	dialogTemplate: "Configuration#reversalConfigModal",
        	
        	url: 'api/config/reversal',
        	
        	ui: {
        	    showUpdateDialog: '.showReversalConfigDialog'
        	},
        	
        	dialogTitle: App.i18ntxt.config.reversalModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.reversalBC,
	  				href: "#config-reversal"
	  			};
        	}        	
        	
        });
        
        return ReversalConfigView;
});