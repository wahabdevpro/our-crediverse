define(["marionette", "models/ValidationModel"],
	function(Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var AreaModel = ValidationModel.extend ({
			url: 'api/areas',
			mode: 'create',
			id: null,
						
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
					required: true
				},
				'type': {
					required: true
				}
			}
			
		});
		
		return AreaModel;
	}
);
