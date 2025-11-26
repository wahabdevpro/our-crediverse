define(["marionette", "models/ValidationModel"],
	function(Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var ServiceClassValidationModel = ValidationModel.extend ({
			urlRoot: 'api/serviceclass',
			mode: 'create',
			autofetch: true,
			id: null,
			
			defaults: {
				editMode: false
			},
			initialize: function(options) {
				if (typeof options !== 'undefined') {
					if (options.id) {
						this.id = options.id;
					}	
					if (options.mode) this.mode = options.mode;
				}
				if (!_.isUndefined(options) && !_.isUndefined(options.form)) {
					this.bind(options.form);
				}
				if (!_.isUndefined(options) && !_.isUndefined(options.url)) {
					this.url = options.url
				}
			},
			rules: {
				'name': {
					minlength: 3,
		            maxlength: 50,
		            required: true
				},
				'description': {
		            maxlength: 80,
		            required: true
				},
				'maxTransactionAmount': {
					required: false
				},
				'maxDailyCount': {
					required: false
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
		
		return ServiceClassValidationModel;
	}
);
