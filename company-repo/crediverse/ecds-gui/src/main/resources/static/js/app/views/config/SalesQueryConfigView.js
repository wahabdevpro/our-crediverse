define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var SalesQueryConfigView = GenericConfigView.extend( {
        	template: 'Configuration#salesQueryConfig', 
        	dialogTemplate: "Configuration#salesQueryConfigModal",
        	
        	url: 'api/config/sales_queries',
        	
        	ui: {
        	    showUpdateDialog: '.showSalesQueryConfigDialog'
        	},

        	dialogTitle: App.i18ntxt.config.salesQueryModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.salesQueryBC,
	  				href: "#config-querySales"
	  			};
        	}        	
        	
        });
        
        return SalesQueryConfigView;
        
});