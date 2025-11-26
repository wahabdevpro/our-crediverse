define( ['jquery', 'App', 'underscore', 'marionette', 'utils/CommonUtils', 'file-upload', 'jquery-sortable'],
    function($, App, _, Marionette, CommonUtils) {
	
		var i18ntxt = App.i18ntxt.ussdmenu;

        var UssdMenuToolbarView = Marionette.ItemView.extend( {
        	template: 'UssdMenuConfiguration#ussdMenuToolbar',
        	menuContainer: null,
        	
        	tagName: 'div',
        	attributes: {
        		class: "pull-right"
        	},
        	
        	ui: {
        	    exportUssdMenu: '.exportUssdMenuButton',
        	    importUssdMenu: '.importUssdMenuButton',
        	    resetUssdMenuConfig: '.resetUssdMenuConfig',
        	    menuSaveLayout: '.menuSaveLayout'
        	},
        	carouselHistory: [],
        	renderer: null,
        	// DOM Events
            events: {
            	"click @ui.exportUssdMenu": 'exportUssdMenu',
            	"click @ui.importUssdMenu": 'importUssdMenu',
            	"click @ui.resetUssdMenuConfig": 'resetUssdMenuConfig',
            	"click @ui.menuSaveLayout": 'menuSaveLayout'
            },
            
            initialize:function (options) {
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.model)) {
            			this.model = options.model;
            		}
            		if (!_.isUndefined(options.menuContainer)) {
            			this.menuContainer = options.menuContainer;
            		}
            	}
            },
            
            registerRenderer: function(renderer) {
            	this.renderer = renderer;
            },
            
            exportUssdMenu: function(ev) {
    			CommonUtils.exportAsJson(this.model.url);
            },
            
            importUssdMenu: function(ev) {
            	var that = this;
            	var importb = this.$(".file-import");
            	var progress = 0;

            	//importb.off();
            	importb.fileupload({
            	    url: '/api/config/ussdmenu/import',
            	    sequentialUploads: true,
            	    dataType: 'json'
            	})
            	.on('fileuploadstart', function (e, data) {
            		progress = 0;
            		
            		App.vent.trigger('application:dialog', {
                		name: "statusDialog",
    					text: i18ntxt.importMenuStatus,
    					init: function(obj) {
    						statusDialog = obj;
    					}
                	});
                })
            	.on('fileuploadprogressall', function (e, data) {
            		// progressbar support, currently unused
                    progress = parseInt(data.loaded / data.total * 100, 10);
                    $('#uploadProgress .progress-bar').css(
                        'width',
                        progress + '%'
                    );
                })
            	.on('fileuploadsubmit', function (e, data) {
            		// send to server
            		//alert('fileuploadsubmit '+JSON.stringify(data, null, 2))
                })
            	.on('fileuploaddone', function (e, data) {
            		//alert('fileuploaddone '+JSON.stringify(data, null, 2))
            		that.model.fetch({
            			success: function(ev) {
            				//that.render();
            				App.appRouter.navigate("config-ussdmenu/1", {trigger:true});
            			},
            			
            			error: function(ev) {
            				
            			}
            		});
                })
            	.on('fileuploadfail', function (e, data) {
            		//alert('fileuploadfail '+JSON.stringify(data, null, 2))
                })
            	.on('fileuploadadd', function (e, data) {
            		//alert('fileuploadadd '+JSON.stringify(data, null, 2))
            		jqXhr = data;
            		$('body').focus();// Avoid displaying large cursor in IE on Windows
            	})
            	.on('fileuploadalways', function(e, data) {
            		statusDialog.hide();
            	});
            },
            
            resetUssdMenuConfig: function(ev) {
            	var that = this;
            	var statusDialog = null;
            	
        		App.vent.trigger('application:dialog', {
	        		text: i18ntxt.resetMenuStatus,
	        		name: "yesnoDialog",
	        		init: function(obj) {
						statusDialog = obj;
					},
	        		events: {
	        			"click .yesButton": 
	        			function(event) {
	        				var dialog = that;
	        				
	        				var jqxhr = $.post( that.model.url+'/reset', function(data) {
	                			App.appRouter.navigate("config-ussdmenu/1", {trigger:true});
	                		})
	                		.fail(function(data) {
	                			App.log(data);
	                		})
	                		.complete(function(data) {
	                			statusDialog.hide();
	                			$(dialog).find(".unsaveChangesDialog").hide();
	                			that.model.set('unsavedChanges', false);
	                        	App.unsavedChanges = false;
	                        	if (!_.isNull(that.renderer)) {
	                				that.renderer();
	                			}
	                		});
	        			}
	        		}
        		});
            },
            
            updateMenuFromView: function(ev) {
            	var visibleMenu = this.menuContainer.find('.active');
            	//var visibleMenu = $('#menu-container .active');
        		var menuId = visibleMenu.attr('data-menu-id');
        		var menuOffset = visibleMenu.attr('data-menu-offset');
        		var buttonOrder = [];
        		visibleMenu.find('li').each(function(offset, item) {
        			buttonOrder.push($(item).attr('data-button-id'));
        		});
        		this.model.reorderMenu(menuId, menuOffset, buttonOrder);
            },
            
            menuSaveLayout: function(ev) {
            	var statusDialog = null;
            	var that = this;
            	App.vent.trigger('application:dialog', {
            		name: "statusDialog",
					text: i18ntxt.saveMenuStatus,
					init: function(obj) {
						statusDialog = obj;
					}
            	});
            	this.updateMenuFromView(ev);
            	this.model.save(null, {
            		success: function(response) {
            			//alert('success');
            			statusDialog.hide();
            			if (!_.isNull(that.renderer)) {
            				that.renderer();
            			}
            		},
            		error: function(data, response) {
            			//alert('error');
            			statusDialog.hide();
        				var errorPanel = $(".error-panel");
        				if (errorPanel.length >0) {
        					errorPanel.removeClass("hide");
        					
        					// Add Message
            				if (!_.isUndefined(response.responseJSON.violations) && response.responseJSON.violations.length > 0) {
            					var ref = response.responseJSON.violations[0].validations[0];
            					var msg = ref;
            					if (!_.isUndefined(violationMsgs[ref])) {
            						msg = Handlebars.compile(violationMsgs[ref])({item: "USSD Menu"});
            					}
            					var compiled = Handlebars.compile(violationMsgs["menuOperationFailMessage"]);
            					$(".form-error-heading").text(compiled({
            						msg:msg,
            						correlationID:response.responseJSON.violations[0].correlationID
            					}));
            				}
        				}				
            		}
            	});
            },
            
        });
        
        return UssdMenuToolbarView;
});