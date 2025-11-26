define( ['jquery', 'App', 'backbone', 'marionette', "handlebars", 
         'utils/HandlebarHelpers', 'models/ServiceClassValidationModel', 'views/serviceclasses/ServiceClassDialogView', 'utils/CommonUtils', 'datatables'],
    function($, App, BackBone, Marionette, Handlebars, 
    		HBHelper, ServiceClassValidationModel, ServiceClassDialogView, CommonUtils) {
        //ItemView provides some default rendering logic
        var ServiceClassView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "ServiceClassView#serviceclassdetails",
  		  	url: 'api/serviceclass/',
  		  	error: null,
			id: null,
			model: null,
			i18ntxt: App.i18ntxt.serviceclass,
  		  	breadcrumb: function() {
  		  		var txt = this.i18ntxt;
  		  		return {
  		  			heading: txt.serviceClassProfile,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.serviceClassList,
  		  				href: "#serviceClasses",
						iclass:"fa fa-flag"
  		  			}, {
  		  				text: txt.serviceClassProfile,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function (options) {
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.id)) {
            			this.model = new ServiceClassValidationModel({id: options.id});
            		}
            	}
            },
            
            onRender: function () {
            	var self = this;
            },
            
            editSc: function(ev) {
            	var self = this;
            	var scData = this.model.attributes;
            	var title = CommonUtils.renderHtml(App.i18ntxt.serviceclass.editServiceClass, {name: scData.name});
            		
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: ServiceClassDialogView,
            		title: title,
            		model: this.model,	// For Auto-Form text insert
            		hide: function() {
	        			self
	        			.render();
	        		},
            		params: {
            			model: self.model
            		}
            	});
            	return false;
            },
                        
            deleteSc: function(ev) {
            	var self = this;
            	var data = this.model.attributes;

	        	CommonUtils.delete({
	        		itemType: App.i18ntxt.serviceclass.serviceClass,
	        		url: 'api/serviceclass/'+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.serviceclass.serviceClass,
	        			name: data.name,
	        			description: data.description
	        		}
	        	},
	        	{
	        		success: function(model, response) {
	        			App.appRouter.navigate('#serviceClasses', {trigger: true, replace: true});
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
            },
            
            ui: {
            	editSc: 	'.editClassButton',
            	deleteSc: 	'.deleteClassButton',
            },

            events: {
            	"click @ui.editSc":		'editSc',
            	"click @ui.deleteSc":	'deleteSc',
            }
        });
        return ServiceClassView;
    });

