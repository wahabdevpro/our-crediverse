define( ['jquery', 'App', 'marionette'],
    function($, App, Marionette) {
        //ItemView provides some default rendering logic
        var NoPermissionView =  Marionette.LayoutView.extend( {
        	tagName: 'div',
        	
        	attributes: {
        		class: "row"
        	},
        	
        	regions: {
        		auditLog: ".auditlog" 
        	},
        	
  		  	template: "Dashboard#noPermissions",
            
  		  	initialize: function (options) {
            },
            
            ui: {
                view: ''
            },

            // View Event Handlers
            events: {
            	"click @ui.view": 'view'
            },
            
            view: function() {
            	
            }

        });
        return NoPermissionView;
    });
