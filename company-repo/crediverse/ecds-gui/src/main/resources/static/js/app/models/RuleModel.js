define(["jquery", "backbone", "App", "models/ValidationModel"],
	function ($, Backbone, app, ValidationModel) {
		// Creates a new Backbone Model class object
		var RuleModel = ValidationModel.extend({
			initialize:function (options) {
				this.url = (options || {}).url;
				if (!_.isUndefined(options) && !_.isUndefined(options.form)) {
					this.bind(options.form);
				}
			},
			events: {
				'invalid': function() {
				}
			},

			fieldMappings: {
				"targetTier.type" : "targetTierID",
				"sourceTier.type" : "sourceTierID",
				"buyerTradeBonusPercentage" : "buyerTradeBonusPercentageString",
				"targetBonusPercentage" : "targetBonusPercentageString"
			},

			masks: {// TODO Find way to add comma and space seperators to input. See https://github.com/BobKnothe/autoNumeric
				//minimumAmount: '99/99/9999',
				//maximumAmount: '99/99/9999'
				"startTimeOfDayString": "99:99:99",
				"endTimeOfDayString": "99:99:99",
			},

			rules: {
				'name': {
					maxlength: 80,
					required: true
				},
				'currentDays': {
					required: true
				},
				'buyerTradeBonusPercentageString': {
					required: true,
					numeric: true,
					places: 8,
					min: 0,
					max:100
				},
				'targetBonusPercentageString': {
					numeric: true,
					places: 8,
					min: 0,
					max:1000
				},
				'targetBonusProfile': {
					maxlength: 10,
				},
				'sourceTierID' : {
					required: true
				},
				'targetTierID' : {
					required: true
				},
				'startTimeOfDayString' : {
					time24: true,
				},
				'endTimeOfDayString': {
					time24: true,
				}
			},

			masks: {
				"startTimeOfDayString":	"99:99:99",
				"endTimeOfDayString":	"99:99:99",
			},

			// Default values for all of the Model attributes
			defaults:{
				enabled: false,
				action: 'Modify',
				actionClass: 'roleModifyButton',
				ruleState: false,
				active: false,
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

		return RuleModel;
	}
);
