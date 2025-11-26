define(['jquery', 'underscore', 'App', 'handlebars', 'utils/IsoLanguage', 'utils/CommonUtils'], 
	function($, _, App, Handlebars, IsoLanguage, CommonUtils) {

	var HandleBarHelpers = {
			createUnsortedSelectHTML: function(name, details, defItem, value, className) {
				var html = [];
				try {				
					var sorted = [];
					var firstItem = ""; //a.k.a. Default Item, but there is already a "defItem".  Two things that are supposedly the same thing.  "First item" doesn't have a value in the key value pair data string.
					details.split(',').forEach(function(x){
					    var arr = x.split(':');
					    if(arr[1]) {
					    	var itext = arr[1];
							try {
								itext = App.translate(arr[1]);
								if (_.isUndefined(itext))  {
									itext = arr[1];
								}
							} catch(err) {
								App.error(err);
							}
					    	if(arr[0]){
					    		sorted.push([arr[0], itext]);
					    	} else
					    		firstItem = itext;
					    }
					});
					
					//sorted.unshift(["", firstItem]);
					
					var classList = "form-control" 
					if(className) {
						classList +=  " " + className;
					}				
					html.push('<select id="' + name + '" name="'+name+'" class="' + classList + '">');
					for(var i=0; i < sorted.length; i++) {
						var keyPair = sorted[i];
						html.push('<option value="');
						var key = $.trim(keyPair[0]);
						html.push(key);
						html.push('"');
						if ( (!_.isUndefined(value)) && (value!= null) && (value==key) ) {
							html.push(' selected="selected"');
						}
						html.push('>');
						var itext = keyPair[1];
						html.push(itext);
						html.push('</option>');
					}
					html.push('</select>');
				} catch(err) {
					if (console) console.error(err);
				}
				return html.join("");
			},
			registerUnsortedSelect: function() {
				var self = this;
				Handlebars.registerHelper('unsortedselect', function(name, details, value, defitem, className) {
					return new Handlebars.SafeString( self.createUnsortedSelectHTML(name, details, defitem, value, className) );
				});
				
			},
			
		createSelectHTML: function(name, details, defItem, value, className) {
			var html = [];
			try {				
				var sorted = [];
				var firstItem = ""; //a.k.a. Default Item, but there is already a "defItem".  Two things that are supposedly the same thing.  "First item" doesn't have a value in the key value pair data string.
				details.split(',').forEach(function(x){
				    var arr = x.split(':');
				    if(arr[1]) {
				    	var itext = arr[1];
						try {
							itext = App.translate(arr[1]);
							if (_.isUndefined(itext))  {
								itext = arr[1];
							}
						} catch(err) {
							App.error(err);
						}
				    	if(arr[0]){
				    		sorted.push([arr[0], itext]);
				    	} else
				    		firstItem = itext;
				    }
				});
				
				sorted.sort(function(a, b) {
				    if(a[1] > b[1]) {
				    	return 1;
				    } if(a[1] < b[1]) {
				    	return -1;
				    } else {
				    	return 0;
				    }	
				});
				
				sorted.unshift(["", firstItem]);
				
				var classList = "form-control" 
				if(className) {
					classList +=  " " + className;
				}				
				html.push('<select id="' + name + '" name="'+name+'" class="' + classList + '">');
				if ((!_.isUndefined(defItem)) && (defItem!= null)) {
					html.push('<option value="">');
					html.push($.trim(defItem));
					html.push('</option>');
				}
				for(var i=0; i < sorted.length; i++) {
					var keyPair = sorted[i];
					html.push('<option value="');
					var key = $.trim(keyPair[0]);
					html.push(key);
					html.push('"');
					if ( (!_.isUndefined(value)) && (value!= null) && (value==key) ) {
						html.push(' selected="selected"');
					}
					html.push('>');
					var itext = keyPair[1];
					html.push(itext);
					html.push('</option>');
				}
				html.push('</select>');
			} catch(err) {
				if (console) console.error(err);
			}
			return html.join("");
		},
		
		registerSelect: function() {
			var self = this;
			Handlebars.registerHelper('select', function(name, details, value, className) {
				return new Handlebars.SafeString( self.createSelectHTML(name, details, null, value, className) );
			});
			
		},
		
		// This allows expressions like: {{ i18n (concat "enums.transactionType." this.transactionTypeName) }}
		registerConcatHelper: function() {
			Handlebars.registerHelper('concat', function() {
	            var outStr = '';
	            for(var arg in arguments){
	                if(typeof arguments[arg]!='object'){
	                    outStr += arguments[arg];
	                }
	            }
	            return outStr;
	        });
		},
		
		registerIfCondition: function() {
			Handlebars.registerHelper('ifCond', function (v1, operator, v2, options) {
			    switch (operator) {
			        case '==':
			            return (v1 == v2) ? options.fn(this) : options.inverse(this);
			        case '!=':
			            return (v1 != v2) ? options.fn(this) : options.inverse(this);
			        case '===':
			            return (v1 === v2) ? options.fn(this) : options.inverse(this);
			        case '<':
			            return (v1 < v2) ? options.fn(this) : options.inverse(this);
			        case '<=':
			            return (v1 <= v2) ? options.fn(this) : options.inverse(this);
			        case '>':
			            return (v1 > v2) ? options.fn(this) : options.inverse(this);
			        case '>=':
			            return (v1 >= v2) ? options.fn(this) : options.inverse(this);
			        case '&&':
			            return (v1 && v2) ? options.fn(this) : options.inverse(this);
			        case '||':
			            return (v1 || v2) ? options.fn(this) : options.inverse(this);
			        default:
			            return options.inverse(this);
			    }
			});
		},
		
		registerIn: function() {
			Handlebars.registerHelper('in', function() {
				var value = arguments[0];
				var options = arguments[ arguments.length - 1];
				for(var i=1; i<(arguments.length - 1); i++) {
					if (value === arguments[i]) {
						return options.fn(this);
					}
				}
				return options.inverse(this);
			});
		},
		
		registerNotIn: function() {
			Handlebars.registerHelper('notIn', function() {
				var value = arguments[0];
				var options = arguments[ arguments.length - 1];
				for(var i=1; i<(arguments.length - 1); i++) {
					if (value === arguments[i]) {
						return options.inverse(this);
					}
				}
				return options.fn(this);;
			});
		},
		
		registerHasPermissions: function() {
			/**
			 * Checks that user has all permissions given
			 * e.g. {{#hasPerms "canDoX" "canDoY"}} ... {{/hasPerms}}
			 */
			Handlebars.registerHelper('hasPerms', function () {
				var hasAllPermissions = true;
				var options = arguments[ arguments.length - 1];
				
				//Iterate though Permissions
				try {
					for(var i=0; i<(arguments.length - 1); i++) {
						var perm = arguments[i].toLowerCase();
						var hasPerm = _.isUndefined(App.userPerms[perm])? false : App.userPerms[perm];
//						App.log("Checking for perm: " + arguments[i] + " -> " + hasPerm);
						hasAllPermissions = hasAllPermissions && hasPerm;						
					}
				} catch(err) {
					App.error(err);
				}

				return hasAllPermissions? options.fn(this) : options.inverse(this);
			});
			
			Handlebars.registerHelper('hasAnyPerms', function () {
				var hasAnyPermissions = false;
				var options = arguments[ arguments.length - 1];
				
				//Iterate though Permissions
				try {
					for(var i=0; i<(arguments.length - 1); i++) {
						var perm = arguments[i].toLowerCase();
						var hasPerm = _.isUndefined(App.userPerms[perm])? false : App.userPerms[perm];
//						App.log("Checking for any perm: " + arguments[i] + " -> " + hasPerm);
						hasAnyPermissions = hasAnyPermissions || hasPerm;
					}
				} catch(err) {
					App.error(err);
				}

				return hasAnyPermissions? options.fn(this) : options.inverse(this);
			});
			
			Handlebars.registerHelper('perm', function (group, name) {
				return group + "_" + name;
			});
			
			
		},
		
		registerLoops: function() {
        	Handlebars.registerHelper('each', function(context, options) {
        		  var ret = "";

        		  if (!_.isUndefined(context)) {
            		  for(var i=0, j=context.length; i<j; i++) {
              		    ret = ret + options.fn(context[i]);
              		  }
        		  }

        		  return ret;
          	});
		},
		
		registerI18NHelper: function() {
			Handlebars.registerHelper('i18n', function(str) {
				var result = "unknown";
				if (!_.isUndefined(str)) {
					try {
						result = App.translate(str);
					} catch(err) {
						App.error("Error for str: " + str);
						App.error(err);
					}
					
				}
		    	return (_.isUndefined(result))? str : result;
		    });
			
			// If you need to render context sensitive string
			Handlebars.registerHelper('i18nctx', function(str) {
				var result = "unknown";
				if (!_.isUndefined(str)) {
					try {
						var translation = App.translate(str, str);
						var template = Handlebars.compile(translation, {noEscape:true});
						return template(this);							
					} catch(err) {
						App.error("Error for str: " + str);
						App.error(err);
					}
				}
		    	return (_.isUndefined(result))? str : result;
		    });
			
			// This allows expressions like: {{ i18nEnum "transactionType" this.transactionTypeName }}
			Handlebars.registerHelper('i18nEnum', function(enumName, constant) {
				var result = enumName;
				if (!_.isUndefined(constant)) {
					try {
						result = App.translate("enums." + enumName + "." + constant);	
					} catch(err) {
						result = constant;
						App.error("Error for str: " + str);
						App.error(err);
					}
				}
				
				return result;
			});
			
			Handlebars.registerHelper('i18nNum', function(number, noValueString) {
				if (!_.isUndefined(number) && _.isNumber(number)) {
					try {
						return CommonUtils.formatNumber(number);
					} catch(err) {
						App.error(err);
					}
				}
				
				if (!_.isUndefined(noValueString))
					return noValueString;
				else
					return App.translate("global.notAvailable");
			});
			
			this.registerIfCondition();
		},
		
		registerConfigRenderHelpers: function() {
		
			Handlebars.registerHelper('expandLanguage', function(langCode) {
				if (_.isUndefined(langCode)) {
					return "";
				} else {
					return IsoLanguage[langCode].name;	
				}
			});
			
			Handlebars.registerHelper('colourizeVariables', function(text) {
				if (!_.isUndefined(text) && ("" + text).indexOf("{") >= 0) {
						var html=[];
						for(var i=0; i<text.length; i++) {
							var ch = text[i];
							if (ch == "{")
								html.push("<span class='cm-variable'>");
							html.push(ch);
							if (ch == "}")
								html.push("</span>");
						}
						return new Handlebars.SafeString( html.join("") );
				} if (("" + text).match(/^ $/)) {
					return new Handlebars.SafeString( "<span class='no-data'>[space]</span>" );
				} if (_.isUndefined(text) || ("" + text).length == 0) {
					return new Handlebars.SafeString( "<span class='no-data'>(not set)</span>" );
				} else {
					return text;
				}
			});
			
		},
		
		registerStringHelpers: function() {
			Handlebars.registerHelper('toLowerCase', function (str) {
				if (!_.isUndefined(str) && str != null) {
					if(typeof str === "string") {
						return str.toLowerCase();
					} else {
						return str;
					}
				}
				return '';
			});
		},
		
		registerDateHelpers: function() {
			Handlebars.registerHelper('formatDate', function(date, noValueString) {
				if (!_.isUndefined(date) && _.isNumber(date)) {
					try {
						return CommonUtils.formatDate(date);
					} catch(err) {
						App.error(err);
					}
				}				
				if (!_.isUndefined(noValueString))
					return noValueString;
				else
					return Handlebars.SafeString( "<span class='no-data'>(not set)</span>" );
			});
		},
		registerContextHelpers: function() {
			Handlebars.registerHelper('ifThisUserIsYou', function(userId, options) {
				if (!_.isUndefined(userId) && userId == App.contextConfig.user.id) {
					 return options.fn(this)
				} else {
					 return options.inverse(this);
				}				
			});
		}
		
	}
	
	return HandleBarHelpers;
});