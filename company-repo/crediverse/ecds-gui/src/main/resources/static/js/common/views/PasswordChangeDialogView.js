define( ['jquery', 'underscore', 'App', 'backbone', 'marionette', 'utils/CommonUtils'],
    function($, _, App, BackBone, Marionette, CommonUtils) {
        var PasswordChangeDialogView =  Marionette.ItemView.extend( {
        	tagName: 'div',
        	template: "ChangePasswordDialog#changePassword",

        	ui: {
        		view: "",
        		saveButton: '.saveButton',
        		currentPassword: '#currentPassword',
                newPassword: '#newPassword',
                repeatPassword: '#repeatPassword'
        	},

        	events: {
        		"input @ui.currentPassword": 'checkPasswordValid',
            	"input @ui.repeatPassword": 'checkPasswordValid',
            	"input @ui.newPassword": 'checkPasswordValid',
        		"click @ui.saveButton": "performPasswordChange"
        	},

        	attributes: {
        		class: "modal-content"
        	},

        	isTransferring: false,

        	initialize: function (options) {
        	},

        	onRender: function () {
        		var minPasswordLengthText = App.translate("changePasswordDialog.passwordRules5").replace("{{minPinLength}}", this.model.attributes.minPasswordLength - 1);
        		var minPasswordLengthLabel = this.$('#minPinLengthLabel');
        		//minPinLengthLabel.empty();
        		minPasswordLengthLabel.html(minPasswordLengthText);
        		this.updatePasswordRules("");
			},

        	checkPasswordValid: function(ev) {
        		var self = this;
        		try {
        			var currentPassword = $("#currentPassword").val();
            		var newPassword = $("#newPassword").val();
            		var repeatPassword = $("#repeatPassword").val();
            		self.updatePasswordRules(newPassword);
            		var isDisabled = $(".saveButton").is(':disabled');
            		if ((currentPassword.length > 0 && newPassword.length > 0 && repeatPassword.length > 0) && isDisabled) {
            			$(".saveButton").prop('disabled', false);
            		} else if (newPassword.length == 0 || repeatPassword.length == 0) {
            			$(".saveButton").prop('disabled', true);
            		}
        		} catch(err) {
        			App.error(err);
        		}
        		return true;
        	},

            updateSPasswordner: function(showSPasswordner) {
            	this.$(".newPassword").prop("disabled", showSPasswordner);
            	this.$(".repeatPassword").prop("disabled", showSPasswordner);

            	if (showSPasswordner) {
                	this.$(".enterDetails").addClass("hide");
                	this.$(".performingPasswordChange").removeClass("hide");
            	} else {
                	this.$(".performingPasswordChange").addClass("hide");
                	this.$(".enterDetails").removeClass("hide");
            	}
            },

            showOkDialog: function(body) {
				var title = CommonUtils.renderHtml( App.translate("changePasswordDialog.changePassword") );
				var body = CommonUtils.renderHtml( App.translate(body) );
				CommonUtils.showOkDialog({
					title: title,
					text: body,
					callback: function() {}
				});
            },

            passwordChangedCallback : function(response, self) {//
            	self.isTransferring = false;
            	self.updateSPasswordner(false);
				if(response.returnCode == "SUCCESS"){
					$('#templates .modal').modal('hide');
					// Show success message
					self.showOkDialog("changePasswordDialog.passwordChangeSuccess");
					App.vent.trigger('operations:success', {});
					
				} else {
					//self.showOkDialog("enums.returncode." + response.returnCode);
					self.$("#password-feedback").html(App.translate("enums.returncode." + response.returnCode));
				}
            },
            failedCallback : function(data, self)
            {
            	self.isTransferring = false;
            	self.updateSPasswordner(false);
            },

            doCall: function(urlloc, sendData, successCallback, failedCallback) {
            	var self = this;
            	var fullurl = urlloc;
            	$.ajax({
			    	type : "POST",
			        url: fullurl,
			        headers : {
			            'Accept': 'application/json',
			            'Content-Type': 'application/json'
			        },
			        data : JSON.stringify(sendData, null, 2),
			        datatype : "json",
			        timeout : self.TIMEOUT
			    }).done(function(response) {
			    	successCallback(response, self);
			    }).fail(function(info) {
			    	console.log("Fail Called");
			    	failedCallback(info, self);
			    })
            },

            performPasswordChange: function(ev) {
            	var self = this;

                if(this.model.isValid(true)){
                	self.updateSPasswordner(true);
                	self.isTransferring = true;
                } else {
                	return;
                }

            	// Prevent dialog closing (Depending on this.isTransferring flags)
            	$('#viewDialog').on('hide.bs.modal', function(e){
            		if (self.isTransferring) {
           		     e.preventDefault();
        		     e.stopImmediatePropagation();
        		     return false;
            		}
        		});
        		var payload = {
        				newPassword : this.$('#newPassword').val(),
        				repeatPassword : this.$('#repeatPassword').val(),
        				currentPassword : this.$('#currentPassword').val(),
        				entityId : self.model.attributes.entityId,
        				entityType : self.model.entityType
        		};
            	self.doCall(self.options.url, payload, self.passwordChangedCallback, self.failedCallback);
            },
            updatePasswordRules : function(password){
            	//if password greater than or equal to minPasswordLength
            	if (password.length >= this.model.attributes.minPasswordLength){
            		this.$("#lengthRule").removeClass("fa-close");
            		this.$("#lengthRule").addClass("fa-check");
            		this.$(".length").removeClass("password-rule-invalid");
            		this.$(".length").addClass("password-rule-valid");
            	} else {
            		this.$("#lengthRule").removeClass("fa-check");
            		this.$("#lengthRule").addClass("fa-close");
            		this.$(".length").removeClass("password-rule-valid");
            		this.$(".length").addClass("password-rule-invalid");
            	}

            	//if password has lower case characters
            	if (password.match(/[a-z]/)) {
            		this.$("#lowerRule").removeClass("fa-close");
            		this.$("#lowerRule").addClass("fa-check");
            		this.$(".lower").removeClass("password-rule-invalid");
            		this.$(".lower").addClass("password-rule-valid");
            	} else {
            		this.$("#lowerRule").removeClass("fa-check");
            		this.$("#lowerRule").addClass("fa-close");
            		this.$(".lower").removeClass("password-rule-valid");
            		this.$(".lower").addClass("password-rule-invalid");
            	}

            	//if password has upper case characters
            	if (password.match(/[A-Z]/)) {
            		this.$("#upperRule").removeClass("fa-close");
            		this.$("#upperRule").addClass("fa-check");
            		this.$(".upper").removeClass("password-rule-invalid");
            		this.$(".upper").addClass("password-rule-valid");
            	} else {
            		this.$("#upperRule").removeClass("fa-check");
            		this.$("#upperRule").addClass("fa-close");
            		this.$(".upper").removeClass("password-rule-valid");
            		this.$(".upper").addClass("password-rule-invalid");
            	}
            	//if password has at least one number
            	if (password.match(/\d+/)){
            		this.$("#numberRule").removeClass("fa-close");
            		this.$("#numberRule").addClass("fa-check");
            		this.$(".number").removeClass("password-rule-invalid");
            		this.$(".number").addClass("password-rule-valid");
            	} else {
            		this.$("#numberRule").removeClass("fa-check");
            		this.$("#numberRule").addClass("fa-close");
            		this.$(".number").removeClass("password-rule-valid");
            		this.$(".number").addClass("password-rule-invalid");
            	}
            	//if password has at least one special character
            	if ( password.match(/[ !"#$%&'()*+,-./:;<=>?@\\[\]^_`{|}~]/) ){
            		this.$("#specialRule").removeClass("fa-close");
            		this.$("#specialRule").addClass("fa-check");
            		this.$(".special").removeClass("password-rule-invalid");
            		this.$(".special").addClass("password-rule-valid");
            	} else {
            		this.$("#specialRule").removeClass("fa-check");
            		this.$("#specialRule").addClass("fa-close");
            		this.$(".special").removeClass("password-rule-valid");
            		this.$(".special").addClass("password-rule-invalid");
            	}
            }
        });
        return PasswordChangeDialogView;
    });