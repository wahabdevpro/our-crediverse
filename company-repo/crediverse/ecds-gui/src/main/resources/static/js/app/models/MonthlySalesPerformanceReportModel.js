define(["marionette", "models/ValidationModel"],
	function(Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var MonthlySalesPerformanceReportModel = ValidationModel.extend ({
			url: 'api/reports/monthlysalesperformance',
			mode: 'update',
			id: null,
			
			defaults: {
				editMode: false
			},
			initialize: function(options) {
				if (!_.isUndefined(options) && !_.isUndefined(options.id)) {
					this.url += "/" + options.id;
					this.id = options.id;					
				}
				if (!_.isUndefined(options) && !_.isUndefined(options.mode)) {
					this.mode = options.mode;
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
					required: true
				},
				'description': {
					required: false
				},
				'period': {
					required: true
				},
				'tiers': {
					required: false
				},
				'ownerAgents': {
					required: false
				},
				'agents': {
					required: false
				},
				'transactionTypes': {
		            required: true
				},
				'transactionStatus': {
		            required: true
				}					
			}
		});
		
		return MonthlySalesPerformanceReportModel;
	}
);
