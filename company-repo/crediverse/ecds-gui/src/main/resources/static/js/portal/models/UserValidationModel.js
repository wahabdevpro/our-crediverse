define(["marionette", "App", "models/ValidationModel"],
	function(Marionette, App, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var UserValidationModel = ValidationModel.extend ({
			url: 'api/wusers',
			mode : 'create',
			initialize: function(options) {
				if (!_.isUndefined(options)) {
					if (!_.isUndefined(options) && !_.isUndefined(options.id)) {
						this.url += "/" + options.id;
						this.id = options.id;
					}
					if(options.mode){
						this.mode = options.mode;	
					}
					if (!_.isUndefined(options) && !_.isUndefined(options.form)) {
						this.bind(options.form);
					}
				}
			},
					
			rules: {
				'firstName': {
		            minlength: 3,
		            maxlength: 80,
		            required: true
				},
				'surname': {
		            minlength: 3,
		            maxlength: 80,
		            required: true
				},
				'initials': {
					required: true
				},
				'mobileNumber': {
					minlength: 7,
		            required: true
				},
				'email': {
					email: true,
					required: false
				},
				'accountNumber': {
					required: true
				},
				'domainAccountName': {
					required: true
				},
				'language': {
					required: true
				},				
				'departmentID': {
					required: true
				}
			}
		});
		
		return UserValidationModel;
	}
);