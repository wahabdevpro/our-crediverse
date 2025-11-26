define(['jquery', 'App', 'marionette', 'models/ValidationModel'], 
	function($, App, Marionette, ValidationModel) {
		// Note validate returning true causes problems with changing model
		var ReportModel = ValidationModel.extend ({
			url: 'papi/reports',
			report: null,
			mode: null,
			id: null,
			
			initialize: function(options) {
				if (typeof options !== 'undefined') {
					if (options.id && options.report) {
						this.url += "/" + options.report + '/' + options.id;
						this.id = options.id;
						this.report = options.report;
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
		            maxlength: 50,
		            required: false,
				},
				'description': {
		            maxlength: 80,
		            required: false,
				},
				'a_MobileNumber': {	
					msisdn: true
				},
				'a_OwnerMobileNumber': {	
					msisdn: true
				}
			},
			
			
			defaults: {
			},
		});
		
		return ReportModel;
	}
);
