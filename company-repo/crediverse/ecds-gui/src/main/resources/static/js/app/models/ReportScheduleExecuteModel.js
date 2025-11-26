define(["underscore", "backbone", "marionette", "models/ValidationModel"],
	function(_, BackBone, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var ReportScheduleExecuteModel = ValidationModel.extend ({
			initialize: function() {},
			url: 'api/reports',
			mode: 'execute',
			id: null,
			
			defaults: {
			},
			initialize: function(options) {
				if (typeof options !== 'undefined') {
					if (options.reportType && options.reportId) {
						this.url += "/" + options.reportType + "/" + options.reportId + "/schedule/" + options.scheduleId + "/execute";
						this.reportType = options.reportType;
						this.reportId = options.reportId;
						this.scheduleId = options.scheduleId;
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
				'referenceDt': {
		            required: true,
				},
				'referenceTm' : {
					time24: true,
		            required: true,
				},
			},
			
			masks: {
				"referenceDt":	"9999-99-99",
				"referenceTm":	"99:99:99",
			},

		});
		
		return ReportScheduleExecuteModel;
	}
);
