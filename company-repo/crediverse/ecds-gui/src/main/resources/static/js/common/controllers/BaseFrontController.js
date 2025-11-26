define(['underscore', 'App', 'backbone', 'marionette', 'utils/HandlebarHelpers',
        'views/NavbarMenuView', 'models/ContextModel',
        'views/MainSideBarView', 'views/BreadCrumbView'],
        
    function (_, App, Backbone, Marionette, HBHelper,
    		NavbarMenuView, ContextModel,
    		MainSideBarView, BreadCrumbView) {

    var BaseFrontController = Marionette.Controller.extend({
    	sidebar: null,
    	
    	navbarmenu: new NavbarMenuView({model: new ContextModel()}),
    	
    	breadCrumbHome: {
    		label: 	"",
    		path:	"",
    		icon:	""
    	},
    	
    	constructor : function (options) {
    		this.initBaseFrontController(options);
    		
    		// Following will call inherited initialize
    		Marionette.Controller.prototype.constructor.call(this, options);
    	},
    	
    	initBaseFrontController:function (options) {
    		if (!_.isUndefined(options.path)) this.breadCrumbHome.path = options.path;
    		if (!_.isUndefined(options.label)) this.breadCrumbHome.label = App.translate("navbar." + options.label);
    		if (!_.isUndefined(options.icon)) this.breadCrumbHome.icon = options.icon;
    		
        	this.sidebar = new MainSideBarView()
        	App.mainSideBar.show(this.sidebar);
        	var self = this;
        	this.navbarmenu.model.fetch({
        		success: function() {
    				self.navbarmenu.model.set(App.i18ntxt.navbar);
    				App.navbarMenu.show(self.navbarmenu);
        		}
        	});
        },
        
        loadData: function(view) {
        	if (!_.isUndefined(view.model) && !_.isNull(view.model) && !_.isUndefined(view.model.autofetch) && view.model.autofetch) {
				view.model.fetch({
					success: function(model, response, options) {
						App.mainContent.show(view);
					},
					error:   function(model, xhr, options) {
						App.error("Failed to fetch model");
					}
				});
			}
			else if (!_.isUndefined(view.collection) && !_.isNull(view.collection) && !_.isUndefined(view.collection.autofetch) && view.collection.autofetch) {
				view.collection.fetch({
					success: function(collection, response, options) {
						App.mainContent.show(view);
					},
					error:   function(collection, xhr, options) {
						App.error("Failed to fetch model");
					}
				});
			}
			else {
				App.mainContent.show(view);
			}
        },
        
        loadView: function(viewClass, props) {
        	var that = this;
        	var view = null;
        	if (_.isUndefined(props))
        		view = new viewClass();
        	else if (_.isArguments(props)) {
        		view = new viewClass({
        			args: props
        		});
        	}
        	else
        		view = new viewClass(props);

        	try {
        		this.createBreadcrumbBar(view);
    		} catch(error) {
    			App.error(error);
    		}
    		
    		try {
    			if (!_.isUndefined(view.preloadModels) && !_.isNull(view.preloadModels) && _.isFunction(view.preloadModels)) {
    				try {
    					$.when( view.preloadModels() ).done(function() {
    						that.loadData(view);
    					});
    				}
    				catch(err) {
    					App.error("Failed to fetch model : "+err);
    					that.loadData(view);
    				}
    			}
    			else {
    				that.loadData(view);
    			}
    			
    		} catch(error) {
    			App.error(error);
    		}
        },

        /**
         * If you want to render the breadcrumb from the page, 
         * simply create an attribute: "renderBreadCrumbView: null" 
         * on the View and call this.renderBreadCrumbView(), 
         * when you need to render the BreadCrumb 
         */
        createBreadcrumbBar: function(view) {
        	if (!_.isUndefined(view.renderBreadCrumbView)) {
        		var self = this;	
        		view.renderBreadCrumbView = function() {
        			self.showBreadCrumb(view);
        		};
        	}
        	else {
        		this.showBreadCrumb(view);
        	}
        },
        
        showBreadCrumb: function(view) {
			var options = view.breadcrumb();
			options["home"] = this.breadCrumbHome;
			var bcv = new BreadCrumbView(options);
			App.breadcrumb.show( bcv );
        },

        defaultPage: function() {
        	// Done by Router
        }
    });
    
    return BaseFrontController;
});
