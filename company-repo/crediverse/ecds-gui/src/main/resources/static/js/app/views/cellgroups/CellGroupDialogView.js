define( ['jquery', 'backbone', 'App', 'marionette', 'models/CellGroupModel'],
    function($, BackBone, App, Marionette, CellGroupModel) {
        //ItemView provides some default rendering logic
        var CellGroupDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	url: 'api/permissions',
  		  	template: "ManageCellGroups#singlecellgroupview",  		  	
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
                save: '.cellGroupSaveButton'
                	
            },

            // View Event Handlers
            events: {
            	"click @ui.save": 'saveCellGroup'
            },
            
            saveCellGroup: function() {
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
        return CellGroupDialogView;
    });
