define(["jquery", "backbone", "App", "models/ValidationModel"],
    function ($, Backbone, app, ValidationModel) {
        // Creates a new Backbone Model class object
        var AdjudicateModel = ValidationModel.extend({
            initialize:function (options) {
            	if (!_.isUndefined(options)) {
            		if (!_.isUndefined(options.url)) this.url = options.url;
            		if (!_.isUndefined(options.form)) this.bind(options.form);
            	}
            },
            events: {
            	'invalid': function() {
            	}
            },
            
			fieldMappings: {
				
			},
			
            masks: {
            	//'tradeBonusPercentageString': '99.9'
            },
            
            rules: {
			/*
				'reason': {
		            maxlength: 80,
		            required: true
				},
			*/	
			},

            // Default values for all of the Model attributes
            defaults:{
            	enabled: false,
            	action: 'Modify',
            	actionClass: 'roleModifyButton',
            	ruleState: false,
            	active: false
            },
            
            valid: function() {
				return this.form.valid();
			},
        });

        return AdjudicateModel;
    }
);
