!function($) {
	
	'use strict';
	
	/*
	 * Requires the following files
	 * GraphControlStructure.js		>		Graphing code
	 */	
	var BamMonitor = function(element, options, e) {
		
		if (e) {
			e.stopPropagation();
			e.preventDefault();
		}
		
		//public properties
		this.$element = $(element);
		this.$addButton = null;
		this.graphControls = [];
		
		//Exposed (to default) Properties
		this.bamServerUrl = options.bamServerUrl;
		this.addButtonId = options.addButtonId;
		this.addDialogTitle = options.addDialogTitle;
		this.gridComponentId = options.gridComponentId;
		
		//Expose public Methods
		this.simpleTest = BamMonitor.prototype.simpleTest;
		this.gridsterController = null;
		
		//Constructor
		this.init();
	};
	
	
	BamMonitor.prototype = {
		//Properties
		polling4Data : false,
		pollTimerID : null,
		pollDelay : 10000,
		pollLastMillisecond: 0,
		dialogRef : null,
		
		
		init : function() {
			var that = this,
				id = this.$element.attr("id");
			this.$addButton = $("#" + this.addButtonId);
			this.$addButton.on({
					"click": $.proxy(this.addClicked, this)
			});
			
			this.gridsterController = new GridsterController();		
		},
		
		initPoll4Data : function() {
			if (this.polling4Data) {
				var self = this;
				this.pollTimerID = window.setInterval( $.proxy(self.poll4Data, self), self.pollDelay );
			}
		},
		
		clearPoll4Data : function() {
			window.clearTimeout(this.pollTimerID);
			this.pollTimerID = null;
			this.polling4Data = false;
		},
		
		updateAllControlsWithNewData : function(data) {
			try {
				//Update self.pollLastMillisecond = 0;
				if ((data != null) && (data.data.length)) {
					for(var i=0; i<data.data.length; i++) {
						var mtr = data.data[i].mtr;
						var uid = data.data[i].uid;

						var foundIndex = -1;
						for(var j=0; j<this.graphControls.length; j++) {
							if ((this.graphControls[j].metric.uid == uid) && (this.graphControls[j].metric.metric == mtr)) {
								foundIndex = j;
								break;
							}
						}
						if (foundIndex >= 0 ) {
							//see flot-control.js / ...
							this.graphControls[j].updateData(data.data[i]);
						}
					}
				}
			} catch(err) {
				console.error(err);
			}
		},
		
		poll4Data : function() {
			var self = this;
			try {
				if ((!this.polling4Data) && (this.pollTimerID != null)) {
					this.clearPoll4Data();
				} else if ((this.polling4Data) && (this.pollTimerID == null)) {
					this.initPoll4Data();
				}
				this.sendAsyncAjax(self.bamServerUrl, "act=poll&ms=" + self.pollLastMillisecond, function(data) {
					try {
						self.updateAllControlsWithNewData(data);						
					} catch(err) {
						console.log(err);
					}
				});
				
			} catch(err) {
				console.error(err);
			}
		},
		

		
		simpleTest : function() {
			try {
//				var $html = $("<div><button id='aSimpleTest' class='btn btn-primary'><span>X</span></button></div>");
//				//$.proxy(self.metricClicked, self)
//				$html.bind("beforeShow", function() {
//					console.info("not shown yet");
//				}).bind("afterShow", $.proxy(this.testEventCalled, this)
//				).show(1000, function() {
//					console.info("timed show!");					
//				});
//				var $div = $("#content").removeClass("hide");
//				console.info("binding...");	
//				$("#content").append($html);
				
				
//				var $html = $("<div><button class='btn btn-primary'><span>X</span></button></div>");
//				var $stuff = this.gridsterController.addGridElement("fathom", $html);;
//				console.info($stuff);
//				var obj1 = Object.create(GraphControlStructure);
				
			} catch(err) {
				console.error(err);
			}
		},
		
		testEventCalled : function() {
			try {
				console.info("HI after shown");
			} catch(err) {
				console.error(err);
			}
		},
		
		btnPressed : function(e) {
			console.info("Hio there");
		},
		
// Properties
		addDialogRef : null,
		selectedMetric : null,
		selectedGraph : null,
		availableMetrics : null,
		
// Add Dialog Methods
		addClicked : function(e) {
			this.createAndShowAllModalWindow();
		},
		
		addDialogNextBackClicked : function(dialogRef) {
			alert('next');
		},
		
		addDialogFinishClicked : function(dialogRef) {
			alert("fin clicked");
		},
		
		createWaitMessage : function() {
			var msg= [];
			msg[msg.length] = '<div id="addContent">';
			msg[msg.length] = '<center><img src="/img/bigwait.gif" /></center>';
			msg[msg.length] = '</div>';
			return msg.join("");
		},
		
		sendAsyncAjax : function(url, postdata, callback, errorCallback) {
			var status = $.ajax({
				type: "POST", 
				url: url, 
				async: true,
				dataType: "json",
				data: postdata
			}).done(callback).fail(errorCallback);
		},
		
		createMetricInfo : function(metricInfo, col, index) {
			var name = metricInfo.metric;
			var service = metricInfo.serviceName;
			
			var html = [];
			var id = "metric_" + index;
			html[html.length] = "<div class='col-md-6'>";	//Outer column
			html[html.length] = "<div id='" + id + "' class='metricbox'>";	//Metric Holder
			
			html[html.length] = "<div>";
			html[html.length] = "<span class='metricbox-metricname'>" + metricInfo.metric + "</span>";
			html[html.length] = "<span class='metricbox-service'>" + metricInfo.serviceName + "</span>";
			html[html.length] = "</div>";
			
			var dims = [];
			if (metricInfo.dims == null || metricInfo.dims.length == 0) {
				dims[dims.length] = "NONE";
			} else {
				if (metricInfo.dims.length > 1) {
					for(var i=0; i<metricInfo.dims.length; i++) {
						if (i>0) dims[dims.length] = " | ";
						dims[dims.length] = metricInfo.dims[i].name;
						if (i > 2) {
							dims[dims.length] = " | ...";
							break;
						}
					}
				} else {
					dims[dims.length] = metricInfo.dims[0].name;
					dims[dims.length] = " (";
					dims[dims.length] = metricInfo.dims[0].units;
					dims[dims.length] = ")";
				}
			}
			
			html[html.length] = "<div class='metricbox-dimensions'>";
			html[html.length] = dims.join("");
			html[html.length] = "</div>";
			
			html[html.length] = "</div>";					//Metric Holder
			html[html.length] = "</div>";					//Outer column
			
			return html.join("");
		},
			
		createAvailableMetricsContent : function() {
			var html = [];
			
			html[html.length] = "<div class='metric-container'>";
			html[html.length] = "<div class='metric-heading'>";
			html[html.length] = "Choose metric to monitor";
			html[html.length] = "</div>";
			
			for(var i=0; i<this.availableMetrics.length; i++) {
				//console.log(i%2);  // 0 1 0 1 0 ...
				var metricBox = this.createMetricInfo(this.availableMetrics[i], i%2, i);
				
				if (i%2 == 0) {
					//first element
					html[html.length] = "<div class='row' style='margin: 1px solid #DDD'>";
					html[html.length] = metricBox;
				} else {
					//second element
					html[html.length] = metricBox;
					html[html.length] = "</div>";
				}
			}
			html[html.length] = "</div>";
//			html[html.length] = "<script>registerAddMetricSelectEvents('metric');</script>";
			
			return html.join("");
		},
		
		retrieveAvailableMetricsForDialog : function(dialogRef) {
			var self = this;
			self.dialogRef = dialogRef;
			
			if (self.availableMetrics == null) {
				this.sendAsyncAjax(this.bamServerUrl, "act=avail", function(data) {
					try {
						self.availableMetrics = data.metrics;
						self.updateAndDisplayAvailableMetrics();
					} catch(err) {
						console.log(err);
					}
				}, function(error) {
					console.error(error);
				});
			} else {
				console.log(self.availableMetrics);
				self.updateAndDisplayAvailableMetrics();
			}
		},
		
		//Create dialog content first page content
		updateAndDisplayAvailableMetrics : function() {
			var self = this;
			var $metricContent = $(self.createAvailableMetricsContent());
			self.dialogRef.getModalBody().html($metricContent);
			
			//Register Click event
			$metricContent.on("click", "div .metricbox", $.proxy(self.metricClicked, self) );
		},
		
		// Metric div called
		metricClicked : function(e) {
			try {
				e.preventDefault();
				var $elMetric = $(e.target).closest('.metricbox');
				var id = $elMetric.attr("id");
				var metricIndex = id.split("_")[1];
				this.selectedMetric = this.availableMetrics[metricIndex];
				this.updateDialogDisplayGraphTypes();
				this.addDialogRef.getButton("backbtn").removeClass("hide");
			} catch(err) {
				console.error(err);
			}
		},
		
		updateDialogDisplayGraphTypes : function() {
			var $html = $(this.creatAvailableGraphsHtml());
			$html.on('click', '.metric-graphtype', $.proxy(this.graphClicked, this));
			this.addDialogRef.getModalBody().html($html);
		},
			
		graphClicked : function(e) {
			try {
				e.preventDefault();
				var $elGraph = $(e.target).closest('.metric-graphtype');
				var id = $elGraph.attr("id");
				this.selectedGraph = this.findGraphType(id);
				this.addGraphAndPutOnPage(this.selectedMetric, this.selectedGraph);
				this.addDialogRef.close();
				this.addDialogRef = null;
			} catch(err) {
				console.error(err);
			}
		},
		
		findGraphType : function(id) {
			var result = null;
//			for (var i=0; i<this.GraphTypes.types.length; i++) {
			for (var i=0; i<GraphTypes.types.length; i++) {
				if (GraphTypes.types[i].id = id) {
					result = GraphTypes.types[i];
					break;
				}
			}
			return result;
		},
		
		
		addGraphAndPutOnPage : function(metric, selGraph) {
			try {
				
				//First create the outer container and embed in gridster
				var graphFactory = new GraphFactory();
				var len = this.graphControls.length;
				var graphId="graph_" + (len+1);	//This is the canvasId
				var gridId = "ctl_" + (len+1);
				var $html = this.gridsterController.addGridElement(gridId);
				var self = this;
				
				$html.show(500, function() {
					//Put graph on page
					var graph = graphFactory.createGraph(selGraph, {
						graphId : graphId,
						gridId : gridId,
						container : $html,
						width :  $(this).width(),
						height :  $(this).height(),
						graphType : selGraph.id,
						metric : metric
					});
					if (graph == null) {
						alert("No graph Type defined!");
					} else {
						$html.append(graph.createCanvas());
						graph.initGraph();
					}
					
					//Start polling for data
					self.registerMetricListener(graph);
				});
				
				//Register information
//				
				
				
				
//				console.log($html);
//				console.info($html.width);
//				console.info($html.height);
				
				//Only when create create the canvas
				
				//when canvas loaded then draw graph
				
//				
//				var $canvas = gobj.createCanvas2();	//Note that canvas is only used for rgraph
//				
//				
//				$("#" + gobj.graphId).show(2000, function() {
//					gobj.initGraph();
//				});
//					bind("afterShow", function() {
//					alert("sdf");
//					
//				}).show();
				
				
				//console.info($html);				
			} catch(err) {
				console.error(err);
			}
		},
			
		creatAvailableGraphsHtml : function() {
			var html = [];
			
			html[html.length] = "<div class='metric-container'>";
			html[html.length] = "<div class='metric-heading'>";
			html[html.length] = "Choose graph type";
			html[html.length] = "</div>";	//metric-heading
			html[html.length] = "<div class='row'>";
			for (var i=0; i<GraphTypes.types.length; i++) {
				html[html.length] = "<div class='col-xs-3 col-md-3'>";
				html[html.length] = "<a class='thumbnail metric-graphtype' id='";
				html[html.length] = GraphTypes.types[i].id;
				html[html.length] = "' href='#' class='thumbnail'>";
				html[html.length] = "<img src='/img/graphs/";
				html[html.length] = GraphTypes.types[i].image;
				html[html.length] = "' alt='"
				html[html.length] = GraphTypes.types[i].name;
				html[html.length] = "'>";
				html[html.length] = GraphTypes.types[i].name;
				html[html.length] = "</a>";
				html[html.length] = "</div>";
			}
			html[html.length] = "</div>";
			html[html.length] = "</div>";	//metric-container
			
			return html.join("");
		},
		
		createAndShowAllModalWindow : function() {
			var self = this;
			this.addDialogRef = new BootstrapDialog({
				title:   self.addDialogTitle,
				message: self.createWaitMessage(),
				type:    BootstrapDialog.TYPE_PRIMARY,
				closable: true,
				data: {
				},
				cssClass: 'metric-dialog',
				onshow: function(dialogRef) {
					self.retrieveAvailableMetricsForDialog(dialogRef);
				},
				buttons: [{
		        			id: 'backbtn',
				            label: "Back",
				            cssClass: 'btn-primary hide',
				            action: function(dialog) {
				                typeof dialog.getData('callback') === 'function' && dialog.getData('callback')(false);
				                self.backDialogButtonClicked();
				            }
				        },{
		        			id: 'cancelbtn',
				            label: "Cancel",
				            action: function(dialog) {
				                typeof dialog.getData('callback') === 'function' && dialog.getData('callback')(false);
				                dialog.close();
				            }
		    			}]
			}).open();
			
		},

		backDialogButtonClicked : function() {
			try {
				this.retrieveAvailableMetricsForDialog(this.addDialogRef);
				this.addDialogRef.getButton("backbtn").addClass("hide");
			} catch(err) {
				console.error(err);
			}
		},
		
		// Add to list of controls
		// Registering and polling server for data
		registerMetricListener : function(graphControl) {
			try {
				var self = this;
				
				console.info(graphControl);
				console.info(graphControl.metric.uid);
				console.info(graphControl.metric.metric);
				
				self.graphControls[self.graphControls.length] = graphControl;
				//var recursiveEncoded = $.param(graphControl.metricInfo.metric);
				var dataurl = "act=reg&uid=" + graphControl.metric.uid;
				dataurl += "&metric=" + graphControl.metric.metric;
				
				this.sendAsyncAjax(this.bamServerUrl, dataurl, function(data) {
					try {
						if (typeof data.status !== "undefined" && data.status != null) {
							//Start waiting for data (registration successful, poll for data)
							self.polling4Data = true;
							self.poll4Data();
						}
					} catch(err) {
						console.log("registerMetricListener: " + err);
					}
				}, function(error) {
					console.error(error);
				});
				
			} catch(err) {
				console.error(err);
			}
		}
		
	};
	
	$.fn.bamMonitor = function(option, event) {
		//get the args of the outer function..
		var args = arguments;
		var value;
		var result = null;
		var chain = this.each(function() {
			var $this = $(this),
				data = $this.data('bamMonitor'),
				options = typeof option == 'object' && option;
			
//				$.extend({}, $.fn.c4uprocessgrid.defaults, options, $(this).data());
				if (!data) {
					var mergedOptions = $.extend({}, $.fn.bamMonitor.defaults, options, $(this).data());
	                $this.data('bamMonitor', (data = new BamMonitor(this, mergedOptions, event)));
	            }
			
				result = data;
			var elementId = $(this).attr("id");
		});
		return result;
	};
	
	//Model defaults
	$.fn.bamMonitor.defaults = {
			addButtonId: null,
			addDialogTitle:  "Add new metric to monitor",
			addDialogNext:   "Next",
			addDialogBack:   "Back",
			addDialogFinish: "Finish",
			
			bamServerUrl: "/bammon"
	};
	

  // Override show method
  var _oldShow = $.fn.show;
  $.fn.show = function(speed, oldCallback) {
    return $(this).each(function() {
      var obj = $(this),
          newCallback = function() {
            if ($.isFunction(oldCallback)) {
              oldCallback.apply(obj);
            }
            obj.trigger('afterShow');
          };

      // you can trigger a before show if you want
      obj.trigger('beforeShow');

      // now use the old function to show the element passing the new callback
      _oldShow.apply(obj, [speed, newCallback]);
    });
  }
}(jQuery, window, document);