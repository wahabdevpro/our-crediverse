define(["jquery", "underscore", "jquery.select2full"],
	function ($, _, Select2) {
		"use strict";
	
		var _super = $.fn.select2;
		
		
		// Define proper select2 defaults here
		var _defaults = {
			url: null, // Added for ease of use
			dataMap: {},
      		ajax: {
      			type: "GET",
      		    url: '',
    		    dataType: 'json',
    		    contentType: "application/json",
      		    delay: 250,
      		    
                processResults: function (data) {
                	var dropdownData = {
                    		results: $.map(data, function (item, i) {
                    			return {
                    				id: i,
                    				text: item,
                            		item: item
                                }
                            })
                        };
                	//alert(JSON.stringify(dropdownData, null, 2));
                	return dropdownData;
                }
      		},
      		minimumInputLength: 0,
			allowClear: true, 
			placeholder: "",
			sorter: function(data) {
                return data.sort(function (a, b) {
                	if( !_.isUndefined(a) && !_.isUndefined(b) ){
                    	var aLower = String(a.text).toLowerCase();
                    	var bLower = String(b.text).toLowerCase();
                        if (aLower > bLower) {
                            return 1;
                        }
                        if (aLower < bLower) {
                            return -1;
                        }
                	}
                    return 0;
                });
            }
		};
		
	    $.fn.select2 = function(options) {
			
			if (_.isObject(options) && arguments.length === 1) {
				// Now lets merge things
				var _extendedDefaults = $.extend(true, {}, _defaults, options);
				
				if (!_.isUndefined(options.url) && !_.isNull(options.url)) {
					_extendedDefaults.ajax.url = options.url;
				}

				var args = [];
				args.push(_extendedDefaults);
				$.proxy(_super.apply( this, args ), this);
				return this;
	        }

		     // call the original constructor
			$.proxy(_super.apply( this, arguments ), this);
		    return this;
		};
	
		return $;
	});