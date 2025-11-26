(function (factory) {
	if (typeof define === 'function' && define.amd) {
	    // AMD. Register as an anonymous module depending on jQuery.
		define(['jquery', 'underscore', "i18n!common/auth", 'template/login', 'handlebars', 'rsa'], factory);
	} else {
	    // No AMD. Register plugin with global jQuery object.
	    factory(jQuery, underscore, authText, Handlebars);
	}
}(function ($, _, authText, htmlTemplate, Handlebars) {
	'use strict';
	
	var AuthController = function(element, options, e) {
		if (e) {
			e.stopPropagation();
			e.preventDefault();
		}

		//Public
		this.$element = $(element);
		this.coauth = options.coauth;
		this.parentUuid = options.uuid;
		this.TIMEOUT = options.TIMEOUT;
		this.authComplete = options.authComplete;
		this.errorCallback = options.errorCallback;
		//Private
		this.templates = {};
		this.stateEnum ={
			loginRequest: 0,
			waitLogin: 1,
			pinRequest: 2,
			waitPin: 3,
			resendPin: 4,
			csrfExpired: 5
		};
		this.compState = this.stateEnum.loginRequest;
		this.msg = authText.signinheading;
		this.error = null;
		this.user = null;
		this.pass = null;
		this.imsi = null;
		this.cid = null;
		this.rsa = null;
		this.uuid = null;
		this.liState = null;
		
		// Constructor
		this._init();
	};
	
	AuthController.prototype = {
		_init : function() {
			this.loadTemplates();
			this.drawContent();
		},
		
		loadTemplates: function() {
			try {
				var scripts = $(htmlTemplate);
				
				try {
					this.templates = {};
					for(var i=0; i<scripts.length; i++) {
						var scriptID = scripts[i].id;
						if (!_.isUndefined( scriptID )) {
							var source = $(scripts[i]).html();
							var template = Handlebars.compile(source);
							var html = template(authText);
							this.templates[scriptID] = html;
						}
					}
				} catch(err) {
					if (console) console.error(err);
				}
			} catch(err) {
				if (console) console.error(err);
			}
		},
		
		logError: function(error) {
        	if (window.console && window.console.error) console.dir(error);
		},
		
		outerHTML: function($el) {
			return $("<div />").append($el.clone()).html();
		},
		
		createTopMessage: function(msg) {
			var $html = null;
			if (this.coauth == null)
				$html = $(this.templates.topmsg).html(msg);
			else
				$html = $(this.templates.coauthmsg).html(msg);
			return this.outerHTML($html);
		},
		
		reset: function(error) {
			this.error = _.isUndefined(error)? "" : error;
			this.compState = this.stateEnum.loginRequest;
			this.liState = null;
			this.drawContent();			
		},
		
		drawContent: function() {
			this.$widget = $(this.createContent());
			this.$element.empty().append(this.$widget);
			this.registerEvents();
			this.updateError();
			this.updateFocus();
		},
		
		updateError: function() {
			var errorComp = this.$element.find(".errormsg");
			if (errorComp.length > 0) {
				errorComp.html((this.error != null)? this.error : "");
			}
		},
		
		updateFocus: function() {
			switch(this.compState) {
				case this.stateEnum.loginRequest:
					this.$element.find("#user").focus();
					break;
				case this.stateEnum.pinRequest:
					this.$element.find("#pininput").focus();
					break;
				case this.stateEnum.csrfExpired:
					this.$element.find(".refreshPage").focus();
					break;
			}
		},
		
		// Create HTML Content
		createContent: function() {
			var html = [];
			if (this.coauth == null)
				html.push( '<div>' );
			else
				html.push( '<div class="authorisation-box">' );
				
			html.push( '<div class="login-box-body">' );
			html.push( this.createTopMessage(this.getMessage()) );
			
			switch(this.compState) {
				case this.stateEnum.loginRequest:
					if (this.coauth == null)
						html.push( this.templates.login );
					else
						html.push( this.templates.auth );
					break;
				case this.stateEnum.waitLogin:
				case this.stateEnum.waitPin:
				case this.stateEnum.resendPin:
					html.push( this.templates.wait );
					break;
				case this.stateEnum.pinRequest:
					if (this.coauth == null)
						html.push( this.templates.pin );
					else
						html.push( this.templates.authpin );
					break;
				case this.stateEnum.csrfExpired:
					html.push( this.templates.tokenfail );
					break;
			}
			
			html.push( '</div>' );
			html.push( '</div>' );
			return html.join("");
		},
		
		getMessage: function() {
			switch(this.compState) {
				case this.stateEnum.loginRequest:
					if (this.coauth == null)
						return authText.signinheading;
					else
						return authText.coauthheading;
				case this.stateEnum.waitLogin:
					return authText.waitloginheading;
				case this.stateEnum.pinRequest:
					return authText.pinheading;
				case this.stateEnum.waitPin:
					return authText.waitpinheading;
				case this.stateEnum.resendPin:
					return authText.resendpinheading;
				case this.stateEnum.csrfExpired:
					return authText.tokenExpiredHeading;
			}
		},
		
		registerEvents: function() {
			switch(this.compState) {
				case this.stateEnum.loginRequest:
					this.$element.find("#user").on("keypress", $.proxy(this.autoSignIn, this));
					this.$element.find("#user").on("input propertychange", $.proxy(this.signInCheck, this));
					this.$element.find("#pass").on("keypress", $.proxy(this.autoSignIn, this));
					this.$element.find("#pass").on("input propertychange", $.proxy(this.signInCheck, this));
					
					this.$element.find(".signin").on("click", $.proxy(this.signIn, this));
					break;
				case this.stateEnum.pinRequest:
					//PINBtn
					this.$element.find("#pininput").on("keyup", $.proxy(this.pinInCheck, this));
					this.$element.find("#pininput").on("input propertychange", $.proxy(this.pinInCheck, this));
					
					this.$element.find("#pininput").on("keypress", $.proxy(this.autoPinIn, this));
					this.$element.find(".pinin").on("click", $.proxy(this.pinIn, this));
					this.$element.find(".resendPIN").on("click", $.proxy(this.resendPIN, this));
					this.$element.find(".reSignIn").on("click", $.proxy(this.cancelSignIn, this));
					break;
				case this.stateEnum.csrfExpired:
					this.$element.find(".refreshPage").on("click", $.proxy(this.refreshPage, this));
					break;
			}
		},
		
		
		generalBtnCheck: function(canSubmit, btnClass) {
			var btn = this.$element.find(btnClass);
			if (canSubmit && btn.hasClass("disabled")) {
				btn.removeClass("disabled");
				btn.removeAttr("disabled");
				
			}			
			else if (!canSubmit && !btn.hasClass("disabled")) {
				btn.addClass("disabled");
				btn.attr("disabled","disabled");
			}
		},
		
		//Login Events
		autoSignIn: function(ev) {
			if (ev.which == 13) {
				if ((this.$element.find("#user").val().length > 0) && (this.$element.find("#pass").val().length > 0))
					this.signIn();
				else
					this.$element.find("#pass").focus();
			}
		},
		
		signInCheck: function() {
			var signIn = (this.$element.find("#user").val().length > 0) && (this.$element.find("#pass").val().length > 0);
			this.generalBtnCheck(signIn, ".signin");
		},

		signIn: function() {
			var canSignIn = (this.$element.find("#user").val().length > 0) && (this.$element.find("#pass").val().length > 0)
			if (canSignIn) {
				// Update View
				this.user = this.$element.find("#user").val();
				this.pass = this.$element.find("#pass").val();
				if ((this.$element.find("#imsi")).length > 0)
					this.imsi = this.$element.find("#imsi").val();
				this.error = null;
				this.compState = this.stateEnum.waitLogin;
				this.drawContent();
				
				// Start Sending Data
				this.doCalls();
			}
		},
		
		autoPinIn: function(ev) {
			if (ev.which == 13) {
				this.pinIn();
			}
		},
		pinInCheck: function() {
			var canPinIn = (this.$element.find("#pininput").val().length > 0);
			this.generalBtnCheck(canPinIn, ".pinin");
		},
		
		pinIn: function() {
			var canPinIn = (this.$element.find("#pininput").val().length > 0);
			if (canPinIn) {
				var pin = this.$element.find("#pininput").val();
				this.compState = this.stateEnum.waitPin;
				this.drawContent();
				
				this.doCalls(pin);
			}
		},
		
		cancelSignIn: function() {
			this.liState = null;
			this.compState = this.stateEnum.loginRequest;
			this.drawContent();
		},
		
		refreshPage: function() {
			window.location.reload(true);
		},
		
		doCalls: function(content) {
			var self = this;

			var thisURL = this.getContextPath();
			var lastCharacter = thisURL.slice(-1);
			if ( lastCharacter === '/' ) {
				thisURL = this.getContextPath();
			} else {
				thisURL = this.getContextPath()+"/";
			}

			if (this.liState == null) {
				var urlloc = thisURL + "auth?" + ((this.coauth != null)? "coauth=" + this.coauth : "");
				if (!_.isUndefined(this.parentUuid) && !_.isNull(this.parentUuid)) {
					urlloc = urlloc+"&forTransactionId="+this.parentUuid;
				}
				
			    $.ajax({
			    	url: urlloc,
			        dataType: "json",
			        success: function(data) {
			        	$.proxy(self.handleCallbacks(data), self);
			        },
			        error: function(info) {
			        	$.proxy(self.handleCallbacks(null, info), self);
			        },
			        timeout : self.TIMEOUT
			    });
			} else {
				var urlloc = thisURL + "auth";
				var self = this;
				var encrypted = {
						cid: self.cid,
						data : content
				};
				if (this.uuid != null) {
					_.extend(encrypted, {uuid: self.uuid})
				}

				var jsonText = JSON.stringify(encrypted);
				var encryptedtext = this.rsa.encrypt(jsonText);
				var sendData = {
						data : encryptedtext,
						otp : content
				};
				if (!_.isUndefined(this.parentUuid)) {
					_.extend(sendData, {parentUuid: this.parentUuid})
				}
				
				$.ajax({
			    	type : "POST",
			        url: urlloc,
			        headers : {
			            'Accept': 'application/json',
			            'Content-Type': 'application/json'
			        },
			        data : JSON.stringify(sendData, null, 2),
			        datatype : "json",
			        timeout : self.TIMEOUT
			    }).done(function(data) {
			    	$.proxy(self.handleCallbacks(data), self);
			    }).fail(function(info) {
			    	console.log("Fail Called");
			    	$.proxy(self.handleCallbacks(null, info), self);
			    })
			}
		},
		
		extractFieldId : function(json) {
			var pos = json.state.split("_", 2).join("_").length + 1;
			return json.state.substr(pos);
		},
		
		resendPIN: function(error) {
			this.error = _.isUndefined(error)? null : error;
			this.liState = null;
			this.doCalls();
			
			this.compState = this.stateEnum.resendPin;
			this.drawContent();
		},
		
		extractErrorMessage: function(error) {
			if (!_.isUndefined(error.responseJSON)) {
				var errMsg = null;
				
				try {
					var rspJSON = error.responseJSON;
					if (!_.isUndefined(authText[rspJSON.status])) {
						
						errMsg = this.translate(rspJSON.status);
						
					} else if (!_.isUndefined(authText[rspJSON.message])) {
						
						errMsg = this.translate(rspJSON.message);
						
					} else if (this.coauth && !_.isUndefined(rspJSON.violations) && (rspJSON.violations.length > 0)) {
						
						if (rspJSON.violations[0].validations == "CO_AUTHORIZE") {
							errMsg = this.translate("CO_AUTHORIZE");
						} else if (!_.isUndefined(this.errorCallback) && (this.errorCallback != null)) {
							this.passCoAuthError(error);
						} else {
							
							var validationErr = rspJSON.violations[0].validations;
							if (validationErr.length > 0) {
								var errCode = validationErr[0];
								if (!_.isUndefined(authText[errCode])) {
									errMsg = authText[errCode];
								}
							}
							
							if (errMsg == null) {
								errMsg = rspJSON.violations[0].msgs[0];
							}
							
						}
					} else if (!_.isUndefined(rspJSON.message)) {
						
						errMsg = rspJSON.message;
						
					} 
						
				} catch(err) {
					if (console) console.error(err);
				}

				if (errMsg == null) {
					errMsg = this.translate("ERR_UNKNOWN");
				}
				return errMsg;
			} else {
				return authText["TECHNICAL_ERROR"];
			}
		},
		
		translate: function(txt) {
			var msg = txt;
			if (!_.isUndefined(authText) && !_.isUndefined(authText[txt])) {
				msg = authText[txt];
			}
			return msg;
		},
		
		handleCallbacks: function(data, error) {
			try {
				if (data == null && !_.isUndefined(error)) {
					if (!_.isUndefined( error.responseJSON )) {
						//Do Error Translation Here
						/**
						 * 503 / SERVICE_UNAVAILABLE -> When the TS is just not present
						 * error.responseJSON.correlationID:"0581ba50495"
						 */
						if ((error.status == 403) && (!_.isUndefined(error.responseJSON)) && error.responseJSON.message.indexOf("CSRF")> 0) {
							this.compState = this.stateEnum.csrfExpired;
							this.drawContent();
						} else {
							this.reset( this.extractErrorMessage(error) );	
						}
						return;
					}
				} else {
					var repData = null;
					try {
						repData = JSON.parse(data.data);
					} catch(ex) {
						repData = data.data;
					}

					if (!_.isUndefined(data.auth) && data.auth != null) {
						this.createRsa(data.auth.modulus, data.auth.exponent);	
						this.cid = repData.cid;
					}
					
					if (!_.isUndefined(repData.uuid) && repData.uuid != null) {
						this.uuid = repData.uuid;
					}
					
					if (!_.isUndefined(repData.state)) {
						this.liState = repData.state;
						
						if (repData.state.indexOf("REQUIRE") == 0) {
							var request = this.extractFieldId(repData);
							if (request == "USERNAME") {
								this.doCalls(this.user);
							} else if (request == "IMSI") {
								try {
									this.doCalls(this.imsi);	
								} catch(err) {}
							} else if (request == "PASSWORD" || request == "PIN") {
								this.doCalls(this.pass);
							} else if (request == "OTP") {
								this.compState = this.stateEnum.pinRequest;
								this.drawContent();
							}
						} else if (repData.state == "AUTHENTICATED") {
							if (!_.isUndefined(this.authComplete)) {
								try {
									if (!_.isUndefined(this.parentUuid)) {
										repData.uuid = this.parentUuid;
									}
									this.authComplete.call(this, repData);
								}
								catch(err) {
									if (console) {
										console.error(authText["authCompleteCallbackFailed"]);
										console.error(err);
										console.trace();
									}
								}
							} else {
								if (console) console.error(authText["eventMissing"]);
							}
						} else {
							var msg = this.translate(repData.state);
							if (this.compState == this.stateEnum.waitPin) {
								this.resendPIN(msg);
							} else {
								this.reset(msg);	
							}
						}

					}
					
				}
			} catch(err) {
				this.logError(err);
			}

		},
		
		passCoAuthError: function(error) {
			if (!_.isUndefined(this.errorCallback)) {
				try {
					this.errorCallback(error, this);
				}
				catch(err) {
					if (console) {
						console.error("Error with callback");
						console.trace();
					}
				}
			} else {
				if (console) console.error(authText["eventMissing"]);
			}
		},
		
		// Utility functions
		getContextPath : function() {
			var tmp = '';
			for (var i=0; i<window.location.pathname.length; i++) {
				if (window.location.pathname[i] !== '/') {
					tmp = window.location.pathname.substring(i, window.location.pathname.indexOf("/",i));
					break;
				}
			}
			return window.location.origin+tmp;
		},
		
		createRsa : function(modulus, exponent) {
			this.rsa = new RSAKey();
			this.rsa.setPublic(modulus, exponent);
		},
		
	};
	
	$.fn.authController = function(option, event) {
		// get the args of the outer function..
		var args = arguments;
		var value;
		var result = null;
		var chain = this.each(function() {
		var $this = $(this), data = $this.data('authController'), options = typeof option == 'object' && option;
					
		if (!data) {
			var mergedOptions = $.extend({},$.fn.authController.defaults, options, $(this).data());
			$this.data('authController', (data = new AuthController(this, mergedOptions, event)));
		}
					
		result = data;
			var elementId = $(this).attr("id");
		});
		return result;
	};

	// Model defaults
	$.fn.authController.defaults = {
		cid : 1,
		TIMEOUT : 31000,
		
		// Callbacks
		authComplete: null,
		errorCallback: null,
		coauth: null
	};
	
}));
