define(['jquery', 'underscore', 'App', 'marionette', "models/ValidationModel"], 
	function($, _, App, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var SummaryModel = Backbone.Model.extend ({
			//initialize: function() {},
			url: 'papi/dashboard/summary',
			
			id: null
			
		});
		
		return SummaryModel;
	}
);
