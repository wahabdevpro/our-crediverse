define( ['jquery', 'underscore', 'backbone', 'App', 'marionette', 'models/AreaModel', 'utils/CommonUtils'],
    function($, _, BackBone, App, Marionette, AreaModel, CommonUtils) {
        //ItemView provides some default rendering logic
		var i18ntxt = App.i18ntxt.areas;
		var AreaDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	attributes: {
        		class: "modal-content"
        	},
  		  	template: "ManageAreas#singleareaview",  		  	
  		  	error: null,
            initialize: function (options) {
            	if (!_.isUndefined(options) && !_.isUndefined(options.model))
            		this.model = options.model;
            },

            onRender: function () {           	
            	var parentAreaElement = this.$('#parentAreaID');
  		  		CommonUtils.configureSelect2Control({
  		  			jqElement: parentAreaElement,
  		  			url: "api/areas/dropdown",
  		  			placeholderText: i18ntxt.parentArea,
  		  			minLength: 0,
					isHtml: true
  		  		});
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
            		}
				});
            }
        });
        return AreaDialogView;
    });