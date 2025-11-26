/*
 * Configures Handlebars as the template engine for all views.  Currently only
 * compiles already loaded templates.  Implement loadTemplate to load precompiled templates.
 * See http://marionettejs.com/docs/v2.4.7/marionette.templatecache.html for details.
 * 
 * Note that in order to deal with templates, Bootstrap requires dialog templates
 * to be under a relative div, otherwise, the dialog may appear under the backdrop 
 * and be unclickable.
 * 
 * As we have multiple dialogs, the engine is extended to look for a dialog when each 
 * template is loaded.  If the template is found to contain a dialog, then add it to
 * the page in a location that prevents it from breaking.
 */

define(['jquery', 'backbone', 'marionette', 'underscore', 'handlebars'],
		function ($, Backbone, Marionette, _, Handlebars) {
			Marionette.Handlebars = {
				path: 'templates/',
				extension: '.handlebars'
			};
			Marionette.TemplateCache.prototype.compileTemplate = function(rawTemplate, options) {
				// use Handlebars.js to compile the template
				if (rawTemplate.dialog != null) {
					var dialog = Handlebars.compile(rawTemplate.dialog);
					var templateContainer = $('body #templates');
					if (templateContainer.length <= 0) {
						$('body').append($("<div id='templates'></div>"));
						templateContainer = $('body #templates');
					}
					var dialogId = rawTemplate.dialogId;
					if (rawTemplate.filename != null) {
						dialogId = rawTemplate.filename+dialogId;
					}
					var dialogDiv = templateContainer.find('#'+dialogId);
					
					if (dialogDiv.length <= 0) {
						templateContainer.append($("<div id='"+dialogId+"'></div>"));
						dialogDiv = templateContainer.find('#'+dialogId);
					}
					dialogDiv.html(Handlebars.compile(rawTemplate.dialog));
				}
				 
				return Handlebars.compile(rawTemplate.template);
			};
			
			// "MasterDetailLayout#layout"
			Marionette.TemplateCache.prototype.loadTemplate = function(templateId, options){
				var template=null;
				var foundTemplate={ template: null, dialog: null, filename: null, partial: null};
				var workingId = templateId;
				// e.g. "js/portal/" + Marionette.Handlebars.path
				var fullPath = require.toUrl('') + Marionette.Handlebars.path;
				var commonPath = 'js/common/'+Marionette.Handlebars.path;
				var divId=null;
				var filename=null;
				
				if (workingId.indexOf('/') > 0) {
					// First sort out the directory path
					var sections = workingId.split('/');
					for (var i=0; i<(sections.length - 1);i++) {
						fullPath +=sections[i];
						fullPath += '/';
					}
					workingId=sections[sections.length - 1];
				}
				if (workingId.indexOf('#') > 0) {
					var sections = workingId.split('#');
					filename=sections[0]
					foundTemplate.filename = filename;
					fullPath+=sections[0];
					fullPath+=Marionette.Handlebars.extension;
					divId = '#'+sections[1];
					$.ajax({
						url: fullPath,
						beforeSend: function(xhr){
							if (xhr.overrideMimeType) {
								xhr.overrideMimeType("application/text");
							}
						},
						dataType: 'text',
						success: function(data) {
							var scripts = $(data).find("script");
							foundTemplate.template = scripts.filter(divId).html();
							foundTemplate.dialogId = sections[1]+"dialog";
							var dialogTemplate = scripts.filter('#'+foundTemplate.dialogId);
							if (dialogTemplate.length > 0) {
								foundTemplate.dialog = dialogTemplate.html();
							}
							else {
								foundTemplate.dialog = null;
							}
							
							// Add ability to add partials
							for(var i=0; i<scripts.length; i++) {
								if (typeof scripts[i].type !== "undefined" && scripts[i].type=="partial") {
									var partialID = scripts[i].id;
									var html = scripts[i].innerHTML.trim();
									Handlebars.registerPartial(partialID, html);
								}
							}
							
						},
						error: function(data) {
							foundTemplate.template = templateId;
							//throw "NoTemplateError - Could not load template: '" + templateId + "'";
						},
						async: false
					});
				}
				else {
					foundTemplate.template = templateId;
					//throw "NoTemplateError - Could not find template: '" + templateId + "'";
				}

				// send the template back
				return foundTemplate;
			};
});