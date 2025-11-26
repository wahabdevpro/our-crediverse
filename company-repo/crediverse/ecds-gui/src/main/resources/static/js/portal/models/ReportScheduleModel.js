define(["underscore", "backbone", "marionette", "models/ValidationModel"],
	function(_, BackBone, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var ReportScheduleModel = ValidationModel.extend ({
			initialize: function() {},
			url: 'papi/reports',
			mode: 'create',
			id: null,
			
			defaults: {
				editMode: false
			},
			initialize: function(options) {
				if (typeof options !== 'undefined') {
					if (options.reportType && options.reportId) {
						this.url += "/" + options.reportType + "/" + options.reportId + "/schedule" + (options.editMode != null ? "/" + options.scheduleId : "");
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
				'description': {
		            maxlength: 80,
		            required: true
				},
				'timeOfDayString' : {
					time24HM: true,
				},
				'startTimeOfDayString' : {
					time24HM: true,
				},
				'endTimeOfDayString' : {
					time24HM: true,
				},
			}
		});
		
		return ReportScheduleModel;
	}
);
