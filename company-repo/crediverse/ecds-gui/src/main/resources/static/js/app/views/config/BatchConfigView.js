define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var BatchConfigView = GenericConfigView.extend( {
        	template: 'Configuration#batchConfig',
        	dialogTemplate: "Configuration#batchConfigModal",
        	
        	url: 'api/config/batch',
        	
        	ui: {
        	    showUpdateDialog: '.showBatchConfigDialog'
        	},
        	
        	dialogTitle: App.i18ntxt.config.batchModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: App.i18ntxt.navbar.batchConfig,
	  				href: "#config-batch"
	  			};
        	}      	
        });
        
        return BatchConfigView;
        
});