define(['jquery', 'App', 'marionette', 'models/ValidationModel', 'momentjs'], 
	function($, App, Marionette, ValidationModel, moment) {

		// Note validate returning true causes problems with changing model
		var AnalyticsModel = ValidationModel.extend ({
			url: 'api/analytics',
			autofetch: true,
			id: null,
			
			initialize: function(options) {
				if (typeof options !== 'undefined') {
					if (options.id) {
						this.url += "/" + options.id;
						this.id = options.id;
					}	
					//if (options.mode) this.mode = options.mode;
				}
			},
			
			defaults: {

			},
		});
		
		return AnalyticsModel;
	}
);
