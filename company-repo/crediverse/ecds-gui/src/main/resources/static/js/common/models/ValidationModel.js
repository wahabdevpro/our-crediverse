define(['jquery.plugins', 'underscore', 'App', 'backbone', 'handlebars', 'utils/CommonUtils', 'jquery.numeric', 'backbone.nested'],
	function($, _, App, Backbone, Handlebars, CommonUtils) {

		// Note validate returning true causes problems with changing model
		var ValidationModel = Backbone.NestedModel.extend ({
			// JQuery Validation Rules
			rules: {},

			masks: {},
			
			// For error messages
			errorContext: null,
			
			// server to page mappings ("serverId" : "formId") 
			fieldMappings: {
			},
			
			violationMsgs: App.violations,
			generalErrorClass: "generalError",
			displayRequiredStars: true,
			formGeneralMessage: App.violations.formGeneralMessage,

			validator: null,

			// FormReference
			form: null,

			// Used to override the default model error handling implementation
			// implement as errorHandler: function(mdl, error)
			errorHandler: null,
			
			formCleanup: function() {
				if (!_.isUndefined(this.validator))
					this.validator.destroy();
				this.hideErrorPanel();
			},
			
			clearValidationMarkers: function(element) {
				element.closest('.form-group').removeClass('has-error has-feedback');
	            element.closest('.form-group').find('i.fa').remove();
	            element.closest('.form-group').find('.help-block').remove();
			},

			bind: function(form) {
				var self = this;
				this.form = form;
				try {
					this.form.destroy();
				} catch(err) {
				}
				
				this.validator = this.form.validate({rules:this.rules /*, ignore: ".NumBox, .ignore"*/});
				this.form.validate().resetForm();
				var currentForm = this.form;
				_.each(this.masks, function(expression, name) {
					
					App.log("Associating masked input ...");
					App.log(expression);
					App.log(name);
					
					var $element = currentForm.find('input[name="'+name+'"]');
					if ($element.length == 0) {
						App.error("ValidationModel:: Unable to match "+name+" uniquely");	
					} else {
						// Internationalize expression here
						$element.mask(expression);
					}
					
				});

				// Enable Numeric plugin
				_.each(this.rules, function(expression, name) {
					var places = 0;
					var numeric = false
					var negative = false;
					_.each(expression, function(value, name) {
						if (name === 'places' && value) {
							places = value;
						}
						if (name === 'numeric' && value === true) {
							numeric = true;
						}
						if (name === 'negative' && value === true) {
							negative = true;
						}
					});
					if (numeric) {
						var $element = currentForm.find('input[name='+name+']');
						if (places > 0) {
							$element.numeric({
								decimal: ".",
								negative: negative,
								decimalPlaces: places
							});
						}
						else {
							$element.numeric({
								negative: negative,
								decimalPlaces: places
							});
						}
					}					
				});

				// Display Stars next to each required field 
				if (this.displayRequiredStars) {
					this.showRequiredStars();
				}

				// Find dialog and attach clean up event when closed
				try {
					var ref = $(currentForm).closest(".modal");
					ref.on('hidden.bs.modal', function (e) {
						self.formCleanup();
					});
				} catch(err) {
					App.error("Validation error: " + err);
				}
			},

			/*
			 * If this is overridden, then call bind yourself.
			 */
			initialize: function(options) {
				var that = this;
				if (!_.isUndefined(options)) {
					if (!_.isUndefined(options.form)) this.bind(options.form);
					if (!_.isUndefined(options.url)) this.url = options.url;
					if (!_.isUndefined(options.rules)) this.url = options.rules;					
				}
				this.set('unsavedChanges', false);
			},

			showRequiredStars: function() {
				var self = this;
				var requiredCount = 0;
				_.each(this.rules, function(expression, name) {
					
					var required = false;
					_.each(expression, function(value, name) {
						if (name === 'required' && value === true) {
							required = true;
							requiredCount++;
						} 
					});
					
					if (required) {
						var $element = self.form.find('input[name="'+name+'"], select[name="'+name+'"], textarea[name="'+name+'"]');
						if ($element.length > 0) {
							$element.closest('.form-group').addClass("required");
						}
					} else {
						var $element = self.form.find('input[name="'+name+'"], select[name="'+name+'"], textarea[name="'+name+'"]');
						if ($element.length > 0) {
							$element.closest('.form-group').removeClass("required");
						}
					}
				});
				if (requiredCount > 0) this.showRequiredNote();
			},
			
			showRequiredNote: function() {
				var note = this.form.find(".config-tip-info");
				
				if (note.length == 0) {
					var html = [];
					html.push( "<div class='config-tip-panel config-tip-info'>" );
					html.push( "<p>{{{i18n 'global.requiredNote'}}}</p>" );
					html.push( "</div>" );
					var template = Handlebars.compile( html.join("") );
					this.form.prepend( template );
				}
			},
			
			
			validate: function() {
				if (!_.isUndefined(this.form) && !_.isNull(this.form)) {
					if (!this.form.valid()) {
//						this.showGeneralFieldViolations();
						return "NOT VALID";
					}
				}
			},
			
			valid: function() {
				if (!_.isUndefined(this.form) && !_.isNull(this.form)) {
					return this.form.valid();
				}
				return true;
			},
			
			defaultErrorHandler: function(error) {
				if (!_.isUndefined(error.responseJSON) && !_.isUndefined(error.responseJSON.violations)) {
					try {
						// Field Violation
						var violations = error.responseJSON.violations
						var fieldMappings = (_.isUndefined(this.fieldMappings))? {} : this.fieldMappings;
						var noFieldMappings = this.validator.injectServerViolations(violations, this.form, fieldMappings);

						// General Violation
						this.showGeneralFieldViolations(violations, noFieldMappings);
					} catch(err) {
						App.error(err)
					}
				}
			},
			
			// Override Save
			save: function(data, callbacks) {
				try {
					var self = this;
					if (_.isObject(data) && (_.isUndefined(callbacks))) {
						callbacks = data;
						if (_.isUndefined(this.form) || _.isNull(this.form)) {
							App.error("ValidationModel:: this.form must be a valid form for validation to work");
							return;
						}
						data = Backbone.Syphon.serialize(this.form);
					}
					
					// use "preprocess" to update to be saved data. I the preprocess returns data, that data is then saved
					if (!_.isUndefined(callbacks) && !_.isUndefined(callbacks.preprocess) && _.isFunction(callbacks.preprocess)) {
						var replyData = callbacks.preprocess(data);
						if (! _.isUndefined(replyData))
							data = replyData;
					}

					Backbone.Model.prototype.save.call(this, data, {
						wait: true,
						success: function(mdl, resp) {
							if (!_.isUndefined(callbacks) && !_.isUndefined(callbacks.success)) {
								try {
									callbacks.success(mdl, resp);
									
								}
								catch(err) {
									App.error("ValidationModel::save success callback ");
									App.error(err);
								}
							}
							App.unsavedChanges = false;
							self.set('unsavedChanges', false);
						},
						error: function(mdl, error) {
							try {
								// Either use default ErrorHadler or use overridden one
								if ((self.errorHandler != null) && _.isFunction(self.errorHandler)) {
									self.errorHandler(mdl, error);
								} else {
									$.proxy(self.defaultErrorHandler(error), self);
								}
								
								// View error feedback
								if (!_.isUndefined(callbacks) && !_.isUndefined(callbacks.error)) {
									callbacks.error(mdl, error);
								}
							} catch(err) {
								App.error("ValidationModel::save error callback ");
								App.error(err);
							}
						}
					});
				}
				catch(err) {
					App.error("ValidationModel::save general ");
					App.error(err);
				}
			},

			getViolationsPanel: function() {
				var violationPanel = this.form.find(".form-error-panel");
				if (violationPanel.length == 0) {
					this.form.prepend( this.createErrorPanel() );
					violationPanel = this.form.find(".form-error-panel");
				}
				return violationPanel;
			},

			hideErrorPanel: function() {
				var violationPanel = this.getViolationsPanel();
				violationPanel.hide();
			},

			showErrorPanel: function() {
				var violationPanel = this.getViolationsPanel();
				violationPanel.show();
			},

			createErrorPanel: function() {
				return '<div class="panel panel-danger form-error-panel" role="alert"></div>';
			},

			createListItem: function(txt) {
				var html = [];
				html.push('<li>');
				html.push(txt);
				html.push('</li>');
				return html.join('');
			},
		
			createHandleBarsMessage: function(msg, variables) {
				var template = Handlebars.compile(msg);
				return template(variables);
			},
			
			createErrorList: function(violations, nonMappedFields) {
				var errs = [];
				var fieldViolations = false;
				var uniqueErrors = [];
				
				for(var i=0; i<violations.length; i++) {
					var violation = violations[i];
					if (!_.isUndefined(violation.field) || ((!_.isUndefined(this.fieldMappings)) && (!_.isUndefined(this.fieldMappings.field)))) {
						fieldViolations = true;
					} else {
						// Ensure that the errors reported are unique
						var errCode = violation.validations[0] + ":" + violation.correlationID;
						if ($.inArray(errCode, uniqueErrors) < 0) {
							var err = CommonUtils.createViolationErrorMessage(violation, this.errorContext);
							if (err != null) errs.push(err);
						}
					}
				}
				if (fieldViolations) {
					errs.push(this.violationMsgs.fieldViolations);
				} 
				if (!_.isUndefined(nonMappedFields)) {
					try {
						for(var i=0; i<nonMappedFields.length; i++) {
							App.log(this.createHandleBarsMessage(this.violationMsgs.noFieldMappingConsoleMsg, nonMappedFields[i]));
							var msg = this.createHandleBarsMessage(this.violationMsgs.noFieldMappingUserMsg, nonMappedFields[i]);
							errs.push(msg);
						}
					} catch(err) {
						App.error(err)
					}
				} 
				return errs; 
			},
			
			createErrorText: function(violations, nonMappedFields) {
				var txt = [];
				
				// Create Heading
				txt.push('<div class="panel-heading error-panel-heading"><i class="fa fa-exclamation-circle font-error-heading"></i><span class="txt-error-panel-heading">&nbsp;');
				txt.push(this.formGeneralMessage);
				txt.push('</span></div>' );
				txt.push('<div class="panel-body error-panel-body"><pre class="error-code">')
				//Error List
				var errors = this.createErrorList(violations, nonMappedFields);
				for(var i=0; i<errors.length; i++) {
					txt.push(errors[i]);
					txt.push('<br>');
				}
				txt.push('</pre></div>');
				
				return txt.join("");
			},

			showGeneralFieldViolations: function(violations, nonMappedFields) {
				try {
					if (this.form != null) {
						var violationPanel = this.getViolationsPanel();
						if (_.isUndefined(violations)) {
							violations = [{field:"tests"}];
						}
						var txt = this.createErrorText(violations, nonMappedFields);
						violationPanel.html(txt);
						this.showErrorPanel();
					}
				} catch(err) {
					App.error(err);
				}
			}

		});


		return ValidationModel;
});
