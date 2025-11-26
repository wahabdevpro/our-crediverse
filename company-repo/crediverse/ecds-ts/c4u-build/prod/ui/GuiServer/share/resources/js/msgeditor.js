$.fn.convertLineBreaks = function() {
	this.each(function() {
		$(this).on("keypress", function(e) {
			var br, range, selection, textNode;
			if (e.keyCode === 13) {
				e.preventDefault();
				if (window.getSelection) {
					selection = window.getSelection();
					range = selection.getRangeAt(0);
					br = document.createElement("br");
					textNode = document.createTextNode("\u00a0");
					range.deleteContents();
					range.insertNode(br);
					range.collapse(false);
					range.insertNode(textNode);
					range.selectNodeContents(textNode);
					selection.removeAllRanges();
					selection.addRange(range);
					return false;
				}
			}
		});
	});
};

(function($) {
	'use strict';
	/**
	 * Makes contenteditable elements within a container generate change events.
	 * 
	 * When you do, e.g. $obj.editable(), all the DOM elements with attribute
	 * contenteditable that are children of the DOM element $obj will trigger a
	 * change event when their contents is edited and changed.
	 * 
	 * See: http://html5demos.com/contenteditable
	 * 
	 * @return {*}
	 */
	$.fn.editable = function() {
		this.on('focus', '[contenteditable]', function() {
			var $this = $(this);
			$this.data('beforeContentEdit', $this.html());
		});
		this.on('blur', '[contenteditable]', function() {
			var $this = $(this);
			if ($this.data('beforeContentEdit') !== $this.html()) {
				$this.removeData('beforeContentEdit').trigger('change');
			}
		});
		return this;
	};
	var browser = navigator.userAgent.toLowerCase();
}(jQuery));

// Variable Format
var MsgVariableStruct = {
	name : "",
	description : ""
};
var msgVariables;

convertToContentEditable = function(text) {
	var index = 0, len = text.length, varread = false, variable = [], htm = [];
	try {
		while (index <= len) {
			var currentChar = text.charAt(index);
			if (currentChar == '{') {
				variable = [];
				variable[variable.length] = currentChar;
				varread = true;
			} else if (currentChar == '}' && varread) {
				variable[variable.length] = currentChar;
				htm[htm.length] = '<span contenteditable="true">';
				htm[htm.length] = '<span class="atwho-view-flag atwho-view-flag-at-mentions" contenteditable="false">';
				htm[htm.length] = '<span class="variable-formatting">'
						+ variable.join('') + '</span>';
				htm[htm.length] = '</span></span>';
				varread = false;
			} else if (varread) {
				variable[variable.length] = currentChar;
			} else {
				htm[htm.length] = currentChar;
			}
			index++;
		}
		return htm.join('');
	} catch (err) {
		alert(err);
	}
	return "";
}

initMessaging = function() {
	try {
		// Initialize message variables
		var data = new Array();

		var info = $.map(msgVariables, function(value, i) {
			return {
				id : i,
				'name' : '{' + msgVariables[i].name + '}',
				'desc' : msgVariables[i].description
			};
		});

		$('.inputor')
				.atwho(
						{
							at : "{",
							// alias: "at-mentions",
							alias : "",
							tpl : "<li data-value='${name}'>${name}<small>${desc}</small></li>",
							// max_len: 3,
							'data' : info,
							limit : 20,
							show_the_at : true,
							'start_with_space' : true,
							// insert_tpl: "<span>{${name}}</span>",
							insert_tpl : "<span class='variable-formatting'>${name}</span>",
							'callbacks' : {
								// filter: function (query, data, search_key) {
								// console.log("custom filter",query, data);
								// return this.super_call("filter", query, data,
								// search_key)
								// // return $.map(data, function(item, i) {
								// // return
								// item[search_key].toLowerCase().indexOf(query)
								// < 0 ? null : item
								// // })
								// },
								remote_filter : function(query, callback) {
									// this.super_call("remote_filter", query,
									// callback)
									// console.log("calling remote_filter width
									// query: " + query);
									callback(null);
								},
								filter : function(query, data, search_key) {
									// console.log("calling filter with query: "
									// + query);
									return this.call_default("filter", query,
											data, search_key);
								}
							}
						});

		// $(".inputor").keypress(function(event) {
		$(".inputor").off().on(
				"keypress",
				function(event) {
					var browser = navigator.userAgent.toLowerCase();
					if ((/msie/.test(browser) || !!browser
							.match(/trident.*rv\:11\./))) {
						var br, range, selection, textNode;
						if (event.keyCode === 13) {
							event.preventDefault();
							if (window.getSelection) {
								selection = window.getSelection();
								range = selection.getRangeAt(0);
								br = document.createElement("br");
								textNode = document.createTextNode("\u00a0");
								range.deleteContents();
								range.insertNode(br);
								range.collapse(false);
								range.insertNode(textNode);
								range.selectNodeContents(textNode);
								selection.removeAllRanges();
								selection.addRange(range);
								return false;
							}
						}
					}

					dataUpdated();
				});

		$(".inputor").keydown(function(event) {
			if (event.which) {

				if ((event.which == '8' || event.which == '46')) {
					dataUpdated();
				}
			}
		});

		$(".inputor").each(function() {
			var str = convertToContentEditable($(this).html());
			$(this).html("&lrm;" + str + "&lrm;");
		});

		// console.info("connecting out");
	} catch (err) {
		alert(err);
	}
};

function getContentEditableText(jqelement) {
	var ce = $("<pre />").html(jqelement.html());
	try {
		var browser = navigator.userAgent.toLowerCase();
		var isMozilla = /mozilla/.test(browser);
		var isIE = /msie/.test(browser);
		var isWebkit = /webkit/.test(browser);

		if (isWebkit)
			ce.find("div").replaceWith(function() {
				return "\n" + this.innerHTML;
			});
		if (isIE)
			ce.find("p").replaceWith(function() {
				return this.innerHTML + "<br>";
			});
		if (isMozilla)
			ce.find("br").replaceWith("\n");
		
	} catch (err) {
		console.error(err);
	}
	
	return ce.text();
}

var extractNotifications = function() {
	var parms = [], index = 0;

	$(".inputor").each(function() {
		parms[index++] = "&";
		parms[index++] = this.id;
		parms[index++] = "=";

		var text = getContentEditableText($(this));
		text = text.replace(/\u200e/g, '');
//		text = text.replace(/\u00a0/g, " ");
		var send = escape(text);

		parms[index++] = send;
	});
	var str = parms.join("");
	// console.info(str);
	return str;
};