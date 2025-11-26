define( ['jquery', 'App', 'marionette'],
    function($, App, Marionette) {
        //ItemView provides some default rendering logic
        var NavbarMenuView =  Marionette.ItemView.extend( {
        	tagName: "ul",
        	attributes: {
  			  class: "nav navbar-nav"
  		  	},
            //Template HTML string
            template: 'Dashboard#navbarmenu',
            initialize: function () {
//            	this.model
            },
            onRender: function () {
            	
            },
            
            ui: {
                role: '',
                create: '.roleCreateButton',
                userMenu: '.user-menu',
                userLogout: '.userLogout',
                userProfile: '.userProfile'
            },

            // View Event Handlers
            events: {
            	"click @ui.role": 'viewRole',
            	"click @ui.create": 'createRole',
            	"click @ui.userMenu": 'toggleMenu',
            	"click @ui.userLogout": 'userLogout',
            	"click @ui.userProfile": 'userProfile'
            },
            
            userProfile: function(ev) {
            	App.appRouter.navigate('#profile', {trigger: true, replace: true});
            	this.$('.dropdown-user').hide();
            	this.$('.user-menu').removeClass('user-menu-open');
            	return false;
            },
            
            userLogout: function(ev) {
            	var tmp = '';
    			for (var i=0; i<window.location.pathname.length; i++) {
    				if (window.location.pathname[i] !== '/') {
    					tmp = window.location.pathname.substring(i, window.location.pathname.indexOf("/",i));
    					break;
    				}
    			}
            	 $.ajax({
            		 		url: window.location.origin+tmp+"/logout",
            		 		success: function(data) {
            		 			window.location = window.location.origin+tmp+"/login";
            		 		},
            		 		error: function(data) {
            		 			App.log(data);
            		 			window.location = window.location.origin+tmp+"/login";
            		 		}
            	        });
            	 return false;
            },
            
            toggleMenu: function(ev) {
            	$state = this.$('li.user-menu-open');
            	if ($state.length > 0) {
            		this.$('.user-menu').removeClass('user-menu-open');
            		this.$('.dropdown-user').hide();
            	}
            	else {
            		this.$('.dropdown-user').show();
            		this.$('.user-menu').addClass('user-menu-open');
            	}
            	return false;
            }
        });
        return NavbarMenuView;
    });