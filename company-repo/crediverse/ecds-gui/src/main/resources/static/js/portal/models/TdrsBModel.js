define(['jquery', 'underscore', 'App', 'marionette', "models/ValidationModel"], 
	function($, _, App, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var TdrsBModel = Backbone.Model.extend ({
			//initialize: function() {},
			url: 'papi/tdrs/myPurchases',
			
			id: null
			
		});
		
		return TdrsBModel;
	}
);