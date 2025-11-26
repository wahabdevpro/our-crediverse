var MAX_LANGUAGES = 4;

addErrorAfter = function(compid, msg) 
{
	var span = "<span id='msg_" + compid + "' class='error_message'>" + msg + "</span>";
	var label = "<label class='error_message' for='name' generated='true' style='height:15px;'>This field is required.</label>";
	$("#"+compid + ":not(:has(.error_message))").append(label);
};

showErrorMessage = function(fieldid, msg, formid)
{
	var $element = null;
	if (typeof formid !== 'undefined') {
		$element = $("#" + formid).find("#" + fieldid + "_error");
	} else {
		$element =$("#" + fieldid + "_error");
	}
	$element.parent("div").addClass("has-error");
	$element.html(msg);
	$element.removeClass("hide");
};

hideErrorMessage = function(compid, formid)
{
	var $element =$("#" + compid + "_error"); 
	if (typeof formid !== 'undefined') {
		$element = $("#" + formid).find("#" + compid + "_error");
	}
	$element.parent("div").removeClass("has-error");
	$element.html("");
	$element.addClass("hide");
};

hideAllErrorMessage = function(compid)
{
	$("[id$='_error']").parent("div").removeClass("has-error");
	$("[id$='_error']").parent("div").addClass("hide");
};

validateLength = function(compid, len, msg) {
	var dsp = msg || "Too Long";
	if ($("#" + compid).val().length < len) {
		showErrorMessage(compid, dsp);
		return true;
	} else {
		hideErrorMessage(compid);
	}
	return false;
};

getElementReference = function(compid, formid) {
	var $element =  null;
	if (typeof formid !== 'undefined' && formid != null)
		$element = $("#" + formid).find("#" + compid);
	else
		$element = $("#" + compid);
	return $element;
};

validateLengthMinMax = function(compid, lengthMin, lengthMax, msg, formid) {
	try {
		var dsp = msg || "Too short";
		var $element =  getElementReference(compid, (typeof formid === "undefined")? null : formid);
		var length = $element.val().length;
		
		if (length < lengthMin || length > lengthMax) {
			showErrorMessage(compid, dsp, formid);
			return true;
		} else {
			hideErrorMessage(compid, formid);
		}
		
	} catch(err) {
		console.error(err + " problem with " + compid);
	}
	return false;
};

validateNoPrimaryKeyDuplicate = function(compid, startingID, idList, msg, formid) {
	try {
		if ((typeof idList === "undefined" || idList == null) || (typeof startingID === "undefined" || startingID == null))
			return false;
		
		var arr = $.parseJSON(idList);
		var dsp = msg || "Duplicate Key found";
		var $element =  getElementReference(compid, (typeof formid === "undefined")? null : formid);
		var value = $element.val();

		if (value != startingID){
			var found = false;
			if (arr != null) {
				for(var i=0; i<arr.length; i++) {
					if (arr[i] == value) {
						found = true;
						break;
					}
				}
			}
			if (found) {
				showErrorMessage(compid, dsp, formid);
				return true;
			}
		}
	} catch(err) {
		console.error("validateNoPrimaryKeyDuplicate: " + err);
	}
	return false;
};

validateIPhrase = function(compid, lengthMin, lengthMax, msg, accordianPanelId, formid) {
	var isError = false;
	try {
		if (typeof formid !== 'undefined') {
			$("#" + formid).find("input[id^="+ compid + "]").each(function() {
				if ( $(this).val().length == 0) {
					isError = true;
				}
			});
		}
	} catch(err) {
		if (console) console.error("validateIPhrase: " + err);
	}
	return isError;
};

validateIText = function(compid, lengthMin, lengthMax, msg, accordianPanelId, formid) {
	var isError = false;
	try {
		for(var i=0; i<MAX_LANGUAGES; i++) {
			var fullCompID = compid + "_" + i;
			var $element =  null;
			if (typeof formid !== 'undefined')
				$element = $("#" + formid).find("#" + fullCompID);
			else
				$element = $("#" + fullCompID);
			
			if ($element.length > 0) {
				var length = $element.val().length;
				if (length < lengthMin || length > lengthMax) {
					showErrorMessage(fullCompID, msg, formid);
					if (typeof accordianPanelId !== 'undefined' && accordianPanelId != null) {
						$("#" + accordianPanelId).collapse("show");
					}
//						$("#" + accordianPanelId).addClass("in");
					isError = true;
				} else {
					hideErrorMessage(fullCompID, formid);
				}
			}
		}
	} catch(err) {
		console.error(err + " problem with " + compid);
	}
	return isError;
};

validateRegexExpression = function(compid, lengthMax) {
	var length = $("#" + compid).val().length;
	if (length == 0) {
		showErrorMessage(compid, "Regular Expression required");
		return true;
	} else if (length > lengthMax) {
		showErrorMessage(compid, "Expression too long");
		return true;
	} else {
		try {
			new RegExp( $("#" + compid).val());
			hideErrorMessage(compid);
		} catch(ex) {
			showErrorMessage(compid, "Invalid Regular Expression");
			return true;
		}
	}
	return false;
};

validateTimeField = function(compid, msg) {
	var dsp = msg || "e.g. 12:59";
	var re = new RegExp("([01]?[0-9]|2[0-3]):[0-5][0-9]");
	if (! $("#" + compid).val().match(re)) {
		showErrorMessage(compid, dsp);
		return true;
	} else {
		hideErrorMessage(compid);
	}
	return false;
};

validateRegex = function(compid, regex, msg)
{
	var dsp = msg || "Invalid pattern";
	if (!regex.test($("#" + compid).val()))
	{
		showErrorMessage(compid, dsp);
		return true;
	} else {
		hideErrorMessage(compid);
	}
	return false;
};

isInt = function(value) {
	   return !isNaN(value) && parseInt(value) == value;
};

validateNumber = function(compid, msg, formID) {
	var $element =  null;
	try {
		if (typeof formID !== 'undefined')
			$element = $("#" + formID).find("#" + compid);
		else
			$element = $("#" + compid);
		
		if (!isInt($element.val())) {
			showErrorMessage(compid, msg, formID);
			return true;
		} else {
			hideErrorMessage(compid, formID);
		}
	} catch(err) {
		console.error(err);
	}

	return false;
};

function isFloat(num) {
	var notANumber = isNaN(parseFloat(num));
	if (notANumber)
		return false;
	else if (num.length==0 || !(parseFloat(num + "1")))
		return false;
	else if (num.match(/^\d+$/) || num.match(/^\d+\.\d+$/))
		return true;
	else
		return false;
}

function isInteger(n) {
    return n === +n && n === (n|0);
}

function isNumber(value) {
    if ((undefined === value) || (null === value)) {
        return false;
    }
    if (typeof value == 'number') {
        return true;
    }
    return !isNaN(value - 0);
}

validateCurrency = function(compid, msg, formID) {
	var $element =  null;
	try {
		if (typeof formID !== 'undefined')
			$element = $("#" + formID).find("#" + compid);
		else
			$element = $("#" + compid);
		
		if (! isFloat($element.val()))  {
			showErrorMessage(compid, msg, formID);
			return true;
		} else {
			hideErrorMessage(compid, formID);
		}
	} catch(err) {
		console.error(err);
	}

	return false;
};

validateNotEmpty = function(compid, msg) {
	var dsp = msg || "Value required";
	if ($("#" + compid).val().length == 0) {
		showErrorMessage(compid, dsp);
		return true;
	} else {
		hideErrorMessage(compid);
	}
	return false;
};

validateSame = function(compid1, compid2, msg) {
	if ($("#" + compid1).val() != $("#" + compid2).val()) {
		showErrorMessage(compid1, msg);
		showErrorMessage(compid2, msg);
		return true;
	} else {
		hideErrorMessage(compid1);
		hideErrorMessage(compid2);
	}
	return false;
};

/**
 * Ignores numbers if not number.
 * true = failure
 */
validateMinMax = function(minNumberId, maxNumerId, msg) {
	var valueMin = $("#" + minNumberId).val();
	var valueMax = $("#" + maxNumerId).val();
	
	if (isInt(valueMin) && isInt(valueMin)) {
		var imin = parseInt(valueMin);
		var imax = parseInt(valueMax);
		if (imin > imax) {
			showErrorMessage(maxNumerId, msg);
			return true;
		}
	}
	return false;
};

validateValueLowerEqualThan = function(variableId, amount, msg) {
	var value = $("#" + variableId).val();
	if (isInt(value)) {
		var ivalue = parseInt(value);
		if (ivalue > amount) {
			showErrorMessage(variableId, msg);
			return true;
		}
	}
	return false;
};

numericFieldChecker = function() {
	$(".numericfields").keydown(function (e) {
	    // Allow: backspace, delete, tab, escape, enter and .
	    if ($.inArray(e.keyCode, [46, 8, 9, 27, 13, 110, 190]) !== -1 ||
	         // Allow: Ctrl+A Ctrl+A Ctrl+C
	        ((e.keyCode == 65 || e.keyCode == 86 || e.keyCode ==67) && e.ctrlKey === true) || 
	         // Allow: home, end, left, right
	        (e.keyCode >= 35 && e.keyCode <= 39)) {
	             // let it happen, don't do anything
	             return;
	    }

	    // Ensure that it is a number and stop the keypress
	    if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57)) && (e.keyCode < 96 || e.keyCode > 105)) {
	        e.preventDefault();
	    }
	});

	$(".numericfields").keyup(function(e)
	{
	    if (/\D/g.test(this.value))
	    {
	        // Filter non-digits from input value.
	        this.value = this.value.replace(/\D/g, '');
	    }
	});
	
	$(".currencyfields").keydown(function (e) {
	    // Allow: backspace, delete, tab, escape, enter and .
	    if ($.inArray(e.keyCode, [46, 8, 9, 27, 13, 110, 190]) !== -1 ||
	         // Allow: Ctrl+A Ctrl+A Ctrl+C
	        ((e.keyCode == 65 || e.keyCode == 86 || e.keyCode ==67) && e.ctrlKey === true) || 
	         // Allow: home, end, left, right
	        (e.keyCode >= 35 && e.keyCode <= 39)) {
	             // let it happen, don't do anything
	             return;
	    }

	    // Ensure that it is a number and stop the keypress
	    if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57)) && (e.keyCode < 96 || e.keyCode > 105)) {
	        e.preventDefault();
	    }
	});
		
};

validateHostName = function(elemId)
{
	var hostError = validateLengthMinMax(elemId, 1, 63, "Hostname between 1 to 63 characters required");
	if (!hostError)
	{
		var validLongHostRegex = /^(([a-zA-Z0-9_]|[a-zA-Z0-9_][a-zA-Z0-9\-_]*[a-zA-Z0-9_])\.)*([A-Za-z0-9_]|[A-Za-z0-9_][A-Za-z0-9\-_]*[A-Za-z0-9_])$/g;
		hostError = validateRegex(elemId, validLongHostRegex, "Invalid Host Name");
		if (!hostError)
		{
			var validShortHostRegex = /^(([a-zA-Z0-9_]|[a-zA-Z0-9_][a-zA-Z0-9\-_]*[a-zA-Z0-9_]))$/g;
			hostError = validateRegex(elemId, validShortHostRegex, "Short hostname required (i.e. no domain)");
		}
	}
	
	return hostError;
};


refreshSelectPickers = function() {
    try {
    	$(".selectpicker").selectpicker();
    } catch(err) {
    	alert(err);
    }	
};

(function ($) {
	 
    $.fn.serialize = function (options) {
        return $.param(this.serializeArray(options));
    };

    $.fn.serializeArray = function (options) {
        var o = $.extend({
        checkboxesAsBools: false
    }, options || {});

    var rselectTextarea = /select|textarea/i;
    var rinput = /text|hidden|password|search/i;

    return this.map(function () {
        return this.elements ? $.makeArray(this.elements) : this;
    })
    .filter(function () {
        return this.name && !this.disabled &&
            (this.checked
            || (o.checkboxesAsBools && this.type === 'checkbox')
            || rselectTextarea.test(this.nodeName)
            || rinput.test(this.type));
        })
        .map(function (i, elem) {
            var val = $(this).val();
            return val == null ?
            null :
            $.isArray(val) ?
            $.map(val, function (val, i) {
                return { name: elem.name, value: val };
            }) :
            {
                name: elem.name,
                value: (o.checkboxesAsBools && this.type === 'checkbox') ? //moar ternaries!
                       (this.checked ? 'true' : 'false') :
                       val
            };
        }).get();
    };

})(jQuery);