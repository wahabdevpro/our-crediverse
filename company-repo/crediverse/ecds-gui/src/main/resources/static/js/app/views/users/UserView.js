define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/IsoLanguage', 'models/UserModel', 'views/users/UserOperationsView', 'utils/CommonUtils',
		'views/PasswordChangeDialogView', 'models/PasswordChangeModel'],
    function($, _,  App, BackBone, Marionette, IsoLanguage, UserModel, UserOperationsView, CommonUtils,
    		PasswordChangeDialogView, PasswordChangeModel) {

        var UserView =  UserOperationsView.extend( {
        	tagName: 'div',
        	
        	attributes: {
        		class: "row"
        	},
        	
  		  	template: "UserView#userdetails",
  		  	url: 'api/wusers/',
  		  	
  		  	model: null,
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.userview;
  		  		return {
  		  			heading: txt.heading,
  		  			subheading: txt.subheading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.sectionBC
  		  			}, {
  		  				text: txt.usersBC,
  		  				href: "#users"
  		  			}, {
  		  				text: txt.userBC,
  		  				href: "#user/" + this.id
  		  			}]
  		  		}
  		  	},
			ui: {
		          edit: '.editUserButton',
		          delete: '.deleteUserButton',
		          performPasswordChange: '.changePasswordButton',
		          performPasswordReset: '.resetPasswordButton',
		    },
		
		    events: {
		      	"click @ui.edit": 'editUser',
		      	"click @ui.delete": 'deleteUser',
		      	"click @ui.performPasswordChange" : 'performPasswordChange',
            	"click @ui.performPasswordReset" : 'performPasswordReset'
		    },
  		  	error: null,
  		  	
			tierList: null,
			
			notSet: "Not Set",
            initialize: function (options) {
            	this.model = new UserModel(options);
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.id)) {
            			this.model.url+=options.id;
            		}
            	}
            },
            
            onBeforeRender: function(){
            	var lang = this.model.get("language");
            	if (!_.isUndefined(lang) && !_.isUndefined(IsoLanguage)) {
            		this.model.set("languageName", IsoLanguage[lang].name);
            	}
            },
            
            deleteUser: function(ev) {
            	var self = this;
            	var data = this.model.attributes;
            	
            	if (data.state != "P") {
    	        	CommonUtils.delete({
    	        		itemType: App.i18ntxt.userman.user,
    	        		url: self.url+'/'+data.id,
    	        		data: data,
    	        		context: {
    	        			what: App.i18ntxt.userman.user,
    	        			name: data.fullName,
    	        			description: data.domainAccountName
    	        		},	        		
    	        	}, {
    	        		success: function(model, response) {
    	        			App.appRouter.navigate('#users', {trigger: true, replace: true});
    	        		},
    	        		error: function(model, response) {
    	        			App.error(reponse);
    	        		}
    	        	});
            	}
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
            			//CommonUtils.showViewDialog(model, PasswordChangeDialogView);
            			App.vent.trigger('application:dialog', {
                    		name: "viewDialog",
                    		title:  CommonUtils.renderHtml(App.i18ntxt.changePasswordDialog.changePassword, {minPinLength: model.attributes.minPinLength}),
                    		hide: function() {
        	        		},
                    		view: PasswordChangeDialogView,
                    		params: {
                    			model: model,
                    			url: '/api/wusers/change_password'
                    		}
                    	});
            		}, this)
        		}); 
            	
            },
            performPasswordReset: function(ev) {
            	App.log("performPasswordReset");
            	var self = this;
            	var data = this.model.attributes;
            	this.model.attributes.entityId = data.id;
            	var headingModel = {name: (data.firstName + " " + data.surname), email: data.email};
            	var content = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#passwordResetModalMessage",  headingModel).html();
            	App.vent.trigger('application:dialog', {
	        		text: content,
	        		name: "yesnoDialog",
	        		events: {
	        			"click .yesButton": 
	        			function(event) {
	        				var dialog = this;
	        				self.doPasswordReset(data, dialog);
	        			}
	        		}
        		});
            },
            
            doPasswordReset: function(data, dialog)
            {
            	var self = this;
            	var model = new PasswordChangeModel();
            	model.attributes.entityId = data.id;
            	var headingModel = {name: (data.firstName + " " + data.surname), email: data.email};
            	
            	App.log("doPasswordReset");
            	var payload = {
                		entityId : data.id
                	}; 
            	var fullurl = CommonUtils.getContextPath() + "api/wusers/reset_password";
            	
            	$.ajax({
			    	type : "POST",
			        url: fullurl,
			        headers : {
			            'Accept': 'application/json',
			            'Content-Type': 'application/json'
			        },
			        data : JSON.stringify(payload, null, 2),
			        datatype : "json",
			        timeout : self.TIMEOUT
			    }).done(function(response) {
			    	$(dialog).find(".msg-content").hide();
        	    	$(dialog).find(".yesButton").hide();
			    	if(!_.isUndefined(data.email)){
						var successMessage = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#passwordResetSuccess",  headingModel).html();
						$(dialog).find(".noButton").text(App.i18ntxt.global.okBtn);	
            	    	$(dialog).find(".modal-text").html( successMessage );
			    	} else {
			    		//User does not have email
			    		var noEmailMessage = App.translate("changePasswordDialog.missingEmail");
	        	    	$(dialog).find(".noButton").text(App.i18ntxt.global.cancelBtn);
	        	    	$(dialog).find(".modal-text").html( noEmailMessage );     
			    	}
			    }).fail(function(response) {
			    	console.log("Password Reset Failed");
			    	var errorMessage = App.translate("enums.returncode." + response.returnCode);
			    	$(dialog).find(".msg-content").hide();
        	    	$(dialog).find(".yesButton").hide();
        	    	$(dialog).find(".noButton").text(App.i18ntxt.global.cancelBtn);
        	    	$(dialog).find(".modal-text").html( errorMessage );        	    	
			    });
            	
            }
    });
    return UserView;
});
      