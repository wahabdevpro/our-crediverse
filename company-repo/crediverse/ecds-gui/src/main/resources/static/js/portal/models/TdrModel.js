define(['jquery', 'App', 'marionette', 'models/ValidationModel'], 
	function($, App, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var TdrModel = ValidationModel.extend ({
			//initialize: function() {},
			url: 'papi/tdrs',
			mode: 'create',
			id: null,
			
			initialize: function(options) {
				if (typeof options !== 'undefined') {
					if (options.id) {
						this.url += "/" + options.id;
						this.id = options.id;
					}	
					if (options.mode) this.mode = options.mode;
				}
			},
			rules: {
				'reason': {
		            maxlength: 80,
		            required: true
				},
				'amount': {
					required: true,
					places: 2,
					numeric:true
				},
				amountFrom: {
					numeric: true,
					digits: true
				},	
				amountTo: {	
					digits: true,
					numeric:true
				},	
				bonusAmountFrom: {	
					digits: true,
					numeric:true
				},	
				bonusAmountTo: {	
					digits: true,
					numeric:true
				},	
				chargeAmountFrom: {	
					digits: true,
					numeric:true
				},	
				chargeAmountTo: {	
					digits: true,
					numeric:true
				},
				msisdnA: {	
					msisdn: true
				},
				msisdnB: {	
					msisdn: true
				}
			},
			
			defaults: {
			},
		});
		
		return TdrModel;
	}
);
