define(['jquery', 'App', 'marionette'], 
	function($, App, Marionette) {
		// Note validate returning true causes problems with changing model
		var TransactionsConfigModel = Backbone.Model.extend ({
			url: 'api/config/transactions',
			mode: null,
			_baseUrl: 'api/config/transactions',

		});
		
		return TransactionsConfigModel;
	}
);
