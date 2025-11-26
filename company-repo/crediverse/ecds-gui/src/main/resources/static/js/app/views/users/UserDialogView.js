define( ['jquery', 'backbone', 'App', 'marionette'],
    function($, BackBone, App, Marionette) {
        //ItemView provides some default rendering logic
        var UserDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},        	
        	url: 'api/wusers',
  		  	template: "ManageUsersTableView#userdialogview",
            initialize: function (options) {;
            	if (!_.isUndefined(options) && !_.isUndefined(options.model)) this.model = options.model;
            },
            
            onRender: function () {
            	// Set Select Options
            	this.$("#language").val(this.model.get("language"));
            	this.$("#state").val(this.model.get("state"));
            	this.$("#departmentID").val(this.model.get("departmentID"));
            	this.$("#roleID").val(this.model.get("roleID"));
            },
            
            ui: {
                view: "",
                save: ".userCreateButton"
            },
            
            // View Event Handlers
            events: {
            	"click @ui.save": "saveUser"
            },
            
            saveUser: function() {
            	var self = this;
            	this.model.save({
            		success: function(ev){
            			var dialog = self.$el.closest(".modal");
            			dialog.modal("hide");
            		},
            		error: function(ev){
            			var dialog = self.$el.closest(".modal");
            		}
				});
            }
        });
        return UserDialogView;
    });