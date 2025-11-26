define(['jquery', 'underscore', 'App', 'marionette', "models/ValidationModel"], 
	function($, _, App, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var TdrsAModel = Backbone.Model.extend ({
			//initialize: function() {},
			url: 'papi/tdrs/mySales',
			
			id: null
			
		});
		
		return TdrsAModel;
	}
);