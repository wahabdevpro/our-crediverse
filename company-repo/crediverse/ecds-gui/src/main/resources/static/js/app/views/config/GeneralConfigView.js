define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var GeneralConfigView = GenericConfigView.extend( {
        	template: 'Configuration#generalConfig',        	
        	dialogTemplate: "Configuration#generalConfigModal",
        	
        	url: 'api/config/general_config',
        	
        	ui: {
        	    showUpdateDialog: '.showGeneralSettingsDialog'
        	},

        	dialogTitle: App.i18ntxt.config.generalConfigModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.generalSettings,
	  				href: "#config-general"
	  			};
        	}        	
        });
        
        return GeneralConfigView;
        
});