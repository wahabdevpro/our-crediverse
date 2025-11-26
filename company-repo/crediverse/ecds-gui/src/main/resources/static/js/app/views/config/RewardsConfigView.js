define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView'],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var RewardsConfigView = GenericConfigView.extend( {
        	template: 'Configuration#rewardsConfig',
        	dialogTemplate: "Configuration#rewardsConfigModal",
        	
        	url: 'api/config/rewards',
        	
            ui: {
                showUpdateDialog: '.showRewardsDialog'
            },
            
        	dialogTitle: App.i18ntxt.config.adjustmentsModalTitle,

        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.rewardsBC,
	  				href: "#config-rewards"
	  			};
        	}
        });
        
        return RewardsConfigView;
});