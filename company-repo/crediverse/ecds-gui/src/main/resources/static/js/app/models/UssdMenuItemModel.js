define(["underscore", "backbone", "marionette", "models/ValidationModel"],
	function(_, BackBone, Marionette, ValidationModel) {
	
		var UssdMenuItemModel = ValidationModel.extend ({
			initialize: function() {},
			url: 'api/config/ussdmenu',
			mode: 'create',
			id: null,
			currentMenu:null,
			autofetch:true,
			
			defaults: {
				editMode: false
			},
			initialize: function(options) {
				
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
		
		return UssdMenuItemModel;
	}
);
