define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView'],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var WebUsersConfigView = GenericConfigView.extend( {
        	template: 'Configuration#webUsersConfig',
        	dialogTemplate: "Configuration#webUsersConfigModal",
        	
        	url: 'api/config/web_users',
        	
            ui: {
                showUpdateDialog: '.showWebUsersDialog'
            },
            
        	dialogTitle: App.i18ntxt.config.webUsersTitle,

        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.webUsersBC,
	  				href: "#config-webusers"
	  			};
        	}
        });
        
        return WebUsersConfigView;
        
});