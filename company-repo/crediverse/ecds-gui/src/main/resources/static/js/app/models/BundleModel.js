define(["jquery", "underscore", "backbone", "App", "models/ValidationModel"],
    function ($, _, Backbone, App, ValidationModel) {
        // Creates a new Backbone Model class object
        var BundleModel = ValidationModel.extend({
        	
        	url: "api/bundles",
            
        	fieldMappings: {
        		"tradeDiscountPercentage": "guiTradeDiscountPercentage"
        	},
        	
            rules: {
				"name": {
		            maxlength: 30,
		            required: true,
				},
				"price": {
					required:	true
				},
				"menuPosition": {
					required: true
				},
				"guiTradeDiscountPercentage": {
					required: true,
					numeric: true,
					places: 8,
					min: 0,
					max:100
				},
			},
			
			errorContext: "bundles",
			
			initialize: function() {
				ValidationModel.prototype.initialize.call(this);
        	},

        });

        return BundleModel;
    }
);
