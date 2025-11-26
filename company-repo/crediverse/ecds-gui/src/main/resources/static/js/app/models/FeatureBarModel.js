define(['jquery', 'App', 'marionette'], 
	function($, App, Marionette) {
		// Note validate returning true causes problems with changing model
		var FeatureBarModel = Backbone.Model.extend ({
			url: 'api/featurebar',
			mode: null,

			_baseUrl: 'api/featurebar',

			setFeature: function(featureName) {
				this.url = `${this._baseUrl}/${featureName}`;
			}
		});
		
		return FeatureBarModel;
	}
);
