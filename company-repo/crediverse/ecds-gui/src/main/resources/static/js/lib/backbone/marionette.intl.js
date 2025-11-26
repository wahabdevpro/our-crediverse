/**
 * Note this is a Customization  of the Marionette Render method for Internationalization
 * @author: Martin, John
 */
define(['underscore', 'marionette.lib'], function (_, Marionette) {
	
			Marionette.Renderer.render = function(template, context) {
			      if (!template) {
				        throw new Marionette.Error({
				          name: 'TemplateNotFoundError',
				          message: 'Cannot render the template since its false, null or undefined.'
				        });
				      }
			      var templateFunc = _.isFunction(template) ? template : Marionette.TemplateCache.get(template);
				  
				  var locale = _.isUndefined( csUserLanguage )? "en-US" : csUserLanguage;
				  
				  var intlData = {
						  locales: locale
				  };
				  
				  return templateFunc(context, {
					  data: {intl: intlData}
				  });
			};
			
			return Marionette;
		}
	);