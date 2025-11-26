var defaultOptions = {

	legend : {
		show : true,
		labelFormatter : function(label, series) {
            return '<div style="font-size:8pt;text-align:left;padding:2px;"> ' + label + '</div>';
        },
		// labelBoxBorderColor: color
		noColumns : 1,
		position : "ne",
		margin : 0,
		backgroundColor : null,
		backgroundOpacity : 0,
		// container: null,
		// sorted: false,
	},

	grid : {

		show : true,
		aboveData : false,
		// color: color,
		// backgroundColor: color/gradient or null
		// margin: number or margin object
		// labelMargin: number
		// axisMargin: number
		// markings: array of markings or (fn: axes -> array of markings)
		borderWidth: 1,
		// "left" properties with different widths
		// borderColor: color or null or object with "top", "right", "bottom"
		// and "left" properties with different colors
		// minBorderMargin: number or null
		clickable : false,
		hoverable : true,
		// autoHighlight: boolean
		// mouseActiveRadius: number

	},

	xaxis : {
		show : true,
		position : "bottom",
		// mode : null,
		// timezone: null,

		// color: null,
		// tickColor: null,
		font: {
			size: 10,
			color: "#000000"
		},

		// min: 0,
		// max: null,
		// autoscaleMargin: null,

		// transform: null,
		// inverseTransform: null,

		// ticks: null,
		// tickSize: 0.1,

		// labelWidth: null,
		// labelHeight: null,
		// reserveSpace: 5,

		// tickLength: 10,

		// alignTicksWithAxis: null,

		axisLabel : null,
		axisLabelUseCanvas : true,
		axisLabelFontSizePixels : 10,
		axisLabelFontFamily : 'Verdana, Arial',
		axisLabelPadding : 10,
	},

	yaxis : {
		show : true,
		position : "left",
		// mode : null,
		// timezone: null,

		// color: null,
		// tickColor: null,
		font: {
			size: 10,
			color: "#000000"
		},

		min : 0,
		// max: null,
		autoscaleMargin : 0.2,

		// transform: null,
		// inverseTransform: null,

		// ticks: 10,
		// tickSize: 0.4,

		// labelWidth: null,
		// labelHeight: null,
		// reserveSpace: null,

		// tickLength: null,

		// alignTicksWithAxis: null,

		axisLabel : null,
		axisLabelUseCanvas : true,
		axisLabelFontSizePixels : 10,
		axisLabelFontFamily : 'Verdana, Arial',
		axisLabelPadding : 2,
	},

	series : {
		lines : {
			show : true,
			lineWidth : 3,
			fill : false,
			fillColor : null,

			// zero: false,

			// steps: false,
		},

		points : {
			show : true,
			lineWidth : 1,
			fill : false,
			fillColor : null,

			radius : 3,
			symbol : "circle",
		},

		bars : {
			show : true,
			lineWidth : 1,
			fill : 1,
			fillColor : null,

			zero : true,

			barWidth : 0.75,
			align : "center",
			horizontal : false,
		},

		pie : {
			show : true,
			innerRadius : 0
		},

		shadowSize : 0,
		highlightColor : 0,
	},

};

////////////////////////////////////////////////////////////////////////////////////////
//
// Themes
//
// /////////////////////////////////

var themes = [
              
vitaminCTheme = {
		
		name: "Vitamin C Theme",
		colors: ["#004358", "#1F8A70", "#BEDB39", "#FFE11A", "#FD7400"],
		borderWidth: 0,
		font: {
			family: "Verdana, Arial",
			size: 10,
			color: "#000000"
		},
		axisColor: "#000000",
		dark: false,
		
},

// 4th Color: #FCFCFC - too light
infoGraphicsTheme = {
		
		name: "Info Graphics Theme",
		colors: ["#F58625", "#314655", "#F05026", "#D1D0CB", "#31BDC1"],
		borderWidth: 1,
		font: {
			family: "Verdana, Arial",
			size: 10,
			color: "#000000"
		},
		axisColor: "#000000",
		dark: false,
		
},

rainbowTheme = {
		
		name: "Rainbow Theme",
		colors: ["#F42D43", "#EC9241", "#E5E241", "#52C562", "#2DCED9"],
		borderWidth: 0,
		font: {
			family: "Verdana, Arial",
			size: 10,
			color: "#000000"
		},
		axisColor: "#000000",
		dark: false,
		
},

//2nd Color: #FFFDF8 - too light
sherbetTheme = {
		
		name: "Sherbet Theme",
		colors: ["#FFC170", "#D1D0CB", "#FF453A", "#00E8D1", "#FFFA71"],
		borderWidth: 1,
		font: {
			family: "Verdana, Arial",
			size: 10,
			color: "#000000"
		},
		axisColor: "#000000",
		dark: false,
		
}
              
];

var currentThemeIndex = 0;

////////////////////////////////////////////////////////////////////////////////////////
//
// Main
//
// /////////////////////////////////

// Converts bam widgets to flot charts
function BamToFlot(widget) {
	
	try {
		
		// Variables
		var dataset;
		var options;

		// Check if there is any cache
		if (widget.cache != null && !widget.changed) {

			// Set the options to the cache
			options = widget.cache;

		}

		// Create a common dataset
		dataset = setupCommonDataset(widget);
	
		// Before Plot Check
	
		// If it is a line graph get options for a line graph
		if (widget.type.index == graphType.LINE.index) {
		
			// Ensure options is null
			if (options == null)
				options = setupLineGraphOptions(widget);
		
		// Else if it is a bar graph uses those options
		} else if (widget.type.index == graphType.BAR.index) {
		
			// Ensure options is null
			if (options == null)
				options = setupBarGraphOptions(widget);
			
			// Setup the data set for a bar graph
			dataset = setupCommonDataset(widget, true);
		
		// Else if it is a pie graph
		} else if (widget.type.index == graphType.PIE.index) {
			
			// Ensure the options is null
			if (options == null)
				options = setupPieGraphOptions(widget);
		
		// Else if it is a hollow pie chart
		} else if (widget.type.index == graphType.HOLLOW_PIE.index) {
		
			// Ensure the options is null
			if (options == null) {
				options = setupPieGraphOptions(widget);
				
				// Set the inner radius
				options["series"]["pie"]["innerRadius"] = 0.5;
			}
		
		// Else if it is a histogram
		} else if (widget.type.index == graphType.HISTOGRAM.index) {
		
			// Ensure the options is null
			if (options == null)
				options = setupHistogramOptions(widget);
		
		// Else if it is a gauge
		} else if (widget.type.index == graphType.GAUGE.index) {
		
			// Ensure the options is null
			if (options == null) {
				options = setupGaugeOptions(widget);
			}
			
			// Set the dataset for the gauge
			dataset = setupGaugeDataset(widget);
		
		}

		// Apply the theme to the options
		options = applyTheme(options);
	
		// Check if the widget was already created or if it changed
		if (widget.ref == null || widget.changed) {

			// Plot the graph
			widget.ref = $.plot($(widget.element).find(".widget-display"), dataset, options);
			
			// Set the cache
			widget.cache = options;
			
			// Use a tooltip
			$(widget.element).UseTooltip();
			
			// Set the changed flag
			widget.changed = false;

		} else {

			// Else update the data
			widget.ref.setData(dataset);
			
			// Setup the grid
			widget.ref.setupGrid();
			
			// Redraw the graph
			widget.ref.draw();

		}
	
		// After Plot Check
	
		// Check if it is a gauge graph
		if (widget.type.index == graphType.GAUGE.index) {
		
			// Setup the widget canvas
			setupGaugeCanvas(widget);
		
		}
	
		// Set the widget to updated
		widget.updated();

	} catch (e) {
		
		console.log("bam-to-flot.BamToFlot: " + e);
		
	}
}

// Applies a theme to the options
function applyTheme(options) {
	
	// Changes the colours of the graph
	options["colors"] = themes[currentThemeIndex]["colors"];
	
	// Changes the border width
	options["grid"]["borderWidth"] = themes[currentThemeIndex]["borderWidth"];
	
	// If there is an x axis
	if (options["xaxis"]) {
		
		// Change the font
		options["xaxis"]["font"] = themes[currentThemeIndex]["font"];
		options["xaxis"]["axisLabelFontFamily"] = themes[currentThemeIndex]["font"]["family"];
		options["xaxis"]["axisLabelFontSizePixels"] = themes[currentThemeIndex]["font"]["size"];
		options["xaxis"]["axisLabelColour"] = themes[currentThemeIndex]["axisColor"];
		
	}
	
	// If there is an y axis
	if (options["yaxis"]) {
		
		// Change the font
		options["yaxis"]["font"] = themes[currentThemeIndex]["font"];
		options["yaxis"]["axisLabelFontFamily"] = themes[currentThemeIndex]["font"]["family"];
		options["yaxis"]["axisLabelFontSizePixels"] = themes[currentThemeIndex]["font"]["size"];
		options["yaxis"]["axisLabelColour"] = themes[currentThemeIndex]["axisColor"];
		
	}
	
	// Return the new options
	return options;
}

var previousPoint = null;
var previousLabel = null;

// Adds a tooltip to the graph
$.fn.UseTooltip = function () {
	
	// Binds the hover function over the graph
    $(this).bind("plothover", function (event, pos, item) {
    	
    	// Ensure the item is valid
        if (item) {
        	
        	// Ensure the label is not part of a pie graph that shows the legend
            if (((previousLabel != item.series.label) ||
         (previousPoint != item.dataIndex)) && !(item.series.pie && item.series.pie["show"])) {
            	
            	// Set the references
                previousPoint = item.dataIndex;
                previousLabel = item.series.label;
                
                // Remove the previous tooltip
                $("#tooltip").remove();

                // Get the coordinates
                var x = item.datapoint[0];
                var y = item.datapoint[1];

                // Get the colour of the graph
                var color = item.series.color;

                // Creates the tooltip
                showTooltip(item.pageX != undefined ? item.pageX : pos.pageX,
                		    item.pageY != undefined ? item.pageY : pos.pageY, color,
                		    "<strong>" + item.series.label + "</strong><br/><strong>" + y + "</strong> " + item.series.yaxis.options["axisLabel"]);
            }
        } else {
        	
        	// Else remove the tooltip
            $("#tooltip").remove();
            previousPoint = null;
        }
        
        
    });
};

// Creates a tooltip
function showTooltip(x, y, color, contents) {
	
	// Appends the tooltip to the body
    $('<div id="tooltip">' + contents + '</div>').css({
        position: 'absolute',
        display: 'none',
        top: y - 40,
        left: x + 10,
        border: '2px solid ' + color,
        padding: '3px',
        'font-size': '9px',
        'border-radius': '6px',
        'background-color': '#000000',
        'font-family': 'Verdana, Arial, Helvetica, Tahoma, sans-serif',
        opacity: 0.9,
        'z-index': 15
    }).appendTo("body").fadeIn(200);
}

////////////////////////////////////////////////////////////////////////////////////////
//
// COMMON
//
// /////////////////////////////////

// Creates a common dataset that most graphs use
function setupCommonDataset(widget, isArray) {
	
	// Check if it is an array
	isArray = typeof isArray !== 'undefined' ? isArray : false;
	
	// Variables
	var datasets = [];
	var index = 0;
	
	// Iterate through the datasets from the widget
	for (var i = 0; i < widget.getTotalDatasets(); i++) {

		// Get the dataset
		var dataset = widget.getDataset(i);

		// Check if the dataset is enabled
		if (dataset.enabled) {
			
			// Create a set with the title of the dataset and the data
			var set = {

				label : dataset.title,
				data : isArray ? [[ index++, dataset.data[1] ]] : dataset.data,

			};

			// Add it to the datasets array
			datasets.push(set);
			
		}
	}

	// Return the datasets
	return datasets;
	
}

////////////////////////////////////////////////////////////////////////////////////////
//
// LINE
//
// /////////////////////////////////

// Setups the line options
function setupLineGraphOptions(widget) {

	// Sets the background for the widget
	setupDefaultBackground(widget);
	
	// Empty options
	var options = {};

	// Get the defaults
	var defaults = copy(defaultOptions);

	// Get the legend
	options["legend"] = defaults["legend"];
	options["legend"]["show"] = widget.showLegend;
	
	// Get the grid options
	options["grid"] = defaults["grid"];

	// Get the x axis options
	options["xaxis"] = defaults["xaxis"];
	options["xaxis"]["mode"] = "time";
	options["xaxis"]["axisLabel"] = "Time";
	options["xaxis"]["tickSize"] = [widget.interval, "second"];
	
	// Set the ticker
	options["xaxis"]["tickFormatter"] = function (v, axis) {
		
		// Get the date
        var date = new Date(v);
 
        // Check if the interval has passed
        if (date.getSeconds() % widget.interval == 0) {
        	
        	// Get the components
            var hours = date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
            var minutes = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
            var seconds = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
 
            // Print out the timestamp
            return hours + ":" + minutes + ":" + seconds;
        } else {
        	
        	// Else print out nothing
            return "";
        }
    };

    // Get the options for the y axis
	options["yaxis"] = defaults["yaxis"];
	options["yaxis"]["axisLabel"] = widget.axisLabelY;

	// Set the series options
	options["series"] = {

		lines : defaults["series"]["lines"]

	};
	
	// Get the shadow size for the series
	options["series"]["shadowSize"] = defaults["series"]["shadowSize"];

	// Return the options
	return options;

}

////////////////////////////////////////////////////////////////////////////////////////
//
// BAR
//
// /////////////////////////////////

// Sets up the bar graph options
function setupBarGraphOptions(widget) {

	// Set the default background for the widget
	setupDefaultBackground(widget);
	
	// Empty options
	var options = {};
	
	// Get the defaults
	var defaults = copy(defaultOptions);

	// Do not show the legend
	options["legend"] = {
			show: false
	};
	
	// Get the grid options
	options["grid"] = defaults["grid"];

	// Get the xaxis options
	options["xaxis"] = defaults["xaxis"];
	
	// Variables
	var categories = [];
	var j = 0;
	
	// Iterate through the datasets for the title for each bar
	for (var i = 0; i < widget.getTotalDatasets(); i++) {
		
		// Ensure the dataset is enabled
		if (widget.getDataset(i).enabled) {
			
			// Add the title to the array
			categories.push([j++, widget.getDataset(i).title]);
			
		}
		
	}
	
	// Set the x axis bar titles
	options["xaxis"]["ticks"] = categories;

	// Set the y axis options
	options["yaxis"] = defaults["yaxis"];
	options["yaxis"]["axisLabel"] = widget.axisLabelY;

	// Set the series to bars
	options["series"] = {

		bars: defaults["series"]["bars"]

	};
	
	// Set the shadow size
	options["series"]["shadowSize"] = defaults["series"]["shadowSize"];

	// Return the options
	return options;
}

////////////////////////////////////////////////////////////////////////////////////////
//
// PIE
//
// /////////////////////////////////

// Sets up the pie graph options
function setupPieGraphOptions(widget) {

	// Set the default background for the widget
	setupDefaultBackground(widget);
	
	// Empty options
	var options = {};
	
	// Get the default options
	var defaults = copy(defaultOptions);

	// Set the legend
	options["legend"] = defaults["legend"];
	options["legend"]["show"] = widget.showLegend;
	
	// Get the defaults for grid
	options["grid"] = defaults["grid"];
	
	// Set the series top pie chart
	options["series"] = {
			
			pie: {
				
				show: true,
				radius: 1,
	            label: {
	            	show: true,
	                radius: 2/3,
	                formatter: function(label, series){
	                	return '<div style="font-size:8pt;text-align:center;padding:2px;color:white;">'+Math.round(series.percent)+'%</div>';
	                },
	                threshold: 0.05
	             }
				
			}
			
	};
	
	// Set the y axis
	options["yaxis"] = defaults["yaxis"];
	options["yaxis"]["axisLabel"] = widget.axisLabelY;
	
	// Set the shadow size
	options["series"]["shadowSize"] = defaults["series"]["shadowSize"];
	
	// Return the options
	return options;
}

////////////////////////////////////////////////////////////////////////////////////////
//
// HISTOGRAM
//
// /////////////////////////////////

// Sets up the histogram options
function setupHistogramOptions(widget) {
	
	// Sets the default background for the widget
	setupDefaultBackground(widget);
	
	// Empty options
	var options = {};
	
	// Gets default options
	var defaults = copy(defaultOptions);

	// Get the legend
	options["legend"] = defaults["legend"];
	options["legend"]["show"] = widget.showLegend;
	
	// Get the grid options
	options["grid"] = defaults["grid"];

	// Set the x axis options
	options["xaxis"] = defaults["xaxis"];
	options["xaxis"]["mode"] = "time";
	options["xaxis"]["axisLabel"] = "Time";
	options["xaxis"]["tickSize"] = [widget.interval, "second"];
	
	// Set the x axis to have time on the axis
	options["xaxis"]["tickFormatter"] = function (v, axis) {
		
		// Get the date
        var date = new Date(v);
 
        // Check if it must display the timestamp
        if (date.getSeconds() % widget.interval == 0) {
        	
        	// Get the components
            var hours = date.getHours() < 10 ? "0" + date.getHours() : date.getHours();
            var minutes = date.getMinutes() < 10 ? "0" + date.getMinutes() : date.getMinutes();
            var seconds = date.getSeconds() < 10 ? "0" + date.getSeconds() : date.getSeconds();
 
            // Return the timestamp
            return hours + ":" + minutes + ":" + seconds;
        } else {
        	// Else return nothing
            return "";
        }
    };

    // Get the y axis options
	options["yaxis"] = defaults["yaxis"];
	options["yaxis"]["axisLabel"] = widget.axisLabelY;

	// Set the series to bars
	options["series"] = {

		bars: defaults["series"]["bars"]

	};
	
	// Set the width of the bars
	options["series"]["bars"]["barWidth"] = 100 * widget.interval * 2;
	
	// Set the shadow size
	options["series"]["shadowSize"] = defaults["series"]["shadowSize"];

	// Return the options
	return options;
	
}

////////////////////////////////////////////////////////////////////////////////////////
//
// GAUGE
//
// /////////////////////////////////

// Sets up the gauge options
function setupGaugeOptions(widget) {

	// Set the background to the gauge image
	$(widget.element).find(".widget-display").css("background-image", "url('/img/bam/gauge.png')");
	$(widget.element).find(".widget-display").css("background-repeat", "no-repeat");
	$(widget.element).find(".widget-display").css("background-position", "center");
	
	// Get the default options
	var defaults = copy(defaultOptions);
	
	// Set the options
	var options = {
			
			xaxis: {
				min: -100,
		        max: 100,
		        show: true
		    },
		    
		    yaxis: {
		        min: -100,
		        max: 100,
		        show: true
		    },
		    
		    grid: {  
		    	show: false
		    },
		    
		    canvas: true,
		    
	};
	
	// Get the legend options
	options["legend"] = defaults["legend"];
	options["legend"]["show"] = widget.showLegend;
	
	// Return the options
	return options;
	
}

var total = 0;

// Sets up the gauge dataset
function setupGaugeDataset(widget) {
	
	// Variables
	var datasets = [];
	var index = 0;
	
	// Iterate through the datasets
	for (var i = 0; i < widget.getTotalDatasets(); i++) {

		// Get the dataset
		var dataset = widget.getDataset(i);

		// Ensure the dataset is enabled
		if (dataset.enabled) {
			
			// Get the location of the point to plot to
			var location = valueToAngle(dataset.data[1], widget.maxValue, 70 - $(widget.element).width() / 20);
			
			// Create the set with coordinates
			var set = {

				label : dataset.title,
				data : [[0,0], location],

			};

			// Add to the datasets
			datasets.push(set);
			
		}
	}

	// Return the datasets
	return datasets;
	
}

// Sets up the canvas for the gauge
function setupGaugeCanvas(widget) {
	
	try {
		
		// Get the elements
		var display = $(widget.element).find(".widget-display");
		var overlay = $(display).find(".flot-base");
		
		// Ensure there is a overlay layer
		if (overlay) {
			
			// Get the context
			var ctx = $(overlay).get(0).getContext("2d");
			var width = $(overlay).width();
			var height = $(overlay).height();
			
			// Set the font
			ctx.font = "10px Verdana";
			ctx.textAlign = "center";
			ctx.fillStyle = "white";
			ctx.fillText(widget.axisLabelY != null ? widget.axisLabelY : "loading...", width / 2, height / 2 + 50);
			
			// Iterate through the angles and add the values to the gauge
			for (var i = 0; i < 101; i += 10) {
				
				// Get the location
				var location = valueToAngle(i, 100, 175);
				location[0] += width / 2;
				location[1] = -location[1];
				location[1] += height / 2;
				
				// Set the font
				ctx.font = "10px Verdana";
				ctx.textAlign = "center";
				ctx.fillStyle = "black";
				ctx.fillText(Math.round(i / 100 * widget.maxValue), location[0], location[1]);
				
			}
		}		
		
		
	} catch (e) {
		console.log("bam-to-flot.setupGaugeCanvas: " + e);
	}
	
}

// Converts the value to an angle
function valueToAngle(value, max, radius) {
	
	// Check if the radius exists, if not default to 50
	radius = typeof radius !== 'undefined' ? radius : 50;
	
	// Set the min Coord and max Coord
	var minCord = {x: -60, y: -60};
	var maxCord = {x: 60, y: -60};
	
	// Get the start angle
	var startAngle = (6.2831 + Math.atan2(minCord.y, minCord.x));
	
	// Get the end angle
	var endAngle = Math.atan2(maxCord.y, maxCord.x);
	
	// Get the degrees between the angles
	var degreesSweep = (-endAngle) + startAngle;
	
	// Get the number of degrees
	var numDegrees = degreesSweep * (value/max);
	
	// Get the angle
    var angle = (startAngle - numDegrees);
    
    // Get the position
    var posX = radius * Math.cos(angle);
    var posY = radius * Math.sin(angle);
    
    // Return the location
    return [posX, posY];
}

////////////////////////////////////////////////////////////////////////////////////////
//
// Helper Methods
//
// /////////////////////////////////

// Sets the widgets background to blank
function setupDefaultBackground(widget) {
	
	$(widget.element).find(".widget-display").css("background-image", "");
	$(widget.element).find(".widget-display").css("background-repeat", "");
	$(widget.element).find(".widget-display").css("background-position", "");
	
}

// Copies a dictionary without references
function copy(dictionary) {
	
	return $.extend(true, {}, dictionary);
	
}