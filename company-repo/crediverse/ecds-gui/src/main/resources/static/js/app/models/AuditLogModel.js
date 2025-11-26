define(['jquery', 'App', 'marionette'], 
	function($, App, Marionette) {

		// Note validate returning true causes problems with changing model
		var AuditLogModel = Backbone.Model.extend ({
			//initialize: function() {},
			url: 'api/auditlog',
			mode: 'create',
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
		
		return AuditLogModel;
	}
);
