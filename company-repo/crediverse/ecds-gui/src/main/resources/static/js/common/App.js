define(['jquery', 'underscore', 'backbone', 'marionette', 'handlebars', 'i18n', 'i18n!nls/translations', 'common/locale', 'i18n!common/violations', 'backbone.syphon'],
    function ($, _, Backbone, Marionette, Handlebars, I18N, translations, commonTranslations, violationMsgs) {
        var App = new Marionette.Application();
        var config = {
        		
        };
        
        App.unsavedChanges = false;

        App.contextPath = translations.context;
        //Organize Application into regions corresponding to DOM elements
        //Regions can contain views, Layouts, or subregions nested as necessary
        // FIXME change this to use layouts
        App.addRegions({
        	breadcrumb: "#breadcrumb",	// Main Header bar
        	mainSideBar:".main-sidebar",
            mainContent:"#mainContent",
            navbarMenu: ".navbar-custom-menu",
            dialog:"#templates",
            //graph: "#content"
        });

        // Translations are created from command and translations file
        App.i18ntxt = $.extend(true, commonTranslations, translations);
        App.violations = violationMsgs;
        App.contextConfig = null;
        App.userPerms = null,
        
        App.assignPerms = function(uperms) {
        	App.userPerms = {};
        	for(var i = 0; i < uperms.length; i++) {
        		var perm = uperms[i].toLowerCase();
        		App.userPerms[perm] = true;
        	}
        },
        
        App.hasPermission = function(group, perm) {
        	if (App.userPerms != null) {
        		var key = "";
        		if (_.isUndefined(perm)) {
        			key = group.toLowerCase();
        		} else {
        			key = group.toLowerCase() + "_" + perm.toLowerCase();
        		}
        		return (_.isUndefined(App.userPerms[key]))? false : App.userPerms[key];
        	}
        	return false;
        };
        
        App.hasAllPermissions = function(perms) {
    		hasPerms = true;
        	for(var i=0; i<perms.length; i++) {
        		hasPerms = hasPerms && App.hasPermission(perms[i]);
        	}
        	return hasPerms
        }

        
        //e.g. App.translate("config.notification")  := App.i18ntxt.config.notification;  
        App.translate = function(str, defaultValue) {
        	// First look in translate
        	var result = str.split('.').reduce(function(obj, i) {
        		return obj[i]
        	}, App.i18ntxt);
        	
        	// Next check in violations
        	try {
            	if ((_.isUndefined(result)) && (str.indexOf('.') < 0))  {
            		result = App.violations[str];
            	}
        	} catch(err) {
        	}
        	
        	if (_.isUndefined(result) && !_.isUndefined(defaultValue)) {
        		result = defaultValue;
        	}
        	
        	return result;
        };
        
        function isMobile() {
            var ua = (navigator.userAgent || navigator.vendor || window.opera, window, window.document);
            return (/iPhone|iPod|iPad|Android|BlackBerry|Opera Mini|IEMobile/).test(ua);
        };

        App.mobile = isMobile();
        
        App.log = function(data) {
        	if (App.jsDebug && window.console && window.console.log) {
        		// console is available
        		console.log(data);
        	}
        };
        
        App.error = function(error) {
        	if (window.console && window.console.error) {
        		console.dir(error);
//        		if (error.stack)  console.error(error.stack);
        	}
        };
        
        App.decorateAjax = function() {
        	/*
        	 * Deal with csrf protection headers generically
        	 */
    	    var token = $("meta[name='_csrf']").attr("content");
    		var header = $("meta[name='_csrf_header']").attr("content");
    		
    		/*
    		 * Global ajax handlers, see http://api.jquery.com/category/ajax/global-ajax-event-handlers/
    		 */
    		$(document).ajaxSend(function(e,xhr,options) {
    			if (!_.isUndefined(xhr) &&
    				!_.isUndefined(xhr.setRequestHeader)) {
    				
    					xhr.setRequestHeader(header, token);
    			}
    		})
    		.ajaxSuccess(function( event, xhr, settings ) {
    			// App.log("success" + JSON.stringify(xhr, null, 2));
    		})
    		.ajaxError(function( event, xhr, settings ) {
    			App.log("error" + JSON.stringify(xhr, null, 2));
    			if(!_.isUndefined(xhr)) {
    				if ((xhr.status === 422)||(xhr.status === 401)) {
    					window.location = 'login';
    				}
    				else if (!_.isUndefined(xhr.responseJSON)) {
        				if (!_.isUndefined(xhr.responseJSON.url) && !_.isUndefined(xhr.responseJSON.login)) {
            				if (xhr.responseJSON.login === 'required') {
            					window.location = xhr.responseJSON.url;
            				}
            			}
        				if (!_.isUndefined(xhr.responseJSON.status)) {
        					if (xhr.responseJSON.status === "SERVICE_UNAVAILABLE" && xhr.responseJSON.message.indexOf('UNAUTHORIZED') >= 0) {
        						window.location = 'login';
        					}
        				}
        			}
    			}
    			 
    		});
        };
        
        App.listenTo(App, 'start', function(options) {
        	
        	jQuery.validator.setDefaults({
        		  ignoreTitle: true
    		});
			
        	$('body').on('change','.select2-hidden-accessible',function(evt){
				$(this).valid();
			});

        	Backbone.Syphon.InputReaders.register('checkbox', function (el$) {
	       		 var value = el$.val();
	       		 if (_.isEmpty(value)) {
	       			 value = (el$.prop('indeterminate')) ? null : el$.prop('checked');
	       		 }
	       		 return value;
	       	 });
        	
        	Backbone.Syphon.InputReaders.register('text', function (el$) {
	       		 var value = el$.val();
	       		 if (el$.hasClass('autonum')) {
	       			 try {
	       				value = el$.autoNumeric('get');
	       			 }
	       			 catch(err) {
	       				el$.autoNumeric('init');
	       				value = el$.autoNumeric('get'); 
	       			 }
	       		 }
	       		 return value;
	       	 });
	
	   		 Backbone.Syphon.KeyAssignmentValidators.register("checkbox", function ($el, key, value) {
	   			 var state = false;
	   			 if (key.indexOf("[]") > 0) {
	   				 state = $el.prop("checked");
	   			 }
	   			 else {
	   				 state = true;
	   			 }
	   		    return state;
	   		 });
    		
    		$('body').on('click', function(event) {
    			return true;
    		});
    		$('body').on('click', '.routerlink', function(event) {
    			var link = $(event.currentTarget);
    			if (link.length !== 0 && link.hasClass('routerlink')) {
    				var url = null;
    				url = link.attr('href');
    				if (url !== null) {
    					var pos = url.indexOf('#');
    					if (pos >= 0) {
    						var destination = url.substring(pos+1);
    						App.log('navigate => '+destination);
    						App.vent.trigger('application:route', destination);
    						return false;
    					}
    					else {
    						App.log('Cannot navigate => '+url);
    					}
    				}
    			}
    			return true;
    		});
        });
        
        App.listenTo(App, 'before:start', function(options) {
        	Backbone.history.start();
        });
        
        App.vent.on('dialog:success', function(options) {
        });
        
        // Retrieve the localization settings
        App.loadMessages = function(messageRef, callback) {
        	
			require(["i18n!nls/" + messageRef, "handlebars"], function(messages, Handlebars) {
				Handlebars.registerHelper('myi18n', function(str) {
			    	var result = messages[str];
			    	return (_.isUndefined(result))? str : result;
			    });
			    
				if (! _.isUndefined(callback))
					callback(messages);
			});
        }
	
		//	Adjust the size of the text to display in the dataTable
        App.refactorString = function(data) {
        	var size = data.length;
			var i;
			var flag = true;
			var counter = 0;
			for (i = 0; i < size; i++) {
				if(data.charAt(i) == " ") {
					counter = 0;
				}
				else {
					counter++;
				}
				if(counter == 10) {
					flag = false;
					break;
				}
			}
			if(flag === false) {
				data = data.substring(0, 10).concat("...");
			}
			return data;
        }

        return App;
});
