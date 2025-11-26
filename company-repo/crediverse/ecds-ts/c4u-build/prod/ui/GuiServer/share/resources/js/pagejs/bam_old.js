//Global information
var tpsData = [];			//Raw data
var plotBuffer = [];		//Ready to be plotted

var data = [],
totalPoints = 300;
var updateInterval = 1000;
var plot = null;
var maxYScale = 350;

//Leave for now!
loadBamInformation = function() {
	try {
		var url = "/bammon";
		var postData = "act=metrics";
		sendAsyncAjax(url, postData, function(data) {
			
		},
		function(error) {
			
		});
	} catch(err) {
		console.error("loadBamInformation: " + err);
	}
};

//Set up the control widget
setupTPSGraph = function() {
	$("#loadingBam").addClass("hide");
	$("#content").removeClass("hide");
	
	var container = $("#placeholder");
	
	plot = $.plot("#placeholder", [ getRandomData() ], {
		label: "transactions/s",
		grid: {
			borderWidth: 1,
			minBorderMargin: 20,
			labelMargin: 10,
//			hoverable: true,
			backgroundColor: {
				colors: ["#fff", "#e4f4f4"]
			},
			margin: {
				top: 8,
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
		series: {
			shadowSize: 1	// Drawing is faster without shadows
		},
		yaxis: {
			min: 0,
			max: maxYScale
		},
		xaxis: {
			show: false
		}
	});
	
	var yaxisLabel = $("<span class='axisLabel yaxisLabel'></span>")
	.text("Transactions/s")
	.appendTo(container);
	yaxisLabel.css("margin-top", 10);
};

initializeTPSData = function() {
	for (var i=50; i<totalPoints; i++) {
		plotBuffer.push([ i, 0 ]);
	}
};

updateTPS = function() {
	getData("TPS", function(data) {
		//Update local buffer
		for (var i=0; i<data.data.length; i++) {
			tpsData.push([ data.data[i][0], data.data[i][1] ]);
		}
		
		//Update buffer to be drawn
		var res = [];
		for(var i=0; i<tpsData.length; i++) {
			res.push([(totalPoints-tpsData.length+i), tpsData[i][1]])
		}
//		console.log(res);
		plot.setData([res]);
		plot.draw();
		setTimeout(updateTPS, updateInterval);
	});
};

getData = function(metric, callback) {
	try {
		var url = "/bammon";
		var postData = "act=data&metric=" + metric;
		sendAsyncAjax(url, postData, callback,
		function(error) {
			console.error("bam issues: " + error);
		});
	} catch(err) {
		console.error("loadBamInformation: " + err);
	}
};



//updateTPS = function() {
//
//	plot.setData([getRandomData()]);
//
//	// Since the axes don't change, we don't need to call plot.setupGrid()
//
//	plot.draw();
//	setTimeout(update, updateInterval);
//};

getMetrics = function() {
	 
};





















///////////////////

updataGraphData = function() {
	
};

getRandomData = function() {
	if (data.length > 0)
		data = data.slice(1);

	// Do a random walk

	while (data.length < totalPoints) {

		var prev = data.length > 0 ? data[data.length - 1] : 50,
			y = prev + Math.random() * 10 - 5;

		if (y < 0) {
			y = 0;
		} else if (y > 100) {
			y = 100;
		}

		data.push(y);
	}

	// Zip the generated y values with the x values

	var res = [];
	for (var i = 0; i < data.length; ++i) {
		res.push([i, data[i]])
	}

//	console.log(res);
	return res;
};

$(function(){
	initializeTPSData();
	setupTPSGraph();
	updateTPS();	
});

