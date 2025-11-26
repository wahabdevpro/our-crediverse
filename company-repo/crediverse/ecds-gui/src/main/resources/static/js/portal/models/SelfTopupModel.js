define(["jquery", "backbone", "App", "models/ValidationModel"],
    function ($, Backbone, app, ValidationModel) {
        // Creates a new Backbone Model class object
        var SelfTopupModel = ValidationModel.extend({
        	
        	url: "papi/transactions/selftopup",
        	
            initialize:function (options) {
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.url)) this.url = options.url;
            		if (!_.isUndefined(options.form)) this.bind(options.form);
            	}
            },
            
            rules: {
				'amount': {
					required: true,
					places: 2,
					numeric:true,
					negative:true
				}
			}

        });

        return SelfTopupModel;
    }
);
