define( ['jquery', 'App', 'backbone', 'marionette', "handlebars", 
         'views/groups/GroupDialogView', 'models/GroupModel', 'utils/CommonUtils',
         'utils/HandlebarHelpers', 'models/GroupModel', 'datatables'],
    function($, App, BackBone, Marionette, Handlebars, 
    		GroupDialogView, GroupModel, CommonUtils,
    		HBHelper, GroupModel) {
        //ItemView provides some default rendering logic
        var GroupView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "row"
        	},
  		  	template: "GroupView#groupdetails",
  		  	url: 'api/groups/',
  		  	error: null,
			tierList: null,
			id: null,
			model: null,
			i18ntxt: App.i18ntxt.groups,
  		  	breadcrumb: function() {
  		  		var txt = this.i18ntxt;
  		  		return {
  		  			heading: txt.groupProfile,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.groupManagement,
  		  				href: "#groups",
						iclass: "fa fa-reorder"
  		  			}, {
  		  				text: txt.groupProfile,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},
  		  	
            initialize: function (options) {
            	var self = this;
            	
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.id)) {
            			this.model = new GroupModel({id: options.id});
            		}
            	}
            },
            
            // TODO: This needs to change to model load before configuration update
            // Model needs to be cleaned up first
            onRender: function () {
            	var self = this;
            	if (this.model == null) {
                	this.model = new GroupModel({id: self.id});
                	this.model.fetch({
                		success: function(ev){
    						self.render();
    					},
    				});
            	}
            },
            
            ui: {
            	editGroup: '.editGroupButton',
                deleteGroup: '.deleteGroupButton'
            },

            events: {
            	"click @ui.editGroup": 'editGroup',
            	"click @ui.deleteGroup": 'deleteGroup'
            },
            
            deleteGroup: function(ev) {
            	var self = this;
            	var data = this.model.attributes;
            	
            	CommonUtils.delete({
	        		itemType: App.i18ntxt.groups.group,
	        		url: self.url+data.id,
	        		data: data,
	        		context: {
	        			what: App.i18ntxt.groups.group,
	        			name: data.name,
	        			description: data.description
	        		},
	        		rowElement: this.$el,
	        	}, {
	        		success: function(model, response) {
	        			App.appRouter.navigate('#groups', {trigger: true, replace: true});
	        		},
	        		error: function(model, response) {
	        			App.error(reponse);
	        		}
	        	});
	        	
            },
            
            editGroup: function(ev) {
            	var self = this;
            	var data = this.model.attributes;
            	this.model.set('editMode', true);
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: GroupDialogView,
            		title:CommonUtils.renderHtml(App.i18ntxt.groups.editModalTitle, {name: data.name}),
            		hide: function() {
	        			self
	        			.render();
	        		},
            		params: {
            			model: self.model
            		}
            	});
            	return false;
            }
        });
        return GroupView;
    });
