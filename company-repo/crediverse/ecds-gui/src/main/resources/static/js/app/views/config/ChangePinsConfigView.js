define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var ChangePinsConfigView = GenericConfigView.extend( {
        	template: 'Configuration#changePinsConfig',
        	dialogTemplate: "Configuration#changePinsConfigModal",
        	
        	url: 'api/config/change_pins',
        	
        	ui: {
        	    showUpdateDialog: '.showChangePinsConfigDialog'
        	},

        	dialogTitle: App.i18ntxt.config.changePinModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.changePin,
	  				href: "#config-changePins"
	  			};
        	} 	
        	
        });
        
        return ChangePinsConfigView;
        
});