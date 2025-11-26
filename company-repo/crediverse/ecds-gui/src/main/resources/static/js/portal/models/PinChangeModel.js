define(["jquery", "backbone", "App", "models/ValidationModel"],
    function ($, Backbone, App, ValidationModel) {
        // Creates a new Backbone Model class object
        var PinChangeModel = ValidationModel.extend({
        	
        	url: "papi/transactions/changepin",
        	
            initialize:function (options) {
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.url)) this.url = options.url;
            		if (!_.isUndefined(options.form)) this.bind(options.form);
            	}
            },
            
            rules: {
            	'newPin' : {
            		required: true,
					numeric:true,
					negative:false
            	},
            	'repeatPin' : {
            		required: true,
					numeric:true,
					negative:false,
					equalTo: '#newPin'
            	},
            	
			},

            updateRules: function() {
            	var minPinLength = this.get("minPinLength");
            	var maxPinLength = this.get("maxPinLength");
            	this.rules["newPin"].minlength = minPinLength;
            	this.rules["newPin"].maxlength = maxPinLength;
            	
            	if (minPinLength == maxPinLength) {
            		this.set("pinLengthExact", minPinLength);
            	}
            }
        });

        return PinChangeModel;
    }
);
