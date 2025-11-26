define(["marionette", "models/ValidationModel"],
	function(Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var CellModel = ValidationModel.extend ({
			url: 'api/cells',
			mode: 'create',
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
				'mobileCountryCode': {
					required: true
				},
				'mobileNetworkCode': {
					required: true
				},
				'localAreaCode': {
					required: true
				},
				'cellID': {
		            required: true
				},
				'latitude': {
		            required: false
				},
				'longitude': {
					required: false
				}					
			}
			
		});
		
		return CellModel;
	}
);
