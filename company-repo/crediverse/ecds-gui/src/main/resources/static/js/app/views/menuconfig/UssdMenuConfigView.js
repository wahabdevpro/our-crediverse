define( ['jquery', 'App', 'handlebars', 'underscore', 'marionette', 'utils/CommonUtils', 'views/menuconfig/UssdMenuItemView', 
         'views/menuconfig/UssdMenuView', 'views/config/GenericConfigView', 'views/menuconfig/UssdMenuToolbarView',
         'models/UssdMenuModel', 'models/UssdMenuItemModel', 'i18n!common/violations', 'file-upload', 'jquery-sortable'],
    function($, App, Handlebars, _, Marionette, CommonUtils, UssdMenuItemView, 
    		UssdMenuView, GenericConfigView, UssdMenuToolbarView,
    		UssdMenuModel, UssdMenuItemModel, violationMsgs) {

		var i18ntxt = App.i18ntxt.ussdmenu;
	
        var UssdMenuConfigView = GenericConfigView.extend( {
        	//template: 'UssdMenuConfiguration#ussdMenuConfig',
        	template: 'configuration/UssdMenu#ussdMenuConfig',
        	
        	regions: {
        		ussdMenuRegion: 	"#menu-configuration",
        		ussdmenuToolbar: 	"#toolbar"
        	},
        	
        	//dialogTemplate: "UssdMenuConfiguration#ussdMenuConfigModal",
        	
        	url: 'api/config/ussdmenu',
        	currentItemView:null,
        	ui: {
        	    
        	},
        	carouselHistory: [],
        	// DOM Events
            events: {
            	
            },
            
            enableToggle: function(ev) {
            	var $itemToggle = $(ev.currentTarget);
            	var visibleMenu = this.$('#menu-container .active');
            	var menuId = visibleMenu.attr('data-menu-id');
        		var menuOffset = visibleMenu.attr('data-menu-offset');
            	$itemToggle.toggleClass('on').toggleClass('off');
            	if ($itemToggle.hasClass('on')) {
            		$itemToggle.closest('.panel').addClass('panel-info').removeClass('panel-default');
            		this.model.toggleButton(menuId, menuOffset, $itemToggle.attr('data-button-id'), false);
            	}
            	else {
            		$itemToggle.closest('.panel').addClass('panel-default').removeClass('panel-info');
            		this.model.toggleButton(menuId, menuOffset, $itemToggle.attr('data-button-id'), true);
            	}
            	return false;
            },

        	dialogTitle: App.i18ntxt.config.ussdMenuModalTitle,

            navigateToItem: function(ev) {
            	var clickedItem = $(ev.currentTarget).attr('href').split('-')[1];
            	//console.log(clickedItem);
            	var menuContainer = this.$('#menu-container');
            	menuContainer.carousel(parseInt(clickedItem));
            	return false;
            },
            
            previousMenu: function(ev) {

            },

            nextMenu: function(ev) {
            	return false;
            },
            
            displayMenu: function(container, menu) {
            	
            },
            
            configureCarouselButtons: function() {
            	var that = this;
            	
            	var visibleMenu = this.$('#menu-container .active');
        		var menuId = visibleMenu.attr('data-menu-id');
        		if (this.model.isFirstMenuId(menuId)) {
        			this.$('.carousel-left-button').addClass('hide');
        		}
        		else {
        			this.$('.carousel-left-button').removeClass('hide');
        			if (this.model.isLastMenuId(menuId)) {
        				this.$('.carousel-right-button').addClass('hide');
            		}
            		else {
            			this.$('.carousel-right-button').removeClass('hide');
            		}
        		}
            },
            
            renderRegions: function (callback) {
            	var that = this;
            	var toolbarView = new UssdMenuToolbarView();
            	that.ussdmenuToolbar.show(toolbarView);
            	
        		var menuModel = new UssdMenuModel({
        			url: '/api/config/ussdmenu'
        		}).fetch({
        			success: function(mdl) {
        				var options = $.extend({
    						model: mdl,
    						prompt: true,
    						save: function(mdl){
    							mdl.set('unsavedChanges', true);
    						}
    					}, that.options)
    					var view = new UssdMenuView(options);

        				that.ussdMenuRegion.show(view);
        				toolbarView.model = mdl;
        				toolbarView.menuContainer = this.$('#menu-container');
        				toolbarView.registerRenderer(that.render);
        				
        				//view.setDefaltTab();
        				callback();
        			},
        			error: function(ev) {
        				App.log('Failed to load USSD menu data');
        			}
        		});
            },
            
            menuEditItem: function(ev) {
            	var that = this;
            	var currentItem = this.$(ev.currentTarget)
            	var currentButtonId = currentItem.attr('data-button-id');
            	var currentMenuId = currentItem.closest('ol').attr('data-menu-id');
            	var itemJson = this.model.getMenuItem(currentMenuId, currentButtonId);
            	//alert(JSON.stringify(itemJson));
            	var itemModel = new UssdMenuItemModel(itemJson);
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
					class: 'modal-lg',
            		view: UssdMenuItemView,
            		//title:CommonUtils.renderHtml(this.i18ntxt.ruleEditDialogTitle, {name: tableData.name}),
            		title: i18ntxt.editItemTitle,
            		hide: function() {
	        			// Update main model here
            			
            			var ussdTypeMap = that.model.get('ussdTypeMap');
            			itemModel.set('typeName', ussdTypeMap[itemModel.get('type')]);
            			that.model.setMenuItem(currentMenuId, currentButtonId, itemModel.attributes);
            			that.render();
	        		},
            		params: {
            			model: itemModel
            		}
            	});

            	return false;
            },
            
            deleteItem: function(ev) {
            	var that = this;
            	var currentItem = this.$(ev.currentTarget);
            	var currentButtonId = currentItem.attr('data-button-id');
            	var currentMenuId = currentItem.closest('ol').attr('data-menu-id');
            	
            	var itemJson = this.model.getMenuItem(currentMenuId, currentButtonId);
            	
            	/*
            	 * Sorry John, tried to use your delete, but couldn't get it working outside a table.
            	 */
            	//alert(JSON.stringify(itemJson, null, 2));
            	var description = i18ntxt.deleteDescriptionPrefix+itemJson.typeName+i18ntxt.deleteDescriptionSuffix;
            	if (!_.isUndefined(itemJson.text)) {
            		description+=i18ntxt.deleteDescriptionAdditionalPrefix+'<b>'+itemJson.text.texts.en+'</b>';
            	}

            	App.vent.trigger('application:dialog', {
					title: description,
					text: i18ntxt.deleteDescriptionText,
					name: "deleteDialog",
					actionBtnText: i18ntxt.deleteButtonText,
					actionBtnClass: 'delete',
					
					events: {
						"init": function() {
							var errorPanel = $(dialog).find(".deleteErrorPanel");
						},
						"click .cancelButton": function(event){
							
						},
						"click .close" : function(event) {
							
						},
						"click .actionButton": function(event) { 
							that.model.deleteButton(currentMenuId, currentButtonId);
							that.render();
							var dialog = $(this).closest('.modal');
			    			dialog.modal('hide');
						}
					}
				});
            	return false;
            }
        	
        });
        
        return UssdMenuConfigView;
});