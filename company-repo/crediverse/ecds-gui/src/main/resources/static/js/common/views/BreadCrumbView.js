define( ['jquery', 'underscore', 'App', 'marionette', 'handlebars', 'utils/HandlebarHelpers'],
    function($, _, App, Marionette, Handlebars, HBHelper) {
        //ItemView provides some default rendering logic
        var BreadCrumbView =  Marionette.ItemView.extend( {
        	tagName: "section",
        	
        	attributes: {
  			  class: "content-header"
  		  	},
  		  	
            //Template HTML string
            template: 'BreadCrumb#breadcrumb',
            model: null,
            defaultHome: true,
/**
	Note: if defaultHome := false, implies that home link will not be inserted automatically (default behaviour if not provided)
	      this implies you will need to add all breadcrumbs in the order they are displayed

 		this.model= new Backbone.Model({
    		heading: "heading",
    		subheading: "subheading",
    		breadcrumb: [{
    			text: "Dashboard",
    			href: "#dashboard",
    			iclass: "fa fa-dashboard"
    		}, {
    			text: "groups",
    			href: "#groups"
    		}]
    	});
    	
    	same as:
    	
 		this.model= new Backbone.Model({
    		heading: "heading",
    		subheading: "subheading",
    		defaultHome: true, 
    		breadcrumb: [{
    			text: "groups",
    			href: "#groups"
    		}]
    	});
 */            
            
            initialize: function (options) {
            	HBHelper.registerLoops();
            	
            	if (!_.isUndefined(options)) {
            		
            		if (!_.isUndefined(options.defaultHome) && options.defaultHome) {

            			// Prefix breadcrumb with default home
            			var icon = _.isUndefined(options.home.icon)? "fa-dashboard" : options.home.icon;
            			icon = ((icon.indexOf("fa") === 0)? "fa " : "glyphicon ") + icon; 
            			
            			options.breadcrumb.unshift({
                			text: _.isUndefined(options.home.label)? App.i18ntxt.global.homeBC : options.home.label,
							href: _.isUndefined(options.home.path)? "#dashboard" : "#" + options.home.path,
                			iclass: icon
            			});
            				
            		}
            		
            		if (!_.isUndefined(options.template)) this.template = options.template;
            		
            		this.model= new Backbone.Model(options);
            	}
            },
            
            onRender: function () {
            	
            },
            
            ui: {
                breadcrumbs: '',
            },

        });
        return BreadCrumbView;
    });
