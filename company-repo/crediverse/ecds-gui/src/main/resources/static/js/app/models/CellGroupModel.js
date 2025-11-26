define(["underscore", "backbone", "marionette", "models/ValidationModel"],
	function(_, BackBone, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var CellGroupModel = ValidationModel.extend ({
			initialize: function() {},
			url: 'api/cellgroups',
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
				'code': {
		            maxlength: 2,
		            required: true
				},						
				'name': {
		            maxlength: 80,
		            required: true
				}								
			}
		});
		
		return CellGroupModel;
	}
);
