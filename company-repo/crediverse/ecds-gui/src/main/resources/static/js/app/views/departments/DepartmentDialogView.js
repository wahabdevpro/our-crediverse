define( ['jquery', 'backbone', 'App', 'marionette', 'models/DepartmentModel'],
    function($, BackBone, App, Marionette, DepartmentModel) {
        //ItemView provides some default rendering logic
        var DepartmentDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	url: 'api/permissions',
  		  	template: "ManageDepartments#singledepartmentview",  		  	
  		  	error: null,
  		  	tierList: null,
            initialize: function (options) {;
            	if (!_.isUndefined(options) && !_.isUndefined(options.model)) this.model = options.model;
            	var self = this;
            },

            onRender: function () {
            	

            },
            
            ui: {
                view: '',
                save: '.departmentSaveButton'
                	
            },

            // View Event Handlers
            events: {
            	"click @ui.save": 'saveDepartment'
            },
            
            saveDepartment: function() {
            	var self = this;
            	this.model.save({
            		success: function(ev){
            			var dialog = self.$el.closest('.modal');
            			dialog.modal('hide');
            		},
            		error: function(ev){
            			var dialog = self.$el.closest('.modal');
            			///dialog.modal('hide');
            		}
				});
            }
        });
        return DepartmentDialogView;
    });