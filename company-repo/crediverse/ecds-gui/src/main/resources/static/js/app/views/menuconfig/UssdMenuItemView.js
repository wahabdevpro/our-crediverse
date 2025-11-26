define( ['jquery', 'App', 'underscore', 'marionette', 'utils/CommonUtils',
         'views/config/GenericConfigDialog',
         'file-upload', 'jquery-sortable'],
    function($, App, _, Marionette, CommonUtils,
    		GenericConfigDialog) {

		var i18ntxt = App.i18ntxt.ussdmenu;
	
        var UssdMenuItemView = GenericConfigDialog.extend( {
        	template: 'UssdMenuConfiguration#ussdMenuItem',
        	
        	tagName: 'div',
        	attributes: {
        		class: "modal-content",
        		id: "ussdMenuItem"
        	},
        	
        	ui: {
        		itemSaveButton: '.itemSaveButton'
        	},
        	
        	// DOM Events
            events: {
            	"click @ui.itemSaveButton": 'saveItem'
            },
            
            saveItem: function(ev) {
            	var data = Backbone.Syphon.serialize($('form'));
            	for(var i=0; i<this.editors.length; i++) {
            		var nl = this.editorNames[i];
            		var name = nl.name.split(".");
            		//TODO: Cleanup!!!!
            		if (name.length == 1) {
            			data[nl.name]["texts"][nl.lang] = this.editors[i].getValue();
            		} else if (name.length == 2) {
            			data[name[0]][name[1]]["texts"][nl.lang] = this.editors[i].getValue();
            		}
            			
            	}
            	/*var nextItem = $("#nextMenuID").select2('data');
            	if (!_.isUndefined(nextItem[0].item)) {
            		data.nextMenuID = nextItem[0].item.menuId;
                	data.nextMenuOffset = nextItem[0].item.menuOffset;
            	}
            	
            	var command =  $("#commandID").select2('data');
            	if (!_.isUndefined(command[0].item)) {
            		data.commandID = command[0].item.id;
                	data.commandName = command[0].item.name.texts.en;
            	}*/
            	
            	this.model.set(data);
            	this.model.set('unsavedChanges', true);
            	App.unsavedChanges = true;
            	if (!_.isUndefined(this.options) && !_.isNull(this.options) && !_.isUndefined(this.options.save) && _.isFunction(this.options.save)) {
            		this.options.save(this.model);
            	}
            	var dialog = this.$el.closest('.modal');
    			dialog.modal('hide');
            },
        	
        	breadcrumb: function() {
  		  		return {
  		  			heading: App.i18ntxt.config.ussdMenuHeading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: App.i18ntxt.config.ussdMenuBC,
  		  				href: window.location.hash
  		  			}]
  		  		}
  		  	},
            
  		  	dialogOnRender: function (ev) {
            	this.$('#type').select2({
            		url: "api/config/ussdmenu/typelist",
            		placeholder: i18ntxt.itemTypePlaceHolder
            	});
            	
            	this.$('#nextMenuID').select2({
            		url: "api/config/ussdmenu/idlist",
            		placeholder: i18ntxt.nextMenuPlaceHolder,
            		ajax: {
            			processResults: function (data) {
	                    	return {
	                    		results: $.map(data, function (item, i) {
	                    			return {
	                    				id: i,
	                    				text: item.menuName,
	                            		item: item
	                                }
	                            })
	                        };
            			}
                    }
            	});
            	
            	this.$('#commandID').select2({
            		url: "api/config/ussdmenu/commands",
            		placeholder: i18ntxt.commandPlaceHolder,
            		ajax: {
            			processResults: function (data) {
	                    	return {
	                    		results: $.map(data, function (item, i) {
	                    			return {
	                    				id: item.id,
	                    				text: item.name.texts.en,
	                            		item: item
	                                }
	                            })
	                        };
            			}
                    }
            	});

            	var adjustment;
            	var list = this.$("ol.simple_with_animation");
            	list.sortable({
            		  group: 'simple_with_animation',
            		  pullPlaceholder: false,
            		  // animation on drop
            		  onDrop: function  ($item, container, _super) {
            		    var $clonedItem = $('<li/>').css({height: 0});
            		    $item.before($clonedItem);
            		    $clonedItem.animate({'height': $item.height()});

            		    $item.animate($clonedItem.position(), function  () {
            		      $clonedItem.detach();
            		      _super($item, container);
            		    });
            		  },

            		  // set $item relative to cursor position
            		  onDragStart: function ($item, container, _super) {
            		    var offset = $item.offset(),
            		        pointer = container.rootGroup.pointer;

            		    adjustment = {
            		      left: pointer.left - offset.left,
            		      top: pointer.top - offset.top
            		    };

            		    _super($item, container);
            		  },
            		  onDrag: function ($item, position) {
            		    $item.css({
            		      left: position.left - adjustment.left,
            		      top: position.top - adjustment.top
            		    });
            		  }
            	});
            }
        });
        
        return UssdMenuItemView;
});