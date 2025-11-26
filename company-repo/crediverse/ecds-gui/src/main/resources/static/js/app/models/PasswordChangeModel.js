define(["jquery", "backbone", "App", "models/ValidationModel"],
    function ($, Backbone, App, ValidationModel) {
        // Creates a new Backbone Model class object
        var PasswordChangeModel = ValidationModel.extend({
        	
        	url: "api/wusers/change_password",
        	
            initialize:function (options) {
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.url)) this.url = options.url;
            		if (!_.isUndefined(options.form)) this.bind(options.form);
            	}
            },
            
            rules: {
            	'webUserID' : {
            		required: true
            	},
            	'currentPassword' : {
            		required: true
            	},
            	'newPassword' : {
            		required: true,
            	},
            	'repeatPassword' : {
            		required: true,
					equalTo: '#newPassword'
            	},
            	
			},

            updateRules: function() {
            	var minPasswordLength = this.get("minPasswordLength");
            	var maxPasswordLength = this.get("maxPasswordLength");
            	this.rules["newPassword"].minlength = minPasswordLength;
            	this.rules["newPassword"].maxlength = maxPasswordLength;
            	
            	if (minPasswordLength == maxPasswordLength) {
            		this.set("PasswordLengthExact", minPasswordLength);
            	}
            }
        });

        return PasswordChangeModel;
    }
);
