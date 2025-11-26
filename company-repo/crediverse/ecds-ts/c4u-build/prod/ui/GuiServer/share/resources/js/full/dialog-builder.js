/**
 * 
 */
var DialogBuilder = null;
!function($) {
	
	"use strict";

	DialogBuilder = function(options) {
		this.defaultOptions = {
				
		};
		this.controls = [];
		this.tabs = [];
		this.initOptions(options);
	};
	
	function DialogControl(inputType, label, id, tab) {
		this.inputType = inputType;
		this.label = label;
		this.id = id;
		this.tab = tab;
		this.cout = function() {
			console.info(this.id + " [" + this.inputType + "] :> " + this.label);
		}
	}
	
	// Constants
	DialogBuilder.TYPE_TEXT = 0;
	DialogBuilder.TYPE_BOOL = 1;
	DialogBuilder.TYPE_BLANK = 2;
	
	DialogBuilder.prototype = {
		constructor : DialogBuilder,
		
		initOptions : function(options) {
            this.options = $.extend(true, this.defaultOptions, options);
            return this;
		},
		
		addControl : function(inputType, label, id, tab) {
			try {
				this.controls[this.controls.length] = new DialogControl(inputType, label, id, tab);
				if (typeof tab !== 'undefined') {
					console.log("yeah!");
				}
			} catch(err) {
				console.error(err);
			}
		},
		
		printControls : function() {
			console.info("printing...");
			for(var i=0; i<this.controls.length; i++) {
				this.controls[i].cout();
			}
		},
		
		addText : function(divId) {
			try {
				var $text = $("<p>Some Text</p>");
				$text.prop('id', "mine");
				var $element = $("#" + divId);
//				$text.on("click", function(event) {
//					alert("Clicked");
//				});
				$element.html($text);
				
				$text.onAvailable(function() {
					$(this).html("Found!");
				});
			} catch(err) {
				console.error("Well. " + err);
			}
		}
	};
	

	$.fn.onAvailable = function(fn){
	    var sel = this.selector;
	    var timer;
	    var self = this;
	    if (this.length > 0) {
	        fn.call(this);   
	    }
	    else {
	        timer = setInterval(function(){
	            if ($(sel).length > 0) {
	                fn.call($(sel));
	                clearInterval(timer);  
	            }
	        },50);  
	    }
	};


}(window.jQuery);