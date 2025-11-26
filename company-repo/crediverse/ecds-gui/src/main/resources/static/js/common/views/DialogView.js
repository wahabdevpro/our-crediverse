define([ 'App', 'jquery', 'underscore', 'backbone', 'marionette'],
	function (App, $, _, BackBone, Marionette) {
    	var DialogView = Marionette.ItemView.extend({
	        template:'Dialog#commondialog',

	        initialize: function() {
	        	App.vent.on('application:dialog', this.processDialog, this);
	        },
	        setTitle: function(dialogName, title) {
	        	if (!_.isUndefined(title)) {
        			$('#'+dialogName+' .modal-title').html(title);
        			$('#'+dialogName+' .modal-header').show();	        	
        		}
	        },
	        setText: function(dialogName, text) {
	        	if (!_.isUndefined(text)) {
    				$('#'+dialogName+' .modal-text').html(text);
    			}
	        },
	        attachEvents: function(element, events) {
	        	_.each(events, function(value, key, list) {
	        		var info = key.split(' ');
	        		var eventname = info.shift();
	        		var item = $(element);
	        		item.on(eventname, info.join(' '), $.proxy(value, item));
	        	});
	        },
	        hide: function() {
	        	$('.modal').delay(1000).fadeOut(450);
	        	setTimeout(function(){$('.modal').modal('hide')}, 1200);
	        },
	        configureEvents: function(element, events) {
	        	switch (element) {
	        		case "defaultDialog":
	        			this.attachEvents('#defaultDialog', events);
	        			break;
	        		case "okDialog":
	        			if (!_.isUndefined(events)) {
	        				this.attachEvents('#okDialog', events);
	        			}
	        			break;
	        		case "errorDialog":
	        			if (!_.isUndefined(events)) {
	        				this.attachEvents('#okDialog', events);
	        			}
	        			break;
	        		case "yesnoDialog":
	        			if (!_.isUndefined(events)) {
	        				this.attachEvents('#yesnoDialog', events);
	        			}
	        			break;
					case "quickDeactivateDialog":
						if (!_.isUndefined(events)) {
							this.attachEvents('#quickDeactivateDialog', events);
						}
						break;
	        		case "deleteDialog":
	        			if (!_.isUndefined(events)) {
	        				this.attachEvents('#deleteDialog', events);
	        			}
	        			break;
	        		case "statusDialog":
	        			if (!_.isUndefined(events)) {
	        				this.attachEvents('#statusDialog', events);
	        			}
	        			break;
	    			default:
	    				if (!_.isUndefined(events)) {
	    					this.attachEvents('#'+element, events);
	            		}
	    		}
	        },
	        processDialog: function(options) {
	        	var self = this;
	        	var modalOptions = {
        			keyboard: false,
	        		backdrop: 'static',
	        		class: 'defaultDialog',
	        		reset: false
	        	};
	        	var dialogName = "defaultDialog";
	        	if (arguments.length >= 1) {
	        		if (!_.isUndefined(options.model)) this.model = options.model;
	        		if (!_.isUndefined(options.text)) modalOptions.text = options.text;
	        		if (!_.isUndefined(options.name)) dialogName = options.name;
	        		$.extend(modalOptions, options);
	        		
	        		if (_.isUndefined(options.title)) {
	        			$('#'+dialogName+' .modal-header').hide();
	        		}
	        		else {
	        			$('#'+dialogName+' .modal-title').html(options.title);
	        			$('#'+dialogName+' .modal-header').show();
	        		}
	        		switch (dialogName) {
		        		case "defaultDialog":
		        			this.setText(dialogName, options.text);
		        			modalOptions.keyboard = true;
		        			modalOptions.backdrop = true;
		        			break;
		        		case "okDialog":
		        			if (!_.isUndefined(options.title)) {
		        				this.setTitle(dialogName, options.title);
		        			}
		        			this.setText(dialogName, options.text);
		        			break;
		        		case "errorDialog":
		        			this.setText(dialogName, options.text);
		        			break;
		        		case "viewDialog":
		        			var vw = options.view;
		        			var params = options.params;
		        			if (_.isUndefined(params)) params = {};
		        			if (_.isUndefined(params.model)) {
		        				params.model = new BackBone.Model();
		        			}
		        			params.model.set('dialogTitle', modalOptions.title);
		        			var currentView = new vw(params);
		        			$('#'+dialogName+' .modal-dialog').html(currentView.render().el);
		        			$('#'+dialogName).on('shown.bs.modal', function () {
			        		       $(this).find('.modal-content').addClass(modalOptions.class);
			        		       var form = $(this).find('form');
			        		       if (!_.isUndefined(options.params) && !_.isUndefined(options.params.model) && !_.isUndefined(options.params.model.bind)) {
			        		    	   options.params.model.bind.call(options.params.model, form);
			        		    	   //options.params.model.form = $('#'+dialogName).find('form');
			        		       }
			        		});
		        			break;
						case "warningDialog":
							var vw = options.view;
							var params = options.params;
							if (_.isUndefined(params)) params = {};
							if (_.isUndefined(params.model)) {
								params.model = new BackBone.Model();
							}
							params.model.set('dialogTitle', modalOptions.title);
							var currentView = new vw(params);
							$('#'+dialogName+' .modal-dialog').html(currentView.render().el);
							break;
		        		case "yesnoDialog":
		        			$('#'+dialogName+' .yesButton').show();
		        			$('#'+dialogName+' .noButton').show();
		        			$('#'+dialogName+' .noButton').text(App.i18ntxt.enums.yesNo.no);
		        			this.setText(dialogName, options.text);
		        			break;
		        		case "deleteDialog":
		        			this.setText(dialogName, options.text);
		        			$('#'+dialogName+' .deleteErrorPanel').addClass("hide");
		        			$('#'+dialogName+' .modal-text').removeClass("hide");
		        			$('#'+dialogName+' .actionButton').removeClass("disabled");
		        			$('#'+dialogName+' .actionButton').removeClass("hide");
		        			$('#'+dialogName+' .modal-title').removeClass("deleteErrorTitle");
		        			$('#'+dialogName+' .cancelButton').text(App.i18ntxt.global.deleteCancel);
		        			$('#'+dialogName+' .actionButton').text(options.actionBtnText);
		        			
		        			$('#'+dialogName+' .actionButton').removeClass("btn-danger");
		        			$('#'+dialogName+' .actionButton').removeClass("btn-warning");
		        			$('#'+dialogName+' .actionButton').addClass(options.actionBtnClass);
		        			break;
		        		case "progressDialog":
		        			var progressbar = $('#progressBar .progress-bar');
		        			progressbar.css(
		                            'width',
		                            '0%'
		                        );
		        		case "statusDialog":
		        			this.setText(dialogName, options.text);
		        			break;
	        			default:
	        				modalOptions.reset = true;
	        		}
	        		if (modalOptions.reset) {
	        			var form = $('#'+dialogName).find('form');
	        			if (form.length > 0) form[0].reset();
	        		}
	        		$('#'+dialogName).modal(modalOptions);
	        		this.configureEvents(dialogName, options.events);
	        		if (typeof options.init === 'function') {
	        			options.init(this);
	        		}
	        		self.trigger("initialize");
	        		
	        		if (options.events && options.events.init)
	        			this.on("init", options.events.init);

	        		var formData = options.model;
	        		if (!_.isUndefined(formData)) {
	        			formData = options.model.attributes;
	        		}
	        		else if (!_.isUndefined(options.data)) {
	        			formData = options.data;
	        		}
	        		
	        		for(var field in formData) {
	        		    var value = formData[field];
	        		    var item = $('#'+dialogName).find('[name="' + field+'"]');
	        		    if (item.length > 0) {
	        		    	var itemType = item.prop('type').toLowerCase();
	        		    	if (itemType != 'radio' && itemType != 'checkbox') {
	        		    		item.val(value);
	        		    	}
	        		    	else {
	        		    		item.filter('[value="'+value+'"]').prop('checked', true);
	        		    	}
	        		    }
	        		}

	        		$('#'+dialogName).on('shown.bs.modal', function () {
	        			 
	        			// No Decimal Points (default for numbers, not currency)
	        			$('.autonum').each(function(index){
	        				try {
	        					var item$ = $(this)
	        					
	        					var options = {};
	        					if (item$.hasClass("currency")) {
	        						if (!_.isUndefined( $(this).attr("data-digits") )) {
	        							options["mDec"] = $(this).attr("data-digits");
	        						} else {
	        							options["mDec"] = App.i18ntxt.numberFormat.fractionalDigits;	
	        						}
	        					}
//	        					if (item$.hasClass("positive")) {
//	        						options["vMin"] = 0;
//	        						item$.on("keyup", function(ev) {
//	        							var value = $(this).autoNumeric('get');
//	        							if ($.isNumeric(value) && (value < 0)) {
//	        								$(this).autoNumeric('set', Math.abs(value));
//	        							}
//	        						});
//	        					}
	        					
	        					item$.autoNumeric('init', options);
//	        					item$.autoNumeric('set', item$.val());
	        				}
	        				catch(err) {
	        					App.error(err);
	        				}
	        			});
	        			
	        		});
	        	}
	        	
	        	$('#'+dialogName).on('hidden.bs.modal', function() {
	        		$('#'+dialogName).off();
					let dialog = document.querySelector("#"+dialogName+" .modal-dialog");
					if(dialog) {
						dialog.style.removeProperty("width");
					}
	        		if (typeof options.hide === 'function') {
	        			var data = null;
	        			if (!_.isUndefined(options.model)) {
	        				data = options.model;
	        			}
	        			options.hide(data);
	        		}
	        	});	  
	        	
	        	$('#'+dialogName).on('submit', function() {
					return false;
				});
	        }
	    });
    	return DialogView;
});
