var FlotPlotter = function(options) {
	this.graphType = options.graphType || "line";	//options include line,
	this.graphId = options.graphId || null;
	this.container = options.container || null;	//var container = $("#placeholder");
	this.maxPoints = options.maxPoints || 160;
	this.width = options.width || 640;
	this.height = options.height || 480;
	this.metric = options.metric;
	this.maxY = options.maxY || 100;
	this._init();
};

FlotPlotter.prototype = {
	//Properties
	graph : null,			// Holder for graph controller
	
	tsData : [],			// Holder of timestamp data
	
	// Latest data
	series : [{
		data:  [],
		lines: {
			fill : true
		}
	}],

	_init : function() {
		console.info(this);
	},
	
	initLineData : function() {
		try {
			for(var i=0; i<this.maxPoints; i++) {
				this.tsData.push(0);
				this.series[0].data.push([i, null]);
			}
		} catch(err) {
			console.log("flot initData: " + err);
		}
	},
	
	createCanvas : function() {
		var $html = null;
		try {
			var self = this;
			$html = $("<div/>", {
				id: self.graphId,
				width: self.width,
				height: self.height,
			});
		} catch(err) {
			console.log(err);
		}
		return $html;
	},
	
	initGraph : function() {
		try {
			var self = this;
			if (this.graphType == "line") {
				this.createLineGraph();
			}			
		} catch(err) {
			console.error("initGraphing: " + err);
		}
	},
	
	createLineGraph : function(graphId) {
		var self = this;
		try {
			this.initLineData();
			
			this.graph = $.plot(self.container, self.series, {
				canvas: true,
				grid: {
					borderWidth: 1,
					minBorderMargin: 20,
					labelMargin: 10,
					backgroundColor: {
						colors: ["#fff", "#e4f4f4"]
					},
					padding: {
						top: 40,
						bottom: 20,
						left: 20
					},
					margin: {
						top: 40,
						bottom: 20,
						left: 20
					},
					markings: function(axes) {
						var markings = [];
						var xaxis = axes.xaxis;
						for (var x = Math.floor(xaxis.min); x < xaxis.max; x += xaxis.tickSize * 2) {
							markings.push({ xaxis: { from: x, to: x + xaxis.tickSize }, color: "rgba(232, 232, 255, 0.2)" });
						}
						return markings;
					}
				},
				xaxis: {
					tickFormatter: function() {
						return "";
					}
				},
				yaxis: {
					min: 0,
					max: 110
				},
				legend: {
					show: true
				}
			});
			
			console.log(self.metric);
			
			//Create Y axis label
			var labelText = self.metric.dims[0].units.replace(/\//g, " \/ ");
			var yaxisLabel = $("<div class='axisLabel yaxisLabel'></div>")
			.text(labelText)
			.appendTo(self.container);
			yaxisLabel.css("margin-top", yaxisLabel.width() / 2 - 20);
			
			this.graph.draw();
			
			//Create title
			this.drawTitle();
		} catch(err) {
			console.error("createLineGraph: " + err);			
		}
	},
	
	drawTitle : function() {
		try {
			var c = this.container.find("canvas")[0];
			var canvas = c.getContext("2d");
			var cx = c.width / 2;
			canvas.font="bold 20px sans-serif";
			canvas.textAlign = 'center';
		    canvas.fillText(this.metric.metric,cx,35);
		} catch(err) {
			console.error(err);
		}
	},
	
	//Add new server data
	updateData : function(data) {
		try {
			var newestCurrent = this.tsData[this.tsData.length-1];
			var startIndex = -1;
			
			if (typeof newestCurrent === "undefined" || newestCurrent == null) {
				newestCurrent = 0;
			}
			
			for(var i=0; i<data.rec.length; i++) {
				var ts = Number(data.rec[i].tm);
				if (ts > newestCurrent) {
					startIndex = i;
					break;
				}
			}
					
			//Shift things left to make space
			var valData = [];
			if (startIndex >= 0) {
				var shiftLeft = (data.rec.length - startIndex);
				for(var i=shiftLeft; i<this.series[0].data.length; i++) {
					valData.push(this.series[0].data[i][1]);
				}
			}
			
			//Insert new Data
			for(var i=startIndex; i<data.rec.length; i++) {
				this.tsData.push(data.rec[i].tm);
				valData.push(Number(data.rec[i].vals[0]));
			}
			
			this.series[0].data = [];
			for(var i=0; i<this.maxPoints; i++) {				
				this.series[0].data.push([i, valData[i]]);
			}

			//Refresh plot
			this.graph.setData(this.series);
			this.graph.draw();
			this.drawTitle();
		} catch(err) {
			console.error("updateData: " + err);
		}
	}
	
};

