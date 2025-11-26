define( ['jquery', 'App', 'underscore', 'marionette', 'handlebars', 'utils/HandlebarHelpers',
         'models/FeatureBarModel', 'models/ValidationModel', 'views/config/GenericConfigDialog', 'toastr','codemirror'
         ],
    function($, App, _, Marionette, Handlebars, HBHelper,
    		FeatureBarModel, ValidationModel, GenericConfigDialog, toastr, CodeMirror) {

        var GenericConfigView =  Marionette.LayoutView.extend( {
        	tagName: 'div',
        	
        	//Required in inherited class
        	template: null,     	
        	dialogTemplate: null,
        	dialogTitle: null,
        	dialogTextAreaHeight: 80,
        	dialogInit: null,
        	url: null,

			featureBar: null,

        	dialogOnRender: null,
        	
        	// Internal
        	model: null,
        	variables: null,
        	
        	i18ntxt: App.i18ntxt.config,
        	
        	// Override in Specific Configuration
        	pageBreadCrumb: function() {
        		return {
	  				text: "Override",
	  				href: "#dashboard"
	  			};
        	},
        	
  		  	breadcrumb: function() {
  		  		var txt = this.i18ntxt;
  		  		return {
  		  			heading: txt.configuration,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.configuration,
						iclass: "fa fa-cogs"
  		  			}, this.pageBreadCrumb()]
  		  		}
  		  	},
        	
            events: {
            	"click @ui.showUpdateDialog": 'showUpdateDialog'
            },
            
            /*
             * Given a query string such as tab=a&owner=john convert to a JSON object such as {"tab":"a","owner":"john"}
             */
            queryToObject: function(string) {
        	  var result = {}, components, i, firstPair, current;
        	  if (!_.isUndefined(string) && !_.isNull(string)) {
	        	  components = string.split("&");
	
	        	  for (i = 0; i < components.length; i++) {
	        	    if (components[i].indexOf("=") !== -1) {
	        	      if (typeof current !== "undefined") {
	        	        result[current] = result[current].join("&");
	        	      }
	        	      firstPair = components[i].split("=");
	        	      current = firstPair[0];
	        	      result[current] = [firstPair[1]];
	        	    } else {
	        	      result[current].push(components[i]);
	        	    }
	        	  }
	
	        	  result[current] = result[current].join("&");
        	  }
        	  return result;
            },
        	
        	parseArgs: function(options, args) {
        		options.urlId = args[0];
        		options.urlEnd = args[1];
        		$.extend(options, this.queryToObject(args[2]));
            },
            
            getQueryDetails: function() {
            	var fragment = Backbone.history.decodeFragment(Backbone.history.getFragment());
        		var params = null;
        		var start = fragment.indexOf('?');
        		if (start > 0) {
        			params = this.queryToObject(fragment.substring(start+1, fragment.length));
        		}
        		else {
        			params = {};
        		}
        		return params;
            },
            
            updateQueryString: function(name, value) {
        		var fragment = Backbone.history.decodeFragment(Backbone.history.getFragment());
        		var params = null;
        		var start = fragment.indexOf('?');
        		var baseUrl = fragment;
        		
        		value = value.replace(/^#/, '');

        		if (start > 0) {
        			params = this.queryToObject(fragment.substring(start+1, fragment.length));
        			baseUrl = fragment.substring(0, start);
        		}
        		else {
        			params = {};
        		}
        		params.tab = value;
        		App.appRouter.navigate(baseUrl+'?'+$.param(params), {trigger: false, replace: false});
        	},
            
        	initialize: function (options) {
        		try {
        			HBHelper.registerConfigRenderHelpers();
					this.featureBar = new FeatureBarModel();
        			
        		} catch(err) {
        			App.error(err);
        		}
        		if (!_.isUndefined(options) && !_.isUndefined(options.args)) {
        			this.parseArgs(options, options.args);
        		}
        		if (!_.isUndefined(this.subInit) && _.isFunction(this.subInit)) {
        			this.subInit(options);
        		}
            },
            
            /*
             * Retrieve Agent configuration, then re-render page
             */
            retrieveConfigData: function(dataRefresh) {
            	var self = this;

				// Fetch config
            	this.model = new ValidationModel();
            	this.model.url = this.url;

            	$.when(
            		$.get(self.url + "/vars", function(data) {
            			self.variables = data;
            		}), 
            		this.model.fetch()
            	)
            	.done(function() {
					let featureNames = [
						'smsConfigurationFeature',
						'mobileMoneyNotificationFeature'
					];
					const promises = [];

					featureNames.forEach((featureName) => {
						self.featureBar.setFeature(featureName);
						const featurePromise = self.featureBar.fetch().then(isEnabled => {
							self.model.set(featureName, isEnabled ? "enabled" : "disabled");
						})
						promises.push(featurePromise);
					});

					Promise.all(promises).then(() => self.render());
            	});
            	
			},
            
			onRender: function () {
				var that = this;
				if (!_.isUndefined(this.beforeRender) && _.isFunction(this.beforeRender)) {
					this.beforeRender(this.options);
				}
				
				if (!_.isUndefined(this.renderRegions) && _.isFunction(this.renderRegions)) {
        			this.renderRegions(function() {
        				if (that.model == null) {
        					that.$el.find(".form-horizontal").hide();
        					that.retrieveConfigData();
    					} else {
    						that.$el.find(".form-horizontal").show();
    					}
        			});
        		}
				else {
					if (this.model == null) {
						this.$el.find(".form-horizontal").hide();
						this.retrieveConfigData();
					} else {
						this.$el.find(".form-horizontal").show();
					}
				}
				if (!_.isUndefined(this.afterRender) && _.isFunction(this.afterRender)) {
					this.afterRender(this.options);
				}
			},
			
			showUpdateDialog: function() {
				var self = this;
				var dialogModel = null;
				
				if (!_.isUndefined(this.processBeforeSave) && _.isFunction(this.processBeforeSave)) {
					self.model.processBeforeSave = this.processBeforeSave;
				}
				
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: GenericConfigDialog,
            		title:self.dialogTitle,
            		hide: function() {
            			self.retrieveConfigData(true);
	        		},
            		params: {
            			model: self.model,
            			variables:self.variables,
            			template: self.dialogTemplate,
            			textAreaHeight: self.dialogTextAreaHeight,
            			dialogOnRender: self.dialogOnRender
            		}
            	});
            	return false;
			},
			
        });
        
        return GenericConfigView;
});
