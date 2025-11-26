define( ['jquery', 'backbone', 'App', 'marionette'],
    function($, BackBone, App, Marionette) {
        //ItemView provides some default rendering logic
        var GroupDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	url: 'api/permissions',
  		  	template: "ManageGroups#singlegroupview",
  		  	//model: App.permissions,
  		  	error: null,
  		  	tierList: null,

            initialize: function (options) {;
            	if (!_.isUndefined(options) && !_.isUndefined(options.model)) this.model = options.model;

				options = options || {};
            	this.model.set('tierList', options.tierList);
            },

            ui: {
                view: '',
                save: '.groupSaveButton'
                	
            },

            // View Event Handlers
            events: {
            	"click @ui.save": 'saveGroup'
            },
            
            saveGroup: function() {
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
        return GroupDialogView;
    });