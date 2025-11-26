define(["jquery", "backbone", "App", "models/ValidationModel"],
    function ($, Backbone, app, ValidationModel) {
        // Creates a new Backbone Model class object
        var AdjustmentModel = ValidationModel.extend({
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
				"targetTier.type" : "targetTierID",
				"buyerTradeBonusPercentage" : "buyerTradeBonusPercentageString",
			},
			
            masks: {
            	//'tradeBonusPercentageString': '99.9'
            },
            
            rules: {
				'reason': {
		            maxlength: 80,
		            required: true
				},
				'amount': {
					required: true,
					places: 8,
//					numeric:true,	// TODO: Need to plug autoNumeric into RULES
					negative:true
				},
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
            getGroups: function() {
            	//var permissions = '';
            },
            
            updatePermission: function(state, data) {
            	var permissions = this.get('permissions');
            	if (_.isUndefined(permissions)) permissions = [];
            	
            	if (state) {
            		permissions.push(data);
            	}
            	else {
            		permissions = _.without(permissions, _.findWhere(permissions, {
              		  id: data.id
              	}));
            	}
            	
            	this.set('permissions', permissions, {slient: true});
            }
        });

        return AdjustmentModel;
    }
);
