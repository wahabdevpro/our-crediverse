define(["marionette", "models/ValidationModel"],
	function(Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var TierValidationModel = ValidationModel.extend ({
			url: 'api/tiers',
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
			}
			
		});
		
		return TierValidationModel;
	}
);