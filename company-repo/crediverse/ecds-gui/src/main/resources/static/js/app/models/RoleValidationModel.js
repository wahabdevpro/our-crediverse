define(["marionette", "utils/HandlebarHelpers", "models/ValidationModel"],
	function(Marionette, HandlebarHelpers, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var RoleValidationModel = ValidationModel.extend ({
			url: 'api/roles',
			
			initialize: function(options) {
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
				'type': {
		            required: true
				},
			}

		});

		return RoleValidationModel;
	}
);
