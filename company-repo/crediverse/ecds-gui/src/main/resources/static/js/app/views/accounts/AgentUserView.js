define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/IsoLanguage', 'models/AgentUserModel', 'views/accounts/AgentUserDialogView', 'views/accounts/ApiUserDialogView', 'utils/CommonUtils', 'models/ValidationModel'],
    function($, _,  App, BackBone, Marionette, IsoLanguage, AgentUserModel, AgentUserDialogView, ApiUserDialogView, CommonUtils, ValidationModel) {
        //ItemView provides some default rendering logic
        var AgentUserView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	
        	attributes: {
        		class: "row"
        	},

  		  	template: "AgentUserView#agentuserdetails",
  		  	url: 'api/ausers/',
  		  	
  		  	// Set if you want to render BC yourself
  		  	renderBreadCrumbView: null,
  		  	
  		  	model: null,
  		  	breadcrumb: function() {
  		  		var txt = App.i18ntxt.agentAccounts;
  		  		return {
  		  			heading: txt.user.heading,
  		  			subheading: txt.user.subheading,
  		  			defaultHome: false,
  		  			breadcrumb: [{
  		  				text: txt.user.usersBC,
  		  				href: "#account/" + this.model.get("agentID"),
						iclass: "fa fa-users"
  		  			}, {
  		  				text: txt.user.userBC,
  		  				href: "#agentuser/" + this.id
  		  			}]
  		  		}
  		  	},
  		  	
  		 	preloadModels: function() {
	          	var self = this;
				this.rolesModel = new Backbone.Model();
	          	this.rolesModel.url = 'api/roles/agent/data';
				this.channelTypesModel = new Backbone.Model();
            	this.channelTypesModel.url = 'api/ausers/channel_types';

	          	return [
	    			this.rolesModel.fetch().then(function(){
	    				self.rolesModel.set( "roles", self.rolesModel.get("data") );
	        		}),
					this.channelTypesModel.fetch().then(function() {
						var channelTypesObj = self.channelTypesModel.attributes;
						var channelTypes = [];
						Object.keys(channelTypesObj)
							.forEach(function eachKey(key) { 
								var channelObj = {
									id: key,
									name: channelTypesObj[key]
								};
								channelTypes.push(channelObj);
							});
						self.channelTypesModel.attributes = {};
						self.channelTypesModel.set("channelTypes", channelTypes);
					})
	    		];
			},
  		  	
  		  	error: null,
  		  	
			tierList: null,
			
			notSet: "Not Set",
			
			ui: {
				pinResetButton: '.pinResetButton',
				editUserButton: '.editUserButton',
				deleteUserButton: '.deleteUserButton',
			},
			
			events: {
            	"click @ui.pinResetButton": 'pinReset',
            	"click @ui.editUserButton": 'editUser',
            	"click @ui.deleteUserButton": 'deleteUser'
			},
			
            initialize: function (options) {
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.id)) {
            			this.model = new AgentUserModel({id: options.id});
            			this.model.url = this.url+options.id;
            			this.model.autofetch = true;
            			this.id = options.id;
            		}
            	}
				this.model.fetch().then( x => {
					this.model.set('channelTypeName', this.channelTypesModel.attributes.channelTypes.filter( y => y.id === x.channelType )[0].name);
				});
            },
			onRender: function() {
			},
			
			editUser: function(ev) {
            	var self = this;
            	var userData = this.model.attributes;
            	var dialogView = null;
            	var title = "";
            	if(userData.authenticationMethod == "A"){
            		dialogView = ApiUserDialogView;
            		title = App.i18ntxt.agentAccounts.editApiUserTitle
            	} else {
            		dialogView = AgentUserDialogView;
            		title = App.i18ntxt.agentAccounts.editAgentUserTitle;
            	}
            	this.model.set('availRoles', this.rolesModel.attributes.roles);
				this.model.set('availableChannelTypes',this.channelTypesModel.attributes.channelTypes);
            	this.model.mode = 'update';
            	
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: dialogView,
            		title: CommonUtils.renderHtml(title, {user: userData.fullName, uniqueID: userData.id}),
            		hide: function() {
	        			self.render();
	        		},
            		params: {
            			model: this.model
            		}
            	});
            	return false;
            },
            
            deleteUser: function(ev) {
            	var self = this;
            	var data = this.model.attributes;
            	
            	if (data.state != "P") {
    	        	CommonUtils.delete({
    	        		itemType: App.i18ntxt.userman.user,
    	        		url: self.ausersUrl+'/'+data.id,
    	        		data: data,
    	        		context: {
    	        			what: App.i18ntxt.userman.user,
    	        			name: data.fullName,
    	        			description: data.mobileNumber
    	        		},	        		
    	        	}, {
    	        		success: function(model, response) {
    	        			window.history.back();
    	        		},
    	        		error: function(model, response) {
    	        			App.error(reponse);
    	        		}
    	        	});
            	}
            },

			pinReset: function(ev) {
            	var self = this;
            	var data = this.model.attributes;          	
            	// pinResetModalMessage
            	var headingModel = "";
            	var content = "";
            	if(data.authenticationMethod == "A"){
            		headingModel = {name: (data.firstName + " " + data.surname), email: data.email, authenticationMethod: data.authenticationMethod};
                	content = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#passwordResetModalMessage",  headingModel).html();
            	} else {
            		headingModel = {name: (data.firstName + " " + data.surname), msisdn: data.mobileNumber, authenticationMethod: data.authenticationMethod};
            		content = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#pinResetModalMessage",  headingModel).html();
            	}
            	if (true) {
            		App.vent.trigger('application:dialog', {
    	        		text: content,
    	        		name: "yesnoDialog",
    	        		events: {
    	        			"click .yesButton": 
    	        			function(event) {
    	        				var dialog = this;
    	        				$.ajax({
    	                    	    url: 'api/ausers/pinreset/'+data.id,
    	                    	    type: 'PUT',
    	                    	    success: function(result) {
    	                    	    	//var successMessage = CommonUtils.getRenderedTemplate( "AgentAccount#pinResetSuccess",  headingModel).html();
    	                    	    	var successMessage = ""
		                    	    	if(data.authenticationMethod == "A"){
		                    	    		successMessage = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#passwordResetSuccess",  headingModel).html();
		                    	    	} else {
		                    	    		successMessage = CommonUtils.getRenderedTemplate( "ChangePasswordDialog#pinResetSuccess",  headingModel).html();
		                    	    	}
    	                    	    	$(dialog).find(".msg-content").hide();
    	                    	    	$(dialog).find(".yesButton").hide();
    	                    	    	$(dialog).find(".noButton").text(App.i18ntxt.global.okBtn);
    	                    	    	$(dialog).find(".modal-text").html( successMessage );
    	                    	    },
    	                    	    error: function(error) {
    	                    	    	$(dialog).find(".msg-content").hide();
    	                    	    	$(dialog).find(".yesButton").hide();
    	                    	    	$(dialog).find(".noButton").text(App.i18ntxt.global.cancelBtn);
    	                    	    	var tmpModel = new ValidationModel({
    	                    	    		form: $(dialog).find("form")
    	                    	    	});
    	                    	    	$.proxy(tmpModel.defaultErrorHandler(error), tmpModel);
    	                    	    }
    	                    	});
    	        				
    	        			}
    	        		}
            		});
            	}
            }
      
    });
    return AgentUserView;
});
      
