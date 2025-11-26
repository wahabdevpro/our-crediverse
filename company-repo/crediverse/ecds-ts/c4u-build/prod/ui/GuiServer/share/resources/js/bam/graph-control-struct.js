GraphTypes = {
	"types" : [{
		"id"    : "line",
		"name"  : "Line Chart",
		"image" : "linec.png"
	}, {
		"id"    : "bar",
		"name"  : "Bar Chart",
		"image" : "barc.png"
	}, {
		"id"    : "pie",
		"name"  : "Pie Chart",
		"image" : "piec.png"
	}]
};

var GraphFactory = function(){};

GraphFactory.prototype = {
	createGraph : function(graphInfo, options) {
		console.log("Hello There!");
		var result = null;
		if (graphInfo.id="line") {
			result = new FlotPlotter(options);
		} else if (graphInfo.id="bar") {
//			console.log("BAR");
		} else if (graphInfo.id="pie") {
//			console.log("PIE");				
		}
		return result;
	}
};




///*
// * Graph Control Structure
// */
//var GraphFactory = function(metricInfo, graphInfo, container) {
//	this.metricInfo = metricInfo || null;
//	this.graphInfo = graphInfo || null;
//	
//	//Page Ids
//	this.controlId = container || null;	//in the form$(container)
//	
//	this.graph = null;
//	this.graphHeight = 400;
//	this.graphWidth = 600;
//	this.graphId = null;
//	this.gridId = null;
//	this.gridIndex = 0;
//	
//	this.$graphCanvas = null;
//	
//	//information for line graph
//	
//	this.lineData = [];
//	this.lineGraph = null;
//
//	this.graphController = null;
//	
//	
//	
//	//Public exposed methods
//	this.createCanvas = GraphControlStructure.prototype.createCanvas;
//
//	//Constructor
//	this._init();
//};



//GraphControlStructure.prototype = {
//	//Constructor
//	_init : function() {
//		
//	},
//	
//	initDrawingArea : function() {
//		try {
//			
//		} catch(err) {
//			
//		}
//	},
//	
//	
//	
//	createCanvas : function() {
//		var $html = null;
//		try {
//			var self = this;
//			$html = $("<canvas/>", {
//				id: self.graphId,
//				height: self.graphHeight,
//				width: self.graphWidth,
//			});
//			
//			console.info($html);
//		} catch(err) {
//			console.log(err);
//		}
//		return $html;
//	},
//	
//	initGraph : function(graphId) {
//		try {
//			 // "cache" this in a local variable
////	        var _RG = RGraph;
////	        RGraph.clear(this.graphId);
//
//			
//			//Initialize data, control and first draw
//			this.graphController.initGraphing();
//			
//		} catch(err) {
//			console.error(err);
//		}
//	},
	
// ------------------- LINE GRAPH DATA ------------------
//	prepareLineData : function() {
//		try {
//			this.lineData = [];
//			for(var i=0; i<this.MAX_LINE_POINTS; i++) {
//				this.lineData.push(null);
//			}
//		} catch(err) {
//			console.log("prepadData: " + err);
//		}
//	},
	
//	createLineGraph : function() {
//		var self = this;
//		
//		try {
//	        var obj = new RGraph.Line({
//	            id: this.graphId,			//Id of canvas
//	            data: self.lineData,
//	            options: {
//	                xticks: 100,
//	                background: {
//	                    color: 'white',
//	                    grid: {
//	                        vlines: false,
//	                        border: false
//	                    }
//	                },
//	                title: {
//	                    self: 'Bandwidth used',			//Title
//	                    vpos: 0.5,
//	                    xaxis: {
//	                        self: 'Last 30 seconds',	//x axis
//	                        pos: 0.5
//	                    }
//	                },
//	                colors: ['black'],
//	                linewidth: 0.5,
//	                yaxispos: 'right',
//	                ymax: 50,
//	                xticks: 25,
//	                filled: true,
//	                numyticks: 2,
//	                tickmarks: null,
//	                ylabels: {
//	                    count: 2
//	                }
//	            }
//	        });
//	        
//	        //Create a gradient fill
//	        var grad = obj.context.createLinearGradient(0,0,0,250);
//	        grad.addColorStop(0, '#efefef');
//	        grad.addColorStop(0.9, 'rgba(0,0,0,0)');
//
//	        obj.set('fillstyle', [grad]);
//		} catch(err) {
//			console.log("createLineGraph: " + err);
//		}
//
//        return obj;
//	},
	

	
	
//};

