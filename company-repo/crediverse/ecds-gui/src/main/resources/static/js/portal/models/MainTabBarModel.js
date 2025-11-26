define(["App", "jquery", "backbone"],
    function (App, $, Backbone) {
        // Creates a new Backbone Model class object
        var Model = Backbone.Model.extend({
        	url: App.contextPath + '/current',
        	
        	events: {
    		},
    		
            initialize:function () {
            	var self = this;
            	App.vent.listenTo(App.vent, 'application:navigate', function(path) {
            		return self.updateState(self, path);
            	});
            },
            
            updateState:function(self, path) {
            	var original = self.get('buttons');
            	
            	var newstate = _.map(original, _.clone);
            	_.each(newstate, function(item) {
            		if (item.href == path) {
            			item.active = true;
            		}
            		else {
            			item.active = false;
            		}
            		return item;
            	});
            	
            	self.set('buttons', newstate);
            },

            // Default values for all of the Model attributes
            defaults:{
            	username: "None",
            	buttons: [
            	          {
            	        	  icon: "fa fa-dashboard",
            	        	  title: "Users",
            	        	  href: "users",
            	        	  active: true
            	          },
            	          {
            	        	  icon: "fa fa-building-o",
            	        	  title: "Roles",
            	        	  href: "roles",
            	        	  active: false
            	          },
            	          {
            	        	  icon: "fa fa-building-o",
            	        	  title: "Tiers",
            	        	  href: "tiers",
            	        	  active: false
            	          }/*,
            	          {
            	        	  icon: "fa fa-font",
            	        	  title: "Reports",
            	        	  href: "reports",
            	        	  active: false
            	          }*/
        	     ]
            },

            // Get's called automatically by Backbone when the set and/or save methods are called (Add your own logic)
            validate:function (attrs) {
            	return true;
            }
        });

        return Model;
    }
);