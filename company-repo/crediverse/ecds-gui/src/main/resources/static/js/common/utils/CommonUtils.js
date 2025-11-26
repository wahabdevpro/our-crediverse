define( ['jquery', 'underscore', 'App', 'handlebars', 'marionette', 'backbone', 'common/locale', 'i18n!common/violations'],
    function($, _, App, Handlebars, Marionette, Backbone, commonTranslations, violationMsgs) {
		function pad(n, width, z) {
		  z = z || '0';
		  n = n + '';
		  return n.length >= width ? n : new Array(width - n.length + 1).join(z) + n;
		};
	
		var CommonUtils = {

			formatDate: function(date,def) {
				if (!_.isUndefined(date) && !isNaN(date) && isFinite(date)) 
				{
					var exp = new Date(date);
					var year = exp.getFullYear();
					var month = exp.getMonth() + 1;
					var day = exp.getDate();
					return year + "-" + (month < 10 ? '0' : '') + month + "-" + (day < 10 ? '0' : '') + day;
				}	
				if (_.isUndefined(def))
					return '';
				return def;
			},
			
			// For Localization (General Helper)
			intlBlock: function(content, options) {
			    var hash = [],
			        option, open, close;

			    for (option in options) {
			        if (options.hasOwnProperty(option)) {
			            hash.push(option + '=' + '"' + options[option] + '"');
			        }
			    }

			    open  = '{{#intl ' + hash.join(' ') + '}}';
			    close = '{{/intl}}';

			    return Handlebars.compile(open + content + close);
			},
			
			fixUpNumber: function(number, ops) {
				if (!_.isUndefined(number)) {
					
					// COnvert to number
					if (!_.isNumber(number)) {
						number = parseFloat(number);
					}

					// Rounding
					var opts = commonTranslations.numberFormat;	
					if (opts.rounding == "up") {
						v = parseFloat(number).toFixed( opts.digits );
					}
				}
				return number;
			},
			
			commonFormatNumber: function(number, style, localization, fd) {
				var local = _.isUndefined( localization )? csUserLanguage : localization;
				
				if (!_.isUndefined(number)) {
					var opts = commonTranslations.numberFormat;
					number = this.fixUpNumber(number);
					
					// Formatting
					var nf = new Intl.NumberFormat([local], {
						style: style,
						currency: opts.currency,
						//currencyDisplay: "symbol",
						maximumFractionDigit: !_.isUndefined(fd) ? fd : opts.fractionalDigits,
						minimumFractionDigits: !_.isUndefined(fd) ? fd : opts.fractionalDigits
					});
					
					var formatted = nf.format(number);
					return formatted;
				}
				
				return number;
			},
				
			
			formatDecimal: function(number, fd, localization) {
				return this.commonFormatNumber(number, "decimal", localization, fd);
			},
			
			formatNumber: function(number, localization) {
				return this.commonFormatNumber(number, "decimal", localization);
			},
			
			formatCurrency: function(number, localization) {
				return this.commonFormatNumber(number, "currency", localization);
			},
			
			formatTimeStampAsDate: function(timestamp, localization) {
				var local = _.isUndefined( localization )? csUserLanguage : localization;
				var tmpl = this.intlBlock('{{formatTime TIMESTAMP}}', {locales: local});
				var time = new Date(timestamp);
				var datePart = tmpl({ TIMESTAMP: timestamp });
				return datePart;
			},
			
			formatTimeStamp: function(timestamp, localization) {
				var local = _.isUndefined( localization )? csUserLanguage : localization;
				var tmpl = this.intlBlock('{{formatTime TIMESTAMP}}', {locales: local});
				var time = new Date(timestamp);
				var datePart = tmpl({ TIMESTAMP: timestamp });
				return datePart+' '+ pad(time.getHours(), 2)+':'+pad(time.getMinutes(), 2)+':'+pad(time.getSeconds(), 2);
			},
			
			timeStampToISODateTime: function(timestamp) {
				var date = new Date(timestamp)
					.toISOString()
					.replace(/T/, ' ')
					.replace(/\..+/, '');
				return date;
				
			},
			
			formatTimeSecAsHM: function(sec) {
				if (sec == null) return null;
				var hours = Math.floor(sec / 3600);
				var minutes = Math.floor((sec % 3600) / 60);
				return CommonUtils.leftPadNumber(hours,2) + ':' + CommonUtils.leftPadNumber(minutes,2);
			},
			
			leftPadNumber: function(num, size) {
				var s = "00000000" + num;
				return s.substr(s.length-size);
			},
			
			formatYesNo: function(boolValue) {
				if (_.isBoolean(boolValue)) {
					return boolValue? commonTranslations.enums.yesNo.yes : commonTranslations.enums.yesNo.no; 
				} else {
					return boolValue;
				}
			},
			
			renderStatus: function(status) {
				var response = [];
				response.push('<span class="label ');
 			   
				var state = (_.isUndefined(status) || status == null)? "" : status.toLowerCase();
				switch(state) {
					case "a":
					case "active":
						response.push('label-success">'+commonTranslations.enums.state.active+'</span>');
						break;
					
					case "s":
					case "suspended":
						response.push('label-warning">'+commonTranslations.enums.state.suspended+'</span>');
						break;
					
					case "d":
					case "deactivated":
						response.push('label-danger">'+commonTranslations.enums.state.deactivated+'</span>');
						break;
					
					case "p":
					case "permanent":
						response.push('label-primary">'+commonTranslations.enums.state.permanent+'</span>');
						break;

					case "notconfigured":
						response.push('label-default">'+commonTranslations.enums.state.notconfigured+'</span>');
						break;
					
					case "unavailable":
						response.push('label-warning">'+commonTranslations.enums.state.unavailable+'</span>');
						break;
						
					case "inactive":
					default:
						response.push('label-default">'+commonTranslations.enums.state.inactive+'</span>');
						break;
				};
				return response.join(' ');

			},
			
			modalType: {
				delete : 0,
				put : 1
			},
			
			delete: function(options, callbacks) {
				var self = this;
				
				// Update Options
				
				var modalOptions = {
					// If not provided one wil; be provided
					title: commonTranslations.global.deleteTitle,
					msg: commonTranslations.global.deleteMsg,
					context: {
						what: null,
						name: null,
						description: "N/A",
					},
						
					itemType: "Service Class",
					data: null,
					rowElement: null,	// jquery reference for highlighting
					errTitle: commonTranslations.global.deleteError,
					url: null,
					msgCss:   "modalDeleteMsg",
					highlightCss: "deleteHighlight",
					actionBtnText:	commonTranslations.global.deleteBtn,
					modalType: this.modalType.delete,
					actionBtnClass: "btn-danger"
				};
				
				
				if (!_.isUndefined(options.data)) {
					modalOptions.data = options.data;
				} else {
					App.error(commonTranslations.commonUtils.deleteOptionsMessage);
					return;
				}
				if (!_.isUndefined(options.data) &&  _.isUndefined(options.data.description)) {
					modalOptions.msg = commonTranslations.global.deleteMsgNoDesc;
				}
				if (!_.isUndefined(options.context)) modalOptions.context = options.context;
				if (!_.isUndefined(options.itemType)) modalOptions.itemType = options.itemType;
				if (!_.isUndefined(options.title)) modalOptions.title = options.title;
				if (!_.isUndefined(options.msg)) modalOptions.msg = options.msg
				if (!_.isUndefined(options.rowElement)) modalOptions.rowElement = options.rowElement
				if (!_.isUndefined(options.modalType)) modalOptions.modalType = options.modalType
				if (!_.isUndefined(options.actionBtnText)) modalOptions.actionBtnText = options.actionBtnText
				if (!_.isUndefined(options.actionBtnClass)) modalOptions.actionBtnClass = options.actionBtnClass
				if (!_.isUndefined(options.highlightCss)) modalOptions.highlightCss = options.highlightCss
				
				modalOptions.msg = "<div class='" + modalOptions.msgCss + "'>" + modalOptions.msg + "</div>";
				
				if (!_.isUndefined(options.url)) 
					modalOptions.url = options.url;
				else {
					App.error(commonTranslations.commonUtils.deleteUrlMessage);
					return;
				}
				var disabled = false;
				
				// Render HTML segments (include context)
				modalOptions.title = this.renderHtml(modalOptions.title, modalOptions.context);
				modalOptions.msg = this.renderHtml(modalOptions.msg, modalOptions.context);
				modalOptions.errTitle = this.renderHtml(modalOptions.errTitle, modalOptions.context);
				
				
				if (!_.isUndefined(modalOptions.rowElement) && modalOptions.rowElement != null)
						modalOptions.rowElement.find("td").addClass(modalOptions.highlightCss);
				
				App.vent.trigger('application:dialog', {
					title: modalOptions.title,
					text: modalOptions.msg,
					name: "deleteDialog",
					actionBtnText: modalOptions.actionBtnText,
					actionBtnClass: modalOptions.actionBtnClass,
					
					events: {
						"init": function() {
							var errorPanel = $(dialog).find(".deleteErrorPanel");
						},
						"click .cancelButton": function(event){
							if (!_.isUndefined(modalOptions.rowElement) && modalOptions.rowElement != null)
								modalOptions.rowElement.find("td").removeClass(modalOptions.highlightCss);
						},
						"click .close" : function(event) {
							if (!_.isUndefined(modalOptions.rowElement) && modalOptions.rowElement != null)
								modalOptions.rowElement.find("td").removeClass(modalOptions.highlightCss);
						},
						"click .actionButton": function(event) { 
							if (!disabled) {
								var dialog = this;
								disabled = true;
								//Disable delete button
								$(dialog).find(".actionButton").addClass("disabled");
								
								var model = new Backbone.Model(modalOptions.data);
								model.url = modalOptions.url;
								
								if (modalOptions.modalType == self.modalType.delete) {
									model.destroy({
										wait:true,
										success: function(model, response) {
											dialog.modal('hide');
											if ( (!_.isUndefined(callbacks)) && (!_.isUndefined(callbacks.success))) {
												callbacks.success(model, response);
											}
										},
										
										error: function(model, response) {
											self.showDeleteDialogErrorMessage(dialog, modalOptions, model, response);
										}
										
									});
								} else if (modalOptions.modalType == self.modalType.put) {
									model.save(null, {
//										wait:true,
										success: function(model, response) {
											if (!_.isUndefined(modalOptions.rowElement) && modalOptions.rowElement != null)
												modalOptions.rowElement.find("td").removeClass(modalOptions.highlightCss);
											
											dialog.modal('hide');

											if ( (!_.isUndefined(callbacks)) && (!_.isUndefined(callbacks.success))) {
												callbacks.success(model, response);
											}
										},
										
										error: function(model, response) {
											self.showDeleteDialogErrorMessage(dialog, modalOptions, model, response);
										}
									
									});
								}
								
							}
							
						}
					}
				});
				
			},
			
			showDeleteDialogErrorMessage : function(dialog, modalOptions, model, response) {
				// Hide Delete button and Text
				$(dialog).find(".actionButton").addClass("hide");
				$(dialog).find(".modal-text").addClass("hide");
				$(dialog).find(".cancelButton").text("OK");
				$(dialog).find(".modal-title").addClass("deleteErrorTitle");
				$(dialog).find(".modal-title").html(modalOptions.errTitle);
				
				var errorPanel = $(dialog).find(".deleteErrorPanel");
				if (errorPanel.length >0) {
					errorPanel.removeClass("hide");
				}
				
				// Add Message
				if (!_.isUndefined(response.responseJSON.violations) && response.responseJSON.violations.length > 0) {
					var ref = response.responseJSON.violations[0].validations[0];
					var msg = Handlebars.compile(violationMsgs[ref])({item: modalOptions.itemType});
					var compiled = Handlebars.compile(violationMsgs["modalOperationFailMessage"]);
					$(dialog).find(".form-error-heading").text(compiled({
						msg:msg,
						correlationID:response.responseJSON.violations[0].correlationID
					}));
				}				
			},
			
			
			renderHtml: function(handleBarsTemplate, context) {
				if (!_.isUndefined(handleBarsTemplate)) {
					var template = Handlebars.compile(handleBarsTemplate, {noEscape:true});
					if (_.isUndefined(context))
						return template();	
					else
						return template(context);
				}
				return "unknown";
			},
			
			exportAsJson: function(url) {
				var pos = url.indexOf('?')
				var baseUrl= (pos >=0)?url.substr(0, pos):url;
				var exportUrl = baseUrl + "/export";
				
				try {
					window.location.href = exportUrl;
				}
				catch(err) {
					App.vent.trigger('application:dialog', {
    	        		text: commonTranslations.commonUtils.exportErrorMessage,
    	        		name: "okDialog"
            		});
				}
			},
			
			exportAsCsv: function(ev, url, search, formParams, nocount) {
				var pos = url.indexOf('?')
				var baseUrl= (pos >=0)?url.substr(0, pos):url;
				var searchUrl = baseUrl + "/csv";
				var countUrl  = baseUrl + "/count";
				
				var searchDetail = {};
				
				var $exportButton = $(ev.currentTarget);
				var exportButtonOrig = $exportButton.html();
				
				var title = CommonUtils.renderHtml( App.translate("commonUtils.exportDialogTitle") );
				var body = CommonUtils.renderHtml( App.translate("commonUtils.exportDialogMessage") );
				
				$exportButton.attr('disabled', 'disabled');
				$exportButton.html('<span class="glyphicon glyphicon-refresh spinning"></span>');
				
				if (!_.isUndefined(search)) {
					if (_.isString(search) && search.length > 0) {
						searchDetail.q = search;
					}
					else if (_.isObject(search)) {
						
						for (var propName in formParams) { 
						    if (formParams[propName] !== null && formParams[propName] !== undefined && formParams[propName].length > 0) {
						    	if(Array.isArray(formParams[propName])){
						    		//$.param which is used down below is really crappy, if this is not done then it renders two scalar items in the URL.
						    		searchDetail[propName] = JSON.stringify(formParams[propName]);
						    	} else {
						    		searchDetail[propName] = formParams[propName];
						    	}
						    }
						}
						searchDetail.search = search.search;
						searchDetail.start  = search.start;
						searchDetail.length = search.length;
					}
				}
				//searchDetail.docount = _.isUndefined(nocount);
				searchDetail.docount = false;// Default to no count

				var params = $.param(searchDetail);
				searchUrl+='?'+params;
				countUrl+='?'+params;
			
				var jqxhr = $.ajax({
				    type: 'GET',
				    url: countUrl,
				    processData: false
				})
				.done(function(response) {
					searchUrl = searchUrl+'&uniqid='+response.uniqid;
					var statusUrl = 'context/util/status/'+response.uniqid;
					var timer = setInterval( function () {
						
						var jqxhr = $.get(statusUrl, function() {
							 // alert( "success" );
							})
							  .done(function(data) {
								  if (data.complete) {
									  clearInterval(timer);
									  $exportButton.removeAttr('disabled');
									  $exportButton.html(exportButtonOrig);
								  }
							  })
							  .fail(function() {
							    clearInterval(timer);
							    $exportButton.removeAttr('disabled');
							    $exportButton.html(exportButtonOrig);
							  })
							  .always(function() {
							    //alert( "finished" );
							  });
	  					}, 3000 );
					if (searchDetail.docount) {
						if (response.count > 0) {
	              			window.location.href = searchUrl;
	              		}
	              		else {
	              			App.vent.trigger('application:dialog', {
	        	        		text: commonTranslations.commonUtils.exportOkMessage,
	        	        		name: "okDialog"
	                		});
	              		}
					}
					else {
						window.location = searchUrl;
					}
				})
				.fail(function(response) {
					$exportButton.removeAttr('disabled');
					$exportButtonParent.appendTo(exportButtonOrig);
              		App.vent.trigger('application:dialog', {
    	        		text: commonTranslations.commonUtils.exportErrorMessage,
    	        		name: "okDialog"
            		});
              	})
              	.always(function(data) {
              	});
			},
			
			urlEncodeForm: function(form) {
				if (_.isUndefined(form)) {
					form = $('form');
				}
				var formEncoded = '/asf!' + (form.is(':visible') ? 'on' : 'off');
				var formdata = Backbone.Syphon.serialize(form, {selectValues: true});
				for (var property in formdata) {
				    if (formdata.hasOwnProperty(property)) {
				    	try {
				    		var encodedValue = encodeURIComponent(formdata[property]);
				    		if (encodedValue.length > 0) {
				    			var value = '/' + property + '!' + encodedValue;
						    	formEncoded += value;
				    		}
				    	}
				    	catch(err){}
				    }
				}
				
				// sv += '~' + encodeURIComponent(data.search.search);
				
				return formEncoded;
			},
			
			/*
			 * Backbone.Syphon.InputReaders.register('select', function (el$) {
        		var select$;
        		try {
        			select$ = el$.data('config');
        		}
        		catch(err){}
        		var select$ = el$.select2().data();
	       		 var value = select$.val();
	       		 return value;
	       	 });
			 */
			
			urlDecodeForm: function(formEncodedData, form) {
				var response = false;
				if (_.isUndefined(form)) {
					form = $('form');
				}
				// Restore form here decodeURIComponent(split[2]);
				// http://localhost:8084/ecds-gui/#transactionSearch/groupIDA!4/number!12435/channel!A
				for (var i=0; i<formEncodedData.length; i++) {
					try {
						var item = formEncodedData[i].split('!');
						if (item.length === 2) {
							if(item[0]=='asf') {
								if(item[1]=='on') {
									response = true;
								}
								continue;
							}
							if(item[1] === 'null'){
								continue;
							}
							var formField = $("[name='"+item[0]+"']");
							var fieldType = formField.prop('type');
							if(fieldType  === 'select-one' || fieldType  === 'select-multiple') {
								try {
									var formFieldConfig = formField.data('config');
									if (!_.isUndefined(formFieldConfig) && !_.isUndefined(formFieldConfig.url)) {
										$.ajax({
											  context: {
												  field: formField,
												  item: item
											  },
											  url: formFieldConfig.url,
											  async: false
											}).done(function(data) {
												for (var property in data) {
												    if (data.hasOwnProperty(property)) {
												    	if (this.item[1] === property) {
												    		this.field.empty();
												    		this.field.append('<option value="'+property+'" selected="selected" >'+data[property]+' </option>').trigger("change");
														}
												    }
												}
											});
									}
									else {
										formField.val(decodeURIComponent(item[1])).trigger("change");
									}
								}
								catch(err){
									App.error(err);
								}
							}
							else {
								formField.val(decodeURIComponent(item[1]));
							}
						}
					}
					catch(err){}
				}
				return response;
			},
			
			/*
			 * Renders a Marionette template in a handlebars file to jquery Element
			 * templateReference: "AgentAccounts#viewButton"
			 * modelData: {label: "Hello", icon: "fa-money"}
			 * Note: This is not yet attached to the page DOM. use jq append, or html() 
			 * to retrieve html text
			 */
			getRenderedTemplate: function(templateReference, modelData) {
				var view = new Marionette.ItemView({
					template: templateReference,
					model: new Backbone.Model((_.isUndefined(modelData))? {} : modelData)
				});
				view.render();
				return view.$el;
			},
			
			getTemplateHtml: function(templateReference, modelData) {
				return this.getRenderedTemplate(templateReference, modelData).html();
			},
			
			/*
			 * options: title, text, callback
			 */
			showOkDialog: function(options) {
				App.vent.trigger('application:dialog', {
					title: options.title,				
					text: options.text,
					name: "okDialog",
					events: {
						"click .okButton": 
						function(event) {
							if (!_.isUndefined(options.callback)) {
								options.callback();
							}
							this.modal('hide');
						}
					}
				});
			},
			
			showViewDialog : function(model, viewClass) {
            	App.vent.trigger('application:dialog', {
            		name: "viewDialog",
            		view: viewClass,
            		params: {
            			model: model
            		}
            	});
			},
			
			/*
			 * NOTE:  violations = error.responseJSON.violations
			 * OR:	  violations = error (such that above exists)
			 */
			createErrorList: function(violations, context) {
				
				if (!_.isUndefined(violations.responseJSON) && !_.isUndefined(violations.responseJSON.violations)) {
					violations = error.responseJSON.violations
				}
				
				var errs = [];
				var uniqueErrors = [];
				var fieldMappings = false;
				
				for(var i=0; i<violations.length; i++) {
					var violation = violations[i];

					// Ensure that the errors reported are unique
					var errCode = violation.validations[0] + ":" + violation.correlationID;
					if ($.inArray(errCode, uniqueErrors) < 0) {
						var err = this.createViolationErrorMessage(violation, context);
						if (err != null) errs.push(err);
					}
				}
				
				return errs; 
			},
			
			createViolationErrorMessage: function(violation, context) {
				var errMsg = null;
				
				try {
					var errorId = violation.correlationID;
					var ref = violation.validations[0];
					var addMsg = (!_.isUndefined(violation.msgs[0]))? violation.msgs[0] : "";
					var msg = null;
					
					if ( (!_.isUndefined(context)) && (context != null) ) {
						var ctxRef = context +"." + ref;
						
						var result = ctxRef.split('.').reduce(function(obj, i) {
			        		return obj[i]
			        	}, violationMsgs);
						
			        	if (!_.isUndefined(result)) {
			        		msg = result;
			        	}
					}
					
					if (msg == null) {
						if (!_.isUndefined(violationMsgs[ref])) {

							msg = violationMsgs[ref];
						} else {

							var fullMsg = "";
							try {
								fullMsg = addMsg.substring(addMsg.indexOf("{", 1));
								fullMsg = JSON.parse(fullMsg);
								fullMsg['ref'] = ref;
								var moreInfo = "";
								if (!_.isUndefined(violationMsgs[ref])) {
									moreInfo = violationMsgs[ref];
								}
								fullMsg['moreInfo'] = moreInfo;
								fullMsg['errorId'] = errorId;
								msg = JSON.stringify(fullMsg, undefined, 2);
								return msg;
							} catch (ex) {

								msg = ref + " (" + addMsg + ")";
							}
						}	
					}
					
					if (!_.isUndefined(violation.field)) {
						msg += " (" +  violation.field + ")";
					}
					
					msg += violationMsgs.errorIdMsg;
					errMsg = this.renderHtml(msg, violation);
				} catch(err) {
					App.error(err)
				}
				
				return errMsg;
			},

			adjustDropdownDir: function(){
				var group=$(this).closest('.btn-group');
				var ul=group.find('ul');
				var otop=$(this).offset().top+$(this).height()-$(window).scrollTop();
				var ulh=ul.height() + 10;
				var obot=$(window).height()-$(this).height()-$(this).offset().top+$(window).scrollTop();
	
				if ((obot < ulh) && (otop > ulh))
					group.addClass('dropup');
				else
					group.removeClass('dropup');
			},
			
			i18nLookup: function(data) {
				var i18ntxt = App.translate(data);
				var translator = {
					translate: function(item) {
						/*
						 * First case, called with a single argument, lookup 
						 * the variable within the provided context;
						 */
						if (arguments.length === 1) {
							if (_.isUndefined(i18ntxt)) {
								return App.translate.apply(App, arguments);
							}
							else {
								var translation = i18ntxt[item];
								if (_.isUndefined(translation)) {
									return App.translate.apply(App, arguments);
								}
								else {
									return translation;
								}
							}
						}
						/*
						 * Case 2, multiple arguments, so just call the original translate method.
						 */
						else {
							App.translate.apply(App, arguments);
						}
					}
				};
				
				return translator;
			},
			
			/** 
			 *	Configure Select 2 control
			 */ 
        	configureSelect2Control: function(options) {
        		var selectOptions = {
    				el : options.jqElement,
    				url : options.url,
    				minLength: 0,
    				placeholderText: "",
    				key: null,
    				value: null,
					isHtml: null,
					currentId: null
        		};
            		
        		if (!_.isUndefined(options.minLength)) selectOptions.minLength = options.minLength;
        		if (!_.isUndefined(options.placeholderText)) selectOptions.placeholderText = options.placeholderText;
        		if (!_.isUndefined(options.key)) selectOptions.key = options.key;
        		if (!_.isUndefined(options.value)) selectOptions.value = options.value;
        		if (!_.isUndefined(options.isHtml)) selectOptions.isHtml = options.isHtml;
            		
        		return selectOptions.el.select2({
        			dataMap: {},
	          		ajax: {
	          			type: "GET",
	          		    url: selectOptions.url,
            		    dataType: 'json',
            		    contentType: "application/json",
	          		    delay: 250,
	          		    
	                    processResults: function (data) {
							if (selectOptions.isHtml)
							return {
								results: $.map(data, function (item, i) {
	                    			return {
	                    				id: (selectOptions.key == null)? item.id : item[selectOptions.key],
	                    				text: (selectOptions.value == null) ? 
										(item.name ) : item[selectOptions.value],
                                		item: item
	                                }
	                            })
							};
	                    	return {
	                    		results: $.map(data, function (item, i) {
	                    			return {
	                    				id: (selectOptions.key == null)? i : item[selectOptions.key],
	                    				text: (selectOptions.value == null)? item : item[selectOptions.value],
                                		item: item
	                                }
	                            })
	                        };
	                    }
	          		},
	          		minimumInputLength: selectOptions.minLength,
					allowClear: true, 
					placeholder: selectOptions.placeholderText,
					sorter: function(data) {
                        return data.sort(function (a, b) {
                        	if( !_.isUndefined(a) && !_.isUndefined(b) ){
	                        	aLower = String(a.text).toLowerCase();
	                        	bLower = String(b.text).toLowerCase();
	                            if (aLower > bLower) {
	                                return 1;
	                            }
	                            if (aLower < bLower) {
	                                return -1;
	                            }
                        	}
                            return 0;
                        });
                    },
					templateResult: function(data) {
						if (!selectOptions.isHtml) {
							return data.text;
						}

						return data.item ? data.item.name
								+ "<span class='align-right-type'>"
								+ data.item.type
								+ "</span>" : data.text;
					},
					escapeMarkup: function(m) {
						return m;
					},
					templateSelection: function(data) {
						if (!selectOptions.isHtml) {
							return data.text;
						} else {
							let keepDefault = true;
							let customData = null;
							if(data.selected) {
								if(!data.item && data.id) {
									$.ajax({
										type: "GET", 
										url: "api/areas/"+data.id, 
										async: false,
										cache: false
									}).done( function(content) {
										keepDefault = false;
										customData = content.name
										+ "<span class='align-right-type font-small-type margin-right-20'>"
										+ content.type
										+ "</span>";
									}).fail( function(xhr, status, msg) {
									});
								}
							}

							if (keepDefault)
							return data.item ? data.item.name
							+ "<span class='align-right-type font-small-type margin-right-20'>"
							+ data.item.type
							+ "</span>" : data.text;

							return customData;
						}
					}
        		});
			},			
            getFormData: function(formElement) {
            	var criteria = Backbone.Syphon.serialize(formElement);
            	var args = "";
            	
            	for (var key in criteria) {
					if ( criteria[key] != "" ) {
    					if (args != "") args += "&";
	    				args += key + "=" + encodeURIComponent(criteria[key]);
					}	
				}
				return {
					"criteria": criteria,
					"args":     args
				};
            },
            
            getContextPath : function() {
 			   return window.location.pathname.substring(0, window.location.pathname.indexOf("/",1));
            },
                        
		};
		
		return CommonUtils;
});
