define( ['jquery', 'App', 'marionette'],
    function($, App, Marionette) {
        //ItemView provides some default rendering logic
        var NavbarMenuView =  Marionette.ItemView.extend( {
        	tagName: "ul",
        	attributes: {
  			  class: "nav navbar-nav"
  		  	},
            //Template HTML string
            template: 'MainView#navbarmenu',
            initialize: function () {
            	this.model
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
            
            userLogout: function(ev) {
            	 $.ajax({
            		 		url: "logout",
            		 		success: function(data) {
            		 			window.location.reload();
            		 		},
            		 		error: function(data) {
            		 			App.log(data);
            		 		}
            	        });
            	 return false;
            },
            
            userProfile: function(ev) {
            	App.appRouter.navigate('#profile', {trigger: true, replace: true});
            	this.$('.dropdown-user').hide();
            	this.$('.user-menu').removeClass('user-menu-open');
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
            },
            
            /*triggers: {
            	"click @ui.role": 'edit:role'
            },*/
            
            onEditRole: function(ev) {
            	App.log("Editing role "+this.model.get('id'));
            	return false;
            },
            
            viewRole: function(ev) {
            	this.model.trigger('view:role', ev, this.model);
            	return false;
            },
            
            createRole: function(ev) {
            	alert('Create Role Called');
            	return false;
            }
        });
        return NavbarMenuView;
    });