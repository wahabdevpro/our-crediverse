define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView'],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var AnalyticsConfigView = GenericConfigView.extend( {
        	template: 'Configuration#analyticsConfig',
        	dialogTemplate: "Configuration#analyticsConfigModal",
        	
        	url: 'api/config/analytics',
        	
            ui: {
                showUpdateDialog: '.showAnalyticsConfigDialog'
            },
            
        	dialogTitle: App.i18ntxt.config.analyticsHeading,

        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.analyticsConfigBC,
	  				href: "#config-analytics"
	  			};
        	}
        });
        
        return AnalyticsConfigView;
        
});