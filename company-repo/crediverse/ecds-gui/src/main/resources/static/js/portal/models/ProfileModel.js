define(['jquery', 'underscore', 'App', 'marionette', "models/ValidationModel"], 
	function($, _, App, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var ProfileModel = ValidationModel.extend ({
			//initialize: function() {},
			url: 'papi/profile',
			
			id: null
		});
		
		return ProfileModel;
	}
);
