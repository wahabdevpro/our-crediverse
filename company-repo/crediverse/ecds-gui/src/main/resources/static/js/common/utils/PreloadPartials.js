define(['jquery', "App", 'handlebars', 'marionette', 'utils/HandlebarHelpers', 'common/CommonPartials' ], 
	function($, App, Handlebars, Marionette, HBHelper, htmlTemplate) {
		var PreloadPartials = {
				initialize: function() {
					
					var scripts = $(htmlTemplate).find("script");

					try {
						for(var i=0; i<scripts.length; i++) {
							if (typeof scripts[i].type !== "undefined" && scripts[i].type=="partial") {
								var partialID = scripts[i].id;
								var html = scripts[i].innerHTML.trim();
								Handlebars.registerPartial(partialID, html);
							}
						}
					} catch(err) {
						App.error(err);
					}
				}
		};
		
		return PreloadPartials;
	}
);