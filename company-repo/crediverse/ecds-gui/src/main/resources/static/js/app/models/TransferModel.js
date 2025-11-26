define(["jquery", "backbone", "App", "models/ValidationModel"],
    function ($, Backbone, app, ValidationModel) {
        // Creates a new Backbone Model class object
        var TransferModel = ValidationModel.extend({
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
				//"targetTier.type" : "targetTierID",
				//"tradeBonusPercentage" : "tradeBonusPercentageString"
			},
			
            masks: {
            	//'tradeBonusPercentageString': '99.9'
            },
            
            rules: {
				/* the autoNumeric is restricting the output sufficiently, no need for this validation, causes issue with FR numbers, which contains spaces
				'amount': {
					required: true,
					places: 2,
					number:true
				},
				*/
				//'bonusAmount': {
				//	required: true,
				//	places: 8,
				//	numeric:true
				//}
			},

            // Default values for all of the Model attributes
            defaults:{
            	enabled: false,
            	action: 'Modify',
            	actionClass: 'roleModifyButton',
            	ruleState: false,
            	active: false
            },
        });

        return TransferModel;
    }
);
