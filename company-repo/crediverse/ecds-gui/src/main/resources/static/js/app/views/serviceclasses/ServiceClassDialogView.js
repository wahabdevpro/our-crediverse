define( ['jquery', 'backbone', 'App', 'marionette'],
    function($, BackBone, App, Marionette) {
        var ServiceClassDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	url: 'api/permissions',
  		  	template: "ManageServiceClasses#serviceClassDialog",
  		  	//model: App.permissions,
  		  	error: null,
  		  	tierList: null,
            initialize: function (options) {;
            	//if (!_.isUndefined(options) && !_.isUndefined(options.model)) this.model = options.model;
            },

            onRender: function () {
            	

            },
            
            ui: {
                view: '',
                save: '.scCreateButton'
                	
            },

            // View Event Handlers
            events: {
            	"click @ui.save": 'saveSc'
            },
            
            saveSc: function() {
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
        return ServiceClassDialogView;
    });