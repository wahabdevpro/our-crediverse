define(['jquery', 'underscore', 'App', 'backbone', 'marionette', 'models/ValidationModel'], 
	function($, _, App, Backbone, Marionette, ValidationModel) {

		// Note validate returning true causes problems with changing model
		var WorkItemModel = ValidationModel.extend ({
			//initialize: function() {},
			url: 'api/workflow',
			
			initialize: function(options) {
            	if (!_.isUndefined(options) && !_.isUndefined(options.url)) {
            		this.url = options.url;
				}
			},
			
			rules: {
				'updateReason': {
					required: true
				}
			},
			
			defaults: {
			},
		});
		
		return WorkItemModel;
	}
);
