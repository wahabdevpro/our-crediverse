define( ['jquery', 'App', 'underscore', 'marionette', 'utils/CommonUtils',
         'views/config/GenericConfigDialog',
         'file-upload', 'jquery-sortable'],
    function($, App, _, Marionette, CommonUtils,
    		GenericConfigDialog) {

        var UssdNameView = GenericConfigDialog.extend( {
        	template: 'UssdMenuConfiguration#ussdMenuName',
        	
        	tagName: 'div',
        	attributes: {
        		class: "modal-content",
        		id: "ussdMenuItem"
        	},
        	
        	
        	i18ntxt: App.i18ntxt.ussdmenu,
        	ui: {
        		itemSaveButton: '.itemSaveButton'
        	},
        	
        	// DOM Events
            events: {
            	"click @ui.itemSaveButton": 'saveItem'
            },
            
            saveItem: function(ev) {
            	var data = Backbone.Syphon.serialize($('form'));
            	this.model.set(data);
            	this.model.set('unsavedChanges', true);
            	App.unsavedChanges = true;
            	if (!_.isUndefined(this.options) && !_.isNull(this.options) && !_.isUndefined(this.options.save) && _.isFunction(this.options.save)) {
            		this.options.save(this.model);
            	}
            	var dialog = this.$el.closest('.modal');
    			dialog.modal('hide');
    			return false;
            },
        	
        	initialize:function (options) {
            },
            
            onRender: function (ev) {
            	
            }
        });
        
        return UssdNameView;
});