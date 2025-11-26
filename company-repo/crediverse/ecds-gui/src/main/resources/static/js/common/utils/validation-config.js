/*
 * http://johnnycode.com/2014/03/27/using-jquery-validate-plugin-html5-data-attribute-rules/
 * (Tested, core)

    data-rule-required="true"
    data-rule-email="true"

(Untested, core, but should work)

    data-rule-url="true"
    data-rule-date="true"
    data-rule-dateISO="true"
    data-rule-number="true"
    data-rule-digits="true"
    data-rule-creditcard="true"
    data-rule-minlength="6"
    data-rule-maxlength="24"
    data-rule-rangelength="5,10"
    data-rule-min="5"
    data-rule-max="10"
    data-rule-range="5,10"
    data-rule-equalto="#password"
    data-rule-remote="custom-validatation-endpoint.aspx"

(Untested, additional, but should work)

    data-rule-accept=""
    data-rule-bankaccountNL="true"
    data-rule-bankorgiroaccountNL="true"
    data-rule-bic=""
    data-rule-cifES=""
    data-rule-creditcardtypes=""
    data-rule-currency=""
    data-rule-dateITA=""
    data-rule-dateNL=""
    data-rule-extension=""
    data-rule-giroaccountNL=""
    data-rule-iban=""
    data-rule-integer="true"
    data-rule-ipv4="true"
    data-rule-ipv6="true"
    data-rule-mobileNL=""
    data-rule-mobileUK=""
    data-rule-lettersonly="true"
    data-rule-nieES=""
    data-rule-nifES=""
    data-rule-nowhitespace="true"
    data-rule-pattern=""
    data-rule-phoneNL="true"
    data-rule-phoneUK="true"
    data-rule-phoneUS="true"
    data-rule-phonesUK="true"
    data-rule-postalcodeNL="true"
    data-rule-postcodeUK="true"
    data-rule-require_from_group=""
    data-rule-skip_or_fill_minimum=""
    data-rule-strippedminlength=""
    data-rule-time=""
    data-rule-time12h=""
    data-rule-url2=""
    data-rule-vinUS=""
    data-rule-zipcodeUS="true"
    data-rule-ziprange=""

 */

define(['jquery', 'underscore', "App", 'handlebars', 'i18n!common/violations'], 
	function($, _, App, Handlebars, violationMsgs) {

	// TODO: Remove (grr...)
	if ((!_.isUndefined(csUserLanguage)) && (csUserLanguage =="fr")) {
		$.extend( $.validator.messages, {
			required: "Ce champ est obligatoire.",
			remote: "Veuillez corriger ce champ.",
			email: "Veuillez fournir une adresse électronique valide.",
			url: "Veuillez fournir une adresse URL valide.",
			date: "Veuillez fournir une date valide.",
			dateISO: "Veuillez fournir une date valide (ISO).",
			number: "Veuillez fournir un numéro valide.",
			digits: "Veuillez fournir seulement des chiffres.",
			creditcard: "Veuillez fournir un numéro de carte de crédit valide.",
			equalTo: "Veuillez fournir encore la même valeur.",
			extension: "Veuillez fournir une valeur avec une extension valide.",
			maxlength: $.validator.format( "Veuillez fournir au plus {0} caractères." ),
			minlength: $.validator.format( "Veuillez fournir au moins {0} caractères." ),
			rangelength: $.validator.format( "Veuillez fournir une valeur qui contient entre {0} et {1} caractères." ),
			range: $.validator.format( "Veuillez fournir une valeur entre {0} et {1}." ),
			max: $.validator.format( "Veuillez fournir une valeur inférieure ou égale à {0}." ),
			min: $.validator.format( "Veuillez fournir une valeur supérieure ou égale à {0}." ),
			maxWords: $.validator.format( "Veuillez fournir au plus {0} mots." ),
			minWords: $.validator.format( "Veuillez fournir au moins {0} mots." ),
			rangeWords: $.validator.format( "Veuillez fournir entre {0} et {1} mots." ),
			letterswithbasicpunc: "Veuillez fournir seulement des lettres et des signes de ponctuation.",
			alphanumeric: "Veuillez fournir seulement des lettres, nombres, espaces et soulignages.",
			lettersonly: "Veuillez fournir seulement des lettres.",
			nowhitespace: "Veuillez ne pas inscrire d'espaces blancs.",
			ziprange: "Veuillez fournir un code postal entre 902xx-xxxx et 905-xx-xxxx.",
			integer: "Veuillez fournir un nombre non décimal qui est positif ou négatif.",
			vinUS: "Veuillez fournir un numéro d'identification du véhicule (VIN).",
			dateITA: "Veuillez fournir une date valide.",
			time: "Veuillez fournir une heure valide entre 00:00 et 23:59.",
			phoneUS: "Veuillez fournir un numéro de téléphone valide.",
			phoneUK: "Veuillez fournir un numéro de téléphone valide.",
			mobileUK: "Veuillez fournir un numéro de téléphone mobile valide.",
			strippedminlength: $.validator.format( "Veuillez fournir au moins {0} caractères." ),
			email2: "Veuillez fournir une adresse électronique valide.",
			url2: "Veuillez fournir une adresse URL valide.",
			creditcardtypes: "Veuillez fournir un numéro de carte de crédit valide.",
			ipv4: "Veuillez fournir une adresse IP v4 valide.",
			ipv6: "Veuillez fournir une adresse IP v6 valide.",
			require_from_group: "Veuillez fournir au moins {0} de ces champs.",
			nifES: "Veuillez fournir un numéro NIF valide.",
			nieES: "Veuillez fournir un numéro NIE valide.",
			cifES: "Veuillez fournir un numéro CIF valide.",
			postalCodeCA: "Veuillez fournir un code postal valide."
		} );
	}
	
 	$.validator.setDefaults({
	    highlight: function (element, errorClass, validClass) {
	    	if (!_.isUndefined(element) && !$(element).hasClass("novalidate")) {
		        if (element.type === "radio") {
		            this.findByName(element.name).addClass(errorClass).removeClass(validClass);
		        } else {
		            $(element).closest('.form-group').removeClass('has-success has-feedback').addClass('has-error has-feedback');
		        }
	    	}
	    },
	    unhighlight: function (element, errorClass, validClass) {
	    	if (!_.isUndefined(element) && !$(element).hasClass("novalidate")) {
		        if (element.type === "radio") {
		            this.findByName(element.name).removeClass(errorClass).addClass(validClass);
		        } else {
		            $(element).closest('.form-group').removeClass('has-error has-feedback').addClass('has-success has-feedback');
		        }
	    	}
	    },
	    errorElement: 'span',
	    errorClass: 'help-block',
	    errorPlacement: function(error, element) {
	    	if (!_.isUndefined(element)) {
	    		if (element.parent('.input-group').length) {
		            error.insertAfter(element.parent());
		        } else if (element.hasClass('select2-hidden-accessible')) {
					error.insertAfter(element.next('span'));
				} else {
		        	if ($(element).closest('.form-group').find('.CodeMirror').length > 0) {
		        		var codeMirrorElement = $(element).closest('.form-group').find('.CodeMirror');
		        		error.addClass("col-sm-9 col-sm-offset-3");
		        		error.insertAfter(codeMirrorElement);	
		        	} else {
		        		error.insertAfter(element);	
		        	}
		        }
	    	}
	    },
		
	});
 	
 	$.validator.prototype.resetForm = function() {
 	    if ( $.fn.resetForm ) {
 	        $(this.currentForm).resetForm();
 	    }
 	    this.submitted = {};
 	    this.lastElement = null;
 	    this.prepareForm();
 	    this.hideErrors();
 	    var elements = this.elements().removeData("previousValue").removeAttr("aria-invalid");
 	    
 	    $(this.currentForm).find(".form-group").each(function(i,obj) {
 	    	$(obj).removeClass('has-success has-feedback');
 	    	$(obj).removeClass('has-error has-feedback');
 	    	//$(obj).find('i.fa').remove();
 	    }); 	    
 	},
 	
 	

 	
 	/**
 	* Inject a custom error (can use with feedback from server. e.g. of use:
	*		validator.injectErrors([{
	*			element: $('#name'),
	*			message: 'Some error message'          
    *        }]);
 	*/
 	$.validator.prototype.injectErrors = function (errors) {
        for (var i = 0; i < errors.length; i++) {
            this.errorList.push({
                element: $(errors[i].element)[0],
                message: errors[i].message
            });
        }           
        this.showErrors();
    },
    
    /**
     * violationMsgs -> "code" : "value"
     */
 	$.validator.prototype.injectServerViolations = function (violationErrors, form, serverFieldMappings) {
    	var notFound = [];
    	try {
            for (var i = 0; i < violationErrors.length; i++) {
            	//field is not defined for general errors
            	if (! _.isUndefined(violationErrors[i].field)) {
                	var field = violationErrors[i].field;
                	var msg = null;
                	if (!_.isUndefined(violationErrors[i].validations[0])) {
                		var ref = violationErrors[i].validations[0];
                		var msg = null;
                		
                		// resultCode -> msg -> unknown Error
                		if (!_.isUndefined(violationMsgs[ref])) {
                			msg = violationMsgs[ref];
                		} else if (!_.isUndefined(violationErrors[i].msgs[0])) {
                			msg = violationErrors[i].msgs[0];
                		} else {
                			msg = violationMsgs[ref]["unknown"];
                		}
                		
                		var compiled = Handlebars.compile(msg);
                		var context = violationErrors[i].parameters;
                		msg = compiled(context);
                	} else {
                		msg = violationErrors[i].msgs[0];
                	}

                	var $form = $(form);
                	if ((! _.isUndefined(serverFieldMappings)) && (!_.isUndefined(serverFieldMappings[field]))) {
                		field = serverFieldMappings[field];
                	}
                	
                	try {
                		var findField = field;
                		if (field.indexOf("." !== -1)) {
                			var fields = field.split('.');
                			findField = fields[0];
                			for(var j = 1; j < fields.length; j++) {
                				findField += "[" + fields[j] + "]";
                			}
                		}
                		
                		
                		
                		var $element = $form.find('[id^="' + findField + '"]');
                		if ($element.length <= 0) {
                			$element = $form.find('[name^="' + findField + '"]');
                		}
                    	if ($element.length < 1) {
                    		notFound.push({field: field, msg: msg, correlationID: violationErrors[i].correlationID});
                    	}
                    	else {
							// Find LAST help block, add error there
							var $el = Array.from($element).reverse().find(function (el) { return el.classList.contains('help-block'); });

							// Fallback to first element (previous behaviour)
							if (!$el) $el = $element[0];

                    		this.errorList.push({
                                element: $el,
                                message: msg
                            });
                    	}
                	} catch(err) {
                		notFound.push({field: field, msg: msg, correlationID: violationErrors[i].correlationID});
                	}                	
            	}

            }
            
            this.showErrors();      
    	} catch(err) {
    		if (console) console.error("injectServerViolations error: " + err);
    	}
    	return notFound;
    }
    
 	/*
 	 * Custom validators below here
 	 */
 	
    /*
     * Allow a value of 0-100 with a single decimal digit after the point.
     */
    $.validator.addMethod("places", function (value, element, params) {
       return true;
 	}, "Invalid places.");
    
    $.validator.addMethod("numeric", function (value, element, params) {
    	return true;
  	}, "Invalid numeric.");
    
	$.validator.addMethod("negative", function (value, element, params) {
    	return true;
  	}, "Invalid negative indicator.");
	
	$.validator.addMethod( "msisdn", function( value, element ) {
		if ( this.optional( element ) ) {
			return true;
		}
		if ( !( /[0-9() -+]+/.test( value ) ) ) {
			return false;
		}

		return true;
	}, "Please specify a valid MSISDN" );
	
 	// Time validation for time format (HH:MM:SS)
 	$.validator.addMethod("time24", function(value, element) {
 		if (value.length === 0) return true;
 		if (value === "__:__:__") return true;
 	    if (!/^\d{2}:\d{2}:\d{2}$/.test(value)) return false;
 	    var parts = value.split(':');
 	    if (parts[0] > 23 || parts[1] > 59 || parts[2] > 59) return false;
 	    return true;
 	}, violationMsgs.invalidTimeFormat);
 	
 	
 	$.validator.addMethod("time24HM", function(value, element) {
 		if (value.length === 0) return true;
 		if (value === "__:__") return true;
 	    if (!/^\d{2}:\d{2}$/.test(value)) return false;
 	    var parts = value.split(':');
 	    if (parts[0] > 23 || parts[1] > 59) return false;
 	    return true;
 	}, violationMsgs.invalidTimeHMFormat);

 	// This is literally the jquery validator function doing success first, then fail
 	$.validator.prototype.defaultShowErrors = function() {
		var i, elements, error;
		
		if ( this.settings.success ) {
			for ( i = 0; this.successList[ i ]; i++ ) {
				this.showLabel( this.successList[ i ] );
			}
		}
		if ( this.settings.unhighlight ) {
			for ( i = 0, elements = this.validElements(); elements[ i ]; i++ ) {
				this.settings.unhighlight.call( this, elements[ i ], this.settings.errorClass, this.settings.validClass );
			}
		}

		for ( i = 0; this.errorList[ i ]; i++ ) {
			error = this.errorList[ i ];
			if ( this.settings.highlight ) {
				this.settings.highlight.call( this, error.element, this.settings.errorClass, this.settings.validClass );
			}
			this.showLabel( error.element, error.message );
		}
		if ( this.errorList.length ) {
			this.toShow = this.toShow.add( this.containers );
		}

		this.toHide = this.toHide.not( this.toShow );
		this.hideErrors();
		this.addWrapper( this.toShow ).show();
	};
});

