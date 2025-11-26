define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var WorkflowConfigView = GenericConfigView.extend( {
        	template: 'Configuration#workflowConfig',
        	dialogTemplate: "Configuration#workflowConfigModal",
        	
        	url: 'api/config/workflow',
        	
        	ui: {
        	    showUpdateDialog: '.showWorkflowConfigDialog'
        	},
        	
        	dialogTitle: App.i18ntxt.config.workflowModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.workflowBC,
	  				href: "#config-workflows"
	  			};
        	}        	
        	
        });
        
        return WorkflowConfigView;
});