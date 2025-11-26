define(["marionette", "models/ValidationModel"],
	function(Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var CoAuthModel = ValidationModel.extend ({
			url: 'api/areas',
			mode: 'create',
			id: null,
						
			initialize: function(options) {
			},
			rules: {				
			}
			
		});
		
		return CoAuthModel;
	}
);
