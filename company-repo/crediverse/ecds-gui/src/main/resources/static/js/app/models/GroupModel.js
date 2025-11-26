define(["underscore", "backbone", "marionette", "models/ValidationModel"],
	function(_, BackBone, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		//var GroupModel = ValidationModel.extend ({
		var GroupModel = ValidationModel.extend ({
			initialize: function() {},
			urlRoot: 'api/groups',
			mode: 'create',
			autofetch: true,
			id: null,
			
			defaults: {
				editMode: false
			},
			initialize: function(options) {
				if (typeof options !== 'undefined') {
					if (options.mode) this.mode = options.mode;
				}
				if (!_.isUndefined(options) && !_.isUndefined(options.form)) {
					this.bind(options.form);
				}
			},
			rules: {
				'description': {
		            maxlength: 80,
		            required: true
				},
				'name': {
		            maxlength: 80,
		            required: true
				}								
			}
		});
		
		return GroupModel;
	}
);
