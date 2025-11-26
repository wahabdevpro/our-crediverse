define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/IsoLanguage', 
		'views/users/ProfileDialogView', 'views/users/PermanentUserDialogView',
			'models/UserModel', 'models/UserValidationModel', 'utils/CommonUtils',
			'views/PasswordChangeDialogView', 'models/PasswordChangeModel'],
    function($, _,  App, BackBone, Marionette, IsoLanguage, 
    		ProfileDialogView, PermanentUserDialogView,
    		UserModel, UserValidationModel, CommonUtils,
    		PasswordChangeDialogView, PasswordChangeModel) {

	var txt = App.i18ntxt.userview;
	var i18n = CommonUtils.i18nLookup('profile');
	
    var ProfileView =  Marionette.ItemView.extend( {
    	tagName: 'div',
    	
    	attributes: {
    		class: "row"
    	},
    	
	  	template: "UserView#userProfile",
	  	
	  	model: null,
	  	departmentsModel: null,
	  	urlDepartments: 'api/departments',
	  	breadcrumb: function() {
	  		return {
	  			heading: i18n.translate('heading'),
	  			subheading: i18n.translate('subheading'),
	  			defaultHome: false,
	  			breadcrumb: [{
	  				text: i18n.translate('profileBC'),
	  				href: "#profile"
	  			}]
	  		}
	  	},
	  	
	  	preloadModels: function() {
        	var self = this;
        	
    		this.departmentsModel = new Backbone.Model();
    		this.departmentsModel.url = this.urlDepartments;
    		return [
        		this.departmentsModel.fetch().then(function(){
        			self.departmentsModel.set("departments", self.addDefaultItem(self.departmentsModel));
        		})
    		];
        },
	  	
		ui: {
	          edit: '.editProfileButton',
	          performPasswordChange: '.changePasswordButton',
	          performPasswordReset: '.resetPasswordButton',
	    },
	
	    events: {
	      	"click @ui.edit": 'editProfile',
	      	"click @ui.performPasswordChange" : 'performPasswordChange',
        	"click @ui.performPasswordReset" : 'performPasswordReset'
	    },
	  	error: null,
	  	
		tierList: null,
		
		notSet: "Not Set",
        initialize: function (options) {
        	this.model = new UserModel();
        	this.model.url+='profile'
        },
        
        onBeforeRender: function(){
        	var lang = this.model.get("language");
        	if (!_.isUndefined(lang) && !_.isUndefined(IsoLanguage)) {
        		this.model.set("languageName", IsoLanguage[lang].name);
        	}
        },
        // Add "0" item to roles and departments list
        addDefaultItem: function (model) {
			var data = model.get("data");
			data.unshift({id:0, name:App.i18ntxt.notSet, description:App.i18ntxt.notSet});
			return data;
        },
        
        editProfile: function(ev) {
        	var self = this;
        	var userData = this.model.attributes;
        	
        	// url ?!?
        	var model = new UserValidationModel({
        		availDepartments: this.departmentsModel.attributes.departments
        	});
        	
        	model.set(userData);
        	model.url+='/profile';
        	
        	App.vent.trigger('application:dialog', {
        		name: "viewDialog",
        		view: (userData.state == 'P')? PermanentUserDialogView : ProfileDialogView,
        		title: CommonUtils.renderHtml(App.i18ntxt.userman.editUserTitle, {user: userData.domainAccountName, uniqueID: userData.id}),
        		hide: function() {
        			if (_.isUndefined(self.dataTable)) {
        				self.model.set(model.attributes);
        				self.render();
        			}
        			else {
        				self
            			.dataTable.ajax.reload().draw();
        			}
        		},
        		params: {
        			model: model
        		}
        	});
        	return false;
        },
        performPasswordChange: function(ev) {
        	var self = this;
        	var data = this.model.attributes;
        	var model = new PasswordChangeModel();
        	model.attributes.entityId = data.id;
        	model.attributes.entityType = 'WEBUSER';
        	App.log("performPasswordChange");
        	model.fetch({
        		url: "api/wusers/passwordrules",
        		success: _.bind(function() {
        			model.updateRules();
        			App.vent.trigger('application:dialog', {
                		name: "viewDialog",
                		view: PasswordChangeDialogView,
                		title: CommonUtils.renderHtml(App.i18ntxt.changePasswordDialog.changePassword, {minPinLength: model.attributes.minPinLength}),
                		hide: function() {
                			if (_.isUndefined(self.dataTable)) {
                				self.model.set(model.attributes);
                				self.render();
                			}
                			else {
                				self
                    			.dataTable.ajax.reload().draw();
                			}
                		},
                		params: {
                			model: model,
                			url: '/api/wusers/change_password'
                		}
                	});
        		}, this)
    		}); 
        	
        }
    });
    return ProfileView;
});
      