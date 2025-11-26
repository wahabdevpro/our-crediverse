define(['jquery', 'underscore', 'App', 'marionette', "models/ValidationModel"], 
	function($, _, App, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var AgentModel = ValidationModel.extend ({
			//initialize: function() {},
			url: 'api/agents',
			
			mode: 'create',
			
			id: null,
			
			fieldMappings: {
				"tier.type" : "tierID",
			},
			
			rules: {
				'title': {
		            required: true
				},
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
				'language': {
					required:true
				},
				'gender': {
					required:true
				},
				'warningThreshold' : {
					places: 8,
					numeric:true,
				},
				'tierID' : {
					required: true
				},
				'roleID' : {
					required: true
				},
				'maxTransactionAmount' : {
					places: 8,
					numeric:true,
				},
				'maxDailyCount' : {
					places: 8,
					numeric:true,
				},
				'maxDailyAmount' : {
					places: 8,
					numeric:true,
				},
				'maxMonthlyCount' : {
					places: 8,
					numeric:true,
				},
				'maxMonthlyAmount' : {
					places: 8,
					numeric:true,
				},
				'accountNumber': {
					required: true
				},
				'authenticationType':{
					required:true
				},
				'email' :{
					required:true
				},
				'domainAccountName':{
					required:true
				},
				'authenticationMethod':{
					required:true
				}
			},
			
			initialize: function(options) {
				if (!_.isUndefined(options)) {
					if (options.id) {
						this.url += "/" + options.id;
						this.id = options.id;
					}
					
					if (options.account) {
						this.url += "/account/" + options.account;
					}
					if (options.mode) this.mode = options.mode;
					if (!_.isUndefined(options) && !_.isUndefined(options.form)) {
						this.bind(options.form);
					}
				}
			},
			
			defaults: {
			},
		});
		
		return AgentModel;
	}
);
