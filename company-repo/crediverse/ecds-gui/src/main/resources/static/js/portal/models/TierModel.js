define(["marionette"],
	function(Marionette) {

		// Note validate returning true causes problems with changing model
		var TierModel = Backbone.Model.extend ({
			initialize: function() {},
			url: 'papi/tiers',
			
			initialize: function(options) {
				if (options.url) {
					this.url = options.url
				}
			},
			
			validate: function() {
				
			},
			
			serverValidation: function(data, options) {
				var self = this;
				
				$.ajax({
					type: "POST",
					async: true,
					url: self.url + "/validate",
					data: JSON.stringify(data),
					success: options.success,
					error: options.error,
					contentType: "application/json; charset=utf-8",
					dataType: "json",
				});
			}
			
		});
		
		return TierModel;
	}
);