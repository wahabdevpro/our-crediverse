define(['App', 'backbone', 'marionette', "controllers/FrontController"],
		function(App, Backbone, Marionette, FrontController) {
	var BaseAppRouter =  Marionette.AppRouter.extend({
		controller: null,
   	    
        checkPermissions: function(name, path, args) {
        	
        	// Adjust Path
        	var refPath = path;
        	if (refPath == "*other") {
        		refPath = _.keys(this.permissions)[0];
        	} else if (path.indexOf("*") > 0) {
        		refPath = path.substring(0, path.indexOf("*"));
        	} else if (path.indexOf("/") > 0) {
        		refPath = path.substring(0, path.indexOf("/"));
        	} else if (path.indexOf("?") > 0) {
        		refPath = path.substring(0, path.indexOf("?"));
        	}

        	// Check permissions
        	var perms = this.permissions[refPath];
        	if (_.isUndefined( perms )) {
        		return {name:name, path:path, args:args};
        	} else if (App.hasAllPermissions( perms )) {
        		if (path=="*other")
        			return {name:name, path:_.keys(this.permissions)[0], args:args};
    			else
    				return {name:name, path:path, args:args};
        	}
        	
        	// Find first route that has all permissions
        	var routes = _.keys(this.permissions);
        	perms = _.values(this.permissions);
        	for(var i=0; i<routes.length; i++) {
        		if (App.hasAllPermissions(perms[i])) {
        			var updatedRoute = this.getRouteName(routes[i]);
    				return {name:updatedRoute, path:routes[i], args:""};
        		}
        	}
        	
        	return {name:getRouteName(routes[i]), path:routes[i], args:""};
        },
        
        // Return i18n ref for 1st link in breadcrumb 
        getDefaultHome: function() {
        	try {
            	var route = this.checkPermissions("defaultPage", "*other");
            	return {
            		path:	route.path,
            		label: 	this.breadCrumbHome[route.path][0],
            		icon:	this.breadCrumbHome[route.path][1]
            	
            	}
        	} catch(err){
        	}
        	
        	return {
        		path:	"",
        		label: 	"dashboard"	
        	}
        },
        
        getRouteName: function(path) {
        	if (_.isUndefined(this.appRoutes[path])) {
        		App.error("Router path not found: " + path);
        		return "*other";
        	} else {
        		return this.appRoutes[path];
        	}
        	
        },
       
       initialize:function() {
    	var self = this;
    	var home = this.getDefaultHome();
    	
    	this.controller = new FrontController(home);
       	App.vent.listenTo(App.vent, 'application:route', function(path, args) {
       		App.log("application:route path: " + path);
       		App.log("application:route args: " + args);
       		self.applicationRoute(self, path);
       	});
       },
       
       applicationRoute:function(self, path) {
    	   self.navigate(path, {trigger: true});
       },
       
       onRoute: function(name, path, args) {
       	$('html, body').scrollTop(0); //For scrolling to top
    	var updateRoutes = this.checkPermissions(name, path, args);
    	
    	if (!_.isUndefined(updateRoutes)) {
        	if (path != updateRoutes.path) {
    			App.log("Redirecting to: " + updateRoutes.path);
    			App.vent.trigger("application:route", updateRoutes.path);
    		}
        	
        	name = updateRoutes.name;
        	path = updateRoutes.path;
        	args = updateRoutes.args;
    	}
    	
    	App.log("onRoute name: " + name);
    	App.log("onRoute path: " + path);
    	App.log("onRoute args: " + args);
    	
    	var realpath = path;
    	if (path.indexOf('*') >= 0) realpath = path.substring(0, path.indexOf( "*" ));
    	if (path.indexOf('?') >= 0) realpath = path.substring(0, path.indexOf( "?" ));
       	
    	// This line actually sets the default page
    	this.controller.sidebar.setActive(realpath, args);
       },
       execute: function(callback, args, name) {
    	    var status = {
    	    		msg: App.i18ntxt.navigateWarning
    	    };
      		Backbone.trigger('application:beforenavigate', status);
      		
      		if(App.unsavedChanges) {
      			App.unsavedChanges = false;
      		  var confirmed = window.confirm(status.msg || App.i18ntxt.navigateWarning);
      		  if (!confirmed){
      		    /*var history = self.history;
      		    if (history.length > 1){
      		    	self.navigate(history[history.length-2], {replace: true});                            
      		     }*/
      			  App.unsavedChanges = true;
      			  return false;
      		  }
      		}
	    if (callback) callback.apply(this, args);
	   }
   });
   
   return BaseAppRouter;
});
