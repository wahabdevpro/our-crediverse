define(["jquery", "underscore", "backbone", "App", "models/ValidationModel"],
    function ($, _, Backbone, App, ValidationModel) {
        // Creates a new Backbone Model class object
        var RootAccountModel = ValidationModel.extend({
        	
            initialize:function (options) {
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.url)) this.url = options.url;
            		if (!_.isUndefined(options.form)) this.bind(options.form);
            	}
            },
            
            rules: {
				'amount': {
					required: true,
					places: 8,
					numeric:true
				},
				'bonusProvision': {
					required: true,
					places: 8,
					numeric:true
				},				
			},
        });

        return RootAccountModel;
    }
);