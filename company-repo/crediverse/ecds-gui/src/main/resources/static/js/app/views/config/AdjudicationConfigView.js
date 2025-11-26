define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var AdjudicationConfigView = GenericConfigView.extend( {
        	template: 'Configuration#adjudicationConfig',
        	dialogTemplate: "Configuration#adjudicationConfigModal",
        	
        	url: 'api/config/adjudication',
        	
        	ui: {
        	    showUpdateDialog: '.showAdjudicationConfigDialog'
        	},
        	
        	dialogTitle: App.i18ntxt.config.adjudicationModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.adjudicationBC,
	  				href: "#config-adjudication"
	  			};
        	}        	
        	
        });
        
        return AdjudicationConfigView;
});
