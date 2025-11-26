// The enum for the graph types
graphType = {
		
		LINE: {index: 0, hasTime: true},
		BAR: {index: 1},
		PIE: {index: 2},
		HOLLOW_PIE: {index: 3},
		HISTOGRAM: {index: 4, hasTime:true},
		GAUGE: {index: 5},
		
};

// Constructor for the bam widget
var BamWidget = function(element, uid, name, grid, options) {

	// Default options
	var defaultOptions = {
			
			numOfGridX: 6,
			numOfGridY: 5,
			
			animationDuration: 250,
			update: null,
			
			// Graph Types
			type: graphType.LINE,
			
			maxDataKept: 25,
			interval: 10,
			
	};
	
	// The actual html element the widget is referencing
	this.element = element;
	
	// The plugin uid that the widget refers to
	this.uid = uid;
	
	// The name of the metric
	this.name = name;
	
	// The dimensions of the grid
	this.gridWidth = grid.outerWidth();
	this.gridHeight = grid.outerHeight();

	// Check if options have been provided else use default
	if (typeof options == 'object') {
		options = $.extend(defaultOptions, options);
	} else {
		options = defaultOptions;
	}
	
	// The number of square grids on the x and y
	this.numOfGridX = options.numOfGridX;
	this.numOfGridY = options.numOfGridY;
	
	// The animation duration
	this.animationDuration = options.animationDuration;
	
	// The update method
	this.update = options.update;
	
	// The type of graph
	this.type = options.type;
	
	// The max data to keep stored in memory
	this.maxDataKept = options.maxDataKept;
	
	// The interval for data
	this.interval = options.interval;
	
	// A reference to the datasets
	this.datasets = [];
	
	// The labels for the axis's
	this.axisLabelX = null;
	this.axisLabelY = null;
	
	// The date of the last update
	this.lastUpdated = new Date();
	
	// Whether the widget has started
	this.started = false;
	
	// The reference to the the chart mechanic
	this.ref = null;
	
	// Cache for the chart
	this.cache = null;
	
	// Whether it is rendering the graph or not
	this.render = true;
	
	// Whether there has been a change in the widget
	this.changed = false;
	
	// Ajax reference id
	this.ajaxRef = null;
	
	// Whether the widget is polling for data
	this.busyPolling = false;
	
	// Whether the widget must force an update
	this.forceUpdate = true;
	
	// Whether the widget must show a legend
	this.showLegend = false;
	
	// The max amount of series to show
	this.maxOnLoad = 5;
	
	// The current max value for the graph
	this.maxValue = 100;
	
	// Call to the init function
	this.init();

};

// Prototype of the Bam Widget
BamWidget.prototype = {
	
	padding: -12.5,
	
	// Initialises self to this
	init: function() {
		var self = this;
	},

	// Starts the widget and executes the complete function when done
	start: function(complete) {
		
		// Refreshes the widgets dimensions
		this.refresh(complete);
		
		// Sets the flag to true
		this.started = true;
		
	},
	
	// Resizes the widget to the number of grids on the x and y
	refresh: function(complete) {
		
		try {
			
			// Get the parent of the widget
			var current = $($(this.element).parent());
			var width = 0;
			
			// Iterate through the number of grids on the x axis
			for (var i = 0; i < this.numOfGridX; i++) {
					
				// Add the width from the grid
				width += $(current).outerWidth();
				
				// Gets the next grid element
				current = $(current).next();
					
			}
				
			// Animates the element expanding to the width and height
			$(this.element).animate({
				width: width + this.padding,
				height: this.gridHeight * this.numOfGridY + this.padding,
			}, this.animationDuration, function() {
				
					// Execute the complete function
					if (complete)
						complete();
			});
			
		} catch (e) {
			
			console.log("bam-widget.refresh: " + e);
			
		}
		
	},
	
	// Resizes the widget without any animation
	immediate: function() {
		
		try {
			
			// Get the width for the widget
			var current = $($(this.element).parent());
			var width = 0;
			for (var i = 0; i < this.numOfGridX; i++) {
				
				// Add the width of the grids
				width += $(current).outerWidth();
				current = $(current).next();
				
			}
			
			// Set the width and height
			$(this.element).width(width + this.padding);
			$(this.element).height(this.gridHeight * this.numOfGridY + this.padding);
			
		} catch (e) {
			
			console.log("bam-widget.immediate: " + e);
			
		}
		
	},
	
	// Resizes the widget according to the grid
	resize: function(grid) {
		this.gridWidth = grid.outerWidth();
		this.gridHeight = grid.outerHeight();
	},
	
	// Gets the datasets
	getAllDatasets: function() {
		return this.datasets;
	},
	
	// Gets a dataset from the datasets
	getDataset: function(index) {
		return this.datasets[index];
	},
	
	// Gets the total number of datasets
	getTotalDatasets: function() {
		return this.datasets.length;
	},
	
	// Adds data to the datasets
	addData: function(index, dimension) {
		
		try {
			
			// Ensure the dimension is greater than one
			if (dimension.length < 2) {
				return;
			}
			
			// Check that the datasets is greater than or equal to the index
			if (this.datasets.length >= index + 1) {
				
				// Set the last updated value
				lastUpdated = new Date();
				
				// Set the axis label for the y
				if (this.axisLabelY == null) {
					
					this.axisLabelY = dimension[2];
					
				}
				
				// Create the set
				var set;
				
				// Check if dimension[1] is an array
				if (dimension[1] instanceof Array) {
					
					// Set the set to the array
					set = dimension[1];
					
				// Else use time
				} else {
					
					// Get the time and dimension data
					set = [ lastUpdated.getTime() , parseFloat(dimension[1]) ];
				}
				
				// Check if it is a line graph or histogram
				if (this.type == graphType.LINE || this.type == graphType.HISTOGRAM) {
					
					// Set the x axis to time
					if (this.axisLabelX == null) {
						
						this.axisLabelX = "Time";
						
					}
					
					// Add the set to the datasets
					this.datasets[index].addSet(set, this.maxDataKept);
					
				} else {
					
					// Else just set the set for the dataset
					this.datasets[index].setSet(set);
					
				}
				
				// Adjust the max value according to the set value
				if (set[1] > this.maxValue)
					this.maxValue = Math.ceil(set[1] / 100) * 100;
				
				// Return from the method
				return;
			}
			
			// Else if the dataset does not exist
			index = this.datasets.length;
			
			// Create the dataset
			this.datasets[index] = new Dataset(dimension[0]);
			
			
			// Disable the dataset if the current number of series is above the max
			if (this.datasets.length > this.maxOnLoad)
				this.datasets[index].enabled = false;
			
			// Call the method again
			this.addData(index, dimension);
			
		} catch (e) {
			
			console.log("bam-widget.addData: " + e);
			
		}
		
	},
	
	// Adds blank information to the graph
	addBlank: function() {
		
		try {
			
			// Update the last updated date
			lastUpdated = new Date();
			
			// Check if it is a line graph
			if (this.type == graphType.LINE) {
				
				// Iterate through the datasets
				for (var i = 0; i < this.datasets.length; i++) {
					
					// Get the index
					var index = this.datasets[i].data.length;
					index = index > 0 ? index - 1 : 0;
					
					// Add the same data from the previous time
					this.datasets[i].addSet([ lastUpdated.getTime(), this.datasets[i].data[index][1] ], this.maxDataKept);
					
				}
				
			// Else rather set the datasets
			} else {
				
				// Iterate through the datasets
				for (var i = 0; i < this.datasets.length; i++) {
					
					// Set the dataset set to the previous data
					this.datasets[i].setSet([ i, this.datasets[i].data[1] ]);
					
				}
				
			}
			
		} catch (e) {
			
			console.log("bam-widget.addBlank: " + e);
			
		}
		
	},
	
	// Destroys the widget
	destroy: function() {
		
		try {
			
			// Removes the widget from the element
			$(this.element).remove();
			
			// Removes the update function
			this.update = null;
			
		} catch (e) {
			
			console.log("bam-widget.destroy: " + e);
			
		}
		
	},
	
	// Returns if the widget has started
	hasStarted: function() {
		return this.started;
	},
	
	// Changes the type of widget graph
	changeType: function(type) {
		
		try {
			
			// Iterate through the graph types and get the enum
			for (key in graphType) {
				
				// Compare the graph names
				if (graphType[key].index == type) {
					
					// Set the type
					this.type = graphType[key];
					break;
				}
			}
			
			// If the graph hasn't started, then return
			if (!this.started) return;
			
			// Create a temporary dataset
			var temp = $.extend(true, [], this.datasets);
			
			// Clear the datasets
			this.datasets = [];
			
			// Iterate through the temporary datasets
			for (var i = 0; i < temp.length; i++) {
				
				// Get the data for the dataset
				var data = temp[i].data;
				
				// Get the last
				var last = data[data.length - 1];
				
				// Check if it is an array
				if (last instanceof Array) {
					
					// Get the last date
					var lastDate = last[0];
					
					// Set last to the previous value
					last = last[last.length - 1];
					
					// Add the data to the dataset
					this.addData(i, [ temp[i].title, [ lastDate, last ], this.axisLabelY ])
				}
				
				// Add the data to the dataset
				this.addData(i, [ temp[i].title, last, this.axisLabelY ]);
				
			}
			
			// Set it to changed 
			this.changed = true;
			
			// Call the update method
			this.update();
			
		} catch (e) {
			
			console.log("bam-widget.changeType: " + e);
			
		}
		
	},
	
	// Called when the widget has been updated
	updated: function() {
		
		// Sets the flag for force update to false
		this.forceUpdate = false;
		
		// Adjusts the max data kept according to the interval
		this.maxDataKept = this.interval < 4 ? 4 : this.interval;
		
	},
	
	// Converts the widget to json
	toJson: function() {
		
		try {
			
			// Creates the json object
			var json = {
					
					uid: this.uid,
					name: this.name,
					numOfGridX: this.numOfGridX,
					numOfGridY: this.numOfGridY,
					type: this.type,
					interval: this.interval,
						
			};
			
			// Create an empty dataset
			json["dataset"] = [];
				
			// Iterate through the datasets
			for (var i = 0; i < this.datasets.length; i++) {
					
				// Add the dataset to the json
				json["dataset"].push(this.datasets[i].toJson());
					
			}
				
			// Return the json
			return json;
			
		} catch (e) {
			
			console.log("bam-widget.toJson: " + e);
			
		}
		return "";
		
	},

};

// Constructor for the Dataset
var Dataset = function(title) {
	
	// Title of the dataset
	this.title = title;
	
	// Reference to the data
	this.data = [];
	
	// Whether it is enabled or not
	this.enabled = true;
	
};

// Prototype of the dataset
Dataset.prototype = {
	
		// Adds a set to the data
		addSet: function(set, max) {
			
			// Adds to the data array
			this.data.push(set);
			
			// While it is above max, remove from the first items
			while (this.data.length >= max)
				this.data.shift();
		},

		// Sets the data
		setSet: function(set) {
			
			// Sets the data to the set
			this.data = set;
			
		},
		
		// Converts the object to json
		toJson: function() {
			
			try {
				
				// Create the json object
				var json = {
						
						title: this.title,
						enabled: this.enabled,
						
				};
				
				// Return the json object
				return json;
				
			} catch (e) {
				
				console.log("bam-widget.toJson: " + e);
				
			}
			
			return "";
		}
		
};
