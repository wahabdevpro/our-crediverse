define(["marionette", "models/ValidationModel"],
	function(Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var TierModel = ValidationModel.extend ({
			initialize: function() {},
			url: 'api/tiers',
			autofetch: true,
			rules: {
				'name': {
		            minlength: 3,
		            maxlength: 80,
		            required: true
				},
				'description': {
		            minlength: 3,
		            maxlength: 80,
		            required: true
				},
				'buyerDefaultTradeBonusPercentage': {
					required: true
				},
				'maxTransactionAmount': {
					required: false
				},
				'maxDailyCount': {
					numeric:true
				},
				'maxDailyAmount': {
					required: false
				},
				'maxMonthlyAmount': {
					required: false
				},	
				'maxMonthlyCount': {
					required: false
				},
				'status':{
					required: true
				}
			},
			
			initialize: function(options) {
				if (!_.isUndefined(options) && !_.isUndefined(options.url)) {
					this.url = options.url
				}
			},
			
			validate: function() {
				
			}
			
		});
		
		return TierModel;
	}
);