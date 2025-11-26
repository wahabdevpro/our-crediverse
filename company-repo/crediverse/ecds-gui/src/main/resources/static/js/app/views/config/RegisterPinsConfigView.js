define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var RegisterPinsConfigView = GenericConfigView.extend( {
        	template: 'Configuration#registerPinsConfig',
        	dialogTemplate: "Configuration#registerPinsConfigModal",
        	
        	url: 'api/config/register_pins',
        	
        	ui: {
        	    showUpdateDialog: '.showRegisterPinsConfigDialog'
        	},

        	dialogTitle: App.i18ntxt.config.regPinModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.regPinBC,
	  				href: "#config-registerPins"
	  			};
        	}        	
        	
        });
        
        return RegisterPinsConfigView;
        
});