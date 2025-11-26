define( ['jquery', 'backbone', 'App', 'marionette'],
    function($, BackBone, App, Marionette) {
        //ItemView provides some default rendering logic
        var ProfileDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},        	
        	url: 'api/wusers/profile',
  		  	template: "UserProfile#profiledialogview",
            initialize: function (options) {;
            	if (!_.isUndefined(options) && !_.isUndefined(options.model)) this.model = options.model;
            },
            
            onRender: function () {
            	// Set Select Options
            	this.$("#title").val(this.model.get("title"));
            	this.$("#language").val(this.model.get("language"));
            	this.$("#departmentID").val(this.model.get("departmentID"));
            },
            
            ui: {
                view: "",
                save: ".profileUpdateButton"
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
        return ProfileDialogView;
    });
