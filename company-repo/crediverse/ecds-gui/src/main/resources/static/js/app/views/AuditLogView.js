define( ['jquery', 'App', 'marionette'],
    function($, App, Marionette) {
        //ItemView provides some default rendering logic
        var AuditLogView =  Marionette.LayoutView.extend( {
        	tagName: 'div',
        	
        	attributes: {
        		class: "row"
        	},
        	
        	regions: {
        		auditLog: ".auditlog" 
        	},
        	
  		  	template: "AuditLog#auditlogContent",
            
  		  	initialize: function (options) {
            },
            
            onRender: function () {
  		  	},
            
            ui: {
                view: ''
            },

            // View Event Handlers
            events: {
            	"click @ui.view": 'viewDashboard'
            },
            
            viewDashboard: function() {
            	
            }

        });
        return AuditLogView;
    });
