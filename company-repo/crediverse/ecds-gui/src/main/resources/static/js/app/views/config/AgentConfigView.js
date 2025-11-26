define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'views/config/GenericConfigView'],
    function($, App, _, Marionette, Handlebars, GenericConfigView) {

        var AgentConfigView = GenericConfigView.extend( {
        	template: 'configuration/AgentConfig#agentConfig',	
        	dialogTemplate: "configuration/AgentConfig#agentConfigModal",
        	
        	url: 'api/config/agents',
        	
            ui: {
                showUpdateDialog: '.showAgentDialog'
            },
            
        	dialogTitle: App.i18ntxt.config.agentsModalTitle,

        	pageBreadCrumb: function() {
        		return {
	  				text: this.i18ntxt.agentsBC,
	  				href: "#config-agents"
	  			};
        	},
        	
        	dialogOnRender: function(el) {
        		el.find(".dump-enabled").on("click", function(ev) {
        			if (ev.target.id == "dump-enabled-yes") {
        				el.find("#scheduledAccountDumpEnabled").show();
        			} else {
        				el.find("#scheduledAccountDumpEnabled").hide();
        			}
        		});

				// EXPLICIT disable ... we do not want this to be visible or usable anymore, disabling
        		el.find("#activityScale").val('disabled');

        		var activityScale =  el.find("#activityScale").val();
        		if (activityScale === 'disabled') {
        			var value = el.find("#activityScaleValue");
        			value.attr('disabled', true);
        			value.addClass('disabled');
        		}
        		
				/*
        		el.find("#activityScale").on("change", function(ev) {
        			activityScale =  el.find("#activityScale").val();
        			if (activityScale === 'disabled') {
            			var value = el.find("#activityScaleValue");
            			value.attr('disabled', true);
            			value.addClass('disabled');
            		}
        			else {
        				var value = el.find("#activityScaleValue");
            			value.removeAttr('disabled');
            			value.removeClass('disabled');
        			}
        		});
				*/
        		
        		var val =  el.find("#scheduledAccountDumpIntervalMinutes").val();
        		if (val == "0") {
        			el.find("#repeatScheduledAccountDumpInterval").hide();
        		}
        		
        		el.find(".repeat-dumps").on("click", function(ev) {
        			if (ev.target.id == "repeat-yes") {
        				el.find("#repeatScheduledAccountDumpInterval").show();
        				el.find("#scheduledAccountDumpIntervalMinutes").val("60");
        			} else {
        				el.find("#repeatScheduledAccountDumpInterval").hide();
        				el.find("#scheduledAccountDumpIntervalMinutes").val("0");
        			}
        		});
        	}
        	
        });
        
        return AgentConfigView;
        
});