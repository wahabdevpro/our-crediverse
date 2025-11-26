var Utils = {

	numericFieldChecker : function() {
		$(".numericfields").on("keydown", function(e) {
			// Allow: backspace, delete, tab, escape, enter and .
			if ($.inArray(e.keyCode, [ 46, 8, 9, 27, 13, 110, 190,
					107 ]) !== -1
					||
					// Allow: Ctrl+A Ctrl+A Ctrl+C
					((e.keyCode == 65 || e.keyCode == 86 || e.keyCode == 67) && e.ctrlKey === true)
					||
					// Allow: home, end, left, right
					(e.keyCode >= 35 && e.keyCode <= 39)) {
				// let it happen, don't do anything
				return;
			}

			// Ensure that it is a number and stop the keypress
			if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57))
					&& (e.keyCode < 96 || e.keyCode > 105)) {
				e.preventDefault();
			}
		});

		$(".numericfields").on("keyup", function(e) {
			if (/[^+0-9]+/g.test(this.value)) {
				// Filter non-digits from input value.
				this.value = this.value.replace(/[^+0-9]+/g, '');
			}
		});

	},
	
	currencyFieldChecker : function() {
		$(".currency").on("keydown", function(e) {
			// Allow: backspace, delete, tab, escape, enter and .
			if ($.inArray(e.keyCode, [ 190, 46, 8, 9, 27, 13, 110, 107 ]) !== -1
					||
					// Allow: Ctrl+A Ctrl+A Ctrl+C
					((e.keyCode == 65 || e.keyCode == 86 || e.keyCode == 67) && e.ctrlKey === true)
					||
					// Allow: home, end, left, right
					(e.keyCode >= 35 && e.keyCode <= 39)) {
				// let it happen, don't do anything
				return;
			}

			// Ensure that it is a number and stop the keypress
			if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57))
					&& (e.keyCode < 96 || e.keyCode > 105)) {
				e.preventDefault();
			}
		});

		$(".currency").on("keyup", function(e) {
			if (/[^+0-9\.]+/g.test(this.value)) {
				// Filter non-digits from input value.
				this.value = this.value.replace(/[^+0-9\.]+/g, '');
			}
		});

	},
	
	mutiFieldChecker : function(id) {
		$("#" + id).on("keydown", function(e) {
			
			if ($("#" + id).hasClass("currency") && (e.keyCode == 110)) {
				return;
			}
			
			// Allow: backspace, delete, tab, escape, enter and .
			if ($.inArray(e.keyCode, [ 46, 8, 9, 27, 13, 190,
					107 ]) !== -1
					||
					// Allow: Ctrl+A Ctrl+A Ctrl+C
					((e.keyCode == 65 || e.keyCode == 86 || e.keyCode == 67) && e.ctrlKey === true)
					||
					// Allow: home, end, left, right
					(e.keyCode >= 35 && e.keyCode <= 39)) {
				// let it happen, don't do anything
				return;
			}

			// Ensure that it is a number and stop the keypress
			if ((e.shiftKey || (e.keyCode < 48 || e.keyCode > 57))
					&& (e.keyCode < 96 || e.keyCode > 105)) {
				e.preventDefault();
			}
			
			

		});
	},
	
	currencyFieldChecker2 : function() {
		$(".currency").keydown ( function(e) {
		    var i,exceptions=[8,46,37,39,13,9]; // backspace, delete, arrowleft & right, enter, tab
		    var isException=false;
		    var isDot=(190==e.keyCode); // dot
		    var k=String.fromCharCode(e.keyCode);

		    for(i=0;i<exceptions.length;i++)
		    if(exceptions[i]==e.keyCode)
		    isException=true;
		    
		    if(isNaN(k) && (!isException) && (!isDot))
		        return false;
		    else {
		        var p=new String($(this).val() + k).indexOf(".");
		        if((p<currency.value.length-2 || isDot) && p>-1 && (!isException))
		            return false;
		        else if(currency.value.length>=15 && (!isException))
		            return false;
		    }
		});
	},
	
	configureToolTip : function(element, placement) {
		$(element).tooltip({
			'placement' : placement,
			'html' : true,
			'delay' : {
				show : 1000,
				hide : 100
			}
		});
	},
	
	enclodeField : function(value) {
		try {
			return encodeURIComponent(value);
		} catch (err) {
			return escape(value);
		}
	},

	log : function(msg) {
		try {
			console.error(msg);
		} catch (err) {
			alert(err);
		}
	},
	
	logError : function(methodName, error) {
		this.log(methodName + " : " + error);
	},
	
	refreshSelectPickers : function() {
		try {
			$(".selectpicker").selectpicker();
		} catch (err) {
		}
	},
	
	refreshPlaceholders : function() {
		try {
			if (/MSIE\s([\d.]+)/.test(navigator.userAgent)) {
				// Get the IE version. This will be 6 for IE6, 7 for IE7, etc...
				version = new Number(RegExp.$1);
				if (version < 9) {
					$('input, textarea').placeholder();
				}
			}
		} catch (err) {
			console.error("refreshPlaceholders: " + err);
		}
	},

	removeSpaces : function(value) {
		try {
			value = value.replace(/\s/g, '');
		} catch (err) {
		}
		return value;
	},
	
	createDivHtml : function(id, content) {
		return "<div id='" + id + "'>" + content + "</div>";
	},
	
	updateDivContent : function(id, content) {
		$("#" + id).html(content);
	},
	
	stringFormat : function(msg, parms) {
		var result = msg;
		try {
			if (typeof parms !== "undefined" && parms != null) {
				for(var i=0; i<parms.length; i++) {
					result = result.replace("{"+i+"}", parms[i]);
				}
			}
		} catch(err) {
			console.error("localizeStringFormat: " + err);
		}
		return result;
	}
	
};

var createAlert = function(divToAppendTo, alertType, message) {
	if (alertType == "error")
		alertType = "danger";
	var html = [];
	alertCounter += 1;
	html[html.length] = '<div class="alert alert-' + alertType
			+ '" style="margin: 2px 2px 2px 2px !important;">';
	html[html.length] = '<a id="alert_'
			+ alertCounter
			+ '" href="#" class="close" data-dismiss="alert"><span class="glyphicon glyphicon-remove"></span></a>';
	if (alertType == 'success') {
		html[html.length] = '<strong>Success!</strong> ';
	} else if (alertType == 'danger') {
		html[html.length] = '<strong>Error!</strong> ';
	} else {
		html[html.length] = '<strong>Info!</strong> ';
	}
	html[html.length] = message;
	html[html.length] = '</div>';
	var txt = html.join("");
	$(txt).insertAfter("#" + divToAppendTo);
};

var createLoaderHtml = function() {
	var html = [];
	html[html.length] = "<img src='/img/load.gif' />";
	html[html.length] = "<span>loading...</span>";
	return html.join("");
};



