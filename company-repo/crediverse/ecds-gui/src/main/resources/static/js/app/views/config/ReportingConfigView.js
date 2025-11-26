define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView' ],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var ReportingConfigView = GenericConfigView.extend( {
        	template: 'Configuration#reportingConfig',
        	dialogTemplate: "Configuration#reportingConfigModal",
        	url: 'api/config/reporting',
        	
        	ui: {
        	    showUpdateDialog: '.showReportingConfigDialog'
        	},

			dialogOnRender: function(element) {
				const self = this;

				this.$('input[id^="smsConfigurationEnabled"]').off('click');
				this.$('input[id^="smsConfigurationEnabled"]').on('click', function(ev) {
					const id = ev.currentTarget.id;
					const checked = ev.currentTarget.checked;
					if (checked) {
						if (String(id).match(/yes$/)) self.$('#sms-configuration-enabled').show();
						if (String(id).match(/no$/)) self.$('#sms-configuration-enabled').hide();
					}
				})
			},

        	dialogTitle: App.i18ntxt.config.reportingModalTitle,
        	
        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.reporting,
	  				href: "#config-reporting"
	  			};
        	} 	
        	
        });
        
        return ReportingConfigView;
        
});
