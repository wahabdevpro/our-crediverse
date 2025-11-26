define( ['jquery', 'App', 'handlebars', 'underscore', 'marionette', 'utils/CommonUtils', 
         'views/menuconfig/UssdMenuItemView', 'views/menuconfig/UssdCommandView',  'views/menuconfig/UssdNameView',
         'models/UssdMenuModel', 'models/UssdMenuItemModel', 'i18n!common/violations', 'file-upload', 'jquery-sortable'],
    function($, App, Handlebars, _, Marionette, CommonUtils, 
    		UssdMenuItemView, UssdCommandView, UssdNameView,
    		UssdMenuModel, UssdMenuItemModel, violationMsgs) {

		var i18ntxt = App.i18ntxt.ussdmenu;
	
        var UssdMenuView = Marionette.ItemView.extend( {
        	template: 'UssdMenuConfiguration#ussdMenuConfig',
        	dialogTemplate: "UssdMenuConfiguration#ussdMenuConfigModal",
        	carousel: null,
        	currentItemView:null,
        	navigating: false,
        	ui: {
        	    showUpdateDialog: '.showUssdMenuConfigDialog',
        	    exportUssdMenu: '.exportUssdMenuButton',
        	    resetUssdMenuConfig: '.resetUssdMenuConfig',
        	    //previousMenuButton: '#menu-container .carousel-left-button',
        	    nextMenuButton: '.nextMenuButton',
        	    navigateToItem: '.menu-navigation-arrow',
        	    menuSaveLayout: '.menuSaveLayout',
        	    editItemAction: '.editItemAction',
        	    deleteItemAction: '.deleteItemAction',
        	    noAction:			'.menu-no-action',
        	    editCommandAction:	'.editCommandAction',
        	    enableToggleAction:	'.enableToggleAction',
        	    editNameAction:		'.editNameAction'
        	},
        	carouselHistory: [],
        	carouselCurrentOffset: null,
        	// DOM Events
            events: {
            	"click @ui.exportUssdMenu": 'exportUssdMenu',
            	"click @ui.resetUssdMenuConfig": 'resetUssdMenuConfig',
            	//"click @ui.previousMenuButton": 'previousMenu',
            	"click @ui.nextMenuButton": 'nextMenu',
            	"click @ui.navigateToItem": 'navigateToItem',
            	"click @ui.menuSaveLayout": 'menuSaveLayout',
            	"click @ui.editItemAction": 'menuEditItem',
            	"click @ui.deleteItemAction": 'deleteItem',
            	"click @ui.noAction": 'noAction',
            	"click @ui.editCommandAction": 'editCommand',
            	"click @ui.enableToggleAction": 'enableToggle',
            		"click @ui.editNameAction": 'editName'
            },
            
            noAction: function(ev) {
            	return false;
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
            	this.model.set('unsavedChanges', true);
            	App.unsavedChanges = true;
            	if (!_.isUndefined(this.options) && !_.isNull(this.options) && !_.isUndefined(this.options.save) && _.isFunction(this.options.save)) {
            		this.options.save(this.model);
            		if (App.unsavedChanges && !_.isUndefined(this.options.prompt) && this.options.prompt) {
            			this.$('.unsavedChangesDialog .alert-warning').removeClass('invisible');
            		}
    			}
            	return false;
            },

        	dialogTitle: App.i18ntxt.config.ussdMenuModalTitle,
        	
        	initialize:function (options) {
        		if (!_.isUndefined(options) && !_.isUndefined(options.model)) {
        			this.model = options.model;
        		}
        		
            	//this.model = new UssdMenuModel(options);
            	this.currentItemView = new UssdMenuItemView();
            },
            
            updateMenuFromView: function(ev) {
            	var visibleMenu = this.$('#menu-container .active');
        		var menuId = visibleMenu.attr('data-menu-id');
        		var menuOffset = visibleMenu.attr('data-menu-offset');
        		var buttonOrder = [];
        		visibleMenu.find('li').each(function(offset, item) {
					if ($(item).hasClass('placeholder')) return;
        			buttonOrder.push($(item).attr('data-button-id'));
        		});
        		this.model.reorderMenu(menuId, menuOffset, buttonOrder);
        		if (!_.isUndefined(this.options) && !_.isNull(this.options) && !_.isUndefined(this.options.save) && _.isFunction(this.options.save)) {
            		this.options.save(this.model);
    			}
            },
            
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
            
            getPrevious: function(current) {
            	var that = this;
            	var result;
            	var previous = that.carouselHistory.pop();
            	if (!_.isUndefined(previous)) {
            		if (!_.isUndefined(current) && parseInt(current) === parseInt(previous)) {
            			result = that.getPrevious();
            		}
            		else {
            			result = previous;
            		}
            	}
            		
            		
            	return result;
            },
            
            configureDragDrop: function() {
            	var that = this;
            	var adjustment;
            	var list = this.$("ol.simple_with_animation");
            	list.sortable({
            		  
            		  pullPlaceholder: false,
            		  // animation on drop
            		  onDrop: function  ($item, container, _super) {
            		    var $clonedItem = $('<li/>').css({height: 0});
            		    $item.before($clonedItem);
            		    $clonedItem.animate({'height': $item.height()});
					  	
            		    that.updateMenuFromView();

            		    $item.animate($clonedItem.position(), function  () {
            		      $clonedItem.detach();
            		      _super($item, container);
            		    });
            		  },
            		  
            		  //afterMove: function($placeholder, container, $closestItemOrContainer) {
            		  //	 that.updateMenuFromView();
            		  //},

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
            },
            
            onRender: function (ev) {
            	var that = this;
            	
            	var currentPath = Backbone.history.getHash();
            	App.log(currentUrl+' :: '+currentPath);
            	var $container = this.$('#menu-container');
            	//var $leftBtn = this.$('#menu-container .left').off();
            	var $leftBtn = $container.find('.carousel-left-button');
        		//$leftBtn.off();
        		$leftBtn.off();
        		$leftBtn.on('click', function(ev) {
        			
        			var previous = that.getPrevious(that.carouselCurrentOffset);
        			var menuContainer = that.$('#menu-container');
        			if (_.isUndefined(previous)) {
        				menuContainer.carousel(0);
        			}
        			else {
        				that.navigating = true;
                    	menuContainer.carousel(parseInt(previous));
        			}
        			
        			/*var previous = 0;
                	if (that.carouselHistory.length > 0)
                		previous = that.carouselHistory.pop();
            		var menuContainer = that.$('#menu-container');
                	menuContainer.carousel(parseInt(previous));*/
                	return false;
        		});
        		var $visible = null;
        		var currentUrl = Backbone.history.decodeFragment(Backbone.history.getFragment()).split('/');
            	if (currentUrl.length > 1) {
            		var currentId = currentUrl[currentUrl.length - 1];
            		var pos = currentId.indexOf('?');
            		if (pos >= 0) {
            			$visible = $container.find('.menu'+currentId.substring(0, pos));
            		}
            		else {
            			$visible = $container.find('.menu'+currentId);
            		}
            	}

        		if (_.isNull($visible)) {
        			$visible = $container.find('.menu1');
        		}
        		
        		$visible.addClass('active');
        		
        		if (_.isNull(this.carousel)) {
        			App.log("new carousel");
        		}
        		else {
        			App.log("existing carousel");
        		}
            	$container.carousel({
            		interval: false,
            		wrap: false
            	})
            	.on('slide.bs.carousel', function(newItem) {
            		that.updateMenuFromView(ev);
            	})
            	.on('slid.bs.carousel', function(direction, newItem) {
            		var queryString = '';
            		var visibleMenu = that.$('#menu-container .active');
            		var menuId = visibleMenu.attr('data-menu-id');
            		var menuOffset = visibleMenu.attr('data-menu-offset');
            		that.options.id = menuId;
            		var currentUrl = Backbone.history.decodeFragment(Backbone.history.getFragment()).split('?');
            		if (currentUrl.length === 2) {
            			queryString = currentUrl[1];
            		}
            		var baseUrl = (currentUrl[0].split('/'))[0];

            		App.appRouter.navigate(baseUrl+"/"+menuId+'?'+queryString, {trigger: false, replace: true});
            		$.proxy(that.configureCarouselButtons(), that);
            		that.carouselCurrentOffset = menuOffset;
            		if (!that.navigating) {
            			that.carouselHistory.push(menuOffset);
            		}
            		else {
            			that.navigating = false;
            		}
            	});
            	that.configureCarouselButtons();
            	
            	that.configureDragDrop();
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
            		params: {
            			model: itemModel,
            			variables: {
            				text: that.model.getEditorVariables()
            			},
            			save: function(model) {
            				var ussdTypeMap = that.model.get('ussdTypeMap');
                			itemModel.set('typeName', ussdTypeMap[itemModel.get('type')]);
                			that.model.setMenuItem(currentMenuId, currentButtonId, itemModel.attributes);
                			if (!_.isUndefined(that.options) && !_.isNull(that.options) && !_.isUndefined(that.options.save) && _.isFunction(that.options.save)) {
                				that.options.save(that.model);
                			}
                			that.render();
            			}
            		}
            	});

            	return false;
            },
            
            editCommand: function(ev) {
            	var that = this;
            	var itemModel = new UssdMenuItemModel({
            		ussdMenuCommand: that.model.get('ussdMenuCommand')
            	});
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
					class: 'modal-lg',
            		view: UssdCommandView,
            		//title:CommonUtils.renderHtml(this.i18ntxt.ruleEditDialogTitle, {name: tableData.name}),
            		title: i18ntxt.editCommandTitle,
            		params: {
            			model: itemModel,
            			save: function(model) {
            				that.model.set('ussdMenuCommand', itemModel.get('ussdMenuCommand'));
                			if (!_.isUndefined(that.options) && !_.isNull(that.options) && !_.isUndefined(that.options.save) && _.isFunction(that.options.save)) {
                				that.options.save(that.model);
                			}
                			that.render();
            			}
            		}
            	});
            	
            	return false;
            },
            
            editName: function(ev) {
            	var that = this;
            	var currentItem = this.$(ev.currentTarget);
            	var currentMenuOffset = currentItem.attr('data-menu-offset');
            	var currentMenuId = currentItem.attr('data-menu-id');
            	
            	var itemModel = new UssdMenuItemModel({
            		name: that.model.getMenuProperty(currentMenuOffset, 'name')
            	});
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
					class: 'modal-lg',
            		view: UssdNameView,
            		//title:CommonUtils.renderHtml(this.i18ntxt.ruleEditDialogTitle, {name: tableData.name}),
            		title: i18ntxt.editMenuNameTitle,
            		params: {
            			model: itemModel,
            			save: function(model) {
            				that.model.setMenuProperty(currentMenuOffset, 'name', itemModel.get('name'));
                			that.model.updateMenuNames(currentMenuId, itemModel.get('name'));
                			if (!_.isUndefined(that.options) && !_.isNull(that.options) && !_.isUndefined(that.options.save) && _.isFunction(that.options.save)) {
                				that.options.save(that.model);
                			}
                			that.render();
            			}
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
							if (!_.isUndefined(that.options) && !_.isNull(that.options) && !_.isUndefined(that.options.save) && _.isFunction(that.options.save)) {
	            				that.options.save(that.model);
	            			}
							that.render();
							var dialog = $(this).closest('.modal');
			    			dialog.modal('hide');
						}
					}
				});
            	return false;
            }
        	
        });
        
        return UssdMenuView;
});
