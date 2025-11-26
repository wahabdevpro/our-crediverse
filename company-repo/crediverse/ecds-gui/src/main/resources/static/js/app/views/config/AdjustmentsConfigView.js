define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView'],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var AdjustmentsConfigView = GenericConfigView.extend( {
        	template: 'Configuration#adjustmentsConfig',
        	dialogTemplate: "Configuration#adjustmentsConfigModal",
        	
        	url: 'api/config/adjustments',
        	
            ui: {
                showUpdateDialog: '.showAdjustmentDialog'
            },
            
        	dialogTitle: App.i18ntxt.config.adjustmentsModalTitle,

        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.adjustmentsBC,
	  				href: "#config-adjustments"
	  			};
        	}
        });
        
        return AdjustmentsConfigView;
        
});