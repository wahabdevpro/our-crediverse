define(["marionette", "App", "models/ValidationModel"],
	function(Marionette, App, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var AgentUserModel = ValidationModel.extend ({
			url: 'api/ausers',
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
				'mobileNumber': {
					minlength: 7,
		            required: true
				},
				'accountNumber': {
					required: true
				},
				'domainAccountName': {
					required: false
				},
				'language': {
					required: true
				},				
				'department': {
				},
				'roleID': {
					required: true
				},
				'channelID': {
					required: false
				},
				'email': {
					required: false
				},
				'authenticationMethod' : {
					required: true
				},
				'channelTypeName' : {
					required: false
				},
				'title' : {
					required: false
				}
			}
		});
		
		return AgentUserModel;
	}
);
