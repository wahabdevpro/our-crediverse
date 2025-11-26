define( ['jquery', 'backbone', 'App', 'marionette', 'models/AreaModel'],
    function($, BackBone, App, Marionette, AreaModel) {
        //ItemView provides some default rendering logic
        var AreaView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
        	//url: 'api/areas',
  		  	template: "ManageAreas#singleareaview",
  		  	//model: App.permissions,
  		  	error: null,
  		  	tierList: null,
            initialize: function (options) {;
            	if (!_.isUndefined(options) && !_.isUndefined(options.model)) this.model = options.model;
            	var self = this;
            	this.model = new CellModel({id: self.id});
            	this.model.fetch({
            		success: function(ev){
						self.render();
					},
				});
            },

            onRender: function () {
            	

            },
            
            ui: {
                view: '',
                save: '.areaSaveButton'
                	
            },

            // View Event Handlers
            events: {
            	"click @ui.save": 'saveArea'
            },
            
            saveArea: function() {
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
        return AreaView;
    });