define([ 'jquery', 'App', 'underscore', 'marionette', 'handlebars',
		'views/config/GenericConfigView' ], 
		function($, App, _, Marionette, Handlebars, GenericConfigView) {

	var ReplenishConfigView = GenericConfigView.extend({
		template : 'Configuration#replenishConfig',
		dialogTemplate : "Configuration#replenishConfigModal",

		url : 'api/config/replenish',

		ui : {
			showUpdateDialog : '.showReplenishConfigDialog'
		},

		dialogTitle: App.i18ntxt.config.replenishModalTitle,
    	
    	pageBreadCrumb: function() {
    		return {
  				text: this.i18ntxt.replenishBC,
  				href: "#config-replenish"
  			};
    	}        	
		
	});

	return ReplenishConfigView;

});